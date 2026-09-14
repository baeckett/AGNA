# Agna 2.1.3 — Release notes

Open-source revival of Agna (last closed-source release: 2.1.2), restructured
into a Maven monorepo.
Copyright (C) 2001–2026 Marius Ion Bența.
Website: https://www.netanalysis.co.uk — contact@netanalysis.co.uk

## Layout (monorepo)

- `agna-core` — network engine: analyses, layouts, import/export
- `agna-cli` — command-line interface (30+ commands: analyse, convert,
  transform, draw, matrix, metrics, distance, diff, generate, ...)
- `agna-desktop` — the Swing desktop application
- `docs/`, `packaging/`, `samples/`, `LICENSE`, `CITATION.cff`/`CITATION.bib`

## Features

- Modern build: Maven/JDK 17, FlatLaf UI (light default, live look-and-feel
  picker, classic-theme fallback), 230 tests (138 core, 42 CLI, 50 desktop)
- Analysis battery: Full Analysis, scalable N-Cliques, prestige (Lin 1976),
  Brandes betweenness, distance/diff suites
- Import/export: agn, text/CSV, Pajek `.net` (Gephi-safe), Excel import
  (.xls/.xlsx, incl. Numbers), GraphML, GML, GraphSON
- Merge Network (by name; sum/max/keep policies); session log in the Output
  pane; background analyses with cancellation
- Headless rendering and drawing; Barnes–Hut spring layout; fuzz, stress and
  rendering test suites
- UTF-8 text I/O with legacy latin-1 read fallback
- Node-face fallback; modern Help with formulas and links (incl. the citation
  guide); settings stored in the user home (bundle-ready)

## Citation

DOI: 10.5281/zenodo.22708199 — see CITATION.cff / CITATION.bib

## Files

- `agna-desktop/target/agna-2.1.3.jar` — run: `java -jar agna-desktop/target/agna-2.1.3.jar`
- `agna-cli/target/agna-cli-2.1.3.jar` — run: `java -jar agna-cli/target/agna-cli-2.1.3.jar --help`
- `agna-core/target/agna-2.1.3.jar` — library
- `packaging/` — macOS dmg recipe, app icon (.icns), signing guide

## License

Apache License 2.0 (all modules). See LICENSE. The High-Risk Activities
Disclaimer is in the README.