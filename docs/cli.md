# Agna CLI — command reference

The command-line interface (`agna`) runs the Agna engine headlessly: no
desktop, no windowing, everything pipable and scriptable. It ships as
`agna-cli-2.1.7.jar` (run with `java -jar agna-cli-2.1.7.jar <command>`).

The CLI understands `.agn`, `.txt`, `.csv`, `.net`, `.xls`, `.xlsx`,
`.graphml`, `.gml`, `.graphson` and `.dl` (UCINET) on input, and writes the same set on
output (`.xls` needs a real file; the text formats also accept `-` for
stdout). A file argument of `-` reads agn text from stdin (override with
`--in-format`).

Exit status: 0 on success; 1 on usage errors, unknown commands/analyses/
metrics, or exceptions; 2 on pre-flight data validation failure or unreadable files.

Command summary:

| command | purpose |
|---|---|
| `version` | print the version |
| `info FILE [--format text\|json] [--json]` | network summary (name, size, edges, connectivity, outsiders) |
| `validate FILE [--format text\|json] [--json]` | pre-flight data validation, format integrity, isolates, components, self-loops |
| `explain [METRIC] [--list] [--format text\|json] [--json]` | SNA definitions, formulas, usage guidelines, and cautious interpretations |
| `advise FILE [--format text\|json] [--json]` | topology-driven analysis, layout, and null-model recommendations |
| `narrative FILE [--format text\|json] [--json]` | automated plain-English / Markdown structural narrative synthesis |
| `capabilities [--format text\|json] [--json]` | self-describing manifest of all supported formats, algorithms, and commands |
| `analyse FILE [--all\|NAME...\|cliques:N] [--out FILE]` | the full analysis battery |
| `convert IN OUT [--in-format F] [--out-format F]` | format conversion |
| `transform IN OUT --op OP` | network operations (see below) |
| `draw IN --out PNG [--layout L] [--size WxH] [--labels] [--background #rrggbb] [--no-faces] [--edge-values]` | headless visualisation (circular, random, spring, grid, concentric, star, hierarchical, core-periphery, ego-radial, community, layered-dag, linear) |
| `generate --nodes N --out FILE [--type ...] [options]` | seeded synthetic networks (empty, star, circular, full, random-connected, random-weighted, square-lattice, beehive, hexagram, watts-strogatz, barabasi-albert, sbm, erdos-renyi, linear, tree) |
| `matrix FILE [--format csv\|tsv] [--out FILE]` | raw matrix table |
| `nodes FILE [--out FILE]` | node table (degrees + coordinates) |
| `ego FILE --node NAME --out OUT` | 1-hop ego network extraction |
| `components FILE` | connected components |
| `metrics FILE [METRIC...] [--all] [--top N] [--format csv\|json] [--json] [--out FILE]` | structured metrics and token-efficient top-N summaries |
| `distance FILE --from A --to B` | shortest path between two nodes |
| `diff A B [--out FILE.csv]` | structural comparison |
| `test-null FILE [--null-model M] [--samples N] [--seed S]` | Monte Carlo hypothesis testing against null models |
| `add-scalar FILE V --out OUT` … `renumber FILE --out OUT` | desktop Network-menu commands (see below) |
| `from-chain FILE --out OUT` | create a network from a chain file |
| `layout FILE --layout L --out OUT` | apply coordinates (circular|random|spring|grid|concentric|star|hierarchical|core-periphery|ego-radial|community|layered-dag|linear) without rendering |
| `set FILE --out OUT [--flag VALUE ...]` | edit viewer attributes |
| `--help` | this text |

## Command details

### version

```
agna version
agna --version
```

Prints `Agna CLI 2.1.6`.

```
$ agna version
Agna CLI 2.1.6
```

### info

```
agna info FILE [--out FILE]
```

Prints the network name, node count, edge count and Agna's basic
description — node/edge totals, outsiders, and (2.1.3) a connectivity
note: "The network is connected." or that it is disconnected. The
smallest useful command; also the fastest way to sanity-check a file.
`--out FILE` writes the report instead of stdout.

```
$ agna info samples/example2.agn
name:   Example 2
nodes:  9
edges:  20
Basic description of Example 2
...
The network is connected.
$ agna info net.agn --json
{
  "name": "Example 2",
  "nodes": 9,
  "edges": 20,
  "density": 0.2778,
  "directed": false,
  "connected": true,
  "components": 1
}
$ agna info net.agn --out summary.txt
wrote summary to summary.txt
```

