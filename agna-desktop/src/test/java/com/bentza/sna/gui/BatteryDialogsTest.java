/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.bentza.sna.report.CentralityOptions;
import com.bentza.sna.report.ClusteringOptions;
import com.bentza.sna.report.DistanceOptions;
import com.bentza.sna.report.SociometricsOptions;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class BatteryDialogsTest {

    @Test
    public void testBatteryDialogsInstantiate() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }

        FullNet full = new FullNet();
        full.createDefaultNetwork(4);
        Network net = full.getNetwork();
        net.setName("DialogTestNet");

        // SymmetrizeDialog
        SymmetrizeDialog symDlg = new SymmetrizeDialog(null, net, null);
        assertNotNull(symDlg);
        assertTrue(symDlg.getTitle().contains("Symmetrize"));

        // CentralityBatteryDialog
        AtomicReference<CentralityOptions> centRef = new AtomicReference<>();
        CentralityBatteryDialog centDlg = new CentralityBatteryDialog(null, net, centRef::set);
        assertNotNull(centDlg);
        assertTrue(centDlg.getTitle().contains("Centrality"));

        // DistanceBatteryDialog
        AtomicReference<DistanceOptions> distRef = new AtomicReference<>();
        DistanceBatteryDialog distDlg = new DistanceBatteryDialog(null, net, distRef::set);
        assertNotNull(distDlg);
        assertTrue(distDlg.getTitle().contains("Distance"));

        // ClusteringBatteryDialog
        AtomicReference<ClusteringOptions> clustRef = new AtomicReference<>();
        ClusteringBatteryDialog clustDlg = new ClusteringBatteryDialog(null, net, clustRef::set);
        assertNotNull(clustDlg);
        assertTrue(clustDlg.getTitle().contains("Clustering"));

        // SociometricsBatteryDialog
        AtomicReference<SociometricsOptions> socRef = new AtomicReference<>();
        SociometricsBatteryDialog socDlg = new SociometricsBatteryDialog(null, net, socRef::set);
        assertNotNull(socDlg);
        assertTrue(socDlg.getTitle().contains("Sociometrics"));

        // FullAnalysisDialog with new modules
        AtomicReference<com.bentza.sna.report.FullAnalysisOptions> fullRef = new AtomicReference<>();
        FullAnalysisDialog fullDlg = new FullAnalysisDialog(null, net, fullRef::set);
        assertNotNull(fullDlg);
        assertTrue(fullDlg.getTitle().contains("Full Analysis"));
    }
}
