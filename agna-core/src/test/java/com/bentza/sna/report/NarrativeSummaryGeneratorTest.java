/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import org.junit.jupiter.api.Test;

public class NarrativeSummaryGeneratorTest {

    @Test
    public void testNarrativeSummaryStarGraph() {
        AgnaLib lib = new AgnaLib();
        Network star = new Network(10, 1); // 10-star
        star.setName("10 Star Benchmark");

        String summary = NarrativeSummaryGenerator.generateSummary(star, lib);
        assertNotNull(summary);
        assertTrue(summary.contains("Network Scale & Density"));
        assertTrue(summary.contains("Single connected component") || summary.contains("single connected component"));
        assertTrue(summary.contains("Degree centralization is <b>1.000</b>") || summary.contains("high star-like concentration"));
        assertTrue(summary.contains("Suggested Next Steps"));
    }

    @Test
    public void testNarrativeSummaryDisconnectedGraph() {
        AgnaLib lib = new AgnaLib();
        Network net = new Network(6); // empty / isolates
        net.setName("Isolates Net");

        String summary = NarrativeSummaryGenerator.generateSummary(net, lib);
        assertNotNull(summary);
        assertTrue(summary.contains("disconnected"));
        assertTrue(summary.contains("sparse"));
    }
}
