/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.advisor;

import com.bentza.sna.census.DyadCensus;
import com.bentza.sna.clustering.ClusteringMetrics;
import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Intelligent topological analysis and workflow recommendation engine.
 * Inspects graph order, density, directedness, components, degree variance,
 * and reciprocity to advise researchers and AI agents on the most scientifically
 * sound SNA metrics, layout algorithms, and null hypothesis models.
 */
public final class NetworkAdvisor {

    public static class AnalysisRecommendation {
        public final String id;
        public final String name;
        public final String priority; // HIGH, MEDIUM, LOW
        public final String rationale;

        public AnalysisRecommendation(String id, String name, String priority, String rationale) {
            this.id = id;
            this.name = name;
            this.priority = priority;
            this.rationale = rationale;
        }
    }

    public static class LayoutRecommendation {
        public final String layout;
        public final String rationale;

        public LayoutRecommendation(String layout, String rationale) {
            this.layout = layout;
            this.rationale = rationale;
        }
    }

    public static class NullModelRecommendation {
        public final String model;
        public final String rationale;

        public NullModelRecommendation(String model, String rationale) {
            this.model = model;
            this.rationale = rationale;
        }
    }

    public static class AdviceReport {
        public String networkName;
        public int nodes;
        public int edges;
        public double density;
        public boolean directed;
        public boolean connected;
        public int components;
        public int isolates;
        public String summary;
        public List<AnalysisRecommendation> recommendedAnalyses = new ArrayList<>();
        public List<LayoutRecommendation> recommendedLayouts = new ArrayList<>();
        public List<NullModelRecommendation> recommendedNullModels = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();

        public String toPlainText() {
            StringBuilder sb = new StringBuilder(1024);
            sb.append("=== AGNA Network Analysis Advisory: ").append(networkName).append(" ===\n\n");
            sb.append("Topological Summary:\n  ").append(summary).append("\n\n");

            sb.append("Recommended Analyses:\n");
            for (AnalysisRecommendation a : recommendedAnalyses) {
                sb.append(String.format(Locale.ROOT, "  [%-6s] %-24s - %s\n", a.priority, a.name, a.rationale));
            }
            sb.append("\nRecommended Visual Layouts:\n");
            for (LayoutRecommendation l : recommendedLayouts) {
                sb.append(String.format(Locale.ROOT, "  %-16s - %s\n", l.layout, l.rationale));
            }

            if (!recommendedNullModels.isEmpty()) {
                sb.append("\nRecommended Hypothesis Null Models:\n");
                for (NullModelRecommendation m : recommendedNullModels) {
                    sb.append(String.format(Locale.ROOT, "  %-22s - %s\n", m.model, m.rationale));
                }
            }

            if (!warnings.isEmpty()) {
                sb.append("\nMethodological Warnings & Caveats:\n");
                for (String w : warnings) {
                    sb.append("  ⚠️  ").append(w).append("\n");
                }
            }
            return sb.toString();
        }

        public String toJson() {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            root.put("network", networkName != null ? networkName : "");
            root.put("nodes", nodes);
            root.put("edges", edges);
            root.put("density", density);
            root.put("directed", directed);
            root.put("connected", connected);
            root.put("components", components);
            root.put("isolates", isolates);
            root.put("summary", summary);

            ArrayNode an = root.putArray("recommendedAnalyses");
            for (AnalysisRecommendation a : recommendedAnalyses) {
                ObjectNode obj = an.addObject();
                obj.put("id", a.id);
                obj.put("name", a.name);
                obj.put("priority", a.priority);
                obj.put("rationale", a.rationale);
            }

            ArrayNode ly = root.putArray("recommendedLayouts");
            for (LayoutRecommendation l : recommendedLayouts) {
                ObjectNode obj = ly.addObject();
                obj.put("layout", l.layout);
                obj.put("rationale", l.rationale);
            }

            ArrayNode nm = root.putArray("recommendedNullModels");
            for (NullModelRecommendation m : recommendedNullModels) {
                ObjectNode obj = nm.addObject();
                obj.put("model", m.model);
                obj.put("rationale", m.rationale);
            }

            ArrayNode wn = root.putArray("warnings");
            for (String w : warnings) {
                wn.add(w);
            }

            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            } catch (Exception e) {
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        }
    }

    private NetworkAdvisor() {}

