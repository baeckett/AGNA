/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.audit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.bentza.sna.community.CommunityDetectionService;
import com.bentza.sna.core.AppRuntime;
import com.bentza.sna.io.InputIntegrityChecker;
import com.bentza.sna.io.InputIntegrityChecker.Issue;
import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.bentza.sna.net.NetworkLayouts;
import com.bentza.sna.spectral.SpectralCentrality;

/**
 * 2.2.2 audit regression battery. Every test pins a defect found in the
 * 2.2.5 audit so that it cannot come back silently. See
 * docs/audit/AUDIT_2.2.5.md for the findings (IDs in brackets).
 */
public class Backport222RegressionTest
    {
    // 2.2.2 backport: tests ported from the 2.2.2 audit suite

    // ----------------------------------------------------------------- helpers

    private static Network undirected(int n, int[][] edges)
        {
        Network net = new Network(n);
        for (int[] e : edges)
            {
            net.setValue(1f, e[0], e[1]);
            net.setValue(1f, e[1], e[0]);
            }
        return net;
        }


    private static Network complete(int n)
        {
        Network net = new Network(n);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j)
                    net.setValue(1f, i, j);
        return net;
        }


    /** 5-path A-B-C-D-E plus a separate dyad F-G. */
    private static Network pathPlusDyad()
        {
        Network net = undirected(7, new int[][] { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 4 }, { 5, 6 } });
        net.setName("PathPlusDyad");
        return net;
        }


    /** Zachary (1977) karate club, 34 nodes / 78 edges (0-based). */
    static final int[][] KARATE = { { 0, 1 }, { 0, 2 }, { 0, 3 }, { 0, 4 }, { 0, 5 }, { 0, 6 }, { 0, 7 },
            { 0, 8 }, { 0, 10 }, { 0, 11 }, { 0, 12 }, { 0, 13 }, { 0, 17 }, { 0, 19 }, { 0, 21 }, { 0, 31 },
            { 1, 2 }, { 1, 3 }, { 1, 7 }, { 1, 13 }, { 1, 17 }, { 1, 19 }, { 1, 21 }, { 1, 30 }, { 2, 3 },
            { 2, 7 }, { 2, 8 }, { 2, 9 }, { 2, 13 }, { 2, 27 }, { 2, 28 }, { 2, 32 }, { 3, 7 }, { 3, 12 },
            { 3, 13 }, { 4, 6 }, { 4, 10 }, { 5, 6 }, { 5, 10 }, { 5, 16 }, { 6, 16 }, { 8, 30 }, { 8, 32 },
            { 8, 33 }, { 9, 33 }, { 13, 33 }, { 14, 32 }, { 14, 33 }, { 15, 32 }, { 15, 33 }, { 18, 32 },
            { 18, 33 }, { 19, 33 }, { 20, 32 }, { 20, 33 }, { 22, 32 }, { 22, 33 }, { 23, 25 }, { 23, 27 },
            { 23, 29 }, { 23, 32 }, { 23, 33 }, { 24, 25 }, { 24, 27 }, { 24, 31 }, { 25, 31 }, { 26, 29 },
            { 26, 33 }, { 27, 33 }, { 28, 31 }, { 28, 33 }, { 29, 32 }, { 29, 33 }, { 30, 32 }, { 30, 33 },
            { 31, 32 }, { 31, 33 }, { 32, 33 } }
