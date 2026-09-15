# Third-party software

This project builds on the following third-party components. AGNA itself is
licensed under the Apache License 2.0 (see LICENSE and the README); the
licences below govern the third-party components only.

## Apache POI

- Component: MS Excel `.xls` (HSSF) read/write library, used for the Excel
  import/export feature.
- Resolved from Maven Central (`org.apache.poi:poi`).
- Licence: Apache License 2.0.
- Transitive dependencies (all Apache License 2.0): commons-codec,
  commons-collections4, commons-math3, SparseBitSet, log4j-api.

## FlatLaf

- Component: modern cross-platform Swing look and feel, the application's
  default theme.
- Version: resolved from Maven Central (`com.formdev:flatlaf`).
- Licence: Apache License 2.0.

## Jackson

- Component: JSON handling for GraphSON import/export.
- Resolved from Maven Central.
- Licence: Apache License 2.0.

## JUnit 5

- Component: test framework (test scope only).
- Resolved from Maven Central.
- Licence: Eclipse Public License 2.0.

## Sample data

`samples/` and the bundled faces/help assets are part of the original
Agna 2.1.2 distribution by Marius Ion Bența and are carried over unchanged.