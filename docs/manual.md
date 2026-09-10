# Agna User Manual — 2.1.3

## 1. Quick start

- **Open a network**: File ▸ Open Network (or use the toolbar). Agna reads
  its own format (`.agn`), text tables (`.txt`, `.csv`), Pajek (`.net`),
  Excel (`.xls` and `.xlsx`, including files saved by Apple Numbers),
  GraphML (`.graphml`), GML (`.gml`) and GraphSON (`.json`).
- **Enter data**: with a new network (File ▸ New Network) the sociomatrix
  grid appears; type tie values into the cells. A value of 0 means "no
  tie".
- **Analyse**: pick an analysis from the Analysis menu; the report appears
  in the Output pane, which doubles as a session log (every operation is
  recorded there with a timestamp).
- **Move data to other tools**: use File ▸ Save Network As... and choose
  GraphML, GML, GraphSON or Pajek — Gephi, Cytoscape, NetworkX, igraph and
  the R SNA packages all read at least one of these.
- **Get help**: F1-style content lives in the Help ▸ Contents window;
  click anywhere on the startup splash to skip it.

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

## 3. Methodology — what exactly is computed (read before publishing results)

- **Density** uses the directed convention: arcs / (n · (n − 1)). For a
  symmetric matrix the undirected reading (edges / (n · (n − 1)/2)) yields
  the same value; Agna prints both labels explicitly, and warns when the
  matrix is asymmetric so no undirected density exists.
- **Nodal degree** per direction: outdegree d⁺(i) = Σⱼ A[i][j],
  indegree d⁻(j) = Σᵢ A[i][j].
- **Geodesics matrix**: entries are shortest-path hop counts. A 0 entry
  means "no path" (and the diagonal). **Eccentricity, Closeness and
  Betweenness refuse to compute on disconnected networks** and print a
  warning instead of silently treating unreachable pairs as distance 0.
- **Eccentricity** e(i) = max over reachable j of d(i, j); **diameter** =
  max over i of e(i) (largest geodesic).
- **Betweenness** is raw (unnormalised) Freeman betweenness, summed over
  ordered pairs, computed with Brandes' algorithm: for each pair (s, t),
  the share of shortest s–t paths that pass through the actor.
- **Bavelas-Leavitt / Closeness / Fareness** are distance-based measures
  built on the geodesic matrix; they require a connected network (see
  above).
- **Prestige** = proximity prestige (Lin 1976): the share of other actors
  that can reach i, times the average closeness of those actors; 0 when
  nobody can reach i.
- **Cohesion** = density of mutual (two-way) dyads: the share of ordered
  pairs with A[i][j] = A[j][i] ≠ 0.
- **Determination** = (in-strength − out-strength) / (n − 1);
  **Sociometric Status** = (in-strength + out-strength) / (n − 1).
  These are Agna's own coefficients; verify they match the convention in
  your field before citing them.
- Analyses that depend on distances assume unweighted (graph) distances;
  edge *values* are used by the degree/status/density statistics, not by
  the path-based measures.
- **N-Cliques** are enumerated with the Bron–Kerbosch (Tomita pivot)
  algorithm; the search is capped at 1000 reported cliques and the report
  says so when the cap is reached.

## 4. Matrix operations — formulas

All operations write back into the current sociomatrix. The diagonal
(no self-loops) is preserved as 0 by every operation below.

- **Add scalar** (with value c): M′[i][j] = M[i][j] + c for i ≠ j.
  Useful to shift a weighted matrix before dichotomising.
- **Scalar multiplication** (factor c): M′[i][j] = c · M[i][j] for i ≠ j.
- **Transpose**: M′[i][j] = M[j][i] — mirrors the matrix across the
  diagonal, swapping rows and columns.
- **Symmetrize...** — for every pair i < j with a = M[i][j],
  b = M[j][i], both cells become f(a, b):

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

  Use **Sum** to aggregate two measurements of the same tie, **Maximum**
  to take the stronger, **Below/Above** to keep one triangle's opinion.
- **Normalize (Binarize)...** — two options:
  - *Binary*: M′[i][j] = 1 if M[i][j] ≠ 0, else 0.
  - *By threshold* (value t): M′[i][j] = 1 if M[i][j] > t, else 0.
    Values exactly equal to the threshold become 0.
