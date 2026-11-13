# Agna for AI Agents & LLM Tools

**AGNA (Applied Graph and Network Analysis)** is a headless-capable, high-performance Java engine and CLI for social network analysis (SNA), sociometry, and graph theory.

This guide provides AI agents (Claude, Cursor, Copilot, ChatGPT, Antigravity) with canonical commands, structured JSON interfaces, recipes, and recovery strategies.

---

## When to Use Agna

Use Agna when the user asks to:
1. **Analyze social, organizational, or communication networks**: compute density, reciprocity, centrality (degree, closeness, betweenness, eigenvector, Bonacich, PageRank, Lin prestige), or centralization.
2. **Detect clusters, factions, or subgroups**: find maximal cliques, k-core decompositions, Watts-Strogatz local clustering, triad census (MAN 16), or Louvain modularity communities.
3. **Validate network data**: verify matrix dimensions, detect isolates, self-loops, component partitions, or syntax errors.
4. **Convert between SNA graph formats**: translate between Pajek (`.net`), GraphML, GML, GraphSON, UCINET DL, CSV/TSV matrices, and Agna native format (`.agn`).
5. **Test structural hypotheses**: run Monte Carlo permutation tests against Erdős–Rényi, degree-preserving, or dyad-census null models.
6. **Classify network archetypes**: diagnose whether a graph exhibits Small-World, Scale-Free / Preferential Attachment, or Core-Periphery topology.

---

## Quickstart & Invocations

Agna runs headless on Java 17+:
```bash
# Via pre-installed binary on PATH:
agna <command> [options]

# Or directly via shaded CLI JAR:
java -jar agna-cli-2.1.7.jar <command> [options]
```

All commands operate non-interactively with zero human prompts. Pass `-` for standard input or standard output.

---

## Standard Exit Codes

Agna CLI uses deterministic exit codes:
* **`0` (SUCCESS):** Command executed and completed normally.
* **`1` (USAGE / ARGUMENT ERROR):** Unknown command, missing required flag, or unknown metric name.
* **`2` (DATA VALIDATION FAILURE):** File not found, unparseable graph syntax, non-square matrix, or failed pre-flight validation.
* **`3` (COMPUTATIONAL FAILURE):** Algorithm convergence limit reached, or mathematical precondition not met (e.g., closeness requested on disconnected graph).

---

## Core Recipes for Agents

### 1. Pre-Flight Validation (`agna validate`)
Always validate user-supplied network files before running deep analyses:
```bash
agna validate data/network.agn --json
```

**Expected JSON Response:**
```json
{
  "valid" : true,
  "file" : "data/network.agn",
  "format" : "agn",
  "nodes" : 9,
  "edges" : 18,
  "density" : 0.25,
  "directed" : true,
  "weighted" : false,
  "connected" : true,
  "components" : 1,
  "selfLoops" : 0,
  "isolates" : [ ],
  "outsiders" : [ ],
  "diagnostics" : [ ]
}
```

If the file is malformed or missing (Exit Code 2):
```json
{
  "valid" : false,
  "file" : "missing.agn",
  "error" : "File does not exist: missing.agn",
  "hint" : "Verify the file path. Example: agna validate samples/example2.agn"
}
```

---

### 2. Network Summary (`agna info`)
Retrieve high-level topological metadata in machine-readable JSON:
```bash
agna info data/network.agn --json
```

**Expected JSON Response:**
```json
{
  "name" : "Advice Network",
  "nodes" : 15,
  "edges" : 42,
  "density" : 0.200,
  "directed" : true,
  "symmetric" : false,
  "connected" : true,
  "components" : 1,
  "outsiders" : [ ],
  "software" : {
    "name" : "AGNA CLI",
    "version" : "2.1.7"
  }
}
```

---

### 3. Metric Definition & Educational Guidance (`agna explain`)
Query definitions, formulas, and cautious sociological interpretations for any metric:
```bash
# List all 37 supported metric topics:
agna explain --list --json

# Query specific metric:
agna explain betweenness --json
```

