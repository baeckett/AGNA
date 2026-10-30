/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.theory;

import java.util.Locale;

/**
 * Metric container holding empirical graph properties evaluated against
 * theoretical archetype signatures.
 */
public class ArchetypeMetrics {
    private final int nodeCount;
    private final int edgeCount;
    private final float density;
    private final boolean directed;

    // Small-World metrics (Humphries & Gurney, 2008; Watts & Strogatz, 1998)
    private float clusteringC;
    private float cRand;
    private float charPathL;
    private float lRand;
    private float smallWorldSigma;

    // Preferential Attachment metrics (Barabási & Albert, 1999; Clauset et al., 2009)
    private float powerLawGamma;
    private float degreeGini;
    private float maxDegreeHubRatio;

    // Homophily & Modularity metrics (Newman 2002, 2006; McPherson et al., 2001)
    private float modularityQ;
    private int communityCount;
    private float degreeAssortativityR;

    // Hierarchy metrics (Krackhardt, 1994; Weber, 1922)
    private float krackhardtConnectedness;
    private float krackhardtHierarchy;
    private float krackhardtEfficiency;
    private float krackhardtLUB;

    // Fit Scores [0.0, 1.0]
    private float smallWorldFit;
    private float preferentialAttachmentFit;
    private float homophilyFit;
    private float hierarchyFit;

    public ArchetypeMetrics(int nodeCount, int edgeCount, float density, boolean directed) {
        this.nodeCount = nodeCount;
        this.edgeCount = edgeCount;
        this.density = density;
        this.directed = directed;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public float getDensity() {
        return density;
    }

    public boolean isDirected() {
        return directed;
    }

    public float getClusteringC() {
        return clusteringC;
    }

    public void setClusteringC(float clusteringC) {
        this.clusteringC = clusteringC;
    }

    public float getCRand() {
        return cRand;
    }

    public void setCRand(float cRand) {
        this.cRand = cRand;
    }

    public float getCharPathL() {
        return charPathL;
    }

    public void setCharPathL(float charPathL) {
        this.charPathL = charPathL;
    }

    public float getLRand() {
        return lRand;
    }

    public void setLRand(float lRand) {
        this.lRand = lRand;
    }

    public float getSmallWorldSigma() {
        return smallWorldSigma;
    }

    public void setSmallWorldSigma(float smallWorldSigma) {
        this.smallWorldSigma = smallWorldSigma;
    }

    public float getPowerLawGamma() {
        return powerLawGamma;
    }

    public void setPowerLawGamma(float powerLawGamma) {
        this.powerLawGamma = powerLawGamma;
    }

    public float getDegreeGini() {
        return degreeGini;
    }

    public void setDegreeGini(float degreeGini) {
        this.degreeGini = degreeGini;
    }

    public float getMaxDegreeHubRatio() {
        return maxDegreeHubRatio;
    }

    public void setMaxDegreeHubRatio(float maxDegreeHubRatio) {
        this.maxDegreeHubRatio = maxDegreeHubRatio;
    }

    public float getModularityQ() {
        return modularityQ;
    }

    public void setModularityQ(float modularityQ) {
        this.modularityQ = modularityQ;
    }

    public int getCommunityCount() {
        return communityCount;
    }

    public void setCommunityCount(int communityCount) {
        this.communityCount = communityCount;
    }

    public float getDegreeAssortativityR() {
        return degreeAssortativityR;
    }

    public void setDegreeAssortativityR(float degreeAssortativityR) {
        this.degreeAssortativityR = degreeAssortativityR;
    }

    public float getKrackhardtConnectedness() {
        return krackhardtConnectedness;
    }

    public void setKrackhardtConnectedness(float krackhardtConnectedness) {
        this.krackhardtConnectedness = krackhardtConnectedness;
    }

    public float getKrackhardtHierarchy() {
        return krackhardtHierarchy;
    }

    public void setKrackhardtHierarchy(float krackhardtHierarchy) {
        this.krackhardtHierarchy = krackhardtHierarchy;
    }

    public float getKrackhardtEfficiency() {
        return krackhardtEfficiency;
    }

    public void setKrackhardtEfficiency(float krackhardtEfficiency) {
        this.krackhardtEfficiency = krackhardtEfficiency;
    }

    public float getKrackhardtLUB() {
        return krackhardtLUB;
    }

    public void setKrackhardtLUB(float krackhardtLUB) {
        this.krackhardtLUB = krackhardtLUB;
    }

    public float getSmallWorldFit() {
        return smallWorldFit;
    }

    public void setSmallWorldFit(float smallWorldFit) {
        this.smallWorldFit = smallWorldFit;
    }

    public float getPreferentialAttachmentFit() {
        return preferentialAttachmentFit;
    }

    public void setPreferentialAttachmentFit(float preferentialAttachmentFit) {
        this.preferentialAttachmentFit = preferentialAttachmentFit;
    }

    public float getHomophilyFit() {
        return homophilyFit;
    }

    public void setHomophilyFit(float homophilyFit) {
        this.homophilyFit = homophilyFit;
    }

    public float getHierarchyFit() {
        return hierarchyFit;
    }

    public void setHierarchyFit(float hierarchyFit) {
        this.hierarchyFit = hierarchyFit;
    }

    public float getFit(StructuralArchetype archetype) {
        switch (archetype) {
            case SMALL_WORLD: return smallWorldFit;
            case PREFERENTIAL_ATTACHMENT: return preferentialAttachmentFit;
            case HOMOPHILY: return homophilyFit;
            case HIERARCHY: return hierarchyFit;
            default: return 0f;
        }
    }
}
