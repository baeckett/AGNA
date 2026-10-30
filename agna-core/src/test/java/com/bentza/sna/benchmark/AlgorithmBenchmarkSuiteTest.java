/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.benchmark;

import com.bentza.sna.net.Network;
import com.bentza.sna.net.NetworkGenerator;
import com.bentza.sna.report.AnalysisReport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlgorithmBenchmarkSuiteTest {

    @Test
    public void testBenchmarkRunsAcrossAllCategories() {
        Network net = NetworkGenerator.createWattsStrogatz(35, 4, 0.1, 42L);
        net.setName("Benchmark_Test_Network");

        List<BenchmarkResult> results = AlgorithmBenchmarkSuite.runBenchmark(net, 3);
        assertNotNull(results);
        assertEquals(10, results.size(), "Should benchmark 10 distinct algorithms");

        boolean hasCentrality = false;
        boolean hasClustering = false;
        boolean hasPaths = false;

        for (BenchmarkResult r : results) {
            assertTrue(r.isPassedValidation(), "Algorithm " + r.getAlgorithmName() + " should pass validation: " + r.getValidationNotes());
            assertTrue(r.getMeanTimeMs() >= 0.0);
            assertTrue(r.getOpsPerSec() >= 0.0);
            if (r.getCategory() == BenchmarkCategory.CENTRALITY) hasCentrality = true;
            if (r.getCategory() == BenchmarkCategory.CLUSTERING) hasClustering = true;
            if (r.getCategory() == BenchmarkCategory.PATHS) hasPaths = true;
        }

        assertTrue(hasCentrality, "Should contain Centrality benchmarks");
        assertTrue(hasClustering, "Should contain Clustering benchmarks");
        assertTrue(hasPaths, "Should contain Paths benchmarks");
    }

    @Test
    public void testBenchmarkReportGeneration() {
        Network net = NetworkGenerator.createBarabasiAlbert(30, 2, 42L);
        net.setName("BA_Benchmark");

        List<BenchmarkResult> results = AlgorithmBenchmarkSuite.runBenchmark(net, 2);
        AnalysisReport report = AlgorithmBenchmarkSuite.createBenchmarkReport(net, results, 2);

        assertNotNull(report);
        assertEquals(1, report.getTables().size());
        assertTrue(report.getFootnotes().size() >= 4, "Report should have SPSS footnotes");

        String html = report.toHtmlReport();
        assertTrue(html.contains("Algorithm Benchmark Results"));
        assertTrue(html.contains("Brandes Betweenness"));
        assertTrue(html.contains("Watts-Strogatz Clustering"));
        assertTrue(html.contains("All-Pairs Geodesics"));
        assertTrue(html.contains("Notes / Methodological Remarks"));
    }
}
