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
        public final List<NodeUncertaintyInfo> nodeUncertainties;
        public final double meanNmi;
        public final double meanVi;

        public CommunityResult(String algorithm, double modularityQ, int communityCount, int[] nodeAssignments, List<CommunityInfo> communities) {
            this(algorithm, modularityQ, communityCount, nodeAssignments, communities, Collections.emptyList(), 1.0, 0.0);
        }

        public CommunityResult(String algorithm, double modularityQ, int communityCount, int[] nodeAssignments,
                               List<CommunityInfo> communities, List<NodeUncertaintyInfo> nodeUncertainties,
                               double meanNmi, double meanVi) {
            this.algorithm = algorithm;
            this.modularityQ = modularityQ;
            this.communityCount = communityCount;
            this.nodeAssignments = nodeAssignments;
            this.communities = Collections.unmodifiableList(communities);
            this.nodeUncertainties = Collections.unmodifiableList(nodeUncertainties);
            this.meanNmi = meanNmi;
            this.meanVi = meanVi;
        }
    }

    public static final class NodeUncertaintyInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public final int nodeIndex;
        public final String nodeName;
        public final int communityId;
        public final double stability;
        public final int secondChoiceCommunity;
        public final double secondChoiceShare;
        public final double entropy;
        public final boolean isBoundary;

        public NodeUncertaintyInfo(int nodeIndex, String nodeName, int communityId,
                                   double stability, int secondChoiceCommunity,
                                   double secondChoiceShare, double entropy, boolean isBoundary) {
            this.nodeIndex = nodeIndex;
            this.nodeName = nodeName;
            this.communityId = communityId;
            this.stability = stability;
            this.secondChoiceCommunity = secondChoiceCommunity;
            this.secondChoiceShare = secondChoiceShare;
            this.entropy = entropy;
            this.isBoundary = isBoundary;
        }
    }

    private CommunityDetectionService() {}

    /**
     * Executes the Louvain algorithm to partition the network into cohesive communities maximizing modularity Q.
     */
    public static CommunityResult detectLouvain(Network net) {
        return detectLouvain(net, 42L, 1.0);
    }

    /**
     * Executes the parameterized Louvain algorithm with a random seed and resolution parameter gamma.
     */
    public static CommunityResult detectLouvain(Network net, long seed, double resolution) {
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

        // 2.2.2: complete Louvain method (Blondel, Guillaume, Lambiotte &
        // Lefebvre 2008). Before 2.2.2 only phase 1 (local moving) was run,
        // so communities were never aggregated into super-nodes and the
        // modularity reached was systematically lower than the reference
        // implementation (e.g. Q = 0.33 vs 0.42 on a 60-node BA graph).
        //
        // Phase 1: move single (super-)nodes to the neighbouring community
        //          with the largest modularity gain until no move helps.
        // Phase 2: collapse every community into one super-node (edge
        //          weights summed, node strengths preserved) and repeat.
        // The gain of moving node i into community C is
        //     k_i,C - gamma * k_i * Sigma_tot(C) / (2m)
        // which is the Blondel delta-Q up to the positive factor 1/m.
        java.util.Random rng = new java.util.Random(seed);

        // level-0 graph as adjacency lists
        int ln = n;
        int[][] nbr = new int[n][];
        double[][] nbw = new double[n][];
        for (int i = 0; i < n; i++) {
            int cnt = 0;
            for (int j = 0; j < n; j++) if (j != i && adj[i][j] > 0.0) cnt++;
            nbr[i] = new int[cnt];
            nbw[i] = new double[cnt];
            int c = 0;
            for (int j = 0; j < n; j++) {
                if (j != i && adj[i][j] > 0.0) { nbr[i][c] = j; nbw[i][c] = adj[i][j]; c++; }
            }
        }
        double[] k = degrees.clone();
        int[] membership = new int[n]; // original node -> current super-node
        for (int i = 0; i < n; i++) membership[i] = i;

        for (int level = 0; level < 64; level++) {
            int[] comm = new int[ln];
            double[] tot = new double[ln];
            for (int i = 0; i < ln; i++) { comm[i] = i; tot[i] = k[i]; }
            List<Integer> order = new ArrayList<>(ln);
            for (int i = 0; i < ln; i++) order.add(i);
            double[] wTo = new double[ln];
            int[] touched = new int[ln];
            boolean anyMove = false;
            for (int pass = 0; pass < 100; pass++) {
                boolean moved = false;
                Collections.shuffle(order, rng);
                for (int i : order) {
                    if (k[i] == 0.0) continue;
                    int cur = comm[i];
                    int nt = 0;
                    for (int e = 0; e < nbr[i].length; e++) {
                        int c = comm[nbr[i][e]];
                        if (wTo[c] == 0.0) touched[nt++] = c;
                        wTo[c] += nbw[i][e];
                    }
                    tot[cur] -= k[i];
                    int best = cur;
                    double bestGain = wTo[cur] - resolution * k[i] * tot[cur] / m2;
                    for (int t = 0; t < nt; t++) {
                        int c = touched[t];
                        double gain = wTo[c] - resolution * k[i] * tot[c] / m2;
                        if (gain > bestGain + 1e-12) { bestGain = gain; best = c; }
                    }
                    tot[best] += k[i];
                    comm[i] = best;
                    if (best != cur) moved = true;
                    for (int t = 0; t < nt; t++) wTo[touched[t]] = 0.0;
                    wTo[cur] = 0.0;
                }
                if (!moved) break;
                anyMove = true;
            }
            // renumber the level's communities
            int[] ren = new int[ln];
            java.util.Arrays.fill(ren, -1);
            int nk = 0;
            for (int i = 0; i < ln; i++) if (ren[comm[i]] < 0) ren[comm[i]] = nk++;
            for (int i = 0; i < n; i++) membership[i] = ren[comm[membership[i]]];
            if (!anyMove || nk == ln) break;

            // phase 2: aggregate
            List<Map<Integer, Double>> agg = new ArrayList<>(nk);
            for (int c = 0; c < nk; c++) agg.add(new HashMap<>());
            double[] nkDeg = new double[nk];
            for (int i = 0; i < ln; i++) {
                int ci = ren[comm[i]];
                nkDeg[ci] += k[i];
                for (int e = 0; e < nbr[i].length; e++) {
                    int cj = ren[comm[nbr[i][e]]];
                    if (cj != ci) agg.get(ci).merge(cj, nbw[i][e], Double::sum);
                }
            }
            nbr = new int[nk][];
            nbw = new double[nk][];
            for (int c = 0; c < nk; c++) {
                Map<Integer, Double> mp = agg.get(c);
                nbr[c] = new int[mp.size()];
                nbw[c] = new double[mp.size()];
                int e = 0;
                for (Map.Entry<Integer, Double> en : new java.util.TreeMap<>(mp).entrySet()) {
                    nbr[c][e] = en.getKey(); nbw[c][e] = en.getValue(); e++;
                }
            }
            k = nkDeg;
            ln = nk;
        }
        int[] community = membership;

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
     * Consensus Community Detection (Lancichinetti &amp; Fortunato, 2012):
     * Runs multiple stochastic partitions, constructs the co-association matrix D_ij,
     * thresholds at tau, and computes node-level stability and partition uncertainty.
     */
    public static CommunityResult detectConsensus(Network net, int runs, double tau, double resolution, long seed) {
        int n = net.getSize();
        if (n == 0) return detectLouvain(net);
        int r = Math.max(2, runs);

        int[][] runAssignments = new int[r][n];
        double[][] coAssoc = new double[n][n];

        for (int run = 0; run < r; run++) {
            CommunityResult cr = detectLouvain(net, seed + run * 10007L + 1, resolution);
            runAssignments[run] = cr.nodeAssignments;
            for (int i = 0; i < n; i++) {
                for (int j = i; j < n; j++) {
                    if (cr.nodeAssignments[i] == cr.nodeAssignments[j]) {
                        coAssoc[i][j] += 1.0;
                        if (i != j) coAssoc[j][i] += 1.0;
                    }
                }
            }
        }

        // Normalize co-association matrix D_ij in [0, 1]
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                coAssoc[i][j] /= r;
            }
        }

        // Create thresholded co-association network for consensus clustering
        Network consensusNet = new Network(n);
        for (int i = 0; i < n; i++) consensusNet.getActor(i).setName(net.getActor(i).getName());

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (coAssoc[i][j] >= tau) {
                    consensusNet.setValue((float) coAssoc[i][j], i, j);
                    consensusNet.setValue((float) coAssoc[i][j], j, i);
                }
            }
        }

        // Consensus partition on thresholded co-association
        CommunityResult consensusResult = detectLouvain(consensusNet, seed + 9999L, 1.0);
        int[] consensusAss = consensusResult.nodeAssignments;

        // Compute node uncertainties: stability, secondChoice, entropy, boundary
        List<NodeUncertaintyInfo> uncertainties = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int ownComm = consensusAss[i];
            double ownCommSum = 0.0;
            int ownCommSize = 0;

            Map<Integer, Double> commShares = new HashMap<>();
            for (int j = 0; j < n; j++) {
                if (consensusAss[j] == ownComm) {
                    ownCommSum += coAssoc[i][j];
                    ownCommSize++;
                }
                int c = consensusAss[j];
                commShares.put(c, commShares.getOrDefault(c, 0.0) + coAssoc[i][j]);
            }
            double stability = ownCommSize > 0 ? (ownCommSum / ownCommSize) : 1.0;

            // Second choice community
            int secondComm = ownComm;
            double secondVal = 0.0;
            for (Map.Entry<Integer, Double> e : commShares.entrySet()) {
                if (e.getKey() != ownComm && e.getValue() > secondVal) {
                    secondVal = e.getValue();
                    secondComm = e.getKey();
                }
            }

            // Shannon entropy across consensus communities based on co-association distribution
            double entropy = 0.0;
            double totalCommShare = 0.0;
            for (double s : commShares.values()) totalCommShare += s;
            if (totalCommShare > 1e-9) {
                for (double s : commShares.values()) {
                    double p = s / totalCommShare;
                    if (p > 1e-9) entropy -= p * (Math.log(p) / Math.log(2.0));
                }
            }

            boolean isBoundary = stability < 0.70;
            uncertainties.add(new NodeUncertaintyInfo(i, net.getActor(i).getName(), ownComm + 1,
                    stability, secondComm + 1, secondVal / Math.max(1.0, n), entropy, isBoundary));
        }

        // Compute mean pairwise NMI and Variation of Information across runs
        double sumNmi = 0.0;
        double sumVi = 0.0;
        int pairCount = 0;
        for (int a = 0; a < Math.min(r, 10); a++) {
            for (int b = a + 1; b < Math.min(r, 10); b++) {
                sumNmi += computeNmi(runAssignments[a], runAssignments[b]);
                sumVi += computeVi(runAssignments[a], runAssignments[b]);
                pairCount++;
            }
        }
        double meanNmi = pairCount > 0 ? sumNmi / pairCount : 1.0;
        double meanVi = pairCount > 0 ? sumVi / pairCount : 0.0;

        // Compute modularity Q of consensus partition on original network
        float[][] origMat = net.getMatrix();
        double[][] origAdj = new double[n][n];
        double[] origDeg = new double[n];
        double origTotalWeight = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double w = Math.max(origMat[i][j], origMat[j][i]);
                if (w > 0.0) {
                    origAdj[i][j] = w;
                    origAdj[j][i] = w;
                    origDeg[i] += w;
                    origDeg[j] += w;
                    origTotalWeight += w;
                }
            }
        }
        double origM2 = 2.0 * origTotalWeight;
        double origQ = (origM2 > 0.0) ? computeModularity(origAdj, consensusAss, origDeg, origM2) : 0.0;

        List<CommunityInfo> commInfoList = buildCommunityInfoList(net, origMat, consensusAss, consensusResult.communityCount);

        return new CommunityResult("Consensus Louvain (" + r + " runs)", origQ,
                consensusResult.communityCount, consensusAss, commInfoList, uncertainties, meanNmi, meanVi);
    }

    private static double computeNmi(int[] p1, int[] p2) {
        int n = p1.length;
        if (n == 0) return 1.0;
        Map<Integer, Integer> c1 = new HashMap<>();
        Map<Integer, Integer> c2 = new HashMap<>();
        Map<String, Integer> joint = new HashMap<>();

        for (int i = 0; i < n; i++) {
            c1.put(p1[i], c1.getOrDefault(p1[i], 0) + 1);
            c2.put(p2[i], c2.getOrDefault(p2[i], 0) + 1);
            String k = p1[i] + ":" + p2[i];
            joint.put(k, joint.getOrDefault(k, 0) + 1);
        }

        double mi = 0.0;
        for (Map.Entry<String, Integer> e : joint.entrySet()) {
            String[] parts = e.getKey().split(":");
            int k1 = Integer.parseInt(parts[0]);
            int k2 = Integer.parseInt(parts[1]);
            double p12 = (double) e.getValue() / n;
            double pr1 = (double) c1.get(k1) / n;
            double pr2 = (double) c2.get(k2) / n;
            mi += p12 * (Math.log(p12 / (pr1 * pr2)) / Math.log(2.0));
        }

        double h1 = 0.0, h2 = 0.0;
        for (int cnt : c1.values()) {
            double p = (double) cnt / n;
            h1 -= p * (Math.log(p) / Math.log(2.0));
        }
        for (int cnt : c2.values()) {
            double p = (double) cnt / n;
            h2 -= p * (Math.log(p) / Math.log(2.0));
        }

        double denom = Math.sqrt(h1 * h2);
        return denom > 1e-9 ? Math.max(0.0, Math.min(1.0, mi / denom)) : 1.0;
    }

    private static double computeVi(int[] p1, int[] p2) {
        int n = p1.length;
        if (n == 0) return 0.0;
        Map<Integer, Integer> c1 = new HashMap<>();
        Map<Integer, Integer> c2 = new HashMap<>();
        Map<String, Integer> joint = new HashMap<>();

        for (int i = 0; i < n; i++) {
            c1.put(p1[i], c1.getOrDefault(p1[i], 0) + 1);
            c2.put(p2[i], c2.getOrDefault(p2[i], 0) + 1);
            String k = p1[i] + ":" + p2[i];
            joint.put(k, joint.getOrDefault(k, 0) + 1);
        }

        double mi = 0.0;
        for (Map.Entry<String, Integer> e : joint.entrySet()) {
            String[] parts = e.getKey().split(":");
            int k1 = Integer.parseInt(parts[0]);
            int k2 = Integer.parseInt(parts[1]);
            double p12 = (double) e.getValue() / n;
            double pr1 = (double) c1.get(k1) / n;
            double pr2 = (double) c2.get(k2) / n;
            mi += p12 * (Math.log(p12 / (pr1 * pr2)) / Math.log(2.0));
        }

        double h1 = 0.0, h2 = 0.0;
        for (int cnt : c1.values()) {
            double p = (double) cnt / n;
            h1 -= p * (Math.log(p) / Math.log(2.0));
        }
        for (int cnt : c2.values()) {
            double p = (double) cnt / n;
            h2 -= p * (Math.log(p) / Math.log(2.0));
        }

        return Math.max(0.0, h1 + h2 - 2.0 * mi);
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
