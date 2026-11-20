# Verification tooling

Scripts used to verify the 2.2.2 correction release. They only need Java 17 and Python 3 (NetworkX for the parity check).

| Script | Purpose |
|---|---|
| `differential.py` | Runs about 70 commands on 21 inputs with two jars and records stdout, exit codes and written files. `python differential.py run JAR OUT.json SAMPLES_DIR FIXTURE_DIR`; `python differential.py diff BASE.json CAND.json` groups the differences. |
| `classify_differences.py` | Assigns every difference to a reviewed category and exits 1 on an unexplained change. |
| `cli_battery.py` | Contract and robustness battery (exit codes, JSON validity, stack traces, hostile files). `python cli_battery.py JAR SAMPLES_DIR fixtures OUT.json` |
| `fixtures/` | Edge-case and malformed input files. |
| `../nx-parity/` | NetworkX parity: `AgnaMetricsDump.java` writes every index as JSON Lines, `compare.py` recomputes them with NetworkX. |

Typical backport check: run `differential.py run` for the baseline jar and for the candidate, then `classify_differences.py base.json cand.json`.
