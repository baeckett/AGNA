/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import org.junit.jupiter.api.Test;

public class AnalysisReportTest {

    @Test
    public void testHtmlReportGeneration() {
        Network net = new Network(3);
        net.setName("TestNet");
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);

        AgnaLib lib = new AgnaLib();
        AnalysisReport report = AnalysisReportFactory.createBasicReport(net, lib, "legacy text");

        String html = report.toHtmlReport();
        assertTrue(html.contains("AGNA Analysis: Basic Network Description"));
        assertTrue(html.contains("TestNet"));
        assertTrue(html.contains("Node Count (n)"));
        assertTrue(html.contains("Directed Edges"));
        assertTrue(html.contains("export:"));

        String plain = report.toPlainText();
        assertEquals("legacy text", plain);
    }

    @Test
    public void testInDegreeReportTablesRegistered() {
        Network net = new Network(3);
        net.setName("Triangle");
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 0);

        TableRegistry.clear();
        AgnaLib lib = new AgnaLib();
        AnalysisReport report = AnalysisReportFactory.createInDegreeReport(net, lib, "legacy");

        assertEquals(2, report.getTables().size());
        assertEquals(2, TableRegistry.getRecentTables().size());

        TableData distTable = report.getTables().get(0);
        assertTrue(distTable.getTitle().contains("Indegree"));
        assertEquals(3, distTable.getRowCount());
    }
}
