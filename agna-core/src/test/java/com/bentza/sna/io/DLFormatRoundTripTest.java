/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.io;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.net.Network;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

public class DLFormatRoundTripTest {

    @Test
    public void testParseFullMatrixDL() throws Exception {
        String dlContent = "dl n=3\n"
                + "format=fullmatrix\n"
                + "labels:\n"
                + "Alice, Bob, Carol\n"
                + "data:\n"
                + "0 1 2.5\n"
                + "1 0 0\n"
                + "0 3.0 0\n";

        BufferedReader reader = new BufferedReader(new StringReader(dlContent));
        Network net = DLImporter.parseDL(reader, "TestDL");

        assertNotNull(net);
        assertEquals(3, net.getSize());
        assertEquals("Alice", net.getActor(0).getName());
        assertEquals("Bob", net.getActor(1).getName());
        assertEquals("Carol", net.getActor(2).getName());

        assertEquals(1.0f, net.getValue(0, 1), 1e-4);
        assertEquals(2.5f, net.getValue(0, 2), 1e-4);
        assertEquals(3.0f, net.getValue(2, 1), 1e-4);
    }

    @Test
    public void testParseEdgeListDL() throws Exception {
        String dlContent = "dl n=3\n"
                + "format=edgelist1\n"
                + "data:\n"
                + "Anna Beth 1.5\n"
                + "Beth Chloe 2.0\n";

        BufferedReader reader = new BufferedReader(new StringReader(dlContent));
        Network net = DLImporter.parseDL(reader, "EdgeListDL");

        assertNotNull(net);
        assertEquals(3, net.getSize());
        assertEquals(1.5f, net.getValue(0, 1), 1e-4);
        assertEquals(2.0f, net.getValue(1, 2), 1e-4);
    }

    @Test
    public void testExportAndReimportDL() throws Exception {
        Network orig = new Network(3);
        orig.setName("DLRoundTrip");
        orig.getActor(0).setName("Alpha");
        orig.getActor(1).setName("Beta");
        orig.getActor(2).setName("Gamma");
        orig.setValue(1.0f, 0, 1);
        orig.setValue(5.5f, 1, 2);

        File tempFile = Files.createTempFile("agna_test_", ".dl").toFile();
        tempFile.deleteOnExit();

        DLExporter.exportDL(orig, tempFile);
        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);

        Network imported = DLImporter.importDL(tempFile);
        assertNotNull(imported);
        assertEquals(3, imported.getSize());
        assertEquals("Alpha", imported.getActor(0).getName());
        assertEquals(1.0f, imported.getValue(0, 1), 1e-4);
        assertEquals(5.5f, imported.getValue(1, 2), 1e-4);
    }

    @Test
    public void testParseNodeListDL() throws Exception {
        String dlContent = "dl nr=3, format=nodelist1\n"
                + "labels:\n"
                + "\"Node One\", \"Node Two\", \"Node Three\"\n"
                + "data:\n"
                + "\"Node One\" \"Node Two\" \"Node Three\"\n"
                + "\"Node Two\" \"Node Three\"\n";

        BufferedReader reader = new BufferedReader(new StringReader(dlContent));
        Network net = DLImporter.parseDL(reader, "NodeListDL");

        assertNotNull(net);
        assertEquals(3, net.getSize());
        assertEquals("Node One", net.getActor(0).getName());
        assertEquals("Node Two", net.getActor(1).getName());
        assertEquals("Node Three", net.getActor(2).getName());

        assertEquals(1.0f, net.getValue(0, 1), 1e-4);
        assertEquals(1.0f, net.getValue(0, 2), 1e-4);
        assertEquals(1.0f, net.getValue(1, 2), 1e-4);
    }
}
