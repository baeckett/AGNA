# Changelog

All notable changes to the **AGNA** (Applied Graph and Network Analysis Open Source) project since version 2.1.3 are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.2.0] - 2026-11-14

### Added
- **Standalone Native GraalVM AOT CLI Binaries (`agna` / `agna.exe`)**:
  - Ahead-of-Time (AOT) compiled native machine code for macOS (Apple Silicon ARM64, Intel x86_64), Linux (x86_64), and Windows (x64).
  - Starts in ~7 milliseconds with zero Java/JRE runtime requirements and minimal memory footprint (~15 MB RSS).
  - Isolated headless AWT/Swing execution paths across `Actor`, `FullNet`, `ImageStock`, and `ImageItem` to eliminate GUI/display link errors on headless servers and minimal containers.
  - Automated multi-platform GitHub Actions build workflow (`.github/workflows/native-cli.yml`).
- **Native Model Context Protocol (MCP) Server (`agna mcp`)**:
  - Native JSON-RPC 2.0 stdio server conforming to MCP specification `2024-11-05`.
  - Exposes 10 first-class tools for AI agents (Claude Desktop, Cursor, Copilot, Antigravity): `agna_validate`, `agna_info`, `agna_metrics`, `agna_advise`, `agna_narrative`, `agna_explain`, `agna_convert`, `agna_archetype`, `agna_test_null`, `agna_capabilities`.
- **Topological Advisory & Knowledge Base Engine (`agna advise`, `agna explain`, `agna narrative`)**:
  - Topology-driven analytical recommendation engine advising users and agents on suitable metrics, layout algorithms, and null models.
  - Built-in SNA definitions, mathematical formulas, and sociological caveats for 37 metrics via CLI and MCP.
  - Qualitative plain-English structural narrative synthesis of network cohesion, hierarchy, and reciprocity.
- **Desktop Toolbar & UX Modernization**:
  - Replaced legacy font dropdown with dedicated toolbar action buttons: Normalize, Extract Ego Network, Analysis Advisor, Full Analysis, Sociometric & Basic, Centrality & Prestige, and Export Analysis Table.
  - Instant splash screen dismissal on click anywhere on screen.
  - Intelligent multi-format `Save As...` dialog with automatic extension synchronization and `.agn` default.
  - Unsaved changes confirmation dialogs for both modified network models and analysis reports on exit.
  - Refined menu action titles with standard ellipses (`...`) for all dialog-triggering actions.
  - Redesigned multi-resolution macOS App Icon (`Agna.icns`) with pixel-crisp 16px/32px/64px assets for Finder List and Column views.
- **Educational Scaffolding & Case Studies Suite**:
  - Educational tooltips across computational battery dialogues (`EducationalTooltips`).
  - Curated historical benchmark case studies browser (`SampleNetworksDialog`) with Padgett Florentine Families, Zachary Karate Club, Davis Southern Women, and canonical star/circle graphs.
  - Analysis Advisor ("What Should I Compute?") wizard (`AnalysisAdvisorDialog`).
  - In-app searchable SNA Concept Reference and formula cheat-sheet (`SnaCheatSheetDialog`).
  - Interactive Node Hover Inspector in visual Network Viewer (`NodeArea`).

### Fixed
- Fixed report nesting defect where subsequent analyses were incorrectly embedded inside notes tables.
- Fixed layout alignment in Full Analysis options dialog for Extended Modules.
- Fixed coordinate handling when loading and manipulating network actors without pre-existing positions.
- Cleaned sample networks: replaced empty `random200.agn` with a 200-node Erdős–Rényi random network, removed obsolete files.

---

## [2.1.6] - 2026-10-30

### Added
- **Watts-Strogatz Local Clustering & Newman Global Transitivity (`com.bentza.sna.clustering.ClusteringMetrics`)**:
  - Implemented analytical local clustering coefficient $C_i = 2 T_i / (k_i(k_i - 1))$ for each node and network average $\langle C \rangle$ (Watts & Strogatz, 1998, *Nature*).
  - Implemented Newman global network transitivity $T = 3 \times \text{Triangles} / \text{Connected Triplets}$ (Newman, 2003, *SIAM Review*).
  - Surfaced in Swing Desktop under **Analysis ▸ Clustering & Cohesion ▸ Local Clustering (Watts-Strogatz)...** and **Global Transitivity (Triads)...** with SPSS-style analytical reports and tables.
  - Added CLI support: `agna analyse FILE --metric clustering` and `agna analyse FILE --metric transitivity`.
- **$k$-Core Decomposition (`com.bentza.sna.clustering.KCoreDecomposition`)**:
  - Implemented linear-time $\mathcal{O}(V + E)$ core percolation decomposition using Batagelj & Zaveršnik (2003) bucket sort.
  - Outputs maximum coreness $k_{\max}$, shell distribution tables, and actor-level coreness values.
  - Desktop: **Analysis ▸ Clustering & Cohesion ▸ k-Core Decomposition...**.
  - CLI: `agna analyse FILE --metric kcore`.
