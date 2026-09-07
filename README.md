# Agna

**Agna** is a desktop Java application for **social network analysis and sequence
analysis**. It was originally written by Marius Benta (2002–2005): you enter a
sociomatrix in a spreadsheet-style grid, inspect it as a node/edge graph, and
run the classic measures (degree, density, cohesion, centrality, geodesics,
shortest paths, etc.). Files can be imported/exported in Agna's own `.agn`
format, tab/comma-separated text, CSV, Pajek `.net` and MS Excel; the graph view
exports to SVG, JPEG and HTML.

This is the **2.1.3** worktree: a cleaned-up, bug-fixed, re-organized revival of
the original closed-source **2.1.2** release. The licence for 2.1.3 is **still
being decided** — no licence file is shipped yet.

## Layout

```
src/main/java/com/bentza/sna/   application code (gui, io, net, gui/filter)
src/main/resources/             bundled assets: buttons/, faces/, help/, themepacks/
src/test/java/com/bentza/sna/   JUnit 5 tests
samples/                        original sample networks (kept byte-identical)
docs/manuals/                   user manual (.htm/.doc/.pdf) and print masters
lib/                            vendored third-party binary (see THIRD-PARTY.md)
pom.xml                         Maven build (JDK 17)
```

## Build and run

Requires JDK 17 and Maven.

```
mvn test        # compile + run the unit tests
mvn package     # builds target/agna-2.1.3.jar (self-contained, shaded)
java -jar target/agna-2.1.3.jar
```

On first start the bundled node-face images and theme packs are materialized
under `~/.agna/faces` (faces are stored by file path inside `.agn` documents,
so they must exist on disk).

## What changed in 2.1.3 (highlights)

- **Namespace**: package root renamed `com.benta` → `com.bentza`; the network
  engine class `Ajna` renamed `AgnaLib`.
- **Modern toolchain**: compiles on JDK 17; removed the proprietary
  `com.apple.mrj` and internal `com.sun.image.codec.jpeg` APIs; the l2fprod skin
  engine and JExcelAPI are proper dependencies now.
- **Bug fixes**
  - Deleting two nodes no longer corrupts the matrix with stray zeros
    (`AgnaTableModel.delRowCol` removed the wrong data column).
  - Copy/paste from a spreadsheet no longer loses a column on empty cells
    (`ExcelAdapter` tokenizer preserved imports).
  - Text matrices with an empty diagonal (missing values) parse as zeros
    instead of failing.
  - Resource leaks closed (streams), 26 misguided `System.gc()` calls removed,
    data layer no longer crashes before the GUI frame exists (headless-safe).
- **Backward compatibility**: old `.agn` files that reference faces with the
  legacy relative `.\Faces\...` paths and/or the old "Shaddow" spelling load
  transparently; the bundled faces were renamed to the correct "Shadow"
  spelling and stale misspelled files are cleaned from `~/.agna/faces`.
- **Tests**: JUnit 5 suite covering deletion integrity, paste parsing, file I/O
  round-trips against the original samples, and the legacy-face compatibility.

## Samples note

The `samples/` directory is kept byte-identical to 2.1.2, including its legacy
"Shaddow" face references — those files double as compatibility fixtures for
the tests.

## Third-party software

See `THIRD-PARTY.md` for attribution of the bundled and dependency components.