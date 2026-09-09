package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.FullNet;

/**
 * 2.1.3: the FlatLaf dependency must be bundled, the look-and-feel
 * preference must round-trip through the settings parser, and the
 * sociomatrix grid lines must be visible under every look and feel —
 * drawn by the table itself when the look and feel refuses to.
 */
public class LookAndFeelTest
    {
    @Test
    public void flatlafClassesAreOnTheClasspath() throws Exception
        {
        assertNotNull(Class.forName("com.formdev.flatlaf.FlatDarkLaf"));
        assertNotNull(Class.forName("com.formdev.flatlaf.FlatLightLaf"));
        }

    @Test
    public void lookAndFeelPreferenceRoundTrips()
        {
        String settings = "Look And Feel\tflatlaf\n"
                + "Pajek Vectors Enabled\tyes\n"
                + "Default Export Format\t3\n";
        FullNet.parseAgnaNonGraphicDefaultSettings(settings);
        assertEquals("flatlaf", MainFrame.getLookAndFeel());
        }

    @Test
    public void gridLinesAreForcedVisibleInEveryLookAndFeel()
        {
        MainFrame.setLookAndFeel("flatlaf");
        MainFrame.setNativeLookAndFeel();
        assertEquals(Boolean.TRUE,
                javax.swing.UIManager.get("Table.showHorizontalLines"));
        assertEquals(Boolean.TRUE,
                javax.swing.UIManager.get("Table.showVerticalLines"));
        }

    @Test
    public void gridLinesAreDrawnByTheTableItself()
        {
        AgnaTable grid = new AgnaTable();
        grid.setModel(new javax.swing.table.DefaultTableModel(3, 3));
        grid.setRowHeight(20);
        // the look and feel would not paint any grid here:
        grid.setShowGrid(false);
        grid.setSize(120, 80);
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                120, 80, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        grid.paint(g);
        g.dispose();
        int boundary = img.getRGB(5, grid.getCellRect(1, 0, true).y);
        int center = img.getRGB(5, grid.getCellRect(0, 0, true).y + 5);
        assertNotEquals(center, boundary);
        }
    }
