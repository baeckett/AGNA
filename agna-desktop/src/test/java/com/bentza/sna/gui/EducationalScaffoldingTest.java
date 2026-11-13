/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.gui.EducationalTooltips.MetricGuide;
import com.bentza.sna.net.FullNet;
import java.io.File;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class EducationalScaffoldingTest {

    @Test
    public void testEducationalTooltipsDictionary() {
        Map<String, MetricGuide> all = EducationalTooltips.getAll();
        assertNotNull(all);
        assertTrue(all.size() >= 25, "Expected at least 25 educational guides, found: " + all.size());

        MetricGuide betw = EducationalTooltips.get("betweenness");
        assertNotNull(betw);
        assertTrue(betw.title.contains("Betweenness"));
        assertTrue(betw.whyUseIt.contains("brokers") || betw.whyUseIt.contains("gatekeepers"));
        assertTrue(betw.toHtmlTooltip().startsWith("<html>"));
        assertTrue(betw.toHtmlTooltip().contains("What it measures"));

        MetricGuide comm = EducationalTooltips.get("communities");
        assertNotNull(comm);
        assertTrue(comm.title.contains("Community"));
        assertTrue(comm.toHtmlTooltip().contains("Louvain"));
    }

    @Test
    public void testSampleDatasetsParseAndLoad() throws Exception {
        String[] samples = {
            "Padgett Florentine Families.agn",
            "Zachary Karate Club.agn",
            "Davis Southern Women.agn",
            "10 star.agn",
            "10 circle.agn"
        };

        for (String sampleName : samples) {
            File f = new File("samples", sampleName);
            assertTrue(f.exists(), "Sample file missing: " + f.getAbsolutePath());
            FullNet full = new FullNet();
            full.readNetwork(java.nio.file.Files.readString(f.toPath()), "agn");
            assertNotNull(full.getNetwork(), "Network must parse: " + sampleName);
            assertTrue(full.getNetwork().getSize() > 0, "Network must have nodes: " + sampleName);
        }
    }

    @Test
    public void testHeadlessDialogCreation() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }

        SampleNetworksDialog sampleDlg = new SampleNetworksDialog(null, null);
        assertNotNull(sampleDlg);
        assertTrue(sampleDlg.getTitle().contains("Sample Networks"));

        AnalysisAdvisorDialog advisorDlg = new AnalysisAdvisorDialog(null, null);
        assertNotNull(advisorDlg);
        assertTrue(advisorDlg.getTitle().contains("Analysis Advisor"));

        SnaCheatSheetDialog cheatDlg = new SnaCheatSheetDialog(null);
        assertNotNull(cheatDlg);
        assertTrue(cheatDlg.getTitle().contains("Cheat-Sheet"));
    }
}
