/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import java.io.Serializable;

/**
 * Options for the Centrality & Prestige Battery in Agna.
 */
public class CentralityOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    // Degree-based centralities
    private boolean includeNodalDegree = true;
    private boolean includeInDegree = true;
    private boolean includeOutDegree = true;
    private boolean includeEmission = false;
    private boolean includeReception = false;
    private boolean includeDetermination = false;

    // Geodesic centralities
    private boolean includeBetweenness = true;
    private boolean includeCloseness = true;
    private boolean includeBavelas = true;
    private boolean includeFareness = false;
    private boolean includePrestige = true;
    private boolean includeSociostatus = false;

    // Spectral centralities
    private boolean includeEigenvector = true;
    private boolean includeBonacich = false;
    private double bonacichBeta = 0.10;
    private boolean includePageRank = false;
    private double pageRankDamping = 0.85;

    public CentralityOptions() {
    }

    public static CentralityOptions createStandard() {
        return new CentralityOptions();
    }

    public static CentralityOptions createAll() {
        CentralityOptions opt = new CentralityOptions();
        opt.setIncludeNodalDegree(true);
        opt.setIncludeInDegree(true);
        opt.setIncludeOutDegree(true);
        opt.setIncludeEmission(true);
        opt.setIncludeReception(true);
        opt.setIncludeDetermination(true);
        opt.setIncludeBetweenness(true);
        opt.setIncludeCloseness(true);
        opt.setIncludeBavelas(true);
        opt.setIncludeFareness(true);
        opt.setIncludePrestige(true);
        opt.setIncludeSociostatus(true);
        opt.setIncludeEigenvector(true);
        opt.setIncludeBonacich(true);
        opt.setIncludePageRank(true);
        return opt;
    }

    public boolean isIncludeNodalDegree() { return includeNodalDegree; }
    public void setIncludeNodalDegree(boolean includeNodalDegree) { this.includeNodalDegree = includeNodalDegree; }

    public boolean isIncludeInDegree() { return includeInDegree; }
    public void setIncludeInDegree(boolean includeInDegree) { this.includeInDegree = includeInDegree; }

    public boolean isIncludeOutDegree() { return includeOutDegree; }
    public void setIncludeOutDegree(boolean includeOutDegree) { this.includeOutDegree = includeOutDegree; }

    public boolean isIncludeEmission() { return includeEmission; }
    public void setIncludeEmission(boolean includeEmission) { this.includeEmission = includeEmission; }

    public boolean isIncludeReception() { return includeReception; }
    public void setIncludeReception(boolean includeReception) { this.includeReception = includeReception; }

    public boolean isIncludeDetermination() { return includeDetermination; }
    public void setIncludeDetermination(boolean includeDetermination) { this.includeDetermination = includeDetermination; }

    public boolean isIncludeBetweenness() { return includeBetweenness; }
    public void setIncludeBetweenness(boolean includeBetweenness) { this.includeBetweenness = includeBetweenness; }

    public boolean isIncludeCloseness() { return includeCloseness; }
    public void setIncludeCloseness(boolean includeCloseness) { this.includeCloseness = includeCloseness; }

    public boolean isIncludeBavelas() { return includeBavelas; }
    public void setIncludeBavelas(boolean includeBavelas) { this.includeBavelas = includeBavelas; }

    public boolean isIncludeFareness() { return includeFareness; }
    public void setIncludeFareness(boolean includeFareness) { this.includeFareness = includeFareness; }

    public boolean isIncludePrestige() { return includePrestige; }
    public void setIncludePrestige(boolean includePrestige) { this.includePrestige = includePrestige; }

    public boolean isIncludeSociostatus() { return includeSociostatus; }
    public void setIncludeSociostatus(boolean includeSociostatus) { this.includeSociostatus = includeSociostatus; }

    public boolean isIncludeEigenvector() { return includeEigenvector; }
    public void setIncludeEigenvector(boolean includeEigenvector) { this.includeEigenvector = includeEigenvector; }

    public boolean isIncludeBonacich() { return includeBonacich; }
    public void setIncludeBonacich(boolean includeBonacich) { this.includeBonacich = includeBonacich; }

    public double getBonacichBeta() { return bonacichBeta; }
    public void setBonacichBeta(double bonacichBeta) { this.bonacichBeta = bonacichBeta; }

    public boolean isIncludePageRank() { return includePageRank; }
    public void setIncludePageRank(boolean includePageRank) { this.includePageRank = includePageRank; }

    public double getPageRankDamping() { return pageRankDamping; }
    public void setPageRankDamping(double pageRankDamping) { this.pageRankDamping = pageRankDamping; }
}
