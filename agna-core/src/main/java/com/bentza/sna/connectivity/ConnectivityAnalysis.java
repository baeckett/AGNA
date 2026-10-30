/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.connectivity;

import com.bentza.sna.net.Network;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Computes connectivity properties of a network:
 * - Weakly Connected Components (WCC)
 * - Strongly Connected Components (SCC via Tarjan's algorithm)
 * - Articulation Points / Cut-Vertices (Hopcroft-Tarjan algorithm)
 * - Bridges / Cut-Edges
 *
 * References:
 * - Tarjan, R. E. (1972). Depth-first search and linear graph algorithms.
 *   SIAM Journal on Computing, 1(2), 146-160.
 * - Hopcroft, J., &amp; Tarjan, R. (1973). Algorithm 447: efficient algorithms for
 *   graph manipulation. Communications of the ACM, 16(6), 372-378.
 */
public final class ConnectivityAnalysis {

    public static final class Bridge {
        public final int u;
        public final int v;
        public final String uName;
        public final String vName;

        public Bridge(int u, int v, String uName, String vName) {
            this.u = u;
            this.v = v;
            this.uName = uName;
            this.vName = vName;
        }

        @Override
        public String toString() {
            return uName + " -- " + vName;
        }
    }

    public final List<List<Integer>> weakComponents;
    public final List<List<Integer>> strongComponents;
    public final List<Integer> articulationPoints;
    public final List<Bridge> bridges;
    public final int giantComponentSize;
    public final double giantComponentFraction;

    public ConnectivityAnalysis(List<List<Integer>> weakComponents,
                                List<List<Integer>> strongComponents,
                                List<Integer> articulationPoints,
                                List<Bridge> bridges,
                                int giantComponentSize,
                                double giantComponentFraction) {
        this.weakComponents = weakComponents;
        this.strongComponents = strongComponents;
        this.articulationPoints = articulationPoints;
        this.bridges = bridges;
        this.giantComponentSize = giantComponentSize;
        this.giantComponentFraction = giantComponentFraction;
    }

    public static ConnectivityAnalysis analyze(Network net) {
        int n = net.getSize();
        if (n == 0) {
            return new ConnectivityAnalysis(Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), 0, 0.0);
        }

        float[][] mat = net.getMatrix();

        // 1. Weakly Connected Components (undirected BFS)
        List<List<Integer>> weak = new ArrayList<>();
        boolean[] visitedWeak = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (!visitedWeak[i]) {
                List<Integer> comp = new ArrayList<>();
                ArrayDeque<Integer> q = new ArrayDeque<>();
                q.add(i);
                visitedWeak[i] = true;
                while (!q.isEmpty()) {
                    int u = q.poll();
                    comp.add(u);
                    for (int v = 0; v < n; v++) {
                        if (u != v && !visitedWeak[v] && (mat[u][v] != 0f || mat[v][u] != 0f)) {
                            visitedWeak[v] = true;
                            q.add(v);
                        }
                    }
                }
                weak.add(comp);
            }
        }
        // Sort components by descending size
        weak.sort((a, b) -> Integer.compare(b.size(), a.size()));
        int giant = weak.isEmpty() ? 0 : weak.get(0).size();
        double giantFraction = n > 0 ? (double) giant / (double) n : 0.0;

        // 2. Strongly Connected Components (Tarjan's Algorithm)
        List<List<Integer>> strong = runTarjanSCC(mat, n);
        strong.sort((a, b) -> Integer.compare(b.size(), a.size()));

        // 3. Articulation Points & Bridges (Hopcroft-Tarjan on underlying undirected graph)
        List<Integer> artPoints = new ArrayList<>();
        List<Bridge> bridgeList = new ArrayList<>();
        findCutVerticesAndBridges(net, mat, n, artPoints, bridgeList);
        Collections.sort(artPoints);

        return new ConnectivityAnalysis(weak, strong, artPoints, bridgeList, giant, giantFraction);
    }

    private static List<List<Integer>> runTarjanSCC(float[][] mat, int n) {
        List<List<Integer>> scc = new ArrayList<>();
        int[] disc = new int[n];
        int[] low = new int[n];
        boolean[] inStack = new boolean[n];
        Deque<Integer> stack = new ArrayDeque<>();
        java.util.Arrays.fill(disc, -1);
        java.util.Arrays.fill(low, -1);
        int[] timer = new int[]{0};

        for (int i = 0; i < n; i++) {
            if (disc[i] == -1) {
                tarjanDfs(i, mat, n, disc, low, inStack, stack, timer, scc);
            }
        }
        return scc;
    }

    private static void tarjanDfs(int u, float[][] mat, int n, int[] disc, int[] low,
                                  boolean[] inStack, Deque<Integer> stack, int[] timer,
                                  List<List<Integer>> scc) {
        disc[u] = low[u] = ++timer[0];
        stack.push(u);
        inStack[u] = true;

        for (int v = 0; v < n; v++) {
            if (u != v && mat[u][v] != 0f) {
                if (disc[v] == -1) {
                    tarjanDfs(v, mat, n, disc, low, inStack, stack, timer, scc);
                    low[u] = Math.min(low[u], low[v]);
                } else if (inStack[v]) {
                    low[u] = Math.min(low[u], disc[v]);
                }
            }
        }

        if (low[u] == disc[u]) {
            List<Integer> comp = new ArrayList<>();
            while (true) {
                int w = stack.pop();
                inStack[w] = false;
                comp.add(w);
                if (w == u) break;
            }
            scc.add(comp);
        }
    }

    private static void findCutVerticesAndBridges(Network net, float[][] mat, int n,
                                                  List<Integer> artPoints, List<Bridge> bridgeList) {
        int[] disc = new int[n];
        int[] low = new int[n];
        int[] parent = new int[n];
        boolean[] isArt = new boolean[n];
        java.util.Arrays.fill(disc, -1);
        java.util.Arrays.fill(low, -1);
        java.util.Arrays.fill(parent, -1);
        int[] timer = new int[]{0};

        for (int i = 0; i < n; i++) {
            if (disc[i] == -1) {
                bridgeDfs(i, mat, n, disc, low, parent, isArt, timer, bridgeList, net);
            }
        }

        for (int i = 0; i < n; i++) {
            if (isArt[i]) {
                artPoints.add(i);
            }
        }
    }

    private static void bridgeDfs(int u, float[][] mat, int n, int[] disc, int[] low,
                                  int[] parent, boolean[] isArt, int[] timer,
                                  List<Bridge> bridgeList, Network net) {
        disc[u] = low[u] = ++timer[0];
        int children = 0;

        for (int v = 0; v < n; v++) {
            if (u == v || (mat[u][v] == 0f && mat[v][u] == 0f)) continue;

            if (disc[v] == -1) {
                children++;
                parent[v] = u;
                bridgeDfs(v, mat, n, disc, low, parent, isArt, timer, bridgeList, net);

                low[u] = Math.min(low[u], low[v]);

                // Articulation point condition 1: Root of DFS tree with >= 2 children
                if (parent[u] == -1 && children > 1) {
                    isArt[u] = true;
                }
                // Articulation point condition 2: Non-root where low[v] >= disc[u]
                if (parent[u] != -1 && low[v] >= disc[u]) {
                    isArt[u] = true;
                }
                // Bridge condition: low[v] > disc[u]
                if (low[v] > disc[u]) {
                    String uName = net.getActorName(u) != null ? net.getActorName(u).trim() : ("Actor " + (u + 1));
                    String vName = net.getActorName(v) != null ? net.getActorName(v).trim() : ("Actor " + (v + 1));
                    bridgeList.add(new Bridge(u, v, uName, vName));
                }
            } else if (v != parent[u]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }
    }
}
