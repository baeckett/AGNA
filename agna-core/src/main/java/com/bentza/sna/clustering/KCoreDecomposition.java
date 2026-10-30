/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.clustering;

import com.bentza.sna.net.Network;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Computes k-core decomposition of a network using the linear-time algorithm of
 * Batagelj and Zaveršnik (2003).
 *
 * A k-core is a maximal subgraph in which every vertex has degree at least k.
 *
 * Reference:
 * - Seidman, S. B. (1983). Network structure and minimum degree.
 *   Social Networks, 5(3), 269-287.
 * - Batagelj, V., &amp; Zaveršnik, M. (2003). An O(m) algorithm for cores
 *   decomposition of networks. arXiv:cs/0310049.
 */
public final class KCoreDecomposition {

    private KCoreDecomposition() {}

    public static final class KCoreResult {
        public final int[] coreness;
        public final int maxCore;
        public final Map<Integer, Integer> coreDistribution; // core -> count of actors with this coreness
        public final Map<Integer, List<String>> coreMembers;

        public KCoreResult(int[] coreness, int maxCore,
                           Map<Integer, Integer> coreDistribution,
                           Map<Integer, List<String>> coreMembers) {
            this.coreness = coreness;
            this.maxCore = maxCore;
            this.coreDistribution = coreDistribution;
            this.coreMembers = coreMembers;
        }
    }

    /**
     * Performs k-core decomposition on the given network.
     * Treats ties as undirected for standard topological core decomposition.
     */
    public static KCoreResult decompose(Network net) {
        int n = net.getSize();
        if (n == 0) {
            return new KCoreResult(new int[0], 0, Collections.emptyMap(), Collections.emptyMap());
        }

        float[][] mat = net.getMatrix();
        // Build neighbor lists
        List<List<Integer>> adj = new ArrayList<>(n);
        int[] deg = new int[n];
        int maxDeg = 0;

        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (mat[i][j] != 0f || mat[j][i] != 0f) {
                    adj.get(i).add(j);
                    adj.get(j).add(i);
                    deg[i]++;
                    deg[j]++;
                }
            }
            if (deg[i] > maxDeg) {
                maxDeg = deg[i];
            }
        }

        // Batagelj-Zaveršnik bucket sort
        int[] bin = new int[maxDeg + 1];
        for (int i = 0; i < n; i++) {
            bin[deg[i]]++;
        }

        int start = 0;
        for (int d = 0; d <= maxDeg; d++) {
            int num = bin[d];
            bin[d] = start;
            start += num;
        }

        int[] pos = new int[n];
        int[] vert = new int[n];
        for (int i = 0; i < n; i++) {
            pos[i] = bin[deg[i]];
            vert[pos[i]] = i;
            bin[deg[i]]++;
        }

        for (int d = maxDeg; d >= 1; d--) {
            bin[d] = bin[d - 1];
        }
        bin[0] = 0;

        // Core extraction
        int[] coreness = new int[n];
        int maxCore = 0;

        for (int i = 0; i < n; i++) {
            int v = vert[i];
            coreness[v] = deg[v];
            if (coreness[v] > maxCore) {
                maxCore = coreness[v];
            }

            for (int u : adj.get(v)) {
                if (deg[u] > deg[v]) {
                    int du = deg[u];
                    int pu = pos[u];
                    int pw = bin[du];
                    int w = vert[pw];
                    if (u != w) {
                        pos[u] = pw;
                        vert[pu] = w;
                        pos[w] = pu;
                        vert[pw] = u;
                    }
                    bin[du]++;
                    deg[u]--;
                }
            }
        }

        Map<Integer, Integer> distribution = new TreeMap<>();
        Map<Integer, List<String>> members = new TreeMap<>();
        for (int k = 0; k <= maxCore; k++) {
            distribution.put(k, 0);
            members.put(k, new ArrayList<>());
        }

        for (int i = 0; i < n; i++) {
            int c = coreness[i];
            distribution.put(c, distribution.get(c) + 1);
            String name = (net.getActorName(i) != null && !net.getActorName(i).trim().isEmpty())
                    ? net.getActorName(i).trim() : ("Actor " + (i + 1));
            members.get(c).add(name);
        }

        return new KCoreResult(coreness, maxCore, distribution, members);
    }
}
