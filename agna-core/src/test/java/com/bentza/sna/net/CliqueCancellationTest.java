/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the clique enumeration must honor the cancellation flag (checked
 * inside the recursion) so a user abort returns promptly instead of hanging
 * the UI, and the pivot-based Bron-Kerbosch keeps the same results.
 */
public class CliqueCancellationTest
    {
    @Test
    public void cancelledEnumerationReturnsPromptly()
        {
        Network net = new Network(60);
        for (int i = 0; i < 60; i++)
            {
            for (int j = 0; j < 60; j++)
                {
                if (i != j && ((i + j) % 3 == 0 || i % 5 == 0))
                    {
                    net.setValue(1f, i, j);
                    }
                }
            }
        AgnaLib.clique_search_cancelled = true;
        long t0 = System.currentTimeMillis();
        String out = new AgnaLib().outCliques(net, 3);
        long t1 = System.currentTimeMillis();
        AgnaLib.clique_search_cancelled = false;
        assertTrue((t1 - t0) < 2000,
                "cancelled enumeration must return fast (took " + (t1 - t0) + " ms)");
        assertTrue(out.contains("No 3-cliques") || out.contains("3-Cliques"),
                "expected a clique report: " + out.substring(0, Math.min(40, out.length())));
        }

    @Test
    public void pivotAlgorithmStillFindsKnownCliques()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 0, 2);
        net.setValue(1f, 2, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);
        AgnaLib lib = new AgnaLib();
        String out = lib.outCliques(net, 2);
        assertTrue(out.contains("2-Cliques"));
        assertTrue(!out.contains("No 2-cliques"));
        }
    }