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
        // 2.2.2: Lanczos with full reorthogonalisation and restarts on the
        // symmetrised weights a_ij = w_ij + w_ji (same model as before).
        // Plain power iteration (2.2.5) stopped after 100-150 steps and was
        // badly unconverged on graphs with a small spectral gap (paths,
        // ladders, lattices, sparse small worlds): on a 500-node path the end
        // nodes scored 0.97 instead of 0.15. Lanczos converges with the
        // square root of the gap and is checked by its residual. The
        // maxIterations / tolerance arguments are kept for compatibility.
        final com.bentza.sna.net.AdjacencyIndex g = com.bentza.sna.net.AdjacencyIndex.of(net);
        java.util.function.BiConsumer<double[], double[]> mul = (x, y) -> {
            for (int r = 0; r < n; r++) {
                double sum = 0.0;
                for (int p = g.outStart[r], e = g.outStart[r + 1]; p < e; p++) sum += g.outWeight[p] * x[g.outTarget[p]];
                for (int p = g.inStart[r], e = g.inStart[r + 1]; p < e; p++) sum += g.inWeight[p] * x[g.inSource[p]];
                y[r] = sum;
            }
        };
        double[] start = new double[n];
        java.util.Arrays.fill(start, 1.0 / Math.sqrt(n));
        double[] best = start;
        int k = Math.min(n, 60);
        double[] ax = new double[n];
        for (int cycle = 0; cycle < 40; cycle++) {
            double[][] q = new double[k + 1][];
            double[] alpha = new double[k], beta = new double[k];
            q[0] = best.clone();
            normalize(q[0]);
            int m = 0;
            double[] w = new double[n];
            for (int jj = 0; jj < k; jj++) {
                mul.accept(q[jj], w);
                double a = dot(q[jj], w);
                alpha[jj] = a;
                for (int r = 0; r < n; r++) w[r] -= a * q[jj][r] + (jj > 0 ? beta[jj - 1] * q[jj - 1][r] : 0.0);
                for (int pass = 0; pass < 2; pass++)          // full reorthogonalisation
                    for (int ii = 0; ii <= jj; ii++) {
                        double c = dot(q[ii], w);
                        for (int r = 0; r < n; r++) w[r] -= c * q[ii][r];
                    }
                m = jj + 1;
                double b = Math.sqrt(dot(w, w));
                if (b < 1e-12 || jj == k - 1) { beta[jj] = b; break; }
                beta[jj] = b;
                q[jj + 1] = new double[n];
                for (int r = 0; r < n; r++) q[jj + 1][r] = w[r] / b;
            }
            double[][] t = new double[m][m];
            for (int a = 0; a < m; a++) {
                t[a][a] = alpha[a];
                if (a + 1 < m) { t[a][a + 1] = beta[a]; t[a + 1][a] = beta[a]; }
            }
            com.bentza.sna.infra.LinearAlgebra.EigenResult er = com.bentza.sna.infra.LinearAlgebra.eigenSymmetric(t, false);
            double theta = er.values[0];
            double[] x = new double[n];
            for (int a = 0; a < m; a++) {
                double c = er.vectors[a][0];
                for (int r = 0; r < n; r++) x[r] += c * q[a][r];
            }
            normalize(x);
            best = x;
            mul.accept(x, ax);
            double res = 0.0;
            for (int r = 0; r < n; r++) { double d = ax[r] - theta * x[r]; res += d * d; }
            if (Math.sqrt(res) <= 1e-10 * Math.max(1.0, Math.abs(theta)) || m < k || theta == 0.0) break;
            k = Math.min(n, Math.min(1500, k * 2));
        }
        double[] v = best;
        double sum = 0.0;
        for (double val : v) sum += val;
        if (sum < 0) for (int r = 0; r < n; r++) v[r] = -v[r];
        double maxVal = 0.0;
        for (double val : v) if (val > maxVal) maxVal = val;
        if (maxVal > 0) {
            for (int r = 0; r < n; r++) v[r] = Math.abs(v[r]) < 1e-14 ? 0.0 : v[r] / maxVal;
        } else {
            java.util.Arrays.fill(v, 0.0);
        }
        return v;
    }

    private static double dot(double[] a, double[] b) {
        double s = 0.0;
        for (int i = 0; i < a.length; i++) s += a[i] * b[i];
        return s;
    }

    private static void normalize(double[] a) {
        double s = Math.sqrt(dot(a, a));
        if (s > 0) for (int i = 0; i < a.length; i++) a[i] /= s;
    }

    /**
     * Largest eigenvalue (spectral radius) of the symmetrised binary adjacency
     * matrix used by Bonacich power centrality. By Perron-Frobenius the
     * spectral radius of a non-negative symmetric matrix is its largest
     * eigenvalue; power iteration on (A + I) avoids oscillation on bipartite
     * graphs. Returns 0 for an empty graph.
     */
    public static double bonacichSpectralRadius(Network net) {
        double[][] a = bonacichAdjacency(net);
        int n = a.length;
        if (n == 0) return 0.0;
        double[] v = new double[n];
        java.util.Arrays.fill(v, 1.0 / Math.sqrt(n));
        double lambda = 0.0;
        for (int iter = 0; iter < 10000; iter++) {
            double[] next = new double[n];
            for (int i = 0; i < n; i++) {
                double sum = v[i];
                for (int j = 0; j < n; j++) sum += a[i][j] * v[j];
                next[i] = sum;
            }
            double norm = 0.0;
            for (double x : next) norm += x * x;
            norm = Math.sqrt(norm);
            if (norm <= 1e-300) return 0.0;
            double rq = 0.0; // Rayleigh quotient of (A + I)
            for (int i = 0; i < n; i++) rq += v[i] * next[i];
            for (int i = 0; i < n; i++) next[i] /= norm;
            v = next;
            if (Math.abs(rq - lambda) < 1e-12 * Math.max(1.0, Math.abs(rq))) { lambda = rq; break; }
            lambda = rq;
        }
        return Math.max(0.0, lambda - 1.0);
    }

    /**
     * Largest |beta| for which Bonacich power centrality is defined
     * (the series sum_k beta^k A^(k+1) 1 converges iff |beta| &lt; 1 / lambda_max).
     * Returns +infinity for a graph without ties.
     */
    public static double bonacichBetaLimit(Network net) {
        double l = bonacichSpectralRadius(net);
        return l > 0 ? 1.0 / l : Double.POSITIVE_INFINITY;
    }

    private static double[][] bonacichAdjacency(Network net) {
        int n = net.getSize();
        float[][] mat = net.getMatrix();
        double[][] a = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    a[i][j] = (mat[i][j] != 0f || mat[j][i] != 0f) ? 1.0 : 0.0;
                }
            }
        }
        return a;
    }

    /**
     * Computes Bonacich Power Centrality (Bonacich 1987) with alpha = 1:
     * c(beta) = (I - beta A)^(-1) A 1 on the symmetrised binary adjacency matrix.
     *
     * 2.2.2: solved exactly by Gaussian elimination with partial pivoting
     * instead of a truncated power series, and rejected with an
     * IllegalArgumentException when |beta| &gt;= 1 / lambda_max, where the
     * measure is undefined (before 2.2.2 a meaningless truncated value of a
     * divergent series was returned). The maxIter argument is kept for API
     * compatibility and is ignored.
     */
    public static double[] computeBonacich(Network net, double beta, int maxIter) {
        int n = net.getSize();
        if (n == 0) return new double[0];
        double[][] a = bonacichAdjacency(net);
        double limit = bonacichBetaLimit(net);
        if (Double.isNaN(beta) || Double.isInfinite(beta) || Math.abs(beta) >= limit * (1.0 - 1e-9)) {
            throw new IllegalArgumentException(String.format(java.util.Locale.ROOT,
                    "Bonacich power is undefined for beta = %.4f: |beta| must be smaller than 1/lambda_max = %.4f "
                    + "for this network (lambda_max = %.4f). Choose a smaller |beta|, e.g. %.4f.",
                    beta, limit, limit > 0 && !Double.isInfinite(limit) ? 1.0 / limit : 0.0,
                    Double.isInfinite(limit) ? 0.1 : 0.5 * limit));
        }
        double[] rhs = new double[n];
        double[][] m = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rhs[i] += a[i][j];
                m[i][j] = (i == j ? 1.0 : 0.0) - beta * a[i][j];
            }
        }
        // Gaussian elimination with partial pivoting
        for (int col = 0; col < n; col++) {
            int piv = col;
            for (int r = col + 1; r < n; r++) if (Math.abs(m[r][col]) > Math.abs(m[piv][col])) piv = r;
            if (Math.abs(m[piv][col]) < 1e-14) {
                throw new IllegalArgumentException("Bonacich power: (I - beta A) is singular for beta = " + beta);
            }
            double[] tr = m[col]; m[col] = m[piv]; m[piv] = tr;
            double tv = rhs[col]; rhs[col] = rhs[piv]; rhs[piv] = tv;
            for (int r = col + 1; r < n; r++) {
                double f = m[r][col] / m[col][col];
                if (f == 0.0) continue;
                for (int c = col; c < n; c++) m[r][c] -= f * m[col][c];
                rhs[r] -= f * rhs[col];
            }
        }
        double[] x = new double[n];
        for (int r = n - 1; r >= 0; r--) {
            double sum = rhs[r];
            for (int c = r + 1; c < n; c++) sum -= m[r][c] * x[c];
            x[r] = sum / m[r][r];
        }
        return x;
    }

    /**
     * Computes PageRank Centrality for directed networks using the random walk model:
     * PR(u) = (1 - d) / N + d * sum_{v in In(u)} (PR(v) / OutDeg(v)).
     * Default damping factor is 0.85.
     */
    public static double[] computePageRank(Network net, double dampingFactor, int maxIterations) {
        // 2.2.2: iterates the sparse in-neighbour lists, O(m) per iteration
        // instead of a column-wise scan of a dense matrix copy (O(n^2) time,
        // n^2 floats of memory). Same binary random-walk model and result.
        com.bentza.sna.net.AdjacencyIndex g = com.bentza.sna.net.AdjacencyIndex.of(net);
        int n = g.n;
        if (n == 0) return new double[0];
        double[] pr = new double[n];
        java.util.Arrays.fill(pr, 1.0 / n);
        double teleport = (1.0 - dampingFactor) / n;
        double[] next = new double[n];
        for (int iter = 0; iter < maxIterations; iter++) {
            double danglingSum = 0.0;
            for (int i = 0; i < n; i++) {
                if (g.outDegree(i) == 0) danglingSum += pr[i];
            }
            for (int i = 0; i < n; i++) {
                double incoming = 0.0;
                for (int p = g.inStart[i], e = g.inStart[i + 1]; p < e; p++) {
                    int src = g.inSource[p];
                    incoming += pr[src] / g.outDegree(src);
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
