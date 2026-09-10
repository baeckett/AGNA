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

    private NetworkLayouts()
        {
        }

    public static void apply(Network net, int layout, int width, int height)
        {
        int n = net.getSize();
        if (n == 0)
            {
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
        for (int iter = 0; iter < 60; iter++)
            {
            float[] fx = new float[n];
            float[] fy = new float[n];
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
                px[i] = Math.max(10, Math.min(width - 10, px[i] + fx[i] * 0.2f));
                py[i] = Math.max(10, Math.min(height - 10, py[i] + fy[i] * 0.2f));
                }
            }
        for (int i = 0; i < n; i++)
            {
            net.getActor(i).setX((int) Math.round(px[i]), width);
            net.getActor(i).setY((int) Math.round(py[i]), height);
            }
        }
    }
