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
    }