### validate

```
agna validate FILE [--format text|json] [--json] [--out FILE]
```

Runs comprehensive pre-flight verification on a network file to inspect
its structural integrity before running intensive analyses. Checks:
- File existence and format syntax across all supported formats (`.agn`, `.csv`, `.net`, `.graphml`, etc.)
- Matrix squareness and non-finite tie values
- Disconnected component partitions and outsiders
- Self-loops (diagonal non-zero values)
- Isolate actors (degree 0)
- Directed vs. symmetric status and valued weights

Exit code is `0` if valid, or `2` if the file is missing, unparseable, or fails validation.

```
$ agna validate samples/example2.agn --json
{
  "valid": true,
  "file": "samples/example2.agn",
  "format": "agn",
  "nodes": 9,
  "edges": 20,
  "density": 0.2778,
  "directed": false,
  "weighted": false,
  "connected": true,
  "components": 1,
  "selfLoops": 0,
  "isolates": [],
  "outsiders": [],
  "diagnostics": []
}
```

### explain

```
agna explain [METRIC] [--list] [--format text|json] [--json] [--out FILE]
```

Inspects Agna's built-in Social Network Analysis knowledge base (37 metrics).
Provides formal definitions, mathematical formulas, when-to-use criteria,
technical computation notes, and cautious sociological interpretations.

- `agna explain --list` — lists all 37 explained metrics by category.
- `agna explain <metric> [--json]` — queries the detailed micro-guide for a specific metric.

```
$ agna explain betweenness --json
{
  "id": "betweenness",
  "title": "Betweenness Centrality (Freeman / Brandes)",
  "category": "Path Centrality",
  "whatItMeasures": "The proportion of all shortest paths between all pairs of nodes that pass through this actor.",
  "whyUseIt": "Identifies brokers, gatekeepers, and potential bottlenecks. Vital for studying structural holes and information control.",
  "interpretation": "High betweenness nodes may act as pivotal brokers or gatekeepers; their absence could potentially disconnect or delay communication between subgroups.",
  "technicalNote": "Computed via Brandes O(VE) algorithm. Normalized against theoretical maximum."
}
```

### analyse

```
agna analyse FILE [--all | NAME... | cliques:N] [--out FILE]
```

Runs the analysis battery. With no names (or `--all`) every analysis
runs; named analyses run in order. Available names:

`basic`, `density`, `cohesion`, `nodal`, `indegree`, `outdegree`,
`emission`, `reception`, `determination`, `status`, `geodesics`,
`open-chain`, `eccentricity`, `diameter`, `bavelas`, `closeness`,
`fareness`, `betweenness`, `prestige`, `cliques` (default clique size 2;
`cliques:3` for triangles), `clustering` (local clustering), `transitivity` (global transitivity),
`kcore` (k-core decomposition), `triads` (`triad-census`), `holes` (`structural-holes`),
`components` (`connectivity`), `eigenvector`, `bonacich`, `pagerank`,
`assortativity` (degree assortativity & directed mixing),
`community` (`louvain`), `girvan-newman`, `equivalence` (`blockmodel`),
`weighted` (valued & weighted metrics), `freeman` (`centralization`),
`centrality` (centrality battery), `distance-battery`, `clustering-battery`, `sociometrics` (sociometrics battery),
`full` (full analysis).

This is the display-oriented report — rich text, tables, and Agna's own
conventions. For machine-readable values use `metrics`.

`--out FILE` writes the whole report to FILE instead of stdout.

```
$ agna analyse samples/example2.agn cliques:3
3-Cliques found in Example 2
...
$ agna analyse samples/example2.agn freeman
Freeman Network Centralization Indices of Example 2
...
```

### convert

```
agna convert IN OUT [--in-format F] [--out-format F]
```

Converts a network between formats; both are inferred from the file
extensions. `agn`, `txt`, `csv`, `net` (Pajek), `graphml`, `gml`,
`graphson`, `dl` (UCINET) and `xls` are supported on input; `xls` additionally reads
`.xlsx` (including Apple Numbers files).

`--in-format` overrides input inference (required when `IN` is `-`);
`--out-format` overrides output inference. Text formats accept `-` for
stdout.

```
$ agna convert net.agn out.graphml
$ agna convert net.agn out.dl
$ cat net.agn | agna convert - out.gml
$ agna convert net.agn - --out-format graphson | jq '.graph'
```

