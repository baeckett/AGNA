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
import java.util.Locale;

/**
 * Automated plain-English structural narrative generator for Social Network Analysis.
 * Translates raw mathematical coefficients (density, centralization, transitivity,
 * reciprocity, and components) into actionable, beginner-friendly interpretations.
 */
public final class NarrativeSummaryGenerator {

    private NarrativeSummaryGenerator() {}

    /**
     * Generates a concise 2-3 paragraph plain-English structural interpretation of the network.
     */
    public static String generateSummary(Network net, AgnaLib lib) {
        if (net == null || net.getSize() < 2) {
            return "Network is too small to synthesize a structural interpretation (minimum 2 actors required).";
        }

        int n = net.getSize();
        int edges = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && net.getValue(i, j) != 0) {
                    edges++;
                }
            }
        }
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
}
