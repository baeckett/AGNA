/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.net.Network;
import org.junit.jupiter.api.Test;

public class WeightedMetricsTest {

    @Test
    public void testWeightedStrengthsAndDensity() {
        Network net = new Network(3);
        net.setValue(2.0f, 0, 1);
        net.setValue(3.0f, 1, 2);
        net.setValue(4.0f, 2, 0);

        WeightedMetrics.WeightedSummary sum = WeightedMetrics.compute(net);
        assertEquals(3, sum.nodeCount);

        // Node 0: out-strength = 2, in-strength = 4 -> total = 6
        assertEquals(2.0f, sum.outStrength[0], 1e-4);
        assertEquals(4.0f, sum.inStrength[0], 1e-4);
        assertEquals(6.0f, sum.totalStrength[0], 1e-4);

        // Binary density: 3 ties out of 6 possible = 0.5
        assertEquals(0.5, sum.binaryDensity, 1e-4);
        // Valued density: (2 + 3 + 4) / 6 = 1.5
        assertEquals(1.5, sum.valuedDensity, 1e-4);
        // Average tie weight: (2 + 3 + 4) / 3 = 3.0
        assertEquals(3.0, sum.meanTieWeight, 1e-4);
    }
}