    /**
     * Inspects network topology and generates a tailored advisory report.
     */
    public static AdviceReport advise(Network net, AgnaLib lib) {
        AdviceReport rep = new AdviceReport();
        rep.networkName = net.getName() != null ? net.getName() : "Network";
        int n = net.getSize();
        rep.nodes = n;

        if (n < 2) {
            rep.summary = "Network contains fewer than 2 actors; insufficient relational structure to evaluate.";
            rep.warnings.add("Minimum 2 actors required for social network analysis.");
            return rep;
        }

        int edges = 0;
        boolean hasWeights = false;
        int selfLoops = 0;
        for (int i = 0; i < n; i++) {
            if (net.getValue(i, i) != 0f) selfLoops++;
            for (int j = 0; j < n; j++) {
                float v = net.getValue(i, j);
                if (i != j && v != 0f) {
                    edges++;
                    if (v != 1f) hasWeights = true;
                }
            }
        }
        rep.edges = edges;
        rep.directed = !net.isSymmetric();
        rep.density = Math.round(lib.density(net) * 10000.0) / 10000.0;
        rep.connected = lib.isConnected(net);
        rep.components = countComponents(net);

        int isolates = 0;
        for (int i = 0; i < n; i++) {
            if (net.isOutsider(i)) isolates++;
        }
        rep.isolates = isolates;

        // Degree distribution
        float[] deg = lib.nodalDegree(net);
        float maxDeg = 0f;
        float sumDeg = 0f;
        for (int i = 0; i < n; i++) {
            if (deg != null && i < deg.length) {
                sumDeg += deg[i];
                if (deg[i] > maxDeg) maxDeg = deg[i];
            }
        }
        float avgDeg = n > 0 ? (sumDeg / n) : 0f;
        float degCentralization = lib.freemanDegreeCentralization(net);

        // Topological classification summary
        String scaleDesc = n < 25 ? "Small" : (n <= 150 ? "Medium-scale" : "Large");
        String dirDesc = rep.directed ? "directed" : "undirected";
        String denseDesc = rep.density >= 0.50 ? "dense" : (rep.density >= 0.15 ? "moderately connected" : "sparse");
        String compDesc = rep.connected ? "single component" : (rep.components + " disconnected components");
        rep.summary = String.format(Locale.ROOT,
                "%s %s, %s network (%d nodes, %d edges) forming a %s with degree centralization %.2f.",
                scaleDesc, dirDesc, denseDesc, n, edges, compDesc, degCentralization);

        // Warnings
        if (!rep.connected) {
            rep.warnings.add(String.format(Locale.ROOT,
                    "Graph is fragmented into %d components. Global shortest-path measures (closeness, diameter, fareness) are mathematically undefined across disconnected components.",
                    rep.components));
        }
        if (selfLoops > 0) {
            rep.warnings.add(String.format(Locale.ROOT,
                    "Contains %d self-loop(s). May artificially inflate degree totals and distort geodesic path computations.",
                    selfLoops));
        }
        if (rep.density >= 0.80) {
            rep.warnings.add("Density is extraordinarily high (>0.80). Most paths are 1-hop and triadic closure is saturated; path-based brokerage metrics will show low variance.");
        } else if (rep.density <= 0.02 && n > 20) {
            rep.warnings.add("Extremely sparse network (<0.02). Closeness will be low and many node pairs will be distant or unreachable.");
        }

        // Analysis Recommendations
        if (rep.connected) {
            rep.recommendedAnalyses.add(new AnalysisRecommendation("betweenness", "Betweenness Centrality", "HIGH",
                    "Network is connected: betweenness will effectively identify key information brokers and bottleneck bridges."));
            rep.recommendedAnalyses.add(new AnalysisRecommendation("closeness", "Closeness Centrality", "MEDIUM",
                    "Evaluates which actors can disseminate or receive information with the fewest intermediate hops."));
            rep.recommendedAnalyses.add(new AnalysisRecommendation("diameter", "Network Diameter & Geodesics", "MEDIUM",
                    "Measures overall communication efficiency and longest geodesic delay."));
        } else {
            rep.recommendedAnalyses.add(new AnalysisRecommendation("components", "Connected Components", "HIGH",
                    "Isolate subgraphs and analyze structural silos before computing global metrics."));
            rep.recommendedAnalyses.add(new AnalysisRecommendation("ego_network", "Ego Network Analysis", "HIGH",
                    "Focus on localized 1-hop and 2-hop neighborhoods since global reachability is bounded by component partitions."));
        }

        if (rep.directed) {
            rep.recommendedAnalyses.add(new AnalysisRecommendation("indegree_outdegree", "In-Degree & Out-Degree", "HIGH",
                    "Directional ties distinguish deference/prestige (inflow) from social expansiveness/outreach (outflow)."));
            rep.recommendedAnalyses.add(new AnalysisRecommendation("triad_census", "Triad Census (MAN 16)", "HIGH",
                    "Examines directional micro-structures: transitive hierarchy (030T), cycling (030C), and forbidden triad closure."));
            rep.recommendedAnalyses.add(new AnalysisRecommendation("lin_prestige", "Lin Proximity Prestige", "MEDIUM",
                    "Measures prestige based on the proportion of the group that can directionally reach an actor, weighted by distance."));
        } else {
            rep.recommendedAnalyses.add(new AnalysisRecommendation("nodal_degree", "Nodal Degree Centrality", "HIGH",
                    "Core baseline measure of local social activity and connectedness."));
        }

        if (rep.density >= 0.25) {
            rep.recommendedAnalyses.add(new AnalysisRecommendation("transitivity", "Global Transitivity & Closure", "HIGH",
                    "Evaluates the prevalence of triadic closure ('friends of friends are friends') and collective social capital."));
            rep.recommendedAnalyses.add(new AnalysisRecommendation("cliques", "Maximal Cliques", "MEDIUM",
                    "Exhaustively identifies tightly-knit mutual factions and overlapping liaison members."));
        } else {
            rep.recommendedAnalyses.add(new AnalysisRecommendation("structural_holes", "Burt Structural Holes", "HIGH",
                    "Sparse network with potential structural holes: evaluates constraint and non-redundant bridging opportunities."));
        }

        if (n >= 10) {
            rep.recommendedAnalyses.add(new AnalysisRecommendation("communities", "Community Detection (Louvain)", "HIGH",
                    "Automatically partitions actors into cohesive informal factions and functional clusters based on modularity."));
            rep.recommendedAnalyses.add(new AnalysisRecommendation("kcore", "k-Core Decomposition", "MEDIUM",
                    "Discovers resilient structural backbone shells and distinguishes core members from peripheral appendages."));
        }

        if (hasWeights) {
            rep.recommendedAnalyses.add(new AnalysisRecommendation("weighted_metrics", "Valued/Weighted Network Analysis", "HIGH",
                    "Graph contains non-binary tie values: computes continuous node strengths and Barrat weighted clustering without loss of intensity."));
        }

        // Layout Recommendations
        if (degCentralization >= 0.40 || maxDeg >= avgDeg * 2.5) {
            rep.recommendedLayouts.add(new LayoutRecommendation("concentric",
                    "High degree centralization detected: concentric layout places the most prominent hubs at the center with peripheral actors on outer rings."));
        }
        if (n >= 15 && rep.density <= 0.40) {
            rep.recommendedLayouts.add(new LayoutRecommendation("community",
                    "Clusters nodes by modularity community, clearly visualizing internal subgroups and cross-group bridging ties."));
        }
        if (rep.directed && !rep.connected && degCentralization > 0.30) {
            rep.recommendedLayouts.add(new LayoutRecommendation("layered-dag",
                    "Hierarchical DAG layout reveals directional flow, status tiers, and chain of command."));
        }
        rep.recommendedLayouts.add(new LayoutRecommendation("spring",
                "Force-directed spring embedding (Fruchterman-Reingold) provides an intuitive, distance-preserving visualization for general exploration."));
        rep.recommendedLayouts.add(new LayoutRecommendation("circular",
                "Neutral circular arrangement useful for highlighting cross-cutting chords and boundary spanners without layout bias."));

        // Null Model Recommendations
        rep.recommendedNullModels.add(new NullModelRecommendation("degree-preserving",
                "Maslov-Sneppen double edge swaps: tests whether observed clustering or transitivity is genuinely sociological or an artifact of degree distribution."));
        if (rep.directed) {
            rep.recommendedNullModels.add(new NullModelRecommendation("dyad-census",
                    "Holland-Leinhardt conditioned random graph: controls for bilateral reciprocity norms when testing higher-order triad structures."));
        }
        rep.recommendedNullModels.add(new NullModelRecommendation("erdos-renyi",
                "Classical G(n, m) uniform random model: fundamental baseline benchmark holding order and density constant."));

        return rep;
    }

    private static int countComponents(Network net) {
        int n = net.getSize();
        if (n == 0) return 0;
        boolean[] seen = new boolean[n];
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (!seen[i]) {
                count++;
                List<Integer> queue = new ArrayList<>();
                queue.add(i);
                seen[i] = true;
                while (!queue.isEmpty()) {
                    int v = queue.remove(queue.size() - 1);
                    for (int w = 0; w < n; w++) {
                        if (!seen[w] && (net.getValue(v, w) != 0f || net.getValue(w, v) != 0f)) {
                            seen[w] = true;
                            queue.add(w);
                        }
                    }
                }
            }
        }
        return count;
    }
}
