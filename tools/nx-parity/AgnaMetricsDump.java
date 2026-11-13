/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 *
 * NetworkX parity harness, Java side.
 *
 * Loads every network file given on the command line with Agna's own
 * readers, computes every numeric index Agna exposes, and writes one JSON
 * document per network (JSON Lines) to stdout. The adjacency matrix *as
 * Agna parsed it* is included, so the Python side rebuilds exactly the same
 * graph for NetworkX.
 *
 * Usage: java -cp agna-cli.jar:. AgnaMetricsDump FILE... > agna.jsonl
 *   Files whose name starts with "Chain" are read as text chains.
 */

import com.bentza.sna.capital.StructuralHoles;
import com.bentza.sna.census.DyadCensus;
import com.bentza.sna.census.TriadCensus;
import com.bentza.sna.clustering.ClusteringMetrics;
import com.bentza.sna.clustering.KCoreDecomposition;
import com.bentza.sna.community.CommunityDetectionService;
import com.bentza.sna.connectivity.ConnectivityAnalysis;
import com.bentza.sna.io.IOUtils;
import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.bentza.sna.spectral.SpectralCentrality;
import com.bentza.sna.stats.DegreeAssortativity;
import com.bentza.sna.stats.NetworkMetric;
import com.bentza.sna.stats.WeightedMetrics;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

public class AgnaMetricsDump {

    private final StringBuilder sb = new StringBuilder();
    private boolean first = true;

    public static void main(String[] args) throws Exception {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        for (String path : args) {
            try {
                out.println(new AgnaMetricsDump().dump(path));
            } catch (Throwable t) {
                out.println("{\"file\":" + q(path) + ",\"error\":" + q(t.toString()) + "}");
            }
        }
    }

