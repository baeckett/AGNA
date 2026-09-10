package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: correctness of the Barnes-Hut quadtree repulsion used by the
 * spring embedder above SPRING_BH_ABOVE nodes. The quadtree must agree
 * with the exact all-pairs forces within the classic theta bound, and a
 * full layout at quadtree scale must be reproducible.
 */
public class BarnesHutTest
    {
    private static final int W = 600;
    private static final int H = 600;

    @Test
    public void repulsionMatchesExactWithinTheThetaBound() throws Exception
        {
        int n = 300;
        float[] px = new float[n];
        float[] py = new float[n];
        Random rnd = new Random(7L);
        for (int i = 0; i < n; i++)
            {
            px[i] = 10 + 580 * rnd.nextFloat();
            py[i] = 10 + 580 * rnd.nextFloat();
            }
        float[] exact = NetworkLayouts.repulsionForces(n, px, py, 50.0,
                W, H, false);
        float[] approx = NetworkLayouts.repulsionForces(n, px, py, 50.0,
                W, H, true);
        // vector-level per-node error: what the embedder actually integrates
        double meanRel = 0.0;
        double maxRel = 0.0;
        for (int i = 0; i < n; i++)
            {
            double ex = Math.hypot(exact[2 * i], exact[2 * i + 1]);
            double dx = approx[2 * i] - exact[2 * i];
            double dy = approx[2 * i + 1] - exact[2 * i + 1];
            double err = Math.hypot(dx, dy) / (ex + 1e-3);
            meanRel += err;
            maxRel = Math.max(maxRel, err);
            }
        meanRel /= n;
        System.out.printf("barnes-hut theta=%.1f: mean force error %.4f,"
                + " max %.4f%n", NetworkLayouts.SPRING_THETA, meanRel,
                maxRel);
        assertTrue(meanRel < 0.08, "mean relative force error " + meanRel);
        assertTrue(maxRel < 0.75, "max relative force error " + maxRel);
        }

    @Test
    public void bhLayoutIsDeterministic() throws Exception
        {
        Network a = buildGraph(1200, 42L);
        Network b = buildGraph(1200, 42L);
        NetworkLayouts.apply(a, NetworkLayouts.SPRING, W, H, 30);
        NetworkLayouts.apply(b, NetworkLayouts.SPRING, W, H, 30);
        for (int i = 0; i < 1200; i++)
            {
            assertTrue(Float.isFinite(a.getActor(i).getX())
                    && Float.isFinite(a.getActor(i).getY()), "finite");
            assertTrue(a.getActor(i).getX() == b.getActor(i).getX()
                    && a.getActor(i).getY() == b.getActor(i).getY(),
                    "same seed, same position actor " + i);
            }
        }

    @Test
    public void bhInCanvasCollisionSafe() throws Exception
        {
        // many nodes on a shared position: the depth cap must stop the
        // quadtree from recursing forever on identical floats
        Network net = new Network(500);
        for (int i = 0; i < 500; i++)
            {
            for (int j = i + 1; j < 500; j++)
                {
                if ((i + j) % 7 == 0)
                    {
                    net.setValue(1f, i, j);
                    net.setValue(1f, j, i);
                    }
                }
            }
        for (int i = 0; i < 500; i++)
            {
            net.getActor(i).setX(300f);
            net.getActor(i).setY(300f);
            }
        NetworkLayouts.apply(net, NetworkLayouts.SPRING, W, H, 10);
        for (int i = 0; i < 500; i++)
            {
            assertTrue(Float.isFinite(net.getActor(i).getX())
                    && Float.isFinite(net.getActor(i).getY()),
                    "finite actor " + i);
            }
        }

    private static Network buildGraph(int n, long seed)
        {
        Network net = new Network(n);
        Random rnd = new Random(seed);
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                if (rnd.nextDouble() < 2.0 / (n - 1))
                    {
                    net.setValue(1f, i, j);
                    net.setValue(1f, j, i);
                    }
                }
            }
        for (int i = 0; i < n; i++)
            {
            net.getActor(i).createCoordinates();
            }
        return net;
        }
    }