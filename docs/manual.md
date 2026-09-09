# Agna User Manual — 2.1.3

Single source of truth for the in-app help and the printable PDF.
Build step (planned): this Markdown is converted to (a) the in-app HTML
pages under `src/main/resources/help/` and (b) the PDF manual in
`docs/manuals/`. Screenshots will be captured on macOS and referenced
from the Quick Start section.

## 1. Quick start
1. Open a sample network (`File > Open…`, pick `samples/example2.agn`).
2. Edit the sociomatrix in the grid (each cell = strength of the tie
   from the row node to the column node; the diagonal stays 0).
3. Run an analysis: `Analysis > …` (e.g. Density, Nodal Degree,
   Centrality > Betweenness).
4. Inspect the graph view (`Ctrl+Z`), the output pane and the statistics
   table.
5. Save as Agna (`.agn`), Pajek (`.net`), plain text, CSV or Excel.

*Screenshots to be captured on macOS (steps 1-5).*

## 2. Concepts — a short SNA glossary
- *Sociomatrix*: adjacency matrix; row i, column j = value of the direct
  tie from i to j. Agna networks are directed by default.
- *Degree measures*: Nodal Degree (neighbours of a node, symmetric
  networks), Emission/Reception Degree (outgoing/incoming tie counts),
  weighted variants (sums of tie values). Indegree/Outdegree are BINARY
  counts (only presence matters): changing a tie's value does not move
  them - add/remove ties or use the weighted variants instead.
- *Centrality*: Betweenness (Freeman), Closeness (Freeman; inverse of
  the distance sum), Fareness, Eccentricity (maximum distance),
  Bavelas-Leavitt index.
- *Prestige*: proximity prestige (Lin 1976) — see Methodology.
- *Cliques*: maximal n-cliques (Luce's n-clique: maximal node sets whose
  mutual geodesic distance is at most the chosen diameter).
- *Status/Determination*: Agna-specific degree-based coefficients —
  see Methodology.

## 3. Methodology — what exactly is computed (read before publishing results)
- **Density** uses the directed convention: arcs / (n * (n-1)). For a
  symmetric matrix the undirected reading (edges / (n * (n-1)/2)) yields
  the same value; Agna prints both labels explicitly, and warns when the
  matrix is asymmetric so no undirected density exists.
- **Geodesics matrix**: entries are shortest-path hop counts. A 0 entry
  means "no path" (and the diagonal). **Eccentricity, Closeness and
  Betweenness refuse to compute on disconnected networks** and print a
  warning instead of silently treating unreachable pairs as distance 0.
- **Betweenness** is raw (unnormalised) Freeman betweenness, summed over
  ordered pairs.
- **Prestige** = proximity prestige: (share of nodes that can reach i)
  x (average closeness of those nodes); 0 when nobody can reach i.
- **Cohesion** = density of mutual (two-way) dyads.
- **Determination** = (in-strength - out-strength) / (n-1);
  **Sociometric Status** = (in-strength + out-strength) / (n-1).
  These are Agna's own coefficients; verify they match the convention in
  your field before citing them.
- Analyses that depend on distances assume unweighted (graph) distances;
  edge *values* are used by the degree/status/density statistics, not by
  the path-based measures.

## 4. File formats
- `.agn` — Agna format (version 2.1); note the settings sections
  (viewer settings, node faces, node coordinates).
- `.net` — Pajek export.
- `.txt` / `.csv` — tab- or comma-separated matrices; CSV supports
  quoted node names; empty cells read as 0.
- `.xls` — Excel export (JExcelAPI).
- Legacy files: old files store face paths with platform-specific
  separators; Agna 2.1.3 resolves such references transparently.

## 5. Menu reference
(Filled from the menus in sync; currently the pages mirror the 2.1.2
menu structure with the re-enabled N-Cliques, Prestige and Full
Analysis entries.)

## 6. Keyboard shortcuts
(Fill: Ctrl+C/V, Ctrl+Z viewer, Ctrl+K cell edit, Ctrl+A select all.)

## 7. FAQ / Troubleshooting
(Fill: "why is there no analysis output for my disconnected network",
"the matrix reads with zeros after paste", "faces missing on load".)

## 8. Version history
- 2.1.3: namespace com.bentza.sna, JDK 17 build, disconnected-network
  guards, explicit density conventions, proximity prestige, N-Cliques
  implemented, logging, charset policy, 35+ automated tests.
- 2.1.2: last closed-source release (2005).
