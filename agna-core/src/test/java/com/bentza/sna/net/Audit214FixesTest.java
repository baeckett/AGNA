/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.io.HTMLParser;
import com.bentza.sna.io.IOUtils;
import java.io.File;
import org.junit.jupiter.api.Test;

/**
 * Regression and validation tests for AGNA 2.1.4 audit improvements:
 * - Brandes betweenness double precision accumulation
 * - HTMLParser XML/HTML entity escaping and line break preservation
 * - IOUtils path preservation without lowercase destruction or directory-dot confusion
 */
public class Audit214FixesTest
    {
    @Test
    public void betweennessOnDiamond()
        {
        // 0 -> 1 -> 3, 0 -> 2 -> 3: nodes 1 and 2 each carry half the geodesics
        Network net = new Network(4);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 0, 2);
        net.setValue(1f, 1, 3);
        net.setValue(1f, 2, 3);

        float[] cb = new AgnaLib().betweenness(net);
        assertEquals(0f, cb[0], 1e-5f);
        assertEquals(0.5f, cb[1], 1e-5f);
        assertEquals(0.5f, cb[2], 1e-5f);
        assertEquals(0f, cb[3], 1e-5f);
        }

    @Test
    public void htmlParserEscapesEntitiesAndHandlesTrailingNewlines()
        {
        String raw = "Node <A> & Node \"B\"\nSecond line\n";
        String html = HTMLParser.parseTextToHTML(raw);
        assertTrue(html.contains("&lt;A&gt;"), "escapes < and >: " + html);
        assertTrue(html.contains("&amp;"), "escapes &: " + html);
        assertTrue(html.contains("&quot;B&quot;"), "escapes quotes: " + html);
        assertTrue(html.contains("<br>Second line<br>"), "handles intermediate and terminal newlines: " + html);
        }

    @Test
    public void pathWithoutExtensionPreservesCaseAndDirectoryDots()
        {
        String path = "/Volumes/MacAPFS/MyData.2.1.4/SocialNetwork.agn";
        String result = IOUtils.getPathWithoutExtension(path);
        assertEquals("/Volumes/MacAPFS/MyData.2.1.4/SocialNetwork", result);
        }

    @Test
    public void pathWithoutExtensionHandlesBareFile()
        {
        assertEquals("Network", IOUtils.getPathWithoutExtension("Network.csv"));
        assertEquals("NoExtension", IOUtils.getPathWithoutExtension("NoExtension"));
        }

    @Test
    public void intListO1AppendAndIteration()
        {
        IntList list = new IntList();
        assertEquals(0, list.getSize());
        for (int i = 0; i < 1000; i++)
            {
            list.appendValue(i);
            }
        assertEquals(1000, list.getSize());
        assertEquals(0, list.getFirstElement().getValue());
        assertEquals(999, list.getLastElement().getValue());
        assertEquals(500, list.getElementAt(500).getValue());

        int count = 0;
        for (int val : list)
            {
            assertEquals(count++, val);
            }
        assertEquals(1000, count);

        int[] arr = list.toArray();
        assertEquals(1000, arr.length);
        assertEquals(0, arr[0]);
        assertEquals(999, arr[999]);

        list.deleteElementAt(0);
        assertEquals(999, list.getSize());
        assertEquals(1, list.getFirstElement().getValue());

        list.deleteElementAt(list.getSize() - 1);
        assertEquals(998, list.getSize());
        assertEquals(998, list.getLastElement().getValue());
        }

    @Test
    public void networkOptimizedAccessorsProduceIdenticalResults()
        {
        Network net = new Network(4);
        net.setValue(1.5f, 0, 1);
        net.setValue(2.5f, 1, 0);
        net.setValue(3.0f, 1, 2);
        net.setValue(4.0f, 2, 3);

        assertEquals(4, net.getEdgesNumber());
        assertEquals(1.5f, net.getMin(), 1e-5f);
        assertEquals(4.0f, net.getMax(), 1e-5f);
        assertTrue(!net.isSymmetric());

        float[][] mat = net.getMatrix();
        assertEquals(1.5f, mat[0][1], 1e-5f);
        assertEquals(2.5f, mat[1][0], 1e-5f);
        assertEquals(3.0f, mat[1][2], 1e-5f);
        assertEquals(4.0f, mat[2][3], 1e-5f);
        assertEquals(0.0f, mat[0][0], 1e-5f);

        boolean[][] bMat = net.getBooleanMatrix();
        assertTrue(bMat[0][1]);
        assertTrue(bMat[1][0]);
        assertTrue(bMat[1][2]);
        assertTrue(bMat[2][3]);
        assertTrue(!bMat[0][0]);
        assertTrue(!bMat[0][2]);

        int[][] iMat = net.getIntegerMatrix();
        assertEquals(1, iMat[0][1]);
        assertEquals(2, iMat[1][0]);
        assertEquals(3, iMat[1][2]);
        assertEquals(4, iMat[2][3]);

        // Symmetric check
        Network symNet = new Network(3);
        symNet.setValue(2f, 0, 1);
        symNet.setValue(2f, 1, 0);
        symNet.setValue(5f, 1, 2);
        symNet.setValue(5f, 2, 1);
        assertTrue(symNet.isSymmetric());

        // Reception degree validation
        AgnaLib lib = new AgnaLib();
        float[] rec = lib.weightedReceptionDegree(net);
        assertEquals(2.5f, rec[0], 1e-5f); // in-degree to node 0: 1->0 (2.5)
        assertEquals(1.5f, rec[1], 1e-5f); // in-degree to node 1: 0->1 (1.5)
        assertEquals(3.0f, rec[2], 1e-5f); // in-degree to node 2: 1->2 (3.0)
        assertEquals(4.0f, rec[3], 1e-5f); // in-degree to node 3: 2->3 (4.0)
        }
    }
