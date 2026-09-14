# Agna User Manual — 2.1.3

## 1. Quick start

- **Open a network**: File ▸ Open Network (or use the toolbar). Agna reads
  its own format (`.agn`), text tables (`.txt`, `.csv`), Pajek (`.net`),
  Excel (`.xls` and `.xlsx`, including files saved by Apple Numbers),
  GraphML (`.graphml`), GML (`.gml`) and GraphSON (`.json`). See
  [File formats](6fileformats.htm).
- **Enter data**: with a new network (File ▸ New Network) the sociomatrix
  grid appears; type tie values into the cells. A value of 0 means "no
  tie".
- **Analyse**: pick an analysis from the Analysis menu; the report appears
  in the Output pane, which doubles as a session log (every operation is
  recorded there with a timestamp). Every computation is explained in
  [Methodology](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm).
- **Move data to other tools**: use File ▸ Save Network As... and choose
  GraphML, GML, GraphSON or Pajek — Gephi, Cytoscape, NetworkX, igraph and
  the R SNA packages all read at least one of these.
- **Get help**: Help ▸ Contents opens this manual; click anywhere on the
  startup splash to skip it.

## 2. Concepts — a short SNA glossary

- **Sociomatrix**: a square table whose cell (i, j) holds the value of the
  tie from actor i to actor j. Row i is actor i's *emission* (outgoing)
  profile, column j is actor j's *reception* (incoming) profile.
- **Directed vs undirected**: a directed tie (arc) goes one way; an
  undirected tie (edge) goes both ways. Agna stores arcs; a network whose
  matrix is symmetric is read as possibly undirected.
- **Degree**: for directed networks, outdegree = number of outgoing ties,
  indegree = number of incoming ties; nodal degree = the pair.
- **Geodesic**: the shortest path between two actors, measured in hops.
- **Outsider / isolate**: an actor with an all-zero row *and* an all-zero
  column (neither emits nor receives).
- **Walk vs path**: a walk may repeat actors; a path may not. Matrices
  square to count walks of length 2.

## 3. Menu reference

Items that compute something link to their explanation; follow the link
to read exactly what is calculated.

