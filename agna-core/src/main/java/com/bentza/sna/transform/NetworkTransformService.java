/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.transform;

import com.bentza.sna.net.Actor;
import com.bentza.sna.net.Network;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service providing core network transformation and matrix editing operations:
 * - Flexible Symmetrization (Max, Min, Average, Sum)
 * - Thresholded Dichotomization &amp; Slicing
 * - Diagonal operations (zeroing self-loops or setting values)
 * - Ego-network neighborhood extraction (1-step and 2-step)
 * - Matrix Network Algebra across two networks (Add, Subtract, Intersect, Hadamard)
 * - Sociomatrix permutation / Actor sorting
 */
public final class NetworkTransformService {

    public enum SymmetrizeMode {
        MAX("Maximum (Union: max(a_ij, a_ji))"),
        MIN("Minimum (Intersection: min(a_ij, a_ji))"),
        AVERAGE("Average (Arithmetic mean: (a_ij + a_ji) / 2)"),
        SUM("Sum (Aggregate: a_ij + a_ji)");

        private final String label;

        SymmetrizeMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum AlgebraOp {
        ADD("Matrix Addition (A + B)"),
        SUBTRACT("Matrix Difference (A - B)"),
        INTERSECTION_MIN("Intersection Minimum (min(A, B))"),
        HADAMARD("Element-wise / Hadamard Product (A * B)");

        private final String label;

        AlgebraOp(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum SortCriterion {
        NAME("Actor Name (Alphabetical)"),
        DEGREE("Total Degree (k)"),
        INDEGREE("In-Degree / Reception"),
        OUTDEGREE("Out-Degree / Emission");

        private final String label;

        SortCriterion(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private NetworkTransformService() {}

    /**
     * Symmetrizes the network according to the specified rule.
     */
    public static void symmetrize(Network net, SymmetrizeMode mode) {
        int n = net.getSize();
        float[][] mat = net.getMatrix();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                float a = mat[i][j];
                float b = mat[j][i];
                float symVal;
                switch (mode) {
                    case MIN:
                        symVal = Math.min(a, b);
                        break;
                    case AVERAGE:
                        symVal = (a + b) / 2.0f;
                        break;
                    case SUM:
                        symVal = a + b;
                        break;
                    case MAX:
                    default:
                        symVal = Math.max(a, b);
                        break;
                }
                net.setValue(symVal, i, j);
                net.setValue(symVal, j, i);
            }
        }
    }

    /**
     * Dichotomizes the network ties with a threshold value.
     */
    public static void dichotomize(Network net, float threshold, boolean greaterOrEqual) {
        int n = net.getSize();
        float[][] mat = net.getMatrix();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    float val = mat[i][j];
                    boolean pass = greaterOrEqual ? (val >= threshold) : (val > threshold);
                    net.setValue(pass ? 1.0f : 0.0f, i, j);
                }
            }
        }
    }

