package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the deterministic layouts used by the CLI renderer must place
 * every actor inside the canvas - circular on a ring around the centre,
 * random and spring spread across the area, all reproducible.
 */
public class NetworkLayoutsTest
    {
    private static Network sample() throws Exception
        {
        FullNet full = new FullNet();
        full.readNetwork(new String(Files.readAllBytes(new File(
                "samples/example2.agn").toPath()),
                StandardCharsets.ISO_8859_1), "agn");
        return full.getNetwork();
        }

    private static int px(Actor actor, int width)
        {
        return actor.getX(width);
        }

    private static int py(Actor actor, int height)
        {
        return actor.getY(height);
        }

    @Test
    public void circularPutsEveryActorOnTheRing() throws Exception
        {
        Network net = sample();
        int w = 800;
        int h = 600;
        NetworkLayouts.apply(net, NetworkLayouts.CIRCULAR, w, h);
        double cx = w / 2.0;
        double cy = h / 2.0;
        double expected = Math.min(w, h) * 0.42;
        for (int i = 0; i < net.getSize(); i++)
            {
            Actor actor = net.getActor(i);
            int x = px(actor, w);
            int y = py(actor, h);
            assertTrue(x >= 0 && x < w && y >= 0 && y < h,
                    "actor " + i + " inside canvas");
            double dist = Math.hypot(x - cx, y - cy);
            assertTrue(Math.abs(dist - expected) < 15,
                    "actor " + i + " on the ring, dist=" + dist);
            }
        }

    @Test
    public void randomAndSpringStayInsideTheCanvas() throws Exception
        {
        for (int layout : new int[] { NetworkLayouts.RANDOM,
                NetworkLayouts.SPRING })
            {
            Network net = sample();
            NetworkLayouts.apply(net, layout, 700, 500);
            for (int i = 0; i < net.getSize(); i++)
                {
                int x = px(net.getActor(i), 700);
                int y = py(net.getActor(i), 500);
                assertTrue(x >= 0 && x < 700 && y >= 0 && y < 500,
                        "layout " + layout + " actor " + i);
                }
            }
        }

    @Test
    public void gridFillsTheCanvasInRows() throws Exception
        {
        Network net = sample();
        int w = 800;
        int h = 600;
        NetworkLayouts.apply(net, NetworkLayouts.GRID, w, h);
        for (int i = 0; i < net.getSize(); i++)
            {
            int x = px(net.getActor(i), w);
            int y = py(net.getActor(i), h);
            assertTrue(x >= 0 && x < w && y >= 0 && y < h,
                    "grid actor " + i + " inside canvas");
            }
        // every actor lands on a different grid cell (no two same)
        for (int i = 0; i < net.getSize(); i++)
            {
            for (int j = i + 1; j < net.getSize(); j++)
                {
                boolean same = px(net.getActor(i), w) == px(
                        net.getActor(j), w)
                        && py(net.getActor(i), h) == py(net.getActor(j), h);
                assertTrue(!same, "grid cells distinct for " + i + "," + j);
                }
            }
        }

    @Test
    public void concentricPutsTheHubInTheCentre() throws Exception
        {
        Network net = sample();
        int w = 800;
        int h = 600;
        int hub = 0;
        for (int i = 1; i < net.getSize(); i++)
            {
            if (degree(net, i) > degree(net, hub))
                {
                hub = i;
                }
            }
        NetworkLayouts.apply(net, NetworkLayouts.CONCENTRIC, w, h);
        double cx = w / 2.0;
        double cy = h / 2.0;
        double hubDist = Math.hypot(px(net.getActor(hub), w) - cx,
                py(net.getActor(hub), h) - cy);
        assertTrue(hubDist < 15, "highest-degree actor at the centre, "
                + "dist=" + hubDist);
        for (int i = 0; i < net.getSize(); i++)
            {
            int x = px(net.getActor(i), w);
            int y = py(net.getActor(i), h);
            assertTrue(x >= 0 && x < w && y >= 0 && y < h,
                    "concentric actor " + i + " inside canvas");
            }
        }

    @Test
    public void layoutsAreDeterministic() throws Exception
        {
        for (int layout : new int[] { NetworkLayouts.RANDOM,
                NetworkLayouts.GRID, NetworkLayouts.CONCENTRIC })
            {
            Network a = sample();
            Network b = sample();
            NetworkLayouts.apply(a, layout, 640, 480);
            NetworkLayouts.apply(b, layout, 640, 480);
            for (int i = 0; i < a.getSize(); i++)
                {
                assertTrue(px(a.getActor(i), 640) == px(b.getActor(i), 640)
                        && py(a.getActor(i), 480) == py(b.getActor(i), 480),
                        "layout " + layout + " same seed, same position for "
                                + "actor " + i);
                }
            }
        }

    private static int degree(Network net, int i)
        {
        int d = 0;
        for (int j = 0; j < net.getSize(); j++)
            {
            if (i != j && net.getValue(i, j) != 0f)
                {
                d++;
                }
            }
        return d;
        }
    }