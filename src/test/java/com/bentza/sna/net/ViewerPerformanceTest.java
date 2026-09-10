package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the viewer's area must keep up with large networks - build and
 * repaint the 200-node network repeatedly within generous time bounds,
 * so an accidental O(n^3) regression cannot slip through silently.
 */
public class ViewerPerformanceTest
    {
    private static String readSample(String name) throws Exception
        {
        return new String(Files.readAllBytes(new File(
                "samples" + File.separator + name).toPath()),
                StandardCharsets.ISO_8859_1);
        }

    @Test
    public void largeNetworkBuildsAndRepaintsWithinBounds()
        throws Exception
        {
        FullNet full = new FullNet();
        full.readNetwork(readSample("200.agn"), "agn");
        assertTrue(full.getNetwork().getSize() >= 200);

        full.net_area = new NodeArea();
        full.net_area.setSize(900, 700);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            {
            full.net_area.updateArea(full.getNetwork());
            // paint through the real component paint path (paintEdges
            // needs the area's graphics, which painting initialises)
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                    900, 700, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            full.net_area.paint(g);
            g.dispose();
            }
        long elapsed = System.currentTimeMillis() - start;
        assertEquals(full.getNetwork().getSize(),
                full.net_area.my_nodes.length);
        assertTrue(elapsed < 30000,
                "20 build+repaint cycles on 200 nodes took " + elapsed
                        + " ms");
        }
    }