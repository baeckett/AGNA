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
 * 2.1.3: GraphSON (JSON) must round-trip - vertices in order with names,
 * weighted edges, no self-loops, unicode names.
 */
public class GraphSONRoundTripTest
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

    private static FullNet importGraphSON(String json)
        {
        FullNet full = new FullNet();
        full.readNetwork(json, "graphson");
        return full;
        }

    @Test
    public void exportsGraphSONDocument()
        {
        String json = new GraphSONExporter().getGraphSON(named(
                new float[][] { { 0f, 1f }, { 0f, 0f } }, "A", "B"));
        assertTrue(json != null && json.contains("\"vertices\""));
        assertTrue(json.contains("\"graph\""));
        }

    @Test
    public void roundTripPreservesNamesValuesAndDiagonal()
        {
        Network net = named(new float[][] { { 0f, 1f, 2f },
                { 3f, 0f, 4f }, { 0f, 0f, 0f } }, "A", "B", "C");
        FullNet back = importGraphSON(new GraphSONExporter().getGraphSON(net));
        Network out = back.getNetwork();
        assertEquals(3, out.getSize());
        assertEquals("A", out.getActorName(0));
        assertEquals("B", out.getActorName(1));
        assertEquals("C", out.getActorName(2));
        assertEquals(1f, out.getValue(0, 1), 1e-6f);
        assertEquals(2f, out.getValue(0, 2), 1e-6f);
        assertEquals(4f, out.getValue(1, 2), 1e-6f);
        assertEquals(0f, out.getValue(2, 1), 1e-6f);
        assertEquals(0f, out.getValue(0, 0), 1e-6f);
        }

    @Test
    public void unicodeNamesRoundTrip()
        {
        Network net = named(new float[][] { { 0f, 1f }, { 0f, 0f } },
                "Jöhn", "Åland");
        FullNet back = importGraphSON(
                new GraphSONExporter().getGraphSON(net));
        assertEquals("Jöhn", back.getNetwork().getActorName(0));
        assertEquals("Åland", back.getNetwork().getActorName(1));
        }

    @Test
    public void bareDocumentAndDefaultWeight()
        {
        String json = "{ \"vertices\": [ {\"id\": 0, \"label\": \"A\"},"
                + " {\"id\": 1, \"label\": \"B\"} ],"
                + " \"edges\": [ {\"source\": 0, \"target\": 1} ] }";
        FullNet back = importGraphSON(json);
        assertEquals(2, back.getNetwork().getSize());
        assertEquals(1f, back.getNetwork().getValue(0, 1), 1e-6f);
        assertEquals(0f, back.getNetwork().getValue(0, 0), 1e-6f);
        }
    }