    // ------------------------------------------------------------------
    String dump(String path) throws Exception {
        File f = new File(path);
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        String ext = dot > 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "agn";
        String text = IOUtils.utf8BytesToText(Files.readAllBytes(f.toPath()));
        FullNet full = new FullNet();
        if (name.startsWith("Chain")) full.readNetworkFromChain(text, ext);
        else full.readNetwork(text, ext);
        Network net = full.getNetwork();
        AgnaLib lib = new AgnaLib();
        int n = net.getSize();

        sb.append('{');
        kv("file", q(path));
        kv("name", q(net.getName()));
        kv("n", String.valueOf(n));
        kv("symmetric", String.valueOf(net.isSymmetric()));
        String[] names = new String[n];
        for (int i = 0; i < n; i++) names[i] = net.getActor(i).getName();
        kv("names", arr(names));
        kv("matrix", mat(net.getMatrix()));

        // --- degree family -------------------------------------------------
        safe("out_degree", () -> arr(lib.emissionDegree(net)));
        safe("in_degree", () -> arr(lib.receptionDegree(net)));
        safe("nodal_degree", () -> arr(lib.nodalDegree(net)));
        safe("w_out_degree", () -> arr(lib.weightedEmissionDegree(net)));
        safe("w_in_degree", () -> arr(lib.weightedReceptionDegree(net)));
        safe("density", () -> num(lib.density(net)));
        safe("weighted_density", () -> num(lib.weightedDensity(net)));
        safe("cohesion", () -> num(lib.cohesion(net)));

        // --- distance family -----------------------------------------------
        safe("connected", () -> String.valueOf(lib.isConnected(net)));
        safe("geodesics", () -> imat(lib.geodesics(net)));
        safe("eccentricity", () -> arr(lib.eccentricity(net)));
        safe("fareness", () -> arr(lib.fareness(net)));
        safe("closeness", () -> arr(lib.closeness(net)));
        safe("betweenness", () -> arr(lib.betweenness(net)));
        safe("prestige", () -> arr(lib.prestige(net)));
        safe("bavelas", () -> arr(lib.bavelas(net)));

        // --- centralization ------------------------------------------------
        safe("freeman_degree_centralization", () -> num(lib.freemanDegreeCentralization(net)));
        if (n > 2) {
            safe("report_betweenness_centralization", () -> num(lib.freemanGeneralIndex(
                    lib.betweenness(net), lib.betweenness(new Network(n, 1)))));
            safe("report_closeness_centralization", () -> num(lib.freemanGeneralIndex(
                    lib.closeness(net), lib.closeness(new Network(n, 1)))));
        }
        for (NetworkMetric m : NetworkMetric.values()) {
            safe("metric_" + m.name().toLowerCase(Locale.ROOT), () -> num(m.compute(net)));
        }

        // --- spectral -----------------------------------------------------
        safe("eigenvector", () -> arr(SpectralCentrality.computeEigenvector(net, 150, 1e-6)));
        safe("bonacich_0.10", () -> arr(SpectralCentrality.computeBonacich(net, 0.10, 100)));
        safe("bonacich_0.05", () -> arr(SpectralCentrality.computeBonacich(net, 0.05, 100)));
        safe("pagerank", () -> arr(SpectralCentrality.computePageRank(net, 0.85, 100)));

        // --- clustering / cores -------------------------------------------
        safe("local_clustering", () -> {
            ClusteringMetrics.LocalClusteringSummary s = ClusteringMetrics.computeLocalClustering(net);
            double[] c = new double[s.actorResults.length];
            int[] tri = new int[s.actorResults.length];
            for (int i = 0; i < c.length; i++) {
                c[i] = s.actorResults[i].clusteringCoefficient;
                tri[i] = s.actorResults[i].triangles;
            }
            return "{\"values\":" + arr(c) + ",\"triangles\":" + iarr(tri)
                    + ",\"mean\":" + num(s.meanClustering)
                    + ",\"total_triangles\":" + s.totalTriangles + "}";
        });
        safe("transitivity", () -> {
            ClusteringMetrics.TransitivityResult t = ClusteringMetrics.computeTransitivity(net);
            return "{\"value\":" + num(t.transitivity) + ",\"triangles\":" + t.totalTriangles
                    + ",\"connected_triplets\":" + t.connectedTriplets + "}";
        });
        safe("coreness", () -> iarr(KCoreDecomposition.decompose(net).coreness));

        // --- census -------------------------------------------------------
        safe("dyad_census", () -> {
            DyadCensus d = DyadCensus.compute(net);
            return "{\"M\":" + d.mutual + ",\"A\":" + d.asymmetric + ",\"N\":" + d.nullDyads
                    + ",\"reciprocity\":" + num(d.reciprocityRate) + "}";
        });
        safe("triad_census", () -> {
            TriadCensus t = TriadCensus.compute(net);
            StringBuilder b = new StringBuilder("{");
            TriadCensus.TriadType[] types = TriadCensus.TriadType.values();
            for (int i = 0; i < types.length; i++) {
                if (i > 0) b.append(',');
                b.append(q(types[i].getCode())).append(':').append(t.getCount(types[i]));
            }
            return b.append('}').toString();
        });

        // --- structural holes ---------------------------------------------
        safe("structural_holes", () -> {
            StructuralHoles h = StructuralHoles.compute(net);
            int k = h.actorMetrics.length;
            double[] es = new double[k], ef = new double[k], co = new double[k], hi = new double[k];
            for (int i = 0; i < k; i++) {
                es[i] = h.actorMetrics[i].effectiveSize;
                ef[i] = h.actorMetrics[i].efficiency;
                co[i] = h.actorMetrics[i].constraint;
                hi[i] = h.actorMetrics[i].hierarchy;
            }
            return "{\"effective_size\":" + arr(es) + ",\"efficiency\":" + arr(ef)
                    + ",\"constraint\":" + arr(co) + ",\"hierarchy\":" + arr(hi) + "}";
        });

        // --- connectivity -------------------------------------------------
        safe("connectivity", () -> {
            ConnectivityAnalysis c = ConnectivityAnalysis.analyze(net);
            StringBuilder b = new StringBuilder("{");
            b.append("\"weak\":").append(comps(c.weakComponents));
            b.append(",\"strong\":").append(comps(c.strongComponents));
            b.append(",\"articulation\":").append(ilist(c.articulationPoints));
            b.append(",\"bridges\":[");
            for (int i = 0; i < c.bridges.size(); i++) {
                if (i > 0) b.append(',');
                b.append('[').append(c.bridges.get(i).u).append(',').append(c.bridges.get(i).v).append(']');
            }
            b.append("],\"giant\":").append(c.giantComponentSize).append('}');
            return b.toString();
        });

        // --- assortativity ------------------------------------------------
        safe("assortativity", () -> {
            DegreeAssortativity.AssortativityResult r = DegreeAssortativity.compute(net);
            return "{\"total\":" + num(r.totalDegreeAssortativity) + ",\"out_in\":" + num(r.outInAssortativity)
                    + ",\"in_in\":" + num(r.inInAssortativity) + ",\"out_out\":" + num(r.outOutAssortativity)
                    + ",\"in_out\":" + num(r.inOutAssortativity) + ",\"edges\":" + r.edgeCount + "}";
        });

        // --- weighted -----------------------------------------------------
        safe("weighted", () -> {
            WeightedMetrics.WeightedSummary w = WeightedMetrics.compute(net);
            return "{\"in_strength\":" + arr(w.inStrength) + ",\"out_strength\":" + arr(w.outStrength)
                    + ",\"total_strength\":" + arr(w.totalStrength)
                    + ",\"w_clustering\":" + arr(w.weightedClustering)
                    + ",\"mean_w_clustering\":" + num(w.meanWeightedClustering)
                    + ",\"binary_density\":" + num(w.binaryDensity)
                    + ",\"valued_density\":" + num(w.valuedDensity)
                    + ",\"mean_weight\":" + num(w.meanTieWeight) + "}";
        });

        // --- community ----------------------------------------------------
        safe("louvain", () -> comm(CommunityDetectionService.detectLouvain(net)));
        if (n <= 60) safe("girvan_newman", () -> comm(CommunityDetectionService.detectGirvanNewman(net)));

        sb.append('}');
        return sb.toString();
    }

