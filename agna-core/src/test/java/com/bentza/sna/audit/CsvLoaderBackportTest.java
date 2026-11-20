/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;

/** 2.2.2: CSV files are read as RFC 4180 (quoted names, other separators, BOM); unquoted legacy files are unchanged. */
public class CsvLoaderBackportTest
    {
    private static Network load(String text, String ext)
        {
        FullNet full = new FullNet();
        full.readNetwork(text, ext);
        assertNotNull(full.getNetwork(), "network must load");
        return full.getNetwork();
        }

    @Test
    public void quotedNamesWithSeparatorsAndQuotesLoadAllActors()
        {
        // 2.2.1 loaded only three of these four actors and renamed them 1, 2, 3
        String csv = ",\u0218tefan,\u0418\u043e\u043d\u043e\u0432,\u674e\u96f7,\"Jos\u00e9 \"\"Pepe\"\", Jr.\"\n"
                + "\u0218tefan,0,1,1,1\n"
                + "\u0418\u043e\u043d\u043e\u0432,1,0,1,1\n"
                + "\u674e\u96f7,1,1,0,1\n"
                + "\"Jos\u00e9 \"\"Pepe\"\", Jr.\",1,1,1,0\n";
        Network net = load(csv, "csv");
        assertEquals(4, net.getSize());
        assertEquals("\u0218tefan", net.getActor(0).getName());
        assertEquals("Jos\u00e9 \"Pepe\", Jr.", net.getActor(3).getName());
        assertEquals(1f, net.getValue(3, 0), 0f);
        assertEquals(0f, net.getValue(3, 3), 0f);
        }

    @Test
    public void semicolonSeparatedAndBomFilesLoad()
        {
        String csv = "\ufeff;a;b;c\na;0;1;0\nb;1;0;1\nc;0;1;0\n";
        Network net = load(csv, "csv");
        assertEquals(3, net.getSize());
        assertEquals("b", net.getActor(1).getName());
        assertEquals(1f, net.getValue(1, 2), 0f);
        }

    @Test
    public void plainLabelledCsvIsUnchanged()
        {
        String csv = ",a,b,c\na,0,1,0\nb,1,0,1\nc,0,1,0\n";
        Network net = load(csv, "csv");
        assertEquals(3, net.getSize());
        assertEquals("a", net.getActor(0).getName());
        assertEquals(1f, net.getValue(0, 1), 0f);
        assertEquals(0f, net.getValue(0, 2), 0f);
        }

    @Test
    public void unlabelledNumericCsvStillLoadsThroughTheLegacyReader()
        {
        String csv = "0,1,0\n1,0,1\n0,1,0\n";
        Network net = load(csv, "csv");
        assertEquals(3, net.getSize());
        assertEquals(1f, net.getValue(1, 2), 0f);
        }
    }
