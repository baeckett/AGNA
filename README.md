# Agna

**Agna** is a cross-platform, open-source Java application for **social
network analysis and sequence analysis**. Enter a sociomatrix in a
spreadsheet-style grid, inspect it as a node/edge graph, and run the classic
measures — degree, density, cohesion, centrality, geodesics, shortest paths,
cliques and more. Networks import/export in Agna's own `.agn` format, plain
text, CSV, Pajek `.net`, MS Excel (`.xls`/`.xlsx`), GraphML, GML and
GraphSON; the graph view exports images, and the command line renders
networks headlessly.

Written originally by
[Marius Ion Bența](https://www.netanalysis.co.uk) (2001–2005), **2.1.3** is
the open-source revival of the last closed-source release (2.1.2):
re-organized into a Maven monorepo, modernised (JDK 17, FlatLaf), bug-fixed,
UTF-8 based, with a full test suite.

## Layout

```
agna-core/      engine: networks, analyses (AgnaLib), layouts, import/export
agna-cli/       command-line interface (info, analyse, convert, transform,
                draw, generate, metrics, ...)
agna-desktop/   the desktop application (Swing)
samples/        original sample networks (kept byte-identical)
docs/           user manual, CLI reference, man page, shell completions
packaging/      jpackage/DMG recipe, Agna.icns, signing guide
CITATION.cff    machine-readable citation metadata (Zenodo DOI)
```

## Build and run

Requires JDK 17 and Maven.

```
mvn verify      # compile + run the full test suite (230 tests)
mvn package     # builds the jars in each module's target/
java -jar agna-desktop/target/agna-2.1.3.jar        # desktop
java -jar agna-cli/target/agna-cli-2.1.3.jar --help # CLI
```

On first start the bundled node-face images are materialized under
`~/.agna/faces`.

## License

Apache License 2.0 — see [LICENSE](LICENSE). No warranty; use at your own
discretion.

## High-Risk Activities Disclaimer

AGNA is provided for research, educational, and general analytical purposes.
It is not designed, developed, tested, certified, or intended for use in
hazardous or safety-critical environments requiring fail-safe performance.
Without limitation, AGNA must not be used for the design, construction,
operation, maintenance, monitoring, or control of nuclear facilities;
aircraft, aviation navigation, aviation communications, or air-traffic
control systems; direct life-support or life-critical medical systems;
weapons systems; emergency-response control systems; or any other activity in
which a failure, error, delay, interruption, or inaccurate output could
reasonably be expected to result in death, personal injury, or severe
physical, environmental, or property damage. Any use of AGNA in such high-risk
activities is entirely at the user’s own risk. To the maximum extent permitted
by applicable law, the author and contributors disclaim all warranties,
whether express, implied, statutory, or otherwise, including any warranty of
fitness for high-risk activities.

## Citation

If you use Agna in your research, please cite it (see also
[CITATION.cff](CITATION.cff) and [CITATION.bib](CITATION.bib)):

> Bența, M. I. (2026). *AGNA: Applied Graph and Network Analysis Open Source*
> (Version 2.1.3) [Computer software]. https://doi.org/10.5281/zenodo.22708199

## Links

- Website: https://www.netanalysis.co.uk
- Contact: contact@netanalysis.co.uk
- Copyright 2001–2026 Marius Ion Bența