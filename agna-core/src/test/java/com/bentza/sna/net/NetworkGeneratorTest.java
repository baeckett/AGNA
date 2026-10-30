/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.junit.jupiter.api.Test;

import com.bentza.sna.Environment;

/**
 * Unit tests for Agna 2.1.5 enhancements:
 * - Default open directory resolution
 * - NetworkGenerator topologies: empty, star (symmetric/asymmetric), circular,
 *   full binary, random connected (no outsiders), random weighted,
 *   square lattice, hexagram lattice.
 */
public class NetworkGeneratorTest
    {
    @Test
    public void defaultOpenDirectoryResolvesValidFolder()
        {
        File dir = Environment.getDefaultOpenDirectory();
        assertNotNull(dir);
        assertTrue(dir.isDirectory(), "default directory should exist and be a directory: " + dir);
        assertTrue(dir.canRead(), "default directory should be readable: " + dir);
        }

    @Test
    public void createEmptyNetwork()
        {
        Network net = NetworkGenerator.createEmpty(10);
        assertEquals(10, net.getSize());
        assertEquals(0, net.getEdgesNumber());
        assertEquals("Empty Network (10 nodes)", net.getName());
        }

    @Test
    public void createStarSymmetricAndAsymmetric()
        {
        Network starSym = NetworkGenerator.createStar(6, true);
        assertEquals(6, starSym.getSize());
        assertEquals(10, starSym.getEdgesNumber()); // 5 undirected edges = 10 directed cells
        assertTrue(starSym.isSymmetric());

        Network starAsym = NetworkGenerator.createStar(6, false);
        assertEquals(6, starAsym.getSize());
        assertEquals(5, starAsym.getEdgesNumber()); // 5 directed edges (0 -> 1..5)
        assertFalse(starAsym.isSymmetric());
        }

    @Test
    public void createCircularNetwork()
        {
        Network ring = NetworkGenerator.createCircular(8);
        assertEquals(8, ring.getSize());
        assertEquals(16, ring.getEdgesNumber()); // 8 undirected edges = 16 directed cells
        assertTrue(ring.isSymmetric());
        for (int i = 0; i < 8; i++)
            {
            assertEquals(1.0f, ring.getValue(i, (i + 1) % 8));
            assertEquals(1.0f, ring.getValue((i + 1) % 8, i));
            }
        }

    @Test
    public void createFullBinaryNetwork()
        {
        Network full = NetworkGenerator.createFullBinary(5);
        assertEquals(5, full.getSize());
        assertEquals(20, full.getEdgesNumber()); // 5 * 4 = 20 edges
        for (int i = 0; i < 5; i++)
            {
            for (int j = 0; j < 5; j++)
                {
                if (i != j)
                    {
                    assertEquals(1.0f, full.getValue(i, j));
                    }
                else
                    {
                    assertEquals(0.0f, full.getValue(i, j));
                    }
                }
            }
        }

    @Test
    public void createRandomConnectedHasNoOutsiders()
        {
        Network net = NetworkGenerator.createRandomConnected(25, 0.1, 42);
        assertEquals(25, net.getSize());
        // Verify every node has at least degree 1 (no outsiders)
        for (int i = 0; i < 25; i++)
            {
            int degree = 0;
            for (int j = 0; j < 25; j++)
                {
                if (net.getValue(i, j) > 0.0f)
                    {
                    degree++;
                    }
                }
            assertTrue(degree >= 1, "Node " + i + " must have degree >= 1");
            }
        }

    @Test
    public void createRandomWeightedValuesAreStrictlyInsideOpenInterval()
        {
        Network net = NetworkGenerator.createRandomWeighted(20, 0.5, true, 12345);
        assertEquals(20, net.getSize());
        int edgeCount = 0;
        for (int i = 0; i < 20; i++)
            {
            for (int j = 0; j < 20; j++)
                {
                float val = net.getValue(i, j);
                if (i == j)
                    {
                    assertEquals(0.0f, val);
                    }
                else if (val > 0.0f)
                    {
                    edgeCount++;
                    assertTrue(val > 0.0f && val < 1.0f, "Edge weight " + val + " must be strictly between 0 and 1");
                    assertNotEquals(0.0f, val);
                    assertNotEquals(1.0f, val);
                    }
                }
            }
        assertTrue(edgeCount > 0, "Network should contain weighted edges");
        }

    @Test
    public void createSquareLattice()
        {
        Network grid = NetworkGenerator.createSquareLattice(4); // 4x4 = 16 nodes
        assertEquals(16, grid.getSize());
        assertTrue(grid.isSymmetric());
        // In 4x4 grid: corners have deg 2 (4*2=8), edges have deg 3 (8*3=24), interior have deg 4 (4*4=16)
        // Total directed edges = 8 + 24 + 16 = 48 (or 24 undirected edges)
        assertEquals(48, grid.getEdgesNumber());

        // Verify layout coordinates preserved in NodeArea
        FullNet fn = new FullNet();
        fn.setNetwork(grid);
        fn.attachArea();
        NodeArea area = fn.getArea();
        assertNotNull(area);

        // Nodes in the same row should share same Y; nodes in same column share same X
        // Corner (0,0) top-left, corner (3,3) bottom-right
        Actor a00 = grid.getActor(0);
        Actor a01 = grid.getActor(1);
        Actor a10 = grid.getActor(4);
        Actor a33 = grid.getActor(15);
        assertEquals(a00.getY(), a01.getY(), 0.01f, "Row 0 nodes should have identical Y");
        assertEquals(a00.getX(), a10.getX(), 0.01f, "Col 0 nodes should have identical X");
        assertTrue(a01.getX() > a00.getX(), "Col 1 should be to the right of Col 0");
        assertTrue(a10.getY() > a00.getY(), "Row 1 should be below Row 0");
        assertTrue(a33.getX() > a00.getX());
        assertTrue(a33.getY() > a00.getY());
        }

    @Test
    public void createBeeHiveLattice()
        {
        // R=1: 1 hexagon, 6 nodes, 6 edges (12 directed)
        Network b1 = NetworkGenerator.createBeeHiveLattice(1);
        assertEquals(6, b1.getSize());
        assertEquals(12, b1.getEdgesNumber());
        assertTrue(b1.isSymmetric());

        // R=2: 7 hexagons, 24 nodes, 30 edges (60 directed)
        Network b2 = NetworkGenerator.createBeeHiveLattice(2);
        assertEquals(24, b2.getSize());
        assertEquals(60, b2.getEdgesNumber());
        assertTrue(b2.isSymmetric());

        // Verify degree constraints:
        // In bee-hive, interior nodes have degree 3, boundary nodes have degree 2
        for (int i = 0; i < 24; i++)
            {
            int deg = 0;
            for (int j = 0; j < 24; j++)
                {
                if (b2.getValue(i, j) > 0.0f)
                    deg++;
                }
            assertTrue(deg == 2 || deg == 3, "Bee-hive node degree must be 2 or 3, got: " + deg);
            }

        // R=3: 19 hexagons, 54 nodes, 72 edges (144 directed)
        Network b3 = NetworkGenerator.createBeeHiveLattice(3);
        assertEquals(54, b3.getSize());
        assertEquals(144, b3.getEdgesNumber());
        assertTrue(b3.isSymmetric());

        // Verify layout coordinates preserved in NodeArea
        FullNet fn = new FullNet();
        fn.setNetwork(b2);
        fn.attachArea();
        NodeArea area = fn.getArea();
        assertNotNull(area);
        for (int i = 0; i < b2.getSize(); i++)
            {
            Actor a = b2.getActor(i);
            assertTrue(a.hasCoordinates());
            assertTrue(a.getX() >= 9.0f && a.getX() <= 91.0f);
            assertTrue(a.getY() >= 9.0f && a.getY() <= 91.0f);
            }
        }

    @Test
    public void createHexagramLattice()
        {
        Network hex = NetworkGenerator.createHexagramLattice(1);
        assertTrue(hex.getSize() > 2);
        assertTrue(hex.isSymmetric());
        assertTrue(hex.getEdgesNumber() > 0);

        Network hex2 = NetworkGenerator.createHexagramLattice(2);
        assertTrue(hex2.getSize() > hex.getSize());
        }

    @Test
    public void validationBoundsCheck()
        {
        assertThrows(IllegalArgumentException.class, () -> NetworkGenerator.createEmpty(1));
        assertThrows(IllegalArgumentException.class, () -> NetworkGenerator.createEmpty(0));
        assertThrows(IllegalArgumentException.class, () -> NetworkGenerator.createEmpty(-5));
        assertThrows(IllegalArgumentException.class, () -> NetworkGenerator.createEmpty(10000));
        }
    }
