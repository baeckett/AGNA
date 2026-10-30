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
