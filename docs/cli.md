# Agna CLI — command reference

The command-line interface (`agna`) runs the Agna engine headlessly: no
desktop, no windowing, everything pipable and scriptable. It ships as
`agna-cli-2.1.3.jar` (run with `java -jar agna-cli-2.1.3.jar <command>`).

The CLI understands `.agn`, `.txt`, `.csv`, `.net`, `.xls`, `.xlsx`,
`.graphml`, `.gml` and `.graphson` on input, and writes the same set on
output (`.xls` needs a real file; the text formats also accept `-` for
stdout). A file argument of `-` reads agn text from stdin (override with
`--in-format`).

Command summary:

| command | purpose |
|---|---|
| `version` | print the version |
| `info FILE` | network summary (name, size, edges, basic description) |
| `analyse FILE [--all\|NAME...\|cliques:N] [--out FILE]` | the full analysis battery |
| `convert IN OUT [--in-format F] [--out-format F]` | format conversion |
| `transform IN OUT --op OP` | network operations (see below) |
| `draw IN --out PNG [--layout L] [--size WxH] [--labels]` | headless visualisation |
| `generate --nodes N --out FILE [--type ...] [--seed S] [--degree D]` | seeded synthetic networks |
| `matrix FILE [--format csv\|tsv] [--out FILE]` | raw matrix table |
| `nodes FILE [--out FILE]` | node table (degrees + coordinates) |
| `ego FILE --node NAME --out OUT` | 1-hop ego network extraction |
| `components FILE` | connected components |
| `metrics FILE [METRIC...] [--all] [--format csv\|json] [--out FILE]` | structured metrics (below) |
| `distance FILE --from A --to B` | shortest path between two nodes |
| `diff A B [--out FILE.csv]` | structural comparison |
| `--help` | this text |

`transform` ops: `transpose`, `symmetrize-max`, `symmetrize-sum`,
`symmetrize-below`, `normalize-binary`, `normalize-threshold:V`,
`add-scalar:V`, `multiply-scalar:V`, `square`, `merge:FILE:POLICY`,
`delete-node:N`, `delete-nodes:N1,N2,..`, `isolate:N`,
`merge-nodes:N1,N2,..`, `remove-outsiders`.

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
    "value" : 1.0000,
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