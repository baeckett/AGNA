/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import java.io.Serializable;

/**
 * Options for the Clustering & Cohesion Battery in Agna.
 */
public class ClusteringOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean includeLocalClustering = true;
    private boolean includeTransitivity = true;
    private boolean includeKCore = true;
    private boolean includeCohesion = false; // Knoke mutual tie cohesion
    private boolean includeOpenChains = false;
    private boolean includeCliques = false;
    private int cliqueDiameter = 1;
    private boolean includeCommunities = true;
    private boolean useGirvanNewman = false;
    private boolean includeStructuralEquivalence = false;
    private int targetBlocks = 2;

    public ClusteringOptions() {
    }

    public static ClusteringOptions createStandard() {
        return new ClusteringOptions();
    }

    public static ClusteringOptions createAll() {
        ClusteringOptions opt = new ClusteringOptions();
        opt.setIncludeLocalClustering(true);
        opt.setIncludeTransitivity(true);
        opt.setIncludeKCore(true);
        opt.setIncludeCohesion(true);
        opt.setIncludeOpenChains(true);
        opt.setIncludeCliques(true);
        opt.setIncludeCommunities(true);
        opt.setIncludeStructuralEquivalence(true);
        return opt;
    }

    public boolean isIncludeLocalClustering() { return includeLocalClustering; }
    public void setIncludeLocalClustering(boolean includeLocalClustering) { this.includeLocalClustering = includeLocalClustering; }

    public boolean isIncludeTransitivity() { return includeTransitivity; }
    public void setIncludeTransitivity(boolean includeTransitivity) { this.includeTransitivity = includeTransitivity; }

    public boolean isIncludeKCore() { return includeKCore; }
    public void setIncludeKCore(boolean includeKCore) { this.includeKCore = includeKCore; }

    public boolean isIncludeCohesion() { return includeCohesion; }
    public void setIncludeCohesion(boolean includeCohesion) { this.includeCohesion = includeCohesion; }

    public boolean isIncludeOpenChains() { return includeOpenChains; }
    public void setIncludeOpenChains(boolean includeOpenChains) { this.includeOpenChains = includeOpenChains; }

    public boolean isIncludeCliques() { return includeCliques; }
    public void setIncludeCliques(boolean includeCliques) { this.includeCliques = includeCliques; }

    public int getCliqueDiameter() { return cliqueDiameter; }
    public void setCliqueDiameter(int cliqueDiameter) { this.cliqueDiameter = cliqueDiameter; }

    public boolean isIncludeCommunities() { return includeCommunities; }
    public void setIncludeCommunities(boolean includeCommunities) { this.includeCommunities = includeCommunities; }

    public boolean isUseGirvanNewman() { return useGirvanNewman; }
    public void setUseGirvanNewman(boolean useGirvanNewman) { this.useGirvanNewman = useGirvanNewman; }

    public boolean isIncludeStructuralEquivalence() { return includeStructuralEquivalence; }
    public void setIncludeStructuralEquivalence(boolean includeStructuralEquivalence) { this.includeStructuralEquivalence = includeStructuralEquivalence; }

    public int getTargetBlocks() { return targetBlocks; }
    public void setTargetBlocks(int targetBlocks) { this.targetBlocks = targetBlocks; }
}
