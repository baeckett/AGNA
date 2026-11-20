# AGNA 2.2.2 release package

- The folder one level up is the complete source tree (build: `mvn -B -ntp install`, JDK 17).
- `AGNA-2.2.2-vs-v2.2.1.patch.txt`: the full diff against the v2.2.1 tag (rename to `.patch` to apply with `git apply`).
- `ERRATUM_2.2.1.md` and `CHANGELOG.md`: what was wrong in 2.2.1 and what 2.2.2 changes.
- `verification-evidence/`: unit test totals, differential-test classification, NetworkX parity reports, robustness-battery summary.
  The scripts that produced them are in `tools/audit` and `tools/nx-parity`.
