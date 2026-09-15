# Agna 2.1.3 — Release notes

Open-source revival of Agna (last closed-source release: 2.1.2), restructured
into a Maven monorepo.
Copyright (C) 2001–2026 Marius Ion Bența.
Website: https://www.netanalysis.co.uk — contact@netanalysis.co.uk

## What is AGNA

AGNA — Applied Graph and Network Analysis Open Source — is free software for
social network analysis, sociometry, and sequential analysis: a desktop
application, a command-line interface, and a reusable Java engine, all under
the Apache License 2.0.

## Layout (monorepo)

- `agna-core` — network engine: networks, analyses, layouts, import/export
- `agna-cli` — command-line interface (30+ commands: analyse, convert,
  transform, draw, generate, matrix, metrics, distance, diff, layout, ...)
- `agna-desktop` — the Swing desktop application
- `docs/`, `packaging/`, `samples/`, `LICENSE`, `CITATION.cff`/`CITATION.bib`

## New features in 2.1.3

- **New layout engine** — three algorithms: **grid**, **concentric**
  (hub-centered by degree), and **force-directed spring embedding**
  (Fruchterman–Reingold with Barnes–Hut acceleration for large networks).
  Available in the desktop Image menu and toolbar, and from the CLI
  (`layout`, `draw --layout`).
- **AGNA CLI** — a full command surface (30+ commands) for headless,
  reproducible analysis and diagram generation, with pipe support,
  structured output (CSV/JSON), man page and shell completions.
- **Analysis battery** — Full Analysis, scalable N-Cliques, prestige
  (Lin 1976), Brandes betweenness, distance and structural-diff suites.
- **Import/export** — agn, text/CSV, Pajek `.net` (Gephi-safe), Excel
  (`.xls`/`.xlsx`, incl. Apple Numbers), GraphML, GML, GraphSON.
- **Merge Network** (by name; sum/max/keep policies); session log in the
  Output pane; background analyses with cancellation.
- **Modern UI** — FlatLaf look and feel (light default, live theme picker,
  classic fallback); node-face fallback; resizable modern Help with
  formulas, links and the citation guide.
- **UTF-8 text I/O** throughout, with a legacy latin-1 read fallback for
  old files.
- **Quality** — 230 automated tests (138 core, 42 CLI, 50 desktop):
  fuzz, stress, performance, rendering, round-trips and unicode suites.

## Bugs fixed in 2.1.3

- Save As dialog reliability: self-healing default name, format filters,
  directory/path guards, extension handling.
- `.agn` reader: crash on files without a Background Image File setting;
  optional-settings NPEs; spurious "Node" parse warnings.
- Settings: the title vertical offset is restored correctly; no crash when
  an existing `~/.agna` settings file is present.
- Pajek export is Gephi-clean: no comment header, optional vector blocks,
  coordinates normalized to 0..1, face round-trip.
- Grid lines are now drawn unconditionally, visible under native look and
  feels (macOS Aqua) that suppress them.
- Spring layout: gentle displacement and fit-to-canvas normalization (no
  border hugging); Barnes–Hut acceleration above 256 nodes (~12x faster on
  1000–5000 node networks, same layout quality).
- Path helpers no longer truncate at dots inside directory names; Excel
  export is safe for headless runs.
- Binary-degree semantics documented; the multiple-geodesics stub replaced;
  clique enumeration capped with a truncation note; analyses land safely
  on the event thread with cancellation.
- Help: resizable window, regenerated contents with working anchors, stale
  pages removed; legacy commented-out code removed from the sources.
- Engine hardening: matrix APIs validate shape before computing (no silent
  ArrayIndexOutOfBounds); integer matrix multiplication accumulates in
  `long` and fails loudly on overflow; non-finite (NaN/infinity) tie values
  are rejected at the transformation entry points; clique reports state
  when enumeration was truncated or cancelled; all-shortest-path
  enumeration is capped; metamorphic invariance tests (permutation,
  transposition, symmetrization) added.

## Citation

DOI: 10.5281/zenodo.22708199 — see CITATION.cff / CITATION.bib

## Files

- `AGNA-2.1.3-desktop.jar` — run: `java -jar AGNA-2.1.3-desktop.jar`
- `AGNA-2.1.3-cli.zip` — CLI jar + reference + man page + completions
- `AGNA-2.1.3-core.jar` (+ sources jar)
- `AGNA-2.1.3-Linux-x64.tar.gz` — self-contained Linux archive
- macOS (.dmg) and Windows installers are built on their platforms from
  the recipes in `packaging/`

## License

Apache License 2.0 (all modules). See LICENSE and the README (including the
High-Risk Activities Disclaimer).

## Software bills of materials

- `docs/sbom/AGNA-2.1.3-SBOM.json` — CycloneDX 1.4 aggregate SBOM.
- `docs/sbom/AGNA-2.1.3-SPDX.json` — SPDX 2.2 variant (ISO/IEC 5962),
  converted from the same dependency graph.
### SBOM diff for this release

| | First BOM (audit baseline) | Final BOM |
|---|---|---|
| Format | CycloneDX 1.4 | CycloneDX 1.4 + SPDX 2.2 |
| Components | 9 | 14 (all Apache License 2.0) |
| Non-permissive licenses | LGPL (JExcelAPI/jxl 2.6.12) | none |
| Known-vulnerable EOL component | log4j 1.2.14 (transitive of jxl) | none (log4j-api 2.23.1, Apache-2.0) |
| Excel `.xls` stack | JExcelAPI (LGPL) | Apache POI 5.3.0 (with commons-codec,
  commons-collections4, commons-math3, commons-io, SparseBitSet) |

The `sbom-gate` CI job now enforces the Apache-2.0-only policy; the gate
allowlist contains no other license.

- Both are attached on the release page; the `sbom-gate` CI job fails the
  build when a component's license is not allowlisted or a banned/
  SNAPSHOT component appears. All runtime dependencies are Apache License
  2.0: the Excel `.xls` path moved from JExcelAPI (LGPL) to Apache POI,
  which also removed the end-of-life log4j 1.x transitive. The optional
  OWASP NVD scan runs locally with `mvn -Psbom-gate verify
  -Ddependency-check.skip=false`.