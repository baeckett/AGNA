/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import com.bentza.sna.capital.StructuralHoles;
import com.bentza.sna.census.DyadCensus;
import com.bentza.sna.census.TriadCensus;
import com.bentza.sna.clustering.ClusteringMetrics;
import com.bentza.sna.clustering.KCoreDecomposition;
import com.bentza.sna.community.CommunityDetectionService;
import com.bentza.sna.connectivity.ConnectivityAnalysis;
import com.bentza.sna.core.AppRuntime;
import com.bentza.sna.equivalence.StructuralEquivalenceService;
import com.bentza.sna.net.Actor;
import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import com.bentza.sna.spectral.SpectralCentrality;
import com.bentza.sna.stats.DegreeAssortativity;
import com.bentza.sna.stats.WeightedMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Factory that synthesizes structured {@link AnalysisReport} and {@link TableData}
 * instances for all AGNA analytical computations.
 */
public final class AnalysisReportFactory {

    private AnalysisReportFactory() {}

    /**
     * Builds a standard Statistics TableData from AgnaLib's float[11] distribution.
     */
    public static TableData createStatisticsTable(String title, String networkName, float[] stats) {
        if (stats == null) return null;
        TableData.Builder b = TableData.builder(title)
                .network(networkName)
                .columns("Statistic", "Value")
                .isNumeric(false, true);

        if (stats[0] != Float.POSITIVE_INFINITY && stats[1] != Float.NEGATIVE_INFINITY) {
            b.addRow("Minimum", stats[0]);
            b.addRow("Maximum", stats[1]);
        }
        b.addRow("Sum", stats[2]);
        b.addRow("Mean", stats[3]);
        b.addRow("Variance", stats[4]);
        b.addRow("Standard Deviance", stats[5]);
        if (stats.length > 6 && stats[6] != 0) {
            b.addRow("Absolute Entropy (natural log.)", stats[6]);
            b.addRow("Maximum Entropy (natural log.)", stats[7]);
            b.addRow("Absolute Entropy (base 2 log.)", stats[9]);
            b.addRow("Maximum Entropy (base 2 log.)", stats[10]);
            b.addRow("Relative Entropy (%)", stats[8]);
        }
        TableData td = b.build();
        TableRegistry.register(td);
        return td;
    }

    public static AnalysisReport createBasicReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        int edges = src.getEdgesNumber();
        boolean symmetric = src.isSymmetric();
        int outsiders = lib.getOusidersNumber(src);
        boolean connected = lib.isConnected(src);
        float min = src.getMin();
        float max = src.getMax();

        AnalysisReport report = new AnalysisReport("Basic Network Description", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Network Structural Parameters")
                .network(src.getName())
                .columns("Property", "Value", "Description")
                .isNumeric(false, false, false);

        b.addRow("Node Count (n)", String.valueOf(size), size == 0 ? "Null network" : (size == 1 ? "Trivial network" : "Total actors"));
        if (!symmetric) {
            b.addRow("Directed Edges (m)", String.valueOf(edges), "Arcs present in sociomatrix");
        } else {
            b.addRow("Undirected Edges", String.valueOf((int) (edges / 2)), "Unordered pairs with ties");
            b.addRow("Directed Edges", String.valueOf(edges), "Directed representation of ties");
        }
        b.addRow("Directed Density", String.format(Locale.ROOT, "%.4f", lib.density(src)), "Arcs / (n*(n-1))");
        b.addRow("Reciprocity / Symmetry", symmetric ? "Symmetric (Nondirected)" : "Asymmetric (Directed)", symmetric ? "Ties are mutual" : "Ties are directed");

        String weightStr = "Uniform";
        if (size != 0 && edges != 0) {
            if (max == min) {
                weightStr = (min == 1.0f) ? "Binary (0/1)" : "Uniform (" + min + ")";
            } else {
                weightStr = "Weighted (non-uniform)";
            }
        }
        b.addRow("Weight Structure", weightStr, "Edge value scale");
        b.addRow("Min Tie Weight", String.valueOf(min), "Minimum observed weight");
        b.addRow("Max Tie Weight", String.valueOf(max), "Maximum observed weight");
        b.addRow("Connectivity", connected ? (lib.isStronglyConnected(src) ? "Connected (Strongly)" : "Connected (Weakly)") : "Disconnected",
                connected ? (lib.isStronglyConnected(src) ? "All node pairs mutually reachable in both directions" : "Single connected component (reachability exists across all nodes)") : "Multiple disconnected components");
        b.addRow("Outsider Nodes", String.valueOf(outsiders), "Isolated actors with zero ties");

        TableData paramsTable = b.build();
        TableRegistry.register(paramsTable);
        report.addTable(paramsTable);

        if (outsiders > 0) {
            TableData.Builder outB = TableData.builder("Outsiders (Isolated Actors)")
                    .network(src.getName())
                    .columns("Index", "Actor Name")
                    .isNumeric(true, false);
            for (int i = 0; i < size; i++) {
                if (src.isOutsider(i)) {
                    outB.addRow(i + 1, src.getActor(i).getName());
                }
            }
            TableData outTable = outB.build();
            TableRegistry.register(outTable);
            report.addTable(outTable);
        }

        if (connected) {
            report.setNote("The network is connected.", false);
        } else {
            report.setNote("The network is disconnected (not all pairs of nodes are mutually reachable).", true);
        }
        report.addFootnote("Active sociomatrix dimensions: N = " + size + " actors, " + edges + " ties.");
        report.addFootnote("Directed density is computed as m / [n(n - 1)]; undirected density as 2m / [n(n - 1)].");
        report.addFootnote("Outsider nodes represent isolated actors with degree k_i = 0.");
        return report;
    }

