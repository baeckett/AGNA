# Agna CLI — command reference

The command-line interface (`agna`) runs the Agna engine headlessly: no
desktop, no windowing, everything pipable and scriptable. It ships as
`agna-cli-2.1.3.jar` (run with `java -jar agna-cli-2.1.3.jar <command>`).

The CLI understands `.agn`, `.txt`, `.csv`, `.net`, `.xls`, `.xlsx`,
`.graphml`, `.gml` and `.graphson` on input, and writes the same set on
output (`.xls` needs a real file; the text formats also accept `-` for
stdout). A file argument of `-` reads agn text from stdin (override with
`--in-format`).

Exit status: 0 on success; 1 on usage errors, unknown commands/analyses/
metrics, unreadable files and exceptions.

Command summary:

| command | purpose |
|---|---|
| `version` | print the version |
| `info FILE` | network summary (name, size, edges, basic description) |
| `analyse FILE [--all\|NAME...\|cliques:N] [--out FILE]` | the full analysis battery |
| `convert IN OUT [--in-format F] [--out-format F]` | format conversion |
| `transform IN OUT --op OP` | network operations (see below) |
| `draw IN --out PNG [--layout L] [--size WxH] [--labels] [--background #rrggbb] [--no-faces] [--edge-values]` | headless visualisation |
| `generate --nodes N --out FILE [--type ...] [--seed S] [--degree D]` | seeded synthetic networks |
| `matrix FILE [--format csv\|tsv] [--out FILE]` | raw matrix table |
| `nodes FILE [--out FILE]` | node table (degrees + coordinates) |
| `ego FILE --node NAME --out OUT` | 1-hop ego network extraction |
| `components FILE` | connected components |
| `metrics FILE [METRIC...] [--all] [--format csv\|json] [--out FILE]` | structured metrics (below) |
| `distance FILE --from A --to B` | shortest path between two nodes |
| `diff A B [--out FILE.csv]` | structural comparison |
| `add-scalar FILE V --out OUT` … `renumber FILE --out OUT` | desktop Network-menu commands (see below) |
| `from-chain FILE --out OUT` | create a network from a chain file |
| `layout FILE --layout L --out OUT` | apply coordinates without rendering |
| `set FILE --out OUT [--flag VALUE ...]` | edit viewer attributes |
| `--help` | this text |

## Command details

### version

```
agna version
agna --version
```

Prints `Agna CLI 2.1.3`.

### info

```
agna info FILE
```

Prints the network name, node count, edge count and Agna's basic
description (node/edge totals, outsiders). The smallest useful command;
also the fastest way to sanity-check a file.

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
`cliques:3` for triangles), `full`.

This is the display-oriented report — rich text, tables, and Agna's own
conventions. For machine-readable values use `metrics`.

`--out FILE` writes the whole report to FILE instead of stdout.

```
$ agna analyse samples/example2.agn cliques:3
3-Cliques found in Example 2
...
```

### convert

```
agna convert IN OUT [--in-format F] [--out-format F]
```

Converts a network between formats; both are inferred from the file
extensions. `agn`, `txt`, `csv`, `net` (Pajek), `graphml`, `gml`,
`graphson` and `xls` are supported on input; `xls` additionally reads
`.xlsx` (including Apple Numbers files).

`--in-format` overrides input inference (required when `IN` is `-`);
`--out-format` overrides output inference. Text formats accept `-` for
stdout.

```
$ agna convert net.agn out.graphml
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
| `symmetrize-max` | a[i][j] = a[j][i] = max of the pair |
| `symmetrize-sum` | a[i][j] = a[j][i] = sum of the pair |
| `symmetrize-below` | copy the lower triangle onto the upper |
| `normalize-binary` | every non-zero value becomes 1 |
| `normalize-threshold:V` | values > V become 1, others 0 |
| `add-scalar:V` | add V to every cell |
| `multiply-scalar:V` | multiply every cell by V |
| `square` | matrix square (walks of length 2) |
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
`grid`, `concentric`. Default canvas 1200x900 (any `--size WxH` works; a
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
agna generate --nodes N --out FILE [--type T] [--seed S] [--degree D]
```

Generates a synthetic network (default type `random`, default degree 2).
Types:

| type | shape |
|---|---|
| `random` | seeded Erdős–Rényi, average degree D |
| `star` | one hub connected to every other node, laid out hub-and-ring |
| `circular` | an edgeless ring of nodes (right for benchmarks and empty matrices) |

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
agna components FILE
```

Lists the connected components with their member names (undirected
connectivity):

```
component 1 (9 nodes): 1, 9, 4, 5, 6, 2, 8, 7, 3
9 nodes, 1 component
```

### distance

```
agna distance FILE --from A --to B
```

Prints the shortest path between two named nodes, using the engine's
own shortest-path routine. A geodesic of 0 means no path.

```
$ agna distance in.agn --from Alice --to Bob
Shortest Path from node Alice to node Bob
     * Alice  Carol  Bob
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
agna metrics FILE [METRIC...] [--all] [--format csv|json] [--out FILE]
```

- `FILE` — the input network (any supported format; `-` for stdin).
- `METRIC...` — the metrics to produce, by name (see the list below).
  Any number may be given.
- `--all` — produce every metric. **This is also the default:** when no
  metric names are given, the full set is produced, so `agna metrics
  net.agn` behaves exactly like `agna metrics net.agn --all`.
- `--format csv|json` — output format (default `csv`).
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