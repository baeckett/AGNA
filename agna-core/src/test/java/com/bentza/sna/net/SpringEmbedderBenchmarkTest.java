package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: performance benchmarks for the spring embedder across network
 * sizes. Each run measures iterations per second and the quality of the
 * final layout via the standard normalized stress metric (euclidean
 * distances rescaled by the mean one-hop length, then compared with
 * graph-theoretic hop distances; disconnected pairs carry no stress).
 * The graphs are seeded, so the numbers are reproducible.
 */
public class SpringEmbedderBenchmarkTest
    {
    private static final int W = 600;
    private static final int H = 600;
    private static final int INF = Integer.MAX_VALUE / 2;

    private static Network buildGraph(int n, long seed, int avgDegree)
        {
        Network net = new Network(n);
        Random rnd = new Random(seed);
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                if (rnd.nextDouble() < (double) avgDegree
                        / Math.max(1, n - 1))
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

    private static List<Integer>[] adjacency(Network net)
        {
        int n = net.getSize();
        @SuppressWarnings("unchecked")
        List<Integer>[] adj = new List[n];
        for (int i = 0; i < n; i++)
            {
            adj[i] = new ArrayList<>();
            }
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                if (net.getValue(i, j) != 0f)
                    {
                    adj[i].add(j);
                    adj[j].add(i);
                    }
                }
            }
        return adj;
        }

    private static int[][] hopDistances(Network net, List<Integer>[] adj)
        {
        int n = net.getSize();
        int[][] dist = new int[n][n];
        for (int s = 0; s < n; s++)
            {
            Arrays.fill(dist[s], INF);
            dist[s][s] = 0;
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(s);
            while (!queue.isEmpty())
                {
                int u = queue.poll();
                for (int v : adj[u])
                    {
                    if (dist[s][v] == INF)
                        {
                        dist[s][v] = dist[s][u] + 1;
                        queue.add(v);
                        }
                    }
                }
            }
        return dist;
        }

    private static double euclid(Network net, int i, int j)
        {
        double dx = net.getActor(i).getX() - net.getActor(j).getX();
        double dy = net.getActor(i).getY() - net.getActor(j).getY();
        return Math.hypot(dx, dy);
        }

    // normalized stress, scale invariant (one-hop euclidean length = 1)
    private static double stress(Network net, int[][] hops)
        {
        int n = net.getSize();
        double meanOneHop = 0.0;
        long oneHopPairs = 0;
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                if (hops[i][j] == 1)
                    {
                    meanOneHop += euclid(net, i, j);
                    oneHopPairs++;
                    }
                }
            }
        if (oneHopPairs == 0)
            {
            return Double.POSITIVE_INFINITY;
            }
        meanOneHop /= oneHopPairs;
        double num = 0.0;
        double den = 0.0;
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                int h = hops[i][j];
                if (h >= INF)
                    {
                    continue; // disconnected pairs carry no stress
                    }
                double d = euclid(net, i, j) / meanOneHop;
                num += (d - h) * (d - h);
                den += (double) h * h;
                }
            }
        return den == 0.0 ? Double.POSITIVE_INFINITY : num / den;
        }

    private void benchmark(int n, int iterations, long seed)
        {
        Network net = buildGraph(n, seed, 2);
        int[][] hops = hopDistances(net, adjacency(net));
        double before = stress(net, hops);
        long t0 = System.nanoTime();
        NetworkLayouts.apply(net, NetworkLayouts.SPRING, W, H, iterations);
        long ms = (System.nanoTime() - t0) / 1_000_000;
        double after = stress(net, hops);
        double ips = iterations / (ms / 1000.0);
        System.out.printf("spring n=%-5d iters=%-3d %6d ms  %9.2f iter/s"
                + "  stress %.3f -> %.3f%n", n, iterations, ms, ips,
                before, after);
        assertTrue(ips > 0, "iterations per second measured at n=" + n);
        assertTrue(after < before, "spring lowers stress at n=" + n + " ("
                + before + " -> " + after + ")");
        assertTrue(Double.isFinite(after), "finite stress at n=" + n);
        for (int i = 0; i < n; i++)
            {
            assertTrue(Float.isFinite(net.getActor(i).getX())
                    && Float.isFinite(net.getActor(i).getY()),
                    "finite coordinates at n=" + n + " actor " + i);
            }
        }

    @Test
    public void benchmark100Nodes()
        {
        benchmark(100, 60, 11L);
        }

    @Test
    public void benchmark1000Nodes()
        {
        benchmark(1000, 30, 22L);
        }

    @Test
    public void benchmark5000Nodes()
        {
        benchmark(5000, 12, 33L);
        }
    }