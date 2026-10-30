/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.clustering;

import com.bentza.sna.net.Network;

/**
 * Computes Watts-Strogatz local clustering coefficients and Newman global transitivity.
 * 
 * References:
 * - Watts, D. J., &amp; Strogatz, S. H. (1998). Collective dynamics of 'small-world' networks.
 *   Nature, 393(6684), 440-442.
 * - Newman, M. E. (2003). The structure and function of complex networks.
 *   SIAM Review, 45(2), 167-256.
 */
public final class ClusteringMetrics {

    private ClusteringMetrics() {}

    /**
     * Container for actor-level clustering results.
     */
    public static final class LocalClusteringResult {
        public final int actorIndex;
        public final String actorName;
        public final int degree;
        public final int triangles;
        public final int maxTriangles;
        public final double clusteringCoefficient;

        public LocalClusteringResult(int actorIndex, String actorName, int degree,
                                     int triangles, int maxTriangles, double clusteringCoefficient) {
            this.actorIndex = actorIndex;
            this.actorName = actorName;
            this.degree = degree;
            this.triangles = triangles;
            this.maxTriangles = maxTriangles;
            this.clusteringCoefficient = clusteringCoefficient;
        }
    }

    /**
     * Summary container for local clustering analysis across the entire network.
     */
    public static final class LocalClusteringSummary {
        public final LocalClusteringResult[] actorResults;
        public final double meanClustering;
        public final int totalTriangles;
        public final int isolatedOrSingleDegreeCount;

        public LocalClusteringSummary(LocalClusteringResult[] actorResults,
                                      double meanClustering,
                                      int totalTriangles,
                                      int isolatedOrSingleDegreeCount) {
            this.actorResults = actorResults;
            this.meanClustering = meanClustering;
            this.totalTriangles = totalTriangles;
            this.isolatedOrSingleDegreeCount = isolatedOrSingleDegreeCount;
        }
    }

    /**
     * Container for network-level transitivity (ratio of 3 * triangles / connected triples).
     */
    public static final class TransitivityResult {
        public final long closedTriplets; // 3 * triangles
        public final long connectedTriplets;
        public final long totalTriangles;
        public final double transitivity;

        public TransitivityResult(long closedTriplets, long connectedTriplets,
                                  long totalTriangles, double transitivity) {
            this.closedTriplets = closedTriplets;
            this.connectedTriplets = connectedTriplets;
            this.totalTriangles = totalTriangles;
            this.transitivity = transitivity;
        }
    }

    /**
     * Computes the Watts-Strogatz local clustering coefficient C_i for each actor:
     * C_i = 2 * T_i / (k_i * (k_i - 1)), where T_i is triangles containing i, and k_i is degree.
     * If k_i &lt; 2, C_i = 0.0.
     */
    public static LocalClusteringSummary computeLocalClustering(Network net) {
        int n = net.getSize();
        float[][] mat = net.getMatrix();
        LocalClusteringResult[] results = new LocalClusteringResult[n];

        // Symmetric adjacency view for clustering
        boolean[][] adj = new boolean[n][n];
        int[] deg = new int[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && (mat[i][j] != 0f || mat[j][i] != 0f)) {
                    adj[i][j] = true;
                    deg[i]++;
                }
            }
        }

        double sumC = 0.0;
        int isolatedOrLeaf = 0;
        int totalTriangles = 0;

        for (int i = 0; i < n; i++) {
            int k = deg[i];
            int triangles = 0;
            int maxTri = k >= 2 ? (k * (k - 1) / 2) : 0;
            double c = 0.0;

            if (k >= 2) {
                for (int u = 0; u < n; u++) {
                    if (u != i && adj[i][u]) {
                        for (int v = u + 1; v < n; v++) {
                            if (v != i && adj[i][v] && adj[u][v]) {
                                triangles++;
                            }
                        }
                    }
                }
                c = (double) triangles / (double) maxTri;
                sumC += c;
            } else {
                isolatedOrLeaf++;
            }

            String name = (net.getActorName(i) != null && !net.getActorName(i).trim().isEmpty())
                    ? net.getActorName(i).trim() : ("Actor " + (i + 1));
            results[i] = new LocalClusteringResult(i, name, k, triangles, maxTri, c);
        }

        // Total unique triangles in graph = sum of local triangles / 3
        long sumLocalTriangles = 0;
        for (LocalClusteringResult r : results) {
            sumLocalTriangles += r.triangles;
        }
        totalTriangles = (int) (sumLocalTriangles / 3);

        double meanC = n > 0 ? (sumC / n) : 0.0;
        return new LocalClusteringSummary(results, meanC, totalTriangles, isolatedOrLeaf);
    }

    /**
     * Computes Newman's global network transitivity:
     * T = (3 * Number of Triangles) / (Number of Connected Triples)
     */
    public static TransitivityResult computeTransitivity(Network net) {
        int n = net.getSize();
        if (n < 3) {
            return new TransitivityResult(0, 0, 0, 0.0);
        }

        float[][] mat = net.getMatrix();
        boolean[][] adj = new boolean[n][n];
        int[] deg = new int[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && (mat[i][j] != 0f || mat[j][i] != 0f)) {
                    adj[i][j] = true;
                    deg[i]++;
                }
            }
        }

        long totalTriangles = 0;
        long connectedTriplets = 0;

        for (int i = 0; i < n; i++) {
            if (deg[i] >= 2) {
                connectedTriplets += (long) deg[i] * (deg[i] - 1) / 2;
            }
            for (int j = i + 1; j < n; j++) {
                if (adj[i][j]) {
                    for (int k = j + 1; k < n; k++) {
                        if (adj[i][k] && adj[j][k]) {
                            totalTriangles++;
                        }
                    }
                }
            }
        }

        long closedTriplets = 3 * totalTriangles;
        double t = connectedTriplets > 0 ? ((double) closedTriplets / (double) connectedTriplets) : 0.0;
        return new TransitivityResult(closedTriplets, connectedTriplets, totalTriangles, t);
    }
}
