/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import java.io.Serializable;

/**
 * Configuration options for the Full Analysis Battery in Agna.
 * <p>
 * Controls which macro and micro sociometric modules are computed during
 * a composite analysis run, separating zero-configuration polynomial-time
 * metrics from parameterized or exponential-time routines.
 */
public class FullAnalysisOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    // Standard batteries (Enabled by default in Standard profile)
    private boolean includeBasicAndDensity = true;
    private boolean includeDegreeMetrics = true;
    private boolean includePrestige = true;
    private boolean includeGeodesicCentralities = true;
    private boolean includeSpectralCentrality = true;
    private boolean includeClusteringAndCores = true;
    private boolean includeStructuralHoles = true;
    private boolean includeConnectivityAndDyad = true;
    private boolean includeAssortativity = true;
    private boolean includeCommunityDetection = true;
    private boolean useGirvanNewman = false;
    private boolean includeStructuralEquivalence = false;
    private int targetBlocks = 2;
    private boolean includeWeightedMetrics = false;

    // Extended / Parameterized batteries (Disabled by default)
    private boolean includeBonacich = false;
    private double bonacichBeta = 0.10;

    private boolean includePageRank = false;
    private double pageRankDamping = 0.85;

    private boolean includeTriadCensus = false;

    private boolean includeCliques = false;
    private int cliqueDiameter = 1;

    public FullAnalysisOptions() {
    }

    public static FullAnalysisOptions createStandard() {
        return new FullAnalysisOptions();
    }

    public static FullAnalysisOptions createAll() {
        FullAnalysisOptions opt = new FullAnalysisOptions();
        opt.setIncludeBasicAndDensity(true);
        opt.setIncludeDegreeMetrics(true);
        opt.setIncludePrestige(true);
        opt.setIncludeGeodesicCentralities(true);
        opt.setIncludeSpectralCentrality(true);
        opt.setIncludeClusteringAndCores(true);
        opt.setIncludeStructuralHoles(true);
        opt.setIncludeConnectivityAndDyad(true);
        opt.setIncludeBonacich(true);
        opt.setIncludePageRank(true);
        opt.setIncludeTriadCensus(true);
        opt.setIncludeCliques(true);
        return opt;
    }

    public static FullAnalysisOptions createLegacy() {
        FullAnalysisOptions opt = new FullAnalysisOptions();
        opt.setIncludeBasicAndDensity(true);
        opt.setIncludeDegreeMetrics(true);
        opt.setIncludePrestige(true);
        opt.setIncludeGeodesicCentralities(true);
        opt.setIncludeSpectralCentrality(false);
        opt.setIncludeClusteringAndCores(false);
        opt.setIncludeStructuralHoles(false);
        opt.setIncludeConnectivityAndDyad(false);
        opt.setIncludeBonacich(false);
        opt.setIncludePageRank(false);
        opt.setIncludeTriadCensus(false);
        opt.setIncludeCliques(false);
        return opt;
    }

    public boolean isIncludeBasicAndDensity() {
        return includeBasicAndDensity;
    }

    public void setIncludeBasicAndDensity(boolean includeBasicAndDensity) {
        this.includeBasicAndDensity = includeBasicAndDensity;
    }

    public boolean isIncludeDegreeMetrics() {
        return includeDegreeMetrics;
    }

    public void setIncludeDegreeMetrics(boolean includeDegreeMetrics) {
        this.includeDegreeMetrics = includeDegreeMetrics;
    }

    public boolean isIncludePrestige() {
        return includePrestige;
    }

    public void setIncludePrestige(boolean includePrestige) {
        this.includePrestige = includePrestige;
    }

    public boolean isIncludeGeodesicCentralities() {
        return includeGeodesicCentralities;
    }

    public void setIncludeGeodesicCentralities(boolean includeGeodesicCentralities) {
        this.includeGeodesicCentralities = includeGeodesicCentralities;
    }

    public boolean isIncludeSpectralCentrality() {
        return includeSpectralCentrality;
    }

    public void setIncludeSpectralCentrality(boolean includeSpectralCentrality) {
        this.includeSpectralCentrality = includeSpectralCentrality;
    }

    public boolean isIncludeClusteringAndCores() {
        return includeClusteringAndCores;
    }

    public void setIncludeClusteringAndCores(boolean includeClusteringAndCores) {
        this.includeClusteringAndCores = includeClusteringAndCores;
    }

    public boolean isIncludeStructuralHoles() {
        return includeStructuralHoles;
    }

    public void setIncludeStructuralHoles(boolean includeStructuralHoles) {
        this.includeStructuralHoles = includeStructuralHoles;
    }

    public boolean isIncludeConnectivityAndDyad() {
        return includeConnectivityAndDyad;
    }

    public void setIncludeConnectivityAndDyad(boolean includeConnectivityAndDyad) {
        this.includeConnectivityAndDyad = includeConnectivityAndDyad;
    }

    public boolean isIncludeBonacich() {
        return includeBonacich;
    }

    public void setIncludeBonacich(boolean includeBonacich) {
        this.includeBonacich = includeBonacich;
    }

    public double getBonacichBeta() {
        return bonacichBeta;
    }

    public void setBonacichBeta(double bonacichBeta) {
        this.bonacichBeta = bonacichBeta;
    }

    public boolean isIncludePageRank() {
        return includePageRank;
    }

    public void setIncludePageRank(boolean includePageRank) {
        this.includePageRank = includePageRank;
    }

    public double getPageRankDamping() {
        return pageRankDamping;
    }

    public void setPageRankDamping(double pageRankDamping) {
        this.pageRankDamping = pageRankDamping;
    }

    public boolean isIncludeTriadCensus() {
        return includeTriadCensus;
    }

    public void setIncludeTriadCensus(boolean includeTriadCensus) {
        this.includeTriadCensus = includeTriadCensus;
    }

    public boolean isIncludeCliques() {
        return includeCliques;
    }

    public void setIncludeCliques(boolean includeCliques) {
        this.includeCliques = includeCliques;
    }

    public int getCliqueDiameter() {
        return cliqueDiameter;
    }

    public void setCliqueDiameter(int cliqueDiameter) {
        this.cliqueDiameter = cliqueDiameter;
    }

    public boolean isIncludeAssortativity() {
        return includeAssortativity;
    }

    public void setIncludeAssortativity(boolean includeAssortativity) {
        this.includeAssortativity = includeAssortativity;
    }

    public boolean isIncludeCommunityDetection() {
        return includeCommunityDetection;
    }

    public void setIncludeCommunityDetection(boolean includeCommunityDetection) {
        this.includeCommunityDetection = includeCommunityDetection;
    }

    public boolean isUseGirvanNewman() {
        return useGirvanNewman;
    }

    public void setUseGirvanNewman(boolean useGirvanNewman) {
        this.useGirvanNewman = useGirvanNewman;
    }

    public boolean isIncludeStructuralEquivalence() {
        return includeStructuralEquivalence;
    }

    public void setIncludeStructuralEquivalence(boolean includeStructuralEquivalence) {
        this.includeStructuralEquivalence = includeStructuralEquivalence;
    }

    public int getTargetBlocks() {
        return targetBlocks;
    }

    public void setTargetBlocks(int targetBlocks) {
        this.targetBlocks = targetBlocks;
    }

    public boolean isIncludeWeightedMetrics() {
        return includeWeightedMetrics;
    }

    public void setIncludeWeightedMetrics(boolean includeWeightedMetrics) {
        this.includeWeightedMetrics = includeWeightedMetrics;
    }
}
