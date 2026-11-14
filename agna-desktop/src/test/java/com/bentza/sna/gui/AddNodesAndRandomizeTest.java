/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.net.Network;
import com.bentza.sna.report.CentralityOptions;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class AddNodesAndRandomizeTest {

    @Test
    public void testCentralityOptionsDefaultsIncludeBavelas() {
        CentralityOptions opts = new CentralityOptions();
        assertTrue(opts.isIncludeBavelas(), "Bavelas-Leavitt must be included by default in CentralityOptions");
        assertTrue(opts.isIncludeBetweenness());
        assertTrue(opts.isIncludeCloseness());

        CentralityOptions standard = CentralityOptions.createStandard();
        assertTrue(standard.isIncludeBavelas(), "Standard preset must have Bavelas-Leavitt checked by default");
    }

    @Test
    public void testAddNodesHelperConstants() {
        assertEquals(250, AddNodesHelper.LIMIT_WITHOUT_WARNING);
        assertEquals(500, AddNodesHelper.TOTAL_SIZE_WARNING_THRESHOLD);
        assertEquals(5000, AddNodesHelper.MAX_NODES);
    }

    @Test
    public void testRandomizeNetworkDialogInstantiates() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        Network net = new Network(5);
        net.setName("TestNetwork");

        AtomicReference<RandomizeNetworkDialog.RandomizeResult> ref = new AtomicReference<>();
        RandomizeNetworkDialog dlg = new RandomizeNetworkDialog(null, net, ref::set);
        assertNotNull(dlg);
        assertTrue(dlg.getTitle().contains("Populate"));
    }

    @Test
    public void testCentralityBatteryDialogBavelasDefault() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        Network net = new Network(4);
        net.setName("CentTest");

        AtomicReference<CentralityOptions> ref = new AtomicReference<>();
        CentralityBatteryDialog dlg = new CentralityBatteryDialog(null, net, ref::set);
        assertNotNull(dlg);
    }
}
