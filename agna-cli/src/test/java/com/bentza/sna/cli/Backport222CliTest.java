/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 2.2.2 audit regression battery for the command-line contract (findings
 * C1-C8 in docs/audit/AUDIT_2.2.5.md).
 */
public class Backport222CliTest
    {
    // 2.2.2 backport: tests ported from the 2.2.2 audit suite

    private static final class Result
        {
        int code;
        String out;
        }


    private static Result exec(String... args) throws Exception
        {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        Result r = new Result();
        r.code = new Cli(new PrintStream(buf, true, "UTF-8")).run(args);
        r.out = buf.toString(StandardCharsets.UTF_8.name());
        return r;
        }


    private static File temp(String name, String content) throws Exception
        {
        File f = File.createTempFile("agna_audit_", name);
        f.deleteOnExit();
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return f;
        }


    private static File disconnectedCsv() throws Exception
        {
        // path A-B-C-D-E plus dyad F-G
        String[] n = { "A", "B", "C", "D", "E", "F", "G" };
        int[][] m = new int[7][7];
        int[][] e = { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 4 }, { 5, 6 } };
        for (int[] p : e)
            {
            m[p[0]][p[1]] = 1;
            m[p[1]][p[0]] = 1;
            }
        StringBuilder sb = new StringBuilder();
        for (String s : n)
            sb.append(',').append(s);
        sb.append('\n');
        for (int i = 0; i < 7; i++)
            {
            sb.append(n[i]);
            for (int j = 0; j < 7; j++)
                sb.append(',').append(m[i][j]);
            sb.append('\n');
            }
        return temp(".csv", sb.toString());
        }


    // [C1] 'agna matrix' prints the matrix in the original order
    @Test
    public void matrixPrintsOriginalOrderWithAlignedHeader() throws Exception
        {
        File f = temp(".csv", ",Zed,Amy,Bob\nZed,0,1,0\nAmy,1,0,1\nBob,0,1,0\n");
        Result r = exec("matrix", f.getAbsolutePath(), "--format", "csv");
        assertEquals(0, r.code);
        String[] lines = r.out.split("\n");
        assertEquals(",Zed,Amy,Bob", lines[0], "header starts with an empty corner cell");
        assertTrue(lines[1].startsWith("Zed,"), "rows keep the original actor order");
        assertTrue(lines[3].startsWith("Bob,"));
        }


    // [C2] generate: positional type, unknown types and options rejected
    @Test
    public void generateAcceptsPositionalTypeAndRejectsUnknownOnes() throws Exception
        {
        File out = File.createTempFile("agna_audit_gen", ".agn");
        out.deleteOnExit();
        Result r = exec("generate", "star", "--nodes", "8", "--out", out.getAbsolutePath());
        assertEquals(0, r.code);
        assertTrue(r.out.contains("generated star network"), r.out);
        assertEquals(1, exec("generate", "--type", "nonsense", "--nodes", "8", "--out",
                out.getAbsolutePath()).code);
        assertEquals(1, exec("generate", "--type", "star", "--bogus", "--out", out.getAbsolutePath()).code);
        }


    // [C3] validate reports corrupt input; other commands refuse it
    @Test
    public void validateFlagsCorruptFilesAsInvalid() throws Exception
        {
        File f = temp(".csv", ",a,b,c\na,0,1,1\nb,1,0,1\n");
        Result r = exec("validate", f.getAbsolutePath(), "--json");
        assertEquals(2, r.code);
        JsonNode js = new ObjectMapper().readTree(r.out);
        assertFalse(js.get("valid").asBoolean());
        assertEquals("NOT_SQUARE", js.get("integrity").get(0).get("code").asText());
        }


    @Test
    public void analysisCommandsRefuseCorruptFilesUnlessLenient() throws Exception
        {
        File f = temp(".csv", ",a,b\na,0,yes\nb,no,0\n");
        Cli.InputIntegrityException ex = assertThrows(Cli.InputIntegrityException.class,
                () -> exec("info", f.getAbsolutePath()));
        assertEquals(2, Main.exitCodeFor(ex));
        assertEquals(0, exec("info", f.getAbsolutePath(), "--lenient").code);
        }


    @Test
    public void headerlessAgnFileIsRejected() throws Exception
        {
        File f = temp(".agn", "0 1\n1 0\n");
        assertThrows(Cli.InputIntegrityException.class, () -> exec("info", f.getAbsolutePath()));
        }


    @Test
    public void bundledSamplesAreAllValid() throws Exception
        {
        for (String s : new String[] { "example1.agn", "example2.agn", "example3.agn", "example4.agn",
                "Zachary Karate Club.agn", "Padgett Florentine Families.agn", "Davis Southern Women.agn",
                "random200.agn" })
            {
            Result r = exec("validate", "samples/" + s, "--json");
            assertEquals(0, r.code, s + " -> " + r.out);
            }
        }


    @Test
    public void missingFileIsADataError() throws Exception
        {
        java.io.FileNotFoundException ex = assertThrows(java.io.FileNotFoundException.class,
                () -> exec("info", "no/such/file.agn"));
        assertEquals(2, Main.exitCodeFor(ex));
        }


    // [C7] plain-text reports carry values
    @Test
    public void plainTextReportsContainValues() throws Exception
        {
        Result r = exec("analyse", "samples/example2.agn", "bonacich", "pagerank", "eigenvector", "kcore",
                "clustering", "triads", "holes");
        assertEquals(0, r.code);
        assertTrue(r.out.contains("3.9809"), "Bonacich values are printed: " + r.out);
        assertTrue(r.out.contains("Coreness"), r.out);
        }


    // [A0] 'agna metrics' distances: true BFS (2.2.5 used a stack)
    @Test
    public void metricsDistancesAgreeWithTheCoreEngineAndKnownValues() throws Exception
        {
        for (String sample : new String[] { "samples/Zachary Karate Club.agn",
                "samples/Padgett Florentine Families.agn", "samples/random200.agn" })
            {
            Result r = exec("metrics", sample, "--all", "--json");
            assertEquals(0, r.code);
            JsonNode js = new ObjectMapper().readTree(r.out);
            com.bentza.sna.net.Network net = new Cli(new PrintStream(new ByteArrayOutputStream()))
                    .open(sample).getNetwork();
            com.bentza.sna.net.AgnaLib lib = new com.bentza.sna.net.AgnaLib();
            float[] ecc = lib.eccentricity(net);
            float[] clo = lib.closeness(net);
            float[] bet = lib.betweenness(net);
            int n = net.getSize();
            int ie = 0, ic = 0, ib = 0;
            double diameter = -1;
            for (JsonNode m : js.get("metrics"))
                {
                String k = m.get("metric").asText();
                double v = m.get("value").asDouble();
                if ("diameter".equals(k))
                    diameter = v;
                else if ("eccentricity".equals(k))
                    assertEquals(ecc[ie++], v, 1e-6, sample + " eccentricity");
                else if ("closeness".equals(k))
                    assertEquals(clo[ic++] * (n - 1), v, 1e-4, sample + " closeness");
                else if ("betweenness".equals(k))
                    assertEquals(bet[ib++], v, Math.max(1e-3, 1e-5 * Math.abs(v)), sample + " betweenness");
                }
            assertEquals(n, ic);
            assertEquals(n, ib);
            if (sample.contains("Zachary") || sample.contains("Padgett"))
                assertEquals(5.0, diameter, 0.0, sample + " diameter");
            if (sample.contains("random200"))
                assertEquals(4.0, diameter, 0.0, sample + " diameter");
            }
        }


    @Test
    public void distanceCommandReturnsAShortestPath() throws Exception
        {
        // ring of 8: 0 -> 2 is two hops, the long way round is six
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++)
            sb.append(",n").append(i);
        sb.append('\n');
        for (int i = 0; i < 8; i++)
            {
            sb.append("n").append(i);
            for (int j = 0; j < 8; j++)
                sb.append(',').append((Math.abs(i - j) == 1 || Math.abs(i - j) == 7) ? 1 : 0);
            sb.append('\n');
            }
        File f = temp(".csv", sb.toString());
        Result r = exec("distance", f.getAbsolutePath(), "--from", "n0", "--to", "n2");
        assertEquals(0, r.code);
        assertTrue(r.out.contains("2"), r.out);
        assertFalse(r.out.contains("n7"), "path must not go the long way round: " + r.out);
        }
    
    // 2.2.2: a network file without viewer settings has grid space 0; the grid loop never advanced and `draw` hung forever
    @Test
    public void drawFinishesForNetworksWithoutViewerSettings() throws Exception
        {
        String agn = "Agna Data File\nVersion\t2.2.5\nNetwork Name\tNew Network\nNetwork Size\t4\nDirected\tyes\nHas Viewer\tno\n"
                + "Node Names\nA\tB\tC\tD\t\nEnd Node Names\nNetwork Matrix\n0.0\t1.0\t0.0\t0.0\n1.0\t0.0\t0.0\t0.0\n"
                + "0.0\t0.0\t0.0\t1.0\n0.0\t0.0\t1.0\t0.0\nEnd Network Matrix\n";
        File f = temp(".agn", agn);
        File png = File.createTempFile("agna_draw_", ".png");
        png.deleteOnExit();
        Result r = org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(60),
                () -> exec("draw", f.getAbsolutePath(), "--out", png.getAbsolutePath(), "--layout", "spring"));
        assertEquals(0, r.code, r.out);
        assertTrue(png.length() > 1000, "a non-empty image is written");
        }
    }
