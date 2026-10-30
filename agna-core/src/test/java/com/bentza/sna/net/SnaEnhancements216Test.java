/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import com.bentza.sna.capital.StructuralHoles;
import com.bentza.sna.census.DyadCensus;
import com.bentza.sna.census.TriadCensus;
import com.bentza.sna.clustering.ClusteringMetrics;
import com.bentza.sna.clustering.KCoreDecomposition;
import com.bentza.sna.connectivity.ConnectivityAnalysis;
import com.bentza.sna.spectral.SpectralCentrality;
import com.bentza.sna.transform.NetworkTransformService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SnaEnhancements216Test {

    @Test
    public void testWattsStrogatzClusteringAndTransitivity() {
        // Complete graph K_4: all local clusterings must be 1.0, transitivity must be 1.0
        Network k4 = NetworkGenerator.createFullBinary(4);
        ClusteringMetrics.LocalClusteringSummary summary = ClusteringMetrics.computeLocalClustering(k4);
        assertEquals(1.0, summary.meanClustering, 1e-4);
        assertEquals(4, summary.totalTriangles);
        for (ClusteringMetrics.LocalClusteringResult r : summary.actorResults) {
            assertEquals(1.0, r.clusteringCoefficient, 1e-4);
            assertEquals(3, r.degree);
        }

        ClusteringMetrics.TransitivityResult trans = ClusteringMetrics.computeTransitivity(k4);
        assertEquals(1.0, trans.transitivity, 1e-4);
        assertEquals(4, trans.totalTriangles);

        // Star graph with 5 nodes: center has 4 neighbors, zero triangles; leaves have 1 neighbor, C=0.
        Network star = NetworkGenerator.createStar(5, true);
        ClusteringMetrics.LocalClusteringSummary starSummary = ClusteringMetrics.computeLocalClustering(star);
        assertEquals(0.0, starSummary.meanClustering, 1e-4);
        assertEquals(0, starSummary.totalTriangles);
    }

    @Test
    public void testKCoreDecomposition() {
        // Triangle (0, 1, 2) connected via bridge (2-3) to a tree tail (3-4)
        Network net = new Network(5);
        net.setName("CoreNet");
        net.setValue(1f, 0, 1); net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2); net.setValue(1f, 2, 1);
        net.setValue(1f, 0, 2); net.setValue(1f, 2, 0);
        net.setValue(1f, 2, 3); net.setValue(1f, 3, 2);
        net.setValue(1f, 3, 4); net.setValue(1f, 4, 3);

        KCoreDecomposition.KCoreResult res = KCoreDecomposition.decompose(net);
        assertEquals(2, res.maxCore);
        // Triangle nodes have coreness 2
        assertEquals(2, res.coreness[0]);
        assertEquals(2, res.coreness[1]);
        assertEquals(2, res.coreness[2]);
        // Bridge node 3 and leaf 4 have coreness 1
        assertEquals(1, res.coreness[3]);
        assertEquals(1, res.coreness[4]);
    }

    @Test
    public void testDyadAndTriadCensus() {
        // 3 nodes: 0 -> 1 -> 2 and 0 -> 2. This is a classic Transitive Triple (030T)
        Network net = new Network(3);
        net.setName("TriadNet");
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 0, 2);

        DyadCensus dyads = DyadCensus.compute(net);
        assertEquals(0, dyads.mutual);
        assertEquals(3, dyads.asymmetric);
        assertEquals(0, dyads.nullDyads);
        assertEquals(0.0, dyads.reciprocityRate, 1e-4);

        TriadCensus triads = TriadCensus.compute(net);
        assertEquals(1, triads.totalTriads);
        assertEquals(1, triads.getCount(TriadCensus.TriadType.T030T));
        assertEquals(0, triads.getCount(TriadCensus.TriadType.T003));
        assertEquals(1.0, triads.transitivityRatio, 1e-4);

        // Cyclic Triple: 0 -> 1 -> 2 -> 0 (030C)
        Network cyclic = new Network(3);
        cyclic.setName("Cycle");
        cyclic.setValue(1f, 0, 1);
        cyclic.setValue(1f, 1, 2);
        cyclic.setValue(1f, 2, 0);
        TriadCensus cycleTriads = TriadCensus.compute(cyclic);
        assertEquals(1, cycleTriads.getCount(TriadCensus.TriadType.T030C));
        assertEquals(0, cycleTriads.getCount(TriadCensus.TriadType.T030T));
    }

    @Test
    public void testStructuralHoles() {
        // Star graph on 5 nodes: center node 0 connects to 4 independent alters.
        // Node 0 has high effective size and low constraint; leaves have high constraint (1.0).
        Network star = NetworkGenerator.createStar(5, true);
        StructuralHoles holes = StructuralHoles.compute(star);

        assertEquals(5, holes.actorMetrics.length);
        StructuralHoles.ActorHolesMetrics center = holes.actorMetrics[0];
        assertEquals(4, center.degree);
        assertEquals(4.0, center.effectiveSize, 1e-3);
        assertEquals(1.0, center.efficiency, 1e-3);
        // Constraint on center with 4 independent alters is 4 * (1/4)^2 = 0.25
        assertEquals(0.25, center.constraint, 1e-3);

        // Leaves have 1 alter, so constraint is 1.0
        for (int i = 1; i < 5; i++) {
            assertEquals(1.0, holes.actorMetrics[i].constraint, 1e-3);
        }
    }

    @Test
    public void testConnectivityAnalysis() {
        // Disconnected network with two components:
        // Comp 1: Barbell graph (0-1-2 with 1 as cut vertex)
        // Comp 2: Isolated pair (3-4)
        Network net = new Network(5);
        net.setName("Disconn");
        net.setValue(1f, 0, 1); net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2); net.setValue(1f, 2, 1);
        net.setValue(1f, 3, 4); net.setValue(1f, 4, 3);

        ConnectivityAnalysis conn = ConnectivityAnalysis.analyze(net);
        assertEquals(2, conn.weakComponents.size());
        assertEquals(3, conn.giantComponentSize);
        assertEquals(0.6, conn.giantComponentFraction, 1e-4);

        // Node 1 is an articulation point in component 1
        assertTrue(conn.articulationPoints.contains(1));
        assertFalse(conn.articulationPoints.contains(0));
        assertFalse(conn.articulationPoints.contains(2));

        // Bridges: 0-1, 1-2, 3-4
        assertEquals(3, conn.bridges.size());
    }

    @Test
    public void testSpectralCentralities() {
        Network star = NetworkGenerator.createStar(5, true);
        double[] ev = SpectralCentrality.computeEigenvector(star, 100, 1e-6);
        // Center node must have maximum eigenvector score (1.0)
        assertEquals(1.0, ev[0], 1e-4);
        for (int i = 1; i < 5; i++) {
            assertTrue(ev[i] < ev[0], "Periphery score must be less than center");
        }

        double[] pr = SpectralCentrality.computePageRank(star, 0.85, 100);
        assertEquals(5, pr.length);
        assertTrue(pr[0] > pr[1], "Center node PageRank must exceed leaf");
    }

    @Test
    public void testNetworkTransformations() {
        // Asymmetric directed network
        Network net = new Network(3);
        net.setName("Asym");
        net.setValue(2.0f, 0, 1);
        net.setValue(4.0f, 1, 0);
        net.setValue(1.0f, 1, 2);
        net.setValue(5.0f, 0, 0); // self loop

        // 1. Zero diagonal
        NetworkTransformService.zeroDiagonal(net);
        assertEquals(0.0f, net.getValue(0, 0));

        // 2. Symmetrize MAX
        NetworkTransformService.symmetrize(net, NetworkTransformService.SymmetrizeMode.MAX);
        assertEquals(4.0f, net.getValue(0, 1));
        assertEquals(4.0f, net.getValue(1, 0));

        // 3. Symmetrize MIN
        net.setValue(2.0f, 0, 1);
        net.setValue(6.0f, 1, 0);
        NetworkTransformService.symmetrize(net, NetworkTransformService.SymmetrizeMode.MIN);
        assertEquals(2.0f, net.getValue(0, 1));
        assertEquals(2.0f, net.getValue(1, 0));

        // 4. Dichotomize with threshold 2.0
        NetworkTransformService.dichotomize(net, 2.0f, true);
        assertEquals(1.0f, net.getValue(0, 1));

        // 5. Ego-network extraction
        Network star = NetworkGenerator.createStar(5, true);
        Network ego = NetworkTransformService.extractEgoNetwork(star, 0, 1, true);
        assertEquals(5, ego.getSize());
    }
}
