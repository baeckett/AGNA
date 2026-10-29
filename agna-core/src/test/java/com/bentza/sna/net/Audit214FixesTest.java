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
    }
