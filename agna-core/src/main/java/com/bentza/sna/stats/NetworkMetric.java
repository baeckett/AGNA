/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;

/**
 * Standard Social Network Analysis metrics evaluable on networks
 * for statistical hypothesis testing against null models.
 */
public enum NetworkMetric
    {
    RECIPROCITY("Dyadic Reciprocity", "Fraction of directed edges that are reciprocated (2M / (2M + A))")
        {
        @Override
        public double compute(Network net)
            {
            int n = net.getSize();
            if (n < 2)
                return 0.0;
            int mutual = 0;
            int asymmetric = 0;
            for (int i = 0; i < n; i++)
                {
                for (int j = i + 1; j < n; j++)
                    {
                    boolean ij = net.getValue(i, j) != 0f;
                    boolean ji = net.getValue(j, i) != 0f;
                    if (ij && ji)
                        mutual++;
                    else if (ij || ji)
                        asymmetric++;
                    }
                }
            int total = 2 * mutual + asymmetric;
            return total > 0 ? (2.0 * mutual) / (double) total : 0.0;
            }
        },

    TRANSITIVITY("Transitivity (Clustering Coefficient)", "Global transitivity: 3 * triangles / connected triplets")
        {
        @Override
        public double compute(Network net)
            {
            int n = net.getSize();
            if (n < 3)
                return 0.0;
            long triangles = 0;
            long triplets = 0;
            // Treat as adjacency for clustering
            boolean[][] adj = new boolean[n][n];
            int[] deg = new int[n];
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    if (i != j && (net.getValue(i, j) != 0f || net.getValue(j, i) != 0f))
                        {
                        adj[i][j] = true;
                        deg[i]++;
                        }
                    }
                }

            for (int i = 0; i < n; i++)
                {
                if (deg[i] >= 2)
                    triplets += (long) deg[i] * (deg[i] - 1) / 2;
                for (int j = i + 1; j < n; j++)
                    {
                    if (adj[i][j])
                        {
                        for (int k = j + 1; k < n; k++)
                            {
                            if (adj[i][k] && adj[j][k])
                                triangles++;
                            }
                        }
                    }
                }
            return triplets > 0 ? (3.0 * triangles) / (double) triplets : 0.0;
            }
        },

    DENSITY("Network Density", "Proportion of potential ties that are realized")
        {
        @Override
        public double compute(Network net)
            {
            int n = net.getSize();
            if (n < 2)
                return 0.0;
            long possible = (long) n * (n - 1);
            long actual = 0;
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    if (i != j && net.getValue(i, j) != 0f)
                        actual++;
                    }
                }
            return (double) actual / (double) possible;
            }
        },

    DEGREE_CENTRALIZATION("Degree Centralization", "Freeman's general degree centralization index")
        {
        @Override
        public double compute(Network net)
            {
            int n = net.getSize();
            if (n < 3)
                return 0.0;
            double[] degrees = new double[n];
            double maxDeg = 0.0;
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    if (i != j && net.getValue(i, j) != 0f)
                        degrees[i]++;
                    }
                if (degrees[i] > maxDeg)
                    maxDeg = degrees[i];
                }
            double sumDiff = 0.0;
            for (int i = 0; i < n; i++)
                sumDiff += (maxDeg - degrees[i]);

            // Theoretical maximum difference for star graph: (n - 1) * (n - 2)
            double maxPossible = (double) (n - 1) * (n - 2);
            return maxPossible > 0 ? sumDiff / maxPossible : 0.0;
            }
        },

    BETWEENNESS_CENTRALIZATION("Betweenness Centralization", "Freeman's betweenness centralization index")
        {
        @Override
        public double compute(Network net)
            {
            int n = net.getSize();
            if (n < 3)
                return 0.0;
            AgnaLib lib = new AgnaLib();
            float[] b = lib.betweenness(net);
            if (b == null || b.length < n)
                return 0.0;
            double maxB = 0.0;
            for (int i = 0; i < n; i++)
                {
                if (b[i] > maxB)
                    maxB = b[i];
                }
            double sumDiff = 0.0;
            for (int i = 0; i < n; i++)
                sumDiff += (maxB - b[i]);

            double maxPossible = (double) (n - 1) * (n - 1) * (n - 2) / 2.0;
            return maxPossible > 0 ? sumDiff / maxPossible : 0.0;
            }
        },

    CLOSENESS_CENTRALIZATION("Closeness Centralization", "Freeman's closeness centralization index")
        {
        @Override
        public double compute(Network net)
            {
            int n = net.getSize();
            if (n < 3)
                return 0.0;
            AgnaLib lib = new AgnaLib();
            float[] c = lib.closeness(net);
            if (c == null || c.length < n)
                return 0.0;
            double maxC = 0.0;
            for (int i = 0; i < n; i++)
                {
                if (c[i] > maxC)
                    maxC = c[i];
                }
            double sumDiff = 0.0;
            for (int i = 0; i < n; i++)
                sumDiff += (maxC - c[i]);

            double maxPossible = (double) (n - 2) / (double) (2 * n - 3);
            return maxPossible > 0 ? sumDiff / (n * maxPossible) : 0.0;
            }
        },

    AVERAGE_DISTANCE("Average Geodesic Distance", "Mean shortest path length among reachable node pairs")
        {
        @Override
        public double compute(Network net)
            {
            int n = net.getSize();
            if (n < 2)
                return 0.0;
            // BFS shortest paths
            long totalDist = 0;
            long pairs = 0;
            for (int s = 0; s < n; s++)
                {
                int[] dist = new int[n];
                java.util.Arrays.fill(dist, -1);
                dist[s] = 0;
                java.util.ArrayDeque<Integer> q = new java.util.ArrayDeque<>();
                q.add(s);
                while (!q.isEmpty())
                    {
                    int u = q.poll();
                    for (int v = 0; v < n; v++)
                        {
                        if (u != v && net.getValue(u, v) != 0f && dist[v] == -1)
                            {
                            dist[v] = dist[u] + 1;
                            q.add(v);
                            }
                        }
                    }
                for (int t = 0; t < n; t++)
                    {
                    if (s != t && dist[t] > 0)
                        {
                        totalDist += dist[t];
                        pairs++;
                        }
                    }
                }
            return pairs > 0 ? (double) totalDist / (double) pairs : 0.0;
            }
        },

    MODULARITY("Modularity (Newman Q)", "Maximum modularity score Q of community partition")
        {
        @Override
        public double compute(Network net)
            {
            int n = net.getSize();
            if (n < 3)
                return 0.0;
            // Compute modularity using greedy optimization
            double totalWeight = 0.0;
            double[] kOut = new double[n];
            double[] kIn = new double[n];
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    float w = net.getValue(i, j);
                    if (w != 0f)
                        {
                        totalWeight += w;
                        kOut[i] += w;
                        kIn[j] += w;
                        }
                    }
                }
            if (totalWeight <= 0.0)
                return 0.0;

            int[] community = new int[n];
            for (int i = 0; i < n; i++)
                community[i] = i;

            // Compute current modularity Q
            return computeQ(net, community, kOut, kIn, totalWeight);
            }

        private double computeQ(Network net, int[] comm, double[] kOut, double[] kIn, double m)
            {
            int n = net.getSize();
            double q = 0.0;
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    if (comm[i] == comm[j])
                        {
                        double aij = net.getValue(i, j);
                        q += (aij - (kOut[i] * kIn[j]) / m);
                        }
                    }
                }
            return q / m;
            }
        };

    private final String displayName;
    private final String description;

    NetworkMetric(String displayName, String description)
        {
        this.displayName = displayName;
        this.description = description;
        }

    public abstract double compute(Network net);

    public String getDisplayName()
        {
        return displayName;
        }

    public String getDescription()
        {
        return description;
        }

    @Override
    public String toString()
        {
        return displayName;
        }

    public static NetworkMetric fromString(String str)
        {
        if (str == null)
            return TRANSITIVITY;
        String s = str.trim().toLowerCase().replace('-', '_').replace(' ', '_');
        if (s.contains("reciproc"))
            return RECIPROCITY;
        if (s.contains("transit") || s.contains("cluster"))
            return TRANSITIVITY;
        if (s.contains("densit"))
            return DENSITY;
        if (s.contains("deg") && s.contains("centr"))
            return DEGREE_CENTRALIZATION;
        if (s.contains("between") && s.contains("centr"))
            return BETWEENNESS_CENTRALIZATION;
        if (s.contains("close") && s.contains("centr"))
            return CLOSENESS_CENTRALIZATION;
        if (s.contains("dist") || s.contains("path") || s.contains("geodesic"))
            return AVERAGE_DISTANCE;
        if (s.contains("modul") || s.contains("communit"))
            return MODULARITY;
        return TRANSITIVITY;
        }
    }
