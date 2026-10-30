/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

import com.bentza.sna.net.Network;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * High-performance random graph generation and network permutation algorithms
 * for Social Network Analysis null models.
 */
public class NetworkRandomizer
    {
    /**
     * Randomizes a network according to the specified null model using the provided PRNG.
     */
    public static Network randomize(Network src, NullModel model, Random rng)
        {
        if (src == null)
            return null;
        switch (model)
            {
            case DEGREE_PRESERVING:
                return randomizeDegreePreserving(src, 10, rng);
            case ERDOS_RENYI:
                return randomizeErdosRenyi(src, rng);
            case DYAD_CENSUS:
                return randomizeDyadCensus(src, rng);
            case WEIGHT_PERMUTATION:
                return randomizeWeights(src, rng);
            default:
                return randomizeDegreePreserving(src, 10, rng);
            }
        }

    /**
     * Clones actor metadata and edge values onto a fresh Network instance.
     */
    public static Network copyNetwork(Network src)
        {
        Network copy = cloneSkeleton(src);
        int n = src.getSize();
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                float w = src.getValue(i, j);
                if (w != 0f)
                    copy.setValue(w, i, j);
                }
            }
        return copy;
        }

    /**
     * Clones actor metadata (names, positions, sizes) onto a fresh Network with 0 edges.
     */
    private static Network cloneSkeleton(Network src)
        {
        int n = src.getSize();
        Network target = new Network(n);
        target.setName(src.getName() + " [null]");
        for (int i = 0; i < n; i++)
            {
            target.getActor(i).setName(src.getActor(i).getName());
            target.getActor(i).setX(src.getActor(i).getX());
            target.getActor(i).setY(src.getActor(i).getY());
            target.getActor(i).setSize(src.getActor(i).getSize());
            }
        return target;
        }

    /**
     * Checks if the given network is symmetric (undirected).
     */
    public static boolean isSymmetric(Network net)
        {
        int n = net.getSize();
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                if (net.getValue(i, j) != net.getValue(j, i))
                    return false;
                }
            }
        return true;
        }

    /**
     * Maslov–Sneppen (2002) / Rao-Jana-Bandyopadhyay (1996) Double Edge Swap algorithm.
     * Preserves the exact in-degree and out-degree sequence of every node in the network.
     * Runs Q * m successful swaps (default Q = 10).
     */
    public static Network randomizeDegreePreserving(Network src, int swapsPerEdge, Random rng)
        {
        int n = src.getSize();
        if (n < 4)
            return copyNetwork(src);

        boolean undirected = isSymmetric(src);
        Network copy = cloneSkeleton(src);

        if (undirected)
            {
            // Extract unique undirected edges (i < j)
            ArrayList<int[]> edges = new ArrayList<>();
            boolean[][] adj = new boolean[n][n];
            float[][] weights = new float[n][n];

            for (int i = 0; i < n; i++)
                {
                for (int j = i + 1; j < n; j++)
                    {
                    float w = src.getValue(i, j);
                    if (w != 0f)
                        {
                        edges.add(new int[] { i, j });
                        adj[i][j] = true;
                        adj[j][i] = true;
                        weights[i][j] = w;
                        weights[j][i] = w;
                        }
                    }
                }

            int m = edges.size();
            if (m < 2)
                return copyNetwork(src);

            int targetSwaps = swapsPerEdge * m;
            int maxAttempts = targetSwaps * 10;
            int swaps = 0;

            for (int iter = 0; iter < maxAttempts && swaps < targetSwaps; iter++)
                {
                int e1 = rng.nextInt(m);
                int e2 = rng.nextInt(m);
                if (e1 == e2)
                    continue;

                int[] edge1 = edges.get(e1);
                int[] edge2 = edges.get(e2);
                int u = edge1[0];
                int v = edge1[1];
                int x = edge2[0];
                int y = edge2[1];

                // Ensure 4 distinct vertices
                if (u == x || u == y || v == x || v == y)
                    continue;

                // Randomly decide endpoint pairing: (u-x, v-y) or (u-y, v-x)
                int newV1, newV2;
                if (rng.nextBoolean())
                    {
                    newV1 = y;
                    newV2 = x;
                    }
                else
                    {
                    newV1 = x;
                    newV2 = y;
                    }

                // Check that target edges do not already exist and are not self-loops
                if (!adj[u][newV1] && !adj[v][newV2] && u != newV1 && v != newV2)
                    {
                    // Remove old edges
                    adj[u][v] = false;
                    adj[v][u] = false;
                    adj[x][y] = false;
                    adj[y][x] = false;

                    // Add new edges
                    adj[u][newV1] = true;
                    adj[newV1][u] = true;
                    adj[v][newV2] = true;
                    adj[newV2][v] = true;

                    float w1 = weights[u][v];
                    float w2 = weights[x][y];
                    weights[u][newV1] = w1;
                    weights[newV1][u] = w1;
                    weights[v][newV2] = w2;
                    weights[newV2][v] = w2;

                    edge1[0] = Math.min(u, newV1);
                    edge1[1] = Math.max(u, newV1);
                    edge2[0] = Math.min(v, newV2);
                    edge2[1] = Math.max(v, newV2);

                    swaps++;
                    }
                }

            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    if (adj[i][j])
                        copy.setValue(weights[i][j] != 0f ? weights[i][j] : 1.0f, i, j);
                    }
                }
            }
        else
            {
            // Directed double-edge swap
            ArrayList<int[]> edges = new ArrayList<>();
            boolean[][] adj = new boolean[n][n];
            float[][] weights = new float[n][n];

            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    if (i != j)
                        {
                        float w = src.getValue(i, j);
                        if (w != 0f)
                            {
                            edges.add(new int[] { i, j });
                            adj[i][j] = true;
                            weights[i][j] = w;
                            }
                        }
                    }
                }

            int m = edges.size();
            if (m < 2)
                return copyNetwork(src);

            int targetSwaps = swapsPerEdge * m;
            int maxAttempts = targetSwaps * 10;
            int swaps = 0;

            for (int iter = 0; iter < maxAttempts && swaps < targetSwaps; iter++)
                {
                int e1 = rng.nextInt(m);
                int e2 = rng.nextInt(m);
                if (e1 == e2)
                    continue;

                int[] edge1 = edges.get(e1);
                int[] edge2 = edges.get(e2);
                int u = edge1[0];
                int v = edge1[1];
                int x = edge2[0];
                int y = edge2[1];

                // Check 4 distinct endpoints and no self loops (u -> y, x -> v)
                if (u == x || v == y || u == y || x == v)
                    continue;

                if (!adj[u][y] && !adj[x][v])
                    {
                    adj[u][v] = false;
                    adj[x][y] = false;
                    adj[u][y] = true;
                    adj[x][v] = true;

                    float w1 = weights[u][v];
                    float w2 = weights[x][y];
                    weights[u][y] = w1;
                    weights[x][v] = w2;

                    edge1[1] = y;
                    edge2[1] = v;

                    swaps++;
                    }
                }

            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    if (adj[i][j])
                        copy.setValue(weights[i][j] != 0f ? weights[i][j] : 1.0f, i, j);
                    }
                }
            }

        return copy;
        }

    /**
     * Erdős–Rényi G(n, m) density-preserving random model.
     * Preserves exact node count n and edge count m.
     */
    public static Network randomizeErdosRenyi(Network src, Random rng)
        {
        int n = src.getSize();
        Network copy = cloneSkeleton(src);
        boolean undirected = isSymmetric(src);

        if (undirected)
            {
            int m = 0;
            ArrayList<Float> weights = new ArrayList<>();
            for (int i = 0; i < n; i++)
                {
                for (int j = i + 1; j < n; j++)
                    {
                    float w = src.getValue(i, j);
                    if (w != 0f)
                        {
                        m++;
                        weights.add(w);
                        }
                    }
                }

            ArrayList<int[]> allPairs = new ArrayList<>();
            for (int i = 0; i < n; i++)
                {
                for (int j = i + 1; j < n; j++)
                    allPairs.add(new int[] { i, j });
                }

            Collections.shuffle(allPairs, rng);
            int count = Math.min(m, allPairs.size());
            for (int k = 0; k < count; k++)
                {
                int[] pair = allPairs.get(k);
                float w = k < weights.size() ? weights.get(k) : 1.0f;
                copy.setValue(w, pair[0], pair[1]);
                copy.setValue(w, pair[1], pair[0]);
                }
            }
        else
            {
            int m = 0;
            ArrayList<Float> weights = new ArrayList<>();
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    if (i != j && src.getValue(i, j) != 0f)
                        {
                        m++;
                        weights.add(src.getValue(i, j));
                        }
                    }
                }

            ArrayList<int[]> allPairs = new ArrayList<>();
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    if (i != j)
                        allPairs.add(new int[] { i, j });
                    }
                }

            Collections.shuffle(allPairs, rng);
            int count = Math.min(m, allPairs.size());
            for (int k = 0; k < count; k++)
                {
                int[] pair = allPairs.get(k);
                float w = k < weights.size() ? weights.get(k) : 1.0f;
                copy.setValue(w, pair[0], pair[1]);
                }
            }

        return copy;
        }

    /**
     * Holland-Leinhardt U|MAN dyad census model.
     * Preserves the exact counts of mutual (M), asymmetric (A), and null (N) dyads.
     */
    public static Network randomizeDyadCensus(Network src, Random rng)
        {
        int n = src.getSize();
        Network copy = cloneSkeleton(src);

        int mutual = 0;
        int asymmetric = 0;
        ArrayList<Float> mutualWeights1 = new ArrayList<>();
        ArrayList<Float> mutualWeights2 = new ArrayList<>();
        ArrayList<Float> asymWeights = new ArrayList<>();

        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                float w1 = src.getValue(i, j);
                float w2 = src.getValue(j, i);
                if (w1 != 0f && w2 != 0f)
                    {
                    mutual++;
                    mutualWeights1.add(w1);
                    mutualWeights2.add(w2);
                    }
                else if (w1 != 0f || w2 != 0f)
                    {
                    asymmetric++;
                    asymWeights.add(w1 != 0f ? w1 : w2);
                    }
                }
            }

        ArrayList<int[]> allPairs = new ArrayList<>();
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                allPairs.add(new int[] { i, j });
            }

        Collections.shuffle(allPairs, rng);

        int idx = 0;
        // Place mutual dyads
        for (int m = 0; m < mutual && idx < allPairs.size(); m++, idx++)
            {
            int[] pair = allPairs.get(idx);
            float w1 = m < mutualWeights1.size() ? mutualWeights1.get(m) : 1.0f;
            float w2 = m < mutualWeights2.size() ? mutualWeights2.get(m) : 1.0f;
            copy.setValue(w1, pair[0], pair[1]);
            copy.setValue(w2, pair[1], pair[0]);
            }

        // Place asymmetric dyads with random direction (p = 0.5)
        for (int a = 0; a < asymmetric && idx < allPairs.size(); a++, idx++)
            {
            int[] pair = allPairs.get(idx);
            float w = a < asymWeights.size() ? asymWeights.get(a) : 1.0f;
            if (rng.nextBoolean())
                copy.setValue(w, pair[0], pair[1]);
            else
                copy.setValue(w, pair[1], pair[0]);
            }

        return copy;
        }

    /**
     * Randomly permutes edge weights across existing network edges,
     * preserving binary topology.
     */
    public static Network randomizeWeights(Network src, Random rng)
        {
        int n = src.getSize();
        Network copy = cloneSkeleton(src);

        ArrayList<int[]> edges = new ArrayList<>();
        ArrayList<Float> weights = new ArrayList<>();

        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                float w = src.getValue(i, j);
                if (w != 0f)
                    {
                    edges.add(new int[] { i, j });
                    weights.add(w);
                    }
                }
            }

        Collections.shuffle(weights, rng);
        for (int k = 0; k < edges.size(); k++)
            {
            int[] edge = edges.get(k);
            copy.setValue(weights.get(k), edge[0], edge[1]);
            }

        return copy;
        }
    }
