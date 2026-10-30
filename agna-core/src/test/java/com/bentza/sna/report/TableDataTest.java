/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import javax.swing.table.TableModel;
import org.junit.jupiter.api.Test;

public class TableDataTest {

    @Test
    public void testCsvExportWithEscaping() {
        TableData td = TableData.builder("Test Table")
                .network("test.agn")
                .columns("Actor", "Quote,Comma", "Score")
                .isNumeric(false, false, true)
                .addRow("Alice", "Hello, \"World\"", 4.2)
                .addRow("Bob", "Line 1\nLine 2", 10.0)
                .build();

        String csv = td.toCsv();
        assertTrue(csv.contains("Actor,\"Quote,Comma\",Score"));
        assertTrue(csv.contains("\"Hello, \"\"World\"\"\""));
        assertTrue(csv.contains("\"Line 1\nLine 2\""));
        assertTrue(csv.contains("4.2000"));
    }

    @Test
    public void testJsonExport() {
        TableData td = TableData.builder("Indegree Table")
                .network("net1")
                .columns("Node", "Indegree")
                .isNumeric(false, true)
                .addRow("N1", 0.5)
                .addRow("N2", 0.75)
                .build();

        String json = td.toJson();
        assertTrue(json.contains("\"title\": \"Indegree Table\""));
        assertTrue(json.contains("\"columns\": [\"Node\", \"Indegree\"]"));
        assertTrue(json.contains("\"rowCount\": 2"));
        assertTrue(json.contains("[\"N1\", 0.5000]"));
        assertTrue(json.contains("[\"N2\", 0.7500]"));
    }

    @Test
    public void testTsvExport() {
        TableData td = TableData.builder("TSV Table")
                .columns("Col1", "Col2")
                .addRow("Val1", "Val2")
                .build();

        String tsv = td.toTsv();
        assertTrue(tsv.contains("Col1\tCol2\n"));
        assertTrue(tsv.contains("Val1\tVal2\n"));
    }

    @Test
    public void testHtmlExportContainsStylesAndExportLink() {
        TableData td = TableData.builder("HTML Table")
                .id("test-tbl-123")
                .columns("Actor", "Degree")
                .addRow("NodeA", 3)
                .build();

        String html = td.toHtmlTable(true);
        assertTrue(html.contains("export:test-tbl-123"));
        assertTrue(html.contains("border-collapse:collapse"));
        assertTrue(html.contains("NodeA"));
    }

    @Test
    public void testPlainTextTableAlignment() {
        TableData td = TableData.builder("Ascii Table")
                .columns("Node", "Value")
                .isNumeric(false, true)
                .addRow("A", 1.23)
                .addRow("LongNodeName", 456.78)
                .build();

        String text = td.toPlainTextTable();
        assertTrue(text.contains("+--------------+----------+"));
        assertTrue(text.contains("| Node         |    Value |"));
        assertTrue(text.contains("| LongNodeName | 456.7800 |"));
    }

    @Test
    public void testTableModelIntegration() {
        TableData td = TableData.builder("Model Table")
                .columns("ColA", "ColB")
                .addRow("Row1A", "Row1B")
                .addRow("Row2A", "Row2B")
                .build();

        TableModel model = td.toTableModel();
        assertEquals(2, model.getRowCount());
        assertEquals(2, model.getColumnCount());
        assertEquals("ColA", model.getColumnName(0));
        assertEquals("Row2B", model.getValueAt(1, 1));
    }
}
