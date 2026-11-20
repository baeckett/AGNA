/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

/**
 * 2.2.2: immutable compressed-sparse-row (CSR) view of a network's ties.
 *
 * <p>Agna stores a network as dense rows ({@code Actor.emissions}, one
 * {@code float[n]} per actor). Graph algorithms that walk neighbourhoods
 * (BFS, Brandes betweenness, triad enumeration, k-cores, clustering) should
 * not scan a full row of n cells per visited actor: on a sparse network that
 * turns O(n m) algorithms into O(n^3). This index is built in one O(n^2)
 * pass over the rows and then offers O(degree) neighbour iteration over
 * primitive arrays (no boxing, no linked-list pointer chasing).
 *
 * <p>Conventions: a tie is a non-zero, finite cell; the diagonal
 * (self-connections) is excluded from the neighbour arrays and counted
 * separately in {@link #selfLoops}. Neighbour arrays are sorted ascending.
 * The index is a snapshot: rebuild it after the network changes.
 */
public final class AdjacencyIndex
    {
    public final int n;
    /** out-neighbours of v: outTarget[outStart[v] .. outStart[v+1]) */
    public final int[] outStart;
    public final int[] outTarget;
    public final float[] outWeight;
    /** in-neighbours of v: inSource[inStart[v] .. inStart[v+1]) */
    public final int[] inStart;
    public final int[] inSource;
    public final float[] inWeight;
    /** undirected neighbours (union of in and out), sorted */
    public final int[] nbStart;
    public final int[] nbTarget;
    public final int selfLoops;
    public final long arcs;

    private AdjacencyIndex(int n, int[] outStart, int[] outTarget, float[] outWeight, int[] inStart,
            int[] inSource, float[] inWeight, int[] nbStart, int[] nbTarget, int selfLoops)
        {
        this.n = n;
        this.outStart = outStart;
        this.outTarget = outTarget;
        this.outWeight = outWeight;
        this.inStart = inStart;
        this.inSource = inSource;
        this.inWeight = inWeight;
        this.nbStart = nbStart;
        this.nbTarget = nbTarget;
        this.selfLoops = selfLoops;
        this.arcs = outTarget.length;
        }

    /** Builds the index from the network's rows in O(n^2) time and O(n + m) memory. */
    public static AdjacencyIndex of(Network net)
        {
        int n = net == null ? 0 : net.getSize();
        float[][] rows = new float[n][];
        for (int i = 0; i < n; i++)
            {
            Actor a = net.getActor(i);
            rows[i] = (a != null && a.emissions != null) ? a.emissions : new float[0];
            }
        int[] outDeg = new int[n];
        int[] inDeg = new int[n];
        int loops = 0;
        long m = 0;
        for (int i = 0; i < n; i++)
            {
            float[] r = rows[i];
            int lim = Math.min(n, r.length);
            for (int j = 0; j < lim; j++)
                {
                float v = r[j];
                if (v == 0f || Float.isNaN(v))
                    continue;
                if (i == j)
                    {
                    loops++;
                    continue;
                    }
                outDeg[i]++;
                inDeg[j]++;
                m++;
                }
            }
        int[] outStart = new int[n + 1];
        int[] inStart = new int[n + 1];
        for (int i = 0; i < n; i++)
            {
            outStart[i + 1] = outStart[i] + outDeg[i];
            inStart[i + 1] = inStart[i] + inDeg[i];
            }
        int[] outTarget = new int[(int) m];
        float[] outWeight = new float[(int) m];
        int[] inSource = new int[(int) m];
        float[] inWeight = new float[(int) m];
        int[] inFill = new int[n];
        for (int i = 0; i < n; i++)
            {
            float[] r = rows[i];
            int lim = Math.min(n, r.length);
            int p = outStart[i];
            for (int j = 0; j < lim; j++)
                {
                float v = r[j];
                if (v == 0f || Float.isNaN(v) || i == j)
                    continue;
                outTarget[p] = j;
                outWeight[p++] = v;
                int q = inStart[j] + inFill[j]++;
                inSource[q] = i; // rows are visited in increasing i: sorted
                inWeight[q] = v;
                }
            }
        // undirected union: merge two sorted lists per actor
        int[] nbStart = new int[n + 1];
        int[] tmp = new int[Math.max(1, n)];
        int[][] merged = new int[n][];
        for (int v = 0; v < n; v++)
            {
            int a = outStart[v], ae = outStart[v + 1], b = inStart[v], be = inStart[v + 1], c = 0;
            while (a < ae || b < be)
                {
                int x = a < ae ? outTarget[a] : Integer.MAX_VALUE;
                int y = b < be ? inSource[b] : Integer.MAX_VALUE;
                if (x == y)
                    {
                    tmp[c++] = x;
                    a++;
                    b++;
                    }
                else if (x < y)
                    {
                    tmp[c++] = x;
                    a++;
                    }
                else
                    {
                    tmp[c++] = y;
                    b++;
                    }
                }
            merged[v] = java.util.Arrays.copyOf(tmp, c);
            nbStart[v + 1] = nbStart[v] + c;
            }
        int[] nbTarget = new int[nbStart[n]];
        for (int v = 0; v < n; v++)
            System.arraycopy(merged[v], 0, nbTarget, nbStart[v], merged[v].length);
        return new AdjacencyIndex(n, outStart, outTarget, outWeight, inStart, inSource, inWeight, nbStart,
                nbTarget, loops);
        }

    public int outDegree(int v)
        {
        return outStart[v + 1] - outStart[v];
        }

    public int inDegree(int v)
        {
        return inStart[v + 1] - inStart[v];
        }

    public int degree(int v)
        {
        return nbStart[v + 1] - nbStart[v];
        }

    /** O(log degree) arc test via binary search in the sorted out-list. */
    public boolean hasArc(int u, int v)
        {
        return java.util.Arrays.binarySearch(outTarget, outStart[u], outStart[u + 1], v) >= 0;
        }

    /** Undirected adjacency (a tie in either direction). */
    public boolean adjacent(int u, int v)
        {
        return java.util.Arrays.binarySearch(nbTarget, nbStart[u], nbStart[u + 1], v) >= 0;
        }

    /** Hop distances from s along out-arcs; -1 = unreachable. Reuses dist/queue buffers of length n. */
    public void bfs(int s, int[] dist, int[] queue)
        {
        java.util.Arrays.fill(dist, -1);
        dist[s] = 0;
        int head = 0, tail = 0;
        queue[tail++] = s;
        while (head < tail)
            {
            int u = queue[head++];
            int du = dist[u] + 1;
            for (int p = outStart[u], e = outStart[u + 1]; p < e; p++)
                {
                int v = outTarget[p];
                if (dist[v] < 0)
                    {
                    dist[v] = du;
                    queue[tail++] = v;
                    }
                }
            }
        }
    }
