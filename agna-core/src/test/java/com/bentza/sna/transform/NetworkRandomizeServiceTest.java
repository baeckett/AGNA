/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.transform;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import com.bentza.sna.net.NetworkGenerator;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NetworkRandomizeServiceTest {

    private Network net;
    private AgnaLib lib;

    @BeforeEach
    public void setUp() {
        net = new Network(10);
        for (int i = 0; i < 10; i++) {
            net.getActor(i).setName("Node_" + (i + 1));
            net.getActor(i).setX(10 + i * 20);
            net.getActor(i).setY(10 + i * 15);
        }
        lib = new AgnaLib();
    }

    private void assertPreservedMetadata(Network target) {
        assertEquals(10, target.getSize());
        for (int i = 0; i < 10; i++) {
            assertEquals("Node_" + (i + 1), target.getActor(i).getName());
            assertEquals(10 + i * 20, target.getActor(i).getX());
            assertEquals(10 + i * 15, target.getActor(i).getY());
            assertEquals(0.0f, target.getValue(i, i), "Diagonal self-loop must be 0.0");
        }
    }

    @Test
    public void testErdosRenyiGnpDirected() {
        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.ERDOS_RENYI_GNP);
        opts.setProbability(0.50);
        opts.setSymmetric(false);

        NetworkRandomizeService.populate(net, opts, new Random(42));
        assertPreservedMetadata(net);

        int edgeCount = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (i != j && net.getValue(i, j) != 0.0f) {
                    assertEquals(1.0f, net.getValue(i, j), "Binary ties must be 1.0");
                    edgeCount++;
                }
            }
        }
        assertTrue(edgeCount > 0 && edgeCount < 90, "Expected non-trivial edge count, got: " + edgeCount);
    }

    @Test
    public void testErdosRenyiGnpSymmetric() {
        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.ERDOS_RENYI_GNP);
        opts.setProbability(0.40);
        opts.setSymmetric(true);

        NetworkRandomizeService.populate(net, opts, new Random(1234));
        assertPreservedMetadata(net);
        assertTrue(net.isSymmetric(), "Network should be strictly symmetric");
    }

    @Test
    public void testErdosRenyiGnmExactTieCount() {
        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.ERDOS_RENYI_GNM);
        opts.setTieCount(15);
        opts.setSymmetric(false);

        NetworkRandomizeService.populate(net, opts, new Random(999));
        assertPreservedMetadata(net);

        int edgeCount = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (i != j && net.getValue(i, j) != 0.0f) {
                    edgeCount++;
                }
            }
        }
        assertEquals(15, edgeCount, "Directed G(n, m) must place exactly 15 ties");
    }

    @Test
    public void testErdosRenyiGnmSymmetric() {
        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.ERDOS_RENYI_GNM);
        opts.setTieCount(8);
        opts.setSymmetric(true);

        NetworkRandomizeService.populate(net, opts, new Random(777));
        assertPreservedMetadata(net);
        assertTrue(net.isSymmetric());

        int edgePairs = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                if (net.getValue(i, j) != 0.0f) {
                    edgePairs++;
                }
            }
        }
        assertEquals(8, edgePairs, "Undirected G(n, m) must place exactly 8 undirected edges");
    }

    @Test
    public void testRandomWeightedContinuous() {
        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.RANDOM_WEIGHTED);
        opts.setProbability(0.50);
        opts.setMinWeight(2.0);
        opts.setMaxWeight(5.0);
        opts.setSymmetric(true);

        NetworkRandomizeService.populate(net, opts, new Random(555));
        assertPreservedMetadata(net);
        assertTrue(net.isSymmetric());

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                float w = net.getValue(i, j);
                if (i != j && w != 0.0f) {
                    assertTrue(w >= 2.0f && w <= 5.0f, "Weight " + w + " should be in [2.0, 5.0]");
                }
            }
        }
    }

    @Test
    public void testWattsStrogatzSmallWorld() {
        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.WATTS_STROGATZ);
        opts.setWattsStrogatzK(4);
        opts.setWattsStrogatzBeta(0.20);

        NetworkRandomizeService.populate(net, opts, new Random(888));
        assertPreservedMetadata(net);
        assertTrue(net.isSymmetric(), "Watts-Strogatz is undirected");
    }

    @Test
    public void testBarabasiAlbertScaleFree() {
        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.BARABASI_ALBERT);
        opts.setBarabasiAlbertM(2);

        NetworkRandomizeService.populate(net, opts, new Random(111));
        assertPreservedMetadata(net);
        assertTrue(net.isSymmetric(), "Barabasi-Albert is undirected");
    }

    @Test
    public void testRandomConnectedNoIsolates() {
        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.RANDOM_CONNECTED);
        opts.setExtraEdgeProb(0.20);

        NetworkRandomizeService.populate(net, opts, new Random(222));
        assertPreservedMetadata(net);
        assertTrue(lib.isConnected(net), "Network must be connected with 0 isolates");
    }

    @Test
    public void testDegreePreservingPermutation() {
        // First populate with a known structure
        for (int i = 0; i < 9; i++) {
            net.setValue(1.0f, i, i + 1);
            net.setValue(1.0f, i + 1, i);
        }
        net.setValue(1.0f, 0, 9);
        net.setValue(1.0f, 9, 0);

        int[] origDegrees = new int[10];
        for (int i = 0; i < 10; i++) {
            origDegrees[i] = (int) lib.degree(net, i);
        }

        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.DEGREE_PRESERVING);
        opts.setSwapsPerEdge(10);

        NetworkRandomizeService.populate(net, opts, new Random(333));
        assertPreservedMetadata(net);

        for (int i = 0; i < 10; i++) {
            assertEquals(origDegrees[i], (int) lib.degree(net, i), "Degree of node " + i + " must be preserved");
        }
    }

    @Test
    public void testWeightPermutation() {
        // Assign distinct weights to several edges
        net.setValue(1.5f, 0, 1);
        net.setValue(2.5f, 1, 2);
        net.setValue(3.5f, 2, 3);
        net.setValue(4.5f, 3, 4);

        RandomizeOptions opts = new RandomizeOptions();
        opts.setModel(RandomizeOptions.Model.WEIGHT_PERMUTATION);

        NetworkRandomizeService.populate(net, opts, new Random(444));
        assertPreservedMetadata(net);

        // Binary topology should be unchanged (same 4 edges)
        assertTrue(net.getValue(0, 1) > 0.0f);
        assertTrue(net.getValue(1, 2) > 0.0f);
        assertTrue(net.getValue(2, 3) > 0.0f);
        assertTrue(net.getValue(3, 4) > 0.0f);

        // Sum of weights must be preserved
        float totalWeight = net.getValue(0, 1) + net.getValue(1, 2) + net.getValue(2, 3) + net.getValue(3, 4);
        assertEquals(12.0f, totalWeight, 0.001f, "Total weight sum must remain constant");
    }
}
