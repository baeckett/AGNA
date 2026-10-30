/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

import com.bentza.sna.net.Network;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Executes Monte Carlo statistical hypothesis tests comparing observed social network
 * metrics against randomized null model ensembles. Supports parallel multi-core execution.
 */
public class NetworkHypothesisTester
    {
    /**
     * Runs a Monte Carlo hypothesis test with a random seed.
     */
    public static HypothesisTestResult runTest(Network net, NetworkMetric metric, NullModel nullModel, int replications)
        {
        return runTest(net, metric, nullModel, replications, System.currentTimeMillis());
        }

    /**
     * Runs a deterministic, reproducible Monte Carlo hypothesis test with the given random seed.
     */
    public static HypothesisTestResult runTest(Network net, NetworkMetric metric, NullModel nullModel, int replications, long seed)
        {
        if (net == null)
            throw new IllegalArgumentException("Network cannot be null");
        if (metric == null)
            throw new IllegalArgumentException("NetworkMetric cannot be null");
        if (nullModel == null)
            throw new IllegalArgumentException("NullModel cannot be null");
        if (replications < 10)
            replications = 10;

        double observed = metric.compute(net);
        double[] nullValues = new double[replications];

        // Parallel Monte Carlo replication
        long masterSeed = seed;
        IntStream.range(0, replications).parallel().forEach(r ->
            {
            Random rng = new Random(masterSeed + (long) r * 10007L + 31L);
            Network randomized = NetworkRandomizer.randomize(net, nullModel, rng);
            nullValues[r] = metric.compute(randomized);
            });

        // Compute summary statistics
        double sum = 0.0;
        for (double v : nullValues)
            sum += v;
        double mean = sum / replications;

        double sumSq = 0.0;
        for (double v : nullValues)
            sumSq += (v - mean) * (v - mean);
        double stdDev = replications > 1 ? Math.sqrt(sumSq / (replications - 1)) : 0.0;

        double zScore = stdDev > 1e-12 ? (observed - mean) / stdDev : 0.0;

        // Exact Davison-Hinkley Monte Carlo empirical p-values with +1 pseudocount
        long greaterCount = 0;
        long lessCount = 0;
        long extremeDiffCount = 0;
        double obsDiff = Math.abs(observed - mean);

        for (double v : nullValues)
            {
            if (v >= observed)
                greaterCount++;
            if (v <= observed)
                lessCount++;
            if (Math.abs(v - mean) >= obsDiff - 1e-12)
                extremeDiffCount++;
            }

        double pGreater = (double) (1 + greaterCount) / (double) (replications + 1);
        double pLess = (double) (1 + lessCount) / (double) (replications + 1);
        double pTwoTailed = (double) (1 + extremeDiffCount) / (double) (replications + 1);

        double[] sorted = nullValues.clone();
        Arrays.sort(sorted);
        int idxLow = (int) Math.round(0.025 * (replications - 1));
        int idxHigh = (int) Math.round(0.975 * (replications - 1));
        double ciLower = sorted[Math.max(0, Math.min(replications - 1, idxLow))];
        double ciUpper = sorted[Math.max(0, Math.min(replications - 1, idxHigh))];

        String netName = net.getName();
        if (netName == null || netName.trim().isEmpty())
            netName = "Network (N=" + net.getSize() + ")";

        return new HypothesisTestResult(netName, metric, nullModel, replications,
                observed, mean, stdDev, zScore, pTwoTailed, pGreater, pLess,
                ciLower, ciUpper, nullValues);
        }
    }