    /**
     * Slices/clips the tie values within a range, setting outliers to 0.
     */
    public static void slice(Network net, float minThreshold, float maxThreshold) {
        int n = net.getSize();
        float[][] mat = net.getMatrix();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    float val = mat[i][j];
                    if (val < minThreshold || val > maxThreshold) {
                        net.setValue(0.0f, i, j);
                    }
                }
            }
        }
    }

    /**
     * Sets all diagonal elements (self-loops) to 0.
     */
    public static void zeroDiagonal(Network net) {
        setDiagonal(net, 0.0f);
    }

    /**
     * Sets all diagonal elements (self-loops) to a specific scalar value.
     */
    public static void setDiagonal(Network net, float val) {
        int n = net.getSize();
        for (int i = 0; i < n; i++) {
            net.setValue(val, i, i);
        }
    }

    /**
     * Extracts an ego-network subnetwork centered on egoIndex within radius hops.
     */
    public static Network extractEgoNetwork(Network net, int egoIndex, int radius, boolean includeEgo) {
        int n = net.getSize();
        if (egoIndex < 0 || egoIndex >= n) {
            throw new IllegalArgumentException("Invalid ego index: " + egoIndex);
        }

        float[][] mat = net.getMatrix();
        Set<Integer> egoGroup = new HashSet<>();
        egoGroup.add(egoIndex);

        int[] dist = new int[n];
        java.util.Arrays.fill(dist, -1);
        dist[egoIndex] = 0;

        ArrayDeque<Integer> q = new ArrayDeque<>();
        q.add(egoIndex);

        while (!q.isEmpty()) {
            int u = q.poll();
            if (dist[u] < radius) {
                for (int v = 0; v < n; v++) {
                    if (u != v && (mat[u][v] != 0f || mat[v][u] != 0f)) {
                        if (dist[v] == -1) {
                            dist[v] = dist[u] + 1;
                            egoGroup.add(v);
                            q.add(v);
                        }
                    }
                }
            }
        }

        if (!includeEgo) {
            egoGroup.remove(egoIndex);
        }

        List<Integer> subNodes = new ArrayList<>(egoGroup);
        Collections.sort(subNodes);

        int subN = subNodes.size();
        String egoName = net.getActorName(egoIndex) != null ? net.getActorName(egoIndex).trim() : ("Actor_" + (egoIndex + 1));
        String newName = "Ego_" + egoName + "_r" + radius;
        Network subNet = new Network(subN);
        subNet.setName(newName);

        for (int i = 0; i < subN; i++) {
            int origI = subNodes.get(i);
            String actorName = net.getActorName(origI);
            if (actorName != null && subNet.getActor(i) != null) {
                subNet.getActor(i).setName(actorName);
            }
            if (subNet.getActor(i) != null && net.getActor(origI) != null) {
                subNet.getActor(i).copyCoordinatesFrom(net.getActor(origI));
            }
        }

        for (int i = 0; i < subN; i++) {
            int origI = subNodes.get(i);
            for (int j = 0; j < subN; j++) {
                int origJ = subNodes.get(j);
                subNet.setValue(mat[origI][origJ], i, j);
            }
        }

        return subNet;
    }

    /**
     * Combines two networks via matrix algebra. Both networks should have the same dimensions.
     */
    public static Network combine(Network netA, Network netB, AlgebraOp op) {
        int n = Math.min(netA.getSize(), netB.getSize());
        String newName = netA.getName() + "_" + op.name() + "_" + netB.getName();
        Network res = new Network(n);
        res.setName(newName);

        for (int i = 0; i < n; i++) {
            String name = netA.getActorName(i);
            if (name != null && res.getActor(i) != null) {
                res.getActor(i).setName(name);
            }
            if (res.getActor(i) != null && netA.getActor(i) != null) {
                res.getActor(i).copyCoordinatesFrom(netA.getActor(i));
            }
        }

        float[][] matA = netA.getMatrix();
        float[][] matB = netB.getMatrix();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                float a = matA[i][j];
                float b = matB[i][j];
                float val = 0.0f;
                switch (op) {
                    case ADD:
                        val = a + b;
                        break;
                    case SUBTRACT:
                        val = Math.max(0.0f, a - b);
                        break;
                    case INTERSECTION_MIN:
                        val = Math.min(a, b);
                        break;
                    case HADAMARD:
                        val = a * b;
                        break;
                }
                res.setValue(val, i, j);
            }
        }

        return res;
    }

    /**
     * Sorts/permutes rows and columns of the sociomatrix according to the given criterion.
     */
    public static void sortActors(Network net, SortCriterion criterion, boolean ascending) {
        int n = net.getSize();
        if (n < 2) return;

        float[][] mat = net.getMatrix();
        List<Integer> order = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            order.add(i);
        }

        int[] inDeg = new int[n];
        int[] outDeg = new int[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && mat[i][j] != 0f) {
                    outDeg[i]++;
                    inDeg[j]++;
                }
            }
        }

        order.sort((a, b) -> {
            int cmp = 0;
            switch (criterion) {
                case NAME:
                    String na = net.getActorName(a) != null ? net.getActorName(a).trim() : "";
                    String nb = net.getActorName(b) != null ? net.getActorName(b).trim() : "";
                    cmp = na.compareToIgnoreCase(nb);
                    break;
                case INDEGREE:
                    cmp = Integer.compare(inDeg[a], inDeg[b]);
                    break;
                case OUTDEGREE:
                    cmp = Integer.compare(outDeg[a], outDeg[b]);
                    break;
                case DEGREE:
                default:
                    cmp = Integer.compare(inDeg[a] + outDeg[a], inDeg[b] + outDeg[b]);
                    break;
            }
            return ascending ? cmp : -cmp;
        });

        // Apply permutation to network
        String[] newNames = new String[n];
        Actor[] origActors = new Actor[n];
        float[][] newMat = new float[n][n];

        for (int i = 0; i < n; i++) {
            int oldI = order.get(i);
            newNames[i] = net.getActorName(oldI);
            origActors[i] = net.getActor(oldI);
            for (int j = 0; j < n; j++) {
                int oldJ = order.get(j);
                newMat[i][j] = mat[oldI][oldJ];
            }
        }

        for (int i = 0; i < n; i++) {
            if (newNames[i] != null && net.getActor(i) != null) {
                net.getActor(i).setName(newNames[i]);
            }
            if (net.getActor(i) != null && origActors[i] != null) {
                net.getActor(i).copyCoordinatesFrom(origActors[i]);
            }
            for (int j = 0; j < n; j++) {
                net.setValue(newMat[i][j], i, j);
            }
        }
    }
}
