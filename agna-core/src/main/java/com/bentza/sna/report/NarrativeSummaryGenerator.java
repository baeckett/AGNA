/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import com.bentza.sna.census.DyadCensus;
import com.bentza.sna.clustering.ClusteringMetrics;
import com.bentza.sna.net.Actor;
import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;

/**
 * Automated plain-English structural narrative generator for Social Network Analysis.
 * Translates raw mathematical coefficients (density, centralization, transitivity,
 * reciprocity, and components) into actionable, beginner-friendly interpretations.
 * Supports HTML (for GUI reports), Markdown/Plain Text, and structured JSON (for CLI & AI agents).
 */
public final class NarrativeSummaryGenerator {

    private NarrativeSummaryGenerator() {}

    /**
     * Generates a concise HTML structural interpretation of the network for in-app reports.
     */
    public static String generateSummary(Network net, AgnaLib lib) {
        if (net == null || net.getSize() < 2) {
            return "Network is too small to synthesize a structural interpretation (minimum 2 actors required).";
        }

        int n = net.getSize();
        int edges = countEdges(net);
        boolean symmetric = net.isSymmetric();
        boolean connected = lib.isConnected(net);
        float density = lib.density(net);

        StringBuilder sb = new StringBuilder(512);

        // 1. Order, Size, and Density
        sb.append(String.format(Locale.ROOT,
                "<b>Network Scale & Density:</b> Contains %d actors and %d ties (density = %.3f). ",
                n, edges, density));

        if (density >= 0.50f) {
            sb.append("This is a <i>dense, highly saturated network</i> where most members are directly connected, which may facilitate rapid group consensus.");
        } else if (density >= 0.15f) {
            sb.append("This is a <i>moderately connected network</i>, which may balance cohesive local clusters with manageable path lengths.");
        } else {
            sb.append("This is a <i>sparse network</i> where ties appear to form selectively, typical of larger social or organizational environments.");
        }
        sb.append("<br><br>");

        // 2. Topology and Connectedness
        sb.append("<b>Connectedness & Reachability:</b> ");
        if (connected) {
            sb.append("The network forms a <i>single connected component</i>: every actor can reach every other actor through a chain of intermediaries with no fully isolated subgroups.");
        } else {
            int outsiders = lib.getOusidersNumber(net);
            sb.append(String.format(Locale.ROOT,
                    "The network is <i>disconnected</i> into separate components%s. Information may not be able to flow globally without external intermediaries.",
                    outsiders > 0 ? " and contains " + outsiders + " isolate(s)" : ""));
        }
        sb.append("<br><br>");

        // 3. Centralization and Dominance
        float maxDeg = -1f;
        int maxNode = -1;
        float[] deg = lib.nodalDegree(net);
        if (deg != null) {
            for (int i = 0; i < n; i++) {
                if (deg[i] > maxDeg) {
                    maxDeg = deg[i];
                    maxNode = i;
                }
            }
        }
        float degCent = lib.freemanDegreeCentralization(net);
        Actor topActor = (maxNode >= 0) ? net.getActor(maxNode) : null;
        String topName = (topActor != null && topActor.getName() != null && !topActor.getName().isEmpty())
                ? topActor.getName() : ("Node " + (maxNode + 1));

        sb.append(String.format(Locale.ROOT,
                "<b>Inequality & Centralization:</b> Degree centralization is <b>%.3f</b> (on a scale of 0.0 to 1.0). ",
                degCent));

        if (degCent >= 0.50f) {
            sb.append(String.format(Locale.ROOT,
                    "This may indicate <i>high star-like concentration</i>: communication may be disproportionately routed through central hub <b>'%s'</b> (degree %.0f), who may hold primary coordinating authority.",
                    topName, maxDeg));
        } else if (degCent >= 0.20f) {
            sb.append(String.format(Locale.ROOT,
                    "This may indicate <i>moderate centralization</i>: activity appears distributed across a core group of influential actors rather than concentrated in a single node.",
                    topName));
        } else {
            sb.append("This may indicate an <i>egalitarian, decentralized topology</i>: power and connectivity appear relatively evenly dispersed across members with low hierarchy.");
        }
        sb.append("<br><br>");

        // 4. Triadic Closure & Reciprocity
        ClusteringMetrics.TransitivityResult trans = ClusteringMetrics.computeTransitivity(net);
        float transitivity = (trans != null) ? (float) trans.transitivity : 0f;

        sb.append(String.format(Locale.ROOT,
                "<b>Clustering & Triadic Closure:</b> Global transitivity is <b>%.3f</b>. ",
                transitivity));
        if (transitivity >= 0.40f) {
            sb.append("There appears to be <i>strong social clustering</i>: 'friends of friends tend to be friends', which may foster internal trust and cohesive cliques.");
        } else {
            sb.append("There appears to be <i>lower triadic closure</i>: relationships may follow instrumental, tree-like, or hub-and-spoke branching rather than closed triangles.");
        }

        if (!symmetric) {
            DyadCensus dyad = DyadCensus.compute(net);
            if (dyad != null && (dyad.mutual + dyad.asymmetric) > 0) {
                float recip = (float) dyad.reciprocityRate;
                sb.append(String.format(Locale.ROOT,
                        "<br><br><b>Reciprocity:</b> Dyadic reciprocity rate is <b>%.1f%%</b> (%d mutual vs %d asymmetric ties). %s",
                        recip * 100f, dyad.mutual, dyad.asymmetric,
                        recip >= 0.50f ? "Relations appear predominantly mutual and bilateral, which may reflect shared social norms."
                                       : "Ties appear predominantly unreciprocated, which may suggest status differentials or directional information passing."));
            }
        }

        // 5. Educational Guidance / What to explore next
        sb.append("<br><br><b>💡 Suggested Next Steps for Analysis:</b>");
        sb.append("<ul style='margin-top:4px; margin-bottom:0px; padding-left:20px;'>");
        if (connected) {
            sb.append("<li>Run <b>Betweenness Centrality</b> to identify non-obvious gatekeepers bridging distinct subgroups.</li>");
        }
        sb.append("<li>Run <b>Community Detection (Louvain)</b> to automatically reveal natural clusters and informal teams.</li>");
        sb.append("<li>Run <b>Hypothesis Testing (Null Models)</b> to verify if this network's transitivity is statistically significant.</li>");
        sb.append("</ul>");

        return sb.toString();
    }

