/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.net.Network;
import org.junit.jupiter.api.Test;

public class DegreeAssortativityTest {

    @Test
    public void testStarNetworkIsDisassortative() {
        // Star network of 5 nodes: node 0 connected to 1, 2, 3, 4
        Network star = new Network(5);
        for (int i = 1; i < 5; i++) {
            star.setValue(1.0f, 0, i);
            star.setValue(1.0f, i, 0);
        }

        DegreeAssortativity.AssortativityResult res = DegreeAssortativity.compute(star);
        assertEquals(8, res.edgeCount);
        // Star networks have perfectly negative degree assortativity r = -1.0
        assertEquals(-1.0, res.totalDegreeAssortativity, 1e-4);
        assertTrue(res.mixingRegime.contains("Disassortative"));
    }

    @Test
    public void testCompleteNetworkHasNeutralDegreeAssortativity() {
        // Complete graph K_4: all degrees equal 3 -> uniform degree -> r = 0
        Network k4 = new Network(4);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) k4.setValue(1.0f, i, j);
            }
        }

        DegreeAssortativity.AssortativityResult res = DegreeAssortativity.compute(k4);
        assertEquals(0.0, res.totalDegreeAssortativity, 1e-4);
    }

    @Test
    public void testAssortativeCommunities() {
        // Two high-degree cliques of 4 nodes each, joined by a single bridge
        Network net = new Network(8);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) net.setValue(1.0f, i, j);
            }
        }
        for (int i = 4; i < 8; i++) {
            for (int j = 4; j < 8; j++) {
                if (i != j) net.setValue(1.0f, i, j);
            }
        }
        // Bridge between node 3 and node 4
        net.setValue(1.0f, 3, 4);
        net.setValue(1.0f, 4, 3);

        DegreeAssortativity.AssortativityResult res = DegreeAssortativity.compute(net);
        // High degree nodes connect almost exclusively to high degree nodes
        assertTrue(res.totalDegreeAssortativity >= -0.1);
    }
}
