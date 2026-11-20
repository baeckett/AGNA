#!/usr/bin/env python3
"""Agna CLI contract & robustness battery.

Runs every analysis command against well-formed samples, edge-case
fixtures and malformed files. Records exit code, wall time, JSON validity,
leaked stack traces and NaN/Infinity tokens. Usage:
    python cli_battery.py AGNA_JAR SAMPLES_DIR FIXTURE_DIR OUT_JSON
"""
import json, os, subprocess, sys, time, glob, re, tempfile
from concurrent.futures import ThreadPoolExecutor

JAR, SAMPLES, FX, OUT = sys.argv[1:5]
JAVA = os.environ.get("JAVA", "java")
TMP = tempfile.mkdtemp(prefix="agna_bat_")

VALID = [os.path.join(SAMPLES, f) for f in (
    "example2.agn", "Zachary Karate Club.agn", "Davis Southern Women.agn",
    "Padgett Florentine Families.agn", "10 star.agn", "random200.agn")]
EDGE = sorted(glob.glob(os.path.join(FX, "*")))
MALFORMED = {"empty.csv", "empty.agn", "garbage.agn", "nonsquare.csv", "ragged.csv",
             "text_cells.csv", "bad.net", "bad.graphml", "xxe.graphml",
             "truncated.agn", "nan_cell.csv", "dup_names.csv"}

def cmds(f):
    o = os.path.join(TMP, os.path.basename(f) + ".out")
    return {
        "info": ["info", f, "--json"],
        "validate": ["validate", f, "--json"],
        "advise": ["advise", f, "--json"],
        "narrative": ["narrative", f, "--json"],
        "archetype": ["archetype", f, "--format", "json"],
        "metrics_all": ["metrics", f, "--all", "--json"],
        "metrics_betw": ["metrics", f, "betweenness", "--json"],
        "dimension": ["dimension", f, "--json", "--seed", "1"],
        "backbone": ["backbone", f, "--json"],
        "brokerage": ["brokerage", f, "--reps", "50", "--seed", "1", "--json"],
        "motifs": ["motifs", f, "--reps", "20", "--seed", "1", "--json"],
        "suspects": ["suspects", f, "--json"],
        "stability": ["stability", f, "--reps", "50", "--json"],
        "robustness": ["robustness", f, "--reps", "5", "--steps", "5", "--json"],
        "test_null": ["test-null", f, "-r", "50", "--seed", "1", "--format", "json"],
        "partition": ["partition", f, "--louvain", "--json"],
        "components": ["components", f],
        "matrix_csv": ["matrix", f, "--format", "csv"],
        "analyse_full": ["analyse", f, "--all"],
        "convert_net": ["convert", f, o + ".net"],
        "convert_graphml": ["convert", f, o + ".graphml"],
        "draw_png": ["draw", f, "--out", o + ".png", "--layout", "spring"],
        "report": ["report", f, "--out", o + ".html"],
    }

def run(case):
    kind, f, name, args = case
    t = time.time()
    try:
        p = subprocess.run([JAVA, "-Xmx512m", "-Djava.awt.headless=true", "-jar", JAR] + args,
                           capture_output=True, text=True, timeout=90)
        rc, so, se = p.returncode, p.stdout, p.stderr
    except subprocess.TimeoutExpired:
        rc, so, se = "TIMEOUT", "", ""
    dt = round(time.time() - t, 2)
    wants_json = "--json" in args or "json" in args
    js_ok = None
    if wants_json and rc == 0:
        try:
            json.loads(so); js_ok = True
        except Exception:
            # some commands print a note line before JSON
            i = so.find("{")
            try:
                json.loads(so[i:]) if i >= 0 else 1 / 0
                js_ok = "prefixed"
            except Exception:
                js_ok = False
    trace = bool(re.search(r"\n\s+at (com\.bentza|java\.)", so + se)) or \
        bool(re.search(r"(NullPointer|ArrayIndexOutOfBounds|NumberFormat|ClassCast|IllegalState|NegativeArraySize)Exception", so + se))
    nan = bool(re.search(r"\b(NaN|-?Infinity)\b", so)) if wants_json else False
    return dict(kind=kind, file=os.path.basename(f), cmd=name, rc=rc, sec=dt, json=js_ok,
                trace=trace, nan=nan, err=(se.strip().splitlines() or [""])[-1][:240],
                out_head=so[:160])

cases = []
for f in VALID:
    for n, a in cmds(f).items(): cases.append(("valid", f, n, a))
for f in EDGE:
    k = "malformed" if os.path.basename(f) in MALFORMED else "edge"
    for n, a in cmds(f).items(): cases.append((k, f, n, a))

with ThreadPoolExecutor(int(os.environ.get("PAR", "2"))) as ex:
    res = list(ex.map(run, cases))
json.dump(res, open(OUT, "w"), indent=1, ensure_ascii=False)
print("cases", len(res))
