/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;

/**
 * 2.1.3: GML must round-trip - nodes in order with their names, weighted
 * edges, no self-loops, quoted labels with special characters.
 */
public class GMLRoundTripTest
    {
    private static Network named(float[][] mat, String... names)
        {
        Network net = new Network(mat);
        net.setName("GML");
        for (int i = 0; i < names.length; i++)
            {
            net.getActor(i).setName(names[i]);
            }
        return net;
        }

    private static FullNet importGML(String text)
        {
        FullNet full = new FullNet();
        full.readNetwork(text, "gml");
        return full;
        }

    @Test
    public void exportsGraphBlockWithDirectedEdges()
        {
        String gml = new GMLExporter().getGML(named(
                new float[][] { { 0f, 1f }, { 0f, 0f } }, "A", "B"));
        assertTrue(gml != null && gml.contains("graph ["));
        assertTrue(gml.contains("directed 1"));
        assertTrue(gml.contains("label \"A\""));
        }

    @Test
    public void roundTripPreservesNamesValuesAndDiagonal()
        {
        Network net = named(new float[][] { { 0f, 1f, 2f },
                { 3f, 0f, 4f }, { 0f, 0f, 0f } }, "A", "B", "C");
        FullNet back = importGML(new GMLExporter().getGML(net));
        Network out = back.getNetwork();
        assertEquals(3, out.getSize());
        assertEquals(1f, out.getValue(0, 1), 1e-6f);
        assertEquals(2f, out.getValue(0, 2), 1e-6f);
        assertEquals(3f, out.getValue(1, 0), 1e-6f);
        assertEquals(4f, out.getValue(1, 2), 1e-6f);
        assertEquals(0f, out.getValue(2, 1), 1e-6f);
        assertEquals(0f, out.getValue(0, 0), 1e-6f);
        }

    @Test
    public void quotedLabelsWithSpecialCharactersRoundTrip()
        {
        Network net = named(new float[][] { { 0f, 1f }, { 0f, 0f } },
                "A\"B", "O'Brien");
        FullNet back = importGML(new GMLExporter().getGML(net));
        assertEquals("A\"B", back.getNetwork().getActorName(0));
        assertEquals("O'Brien", back.getNetwork().getActorName(1));
        }

    @Test
    public void missingValueDefaultsToOne()
        {
        String gml = "graph [\n"
                + "  node [ id 0 label \"A\" ]\n"
                + "  node [ id 1 label \"B\" ]\n"
                + "  edge [ source 0 target 1 ]\n"
                + "]\n";
        FullNet back = importGML(gml);
        assertEquals(2, back.getNetwork().getSize());
        assertEquals(1f, back.getNetwork().getValue(0, 1), 1e-6f);
        }
    }
