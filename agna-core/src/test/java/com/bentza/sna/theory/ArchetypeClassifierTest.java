/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.theory;

import com.bentza.sna.net.Network;
import com.bentza.sna.net.NetworkGenerator;
import com.bentza.sna.report.AnalysisReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArchetypeClassifierTest {

    @Test
    public void testSmallWorldArchetypeDetection() {
        // Watts-Strogatz small-world network: N=40, k=4, beta=0.05
        Network sw = NetworkGenerator.createWattsStrogatz(40, 4, 0.05, 42L);
        sw.setName("WattsStrogatz_Test");

        ArchetypeMetrics m = ArchetypeClassifier.computeMetrics(sw);
        assertNotNull(m);
        assertTrue(m.getClusteringC() > 0.2f, "Small-world should have high clustering C");
        assertTrue(m.getSmallWorldSigma() >= 1.0f, "Small-world sigma should be >= 1.0");
        assertTrue(m.getSmallWorldFit() >= 0.5f, "Small-world fit score should be high");
    }

    @Test
    public void testPreferentialAttachmentArchetypeDetection() {
        // Barabási-Albert scale-free network: N=50, m=2
        Network ba = NetworkGenerator.createBarabasiAlbert(50, 2, 42L);
        ba.setName("BarabasiAlbert_Test");

        ArchetypeMetrics m = ArchetypeClassifier.computeMetrics(ba);
        assertNotNull(m);
        assertTrue(m.getDegreeGini() > 0.25f, "Scale-free degree distribution should show inequality");
        assertTrue(m.getMaxDegreeHubRatio() > 1.8f, "Hub nodes should have degree higher than average");
        assertTrue(m.getPreferentialAttachmentFit() > 0.4f, "Preferential attachment fit score should be significant");
    }

    @Test
    public void testHomophilyArchetypeDetection() {
        // Stochastic Block Model: 3 blocks, high intra-block density, low inter-block density
        Network sbm = NetworkGenerator.createStochasticBlockModel(45, 3, 0.70, 0.02, 42L);
        sbm.setName("SBM_Homophily_Test");

        ArchetypeMetrics m = ArchetypeClassifier.computeMetrics(sbm);
        assertNotNull(m);
        assertTrue(m.getModularityQ() > 0.35f, "Clustered homophilous network should have Newman Q > 0.35");
        assertTrue(m.getCommunityCount() >= 2, "Should detect multiple community clusters");
        assertTrue(m.getHomophilyFit() >= 0.6f, "Homophily fit score should be high");
    }

    @Test
    public void testHierarchyArchetypeDetection() {
        // Hierarchical Tree: branching factor 3, directed, no lateral
        Network tree = NetworkGenerator.createHierarchicalTree(40, 3, false, true);
        tree.setName("Corporate_Hierarchy_Test");

        ArchetypeMetrics m = ArchetypeClassifier.computeMetrics(tree);
        assertNotNull(m);
        assertEquals(1.0f, m.getKrackhardtHierarchy(), 0.01f, "Pure directed tree should have GH = 1.0 (acyclic)");
        assertTrue(m.getKrackhardtEfficiency() > 0.8f, "Tree should have high structural efficiency");
        assertTrue(m.getHierarchyFit() >= 0.7f, "Hierarchy fit score should be high");
    }

    @Test
    public void testDiagnosticReportGeneration() {
        Network sw = NetworkGenerator.createWattsStrogatz(30, 4, 0.05, 42L);
        sw.setName("DiagnosticReport_Test");

        ArchetypeMetrics m = ArchetypeClassifier.computeMetrics(sw);
        AnalysisReport report = ArchetypeClassifier.createDiagnosticReport(sw, m);

        assertNotNull(report);
        assertEquals(2, report.getTables().size(), "Report should contain Summary and Detail tables");
        assertTrue(report.getFootnotes().size() >= 3, "Report should contain SPSS-style footnotes");

        String html = report.toHtmlReport();
        assertTrue(html.contains("Theoretical Archetype Fit Summary"));
        assertTrue(html.contains("Structural Signature Diagnostics"));
        assertTrue(html.contains("Notes / Methodological Remarks"));
        assertTrue(html.contains("Humphries &amp; Gurney (2008)") || html.contains("Humphries & Gurney (2008)"));
    }
}
