package com.bentza.sna.net;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.core.AppRuntime;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Supplier;
import javax.imageio.ImageIO;

/**
 * 2.1.3: headless network visualisation. Applies a layout and paints the
 * same viewer canvas (NodeArea) into an image file - PNG for now - so the
 * command-line output carries the same visual language as the desktop.
 */
public class NetworkRenderer
    {
    /** render overrides; null fields fall back to the viewer defaults */
    public static final class RenderOptions
        {
        public boolean labels;
        public Color background;
        public Boolean facesVisible;
        public Boolean edgeValues;
        }

    private NetworkRenderer()
        {
        }

    public static boolean renderToImage(FullNet full, File outPng,
            int width, int height, int layout, boolean showLabels)
        {
        RenderOptions opts = new RenderOptions();
        opts.labels = showLabels;
        return renderToImage(full, outPng, width, height, layout, opts);
        }

    public static boolean renderToImage(FullNet full, File outPng,
            int width, int height, int layout, RenderOptions opts)
        {
        // the NodeArea constructor and paintEdges consult the
        // AppRuntime current network; save whatever the caller had and
        // restore it afterwards
        Supplier<FullNet> prevFull = AppRuntime.currentFullNetSupplier();
        Supplier<Network> prevNet = AppRuntime.currentNetworkSupplier();
        AppRuntime.setCurrentFullNet(full);
        AppRuntime.setCurrentNetwork(full.getNetwork());
        try
            {
            Network net = full.getNetwork();
            NetworkLayouts.apply(net, layout, width, height);

            NodeArea area = new NodeArea(net);
            area.setSize(width, height);
            // the viewer models a square canvas internally; render on
            // that square, then scale to the requested dimensions
            area.setWidthSimply(Math.max(width, height));
            area.print_names = opts.labels;
            if (opts.background != null)
                {
                area.setBackgroundColor(opts.background);
                }
            if (opts.facesVisible != null)
                {
                area.setFacesVisible(opts.facesVisible.booleanValue());
                }
            if (opts.edgeValues != null)
                {
                area.setEdgeValueVisible(opts.edgeValues.booleanValue());
                }
            area.updateArea(net);
            // the viewer paints into an offscreen edges image first;
            // without this the render would be an empty viewport
            area.paintEdges();

            BufferedImage square = new BufferedImage(area.area_width,
                    area.area_width, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = square.createGraphics();
            area.paint(g);
            g.dispose();

            BufferedImage img = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(square, 0, 0, width, height, null);
            g2.dispose();
            ImageIO.write(img, "png", outPng);
            return true;
            } catch (Exception e)
            {
            AgnaLog.warn("render failed: " + e);
            return false;
            } finally
            {
            AppRuntime.setCurrentFullNetSupplier(prevFull);
            AppRuntime.setCurrentNetworkSupplier(prevNet);
            }
        }
    }
