/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.census;

import com.bentza.sna.net.Network;

/**
 * Computes the Dyad Census (Mutual, Asymmetric, Null) for directed networks.
 *
 * References:
 * - Holland, P. W., &amp; Leinhardt, S. (1970). A method for detecting structure in
 *   sociometric data. American Journal of Sociology, 76(3), 492-513.
 * - Wasserman, S., &amp; Faust, K. (1994). Social Network Analysis: Methods and Applications.
 *   Cambridge University Press (Chapter 13).
 */
public final class DyadCensus {

    public final long mutual;     // M: both i->j and j->i exist
    public final long asymmetric; // A: exactly one of i->j or j->i exists
    public final long nullDyads;  // N: neither i->j nor j->i exists
    public final long totalDyads; // N * (N - 1) / 2
    public final double reciprocityRate; // 2M / (2M + A)

    public DyadCensus(long mutual, long asymmetric, long nullDyads, long totalDyads, double reciprocityRate) {
        this.mutual = mutual;
        this.asymmetric = asymmetric;
        this.nullDyads = nullDyads;
        this.totalDyads = totalDyads;
        this.reciprocityRate = reciprocityRate;
    }

    public static DyadCensus compute(Network net) {
        int n = net.getSize();
        if (n < 2) {
            return new DyadCensus(0, 0, 0, 0, 0.0);
        }

        long m = 0;
        long a = 0;
        long nulls = 0;
        float[][] mat = net.getMatrix();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                boolean ij = mat[i][j] != 0f;
                boolean ji = mat[j][i] != 0f;
                if (ij && ji) {
                    m++;
                } else if (ij || ji) {
                    a++;
                } else {
                    nulls++;
                }
            }
        }

        long total = (long) n * (n - 1) / 2;
        long directedEdges = 2 * m + a;
        double reciprocity = directedEdges > 0 ? (2.0 * m) / (double) directedEdges : 0.0;

        return new DyadCensus(m, a, nulls, total, reciprocity);
    }
}