- **File**: New Network; Open Network; [Merge Network...](5matrixoperationsformulas.htm#mergenetwork);
  Save Network As... (the format list is documented in
  [File formats](6fileformats.htm)); Save Network; New From Chain...; Quit.
- **Edit**: Cut / Copy / Paste / Delete / Select All (spreadsheet cell
  editing).
- **Network**:
  - Title...; Add Nodes...; Delete Selected Node — bookkeeping, no
    computation.
  - [Add Scalar...](5matrixoperationsformulas.htm#addscalar) — shifts all
    off-diagonal values by a constant.
  - [Scalar Multiplication...](5matrixoperationsformulas.htm#scalarmultiplication)
    — scales all off-diagonal values by a factor.
  - [Transpose](5matrixoperationsformulas.htm#transpose) — mirrors the
    matrix across the diagonal.
  - [Symmetrize...](5matrixoperationsformulas.htm#symmetrize) — makes the
    matrix symmetric; choose among 13 rules.
  - [Normalize (Binarize)...](5matrixoperationsformulas.htm#normalizebinarize)
    — dichotomise to 0/1, or threshold a weighted matrix.
  - [Remove Outsiders](5matrixoperationsformulas.htm#removeoutsiders) —
    delete isolates.
  - Renumber Nodes... — replaces names with numbers, no computation.
  - [Square Matrix](5matrixoperationsformulas.htm#squarematrix) — M×M,
    counting two-step walks.
- **Analysis**:
  - Basic Description — the [full methodology page](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm)
    explains every number it reports.
  - [Nodal Degree](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#nodaldegreeanddirection),
    [Indegree](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#nodaldegreeanddirection),
    [Outdegree](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#nodaldegreeanddirection)
    — per-direction degree counts.
  - [Density](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#density)
    — the share of possible ties that exist.
  - [Cohesion](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#cohesion)
    — the share of mutual dyads.
  - Emission Degree, Reception Degree — the weighted out/in totals, see
    [Nodal degree and direction](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#nodaldegreeanddirection).
  - [Determination Degree](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#determinationandsociometricstatus),
    [Sociometric Status](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#determinationandsociometricstatus)
    — Agna's own status coefficients.
  - [Eccentricity](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#eccentricityanddiameter),
    [Diameter](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#eccentricityanddiameter)
    — the longest geodesics.
  - [Geodesic Matrix](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#geodesicsandshortestpaths),
    [Shortest Paths...](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#geodesicsandshortestpaths),
    [All Shortest Paths](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#geodesicsandshortestpaths).
  - [N-Cliques](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#ncliques).
  - [Bavelas-Leavitt](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#bavelasclosenessandfareness),
    [Closeness](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#bavelasclosenessandfareness),
    [Fareness](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#bavelasclosenessandfareness)
    — the distance-based centralities.
  - [Betweenness](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#betweenness).
  - [Prestige](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm#prestige).
  - Full Analysis — the whole battery in one pass.
- **View / Output**: Network Viewer (and its Close item); Hide/Show
  Output; Open/Save/Clear the Output pane (the [session log](7newin213.htm#sessionlog)).
- **Preferences**: Working Directory...; Save Settings As Default...;
  Look and Feel.
- **Help**: Contents...; About Agna...

## 4. Methodology — what exactly is computed (read before publishing results)

### Density

Directed convention: arcs / (n · (n − 1)). For a symmetric matrix the
undirected reading (edges / (n · (n − 1)/2)) yields the same value; Agna
prints both labels explicitly, and warns when the matrix is asymmetric so
no undirected density exists.

### Nodal degree and direction

Outdegree d⁺(i) = Σⱼ A[i][j]; indegree d⁻(j) = Σᵢ A[i][j]. Emission
degree and reception degree are the weighted variants (they sum the tie
*values*, not just the non-zero cells).

### Geodesics and shortest paths

The geodesics matrix holds shortest-path hop counts. A 0 entry means "no
path" (and the diagonal). Shortest Paths reports the actual intermediate
steps. **Eccentricity, Closeness and Betweenness refuse to compute on
disconnected networks** and print a warning instead of silently treating
unreachable pairs as distance 0.

### Eccentricity and diameter

Eccentricity e(i) = max over reachable j of d(i, j); diameter =
max over i of e(i) — the largest geodesic of the network.

### Betweenness

Raw (unnormalised) Freeman betweenness, summed over ordered pairs and
computed with Brandes' algorithm: for each pair (s, t), the share of the
shortest s–t paths that pass through the actor.

### Bavelas, closeness and fareness

Distance-based centralities built on the geodesic matrix. They require a
connected network (see above); read the exact normalisation in the
formulas.wpd companion — the family differs between authors, and Agna
implements the classic versions named after Bavelas-Leavitt, closeness
and fareness.

### Prestige

Proximity prestige (Lin 1976): the share of other actors that can reach
i, times the average closeness of those actors; 0 when nobody can reach
i.

### Cohesion

The density of mutual (two-way) dyads: the share of ordered pairs with
A[i][j] = A[j][i] ≠ 0.

### Determination and sociometric status

Determination = (in-strength − out-strength) / (n − 1); Sociometric
Status = (in-strength + out-strength) / (n − 1). These are Agna's own
coefficients; verify they match the convention in your field before
citing them.

### A note on distances

Analyses that depend on distances assume unweighted (graph) distances;
edge *values* are used by the degree/status/density statistics, not by
the path-based measures.

### N-Cliques

Enumerated with the Bron–Kerbosch (Tomita pivot) algorithm; the search is
capped at 1000 reported cliques and the report says so when the cap is
reached.

## 5. Matrix operations — formulas

All operations write back into the current sociomatrix. The diagonal (no
self-loops) is preserved as 0 by every operation below. These are the
operations behind the Network menu; the [menu reference](3menureference.htm)
links them back here.

### Add scalar

M′[i][j] = M[i][j] + c for i ≠ j. Useful to shift a weighted matrix
before dichotomising.

### Scalar multiplication

M′[i][j] = c · M[i][j] for i ≠ j.

### Transpose

M′[i][j] = M[j][i] — mirrors the matrix across the diagonal, swapping
rows and columns.

### Symmetrize

For every pair i < j with a = M[i][j], b = M[j][i], both cells become
f(a, b):

| Option | f(a, b) |
|---|---|
| Maximum | max(a, b) |
| Minimum | min(a, b) |
| Maximum Non-Zero | the non-zero one if exactly one is non-zero, else max(a, b) |
| Minimum Non-Zero | the non-zero one if exactly one is non-zero, else min(a, b) |
| Below Diagonal | the lower-triangle value (b) wins |
| Above Diagonal | the upper-triangle value (a) wins |
| Below Non-Zero | b if b ≠ 0, else a |
| Above Non-Zero | a if a ≠ 0, else b |
| Sum | a + b |
| Product | a · b |
| Product Non-Zero | the non-zero one if exactly one is non-zero, else a · b |
| Arithmetical Mean | (a + b) / 2 |
| Geometrical Mean | √(a · b) — for opposite-sign pairs the result is 0 |

Use **Sum** to aggregate two measurements of the same tie, **Maximum** to
take the stronger, **Below/Above** to keep one triangle's opinion.

### Normalize (Binarize)

Two options:
- *Binary*: M′[i][j] = 1 if M[i][j] ≠ 0, else 0.
- *By threshold* (value t): M′[i][j] = 1 if M[i][j] > t, else 0.
  Values exactly equal to the threshold become 0.

### Square Matrix

M′ = M × M, i.e. M′[i][j] = Σₖ M[i][k] · M[k][j]. The entry (i, j)
counts the walks of length 2 from i to j — the number of two-step
intermediaries.

### Merge Network

Joins the current network with another file. Actors are matched **by
name**: a name present in both networks becomes one actor; a name present
only in the second file is appended. For a tie present in both, the
policy decides: Sum → A[i][j] + B[i][j]; Maximum → the larger;
Keep current value → the first network's. The diagonal stays zero.

### Remove outsiders

Deletes every actor whose row *and* column are all zero, then renumbers
the rest.

### The boolean product

Reachability questions use the boolean product internally:
(A ⊗ B)[i][j] is 1 exactly when some k has A[i][k] = B[k][j] = 1.

## 6. File formats

Agna saves and opens the following formats (File ▸ Save Network As... /
Open Network):

| Format | Extension | Use |
|---|---|---|
| Agna | `.agn` | native format, preserves faces, coordinates, viewer settings |
| Plain text | `.txt`, `.csv` | tab- or comma-separated matrices; CSV quotes node names |
| Pajek | `.net` | the classic SNA format (Gephi-friendly export) |
| Excel | `.xls`, `.xlsx` | JExcelAPI (`.xls`) and the built-in `.xlsx` reader |
| GraphML | `.graphml` | XML graph format of Gephi, NetworkX, igraph, Cytoscape |
| GML | `.gml` | the classic text format of igraph / NetworkX |
| GraphSON | `.json` | JSON graph format (TinkerPop family, Gephi, Cytoscape) |

Interoperability notes:

- **GraphML / GML / GraphSON** export the matrix as a *directed* graph:
  nodes in their file order with their names, every non-zero tie as an
  edge carrying its value as weight. On import, missing weights default
  to 1, self-loops are ignored (the diagonal stays zero), and node order
  defines the matrix indexing.
- **GraphSON** import accepts both the `{"graph": {vertices, edges}}`
  wrapper and a bare `{vertices, edges}` document; weights may use the
  key `value` or `weight`.
- **Excel**: Numbers files (.xlsx) open directly; saving still writes
  the legacy `.xls`, which Excel and Numbers both read.
- **Pajek**: the export omits comment lines so Gephi imports cleanly;
  optional per-node Vector blocks are controlled by a checkbox (remembered
  in the settings).
- Legacy files store face paths with platform-specific separators; Agna
  2.1.3 resolves them transparently, and any node without a stored face
  falls back to the red-bullet default.

## 7. New in 2.1.3

- **Modern interface**: FlatLaf look and feel (light by default, with a
  live picker in Preferences ▸ Look and Feel; Native and the classic
  themes are available, and a `Classic Toolbar Icons` setting restores
  the old icon set). The sociomatrix grid and its selection are visible
  in every look, and node faces never disappear at launch — missing faces
  revert to the bundled red bullet.
- **Merge Network** (File ▸ Merge Network...): join two networks by
  actor name with a Sum / Maximum / Keep policy, as described in
  [Matrix operations](5matrixoperationsformulas.htm#mergenetwork).
### Session log

The Output pane records every operation with a timestamp
(`> [time] ...` lines) — open, new, save, transforms, merge — so you can
reconstruct what was done to a network. Use View ▸ Hide/Show Output to
toggle the pane.
- **Export/import family**: Excel import (including Numbers `.xlsx`),
  GraphML, GML and GraphSON round-trips — see
  [File formats](6fileformats.htm).
- **Full Analysis** (Analysis ▸ Full Analysis): runs the whole battery
  in one pass, with a skip note when the network is disconnected.
- Analyses run in the background with a cancellable progress dialog.
- **This manual**: the help now explains every computation — see
  [Methodology](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm)
  and [Matrix operations](5matrixoperationsformulas.htm).

## 8. Keyboard shortcuts

- **Ctrl+P** — add new nodes to the current network.
- **Ctrl+H** — open the Agna Help window.
- Menu items show their shortcut letters as underlined mnemonics.

## 9. FAQ / Troubleshooting

- **I exported to Excel and Numbers asks for a format** — Agna saves the
  legacy `.xls`; Excel and Numbers open it directly. If you save from
  Numbers as `.xlsx`, Agna opens that file too.
- **How do I move a network to R or Python?** — save as GraphML, GML or
  GraphSON; the R packages igraph/statnet and Python's NetworkX read
  those formats directly. See [File formats](6fileformats.htm).
- **Gephi won't import my Pajek file** — ensure the network export has
  comment lines disabled (they are by default; the option is remembered
  for the Pajek format).
- **Node faces are missing at launch** — a missing or `-` face now falls
  back to the red-bullet default; if you still see none, check that the
  settings file does not pin an invalid `Default Node Face`.
- **An analysis prints a warning instead of numbers** — distance-based
  measures (eccentricity, closeness, fareness, betweenness, geodesics)
  need a connected network; that is intentional, not a bug. See
  [Methodology](4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm).
- **I cannot see grid lines / selections** — change the look and feel in
  Preferences; the grid is drawn by the table itself in every theme.

## 10. Version history

- **2.1.3** — open-source revival of Agna 2.1.2: Maven/JDK 17 build,
  namespace `com.bentza.sna`, engine `AgnaLib`; background analyses with
  cancellation; scalable clique search; Pajek and Excel import fixes;
  Full Analysis; GraphML/GML/GraphSON import and export; Merge Network;
  session log; FlatLaf interface with classic-icon fallback; modernised
  Help; 100+ regression tests.
- **2.1.2** — the last closed-source release by Marius Ion Bența;
  single-jar application with the l2fprod skin themes.

## 11. Cite AGNA

If you use AGNA in your research, please cite it as:

Bența, M. I. (2026). AGNA: Applied Graph and Network Analysis Open
Source (Version 2.1.3) [Computer software].
https://doi.org/10.5281/zenodo.22708199

BibTeX entry:

```
@software{benta_2026_agna,
  author  = {Bența, Marius Ion},
  title   = {AGNA: Applied Graph and Network Analysis Open Source},
  version = {2.1.3},
  year    = {2026},
  doi     = {10.5281/zenodo.22708199},
  url     = {https://www.netanalysis.co.uk}
}
```

The DOI is registered with Zenodo; the `CITATION.cff` file at the
repository root carries the same metadata in machine-readable form.

For reproducibility, report the AGNA version and the analysis options
used.

Copyright 2001–2026 Marius Ion Bența. Website:
https://www.netanalysis.co.uk