- **Davis & Leinhardt / Holland & Leinhardt 16 Triad Census & Dyad Census (`com.bentza.sna.census`)**:
  - Full dyadic census (Mutual, Asymmetric, Null) and dyadic reciprocity rate $2M / (2M + A)$.
  - Exhaustive enumeration of all 16 directed triad configurations ($003, 012, 102, 021D, 021U, 021C, 111D, 111U, 030T, 030C, 201, 120D, 120U, 120C, 210, 300$).
  - Triad transitivity ratio and balance diagnostics.
  - Desktop: **Analysis ▸ Clustering & Cohesion ▸ Triad & Dyad Census...**.
  - CLI: `agna analyse FILE --metric triads`.
- **Ronald Burt's Social Capital & Structural Holes (`com.bentza.sna.capital.StructuralHoles`)**:
  - Implemented Burt's (1992) network constraint $C_i = \sum_j c_{ij}$, dyadic constraint $c_{ij}$, effective size of ego network $S_i$, efficiency $E_i = S_i / k_i$, and constraint hierarchy $H_i$.
  - Desktop: **Analysis ▸ Social Capital & Structural Holes (Burt)...**.
  - CLI: `agna analyse FILE --metric holes`.
- **Connectivity, Articulation Points (Cut-Vertices) & Bridges (`com.bentza.sna.connectivity.ConnectivityAnalysis`)**:
  - Weakly connected components (undirected BFS) and strongly connected components via Tarjan's linear $\mathcal{O}(V+E)$ algorithm.
  - Hopcroft-Tarjan articulation point and bridge detection finding single structural points of failure.
  - Giant component size and share analysis.
  - Desktop: **Analysis ▸ Distance ▸ Connectivity & Articulation Points...**.
  - CLI: `agna analyse FILE --metric components`.
- **Spectral & Power Centrality Suite (`com.bentza.sna.spectral.SpectralCentrality`)**:
  - **Eigenvector Centrality**: Bonacich (1972) shifted power iteration $(A + I)$ guaranteeing strict Perron-Frobenius convergence without bipartite oscillations.
  - **Bonacich Power Centrality**: $c(\alpha, \beta)$ with user-tunable attenuation parameter $\beta$ (distinguishing influence $\beta > 0$ vs. exchange bargaining power $\beta < 0$).
  - **PageRank Centrality**: Page et al. (1999) random walk stationary distribution with configurable damping factor $d = 0.85$.
  - Desktop: **Analysis ▸ Centrality ▸ Eigenvector Centrality...**, **Bonacich Power Centrality...**, and **PageRank Centrality...**.
  - CLI: `agna analyse FILE --metric eigenvector|bonacich|pagerank`.
- **Network Transformation & Sociomatrix Editing Service (`com.bentza.sna.transform.NetworkTransformService`)**:
  - **Flexible Symmetrization Dialog**: Selectable Max (Union), Min (Intersection), Average (Mean), or Sum (Aggregate).
  - **Dichotomize & Threshold Ties**: User-defined cut-off threshold ($w \ge \theta$).
  - **Diagonal Operations**: Clean zeroing of self-loops or setting diagonal to scalar.
  - **Ego-Network Extraction**: Extracts 1-hop or 2-hop induced ego subnetworks with alter-alter ties into a new active dataset.
  - **Network Matrix Algebra**: Combine two loaded networks via Addition ($A + B$), Difference ($A - B$), Intersection Minimum ($\min(A, B)$), or Hadamard Product ($A \odot B$).
  - **Sociomatrix Permutation / Reordering**: Sorts rows and columns by Name (A-Z/Z-A), Total Degree, In-Degree, or Out-Degree with immediate viewer and grid synchronization.
  - Desktop: Integrated under **Data** menu (`Data ▸ Symmetrize Options...`, `Data ▸ Dichotomize / Threshold Ties...`, `Data ▸ Zero Diagonal`, `Data ▸ Set Diagonal...`, `Data ▸ Extract Ego Network...`, `Data ▸ Network Algebra...`, `Data ▸ Reorder Sociomatrix...`).
- **Freeman (1979) Global Network Centralization Indices**:
  - Implemented graph-level degree ($C_D$), betweenness ($C_B$), closeness ($C_C$), and Bavelas-Leavitt ($C_{\text{Bav}}$) centralization measures benchmarked against the star graph.
  - Generates dedicated Freeman Centralization summary tables across individual reports and consolidated battery reports.
  - Retired ambiguous legacy "Freeman General Coefficient" label; standardized all text and HTML reports on canonical Freeman (1979) centralization nomenclature ($C_D, C_B, C_C, C_{\text{Bav}}$) with $O(n)$ closed-form degree centralization.
  - Added Agna Core method `AgnaLib.outFreemanCentralization(Network)`.
  - Added CLI support: `agna analyse FILE --metric freeman|centralization|freeman-centralization`.
