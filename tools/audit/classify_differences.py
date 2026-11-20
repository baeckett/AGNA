#!/usr/bin/env python3
"""Classify every difference between two differential.py result files into reviewed categories.

    python classify_differences.py BASE.json CAND.json

Exit code 1 when a difference falls outside the categories below (an unexplained behaviour change).
Categories describe the fixes of the 2.2.2 correction release; adapt them for later backports.
"""
import json, re, sys, collections

a, b = json.load(open(sys.argv[1])), json.load(open(sys.argv[2]))
uuid = re.compile(r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
for d in (a, b):
    for v in d.values():
        v["out"] = uuid.sub("UUID", v["out"])
def strip_note(o):
    """Remove the 2.2.2 betweenness-convention note (text line, JSON field, stderr-only for CSV) before comparing."""
    out, skip = [], 0
    for l in o.split("\n"):
        if skip:
            skip -= 1
            continue
        if '"conventions"' in l:
            skip = 2          # the field line and the closing brace
            continue
        if "summed over ordered pairs" in l:
            continue
        out.append(l)
    return "\n".join(out).replace('",\n  }', '"\n  }')


for v in b.values():
    v["out"] = strip_note(v["out"])
UNI = {"unicode_names.csv", "unicode_ok.csv"}   # quoted-name CSV: 2.2.1 lost actors, 2.2.2 reads RFC 4180
FIXED_EMPTY = {"analyse_bonacich", "analyse_clustering", "analyse_components", "analyse_eigenvector", "analyse_holes",
               "analyse_kcore", "analyse_pagerank", "analyse_transitivity", "analyse_triads"}


def num_lines(t):
    return sum(1 for l in t.split("\n") if re.search(r"\d", l) and not l.startswith("Cite") and "doi.org" not in l and "Version" not in l)


res, problems = collections.defaultdict(list), []
for k in sorted(a):
    ra, rb = a[k], b[k]
    inp, cmd = k.split("::")
    if ra["code"] == rb["code"] and ra["out"] == rb["out"] and ra["file"] == rb["file"]:
        continue
    if inp in UNI:
        res["unicode-csv"].append(k)
    elif cmd in FIXED_EMPTY:
        ok = rb["code"] == 0 and ra["code"] == 0 and num_lines(rb["out"]) > num_lines(ra["out"])
        (res["empty-report-fixed"] if ok else problems).append(k if ok else (k, "empty report"))
    elif cmd == "analyse_betweenness" and ra["code"] == rb["code"] and \
            [l for l in rb["out"].split("\n") if "summed over ordered pairs" not in l] == ra["out"].split("\n"):
        res["betweenness-convention-note"].append(k)   # only the new footnote line differs
    elif cmd in ("analyse_closeness", "analyse_full"):
        res["closeness-bounded"].append(k)
    elif cmd == "analyse_community":
        res["louvain"].append(k)
    elif cmd in ("metrics_csv", "metrics_json"):
        res["metrics-distances"].append(k)
    elif cmd in ("validate", "validate_json"):
        res["validate-additive"].append(k)
    elif cmd in ("matrix_csv", "matrix_txt"):
        if ra["out"].split("\n")[1:] != rb["out"].split("\n")[1:]:
            problems.append((k, "matrix body changed"))
        else:
            res["matrix-header"].append(k)
    else:
        problems.append((k, cmd))
for c, v in res.items():
    print("%-22s %d" % (c, len(v)))
print("unexplained:", len(problems))
for p in problems[:20]:
    print("  ", p)
sys.exit(1 if problems else 0)
