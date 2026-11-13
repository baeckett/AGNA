/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JTextPane;

import org.junit.jupiter.api.Test;

import com.bentza.sna.io.IOUtils;

/**
 * 2.1.3: the Output pane content - plain log lines and html analysis
 * reports - must survive the save/reopen cycle without errors and with
 * its text intact.
 */
public class OutputRoundTripTest
    {
    @Test
    public void outputSavesAndReopensCleanly() throws Exception
        {
        AgnaTextPane pane = new AgnaTextPane();
        pane.appendString("> [12:00:00] Opened example2.agn (9 nodes).\n");
        pane.appendBlock("Some <b>analysis</b> output");
        pane.appendString("> [12:00:01] Transposed the sociomatrix.\n");

        File out = File.createTempFile("agna_output_rt", ".txt");
        assertTrue(MainFrame.saveOutput(pane, out.getAbsolutePath()),
                "saveOutput must succeed");

        // reading path used by the GUI's Open Output File
        JTextPane back = new JTextPane();
        back.read(IOUtils.reader(out), null);
        String text = back.getText();
        assertTrue(text.contains("Opened example2.agn"),
                "log line survived: " + text);
        assertTrue(text.contains("analysis"),
                "html report survived");
        assertTrue(text.contains("Transposed"),
                "second log line survived");
        out.delete();
        }

    @Test
    public void plainAndHtmlContentTypesBothRoundTrip() throws Exception
        {
        AgnaTextPane plain = new AgnaTextPane();
        plain.appendString("plain text line");
        File f1 = File.createTempFile("agna_out_plain", ".txt");
        assertTrue(MainFrame.saveOutput(plain, f1.getAbsolutePath()));
        String back1 = new String(Files.readAllBytes(f1.toPath()),
                StandardCharsets.ISO_8859_1);
        assertTrue(back1.contains("plain text line"));
        f1.delete();
        }

    @Test
    public void consecutiveAnalysesDoNotNestTables() throws Exception
        {
        AgnaTextPane pane = new AgnaTextPane();
        pane.setContentType("text/html");
        pane.clearAll();

        String report1 = "<div style='font-family:sans-serif;'>"
                + "<table border='1'><tr><th>Actor</th><th>Clustering</th></tr><tr><td>1</td><td>0.5</td></tr></table>"
                + "<table style='background:#fafbfc;'>"
                + "  <tr><td><b>Notes / Methodological Remarks</b></td></tr>"
                + "  <tr><td>a. Note 1</td></tr>"
                + "</table>"
                + "</div>";
        pane.appendBlock(report1);

        String report2 = "<div style='font-family:sans-serif;'>"
                + "<table style='border-bottom:2px solid #004080;'><tr><td><b>AGNA Analysis: Connectivity & Articulation Points</b></td></tr></table>"
                + "<table border='1'><tr><th>Component</th><th>Nodes</th></tr><tr><td>1</td><td>4</td></tr></table>"
                + "</div>";
        pane.appendParagraph(report2);

        String fullHtml = pane.getText();
        int r1NotesIdx = fullHtml.indexOf("Notes / Methodological Remarks");
        int r1TableEnd = fullHtml.indexOf("</table>", r1NotesIdx);
        int r2Header = fullHtml.indexOf("AGNA Analysis: Connectivity");

        assertTrue(r1NotesIdx > 0, "Report 1 Notes must exist");
        assertTrue(r2Header > 0, "Report 2 Connectivity header must exist");
        assertTrue(r2Header > r1TableEnd, "Report 2 MUST be appended OUTSIDE Report 1's table, found r2Header=" + r2Header + " <= r1TableEnd=" + r1TableEnd);
        }
    }