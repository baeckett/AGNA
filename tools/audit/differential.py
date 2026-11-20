#!/usr/bin/env python3
"""Differential regression harness for backports.

Runs the same commands on the same valid inputs with two jars (a baseline and a candidate) and groups every
difference in stdout, exit code and written files, so each change in behaviour can be reviewed and either
allow-listed (an intended fix) or treated as a regression.

    python differential.py run   JAR OUT.json SAMPLES_DIR FIXTURE_DIR
    python differential.py diff  BASE.json CAND.json [--show N]
"""
import glob, hashlib, json, os, re, subprocess, sys, tempfile
from concurrent.futures import ThreadPoolExecutor

JAVA = os.environ.get("JAVA", "java")
MEASURES = ["basic", "density", "cohesion", "nodal", "indegree", "outdegree", "emission", "reception",
            "determination", "status", "geodesics", "open-chain", "eccentricity", "diameter", "bavelas",
            "closeness", "fareness", "betweenness", "prestige", "cliques", "clustering", "transitivity",
            "kcore", "triads", "holes", "components", "eigenvector", "bonacich", "pagerank",
            "assortativity", "community", "equivalence", "weighted", "full"]
VALID_FIXTURES = ["unicode_ok.csv", "isolates5.csv", "directed_chain.csv", "dyad.csv", "one_node.csv",
                  "selfloops.csv", "negative_weights.csv", "disconnected.agn", "two_isolates.csv",
                  "huge_weight.csv", "unicode_names.csv"]


def commands(f, tmp):
    base = os.path.basename(f)
    c = {}
    c["info"] = ["info", f]
    c["info_json"] = ["info", f, "--json"]
    c["validate"] = ["validate", f]
    c["validate_json"] = ["validate", f, "--json"]
    for m in MEASURES:
        c["analyse_" + m] = ["analyse", f, m]
    c["metrics_csv"] = ["metrics", f, "--all"]
    c["metrics_json"] = ["metrics", f, "--all", "--json"]
    c["metrics_deg"] = ["metrics", f, "indegree", "outdegree", "--json"]
    c["advise"] = ["advise", f, "--json"]
    c["narrative"] = ["narrative", f]
    c["archetype"] = ["archetype", f, "--format", "json"]
    c["components"] = ["components", f]
    c["nodes"] = ["nodes", f]
    c["ego"] = ["ego", f, "--node", "1", "--out", os.path.join(tmp, base + ".ego.agn")]
    c["matrix_csv"] = ["matrix", f, "--format", "csv"]
    c["matrix_txt"] = ["matrix", f]
    c["explain"] = ["explain", "closeness"]
    for fmt in ("csv", "agn", "txt", "gml", "net", "graphml", "dl"):
        o = os.path.join(tmp, base + "." + fmt)
        c["convert_" + fmt] = ["convert", f, o]
    for op in ("transpose", "symmetrize-max", "symmetrize-sum", "symmetrize-union"):
        o = os.path.join(tmp, base + "." + op + ".agn")
        c["transform_" + op] = ["transform", f, o, "--op", op]
    for cmd_name in ("transpose", "symmetrize", "normalize", "square"):
        o = os.path.join(tmp, base + "." + cmd_name + ".out.agn")
        c["cmd_" + cmd_name] = [cmd_name, f, "--out", o]
    return c


def norm(s):
    s = re.sub(r"\(Version [0-9.]+\)", "(Version X)", s)
    s = re.sub(r"AGNA CLI [0-9.]+", "AGNA CLI X", s)
    s = re.sub(r'"version"\s*:\s*"[0-9.]+"', '"version": "X"', s)
    s = re.sub(r"/tmp/[A-Za-z0-9_./-]+", "/tmp/P", s)
    s = re.sub(r"Version\t[0-9.]+", "Version\tX", s)
    return s


def run_one(args):
    jar, key, argv, outfile = args
    try:
        p = subprocess.run([JAVA, "-jar", jar] + argv, capture_output=True, timeout=120)
        out = p.stdout.decode("utf-8", "replace")
        err = p.stderr.decode("utf-8", "replace")
        code = p.returncode
    except subprocess.TimeoutExpired:
        out, err, code = "", "TIMEOUT", -9
    fh = None
    if outfile and os.path.exists(outfile):
        with open(outfile, "rb") as fp:
            fh = hashlib.sha256(norm(fp.read().decode("utf-8", "replace")).encode()).hexdigest()[:16]
        os.remove(outfile)
    out = norm(out)
    if key.endswith("::nodes"):
        # layout coordinates (last two columns) are random per run for files without stored coordinates
        out = "\n".join("\t".join(l.split("\t")[:4]) for l in out.split("\n"))
    return key, {"code": code, "out": out, "err": norm(err)[:400], "file": fh}


def do_run(jar, out_json, samples, fx):
    inputs = sorted(glob.glob(os.path.join(samples, "*.agn")))
    inputs += [os.path.join(fx, f) for f in VALID_FIXTURES if os.path.exists(os.path.join(fx, f))]
    jobs = []
    tmp = tempfile.mkdtemp(prefix="agna_diff_")
    for f in inputs:
        d = os.path.join(tmp, hashlib.md5(f.encode()).hexdigest()[:6])
        os.makedirs(d, exist_ok=True)
        only = [x for x in os.environ.get("DIFF_ONLY", "").split(",") if x]
        for k, argv in commands(f, d).items():
            if only and not any(k.startswith(x) for x in only):
                continue
            outfile = next((a for a in argv if a.startswith(d + os.sep)), None)
            jobs.append((jar, os.path.basename(f) + "::" + k, argv, outfile))
    with ThreadPoolExecutor(max_workers=2) as ex:
        res = dict(ex.map(run_one, jobs))
    json.dump(res, open(out_json, "w"))
    print("ran", len(res), "commands on", len(inputs), "inputs")


def do_diff(base_json, cand_json, show):
    a, b = json.load(open(base_json)), json.load(open(cand_json))
    uuid = re.compile(r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    for d in (a, b):
        for v in d.values():
            v["out"] = uuid.sub("UUID", v["out"])
    groups = {}
    same = 0
    for k in sorted(a):
        ra, rb = a[k], b.get(k)
        if rb is None:
            continue
        if ra["code"] == rb["code"] and ra["out"] == rb["out"] and ra["file"] == rb["file"]:
            same += 1
            continue
        cmd = k.split("::")[1]
        groups.setdefault(cmd, []).append(k)
    print("identical: %d of %d" % (same, len(a)))
    print("different commands: %d" % len(groups))
    for cmd, keys in sorted(groups.items()):
        k = keys[0]
        ra, rb = a[k], b[k]
        print("\n== %-22s %d input(s) differ, e.g. %s" % (cmd, len(keys), k.split("::")[0]))
        print("   exit %s -> %s" % (ra["code"], rb["code"]))
        if show:
            ia, ib = ra["out"].split("\n"), rb["out"].split("\n")
            n = 0
            for x, y in zip(ia, ib):
                if x != y:
                    print("   - " + x[:130])
                    print("   + " + y[:130])
                    n += 1
                    if n >= show:
                        break
            if len(ia) != len(ib):
                print("   (lines %d -> %d)" % (len(ia), len(ib)))
            if ra["file"] != rb["file"]:
                print("   written file hash %s -> %s" % (ra["file"], rb["file"]))


if __name__ == "__main__":
    if sys.argv[1] == "run":
        do_run(*sys.argv[2:6])
    else:
        show = int(sys.argv[sys.argv.index("--show") + 1]) if "--show" in sys.argv else 2
        do_diff(sys.argv[2], sys.argv[3], show)
