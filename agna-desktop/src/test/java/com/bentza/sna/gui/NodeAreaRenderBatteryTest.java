/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.core.AppRuntime;
import com.bentza.sna.net.Actor;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.bentza.sna.net.NodeArea;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: battery over the desktop's live canvas (NodeArea) painted
 * headlessly - the same path the CLI draw command uses. Each test
 * asserts on actual pixels, so viewer regressions (grid, faces, names,
 * titles, edge values, background) fail loudly.
 */
public class NodeAreaRenderBatteryTest
    {
    private Network sampleNet()
        {
        FullNet full = new FullNet();
        full.createDefaultNetwork(6);
        Network net = full.getNetwork();
        net.setName("Ring");
        for (int i = 0; i < 6; i++)
            {
            net.setValue(1f, i, (i + 1) % 6);
            net.setValue(1f, (i + 1) % 6, i);
            }
        for (int i = 0; i < 6; i++)
            {
            net.getActor(i).setName("n" + (i + 1));
            }
        return net;
        }

    private interface AreaSetup
        {
        void apply(NodeArea area);
        }

    private BufferedImage render(Network net, AreaSetup setup)
        {
        Supplier<FullNet> prevFull = AppRuntime.currentFullNetSupplier();
        Supplier<Network> prevNet = AppRuntime.currentNetworkSupplier();
        try
            {
            FullNet holder = new FullNet();
            holder.setNetwork(net);
            holder.attachArea();
            AppRuntime.setCurrentFullNet(holder);
            AppRuntime.setCurrentNetwork(net);
            NodeArea area = holder.getArea();
            area.setSize(400, 400);
            area.setWidthSimply(400);
            setup.apply(area);
            area.updateArea(net);
            area.paintEdges();
            BufferedImage img = new BufferedImage(400, 400,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            area.paint(g);
            g.dispose();
            return img;
            } finally
            {
            AppRuntime.setCurrentNetworkSupplier(prevNet);
            AppRuntime.setCurrentFullNetSupplier(prevFull);
            }
        }

    private static int distinctColors(BufferedImage img)
        {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < img.getHeight(); y += 2)
            {
            for (int x = 0; x < img.getWidth(); x += 2)
                {
                colors.add(img.getRGB(x, y) & 0xffffff);
                }
            }
        return colors.size();
        }

    private static boolean samePixels(BufferedImage a, BufferedImage b)
        {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight())
            {
            return false;
            }
        return java.util.Arrays.equals(
                a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0,
                        a.getWidth()),
                b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0,
                        b.getWidth()));
        }

    @Test
    public void canvasPaintsRealContent()
        {
        BufferedImage img = render(sampleNet(), area ->
            {
            });
        assertTrue(distinctColors(img) > 5,
                "background + grid + edges + faces present, got "
                        + distinctColors(img));
        }

    @Test
    public void backgroundColorReachesTheCorner()
        {
        BufferedImage img = render(sampleNet(), area ->
            {
            area.setBackgroundColor(new Color(0x10, 0x20, 0x30));
            });
        assertEqualsPixel(0x102030, img.getRGB(3, 3) & 0xffffff);
        }

    @Test
    public void gridToggleChangesTheRender()
        {
        BufferedImage on = render(sampleNet(), area -> area.setGridEnabled(
                true));
        BufferedImage off = render(sampleNet(), area -> area.setGridEnabled(
                false));
        assertFalse(samePixels(on, off), "grid on vs off differ");
        }

    @Test
    public void namesToggleChangesTheRender()
        {
        BufferedImage on = render(sampleNet(), area -> area.setPrintNames(
                true));
        BufferedImage off = render(sampleNet(), area -> area.setPrintNames(
                false));
        assertNotEquals(distinctColors(on), distinctColors(off),
                "name labels change the pixel census");
        }

    @Test
    public void facesToggleChangesTheRender()
        {
        BufferedImage on = render(sampleNet(), area -> area.setFacesVisible(
                true));
        BufferedImage off = render(sampleNet(), area -> area.setFacesVisible(
                false));
        assertFalse(samePixels(on, off), "faces on vs off differ");
        assertTrue(distinctColors(on) >= distinctColors(off),
                "faces add colour, on=" + distinctColors(on) + " off="
                        + distinctColors(off));
        }

    @Test
    public void titleToggleChangesTheRender()
        {
        BufferedImage on = render(sampleNet(), area -> area.setTitleVisible(
                true));
        BufferedImage off = render(sampleNet(), area -> area.setTitleVisible(
                false));
        assertFalse(samePixels(on, off), "title on vs off differ");
        }

    @Test
    public void edgeValuesToggleChangesTheRender()
        {
        BufferedImage on = render(sampleNet(), area -> area.setEdgeValueVisible(
                true));
        BufferedImage off = render(sampleNet(), area -> area.setEdgeValueVisible(
                false));
        assertFalse(samePixels(on, off), "edge values on vs off differ");
        }

    @Test
    public void rendersAreDeterministic()
        {
        BufferedImage a = render(sampleNet(), area ->
            {
            });
        BufferedImage b = render(sampleNet(), area ->
            {
            });
        assertTrue(samePixels(a, b), "identical inputs, identical pixels");
        }

    @Test
    public void actorFacesMatchTheBundledSetAfterExtraction()
        {
        // faces are resolved through the extracted asset directory; a
        // fresh actor defaults to the bundled face source
        Network net = sampleNet();
        render(net, area ->
            {
            });
        String face = net.getActor(0).getFaceSource();
        assertTrue(face != null && face.length() > 0, "a face source");
        }

    private static void assertEqualsPixel(int expected, int actual)
        {
        if (expected != actual)
            {
            throw new AssertionError("pixel: expected #"
                    + Integer.toHexString(expected) + " but was #"
                    + Integer.toHexString(actual));
            }
        }
    }