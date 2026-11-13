/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.cli;

import com.bentza.sna.Environment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.List;

/**
 * Self-describing schema and capability manifest for the Agna CLI and MCP tools.
 * Allows autonomous agents, IDE plugins, and external callers to introspect
 * supported algorithms, formats, layout models, and CLI arguments dynamically.
 */
public final class CliCapabilities {

    private CliCapabilities() {}

    public static final List<String> INPUT_FORMATS = List.of(
            "agn", "csv", "tsv", "txt", "net", "graphml", "gml", "graphson", "dl", "xls", "xlsx"
    );

    public static final List<String> OUTPUT_FORMATS = List.of(
            "agn", "csv", "tsv", "txt", "net", "graphml", "gml", "graphson", "dl", "xls", "png"
    );

    public static final List<String> LAYOUTS = List.of(
            "circular", "random", "spring", "grid", "concentric", "star",
            "hierarchical", "core-periphery", "ego-radial", "community", "layered-dag", "linear"
    );

    public static final List<String> GENERATOR_TYPES = List.of(
            "empty", "star", "circular", "full", "random-connected", "random-weighted",
            "square-lattice", "beehive", "hexagram", "watts-strogatz", "barabasi-albert",
            "sbm", "erdos-renyi", "linear", "tree"
    );

    public static final List<String> NULL_MODELS = List.of(
            "erdos-renyi", "degree-preserving", "dyad-census"
    );

    public static final List<String> TRANSFORM_OPERATIONS = List.of(
            "transpose", "symmetrize-max", "symmetrize-min", "symmetrize-average",
            "symmetrize-sum", "symmetrize-union", "symmetrize-intersect", "symmetrize-below",
            "normalize-binary", "normalize-threshold", "add-scalar", "multiply-scalar",
            "square", "zero-diagonal", "set-diagonal", "dichotomize", "slice",
            "ego", "combine", "reorder", "merge", "delete-node", "delete-nodes",
            "isolate", "merge-nodes", "remove-outsiders", "renumber", "add-nodes", "clone-node"
    );

    public static class CommandInfo {
        public final String name;
        public final String description;
        public final List<String> options;

        public CommandInfo(String name, String description, String... options) {
            this.name = name;
            this.description = description;
            this.options = Arrays.asList(options);
        }
    }

    public static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("version", "Print Agna CLI version and citation metadata"),
            new CommandInfo("info", "High-level topological summary, size, density, connectivity", "--format", "--json", "--out"),
            new CommandInfo("validate", "Pre-flight validation, format syntax, isolate and component checks", "--format", "--json", "--out"),
            new CommandInfo("explain", "Educational definitions, formulas, and interpretations for 37 metrics", "--list", "--format", "--json", "--out"),
            new CommandInfo("advise", "Intelligent topology-driven recommendations for metrics and layouts", "--format", "--json", "--out"),
            new CommandInfo("narrative", "Natural-language structural synthesis and qualitative summary", "--format", "--json", "--out"),
            new CommandInfo("capabilities", "Self-describing feature manifest and schema introspection", "--format", "--json", "--out"),
            new CommandInfo("mcp", "Start headless Model Context Protocol (MCP) JSON-RPC stdio server"),
            new CommandInfo("analyse", "Run full analysis batteries or specific metrics with rich report", "--all", "--out", "NAME..."),
            new CommandInfo("metrics", "Structured machine-readable CSV/JSON metrics with top-N ranking", "--all", "--top", "--format", "--json", "--out"),
            new CommandInfo("convert", "Convert between 10+ SNA graph formats without metadata loss", "--in-format", "--out-format"),
            new CommandInfo("transform", "Execute matrix algebra and topological transformations", "--op"),
            new CommandInfo("draw", "Render publication-ready PNG visualization with layout engine", "--out", "--layout", "--size", "--labels", "--background"),
            new CommandInfo("generate", "Generate seeded synthetic benchmark networks", "--nodes", "--out", "--type"),
            new CommandInfo("matrix", "Print network adjacency matrix as CSV or TSV", "--format", "--out"),
            new CommandInfo("nodes", "Export nodal degree and coordinate table", "--out"),
            new CommandInfo("ego", "Extract 1-hop ego network for a designated focal actor", "--node", "--out"),
            new CommandInfo("components", "Identify connected components and cluster memberships", "--out"),
            new CommandInfo("distance", "Calculate shortest path and hop route between two actors", "--from", "--to", "--out"),
            new CommandInfo("diff", "Compute structural difference between two graphs", "--out"),
            new CommandInfo("archetype", "Classify network against Small-World, Scale-Free, and Core-Periphery models", "--format", "--out"),
            new CommandInfo("benchmark", "Benchmark performance of centrality, path, and clustering algorithms", "--iterations", "--format", "--out"),
            new CommandInfo("test-null", "Monte Carlo hypothesis testing against randomized null models", "--metric", "--model", "-r", "--seed", "--format", "--out")
    );

    public static String toPlainText() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("AGNA CLI Capabilities & Feature Manifest (Version ")
          .append(Environment.getApplicationVersion()).append(")\n\n");

        sb.append("Commands (").append(COMMANDS.size()).append(" total):\n");
        for (CommandInfo c : COMMANDS) {
            sb.append(String.format("  %-14s - %s\n", c.name, c.description));
        }

        sb.append("\nSupported Graph Input Formats:  ").append(String.join(", ", INPUT_FORMATS)).append("\n");
        sb.append("Supported Graph Output Formats: ").append(String.join(", ", OUTPUT_FORMATS)).append("\n");
        sb.append("Visual Layout Engines (12):     ").append(String.join(", ", LAYOUTS)).append("\n");
        sb.append("Synthetic Graph Generators (15):").append(String.join(", ", GENERATOR_TYPES)).append("\n");
        sb.append("Hypothesis Null Models:         ").append(String.join(", ", NULL_MODELS)).append("\n");
        sb.append("Transform Matrix Operations:    ").append(String.join(", ", TRANSFORM_OPERATIONS)).append("\n");

        return sb.toString();
    }

    public static String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        ObjectNode sw = root.putObject("software");
        sw.put("name", "AGNA");
        sw.put("version", Environment.getApplicationVersion());
        sw.put("license", "Apache-2.0");
        sw.put("citationDoi", Environment.getCitationDoi());

        ObjectNode fmt = root.putObject("formats");
        ArrayNode inFmt = fmt.putArray("import");
        for (String f : INPUT_FORMATS) inFmt.add(f);
        ArrayNode outFmt = fmt.putArray("export");
        for (String f : OUTPUT_FORMATS) outFmt.add(f);

        ArrayNode cmds = root.putArray("commands");
        for (CommandInfo c : COMMANDS) {
            ObjectNode co = cmds.addObject();
            co.put("name", c.name);
            co.put("description", c.description);
            ArrayNode opt = co.putArray("flags");
            for (String o : c.options) opt.add(o);
        }

        ArrayNode ly = root.putArray("layouts");
        for (String l : LAYOUTS) ly.add(l);

        ArrayNode gen = root.putArray("generators");
        for (String g : GENERATOR_TYPES) gen.add(g);

        ArrayNode nm = root.putArray("nullModels");
        for (String m : NULL_MODELS) nm.add(m);

        ArrayNode ops = root.putArray("transforms");
        for (String op : TRANSFORM_OPERATIONS) ops.add(op);

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
