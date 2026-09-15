/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for the 2.1.3 getMin/getMax fix: an all-zero network used
 * to yield +Infinity/-Infinity (breaking the viewer's transparency mapping);
 * now it yields 0f. Also verifies the values of a known matrix.
 */
public class NetworkExtremesTest
    {
    private static final float[][] SAMPLE_4 = new float[][] { { 0f, 2f, 3f, 4f },
            { 5f, 0f, 7f, 8f }, { 9f, 10f, 0f, 12f },
            { 13f, 14f, 15f, 0f } };

    @Test
    public void allZeroNetworkHasZeroExtremes()
        {
        Network net = new Network(3);
        assertEquals(0f, net.getMin());
        assertEquals(0f, net.getMax());
        }

    @Test
    public void knownMatrixExtremes()
        {
        Network net = new Network(SAMPLE_4);
        assertEquals(2f, net.getMin()); // smallest non-zero value
        assertEquals(15f, net.getMax());
        }

    @Test
    public void afterDeletionExtremesFollowTheMatrix()
        {
        Network net = new Network(SAMPLE_4);
        net.deleteActor(3); // node 3 (with values 13,14,15) is gone
        assertEquals(2f, net.getMin());
        assertEquals(10f, net.getMax());
        }
    }