- **Agna CLI Full Parity for Network Transformations**:
  - Added CLI support for all `NetworkTransformService` operations in `agna transform IN OUT --op OP`:
    - `zero-diagonal`: remove self-loops.
    - `set-diagonal:V`: set diagonal cells to scalar `V`.
    - `dichotomize:V`: threshold ties at value `V`.
    - `slice:MIN:MAX`: filter ties within range `[MIN, MAX]`.
    - `ego:NODE[:HOPS]`: extract 1-hop or 2-hop ego subnetwork.
    - `combine:FILE:OP`: network algebra matrix combination (`add`, `subtract`, `min`, `hadamard`).
    - `reorder:CRIT[:asc|desc]`: sort matrix by `name`, `degree`, `indegree`, or `outdegree`.
- **Agna vs NetworkX Parity Battery & Verification Harness**:
  - Built automated parity validation suite (`tools/nx-parity/run_parity_battery.py`, `tools/nx-parity/AgnaMetricsDump.java`) benchmarking Agna against NetworkX 3.4.2 across 7 benchmark networks and 18 graph metrics.
  - Validated numerical equivalence across degree distributions, density, reciprocity, Brandes betweenness, closeness, Bavelas fareness, Watts-Strogatz clustering, Newman transitivity, k-core decomposition, dyad and triad censuses, Newman assortativity, and Louvain modularity.
- **Documentation & In-App Help Updates**:
  - Comprehensive update to `docs/manual.md` documenting Freeman centralization, Newman assortativity, Louvain & Girvan-Newman community detection, structural equivalence, weighted metrics, all matrix transformations, and UCINET DL format.
  - Regenerated all in-app HTML help files via `tools/md2help.py`.
  - Updated `docs/cli.md` with complete CLI documentation for all new 2.1.6 options.
- **Newman Degree Assortativity & Directed Mixing Patterns (`com.bentza.sna.stats.DegreeAssortativity`)**:
  - Implemented Newman (2002/2003) total degree assortativity correlation coefficient $r$ and directed mixing patterns ($r_{\text{out-in}}, r_{\text{in-in}}, r_{\text{out-out}}, r_{\text{in-out}}$).
  - CLI: `agna analyse FILE --metric assortativity`.
- **Louvain & Girvan-Newman Community Detection (`com.bentza.sna.community.CommunityDetectionService`)**:
  - Implemented multi-phase Louvain modularity optimization and Girvan-Newman edge-betweenness community partitioning with modularity $Q$ tracking and internal cluster density calculations.
  - CLI: `agna analyse FILE --metric community|louvain|girvan-newman`.
- **Structural Equivalence & Positional Blockmodeling (`com.bentza.sna.equivalence.StructuralEquivalenceService`)**:
  - Implemented Burt profile Euclidean distance matrix, Pearson profile correlation matrix, hierarchical average-linkage positional clustering, and $K \times K$ blockmodel image density matrix.
  - CLI: `agna analyse FILE --metric equivalence|blockmodel`.
- **Valued & Weighted Network Metrics (`com.bentza.sna.stats.WeightedMetrics`)**:
  - Node strength (in-strength, out-strength, total strength), valued network density $\Delta = W / (n(n-1))$, and Barrat et al. (2004) weighted local clustering coefficient $C_i^w$.
  - CLI: `agna analyse FILE --metric weighted`.
- **UCINET DL Data Format (`.dl`) Support (`com.bentza.sna.io`)**:
  - Complete import and export for UCINET DL formats including `fullmatrix`, `edgelist1`, and `nodelist1`, with label quoting and header parsing.
  - CLI: `agna convert in.agn out.dl` and `agna convert in.dl out.csv`.
- **Consolidated Battery Dialogs & Configurable Full Analysis**:
  - Consolidated scattered dialogs into clean, category-level battery dialogs: Centrality & Prestige Battery, Distances & Geodesics Battery, Clustering & Cohesion Battery, Sociometrics Battery, and Unified Symmetrization Dialog (15 rules).
  - Fully configurable Full Analysis Battery (Option B) supporting selective execution across all 16 analytical modules with instant Presets (Standard / All / Clear).

## [2.1.5] - 2026-10-30

