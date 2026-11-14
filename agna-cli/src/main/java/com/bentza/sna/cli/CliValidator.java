/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.cli;

import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pre-flight network validation and actionable diagnostics for the Agna CLI.
 * Enables AI agents and automated pipelines to verify input data integrity,
 * detect structural anomalies, and avoid computational runtime failures.
 */
public final class CliValidator {

    public static class ValidationReport {
        public boolean valid;
        public String file;
        public String format;
        public int nodes;
        public int edges;
        public double density;
        public boolean directed;
        public boolean weighted;
        public boolean connected;
        public int components;
        public int selfLoops;
        public List<String> isolates = new ArrayList<>();
        public List<String> outsiders = new ArrayList<>();
        public List<String> diagnostics = new ArrayList<>();
        public String error;
        public String hint;

        public String toPlainText() {
            StringBuilder sb = new StringBuilder(512);
            if (!valid) {
                sb.append("Agna Network Validation: FAILED\n");
                sb.append("File:  ").append(file).append("\n");
                sb.append("Error: ").append(error).append("\n");
                if (hint != null) {
                    sb.append("Hint:  ").append(hint).append("\n");
                }
                return sb.toString();
            }

            sb.append("Agna Network Validation: PASSED\n");
            sb.append("File:        ").append(file).append(" (format: ").append(format).append(")\n");
            sb.append("Nodes:       ").append(nodes).append("\n");
            sb.append("Edges:       ").append(edges).append("\n");
            sb.append(String.format(Locale.ROOT, "Density:     %.4f\n", density));
            sb.append("Directed:    ").append(directed ? "Yes (asymmetric)" : "No (symmetric)").append("\n");
            sb.append("Weighted:    ").append(weighted ? "Yes (continuous/valued)" : "No (binary 0/1)").append("\n");
            sb.append("Connected:   ").append(connected ? "Yes (single component)" : ("No (" + components + " components)")).append("\n");
            sb.append("Self-loops:  ").append(selfLoops).append("\n");
            sb.append("Isolates:    ").append(isolates.size()).append(isolates.isEmpty() ? "" : (" (" + String.join(", ", isolates) + ")")).append("\n");
            if (!outsiders.isEmpty()) {
                sb.append("Outsiders:   ").append(outsiders.size()).append(" (").append(String.join(", ", outsiders)).append(")\n");
            }
            if (diagnostics.isEmpty()) {
                sb.append("Diagnostics: None (Clean structure)\n");
            } else {
                sb.append("Diagnostics:\n");
                for (String d : diagnostics) {
                    sb.append("  - ").append(d).append("\n");
                }
            }
            return sb.toString();
        }

        public String toJson() {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            root.put("valid", valid);
            root.put("file", file != null ? file : "");
            if (!valid) {
                root.put("error", error != null ? error : "Validation failed");
                if (hint != null) root.put("hint", hint);
                try {
                    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
                } catch (Exception e) {
                    return "{\"valid\":false,\"error\":\"" + escape(error) + "\"}";
                }
            }

            root.put("format", format != null ? format : "");
            root.put("nodes", nodes);
            root.put("edges", edges);
            root.put("density", density);
            root.put("directed", directed);
            root.put("weighted", weighted);
            root.put("connected", connected);
            root.put("components", components);
            root.put("selfLoops", selfLoops);

            ArrayNode iso = root.putArray("isolates");
            for (String s : isolates) iso.add(s);

            ArrayNode out = root.putArray("outsiders");
            for (String s : outsiders) out.add(s);

            ArrayNode diag = root.putArray("diagnostics");
            for (String s : diagnostics) diag.add(s);

            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            } catch (Exception e) {
                return "{\"valid\":false,\"error\":\"" + escape(e.getMessage()) + "\"}";
            }
        }

