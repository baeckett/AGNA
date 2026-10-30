/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

/**
 * Supported null models for statistical hypothesis testing in Social Network Analysis (SNA).
 * Each null model defines a constrained random graph ensemble that preserves specific
 * structural properties of the observed network while randomizing others.
 */
public enum NullModel
    {
    /**
     * Preserves the exact in-degree and out-degree of every single vertex.
     * Uses the Maslov-Sneppen (2002) / Rao-Jana-Bandyopadhyay (1996) double-edge-swap
     * Markov Chain Monte Carlo (MCMC) algorithm. The gold standard in SNA for testing
     * whether higher-order structures (clustering, modularity, path lengths) arise
     * beyond what actor degree heterogeneity explains.
     */
    DEGREE_PRESERVING("Degree-Preserving (Maslov-Sneppen MCMC)",
            "Preserves the exact in-degree and out-degree sequence of every node via double edge swaps."),

    /**
     * Bernoulli / Erdős–Rényi G(n, m) random graph preserving only the total vertex count n
     * and total edge count m (exact density), distributing edges uniformly at random.
     */
    ERDOS_RENYI("Erdős–Rényi G(n, m) Density-Preserving",
            "Preserves node count n and edge count m (density), randomizing all connections uniformly."),

    /**
     * Holland-Leinhardt (1976) U|MAN model preserving the exact dyad census:
     * mutual (M), asymmetric (A), and null (N) dyad counts. Tests whether network
     * patterns exceed what dyadic reciprocity alone explains.
     */
    DYAD_CENSUS("Directed Dyad Census (Holland-Leinhardt U|MAN)",
            "Preserves the exact counts of mutual, asymmetric, and null dyads."),

    /**
     * Preserves the binary network topology while randomly permuting tie weights
     * among existing edges. Tests whether strong ties are non-randomly positioned.
     */
    WEIGHT_PERMUTATION("Weighted Edge Weight Permutation",
            "Preserves graph topology, randomly permuting weights across existing edges.");

    private final String displayName;
    private final String description;

    NullModel(String displayName, String description)
        {
        this.displayName = displayName;
        this.description = description;
        }

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

    public static NullModel fromString(String str)
        {
        if (str == null)
            return DEGREE_PRESERVING;
        String s = str.trim().toLowerCase().replace('-', '_').replace(' ', '_');
        if (s.contains("degree") || s.contains("maslov") || s.contains("swap") || s.contains("config"))
            return DEGREE_PRESERVING;
        if (s.contains("erdos") || s.contains("renyi") || s.contains("bernoulli") || s.contains("density") || s.equals("gnm"))
            return ERDOS_RENYI;
        if (s.contains("dyad") || s.contains("census") || s.contains("man") || s.contains("holland"))
            return DYAD_CENSUS;
        if (s.contains("weight") || s.contains("strength"))
            return WEIGHT_PERMUTATION;
        return DEGREE_PRESERVING;
        }
    }
