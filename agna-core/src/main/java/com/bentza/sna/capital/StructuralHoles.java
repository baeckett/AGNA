/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.capital;

import com.bentza.sna.net.Network;

/**
 * Computes Ronald Burt's Social Capital and Structural Holes metrics:
 * - Dyadic Constraint (c_ij)
 * - Aggregate Constraint (C_i)
 * - Effective Size of Ego Network (S_i)
 * - Efficiency (E_i = S_i / k_i)
 * - Hierarchy (H_i)
 *
 * Reference:
 * - Burt, R. S. (1992). Structural Holes: The Social Structure of Competition.
 *   Harvard University Press, Cambridge, MA.
 * - Borgatti, S. P. (1997). Structural holes: Unpacking Burt's redundancy measures.
 *   CONNECTIONS, 20(1), 35-38.
 */
public final class StructuralHoles {

    public static final class ActorHolesMetrics {
        public final int actorIndex;
        public final String actorName;
        public final int degree;
        public final double effectiveSize;
        public final double efficiency;
        public final double constraint;
        public final double hierarchy;

        public ActorHolesMetrics(int actorIndex, String actorName, int degree,
                                 double effectiveSize, double efficiency,
                                 double constraint, double hierarchy) {
            this.actorIndex = actorIndex;
            this.actorName = actorName;
            this.degree = degree;
            this.effectiveSize = effectiveSize;
            this.efficiency = efficiency;
            this.constraint = constraint;
            this.hierarchy = hierarchy;
        }
    }

    public final ActorHolesMetrics[] actorMetrics;
    public final double meanConstraint;
    public final double meanEffectiveSize;
    public final double meanEfficiency;
    public final double[][] dyadicConstraint;

    public StructuralHoles(ActorHolesMetrics[] actorMetrics, double meanConstraint,
                           double meanEffectiveSize, double meanEfficiency,
                           double[][] dyadicConstraint) {
        this.actorMetrics = actorMetrics;
        this.meanConstraint = meanConstraint;
        this.meanEffectiveSize = meanEffectiveSize;
        this.meanEfficiency = meanEfficiency;
        this.dyadicConstraint = dyadicConstraint;
    }

    public static StructuralHoles compute(Network net) {
        int n = net.getSize();
        float[][] mat = net.getMatrix();
        double[][] w = new double[n][n];

        // Symmetrized tie weight for structural holes analysis
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    w[i][j] = mat[i][j] + mat[j][i];
                }
            }
        }

        // Proportional tie investments p_ij = w_ij / sum_k(w_ik)
        double[][] p = new double[n][n];
        int[] degree = new int[n];
        for (int i = 0; i < n; i++) {
            double sumW = 0.0;
            for (int j = 0; j < n; j++) {
                sumW += w[i][j];
                if (w[i][j] > 0) {
                    degree[i]++;
                }
            }
            if (sumW > 0) {
                for (int j = 0; j < n; j++) {
                    p[i][j] = w[i][j] / sumW;
                }
            }
        }

        // Marginal strength m_jq = w_jq / max_k(w_jk)
        double[][] m = new double[n][n];
        for (int j = 0; j < n; j++) {
            double maxW = 0.0;
            for (int k = 0; k < n; k++) {
                if (w[j][k] > maxW) maxW = w[j][k];
            }
            if (maxW > 0) {
                for (int q = 0; q < n; q++) {
                    m[j][q] = w[j][q] / maxW;
                }
            }
        }

        // Dyadic constraint c_ij = (p_ij + sum_q(p_iq * p_qj))^2
        double[][] c = new double[n][n];
        ActorHolesMetrics[] metrics = new ActorHolesMetrics[n];
        double sumConstraint = 0.0;
        double sumEffSize = 0.0;
        double sumEfficiency = 0.0;

        for (int i = 0; i < n; i++) {
            int k = degree[i];
            double totalConstraint = 0.0;
            double effectiveSize = 0.0;

            if (k > 0) {
                for (int j = 0; j < n; j++) {
                    if (w[i][j] > 0) {
                        double indirect = 0.0;
                        for (int q = 0; q < n; q++) {
                            if (q != i && q != j) {
                                indirect += p[i][q] * p[q][j];
                            }
                        }
                        double cij = Math.pow(p[i][j] + indirect, 2);
                        c[i][j] = cij;
                        totalConstraint += cij;

                        // Redundancy calculation for effective size
                        double redundancy = 0.0;
                        for (int q = 0; q < n; q++) {
                            if (q != i && q != j) {
                                redundancy += p[i][q] * m[j][q];
                            }
                        }
                        effectiveSize += (1.0 - redundancy);
                    }
                }
            }

            double efficiency = k > 0 ? (effectiveSize / (double) k) : 0.0;

            // Hierarchy H_i: concentration of constraint across alters
            double hierarchy = 0.0;
            if (k > 1 && totalConstraint > 0) {
                double meanC = totalConstraint / (double) k;
                double hSum = 0.0;
                for (int j = 0; j < n; j++) {
                    if (w[i][j] > 0 && c[i][j] > 0) {
                        double ratio = c[i][j] / meanC;
                        if (ratio > 0) {
                            hSum += ratio * Math.log(ratio);
                        }
                    }
                }
                hierarchy = hSum / ((double) k * Math.log(k));
                if (hierarchy < 0.0) hierarchy = 0.0;
            }

            sumConstraint += totalConstraint;
            sumEffSize += effectiveSize;
            sumEfficiency += efficiency;

            String name = (net.getActorName(i) != null && !net.getActorName(i).trim().isEmpty())
                    ? net.getActorName(i).trim() : ("Actor " + (i + 1));
            metrics[i] = new ActorHolesMetrics(i, name, k, effectiveSize, efficiency, totalConstraint, hierarchy);
        }

        double meanC = n > 0 ? (sumConstraint / n) : 0.0;
        double meanS = n > 0 ? (sumEffSize / n) : 0.0;
        double meanE = n > 0 ? (sumEfficiency / n) : 0.0;

        return new StructuralHoles(metrics, meanC, meanS, meanE, c);
    }
}
