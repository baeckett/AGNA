/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.transform;

import com.bentza.sna.net.Network;
import com.bentza.sna.net.NetworkGenerator;
import com.bentza.sna.stats.NetworkRandomizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Service providing algorithms to populate and randomize network ties and edge weights.
 * Operates on existing nodes/actors, preserving names, coordinates, and visual attributes
 * while updating the sociomatrix connections according to theoretical SNA random models.
 */
public final class NetworkRandomizeService {

    private NetworkRandomizeService() {
    }

    /**
     * Populates the given network with random ties according to the specified options.
     * Enforces the Agna invariant that all diagonal elements (self-loops) remain 0.0.
     *
     * @param target the network to populate (must have at least 2 nodes)
     * @param options configuration parameters
     * @param rng pseudorandom number generator
     */
    public static void populate(Network target, RandomizeOptions options, Random rng) {
        if (target == null || target.getSize() < 2 || options == null) {
            return;
        }
        if (rng == null) {
            rng = new Random();
        }

        int n = target.getSize();
        switch (options.getModel()) {
            case ERDOS_RENYI_GNP:
                populateErdosRenyiGnp(target, options.getProbability(), options.isSymmetric(), rng);
                break;
            case ERDOS_RENYI_GNM:
                populateErdosRenyiGnm(target, options.getTieCount(), options.isSymmetric(), rng);
                break;
            case RANDOM_WEIGHTED:
                populateRandomWeighted(target, options.getProbability(), options.getMinWeight(), options.getMaxWeight(), options.isSymmetric(), rng);
                break;
            case WATTS_STROGATZ:
                populateWattsStrogatz(target, options.getWattsStrogatzK(), options.getWattsStrogatzBeta(), rng);
                break;
            case BARABASI_ALBERT:
                populateBarabasiAlbert(target, options.getBarabasiAlbertM(), rng);
                break;
            case RANDOM_CONNECTED:
                populateRandomConnected(target, options.getExtraEdgeProb(), rng);
                break;
            case DEGREE_PRESERVING:
                populateDegreePreserving(target, options.getSwapsPerEdge(), rng);
                break;
            case WEIGHT_PERMUTATION:
                populateWeightPermutation(target, rng);
                break;
        }

        // Enforce diagonal invariant
        for (int i = 0; i < n; i++) {
            target.setValue(0.0f, i, i);
        }
    }

    private static void populateErdosRenyiGnp(Network target, double p, boolean symmetric, Random rng) {
        int n = target.getSize();
        clearMatrix(target);

        if (symmetric) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    float val = (rng.nextDouble() < p) ? 1.0f : 0.0f;
                    target.setValue(val, i, j);
                    target.setValue(val, j, i);
                }
            }
        } else {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        float val = (rng.nextDouble() < p) ? 1.0f : 0.0f;
                        target.setValue(val, i, j);
                    }
                }
            }
        }
    }

    private static void populateErdosRenyiGnm(Network target, int m, boolean symmetric, Random rng) {
        int n = target.getSize();
        clearMatrix(target);

        List<int[]> candidates = new ArrayList<>();
        if (symmetric) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    candidates.add(new int[] { i, j });
                }
            }
        } else {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        candidates.add(new int[] { i, j });
                    }
                }
            }
        }

        Collections.shuffle(candidates, rng);
        int toPlace = Math.min(Math.max(0, m), candidates.size());
        for (int k = 0; k < toPlace; k++) {
            int[] pair = candidates.get(k);
            target.setValue(1.0f, pair[0], pair[1]);
            if (symmetric) {
                target.setValue(1.0f, pair[1], pair[0]);
            }
        }
    }

    private static void populateRandomWeighted(Network target, double p, double minW, double maxW, boolean symmetric, Random rng) {
        int n = target.getSize();
        clearMatrix(target);
        float lo = (float) Math.min(minW, maxW);
        float hi = (float) Math.max(minW, maxW);

        if (symmetric) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (rng.nextDouble() < p) {
                        float w = lo + (hi - lo) * rng.nextFloat();
                        w = Math.round(w * 10000.0f) / 10000.0f;
                        target.setValue(w, i, j);
                        target.setValue(w, j, i);
                    }
                }
            }
        } else {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j && rng.nextDouble() < p) {
                        float w = lo + (hi - lo) * rng.nextFloat();
                        w = Math.round(w * 10000.0f) / 10000.0f;
                        target.setValue(w, i, j);
                    }
                }
            }
        }
    }

    private static void populateWattsStrogatz(Network target, int k, double beta, Random rng) {
        int n = target.getSize();
        // k must be an even integer between 2 and n - 1
        int effectiveK = k;
        if (effectiveK < 2) effectiveK = 2;
        if (effectiveK >= n) {
            effectiveK = 2 * ((n - 1) / 2);
        }
        if (effectiveK < 2) {
            populateErdosRenyiGnp(target, 0.20, true, rng);
            return;
        }

        Network generated = NetworkGenerator.createWattsStrogatz(n, effectiveK, beta, rng.nextLong());
        copyMatrix(generated, target);
    }

    private static void populateBarabasiAlbert(Network target, int m, Random rng) {
        int n = target.getSize();
        int effectiveM = Math.max(1, Math.min(m, n - 1));
        Network generated = NetworkGenerator.createBarabasiAlbert(n, effectiveM, rng.nextLong());
        copyMatrix(generated, target);
    }

    private static void populateRandomConnected(Network target, double extraProb, Random rng) {
        int n = target.getSize();
        Network generated = NetworkGenerator.createRandomConnected(n, extraProb, rng.nextLong());
        copyMatrix(generated, target);
    }

    private static void populateDegreePreserving(Network target, int swapsPerEdge, Random rng) {
        int edgeCount = countEdges(target);
        if (edgeCount < 2) {
            // Nothing to rewire
            return;
        }
        Network randomized = NetworkRandomizer.randomizeDegreePreserving(target, swapsPerEdge, rng);
        copyMatrix(randomized, target);
    }

    private static void populateWeightPermutation(Network target, Random rng) {
        int edgeCount = countEdges(target);
        if (edgeCount < 1) {
            return;
        }
        Network randomized = NetworkRandomizer.randomizeWeights(target, rng);
        copyMatrix(randomized, target);
    }

    private static void clearMatrix(Network target) {
        int n = target.getSize();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                target.setValue(0.0f, i, j);
            }
        }
    }

    private static void copyMatrix(Network source, Network target) {
        int n = Math.min(source.getSize(), target.getSize());
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                target.setValue(i == j ? 0.0f : source.getValue(i, j), i, j);
            }
        }
    }

    private static int countEdges(Network net) {
        int n = net.getSize();
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && net.getValue(i, j) != 0.0f) {
                    count++;
                }
            }
        }
        return count;
    }
}
