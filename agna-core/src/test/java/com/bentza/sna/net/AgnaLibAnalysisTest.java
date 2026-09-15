/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Spot checks of the core analysis algorithms (AgnaLib) on small, fully
 * known networks. These guard the behavioural contract of the engine.
 */
public class AgnaLibAnalysisTest
    {
    @Test
    public void densityOfCompleteDirectedGraphIsOne()
        {
        // directed 4-node network with all 12 off-diagonal edges present:
        // density = 12 / (4 * 3) = 1.0
        Network net = new Network(4);
        for (int i = 0; i < 4; i++)
            {
            for (int j = 0; j < 4; j++)
                {
                if (i != j)
                    {
                    net.setValue(1f, i, j);
                    }
                }
            }

        AgnaLib lib = new AgnaLib();
        String out = lib.outDensity(net);
        assertTrue(out.contains("Density = 1.0"), "unexpected density output: " + out);
        }

    @Test
    public void densityOfEmptyGraphIsZero()
        {
        Network net = new Network(4);
        AgnaLib lib = new AgnaLib();
        String out = lib.outDensity(net);
        assertTrue(out.contains("Density = 0.0"), "unexpected density output: " + out);
        }

    @Test
    public void cliquesFindTriangle()
        {
        // K3: three mutually connected nodes -> a single 3-node 2-clique
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 0, 2);
        net.setValue(1f, 2, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);

        AgnaLib lib = new AgnaLib();
        String out = lib.outCliques(net, 2);
        assertTrue(out.contains("2-Cliques"), "expected cliques header, got: " + out);
        assertTrue(!out.contains("No 2-cliques"), "triangle must yield a clique: " + out);
        }

    @Test
    public void isolatedNodesYieldNoClique()
        {
        Network net = new Network(3); // no edges
        AgnaLib lib = new AgnaLib();
        String out = lib.outCliques(net, 2);
        assertTrue(out.contains("No 2-cliques"), "expected no-clique message, got: " + out);
        }
    }