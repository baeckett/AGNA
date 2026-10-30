/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.Actor;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.bentza.sna.net.NetworkGenerator;
import com.bentza.sna.net.NodeArea;

/**
 * Tests for Agna 2.1.5 New Network topologies and their layouts in Network Viewer:
 * - Square lattice layout in NodeArea
 * - Bee-hive layout in NodeArea
 */
public class NewNetworkDialogTest
    {
    @Test
    public void squareLatticeViewerLayout()
        {
        Network square = NetworkGenerator.createSquareLattice(3); // 3x3 = 9 nodes
        assertEquals(9, square.getSize());

        FullNet fn = new FullNet();
        fn.setNetwork(square);
        fn.attachArea();

        NodeArea area = fn.getArea();
        assertNotNull(area);

        // Verify that layout in NodeArea is a grid, not circle layout
        // For 3x3:
        // row 0: (0, 1, 2)
        // row 1: (3, 4, 5)
        // row 2: (6, 7, 8)
        Actor a0 = square.getActor(0);
        Actor a1 = square.getActor(1);
        Actor a2 = square.getActor(2);
        Actor a3 = square.getActor(3);
        Actor a6 = square.getActor(6);

        // Row 0 nodes have identical Y
        assertEquals(a0.getY(), a1.getY(), 0.001f);
        assertEquals(a1.getY(), a2.getY(), 0.001f);

        // Col 0 nodes have identical X
        assertEquals(a0.getX(), a3.getX(), 0.001f);
        assertEquals(a3.getX(), a6.getX(), 0.001f);

        // X strictly increases across columns
        assertTrue(a1.getX() > a0.getX());
        assertTrue(a2.getX() > a1.getX());

        // Y strictly increases across rows
        assertTrue(a3.getY() > a0.getY());
        assertTrue(a6.getY() > a3.getY());
        }

    @Test
    public void beeHiveLatticeViewerLayout()
        {
        Network beehive = NetworkGenerator.createBeeHiveLattice(2); // 24 nodes
        assertEquals(24, beehive.getSize());

        FullNet fn = new FullNet();
        fn.setNetwork(beehive);
        fn.attachArea();

        NodeArea area = fn.getArea();
        assertNotNull(area);

        // Verify all 24 nodes have coordinates inside [10, 90]
        for (int i = 0; i < 24; i++)
            {
            Actor a = beehive.getActor(i);
            assertTrue(a.hasCoordinates());
            assertTrue(a.getX() >= 9.9f && a.getX() <= 90.1f);
            assertTrue(a.getY() >= 9.9f && a.getY() <= 90.1f);
            }

        // Verify bee-hive symmetry: centered around ~50%
        float sumX = 0f;
        float sumY = 0f;
        for (int i = 0; i < 24; i++)
            {
            sumX += beehive.getActor(i).getX();
            sumY += beehive.getActor(i).getY();
            }
        float avgX = sumX / 24f;
        float avgY = sumY / 24f;
        assertEquals(50f, avgX, 1.0f, "Bee-hive should be horizontally centered near 50%");
        assertEquals(50f, avgY, 1.0f, "Bee-hive should be vertically centered near 50%");
        }

    @Test
    public void hierarchicalTreeViewerLayout()
        {
        Network tree = NetworkGenerator.createHierarchicalTree(7, 2, false, true);
        assertEquals(7, tree.getSize());

        FullNet fn = new FullNet();
        fn.setNetwork(tree);
        fn.attachArea();

        NodeArea area = fn.getArea();
        assertNotNull(area);

        // In hierarchical layout, root is at top, children at lower tiers
        Actor root = tree.getActor(0);
        Actor child1 = tree.getActor(1);
        Actor grandChild = tree.getActor(3);
        assertTrue(root.getY() < child1.getY());
        assertTrue(child1.getY() < grandChild.getY());
        }

    @Test
    public void linearChainViewerLayout()
        {
        Network chain = NetworkGenerator.createLinearChain(5, true);
        FullNet fn = new FullNet();
        fn.setNetwork(chain);
        fn.attachArea();

        NodeArea area = fn.getArea();
        assertNotNull(area);

        for (int i = 0; i < 4; i++)
            {
            Actor cur = chain.getActor(i);
            Actor next = chain.getActor(i + 1);
            assertEquals(cur.getY(), next.getY(), 0.001f, "Single row chain nodes share Y coordinate");
            assertTrue(next.getX() > cur.getX(), "Chain nodes ordered left-to-right");
            }
        }
    }
