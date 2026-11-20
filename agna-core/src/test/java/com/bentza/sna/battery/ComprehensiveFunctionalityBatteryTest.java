/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.battery;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.community.CommunityDetectionService;
import com.bentza.sna.community.CommunityDetectionService.CommunityResult;
import com.bentza.sna.equivalence.StructuralEquivalenceService;
import com.bentza.sna.equivalence.StructuralEquivalenceService.EquivalenceResult;
import com.bentza.sna.io.DLExporter;
import com.bentza.sna.io.DLImporter;
import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import com.bentza.sna.report.AnalysisReport;
import com.bentza.sna.report.AnalysisReportFactory;
import com.bentza.sna.report.CentralityOptions;
import com.bentza.sna.report.ClusteringOptions;
import com.bentza.sna.report.DistanceOptions;
import com.bentza.sna.report.FullAnalysisOptions;
import com.bentza.sna.report.SociometricsOptions;
import com.bentza.sna.report.TableData;
import com.bentza.sna.report.TableRegistry;
import com.bentza.sna.stats.DegreeAssortativity;
import com.bentza.sna.stats.DegreeAssortativity.AssortativityResult;
import com.bentza.sna.stats.WeightedMetrics;
import com.bentza.sna.stats.WeightedMetrics.WeightedSummary;
import com.bentza.sna.transform.NetworkTransformService;
import com.bentza.sna.transform.NetworkTransformService.SymmetrizeMode;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive verification battery covering all 2.1.6 analytical additions:
 * - Freeman Centralization indices & summary tables
 * - Newman Degree Assortativity & mixing patterns
 * - Louvain & Girvan-Newman Community Detection
 * - Structural Equivalence & Blockmodel Image Matrices
 * - Valued / Weighted Node Strengths & Barrat Clustering
 * - UCINET DL format import/export
 * - Battery Dialog options & dual-fidelity reports
 * - Metamorphic & symmetry invariance
 */
public class ComprehensiveFunctionalityBatteryTest {

    private AgnaLib lib;

    @BeforeEach
    public void setUp() {
        lib = new AgnaLib();
        TableRegistry.clear();
    }

    // =========================================================================
    // 1. FREEMAN GLOBAL CENTRALIZATION SUITE
    // =========================================================================
    @Nested
    @DisplayName("1. Freeman Centralization Suite")
    class FreemanCentralizationTests {

        @Test
        @DisplayName("Star network achieves maximum theoretical centralization (1.0)")
        public void testStarNetworkCentralizationIsOne() {
            int n = 5;
            Network star = new Network(n, 1); // Star network constructor

            float[] deg = lib.nodalDegree(star);
            float cD = lib.freemanGeneralIndex(deg, lib.nodalDegree(new Network(n, 1)));
            assertEquals(1.0f, cD, 1e-4);
            assertEquals(1.0f, lib.freemanDegreeCentralization(star), 1e-4);

            float[] betw = lib.betweenness(star);
            float cB = lib.freemanGeneralIndex(betw, lib.betweenness(new Network(n, 1)));
            assertEquals(1.0f, cB, 1e-4);

            float[] close = lib.closeness(star);
            float cC = lib.freemanGeneralIndex(close, lib.closeness(new Network(n, 1)));
            assertEquals(1.0f, cC, 1e-4);
        }

        @Test
        @DisplayName("Ring / Circle network achieves zero centralization (0.0)")
        public void testCircleNetworkCentralizationIsZero() {
            int n = 6;
            Network circle = new Network(n);
            for (int i = 0; i < n; i++) {
                int next = (i + 1) % n;
                circle.setValue(1.0f, i, next);
                circle.setValue(1.0f, next, i);
            }

            float[] deg = lib.nodalDegree(circle);
            float cD = lib.freemanGeneralIndex(deg, lib.nodalDegree(new Network(n, 1)));
            assertEquals(0.0f, cD, 1e-4);
            assertEquals(0.0f, lib.freemanDegreeCentralization(circle), 1e-4);

            float[] betw = lib.betweenness(circle);
            float cB = lib.freemanGeneralIndex(betw, lib.betweenness(new Network(n, 1)));
            assertEquals(0.0f, cB, 1e-4);

            float[] close = lib.closeness(circle);
            float cC = lib.freemanGeneralIndex(close, lib.closeness(new Network(n, 1)));
            assertEquals(0.0f, cC, 1e-4);
        }

