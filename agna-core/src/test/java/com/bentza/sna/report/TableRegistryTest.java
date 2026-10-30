/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TableRegistryTest {

    @BeforeEach
    public void setup() {
        TableRegistry.clear();
    }

    @Test
    public void testRegisterAndRetrieve() {
        TableData td = TableData.builder("T1").id("id-1").columns("A").addRow("B").build();
        TableRegistry.register(td);

        assertEquals(td, TableRegistry.get("id-1"));
        assertNull(TableRegistry.get("nonexistent"));

        List<TableData> recent = TableRegistry.getRecentTables();
        assertEquals(1, recent.size());
        assertEquals("id-1", recent.get(0).getId());
    }

    @Test
    public void testRecentOrderAndLimit() {
        for (int i = 0; i < 120; i++) {
            TableData td = TableData.builder("T" + i).id("id-" + i).columns("A").addRow(i).build();
            TableRegistry.register(td);
        }

        List<TableData> recent = TableRegistry.getRecentTables();
        assertEquals(100, recent.size());
        assertEquals("id-119", recent.get(0).getId());
        assertNull(TableRegistry.get("id-0")); // trimmed
    }
}