### Added
- **Algorithm Benchmarking Suite (`com.bentza.sna.benchmark`)**:
  - Engineered an empirical benchmarking engine (`AlgorithmBenchmarkSuite`, `BenchmarkResult`, `BenchmarkCategory`) for measuring execution runtime (ms), operational throughput (ops/sec), algorithmic complexity scaling, and mathematical invariant validation across 10 foundational graph algorithms:
    - **Centrality**: Freeman Closeness $\mathcal{O}(V^2)$, Brandes Betweenness $\mathcal{O}(VE)$, Degree Centrality suite $\mathcal{O}(V^2)$, Bavelas-Leavitt index $\mathcal{O}(V^2)$.
    - **Clustering**: Watts-Strogatz Local Clustering $\mathcal{O}(V \langle k \rangle^2)$, Newman Global Transitivity $\mathcal{O}(V \langle k \rangle^2)$, Bron-Kerbosch Maximal Cliques $\mathcal{O}(3^{V/3})$.
    - **Paths & Distances**: All-Pairs Geodesic Distances $\mathcal{O}(V^3)$, Graph Diameter & Extremes $\mathcal{O}(V^2)$, Shortest Path Routing $\mathcal{O}(V+E)$.
  - Generates an SPSS-style analytical report complete with benchmark performance metrics, Big-O reference annotations, correctness checkmarks, and methodological notes.
  - CLI support: `agna benchmark FILE [--iterations N] [--format text|json|csv|html] [--out OUT]`.
  - Desktop GUI support: **Analysis ▸ Algorithm Benchmarks (Centrality, Clustering, Paths)...** in main menu.
- **SPSS-Style Methodological Footnotes for Hypothesis Testing**:
  - Enhanced `HypothesisTestResult` with formatted lettered SPSS notes block (in both modern HTML cards and ASCII text reports) explaining Monte Carlo empirical p-value interpretation, null model generation constraints, degree sequence preservation, and statistical significance criteria.
- **Structural Archetypes Diagnostic Framework (`com.bentza.sna.theory`)**:
  - Implemented analytical diagnostic classifier (`ArchetypeClassifier`, `ArchetypeMetrics`, `StructuralArchetype`) evaluating empirical networks against 4 classic sociological theories:
    1. **Small-World / Strength of Weak Ties** (Watts & Strogatz, 1998; Humphries & Gurney, 2008): Measures Small-World quotient $\sigma = (C / C_{\text{rand}}) / (L / L_{\text{rand}})$, clustering coefficient $C$, and characteristic path length $L$.
    2. **Cumulative Advantage / Preferential Attachment (Matthew Effect)** (Barabási & Albert, 1999; Merton, 1968; Price, 1976; Clauset, Shalizi & Newman, 2009): Estimates power-law scaling exponent $\hat{\gamma}$ via MLE, degree Gini inequality $G_{\text{deg}}$, and hub prominence ratio $k_{\max} / \langle k \rangle$.
    3. **Homophily & Echo Chambers (Birds of a Feather)** (McPherson, Smith-Lovin & Cook, 2001; Blau, 1977; Newman, 2002, 2006): Computes Newman's modularity $Q$ through greedy agglomerative community clustering and degree assortativity coefficient $r$.
    4. **Formal Authority & Bureaucracy** (Weber, 1922; Simon, 1947; Krackhardt, 1994): Evaluates Krackhardt's 4 organizational dimensions: Connectedness, Graph Hierarchy ($GH$), Graph Efficiency ($GE$), and Least Upper Bound ($LUB$).
  - Produces an SPSS-style Archetype Goodness-of-Fit scorecard, structural signature diagnostics table, sociological interpretation narrative, and methodological footnotes.
  - CLI support: `agna archetype FILE [--format text|json|csv|html] [--out OUT]`.
  - Desktop GUI support: **Analysis ▸ Structural Archetypes Diagnostic...** in main menu.
- **Modern Analysis Output Rendering & Unified Table Reporting Framework (`com.bentza.sna.report`)**:
  - Engineered an enterprise-grade reporting system featuring `TableData`, `TableRegistry`, `AnalysisReport`, and `AnalysisReportFactory`.
  - Modernized all 22 network analysis routines in `AgnaLib` (`outBasic`, `outDensity`, `outCohesion`, `outInDegree`, `outOutDegree`, `outNodalDegree`, `outEmissionDegree`, `outReceptionDegree`, `outDeterminationDegree`, `outSociometricStatus`, `outBetweenness`, `outCloseness`, `outFareness`, `outEccentricity`, `outDiameter`, `outGeodesics`, `outBavelas`, `outPrestige`, `outCliques`, `outShortestPaths`, `outOpenChainSummary`, `outFullAnalysis`).
  - Output displays formatted HTML report cards in Swing Desktop mode with styled metadata headers, KPI badges, alternating row highlights, and clean typography, while preserving 100% plain text fidelity in headless CLI and test executions.
  - Embedded one-click action links (`[Export Table (CSV/JSON)]`) in output HTML above all analytical tables.
  - Added dedicated **Export Table Dialog (`ExportTableDialog`)** accessible via the embedded links, `Output ▸ Export Analysis Table...` (`mFile`), or `Analysis ▸ Export Table...` (`mAnalysis`). Supports live preview, column metadata, clipboard copying, and export to RFC 4180 CSV, formatted JSON, TSV, or HTML.
  - Upgraded CLI `analyse` command with `--format text|json|csv|html` to output individual metrics or full reports in machine-readable or styled formats.
