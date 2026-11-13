/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.community;

import com.bentza.sna.net.Network;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Community detection and modularity partitioning algorithms for social networks:
 * - Louvain modularity maximization (Blondel et al., 2008)
 * - Girvan-Newman edge-betweenness community division (Girvan &amp; Newman, 2002)
 *
 * <p>References:
 * <ul>
 *   <li>Blondel, V. D., Guillaume, J. L., Lambiotte, R., &amp; Lefebvre, E. (2008). Fast unfolding of communities in large networks. <i>Journal of Statistical Mechanics: Theory and Experiment</i>, 2008(10), P10008.</li>
 *   <li>Newman, M. E. J., &amp; Girvan, M. (2004). Finding and evaluating community structure in networks. <i>Physical Review E</i>, 69(2), 026113.</li>
 *   <li>Girvan, M., &amp; Newman, M. E. J. (2002). Community structure in social and biological networks. <i>PNAS</i>, 99(12), 7821-7826.</li>
 * </ul>
 */
public final class CommunityDetectionService {

    public static final class CommunityInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public final int communityId;
        public final int memberCount;
        public final double internalDensity;
        public final List<String> memberNames;

        public CommunityInfo(int communityId, int memberCount, double internalDensity, List<String> memberNames) {
            this.communityId = communityId;
            this.memberCount = memberCount;
            this.internalDensity = internalDensity;
            this.memberNames = Collections.unmodifiableList(memberNames);
        }
    }

    public static final class CommunityResult implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String algorithm;
        public final double modularityQ;
        public final int communityCount;
        public final int[] nodeAssignments; // actorIndex -> communityId (0-indexed)
        public final List<CommunityInfo> communities;

        public CommunityResult(String algorithm, double modularityQ, int communityCount, int[] nodeAssignments, List<CommunityInfo> communities) {
            this.algorithm = algorithm;
            this.modularityQ = modularityQ;
            this.communityCount = communityCount;
            this.nodeAssignments = nodeAssignments;
            this.communities = Collections.unmodifiableList(communities);
        }
    }

    private CommunityDetectionService() {}

    /**
     * Executes the Louvain algorithm to partition the network into cohesive communities maximizing modularity Q.
     */
    public static CommunityResult detectLouvain(Network net) {
        int n = net.getSize();
        if (n == 0) {
            return new CommunityResult("Louvain Modularity", 0.0, 0, new int[0], Collections.emptyList());
        }

        // Build symmetric adjacency weights for community detection
        float[][] origMat = net.getMatrix();
        double[][] adj = new double[n][n];
        double totalWeight = 0.0;
        double[] degrees = new double[n];

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double w = Math.max(origMat[i][j], origMat[j][i]);
                if (w > 0.0) {
                    adj[i][j] = w;
                    adj[j][i] = w;
                    degrees[i] += w;
                    degrees[j] += w;
                    totalWeight += w; // count each undirected edge once
                }
            }
        }

        double m2 = 2.0 * totalWeight;
        if (m2 == 0.0) {
            // Disconnected network with 0 ties
            int[] assignments = new int[n];
            List<CommunityInfo> comms = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                assignments[i] = i;
                comms.add(new CommunityInfo(i, 1, 0.0, Collections.singletonList(net.getActor(i).getName())));
            }
            return new CommunityResult("Louvain Modularity", 0.0, n, assignments, comms);
        }

        // Initial partition: each node in its own community
        int[] community = new int[n];
        for (int i = 0; i < n; i++) {
            community[i] = i;
        }

        double[] communityTot = new double[n]; // sum of degrees of nodes in community
        System.arraycopy(degrees, 0, communityTot, 0, n);

        boolean improved = true;
        int maxPasses = 50;
        int pass = 0;

        while (improved && pass++ < maxPasses) {
            improved = false;
            for (int i = 0; i < n; i++) {
                int currentComm = community[i];
                double ki = degrees[i];
                if (ki == 0.0) continue;

                // Weight from node i to its current community (excluding self)
                double kiInCurrent = 0.0;
                for (int j = 0; j < n; j++) {
                    if (j != i && community[j] == currentComm) {
                        kiInCurrent += adj[i][j];
                    }
                }

                // Remove i from currentComm temporarily
                communityTot[currentComm] -= ki;

                // Find neighbor communities and compute weights to them
                Map<Integer, Double> commWeights = new HashMap<>();
                for (int j = 0; j < n; j++) {
                    if (j != i && adj[i][j] > 0.0) {
                        int c = community[j];
                        commWeights.put(c, commWeights.getOrDefault(c, 0.0) + adj[i][j]);
                    }
                }

                int bestComm = currentComm;
                // Delta Q if moving out of currentComm and into bestComm
                double bestDeltaQ = 0.0;

                for (Map.Entry<Integer, Double> entry : commWeights.entrySet()) {
                    int targetComm = entry.getKey();
                    double kiInTarget = entry.getValue();
                    double totTarget = communityTot[targetComm];

                    // Standard Louvain Delta Q formula (Blondel et al. 2008):
                    // dQ = 2 * (kiInTarget - kiInCurrent)/m2 - 2 * ki * (totTarget - communityTot[currentComm]) / (m2 * m2)
                    double dQ = (2.0 * (kiInTarget - kiInCurrent)) / m2 - (ki * (totTarget - communityTot[currentComm])) / (m2 * m2 / 2.0);
                    if (dQ > bestDeltaQ) {
                        bestDeltaQ = dQ;
                        bestComm = targetComm;
                    }
                }

                // Assign i to best community
                community[i] = bestComm;
                communityTot[bestComm] += ki;
                if (bestComm != currentComm) {
                    improved = true;
                }
            }
        }

        // Renumber communities to contiguous IDs 0..K-1
        Map<Integer, Integer> idMap = new HashMap<>();
        int nextId = 0;
        for (int i = 0; i < n; i++) {
            if (!idMap.containsKey(community[i])) {
                idMap.put(community[i], nextId++);
            }
        }
        int[] normalized = new int[n];
        for (int i = 0; i < n; i++) {
            normalized[i] = idMap.get(community[i]);
        }
        int communityCount = nextId;

        // Compute modularity Q of final partition
        double finalQ = computeModularity(adj, normalized, degrees, m2);

        // Build community info list
        List<CommunityInfo> commInfoList = buildCommunityInfoList(net, origMat, normalized, communityCount);

        return new CommunityResult("Louvain Modularity", finalQ, communityCount, normalized, commInfoList);
    }

    /**
     * Executes the Girvan-Newman edge-betweenness community division algorithm.
     */
    public static CommunityResult detectGirvanNewman(Network net) {
        int n = net.getSize();
        if (n == 0) {
            return new CommunityResult("Girvan-Newman", 0.0, 0, new int[0], Collections.emptyList());
        }

        float[][] origMat = net.getMatrix();
        boolean[][] currentAdj = new boolean[n][n];
        double[][] weightAdj = new double[n][n];
        double[] degrees = new double[n];
        double totalEdges = 0.0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (origMat[i][j] > 0 || origMat[j][i] > 0) {
                    currentAdj[i][j] = true;
                    currentAdj[j][i] = true;
                    weightAdj[i][j] = 1.0;
                    weightAdj[j][i] = 1.0;
                    degrees[i] += 1.0;
                    degrees[j] += 1.0;
                    totalEdges += 1.0;
                }
            }
        }

        double m2 = 2.0 * totalEdges;
        if (m2 == 0.0) {
            return detectLouvain(net);
        }

        int[] bestPartition = new int[n];
        double bestQ = -1.0;
        int bestK = 1;

        // Initial component partition
        int[] initialPartition = findConnectedComponents(currentAdj, n);
        int initialK = countUnique(initialPartition);
        double initialQ = computeModularity(weightAdj, initialPartition, degrees, m2);
        bestQ = initialQ;
        bestPartition = initialPartition.clone();
        bestK = initialK;

        int maxEdgeRemovals = (int) totalEdges;
        for (int step = 0; step < maxEdgeRemovals; step++) {
            double[][] edgeBetweenness = computeEdgeBetweenness(currentAdj, n);

            // Find edge with maximum betweenness
            double maxEB = -1.0;
            int maxU = -1;
            int maxV = -1;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (currentAdj[i][j] && edgeBetweenness[i][j] > maxEB) {
                        maxEB = edgeBetweenness[i][j];
                        maxU = i;
                        maxV = j;
                    }
                }
            }

            if (maxU == -1) break; // No more edges

            // Remove edge (maxU, maxV)
            currentAdj[maxU][maxV] = false;
            currentAdj[maxV][maxU] = false;

            int[] currentPartition = findConnectedComponents(currentAdj, n);
            double q = computeModularity(weightAdj, currentPartition, degrees, m2);
            int k = countUnique(currentPartition);

            if (q > bestQ) {
                bestQ = q;
                bestPartition = currentPartition.clone();
                bestK = k;
            }
        }

        // Remap partition to 0..K-1
        Map<Integer, Integer> map = new HashMap<>();
        int nextId = 0;
        int[] normalized = new int[n];
        for (int i = 0; i < n; i++) {
            if (!map.containsKey(bestPartition[i])) {
                map.put(bestPartition[i], nextId++);
            }
            normalized[i] = map.get(bestPartition[i]);
        }

        List<CommunityInfo> commInfoList = buildCommunityInfoList(net, origMat, normalized, nextId);
        return new CommunityResult("Girvan-Newman", bestQ, nextId, normalized, commInfoList);
    }

    private static double computeModularity(double[][] adj, int[] comm, double[] degrees, double m2) {
        int n = comm.length;
        double q = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (comm[i] == comm[j]) {
                    q += (adj[i][j] - (degrees[i] * degrees[j]) / m2);
                }
            }
        }
        return q / m2;
    }

    private static double[][] computeEdgeBetweenness(boolean[][] adj, int n) {
        double[][] cb = new double[n][n];

        // Brandes edge-betweenness algorithm for unweighted undirected graphs
        for (int s = 0; s < n; s++) {
            int[] d = new int[n];
            Arrays.fill(d, -1);
            d[s] = 0;

            double[] sigma = new double[n];
            sigma[s] = 1.0;

            List<List<Integer>> pred = new ArrayList<>(n);
            for (int i = 0; i < n; i++) pred.add(new ArrayList<>());

            List<Integer> order = new ArrayList<>(n);
            Queue<Integer> q = new ArrayDeque<>();
            q.add(s);

            while (!q.isEmpty()) {
                int v = q.poll();
                order.add(v);
                for (int w = 0; w < n; w++) {
                    if (adj[v][w]) {
                        if (d[w] < 0) {
                            d[w] = d[v] + 1;
                            q.add(w);
                        }
                        if (d[w] == d[v] + 1) {
                            sigma[w] += sigma[v];
                            pred.get(w).add(v);
                        }
                    }
                }
            }

            double[] delta = new double[n];
            for (int i = order.size() - 1; i >= 0; i--) {
                int w = order.get(i);
                for (int v : pred.get(w)) {
                    double c = (sigma[v] / sigma[w]) * (1.0 + delta[w]);
                    cb[Math.min(v, w)][Math.max(v, w)] += c;
                    delta[v] += c;
                }
            }
        }

        // Each undirected edge was counted from both directions
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                cb[i][j] /= 2.0;
                cb[j][i] = cb[i][j];
            }
        }
        return cb;
    }

    private static int[] findConnectedComponents(boolean[][] adj, int n) {
        int[] comp = new int[n];
        Arrays.fill(comp, -1);
        int currentId = 0;

        for (int i = 0; i < n; i++) {
            if (comp[i] < 0) {
                Queue<Integer> q = new ArrayDeque<>();
                q.add(i);
                comp[i] = currentId;
                while (!q.isEmpty()) {
                    int u = q.poll();
                    for (int v = 0; v < n; v++) {
                        if (adj[u][v] && comp[v] < 0) {
                            comp[v] = currentId;
                            q.add(v);
                        }
                    }
                }
                currentId++;
            }
        }
        return comp;
    }

    private static int countUnique(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int val : arr) set.add(val);
        return set.size();
    }

    private static List<CommunityInfo> buildCommunityInfoList(Network net, float[][] origMat, int[] assignments, int count) {
        int n = assignments.length;
        List<List<Integer>> members = new ArrayList<>(count);
        for (int i = 0; i < count; i++) members.add(new ArrayList<>());
        for (int i = 0; i < n; i++) {
            members.get(assignments[i]).add(i);
        }

        List<CommunityInfo> list = new ArrayList<>(count);
        for (int c = 0; c < count; c++) {
            List<Integer> commNodes = members.get(c);
            int sz = commNodes.size();
            List<String> names = new ArrayList<>(sz);
            int internalTies = 0;

            for (int u : commNodes) {
                names.add(net.getActor(u).getName());
                for (int v : commNodes) {
                    if (u != v && (origMat[u][v] > 0 || origMat[v][u] > 0)) {
                        internalTies++;
                    }
                }
            }
            int possible = sz * (sz - 1);
            double dens = (possible > 0) ? ((double) internalTies / possible) : 1.0;
            list.add(new CommunityInfo(c + 1, sz, dens, names));
        }
        return list;
    }
}
