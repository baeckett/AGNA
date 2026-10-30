/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe central registry for analysis tables generated during an AGNA session.
 * Allows table lookup by ID for interactive HTML hyperlink exports, and maintains
 * an ordered list of recent tables for the Export Table dialog.
 */
public final class TableRegistry {
    private static final Map<String, TableData> tableMap = new ConcurrentHashMap<>();
    private static final List<TableData> recentTables = new CopyOnWriteArrayList<>();
    private static final int MAX_RECENT = 100;

    private TableRegistry() {}

    /**
     * Registers a table in the global session registry.
     */
    public static void register(TableData table) {
        if (table == null || table.getId() == null) {
            return;
        }
        tableMap.put(table.getId(), table);

        // Remove any existing entry with the same ID so recent order moves to front
        for (int i = 0; i < recentTables.size(); i++) {
            if (table.getId().equals(recentTables.get(i).getId())) {
                recentTables.remove(i);
                break;
            }
        }
        recentTables.add(0, table);

        // Trim excess
        while (recentTables.size() > MAX_RECENT) {
            TableData removed = recentTables.remove(recentTables.size() - 1);
            if (removed != null && removed.getId() != null) {
                tableMap.remove(removed.getId());
            }
        }
    }

    /**
     * Finds a table by its unique ID.
     */
    public static TableData get(String id) {
        if (id == null) return null;
        return tableMap.get(id);
    }

    /**
     * Returns an unmodifiable list of recently generated tables (newest first).
     */
    public static List<TableData> getRecentTables() {
        return Collections.unmodifiableList(new ArrayList<>(recentTables));
    }

    /**
     * Clears all registered tables.
     */
    public static void clear() {
        tableMap.clear();
        recentTables.clear();
    }
}