### transform

```
agna transform IN OUT --op OP [--in-format F] [--out-format F]
```

Applies one operation and writes the result to OUT — the input file is
never modified, so "save as a new network" is inherent to every op.

| op | effect |
|---|---|
| `transpose` | mirror the matrix on the diagonal |
| `symmetrize-max` | a[i][j] = a[j][i] = max of the pair (Union) |
| `symmetrize-min` | a[i][j] = a[j][i] = min of the pair (Intersection) |
| `symmetrize-average` | a[i][j] = a[j][i] = arithmetic mean of the pair |
| `symmetrize-sum` | a[i][j] = a[j][i] = sum of the pair |
| `symmetrize-union` | boolean OR: 1 if either tie > 0 |
| `symmetrize-intersect` | boolean AND: 1 if both ties > 0 |
| `symmetrize-below` | copy the lower triangle onto the upper |
| `zero-diagonal` | set all self-loops a[i][i] = 0 |
| `set-diagonal:V` | set main diagonal a[i][i] = V |
| `dichotomize:V` | binary thresholding: values >= V become 1, others 0 |
| `slice:MIN:MAX` | band-pass filter: keep values in [MIN, MAX], zero others |
| `normalize-binary` | every non-zero value becomes 1 |
| `normalize-threshold:V` | values > V become 1, others 0 |
| `add-scalar:V` | add V to every cell |
| `multiply-scalar:V` | multiply every cell by V |
| `square` | matrix square (walks of length 2) |
| `ego:NODE[:HOPS]` | extract k-hop ego network around NODE |
| `combine:FILE:OP` | network matrix algebra with another file (`add`, `subtract`, `min`, `hadamard`) |
| `reorder:CRIT[:asc\|desc]` | sort sociomatrix rows/cols by `name`, `degree`, `indegree`, or `outdegree` |
| `merge:FILE:POLICY` | merge another network by node name (sum/max/keep) |
| `delete-node:N` | delete one node by name |
| `delete-nodes:N1,N2,..` | delete a list of nodes |
| `isolate:N` | remove all ties of one node (it stays) |
| `merge-nodes:N1,N2,..` | fold the listed nodes into the first: ties summed, the rest removed |
| `remove-outsiders` | delete every node with no ties |
| `renumber` | name the nodes 1..n |
| `add-nodes:N` | append N new nodes |
| `clone-node:N` | copy a node with its ties (named "Clone of N") |

```
$ agna transform in.agn out.agn --op zero-diagonal
$ agna transform in.agn out.agn --op ego:Alice:2
$ agna transform in.agn out.agn --op combine:other.agn:hadamard
$ agna transform in.agn out.agn --op reorder:degree:desc
$ agna transform in.agn out.agn --op merge-nodes:Alice,Bob
$ agna transform in.agn - --op remove-outsiders --out-format graphml
$ agna transform in.agn out.agn --op clone-node:Eve
```

### draw

```
agna draw IN --out PNG [--layout L] [--size WxH] [--labels]
                   [--background #rrggbb] [--no-faces] [--edge-values]
```