        private static String escape(String s) {
            if (s == null) return "";
            return s.replace("\"", "\\\"").replace("\n", " ");
        }
    }

    private CliValidator() {}

    /**
     * Validates a network file, checks syntax and structural anomalies.
     */
    public static ValidationReport validate(String filePath, Cli cli) {
        ValidationReport report = new ValidationReport();
        report.file = filePath;

        if (filePath == null || filePath.trim().isEmpty()) {
            report.valid = false;
            report.error = "No file path provided";
            report.hint = "Specify a network file: agna validate <file>";
            return report;
        }

        File f = new File(filePath);
        if (!"-".equals(filePath) && !f.exists()) {
            report.valid = false;
            report.error = "File does not exist: " + filePath;
            report.hint = "Verify the file path. Example: agna validate samples/example2.agn";
            return report;
        }

        FullNet full;
        try {
            full = cli.open(filePath);
        } catch (Exception e) {
            report.valid = false;
            report.error = "Failed to parse network file: " + e.getMessage();
            report.hint = "Check file encoding and syntax. Supported formats: agn, csv, txt, net (Pajek), graphml, gml, graphson, dl, xls.";
            return report;
        }

        Network net = full.getNetwork();
        if (net == null) {
            report.valid = false;
            report.error = "File opened but produced no active network";
            report.hint = "Ensure file contains valid relational matrix or edge definitions.";
            return report;
        }

        report.valid = true;
        report.format = Cli.extOf(filePath);
        report.nodes = net.getSize();
        report.edges = net.getEdgesNumber();
        report.directed = !net.isSymmetric();

        AgnaLib lib = new AgnaLib();
        int n = net.getSize();

        if (n < 2) {
            report.density = 0.0;
            report.diagnostics.add("Network contains fewer than 2 actors. Most SNA algorithms require at least 2 actors.");
            return report;
        }

        report.density = lib.density(net);

        // Check self-loops & weights
        int loops = 0;
        boolean hasWeights = false;
        for (int i = 0; i < n; i++) {
            float selfVal = net.getValue(i, i);
            if (selfVal != 0f) {
                loops++;
            }
            for (int j = 0; j < n; j++) {
                float v = net.getValue(i, j);
                if (v != 0f && v != 1f) {
                    hasWeights = true;
                }
            }
        }
        report.selfLoops = loops;
        report.weighted = hasWeights;

        // Check isolates
        for (int i = 0; i < n; i++) {
            boolean active = false;
            for (int j = 0; j < n; j++) {
                if (net.getValue(i, j) != 0f || net.getValue(j, i) != 0f) {
                    active = true;
                    break;
                }
            }
            if (!active) {
                report.isolates.add(net.getActor(i).getName());
            }
        }

        // Connectivity & Components
        report.connected = lib.isConnected(net);
        report.components = countComponents(net);

        // Outsiders
        for (int i = 0; i < n; i++) {
            if (net.isOutsider(i)) {
                report.outsiders.add(net.getActor(i).getName());
            }
        }

        // Construct diagnostics
        if (report.selfLoops > 0) {
            report.diagnostics.add(String.format(Locale.ROOT,
                    "Found %d self-loop(s). Self-loops can inflate degree and bias path-based centralities.",
                    report.selfLoops));
        }
        if (!report.isolates.isEmpty()) {
            report.diagnostics.add(String.format(Locale.ROOT,
                    "Found %d isolated node(s) with degree 0: %s",
                    report.isolates.size(),
                    report.isolates.subList(0, Math.min(5, report.isolates.size()))));
        }
        if (!report.connected) {
            report.diagnostics.add(String.format(Locale.ROOT,
                    "Graph is disconnected into %d components. Distance, closeness, and diameter metrics will be undefined across disconnected pairs.",
                    report.components));
        } else if (report.directed && !lib.isStronglyConnected(net)) {
            report.diagnostics.add("Network is directed and not strongly connected (some directed paths are one-way only).");
        }
        if (report.weighted) {
            report.diagnostics.add("Network contains valued/weighted ties. Binary-only algorithms will treat non-zero ties as 1.");
        }
        if (report.density > 0.85) {
            report.diagnostics.add("Graph is extremely dense (>0.85). Path lengths are trivially short and clustering may be saturated.");
        } else if (report.density == 0.0) {
            report.diagnostics.add("Graph has zero edges.");
        }

        return report;
    }

    public static int countComponents(Network net) {
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
