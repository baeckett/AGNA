/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.benchmark;

import java.util.Locale;

/**
 * Metric result capturing runtime performance and numerical validation
 * of a benchmarked network algorithm.
 */
public class BenchmarkResult {
    private final String algorithmName;
    private final BenchmarkCategory category;
    private final String theoreticalComplexity;
    private final int nodeCount;
    private final int edgeCount;
    private final int iterations;
    private final double totalTimeMs;
    private final double meanTimeMs;
    private final double opsPerSec;
    private final boolean passedValidation;
    private final String validationNotes;

    public BenchmarkResult(String algorithmName, BenchmarkCategory category,
                           String theoreticalComplexity, int nodeCount, int edgeCount,
                           int iterations, double totalTimeMs,
                           boolean passedValidation, String validationNotes) {
        this.algorithmName = algorithmName;
        this.category = category;
        this.theoreticalComplexity = theoreticalComplexity;
        this.nodeCount = nodeCount;
        this.edgeCount = edgeCount;
        this.iterations = Math.max(1, iterations);
        this.totalTimeMs = totalTimeMs;
        this.meanTimeMs = totalTimeMs / this.iterations;
        this.opsPerSec = (this.meanTimeMs > 0) ? (1000.0 / this.meanTimeMs) : 0;
        this.passedValidation = passedValidation;
        this.validationNotes = validationNotes;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public BenchmarkCategory getCategory() {
        return category;
    }

    public String getTheoreticalComplexity() {
        return theoreticalComplexity;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getIterations() {
        return iterations;
    }

    public double getTotalTimeMs() {
        return totalTimeMs;
    }

    public double getMeanTimeMs() {
        return meanTimeMs;
    }

    public double getOpsPerSec() {
        return opsPerSec;
    }

    public boolean isPassedValidation() {
        return passedValidation;
    }

    public String getValidationNotes() {
        return validationNotes;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%-28s | %-12s | %8.3f ms | %8.1f ops/s | %s",
                algorithmName, category.name(), meanTimeMs, opsPerSec,
                passedValidation ? "PASS" : "FAIL (" + validationNotes + ")");
    }
}
