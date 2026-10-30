/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.equivalence;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.net.Network;
import org.junit.jupiter.api.Test;

public class StructuralEquivalenceTest {

    @Test
    public void testIdenticalRolesHaveZeroDistance() {
        // Complete bipartite graph K_{2,2}: nodes 0 and 1 connect to 2 and 3; nodes 2 and 3 connect to 0 and 1
        Network k22 = new Network(4);
        k22.setValue(1.0f, 0, 2); k22.setValue(1.0f, 2, 0);
        k22.setValue(1.0f, 0, 3); k22.setValue(1.0f, 3, 0);
        k22.setValue(1.0f, 1, 2); k22.setValue(1.0f, 2, 1);
        k22.setValue(1.0f, 1, 3); k22.setValue(1.0f, 3, 1);

        StructuralEquivalenceService.EquivalenceResult res = StructuralEquivalenceService.analyze(k22, 2);
        assertEquals(2, res.blockCount);

        // Nodes 0 and 1 are structurally equivalent -> Euclidean distance is 0
        assertEquals(0.0, res.euclideanDistance[0][1], 1e-6);
        // Nodes 2 and 3 are structurally equivalent -> Euclidean distance is 0
        assertEquals(0.0, res.euclideanDistance[2][3], 1e-6);

        // Nodes 0 and 1 belong to the same block
        assertEquals(res.blockAssignments[0], res.blockAssignments[1]);
        // Nodes 2 and 3 belong to the same block
        assertEquals(res.blockAssignments[2], res.blockAssignments[3]);
        // But different from each other
        assertNotEquals(res.blockAssignments[0], res.blockAssignments[2]);

        // Cross-block density between block 0 and block 1 should be 1.0 (all bipartite ties exist)
        int bA = res.blockAssignments[0];
        int bB = res.blockAssignments[2];
        assertEquals(1.0, res.blockmodelDensity[bA][bB], 1e-6);
        assertEquals(1.0, res.blockmodelDensity[bB][bA], 1e-6);
        // Internal block densities should be 0.0 (no ties within blocks)
        assertEquals(0.0, res.blockmodelDensity[bA][bA], 1e-6);
        assertEquals(0.0, res.blockmodelDensity[bB][bB], 1e-6);

        // Identical structural roles have correlation 1.0
        assertEquals(1.0, res.pearsonCorrelation[0][1], 1e-6);
        assertEquals(1.0, res.pearsonCorrelation[2][3], 1e-6);
    }
}
