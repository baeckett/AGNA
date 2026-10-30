/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.benchmark;

/**
 * Functional categories of network analysis algorithms for benchmarking.
 */
public enum BenchmarkCategory {
    CENTRALITY("Centrality & Prominence", "Betweenness, Closeness, Degree, Bavelas"),
    CLUSTERING("Clustering & Cohesion", "Local Clustering, Transitivity, Cliques Enumeration"),
    PATHS("Paths & Distances", "All-Pairs Geodesics, Graph Diameter, Shortest Paths");

    private final String displayName;
    private final String description;

    BenchmarkCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
