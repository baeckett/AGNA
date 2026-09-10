# Agna 2.1.3 — Release notes

Open-source revival of Agna (last closed release: 2.1.2).
Copyright (C) 2002–2026 Marius Ion Bența. Website (provisional): netanalysis.co.uk

## Features
- Modern build: Maven/JDK 17, single shaded jar, 120+ tests
- FlatLaf interface: light default, live look-and-feel picker, classic-theme fallback
- New analysis battery: Full Analysis, scalable N-Cliques, prestige (Lin 1976), Brandes betweenness
- Import/export: GraphML, GML, GraphSON; Excel import (.xls and .xlsx, incl. Numbers); Gephi-safe Pajek
- Merge Network (by name; sum/max/keep policies); session log in the Output pane
- Background analyses with cancellation; visible grid/selection in all themes; node-face fallback; modern Help with formulas and links
- Workflow hardening: stress, fuzz and rendering suites; headless/CLI-safe parsing; settings stored in the user home (bundle-ready)

## Files
- agna-2.1.3.jar — run with: java -jar agna-2.1.3.jar
- packaging/ — macOS dmg recipe, app icon (.icns), signing guide

## License
Pending final choice (engine LGPL / app GPL split planned).