;


    @Test
    public void closenessOnConnectedNetworkIsUnchanged()
        {
        // 4-path: farness of an end node = 1+2+3 = 6 -> closeness 1/6
        Network net = undirected(4, new int[][] { { 0, 1 }, { 1, 2 }, { 2, 3 } });
        float[] c = new AgnaLib().closeness(net);
        assertEquals(1.0 / 6.0, c[0], 1e-6);
        assertEquals(1.0 / 4.0, c[1], 1e-6);
        }


    // ------------------------------------------------- [A3] Bonacich power

    @Test
    public void bonacichRejectsBetaOutsideTheConvergenceRange()
        {
        Network k5 = complete(5); // lambda_max = 4
        assertEquals(0.25, SpectralCentrality.bonacichBetaLimit(k5), 1e-6);
        assertThrows(IllegalArgumentException.class, () -> SpectralCentrality.computeBonacich(k5, 0.25, 100));
        assertThrows(IllegalArgumentException.class, () -> SpectralCentrality.computeBonacich(k5, -0.3, 100));
        }


    @Test
    public void bonacichIsExactInsideTheRange()
        {
        Network k5 = complete(5);
        // regular graph: x = d / (1 - beta d) = 4 / (1 - 0.4)
        double[] x = SpectralCentrality.computeBonacich(k5, 0.10, 100);
        for (double v : x)
            assertEquals(4.0 / 0.6, v, 1e-9);
        // negative beta (bargaining power) on a star: residual of (I - bA)x = A1
        Network star = undirected(5, new int[][] { { 0, 1 }, { 0, 2 }, { 0, 3 }, { 0, 4 } });
        double beta = -0.3;
        double[] s = SpectralCentrality.computeBonacich(star, beta, 100);
        // centre: x0 = 4 + b*(x1+..+x4); leaves: xi = 1 + b*x0
        assertEquals(4 + beta * 4 * s[1], s[0], 1e-9);
        assertEquals(1 + beta * s[0], s[1], 1e-9);
        }


    @Test
    public void bonacichReportExplainsAnInadmissibleBeta()
        {
        Network k5 = complete(5);
        String out = new AgnaLib().outBonacich(k5, 0.5);
        assertTrue(out.contains("undefined") && out.contains("1/lambda_max"), out);
        }


    // ------------------------------------------------------- [A4] Louvain

    @Test
    public void louvainSeparatesTwoCliquesJoinedByABridge()
        {
        Network net = new Network(10);
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                if (i != j)
                    {
                    net.setValue(1f, i, j);
                    net.setValue(1f, i + 5, j + 5);
                    }
        net.setValue(1f, 4, 5);
        net.setValue(1f, 5, 4);
        CommunityDetectionService.CommunityResult r = CommunityDetectionService.detectLouvain(net);
        assertEquals(2, r.communityCount);
        }


    @Test
    public void louvainReachesReferenceModularityOnKarate()
        {
        // optimum Q = 0.4198 (Brandes et al. 2008); Blondel's Louvain
        // reaches >= 0.41 on this graph. Phase-1-only 2.2.5 got ~0.39-0.40.
        Network net = undirected(34, KARATE);
        double best = 0;
        for (long seed = 1; seed <= 5; seed++)
            best = Math.max(best, CommunityDetectionService.detectLouvain(net, seed, 1.0).modularityQ);
        assertTrue(best >= 0.41, "Louvain Q on karate = " + best);
        }


    @Test
    public void louvainAggregatesARingOfCliques()
        {
        // 8 cliques of 5 joined in a ring: the obvious partition has
        // 8 communities and Q = 1 - 1/8 - (small) ~= 0.8
        int k = 8, s = 5;
        Network net = new Network(k * s);
        for (int c = 0; c < k; c++)
            {
            for (int i = 0; i < s; i++)
                for (int j = 0; j < s; j++)
                    if (i != j)
                        net.setValue(1f, c * s + i, c * s + j);
            int a = c * s, b = ((c + 1) % k) * s + 1;
            net.setValue(1f, a, b);
            net.setValue(1f, b, a);
            }
        CommunityDetectionService.CommunityResult r = CommunityDetectionService.detectLouvain(net);
        assertTrue(r.modularityQ > 0.78, "Q = " + r.modularityQ);
        assertEquals(8, r.communityCount);
        }


    // -------------------------------------------------- [B2] input integrity

    private static boolean error(String text, String ext, String code)
        {
        List<Issue> l = InputIntegrityChecker.check(text, ext);
        return l.stream().anyMatch(i -> i.severity == InputIntegrityChecker.Severity.ERROR && i.code.equals(code));
        }


    @Test
    public void integrityCheckerFlagsCorruptInput()
        {
        assertTrue(error("", "csv", "EMPTY_FILE"));
        assertTrue(error("\u0001\u0002\u0003\u0004\u0005\u0006garbage\u0000\u0000\u0000", "agn", "BINARY_CONTENT"));
        assertTrue(error("0 1\n1 0\n", "agn", "AGN_NO_HEADER"));
        assertTrue(error(",a,b,c\na,0,1,1\nb,1,0,1\n", "csv", "NOT_SQUARE"));
        assertTrue(error(",a,b,c\na,0,1\nb,1,0,1,1,1\nc,0\n", "csv", "RAGGED_ROWS"));
        assertTrue(error(",a,b\na,0,yes\nb,no,0\n", "csv", "BAD_CELL"));
        assertTrue(error(",a,b\na,0,NaN\nb,1,0\n", "csv", "BAD_CELL"));
        assertTrue(error("*Vertices 3\n1 \"a\"\n2 \"b\"\n3 \"c\"\n*Arcs\n1 9 1\n", "net", "PAJEK_VERTEX_RANGE"));
        assertTrue(error("Agna Data File\nNetwork Size\t2\nNetwork Matrix\n0\t1\n", "agn", "AGN_TRUNCATED"));
        assertTrue(error("Agna Data File\nNetwork Size\t2\nNetwork Matrix\n0\t1\n1\nEnd Network Matrix\n", "agn",
                "AGN_MATRIX_SIZE"));
        }


    @Test
    public void integrityCheckerAcceptsCleanInput()
        {
        assertTrue(InputIntegrityChecker.check(",a,b\na,0,1\nb,1,0\n", "csv").isEmpty());
        assertTrue(InputIntegrityChecker.check("0,1\n1,0\n", "csv").isEmpty());
        assertTrue(InputIntegrityChecker.check(",a,b,\na,0,1.5,\nb,1,0,\n", "csv").isEmpty(),
                "trailing separators (Excel exports) are fine");
        assertTrue(InputIntegrityChecker.check(",a,\"b, c\"\na,0,1\n\"b, c\",1,0\n", "csv").isEmpty(),
                "RFC 4180 quoted names with commas are supported since 2.2.2");
        assertTrue(InputIntegrityChecker.check("source,target\na,b\n", "csv").isEmpty(), "edge list is not a matrix");
        assertTrue(InputIntegrityChecker.check("*Vertices 2\n1 \"a\"\n2 \"b\"\n*Edges\n1 2 1\n", "net").isEmpty());
        List<Issue> dup = InputIntegrityChecker.check(",A,A,B\nA,0,1,1\nA,1,0,1\nB,1,1,0\n", "csv");
        assertFalse(InputIntegrityChecker.hasErrors(dup));
        assertTrue(dup.stream().anyMatch(i -> i.code.equals("DUPLICATE_LABELS")));
        }


    // ------------------------------------- [P] fast algorithms = reference

    private static Network randomDigraph(int n, double p, double pRecip, long seed)
        {
        java.util.Random r = new java.util.Random(seed);
        Network net = new Network(n);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j && r.nextDouble() < p)
                    {
                    net.setValue(1f + r.nextInt(3), i, j);
                    if (r.nextDouble() < pRecip)
                        net.setValue(1f, j, i);
                    }
        return net;
        }
    }
