/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.infra;

import java.util.Arrays;

/**
 * Self-contained linear algebra and discrete optimization algorithms for SNA:
 * - Symmetric Eigenvalue decomposition (Householder tridiagonalization + implicit QL tred2/tqli)
 * - Kuhn-Munkres (Hungarian) minimum weight bipartite matching
 * - DeltaCon fast power-series solver
 * - Matrix and vector norms, metrics, and distance measures
 *
 * <p>Designed without third-party dependencies for instant GraalVM native image execution.
 */
public final class LinearAlgebra {

    private LinearAlgebra() {}

    /**
     * Container holding eigenvalues and orthonormal eigenvectors of a symmetric matrix.
     */
    public static final class EigenResult {
        public final double[] values;       // Eigenvalues (sorted ascending or descending)
        public final double[][] vectors;    // Eigenvectors as column vectors: vectors[row][col]

        public EigenResult(double[] values, double[][] vectors) {
            this.values = values;
            this.vectors = vectors;
        }
    }

    /**
     * Computes the complete eigendecomposition of a real symmetric n x n matrix.
     * Uses Householder reduction to tridiagonal form (tred2) followed by
     * the QL algorithm with implicit shifts (tqli).
     *
     * @param a Real symmetric matrix (n x n)
     * @param ascending If true, sorts eigenvalues in ascending order; if false, descending.
     * @return EigenResult with eigenvalues and eigenvector matrix.
     */
    public static EigenResult eigenSymmetric(double[][] a, boolean ascending) {
        int n = a.length;
        if (n == 0) {
            return new EigenResult(new double[0], new double[0][0]);
        }
        if (n == 1) {
            return new EigenResult(new double[]{a[0][0]}, new double[][]{{1.0}});
        }

        double[][] v = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, v[i], 0, n);
        }

        double[] d = new double[n];
        double[] e = new double[n];

        // 1. Householder reduction to tridiagonal form
        tred2(v, n, d, e);

        // 2. QL algorithm with implicit shifts
        tqli(d, e, n, v);

        // 3. Sort eigenvalues and permute eigenvector columns accordingly
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;

        if (ascending) {
            Arrays.sort(order, (i1, i2) -> Double.compare(d[i1], d[i2]));
        } else {
            Arrays.sort(order, (i1, i2) -> Double.compare(d[i2], d[i1]));
        }

        double[] sortedD = new double[n];
        double[][] sortedV = new double[n][n];
        for (int j = 0; j < n; j++) {
            int srcCol = order[j];
            sortedD[j] = d[srcCol];
            for (int i = 0; i < n; i++) {
                sortedV[i][j] = v[i][srcCol];
            }
        }

        return new EigenResult(sortedD, sortedV);
    }

    private static void tred2(double[][] v, int n, double[] d, double[] e) {
        for (int i = 0; i < n; i++) {
            d[i] = v[n - 1][i];
        }

        for (int i = n - 1; i > 0; i--) {
            double scale = 0.0;
            double h = 0.0;
            for (int k = 0; k < i; k++) {
                scale += Math.abs(d[k]);
            }
            if (scale == 0.0) {
                e[i] = d[i - 1];
                for (int j = 0; j < i; j++) {
                    d[j] = v[i - 1][j];
                    v[i][j] = 0.0;
                    v[j][i] = 0.0;
                }
            } else {
                for (int k = 0; k < i; k++) {
                    d[k] /= scale;
                    h += d[k] * d[k];
                }
                double f = d[i - 1];
                double g = Math.sqrt(h);
                if (f > 0) g = -g;
                e[i] = scale * g;
                h -= f * g;
                d[i - 1] = f - g;
                for (int j = 0; j < i; j++) {
                    e[j] = 0.0;
                }
                for (int j = 0; j < i; j++) {
                    f = d[j];
                    v[j][i] = f;
                    g = e[j] + v[j][j] * f;
                    for (int k = j + 1; k < i; k++) {
                        g += v[k][j] * d[k];
                        e[k] += v[k][j] * f;
                    }
                    e[j] = g;
                }
                f = 0.0;
                for (int j = 0; j < i; j++) {
                    e[j] /= h;
                    f += e[j] * d[j];
                }
                double hh = f / (h + h);
                for (int j = 0; j < i; j++) {
                    e[j] -= hh * d[j];
                }
                for (int j = 0; j < i; j++) {
                    f = d[j];
                    g = e[j];
                    for (int k = j; k < i; k++) {
                        v[k][j] -= (f * e[k] + g * d[k]);
                    }
                    d[j] = v[i - 1][j];
                    v[i][j] = 0.0;
                }
            }
            d[i] = h;
        }

        for (int i = 0; i < n - 1; i++) {
            v[n - 1][i] = v[i][i];
            v[i][i] = 1.0;
            double h = d[i + 1];
            if (h != 0.0) {
                for (int k = 0; k <= i; k++) {
                    d[k] = v[k][i + 1] / h;
                }
                for (int j = 0; j <= i; j++) {
                    double g = 0.0;
                    for (int k = 0; k <= i; k++) {
                        g += v[k][i + 1] * v[k][j];
                    }
                    for (int k = 0; k <= i; k++) {
                        v[k][j] -= g * d[k];
                    }
                }
            }
            for (int k = 0; k <= i; k++) {
                v[k][i + 1] = 0.0;
            }
        }
        for (int j = 0; j < n; j++) {
            d[j] = v[n - 1][j];
            v[n - 1][j] = 0.0;
        }
        v[n - 1][n - 1] = 1.0;
        e[0] = 0.0;
    }

    private static void tqli(double[] d, double[] e, int n, double[][] v) {
        for (int i = 1; i < n; i++) {
            e[i - 1] = e[i];
        }
        e[n - 1] = 0.0;

        for (int l = 0; l < n; l++) {
            int iter = 0;
            int m;
            do {
                for (m = l; m < n - 1; m++) {
                    double dd = Math.abs(d[m]) + Math.abs(d[m + 1]);
                    if (Math.abs(e[m]) + dd == dd) break;
                }
                if (m != l) {
                    if (iter++ == 40) return; // Iteration safeguard
                    double g = (d[l + 1] - d[l]) / (2.0 * e[l]);
                    double r = Math.hypot(g, 1.0);
                    if (g < 0) g = d[m] - d[l] + e[l] / (g - r);
                    else g = d[m] - d[l] + e[l] / (g + r);
                    double s = 1.0;
                    double c = 1.0;
                    double p = 0.0;
                    for (int i = m - 1; i >= l; i--) {
                        double f = s * e[i];
                        double b = c * e[i];
                        r = Math.hypot(f, g);
                        e[i + 1] = r;
                        if (r == 0.0) {
                            d[i + 1] -= p;
                            e[m] = 0.0;
                            break;
                        }
                        s = f / r;
                        c = g / r;
                        g = d[i + 1] - p;
                        r = (d[i] - g) * s + 2.0 * c * b;
                        p = s * r;
                        d[i + 1] = g + p;
                        g = c * r - b;
                        for (int k = 0; k < n; k++) {
                            f = v[k][i + 1];
                            v[k][i + 1] = s * v[k][i] + c * f;
                            v[k][i] = c * v[k][i] - s * f;
                        }
                    }
                    if (r == 0.0) continue;
                    d[l] -= p;
                    e[l] = g;
                    e[m] = 0.0;
                }
            } while (m != l);
        }
    }

    /**
     * Solves the minimum weight bipartite matching problem (assignment problem)
     * using the Kuhn-Munkres (Hungarian) algorithm in O(N^3).
     *
     * @param costMatrix Matrix of non-negative edge costs between workers (rows) and jobs (cols).
     * @return Array matching[row] = col, or -1 if row is unmatched.
     */
    public static int[] hungarianAssignment(double[][] costMatrix) {
        int rows = costMatrix.length;
        if (rows == 0) return new int[0];
        int cols = costMatrix[0].length;
        int n = Math.max(rows, cols);

        // Pad to square n x n with zeros or large values
        double[][] a = new double[n + 1][n + 1];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                a[i + 1][j + 1] = costMatrix[i][j];
            }
        }

        double[] u = new double[n + 1];
        double[] v = new double[n + 1];
        int[] p = new int[n + 1];
        int[] way = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            double[] minv = new double[n + 1];
            Arrays.fill(minv, Double.POSITIVE_INFINITY);
            boolean[] used = new boolean[n + 1];

            do {
                used[j0] = true;
                int i0 = p[j0];
                double delta = Double.POSITIVE_INFINITY;
                int j1 = 0;

                for (int j = 1; j <= n; j++) {
                    if (!used[j]) {
                        double cur = a[i0][j] - u[i0] - v[j];
                        if (cur < minv[j]) {
                            minv[j] = cur;
                            way[j] = j0;
                        }
                        if (minv[j] < delta) {
                            delta = minv[j];
                            j1 = j;
                        }
                    }
                }

                for (int j = 0; j <= n; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minv[j] -= delta;
                    }
                }
                j0 = j1;
            } while (p[j0] != 0);

            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        int[] result = new int[rows];
        Arrays.fill(result, -1);
        for (int j = 1; j <= cols; j++) {
            if (p[j] <= rows && p[j] > 0) {
                result[p[j] - 1] = j - 1;
            }
        }
        return result;
    }

    /**
     * DeltaCon fast power-series affinity matrix approximation:
     * S = [I + eps^2 * D - eps * A]^-1 approx I + eps * A + eps^2 * (A^2 - D).
     *
     * Reference: Koutra et al. (2013), "DeltaCon: A Principled Massive-Graph Similarity Function".
     */
    public static double[][] deltaConAffinity(double[][] a, double eps) {
        int n = a.length;
        double[][] s = new double[n][n];

        // Compute degrees D_ii
        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i] += a[i][j];
            }
        }

        // A^2 computation
        double[][] a2 = multiply(a, a);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double val = (i == j ? 1.0 : 0.0);
                val += eps * a[i][j];
                double a2Term = a2[i][j] - (i == j ? d[i] : 0.0);
                val += eps * eps * a2Term;
                s[i][j] = Math.max(0.0, val);
            }
        }
        return s;
    }

    /**
     * Matrix multiplication C = A * B.
     */
    public static double[][] multiply(double[][] a, double[][] b) {
        int rows = a.length;
        int cols = b[0].length;
        int common = a[0].length;
        double[][] c = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int k = 0; k < common; k++) {
                double aik = a[i][k];
                if (aik != 0.0) {
                    for (int j = 0; j < cols; j++) {
                        c[i][j] += aik * b[k][j];
                    }
                }
            }
        }
        return c;
    }

    /**
     * Matusita (root Euclidean) distance between two affinity matrices:
     * Dist = sqrt(sum((sqrt(S1_ij) - sqrt(S2_ij))^2)).
     */
    public static double matusitaDistance(double[][] s1, double[][] s2) {
        int n = s1.length;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double diff = Math.sqrt(Math.max(0.0, s1[i][j])) - Math.sqrt(Math.max(0.0, s2[i][j]));
                sum += diff * diff;
            }
        }
        return Math.sqrt(sum);
    }

    /**
     * Canberra distance between two feature vectors:
     * sum(|u_i - v_i| / (|u_i| + |v_i|)).
     */
    public static double canberraDistance(double[] u, double[] v) {
        int n = Math.min(u.length, v.length);
        double dist = 0.0;
        for (int i = 0; i < n; i++) {
            double denom = Math.abs(u[i]) + Math.abs(v[i]);
            if (denom > 1e-12) {
                dist += Math.abs(u[i] - v[i]) / denom;
            }
        }
        return dist;
    }

    /**
     * Computes the Fiedler vector (eigenvector corresponding to the second smallest
     * eigenvalue of the graph Laplacian L = D - A).
     */
    public static double[] fiedlerVector(double[][] a) {
        int n = a.length;
        if (n <= 1) return new double[n];

        double[][] l = new double[n][n];
        for (int i = 0; i < n; i++) {
            double deg = 0.0;
            for (int j = 0; j < n; j++) {
                double w = (a[i][j] + a[j][i]) / 2.0; // Symmetrized
                l[i][j] = -w;
                deg += w;
            }
            l[i][i] = deg;
        }

        EigenResult er = eigenSymmetric(l, true); // Sorted ascending: 0 = lambda_1 <= lambda_2 <= ...
        double[] fiedler = new double[n];
        int col = Math.min(1, er.values.length - 1);
        for (int i = 0; i < n; i++) {
            fiedler[i] = er.vectors[i][col];
        }
        return fiedler;
    }
}