        @Test
        @DisplayName("Freeman Centralization table reports correct status for disconnected networks")
        public void testDisconnectedFreemanTable() {
            Network disconnected = new Network(4);
            disconnected.setName("DisconnectedNet");
            disconnected.setValue(1.0f, 0, 1);
            disconnected.setValue(1.0f, 1, 0);
            // nodes 2 and 3 are isolated

            TableData table = AnalysisReportFactory.createFreemanCentralizationSummaryTable(disconnected, lib);
            assertNotNull(table);
            assertEquals("Freeman Network Centralization Indices of DisconnectedNet", table.getTitle());

            // Degree centralization should still be computed
            boolean foundDegree = false;
            boolean foundBetweennessNA = false;
            for (int r = 0; r < table.getRowCount(); r++) {
                String measure = (String) table.getValueAt(r, 0);
                String val = (String) table.getValueAt(r, 1);
                if (measure.contains("Degree")) {
                    foundDegree = true;
                    assertFalse(val.contains("N/A"));
                }
                if (measure.contains("Closeness")) {
                    // 2.2.1: closeness and Bavelas centralization are not reported for disconnected networks
                    foundBetweennessNA = true;
                    assertTrue(val.contains("N/A (Disconnected)"));
                }
            }
            assertTrue(foundDegree);
            assertTrue(foundBetweennessNA);
        }

        @Test
        @DisplayName("Outputs use standardized Freeman (1979) Centralization nomenclature, no Freeman General Coefficient")
        public void testStandardizedCentralizationLabelsInOutputs() {
            Network star = new Network(5, 1);
            star.setName("Star5");

            // Nodal degree report
            AnalysisReport degReport = AnalysisReportFactory.createNodalDegreeReport(star, lib, "");
            String degHtml = degReport.toHtmlReport();
            assertTrue(degHtml.contains("Freeman (1979) Degree Centralization (C_D)"));
            assertFalse(degHtml.contains("Freeman General Coefficient"));

            // Legacy output strings
            String legacyDeg = lib.legacyOutNodalDegree(star);
            assertTrue(legacyDeg.contains("Freeman (1979) Degree Centralization (C_D) = 1.0000"));
            assertFalse(legacyDeg.contains("Freeman General Coefficient"));

            String legacyBetw = lib.legacyOutBetweenness(star);
            assertTrue(legacyBetw.contains("Freeman (1979) Betweenness Centralization (C_B) = 1.0"));
            assertFalse(legacyBetw.contains("Freeman General Coefficient"));

            String legacyClose = lib.legacyOutCloseness(star);
            assertTrue(legacyClose.contains("Freeman (1979) Closeness Centralization (C_C) = 1.0"));
            assertFalse(legacyClose.contains("Freeman General Coefficient"));
        }
    }

    // =========================================================================
    // 2. NEWMAN DEGREE ASSORTATIVITY SUITE
    // =========================================================================
    @Nested
    @DisplayName("2. Newman Degree Assortativity Suite")
    class DegreeAssortativityTests {

        @Test
        @DisplayName("Directed mixing matrix dimensions and bounded correlations")
        public void testDirectedAssortativityProperties() {
            Network net = new Network(4);
            net.setValue(1.0f, 0, 1);
            net.setValue(1.0f, 1, 2);
            net.setValue(1.0f, 2, 0);
            net.setValue(1.0f, 2, 3);

            AssortativityResult res = DegreeAssortativity.compute(net);
            assertEquals(4, res.edgeCount);
            assertTrue(res.totalDegreeAssortativity >= -1.0 && res.totalDegreeAssortativity <= 1.0);
            assertTrue(res.outInAssortativity >= -1.0 && res.outInAssortativity <= 1.0);
            assertTrue(res.inInAssortativity >= -1.0 && res.inInAssortativity <= 1.0);
            assertTrue(res.outOutAssortativity >= -1.0 && res.outOutAssortativity <= 1.0);
            assertNotNull(res.mixingRegime);
        }