    // ------------------------------------------------------------------
    interface Producer { String get() throws Exception; }

    private void safe(String key, Producer p) {
        String v;
        try {
            v = p.get();
            if (v == null) v = "null";
        } catch (Throwable t) {
            v = "{\"error\":" + q(t.toString()) + "}";
        }
        kv(key, v);
    }

    private void kv(String k, String v) {
        if (!first) sb.append(',');
        first = false;
        sb.append(q(k)).append(':').append(v);
    }

    static String comm(CommunityDetectionService.CommunityResult r) {
        return "{\"Q\":" + num(r.modularityQ) + ",\"k\":" + r.communityCount
                + ",\"assignments\":" + iarr(r.nodeAssignments) + "}";
    }

    static String q(String s) {
        if (s == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
            }
        }
        return b.append('"').toString();
    }

    static String num(double d) {
        if (Double.isNaN(d)) return "\"NaN\"";
        if (Double.isInfinite(d)) return d > 0 ? "\"Infinity\"" : "\"-Infinity\"";
        return Double.toString(d);
    }

    static String arr(float[] a) {
        if (a == null) return "null";
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < a.length; i++) { if (i > 0) b.append(','); b.append(num(a[i])); }
        return b.append(']').toString();
    }

    static String arr(double[] a) {
        if (a == null) return "null";
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < a.length; i++) { if (i > 0) b.append(','); b.append(num(a[i])); }
        return b.append(']').toString();
    }

    static String iarr(int[] a) {
        if (a == null) return "null";
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < a.length; i++) { if (i > 0) b.append(','); b.append(a[i]); }
        return b.append(']').toString();
    }

    static String arr(String[] a) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < a.length; i++) { if (i > 0) b.append(','); b.append(q(a[i])); }
        return b.append(']').toString();
    }

    static String mat(float[][] m) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < m.length; i++) { if (i > 0) b.append(','); b.append(arr(m[i])); }
        return b.append(']').toString();
    }

    static String imat(int[][] m) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < m.length; i++) { if (i > 0) b.append(','); b.append(iarr(m[i])); }
        return b.append(']').toString();
    }

    static String ilist(List<Integer> l) {
        if (l == null) return "null";
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) { if (i > 0) b.append(','); b.append(l.get(i)); }
        return b.append(']').toString();
    }

    static String comps(List<List<Integer>> cs) {
        if (cs == null) return "null";
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < cs.size(); i++) { if (i > 0) b.append(','); b.append(ilist(cs.get(i))); }
        return b.append(']').toString();
    }
}
