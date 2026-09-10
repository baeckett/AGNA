package com.bentza.sna.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

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
        String text = buf.toString(StandardCharsets.ISO_8859_1.name());
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
        for (String layout : new String[] { "grid", "concentric", "spring" })
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
        assertTrue(run("version").contains("Agna CLI 2.1.3"));
        assertTrue(run("--version").contains("Agna CLI 2.1.3"));
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
        run("generate", "--nodes", "6", "--type", "circular", "--out",
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
        run("generate", "--nodes", "6", "--type", "circular", "--out",
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
    }