        @Test
        @DisplayName("Degree Assortativity Report generates structured table and properties")
        public void testAssortativityReportGeneration() {
            Network net = new Network(5);
            net.setName("AssortNet");
            for (int i = 1; i < 5; i++) {
                net.setValue(1.0f, 0, i);
                net.setValue(1.0f, i, 0);
            }

            AnalysisReport rep = AnalysisReportFactory.createDegreeAssortativityReport(net, lib, "");
            assertNotNull(rep);
            String html = rep.toHtmlReport();
            assertTrue(html.contains("Degree Assortativity Coefficients"));
            assertTrue(html.contains("Disassortative"));
            assertFalse(rep.getTables().isEmpty());
        }
    }

    // =========================================================================
    // 3. COMMUNITY DETECTION & MODULARITY SUITE
    // =========================================================================
    @Nested
    @DisplayName("3. Community Detection & Modularity Suite")
    class CommunityDetectionTests {

        @Test
        @DisplayName("Louvain partitions disconnected components into separate communities")
        public void testDisconnectedComponentsLouvain() {
            Network net = new Network(6);
            // Component 1: 0-1-2
            net.setValue(1.0f, 0, 1); net.setValue(1.0f, 1, 0);
            net.setValue(1.0f, 1, 2); net.setValue(1.0f, 2, 1);
            net.setValue(1.0f, 0, 2); net.setValue(1.0f, 2, 0);
            // Component 2: 3-4-5
            net.setValue(1.0f, 3, 4); net.setValue(1.0f, 4, 3);
            net.setValue(1.0f, 4, 5); net.setValue(1.0f, 5, 4);
            net.setValue(1.0f, 3, 5); net.setValue(1.0f, 5, 3);

            CommunityResult res = CommunityDetectionService.detectLouvain(net);
            assertEquals(2, res.communityCount);
            assertEquals(res.nodeAssignments[0], res.nodeAssignments[1]);
            assertEquals(res.nodeAssignments[3], res.nodeAssignments[4]);
            assertNotEquals(res.nodeAssignments[0], res.nodeAssignments[3]);
            assertEquals(0.5, res.modularityQ, 1e-3);
        }

        @Test
        @DisplayName("Girvan-Newman partitions two cliques with high modularity")
        public void testGirvanNewmanTwoCliques() {
            Network net = new Network(4);
            // Clique 1: 0-1, Clique 2: 2-3, Bridge: 1-2
            net.setValue(1.0f, 0, 1); net.setValue(1.0f, 1, 0);
            net.setValue(1.0f, 2, 3); net.setValue(1.0f, 3, 2);
            net.setValue(1.0f, 1, 2); net.setValue(1.0f, 2, 1);

            CommunityResult res = CommunityDetectionService.detectGirvanNewman(net);
            assertEquals(2, res.communityCount);
            assertEquals(res.nodeAssignments[0], res.nodeAssignments[1]);
            assertEquals(res.nodeAssignments[2], res.nodeAssignments[3]);
            assertNotEquals(res.nodeAssignments[0], res.nodeAssignments[2]);
        }

        @Test
        @DisplayName("Community detection report produces summary and affiliation tables")
        public void testCommunityReportGeneration() {
            Network net = new Network(4);
            net.setName("CommNet");
            net.setValue(1.0f, 0, 1); net.setValue(1.0f, 1, 0);
            net.setValue(1.0f, 2, 3); net.setValue(1.0f, 3, 2);

            AnalysisReport rep = AnalysisReportFactory.createCommunityDetectionReport(net, false, lib, "");
            assertNotNull(rep);
            assertEquals(2, rep.getTables().size());
            assertEquals("Community Subgraph Summary in CommNet", rep.getTables().get(0).getTitle());
            assertEquals("Actor Community Affiliation in CommNet", rep.getTables().get(1).getTitle());
        }
    }

    // =========================================================================
    // 4. STRUCTURAL EQUIVALENCE & BLOCKMODELING SUITE
    // =========================================================================
    @Nested
    @DisplayName("4. Structural Equivalence Suite")
    class StructuralEquivalenceTests {

