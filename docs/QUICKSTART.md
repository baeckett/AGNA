# AGNA Quick Start

Welcome! This page gets AGNA running on your computer and takes you through
your first network analysis in about ten minutes. No programming is needed
for anything in the first half of this page.

## 1. Installation

Downloads live on the
[latest release page](https://github.com/baeckett/AGNA/releases). Choose the
row for your operating system:

### Linux

Download **AGNA-2.1.3-Linux-x64.tar.gz** — a self-contained archive with the
Java runtime already included (no Java installation required):

1. Extract the archive (double-click it in your file manager, or
   `tar -xzf AGNA-2.1.3-Linux-x64.tar.gz` in a terminal).
2. Open the extracted `AGNA` folder and **double-click the `AGNA` launcher**
   (in `AGNA/bin`). You can also start it from a terminal:
   `./AGNA/bin/AGNA`
3. If your system asks about the file origin, allow it to run (it is a
   trusted open-source application).

### macOS

The polished route — the **AGNA-2.1.3.dmg** disk image (built on macOS; when
it appears on the release page):

1. Download the `.dmg` and open it.
2. Drag the **AGNA** icon into your **Applications** folder.
3. Open AGNA from Applications or Launchpad (right-click and *Open* the first
   time, so macOS trusts the downloaded app).

Until the dmg is published, the jar route works on any macOS with Java 17 or
newer: download **AGNA-2.1.3-desktop.jar** and **double-click it in Finder**.
If nothing happens, install Java first (`brew install openjdk@17` or from
java.com), then double-click again — or run it from a terminal:
`java -jar AGNA-2.1.3-desktop.jar`

### Windows

1. Download **AGNA-2.1.3-desktop.jar**.
2. If you have Java 17 or newer installed, **double-click the jar in File
   Explorer** to launch AGNA.
3. No Java yet? Install it from java.com (or `winget install
   Microsoft.OpenJDK.17`), then double-click again. From a command prompt:
   `java -jar AGNA-2.1.3-desktop.jar`

### For programmers — building from source

Only needed if you want to modify or extend AGNA. Requires JDK 17 and Maven:

```
git clone https://github.com/baeckett/AGNA.git
cd AGNA
mvn verify
java -jar agna-desktop/target/agna-2.1.3.jar
```

## 2. Your first network — about ten minutes

### Start AGNA

Launch it the way your platform prefers (double-click the jar on
Windows/macOS, the `AGNA` launcher on Linux). The splash screen appears for
a few seconds; click it to skip. Alternatively, from a terminal:
`java -jar AGNA-2.1.3-desktop.jar`

### Open the example network

In the **File** menu choose **Open Network** and select `example1.agn` from
the `samples/` folder of this project (a 12-person directed communication
network). The sociomatrix grid now shows who contacts whom — row *i*,
column *j* holds the tie from person *i* to person *j*.

### Open the Network Viewer

Choose **View ▸ Network Viewer** (or press **Ctrl+Z**): each person becomes
a node, each tie an arrow. This is AGNA's visual editor — drag any node to a
better position, zoom as needed.

**Try the layouts.** In the Network Viewer's **Image** menu you find
**Grid Layout**, **Concentric Layout**, and **Spring Layout**. Apply each in
turn — Grid arranges the nodes in rows, Concentric puts the best-connected
nodes in the middle, Spring acts like a physical model of the ties. Keep the
arrangement that reads most clearly. Which people receive or send many ties?
Is anyone isolated?

### Calculate the basic measures

From the **Analysis** menu run **Full Analysis** — one report with size,
density, components, distances, and centrality:

```
Basic description of Example 1
Number of nodes: 12
Number of edges: 29
This network is weighted (ie, nonuniform) and nonsymmetric (ie, directed).
The network is connected.
```

Then run the individual commands — **Indegree** (who receives the most
ties?), **Outdegree** (who sends the most?), **Distance** (how easily can
actors reach each other?) — and read the results in the Output pane.

### Interpret cautiously

A high centrality score describes a position in the network under the chosen
data model. It does not by itself prove influence, authority, prestige, or
causal power — interpret the numbers against your research question.

### Save and export

- **File ▸ Save Network As...** saves your project (agn, text, CSV, Excel,
  Pajek, GraphML, GML, GraphSON — many tools read at least one of these).
- **File ▸ Export Image** (in the Network Viewer) saves the diagram as an
  image or vector graphic for a paper or presentation.
- Record the AGNA version and the analytical choices you used, so the work
  is reproducible.

## 3. The same exercise at the command line

AGNA CLI gives you the same network headlessly — for scripts and
reproducible pipelines:

```
java -jar agna-cli-2.1.3.jar info samples/example1.agn
java -jar agna-cli-2.1.3.jar analyse samples/example1.agn --all
java -jar agna-cli-2.1.3.jar distance samples/example1.agn
java -jar agna-cli-2.1.3.jar draw samples/example1.agn --out example1.png
```

Every command prints a citation footer; `--help` lists the full surface
(30+ commands). See [docs/cli.md](cli.md) for the reference.

## 4. Where to go next

- The **[user manual](manual.md)** explains every menu, measure, and format,
  with formulas and worked examples — also inside the app via **Help ▸
  Contents**.
- `samples/` contains the classic example networks (Example 1–4,
  Sociologists, 10 Circle, Star, ...) — each is a small, teachable dataset.
- The [website](https://www.netanalysis.co.uk) collects releases and news.
- If AGNA misbehaves, report it at
  [the issue tracker](https://github.com/baeckett/AGNA/issues) with the AGNA
  version and the steps that reproduced the problem (never attach
  confidential network data).