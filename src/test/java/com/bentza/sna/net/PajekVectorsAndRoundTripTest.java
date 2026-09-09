package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.bentza.sna.io.PajekExporter;

/**
 * Pajek upgrade round (2.1.3): vector export blocks, header/size export,
 * Pajek-to-Agna face mapping, coordinate import, and adjacency-list formats.
 */
public class PajekVectorsAndRoundTripTest
    {
    @Test
    public void vectorBlocksAreEmittedForConnectedNetworks()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);
        String vectors = new AgnaLib().getPajekVectors(net);
        assertTrue(vectors.contains("*Vector 3"));
        assertTrue(vectors.contains("% Betweenness"));
        assertTrue(vectors.contains("% Emission Degree"));
        }

    @Test
    public void exportIncludesHeaderAndVertexSize()
        {
        FullNet fn = new FullNet();
        fn.createDefaultNetwork(3);
        fn.attachArea();
        String out = new PajekExporter().getPajekNetwork(fn);
        // Gephi compatibility: the export must start directly with *Vertices
        // (no '%' comment header), yet still carry the vertex size
        assertTrue(out.startsWith("*Vertices 3"));
        assertTrue(out.contains("size "), "vertex size expected: " + out);
        }

    @Test
    public void importedFacesAndCoordinatesApply()
        {
        // Green + box + explicit coordinates
        String pajek = "*Vertices 2\n"
                + "1 \"A\" 0.2 0.3 ic Green shape box\n"
                + "2 \"B\" 0.8 0.9\n"
                + "*Arcs\n"
                + "1 2\n";
        FullNet fn = new FullNet();
        fn.readNetwork(pajek, "net");

        Actor a = fn.getNetwork().getActor(0);
        assertTrue(a.getFaceSource().indexOf("Green Square") >= 0,
                "face should map to bundled Green Square: " + a.getFaceSource());
        // coordinates are stored on the actor; the viewer's default layout
        // (attachArea) deliberately overrides them
        int x = a.getX(400);
        assertTrue(x > 0 && x < 320, "coordinate should be applied: " + x);
        }

    @Test
    public void arcslistFormatImports()
        {
        String pajek = "*Vertices 3\n"
                + "1 \"A\"\n"
                + "2 \"B\"\n"
                + "3 \"C\"\n"
                + "*Arcslist\n"
                + "1 2 2 3\n" // vertex 1 has 2 successors: 2 and 3
                + "2 1 3\n";
        FullNet fn = new FullNet();
        fn.readNetwork(pajek, "net");

        assertEquals(1.0f, fn.getNetwork().getValue(0, 1));
        assertEquals(1.0f, fn.getNetwork().getValue(0, 2));
        assertEquals(1.0f, fn.getNetwork().getValue(1, 2));
        }
    }