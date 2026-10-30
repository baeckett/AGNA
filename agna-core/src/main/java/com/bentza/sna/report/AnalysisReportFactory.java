/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import com.bentza.sna.core.AppRuntime;
import com.bentza.sna.net.Actor;
import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
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
        b.addRow("Connectivity", connected ? "Connected" : "Disconnected", connected ? "All node pairs mutually reachable" : "Some node pairs unreachable");
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

        if (!connected) {
            String warnMsg = "WARNING: The network '" + src.getName()
                    + "' is disconnected. Freeman betweenness requires all pairs to be connected; the values below are computed on existing paths only and underestimate centrality.";
            report.setNote(warnMsg, true);
        }

        float[] betw = lib.betweenness(src);
        float cent = (betw != null) ? lib.freemanGeneralIndex(betw, lib.betweenness(new Network(size, 1))) : 0f;

        report.addProperty("Freeman Network Centralization", String.format(Locale.ROOT, "%.4f", cent));

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

        if (!connected) {
            String warnMsg = "WARNING: The network '" + src.getName()
                    + "' is disconnected. Freeman closeness requires a fully connected graph; reported values reflect reachable nodes only and are not comparable across components.";
            report.setNote(warnMsg, true);
        }

        float[] closeness = lib.closeness(src);
        float cent = (closeness != null) ? lib.freemanGeneralIndex(closeness, lib.closeness(new Network(size, 1))) : 0f;

        report.addProperty("Freeman Network Centralization", String.format(Locale.ROOT, "%.4f", cent));

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

        if (!connected) {
            String warnMsg = "WARNING: The network '" + src.getName() + "' is disconnected.";
            report.setNote(warnMsg, true);
        }

        float[] fare = lib.fareness(src);
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

        if (!connected) {
            String warnMsg = "WARNING: The network '" + src.getName() + "' is disconnected.";
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

        if (!connected) {
            report.setNote("WARNING: The network '" + src.getName() + "' is disconnected.", true);
        }

        float[] bav = lib.bavelas(src);
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
        AnalysisReport report = new AnalysisReport("FULL ANALYSIS Battery", src.getName());
        report.setRawPlainText(legacy);
        boolean connected = lib.isConnected(src);

        // Build composite report
        AnalysisReport basic = createBasicReport(src, lib, "");
        for (TableData td : basic.getTables()) report.addTable(td);

        AnalysisReport dens = createDensityReport(src, lib, "");
        for (TableData td : dens.getTables()) report.addTable(td);

        AnalysisReport nodal = createNodalDegreeReport(src, lib, "");
        for (TableData td : nodal.getTables()) report.addTable(td);

        AnalysisReport inDeg = createInDegreeReport(src, lib, "");
        for (TableData td : inDeg.getTables()) report.addTable(td);

        AnalysisReport outDeg = createOutDegreeReport(src, lib, "");
        for (TableData td : outDeg.getTables()) report.addTable(td);

        AnalysisReport prest = createPrestigeReport(src, lib, "");
        for (TableData td : prest.getTables()) report.addTable(td);

        if (connected) {
            AnalysisReport betw = createBetweennessReport(src, lib, "");
            for (TableData td : betw.getTables()) report.addTable(td);

            AnalysisReport close = createClosenessReport(src, lib, "");
            for (TableData td : close.getTables()) report.addTable(td);

            AnalysisReport geo = createGeodesicsReport(src, lib, "");
            for (TableData td : geo.getTables()) report.addTable(td);
        } else {
            report.setNote("Centrality and geodesic measures skipped: the network is disconnected (run each measure separately for details).", true);
        }

        report.addFootnote("Full analysis battery compiles all centralities, degree distributions, distance metrics, and entropy indices.");
        report.addFootnote("Matrix diagonal values (self-loops) are excluded across all actor-level metrics.");

        return report;
    }
}
