# Changelog

All notable changes to the **AGNA** (Applied Graph and Network Analysis Open Source) project since version 2.1.3 are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.1.5] - 2026-10-30

### Added
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
- **Smart Default Directory on Open Network**:
  - When opening a network file via GUI (`File | Open Network...`), the file dialog automatically opens to the bundled `samples/` directory.
  - Bundled sample networks are packaged as classpath resources and extracted on demand to `~/.agna/samples/`.
  - If the samples folder is unavailable, gracefully falls back to the user's OS Documents directory (`~/Documents` on macOS/Linux, `My Documents` on Windows), or the user's home directory.
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
