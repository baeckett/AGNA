/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.transform;

import java.io.Serializable;

/**
 * Configuration options for populating / randomizing network ties and edge weights.
 */
public class RandomizeOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Model {
        ERDOS_RENYI_GNP("Erdős–Rényi G(n, p) (Binary Random Ties)"),
        ERDOS_RENYI_GNM("Erdős–Rényi G(n, m) (Fixed Tie Count)"),
        RANDOM_WEIGHTED("Random Weighted Ties (Uniform Continuous)"),
        WATTS_STROGATZ("Watts–Strogatz Small-World"),
        BARABASI_ALBERT("Barabási–Albert Scale-Free (Preferential Attachment)"),
        RANDOM_CONNECTED("Random Connected Network (No Isolates)"),
        DEGREE_PRESERVING("Degree-Preserving Permutation (Preserve Degree Sequence)"),
        WEIGHT_PERMUTATION("Weight Permutation (Shuffle Existing Tie Values)");

        private final String label;

        Model(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private Model model = Model.ERDOS_RENYI_GNP;
    private double probability = 0.20;
    private int tieCount = 10;
    private boolean symmetric = false;
    private double minWeight = 0.10;
    private double maxWeight = 1.00;
    private int wattsStrogatzK = 2;
    private double wattsStrogatzBeta = 0.20;
    private int barabasiAlbertM = 1;
    private double extraEdgeProb = 0.20;
    private int swapsPerEdge = 10;
    private boolean createNewNetwork = false;

    public RandomizeOptions() {
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model != null ? model : Model.ERDOS_RENYI_GNP;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = Math.max(0.0, Math.min(1.0, probability));
    }

    public int getTieCount() {
        return tieCount;
    }

    public void setTieCount(int tieCount) {
        this.tieCount = Math.max(0, tieCount);
    }

    public boolean isSymmetric() {
        return symmetric;
    }

    public void setSymmetric(boolean symmetric) {
        this.symmetric = symmetric;
    }

    public double getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(double minWeight) {
        this.minWeight = minWeight;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }

    public int getWattsStrogatzK() {
        return wattsStrogatzK;
    }

    public void setWattsStrogatzK(int wattsStrogatzK) {
        this.wattsStrogatzK = Math.max(2, wattsStrogatzK);
    }

    public double getWattsStrogatzBeta() {
        return wattsStrogatzBeta;
    }

    public void setWattsStrogatzBeta(double wattsStrogatzBeta) {
        this.wattsStrogatzBeta = Math.max(0.0, Math.min(1.0, wattsStrogatzBeta));
    }

    public int getBarabasiAlbertM() {
        return barabasiAlbertM;
    }

    public void setBarabasiAlbertM(int barabasiAlbertM) {
        this.barabasiAlbertM = Math.max(1, barabasiAlbertM);
    }

    public double getExtraEdgeProb() {
        return extraEdgeProb;
    }

    public void setExtraEdgeProb(double extraEdgeProb) {
        this.extraEdgeProb = Math.max(0.0, Math.min(1.0, extraEdgeProb));
    }

    public int getSwapsPerEdge() {
        return swapsPerEdge;
    }

    public void setSwapsPerEdge(int swapsPerEdge) {
        this.swapsPerEdge = Math.max(1, swapsPerEdge);
    }

    public boolean isCreateNewNetwork() {
        return createNewNetwork;
    }

    public void setCreateNewNetwork(boolean createNewNetwork) {
        this.createNewNetwork = createNewNetwork;
    }
}