    /**
     * Generates a clean Markdown / plain-text structural interpretation.
     */
    public static String generateMarkdownSummary(Network net, AgnaLib lib) {
        if (net == null || net.getSize() < 2) {
            return "Network is too small to synthesize a structural interpretation (minimum 2 actors required).\n";
        }

        int n = net.getSize();
        int edges = countEdges(net);
        boolean symmetric = net.isSymmetric();
        boolean connected = lib.isConnected(net);
        float density = lib.density(net);

        StringBuilder sb = new StringBuilder(512);

        sb.append("# Structural Narrative Summary: ").append(net.getName()).append("\n\n");

        // 1. Scale & Density
        sb.append("## 1. Scale & Density\n");
        sb.append(String.format(Locale.ROOT,
                "Contains %d actors and %d ties (density = %.3f). ", n, edges, density));
        if (density >= 0.50f) {
            sb.append("This is a dense, highly saturated network where most members are directly connected, which may facilitate rapid group consensus.\n\n");
        } else if (density >= 0.15f) {
            sb.append("This is a moderately connected network, which may balance cohesive local clusters with manageable path lengths.\n\n");
        } else {
            sb.append("This is a sparse network where ties appear to form selectively, typical of larger social or organizational environments.\n\n");
        }

        // 2. Connectedness
        sb.append("## 2. Connectedness & Reachability\n");
        if (connected) {
            sb.append("The network forms a single connected component: every actor can reach every other actor through a chain of intermediaries with no fully isolated subgroups.\n\n");
        } else {
            int outsiders = lib.getOusidersNumber(net);
            sb.append(String.format(Locale.ROOT,
                    "The network is disconnected into separate components%s. Information may not be able to flow globally without external intermediaries.\n\n",
                    outsiders > 0 ? " and contains " + outsiders + " isolate(s)" : ""));
        }

        // 3. Centralization
        float maxDeg = -1f;
        int maxNode = -1;
        float[] deg = lib.nodalDegree(net);
        if (deg != null) {
            for (int i = 0; i < n; i++) {
                if (deg[i] > maxDeg) {
                    maxDeg = deg[i];
                    maxNode = i;
                }
            }
        }
        float degCent = lib.freemanDegreeCentralization(net);
        Actor topActor = (maxNode >= 0) ? net.getActor(maxNode) : null;
        String topName = (topActor != null && topActor.getName() != null && !topActor.getName().isEmpty())
                ? topActor.getName() : ("Node " + (maxNode + 1));

        sb.append("## 3. Inequality & Centralization\n");
        sb.append(String.format(Locale.ROOT, "Degree centralization is %.3f (scale 0.0 to 1.0). ", degCent));
        if (degCent >= 0.50f) {
            sb.append(String.format(Locale.ROOT,
                    "This may indicate high star-like concentration: communication may be disproportionately routed through central hub '%s' (degree %.0f), who may hold primary coordinating authority.\n\n",
                    topName, maxDeg));
        } else if (degCent >= 0.20f) {
            sb.append("This may indicate moderate centralization: activity appears distributed across a core group of influential actors rather than concentrated in a single node.\n\n");
        } else {
            sb.append("This may indicate an egalitarian, decentralized topology: power and connectivity appear relatively evenly dispersed across members with low hierarchy.\n\n");
        }

        // 4. Clustering & Reciprocity
        ClusteringMetrics.TransitivityResult trans = ClusteringMetrics.computeTransitivity(net);
        float transitivity = (trans != null) ? (float) trans.transitivity : 0f;

        sb.append("## 4. Clustering & Triadic Closure\n");
        sb.append(String.format(Locale.ROOT, "Global transitivity is %.3f. ", transitivity));
        if (transitivity >= 0.40f) {
            sb.append("There appears to be strong social clustering: 'friends of friends tend to be friends', which may foster internal trust and cohesive cliques.\n\n");
        } else {
            sb.append("There appears to be lower triadic closure: relationships may follow instrumental, tree-like, or hub-and-spoke branching rather than closed triangles.\n\n");
        }

        if (!symmetric) {
            DyadCensus dyad = DyadCensus.compute(net);
            if (dyad != null && (dyad.mutual + dyad.asymmetric) > 0) {
                float recip = (float) dyad.reciprocityRate;
                sb.append("## 5. Reciprocity\n");
                sb.append(String.format(Locale.ROOT,
                        "Dyadic reciprocity rate is %.1f%% (%d mutual vs %d asymmetric ties). %s\n\n",
                        recip * 100f, dyad.mutual, dyad.asymmetric,
                        recip >= 0.50f ? "Relations appear predominantly mutual and bilateral, which may reflect shared social norms."
                                       : "Ties appear predominantly unreciprocated, which may suggest status differentials or directional information passing."));
            }
        }

        // Suggested Next Steps
        sb.append("## Suggested Next Steps\n");
        if (connected) {
            sb.append("- Run Betweenness Centrality to identify non-obvious gatekeepers bridging distinct subgroups.\n");
        }
        sb.append("- Run Community Detection (Louvain) to automatically reveal natural clusters and informal teams.\n");
        sb.append("- Run Hypothesis Testing (Null Models) to verify if this network's transitivity is statistically significant.\n");

        return sb.toString();
    }