        @Test
        @DisplayName("Complete Bipartite K_{3,3} partition and image matrix")
        public void testBipartiteEquivalence() {
            Network k33 = new Network(6);
            for (int i = 0; i < 3; i++) {
                for (int j = 3; j < 6; j++) {
                    k33.setValue(1.0f, i, j);
                    k33.setValue(1.0f, j, i);
                }
            }

            EquivalenceResult res = StructuralEquivalenceService.analyze(k33, 2);
            assertEquals(2, res.blockCount);

            // Actors in same partition have 0 Euclidean profile distance
            assertEquals(0.0, res.euclideanDistance[0][1], 1e-5);
            assertEquals(0.0, res.euclideanDistance[0][2], 1e-5);
            assertEquals(0.0, res.euclideanDistance[3][4], 1e-5);
            assertEquals(0.0, res.euclideanDistance[3][5], 1e-5);

            // Blocks are non-empty and cover all nodes
            int b0 = res.blockAssignments[0];
            int b3 = res.blockAssignments[3];
            assertNotEquals(b0, b3);

            // Inter-block density is 1.0, intra-block density is 0.0
            assertEquals(1.0, res.blockmodelDensity[b0][b3], 1e-5);
            assertEquals(1.0, res.blockmodelDensity[b3][b0], 1e-5);
            assertEquals(0.0, res.blockmodelDensity[b0][b0], 1e-5);
            assertEquals(0.0, res.blockmodelDensity[b3][b3], 1e-5);
        }

        @Test
        @DisplayName("Structural Equivalence Report generates block density and role tables")
        public void testEquivalenceReportGeneration() {
            Network net = new Network(4);
            net.setName("EquivNet");
            net.setValue(1.0f, 0, 2);
            net.setValue(1.0f, 1, 2);
            net.setValue(1.0f, 2, 3);

            AnalysisReport rep = AnalysisReportFactory.createStructuralEquivalenceReport(net, 2, lib, "");
            assertNotNull(rep);
            assertEquals(2, rep.getTables().size());
            assertTrue(rep.getTables().get(0).getTitle().contains("Blockmodel Image Matrix"));
            assertTrue(rep.getTables().get(1).getTitle().contains("Structural Role / Block Assignments"));
        }
    }

    // =========================================================================
    // 5. VALUED & WEIGHTED NETWORK METRICS SUITE
    // =========================================================================
    @Nested
    @DisplayName("5. Valued & Weighted Network Metrics Suite")
    class WeightedMetricsTests {

        @Test
        @DisplayName("Node strengths, valued density, and Barrat clustering bounds")
        public void testWeightedFormulas() {
            Network net = new Network(3);
            net.setValue(2.5f, 0, 1);
            net.setValue(2.5f, 1, 0);
            net.setValue(3.5f, 1, 2);
            net.setValue(3.5f, 2, 1);
            net.setValue(4.0f, 0, 2);
            net.setValue(4.0f, 2, 0);

            WeightedSummary sum = WeightedMetrics.compute(net);
            assertEquals(3, sum.nodeCount);

            // Node 0: (2.5 out + 4.0 out) + (2.5 in + 4.0 in) = 13.0 total strength
            assertEquals(6.5f, sum.outStrength[0], 1e-4);
            assertEquals(6.5f, sum.inStrength[0], 1e-4);
            assertEquals(13.0f, sum.totalStrength[0], 1e-4);

            // Triangle is closed -> Barrat clustering should be exactly 1.0
            assertEquals(1.0f, sum.weightedClustering[0], 1e-4);
            assertEquals(1.0f, sum.weightedClustering[1], 1e-4);
            assertEquals(1.0f, sum.weightedClustering[2], 1e-4);
            assertEquals(1.0, sum.meanWeightedClustering, 1e-4);
        }

        @Test
        @DisplayName("Weighted Metrics Report generates proper TableData")
        public void testWeightedReportGeneration() {
            Network net = new Network(3);
            net.setName("WeightNet");
            net.setValue(5.0f, 0, 1);

            AnalysisReport rep = AnalysisReportFactory.createWeightedMetricsReport(net, lib, "");
            assertNotNull(rep);
            assertFalse(rep.getTables().isEmpty());
            assertTrue(rep.toHtmlReport().contains("Node Strengths &amp; Weighted Clustering"));
        }
    }

    // =========================================================================
    // 6. UCINET DL FILE FORMAT INTEROPERABILITY SUITE
    // =========================================================================
    @Nested
    @DisplayName("6. UCINET DL Format Suite")
    class DLFormatTests {

