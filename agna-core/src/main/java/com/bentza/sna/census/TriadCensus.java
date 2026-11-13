/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.census;

import com.bentza.sna.net.Network;

/**
 * Computes the Davis and Leinhardt / Holland and Leinhardt 16 Triad Census for directed networks.
 *
 * References:
 * - Davis, J. A., &amp; Leinhardt, S. (1972). The structure of positive interpersonal relations
 *   in small groups. In J. Berger (Ed.), Sociological Theories in Progress (Vol. 2, pp. 218-251).
 * - Holland, P. W., &amp; Leinhardt, S. (1976). Local structure in social networks.
 *   Sociological Methodology, 7, 1-45.
 */
public final class TriadCensus {

    public enum TriadType {
        T003("003", "Empty triad (no ties)", "A, B, C isolated"),
        T012("012", "Single asymmetric tie", "A -> B"),
        T102("102", "Single mutual tie", "A <-> B"),
        T021D("021D", "Out-two-star (Down)", "A <- B -> C"),
        T021U("021U", "In-two-star (Up)", "A -> B <- C"),
        T021C("021C", "Directed path (Cycle-like)", "A -> B -> C"),
        T111D("111D", "Mutual with out-tie (Down)", "A <-> B -> C"),
        T111U("111U", "Mutual with in-tie (Up)", "A <-> B <- C"),
        T030T("030T", "Transitive triple", "A -> B -> C, A -> C"),
        T030C("030C", "Cyclic triple (Paradox)", "A -> B -> C -> A"),
        T201("201", "Two mutual ties", "A <-> B <-> C"),
        T120D("120D", "Mutual with common in-ties", "A <-> B, C -> A, C -> B"),
        T120U("120U", "Mutual with common out-ties", "A <-> B, A -> C, B -> C"),
        T120C("120C", "Mutual with cycle", "A <-> B, B -> C -> A"),
        T210("210", "Two mutuals, one asymmetric", "A <-> B <-> C, A -> C"),
        T300("300", "Complete triad (Full clique)", "A <-> B <-> C <-> A");

        private final String code;
        private final String name;
        private final String description;

        TriadType(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    public final long[] counts = new long[16];
    public final long totalTriads;
    public final double transitivityRatio;
    public final DyadCensus dyadCensus;

    public TriadCensus(long[] counts, long totalTriads, double transitivityRatio, DyadCensus dyadCensus) {
        System.arraycopy(counts, 0, this.counts, 0, 16);
        this.totalTriads = totalTriads;
        this.transitivityRatio = transitivityRatio;
        this.dyadCensus = dyadCensus;
    }

    public long getCount(TriadType type) {
        return counts[type.ordinal()];
    }

    public static TriadCensus compute(Network net) {
        int n = net.getSize();
        long[] c = new long[16];
        DyadCensus dyads = DyadCensus.compute(net);

        if (n < 3) {
            return new TriadCensus(c, 0, 0.0, dyads);
        }

        float[][] mat = net.getMatrix();
        boolean[][] hasEdge = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && mat[i][j] != 0f) {
                    hasEdge[i][j] = true;
                }
            }
        }