    public static AnalysisReport createDensityReport(Network src, AgnaLib lib, String legacy) {
        float d = lib.density(src);
        boolean sym = src.isSymmetric();
        boolean weighted = AppRuntime.getCurrentWeight();
        float wd = weighted ? lib.weightedDensity(src) : 0f;

        AnalysisReport report = new AnalysisReport("Network Density", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Density Summary")
                .network(src.getName())
                .columns("Convention", "Value", "Definition")
                .isNumeric(false, true, false);

        b.addRow("Directed Convention", d, "Arcs / (n*(n-1))");
        if (sym) {
            b.addRow("Undirected Convention", d, "Edges / (n*(n-1)/2); identical to directed density for symmetric networks");
            report.setNote("Undirected convention (edges / (n*(n-1)/2)): the value is the same for a symmetric matrix.", false);
        } else {
            report.setNote("Note: the matrix is asymmetric; an undirected density is not defined.", false);
        }
        if (weighted) {
            b.addRow("Weighted Density", wd, "Mean tie weight across all dyadic pairs");
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);
        report.addFootnote("Directed density is defined as m / [n*(n - 1)]. Self-loops are treated as 0 and excluded.");
        report.addFootnote("Ties with weight zero are treated as null/absent dyads.");
        return report;
    }

    public static AnalysisReport createCohesionReport(Network src, AgnaLib lib, String legacy) {
        float c = lib.cohesion(src);
        AnalysisReport report = new AnalysisReport("Network Cohesion", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Cohesion Summary")
                .network(src.getName())
                .columns("Metric", "Value", "Interpretation")
                .isNumeric(false, true, false);
        b.addRow("Cohesion Index", c, "Structural compactness based on geodesic distances");
        TableData ct = b.build();
        TableRegistry.register(ct);
        report.addTable(ct);
        report.addFootnote("Cohesion index measures overall dyadic compactness based on topological shortest paths.");
        report.addFootnote("Disconnected node pairs are assigned infinite distance and penalized in the cohesion score.");
        return report;
    }

    public static AnalysisReport createInDegreeReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        float[] outrec = lib.receptionDegree(src);
        float[] outrec_star = new float[size];
        float[] raw = new float[size];
        System.arraycopy(outrec, 0, raw, 0, size);
        for (int i = 0; i < size; i++) {
            outrec_star[i] = (float) ((double) outrec[i] / (double) (size - 1));
            outrec[i] = (float) ((double) outrec[i] / (double) size);
        }

        AnalysisReport report = new AnalysisReport("Distribution of Indegree", src.getName());
        report.setRawPlainText(legacy);
        report.setNote("* relative to number of all other nodes (self excluded)\n** relative to number of all nodes (self included)", false);

        TableData.Builder b = TableData.builder("Distribution of Indegree in " + src.getName())
                .subtitle("* relative to N-1 (self excluded) | ** relative to N (self included)")
                .network(src.getName())
                .columns("Node", "Indegree*", "Indegree**")
                .isNumeric(false, true, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), outrec[i], outrec_star[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(outrec);
        TableData st = createStatisticsTable("Indegree Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);

        report.addFootnote("Indegree* is normalized by the maximum possible inward ties (N - 1), excluding the actor itself.");
        report.addFootnote("Indegree** is relative to the total network size N, including self.");
        report.addFootnote("Diagonal entries (self-loops) are excluded from inbound degree summations.");

        return report;
    }

    public static AnalysisReport createOutDegreeReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        float[] outem = lib.emissionDegree(src);
        float[] outem_star = new float[size];
        for (int i = 0; i < size; i++) {
            outem_star[i] = (float) ((double) outem[i] / (double) (size - 1));
            outem[i] = (float) ((double) outem[i] / (double) size);
        }

        AnalysisReport report = new AnalysisReport("Distribution of Outdegree", src.getName());
        report.setRawPlainText(legacy);
        report.setNote("* relative to number of all other nodes (self excluded)\n** relative to number of all nodes (self included)", false);

        TableData.Builder b = TableData.builder("Distribution of Outdegree in " + src.getName())
                .subtitle("* relative to N-1 (self excluded) | ** relative to N (self included)")
                .network(src.getName())
                .columns("Node", "Outdegree*", "Outdegree**")
                .isNumeric(false, true, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), outem[i], outem_star[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(outem);
        TableData st = createStatisticsTable("Outdegree Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);

        report.addFootnote("Outdegree* is normalized by the maximum possible outward ties (N - 1), excluding the actor itself.");
        report.addFootnote("Outdegree** is relative to the total network size N, including self.");
        report.addFootnote("Diagonal entries (self-loops) are excluded from outbound degree summations.");

        return report;
    }

    public static AnalysisReport createNodalDegreeReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        float[] deg = lib.nodalDegree(src);
        float[] deg_star = new float[size];
        for (int i = 0; i < size; i++) {
            deg_star[i] = (float) ((double) deg[i] / (double) (size - 1));
        }

        AnalysisReport report = new AnalysisReport("Distribution of Nodal Degree", src.getName());
        report.setRawPlainText(legacy);
        report.setNote("* relative to number of all other nodes (self excluded)", false);
        if (deg != null && size > 2) {
            float cent = lib.freemanDegreeCentralization(src);
            report.addProperty("Freeman (1979) Degree Centralization (C_D)", String.format(Locale.ROOT, "%.4f", cent));
        }

        TableData.Builder b = TableData.builder("Distribution of Nodal Degree in " + src.getName())
                .network(src.getName())
                .columns("Node", "Degree", "Relative Degree*")
                .isNumeric(false, true, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), deg[i], deg_star[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(deg);
        TableData st = createStatisticsTable("Nodal Degree Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);

        report.addFootnote("Nodal Degree represents total incident ties (In-Degree + Out-Degree).");
        report.addFootnote("Relative Degree* is normalized by (N - 1) for undirected networks, or 2(N - 1) for directed networks.");
        report.addFootnote("Self-loops are treated as 0 and excluded from degree sums.");

        return report;
    }

    public static AnalysisReport createEmissionReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        float[] em = lib.emissionDegree(src);
        AnalysisReport report = new AnalysisReport("Distribution of Emission Coefficients", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Emission Coefficients in " + src.getName())
                .network(src.getName())
                .columns("Node", "Emission Coefficient")
                .isNumeric(false, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), em[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(em);
        TableData st = createStatisticsTable("Emission Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);
        report.addFootnote("Emission Coefficient measures actor signaling capacity across outbound ties.");
        return report;
    }

    public static AnalysisReport createReceptionReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        float[] rec = lib.receptionDegree(src);
        AnalysisReport report = new AnalysisReport("Distribution of Reception Coefficients", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Reception Coefficients in " + src.getName())
                .network(src.getName())
                .columns("Node", "Reception Coefficient")
                .isNumeric(false, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), rec[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(rec);
        TableData st = createStatisticsTable("Reception Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);
        report.addFootnote("Reception Coefficient measures actor receiving capacity across inbound ties.");
        return report;
    }

    public static AnalysisReport createDeterminationReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        float[] det = lib.determinationDegree(src);
        AnalysisReport report = new AnalysisReport("Determination Degree", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Determination Degree in " + src.getName())
                .network(src.getName())
                .columns("Node", "Determination Degree")
                .isNumeric(false, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), det[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(det);
        TableData st = createStatisticsTable("Determination Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);
        report.addFootnote("Determination Degree computes the balance between outbound influence and inbound reception.");
        return report;
    }

    public static AnalysisReport createSociometricStatusReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        float[] stat = lib.sociometricStatus(src);
        AnalysisReport report = new AnalysisReport("Sociometric Status", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Sociometric Status in " + src.getName())
                .network(src.getName())
                .columns("Node", "Sociometric Status")
                .isNumeric(false, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), stat[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(stat);
        TableData st = createStatisticsTable("Sociometric Status Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);
        report.addFootnote("Sociometric Status measures choice popularity scaled relative to group size (N - 1).");
        return report;
    }

    public static AnalysisReport createBetweennessReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        boolean connected = lib.isConnected(src);
        AnalysisReport report = new AnalysisReport("Betweenness Centrality", src.getName());
        report.setRawPlainText(legacy);

        if (!lib.isStronglyConnected(src)) {
            String warnMsg = "Note: Network is not strongly connected. Betweenness is computed on all existing shortest paths.";
            report.setNote(warnMsg, true);
        }

        float[] betw = lib.betweenness(src);
        float cent = (betw != null) ? lib.freemanGeneralIndex(betw, lib.betweenness(new Network(size, 1))) : 0f;

        report.addProperty("Freeman (1979) Betweenness Centralization (C_B)", String.format(Locale.ROOT, "%.4f", cent));

        TableData.Builder b = TableData.builder("Distribution of Betweenness in " + src.getName())
                .network(src.getName())
                .columns("Node", "Betweenness")
                .isNumeric(false, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), (betw != null && i < betw.length) ? betw[i] : 0f);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(betw);
        TableData st = createStatisticsTable("Betweenness Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);

        report.addFootnote("Freeman betweenness centrality reflects the proportion of all shortest paths passing through the actor.");
        report.addFootnote("Normalized betweenness divides raw betweenness by (N - 1)(N - 2) / 2 (undirected) or (N - 1)(N - 2) (directed).");
        report.addFootnote("Shortest paths are computed using Brandes' betweenness algorithm.");

        return report;
    }

    public static AnalysisReport createClosenessReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        boolean connected = lib.isConnected(src);
        AnalysisReport report = new AnalysisReport("Closeness Centrality", src.getName());
        report.setRawPlainText(legacy);

        if (!lib.isStronglyConnected(src)) {
            String warnMsg = "Note: Network is not strongly connected. Freeman closeness values reflect reachable nodes only.";
            report.setNote(warnMsg, true);
        }

        float[] closeness = lib.closeness(src);
        float cent = (closeness != null) ? lib.freemanGeneralIndex(closeness, lib.closeness(new Network(size, 1))) : 0f;

        report.addProperty("Freeman (1979) Closeness Centralization (C_C)", String.format(Locale.ROOT, "%.4f", cent));

        TableData.Builder b = TableData.builder("Distribution of Closeness in " + src.getName())
                .network(src.getName())
                .columns("Node", "Closeness", "Standard Closeness")
                .isNumeric(false, true, true);

        for (int i = 0; i < size; i++) {
            float val = (closeness != null && i < closeness.length) ? closeness[i] : 0f;
            b.addRow(src.getActor(i).getName(), val, val * (size - 1));
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(closeness);
        TableData st = createStatisticsTable("Closeness Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);

        report.addFootnote("Closeness centrality is the reciprocal of the total geodesic distance from an actor to all reachable nodes.");
        report.addFootnote("Standard Closeness is normalized by (N - 1).");
        report.addFootnote("For disconnected networks, unreachable pairs are omitted from standard distance summation.");

        return report;
    }

    public static AnalysisReport createFarenessReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        boolean connected = lib.isConnected(src);
        AnalysisReport report = new AnalysisReport("Fareness Centrality", src.getName());
        report.setRawPlainText(legacy);

        if (!lib.isStronglyConnected(src)) {
            String warnMsg = "Note: Network is not strongly connected. Fareness reflects reachable nodes only.";
            report.setNote(warnMsg, true);
        }

        float[] fare = lib.fareness(src);
        if (connected && fare != null && size > 2) {
            float cent = lib.freemanGeneralIndex(fare, lib.fareness(new Network(size, 1)));
            report.addProperty("Freeman (1979) Fareness Centralization", String.format(Locale.ROOT, "%.4f", cent));
        }
        TableData.Builder b = TableData.builder("Distribution of Fareness in " + src.getName())
                .network(src.getName())
                .columns("Node", "Fareness")
                .isNumeric(false, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), fare[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(fare);
        TableData st = createStatisticsTable("Fareness Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);

        report.addFootnote("Fareness (farness) is the sum of geodesic shortest distances from an actor to all other reachable actors.");
        return report;
    }

    public static AnalysisReport createEccentricityReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        boolean connected = lib.isConnected(src);
        AnalysisReport report = new AnalysisReport("Eccentricity / Graph Centrality", src.getName());
        report.setRawPlainText(legacy);

        if (!lib.isStronglyConnected(src)) {
            String warnMsg = "Note: Network is not strongly connected. Eccentricity reflects maximum reachable distance.";
            report.setNote(warnMsg, true);
        }

        float[] ecc = lib.eccentricity(src);
        TableData.Builder b = TableData.builder("Distribution of Eccentricity in " + src.getName())
                .network(src.getName())
                .columns("Node", "Eccentricity")
                .isNumeric(false, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), ecc[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(ecc);
        TableData st = createStatisticsTable("Eccentricity Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);

        report.addFootnote("Eccentricity represents the maximum shortest-path distance from an actor to any other reachable actor in the graph.");
        report.addFootnote("The minimum eccentricity across all actors defines the network radius; the maximum defines the network diameter.");
        return report;
    }

    public static AnalysisReport createDiameterReport(Network src, AgnaLib lib, String legacy) {
        int[][] geo = lib.geodesics(src);
        int diam = (geo != null && geo.length > 0) ? (int) lib.matrixMax(geo) : 0;
        AnalysisReport report = new AnalysisReport("Graph Diameter & Extremes", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Distance & Diameter Summary")
                .network(src.getName())
                .columns("Metric", "Value", "Description")
                .isNumeric(false, false, false);

        b.addRow("Graph Diameter", String.valueOf(diam), "Maximum shortest path distance between any two reachable nodes");
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);
        report.addFootnote("Network diameter represents the longest finite geodesic distance between any pair of reachable nodes.");
        report.addFootnote("Disconnected pairs are excluded from the diameter calculation.");
        return report;
    }

    public static AnalysisReport createGeodesicsReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        int[][] geo = lib.geodesics(src);

        double sumDist = 0;
        int countPairs = 0;
        if (geo != null) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i != j && geo[i][j] > 0) {
                        sumDist += geo[i][j];
                        countPairs++;
                    }
                }
            }
        }
        float avg = countPairs > 0 ? (float) (sumDist / countPairs) : 0f;

        AnalysisReport report = new AnalysisReport("Geodesic Distance Matrix", src.getName());
        report.setRawPlainText(legacy);
        report.addProperty("Average Geodesic Distance", String.format(Locale.ROOT, "%.4f", avg));

        List<String> colNames = new ArrayList<>(size + 1);
        colNames.add("Node");
        boolean[] isNum = new boolean[size + 1];
        for (int i = 0; i < size; i++) {
            colNames.add(src.getActor(i).getName());
            isNum[i + 1] = true;
        }

        TableData.Builder b = TableData.builder("Geodesic Distances Matrix in " + src.getName())
                .subtitle("Shortest path distance between all pairs of nodes")
                .network(src.getName())
                .columns(colNames)
                .isNumeric(isNum);

        for (int i = 0; i < size; i++) {
            List<Object> row = new ArrayList<>(size + 1);
            row.add(src.getActor(i).getName());
            for (int j = 0; j < size; j++) {
                int dist = (geo != null && i < geo.length && j < geo[i].length) ? geo[i][j] : -1;
                row.add(dist >= 0 ? dist : "-");
            }
            b.addRow(row);
        }
        TableData matTable = b.build();
        TableRegistry.register(matTable);
        report.addTable(matTable);

        TableData.Builder sumB = TableData.builder("Geodesic Summary")
                .network(src.getName())
                .columns("Metric", "Value")
                .isNumeric(false, true);
        sumB.addRow("Average Geodesic Distance", avg);
        TableData sumTable = sumB.build();
        TableRegistry.register(sumTable);
        report.addTable(sumTable);

        report.addFootnote("Geodesic matrix cell (i, j) indicates the topological shortest path distance between actor i and actor j.");
        report.addFootnote("Unreachable actor pairs are indicated by '-' (infinite distance).");

        return report;
    }

    public static AnalysisReport createBavelasReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        boolean connected = lib.isConnected(src);
        AnalysisReport report = new AnalysisReport("Bavelas Centrality", src.getName());
        report.setRawPlainText(legacy);

        if (!lib.isStronglyConnected(src)) {
            report.setNote("Note: Network is not strongly connected. Bavelas centrality reflects reachable nodes only.", true);
        }

        float[] bav = lib.bavelas(src);
        if (connected && bav != null && size > 2) {
            float cent = lib.freemanGeneralIndex(bav, lib.bavelas(new Network(size, 1)));
            report.addProperty("Bavelas-Leavitt Centralization (C_Bav)", String.format(Locale.ROOT, "%.4f", cent));
        }
        TableData.Builder b = TableData.builder("Bavelas-Leavitt Centrality in " + src.getName())
                .network(src.getName())
                .columns("Node", "Bavelas Centrality")
                .isNumeric(false, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), bav[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(bav);
        TableData st = createStatisticsTable("Bavelas Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);
        report.addFootnote("Bavelas Centrality index measures relative distance advantage in centralized communication structures.");
        return report;
    }

    public static AnalysisReport createPrestigeReport(Network src, AgnaLib lib, String legacy) {
        int size = src.getSize();
        float[] prest = lib.prestige(src);
        AnalysisReport report = new AnalysisReport("Prestige Measures", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Distribution of Prestige in " + src.getName())
                .network(src.getName())
                .columns("Node", "Prestige")
                .isNumeric(false, true);

        for (int i = 0; i < size; i++) {
            b.addRow(src.getActor(i).getName(), prest[i]);
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(prest);
        TableData st = createStatisticsTable("Prestige Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);
        report.addFootnote("Prestige index reflects asymmetric prominence derived from inbound choices.");
        return report;
    }

    public static AnalysisReport createCliquesReport(Network src, int diameter, AgnaLib lib, String legacy) {
        AnalysisReport report = new AnalysisReport(diameter + "-Cliques Enumeration", src.getName());
        report.setRawPlainText(legacy);

        List<List<Integer>> cliques = lib.getCliquesList(src, diameter);
        TableData.Builder b = TableData.builder(diameter + "-Cliques in " + src.getName())
                .network(src.getName())
                .columns("Clique #", "Size", "Actors / Members")
                .isNumeric(true, true, false);

        if (cliques == null || cliques.isEmpty()) {
            b.addRow(1, 0, "No " + diameter + "-cliques found in this network.");
            report.setNote("No " + diameter + "-cliques were found.", false);
        } else {
            for (int k = 0; k < cliques.size(); k++) {
                List<Integer> c = cliques.get(k);
                StringBuilder members = new StringBuilder();
                for (int m = 0; m < c.size(); m++) {
                    if (m > 0) members.append(", ");
                    members.append(src.getActor(c.get(m)).getName());
                }
                b.addRow(k + 1, c.size(), members.toString());
            }
            report.addProperty("Total " + diameter + "-Cliques Found", String.valueOf(cliques.size()));
        }

        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);
        report.addFootnote("A clique is a maximal complete subgraph where every actor is directly tied to every other actor.");
        report.addFootnote("Cliques are enumerated using the Bron-Kerbosch backtracking algorithm.");
        return report;
    }

    public static AnalysisReport createShortestPathsReport(Network src, int from, int to, AgnaLib lib, String legacy) {
        String fromName = (from >= 0 && from < src.getSize()) ? src.getActor(from).getName() : String.valueOf(from);
        String toName = (to >= 0 && to < src.getSize()) ? src.getActor(to).getName() : String.valueOf(to);

        AnalysisReport report = new AnalysisReport("Shortest Paths (" + fromName + " -> " + toName + ")", src.getName());
        report.setRawPlainText(legacy);

        TableData.Builder b = TableData.builder("Paths from " + fromName + " to " + toName)
                .network(src.getName())
                .columns("Path #", "Path Route")
                .isNumeric(true, false);

        if (legacy != null && legacy.contains("No path found")) {
            b.addRow(1, "No path found connecting " + fromName + " to " + toName);
            report.setNote("No path found connecting these nodes.", true);
        } else {
            b.addRow(1, legacy != null ? legacy.trim().replace("\n", " -> ") : "Path");
        }

        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);
        report.addFootnote("Displays the reconstructed topological shortest path sequence connecting source and target nodes.");
        return report;
    }

    public static AnalysisReport createOpenChainReport(Network src, AgnaLib lib, String legacy) {
        AnalysisReport report = new AnalysisReport("Open Chain Summary", src.getName());
        report.setRawPlainText(legacy);
        report.addFootnote("Open chains identify unclosed triads (two-step paths without a direct bridging tie).");
        return report;
    }

    public static AnalysisReport createFullAnalysisReport(Network src, AgnaLib lib, String legacy) {
        return createFullAnalysisReport(src, lib, FullAnalysisOptions.createStandard(), legacy);
    }

    public static AnalysisReport createCentralityBatteryReport(Network src, AgnaLib lib, CentralityOptions options) {
        return createCentralityBatteryReport(src, lib, options, "");
    }

    public static AnalysisReport createCentralityBatteryReport(Network src, AgnaLib lib, CentralityOptions options, String legacy) {
        if (options == null) options = CentralityOptions.createStandard();
        AnalysisReport report = new AnalysisReport("Centrality & Prestige Battery", src.getName());
        report.setRawPlainText(legacy);
        boolean connected = lib.isConnected(src);

        if (options.isIncludeNodalDegree()) {
            for (TableData td : createNodalDegreeReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeInDegree()) {
            for (TableData td : createInDegreeReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeOutDegree()) {
            for (TableData td : createOutDegreeReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeEmission()) {
            for (TableData td : createEmissionReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeReception()) {
            for (TableData td : createReceptionReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeDetermination()) {
            for (TableData td : createDeterminationReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludePrestige()) {
            for (TableData td : createPrestigeReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeSociostatus()) {
            for (TableData td : createSociometricStatusReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeBetweenness()) {
            for (TableData td : createBetweennessReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeCloseness()) {
            for (TableData td : createClosenessReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeBavelas()) {
            for (TableData td : createBavelasReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeFareness()) {
            for (TableData td : createFarenessReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (!lib.isStronglyConnected(src) && (options.isIncludeCloseness() || options.isIncludeBavelas() || options.isIncludeFareness())) {
            report.setNote("Note: Network is not strongly connected. Distance-based centrality measures reflect reachable paths only.", true);
        }
        if (options.isIncludeNodalDegree() || options.isIncludeBetweenness() || options.isIncludeCloseness()) {
            TableData freemanTable = createFreemanCentralizationSummaryTable(src, lib);
            if (freemanTable != null) report.addTable(freemanTable);
        }
        if (options.isIncludeEigenvector()) {
            for (TableData td : createEigenvectorReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeBonacich()) {
            for (TableData td : createBonacichReport(src, options.getBonacichBeta(), lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludePageRank()) {
            for (TableData td : createPageRankReport(src, options.getPageRankDamping(), lib, "").getTables()) report.addTable(td);
        }

        report.addFootnote("Centrality battery compiles positional, structural, and spectral node prominence metrics.");
        report.addFootnote("Self-loops are excluded from all centrality calculations.");
        return report;
    }

    public static AnalysisReport createDistanceBatteryReport(Network src, AgnaLib lib, DistanceOptions options) {
        return createDistanceBatteryReport(src, lib, options, "");
    }

    public static AnalysisReport createDistanceBatteryReport(Network src, AgnaLib lib, DistanceOptions options, String legacy) {
        if (options == null) options = DistanceOptions.createStandard();
        AnalysisReport report = new AnalysisReport("Distances & Geodesics Battery", src.getName());
        report.setRawPlainText(legacy);
        boolean connected = lib.isConnected(src);

        if (options.isIncludeGeodesicsMatrix()) {
            for (TableData td : createGeodesicsReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeDiameter()) {
            for (TableData td : createDiameterReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeEccentricity()) {
            for (TableData td : createEccentricityReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeSpecificShortestPath()) {
            for (TableData td : createShortestPathsReport(src, options.getFromNode(), options.getToNode(), lib, "").getTables()) report.addTable(td);
        }
        if (!lib.isStronglyConnected(src)) {
            report.setNote("Note: Network is not strongly connected. Distance and geodesic measures reflect reachable paths only.", true);
        }

        report.addFootnote("Geodesic distances are computed as the shortest path between node pairs.");
        return report;
    }

    public static AnalysisReport createClusteringBatteryReport(Network src, AgnaLib lib, ClusteringOptions options) {
        return createClusteringBatteryReport(src, lib, options, "");
    }

    public static AnalysisReport createClusteringBatteryReport(Network src, AgnaLib lib, ClusteringOptions options, String legacy) {
        if (options == null) options = ClusteringOptions.createStandard();
        AnalysisReport report = new AnalysisReport("Clustering & Cohesion Battery", src.getName());
        report.setRawPlainText(legacy);

        if (options.isIncludeLocalClustering()) {
            for (TableData td : createLocalClusteringReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeTransitivity()) {
            for (TableData td : createTransitivityReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeKCore()) {
            for (TableData td : createKCoreReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeCohesion()) {
            for (TableData td : createCohesionReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeOpenChains()) {
            for (TableData td : createOpenChainReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeCliques()) {
            for (TableData td : createCliquesReport(src, options.getCliqueDiameter(), lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeCommunities()) {
            for (TableData td : createCommunityDetectionReport(src, options.isUseGirvanNewman(), lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeStructuralEquivalence()) {
            for (TableData td : createStructuralEquivalenceReport(src, options.getTargetBlocks(), lib, "").getTables()) report.addTable(td);
        }

        report.addFootnote("Clustering and cohesion metrics measure triad closure, core density, and subgraph cohesion.");
        return report;
    }

    public static AnalysisReport createSociometricsBatteryReport(Network src, AgnaLib lib, SociometricsOptions options) {
        return createSociometricsBatteryReport(src, lib, options, "");
    }

    public static AnalysisReport createSociometricsBatteryReport(Network src, AgnaLib lib, SociometricsOptions options, String legacy) {
        if (options == null) options = SociometricsOptions.createStandard();
        AnalysisReport report = new AnalysisReport("Sociometrics & Basic Battery", src.getName());
        report.setRawPlainText(legacy);

        if (options.isIncludeBasic()) {
            for (TableData td : createBasicReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeDensity()) {
            for (TableData td : createDensityReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeNodalDegree()) {
            for (TableData td : createNodalDegreeReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeInDegree()) {
            for (TableData td : createInDegreeReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeOutDegree()) {
            for (TableData td : createOutDegreeReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeEmission()) {
            for (TableData td : createEmissionReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeReception()) {
            for (TableData td : createReceptionReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeDetermination()) {
            for (TableData td : createDeterminationReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeSociostatus()) {
            for (TableData td : createSociometricStatusReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeDyadCensus()) {
            for (TableData td : createTriadCensusReport(src, lib, "").getTables()) {
                if (td.getTitle().contains("Dyad Census")) {
                    report.addTable(td);
                }
            }
        }
        if (options.isIncludeAssortativity()) {
            for (TableData td : createDegreeAssortativityReport(src, lib, "").getTables()) report.addTable(td);
        }
        if (options.isIncludeWeightedMetrics()) {
            for (TableData td : createWeightedMetricsReport(src, lib, "").getTables()) report.addTable(td);
        }

        report.addFootnote("Sociometrics battery details network density, tie volume, and degree distribution profiles.");
        return report;
    }

    public static AnalysisReport createFullAnalysisReport(Network src, AgnaLib lib, FullAnalysisOptions options, String legacy) {
        if (options == null) {
            options = FullAnalysisOptions.createStandard();
        }
        AnalysisReport report = new AnalysisReport("FULL ANALYSIS Battery", src.getName());
        report.setRawPlainText(legacy);
        report.setExecutiveSummary(NarrativeSummaryGenerator.generateSummary(src, lib));
        boolean connected = lib.isConnected(src);

        // Build composite report based on selected options
        if (options.isIncludeBasicAndDensity()) {
            AnalysisReport basic = createBasicReport(src, lib, "");
            for (TableData td : basic.getTables()) report.addTable(td);

            AnalysisReport dens = createDensityReport(src, lib, "");
            for (TableData td : dens.getTables()) report.addTable(td);
        }

        if (options.isIncludeDegreeMetrics()) {
            AnalysisReport nodal = createNodalDegreeReport(src, lib, "");
            for (TableData td : nodal.getTables()) report.addTable(td);

            AnalysisReport inDeg = createInDegreeReport(src, lib, "");
            for (TableData td : inDeg.getTables()) report.addTable(td);

            AnalysisReport outDeg = createOutDegreeReport(src, lib, "");
            for (TableData td : outDeg.getTables()) report.addTable(td);
        }

        if (options.isIncludePrestige()) {
            AnalysisReport prest = createPrestigeReport(src, lib, "");
            for (TableData td : prest.getTables()) report.addTable(td);
        }

        if (options.isIncludeGeodesicCentralities()) {
            AnalysisReport betw = createBetweennessReport(src, lib, "");
            for (TableData td : betw.getTables()) report.addTable(td);

            AnalysisReport close = createClosenessReport(src, lib, "");
            for (TableData td : close.getTables()) report.addTable(td);

            AnalysisReport geo = createGeodesicsReport(src, lib, "");
            for (TableData td : geo.getTables()) report.addTable(td);

            if (!lib.isStronglyConnected(src)) {
                report.setNote("Note: Network is not strongly connected. Geodesic and distance-based measures reflect reachable paths only.", true);
            }
        }

        if (options.isIncludeDegreeMetrics() || options.isIncludeGeodesicCentralities()) {
            TableData freemanSummary = createFreemanCentralizationSummaryTable(src, lib);
            if (freemanSummary != null) report.addTable(freemanSummary);
        }

        if (options.isIncludeSpectralCentrality()) {
            AnalysisReport eigen = createEigenvectorReport(src, lib, "");
            for (TableData td : eigen.getTables()) report.addTable(td);
        }

        if (options.isIncludeClusteringAndCores()) {
            AnalysisReport local = createLocalClusteringReport(src, lib, "");
            for (TableData td : local.getTables()) report.addTable(td);

            AnalysisReport trans = createTransitivityReport(src, lib, "");
            for (TableData td : trans.getTables()) report.addTable(td);

            AnalysisReport kcore = createKCoreReport(src, lib, "");
            for (TableData td : kcore.getTables()) report.addTable(td);
        }

        if (options.isIncludeStructuralHoles()) {
            AnalysisReport holes = createStructuralHolesReport(src, lib, "");
            for (TableData td : holes.getTables()) report.addTable(td);
        }

        if (options.isIncludeConnectivityAndDyad()) {
            AnalysisReport conn = createConnectivityReport(src, lib, "");
            for (TableData td : conn.getTables()) report.addTable(td);
        }

        if (options.isIncludeBonacich()) {
            AnalysisReport bonacich = createBonacichReport(src, options.getBonacichBeta(), lib, "");
            for (TableData td : bonacich.getTables()) report.addTable(td);
        }

        if (options.isIncludePageRank()) {
            AnalysisReport pagerank = createPageRankReport(src, options.getPageRankDamping(), lib, "");
            for (TableData td : pagerank.getTables()) report.addTable(td);
        }

        if (options.isIncludeTriadCensus()) {
            AnalysisReport triad = createTriadCensusReport(src, lib, "");
            for (TableData td : triad.getTables()) report.addTable(td);
        }

        if (options.isIncludeCliques()) {
            AnalysisReport cliques = createCliquesReport(src, options.getCliqueDiameter(), lib, "");
            for (TableData td : cliques.getTables()) report.addTable(td);
        }

        if (options.isIncludeAssortativity()) {
            AnalysisReport assort = createDegreeAssortativityReport(src, lib, "");
            for (TableData td : assort.getTables()) report.addTable(td);
        }

        if (options.isIncludeCommunityDetection()) {
            AnalysisReport comm = createCommunityDetectionReport(src, options.isUseGirvanNewman(), lib, "");
            for (TableData td : comm.getTables()) report.addTable(td);
        }

        if (options.isIncludeStructuralEquivalence()) {
            AnalysisReport equiv = createStructuralEquivalenceReport(src, options.getTargetBlocks(), lib, "");
            for (TableData td : equiv.getTables()) report.addTable(td);
        }

        if (options.isIncludeWeightedMetrics()) {
            AnalysisReport weighted = createWeightedMetricsReport(src, lib, "");
            for (TableData td : weighted.getTables()) report.addTable(td);
        }

        report.addFootnote("Full analysis battery compiles selected centrality, cohesion, structural capital, and connectivity metrics.");
        report.addFootnote("Matrix diagonal values (self-loops) are excluded across all actor-level metrics.");

        return report;
    }

    public static AnalysisReport createLocalClusteringReport(Network src, AgnaLib lib, String legacy) {
        ClusteringMetrics.LocalClusteringSummary sum = ClusteringMetrics.computeLocalClustering(src);
        AnalysisReport report = new AnalysisReport("Watts-Strogatz Local Clustering", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Mean Local Clustering <C>", String.format(Locale.ROOT, "%.4f", sum.meanClustering));
        report.addProperty("Total Unique Triangles", String.valueOf(sum.totalTriangles));
        report.addProperty("Actors with Degree < 2", String.valueOf(sum.isolatedOrSingleDegreeCount));

        TableData.Builder b = TableData.builder("Distribution of Local Clustering in " + src.getName())
                .network(src.getName())
                .columns("Node", "Degree (k)", "Triangles (T_i)", "Max Triangles", "Clustering (C_i)")
                .isNumeric(false, true, true, true, true);

        float[] cVals = new float[sum.actorResults.length];
        for (int i = 0; i < sum.actorResults.length; i++) {
            ClusteringMetrics.LocalClusteringResult r = sum.actorResults[i];
            cVals[i] = (float) r.clusteringCoefficient;
            b.addRow(r.actorName, r.degree, r.triangles, r.maxTriangles, String.format(Locale.ROOT, "%.4f", r.clusteringCoefficient));
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        float[] statVals = lib.statistics(cVals);
        TableData st = createStatisticsTable("Local Clustering Statistics", src.getName(), statVals);
        if (st != null) report.addTable(st);

        report.addFootnote("Watts-Strogatz local clustering coefficient C_i = 2 * T_i / (k_i * (k_i - 1)) measures neighbor cliquishness.");
        report.addFootnote("Actors with degree k_i < 2 cannot form triangles and are assigned C_i = 0 by standard convention.");
        report.addFootnote("Reference: Watts, D. J., & Strogatz, S. H. (1998). Collective dynamics of 'small-world' networks. Nature, 393(6684), 440-442.");
        return report;
    }

    public static AnalysisReport createTransitivityReport(Network src, AgnaLib lib, String legacy) {
        ClusteringMetrics.TransitivityResult res = ClusteringMetrics.computeTransitivity(src);
        AnalysisReport report = new AnalysisReport("Global Network Transitivity", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Transitivity Ratio T", String.format(Locale.ROOT, "%.4f", res.transitivity));
        report.addProperty("Closed Triplets (3 * Triangles)", String.valueOf(res.closedTriplets));
        report.addProperty("Connected Triplets (Paths of length 2)", String.valueOf(res.connectedTriplets));
        report.addProperty("Total Triangles", String.valueOf(res.totalTriangles));

        TableData.Builder b = TableData.builder("Transitivity Indices of " + src.getName())
                .network(src.getName())
                .columns("Triad Metric", "Value", "Theoretical Definition")
                .isNumeric(false, true, false);

        b.addRow("Global Transitivity T", String.format(Locale.ROOT, "%.4f", res.transitivity), "Ratio: 3 * Triangles / Connected Triplets");
        b.addRow("Closed Triplets", String.valueOf(res.closedTriplets), "Triangles counted from each of 3 vertices (3 * T)");
        b.addRow("Connected Triplets", String.valueOf(res.connectedTriplets), "All 2-star paths sum_i k_i(k_i - 1)/2");
        b.addRow("Unique Triangles", String.valueOf(res.totalTriangles), "Total distinct 3-cliques in network");

        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        report.addFootnote("Global transitivity measures the fraction of two-step paths that are closed into triads across the whole graph.");
        report.addFootnote("Reference: Newman, M. E. (2003). The structure and function of complex networks. SIAM Review, 45(2), 167-256.");
        return report;
    }

    public static AnalysisReport createKCoreReport(Network src, AgnaLib lib, String legacy) {
        KCoreDecomposition.KCoreResult res = KCoreDecomposition.decompose(src);
        AnalysisReport report = new AnalysisReport("k-Core Decomposition", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Max Coreness (k_max)", String.valueOf(res.maxCore));
        report.addProperty("Actors in Maximum Core", String.valueOf(res.coreMembers.getOrDefault(res.maxCore, List.of()).size()));

        TableData.Builder distBuilder = TableData.builder("Core Shell Distribution of " + src.getName())
                .network(src.getName())
                .columns("Core Level (k)", "Actor Count", "Proportion (%)", "Member Nodes Preview")
                .isNumeric(true, true, true, false);

        int totalN = src.getSize();
        for (int k = 0; k <= res.maxCore; k++) {
            int count = res.coreDistribution.getOrDefault(k, 0);
            double pct = totalN > 0 ? (100.0 * count / totalN) : 0.0;
            List<String> mems = res.coreMembers.getOrDefault(k, List.of());
            String preview = mems.size() > 5 ? (String.join(", ", mems.subList(0, 5)) + " ... (" + mems.size() + " total)") : String.join(", ", mems);
            distBuilder.addRow(k, count, String.format(Locale.ROOT, "%.1f%%", pct), preview);
        }
        TableData distTable = distBuilder.build();
        TableRegistry.register(distTable);
        report.addTable(distTable);

        TableData.Builder nodeBuilder = TableData.builder("Actor Coreness Values in " + src.getName())
                .network(src.getName())
                .columns("Node", "Coreness (k_i)")
                .isNumeric(false, true);

        for (int i = 0; i < totalN; i++) {
            nodeBuilder.addRow(src.getActor(i).getName(), res.coreness[i]);
        }
        TableData nodeTable = nodeBuilder.build();
        TableRegistry.register(nodeTable);
        report.addTable(nodeTable);

        report.addFootnote("A k-core is a maximal connected subgraph in which all vertices have degree at least k within the subgraph.");
        report.addFootnote("Computed in linear O(V + E) time via the Batagelj-Zaveršnik bucket sorting peeling algorithm.");
        report.addFootnote("Reference: Seidman, S. B. (1983). Network structure and minimum degree. Social Networks, 5(3), 269-287.");
        return report;
    }

    public static AnalysisReport createTriadCensusReport(Network src, AgnaLib lib, String legacy) {
        TriadCensus census = TriadCensus.compute(src);
        AnalysisReport report = new AnalysisReport("Triad & Dyad Census (Holland-Leinhardt)", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Total Triads C(N, 3)", String.valueOf(census.totalTriads));
        report.addProperty("Triad Transitivity Ratio", String.format(Locale.ROOT, "%.4f", census.transitivityRatio));
        report.addProperty("Dyadic Reciprocity Rate", String.format(Locale.ROOT, "%.4f", census.dyadCensus.reciprocityRate));

        TableData.Builder dyadB = TableData.builder("Dyad Census (M-A-N) of " + src.getName())
                .network(src.getName())
                .columns("Dyad State", "Notation", "Count", "Proportion (%)")
                .isNumeric(false, false, true, true);

        long totDyads = census.dyadCensus.totalDyads;
        double pM = totDyads > 0 ? (100.0 * census.dyadCensus.mutual / totDyads) : 0;
        double pA = totDyads > 0 ? (100.0 * census.dyadCensus.asymmetric / totDyads) : 0;
        double pN = totDyads > 0 ? (100.0 * census.dyadCensus.nullDyads / totDyads) : 0;

        dyadB.addRow("Mutual", "M (A <-> B)", census.dyadCensus.mutual, String.format(Locale.ROOT, "%.2f%%", pM));
        dyadB.addRow("Asymmetric", "A (A -> B)", census.dyadCensus.asymmetric, String.format(Locale.ROOT, "%.2f%%", pA));
        dyadB.addRow("Null", "N (A   B)", census.dyadCensus.nullDyads, String.format(Locale.ROOT, "%.2f%%", pN));

        TableData dyadTable = dyadB.build();
        TableRegistry.register(dyadTable);
        report.addTable(dyadTable);

        TableData.Builder triadB = TableData.builder("Holland & Leinhardt 16 Triad Census of " + src.getName())
                .network(src.getName())
                .columns("Type Code", "Classification Name", "Count", "Proportion (%)", "Configuration Description")
                .isNumeric(false, false, true, true, false);

        for (TriadCensus.TriadType t : TriadCensus.TriadType.values()) {
            long count = census.getCount(t);
            double pct = census.totalTriads > 0 ? (100.0 * count / census.totalTriads) : 0;
            triadB.addRow(t.getCode(), t.getName(), count, String.format(Locale.ROOT, "%.3f%%", pct), t.getDescription());
        }
        TableData triadTable = triadB.build();
        TableRegistry.register(triadTable);
        report.addTable(triadTable);

        report.addFootnote("Holland-Leinhardt triad census enumerates all 16 mutually exclusive directed triad configurations.");
        report.addFootnote("Type codes indicate counts of Mutual, Asymmetric, and Null dyads (plus D=Down, U=Up, C=Cycle, T=Transitive).");
        report.addFootnote("References: Davis & Leinhardt (1972); Holland & Leinhardt (1976).");
        return report;
    }

    public static AnalysisReport createStructuralHolesReport(Network src, AgnaLib lib, String legacy) {
        StructuralHoles holes = StructuralHoles.compute(src);
        AnalysisReport report = new AnalysisReport("Social Capital & Structural Holes (Burt)", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Mean Constraint <C>", String.format(Locale.ROOT, "%.4f", holes.meanConstraint));
        report.addProperty("Mean Effective Size <S>", String.format(Locale.ROOT, "%.4f", holes.meanEffectiveSize));
        report.addProperty("Mean Efficiency <E>", String.format(Locale.ROOT, "%.4f", holes.meanEfficiency));

        TableData.Builder b = TableData.builder("Structural Holes Metrics in " + src.getName())
                .network(src.getName())
                .columns("Node", "Degree (k)", "Effective Size", "Efficiency", "Constraint (C_i)", "Hierarchy (H_i)")
                .isNumeric(false, true, true, true, true, true);

        for (StructuralHoles.ActorHolesMetrics m : holes.actorMetrics) {
            b.addRow(m.actorName, m.degree,
                    String.format(Locale.ROOT, "%.3f", m.effectiveSize),
                    String.format(Locale.ROOT, "%.3f", m.efficiency),
                    String.format(Locale.ROOT, "%.4f", m.constraint),
                    String.format(Locale.ROOT, "%.4f", m.hierarchy));
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        report.addFootnote("Low constraint indicates high structural autonomy and entrepreneurial social capital (brokerage).");
        report.addFootnote("Effective size reflects the number of non-redundant contacts in the actor's ego network.");
        report.addFootnote("Reference: Burt, R. S. (1992). Structural Holes: The Social Structure of Competition. Harvard University Press.");
        return report;
    }

    public static AnalysisReport createConnectivityReport(Network src, AgnaLib lib, String legacy) {
        ConnectivityAnalysis conn = ConnectivityAnalysis.analyze(src);
        AnalysisReport report = new AnalysisReport("Connectivity & Structural Vulnerability", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Weak Components", String.valueOf(conn.weakComponents.size()));
        report.addProperty("Strong Components", String.valueOf(conn.strongComponents.size()));
        report.addProperty("Giant Component Size", conn.giantComponentSize + " nodes (" + String.format(Locale.ROOT, "%.1f%%", conn.giantComponentFraction * 100) + ")");
        report.addProperty("Cut-Vertices (Articulation Points)", String.valueOf(conn.articulationPoints.size()));
        report.addProperty("Bridges (Cut-Edges)", String.valueOf(conn.bridges.size()));

        TableData.Builder compB = TableData.builder("Connected Components of " + src.getName())
                .network(src.getName())
                .columns("Component ID", "Size", "Share (%)", "Members Preview")
                .isNumeric(true, true, true, false);

        int n = src.getSize();
        for (int i = 0; i < conn.weakComponents.size(); i++) {
            List<Integer> comp = conn.weakComponents.get(i);
            double pct = n > 0 ? (100.0 * comp.size() / n) : 0;
            List<String> names = new ArrayList<>();
            for (int idx : comp) names.add(src.getActor(idx).getName());
            String prev = names.size() > 6 ? (String.join(", ", names.subList(0, 6)) + " ...") : String.join(", ", names);
            compB.addRow(i + 1, comp.size(), String.format(Locale.ROOT, "%.1f%%", pct), prev);
        }
        TableData compTable = compB.build();
        TableRegistry.register(compTable);
        report.addTable(compTable);

        TableData.Builder vulnB = TableData.builder("Structural Vulnerability (Cut-Points & Bridges)")
                .network(src.getName())
                .columns("Topology Element", "Node / Tie Identifier", "Vulnerability Impact")
                .isNumeric(false, false, false);

        for (int art : conn.articulationPoints) {
            vulnB.addRow("Cut-Vertex (Articulation Point)", src.getActor(art).getName(), "Removal fragments the network into multiple components");
        }
        for (ConnectivityAnalysis.Bridge br : conn.bridges) {
            vulnB.addRow("Bridge (Cut-Edge)", br.uName + " <-> " + br.vName, "Removal disconnects graph components");
        }
        if (conn.articulationPoints.isEmpty() && conn.bridges.isEmpty()) {
            vulnB.addRow("Biconnected Robustness", "None detected", "Graph has 2-vertex connectivity (no single points of failure)");
        }
        TableData vulnTable = vulnB.build();
        TableRegistry.register(vulnTable);
        report.addTable(vulnTable);

        report.addFootnote("Articulation points and bridges are single structural points of failure whose deletion fragments the network.");
        report.addFootnote("References: Tarjan, R. E. (1972); Hopcroft, J., & Tarjan, R. (1973).");
        return report;
    }

    public static AnalysisReport createEigenvectorReport(Network src, AgnaLib lib, String legacy) {
        double[] ev = SpectralCentrality.computeEigenvector(src, 150, 1e-6);
        AnalysisReport report = new AnalysisReport("Eigenvector Centrality", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Eigenvector Method", "Shifted Power Iteration (A + I)");
        report.addProperty("Convergence Tolerance", "1e-6");
        report.addProperty("Normalization", "Max score = 1.0");

        TableData.Builder b = TableData.builder("Eigenvector Centrality in " + src.getName())
                .network(src.getName())
                .columns("Node", "Eigenvector Centrality")
                .isNumeric(false, true);

        float[] statVals = new float[src.getSize()];
        for (int i = 0; i < src.getSize(); i++) {
            statVals[i] = (float) ev[i];
            b.addRow(src.getActor(i).getName(), String.format(Locale.ROOT, "%.4f", ev[i]));
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        TableData st = createStatisticsTable("Eigenvector Centrality Statistics", src.getName(), lib.statistics(statVals));
        if (st != null) report.addTable(st);

        report.addFootnote("Eigenvector centrality assigns relative scores to all nodes based on the principle that connections to high-scoring nodes contribute more to the score of the node.");
        report.addFootnote("Reference: Bonacich, P. (1972). Factoring and weighting approaches to status scores. Journal of Mathematical Sociology, 2(1), 113-120.");
        return report;
    }

    public static AnalysisReport createBonacichReport(Network src, double beta, AgnaLib lib, String legacy) {
        double[] bon = SpectralCentrality.computeBonacich(src, beta, 100);
        AnalysisReport report = new AnalysisReport("Bonacich Power Centrality", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Attenuation Parameter (Beta)", String.format(Locale.ROOT, "%.3f", beta));
        report.addProperty("Power Model", beta >= 0 ? "Influence / Prestige (alters enhance power)" : "Exchange Theory / Bargaining (dependent alters enhance power)");

        TableData.Builder b = TableData.builder("Bonacich Power Centrality (beta=" + beta + ") in " + src.getName())
                .network(src.getName())
                .columns("Node", "Bonacich Power Centrality")
                .isNumeric(false, true);

        float[] statVals = new float[src.getSize()];
        for (int i = 0; i < src.getSize(); i++) {
            statVals[i] = (float) bon[i];
            b.addRow(src.getActor(i).getName(), String.format(Locale.ROOT, "%.4f", bon[i]));
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        TableData st = createStatisticsTable("Bonacich Power Statistics", src.getName(), lib.statistics(statVals));
        if (st != null) report.addTable(st);

        report.addFootnote("Bonacich power centrality generalizes degree centrality by weighting neighbors' centralities with parameter beta.");
        report.addFootnote("Reference: Bonacich, P. (1987). Power and centrality: A family of measures. American Journal of Sociology, 92(5), 1170-1182.");
        return report;
    }

    public static AnalysisReport createPageRankReport(Network src, double damping, AgnaLib lib, String legacy) {
        double[] pr = SpectralCentrality.computePageRank(src, damping, 100);
        AnalysisReport report = new AnalysisReport("PageRank Centrality", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Damping Factor (d)", String.format(Locale.ROOT, "%.2f", damping));
        report.addProperty("Teleport Probability (1 - d)", String.format(Locale.ROOT, "%.2f", 1.0 - damping));

        TableData.Builder b = TableData.builder("PageRank Centrality in " + src.getName())
                .network(src.getName())
                .columns("Node", "PageRank Centrality")
                .isNumeric(false, true);

        float[] statVals = new float[src.getSize()];
        for (int i = 0; i < src.getSize(); i++) {
            statVals[i] = (float) pr[i];
            b.addRow(src.getActor(i).getName(), String.format(Locale.ROOT, "%.6f", pr[i]));
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        TableData st = createStatisticsTable("PageRank Statistics", src.getName(), lib.statistics(statVals));
        if (st != null) report.addTable(st);

        report.addFootnote("PageRank measures the probability of finding a random walk at each node with random teleportation probability (1 - d).");
        report.addFootnote("Reference: Page, L., Brin, S., Motwani, R., & Winograd, T. (1999). The PageRank citation ranking. Stanford InfoLab.");
        return report;
    }

    public static TableData createFreemanCentralizationSummaryTable(Network src, AgnaLib lib) {
        int size = src.getSize();
        if (size <= 2) return null;
        boolean connected = lib.isConnected(src);

        float[] deg = lib.nodalDegree(src);
        float degCent = (deg != null) ? lib.freemanDegreeCentralization(src) : 0f;

        float[] betw = lib.betweenness(src);
        float betwCent = (betw != null) ? lib.freemanGeneralIndex(betw, lib.betweenness(new Network(size, 1))) : 0f;

        float closeCent = 0f;
        float bavCent = 0f;
        if (connected) {
            float[] close = lib.closeness(src);
            if (close != null) closeCent = lib.freemanGeneralIndex(close, lib.closeness(new Network(size, 1)));
            float[] bav = lib.bavelas(src);
            if (bav != null) bavCent = lib.freemanGeneralIndex(bav, lib.bavelas(new Network(size, 1)));
        }

        TableData.Builder b = TableData.builder("Freeman Network Centralization Indices of " + src.getName())
                .network(src.getName())
                .subtitle("Graph-level centralization measures compared to the maximally centralized star graph of size N=" + size)
                .columns("Centrality Measure", "Network Centralization Index", "Theoretical Range", "Benchmark Graph")
                .isNumeric(false, true, false, false);

        b.addRow("Freeman Degree Centralization (C_D)", String.format(Locale.ROOT, "%.4f", degCent), "[0, 1]", "Star Network (N=" + size + ")");
        b.addRow("Freeman Betweenness Centralization (C_B)", String.format(Locale.ROOT, "%.4f", betwCent), "[0, 1]", "Star Network (N=" + size + ")");
        if (connected) {
            b.addRow("Freeman Closeness Centralization (C_C)", String.format(Locale.ROOT, "%.4f", closeCent), "[0, 1]", "Star Network (N=" + size + ")");
            b.addRow("Bavelas-Leavitt Centralization (C_Bav)", String.format(Locale.ROOT, "%.4f", bavCent), "[0, 1]", "Star Network (N=" + size + ")");
        } else {
            b.addRow("Freeman Closeness Centralization (C_C)", "N/A (Disconnected)", "[0, 1]", "Requires single component");
            b.addRow("Bavelas-Leavitt Centralization (C_Bav)", "N/A (Disconnected)", "[0, 1]", "Requires single component");
        }

        TableData dt = b.build();
        TableRegistry.register(dt);
        return dt;
    }

    public static AnalysisReport createDegreeAssortativityReport(Network src, AgnaLib lib, String legacy) {
        DegreeAssortativity.AssortativityResult res = DegreeAssortativity.compute(src);
        AnalysisReport report = new AnalysisReport("Degree Assortativity & Mixing Patterns", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Total Degree Assortativity (r)", Double.isNaN(res.totalDegreeAssortativity) ? "N/A" : String.format(Locale.ROOT, "%.4f", res.totalDegreeAssortativity));
        report.addProperty("Directed Out-In Assortativity (r_out_in)", Double.isNaN(res.outInAssortativity) ? "N/A" : String.format(Locale.ROOT, "%.4f", res.outInAssortativity));
        report.addProperty("Mixing Regime", res.mixingRegime);
        report.addProperty("Evaluated Edges (M)", String.valueOf(res.edgeCount));

        TableData.Builder b = TableData.builder("Degree Assortativity Coefficients of " + src.getName())
                .network(src.getName())
                .columns("Degree Mixing Type", "Correlation (r)", "Theoretical Range", "Structural Interpretation")
                .isNumeric(false, true, false, false);

        b.addRow("Total Degree Assortativity (r)", Double.isNaN(res.totalDegreeAssortativity) ? "N/A" : String.format(Locale.ROOT, "%.4f", res.totalDegreeAssortativity), "[-1, 1]", res.mixingRegime);
        b.addRow("Out-Degree to In-Degree (r_out_in)", Double.isNaN(res.outInAssortativity) ? "N/A" : String.format(Locale.ROOT, "%.4f", res.outInAssortativity), "[-1, 1]", "Source emission vs. target reception correlation");
        b.addRow("In-Degree to In-Degree (r_in_in)", Double.isNaN(res.inInAssortativity) ? "N/A" : String.format(Locale.ROOT, "%.4f", res.inInAssortativity), "[-1, 1]", "Popularity-popularity mixing");
        b.addRow("Out-Degree to Out-Degree (r_out_out)", Double.isNaN(res.outOutAssortativity) ? "N/A" : String.format(Locale.ROOT, "%.4f", res.outOutAssortativity), "[-1, 1]", "Activity-activity mixing");
        b.addRow("In-Degree to Out-Degree (r_in_out)", Double.isNaN(res.inOutAssortativity) ? "N/A" : String.format(Locale.ROOT, "%.4f", res.inOutAssortativity), "[-1, 1]", "Popularity to activity mixing");

        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        report.addFootnote("Degree assortativity measures Pearson correlation of degrees between connected node pairs across all edges.");
        report.addFootnote("Positive assortativity indicates homophilic mixing (typical of social networks); negative assortativity indicates disassortative hub-spoke hierarchy.");
        report.addFootnote("References: Newman, M. E. J. (2002). Assortative mixing in networks. Phys. Rev. Lett., 89(20), 208701; Foster et al. (2010). PNAS, 107(24), 10815-10820.");
        return report;
    }

    public static AnalysisReport createCommunityDetectionReport(Network src, boolean useGirvanNewman, AgnaLib lib, String legacy) {
        CommunityDetectionService.CommunityResult res = useGirvanNewman ? CommunityDetectionService.detectGirvanNewman(src) : CommunityDetectionService.detectLouvain(src);
        AnalysisReport report = new AnalysisReport("Community Detection & Modularity Partitioning", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Algorithm", res.algorithm);
        report.addProperty("Newman Modularity (Q)", String.format(Locale.ROOT, "%.4f", res.modularityQ));
        report.addProperty("Number of Communities (K)", String.valueOf(res.communityCount));
        report.addProperty("Modularity Quality", res.modularityQ > 0.3 ? "Strong Community Structure (Q > 0.3)" : "Weak / Diffuse Community Structure (Q <= 0.3)");

        TableData.Builder sumB = TableData.builder("Community Subgraph Summary in " + src.getName())
                .network(src.getName())
                .columns("Community ID", "Node Count", "Internal Density", "Members Preview")
                .isNumeric(true, true, true, false);

        for (CommunityDetectionService.CommunityInfo info : res.communities) {
            String prev = info.memberNames.size() > 6 ? String.join(", ", info.memberNames.subList(0, 6)) + " ..." : String.join(", ", info.memberNames);
            sumB.addRow(info.communityId, info.memberCount, String.format(Locale.ROOT, "%.4f", info.internalDensity), prev);
        }
        TableData sumTable = sumB.build();
        TableRegistry.register(sumTable);
        report.addTable(sumTable);

        TableData.Builder nodeB = TableData.builder("Actor Community Affiliation in " + src.getName())
                .network(src.getName())
                .columns("Node", "Community ID")
                .isNumeric(false, true);

        for (int i = 0; i < src.getSize(); i++) {
            nodeB.addRow(src.getActor(i).getName(), res.nodeAssignments[i] + 1);
        }
        TableData nodeTable = nodeB.build();
        TableRegistry.register(nodeTable);
        report.addTable(nodeTable);

        report.addFootnote("Modularity Q measures the fraction of ties falling within communities minus the expected fraction in a degree-equivalent null model.");
        report.addFootnote("Values above 0.3 typically indicate significant modular community organization (Newman & Girvan, 2004).");
        report.addFootnote("References: Blondel et al. (2008). J. Stat. Mech., P10008; Girvan & Newman (2002). PNAS, 99(12), 7821-7826.");
        return report;
    }

    public static AnalysisReport createStructuralEquivalenceReport(Network src, int targetBlocks, AgnaLib lib, String legacy) {
        StructuralEquivalenceService.EquivalenceResult res = StructuralEquivalenceService.analyze(src, targetBlocks);
        AnalysisReport report = new AnalysisReport("Structural Equivalence & Blockmodeling", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Target Positions / Blocks (K)", String.valueOf(res.blockCount));
        report.addProperty("Clustering Method", "Average-Linkage Agglomerative Clustering on Euclidean Distance");

        TableData.Builder imageB = TableData.builder("Blockmodel Image Matrix (Inter-Block Tie Density)")
                .network(src.getName())
                .subtitle("Density of directed ties from Block P (row) to Block Q (column)");
        List<String> cols = new ArrayList<>();
        cols.add("Position / Block");
        boolean[] isNum = new boolean[res.blockCount + 1];
        for (int b = 0; b < res.blockCount; b++) {
            cols.add("Block " + (b + 1));
            isNum[b + 1] = true;
        }
        imageB.columns(cols.toArray(new String[0])).isNumeric(isNum);

        for (int p = 0; p < res.blockCount; p++) {
            List<Object> rowVals = new ArrayList<>();
            rowVals.add("Block " + (p + 1));
            for (int q = 0; q < res.blockCount; q++) {
                rowVals.add(String.format(Locale.ROOT, "%.4f", res.blockmodelDensity[p][q]));
            }
            imageB.addRow(rowVals.toArray());
        }
        TableData imageTable = imageB.build();
        TableRegistry.register(imageTable);
        report.addTable(imageTable);

        TableData.Builder memB = TableData.builder("Structural Role / Block Assignments in " + src.getName())
                .network(src.getName())
                .columns("Node", "Assigned Block / Position")
                .isNumeric(false, true);

        for (int i = 0; i < src.getSize(); i++) {
            memB.addRow(src.getActor(i).getName(), "Block " + (res.blockAssignments[i] + 1));
        }
        TableData memTable = memB.build();
        TableRegistry.register(memTable);
        report.addTable(memTable);

        report.addFootnote("Two actors are structurally equivalent if they hold identical relational profiles to all other network participants (Burt, 1976).");
        report.addFootnote("Blockmodel image matrix shows empirical inter-position tie density, revealing core-periphery or reciprocal structural roles.");
        report.addFootnote("References: Burt, R. S. (1976). Social Forces, 55(1), 93-122; Wasserman & Faust (1994). Social Network Analysis, Chapter 9.");
        return report;
    }

    public static AnalysisReport createWeightedMetricsReport(Network src, AgnaLib lib, String legacy) {
        WeightedMetrics.WeightedSummary sum = WeightedMetrics.compute(src);
        AnalysisReport report = new AnalysisReport("Valued & Weighted Network Analysis", src.getName());
        report.setRawPlainText(legacy);

        report.addProperty("Binary Density (Unweighted)", String.format(Locale.ROOT, "%.4f", sum.binaryDensity));
        report.addProperty("Valued Density (Mean Potential Tie)", String.format(Locale.ROOT, "%.4f", sum.valuedDensity));
        report.addProperty("Average Edge Weight (Existing Ties)", String.format(Locale.ROOT, "%.4f", sum.meanTieWeight));
        report.addProperty("Mean Barrat Weighted Clustering", String.format(Locale.ROOT, "%.4f", sum.meanWeightedClustering));

        TableData.Builder b = TableData.builder("Node Strengths & Weighted Clustering in " + src.getName())
                .network(src.getName())
                .columns("Node", "In-Strength (s_in)", "Out-Strength (s_out)", "Total Strength (s)", "Barrat Clustering (C^w)")
                .isNumeric(false, true, true, true, true);

        for (int i = 0; i < src.getSize(); i++) {
            b.addRow(src.getActor(i).getName(),
                    String.format(Locale.ROOT, "%.2f", sum.inStrength[i]),
                    String.format(Locale.ROOT, "%.2f", sum.outStrength[i]),
                    String.format(Locale.ROOT, "%.2f", sum.totalStrength[i]),
                    String.format(Locale.ROOT, "%.4f", sum.weightedClustering[i]));
        }
        TableData dt = b.build();
        TableRegistry.register(dt);
        report.addTable(dt);

        report.addFootnote("Node strength generalizes degree by summing the weights of adjacent incident ties (Barrat et al., 2004).");
        report.addFootnote("Barrat weighted clustering normalizes local triangle weight by node strength and unweighted degree.");
        report.addFootnote("Reference: Barrat, A., et al. (2004). The architecture of complex weighted networks. PNAS, 101(11), 3747-3752.");
        return report;
    }
}