Renders the network headlessly with the same visual language as the
desktop viewer (node faces, tie colours and transparency, grid),
deterministically. Layouts: `circular` (default), `random`, `spring`,
`grid`, `concentric`, `star`, `hierarchical`, `core-periphery`, `ego-radial`, `community`, `layered-dag`, `linear`. Default canvas 1200x900 (any `--size WxH` works; a
rectangular canvas is scaled from the viewer's internal square).

- `--labels` draws node names
- `--background #rrggbb` sets the canvas colour (also removes the
  default background picture)
- `--no-faces` hides node faces
- `--edge-values` draws the tie values on the edges
- after rendering, the command prints the layout used and the
  coordinates it assigned, one row per node (`index name x y`,
  percent coordinates), ready for scripts to reuse

```
$ agna draw in.agn --out net.png --layout spring --labels --background '#1a2332'
drew net.png (layout spring, 1200x900)
index	name	x	y
0	Alice	42.5	37.2
...
```

### generate

```
agna generate --nodes N --out FILE [--type T] [options]
```

Generates a synthetic network (default type `random`, default degree 2).
Supported types:

| type | shape & parameters |
|---|---|
| `empty` | $N$ isolated vertices in a circular ring layout |
| `star` | central hub with surrounding orbit nodes (`--symmetric` / `--directed`) |
| `circular` | undirected ring lattice with circular viewer coordinates |
| `full` | complete binary network (clique) connecting all distinct pairs |
| `random-connected` | random spanning tree guaranteed to have no isolates (`--density D`, `--seed S`) |
| `random-weighted` | weights strictly in $(0.0, 1.0)$ (`--density D`, `--symmetric` / `--directed`, `--seed S`) |
| `square-lattice` | 2D regular square grid with 4-neighbor planar connectivity (`--side S`) |
| `beehive` | concentric regular hexagonal rings honeycomb (`--radius R`) |
| `hexagram` | Star-of-David triangular lattice of order $k$ (`--order O`) |
| `watts-strogatz` | small-world network with shortcut rewiring (`-k K`, `--beta B`, `--seed S`) |
| `barabasi-albert` | scale-free network with preferential attachment (`-m M`, `--seed S`) |
| `sbm` | modular community blocks (`--blocks B`, `--p-in P`, `--p-out P`, `--seed S`) |
| `erdos-renyi` | classic $G(n, p)$ random graph (`-p P`, `--symmetric` / `--directed`, `--seed S`) |
| `linear` | 1D pathway / pipeline chain (`--symmetric` / `--directed`) |
| `tree` | hierarchical tree / organigram (`-b B`, `--lateral`, `--directed` / `--undirected`) |
| `random` | seeded Erdős–Rényi with average degree (`--degree D`, `--seed S`) |

The same `--seed` always produces byte-identical output — the basis for
reproducible test data:

```
$ agna generate --nodes 50 --type random --seed 7 --out r50.agn
$ agna generate --nodes 50 --type random --seed 7 | cmp - r50.agn && echo identical
```

### matrix

```
agna matrix FILE [--format csv|tsv] [--out FILE]
```

Prints the adjacency matrix as a table with node names in the header
row and column; `--format csv` switches the separator; `--out FILE`
writes it instead of stdout.

```
$ agna matrix samples/example2.agn --format csv --out m.csv
```

### nodes

```
agna nodes FILE [--out FILE]
```

One row per node — index, name, out-degree, in-degree, x, y — tab
separated. The fastest path from a network file into an external stats
pass:

```
$ agna nodes in.agn | awk -F'\t' '$3 > 5 { print $2 }'   # hubs
```

### ego

```
agna ego FILE --node NAME --out OUT
```

Extracts the 1-hop ego network of a named node: the ego, its neighbours,
and every tie among them, names preserved, laid out with the spring
embedder. Node names match case-insensitively; a missing name exits 1.

```
$ agna ego big.agn --node Alice --out alice.agn
extracted 12-node ego network -> alice.agn
```

### components

```
agna components FILE [--out FILE.csv]
```

Lists the connected components with their member names (undirected
connectivity). `--out FILE.csv` writes a CSV file (header
`component,node`, one row per node) instead of the readable listing.

```
$ agna components samples/example2.agn
component 1 (9 nodes): 1, 9, 4, 5, 6, 2, 8, 7, 3
9 nodes, 1 component
$ agna components net.agn --out components.csv
wrote components to components.csv
```

### distance

```
agna distance FILE --from A --to B [--out FILE.csv]
```

Prints the shortest path between two named nodes, using the engine's
own shortest-path routine. A geodesic of 0 means no path.
`--out FILE.csv` writes the machine-readable result instead (header
`from,to,hops,path`).

```
$ agna distance in.agn --from Alice --to Bob
Shortest Path from node Alice to node Bob
     * Alice  Carol  Bob
$ agna distance big.agn --from Alice --to Bob --out hops.csv
wrote distance to hops.csv
```

### diff

```
agna diff A B [--out FILE.csv]
```

Structural comparison by node name: `node-added` / `node-removed` rows
and one `edge-diff` row per pair whose value differs, with both values.
A summary line prints first; `--out FILE.csv` writes the rows (header
`kind,node1,node2,valueA,valueB`).

```
$ agna diff a.agn b.agn
3 node(s) added, 1 node(s) removed, 20 edge value difference(s)
kind,node1,node2,valueA,valueB
edge-diff,1,2,1.0000,0.0000
...
```

### Network-menu commands

The dedicated network-operation commands mirror the desktop's Network
menu; each applies an operation and saves a new file via `--out` (the
input is never modified, so every op is a "save as a new network"). They
share the exact op semantics with `transform`.

```
agna add-scalar FILE V --out OUT        add V to every cell
agna multiply-scalar FILE V --out OUT   multiply every cell by V
agna transpose FILE --out OUT           transpose the sociomatrix
agna symmetrize FILE [--mode below|sum|max] --out OUT
agna normalize FILE [--binary | --threshold V] --out OUT
agna square FILE --out OUT              matrix square (walks of length 2)
agna merge-networks A B [--mode sum|max|keep] --out OUT
agna remove-outsiders FILE --out OUT    delete every node with no ties
agna delete-nodes FILE N1,N2,... --out OUT
agna add-nodes FILE --count N --out OUT
agna renumber FILE --out OUT            name the nodes 1..n
```

Examples:

```
$ agna symmetrize net.agn --mode max --out net-sym.agn
$ agna merge-networks a.agn b.agn --mode sum --out merged.agn
$ agna add-scalar net.agn 5 --out net5.agn
```

### from-chain

```
agna from-chain FILE --out OUT
```

Creates a network from a chain file (whitespace-separated sequences;
arcs follow the transitions between consecutive tokens), using the
engine's own `readNetworkFromChain` — the routine behind the desktop's
"Create a network from a chain file".

```
$ agna from-chain sequences.txt --out chain.agn
created network from chain -> chain.agn
```

### layout

```
agna layout FILE --layout L --out OUT [--size WxH]
```

Applies a layout to an existing network and saves the **new
coordinates** to a new file — no image is rendered. Layouts:
`circular`, `random`, `spring`, `grid`, `concentric`, `star`. The
coordinates are stored in the agn file, so the laid-out network opens in
the desktop viewer exactly as positioned. Same seed, same layout —
output is byte-deterministic.

```
$ agna layout net.agn --layout spring --out laid.agn
layout (spring) -> laid.agn
$ agna draw laid.agn --out laid.png --layout spring   # identical picture
```

### set

```
agna set FILE --out OUT [--flag VALUE ...]
```

Edits the Network Viewer's Image and Edge menu properties of an existing
network file and saves a new file. The attributes are persisted **in the
agn file itself**, so opening the result in the desktop restores them.
Unset properties keep the file's current values.

```
--name T                        network name
--names-visible on|off          node names shown
--names-x N  --names-y N        name offset
--title-visible on|off          title shown
--title-x N  --title-y N        title offset
--grid-visible on|off           grid shown
--grid-step N                   grid spacing (viewer units)
--separator N                   edge separator step
--grid-transparency N           0..255
--max-transparency N            0..255 (Most Faded Edge)
--edge-value-visible on|off     tie values shown
--edge-value-position N
--edge-value-color #rrggbb
--edge-color #rrggbb            edge/arrow colour
--names-color #rrggbb
--grid-color #rrggbb
--title-color #rrggbb
--background-color #rrggbb
--background-image FILE         background picture
--background-image-x N  --background-image-y N
--background-image-width N  --background-image-height N
--faces-visible on|off
--allow-edge-selection on|off
--color-fidelity on|off
--snap-to-grid on|off
--default-face FILE             apply one face to every node
```

Colours are `#rrggbb` (quote them in the shell — an unquoted `#` starts
a comment); booleans accept `on`/`off`. Running `agna set` with no flags
prints this list.

```
$ agna set net.agn --out styled.agn \
    --names-visible on --background-color '#201a30' \
    --edge-color '#e0b0ff' --max-transparency 120
attributes set -> styled.agn
```

## The `metrics` command

`metrics` produces the machine-readable counterpart of the analyses: one
row per metric, node or pair, in CSV or JSON. Unlike `analyse`, which
prints Agna's rich (display-oriented) report, `metrics` emits tidy,
well-defined values ready for spreadsheets, R, Python or jq.

That said, it shares the engine's conventions: a geodesic of `0` means
*no path*, and the distance-based measures refuse (are omitted from)
disconnected networks.

### Syntax

```
agna metrics FILE [METRIC...] [--all] [--top N] [--format csv|json] [--json] [--out FILE]
```

- `FILE` — the input network (any supported format; `-` for stdin).
- `METRIC...` — the metrics to produce, by name (see the list below).
  Any number may be given.
- `--all` — produce every metric. **This is also the default:** when no
  metric names are given, the full set is produced, so `agna metrics
  net.agn` behaves exactly like `agna metrics net.agn --all`.
- `--top N` — filter output to top `N` highest scoring nodes and include a
  `summary` statistical distribution (`mean`, `stdDev`, `min`, `max`) in JSON.
  Essential for LLM agents to conserve token context on large networks.
- `--format csv|json` — output format (default `csv`).
- `--json` — shorthand alias for `--format json`.
- `--out FILE` — write to a file instead of stdout.

### Metric names

| name | value per | definition |
|---|---|---|
| `density` | network (scalar) | arcs / (n·(n−1)); 0 for n < 2 |
| `diameter` | network (scalar) | longest geodesic; omitted when disconnected |
| `eccentricity` | node | longest reachable geodesic (0 for isolates) |
| `closeness` | node | (n−1) / Σ reachable geodesic distances; omitted when disconnected |
| `betweenness` | node | Brandes shortest-path betweenness (unnormalised); omitted when disconnected |
| `indegree` | node | number of incoming arcs |
| `outdegree` | node | number of outgoing arcs |
| `total-degree` | node | indegree + outdegree |
| `emission` | node | sum of outgoing tie values (weighted row sum) |
| `reception` | node | sum of incoming tie values (weighted column sum) |
| `status` | node | reception − emission |
| `determination` | node | (emission − reception) / (emission + reception); 0 when the denominator is 0 |
| `geodesics` | ordered pair | hop distance from node1 to node2; `0` = no path |

Names are matched exactly; unknown names are **rejected** with

```
unknown metric: fancyness (run 'agna --help' for the metric list)
```

and exit code 1 — there is no silent fallback to the full set.

### CSV layout

Uniform header, four columns:

```
metric,node1,node2,value
```

- scalar metrics use empty node cells: `density,,,0.2778`
- node metrics use one cell: `indegree,1,,3.0000`
- pair metrics use both: `geodesics,4,5,1.0000`

Values are rendered with four decimals using a locale-independent format
(the decimal separator is always `.`). Node names containing commas or
quotes are quoted per RFC 4180.

### JSON layout

Pretty-printed Jackson document:

```json
{
  "network" : "Example 2",
  "metrics" : [ {
    "metric" : "density",
    "value" : 0.2778
  }, {
    "metric" : "indegree",
    "value" : 4.0,
    "node1" : "1"
  }, {
    "metric" : "geodesics",
    "value" : 1.0,
    "node1" : "1",
    "node2" : "2"
  } ]
}
```

### Disconnected networks

When the network is disconnected, the distance-based measures
(`diameter`, `eccentricity`, `closeness`, `betweenness`) are omitted, and
a note is printed — but only if the selection actually includes (or
defaults to) them:

```
$ agna metrics disconnected.agn closeness
note: the network is disconnected; the distance-based measures (diameter,
eccentricity, closeness, betweenness) are omitted
```

Requesting only degree-family metrics on the same network produces the
rows without any note.

### Examples

All metrics as CSV (the default):

```
$ agna metrics samples/example2.agn
metric,node1,node2,value
density,,,0.2778
diameter,,,5.0000
eccentricity,1,,4.0000
...
```

A named selection:

```
$ agna metrics samples/example2.agn indegree betweenness
metric,node1,node2,value
indegree,1,,3.0000
indegree,2,,3.0000
...
betweenness,1,,1.0000
```

A named selection as JSON:

```
$ agna metrics samples/example2.agn --format json density betweenness
{
  "network" : "Example 2",
  "metrics" : [ {
    "metric" : "density",
    "value" : 0.2778
  }, ...
```

Save to a file:

```
$ agna metrics samples/example2.agn --out metrics.csv
wrote metrics to metrics.csv
```

Feed a generated network straight in, pipe the JSON into jq:

```
$ agna generate --nodes 50 --type random --seed 7 - \
    | agna metrics - --format json \
    | jq '.metrics[] | select(.metric == "betweenness") | [.node1, .value]'
```

Unknown metric name (exit code 1):

```
$ agna metrics samples/example2.agn fancyness
unknown metric: fancyness (run 'agna --help' for the metric list)
$ echo $?
1
```

## Shell completions and the man page

- `docs/agnacompletion.bash` — bash completion (source it from
  `~/.bashrc`); completes commands, metric names, analyses, transform
  ops, layouts, formats, types, option values, with file fallback.
- `docs/agnacompletion.zsh` — zsh completion (install as `_agna` on the
  `fpath`).
- `docs/agna.1` — the roff man page (`man ./docs/agna.1` or install
  under `share/man/man1/`).