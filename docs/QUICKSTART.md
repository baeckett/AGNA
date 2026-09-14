# AGNA Quick Start

This short exercise introduces a small directed communication network. It is
intended to help a new user open data, inspect a graph, calculate basic
measures, and save results. Allow about ten minutes.

You need AGNA Desktop (the jar from the
[latest release](https://github.com/baeckett/AGNA/releases), or
[built from source](../README.md#build-from-source)) and Java 17 or newer.
The example files ship with the repository under `samples/`.

## 1. Start AGNA and open the example

Start the desktop application:

```
java -jar agna-desktop-2.1.3.jar
```

In the File menu choose **Open** and select `samples/example1.agn` (a
12-node directed network — "Example 1"). The sociomatrix appears in the
main grid; node names are listed in the left panel.

## 2. Inspect the network

Open the **graph view**: the Toolbar button with the network symbol (View >
Graph in older terminology) draws each node and an arrow for each directed
tie. Drag a node to improve readability; zoom in and out as needed.

Ask yourself which nodes receive or send many ties, and whether any node is
isolated (no ties at all).

## 3. Calculate basic measures

From the Analysis menu choose **Full Analysis** (or the analysis commands
one by one). The Output pane reports, for this example:

```
Basic description of Example 1
Number of nodes: 12
Number of edges: 29
This network is weighted (ie, nonuniform) and nonsymmetric (ie, directed).
The network is connected.
```

Then run **Degree** (who has the most incoming/outgoing ties), **Distance**
(how easily actors can reach each other), and **Centrality** (closeness and
betweenness). Each result appears in the Output pane, ready to be copied or
exported.

## 4. Interpret cautiously

A high centrality score describes a position in the network under the chosen
data model. It does not by itself prove influence, authority, prestige, or
causal power — interpret the numbers against your research question.

## 5. Save and export

- **Save** the project under a new name (File > Save As).
- **Export** any table for later work (File > Save As with the desired
  format: text, CSV, Excel, GraphML, GML, GraphSON…).
- **Export the diagram** as an image or vector graphic for a presentation or
  paper (the graph view's File/Export option).
- Record the AGNA version and the analytical choices you used, so the work
  is reproducible.

## Same exercise from the command line

AGNA CLI gives the same network, headless, for scripts and reproducible
pipelines:

```
java -jar agna-cli-2.1.3.jar info samples/example1.agn
java -jar agna-cli-2.1.3.jar analyse samples/example1.agn --all
java -jar agna-cli-2.1.3.jar distance samples/example1.agn
java -jar agna-cli-2.1.3.jar draw samples/example1.agn --out example1.png
```

Every command prints a citation footer; `--help` documents the full surface
(30+ commands: convert, transform, matrix, metrics, components, generate…).
See [docs/cli.md](cli.md) for the complete reference.

## Where to go next

- The [user manual](manual.md) explains every menu, measure, and format,
  with formulas and worked examples.
- `samples/` contains the classic example networks (Example 1–4,
  Sociologists, 10 Circle, Star, …) — each is a small, teachable dataset.
- The [website](https://www.netanalysis.co.uk) collects releases and news.
- If AGNA misbehaves, report it with the AGNA version and the steps that
  reproduced the issue (never attach confidential network data).