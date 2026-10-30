/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.theory;

import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import com.bentza.sna.report.AnalysisReport;
import com.bentza.sna.report.TableData;
import com.bentza.sna.report.TableRegistry;

import java.util.*;

/**
 * Diagnostic classifier that evaluates empirical networks against canonical
 * sociological structural archetypes.
 */
public final class ArchetypeClassifier {

    private ArchetypeClassifier() {}

    /**
     * Computes all structural archetype metrics on the target network.
     */
    public static ArchetypeMetrics computeMetrics(Network net) {
        int n = net.getSize();
        int m = net.getEdgesNumber();
        boolean directed = !net.isSymmetric();
        AgnaLib lib = new AgnaLib();
        float density = lib.density(net);
        float[][] mat = net.getMatrix();

        ArchetypeMetrics metrics = new ArchetypeMetrics(n, m, density, directed);

        if (n < 3 || m == 0) {
            return metrics;
        }

        // 1. Degree sequence
        float[] inDeg = lib.receptionDegree(net);
        float[] outDeg = lib.emissionDegree(net);
        int[] totalDeg = new int[n];
        int maxDeg = 0;
        double sumDeg = 0;
        for (int i = 0; i < n; i++) {
            totalDeg[i] = Math.round(inDeg[i] + (directed ? outDeg[i] : 0));
            if (totalDeg[i] > maxDeg) maxDeg = totalDeg[i];
            sumDeg += totalDeg[i];
        }
        double avgDeg = sumDeg / n;

        // 2. Clustering Coefficient (Humphries & Gurney, 2008; Watts & Strogatz, 1998)
        double sumClust = 0;
        int validClustNodes = 0;
        for (int i = 0; i < n; i++) {
            List<Integer> neighbors = getAdjacentNeighbors(mat, n, i);
            int k = neighbors.size();
            if (k >= 2) {
                int triangles = 0;
                for (int u = 0; u < k; u++) {
                    int n1 = neighbors.get(u);
                    for (int v = u + 1; v < k; v++) {
                        int n2 = neighbors.get(v);
                        if (mat[n1][n2] != 0 || mat[n2][n1] != 0) {
                            triangles++;
                        }
                    }
                }
                double c_i = (2.0 * triangles) / (double) (k * (k - 1));
                sumClust += c_i;
                validClustNodes++;
            }
        }
        float C = (validClustNodes > 0) ? (float) (sumClust / validClustNodes) : 0f;
        float cRand = (float) Math.max(0.0001, (avgDeg / (double) n));
        metrics.setClusteringC(C);
        metrics.setCRand(cRand);

        // 3. Characteristic Path Length L (Geodesics in giant component)
        int[][] geo = lib.geodesics(net);
        double pathSum = 0;
        int pathPairs = 0;
        if (geo != null) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    int d = geo[i][j];
                    if (d > 0) {
                        pathSum += d;
                        pathPairs++;
                    }
                }
            }
        }
        float L = (pathPairs > 0) ? (float) (pathSum / pathPairs) : (float) n;
        float lRand = (avgDeg > 1.0) ? (float) (Math.log(n) / Math.log(avgDeg)) : (float) (n / 2.0);
        if (lRand <= 0) lRand = 1.0f;
        metrics.setCharPathL(L);
        metrics.setLRand(lRand);

        // Small-World Quotient: sigma = (C / C_rand) / (L / L_rand)
        float gammaG = (cRand > 0) ? (C / cRand) : 1f;
        float lambdaG = (lRand > 0) ? (L / lRand) : 1f;
        float sigma = (lambdaG > 0) ? (gammaG / lambdaG) : 0f;
        metrics.setSmallWorldSigma(Math.max(0f, sigma));

        // 4. Preferential Attachment / Power-Law (Clauset et al., 2009; Barabási & Albert, 1999)
        // Degree Gini Coefficient
        double giniDiffSum = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                giniDiffSum += Math.abs(totalDeg[i] - totalDeg[j]);
            }
        }
        float gini = (sumDeg > 0) ? (float) (giniDiffSum / (2.0 * n * sumDeg)) : 0f;
        metrics.setDegreeGini(gini);
        metrics.setMaxDegreeHubRatio((float) (avgDeg > 0 ? (maxDeg / avgDeg) : 0));

        // MLE Power Law Exponent: gamma = 1 + n_tail * [ sum(ln(k / (k_min - 0.5))) ]^(-1)
        int kMin = 1;
        int nTail = 0;
        double logSum = 0;
        for (int i = 0; i < n; i++) {
            if (totalDeg[i] >= kMin) {
                logSum += Math.log((double) totalDeg[i] / (kMin - 0.5));
                nTail++;
            }
        }
        float powerLawGamma = (nTail > 2 && logSum > 0) ? (float) (1.0 + (nTail / logSum)) : 0f;
        metrics.setPowerLawGamma(powerLawGamma);

        // 5. Homophily & Modularity (Newman, 2002, 2006)
        // Degree Assortativity r
        float assortativity = computeDegreeAssortativity(net, totalDeg, m);
        metrics.setDegreeAssortativityR(assortativity);

        // Greedy Modularity Q
        float modularityQ = computeModularity(net, totalDeg, m, metrics);
        metrics.setModularityQ(modularityQ);

        // 6. Krackhardt Hierarchy Dimensions (Krackhardt, 1994)
        computeKrackhardtDimensions(net, lib, metrics);

        // 7. Calculate Archetype Fit Scores [0.0, 1.0]
        calculateFitScores(metrics);

        return metrics;
    }

    private static List<Integer> getAdjacentNeighbors(float[][] mat, int n, int node) {
        List<Integer> list = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            if (j != node && (mat[node][j] != 0 || mat[j][node] != 0)) {
                list.add(j);
            }
        }
        return list;
    }

    private static float computeDegreeAssortativity(Network net, int[] deg, int totalEdges) {
        int n = net.getSize();
        if (totalEdges <= 0) return 0f;
        float[][] mat = net.getMatrix();

        double sumJk = 0;
        double sumJPlusK = 0;
        double sumJ2PlusK2 = 0;
        int countedEdges = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && mat[i][j] != 0) {
                    double jDeg = deg[i];
                    double kDeg = deg[j];
                    sumJk += (jDeg * kDeg);
                    sumJPlusK += (jDeg + kDeg);
                    sumJ2PlusK2 += (jDeg * jDeg + kDeg * kDeg);
                    countedEdges++;
                }
            }
        }

        if (countedEdges == 0) return 0f;
        double term1 = sumJPlusK / (2.0 * countedEdges);
        double num = (sumJk / countedEdges) - (term1 * term1);
        double denom = (sumJ2PlusK2 / (2.0 * countedEdges)) - (term1 * term1);

        if (Math.abs(denom) < 1e-6) return 0f;
        double r = num / denom;
        return (float) Math.max(-1.0, Math.min(1.0, r));
    }

    private static float computeModularity(Network net, int[] deg, int totalEdges, ArchetypeMetrics metrics) {
        int n = net.getSize();
        if (totalEdges <= 0 || n == 0) {
            metrics.setCommunityCount(1);
            return 0f;
        }
        float[][] mat = net.getMatrix();

        // Greedy agglomerative modularity clustering
        int[] community = new int[n];
        for (int i = 0; i < n; i++) community[i] = i;

        double m2 = 2.0 * totalEdges;
        boolean merged;
        do {
            merged = false;
            double bestDeltaQ = 0;
            int bestC1 = -1;
            int bestC2 = -1;

            Set<Integer> activeComms = new HashSet<>();
            for (int c : community) activeComms.add(c);
            List<Integer> commList = new ArrayList<>(activeComms);

            for (int i = 0; i < commList.size(); i++) {
                int c1 = commList.get(i);
                for (int j = i + 1; j < commList.size(); j++) {
                    int c2 = commList.get(j);
                    double e12 = 0;
                    double deg1 = 0;
                    double deg2 = 0;
                    for (int u = 0; u < n; u++) {
                        if (community[u] == c1) {
                            deg1 += deg[u];
                            for (int v = 0; v < n; v++) {
                                if (community[v] == c2 && mat[u][v] != 0) e12++;
                            }
                        } else if (community[u] == c2) {
                            deg2 += deg[u];
                        }
                    }

                    if (e12 > 0) {
                        double deltaQ = (2.0 * e12 / m2) - (2.0 * deg1 * deg2 / (m2 * m2));
                        if (deltaQ > bestDeltaQ) {
                            bestDeltaQ = deltaQ;
                            bestC1 = c1;
                            bestC2 = c2;
                        }
                    }
                }
            }

            if (bestDeltaQ > 1e-4) {
                for (int u = 0; u < n; u++) {
                    if (community[u] == bestC2) community[u] = bestC1;
                }
                merged = true;
            }
        } while (merged);

        // Compute final Q
        Set<Integer> uniqueComms = new HashSet<>();
        for (int c : community) uniqueComms.add(c);
        metrics.setCommunityCount(uniqueComms.size());

        double finalQ = 0;
        for (int c : uniqueComms) {
            double lc = 0;
            double dc = 0;
            for (int u = 0; u < n; u++) {
                if (community[u] == c) {
                    dc += deg[u];
                    for (int v = 0; v < n; v++) {
                        if (community[v] == c && mat[u][v] != 0) lc++;
                    }
                }
            }
            finalQ += (lc / m2) - Math.pow(dc / m2, 2);
        }

        return (float) Math.max(-0.5, Math.min(1.0, finalQ));
    }

    private static void computeKrackhardtDimensions(Network net, AgnaLib lib, ArchetypeMetrics metrics) {
        int n = net.getSize();
        int m = net.getEdgesNumber();
        if (n < 2) return;
        float[][] mat = net.getMatrix();

        // 1. Connectedness: proportion of reachable pairs
        int reachablePairs = 0;
        int totalPairs = n * (n - 1) / 2;
        int[][] geo = lib.geodesics(net);
        if (geo != null) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (geo[i][j] > 0 || geo[j][i] > 0) reachablePairs++;
                }
            }
        }
        float conn = (totalPairs > 0) ? (float) reachablePairs / totalPairs : 0f;
        metrics.setKrackhardtConnectedness(conn);

        // 2. Graph Hierarchy: 1 - (reciprocated dyads / total connected dyads)
        int reciprocated = 0;
        int connectedDyads = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                boolean ij = mat[i][j] != 0;
                boolean ji = mat[j][i] != 0;
                if (ij || ji) {
                    connectedDyads++;
                    if (ij && ji) reciprocated++;
                }
            }
        }
        float gh = (connectedDyads > 0) ? 1.0f - ((float) reciprocated / connectedDyads) : 0f;
        metrics.setKrackhardtHierarchy(Math.max(0f, gh));

        // 3. Graph Efficiency: 1 - (redundant edges / max redundant edges)
        int minEdges = n - 1;
        int maxRedundant = (n * (n - 1)) - minEdges;
        if (m <= minEdges) {
            metrics.setKrackhardtEfficiency(1.0f);
        } else {
            float eff = (maxRedundant > 0) ? 1.0f - ((float) (m - minEdges) / maxRedundant) : 0f;
            metrics.setKrackhardtEfficiency(Math.max(0f, eff));
        }

        // 4. Least Upper Bound (LUB): fraction of pairs with unique common ancestor
        int lubPairs = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int commonAncestors = 0;
                for (int k = 0; k < n; k++) {
                    if (k != i && k != j && geo != null && geo[k][i] > 0 && geo[k][j] > 0) {
                        commonAncestors++;
                    }
                }
                if (commonAncestors == 1) lubPairs++;
            }
        }
        float lub = (totalPairs > 0) ? (float) lubPairs / totalPairs : 0f;
        metrics.setKrackhardtLUB(lub);
    }

    private static void calculateFitScores(ArchetypeMetrics m) {
        // Small-World Fit: sigma >= 3.0 -> 1.0; sigma >= 1.0 -> 0.5..1.0; sigma < 1.0 -> 0..0.5
        float sigma = m.getSmallWorldSigma();
        float swFit;
        if (sigma >= 3.0f) {
            swFit = 1.0f;
        } else if (sigma >= 1.0f) {
            swFit = 0.5f + (0.5f * (sigma - 1.0f) / 2.0f);
        } else {
            swFit = Math.max(0f, 0.5f * sigma);
        }
        m.setSmallWorldFit(swFit);

        // Preferential Attachment Fit: gamma in [2.0, 3.5], high Gini (> 0.35), high hub ratio (> 2.0)
        float gamma = m.getPowerLawGamma();
        float gini = m.getDegreeGini();
        float hubRatio = m.getMaxDegreeHubRatio();
        float paFit = 0f;
        if (gamma >= 1.5f && gamma <= 4.0f) {
            float gammaScore = 1.0f - (Math.abs(gamma - 2.5f) / 1.5f);
            paFit = (0.4f * gammaScore) + (0.35f * Math.min(1.0f, gini / 0.5f)) + (0.25f * Math.min(1.0f, hubRatio / 3.0f));
        } else {
            paFit = (0.5f * Math.min(1.0f, gini / 0.5f)) + (0.5f * Math.min(1.0f, hubRatio / 3.0f));
        }
        m.setPreferentialAttachmentFit(Math.max(0f, Math.min(1.0f, paFit)));

        // Homophily Fit: Modularity Q > 0.35 (Newman 2006) represents strong community clustering
        float q = m.getModularityQ();
        float r = m.getDegreeAssortativityR();
        float qScore;
        if (q >= 0.35f) {
            qScore = 0.7f + (0.3f * Math.min(1.0f, (q - 0.35f) / 0.35f));
        } else if (q > 0.15f) {
            qScore = 0.4f + (0.3f * ((q - 0.15f) / 0.20f));
        } else {
            qScore = Math.max(0f, (q / 0.15f) * 0.4f);
        }
        float rBonus = (r > 0f) ? (0.15f * r) : 0f;
        float homFit = Math.min(1.0f, qScore + rBonus);
        m.setHomophilyFit(Math.max(0f, Math.min(1.0f, homFit)));

        // Hierarchy Fit: GH * GE * Connectedness (directed only; if undirected, max 0.25)
        float gh = m.getKrackhardtHierarchy();
        float ge = m.getKrackhardtEfficiency();
        float conn = m.getKrackhardtConnectedness();
        float hFit = m.isDirected() ? (gh * 0.5f + ge * 0.3f + conn * 0.2f) : (ge * 0.25f);
        m.setHierarchyFit(Math.max(0f, Math.min(1.0f, hFit)));
    }

    /**
     * Generates a comprehensive modern HTML report with SPSS-style notes,
     * radar scores, metric tables, and theoretical interpretation narrative.
     */
    public static AnalysisReport createDiagnosticReport(Network net, ArchetypeMetrics m) {
        AnalysisReport report = new AnalysisReport("Theoretical Structural Archetypes Diagnostic", net.getName());

        // 1. Archetype Goodness-of-Fit Summary Table
        TableData.Builder summaryB = TableData.builder("Theoretical Archetype Fit Summary")
                .network(net.getName())
                .columns("Theoretical Archetype", "Core Sociological Theory", "Goodness-of-Fit", "Diagnostic Verdict")
                .isNumeric(false, false, true, false);

        for (StructuralArchetype arch : StructuralArchetype.values()) {
            float fit = m.getFit(arch);
            String verdict;
            if (fit >= 0.70f) {
                verdict = "Strong Theoretical Match";
            } else if (fit >= 0.45f) {
                verdict = "Moderate Characteristics";
            } else {
                verdict = "Low / Absent Signature";
            }
            summaryB.addRow(arch.getDisplayName(), arch.getFoundationalLiterature(), fit, verdict);
        }
        TableData summaryTable = summaryB.build();
        TableRegistry.register(summaryTable);
        report.addTable(summaryTable);

        // 2. Detailed Metric Diagnostics Table
        TableData.Builder detailB = TableData.builder("Structural Signature Diagnostics")
                .network(net.getName())
                .columns("Structural Dimension", "Observed Metric", "Empirical Value", "Theoretical Benchmark", "Reference")
                .isNumeric(false, false, false, false, false);

        detailB.addRow("Small-World", "Small-World Quotient (σ)", String.format(Locale.ROOT, "%.3f", m.getSmallWorldSigma()), "σ > 1.0 (High C, Low L)", "Humphries & Gurney (2008)");
        detailB.addRow("Small-World", "Clustering (C / C_rand)", String.format(Locale.ROOT, "%.4f / %.4f", m.getClusteringC(), m.getCRand()), "C ≫ C_rand", "Watts & Strogatz (1998)");
        detailB.addRow("Small-World", "Path Length (L / L_rand)", String.format(Locale.ROOT, "%.3f / %.3f", m.getCharPathL(), m.getLRand()), "L ≈ L_rand", "Travers & Milgram (1969)");
        detailB.addRow("Cumulative Advantage", "Power-Law Exponent (γ)", String.format(Locale.ROOT, "%.2f", m.getPowerLawGamma()), "γ ∈ [2.0, 3.5]", "Clauset et al. (2009)");
        detailB.addRow("Cumulative Advantage", "Degree Gini Inequality", String.format(Locale.ROOT, "%.3f", m.getDegreeGini()), "Gini > 0.40", "Merton (1968); Barabási (1999)");
        detailB.addRow("Cumulative Advantage", "Max Hub Ratio (k_max / <k>)", String.format(Locale.ROOT, "%.2f", m.getMaxDegreeHubRatio()), "Ratio > 2.5", "Price (1976)");
        detailB.addRow("Homophily", "Newman Modularity (Q)", String.format(Locale.ROOT, "%.3f (%d comms)", m.getModularityQ(), m.getCommunityCount()), "Q > 0.35", "Newman (2006); Blau (1977)");
        detailB.addRow("Homophily", "Degree Assortativity (r)", String.format(Locale.ROOT, "%.3f", m.getDegreeAssortativityR()), "r > 0.0", "McPherson et al. (2001)");
        detailB.addRow("Formal Hierarchy", "Krackhardt Hierarchy (GH)", String.format(Locale.ROOT, "%.3f", m.getKrackhardtHierarchy()), "GH → 1.0 (Acyclic)", "Krackhardt (1994); Weber (1922)");
        detailB.addRow("Formal Hierarchy", "Krackhardt Efficiency (GE)", String.format(Locale.ROOT, "%.3f", m.getKrackhardtEfficiency()), "GE → 1.0 (Tree-like)", "Simon (1947)");

        TableData detailTable = detailB.build();
        TableRegistry.register(detailTable);
        report.addTable(detailTable);

        // Narrative Synthesis
        StructuralArchetype dominant = getDominantArchetype(m);
        String narrative = "<b>Sociological Interpretation:</b> Network '<b>" + net.getName() + "</b>' exhibits its primary structural affinity toward the <b>"
                + dominant.getDisplayName() + "</b> model (Fit Score: " + String.format(Locale.ROOT, "%.1f%%", m.getFit(dominant) * 100) + "). "
                + dominant.getTheoreticalMechanism();
        report.setNote(narrative, false);

        // SPSS-style Methodological Footnotes
        report.addFootnote("Small-World-Ness σ is computed via Humphries & Gurney (2008), normalizing clustering and path length against an analytical equivalent random graph.");
        report.addFootnote("Power-law scaling exponent γ is estimated via Clauset, Shalizi & Newman (2009) maximum-likelihood estimation (MLE).");
        report.addFootnote("Modularity Q reflects Newman's (2006) modularity optimization detecting community cleavages and homophilous segregation.");
        report.addFootnote("Hierarchy dimensions (GH, GE) follow Krackhardt's (1994) graph-theoretical dimensions of informal organization.");

        return report;
    }

    private static StructuralArchetype getDominantArchetype(ArchetypeMetrics m) {
        StructuralArchetype best = StructuralArchetype.SMALL_WORLD;
        float bestVal = m.getSmallWorldFit();
        for (StructuralArchetype a : StructuralArchetype.values()) {
            if (m.getFit(a) > bestVal) {
                bestVal = m.getFit(a);
                best = a;
            }
        }
        return best;
    }
}