- **Square Matrix**: M′ = M × M, i.e.
  M′[i][j] = Σₖ M[i][k] · M[k][j]. The entry (i, j) counts the walks of
  length 2 from i to j — the number of two-step intermediaries.
- **Merge Network...**: joins the current network with another file.
  Actors are matched **by name**: a name present in both networks becomes
  one actor; a name present only in the second file is appended. For a
  tie present in both, the policy decides (Sum: A[i][j] + B[i][j];
  Maximum: the larger; Keep current value: the first network's). The
  diagonal stays zero.
- **Remove Outsiders**: deletes every actor whose row *and* column are
  all zero, then renumbers the rest.
- Analyses use the **Boolean product** internally for reachability
  questions: (A ⊗ B)[i][j] is 1 exactly when some k has A[i][k] = B[k][j]
  = 1.

## 5. File formats

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
  optional per-node *Vector blocks are controlled by a checkbox (remembered
  in the settings).
- Legacy files store face paths with platform-specific separators; Agna
  2.1.3 resolves them transparently, and any node without a stored face
  falls back to the red-bullet default.

## 6. New in 2.1.3

- **Modern interface**: FlatLaf look and feel (light by default, with a
  live picker in Preferences ▸ Look and Feel; Native and the classic
  themes are available, and a `Classic Toolbar Icons` setting restores
  the old icon set). The sociomatrix grid and its selection are visible
  in every look, and node faces never disappear at launch — missing faces
  revert to the bundled red bullet.
- **Merge Network** (File ▸ Merge Network...): join two networks by
  actor name with a Sum / Maximum / Keep policy, as described in
  section 4.
- **Session log**: the Output pane records every operation with a
  timestamp (`> [time] ...` lines) — open, new, save, transforms, merge —
  so you can reconstruct what was done to a network. Use
  View ▸ Hide/Show Output to toggle the pane.
- **Export/import family**: Excel import (including Numbers `.xlsx`),
  GraphML, GML and GraphSON round-trips — see section 5.
- **Full Analysis** (Analysis ▸ Full Analysis): runs the whole battery
  (basic statistics, density, cohesion, degrees, status, prestige,
  distance measures) in one pass, with a skip note when the network is
  disconnected.
- Analyses run in the background with a cancellable progress dialog.

## 7. Menu reference

- **File**: New Network; Open Network; Merge Network...; Save Network
  As...; Save Network; New From Chain...; Quit.
- **Edit**: Cut / Copy / Paste / Delete / Select All (spreadsheet cell
  editing).
- **Network**: Title...; Add Nodes...; Delete Selected Node; Add Scalar...;
  Scalar Multiplication...; Transpose; Symmetrize...; Normalize
  (Binarize)...; Remove Outsiders; Renumber Nodes...; Square Matrix.
- **Analysis**: Basic Description; Sociometrics (Nodal Degree, Indegree,
  Outdegree, Density, Cohesion, Emission Degree, Reception Degree,
  Determination Degree, Sociometric Status); Distance (Eccentricity,
  Diameter, Geodesic Matrix, Shortest Paths..., All Shortest Paths);
  N-Cliques; Centrality (Bavelas-Leavitt, Closeness, Fareness,
  Betweenness, Prestige); Full Analysis.
- **View / Output**: Network Viewer (and its Close item); Hide/Show
  Output; Open/Save/Clear the Output pane.
- **Preferences**: Working Directory...; Save Settings As Default...;
  Look and Feel.
- **Help**: Contents...; About Agna...

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
  those formats directly.
- **Gephi won't import my Pajek file** — ensure the network export has
  comment lines disabled (they are by default; the option is remembered
  for the Pajek format).
- **Node faces are missing at launch** — a missing or `-` face now falls
  back to the red-bullet default; if you still see none, check that the
  settings file does not pin an invalid `Default Node Face`.
- **An analysis prints a warning instead of numbers** — distance-based
  measures (eccentricity, closeness, fareness, betweenness, geodesics)
  need a connected network; that is intentional, not a bug.
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

Copyright (C) 2002-2026 Marius Ion Bența. Website (provisional):
https://www.netanalysis.co.uk