**Expected JSON Response:**
```json
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

---

### 4. Token-Efficient Nodal Metrics (`agna metrics --top N`)
To prevent flooding the agent's context window on medium/large networks (100+ nodes), always use `--top N` with `--json`. This returns summary statistics plus only the top `N` highest-ranking nodes:
```bash
agna metrics data/network.agn betweenness --top 3 --json
```

**Expected JSON Response:**
```json
{
  "network" : "Corporate Advice",
  "topN" : 3,
  "summary" : {
    "betweenness" : {
      "count" : 15,
      "mean" : 11.6296,
      "stdDev" : 11.3784,
      "min" : 0.0,
      "max" : 30.6667
    }
  },
  "metrics" : [
    { "metric" : "betweenness", "node1" : "Alice", "value" : 30.6667 },
    { "metric" : "betweenness", "node1" : "Bob",   "value" : 25.0000 },
    { "metric" : "betweenness", "node1" : "Carol", "value" : 18.0000 }
  ]
}
```

---

### 5. Format Conversion (`agna convert`)
Convert between graph formats without loss of metadata:
```bash
# Convert CSV matrix to GraphML:
agna convert matrix.csv network.graphml

# Read from stdin, export to stdout as Pajek .net:
cat raw_matrix.csv | agna convert - - --in-format csv --out-format net
```

Supported formats: `agn`, `csv`, `tsv`, `net` (Pajek), `graphml`, `gml`, `graphson`, `dl` (UCINET), `xls`/`xlsx`.

---

### 6. Social Theory Archetype Diagnosis (`agna archetype`)
Classify empirical networks against theoretical benchmarks:
```bash
agna archetype data/network.agn --format json
```
Returns small-world coefficient ($\sigma$), power-law degree exponent ($\gamma$), and core-periphery correlation.

---

### 7. Topology-Driven Advisory (`agna advise`)
Let Agna inspect the graph topology and recommend the most methodologically sound metrics, visual layouts, and null models:
```bash
agna advise data/network.agn --json
```

**Expected JSON Response:**
```json
{
  "network" : "Corporate Advice",
  "nodes" : 15,
  "edges" : 42,
  "density" : 0.200,
  "directed" : true,
  "connected" : true,
  "components" : 1,
  "isolates" : 0,
  "summary" : "Small directed, moderately connected network (15 nodes, 42 edges)...",
  "recommendedAnalyses" : [
    { "id": "betweenness", "name": "Betweenness Centrality", "priority": "HIGH", "rationale": "Connected graph: identifies information brokers and bottlenecks." },
    { "id": "triad_census", "name": "Triad Census (MAN 16)", "priority": "HIGH", "rationale": "Directional ties: uncovers micro-structural hierarchies and transitivities." }
  ],
  "recommendedLayouts" : [
    { "layout": "concentric", "rationale": "Places primary hubs at center with peripheral actors on outer rings." },
    { "layout": "spring", "rationale": "Force-directed visualization for general structural exploration." }
  ],
  "recommendedNullModels" : [
    { "model": "degree-preserving", "rationale": "Tests whether clustering is genuine or an artifact of degree sequence." }
  ]
}
```

---

### 8. Structural Narrative Synthesis (`agna narrative`)
Generate a pre-synthesized, academically cautious plain-English or Markdown narrative of the network's density, centralization, closure, and reciprocity:
```bash
agna narrative data/network.agn --json
```

---

### 9. Feature Introspection (`agna capabilities`)
Allow the agent to dynamically inspect supported graph formats, commands, and layout models:
```bash
agna capabilities --json
```

---

### 10. Monte Carlo Hypothesis Testing (`agna test-null`)
Test whether observed transitivity or reciprocity deviates from random expectation:
```bash
agna test-null data/network.agn --metric transitivity --model degree-preserving -r 500 --format json
```

---

## Safety & Best Practices for Autonomous Agents

1. **Default to Read-Only:** All analysis commands (`info`, `validate`, `explain`, `metrics`, `analyse`, `archetype`) read from disk without modifying input files.
2. **Explicit Output Files:** Transformation commands require an explicit `--out <file>` parameter. Agna will never overwrite the source file in place unless explicitly directed to write to the same path.
3. **Piping and Stdin:** When handling dynamic or in-memory network data generated by an LLM, pass `-` as the input file and supply `--in-format csv` to stream data directly into Agna.
4. **Interpreting Results:** Follow Agna's educational guidelines: frame network metric conclusions using probabilistic, cautious phrasing (*"may indicate"*, *"often suggests"*), recognizing that relational graph topology reflects structural opportunities rather than guaranteed social behaviors.
