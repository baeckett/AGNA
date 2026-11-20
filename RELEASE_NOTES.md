# AGNA 2.2.2 — Release Notes (Hotfix / Correction Release for 2.2.1)

**Release Date:** 2026-11-20  
**License:** Apache License 2.0  
**Author:** Marius Ion Bența (George Barițiu History Institute, Cluj)  
**Website:** https://www.netanalysis.co.uk  
**Zenodo DOI (all versions, always the latest):** https://doi.org/10.5281/zenodo.22708198

---

## Overview

**AGNA 2.2.2** is a correction release for **AGNA 2.2.1**. It fixes two mathematical defects discovered in the post-2.2.1 verification audit—most importantly a depth-first traversal bug in `agna metrics` and `agna distance` that produced non-minimal geodesic distances, diameters, closeness, and betweenness values on cyclic networks—along with input integrity hardening, report completeness fixes, and CLI/MCP contract compliance.

No new features are added in 2.2.2; see [ERRATUM_2.2.1.md](ERRATUM_2.2.1.md) and [CHANGELOG.md](CHANGELOG.md) for full technical details.

---

## Critical Fixes (Erratum for 2.2.1)

1. **True Breadth-First Search (BFS) Geodesic Distances in `agna metrics`, `agna_metrics` (MCP), and `agna distance --out`:**
   - In 2.2.1, path traversal in `agna metrics` used a LIFO stack (depth-first) instead of a FIFO queue (breadth-first), causing shortest-path lengths to be overestimated on cyclic networks (affecting `diameter`, `eccentricity`, `closeness`, `betweenness`, and `geodesics` rows in `agna metrics`).
   - *Note:* The AGNA Desktop application reports, `agna analyse`, and all degree-based measures were **not** affected.
   - Verified against NetworkX across all bundled empirical samples and 65 benchmark graphs:
     - **Zachary Karate Club:** Diameter `5` (was `6` in 2.2.1)
     - **Padgett Florentine Families:** Diameter `5` (was `7` in 2.2.1)
     - **random200:** Diameter `4` (was `53` in 2.2.1)
2. **Wasserman–Faust Bounded Closeness on Disconnected Networks:**
   - Standardized closeness on networks that are not strongly connected now applies the Wasserman–Faust reachable-component scaling factor so values strictly remain within $[0, 1]$ across Desktop, `agna analyse`, and `agna metrics` (and remain identical on connected networks).
   - `agna metrics` omits `diameter` and `eccentricity` on disconnected networks where they are mathematically undefined.
3. **Complete Two-Phase Louvain Modularity & Exact Bonacich Power Centrality:**
   - Louvain community detection now runs both Phase 1 (local moving) and Phase 2 (community aggregation), increasing modularity on Zachary Karate Club from $Q = 0.361$ to $Q = 0.416$.
   - Bonacich power centrality $c(\alpha, \beta)$ is solved exactly via Gaussian elimination with partial pivoting and validates $|\beta| < 1/\lambda_{\max}$; eigenvector centrality uses a converged Lanczos/power solver.

---

## Additional Fixes in 2.2.2

- **Drawing Hang on Header-Only / Generated Networks (`Has Viewer no`):** Clamped grid spacing so `agna draw` and image export never hang in an infinite grid loop when `Grid Space` is `0`.
- **RFC 4180 Quoted CSV & TSV Reader:** Actor names containing commas or escaped quotes (e.g., `"José ""Pepe"", Jr."`) are parsed without splitting or losing actors.
- **Pre-Flight Input Integrity Validation:** Corrupt files (binary data, non-square or ragged CSVs, `NaN`/text cells, Pajek ties to missing vertices, headerless `.agn` files) are refused with **exit code `2`** and structured diagnostics in `agna validate --json` (override with `--lenient`).
- **Complete CLI `agna analyse` Text Reports:** `clustering`, `transitivity`, `kcore`, `triads`, `holes`, `components`, `eigenvector`, `bonacich`, and `pagerank` now print full ASCII data tables instead of title-only headers.
- **Installers ship exactly the 2.2.1 sample set:** the ten sample networks embedded in the jar (listed in `packaging/samples-manifest.txt`). Repository test fixtures (`Chain1.txt`, `Chain2.txt`, `4 full.txt`) are no longer bundled; the build scripts and CI workflows stage the installer input from the manifest, and a unit test guards it.
- **CLI & MCP Contract Hardening:**
  - `agna generate <type> --nodes N` accepts network type positionally and rejects unknown types with exit code `1`.
  - `agna matrix --format csv` writes a leading empty corner cell so exported CSV matrices re-import cleanly.
  - Standard exit codes enforced (`0` success, `1` usage error, `2` data validation error, `3` computation error).
  - `agna metrics --json` writes diagnostic notes exclusively to `stderr` so `stdout` is always pure JSON, and includes a `conventions.betweenness` field documenting ordered-pair summation.
  - `agna mcp` responds to malformed JSON lines with JSON-RPC `-32700` and batch arrays with `-32600`.

---

## Verification Summary

- **Unit Test Suite:** 420 tests (273 core, 80 CLI, 67 desktop) — 0 failures, 0 errors.
- **NetworkX Parity Battery (`tools/nx-parity`):** 65 networks, 3,360 checks — 3,353 PASS, 0 FAIL (down from 68 failures in 2.2.1).
- **Differential Audit (`tools/audit/differential.py`):** 1,365 command runs comparing 2.2.1 vs. 2.2.2 — 0 unexplained differences.
- **CLI Robustness Battery (`tools/audit/cli_battery.py`):** 667 runs — 0 timeouts, 0 stack traces, 0 prefixed/broken JSON payloads.

---

## Release Files

- `AGNA-2.2.2.dmg` — macOS Universal / Apple Silicon installer with bundled Java runtime (Drag to `/Applications`)
- `AGNA-2.2.2-desktop.jar` — Cross-platform executable desktop JAR (`java -jar AGNA-2.2.2-desktop.jar`, Java 17+)
- `AGNA-2.2.2-cli.jar` — Standalone command-line & MCP server JAR (`java -jar AGNA-2.2.2-cli.jar --help`)
- `AGNA-2.2.2-cli.zip` — CLI bundle (executable JAR, CLI reference manual, man page `agna.1`, bash/zsh completions)
- `AGNA-2.2.2-core.jar` & `AGNA-2.2.2-core-sources.jar` — Reusable Java SNA engine library and sources
- `AGNA-2.2.2-Linux-x64.tar.gz` — Portable Linux archive
- `AGNA-2.2.2-source-zenodo.zip` — Complete source code, documentation, verification tools, and packaging recipes for Zenodo archival
- `AGNA-2.2.2-SBOM.json` & `AGNA-2.2.2-SPDX.json` — CycloneDX 1.4 and SPDX 2.2 Software Bills of Materials
- `ERRATUM_2.2.1.md` & `SHA256SUMS.txt` — Erratum notice and SHA-256 cryptographic checksums
