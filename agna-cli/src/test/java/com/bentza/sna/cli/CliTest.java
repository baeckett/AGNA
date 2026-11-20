/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.bentza.sna.Environment;

/**
 * 2.1.3: the command-line surface - info, analyse, convert, transform
 * and draw - must work end to end against the engine.
 */
public class CliTest
    {
    private String run(String... args) throws Exception
        {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int code = new Cli(new java.io.PrintStream(buf)).run(args);
        String text = buf.toString(StandardCharsets.UTF_8.name());
        assertEquals(0, code, "exit code for " + String.join(" ", args)
                + " -> " + text);
        return text;
        }

    @Test
    public void infoPrintsASummary() throws Exception
        {
        String text = run("info", "samples/example2.agn");
        assertTrue(text.contains("nodes:  9"), text);
        }

    @Test
    public void analyseRunsTheFullBattery() throws Exception
        {
        String text = run("analyse", "samples/4 full.txt", "--all");
        assertTrue(text.toLowerCase().contains("density"), text.substring(0,
                Math.min(120, text.length())));
        }

    @Test
    public void convertRoundTrips() throws Exception
        {
        File out = File.createTempFile("agna_cli", ".graphml");
        out.deleteOnExit();
        run("convert", "samples/example2.agn", out.getAbsolutePath());
        String back = new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(back.contains("<graphml"), "graphml written");
        }

    @Test
    public void transformTransposesAndWrites() throws Exception
        {
        File out = File.createTempFile("agna_cli_t", ".agn");
        out.deleteOnExit();
        run("transform", "samples/example2.agn", out.getAbsolutePath(),
                "--op", "transpose");
        String back = new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.ISO_8859_1);
        assertTrue(back.contains("Agna Data File"), "agn written");
        }

    @Test
    public void drawProducesARealPng() throws Exception
        {
        File png = File.createTempFile("agna_cli_draw", ".png");
        png.deleteOnExit();
        run("draw", "samples/example2.agn", "--out", png.getAbsolutePath(),
                "--layout", "circular", "--size", "600x400", "--labels");
        byte[] bytes = Files.readAllBytes(png.toPath());
        assertTrue(bytes.length > 1000, "png has content");
        // PNG magic
        assertTrue(bytes[0] == (byte) 0x89 && bytes[1] == 'P'
                && bytes[2] == 'N' && bytes[3] == 'G', "png signature");
        }

    @Test
    public void drawSupportsEveryNamedLayout() throws Exception
        {
        for (String layout : new String[] { "grid", "concentric", "spring",
                "star", "hierarchical", "core-periphery", "ego-radial",
                "community", "layered-dag", "linear" })
            {
            File png = File.createTempFile("agna_cli_draw_" + layout,
                    ".png");
            png.deleteOnExit();
            run("draw", "samples/example2.agn", "--out",
                    png.getAbsolutePath(), "--layout", layout);
            byte[] bytes = Files.readAllBytes(png.toPath());
            assertTrue(bytes.length > 1000, layout + " png has content");
            }
        }

    @Test
    public void versionPrintsTheVersion() throws Exception
        {
        assertTrue(run("version").contains("Agna CLI " + Environment.getApplicationVersion()));
        assertTrue(run("--version").contains("Agna CLI " + Environment.getApplicationVersion()));
        }

    @Test
    public void extOfHandlesDirectoryDots()
        {
        assertEquals("csv", Cli.extOf("/Volumes/Data.2.1.4/network.csv"));
        assertEquals("agn", Cli.extOf("/Volumes/Data.2.1.4/network"));
        assertEquals("net", Cli.extOf("sample.NET"));
        }

    @Test
    public void convertThroughPipes() throws Exception
        {
        // stdin -> file
        byte[] sample = Files.readAllBytes(new File("samples/example2.agn")
                .toPath());
        File out = File.createTempFile("agna_cli_pipe", ".graphml");
        out.deleteOnExit();
        Cli cli = new Cli(new java.io.PrintStream(
                new ByteArrayOutputStream()), new java.io.ByteArrayInputStream(
                        sample));
        int code = cli.run(new String[] { "convert", "-",
                out.getAbsolutePath() });
        assertEquals(0, code);
        assertTrue(new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.UTF_8).contains("<graphml"),
                "stdin converted to graphml");
        // file -> stdout
        String text = run("convert", "samples/example2.agn", "-",
                "--out-format", "graphml");
        assertTrue(text.contains("<graphml"), "stdout carries graphml");
        }

    @Test
    public void generateIsReproducibleAndStarWorks() throws Exception
        {
        File a = File.createTempFile("agna_gen_a", ".agn");
        File b = File.createTempFile("agna_gen_b", ".agn");
        a.deleteOnExit();
        b.deleteOnExit();
        run("generate", "--nodes", "30", "--type", "random", "--seed",
                "99", "--out", a.getAbsolutePath());
        run("generate", "--nodes", "30", "--type", "random", "--seed",
                "99", "--out", b.getAbsolutePath());
        assertTrue(java.util.Arrays.equals(Files.readAllBytes(a.toPath()),
                Files.readAllBytes(b.toPath())),
                "same seed produces identical output");
        File star = File.createTempFile("agna_gen_star", ".agn");
        star.deleteOnExit();
        run("generate", "--nodes", "8", "--type", "star", "--out",
                star.getAbsolutePath());
        assertTrue(run("info", star.getAbsolutePath()).contains("nodes:  8"),
                "star has 8 nodes");
        }

    @Test
    public void matrixPrintsNamesAndValues() throws Exception
        {
        String text = run("matrix", "samples/example2.agn");
        String header = text.substring(0, text.indexOf('\n'));
        assertTrue(header.contains("1") && header.contains("2"),
                "matrix header names: " + header);
        assertTrue(text.lines().count() >= 10, "matrix has rows");
        }

    @Test
    public void nodesTableHasHeaderAndRows() throws Exception
        {
        String text = run("nodes", "samples/example2.agn");
        assertTrue(text.startsWith("index\tname\tout\tin\tx\ty"), text);
        assertTrue(text.lines().count() == 10, "header + 9 rows");
        }

    @Test
    public void egoExtractsASmallerNetwork() throws Exception
        {
        String name = firstNodeName();
        File out = File.createTempFile("agna_cli_ego", ".agn");
        out.deleteOnExit();
        String text = run("ego", "samples/example2.agn", "--node", name,
                "--out", out.getAbsolutePath());
        assertTrue(text.contains("ego network"), text);
        String info = run("info", out.getAbsolutePath());
        assertTrue(info.contains("nodes:  "), info);
        int nodes = parseNodes(info);
        assertTrue(nodes >= 2 && nodes < 9,
                "ego network smaller than the source: " + nodes);
        }

    @Test
    public void componentsCountsTheExample() throws Exception
        {
        String text = run("components", "samples/example2.agn");
        assertTrue(text.contains("component 1"), text);
        assertTrue(text.contains("1 component"), text);
        }

    @Test
    public void componentsOnAnEdgeFreeGraph() throws Exception
        {
        File f = File.createTempFile("agna_cli_comp", ".agn");
        f.deleteOnExit();
        run("generate", "--nodes", "6", "--type", "empty", "--out",
                f.getAbsolutePath());
        String text = run("components", f.getAbsolutePath());
        assertTrue(text.contains("6 components"), text);
        }

    @Test
    public void transformDeleteNodesShrinksTheNetwork() throws Exception
        {
        String[] names = firstTwoNames();
        File out = File.createTempFile("agna_cli_del", ".agn");
        out.deleteOnExit();
        run("transform", "samples/example2.agn", out.getAbsolutePath(),
                "--op", "delete-nodes:" + names[0] + "," + names[1]);
        assertTrue(parseNodes(run("info", out.getAbsolutePath())) == 7,
                "two nodes deleted");
        }

    @Test
    public void transformMergeNodesShrinksByOne() throws Exception
        {
        String[] names = firstTwoNames();
        File out = File.createTempFile("agna_cli_mer", ".agn");
        out.deleteOnExit();
        run("transform", "samples/example2.agn", out.getAbsolutePath(),
                "--op", "merge-nodes:" + names[0] + "," + names[1]);
        assertTrue(parseNodes(run("info", out.getAbsolutePath())) == 8,
                "one node merged away");
        }

    @Test
    public void transformRemoveOutsidersOnAnEdgeFreeGraph() throws Exception
        {
        File f = File.createTempFile("agna_cli_out", ".agn");
        f.deleteOnExit();
        run("generate", "--nodes", "6", "--type", "empty", "--out",
                f.getAbsolutePath());
        File out = File.createTempFile("agna_cli_out2", ".agn");
        out.deleteOnExit();
        run("transform", f.getAbsolutePath(), out.getAbsolutePath(),
                "--op", "remove-outsiders");
        // all six nodes are outsiders and get removed in memory; the agn
        // writer/reader floor an empty network at 2 nodes, so the file
        // round-trips as 2 (without the op it would read back as 6)
        assertTrue(parseNodes(run("info", out.getAbsolutePath())) == 2,
                "all outsiders removed");
        }

    private String firstNodeName() throws Exception
        {
        String text = run("nodes", "samples/example2.agn");
        return text.lines().skip(1).findFirst().get().split("\t")[1];
        }

    private String[] firstTwoNames() throws Exception
        {
        String text = run("nodes", "samples/example2.agn");
        String[] lines = text.split("\n");
        return new String[] { lines[1].split("\t")[1],
                lines[2].split("\t")[1] };
        }

    private static int parseNodes(String infoText)
        {
        for (String line : infoText.split("\n"))
            {
            if (line.startsWith("nodes: "))
                {
                return Integer.parseInt(line.substring(7).trim());
                }
            }
        return -1;
        }

    @Test
    public void analyseAllIncludesEveryMetric() throws Exception
        {
        String text = run("analyse", "samples/example2.agn", "--all");
        assertTrue(text.contains("Betweenness"), text.substring(0,
                Math.min(200, text.length())));
        assertTrue(text.contains("Determination")
                && text.contains("Emission")
                && text.contains("Reception"), "degree family present");
        assertTrue(text.contains("Cliques found")
                && text.contains("Chain summary"), "cliques + chain present");
        }

    @Test
    public void analyseOutSavesTheReport() throws Exception
        {
        File out = File.createTempFile("agna_cli_rep", ".txt");
        out.deleteOnExit();
        String text = run("analyse", "samples/example2.agn", "density",
                "--out", out.getAbsolutePath());
        assertTrue(text.contains("wrote analysis report"), text);
        assertTrue(new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.UTF_8).contains("Density"),
                "report file carries the analysis");
        }

    @Test
    public void metricsCsvCoversEveryRowShape() throws Exception
        {
        String text = run("metrics", "samples/example2.agn");
        assertTrue(text.startsWith("metric,node1,node2,value"), text);
        assertTrue(text.contains("density,,"), "scalar row");
        assertTrue(text.contains("indegree,") && text.contains("betweenness,"),
                "node rows");
        assertTrue(text.contains("geodesics,"), "pair rows");
        assertTrue(text.contains("closeness,")
                && text.contains("determination,"), "connected metrics");
        }

    @Test
    public void metricsJsonIsStructured() throws Exception
        {
        String text = run("metrics", "samples/example2.agn", "--format",
                "json");
        assertTrue(text.contains("\"network\""), text.substring(0,
                Math.min(120, text.length())));
        assertTrue(text.contains("\"metric\"") && text.contains(
                "\"betweenness\""), "metrics in json");
        }

    @Test
    public void metricsOutWritesACsvFile() throws Exception
        {
        File out = File.createTempFile("agna_cli_met", ".csv");
        out.deleteOnExit();
        String text = run("metrics", "samples/example2.agn", "--out",
                out.getAbsolutePath());
        assertTrue(text.contains("wrote metrics"), text);
        assertTrue(new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.UTF_8).startsWith(
                        "metric,node1,node2,value"), "csv header");
        }

    @Test
    public void distanceReportsAPath() throws Exception
        {
        String text = run("distance", "samples/example2.agn", "--from", "1",
                "--to", "9");
        assertTrue(text.length() > 30, "path output present");
        }

    @Test
    public void diffReportsEdgeDifferences() throws Exception
        {
        File star = File.createTempFile("agna_cli_diff_a", ".agn");
        File rnd = File.createTempFile("agna_cli_diff_b", ".agn");
        star.deleteOnExit();
        rnd.deleteOnExit();
        run("generate", "--nodes", "8", "--type", "star", "--out",
                star.getAbsolutePath());
        run("generate", "--nodes", "8", "--type", "random", "--seed", "3",
                "--out", rnd.getAbsolutePath());
        String text = run("diff", star.getAbsolutePath(),
                rnd.getAbsolutePath());
        assertTrue(text.contains("0 node(s) added")
                && text.contains("0 node(s) removed"), text);
        assertTrue(text.contains("edge-diff,"), "edge rows present");
        }

    @Test
    public void nodesOutWritesTheTable() throws Exception
        {
        File out = File.createTempFile("agna_cli_nodes", ".csv");
        out.deleteOnExit();
        String text = run("nodes", "samples/example2.agn", "--out",
                out.getAbsolutePath());
        assertTrue(text.contains("wrote node table"), text);
        assertTrue(new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.UTF_8).startsWith("index\tname\tout"),
                "node table file");
        }

    @Test
    public void metricsFilterToNamedMetrics() throws Exception
        {
        String text = run("metrics", "samples/example2.agn", "indegree",
                "betweenness");
        String[] lines = text.split("\n");
        assertTrue(lines[0].equals("metric,node1,node2,value"),
                "header kept");
        assertTrue(lines.length == 19, "header + 9 indegree + 9 "
                + "betweenness rows, got " + lines.length);
        for (int i = 1; i < lines.length; i++)
            {
            String metric = lines[i].split(",")[0];
            assertTrue(metric.equals("indegree") || metric.equals(
                    "betweenness"), "unexpected metric " + metric);
            }
        }

    @Test
    public void metricsRejectsUnknownNames() throws Exception
        {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int code = new Cli(new java.io.PrintStream(buf)).run(new String[] {
                "metrics", "samples/example2.agn", "fancyness" });
        assertEquals(1, code);
        assertTrue(buf.toString().contains("unknown metric"), buf.toString());
        }

    @Test
    public void networkCommandAddScalar() throws Exception
        {
        File out = File.createTempFile("agna_cli_scalar", ".agn");
        out.deleteOnExit();
        run("add-scalar", "samples/example2.agn", "5", "--out",
                out.getAbsolutePath());
        // a former 1 becomes 6; assert via the matrix output
        String matrix = run("matrix", out.getAbsolutePath());
        assertTrue(matrix.contains("\t6\t") || matrix.contains("\t6\n")
                || matrix.contains("6\t"), "1 + 5 = 6 present: " + matrix
                        .substring(0, Math.min(80, matrix.length())));
        }

    @Test
    public void networkCommandSymmetrizeMakesItSymmetric() throws Exception
        {
        File rnd = File.createTempFile("agna_cli_sym_in", ".agn");
        File out = File.createTempFile("agna_cli_sym_out", ".agn");
        rnd.deleteOnExit();
        out.deleteOnExit();
        run("generate", "--nodes", "8", "--type", "random", "--seed", "5",
                "--out", rnd.getAbsolutePath());
        run("symmetrize", rnd.getAbsolutePath(), "--mode", "max", "--out",
                out.getAbsolutePath());
        String[] rows = run("matrix", out.getAbsolutePath()).split("\n");
        for (int i = 0; i < 8; i++)
            {
            for (int j = 0; j < 8; j++)
                {
                double a = Float.parseFloat(rows[i + 1].split("\t")[j + 1]);
                double b = Float.parseFloat(rows[j + 1].split("\t")[i + 1]);
                assertTrue(a == b, "symmetric at " + i + "," + j);
                }
            }
        }

    @Test
    public void networkCommandMergeNetworks() throws Exception
        {
        File a = File.createTempFile("agna_cli_mg_a", ".agn");
        File b = File.createTempFile("agna_cli_mg_b", ".agn");
        File out = File.createTempFile("agna_cli_mg_out", ".agn");
        a.deleteOnExit();
        b.deleteOnExit();
        out.deleteOnExit();
        run("generate", "--nodes", "8", "--type", "star", "--out",
                a.getAbsolutePath());
        run("generate", "--nodes", "8", "--type", "star", "--out",
                b.getAbsolutePath());
        run("merge-networks", a.getAbsolutePath(), b.getAbsolutePath(),
                "--mode", "sum", "--out", out.getAbsolutePath());
        String info = run("info", out.getAbsolutePath());
        // sum policy doubles tie VALUES; the nonzero pattern is unchanged
        assertTrue(info.contains("nodes:  8"), info);
        assertTrue(info.contains("edges:  14"), info);
        assertTrue(run("matrix", out.getAbsolutePath()).contains("\t2\t"),
                "arcs doubled to 2");
        }

    @Test
    public void networkCommandTransposeRoundTrips() throws Exception
        {
        File once = File.createTempFile("agna_cli_tr_1", ".agn");
        File twice = File.createTempFile("agna_cli_tr_2", ".agn");
        once.deleteOnExit();
        twice.deleteOnExit();
        run("transpose", "samples/example2.agn", "--out",
                once.getAbsolutePath());
        run("transpose", once.getAbsolutePath(), "--out",
                twice.getAbsolutePath());
        assertEquals(run("matrix", "samples/example2.agn"),
                run("matrix", twice.getAbsolutePath()),
                "transpose twice = identity");
        }

    @Test
    public void networkCommandRenumberAndAddNodes() throws Exception
        {
        File star = File.createTempFile("agna_cli_rn", ".agn");
        File out = File.createTempFile("agna_cli_rn_out", ".agn");
        star.deleteOnExit();
        out.deleteOnExit();
        run("generate", "--nodes", "8", "--type", "star", "--out",
                star.getAbsolutePath());
        run("renumber", star.getAbsolutePath(), "--out",
                out.getAbsolutePath());
        String[] rows = run("nodes", out.getAbsolutePath()).split("\n");
        for (int i = 1; i < rows.length; i++)
            {
            assertEquals(String.valueOf(i), rows[i].split("\t")[1],
                    "node names renumbered");
            }
        File more = File.createTempFile("agna_cli_addn", ".agn");
        more.deleteOnExit();
        run("add-nodes", out.getAbsolutePath(), "--count", "3", "--out",
                more.getAbsolutePath());
        assertTrue(parseNodes(run("info", more.getAbsolutePath())) == 11,
                "three nodes added");
        }

    @Test
    public void fromChainCreatesANetwork() throws Exception
        {
        File chain = File.createTempFile("agna_cli_chain", ".txt");
        File out = File.createTempFile("agna_cli_chain_out", ".agn");
        chain.deleteOnExit();
        out.deleteOnExit();
        Files.write(chain.toPath(),
                "alpha beta gamma beta alpha delta".getBytes(
                        StandardCharsets.UTF_8));
        run("from-chain", chain.getAbsolutePath(), "--out",
                out.getAbsolutePath());
        String info = run("info", out.getAbsolutePath());
        assertTrue(info.contains("nodes:  "), info);
        int nodes = parseNodes(info);
        assertTrue(nodes >= 2, "chain nodes present: " + nodes);
        }

    @Test
    public void layoutSavesDeterministicCoordinates() throws Exception
        {
        File src = File.createTempFile("agna_cli_lay_in", ".agn");
        File a = File.createTempFile("agna_cli_lay_a", ".agn");
        File b = File.createTempFile("agna_cli_lay_b", ".agn");
        src.deleteOnExit();
        a.deleteOnExit();
        b.deleteOnExit();
        run("generate", "--nodes", "12", "--type", "random", "--seed", "8",
                "--out", src.getAbsolutePath());
        run("layout", src.getAbsolutePath(), "--layout", "spring", "--out",
                a.getAbsolutePath());
        run("layout", src.getAbsolutePath(), "--layout", "spring", "--out",
                b.getAbsolutePath());
        assertTrue(java.util.Arrays.equals(Files.readAllBytes(a.toPath()),
                Files.readAllBytes(b.toPath())),
                "layout is deterministic");
        assertTrue(run("info", a.getAbsolutePath()).contains("nodes:  12"),
                "laid-out file opens");
        }

    @Test
    public void layoutCommandSupportsAllNewLayouts() throws Exception
        {
        File src = File.createTempFile("agna_cli_lay_new_in", ".agn");
        src.deleteOnExit();
        run("generate", "--nodes", "15", "--type", "random-connected", "--out", src.getAbsolutePath());
        for (String layout : new String[] { "core-periphery", "ego-radial", "community", "layered-dag", "linear" })
            {
            File out = File.createTempFile("agna_cli_lay_" + layout, ".agn");
            out.deleteOnExit();
            String res = run("layout", src.getAbsolutePath(), "--layout", layout, "--out", out.getAbsolutePath());
            assertTrue(res.contains("layout (" + layout + ") ->"), res);
            assertTrue(out.length() > 0, layout + " file written");
            }
        }

    @Test
    public void setModifiesViewerAttributes() throws Exception
        {
        File src = File.createTempFile("agna_cli_set_in", ".agn");
        File out = File.createTempFile("agna_cli_set_out", ".agn");
        src.deleteOnExit();
        out.deleteOnExit();
        run("generate", "--nodes", "6", "--type", "star", "--out",
                src.getAbsolutePath());
        run("set", src.getAbsolutePath(), "--out", out.getAbsolutePath(),
                "--names-visible", "on", "--background-color", "#ff0000",
                "--max-transparency", "100", "--grid-color", "#0000ff",
                "--default-face", "sample_face.png");
        com.bentza.sna.net.FullNet back = new Cli(
                new java.io.PrintStream(new ByteArrayOutputStream())).open(
                        out.getAbsolutePath());
        assertTrue(back.getArea().getPrintNames(), "names visible");
        assertTrue(back.getArea().getBackgroundColor().getRGB()
                == new java.awt.Color(255, 0, 0).getRGB(),
                "background red");
        assertTrue(back.getArea().getMaxTransparency() == 100,
                "max transparency");
        assertTrue(back.getArea().getGridColor().getRGB()
                == new java.awt.Color(0, 0, 255).getRGB(), "grid blue");
        }

    @Test
    public void cloneNodeCopiesTies() throws Exception
        {
        File out = File.createTempFile("agna_cli_clone", ".agn");
        out.deleteOnExit();
        String[] names = firstTwoNames();
        run("transform", "samples/example2.agn", out.getAbsolutePath(),
                "--op", "clone-node:" + names[0]);
        String info = run("info", out.getAbsolutePath());
        assertTrue(info.contains("nodes:  10"), info);
        String matrix = run("matrix", out.getAbsolutePath());
        String firstDataRow = matrix.lines().skip(1).findFirst().get();
        assertTrue(firstDataRow.split("\t").length == 11,
                "ten matrix columns, got " + firstDataRow.split("\t").length);
        assertTrue(matrix.contains("Clone of"), "clone named");
        }

    @Test
    public void infoOutWritesTheReport() throws Exception
        {
        File out = File.createTempFile("agna_cli_info", ".txt");
        out.deleteOnExit();
        String text = run("info", "samples/example2.agn", "--out",
                out.getAbsolutePath());
        assertTrue(text.contains("wrote summary"), text);
        String report = new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(report.contains("name:   Example 2"), report);
        assertTrue(report.contains("The network is connected."), report);
        }

    @Test
    public void componentsOutWritesCsv() throws Exception
        {
        File out = File.createTempFile("agna_cli_comp_out", ".csv");
        out.deleteOnExit();
        String text = run("components", "samples/example2.agn", "--out",
                out.getAbsolutePath());
        assertTrue(text.contains("wrote components"), text);
        String csv = new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("component,node"), csv);
        assertTrue(csv.lines().count() == 10, "header + 9 nodes");
        }

    @Test
    public void distanceOutWritesCsv() throws Exception
        {
        File out = File.createTempFile("agna_cli_dist_out", ".csv");
        out.deleteOnExit();
        String text = run("distance", "samples/example2.agn", "--from", "1",
                "--to", "9", "--out", out.getAbsolutePath());
        assertTrue(text.contains("wrote distance"), text);
        String csv = new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("from,to,hops,path"), csv);
        String row = csv.lines().skip(1).findFirst().get();
        assertTrue(row.startsWith("1,9,1,"), row);
        assertTrue(row.contains("1 > 9"), row);
        }

    @Test
    public void convertPreservesUnicodeNames() throws Exception
        {
        // Phase A: a UTF-8 agn file with CJK names must convert and
        // reopen with the names intact
        File in = File.createTempFile("agna_cli_utf8_in", ".agn");
        File out = File.createTempFile("agna_cli_utf8_out", ".graphml");
        in.deleteOnExit();
        out.deleteOnExit();
        Files.write(in.toPath(), ("Agna Data File\nVersion\t2.1.3\n"
                + "Network Name\t网络测试\nNetwork Size\t2\nHas Viewer\tno\n"
                + "Node Names\n中文节点\tБукурешть\t\nEnd Node Names\n"
                + "Network Matrix\n0\t1\n1\t0\nEnd Network Matrix\n")
                .getBytes(StandardCharsets.UTF_8));
        run("convert", in.getAbsolutePath(), out.getAbsolutePath());
        String matrix = run("matrix", out.getAbsolutePath());
        assertTrue(matrix.contains("中文节点"), matrix);
        assertTrue(matrix.contains("Букурешть"), matrix);
        }

    @Test
    public void drawRespectsBackgroundAndFlags() throws Exception
        {
        File png = File.createTempFile("agna_cli_draw_bg", ".png");
        png.deleteOnExit();
        run("draw", "samples/example2.agn", "--out", png.getAbsolutePath(),
                "--layout", "circular", "--size", "400x300",
                "--background", "#102030", "--no-faces");
        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(png);
        assertEquals(400, img.getWidth());
        assertEquals(300, img.getHeight());
        int corner = img.getRGB(2, 2) & 0xffffff;
        assertEquals(0x102030, corner, "corner pixel is the background");
        }

    @Test
    public void drawPrintsTheUsedCoordinates() throws Exception
        {
        File png = File.createTempFile("agna_cli_draw_co", ".png");
        png.deleteOnExit();
        String text = run("draw", "samples/example2.agn", "--out",
                png.getAbsolutePath(), "--layout", "grid");
        assertTrue(text.contains("index\tname\tx\ty"), text);
        assertTrue(text.lines().count() == 11, "1 header + 9 coords");
        assertTrue(text.contains("(layout grid"), text);
        }

    @Test
    public void generateCircularProducesConnectedRing() throws Exception
        {
        File f = File.createTempFile("agna_cli_circ", ".agn");
        f.deleteOnExit();
        run("generate", "--nodes", "8", "--type", "circular", "--out", f.getAbsolutePath());
        String info = run("info", f.getAbsolutePath());
        assertTrue(info.contains("nodes:  8"));
        assertTrue(info.contains("edges:  16")); // 8 undirected = 16 directed
        String comp = run("components", f.getAbsolutePath());
        assertTrue(comp.contains("1 components") || comp.contains("1 component"), comp);
        }

    @Test
    public void generateTopologies215() throws Exception
        {
        File f = File.createTempFile("agna_cli_topos", ".agn");
        f.deleteOnExit();

        // 1. Watts-Strogatz
        run("generate", "--nodes", "20", "--type", "watts-strogatz", "-k", "4", "--beta", "0.1", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  20"));

        // 2. Barabasi-Albert
        run("generate", "--nodes", "25", "--type", "barabasi-albert", "-m", "2", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  25"));

        // 3. SBM
        run("generate", "--nodes", "30", "--type", "sbm", "--blocks", "3", "--p-in", "0.6", "--p-out", "0.05", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  30"));

        // 4. Erdos-Renyi
        run("generate", "--nodes", "15", "--type", "erdos-renyi", "-p", "0.3", "--directed", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  15"));

        // 5. Linear Chain
        run("generate", "--nodes", "10", "--type", "linear", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  10"));

        // 6. Hierarchical Tree
        run("generate", "--nodes", "13", "--type", "tree", "-b", "3", "--lateral", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  13"));

        // 7. Square lattice
        run("generate", "--type", "square-lattice", "--side", "4", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  16"));

        // 8. Beehive
        run("generate", "--type", "beehive", "--radius", "2", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  24"));

        // 9. Hexagram
        run("generate", "--type", "hexagram", "--order", "2", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:"));

        // 10. Random connected
        run("generate", "--nodes", "12", "--type", "random-connected", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  12"));

        // 11. Random weighted
        run("generate", "--nodes", "10", "--type", "random-weighted", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  10"));

        // 12. Full binary
        run("generate", "--nodes", "5", "--type", "full", "--out", f.getAbsolutePath());
        assertTrue(run("info", f.getAbsolutePath()).contains("nodes:  5"));
        assertTrue(run("info", f.getAbsolutePath()).contains("edges:  20"));
        }

    @Test
    public void drawAndLayoutHierarchical() throws Exception
        {
        File tree = File.createTempFile("agna_cli_tree", ".agn");
        File laidOut = File.createTempFile("agna_cli_tree_lay", ".agn");
        File png = File.createTempFile("agna_cli_tree_png", ".png");
        tree.deleteOnExit();
        laidOut.deleteOnExit();
        png.deleteOnExit();

        run("generate", "--nodes", "7", "--type", "tree", "-b", "2", "--out", tree.getAbsolutePath());
        run("layout", tree.getAbsolutePath(), "--layout", "hierarchical", "--out", laidOut.getAbsolutePath());
        assertTrue(laidOut.exists());

        String text = run("draw", tree.getAbsolutePath(), "--out", png.getAbsolutePath(), "--layout", "hierarchical");
        assertTrue(text.contains("hierarchical"), text);
        byte[] bytes = Files.readAllBytes(png.toPath());
        assertTrue(bytes.length > 500, "rendered png has content");
        }

    @Test
    public void testNullModelSubcommand() throws Exception
        {
        File sample = new File("samples/example2.agn");
        // Test text format
        String outText = run("test-null", sample.getAbsolutePath(), "--metric", "transitivity",
                "--model", "degree-preserving", "-r", "50", "--seed", "42");
        assertTrue(outText.contains("AGNA STATISTICAL HYPOTHESIS TEST"), outText);
        assertTrue(outText.contains("Null Distribution Histogram"), outText);

        // Test JSON format
        String outJson = run("test-null", sample.getAbsolutePath(), "--metric", "reciprocity",
                "--model", "erdos-renyi", "-r", "50", "--seed", "42", "--format", "json");
        assertTrue(outJson.contains("\"metric\": \"RECIPROCITY\""), outJson);
        assertTrue(outJson.contains("\"null_model\": \"ERDOS_RENYI\""), outJson);
        assertTrue(outJson.contains("\"z_score\":"), outJson);

        // Test CSV format
        String outCsv = run("test-null", sample.getAbsolutePath(), "--metric", "density",
                "--model", "dyad-census", "-r", "50", "--seed", "42", "--format", "csv");
        assertTrue(outCsv.contains("network,metric,null_model,replications"), outCsv);
        assertTrue(outCsv.contains("DYAD_CENSUS"), outCsv);
        }

    @Test
    public void testAnalyseFormats() throws Exception
        {
        File sample = new File("samples/example2.agn");

        // Test CSV format
        String csvOut = run("analyse", sample.getAbsolutePath(), "indegree", "--format", "csv");
        assertTrue(csvOut.contains("# Table:"), csvOut);
        assertTrue(csvOut.contains("Node,Indegree*,Indegree**"), csvOut);

        // Test JSON format
        String jsonOut = run("analyse", sample.getAbsolutePath(), "indegree", "--format", "json");
        assertTrue(jsonOut.contains("\"title\":"), jsonOut);
        assertTrue(jsonOut.contains("\"columns\":"), jsonOut);

        // Test HTML format
        String htmlOut = run("analyse", sample.getAbsolutePath(), "density", "--format", "html");
        assertTrue(htmlOut.contains("<!DOCTYPE html>"), htmlOut);
        assertTrue(htmlOut.contains("border-collapse:collapse"), htmlOut);
        }

    @Test
    public void testArchetypeDiagnosis() throws Exception
        {
        File sample = new File("samples/example2.agn");

        // Test text format
        String textOut = run("archetype", sample.getAbsolutePath());
        assertTrue(textOut.contains("Theoretical Archetype Fit Summary"), textOut);
        assertTrue(textOut.contains("Structural Signature Diagnostics"), textOut);
        assertTrue(textOut.contains("Notes:"), textOut);

        // Test JSON format
        String jsonOut = run("archetype", sample.getAbsolutePath(), "--format", "json");
        assertTrue(jsonOut.contains("\"title\": \"Theoretical Archetype Fit Summary\""), jsonOut);

        // Test HTML format
        String htmlOut = run("archetype", sample.getAbsolutePath(), "--format", "html");
        assertTrue(htmlOut.contains("Theoretical Structural Archetypes Diagnostic"), htmlOut);
        assertTrue(htmlOut.contains("Notes / Methodological Remarks"), htmlOut);
        }

    @Test
    public void testBenchmarkCommand() throws Exception
        {
        File sample = new File("samples/example2.agn");

        // Test text format
        String textOut = run("benchmark", sample.getAbsolutePath(), "--iterations", "2");
        assertTrue(textOut.contains("Algorithm Benchmark Results"), textOut);
        assertTrue(textOut.contains("Brandes Betweenness"), textOut);
        assertTrue(textOut.contains("Watts-Strogatz Clustering"), textOut);
        assertTrue(textOut.contains("All-Pairs Geodesics"), textOut);
        assertTrue(textOut.contains("Notes:"), textOut);

        // Test JSON format
        String jsonOut = run("benchmark", sample.getAbsolutePath(), "--iterations", "2", "--format", "json");
        assertTrue(jsonOut.contains("\"title\": \"Algorithm Benchmark Results\""), jsonOut);

        // Test HTML format
        String htmlOut = run("benchmark", sample.getAbsolutePath(), "--iterations", "2", "--format", "html");
        assertTrue(htmlOut.contains("Algorithm Performance"), htmlOut);
        assertTrue(htmlOut.contains("Notes / Methodological Remarks"), htmlOut);
        }

    @Test
    public void testNewAnalysisMetricsInCli() throws Exception
        {
        File sample = new File("samples/example2.agn");

        String clust = run("analyse", sample.getAbsolutePath(), "--metric", "clustering");
        assertTrue(clust.contains("Watts-Strogatz Local Clustering"), clust);

        String trans = run("analyse", sample.getAbsolutePath(), "--metric", "transitivity");
        assertTrue(trans.contains("Global Transitivity"), trans);

        String kcore = run("analyse", sample.getAbsolutePath(), "--metric", "kcore");
        assertTrue(kcore.contains("k-Core Decomposition"), kcore);

        String triads = run("analyse", sample.getAbsolutePath(), "--metric", "triads");
        assertTrue(triads.contains("Triad & Dyad Census"), triads);

        String holes = run("analyse", sample.getAbsolutePath(), "--metric", "holes");
        assertTrue(holes.contains("Structural Holes"), holes);

        String comp = run("analyse", sample.getAbsolutePath(), "--metric", "components");
        assertTrue(comp.contains("Connectivity"), comp);

        String ev = run("analyse", sample.getAbsolutePath(), "--metric", "eigenvector");
        assertTrue(ev.contains("Eigenvector Centrality"), ev);

        String bon = run("analyse", sample.getAbsolutePath(), "--metric", "bonacich");
        assertTrue(bon.contains("Bonacich Power Centrality"), bon);

        String pr = run("analyse", sample.getAbsolutePath(), "--metric", "pagerank");
        assertTrue(pr.contains("PageRank Centrality"), pr);

        String assort = run("analyse", sample.getAbsolutePath(), "--metric", "assortativity");
        assertTrue(assort.contains("Degree Assortativity"), assort);

        String comm = run("analyse", sample.getAbsolutePath(), "--metric", "community");
        assertTrue(comm.contains("Community Detection"), comm);

        String eq = run("analyse", sample.getAbsolutePath(), "--metric", "equivalence");
        assertTrue(eq.contains("Structural Equivalence"), eq);

        String w = run("analyse", sample.getAbsolutePath(), "--metric", "weighted");
        assertTrue(w.contains("Valued & Weighted Network Analysis"), w);
        }

    @Test
    public void testDlFormatConversionAndRoundTripInCli() throws Exception
        {
        File sample = new File("samples/example2.agn");
        File tempDl = File.createTempFile("agna_test_export", ".dl");
        tempDl.deleteOnExit();

        run("convert", sample.getAbsolutePath(), tempDl.getAbsolutePath());
        assertTrue(tempDl.exists() && tempDl.length() > 0, "DL file was written");

        String dlContent = new String(Files.readAllBytes(tempDl.toPath()), StandardCharsets.UTF_8);
        assertTrue(dlContent.contains("format=fullmatrix"), "DL header present");

        File tempCsv = File.createTempFile("agna_test_back", ".csv");
        tempCsv.deleteOnExit();
        run("convert", tempDl.getAbsolutePath(), tempCsv.getAbsolutePath());
        assertTrue(tempCsv.exists() && tempCsv.length() > 0, "CSV converted from DL was written");
        }

    @Test
    public void testFreemanAndTransformsInCli() throws Exception
        {
        File sample = new File("samples/example2.agn");

        // 1. Freeman centralization analysis
        String freeman = run("analyse", sample.getAbsolutePath(), "--metric", "freeman");
        assertTrue(freeman.contains("Freeman Network Centralization Indices"), freeman);
        assertTrue(freeman.contains("Freeman Degree Centralization (C_D)"), freeman);

        // 2. Transform zero-diagonal
        File tempZero = File.createTempFile("agna_test_zero", ".agn");
        tempZero.deleteOnExit();
        run("transform", sample.getAbsolutePath(), tempZero.getAbsolutePath(), "--op", "zero-diagonal");
        assertTrue(tempZero.exists() && tempZero.length() > 0);

        // 3. Transform dichotomize
        File tempDichot = File.createTempFile("agna_test_dichot", ".agn");
        tempDichot.deleteOnExit();
        run("transform", sample.getAbsolutePath(), tempDichot.getAbsolutePath(), "--op", "dichotomize:1.0");
        assertTrue(tempDichot.exists() && tempDichot.length() > 0);

        // 4. Transform ego network extraction
        File tempEgo = File.createTempFile("agna_test_ego", ".agn");
        tempEgo.deleteOnExit();
        run("transform", sample.getAbsolutePath(), tempEgo.getAbsolutePath(), "--op", "ego:1:1");
        assertTrue(tempEgo.exists() && tempEgo.length() > 0);

        // 5. Randomize command
        File tempRand = File.createTempFile("agna_test_rand", ".agn");
        tempRand.deleteOnExit();
        String outRand = run("randomize", sample.getAbsolutePath(), tempRand.getAbsolutePath(), "--model", "gnp", "--prob", "0.4", "--seed", "123");
        assertTrue(outRand.contains("randomized ->"), outRand);
        assertTrue(tempRand.exists() && tempRand.length() > 0);

        // 6. Transform randomize op
        File tempTransRand = File.createTempFile("agna_test_trans_rand", ".agn");
        tempTransRand.deleteOnExit();
        String outTransRand = run("transform", sample.getAbsolutePath(), tempTransRand.getAbsolutePath(), "--op", "randomize:ws:4:0.2");
        assertTrue(outTransRand.contains("transformed ->"), outTransRand);
        assertTrue(tempTransRand.exists() && tempTransRand.length() > 0);
        }

    @Test
    public void infoSupportsJsonOutput() throws Exception
        {
        String text = run("info", "samples/example2.agn", "--json").replace("\" : ", "\": ");
        assertTrue(text.contains("\"name\""), text);
        assertTrue(text.contains("\"nodes\": 9"), text);
        assertTrue(text.contains("\"edges\": 20"), text);
        assertTrue(text.contains("\"density\":"), text);
        assertTrue(text.contains("\"connected\": true"), text);
        }

    @Test
    public void validateCleanNetworkReturnsPass() throws Exception
        {
        String text = run("validate", "samples/example2.agn");
        assertTrue(text.contains("Agna Network Validation: PASSED"), text);
        assertTrue(text.contains("Nodes:       9"), text);
        assertTrue(text.contains("Edges:       20"), text);
        assertTrue(text.contains("Clean structure"), text);
        }

    @Test
    public void validateSupportsJsonOutput() throws Exception
        {
        String text = run("validate", "samples/example2.agn", "--json");
        assertTrue(text.contains("\"valid\" : true") || text.contains("\"valid\": true"), text);
        assertTrue(text.contains("\"nodes\" : 9") || text.contains("\"nodes\": 9"), text);
        assertTrue(text.contains("\"edges\" : 20") || text.contains("\"edges\": 20"), text);
        assertTrue(text.contains("\"connected\" : true") || text.contains("\"connected\": true"), text);
        assertTrue(text.contains("\"density\""), text);
        }

    @Test
    public void validateMissingFileReturnsErrorCode() throws Exception
        {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int code = new Cli(new java.io.PrintStream(buf)).run(new String[] { "validate", "nonexistent_file_xyz.agn" });
        assertEquals(2, code, "Missing file should return code 2");
        String text = buf.toString(StandardCharsets.UTF_8.name());
        assertTrue(text.contains("FAILED"), text);
        assertTrue(text.contains("does not exist"), text);
        }

    @Test
    public void explainListsMetrics() throws Exception
        {
        String text = run("explain", "--list");
        assertTrue(text.contains("betweenness"), text);
        assertTrue(text.contains("closeness"), text);
        assertTrue(text.contains("freeman_centralization"), text);
        }

    @Test
    public void explainSpecificMetricReturnsGuide() throws Exception
        {
        String text = run("explain", "betweenness");
        assertTrue(text.contains("Betweenness Centrality"), text);
        assertTrue(text.contains("What it measures:"), text);
        assertTrue(text.contains("Brandes"), text);
        }

    @Test
    public void explainSupportsJsonOutput() throws Exception
        {
        String text = run("explain", "betweenness", "--json");
        assertTrue(text.contains("\"id\": \"betweenness\""), text);
        assertTrue(text.contains("\"whatItMeasures\":"), text);
        assertTrue(text.contains("\"whyUseIt\":"), text);
        assertTrue(text.contains("\"interpretation\":"), text);
        }

    @Test
    public void metricsSupportsTopNAndSummaryStatistics() throws Exception
        {
        String text = run("metrics", "samples/example2.agn", "betweenness", "--top", "3", "--json");
        assertTrue(text.contains("\"topN\" : 3") || text.contains("\"topN\": 3"), text);
        assertTrue(text.contains("\"summary\""), text);
        assertTrue(text.contains("\"mean\""), text);
        assertTrue(text.contains("\"stdDev\""), text);
        assertTrue(text.contains("\"min\""), text);
        assertTrue(text.contains("\"max\""), text);
        }

    @Test
    public void adviseRecommendsAnalysesAndLayouts() throws Exception
        {
        String text = run("advise", "samples/example2.agn", "--json");
        assertTrue(text.contains("\"recommendedAnalyses\""), text);
        assertTrue(text.contains("\"recommendedLayouts\""), text);
        assertTrue(text.contains("\"recommendedNullModels\""), text);
        }

    @Test
    public void narrativeProducesMarkdownAndJson() throws Exception
        {
        String md = run("narrative", "samples/example2.agn");
        assertTrue(md.contains("# Structural Narrative Summary"), md);
        assertTrue(md.contains("## 1. Scale & Density"), md);

        String json = run("narrative", "samples/example2.agn", "--json");
        assertTrue(json.contains("\"scaleAndDensity\""), json);
        assertTrue(json.contains("\"centralization\""), json);
        }

    @Test
    public void capabilitiesListsSupportedFeatures() throws Exception
        {
        String json = run("capabilities", "--json");
        assertTrue(json.contains("\"formats\""), json);
        assertTrue(json.contains("\"commands\""), json);
        assertTrue(json.contains("\"layouts\""), json);
        assertTrue(json.contains("\"nullModels\""), json);
        }

    @Test
    public void mcpProtocolEndToEnd() throws Exception
        {
        String requests =
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2024-11-05\"}}\n"
                + "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}\n"
                + "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"ping\"}\n"
                + "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/list\"}\n"
                + "{\"jsonrpc\":\"2.0\",\"id\":4,\"method\":\"tools/call\",\"params\":{\"name\":\"agna_validate\",\"arguments\":{\"file\":\"samples/example2.agn\"}}}\n"
                + "{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"tools/call\",\"params\":{\"name\":\"agna_metrics\",\"arguments\":{\"file\":\"samples/example2.agn\",\"metric\":\"betweenness\",\"top\":3}}}\n"
                + "{\"jsonrpc\":\"2.0\",\"id\":6,\"method\":\"tools/call\",\"params\":{\"name\":\"agna_advise\",\"arguments\":{\"file\":\"samples/example2.agn\"}}}\n";

        java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(requests.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.ByteArrayOutputStream err = new java.io.ByteArrayOutputStream();

        com.bentza.sna.cli.mcp.McpServer server = new com.bentza.sna.cli.mcp.McpServer(
                in,
                new java.io.PrintStream(out, true, java.nio.charset.StandardCharsets.UTF_8),
                new java.io.PrintStream(err, true, java.nio.charset.StandardCharsets.UTF_8));
        int exit = server.run();
        assertEquals(0, exit);

        String output = out.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(output.contains("\"protocolVersion\":\"2024-11-05\""), output);
        assertTrue(output.contains("\"name\":\"agna\""), output);
        assertTrue(output.contains("\"tools\":["), output);
        assertTrue(output.contains("\"name\":\"agna_validate\""), output);
        assertTrue(output.contains("\"name\":\"agna_metrics\""), output);
        assertTrue(output.contains("valid") && output.contains("true"), output);
        assertTrue(output.contains("topN") && output.contains("3"), output);
        assertTrue(output.contains("recommendedAnalyses"), output);
        }
    }