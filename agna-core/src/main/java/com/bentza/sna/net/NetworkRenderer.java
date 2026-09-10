package com.bentza.sna.net;

import com.bentza.sna.AgnaLog;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * 2.1.3: headless network visualisation. Applies a layout and paints the
 * same viewer canvas (NodeArea) into an image file - PNG for now - so the
 * command-line output carries the same visual language as the desktop.
 */
public class NetworkRenderer
    {
    private NetworkRenderer()
        {
        }

    public static boolean renderToImage(FullNet full, File outPng,
            int width, int height, int layout, boolean showLabels)
        {
        try
            {
            Network net = full.getNetwork();
            NetworkLayouts.apply(net, layout, width, height);

            NodeArea area = new NodeArea();
            area.setSize(width, height);
            area.print_names = showLabels;
            area.updateArea(net);

            BufferedImage img = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            area.paint(g);
            g.dispose();
            ImageIO.write(img, "png", outPng);
            return true;
            } catch (Exception e)
            {
            AgnaLog.warn("render failed: " + e);
            return false;
            }
        }
    }
