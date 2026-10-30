/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.community;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.net.Network;
import org.junit.jupiter.api.Test;

public class CommunityDetectionTest {

    @Test
    public void testTwoCliquesLouvainPartition() {
        // Two disjoint triangles joined by a single weak edge: 0-1-2 and 3-4-5, bridge 2-3
        Network net = new Network(6);
        // Triangle 1
        net.setValue(1.0f, 0, 1); net.setValue(1.0f, 1, 0);
        net.setValue(1.0f, 1, 2); net.setValue(1.0f, 2, 1);
        net.setValue(1.0f, 0, 2); net.setValue(1.0f, 2, 0);
        // Triangle 2
        net.setValue(1.0f, 3, 4); net.setValue(1.0f, 4, 3);
        net.setValue(1.0f, 4, 5); net.setValue(1.0f, 5, 4);
        net.setValue(1.0f, 3, 5); net.setValue(1.0f, 5, 3);
        // Bridge
        net.setValue(1.0f, 2, 3); net.setValue(1.0f, 3, 2);

        CommunityDetectionService.CommunityResult res = CommunityDetectionService.detectLouvain(net);
        assertEquals(2, res.communityCount);
        assertTrue(res.modularityQ > 0.3); // High modularity

        // Nodes 0, 1, 2 should share the same community
        assertEquals(res.nodeAssignments[0], res.nodeAssignments[1]);
        assertEquals(res.nodeAssignments[1], res.nodeAssignments[2]);

        // Nodes 3, 4, 5 should share the same community
        assertEquals(res.nodeAssignments[3], res.nodeAssignments[4]);
        assertEquals(res.nodeAssignments[4], res.nodeAssignments[5]);

        // The two groups should have different community IDs
        assertNotEquals(res.nodeAssignments[0], res.nodeAssignments[3]);
    }

    @Test
    public void testGirvanNewmanTwoCliques() {
        Network net = new Network(6);
        net.setValue(1.0f, 0, 1); net.setValue(1.0f, 1, 0);
        net.setValue(1.0f, 1, 2); net.setValue(1.0f, 2, 1);
        net.setValue(1.0f, 0, 2); net.setValue(1.0f, 2, 0);

        net.setValue(1.0f, 3, 4); net.setValue(1.0f, 4, 3);
        net.setValue(1.0f, 4, 5); net.setValue(1.0f, 5, 4);
        net.setValue(1.0f, 3, 5); net.setValue(1.0f, 5, 3);

        net.setValue(1.0f, 2, 3); net.setValue(1.0f, 3, 2);

        CommunityDetectionService.CommunityResult res = CommunityDetectionService.detectGirvanNewman(net);
        assertEquals(2, res.communityCount);
        assertTrue(res.modularityQ > 0.3);
        assertEquals(res.nodeAssignments[0], res.nodeAssignments[1]);
        assertEquals(res.nodeAssignments[3], res.nodeAssignments[4]);
        assertNotEquals(res.nodeAssignments[0], res.nodeAssignments[3]);
    }
}
