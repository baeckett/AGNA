# Third-party software

This project builds on the following third-party components. AGNA itself is
licensed under the Apache License 2.0 (see LICENSE and the README); the
licences below govern the third-party components only.

## JExcelAPI (jxl)

- Component: MS Excel read/write library, used for the Excel export feature.
- Version: 2.6.12, resolved from Maven Central
  (`net.sourceforge.jexcelapi:jxl`).
- Licence: GNU Lesser General Public License (LGPL).
- Note: the original 2.1.2 archive bundled `jxl.jar` 2.5.1 with the same API;
  the dependency was upgraded to the last published 2.6.12 release.

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