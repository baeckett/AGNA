/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.io;

import com.bentza.sna.net.Actor;
import com.bentza.sna.net.Network;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Parser and importer for UCINET DL (.dl) network data files.
 * Supports:
 * - format = fullmatrix / matrix
 * - format = edgelist1 / nodelist1
 * - labels: / labels embedded: / row labels:
 *
 * <p>Reference: Borgatti, S. P., Everett, M. G., &amp; Freeman, L. C. (2002). UCINET 6 for Windows: Software for Social Network Analysis.
 */
public final class DLImporter {

    public static Network importDL(File file) throws IOException {
        String netName = file.getName();
        if (netName.toLowerCase().endsWith(".dl")) {
            netName = netName.substring(0, netName.length() - 3);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            return parseDL(reader, netName);
        }
    }

    public static Network parseDL(BufferedReader reader, String netName) throws IOException {
        int n = -1;
        String format = "fullmatrix";
        List<String> labels = new ArrayList<>();
        boolean inData = false;
        boolean inLabels = false;

        List<String> dataLines = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                continue;
            }

            if (!inData) {
                String lower = trimmed.toLowerCase();
                if (lower.startsWith("dl")) {
                    parseHeaderTokens(trimmed, labels);
                    // check if n is in the same line
                    int parsedN = extractN(lower);
                    if (parsedN > 0) n = parsedN;
                    if (lower.contains("edgelist")) format = "edgelist1";
                    else if (lower.contains("nodelist")) format = "nodelist1";
                    continue;
                }
                if (lower.startsWith("n=") || lower.startsWith("n =") || lower.startsWith("n\t") || lower.startsWith("nr=") || lower.startsWith("nr =")) {
                    int parsedN = extractN(lower);
                    if (parsedN > 0) n = parsedN;
                    continue;
                }
                if (lower.startsWith("format=") || lower.startsWith("format =")) {
                    if (lower.contains("edgelist")) format = "edgelist1";
                    else if (lower.contains("nodelist")) format = "nodelist1";
                    else format = "fullmatrix";
                    continue;
                }
                if (lower.startsWith("labels:") || lower.startsWith("labels embedded:") || lower.startsWith("row labels:")) {
                    inLabels = true;
                    // Labels might start on the same line after colon
                    int colonIdx = trimmed.indexOf(':');
                    if (colonIdx >= 0 && colonIdx < trimmed.length() - 1) {
                        String rest = trimmed.substring(colonIdx + 1).trim();
                        if (!rest.isEmpty()) {
                            parseLabels(rest, labels);
                        }
                    }
                    continue;
                }
                if (lower.startsWith("data:")) {
                    inData = true;
                    inLabels = false;
                    int colonIdx = trimmed.indexOf(':');
                    if (colonIdx >= 0 && colonIdx < trimmed.length() - 1) {
                        String rest = trimmed.substring(colonIdx + 1).trim();
                        if (!rest.isEmpty()) dataLines.add(rest);
                    }
                    continue;
                }
                if (inLabels) {
                    parseLabels(trimmed, labels);
                    continue;
                }
            } else {
                dataLines.add(trimmed);
            }
        }

        // If N was not specified, infer from labels or edgelist/nodelist
        if ("edgelist1".equals(format)) {
            return parseEdgeList(dataLines, labels, netName, n);
        } else if ("nodelist1".equals(format)) {
            return parseNodeList(dataLines, labels, netName, n);
        } else {
            return parseMatrixData(dataLines, labels, netName, n);
        }
    }

    private static final java.util.regex.Pattern N_PATTERN =
            java.util.regex.Pattern.compile("\\b(n|nr)\\s*=\\s*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);

    private static void parseHeaderTokens(String line, List<String> labels) {
        // token parsing is handled by extractN
    }

    private static int extractN(String line) {
        java.util.regex.Matcher m = N_PATTERN.matcher(line);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(2));
            } catch (Exception ignored) {}
        }
        return -1;
    }

    private static List<String> tokenizeQuotedLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null || line.trim().isEmpty()) return tokens;
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == quoteChar) {
                    inQuotes = false;
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"' || c == '\'') {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == ',' || Character.isWhitespace(c)) {
                    if (current.length() > 0) {
                        tokens.add(current.toString().trim());
                        current.setLength(0);
                    }
                } else {
                    current.append(c);
                }
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString().trim());
        }
        return tokens;
    }

    private static void parseLabels(String text, List<String> labels) {
        List<String> tokens = tokenizeQuotedLine(text);
        for (String tok : tokens) {
            if (!tok.equalsIgnoreCase("data:")) {
                labels.add(tok);
            }
        }
    }

    private static Network parseMatrixData(List<String> dataLines, List<String> labels, String netName, int n) {
        List<Float> values = new ArrayList<>();
        for (String dl : dataLines) {
            StringTokenizer st = new StringTokenizer(dl, " ,\t");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                try {
                    values.add(Float.parseFloat(token));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (n <= 0) {
            n = labels.size();
            if (n <= 0) {
                n = (int) Math.round(Math.sqrt(values.size()));
            }
        }

        Network net = new Network(n);
        net.setName(netName);
        for (int i = 0; i < n; i++) {
            if (i < labels.size()) {
                net.getActor(i).setName(labels.get(i));
            } else {
                net.getActor(i).setName(String.valueOf(i + 1));
            }
        }

        int idx = 0;
        for (int i = 0; i < n && idx < values.size(); i++) {
            for (int j = 0; j < n && idx < values.size(); j++) {
                net.setValue(values.get(idx++), i, j);
            }
        }
        return net;
    }

    private static Network parseEdgeList(List<String> dataLines, List<String> labels, String netName, int n) {
        Map<String, Integer> nameToIdx = new HashMap<>();
        List<String> actorNames = new ArrayList<>();

        for (String lbl : labels) {
            if (!nameToIdx.containsKey(lbl)) {
                nameToIdx.put(lbl, actorNames.size());
                actorNames.add(lbl);
            }
        }

        List<String[]> parsedEdges = new ArrayList<>();
        for (String dl : dataLines) {
            List<String> tokens = tokenizeQuotedLine(dl);
            if (tokens.size() >= 2) {
                String from = tokens.get(0);
                String to = tokens.get(1);
                String weight = tokens.size() > 2 ? tokens.get(2) : "1.0";
                parsedEdges.add(new String[]{from, to, weight});

                if (!nameToIdx.containsKey(from)) {
                    nameToIdx.put(from, actorNames.size());
                    actorNames.add(from);
                }
                if (!nameToIdx.containsKey(to)) {
                    nameToIdx.put(to, actorNames.size());
                    actorNames.add(to);
                }
            }
        }

        int finalN = Math.max(n, actorNames.size());
        Network net = new Network(finalN);
        net.setName(netName);
        for (int i = 0; i < finalN; i++) {
            if (i < actorNames.size()) {
                net.getActor(i).setName(actorNames.get(i));
            } else {
                net.getActor(i).setName(String.valueOf(i + 1));
            }
        }

        for (String[] edge : parsedEdges) {
            int u = nameToIdx.get(edge[0]);
            int v = nameToIdx.get(edge[1]);
            float w = 1.0f;
            try {
                w = Float.parseFloat(edge[2]);
            } catch (Exception ignored) {}
            net.setValue(w, u, v);
        }
        return net;
    }

    private static Network parseNodeList(List<String> dataLines, List<String> labels, String netName, int n) {
        Map<String, Integer> nameToIdx = new HashMap<>();
        List<String> actorNames = new ArrayList<>();

        for (String lbl : labels) {
            if (!nameToIdx.containsKey(lbl)) {
                nameToIdx.put(lbl, actorNames.size());
                actorNames.add(lbl);
            }
        }

        List<String[]> parsedEdges = new ArrayList<>();
        for (String dl : dataLines) {
            List<String> tokens = tokenizeQuotedLine(dl);
            if (tokens.size() >= 2) {
                String from = tokens.get(0);
                if (!nameToIdx.containsKey(from)) {
                    nameToIdx.put(from, actorNames.size());
                    actorNames.add(from);
                }
                for (int t = 1; t < tokens.size(); t++) {
                    String to = tokens.get(t);
                    if (!nameToIdx.containsKey(to)) {
                        nameToIdx.put(to, actorNames.size());
                        actorNames.add(to);
                    }
                    parsedEdges.add(new String[]{from, to, "1.0"});
                }
            }
        }

        int finalN = Math.max(n, actorNames.size());
        Network net = new Network(finalN);
        net.setName(netName);
        for (int i = 0; i < finalN; i++) {
            if (i < actorNames.size()) {
                net.getActor(i).setName(actorNames.get(i));
            } else {
                net.getActor(i).setName(String.valueOf(i + 1));
            }
        }

        for (String[] edge : parsedEdges) {
            int u = nameToIdx.get(edge[0]);
            int v = nameToIdx.get(edge[1]);
            net.setValue(1.0f, u, v);
        }
        return net;
    }
}
