# Third-party software and notices

AGNA itself is licensed under the **Apache License, Version 2.0** (see
`LICENSE` and `NOTICE`). This document lists the third-party components
bundled with or used by the distribution, with their licences and copyright
lines. The authoritative copies of each component's licence and notice text
are retained inside the distributed jars (`META-INF/LICENSE*`,
`META-INF/NOTICE*`); the lines below are transcribed from those sources and
from the components' published metadata. The machine-readable inventory of
the same components is in `docs/sbom/AGNA-2.1.3-SBOM.json` (CycloneDX) and
`docs/sbom/AGNA-2.1.3-SPDX.json`.

## Bundled runtime components (Apache License 2.0)

| Component | Version | Copyright |
|-----------|---------|-----------|
| Apache POI (`org.apache.poi:poi`, HSSF `.xls` support) | 5.3.0 | Copyright 2001-2024 The Apache Software Foundation; this product includes software developed by The Apache Software Foundation |
| Jackson JSON processor (`jackson-core`, `jackson-databind`, `jackson-annotations`) | 2.17.1 | Copyright 2007-, Tatu Saloranta |
| Apache Commons Codec | 1.17.0 | Copyright 2002-2024 The Apache Software Foundation |
| Apache Commons Collections | 4.4 | Copyright 2001-2024 The Apache Software Foundation |
| Apache Commons IO | 2.16.1 | Copyright 2002-2024 The Apache Software Foundation |
| Apache Commons Math | 3.6.1 | Copyright 2001-2021 The Apache Software Foundation |
| Apache Log4j API (`log4j-api`) | 2.23.1 | Copyright 1999-2024 Apache Software Foundation |
| SparseBitSet | 1.3 | Copyright 2015 B. Caulfield |
| FlatLaf | 3.4.1 | Copyright (c) 2021 FormDev Software GmbH |

All of the above are distributed under the **Apache License, Version 2.0**.
They are shaded into the deliverable jars and installers; their licence
texts and notices are preserved in the jars' `META-INF` as required by the
Apache License.

## Test-only components (not bundled)

| Component | Version | Licence | Copyright |
|-----------|---------|---------|-----------|
| JUnit 5 (`junit-jupiter`) | 5.10.2 | Eclipse Public License 2.0 | Copyright 2015-2023 The JUnit Team |

JUnit is resolved from Maven Central in `test` scope only; it is never
included in the distributed jars or installers.

## Bundled content from the original distribution

- The example networks under `samples/` and the node-face/help assets are
  part of the original AGNA distribution by Marius Ion Bența (2001-2005)
  and are carried over unchanged.
- Icons, splash artwork, and the website are created for this project and
  are licensed under Apache License 2.0 with the project.
- The historical paper *Studying Communication Networks with AGNA 2.1*
  (Bența, 2005) is attached to the release as a separate asset (CC BY 4.0
  per its Zenodo record) and is not part of the source distribution.

## Keeping this document accurate

The CI "sbom-gate" job regenerates the CycloneDX SBOM on every release and
fails the build if any dependency's licence leaves the Apache-2.0
allowlist. When dependencies change, this document must be updated in the
same change set; the SBOMs and `docs/sbom/` files are regenerated from the
resolved dependency graph.