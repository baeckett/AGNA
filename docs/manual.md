# Agna User Manual — 2.1.7

## 1. Quick start

- **Open a network**: File ▸ Open Network (or use the toolbar). Agna reads
  its own format (`.agn`), text tables (`.txt`, `.csv`), Pajek (`.net`),
  Excel (`.xls` and `.xlsx`, including files saved by Apple Numbers),
  GraphML (`.graphml`), GML (`.gml`), GraphSON (`.json`), and UCINET DL (`.dl`). See
  [File formats](#6-file-formats).
- **Enter data**: with a new network (File ▸ New Network) the sociomatrix
  grid appears; type tie values into the cells. A value of 0 means "no
  tie". Pre-defined theoretical network generators (Watts-Strogatz,
  Barabasi-Albert, SBM, Erdos-Renyi, Organigram, lattices) are also
  available.
- **Analyse**: pick an analysis from the Analysis menu or run a consolidated
  category battery (Sociometrics, Centrality, Distances, Clustering, Full
  Analysis). Reports appear in the Output pane, which doubles as a session log
  (every operation is recorded with a timestamp) and supports one-click table
  export to CSV, TSV, JSON, or HTML. Every computation is explained in
  [Methodology](#4-methodology--what-exactly-is-computed-read-before-publishing-results).
- **Automate and script**: use the `agna` command-line interface (`agna-cli`)
  for headless scripting, batch matrix conversions, structural transformations,
  Monte Carlo hypothesis testing, benchmarking, and parity checks against
  NetworkX or R.
- **Move data to other tools**: use File ▸ Save Network As... and choose
  GraphML, GML, GraphSON, Pajek, or UCINET DL — Gephi, Cytoscape, NetworkX,
  igraph and R SNA packages all read at least one of these.
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
  indegree = number of incoming ties; nodal degree = the pair or total sum.
- **Geodesic**: the shortest path between two actors, measured in hops.
- **Outsider / isolate**: an actor with an all-zero row *and* an all-zero
  column (neither emits nor receives).
- **Centralization**: a macro graph-level index measuring the extent to which
  ties or shortest paths are concentrated around a single central hub actor
  compared to the theoretical maximum (the star graph).
- **Assortativity**: the correlation of degrees between linked actors —
  assortative networks connect high-degree hubs to other hubs, while
  disassortative networks connect hubs to peripheral nodes.
- **Modularity & Communities**: a partition of vertices into dense clusters
  with high internal tie density and sparse bridging ties to other clusters,
  quantified by Newman's modularity score Q.
- **Structural Equivalence**: the degree to which two actors occupy identical
  relational positions by connecting to identical partners with identical tie
  strengths.
- **Walk vs path**: a walk may repeat actors; a path may not. Matrices
  square to count walks of length 2.

## 3. Menu reference

Items that compute something link to their explanation; follow the link
to read exactly what is calculated.

- **File**: [New Network...](#12-pre-defined-network-models-and-academic-references); Open Network; [Open Sample Network & Case Study...](#13-curated-benchmark-case-studies--guided-exercises); [Merge Network...](#merge-network);
  Save Network As... (the format list is documented in
  [File formats](#6-file-formats)); Save Network; New From Chain...; Quit.
- **Edit**: Cut / Copy / Paste / Delete / Select All (spreadsheet cell
  editing).
- **Network**:
  - Title...; Add Nodes...; Delete Selected Node — bookkeeping, no
    computation.
  - [Add Scalar...](#add-scalar) — shifts all off-diagonal values by a constant.
  - [Scalar Multiplication...](#scalar-multiplication) — scales all off-diagonal values by a factor.
  - [Transpose](#transpose) — mirrors the matrix across the diagonal.
  - [Symmetrize...](#symmetrize) — makes the matrix symmetric; choose among 13 rules.
  - [Normalize (Binarize)...](#normalize-binarize) — dichotomise to 0/1, or threshold a weighted matrix.
  - [Dichotomize / Threshold Ties...](#dichotomize--threshold-ties) — binary cut-off at custom threshold.
  - [Zero Diagonal (Remove Self-Loops)](#zero-diagonal-remove-self-loops) — resets all diagonal elements to 0.
  - [Set Diagonal to Value...](#set-diagonal-to-value) — sets all diagonal elements to a constant.
  - [Extract Ego Network...](#extract-ego-network) — extracts the 1-hop or 2-hop ego subnetwork for an actor.
  - [Network Algebra (Combine Networks)...](#network-algebra-combine-networks) — element-wise addition, subtraction, intersection min, or Hadamard product.
  - [Reorder Sociomatrix / Sort Actors...](#reorder-sociomatrix--sort-actors) — permute matrix by Name, Degree, Indegree, or Outdegree.
  - [Remove Outsiders](#remove-outsiders) — delete isolates.
  - Renumber Nodes... — replaces names with numbers.
  - [Square Matrix](#square-matrix) — M×M, counting two-step walks.
- **Analysis**:
  - [Full Analysis Battery...](#full-analysis-battery) — configurable execution of all analytical modules with presets.
  - [Sociometrics & Basic Battery...](#sociometrics--basic-battery) — macro parameters, density, dyad census, Newman assortativity, and valued/weighted metrics.
  - [Centrality & Prestige Battery...](#centrality--prestige-battery) — degree, geodesic (betweenness, closeness, fareness, Bavelas), prestige, and spectral centralities, plus the Freeman Centralization summary table.
  - [Distances & Geodesics Battery...](#distances--geodesics-battery) — geodesic matrix, eccentricity, diameter, and shortest paths.
  - [Clustering & Cohesion Battery...](#clustering--cohesion-battery) — Watts-Strogatz local clustering, Newman transitivity, k-core, cohesion, maximal cliques, Louvain / Girvan-Newman community detection, and structural equivalence.
  - [Social Capital & Structural Holes (Burt)...](#social-capital--structural-holes-burt) — constraint, effective size, efficiency, and hierarchy.
  - [Connectivity & Articulation Points...](#connectivity-articulation-points-and-bridges) — connected components, cut-vertices, and bridges.
  - [Triad & Dyad Census (Holland-Leinhardt)...](#triad--dyad-census-holland-leinhardt) — complete 16 MAN triad census and reciprocity.
  - [Spectral Centralities](#spectral-centralities) — Eigenvector (Bonacich), Bonacich Power c(alpha, beta), PageRank.
  - [Structural Archetypes Diagnostic...](#structural-archetypes-diagnostic) — test empirical networks against small-world, scale-free, homophily, and hierarchy theories.
  - [Hypothesis Testing (Null Models)...](#hypothesis-testing-with-monte-carlo-null-models) — Monte Carlo significance tests with Bernoulli Erdos-Renyi and degree-preserving configuration models.
  - [Analysis Advisor ("What Should I Compute?")...](#analysis-advisor--what-should-i-compute) — beginner wizard mapping research questions to recommended analytical batteries.
  - [Algorithm Benchmarks...](#algorithm-benchmarks-suite) — empirical execution time and scaling benchmarks.
  - [Export Analysis Table...](#export-analysis-table) — export any displayed report table to CSV, TSV, JSON, or HTML.
- **View / Output**: Network Viewer (and its Close item); Hide/Show
  Output; Open/Save/Clear the Output pane (the [session log](#session-log)).
- **Preferences**: Working Directory...; Save Settings As Default...;
  Look and Feel (FlatLaf Light, FlatLaf Dark, Native, Classic).
- **Help**: Contents...; [SNA Quick Reference & Concept Cheat-Sheet...](#sna-quick-reference--concept-cheat-sheet); [Analysis Advisor...](#analysis-advisor--what-should-i-compute); [Guided Case Studies...](#13-curated-benchmark-case-studies--guided-exercises); About Agna...

## 4. Methodology — what exactly is computed (read before publishing results)

### Density

Directed convention: arcs / (n · (n − 1)). For a symmetric matrix the
undirected reading (edges / (n · (n − 1)/2)) yields the same value; Agna
prints both labels explicitly, and warns when the matrix is asymmetric so
no undirected density exists.

### Nodal degree and direction

Outdegree d⁺(i) = Σⱼ A[i][j]; indegree d⁻(j) = Σᵢ A[i][j]. Emission
degree and reception degree are the valued/weighted variants (they sum the tie
*values*, not just the non-zero cells).

### Freeman (1979) Global Network Centralization

Network centralization characterizes the degree of inequality or concentration
of centrality across the entire network, benchmarked against the theoretically
most centralized graph of the same order: the star graph (Freeman, 1979).

General centralization formula:
`C_X = Σᵢ [C_X(v*) − C_X(v_i)] / max_possible_sum`

where `v*` is the actor with the highest centrality in the network.

- **Degree Centralization (C_D)**:
  - Undirected networks: `C_D = Σᵢ [d(v*) − d(v_i)] / [(n − 1)(n − 2)]`
  - Directed in-degree: `C_D(In) = Σᵢ [d_in(v*) − d_in(v_i)] / [(n − 1)²]`
  - Directed out-degree: `C_D(Out) = Σᵢ [d_out(v*) − d_out(v_i)] / [(n − 1)²]`
  - Directed total degree: `C_D(Total) = Σᵢ [d_tot(v*) − d_tot(v_i)] / [(n − 1)(n − 2)]`
  - Range: `[0.0, 1.0]`. Exactly 1.0 for the star network; 0.0 for complete or regular graphs.
- **Betweenness Centralization (C_B)**:
  `C_B = Σᵢ [C_B(v*) − C_B(v_i)] / (n − 1)`
  where `C_B` is raw Brandes betweenness, and the denominator represents the maximum possible sum of betweenness differences achieved uniquely by a star graph center `(n − 1)²(n − 2) / 2`.
- **Closeness Centralization (C_C)**:
  `C_C = Σᵢ [C'_C(v*) − C'_C(v_i)] / [ (n − 2)(n − 1) / (2n − 3) ]`
  where `C'_C(i) = (n − 1) / Σⱼ d(i, j)` is normalized closeness centrality.
- **Bavelas-Leavitt Centralization (C_Bav)**:
  Graph-level centralization of Bavelas index benchmarked against star topology.

*Note on disconnected networks*: Geodesic-based centralization measures (betweenness, closeness, Bavelas) require a single connected component. On disconnected graphs, Agna reports `N/A (Disconnected)` and notes that distances between components are undefined.

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
connected network (see above):
- **Closeness**: `(n − 1) / Σⱼ d(i, j)`
- **Fareness**: `Σⱼ d(i, j)` (sum of geodesic distances to all other nodes)
- **Bavelas-Leavitt**: `[ Σᵢ Σⱼ d(i, j) ] / [ 2 · Σⱼ d(i, j) ]`

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

### Newman Degree Assortativity & Mixing Regimes

Newman's degree assortativity coefficient `r` measures the Pearson correlation
of degrees at the endpoints of each edge:
`r = (Σ_e j_e k_e / M − [Σ_e (j_e + k_e) / (2M)]²) / (Σ_e (j_e² + k_e²) / (2M) − [Σ_e (j_e + k_e) / (2M)]²)`
where `M` is total edges, and `j_e, k_e` are degrees of vertices linked by edge `e`.
- Range: `[-1.0, 1.0]`. Positive `r` (assortative) indicates high-degree nodes link preferentially to high-degree nodes (common in social networks). Negative `r` (disassortative) indicates hubs link to low-degree leaves (common in technological and biological networks).
- Directed networks: Agna computes four distinct directed mixing coefficients:
  - `Out-In`: outgoing degree of source vs. incoming degree of target
  - `In-In`: incoming degree of source vs. incoming degree of target
  - `Out-Out`: outgoing degree of source vs. outgoing degree of target
  - `In-Out`: incoming degree of source vs. outgoing degree of target

### Community Detection (Louvain Modularity & Girvan-Newman)

Community detection identifies cohesive subgroups with high internal tie
density and sparse external ties.
- **Newman Modularity (Q)**:
  `Q = (1 / 2m) Σ_{ij} [ A_{ij} − (k_i k_j / 2m) ] δ(c_i, c_j)`
  where `m` is total edge weight, `k_i` is degree, and `δ(c_i, c_j) = 1` if nodes belong to the same community.
- **Louvain Heuristic**: Fast multi-phase greedy algorithm optimizing modularity locally, then aggregating communities into meta-nodes and repeating until `ΔQ ≤ 0`.
- **Girvan-Newman Divisive Algorithm**: Iteratively computes Brandes edge betweenness, removes the edge with highest betweenness, tracks modularity `Q` of each partition, and selects the division with peak modularity.

### Structural Equivalence & Blockmodeling

Two actors are structurally equivalent if they have identical ties to and from
all other actors in the network (Lorrain & White, 1971; Burt, 1976).
- **Euclidean Distance**:
  `d(i, j) = √[ Σ_k (A_{ik} − A_{jk})² + Σ_k (A_{ki} − A_{kj})² ]`
  A distance of 0.0 indicates perfect structural equivalence.
- **Pearson Profile Correlation**:
  Correlation between the combined emission and reception profiles of actor `i` and actor `j`.
- **Blockmodel Partitioning**:
  Hierarchical average-linkage clustering groups actors into `K` positional blocks.
- **Image Matrix**:
  A `K × K` density table reporting the tie density between positions. Ties in the image matrix are dichotomized against the overall network density threshold `α`.

### Valued & Weighted Network Metrics

For networks with non-binary tie weights:
- **Node Strength**: In-strength `s⁻(i) = Σⱼ A[j][i]`, Out-strength `s⁺(i) = Σⱼ A[i][j]`, Total strength `s(i) = s⁻(i) + s⁺(i)`.
- **Valued Network Density**: `Δ_w = Σ_{ij} A[i][j] / [ n(n − 1) · max(A) ]`.
- **Barrat et al. (2004) Weighted Local Clustering**:
  `C_w(i) = [ 1 / (s_i (k_i − 1)) ] Σ_{j, h} [ (w_{ij} + w_{ih}) / 2 ] a_{ij} a_{ih} a_{jh}`
  Quantifies clustering while weighting triangles by the intensity of local interactions.

### Watts-Strogatz Local Clustering & Newman Transitivity

- **Watts-Strogatz Local Clustering (C_i)**:
  `C_i = 2 T_i / [ k_i (k_i − 1) ]`
  where `T_i` is the number of triangles containing node `i`, and `k_i` is its degree. Mean clustering `⟨C⟩` averages `C_i` over all nodes with `k_i ≥ 2`.
- **Newman Global Transitivity (T)**:
  `T = 3 · Triangles / Connected_Triplets`
  Measures the overall fraction of two-step paths that are closed into triangles.

### k-Core Decomposition

The `k`-core of a graph is the maximal induced subgraph in which every vertex
has degree at least `k` (Seidman, 1983).
- Computed using Batagelj & Zaveršnik's linear `O(V + E)` core percolation algorithm.
- Decomposes the network into nested core shells: `k = 1, 2, ..., k_max`.
- High `k_i` indicates membership in the densely interconnected central core.

### Triad & Dyad Census (Holland-Leinhardt)

- **Dyad Census**: Categorizes all `n(n − 1)/2` pairs into Mutual (M), Asymmetric (A), and Null (N) dyads. Dyadic reciprocity = `2M / (2M + A)`.
- **Triad Census**: Enumerates all 16 mutually exclusive directed triad configurations in the Holland-Leinhardt (MAN) taxonomy (003, 012, 102, 021D, 021U, 021C, 111D, 111U, 030T, 030C, 201, 120D, 120U, 120C, 210, 300).

### Social Capital & Structural Holes (Burt)

Ronald Burt's (1992) structural holes theory posits that actors bridging
disconnected clusters enjoy informational and brokerage advantages.
- **Dyadic Constraint**: `c_{ij} = (p_{ij} + Σ_q p_{iq} p_{qj})²`
- **Network Constraint (C_i)**: `C_i = Σ_j c_{ij}`. Low constraint indicates abundant structural holes.
- **Effective Size (ES_i)**: `ES_i = Σ_j [ 1 − Σ_q p_{iq} m_{jq} ]` for `j ≠ i, q ≠ i, j`.
- **Efficiency**: `E_i = ES_i / k_i`.
- **Hierarchy (H_i)**: Degree to which constraint is concentrated in a single alter.

### Connectivity, Articulation Points and Bridges

- **Weakly and Strongly Connected Components**: Computed via BFS and Tarjan's linear `O(V + E)` algorithm.
- **Articulation Points (Cut-Vertices)**: Vertices whose removal increases the number of connected components.
- **Bridges**: Edges whose deletion partitions a component into two disconnected sets.

### Spectral Centralities

- **Eigenvector Centrality (Bonacich, 1972)**:
  `A x = λ_1 x`. Uses shifted power iteration `(A + I)` guaranteeing unique Perron-Frobenius positive convergence without bipartite oscillation.
- **Bonacich Power Centrality**:
  `c(α, β) = α (I − β A)⁻¹ A 1`. Tunable attenuation `β`: positive `β` models social prestige / influence; negative `β` models exchange bargaining power.
- **PageRank Centrality (Page et al., 1999)**:
  Stationary distribution of random walk with restart: `PR(u) = (1 − d)/n + d Σ_{v ∈ In(u)} PR(v) / OutDeg(v)`. Configurable damping `d` (default 0.85).

### Structural Archetypes Diagnostic

Classifies an empirical network against four classic theoretical paradigms:
1. **Small-World / Weak Ties** (Watts-Strogatz, 1998; Humphries-Gurney, 2008): Small-world quotient `σ = (C / C_rand) / (L / L_rand) > 1`.
2. **Cumulative Advantage / Scale-Free** (Barabási-Albert, 1999; Price, 1976): Power-law MLE exponent `γ`, degree Gini inequality, and hub ratio `k_max / ⟨k⟩`.
3. **Homophily & Echo Chambers** (McPherson et al., 2001; Newman, 2006): Modularity `Q` and assortativity `r`.
4. **Formal Hierarchy & Authority** (Weber, 1922; Krackhardt, 1994): Krackhardt's dimensions (Connectedness, Graph Hierarchy, Graph Efficiency, LUB).

### Hypothesis Testing with Monte Carlo Null Models

Evaluates whether an observed structural statistic (density, clustering, reciprocity, assortativity) is statistically significant or an artifact of random chance:
- **Null Models**:
  - `Bernoulli Erdos-Renyi G(n, p)`: fixed density, edges placed uniformly at random.
  - `Degree-Preserving Configuration Model`: stub-matching preserving exact in- and out-degree sequences.
  - `Dyad Census Randomization`: preserves exact mutual, asymmetric, and null counts.
- **Outputs**: Empirical distribution mean `μ`, standard deviation `σ`, Z-score `Z = (X_obs − μ) / σ`, and two-tailed empirical p-value `p = (#{|X_sim − μ| ≥ |X_obs − μ|} + 1) / (R + 1)` across `R` iterations.

### Algorithm Benchmarks Suite

Measures empirical execution time (ms), throughput (ops/sec), and Big-O scaling across 10 foundational graph algorithms (Brandes betweenness, Freeman closeness, degree centralities, Watts-Strogatz clustering, Newman transitivity, Bron-Kerbosch cliques, all-pairs geodesics, graph diameter, and path routing).

### N-Cliques

Enumerated with the Bron–Kerbosch (Tomita pivot) algorithm; capped at 1000 reported cliques with explicit truncation notices if reached.

### Export Analysis Table

Every analytical table in the modern HTML report contains an `[Export Table (CSV/JSON)]` action link opening the Export Table Dialog for previewing, copying to clipboard, or saving to CSV (RFC 4180), TSV, JSON, or HTML.

## 5. Matrix operations — formulas

All operations write back into the current sociomatrix. The diagonal (no
self-loops) is preserved as 0 by every operation below unless explicitly
altered by diagonal operations.

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

### Normalize (Binarize)

Two options:
- *Binary*: M′[i][j] = 1 if M[i][j] ≠ 0, else 0.
- *By threshold* (value t): M′[i][j] = 1 if M[i][j] > t, else 0.

### Dichotomize / Threshold Ties

M′[i][j] = 1 if M[i][j] ≥ threshold, else 0. Replaces tie values with a clean binary adjacency matrix based on a user-defined threshold.

### Slice Ties

M′[i][j] = M[i][j] if min_val ≤ M[i][j] ≤ max_val, else 0. Retains ties within the specified strength window while setting ties outside the range to 0.

### Zero Diagonal (Remove Self-Loops)

M′[i][i] = 0 for all i. Clears reflexive self-ties across all actors.

### Set Diagonal to Value

M′[i][i] = c for all i. Sets reflexive diagonal values to a user-chosen scalar.

### Square Matrix

M′ = M × M, i.e. M′[i][j] = Σₖ M[i][k] · M[k][j]. The entry (i, j)
counts the walks of length 2 from i to j — the number of two-step
intermediaries.

### Extract Ego Network

Extracts the 1-hop or 2-hop induced subnetwork centered on a chosen focal
actor (ego). The subnetwork includes ego, all alter nodes within the specified
hop distance, and all inter-alter ties among them.

### Network Algebra (Combine Networks)

Combines the active network with a second network of identical size:
- **Addition**: M′[i][j] = A[i][j] + B[i][j]
- **Difference**: M′[i][j] = A[i][j] − B[i][j]
- **Intersection Minimum**: M′[i][j] = min(A[i][j], B[i][j])
- **Hadamard Product**: M′[i][j] = A[i][j] · B[i][j]

### Reorder Sociomatrix / Sort Actors

Permutes rows and columns simultaneously to reorder the sociomatrix:
- By **Name**: alphabetical (A–Z) or reverse (Z–A).
- By **Degree**: ascending or descending total nodal degree.
- By **In-Degree**: ascending or descending incoming reception volume.
- By **Out-Degree**: ascending or descending outgoing emission volume.

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
| Excel | `.xls`, `.xlsx` | Apache POI (`.xls`) and the built-in `.xlsx` reader |
| GraphML | `.graphml` | XML graph format of Gephi, NetworkX, igraph, Cytoscape |
| GML | `.gml` | the classic text format of igraph / NetworkX |
| GraphSON | `.json` | JSON graph format (TinkerPop family, Gephi, Cytoscape) |
| UCINET DL | `.dl` | standard UCINET data language format (fullmatrix, edgelist1, nodelist1) |

Interoperability notes:

- **UCINET DL (`.dl`)**: Imports and exports standard UCINET files. Supports `format = fullmatrix`, `format = edgelist1`, and `format = nodelist1`. Quoted labels, embedded node labels, and matrix diagonals are preserved.
- **GraphML / GML / GraphSON** export the matrix as a *directed* graph: nodes in their file order with their names, every non-zero tie as an edge carrying its value as weight. On import, missing weights default to 1, self-loops are ignored (the diagonal stays zero), and node order defines the matrix indexing.
- **GraphSON** import accepts both the `{"graph": {vertices, edges}}` wrapper and a bare `{vertices, edges}` document; weights may use the key `value` or `weight`.
- **Excel**: Numbers files (.xlsx) open directly; saving writes `.xls`, which Excel and Numbers both read.
- **Pajek**: the export omits comment lines so Gephi imports cleanly; optional per-node Vector blocks are controlled by a checkbox.
- Legacy files store face paths with platform-specific separators; Agna resolves them transparently, and any node without a stored face falls back to the red-bullet default.

## 7. Educational features & beginner's guide (New in 2.1.7)

Version 2.1.7 introduces a comprehensive educational onboarding suite designed
to make Social Network Analysis friendlier, more intuitive, and accessible for
students, instructors, and researchers new to graph theory.

- **Interactive Educational Tooltips Across All Computational Batteries**:
  Every checkbox and option in Agna's battery dialogues (`CentralityBatteryDialog`,
  `ClusteringBatteryDialog`, `SociometricsBatteryDialog`, `DistanceBatteryDialog`,
  `FullAnalysisDialog`, `HypothesisTestingDialog`) now features a rich, structured
  3-part micro-guide rendered directly on hover:
  1. *What it measures*: Mathematical intuition and theoretical definition.
  2. *Why use it / Sociological meaning*: Substantive interpretation (e.g., power, brokerage, cohesion, gatekeeping).
  3. *Interpretation & Complexity*: Practical benchmarks (e.g., Q > 0.35 for communities, C_D = 1.0 for star topologies) and computational complexity caveats.
- **Automated Plain-English Structural Narrative ("Key Takeaways & Beginner's Guide")**:
  Full Analysis reports and analytical batteries now automatically generate an
  executive structural summary (`com.bentza.sna.report.NarrativeSummaryGenerator`)
  translating abstract graph metrics into natural language sociology:
  - *Group Saturation & Density*: Explains the proportion of realized connections and communication bandwidth.
  - *Network Topology & Components*: Clarifies component structure, reachability, and potential information bottlenecks.
  - *Hierarchy vs Equality*: Interprets Freeman (1979) degree centralization, contrasting star-like autocratic leadership against decentralized egalitarian networks.
  - *Triadic Trust & Clustering*: Explains Newman global transitivity and local clustering as measures of social capital and triad closure ("a friend of my friend is my friend").
  - *Reciprocity & Directionality*: Analyzes mutual versus unreciprocated ties.
  - *Recommended Next Steps*: Suggests actionable next analyses (e.g., running Burt's structural holes if betweenness is concentrated, or Louvain community detection if transitivity is elevated).
- **Curated Benchmark Case Studies & Dataset Browser (`SampleNetworksDialog`)**:
  Accessible via **File ▸ Open Sample Network & Case Study...** and **Help ▸ Guided Case Studies...**.
  Features seminal historical datasets with native coordinates and guided questions:
  Padgett's Florentine Families (1434), Zachary's Karate Club (1977), Davis's Southern Women (1941),
  the classical Fathers of Sociology network, and canonical 10-node Star and Circle topologies.
  See [Curated benchmark case studies](#13-curated-benchmark-case-studies--guided-exercises).
- **Analysis Advisor ("What Should I Compute?") Decision Wizard (`AnalysisAdvisorDialog`)**:
  Accessible via **Analysis ▸ Analysis Advisor...** and **Help ▸ Analysis Advisor...**.
  Helps researchers map empirical research questions (*"Who are the leaders?", "Who controls information?", "Are there factions?", "Is the network hierarchical?", "Are findings statistically significant?"*) directly to recommended analytical batteries with one-click execution.
- **In-App SNA Concept Reference & Formula Cheat-Sheet (`SnaCheatSheetDialog`)**:
  Accessible via **Help ▸ SNA Quick Reference & Concept Cheat-Sheet...**.
  Searchable dictionary containing over 27 formal metric definitions, formulas, sociological significance, and category filters.
- **Interactive Node Hover Inspector in Network Viewer (`com.bentza.sna.net.NodeArea`)**:
  Hovering over any node in the visual Network Viewer displays a clean floating badge with the actor's name, index, total degree, and in/out-degree breakdown.

### Previous release highlights (2.1.6)

- **Freeman (1979) Global Network Centralization**:
  Complete centralization suite computing degree (C_D), betweenness (C_B), closeness (C_C), and Bavelas (C_Bav) graph centralization benchmarked against star graph maximums. In Agna Core (`AgnaLib.outFreemanCentralization`), Desktop UI (Centrality Battery), and CLI (`agna analyse FILE --metric freeman`).
- **Newman Degree Assortativity & Mixing Regimes**:
  Degree assortativity correlation r and four directed mixing regimes (out-in, in-in, out-out, in-out).
- **Louvain & Girvan-Newman Community Detection**:
  Multi-phase Louvain modularity optimization and Girvan-Newman edge-betweenness community partitioning with modularity Q metrics.
- **Structural Equivalence & Blockmodeling**:
  Euclidean distance, Pearson correlation, hierarchical complete-linkage clustering, and K × K image density matrices.
- **Valued & Weighted Network Metrics**:
  Node strength, valued network density, and Barrat et al. weighted local clustering.
- **Network Transformations**:
  Zero diagonal, set diagonal, dichotomize, slice, ego network extraction, network matrix algebra, and sociomatrix sorting.
- **UCINET DL Format**:
  Full import and export of UCINET `.dl` files.
- **Agna CLI Full Parity**:
  All metrics, battery reports, null models, and matrix transformations available in `agna-cli`.
- **NetworkX Parity Validation Battery**:
  Automated testing harness comparing Agna outputs directly against NetworkX.

## 8. Keyboard shortcuts

- **Ctrl+P** — add new nodes to the current network.
- **Ctrl+H** — open the Agna Help window.
- Menu items show their shortcut letters as underlined mnemonics.

## 9. FAQ / Troubleshooting

- **How do I move a network to R or Python?** — save as GraphML, GML, GraphSON, or UCINET DL; igraph, statnet, and NetworkX read those formats directly.
- **Why does an analysis report N/A or a warning?** — geodesic-based measures (betweenness, closeness, fareness, eccentricity, diameter) require a connected network; on disconnected networks, distances between components are undefined.
- **Can I run analyses from scripts without opening the GUI?** — yes, use `agna analyse FILE [METRICS...]` or `agna convert IN OUT`.
- **How do I export tables to Excel or SPSS?** — click `[Export Table (CSV/JSON)]` above any report table, or use `Analysis ▸ Export Analysis Table...`.
- **Gephi won't import my Pajek file** — ensure comment lines are disabled during export (default).
- **Node faces are missing at launch** — missing faces fall back to the bundled red bullet.

## 10. Version history

- **2.1.7** — Educational scaffolding and beginner onboarding suite: interactive Educational Tooltips across all computational battery dialogues (centrality, clustering, sociometrics, distance, full analysis, null models); automated Plain-English Structural Narrative generator ("Key Takeaways & Beginner's Guide") in analytical reports; curated Benchmark Case Studies & Dataset Browser (`SampleNetworksDialog`) with historical context and guided exercises for Padgett Florentine Families, Zachary Karate Club, Davis Southern Women, and canonical benchmark topologies; Analysis Advisor ("What Should I Compute?") decision wizard (`AnalysisAdvisorDialog`) mapping research questions to recommended analyses; in-app searchable SNA Concept Reference and formula cheat-sheet (`SnaCheatSheetDialog`); interactive Node Hover Inspector in visual Network Viewer (`NodeArea`).
- **2.1.6** — Freeman (1979) global centralization suite ($C_D, C_B, C_C, C_{\text{Bav}}$); Newman degree assortativity and directed mixing regimes; Louvain modularity and Girvan-Newman community detection; structural equivalence and blockmodeling; valued/weighted network metrics; network transformation suite (zero/set diagonal, dichotomize, slice, ego extraction, algebra, sorting); UCINET DL format; Agna CLI complete parity; NetworkX parity battery and verification suite.
- **2.1.5** — pre-defined synthetic network generators (Watts-Strogatz, Barabási-Albert, Stochastic Block Model, Erdős-Rényi, Hierarchical Tree, hexagonal/hexagram/square lattices); 5 new layout engines; algorithm benchmarking suite; structural archetypes diagnostic; statistical hypothesis testing with Monte Carlo null models; modern HTML table reporting framework with export dialog.
- **2.1.4** — spectral centralities (Bonacich eigenvector, Bonacich power, PageRank); Ronald Burt's structural holes (constraint, effective size, efficiency, hierarchy); Holland-Leinhardt 16 triad census; k-core decomposition; Watts-Strogatz local clustering; Newman global transitivity; connectivity and articulation points.
- **2.1.3** — open-source revival of Agna 2.1.2: Maven/JDK 17 build, namespace `com.bentza.sna`, engine `AgnaLib`; background analyses with cancellation; scalable clique search; Pajek and Excel import fixes; Full Analysis; GraphML/GML/GraphSON import and export; Merge Network; session log; FlatLaf interface; modernized Help.
- **2.1.2** — the last closed-source release by Marius Ion Bența.

## 11. Cite AGNA

If you use AGNA in your research, please cite it as:

Bența, M. I. (2026). AGNA: Applied Graph and Network Analysis Open
Source (Version 2.1.7) [Computer software].
https://doi.org/10.5281/zenodo.22708199

BibTeX entry:

```
@software{benta_2026_agna,
  author    = {Bența, Marius I.},
  title     = {{AGNA}: Applied Graph and Network Analysis Open Source},
  version   = {2.1.7},
  year      = {2026},
  publisher = {AGNA Open Source},
  doi       = {10.5281/zenodo.22708199},
  url       = {https://github.com/baeckett/AGNA}
}
```

The DOI is registered with Zenodo; the `CITATION.cff` file at the
repository root carries the same metadata in machine-readable form.

For reproducibility, report the AGNA version and the analysis options
used.

Copyright 2001–2026 Marius Ion Bența. Website:
https://www.netanalysis.co.uk

## 12. Pre-defined network models and academic references

Agna 2.1.5 introduces synthetic and empirical benchmark network generators
accessible via **File ▸ New Network...**. Each model is constructed using
established mathematical formulations and assigned a natural default layout in
the Network Viewer.

### Watts-Strogatz Small-World Model

The Watts–Strogatz model interpolates between a regular lattice and a random
graph. It begins with a regular ring lattice of $n$ vertices where each node is
connected to its $k$ nearest neighbors ($k/2$ on each side, with $k$ even).
With rewiring probability $\beta \in [0, 1]$, each edge is independently
disconnected from its target and reconnected to a uniformly chosen node,
preventing self-loops and duplicate edges. When $\beta = 0$, the network is a
clustered regular lattice; as $\beta$ increases, small-world properties emerge
(high clustering combined with very short average path lengths), and at
$\beta = 1$ the network approaches a random graph.

- **Parameters in Agna**: $N$ (nodes), $k$ (neighbors per node), $\beta$ (rewiring probability).
- **Default layout**: Circular layout, displaying the ring boundary and transverse chord shortcuts.
- **Reference**:
  Watts, D. J., & Strogatz, S. H. (1998). Collective dynamics of 'small-world' networks. *Nature*, 393(6684), 440–442. https://doi.org/10.1038/30918
- **Relevance**: Foundational paper introducing the $WS(n, k, \beta)$ model, quantifying clustering coefficients and characteristic path lengths across empirical social, neural, and power systems.

### Barabasi-Albert Scale-Free Model

The Barabási–Albert model generates networks with power-law degree
distributions ($P(k) \sim k^{-\gamma}$, with $\gamma \approx 3$) through
continuous network growth and preferential attachment ("the rich get richer").
Starting from an initial seed clique of $m + 1$ vertices, each new incoming
vertex connects to $m$ existing vertices with probability proportional to
their current degree:
$\Pi(i) = k_i / \sum_j k_j$. Nodes that enter early or acquire early ties
develop into central hubs with disproportionately high degree centrality.

- **Parameters in Agna**: $N$ (nodes), $m$ (attachment links per new node).
- **Default layout**: Concentric layout, placing high-degree core hubs at the center and periphery nodes on outer rings.
- **Reference**:
  Barabási, A.-L., & Albert, R. (1999). Emergence of scaling in random networks. *Science*, 286(5439), 509–512. https://doi.org/10.1126/science.286.5439.509
- **Relevance**: Seminal publication demonstrating that growth and preferential attachment account for scale-free degree distributions observed in social collaboration networks, citation networks, and the World Wide Web.

### Stochastic Block Model (SBM) / Clustered Communities

The Stochastic Block Model partitions a population of $n$ actors into $K$
distinct community blocks. The probability of an edge existing between actor $i$
and actor $j$ depends solely on their block memberships: $p_{\text{in}}$ if $i$
and $j$ belong to the same block, and $p_{\text{out}}$ if they belong to
different blocks. Setting $p_{\text{in}} > p_{\text{out}}$ generates modular,
community-structured networks widely used to benchmark community detection,
clustering coefficients, and structural hole algorithms.

- **Parameters in Agna**: $N$ (nodes), $K$ (number of communities), $p_{\text{in}}$ (intra-community density), $p_{\text{out}}$ (inter-community density).
- **Default layout**: Clustered Community layout, positioning the $K$ modules in distinct circular orbits around the canvas center.
- **References**:
  - Holland, P. W., Laskey, K. B., & Leinhardt, S. (1983). Stochastic blockmodels: First steps. *Social Networks*, 5(2), 109–137. https://doi.org/10.1016/0378-8733(83)90021-7
  - Karrer, B., & Newman, M. E. J. (2011). Stochastic blockmodels and community structure in networks. *Physical Review E*, 83(1), 016107. https://doi.org/10.1103/PhysRevE.83.016107
- **Relevance**: Holland et al. (1983) founded the mathematical framework of stochastic blockmodels in social network analysis, moving blockmodeling from deterministic matrices to probabilistic random graphs with explicit group parameters.

### Erdos-Renyi Random Graph G(n, p)

In the classic Bernoulli random graph model $G(n, p)$, a network of $n$
vertices is formed by creating each of the possible $n(n - 1)/2$ edges
independently with fixed probability $p$. In Agna, the generator can produce
either undirected (symmetric) graphs or directed graphs (where ordered pairs
$(i, j)$ are decided independently). Degree distributions follow a binomial
distribution (approaching Poisson for large $n$ with constant mean degree
$\lambda = n \cdot p$).

- **Parameters in Agna**: $N$ (nodes), $p$ (connection probability), Symmetrical (undirected) / Directed.
- **Default layout**: Circular layout, providing an unobstructed view of chord distribution across vertices.
- **References**:
  - Gilbert, E. N. (1959). Random graphs. *The Annals of Mathematical Statistics*, 30(4), 1141–1144. https://doi.org/10.1214/aoms/1177706098
  - Erdős, P., & Rényi, A. (1959). On random graphs. I. *Publicationes Mathematicae (Debrecen)*, 6, 290–297.
  - Erdős, P., & Rényi, A. (1960). On the evolution of random graphs. *Publications of the Mathematical Institute of the Hungarian Academy of Sciences*, 5, 17–61.
- **Relevance**: Gilbert (1959) introduced the binomial $G(n, p)$ formulation where each edge occurs independently with probability $p$. Erdős and Rényi (1959, 1960) developed the rigorous mathematical foundations of random graph theory, identifying the emergence of giant components and connectivity thresholds.

### Hierarchical Tree / Corporate Hierarchy (Organigram)

The Hierarchical Tree model represents formal managerial authority and scalar
chains of command in organizational sociology. Vertices are arranged in a
$k$-ary out-tree (arborescence) with a single root executive (node 0) at the top,
branching downwards with a configurable span of control (branching factor $b$).
The model supports directed downward authority links ($i \to j$) or undirected
relations, with an optional toggle to add horizontal lateral ties connecting
peers within the same administrative branch.

- **Parameters in Agna**: $N$ (nodes), $b$ (branching factor / span of control), Directed (downward hierarchy) / Undirected, Lateral ties within level.
- **Default layout**: Hierarchical Tree Layout, using breadth-first level decomposition to place root managers at the top and subordinate teams cleanly below.
- **References**:
  - Krackhardt, D. (1994). Graph theoretical dimensions of informal organizations. In K. M. Carley & M. J. Prietula (Eds.), *Computational Organization Theory* (pp. 89–111). Hillsdale, NJ: Lawrence Erlbaum Associates.
  - Simon, H. A. (1962). The architecture of complexity. *Proceedings of the American Philosophical Society*, 106(6), 467–482.
- **Relevance**: Krackhardt (1994) formalizes the classical corporate hierarchy in social network analysis as a directed out-tree and measures organizational divergence using four graph-theoretic dimensions: Connectedness, Graph Hierarchy, Graph Efficiency, and Least Upper Bound. Simon (1962) analyzes why complex social and administrative organizations naturally decompose into hierarchic tree structures.

## 13. Curated benchmark case studies & guided exercises

Agna 2.1.7 bundles curated historical datasets designed for classroom instruction,
methodological validation, and hands-on self-study. Each sample dataset can be
opened directly via **File ▸ Open Sample Network & Case Study...** or **Help ▸ Guided Case Studies...**.

### Padgett's Florentine Families (1434) — Political Brokerage and Structural Holes

- **Historical Context**: In 15th-century Renaissance Florence, the Medici family rose to political hegemony despite having less wealth than the Strozzi and fewer patrician ancestors than the Albizzi. Padgett and Ansell (1993) demonstrated that Cosimo de' Medici's political dominance stemmed from strategic network positioning: marriage alliances placed the Medici at the center of a structural hole, bridging opposing aristocratic factions who refused to ally with one another directly.
- **File**: `samples/Padgett Florentine Families.agn` (16 families, 20 marriage alliance ties).
- **Academic Citation**:
  Padgett, J. F., & Ansell, C. K. (1993). Robust action and the rise of the Medici, 1400–1434. *American Journal of Sociology*, 98(6), 1259–1319. https://doi.org/10.1086/230190
- **Guided Exercises in AGNA**:
  1. *Betweenness Centrality*: Run **Analysis ▸ Centrality & Prestige Battery**. Observe that the Medici family achieves a normalized betweenness score of 0.522—more than double any other family—confirming their role as the indispensable political broker.
  2. *Structural Holes*: Run **Analysis ▸ Social Capital & Structural Holes (Burt)**. Note Medici's exceptionally low network constraint (0.209), proving their access to non-redundant information and power arbitrage.
  3. *Ego Network Extraction*: Use **Network ▸ Extract Ego Network...** for "Medici" to inspect their immediate marital coalition.

### Zachary's Karate Club (1977) — Faction Split and Modularity

- **Historical Context**: Wayne Zachary spent two years observing social interactions among 34 members of a university karate club. During the study, an administrative conflict over lesson fees erupted between the club's martial arts instructor (Mr. Hi / Member 1) and its president (John A. / Member 34). The club subsequently fractured into two distinct organizations. This dataset is the world's most widely cited benchmark for community detection algorithms and network fission models.
- **File**: `samples/Zachary Karate Club.agn` (34 members, 78 friendship ties).
- **Academic Citation**:
  Zachary, W. W. (1977). An information flow model for conflict and fission in small groups. *Journal of Anthropological Research*, 33(4), 452–473. https://doi.org/10.1086/jar.33.4.3629752
- **Guided Exercises in AGNA**:
  1. *Community Detection*: Run **Analysis ▸ Clustering & Cohesion Battery** with **Community Detection (Louvain)** checked. Compare the discovered two communities to the historical split centered around Member 1 and Member 34.
  2. *High Modularity*: Inspect Newman's modularity score $Q$. A score above 0.35 confirms strong community clustering.
  3. *Core-Periphery*: Run **k-Core Decomposition** to discover the loyal inner circles ($k = 4$) surrounding each leader versus the peripheral fringe.

### Davis's Southern Women (1941) — Social Circles and Clique Overlap

- **Historical Context**: A foundational sociological and anthropological study of social stratification in Natchez, Mississippi. Allison Davis and colleagues recorded 18 women attending 14 informal social events (teas, church circles, bridge clubs) over nine months. The one-mode projection represents shared event co-attendance, illustrating how overlapping informal social circles define class solidarity and socialite status.
- **File**: `samples/Davis Southern Women.agn` (18 women, 139 co-attendance ties).
- **Academic Citation**:
  Davis, A., Gardner, B. B., & Gardner, M. R. (1941). *Deep South: A Social Anthropological Study of Caste and Class*. University of Chicago Press.
- **Guided Exercises in AGNA**:
  1. *Maximal Cliques*: Run **Analysis ▸ Clustering & Cohesion Battery** with **Maximal Cliques** enabled. Observe how the women partition into two major social cliques with several bridging liaisons (e.g., Ruth, Pearl).
  2. *High Cohesion*: Run **Analysis ▸ Sociometrics & Basic Battery** to observe the unusually high network density of high-society co-attendance.
  3. *Eigenvector Centrality*: Run **Analysis ▸ Spectral Centralities ▸ Bonacich Eigenvector** to discover which women occupy the core of the social elite.

### Fathers of Sociology — Theoretical Triad Closure

- **Historical Context**: A 3-actor theoretical network linking classical sociological thinkers (Max Weber, Émile Durkheim, and Karl Marx) with mutual and asymmetric dialogues.
- **File**: `samples/Sociologists.agn` (3 actors).
- **Guided Exercises in AGNA**:
  1. *Triad Census*: Run **Analysis ▸ Triad & Dyad Census** to examine complete triadic closure (Configuration 300).
  2. *Status & Determination*: Test Agna's proprietary Sociometric Status and Determination degree formulas.

### Canonical Benchmarks — 10-Node Star & Circle Networks

- **10-Node Star Network (`samples/10 star.agn`)**:
  - Maximizes Freeman (1979) degree, betweenness, and closeness centralization ($C_D = C_B = C_C = 1.0000$).
  - Proves the theoretical extreme of centralized autocracy and hub-dependent communication.
- **10-Node Circle Network (`samples/10 circle.agn`)**:
  - A 2-regular ring lattice where every actor connects symmetrically to exactly two neighbors.
  - Exhibits zero centralization ($C_D = C_B = C_C = 0.0000$) and serves as the baseline for Watts-Strogatz small-world rewiring experiments.
