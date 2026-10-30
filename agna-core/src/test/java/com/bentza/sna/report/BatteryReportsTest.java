/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BatteryReportsTest {

    private Network testNet;
    private AgnaLib lib;

    @BeforeEach
    public void setUp() {
        testNet = new Network(4);
        testNet.setName("BatteryTestNet");
        testNet.getActor(0).setName("A");
        testNet.getActor(1).setName("B");
        testNet.getActor(2).setName("C");
        testNet.getActor(3).setName("D");

        // Directed kite graph
        testNet.setValue(1.0f, 0, 1);
        testNet.setValue(1.0f, 1, 0);
        testNet.setValue(1.0f, 1, 2);
        testNet.setValue(1.0f, 2, 1);
        testNet.setValue(1.0f, 0, 2);
        testNet.setValue(1.0f, 2, 0);
        testNet.setValue(1.0f, 2, 3);
        testNet.setValue(1.0f, 3, 2);

        lib = new AgnaLib();
        TableRegistry.clear();
    }

    @Test
    public void testCentralityBatteryReport() {
        CentralityOptions opts = new CentralityOptions();
        opts.setIncludeNodalDegree(true);
        opts.setIncludeInDegree(true);
        opts.setIncludeOutDegree(true);
        opts.setIncludeCloseness(true);
        opts.setIncludeBetweenness(true);
        opts.setIncludeEigenvector(true);
        opts.setIncludeBonacich(true);
        opts.setIncludePageRank(true);
        opts.setIncludePrestige(true);

        AnalysisReport report = AnalysisReportFactory.createCentralityBatteryReport(testNet, lib, opts);
        assertNotNull(report);
        String html = report.toHtmlReport();
        assertTrue(html.contains("Centrality &amp; Prestige Battery"));
        assertTrue(html.contains("Distribution of Nodal Degree"));
        assertTrue(html.contains("Eigenvector Centrality"));
        assertTrue(html.contains("PageRank Centrality"));
        assertTrue(html.contains("Bonacich"));
        assertTrue(html.contains("Freeman Network Centralization Indices"));
        assertFalse(report.getTables().isEmpty());

        String legacy = lib.legacyOutCentralityBattery(testNet, opts);
        assertNotNull(legacy);
        assertTrue(legacy.contains("Centrality & Prestige Battery"));
    }

    @Test
    public void testDistanceBatteryReport() {
        DistanceOptions opts = new DistanceOptions();
        opts.setIncludeGeodesicsMatrix(true);
        opts.setIncludeDiameter(true);
        opts.setIncludeEccentricity(true);
        opts.setIncludeSpecificShortestPath(true);
        opts.setFromNode(0);
        opts.setToNode(2);
        opts.setIncludeAllShortestPaths(true);

        AnalysisReport report = AnalysisReportFactory.createDistanceBatteryReport(testNet, lib, opts);
        assertNotNull(report);
        String html = report.toHtmlReport();
        assertTrue(html.contains("Distances &amp; Geodesics Battery"));
        assertTrue(html.contains("Geodesic Distances Matrix"));
        assertTrue(html.contains("Distance &amp; Diameter Summary"));
        assertTrue(html.contains("Paths from A to C"));
        assertFalse(report.getTables().isEmpty());

        String legacy = lib.legacyOutDistanceBattery(testNet, opts);
        assertNotNull(legacy);
        assertTrue(legacy.contains("Distances & Geodesics Battery"));
    }

    @Test
    public void testClusteringBatteryReport() {
        ClusteringOptions opts = new ClusteringOptions();
        opts.setIncludeLocalClustering(true);
        opts.setIncludeTransitivity(true);
        opts.setIncludeKCore(true);
        opts.setIncludeCohesion(true);
        opts.setIncludeOpenChains(true);
        opts.setIncludeCliques(true);

        AnalysisReport report = AnalysisReportFactory.createClusteringBatteryReport(testNet, lib, opts);
        assertNotNull(report);
        String html = report.toHtmlReport();
        assertTrue(html.contains("Clustering &amp; Cohesion Battery"));
        assertTrue(html.contains("Distribution of Local Clustering"));
        assertTrue(html.contains("Transitivity Indices"));
        assertTrue(html.contains("Core Shell Distribution"));
        assertTrue(html.contains("Cohesion Summary"));
        assertTrue(html.contains("Community Subgraph Summary"));
        assertFalse(report.getTables().isEmpty());

        String legacy = lib.legacyOutClusteringBattery(testNet, opts);
        assertNotNull(legacy);
        assertTrue(legacy.contains("Clustering & Cohesion Battery"));
    }

    @Test
    public void testSociometricsBatteryReport() {
        SociometricsOptions opts = new SociometricsOptions();
        opts.setIncludeBasic(true);
        opts.setIncludeDensity(true);
        opts.setIncludeDyadCensus(true);
        opts.setIncludeInDegree(true);
        opts.setIncludeOutDegree(true);
        opts.setIncludeSociostatus(true);

        AnalysisReport report = AnalysisReportFactory.createSociometricsBatteryReport(testNet, lib, opts);
        assertNotNull(report);
        String html = report.toHtmlReport();
        assertTrue(html.contains("Sociometrics &amp; Basic Battery"));
        assertTrue(html.contains("Network Structural Parameters"));
        assertTrue(html.contains("Density Summary"));
        assertTrue(html.contains("Dyad Census"));
        assertTrue(html.contains("Distribution of Indegree"));
        assertTrue(html.contains("Degree Assortativity Coefficients"));
        assertFalse(report.getTables().isEmpty());

        String legacy = lib.legacyOutSociometricsBattery(testNet, opts);
        assertNotNull(legacy);
        assertTrue(legacy.contains("Sociometrics & Basic Battery"));
    }
}
