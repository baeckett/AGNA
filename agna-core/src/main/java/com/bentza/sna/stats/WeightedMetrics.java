/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

import com.bentza.sna.net.Network;
import java.io.Serializable;

/**
 * Computes valued / weighted network metrics:
 * - Node Strength (in-strength, out-strength, total strength)
 * - Valued Density vs. Binary Density
 * - Barrat et al. (2004) Weighted Local Clustering Coefficient
 *
 * <p>References:
 * <ul>
 *   <li>Barrat, A., Barthélemy, M., Pastor-Satorras, R., &amp; Vespignani, A. (2004). The architecture of complex weighted networks. <i>PNAS</i>, 101(11), 3747-3752.</li>
 *   <li>Opsahl, T., &amp; Panzarasa, P. (2009). Clustering in weighted networks. <i>Social Networks</i>, 31(2), 155-163.</li>
 * </ul>
 */
public final class WeightedMetrics {

    public static final class WeightedSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        public final int nodeCount;
        public final float[] inStrength;
        public final float[] outStrength;
        public final float[] totalStrength;
        public final float[] weightedClustering;
        public final double meanWeightedClustering;
        public final double binaryDensity;
        public final double valuedDensity;
        public final double meanTieWeight;

        public WeightedSummary(int n, float[] inStr, float[] outStr, float[] totStr, float[] wClust, double meanWClust, double binDens, double valDens, double meanWeight) {
            this.nodeCount = n;
            this.inStrength = inStr;
            this.outStrength = outStr;
            this.totalStrength = totStr;
            this.weightedClustering = wClust;
            this.meanWeightedClustering = meanWClust;
            this.binaryDensity = binDens;
            this.valuedDensity = valDens;
            this.meanTieWeight = meanWeight;
        }
    }

    private WeightedMetrics() {}

    /**
     * Computes node strengths, valued density, and Barrat weighted clustering for the network.
     */
    public static WeightedSummary compute(Network net) {
        int n = net.getSize();
        float[][] mat = net.getMatrix();

        float[] inStr = new float[n];
        float[] outStr = new float[n];
        float[] totStr = new float[n];
        int[] degree = new int[n];

        // Symmetrized weights for Barrat clustering
        float[][] symW = new float[n][n];
        boolean[][] adj = new boolean[n][n];

        double sumTotalWeights = 0.0;
        int tieCount = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && mat[i][j] > 0) {
                    float w = mat[i][j];
                    outStr[i] += w;
                    inStr[j] += w;
                    sumTotalWeights += w;
                    tieCount++;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            totStr[i] = inStr[i] + outStr[i];
            for (int j = i + 1; j < n; j++) {
                float w = Math.max(mat[i][j], mat[j][i]);
                if (w > 0) {
                    symW[i][j] = w;
                    symW[j][i] = w;
                    adj[i][j] = true;
                    adj[j][i] = true;
                    degree[i]++;
                    degree[j]++;
                }
            }
        }

        double possibleTies = (double) n * (n - 1);
        double binDens = possibleTies > 0 ? ((double) tieCount / possibleTies) : 0.0;
        double valDens = possibleTies > 0 ? (sumTotalWeights / possibleTies) : 0.0;
        double meanWeight = tieCount > 0 ? (sumTotalWeights / tieCount) : 0.0;

        // Barrat et al. (2004) Weighted Local Clustering:
        // C_i^w = 1 / (s_i * (k_i - 1)) * sum_{j, h} [ (w_ij + w_ih) / 2 * a_ij * a_ih * a_jh ]
        float[] wClust = new float[n];
        double sumC = 0.0;
        int validCount = 0;

        for (int i = 0; i < n; i++) {
            int k = degree[i];
            float s = 0.0f;
            for (int j = 0; j < n; j++) s += symW[i][j];

            if (k < 2 || s <= 0.0f) {
                wClust[i] = 0.0f;
            } else {
                double triSum = 0.0;
                for (int j = 0; j < n; j++) {
                    if (adj[i][j]) {
                        for (int h = j + 1; h < n; h++) {
                            if (adj[i][h] && adj[j][h]) {
                                triSum += (symW[i][j] + symW[i][h]);
                            }
                        }
                    }
                }
                double denom = (double) s * (k - 1);
                double cVal = denom > 0 ? (triSum / denom) : 0.0;
                wClust[i] = (float) Math.min(1.0, Math.max(0.0, cVal));
                sumC += wClust[i];
                validCount++;
            }
        }
        double meanWClust = validCount > 0 ? (sumC / n) : 0.0;

        return new WeightedSummary(n, inStr, outStr, totStr, wClust, meanWClust, binDens, valDens, meanWeight);
    }
}