    /**
     * Generates a structured JSON interpretation for AI agents and programmatic pipelines.
     */
    public static String generateJsonSummary(Network net, AgnaLib lib) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("network", net != null && net.getName() != null ? net.getName() : "");

        if (net == null || net.getSize() < 2) {
            root.put("error", "Network is too small to synthesize a structural interpretation (minimum 2 actors required).");
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            } catch (Exception e) {
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        int n = net.getSize();
        int edges = countEdges(net);
        boolean symmetric = net.isSymmetric();
        boolean connected = lib.isConnected(net);
        float density = lib.density(net);

        // Scale & Density
        ObjectNode scaleObj = root.putObject("scaleAndDensity");
        scaleObj.put("nodes", n);
        scaleObj.put("edges", edges);
        scaleObj.put("density", Math.round(density * 1000.0) / 1000.0);
        if (density >= 0.50f) {
            scaleObj.put("regime", "dense");
            scaleObj.put("interpretation", "Dense, highly saturated network where most members are directly connected, which may facilitate rapid group consensus.");
        } else if (density >= 0.15f) {
            scaleObj.put("regime", "moderate");
            scaleObj.put("interpretation", "Moderately connected network, which may balance cohesive local clusters with manageable path lengths.");
        } else {
            scaleObj.put("regime", "sparse");
            scaleObj.put("interpretation", "Sparse network where ties appear to form selectively, typical of larger social or organizational environments.");
        }

        // Connectedness
        ObjectNode connObj = root.putObject("connectedness");
        connObj.put("connected", connected);
        int outsiders = lib.getOusidersNumber(net);
        connObj.put("outsidersCount", outsiders);
        if (connected) {
            connObj.put("interpretation", "Single connected component: every actor can reach every other actor through a chain of intermediaries.");
        } else {
            connObj.put("interpretation", String.format(Locale.ROOT,
                    "Disconnected into separate components%s. Information cannot flow globally without external bridging.",
                    outsiders > 0 ? " (" + outsiders + " isolates)" : ""));
        }

        // Centralization
        float maxDeg = -1f;
        int maxNode = -1;
        float[] deg = lib.nodalDegree(net);
        if (deg != null) {
            for (int i = 0; i < n; i++) {
                if (deg[i] > maxDeg) {
                    maxDeg = deg[i];
                    maxNode = i;
                }
            }
        }
        float degCent = lib.freemanDegreeCentralization(net);
        Actor topActor = (maxNode >= 0) ? net.getActor(maxNode) : null;
        String topName = (topActor != null && topActor.getName() != null && !topActor.getName().isEmpty())
                ? topActor.getName() : ("Node " + (maxNode + 1));

        ObjectNode centObj = root.putObject("centralization");
        centObj.put("degreeCentralization", Math.round(degCent * 1000.0) / 1000.0);
        centObj.put("topHub", topName);
        centObj.put("topDegree", maxDeg);
        if (degCent >= 0.50f) {
            centObj.put("regime", "high_concentration");
            centObj.put("interpretation", String.format(Locale.ROOT,
                    "High star-like concentration: communication may be disproportionately routed through central hub '%s'.",
                    topName));
        } else if (degCent >= 0.20f) {
            centObj.put("regime", "moderate_centralization");
            centObj.put("interpretation", "Moderate centralization: activity appears distributed across a core group of influential actors.");
        } else {
            centObj.put("regime", "decentralized");
            centObj.put("interpretation", "Egalitarian, decentralized topology: power and connectivity appear relatively evenly dispersed.");
        }

        // Clustering
        ClusteringMetrics.TransitivityResult trans = ClusteringMetrics.computeTransitivity(net);
        float transitivity = (trans != null) ? (float) trans.transitivity : 0f;
        ObjectNode clusterObj = root.putObject("clustering");
        clusterObj.put("transitivity", Math.round(transitivity * 1000.0) / 1000.0);
        if (transitivity >= 0.40f) {
            clusterObj.put("interpretation", "Strong social clustering: 'friends of friends tend to be friends', fostering internal trust and cohesive cliques.");
        } else {
            clusterObj.put("interpretation", "Lower triadic closure: relationships may follow instrumental, tree-like, or branching paths.");
        }

        // Reciprocity
        if (!symmetric) {
            DyadCensus dyad = DyadCensus.compute(net);
            if (dyad != null && (dyad.mutual + dyad.asymmetric) > 0) {
                ObjectNode recipObj = root.putObject("reciprocity");
                recipObj.put("reciprocityRate", Math.round(dyad.reciprocityRate * 1000.0) / 1000.0);
                recipObj.put("mutualTies", dyad.mutual);
                recipObj.put("asymmetricTies", dyad.asymmetric);
                recipObj.put("interpretation", dyad.reciprocityRate >= 0.50
                        ? "Relations appear predominantly mutual and bilateral, reflecting shared social norms."
                        : "Ties appear predominantly unreciprocated, suggesting status differentials or directional information passing.");
            }
        }

        // Next steps
        ArrayNode steps = root.putArray("suggestedNextSteps");
        if (connected) {
            steps.add("Run Betweenness Centrality to identify non-obvious gatekeepers bridging distinct subgroups.");
        }
        steps.add("Run Community Detection (Louvain) to automatically reveal natural clusters and informal teams.");
        steps.add("Run Hypothesis Testing (Null Models) to verify if transitivity is statistically significant.");

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private static int countEdges(Network net) {
        int n = net.getSize();
        int edges = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && net.getValue(i, j) != 0) {
                    edges++;
                }
            }
        }
        return edges;
    }
}