        @Test
        @DisplayName("DL Matrix parser handles whitespace, comments, and labels")
        public void testParseDLMatrixWithComments() throws Exception {
            String dl = "# UCINET test dataset\n"
                    + "dl n = 4, format = fullmatrix\n"
                    + "labels:\n"
                    + "  Alfa, Bravo, Charlie, Delta\n"
                    + "data:\n"
                    + "0 1 0 0\n"
                    + "1 0 2.5 0\n"
                    + "0 2.5 0 1\n"
                    + "0 0 1 0\n";

            BufferedReader reader = new BufferedReader(new StringReader(dl));
            Network net = DLImporter.parseDL(reader, "DLCommentNet");

            assertEquals(4, net.getSize());
            assertEquals("Alfa", net.getActor(0).getName());
            assertEquals("Delta", net.getActor(3).getName());
            assertEquals(2.5f, net.getValue(1, 2), 1e-4);
            assertEquals(1.0f, net.getValue(2, 3), 1e-4);
            assertEquals(0.0f, net.getValue(0, 3), 1e-4);
        }

        @Test
        @DisplayName("DL EdgeList parser handles labeled pairs and custom weights")
        public void testParseDLEdgeList() throws Exception {
            String dl = "dl\n"
                    + "format = edgelist1\n"
                    + "n = 3\n"
                    + "data:\n"
                    + "X Y 3.2\n"
                    + "Y Z 4.8\n"
                    + "Z X 1.1\n";

            BufferedReader reader = new BufferedReader(new StringReader(dl));
            Network net = DLImporter.parseDL(reader, "EdgeListNet");

            assertEquals(3, net.getSize());
            assertEquals(3.2f, net.getValue(0, 1), 1e-4);
            assertEquals(4.8f, net.getValue(1, 2), 1e-4);
            assertEquals(1.1f, net.getValue(2, 0), 1e-4);
        }

        @Test
        @DisplayName("DL export and import round-trip preserves all values and labels")
        public void testExportImportRoundTrip() throws Exception {
            Network orig = new Network(4);
            orig.setName("RoundTripDL");
            orig.getActor(0).setName("P1");
            orig.getActor(1).setName("P2");
            orig.getActor(2).setName("P3");
            orig.getActor(3).setName("P4");

            orig.setValue(1.23f, 0, 1);
            orig.setValue(4.56f, 1, 2);
            orig.setValue(7.89f, 2, 3);

            File tmp = Files.createTempFile("agna_dl_", ".dl").toFile();
            tmp.deleteOnExit();

            DLExporter.exportDL(orig, tmp);
            Network read = DLImporter.importDL(tmp);

            assertEquals(4, read.getSize());
            assertEquals("P1", read.getActor(0).getName());
            assertEquals("P4", read.getActor(3).getName());
            assertEquals(1.23f, read.getValue(0, 1), 1e-2);
            assertEquals(4.56f, read.getValue(1, 2), 1e-2);
            assertEquals(7.89f, read.getValue(2, 3), 1e-2);
            assertEquals(0.0f, read.getValue(0, 3), 1e-4);
        }
    }

    // =========================================================================
    // 7. COMPOSITE BATTERY REPORTS & FULL ANALYSIS SUITE
    // =========================================================================
    @Nested
    @DisplayName("7. Battery Dialogues & Full Analysis Suite")
    class CompositeBatteryTests {

        private Network net;

        @BeforeEach
        public void initNet() {
            net = new Network(4);
            net.setName("BatteryNet");
            net.getActor(0).setName("A");
            net.getActor(1).setName("B");
            net.getActor(2).setName("C");
            net.getActor(3).setName("D");

            net.setValue(1.0f, 0, 1);
            net.setValue(1.0f, 1, 0);
            net.setValue(1.0f, 1, 2);
            net.setValue(1.0f, 2, 1);
            net.setValue(1.0f, 0, 2);
            net.setValue(1.0f, 2, 0);
            net.setValue(1.0f, 2, 3);
            net.setValue(1.0f, 3, 2);
        }

        @Test
        @DisplayName("Centrality Battery incorporates Freeman centralization table")
        public void testCentralityBatteryWithFreemanTable() {
            CentralityOptions opts = CentralityOptions.createStandard();
            AnalysisReport rep = AnalysisReportFactory.createCentralityBatteryReport(net, lib, opts);
            assertNotNull(rep);
            String html = rep.toHtmlReport();

            assertTrue(html.contains("Freeman Network Centralization Indices"));
            assertTrue(html.contains("Freeman Degree Centralization (C_D)"));
            assertTrue(html.contains("Freeman Betweenness Centralization (C_B)"));
            assertTrue(html.contains("Freeman Closeness Centralization (C_C)"));
        }

