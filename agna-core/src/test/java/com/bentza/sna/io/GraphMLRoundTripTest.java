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
 * 2.1.3: GraphML must round-trip - same nodes (names in file order),
 * same weighted edges, no self-loops, and plain XML any SNA tool (Gephi,
 * NetworkX, igraph, R) can read.
 */
public class GraphMLRoundTripTest
    {
    private static Network named(float[][] mat, String... names)
        {
        Network net = new Network(mat);
        net.setName("RT");
        for (int i = 0; i < names.length; i++)
            {
            net.getActor(i).setName(names[i]);
            }
        return net;
        }

    private static FullNet importGraphML(String xml)
        {
        FullNet full = new FullNet();
        full.readNetwork(xml, "graphml");
        return full;
        }

    @Test
    public void exportsWellFormedDirectedGraphML()
        {
        String xml = new GraphMLExporter().getGraphML(named(
                new float[][] { { 0f, 1f }, { 0f, 0f } }, "A", "B"));
        assertTrue(xml != null && xml.contains("<graphml"));
        assertTrue(xml.contains("edgedefault=\"directed\""));
        assertTrue(xml.contains("attr.name=\"name\""));
        assertTrue(xml.contains("attr.name=\"weight\""));
        }

    @Test
    public void roundTripPreservesNamesValuesAndDiagonal()
        {
        Network net = named(new float[][] { { 0f, 1f, 2f },
                { 3f, 0f, 4f }, { 0f, 0f, 0f } }, "A", "B", "C");
        String xml = new GraphMLExporter().getGraphML(net);
        FullNet back = importGraphML(xml);

        Network out = back.getNetwork();
        assertEquals(3, out.getSize());
        assertEquals("RT", out.getName());
        assertEquals("A", out.getActorName(0));
        assertEquals("B", out.getActorName(1));
        assertEquals("C", out.getActorName(2));
        assertEquals(1f, out.getValue(0, 1), 1e-6f);
        assertEquals(2f, out.getValue(0, 2), 1e-6f);
        assertEquals(3f, out.getValue(1, 0), 1e-6f);
        assertEquals(4f, out.getValue(1, 2), 1e-6f);
        assertEquals(0f, out.getValue(2, 1), 1e-6f);
        assertEquals(0f, out.getValue(0, 0), 1e-6f); // diagonal stays zero
        }

    @Test
    public void namesWithXmlSpecialCharactersRoundTrip()
        {
        Network net = named(new float[][] { { 0f, 1f }, { 0f, 0f } },
                "A&B", "O'Brien");
        String xml = new GraphMLExporter().getGraphML(net);
        assertTrue(xml.contains("A&amp;B"));
        FullNet back = importGraphML(xml);
        assertEquals("A&B", back.getNetwork().getActorName(0));
        assertEquals("O'Brien", back.getNetwork().getActorName(1));
        }

    @Test
    public void missingWeightDefaultsToOneAndSelfLoopsAreIgnored()
        {
        String xml = "<?xml version=\"1.0\"?><graphml "
                + "xmlns=\"http://graphml.graphdrawing.org/xmlns\">"
                + "<graph id=\"G\" edgedefault=\"directed\">"
                + "<node id=\"n0\"/><node id=\"n1\"/>"
                + "<edge source=\"n0\" target=\"n1\"/>"
                + "<edge source=\"n0\" target=\"n0\"/>"
                + "</graph></graphml>";
        FullNet back = importGraphML(xml);
        Network out = back.getNetwork();
        assertEquals(2, out.getSize());
        assertEquals(1f, out.getValue(0, 1), 1e-6f);
        assertEquals(0f, out.getValue(0, 0), 1e-6f);
        }
    }
