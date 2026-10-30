/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class HypothesisTestingDialogTest
    {
    @Test
    public void dialogInstantiatesAndWiresOutput() throws Exception
        {
        if (java.awt.GraphicsEnvironment.isHeadless())
            {
            // Skip GUI instantiation in headless runner
            return;
            }

        FullNet full = new FullNet();
        full.createDefaultNetwork(6);
        Network net = full.getNetwork();
        net.setName("TestRing");
        for (int i = 0; i < 6; i++)
            {
            net.setValue(1.0f, i, (i + 1) % 6);
            net.setValue(1.0f, (i + 1) % 6, i);
            }

        AtomicReference<String> outputCapture = new AtomicReference<>();
        HypothesisTestingDialog dlg = new HypothesisTestingDialog(null, net, outputCapture::set);
        assertNotNull(dlg);
        assertTrue(dlg.getTitle().contains("Hypothesis Testing"));
        }
    }