        @Test
        @DisplayName("Clustering Battery includes Community detection and Equivalence")
        public void testClusteringBatteryExtensions() {
            ClusteringOptions opts = ClusteringOptions.createAll();
            AnalysisReport rep = AnalysisReportFactory.createClusteringBatteryReport(net, lib, opts);
            assertNotNull(rep);
            String html = rep.toHtmlReport();

            assertTrue(html.contains("Community Subgraph Summary"));
            assertTrue(html.contains("Blockmodel Image Matrix"));
        }

        @Test
        @DisplayName("Sociometrics Battery includes Assortativity and Weighted metrics")
        public void testSociometricsBatteryExtensions() {
            SociometricsOptions opts = SociometricsOptions.createAll();
            AnalysisReport rep = AnalysisReportFactory.createSociometricsBatteryReport(net, lib, opts);
            assertNotNull(rep);
            String html = rep.toHtmlReport();

            assertTrue(html.contains("Degree Assortativity Coefficients"));
            assertTrue(html.contains("Node Strengths &amp; Weighted Clustering"));
        }

        @Test
        @DisplayName("Full Analysis Battery compiles all active macro and micro modules")
        public void testFullAnalysisAllModules() {
            FullAnalysisOptions opts = FullAnalysisOptions.createAll();
            AnalysisReport rep = AnalysisReportFactory.createFullAnalysisReport(net, lib, opts, "legacy text");
            assertNotNull(rep);
            String html = rep.toHtmlReport();

            assertTrue(html.contains("FULL ANALYSIS Battery"));
            assertTrue(html.contains("Freeman Network Centralization Indices"));
            assertTrue(html.contains("Degree Assortativity Coefficients"));
            assertTrue(html.contains("Community Subgraph Summary"));
            assertTrue(html.contains("Blockmodel Image Matrix"));
            assertTrue(html.contains("Node Strengths &amp; Weighted Clustering"));
            assertFalse(rep.getTables().isEmpty());
        }
    }

    // =========================================================================
    // 8. METAMORPHIC & INVARIANCE SUITE
    // =========================================================================
    @Nested
    @DisplayName("8. Metamorphic & Invariance Suite")
    class MetamorphicInvarianceTests {

        @Test
        @DisplayName("Symmetrizing a symmetric graph is an invariant identity operation")
        public void testSymmetrizeIdentityOnSymmetricGraph() {
            Network net = new Network(3);
            net.setValue(1.0f, 0, 1); net.setValue(1.0f, 1, 0);
            net.setValue(1.0f, 1, 2); net.setValue(1.0f, 2, 1);

            for (SymmetrizeMode mode : SymmetrizeMode.values()) {
                Network copy = net.copy();
                NetworkTransformService.symmetrize(copy, mode);
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        float expected = (mode == SymmetrizeMode.SUM) ? 2.0f * net.getValue(i, j) : net.getValue(i, j);
                        assertEquals(expected, copy.getValue(i, j), 1e-5, "Mismatch in mode: " + mode);
                    }
                }
            }
        }

        @Test
        @DisplayName("Relabeling node order preserves graph-level modularity Q and assortativity r")
        public void testRelabelingInvariance() {
            // Original graph: 4 nodes with kite topology
            Network orig = new Network(4);
            orig.setValue(1.0f, 0, 1); orig.setValue(1.0f, 1, 0);
            orig.setValue(1.0f, 1, 2); orig.setValue(1.0f, 2, 1);
            orig.setValue(1.0f, 2, 3); orig.setValue(1.0f, 3, 2);

            // Permuted graph: swap node 0 and node 3
            Network perm = new Network(4);
            int[] p = {3, 1, 2, 0};
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    perm.setValue(orig.getValue(i, j), p[i], p[j]);
                }
            }

            AssortativityResult rOrig = DegreeAssortativity.compute(orig);
            AssortativityResult rPerm = DegreeAssortativity.compute(perm);
            assertEquals(rOrig.totalDegreeAssortativity, rPerm.totalDegreeAssortativity, 1e-4);

            CommunityResult qOrig = CommunityDetectionService.detectLouvain(orig);
            CommunityResult qPerm = CommunityDetectionService.detectLouvain(perm);
            assertEquals(qOrig.modularityQ, qPerm.modularityQ, 1e-4);
        }
    }
}
