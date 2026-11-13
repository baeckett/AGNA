/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Educational repository of SNA definitions, sociological interpretations,
 * and practical guidance for novice users and researchers.
 * Powers rich HTML tooltips across all computational battery dialogs and the
 * in-app SNA Glossary / Cheat Sheet.
 */
public final class EducationalTooltips {

    public static class MetricGuide {
        public final String id;
        public final String title;
        public final String category;
        public final String whatItMeasures;
        public final String whyUseIt;
        public final String interpretation;
        public final String technicalNote;

        public MetricGuide(String id, String title, String category,
                           String whatItMeasures, String whyUseIt,
                           String interpretation, String technicalNote) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.whatItMeasures = whatItMeasures;
            this.whyUseIt = whyUseIt;
            this.interpretation = interpretation;
            this.technicalNote = technicalNote;
        }

        public String toHtmlTooltip() {
            StringBuilder sb = new StringBuilder(512);
            sb.append("<html><div style='width: 320px; padding: 3px; font-family: sans-serif; font-size: 11px;'>");
            sb.append("<b style='color: #004080; font-size: 12px;'>").append(title).append("</b><br>");
            sb.append("<div style='margin-top: 4px;'>");
            sb.append("<b>What it measures:</b> ").append(whatItMeasures).append("<br>");
            sb.append("<b>Why use it:</b> ").append(whyUseIt).append("<br>");
            if (interpretation != null && !interpretation.isEmpty()) {
                sb.append("<b>Interpretation:</b> ").append(interpretation).append("<br>");
            }
            if (technicalNote != null && !technicalNote.isEmpty()) {
                sb.append("<small style='color: #666666;'><i>Note:</i> ").append(technicalNote).append("</small>");
            }
            sb.append("</div></div></html>");
            return sb.toString();
        }
    }

    private static final Map<String, MetricGuide> GUIDES = new LinkedHashMap<>();

    static {
        // Degree Centrality
        add("nodal_degree", "Nodal Degree (Total Activity)", "Degree & Volume",
            "Total number of direct connections (incoming + outgoing ties) incident to an actor.",
            "Measures overall social volume and direct communication activity. Identifies the most connected local actors.",
            "High degree nodes are active hubs; low degree nodes are peripheral or isolated.",
            "O(V^2) calculation. Undirected graphs treat degree as total incident edges.");

        add("indegree", "In-Degree Centrality (Popularity & Prestige)", "Degree & Volume",
            "Number of incoming ties directed towards an actor from others.",
            "Measures popularity, prestige, advice-seeking, and deference. In advice or citation networks, indicates recognized expertise.",
            "High in-degree means many peers seek out or nominate this person.",
            "Meaningful only in directed networks; equals out-degree in symmetric graphs.");

        add("outdegree", "Out-Degree Centrality (Expansiveness)", "Degree & Volume",
            "Number of outgoing ties an actor sends to others in the network.",
            "Measures social expansiveness, assertiveness, and outreach. In communication networks, indicates active senders.",
            "High out-degree indicates an active sender; low out-degree indicates selective or passive behavior.",
            "Meaningful only in directed networks.");

        add("emission_degree", "Emission Degree (Valued Outflow)", "Degree & Volume",
            "Sum of outgoing tie weights/values emitted by the actor.",
            "Accounts for the intensity or frequency of interaction rather than just binary presence.",
            "Distinguishes someone who sends many weak ties from someone who concentrates strong energy.",
            "Equals out-degree on binary (0/1) networks.");

        add("reception_degree", "Reception Degree (Valued Inflow)", "Degree & Volume",
            "Sum of incoming tie weights/values received by the actor.",
            "Measures total incoming volume, attention, or resource inflow.",
            "Indicates actors who absorb the greatest amount of organizational communication or resources.",
            "Equals in-degree on binary (0/1) networks.");

        add("determination_degree", "Determination Degree (Net Balance)", "Degree & Volume",
            "Normalized difference between reception and emission: (In - Out) / (N - 1).",
            "Measures relational balance: is an actor primarily a recipient or a provider?",
            "Positive = net sink/recipient; Negative = net source/emitter; Zero = balanced interaction.",
            "Agna-specific index normalized by group order.");

        add("sociostatus", "Sociometric Status (Combined Strength)", "Degree & Volume",
            "Normalized total tie weight: (In + Out) / (N - 1).",
            "Measures an actor's overall presence and energetic volume in the group.",
            "Higher values denote individuals who participate in heavy volumes of mutual interaction.",
            "Agna-specific index normalized by group order.");

        // Geodesic Centrality
        add("betweenness", "Betweenness Centrality (Freeman / Brandes)", "Path Centrality",
            "The proportion of all shortest paths between all pairs of nodes that pass through this actor.",
            "Identifies brokers, gatekeepers, and potential bottlenecks. Vital for studying structural holes and information control.",
            "High betweenness nodes hold power over communication flow; removing them fragments the network.",
            "Computed via Brandes O(VE) algorithm. Normalized against theoretical maximum.");

        add("closeness", "Closeness Centrality (Freeman)", "Path Centrality",
            "Inverted sum of shortest path distances to all other actors: (N - 1) / Sum(d(i, j)).",
            "Measures independence from intermediaries and how rapidly information can diffuse from or reach this node.",
            "High closeness means the node can reach everyone in few steps; ideal for message broadcasting.",
            "Requires a single connected component; undefined across disconnected subgraphs.");

        add("bavelas", "Bavelas-Leavitt Centrality Index", "Path Centrality",
            "Classical ratio of total network distance to actor distance: Sum(d) / (2 * Sum(d_i)).",
            "Measures communication efficiency in small groups and organizational hierarchies (Bavelas 1950, Leavitt 1951).",
            "Highest for central coordinators in star and wheel structures.",
            "Requires a single connected component.");

        add("fareness", "Fareness Index (Total Distance)", "Path Centrality",
            "The sum of shortest-path distances from this actor to all other actors: Sum(d(i, j)).",
            "Direct measure of isolation from the rest of the group.",
            "Lower fareness is better (more central); high fareness indicates peripheral, remote actors.",
            "The inverse of closeness centrality.");

        add("prestige", "Proximity Prestige (Lin 1976)", "Path Centrality",
            "The share of other actors who can reach this node, weighted by their average distance.",
            "Measures prestige in asymmetric networks where reachability is directional and unequal.",
            "High prestige requires being reachable by many nodes who are themselves centrally positioned.",
            "Ranges [0.0, 1.0]. Zero if no other actor can reach this node.");

        // Spectral Centrality
        add("eigenvector", "Eigenvector Centrality (Bonacich 1972)", "Spectral Centrality",
            "Principal eigenvector of the adjacency matrix: x_i is proportional to the sum of x_j for all neighbors j.",
            "Measures influence by looking at who your neighbors are ('It is not just how many friends you have, but who they are').",
            "Connecting to a few high-status hubs yields higher score than connecting to many isolates.",
            "Uses shifted power iteration (A + I) guaranteeing strict Perron-Frobenius convergence.");

        add("bonacich", "Bonacich Power Centrality c(alpha, beta)", "Spectral Centrality",
            "Parametric centrality: c = alpha * (I - beta * A)^-1 * A * 1.",
            "Allows tuning between social prestige (beta > 0) and bargaining exchange power (beta < 0).",
            "Beta > 0: Friends of powerful people are powerful. Beta < 0: Powerful actors negotiate with weak, dependent alters.",
            "Beta must stay within |beta| < 1 / lambda_max for mathematical convergence.");

        add("pagerank", "PageRank Centrality (Page et al. 1999)", "Spectral Centrality",
            "Stationary probability distribution of a random surfer with periodic uniform teleportation (damping d).",
            "Measures systemic prominence in directed networks while resisting dead-ends and spider traps.",
            "Standard web and citation ranking metric. Recommended damping factor d = 0.85.",
            "Iterative power method converging to sum = 1.0.");

        // Clustering & Cohesion
        add("local_clustering", "Watts-Strogatz Local Clustering (C_i)", "Clustering & Cohesion",
            "The proportion of possible links between a node's immediate neighbors that actually exist: 2*T_i / (k*(k-1)).",
            "Quantifies triadic closure: 'Are my friends friends with each other?' Foundational for Small-World theory.",
            "High clustering indicates tight, close-knit local cliques; low clustering indicates tree-like branching.",
            "Undefined for nodes with degree < 2. Mean <C> averages across all qualifying nodes.");

        add("transitivity", "Newman Global Network Transitivity (T)", "Clustering & Cohesion",
            "The fraction of all two-step connected paths that close into complete triangles: 3 * Triangles / Triplets.",
            "Measures the overall global propensity for social clustering across the entire network.",
            "Ranges [0.0, 1.0]. Social networks typically show T in [0.3, 0.7]; random graphs have T ~ Density.",
            "Often called the global clustering coefficient.");

        add("kcore", "k-Core Decomposition (Batagelj & Zaveršnik)", "Clustering & Cohesion",
            "Iteratively prunes nodes with degree < k to reveal nested, resilient structural core shells.",
            "Identifies the dense organizational backbone vs fragile peripheral appendages.",
            "Nodes with high k-coreness remain connected even under heavy random node removals.",
            "Linear time O(V + E) core percolation algorithm.");

        add("cohesion", "Classical Cohesion Index (Knoke)", "Clustering & Cohesion",
            "Proportion of all possible dyads that are mutually reciprocal: Mutual / (N * (N - 1) / 2).",
            "Measures bilateral commitment and stability in social groups.",
            "High cohesion indicates reciprocal trust and balanced relations.",
            "Meaningful on directed networks; equals density on undirected networks.");

        add("open_chains", "Open Chains Summary (Two-Step Paths)", "Clustering & Cohesion",
            "Counts two-step paths A -> B -> C where A does not connect directly to C.",
            "Measures structural hole opportunities for node B and potential for future triadic closure.",
            "High open chains signify structural tension where broker B can arbitrate between A and C.",
            "Computed via matrix squaring (A^2 - A).");

        add("cliques", "Maximal Cliques (Bron-Kerbosch)", "Clustering & Cohesion",
            "Exhaustive enumeration of all maximal completely connected subgraphs (every member tied to every other).",
            "Identifies cohesive subgroups with absolute internal agreement and maximum social cohesion.",
            "Actors in multiple overlapping cliques act as organizational liaisons.",
            "NP-hard complexity. Search is capped at 1,000 reported cliques on dense networks.");

        add("communities", "Community Detection & Newman Modularity Q", "Communities",
            "Partitions the network into cohesive clusters with high internal tie density and sparse bridging ties.",
            "Discovers informal teams, factions, scientific specialties, and echo chambers automatically.",
            "Modularity Q > 0.3 indicates significant, non-random modular community structure.",
            "Default engine: multi-phase greedy Louvain modularity optimization.");

        add("girvan_newman", "Girvan-Newman Divisive Algorithm", "Communities",
            "Iteratively computes Brandes edge-betweenness and removes the highest-traffic bridge edges.",
            "Reveals natural hierarchical fault lines and community splits.",
            "Very intuitive: cuts the bridges that connect distinct communities.",
            "Computationally intensive O(E^2 V); recommended for networks with N <= 80 actors.");

        add("structural_equivalence", "Structural Equivalence & Blockmodeling", "Communities",
            "Compares actor tie profiles (inflow & outflow) using Euclidean distance and Pearson correlation.",
            "Identifies actors who occupy the exact same structural position or organizational role, even if they never interact directly.",
            "Generates a K x K image density matrix revealing core-periphery or hierarchical roles.",
            "Uses complete-linkage hierarchical agglomerative clustering into K blocks.");

        // Sociometrics & Topology
        add("basic_metrics", "Basic Structural Parameters", "Sociometrics",
            "Fundamental network statistics: number of actors (order), ties (size), directedness, and weight type.",
            "Essential baseline profile before running specialized algorithms.",
            "Reports whether the matrix is symmetric, asymmetric, binary, or valued.",
            "Always safe and instantaneous.");

        add("density", "Network Density & Completeness", "Sociometrics",
            "Ratio of actual ties to the maximum possible number of ties: Arcs / (N * (N - 1)).",
            "Measures overall connectivity, social saturation, and communication bandwidth.",
            "Values range [0.0, 1.0]. Density typically decreases as network size N grows.",
            "Prints both directed and undirected interpretations when symmetric.");

        add("dyad_census", "MAN Dyad Census & Reciprocity", "Sociometrics",
            "Categorizes all pairs into Mutual (M), Asymmetric (A), and Null (N) ties (Holland & Leinhardt).",
            "Quantifies reciprocity rate: 2M / (2M + A). Essential for studying norms of reciprocity.",
            "High reciprocity indicates egalitarian mutual exchange; low reciprocity indicates hierarchical authority.",
            "Exact partition of N*(N - 1)/2 dyads.");

        add("assortativity", "Newman Degree Assortativity (r)", "Sociometrics",
            "Pearson correlation of degrees at either end of every edge: r in [-1.0, +1.0].",
            "Determines whether hubs connect preferentially to other hubs (assortative) or to peripheral leaves (disassortative).",
            "Social networks are typically assortative (r > 0: famous people know famous people); technical/biological networks are disassortative (r < 0).",
            "Computes total degree r plus 4 directed mixing regimes (out-in, in-in, out-out, in-out).");

        add("weighted_metrics", "Valued & Weighted Network Analysis", "Sociometrics",
            "Computes node strengths (in/out/total) and Barrat et al. (2004) weighted local clustering.",
            "Prevents loss of information when networks contain tie intensities, frequencies, or capacities.",
            "Distinguishes strong relationships from casual ties.",
            "Preserves continuous edge weights without artificial dichotomization.");

        add("freeman_centralization", "Freeman (1979) Global Centralization", "Sociometrics",
            "Measures the degree of inequality or concentration of centrality across the entire graph, benchmarked against a star graph.",
            "Answers: 'Is this network dominated by a single central coordinator, or is power evenly dispersed?'",
            "1.0 = perfect star hierarchy; 0.0 = complete or regular graph where everyone is equal.",
            "Computes degree (C_D), betweenness (C_B), closeness (C_C), and Bavelas (C_Bav) centralizations.");

        // Distances & Paths
        add("geodesics", "Geodesic Distance Matrix", "Distances & Paths",
            "Shortest-path hop counts between all ordered pairs of nodes in the network.",
            "The foundational matrix underlying closeness, betweenness, diameter, and eccentricity.",
            "Entry (i, j) = 0 indicates no path exists or self-distance.",
            "Computed via unweighted breadth-first search (BFS) O(V*(V + E)).");

        add("diameter", "Network Diameter & Average Path Length", "Distances & Paths",
            "Diameter is the largest geodesic distance between any reachable pair of actors.",
            "Measures the maximum communication delay across the network.",
            "Small diameter combined with high clustering defines a Small-World network.",
            "Undefined if network has unreachable disconnected pairs.");

        add("eccentricity", "Node Eccentricity & Periphery", "Distances & Paths",
            "Maximum shortest path from a given node to any other node in the network.",
            "Nodes with minimum eccentricity form the network Center; nodes with maximum eccentricity form the Periphery.",
            "Central nodes can coordinate emergencies faster than peripheral nodes.",
            "Requires connected graph.");

        add("shortest_paths", "Specific Shortest Path Enumeration", "Distances & Paths",
            "Traces the exact intermediate sequence of actors connecting source A to target B.",
            "Useful for investigating specific routing, message chains, or relational influence pathways.",
            "Reports all alternative shortest paths if multiple optimal routes exist.",
            "Breadth-first search backtracking.");

        // Hypothesis Testing
        add("null_erdos_renyi", "Erdős–Rényi Null Model G(n, m)", "Hypothesis Testing",
            "Randomizes ties uniformly while holding network order N and size M (density) constant.",
            "Tests whether an observed metric (e.g. clustering or reciprocity) is higher than expected by pure chance.",
            "If your network's clustering is 0.50 and the null model average is 0.05, clustering is statistically significant.",
            "Preserves density; destroys degree sequence and dyadic reciprocity.");

        add("null_degree_preserving", "Degree-Preserving Configuration Model", "Hypothesis Testing",
            "Randomizes ties via Maslov-Sneppen double edge swaps while preserving the exact in- and out-degree of every node.",
            "Tests whether a property (like transitivity or assortativity) is a real structural feature or merely an artifact of degree distribution.",
            "Gold standard in network science hypothesis testing.",
            "MCMC edge swap algorithm preserving individual degree sequences.");

        add("null_dyad_census", "Dyad Census Preserving Model (U|MAN)", "Hypothesis Testing",
            "Randomizes edges while holding exact counts of Mutual, Asymmetric, and Null dyads constant.",
            "Tests structural features while controlling for bilateral reciprocity norms.",
            "Particularly useful in sociometric studies of friendship and trust.",
            "Holland-Leinhardt conditioned uniform random graph.");
    }

    private static void add(String id, String title, String category,
                            String whatItMeasures, String whyUseIt,
                            String interpretation, String technicalNote) {
        GUIDES.put(id, new MetricGuide(id, title, category, whatItMeasures, whyUseIt, interpretation, technicalNote));
    }

    public static MetricGuide get(String id) {
        return GUIDES.get(id);
    }

    public static String getTooltip(String id) {
        MetricGuide g = GUIDES.get(id);
        return g != null ? g.toHtmlTooltip() : null;
    }

    public static Map<String, MetricGuide> getAll() {
        return Collections.unmodifiableMap(GUIDES);
    }

    private EducationalTooltips() {}
}
