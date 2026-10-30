/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.equivalence;

import com.bentza.sna.net.Network;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service for Structural Equivalence and Positional Blockmodeling analysis.
 *
 * <p>References:
 * <ul>
 *   <li>Burt, R. S. (1976). Positions in networks. <i>Social Forces</i>, 55(1), 93-122.</li>
 *   <li>Breiger, R. L., Boorman, S. A., &amp; Arabie, P. (1975). An algorithm for clustering relational data with applications to social network analysis and comparison with multidimensional scaling. <i>Journal of Mathematical Psychology</i>, 12(3), 328-383.</li>
 *   <li>Wasserman, S., &amp; Faust, K. (1994). <i>Social Network Analysis: Methods and Applications</i>. Cambridge University Press (Chapter 9: Structural Equivalence).</li>
 * </ul>
 */
public final class StructuralEquivalenceService {

    public static final class EquivalenceResult implements Serializable {
        private static final long serialVersionUID = 1L;
        public final int nodeCount;
        public final double[][] euclideanDistance;
        public final double[][] pearsonCorrelation;
        public final int[] blockAssignments; // 0..K-1
        public final int blockCount;
        public final double[][] blockmodelDensity; // K x K inter-block density matrix
        public final List<List<String>> blockMembers;

        public EquivalenceResult(int n, double[][] dist, double[][] corr, int[] blocks, int k, double[][] image, List<List<String>> members) {
            this.nodeCount = n;
            this.euclideanDistance = dist;
            this.pearsonCorrelation = corr;
            this.blockAssignments = blocks;
            this.blockCount = k;
            this.blockmodelDensity = image;
            this.blockMembers = Collections.unmodifiableList(members);
        }
    }

    private StructuralEquivalenceService() {}

    /**
     * Analyzes structural equivalence across all node pairs and generates a blockmodel partition.
     */
    public static EquivalenceResult analyze(Network net, int targetBlocks) {
        int n = net.getSize();
        if (targetBlocks < 2) targetBlocks = 2;
        if (targetBlocks > n) targetBlocks = Math.max(1, n);

        float[][] mat = net.getMatrix();
        double[][] dist = new double[n][n];
        double[][] corr = new double[n][n];

        // 1. Compute Euclidean Distance and Pearson Profile Correlation
        for (int i = 0; i < n; i++) {
            corr[i][i] = 1.0;
            dist[i][i] = 0.0;
            for (int j = i + 1; j < n; j++) {
                // Build profile vectors for i and j (outgoing and incoming, excluding self/each other)
                List<Double> vecI = new ArrayList<>();
                List<Double> vecJ = new ArrayList<>();

                double sumSq = 0.0;
                for (int k = 0; k < n; k++) {
                    if (k != i && k != j) {
                        // outgoing comparison
                        double outDiff = mat[i][k] - mat[j][k];
                        sumSq += outDiff * outDiff;
                        vecI.add((double) mat[i][k]);
                        vecJ.add((double) mat[j][k]);

                        // incoming comparison
                        double inDiff = mat[k][i] - mat[k][j];
                        sumSq += inDiff * inDiff;
                        vecI.add((double) mat[k][i]);
                        vecJ.add((double) mat[k][j]);
                    }
                }
                double d = Math.sqrt(sumSq);
                dist[i][j] = d;
                dist[j][i] = d;

                double r = computePearson(vecI, vecJ, d);
                corr[i][j] = r;
                corr[j][i] = r;
            }
        }

        // 2. Hierarchical agglomerative clustering (Average linkage on Euclidean distance)
        int[] blocks = hierarchicalCluster(dist, n, targetBlocks);

        // 3. Compute Blockmodel Density Image Matrix
        double[][] image = computeBlockDensity(mat, blocks, targetBlocks, n);

        // 4. Collect member actor names
        List<List<String>> members = new ArrayList<>(targetBlocks);
        for (int b = 0; b < targetBlocks; b++) members.add(new ArrayList<>());
        for (int i = 0; i < n; i++) {
            members.get(blocks[i]).add(net.getActor(i).getName());
        }

        return new EquivalenceResult(n, dist, corr, blocks, targetBlocks, image, members);
    }

    private static double computePearson(List<Double> x, List<Double> y, double dist) {
        int m = x.size();
        if (m == 0 || dist < 1e-9) return 1.0;
        double sumX = 0, sumY = 0;
        for (int i = 0; i < m; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
        }
        double meanX = sumX / m;
        double meanY = sumY / m;

        double cov = 0, varX = 0, varY = 0;
        for (int i = 0; i < m; i++) {
            double dx = x.get(i) - meanX;
            double dy = y.get(i) - meanY;
            cov += dx * dy;
            varX += dx * dx;
            varY += dy * dy;
        }

        double denom = Math.sqrt(varX * varY);
        if (denom < 1e-9) return (dist < 1e-9) ? 1.0 : 0.0;
        double r = cov / denom;
        if (r > 1.0) r = 1.0;
        if (r < -1.0) r = -1.0;
        return r;
    }

    private static int[] hierarchicalCluster(double[][] dist, int n, int targetK) {
        if (n <= targetK) {
            int[] res = new int[n];
            for (int i = 0; i < n; i++) res[i] = i;
            return res;
        }

        // Initialize clusters
        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> c = new ArrayList<>();
            c.add(i);
            clusters.add(c);
        }

        while (clusters.size() > targetK) {
            double minDist = Double.MAX_VALUE;
            int bestA = -1;
            int bestB = -1;

            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double d = averageLinkageDistance(clusters.get(i), clusters.get(j), dist);
                    if (d < minDist) {
                        minDist = d;
                        bestA = i;
                        bestB = j;
                    }
                }
            }

            if (bestA == -1 || bestB == -1) break;

            // Merge bestB into bestA, remove bestB
            clusters.get(bestA).addAll(clusters.get(bestB));
            clusters.remove(bestB);
        }

        int[] assignments = new int[n];
        for (int k = 0; k < clusters.size(); k++) {
            for (int node : clusters.get(k)) {
                assignments[node] = k;
            }
        }
        return assignments;
    }

    private static double averageLinkageDistance(List<Integer> c1, List<Integer> c2, double[][] dist) {
        double sum = 0.0;
        for (int u : c1) {
            for (int v : c2) {
                sum += dist[u][v];
            }
        }
        return sum / (c1.size() * c2.size());
    }

    private static double[][] computeBlockDensity(float[][] mat, int[] blocks, int k, int n) {
        double[][] image = new double[k][k];
        int[][] possibleTies = new int[k][k];
        double[][] actualTies = new double[k][k];

        for (int i = 0; i < n; i++) {
            int bi = blocks[i];
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    int bj = blocks[j];
                    possibleTies[bi][bj]++;
                    if (mat[i][j] > 0) {
                        actualTies[bi][bj] += 1.0;
                    }
                }
            }
        }

        for (int bi = 0; bi < k; bi++) {
            for (int bj = 0; bj < k; bj++) {
                image[bi][bj] = possibleTies[bi][bj] > 0 ? (actualTies[bi][bj] / possibleTies[bi][bj]) : 0.0;
            }
        }
        return image;
    }
}
