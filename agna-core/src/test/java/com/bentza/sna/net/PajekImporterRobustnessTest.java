package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Pajek-importer robustness (2.1.3 round): percent comments, arc lines with
 * Pajek line attributes instead of a value, and non-numeric vertex noise
 * must not break parsing.
 */
public class PajekImporterRobustnessTest
    {
    @Test
    public void percentCommentsAndAttributeOnlyArcLinesParse()
        {
        String pajek = "*Vertices 2\n"
                + "% this is a comment\n"
                + "1 \"A\" 0.1 0.2\n"
                + "\"noise line that is not a vertex\"\n"
                + "2 \"B\" 0.3 0.4\n"
                + "*Arcs\n"
                + "1 2 c Red w 3\n"   // value omitted, line attributes follow
                + "2 1\n";
        FullNet fn = new FullNet();
        fn.readNetwork(pajek, "net");

        assertEquals(2, fn.getNetwork().getSize());
        assertEquals("A", fn.getNetwork().getNodeName(0));
        assertEquals(1.0f, fn.getNetwork().getValue(0, 1)); // default value 1
        assertEquals(1.0f, fn.getNetwork().getValue(1, 0));
        }

    @Test
    public void explicitZeroValueMeansNoArc()
        {
        String pajek = "*Vertices 2\n"
                + "1 \"A\"\n"
                + "2 \"B\"\n"
                + "*Arcs\n"
                + "1 2 0.0\n";
        FullNet fn = new FullNet();
        fn.readNetwork(pajek, "net");

        assertEquals(0.0f, fn.getNetwork().getValue(0, 1));
        }
    }