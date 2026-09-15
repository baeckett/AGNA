/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: Network.merge joins two sociomatrices by actor name — shared
 * actors occupy one position with ties combined by policy (sum, max,
 * keep-first), new actors are appended, and the diagonal stays zero.
 */
public class NetworkMergeTest
    {
    private static Network named(float[][] mat, String... names)
        {
        Network net = new Network(mat);
        for (int i = 0; i < names.length; i++)
            {
            net.getActor(i).setName(names[i]);
            }
        return net;
        }

    @Test
    public void mergeAppendsDisjointActors()
        {
        float[][] a = { { 0f, 1f }, { 0f, 0f } }; // A -> B
        float[][] b = { { 0f, 1f }, { 0f, 0f } }; // B -> C
        Network merged = named(a, "A", "B").merge(named(b, "B", "C"),
                Network.MERGE_SUM);
        assertEquals(3, merged.getSize());
        assertEquals("A", merged.getActorName(0));
        assertEquals("B", merged.getActorName(1));
        assertEquals("C", merged.getActorName(2));
        assertEquals(1f, merged.getValue(0, 1), 1e-6f); // A -> B
        assertEquals(1f, merged.getValue(1, 2), 1e-6f); // B -> C
        assertEquals(0f, merged.getValue(1, 0), 1e-6f); // B -> A absent
        assertEquals(0f, merged.getValue(2, 0), 1e-6f); // C -> A absent
        }

    @Test
    public void mergeSumCombinesOverlappingTies()
        {
        float[][] a = { { 0f, 1f }, { 0f, 0f } }; // A -> B = 1
        float[][] b = { { 0f, 2f }, { 0f, 0f } }; // A -> B = 2
        Network merged = named(a, "A", "B").merge(named(b, "A", "B"),
                Network.MERGE_SUM);
        assertEquals(2, merged.getSize());
        assertEquals(3f, merged.getValue(0, 1), 1e-6f);
        assertEquals(0f, merged.getValue(1, 0), 1e-6f);
        }

    @Test
    public void mergeMaxAndKeepFirstPolicies()
        {
        float[][] a = { { 0f, 1f }, { 0f, 0f } };
        float[][] b = { { 0f, 2f }, { 0f, 0f } };
        Network by_max = named(a, "A", "B").merge(named(b, "A", "B"),
                Network.MERGE_MAX);
        assertEquals(2f, by_max.getValue(0, 1), 1e-6f);
        Network by_keep = named(a, "A", "B").merge(named(b, "A", "B"),
                Network.MERGE_KEEP_FIRST);
        assertEquals(1f, by_keep.getValue(0, 1), 1e-6f);
        }

    @Test
    public void mergeKeepsDiagonalAndNetworkName()
        {
        float[][] a = { { 0f, 1f }, { 0f, 0f } };
        float[][] b = { { 2f, 1f }, { 0f, 0f } }; // stray diagonal value 2
        Network first = named(a, "A", "B");
        first.setName("Combined test");
        Network merged = first.merge(named(b, "A", "B"), Network.MERGE_SUM);
        assertEquals("Combined test", merged.getName());
        assertEquals(0f, merged.getValue(0, 0), 1e-6f);
        assertEquals(0f, merged.getValue(1, 1), 1e-6f);
        assertEquals(2f, merged.getValue(0, 1), 1e-6f);
        }

    @Test
    public void mergeWithItselfDoublesTiesConsistently()
        {
        float[][] a = { { 0f, 1f }, { 1f, 0f } };
        Network net = named(a, "A", "B");
        Network merged = net.merge(net, Network.MERGE_SUM);
        assertEquals(2, merged.getSize());
        assertEquals(2f, merged.getValue(0, 1), 1e-6f);
        assertTrue(merged.isSymmetric());
        }
    @Test
    public void mergedActorsAlwaysHaveCoordinates()
        {
        float[][] a = { { 0f, 1f }, { 0f, 0f } };
        float[][] b = { { 0f, 0f }, { 1f, 0f } };
        Network merged = named(a, "A", "B").merge(named(b, "B", "C"),
                Network.MERGE_SUM);
        for (int i = 0; i < merged.getSize(); i++)
            {
            // regression: the viewer calls getX/getY while painting edges
            assertEquals(true, merged.getActor(i).hasCoordinates());
            merged.getActor(i).getX(400);
            merged.getActor(i).getY(400);
            }
        }

    @Test
    public void mergeCarriesTheCurrentLayoutOver()
        {
        float[][] a = { { 0f, 1f }, { 0f, 0f } };
        float[][] b = { { 0f, 0f }, { 1f, 0f } };
        Network first = named(a, "A", "B");
        first.getActor(0).setX(0.3f);
        first.getActor(0).setY(0.7f);
        first.getActor(1).setX(0.6f);
        first.getActor(1).setY(0.2f);
        Network merged = first.merge(named(b, "B", "C"), Network.MERGE_SUM);
        assertEquals(first.getActor(0).getX(400), merged.getActor(0).getX(400));
        assertEquals(first.getActor(0).getY(400), merged.getActor(0).getY(400));
        assertEquals(first.getActor(1).getX(400), merged.getActor(1).getX(400));
        assertEquals(true, merged.getActor(2).hasCoordinates());
        }
    }
