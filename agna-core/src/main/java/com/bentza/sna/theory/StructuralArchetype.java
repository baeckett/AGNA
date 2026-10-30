/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.theory;

/**
 * Canonical structural archetypes representing foundational social network theories.
 */
public enum StructuralArchetype {
    SMALL_WORLD(
            "Small-World / Strength of Weak Ties",
            "Watts & Strogatz (1998); Humphries & Gurney (2008); Travers & Milgram (1969)",
            "High local clustering (triadic closure / strong ties) coupled with short average path length via random bridges (weak ties).",
            "Small-World Quotient σ = (C / C_rand) / (L / L_rand) > 1.0"
    ),
    PREFERENTIAL_ATTACHMENT(
            "Cumulative Advantage / Preferential Attachment (Matthew Effect)",
            "Barabási & Albert (1999); Merton (1968); Price (1976); Clauset, Shalizi & Newman (2009)",
            "Rich-get-richer growth dynamic where prominent actors disproportionately attract new ties, yielding power-law degree distributions and dominant hubs.",
            "Power-law exponent γ ∈ [2.0, 3.5], high degree Gini inequality G > 0.40, prominent hub ratio"
    ),
    HOMOPHILY(
            "Homophily & Echo Chambers (Birds of a Feather)",
            "McPherson, Smith-Lovin & Cook (2001); Blau (1977); Newman (2002, 2006)",
            "In-group preference leading to high intra-community tie density, inter-group structural holes (Burt), and assortative mixing.",
            "Newman Modularity Q > 0.35, Degree Assortativity r > 0.0"
    ),
    HIERARCHY(
            "Formal Authority & Bureaucratic Hierarchy",
            "Weber (1922); Simon (1947); Krackhardt (1994)",
            "Stratified chain of command with asymmetric downward authority, absence of upward feedback cycles, and streamlined structural efficiency.",
            "Krackhardt Graph Hierarchy GH → 1.0, Graph Efficiency GE → 1.0, Directed Acyclicity"
    );

    private final String displayName;
    private final String foundationalLiterature;
    private final String theoreticalMechanism;
    private final String mathematicalSignature;

    StructuralArchetype(String displayName, String literature, String mechanism, String signature) {
        this.displayName = displayName;
        this.foundationalLiterature = literature;
        this.theoreticalMechanism = mechanism;
        this.mathematicalSignature = signature;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFoundationalLiterature() {
        return foundationalLiterature;
    }

    public String getTheoreticalMechanism() {
        return theoreticalMechanism;
    }

    public String getMathematicalSignature() {
        return mathematicalSignature;
    }
}
