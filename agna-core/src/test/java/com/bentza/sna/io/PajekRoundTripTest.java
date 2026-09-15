/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.bentza.sna.io.PajekExporter;

/**
 * Regression tests for the Pajek export/import fixes (2.1.3):
 * - the exporter no longer accumulates style tokens per vertex and actually
 *   writes the arc section
 * - a minimal Pajek (.net) reader round-trips exported files
 */
public class PajekRoundTripTest
    {
    private static FullNet buildAreaNet()
        {
        FullNet fn = new FullNet();
        fn.createDefaultNetwork(3);
        fn.getNetwork().setValue(1f, 0, 1);
        fn.getNetwork().setValue(2.5f, 1, 2);
        fn.attachArea(); // headless-safe NodeArea
        return fn;
        }

    @Test
    public void exportEmitsSingleStyleTokenPerVertex()
        {
        FullNet fn = buildAreaNet();
        String out = new PajekExporter().getPajekNetwork(fn);
        String[] lines = out.split("\n");
        for (String line : lines)
            {
            if (line.startsWith("\"") || line.matches("\\d+ \".*\".*"))
                {
                int occurrences = 0;
                int idx = 0;
                while ((idx = line.indexOf("shape ellipse", idx)) >= 0)
                    {
                    occurrences++;
                    idx += "shape ellipse".length();
                    }
                assertEquals(1, occurrences, "vertex line must not repeat the style: " + line);
                }
            }
        assertTrue(out.contains("1 \"1\" "), "vertex header expected: " + out);
        assertFalse(out.contains("shape ellipse shape ellipse"),
                "accumulated style tokens must be gone");
        }

    @Test
    public void exportWritesArcsSection()
        {
        FullNet fn = buildAreaNet();
        String out = new PajekExporter().getPajekNetwork(fn);
        assertTrue(out.contains("*Arcs"), "arc section header expected");
        assertTrue(out.contains("1 2 1.0"), "arc 1->2 expected: " + out);
        assertTrue(out.contains("2 3 2.5"), "weighted arc 2->3 expected: " + out);
        }

    @Test
    public void exportedFileRoundTripsThroughTheImporter()
        {
        FullNet fn = buildAreaNet();
        String out = new PajekExporter().getPajekNetwork(fn);

        FullNet imported = new FullNet();
        imported.readNetwork(out, "net");

        assertEquals(3, imported.getNetwork().getSize());
        assertEquals(1.0f, imported.getNetwork().getValue(0, 1));
        assertEquals(2.5f, imported.getNetwork().getValue(1, 2));
        assertEquals("1", imported.getNetwork().getNodeName(0));
        }

    @Test
    public void importsHandWrittenPajekWithEdgesAndQuotedNames()
        {
        String pajek = "*Vertices 3\n"
                + "1 \"Alice\" 0.1 0.2\n"
                + "2 \"Bob\" 0.3 0.4\n"
                + "3 \"Carol\" 0.5 0.6\n"
                + "*Edges\n"
                + "1 2\n"
                + "2 3 2.0\n";
        FullNet fn = new FullNet();
        fn.readNetwork(pajek, "net");

        assertEquals(3, fn.getNetwork().getSize());
        assertEquals("Alice", fn.getNetwork().getNodeName(0));
        assertEquals(1.0f, fn.getNetwork().getValue(0, 1)); // edges: symmetric
        assertEquals(1.0f, fn.getNetwork().getValue(1, 0));
        assertEquals(2.0f, fn.getNetwork().getValue(1, 2));
        assertEquals(2.0f, fn.getNetwork().getValue(2, 1));
        }
    }