# Third-party software

This project builds on the following third-party components. Their licence terms
govern their use; Agna's own licence is still being decided (see README).

## JExcelAPI (jxl)

- Component: MS Excel read/write library, used for the Excel export feature.
- Version: 2.6.12, resolved from Maven Central
  (`net.sourceforge.jexcelapi:jxl`).
- Licence: GNU Lesser General Public License (LGPL).
- Note: the original 2.1.2 archive bundled `jxl.jar` 2.5.1 with the same API;
  the dependency was upgraded to the last published 2.6.12 release.

## l2fprod Skin Look and Feel

- Component: skinnable Swing look-and-feel engine behind Agna's theme packs.
- Origin: only binaries were present in the original 2.1.2 archive
  (`Lib/com/l2fprod/...`, 217 classes). These are vendored byte-for-byte in
  `lib/l2fprod-skin-1.0.jar` and exposed to the build through a project-local
  Maven repository (`lib/repo/`), so the jar resolves on a fresh checkout.
- Licence: the l2fprod Skin Look and Feel was released under the GNU Lesser
  General Public License (LGPL) by L2FProd.com. No licence text was shipped in
  the original archive; if you redistribute, include the LGPL notice that
  accompanies the upstream project (l2fprod.com / SourceForge).
- Theme packs (`src/main/resources/themepacks/`) are data files from the
  original distribution.

## Sample data

`Samples/` and the bundled faces/help assets are part of the original Agna 2.1.2
distribution by Marius Benta and are carried over unchanged.

## Java standard library

No other runtime dependencies. JUnit 5 (test scope only) is resolved from
Maven Central under the Eclipse Public Licence 2.0.
- Jackson (Apache-2.0) - JSON handling for GraphSON import/export.