        long total = (long) n * (n - 1) * (n - 2) / 6;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                for (int k = j + 1; k < n; k++) {
                    int typeIndex = classifyTriad(hasEdge, i, j, k);
                    c[typeIndex]++;
                }
            }
        }

        // Transitivity ratio from triad census:
        // Transitive triads (030T, 120D, 120U, 120C, 210, 300) vs potential transitive configurations
        long numTransitive = c[TriadType.T030T.ordinal()]
                + c[TriadType.T120D.ordinal()]
                + c[TriadType.T120U.ordinal()]
                + c[TriadType.T120C.ordinal()]
                + 2 * c[TriadType.T210.ordinal()]
                + 3 * c[TriadType.T300.ordinal()];

        long numPotentiallyTransitive = numTransitive
                + c[TriadType.T021C.ordinal()]
                + c[TriadType.T030C.ordinal()]
                + c[TriadType.T111D.ordinal()]
                + c[TriadType.T111U.ordinal()]
                + c[TriadType.T201.ordinal()];

        double transitivity = numPotentiallyTransitive > 0
                ? (double) numTransitive / (double) numPotentiallyTransitive : 0.0;

        return new TriadCensus(c, total, transitivity, dyads);
    }

    private static int classifyTriad(boolean[][] adj, int u, int v, int w) {
        // Evaluate dyad states: 2 = mutual, 1 = forward, -1 = backward, 0 = null
        int uv = dyadState(adj, u, v);
        int uw = dyadState(adj, u, w);
        int vw = dyadState(adj, v, w);

        int m = (uv == 2 ? 1 : 0) + (uw == 2 ? 1 : 0) + (vw == 2 ? 1 : 0);
        int a = (Math.abs(uv) == 1 ? 1 : 0) + (Math.abs(uw) == 1 ? 1 : 0) + (Math.abs(vw) == 1 ? 1 : 0);

        if (m == 0 && a == 0) return TriadType.T003.ordinal();
        if (m == 0 && a == 1) return TriadType.T012.ordinal();
        if (m == 1 && a == 0) return TriadType.T102.ordinal();
        if (m == 2 && a == 0) return TriadType.T201.ordinal();
        if (m == 3 && a == 0) return TriadType.T300.ordinal();
        if (m == 2 && a == 1) return TriadType.T210.ordinal();

        int[] nodes = new int[]{u, v, w};
        int[] outDeg = new int[3];
        int[] inDeg = new int[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i != j && adj[nodes[i]][nodes[j]]) {
                    outDeg[i]++;
                    inDeg[j]++;
                }
            }
        }

        if (m == 0 && a == 2) {
            // Find vertex with 2 incident asymmetric ties
            for (int i = 0; i < 3; i++) {
                if (outDeg[i] == 2 && inDeg[i] == 0) return TriadType.T021D.ordinal();
                if (inDeg[i] == 2 && outDeg[i] == 0) return TriadType.T021U.ordinal();
            }
            return TriadType.T021C.ordinal();
        }

        if (m == 1 && a == 1) {
            // Node with no mutual tie: degree in asymmetric tie
            for (int i = 0; i < 3; i++) {
                if (outDeg[i] + inDeg[i] == 1) {
                    if (outDeg[i] == 1) return TriadType.T111D.ordinal(); // Apex points to mutual base (Down)
                    if (inDeg[i] == 1) return TriadType.T111U.ordinal();  // Mutual base points to apex (Up)
                }
            }
            return TriadType.T111D.ordinal();
        }

        if (m == 0 && a == 3) {
            for (int i = 0; i < 3; i++) {
                if (outDeg[i] == 2) return TriadType.T030T.ordinal(); // Transitive has out-degrees 2, 1, 0
            }
            return TriadType.T030C.ordinal(); // Cyclic has all out-degrees 1
        }

        if (m == 1 && a == 2) {
            // Node with 0 mutual ties has 2 asymmetric ties
            for (int i = 0; i < 3; i++) {
                // Check if this node is not part of the mutual tie
                boolean hasMutual = false;
                for (int j = 0; j < 3; j++) {
                    if (i != j && adj[nodes[i]][nodes[j]] && adj[nodes[j]][nodes[i]]) {
                        hasMutual = true;
                        break;
                    }
                }
                if (!hasMutual) {
                    if (outDeg[i] == 2) return TriadType.T120D.ordinal(); // C -> A and C -> B
                    if (inDeg[i] == 2) return TriadType.T120U.ordinal();  // A -> C and B -> C
                    return TriadType.T120C.ordinal();                     // A -> C -> B
                }
            }
            return TriadType.T120C.ordinal();
        }

        return TriadType.T003.ordinal();
    }

    private static int dyadState(boolean[][] adj, int i, int j) {
        boolean ij = adj[i][j];
        boolean ji = adj[j][i];
        if (ij && ji) return 2;
        if (ij) return 1;
        if (ji) return -1;
        return 0;
    }
}
