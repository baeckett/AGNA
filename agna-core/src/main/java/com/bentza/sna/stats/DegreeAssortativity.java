/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

import com.bentza.sna.net.Network;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes degree assortativity coefficients for social and complex networks.
 *
 * <p>Reference:
 * <ul>
 *   <li>Newman, M. E. J. (2002). Assortative mixing in networks. <i>Physical Review Letters</i>, 89(20), 208701.</li>
 *   <li>Newman, M. E. J. (2003). Mixing patterns in networks. <i>Physical Review E</i>, 67(2), 026126.</li>
 *   <li>Foster, J. G., Foster, D. V., Grassberger, P., &amp; Paczuski, M. (2010). Edge direction and the structure of networks. <i>PNAS</i>, 107(24), 10815-10820.</li>
 * </ul>
 */
public final class DegreeAssortativity {

    public static final class AssortativityResult {
        public final double totalDegreeAssortativity;
        public final double outInAssortativity;
        public final double inInAssortativity;
        public final double outOutAssortativity;
        public final double inOutAssortativity;
        public final int edgeCount;
        public final String mixingRegime;

        public AssortativityResult(double total, double outIn, double inIn, double outOut, double inOut, int edges) {
            this.totalDegreeAssortativity = total;
            this.outInAssortativity = outIn;
            this.inInAssortativity = inIn;
            this.outOutAssortativity = outOut;
            this.inOutAssortativity = inOut;
            this.edgeCount = edges;
            if (Double.isNaN(total)) {
                this.mixingRegime = "Undefined (Uniform or Insufficient Edges)";
            } else if (total > 0.10) {
                this.mixingRegime = "Assortative (Homophilic degree mixing; typical of human social networks)";
            } else if (total < -0.10) {
                this.mixingRegime = "Disassortative (Heterophilic hub-periphery mixing; typical of hierarchical networks)";
            } else {
                this.mixingRegime = "Neutral / Non-assortative (Random degree mixing)";
            }
        }
    }

    private DegreeAssortativity() {}

    /**
     * Computes the complete set of degree assortativity coefficients for the given network.
     */
    public static AssortativityResult compute(Network net) {
        int n = net.getSize();
        float[][] mat = net.getMatrix();

        float[] inDeg = new float[n];
        float[] outDeg = new float[n];
        float[] totDeg = new float[n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && mat[i][j] > 0) {
                    outDeg[i] += 1.0f;
                    inDeg[j] += 1.0f;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            totDeg[i] = inDeg[i] + outDeg[i];
        }

        // Collect directed edges
        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && mat[i][j] > 0) {
                    edges.add(new int[]{i, j});
                }
            }
        }

        int m = edges.size();
        if (m < 2) {
            return new AssortativityResult(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, m);
        }

        // Newman's symmetric degree assortativity r across all undirected/directed edges
        double rTotal = computeNewmanAssortativity(edges, totDeg, m);
        double rOutIn = computePearsonEdgeCorrelation(edges, outDeg, inDeg);
        double rInIn = computePearsonEdgeCorrelation(edges, inDeg, inDeg);
        double rOutOut = computePearsonEdgeCorrelation(edges, outDeg, outDeg);
        double rInOut = computePearsonEdgeCorrelation(edges, inDeg, outDeg);

        return new AssortativityResult(rTotal, rOutIn, rInIn, rOutOut, rInOut, m);
    }

    private static double computeNewmanAssortativity(List<int[]> edges, float[] k, int m) {
        // Standard formula from Newman (2002):
        // r = (sum(j * k) - [sum(j + k)/2]^2 / m) / (sum((j^2 + k^2)/2) - [sum(j + k)/2]^2 / m)
        double sumProduct = 0.0;
        double sumHalf = 0.0;
        double sumSquareHalf = 0.0;

        for (int[] e : edges) {
            double ki = k[e[0]];
            double kj = k[e[1]];
            sumProduct += ki * kj;
            sumHalf += 0.5 * (ki + kj);
            sumSquareHalf += 0.5 * (ki * ki + kj * kj);
        }

        double term2 = (sumHalf * sumHalf) / m;
        double numerator = sumProduct - term2;
        double denominator = sumSquareHalf - term2;

        if (Math.abs(denominator) < 1e-9) {
            return 0.0; // Uniform degree or regular graph
        }
        double r = numerator / denominator;
        if (r > 1.0) r = 1.0;
        if (r < -1.0) r = -1.0;
        return r;
    }

    private static double computePearsonEdgeCorrelation(List<int[]> edges, float[] sourceProp, float[] targetProp) {
        int m = edges.size();
        double sumX = 0;
        double sumY = 0;
        for (int[] e : edges) {
            sumX += sourceProp[e[0]];
            sumY += targetProp[e[1]];
        }
        double meanX = sumX / m;
        double meanY = sumY / m;

        double cov = 0;
        double varX = 0;
        double varY = 0;
        for (int[] e : edges) {
            double dx = sourceProp[e[0]] - meanX;
            double dy = targetProp[e[1]] - meanY;
            cov += dx * dy;
            varX += dx * dx;
            varY += dy * dy;
        }

        double denom = Math.sqrt(varX * varY);
        if (denom < 1e-9) {
            return 0.0;
        }
        double r = cov / denom;
        if (r > 1.0) r = 1.0;
        if (r < -1.0) r = -1.0;
        return r;
    }
}
