/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.bentza.sna.report.FullAnalysisOptions;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class FullAnalysisDialogTest {
    @Test
    public void dialogInstantiatesInHeadlessOrGui() throws Exception {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }

        FullNet full = new FullNet();
        full.createDefaultNetwork(4);
        Network net = full.getNetwork();
        net.setName("TestNetwork");

        AtomicReference<FullAnalysisOptions> captured = new AtomicReference<>();
        FullAnalysisDialog dlg = new FullAnalysisDialog(null, net, captured::set);

        assertNotNull(dlg);
        assertTrue(dlg.getTitle().contains("Full Analysis Battery"));
    }
}
