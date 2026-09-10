package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JEditorPane;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the help contents and the section pages must actually render
 * through the same component the Help window uses (JEditorPane, text/html)
 * - painted into an image buffer and verified non-blank, so a broken page
 * can never ship silently.
 */
public class HelpRenderingTest
    {
    private static String read(String name) throws Exception
        {
        return new String(Files.readAllBytes(new File(
                "src/main/resources/help" + File.separator + name).toPath()),
                StandardCharsets.UTF_8);
        }

    private static int paintedPixels(JEditorPane pane)
        {
        pane.setSize(640, 480);
        BufferedImage img = new BufferedImage(640, 480,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        pane.paint(g);
        g.dispose();
        int painted = 0;
        for (int y = 0; y < 480; y++)
            for (int x = 0; x < 640; x++)
                {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int gr = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r < 245 || gr < 245 || b < 245)
                    {
                    painted++;
                    }
                }
        return painted;
        }

    @Test
    public void helpContentsRendersNonBlank() throws Exception
        {
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setText(read("help_contents.htm"));
        assertTrue(paintedPixels(pane) > 5000,
                "the contents page must actually render text");
        }

    @Test
    public void sectionPagesRenderNonBlank() throws Exception
        {
        String[] pages = { "3menureference.htm",
                "4methodologywhatexactlyiscomputedreadbeforepublishingresults.htm",
                "5matrixoperationsformulas.htm", "6fileformats.htm" };
        for (String page : pages)
            {
            JEditorPane pane = new JEditorPane();
            pane.setContentType("text/html");
            pane.setText(read(page));
            assertTrue(paintedPixels(pane) > 2000,
                    page + " must render content");
            }
        }

    @Test
    public void gridRendersWithSelectionContrast() throws Exception
        {
        AgnaTable grid = new AgnaTable();
        grid.setModel(new javax.swing.table.DefaultTableModel(6, 6));
        grid.setRowHeight(20);
        grid.setSize(140, 140);
        grid.changeSelection(1, 1, false, false);
        grid.changeSelection(4, 4, true, true);
        BufferedImage img = new BufferedImage(140, 140,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        grid.paint(g);
        g.dispose();
        int selected = img.getRGB(45, 45);          // inside selection
        int unselected = img.getRGB(5, 5);          // outside selection
        assertTrue(selected != unselected,
                "the selected range must be visibly different");
        assertTrue(img.getRGB(5, 21) != img.getRGB(5, 10),
                "grid lines must be visible between rows");
        }
    }