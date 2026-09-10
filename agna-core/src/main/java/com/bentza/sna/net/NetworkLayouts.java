package com.bentza.sna.net;

import java.util.Random;

/**
 * 2.1.3: deterministic network layouts for headless rendering (the CLI and
 * any batch consumer). Positions are set on the actors as pixel
 * coordinates; the viewer and the image renderer both read them from the
 * same percent-based NodeXY storage.
 */
public class NetworkLayouts
    {
    public static final int CIRCULAR = 0;
    public static final int RANDOM = 1;
    public static final int SPRING = 2;
    public static final int GRID = 3;
    public static final int CONCENTRIC = 4;

    public static final int SPRING_ITERATIONS = 60;

    private NetworkLayouts()
        {
        }

    public static void apply(Network net, int layout, int width, int height)
        {
        apply(net, layout, width, height, SPRING_ITERATIONS);
        }

    public static void apply(Network net, int layout, int width, int height,
            int iterations)
        {
        int n = net.getSize();
        if (n == 0)
            {
            return;
            }
        // freshly built networks may carry actors with no coordinates yet
        for (int i = 0; i < n; i++)
            {
            net.getActor(i).createCoordinatesIfMissing();
            }
        if (layout == GRID)
            {
            applyGrid(net, width, height);
            return;
            }
        if (layout == CONCENTRIC)
            {
            applyConcentric(net, width, height);
            return;
            }
        if (layout == CIRCULAR)
            {
            double cx = width / 2.0;
            double cy = height / 2.0;
            double radius = Math.min(width, height) * 0.42;
            for (int i = 0; i < n; i++)
                {
                double a = 2 * Math.PI * i / n;
                net.getActor(i).setX((int) Math.round(cx + radius * Math.cos(a)),
                        width);
                net.getActor(i).setY((int) Math.round(cy + radius * Math.sin(a)),
                        height);
                }
            return;
            }
        Random rnd = new Random(20260910L);
        if (layout == RANDOM)
            {
            for (int i = 0; i < n; i++)
                {
                net.getActor(i).setX(10 + rnd.nextInt(Math.max(1, width - 20)),
                        width);
                net.getActor(i).setY(10 + rnd.nextInt(Math.max(1, height - 20)),
                        height);
                }
            return;
            }
        // SPRING (simple Fruchterman-Reingold style, deterministic)
        float[] px = new float[n];
        float[] py = new float[n];
        for (int i = 0; i < n; i++)
            {
            px[i] = 10 + rnd.nextInt(Math.max(1, width - 20));
            py[i] = 10 + rnd.nextInt(Math.max(1, height - 20));
            }
        double area = (double) width * height;
        double k = Math.sqrt(area / Math.max(1, n));
        int iters = Math.max(1, iterations);
        for (int iter = 0; iter < iters; iter++)
            {
            float[] fx = new float[n];
            float[] fy = new float[n];
            // classic Fruchterman-Reingold cooling: large moves early,
            // gentle settling later
            float temp = 1f - (float) iter / iters;
            for (int i = 0; i < n; i++)
                {
                for (int j = i + 1; j < n; j++)
                    {
                    float dx = px[i] - px[j];
                    float dy = py[i] - py[j];
                    float dist = (float) Math.max(1.0, Math.hypot(dx, dy));
                    float rep = (float) (k * k / dist);
                    float nx = dx / dist;
                    float ny = dy / dist;
                    fx[i] += rep * nx;
                    fy[i] += rep * ny;
                    fx[j] -= rep * nx;
                    fy[j] -= rep * ny;
                    }
                }
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    float v = net.getValue(i, j);
                    if (i != j && v != 0f)
                        {
                        float dx = px[i] - px[j];
                        float dy = py[i] - py[j];
                        float dist = (float) Math.max(1.0, Math.hypot(dx, dy));
                        float attr = (float) (dist * dist / k);
                        float nx = dx / dist;
                        float ny = dy / dist;
                        fx[i] -= attr * nx;
                        fy[i] -= attr * ny;
                        }
                    }
                }
            for (int i = 0; i < n; i++)
                {
                float maxDisp = (float) (temp * k);
                float dx = Math.max(-maxDisp, Math.min(maxDisp, fx[i]));
                float dy = Math.max(-maxDisp, Math.min(maxDisp, fy[i]));
                px[i] = Math.max(10, Math.min(width - 10, px[i] + dx));
                py[i] = Math.max(10, Math.min(height - 10, py[i] + dy));
                }
            }
        for (int i = 0; i < n; i++)
            {
            net.getActor(i).setX((int) Math.round(px[i]), width);
            net.getActor(i).setY((int) Math.round(py[i]), height);
            }
        }

    // deterministic row/column lattice
    private static void applyGrid(Network net, int width, int height)
        {
        int n = net.getSize();
        int cols = (int) Math.ceil(Math.sqrt(n));
        int rows = (int) Math.ceil((double) n / cols);
        float mx = width * 0.08f;
        float my = height * 0.08f;
        float cellW = (width - 2 * mx) / cols;
        float cellH = (height - 2 * my) / rows;
        for (int k = 0; k < n; k++)
            {
            int col = k % cols;
            int row = k / cols;
            net.getActor(k).setX((int) Math.round(mx + col * cellW
                    + cellW / 2), width);
            net.getActor(k).setY((int) Math.round(my + row * cellH
                    + cellH / 2), height);
            }
        }

    // hub-and-spoke: the highest-degree actor sits in the centre, the
    // rest fan out on concentric rings (ring r holds ranks r^2..(r+1)^2)
    private static void applyConcentric(Network net, int width, int height)
        {
        int n = net.getSize();
        Integer[] rank = new Integer[n];
        for (int i = 0; i < n; i++)
            {
            rank[i] = i;
            }
        java.util.Arrays.sort(rank, new java.util.Comparator<Integer>()
            {
            public int compare(Integer a, Integer b)
                {
                return degree(net, b) - degree(net, a);
                }
            });
        double cx = width / 2.0;
        double cy = height / 2.0;
        int rings = (int) Math.ceil(Math.sqrt(n));
        double maxR = Math.min(width, height) * 0.42;
        for (int i = 0; i < n; i++)
            {
            int r = (int) Math.floor(Math.sqrt(i));
            double radius = rings > 1 ? maxR * r / (rings - 1) : 0.0;
            double angle = (i - r * r) * 2 * Math.PI
                    / Math.max(1, 2 * r + 1) + r * 0.5;
            if (r == 0)
                {
                radius = 0.0;
                }
            net.getActor(rank[i]).setX((int) Math.round(cx + radius
                    * Math.cos(angle)), width);
            net.getActor(rank[i]).setY((int) Math.round(cy + radius
                    * Math.sin(angle)), height);
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