- **Statistical Hypothesis Testing with Null Models (`NetworkHypothesisTester`, `HypothesisTestingDialog`)**:
  - Implemented empirical Monte Carlo hypothesis testing against 4 canonical null models: Bernoulli $G(N, p)$, Configuration Model (degree sequence preserving stub rewiring), Uniform Edge Shuffling, and Degree-Preserving Double-Edge Swaps (Masclov–Sneppen).
  - Evaluates observed network statistics (Density, Average Degree, Global Clustering, Reciprocity, Transitivity, Diameter, Average Path Length) against empirical null distributions, calculating mean, standard deviation, Z-score, and empirical two-tailed p-values.
  - Interactive Desktop Dialog (`Analysis ▸ Hypothesis Testing (Null Models)...`) with parameter tuning, null distribution summaries, and integrated table export.
- **Universal Smart Directory Defaults Across File Operations**:
  - Extended smart directory navigation (`Environment.getDefaultOpenDirectory()`) to **Merge Network**, **New from Chain**, **Save Network As**, **Save Output As**, and **Open Output File**, ensuring seamless navigation relative to the user's active workspace and samples directory.
- **Comprehensive "File | New Network..." Dialog (`NewNetworkDialog`)**:
  - Replaced the previous single text prompt with a rich, interactive configuration dialog offering 15 distinct topologies:
    1. **Empty network (isolated nodes)**: Generates $N$ isolated vertices in a circular ring layout.
    2. **Star network**: Configurable as **Symmetrical** (undirected $0 \leftrightarrow j$) or **Asymmetrical** (directed $0 \to j$), with node 0 positioned at the center and peripheral nodes arranged on a surrounding orbit.
    3. **Random binary connected network (no outsiders)**: Guarantees zero isolated nodes and full connectivity by generating a random spanning tree across all $N$ vertices, then populating additional edges to the chosen density.
    4. **Random weighted network**: Generates edge weights strictly within the open interval $(0.0, 1.0)$ (i.e. $0.001 \le w \le 0.999$, without touching the boundary values $0.0$ or $1.0$), with user-selectable density and symmetry.
    5. **Circular network**: Generates an undirected ring lattice with circular viewer coordinates.
    6. **Full binary network (complete clique)**: Connects all distinct pairs with unit weights and positions vertices in a star-shaped layout around the central node.
    7. **Square lattice network ($N \times N$)**: Generates a 2D regular square grid with 4-neighbor planar connectivity, with automated $L \times L$ node calculation and an immediate planar square lattice layout in the Network Viewer.
    8. **Hexagonal lattice network (bee-hive)**: Generates a true honeycomb / bee-hive network of concentric rings of regular hexagonal cells ($6 R^2$ nodes, degree 3 interior), immediately assigned to a planar bee-hive layout in the Network Viewer.
    9. **Hexagram lattice network (Star of David)**: Generates a 6-pointed star triangular lattice of order $k$, calculating the exact vertex count from dual overlapping triangular grids with planar hexagram coordinates.
    10. **Watts–Strogatz Small-World network**: Regular ring lattice with degree $k$ and tunable shortcut rewiring probability $\beta \in [0, 1]$, displaying high clustering and small average path lengths, rendered in circular ring layout.
    11. **Barabási–Albert Scale-Free network**: Preferential attachment growth model with parameter $m$, producing power-law degree distributions ($P(k) \sim k^{-3}$), rendered in a concentric layout with high-degree core hubs at the center.
    12. **Stochastic Block Model (SBM) / Clustered Communities**: Generates modular network structures with $K$ distinct community blocks, intra-community tie probability $p_{\text{in}}$, and inter-community bridge probability $p_{\text{out}}$, automatically positioned in clustered community orbit layouts.
    13. **Erdős–Rényi Random Graph $G(n, p)$**: Classical Bernoulli random graph model with independent edge probability $p$, selectable as symmetric (undirected) or directed, rendered in circular layout.
    14. **Linear / chain network**: $1\text{D}$ path / pipeline topology with configurable symmetry, laid out horizontally or in serpentine multi-row snake formation for larger node counts.
    15. **Hierarchical Tree / Corporate Hierarchy (Organigram)**: Configurable branching factor $b$ (subordinates per leader), selectable directionality (downward authority), and optional lateral peer ties within administrative levels.
- **Agna CLI Full Parity with Core & Desktop Enhancements**:
  - Expanded `agna generate` command to support all 15 network topologies with specific tuning parameters (`--type empty|star|circular|full|random-connected|random-weighted|square-lattice|beehive|hexagram|watts-strogatz|barabasi-albert|sbm|erdos-renyi|linear|tree`, `--density`, `--symmetric/--directed`, `--side`, `--radius`, `--order`, `-k`, `--beta`, `-m`, `--blocks`, `--p-in`, `--p-out`, `-b`, `--lateral`, `--seed`).
  - Added `hierarchical` and `star` layout support to `agna draw --layout` and `agna layout --layout`.
  - Upgraded CLI file loader (`open`) to automatically resolve network files against `Environment.getDefaultOpenDirectory()` (e.g. `~/.agna/samples/`) when relative paths are not in the current working directory.
  - Updated command-line manual page (`docs/agna.1`), CLI guide (`docs/cli.md`), and shell tab-completions for Bash (`docs/agnacompletion.bash`) and Zsh (`docs/agnacompletion.zsh`).
