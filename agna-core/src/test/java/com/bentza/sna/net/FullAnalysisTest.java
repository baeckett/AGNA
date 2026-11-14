/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.core.AppRuntime;
import com.bentza.sna.report.AnalysisReport;
import com.bentza.sna.report.AnalysisReportFactory;
import com.bentza.sna.report.FullAnalysisOptions;
import org.junit.jupiter.api.Test;

/**
 * Full Analysis battery - tests standard profile, customizable options,
 * disconnected network handling, and SPSS-style reporting.
 */
public class FullAnalysisTest
    {
    @Test
    public void connectedNetworkReceivesTheFullBattery()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);

        String out = new AgnaLib().outFullAnalysis(net);
        assertTrue(out.contains("FULL ANALYSIS"));
        assertTrue(out.contains("Density"));
        assertTrue(out.contains("Nodal Degree"));
        assertTrue(out.contains("Betweenness"));
        assertTrue(out.contains("Prestige"));
        assertTrue(out.contains("Geodesic"));
        assertTrue(!out.contains("measures skipped"), "connected net: no skips");
        }

    @Test
    public void disconnectedNetworkSkipsDistanceMeasuresWithANote()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1); // 1 reaches nothing back -> disconnected

        String out = new AgnaLib().outFullAnalysis(net);
        assertTrue(out.contains("measures skipped: the network is disconnected"));
        assertTrue(!out.contains("Distribution of Betweenness"),
                "betweenness must be skipped when disconnected");
        }

    @Test
    public void standardBatteryIncludesModernSnaMetrics()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);
        net.setValue(1f, 0, 2);
        net.setValue(1f, 2, 0);

        AgnaLib lib = new AgnaLib();
        AnalysisReport report = AnalysisReportFactory.createFullAnalysisReport(net, lib, FullAnalysisOptions.createStandard(), "");
        String html = report.toHtmlReport();

        assertTrue(html.contains("FULL ANALYSIS Battery"));
        assertTrue(html.contains("Density"));
        assertTrue(html.contains("Eigenvector Centrality"));
        assertTrue(html.contains("Local Clustering"));
        assertTrue(html.contains("Transitivity Indices") || html.contains("Transitivity"));
        assertTrue(html.contains("Core Shell Distribution") || html.contains("Actor Coreness"));
        assertTrue(html.contains("Structural Holes Metrics") || html.contains("Constraint (C_i)"));
        assertTrue(html.contains("Connected Components of") || html.contains("Weak Components"));
        assertTrue(html.contains("Notes / Methodological Remarks"), "Must contain SPSS notes block");
        }

    @Test
    public void customBatteryOnlyIncludesSelectedMetrics()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);

        FullAnalysisOptions options = new FullAnalysisOptions();
        options.setIncludeBasicAndDensity(true);
        options.setIncludeDegreeMetrics(false);
        options.setIncludePrestige(false);
        options.setIncludeGeodesicCentralities(false);
        options.setIncludeSpectralCentrality(false);
        options.setIncludeClusteringAndCores(false);
        options.setIncludeStructuralHoles(true);
        options.setIncludeConnectivityAndDyad(false);

        AgnaLib lib = new AgnaLib();
        AnalysisReport report = AnalysisReportFactory.createFullAnalysisReport(net, lib, options, "");
        String html = report.toHtmlReport();

        assertTrue(html.contains("Density"));
        assertTrue(html.contains("Structural Holes Metrics") || html.contains("Constraint (C_i)"));
        assertFalse(html.contains("Distribution of Betweenness in"));
        assertFalse(html.contains("Eigenvector"));
        assertFalse(html.contains("k-Core"));
        }

    @Test
    public void fullBatteryWithExtendedMetrics()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);

        FullAnalysisOptions options = FullAnalysisOptions.createAll();
        AgnaLib lib = new AgnaLib();
        AnalysisReport report = AnalysisReportFactory.createFullAnalysisReport(net, lib, options, "");
        String html = report.toHtmlReport();

        assertTrue(html.contains("Bonacich Power Centrality"));
        assertTrue(html.contains("PageRank Centrality"));
        assertTrue(html.contains("Triad Census"));
        assertTrue(html.contains("1-Cliques in") || html.contains("Cliques"));
        }
    }