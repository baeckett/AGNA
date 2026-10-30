/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.spectral;

import com.bentza.sna.net.Network;

/**
 * Computes Spectral and Power Centrality metrics:
 * - Eigenvector Centrality (Bonacich, 1972)
 * - Bonacich Power Centrality c(alpha, beta) (Bonacich, 1987)
 * - PageRank Centrality (Page, Brin, Motwani, &amp; Winograd, 1999)
 *
 * References:
 * - Bonacich, P. (1972). Factoring and weighting approaches to status scores and clique identification.
 *   Journal of Mathematical Sociology, 2(1), 113-120.
 * - Bonacich, P. (1987). Power and centrality: A family of measures.
 *   American Journal of Sociology, 92(5), 1170-1182.
 * - Page, L., Brin, S., Motwani, R., &amp; Winograd, T. (1999). The PageRank citation ranking:
 *   Bringing order to the web. Stanford InfoLab Technical Report.
 */
public final class SpectralCentrality {

    private SpectralCentrality() {}

    /**
     * Computes Eigenvector Centrality using the power iteration method.
     * Treats matrix as symmetric (w_ij + w_ji) for classical undirected eigenvector centrality.
     * Normalized so that the maximum score is 1.0.
     */
    public static double[] computeEigenvector(Network net, int maxIterations, double tolerance) {
        int n = net.getSize();
        if (n == 0) return new double[0];
        if (n == 1) return new double[]{1.0};

        float[][] mat = net.getMatrix();
        double[][] a = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    a[i][j] = (mat[i][j] != 0f || mat[j][i] != 0f) ? (mat[i][j] + mat[j][i]) : 0.0;
                }
            }
        }

        double[] v = new double[n];
        java.util.Arrays.fill(v, 1.0 / Math.sqrt(n));

        for (int iter = 0; iter < maxIterations; iter++) {
            double[] next = new double[n];
            double norm = 0.0;

            for (int i = 0; i < n; i++) {
                // Add positive shift (A + I) to avoid bipartite oscillation
                next[i] = v[i];
                for (int j = 0; j < n; j++) {
                    next[i] += a[i][j] * v[j];
                }
                norm += next[i] * next[i];
            }

            norm = Math.sqrt(norm);
            if (norm <= 1e-12) {
                // Disconnected or zero matrix
                java.util.Arrays.fill(v, 0.0);
                break;
            }

            double diff = 0.0;
            for (int i = 0; i < n; i++) {
                next[i] /= norm;
                diff += Math.abs(next[i] - v[i]);
                v[i] = next[i];
            }

            if (diff < tolerance) {
                break;
            }
        }

        // Normalize so max score is 1.0
        double maxVal = 0.0;
        for (double val : v) {
            if (val > maxVal) maxVal = val;
        }
        if (maxVal > 0) {
            for (int i = 0; i < n; i++) {
                v[i] /= maxVal;
            }
        }

        return v;
    }

    /**
     * Computes Bonacich Power Centrality: c(alpha, beta) = alpha * (I - beta * A)^(-1) * A * 1.
     * Solved via power series expansion / successive approximation:
     * v^(t+1) = A * 1 + beta * A * v^(t).
     */
    public static double[] computeBonacich(Network net, double beta, int maxIter) {
        int n = net.getSize();
        if (n == 0) return new double[0];

        float[][] mat = net.getMatrix();
        double[][] a = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    a[i][j] = (mat[i][j] != 0f || mat[j][i] != 0f) ? 1.0 : 0.0;
                }
            }
        }

        // Base degrees (A * 1)
        double[] base = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                base[i] += a[i][j];
            }
        }

        double[] v = base.clone();

        for (int iter = 0; iter < maxIter; iter++) {
            double[] next = new double[n];
            for (int i = 0; i < n; i++) {
                double alterSum = 0.0;
                for (int j = 0; j < n; j++) {
                    alterSum += a[i][j] * v[j];
                }
                next[i] = base[i] + beta * alterSum;
            }

            double change = 0.0;
            for (int i = 0; i < n; i++) {
                change += Math.abs(next[i] - v[i]);
                v[i] = next[i];
            }
            if (change < 1e-6) break;
        }

        return v;
    }

    /**
     * Computes PageRank Centrality for directed networks using the random walk model:
     * PR(u) = (1 - d) / N + d * sum_{v in In(u)} (PR(v) / OutDeg(v)).
     * Default damping factor is 0.85.
     */
    public static double[] computePageRank(Network net, double dampingFactor, int maxIterations) {
        int n = net.getSize();
        if (n == 0) return new double[0];
        if (n == 1) return new double[]{1.0};

        float[][] mat = net.getMatrix();
        int[] outDeg = new int[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && mat[i][j] != 0f) {
                    outDeg[i]++;
                }
            }
        }

        double[] pr = new double[n];
        java.util.Arrays.fill(pr, 1.0 / n);
        double teleport = (1.0 - dampingFactor) / n;

        for (int iter = 0; iter < maxIterations; iter++) {
            double[] next = new double[n];
            double danglingSum = 0.0;

            for (int i = 0; i < n; i++) {
                if (outDeg[i] == 0) {
                    danglingSum += pr[i];
                }
            }

            for (int i = 0; i < n; i++) {
                double incoming = 0.0;
                for (int j = 0; j < n; j++) {
                    if (j != i && mat[j][i] != 0f && outDeg[j] > 0) {
                        incoming += pr[j] / (double) outDeg[j];
                    }
                }
                next[i] = teleport + dampingFactor * (incoming + danglingSum / n);
            }

            double diff = 0.0;
            for (int i = 0; i < n; i++) {
                diff += Math.abs(next[i] - pr[i]);
                pr[i] = next[i];
            }
            if (diff < 1e-7) break;
        }

        return pr;
    }
}