- **Academic Citations & Documentation Reference (§12 in Manual)**:
  - Documented complete mathematical formulations and verified seminal academic literature citations for newly supported generative network models:
    - **Watts–Strogatz Small-World**: Watts, D. J., & Strogatz, S. H. (1998). *Collective dynamics of 'small-world' networks*. *Nature*, 393(6684), 440–442. [doi:10.1038/30918](https://doi.org/10.1038/30918).
    - **Barabási–Albert Scale-Free**: Barabási, A.-L., & Albert, R. (1999). *Emergence of scaling in random networks*. *Science*, 286(5439), 509–512. [doi:10.1126/science.286.5439.509](https://doi.org/10.1126/science.286.5439.509).
    - **Stochastic Block Model (SBM)**: Holland, P. W., Laskey, K. B., & Leinhardt, S. (1983). *Stochastic blockmodels: First steps*. *Social Networks*, 5(2), 109–137. [doi:10.1016/0378-8733(83)90021-7](https://doi.org/10.1016/0378-8733(83)90021-7); Karrer, B., & Newman, M. E. J. (2011). *Physical Review E*, 83(1), 016107.
    - **Erdős–Rényi Random Graph**: Gilbert, E. N. (1959). *Random graphs*. *The Annals of Mathematical Statistics*, 30(4), 1141–1144. [doi:10.1214/aoms/1177706098](https://doi.org/10.1214/aoms/1177706098); Erdős, P., & Rényi, A. (1959). *Publ. Math. Debrecen*, 6, 290–297.
    - **Hierarchical Tree / Corporate Hierarchy**: Krackhardt, D. (1994). *Graph theoretical dimensions of informal organizations*. In *Computational Organization Theory* (pp. 89–111); Simon, H. A. (1962). *The architecture of complexity*. *Proc. Am. Philos. Soc.*, 106(6), 467–482.
- **Five New Social Network Analysis Layout Engines**:
  - Implemented 5 specialized SNA layout algorithms in [`NetworkLayouts`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-core/src/main/java/com/bentza/sna/net/NetworkLayouts.java) with full integration across Agna Core, Desktop, and CLI:
    1. **Core–Periphery Layout** (`applyCorePeriphery`, constant 7): Implements the classic Borgatti & Everett (1999) dual-tier partition. Dense core hubs are placed in an inner circle ($R \approx 0.16 \min(W, H)$), while peripheral actors are positioned on an outer orbital ring ($R \approx 0.40 \min(W, H)$) angularly aligned toward their core attachments.
    2. **Radial Geodesic / Ego-Centric Layout** (`applyEgoRadial`, constant 8): Places the focal ego (highest total degree) at the exact center $(W/2, H/2)$ with alters arranged on concentric circles proportional to their geodesic hop distance ($d = 1, 2, 3 \dots D$), revealing network distance horizons.
    3. **Community-Clustered / Modularity Layout** (`applyCommunity`, constant 9): Identifies modular communities using Newman's (2004) greedy modularity optimization ($Q$-score maximization) and clusters nodes around sub-centers arranged on an orbit around the canvas center, with localized intra-community layouts.
    4. **Layered / Sugiyama (DAG) Layout** (`applyLayeredDag`, constant 10): Hierarchical multi-layer directed flow layout with source-priority topological ranking, barycentric neighbor sorting to minimize edge crossings, dense layer normalization (eliminating empty gap layers), horizontal staggering to prevent 1D chain collapse, and automatic canvas vertical auto-scaling (allocating $\ge 75\text{px}$ per layer) to prevent narrow chain or squashed horizontal strip artifacts.
    5. **Linear / Serpentine Pathway Layout** (`applyLinear`, constant 11): Sequential path/chain layout along the network's primary trajectory. Arranged in a single horizontal line for $N \le 20$, and multi-row alternating serpentine (snake) pattern for larger networks.
  - **Desktop Viewer Integration**: Added all 5 layout options to the Network Viewer menu (*Image | Layout*) and toolbar buttons in [`GrNet`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-desktop/src/main/java/com/bentza/sna/gui/GrNet.java), complete with dedicated vector icons designed in [`ModernIcons`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-desktop/src/main/java/com/bentza/sna/gui/ModernIcons.java).
  - **CLI Full Parity**: Added full support for `--layout core-periphery|ego-radial|community|layered-dag|linear` in both `agna draw` and `agna layout` commands, with updated man page (`docs/agna.1`), CLI guide (`docs/cli.md`), and shell completions.
- **Hierarchical Layout Engine & Viewer Toolbar Action**:
  - Implemented BFS level decomposition algorithm in [`NetworkLayouts`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-core/src/main/java/com/bentza/sna/net/NetworkLayouts.java) (`applyHierarchical`) to position tree and organizational structures top-down with root nodes centered at the top.
  - Added new **Hierarchical Layout** action to the Network Viewer menu (*Image | Layout | Hierarchical Layout*) and the Network Viewer layout toolbar.
  - Designed vector icon for Hierarchical Layout in [`ModernIcons`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-desktop/src/main/java/com/bentza/sna/gui/ModernIcons.java) matching Agna's native vector look.
- **Dynamic Canvas Dimension Enlargement**:
  - Automatically scales the drawing canvas in [`NodeArea`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-core/src/main/java/com/bentza/sna/net/NodeArea.java) based on network size ($N \le 30 \to 450\,\text{px}$, $N \le 75 \to 650\,\text{px}$, $N \le 150 \to 800\,\text{px}$, $N \le 300 \to 1200\,\text{px}$, $N > 300 \to 1600\,\text{px}$) to avoid visual overlap while preventing excessive memory overhead on large graphs.
- **Topology Generation Engine (`NetworkGenerator`)**:
  - Created [`com.bentza.sna.net.NetworkGenerator`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-core/src/main/java/com/bentza/sna/net/NetworkGenerator.java) containing deterministic, mathematically validated topology constructors and layout coordinate initializers (`createSquareLattice`, `createBeeHiveLattice`, `createHexagonalLattice`, `createHexagramLattice`, `createWattsStrogatz`, `createBarabasiAlbert`, `createStochasticBlockModel`, `createErdosRenyi`, `createLinearChain`, `createHierarchicalTree`, etc.).
- **Immediate Layout Preservation in Network Viewer**:
  - Resolved an issue in [`NodeArea`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-core/src/main/java/com/bentza/sna/net/NodeArea.java) where opening the Network Viewer unconditionally wiped out preset actor coordinates with `setCircleLayout()` and random positions.
  - Network Viewer now detects preset coordinates, preserves the exact topological layout (Square Lattice grid, Bee-hive honeycomb, Star, Circular, etc.), and renders the network in its natural geometry.
  - Implemented isotropic aspect-ratio preserving coordinate fitting (`adjustNodesCoordinates`) with divide-by-zero protection.
  - Connected `MainFrame.doNewNetwork` to attach the viewer area immediately upon generation and automatically refresh an active viewer window.
- **On-the-Spot Input Validation & Safety Bounds**:
  - Validates user input dynamically as typed: rejects non-natural numbers, decimals, and values $< 2$ with immediate visual feedback.
  - Enforces a safety upper bound of **5,000 nodes** to guard against JVM `OutOfMemoryError` on large dense adjacency matrices.
  - Displays non-blocking warnings for networks larger than 300 nodes.
- **Smart Default Directory on Open Network, Merge Network, New from Chain, Save As, and Output Operations**:
  - Uniformly applies the smart directory rule across all file operations: `File | Open Network...`, `File | Merge Network...`, `File | New From Chain...`, `File | Save Network As...`, `File | Open Output File`, `File | Save Output As...`, and `Image | Export Image As...`.
  - Automatically resolves relative to the active file's existing directory, the active network's directory, or `Environment.getDefaultOpenDirectory()` (bundled `samples/`, `~/.agna/samples/`, or user `Documents`).
  - Also active in CLI `open`, resolving sample files when not present in the current working directory.
- **Monte Carlo Statistical Hypothesis Testing against Null Models**:
  - Implemented the [`com.bentza.sna.stats`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-core/src/main/java/com/bentza/sna/stats/) package for non-parametric network hypothesis testing:
    - **Null Models (`NullModel`)**:
      - `DEGREE_PRESERVING`: Maslov–Sneppen (2002) / Rao–Jana–Bandyopadhyay (1996) MCMC Double Edge Swap algorithm, preserving the exact in-degree and out-degree sequence of every node.
      - `ERDOS_RENYI`: Bernoulli $G(n, m)$ density-preserving random graph model.
      - `DYAD_CENSUS`: Holland–Leinhardt (1976) $U \mid MAN$ model preserving exact mutual, asymmetric, and null dyad counts.
      - `WEIGHT_PERMUTATION`: Preserves binary topology while permuting tie weights.
    - **Evaluated SNA Metrics (`NetworkMetric`)**: Global Transitivity (Clustering Coefficient), Dyadic Reciprocity, Network Density, Freeman Degree Centralization, Betweenness Centralization, Closeness Centralization, Average Geodesic Distance, and Modularity (Newman $Q$).
    - **Hypothesis Engine (`NetworkHypothesisTester`)**: Multi-core parallel Monte Carlo simulation with Davison–Hinkley exact empirical $p$-values ($p_{\text{two-tailed}}, p_{\text{greater}}, p_{\text{less}}$), standardized $Z$-scores, and 95% confidence intervals.
  - **Desktop GUI Integration (`HypothesisTestingDialog`)**: Added **Analysis ▸ Hypothesis Testing (Null Models)...** dialog with interactive metric/model selection, configurable replications (100–5,000), progress bar, ASCII distribution histogram with observed value marker, and automated appending of rich HTML report tables to the Output tab.
  - **CLI Full Parity**: Added `agna test-null FILE [--metric M] [--model NULL_MODEL] [-r N] [--seed S] [--format text|json|csv|html] [--out OUT]`.
- **Automated Source Backup Snapshots**:
  - Added project [`backup/`](file:///Volumes/MacAPFS/Antigravity/Agna/backup/) directory for timestamped source-only zip archives (`agna-<version>-<date>-<time>.zip`), configured in `.gitignore`.
- **Automated Test Coverage**:
  - Added [`NetworkGeneratorTest`](file:///Volumes/MacAPFS/Antigravity/Agna/agna-core/src/test/java/com/bentza/sna/net/NetworkGeneratorTest.java) covering all 8 topology algorithms, boundary conditions, edge weight intervals, and safety bounds (bringing total suite to 258 passing tests).

---

## [2.1.4] - 2026-10-30

### Performance & Computation Optimizations
- **Brandes Betweenness Centrality ($O(V \cdot E)$)**:
  - Dropped betweenness computation time on 1,000-node networks from **12.0 seconds** to **2.3 seconds** (**5.13x speedup, -80.5% runtime**).
  - Eliminated inner-loop dynamic heap allocations of `IntList` within the breadth-first search (BFS) queue.
  - Replaced linked stack and queue traversals with contiguous primitive array stacks (`int[] order`) with index pointers ($O(1)$ push/pop with zero GC pressure).
  - Hoisted common normalizing coefficients outside vertex iterations.
- **Row-Major Degree Calculations**:
  - Optimized `weightedReceptionDegree()` and `determinationDegree()` by replacing $O(N^2)$ column-major matrix access with single-pass row-major array accumulations (**1.5x to 4.3x speedup**), maximizing CPU L1/L2 data cache locality.
- **Optimized `Network` Matrix Accessors**:
  - Refactored `getMatrix()`, `getBooleanMatrix()`, `getIntegerMatrix()`, `getEdgesNumber()`, `getMin()`, `getMax()`, `isSymmetric()`, and `getValue()` to eliminate repetitive synchronized `Vector.elementAt(i)` lookups in inner loops.
  - `getBooleanMatrix()` achieved a **3.5x to 8.1x speedup** across all graph sizes.
  - `getMatrix()` now utilizes vectorized row-level `System.arraycopy()`.
- **Linked List Modernization (`IntList`)**:
  - Converted `IntList` into an augmented linked list tracking both `first` and `last` pointers, alongside a cached `size`.
  - `appendValue()` and `getSize()` are now $O(1)$ instead of $O(N)$ list traversals.
  - `getClone()` reduced from $O(N^2)$ to $O(N)$.
  - Implemented `Iterable<Integer>` and `toArray()` for fast iteration without $O(k)$ repeated element lookups.

### Security, Robustness & Fixes
- **Malicious URL Scheme Injection Prevention (`BrowserControl`)**:
  - Replaced raw shell invocation with URI scheme whitelisting (permitting only `http`, `https`, `mailto`, `file`) and adopted `java.awt.Desktop.browse(URI)`.
- **XML External Entity (XXE) Hardening (`HTMLParser`)**:
  - Hardened XML/HTML parsers with `FEATURE_SECURE_PROCESSING`, disabling external DTDs and entity expansion (`disallow-doctype-decl`, `external-general-entities`, `external-parameter-entities`).
- **Excel Spreadsheet Bomb / Memory Exhaustion Defense (`XlsxReader`)**:
  - Enforced a hard cell coordinate safety limit of 10,000 rows / 10,000 columns to prevent out-of-memory crashes on maliciously crafted or corrupt files.
- **Path Traversal Protection (`IOUtils`)**:
  - Sanitized filename extraction in `stripExtension()` to handle arbitrary dot placements in directory paths safely.
- **Executable App Launcher Entry Point**:
  - Corrected `jpackage` packaging manifest from `com.bentza.sna.gui.MainFrame` to the real application bootstrap `com.bentza.sna.Agna`.
- **Repository Clean-Up**:
  - Purged 47 MB of redundant precompiled binaries and bundled Linux test JRE in `AgnaSmoke/`.

---

## [2.1.3] - 2026-09-10

- Initial open-source revival release of AGNA (modernized from original 2001–2005 codebase).
- Maven multi-module structure (`agna-core`, `agna-cli`, `agna-desktop`).
- FlatLaf cross-platform look and feel with dark/light themes.
- Native packaging via `jpackage` for macOS, Linux, and Windows.
- CLI subcommands for analysis, format conversions, and diagram rendering.
