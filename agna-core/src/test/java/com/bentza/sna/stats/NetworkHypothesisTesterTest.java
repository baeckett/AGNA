/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class NetworkHypothesisTesterTest
    {
    private static Network loadSample() throws Exception
        {
        FullNet full = new FullNet();
        full.readNetwork(new String(Files.readAllBytes(new File(
                "samples/example2.agn").toPath()),
                StandardCharsets.ISO_8859_1), "agn");
        return full.getNetwork();
        }

    @Test
    public void degreePreservingRandomizerPreservesExactDegreeSequence() throws Exception
        {
        Network net = loadSample();
        int n = net.getSize();
        int[] originalIn = new int[n];
        int[] originalOut = new int[n];
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                if (net.getValue(i, j) != 0f)
                    {
                    originalOut[i]++;
                    originalIn[j]++;
                    }
                }
            }

        Random rng = new Random(42L);
        Network randomized = NetworkRandomizer.randomizeDegreePreserving(net, 10, rng);

        int[] randIn = new int[n];
        int[] randOut = new int[n];
        for (int i = 0; i < n; i++)
            {
            assertFalse(randomized.getValue(i, i) != 0f, "no self-loops introduced");
            for (int j = 0; j < n; j++)
                {
                if (randomized.getValue(i, j) != 0f)
                    {
                    randOut[i]++;
                    randIn[j]++;
                    }
                }
            }

        for (int i = 0; i < n; i++)
            {
            assertEquals(originalIn[i], randIn[i], "in-degree preserved for node " + i);
            assertEquals(originalOut[i], randOut[i], "out-degree preserved for node " + i);
            }
        }

    @Test
    public void erdosRenyiPreservesEdgeCount() throws Exception
        {
        Network net = loadSample();
        int n = net.getSize();
        int originalEdges = 0;
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                if (net.getValue(i, j) != 0f)
                    originalEdges++;
                }
            }

        Random rng = new Random(12345L);
        Network randomized = NetworkRandomizer.randomizeErdosRenyi(net, rng);

        int randEdges = 0;
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                if (randomized.getValue(i, j) != 0f)
                    randEdges++;
                }
            }
        assertEquals(originalEdges, randEdges, "total edge count preserved under G(n, m)");
        }

    @Test
    public void dyadCensusPreservesMutualAndAsymmetricCounts() throws Exception
        {
        Network net = loadSample();
        int n = net.getSize();
        int origMutual = 0;
        int origAsym = 0;
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                boolean ij = net.getValue(i, j) != 0f;
                boolean ji = net.getValue(j, i) != 0f;
                if (ij && ji)
                    origMutual++;
                else if (ij || ji)
                    origAsym++;
                }
            }

        Random rng = new Random(999L);
        Network randomized = NetworkRandomizer.randomizeDyadCensus(net, rng);

        int randMutual = 0;
        int randAsym = 0;
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                boolean ij = randomized.getValue(i, j) != 0f;
                boolean ji = randomized.getValue(j, i) != 0f;
                if (ij && ji)
                    randMutual++;
                else if (ij || ji)
                    randAsym++;
                }
            }
        assertEquals(origMutual, randMutual, "mutual dyads preserved");
        assertEquals(origAsym, randAsym, "asymmetric dyads preserved");
        }

    @Test
    public void hypothesisTestGeneratesValidStatisticsAndReports() throws Exception
        {
        Network net = loadSample();
        HypothesisTestResult res = NetworkHypothesisTester.runTest(net, NetworkMetric.TRANSITIVITY,
                NullModel.DEGREE_PRESERVING, 100, 20261030L);

        assertNotNull(res);
        assertTrue(res.getObservedValue() >= 0.0);
        assertTrue(res.getNullMean() >= 0.0);
        assertTrue(res.getNullStdDev() >= 0.0);
        assertTrue(res.getPValueTwoTailed() >= 0.0 && res.getPValueTwoTailed() <= 1.0);
        assertTrue(res.getPValueGreater() >= 0.0 && res.getPValueGreater() <= 1.0);
        assertTrue(res.getPValueLess() >= 0.0 && res.getPValueLess() <= 1.0);
        assertTrue(res.getCiLower95() <= res.getCiUpper95());

        String text = res.toTextReport();
        assertTrue(text.contains("AGNA STATISTICAL HYPOTHESIS TEST"));
        assertTrue(text.contains("Null Distribution Histogram"));

        String html = res.toHtmlReport();
        assertTrue(html.contains("AGNA Hypothesis Test"));
        assertTrue(html.contains("Observed Value"));

        String json = res.toJson();
        assertTrue(json.contains("\"metric\": \"TRANSITIVITY\""));
        assertTrue(json.contains("\"null_model\": \"DEGREE_PRESERVING\""));
        }
    }
