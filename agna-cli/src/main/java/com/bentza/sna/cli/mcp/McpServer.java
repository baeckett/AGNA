/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.cli.mcp;

import com.bentza.sna.Environment;
import com.bentza.sna.cli.Cli;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Headless Model Context Protocol (MCP) JSON-RPC 2.0 stdio server for Agna.
 * Enables zero-setup, first-class discovery and invocation by AI agents,
 * IDE assistants (Cursor, Copilot, Claude Desktop, Antigravity), and LLM tools.
 */
public final class McpServer {

    public static final String PROTOCOL_VERSION = "2024-11-05";
    public static final String SERVER_NAME = "agna";

    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;
    private final ObjectMapper mapper;

    public McpServer(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.out = out;
        this.err = err;
        this.mapper = new ObjectMapper();
    }

    /**
     * Execution loop: reads JSON-RPC requests from standard input,
     * dispatches tools, and writes JSON-RPC responses to standard output.
     */
    public int run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // Support both Newline-Delimited JSON and Content-Length header framing
                String jsonPayload = line;
                if (line.regionMatches(true, 0, "Content-Length:", 0, 15)) {
                    int length = Integer.parseInt(line.substring(15).trim());
                    // Skip optional blank line
                    reader.readLine();
                    char[] buffer = new char[length];
                    int read = 0;
                    while (read < length) {
                        int r = reader.read(buffer, read, length - read);
                        if (r == -1) break;
                        read += r;
                    }
                    jsonPayload = new String(buffer, 0, read);
                }

                if (jsonPayload.isEmpty() || !jsonPayload.startsWith("{")) {
                    continue;
                }

                JsonNode root;
                try {
                    root = mapper.readTree(jsonPayload);
                } catch (Exception e) {
                    sendParseError(e.getMessage());
                    continue;
                }

                if (!root.isObject()) {
                    continue;
                }

                JsonNode idNode = root.get("id");
                JsonNode methodNode = root.get("method");
                if (methodNode == null || !methodNode.isTextual()) {
                    if (idNode != null) {
                        sendError(idNode, -32600, "Invalid Request: missing method");
                    }
                    continue;
                }

                String method = methodNode.asText();
                JsonNode params = root.get("params");

                // Handle notifications (no id -> no response)
                if (idNode == null || idNode.isNull()) {
                    handleNotification(method, params);
                    continue;
                }

                // Handle standard request methods
                handleRequest(idNode, method, params);
            }
        } catch (IOException e) {
            err.println("Agna MCP Server IO error: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    private void handleNotification(String method, JsonNode params) {
        // "notifications/initialized" and other client lifecycle notices
        if ("notifications/initialized".equals(method)) {
            err.println("Agna MCP client connected and initialized.");
        }
    }

    private void handleRequest(JsonNode id, String method, JsonNode params) {
        switch (method) {
            case "initialize":
                handleInitialize(id, params);
                break;
            case "ping":
                handlePing(id);
                break;
            case "tools/list":
                handleToolsList(id);
                break;
            case "tools/call":
                handleToolsCall(id, params);
                break;
            case "resources/list":
                handleEmptyList(id, "resources");
                break;
            case "prompts/list":
                handleEmptyList(id, "prompts");
                break;
            default:
                sendError(id, -32601, "Method not found: " + method);
                break;
        }
    }

    private void handleInitialize(JsonNode id, JsonNode params) {
        ObjectNode res = mapper.createObjectNode();
        res.put("protocolVersion", PROTOCOL_VERSION);

        ObjectNode caps = res.putObject("capabilities");
        caps.putObject("tools");

        ObjectNode serverInfo = res.putObject("serverInfo");
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", Environment.getApplicationVersion());

        sendResult(id, res);
    }

    private void handlePing(JsonNode id) {
        sendResult(id, mapper.createObjectNode());
    }

    private void handleEmptyList(JsonNode id, String key) {
        ObjectNode res = mapper.createObjectNode();
        res.putArray(key);
        sendResult(id, res);
    }

    private void handleToolsList(JsonNode id) {
        ObjectNode res = mapper.createObjectNode();
        ArrayNode tools = res.putArray("tools");

        // 1. agna_validate
        ObjectNode tVal = tools.addObject();
        tVal.put("name", "agna_validate");
        tVal.put("description", "Validates network data file for syntactic correctness, matrix squareness, isolates, density, and connected components.");
        ObjectNode sVal = tVal.putObject("inputSchema");
        sVal.put("type", "object");
        ObjectNode pVal = sVal.putObject("properties");
        pVal.putObject("file").put("type", "string").put("description", "Path to network file (e.g. .agn, .net, .csv, .graphml)");
        sVal.putArray("required").add("file");

        // 2. agna_info
        ObjectNode tInfo = tools.addObject();
        tInfo.put("name", "agna_info");
        tInfo.put("description", "Retrieves high-level topological metadata in JSON: order, size, density, connectivity, reciprocity, and isolates.");
        ObjectNode sInfo = tInfo.putObject("inputSchema");
        sInfo.put("type", "object");
        ObjectNode pInfo = sInfo.putObject("properties");
        pInfo.putObject("file").put("type", "string").put("description", "Path to network file");
        sInfo.putArray("required").add("file");

        // 3. agna_metrics
        ObjectNode tMet = tools.addObject();
        tMet.put("name", "agna_metrics");
        tMet.put("description", "Computes centrality and structural metrics (betweenness, degree, closeness, eigenvector, Burt holes, etc.) with optional top-N ranking and summary statistics.");
        ObjectNode sMet = tMet.putObject("inputSchema");
        sMet.put("type", "object");
        ObjectNode pMet = sMet.putObject("properties");
        pMet.putObject("file").put("type", "string").put("description", "Path to network file");
        pMet.putObject("metric").put("type", "string").put("description", "Metric identifier (e.g. betweenness, nodal_degree, closeness, structural_holes) or 'all'");
        pMet.putObject("top").put("type", "integer").put("description", "Limit results to top N highest-ranking nodes with summary statistics (mean, stdDev, min, max)");
        sMet.putArray("required").add("file");

        // 4. agna_advise
        ObjectNode tAdv = tools.addObject();
        tAdv.put("name", "agna_advise");
        tAdv.put("description", "Inspects network topology and recommends methodologically sound SNA metrics, visual layouts, and null models with sociological rationales.");
        ObjectNode sAdv = tAdv.putObject("inputSchema");
        sAdv.put("type", "object");
        ObjectNode pAdv = sAdv.putObject("properties");
        pAdv.putObject("file").put("type", "string").put("description", "Path to network file");
        sAdv.putArray("required").add("file");

        // 5. agna_narrative
        ObjectNode tNar = tools.addObject();
        tNar.put("name", "agna_narrative");
        tNar.put("description", "Synthesizes an academically cautious structural narrative covering density, reachability, centralization, and clustering.");
        ObjectNode sNar = tNar.putObject("inputSchema");
        sNar.put("type", "object");
        ObjectNode pNar = sNar.putObject("properties");
        pNar.putObject("file").put("type", "string").put("description", "Path to network file");
        pNar.putObject("format").put("type", "string").put("description", "Output format ('json' for structured JSON, 'text' for Markdown narrative)");
        sNar.putArray("required").add("file");

        // 6. agna_explain
        ObjectNode tExp = tools.addObject();
        tExp.put("name", "agna_explain");
        tExp.put("description", "Provides educational definitions, mathematical formulas, and cautious sociological interpretations for 37 SNA metrics.");
        ObjectNode sExp = tExp.putObject("inputSchema");
        sExp.put("type", "object");
        ObjectNode pExp = sExp.putObject("properties");
        pExp.putObject("metric").put("type", "string").put("description", "Specific metric identifier (e.g. betweenness, transitivity, structural_holes) or omit/'list' to list all");

        // 7. agna_convert
        ObjectNode tConv = tools.addObject();
        tConv.put("name", "agna_convert");
        tConv.put("description", "Converts between SNA graph formats (agn, csv, tsv, net, graphml, gml, graphson, dl) without loss of metadata.");
        ObjectNode sConv = tConv.putObject("inputSchema");
        sConv.put("type", "object");
        ObjectNode pConv = sConv.putObject("properties");
        pConv.putObject("inFile").put("type", "string").put("description", "Source network file path");
        pConv.putObject("outFile").put("type", "string").put("description", "Destination file path");
        pConv.putObject("inFormat").put("type", "string").put("description", "Optional explicit input format");
        pConv.putObject("outFormat").put("type", "string").put("description", "Optional explicit output format");
        sConv.putArray("required").add("inFile").add("outFile");

        // 8. agna_archetype
        ObjectNode tArch = tools.addObject();
        tArch.put("name", "agna_archetype");
        tArch.put("description", "Classifies empirical graph topology against Small-World (Watts-Strogatz), Scale-Free (Barabasi-Albert), and Core-Periphery models.");
        ObjectNode sArch = tArch.putObject("inputSchema");
        sArch.put("type", "object");
        ObjectNode pArch = sArch.putObject("properties");
        pArch.putObject("file").put("type", "string").put("description", "Path to network file");
        sArch.putArray("required").add("file");

        // 9. agna_test_null
        ObjectNode tNull = tools.addObject();
        tNull.put("name", "agna_test_null");
        tNull.put("description", "Runs Monte Carlo permutation hypothesis tests against random null models (degree-preserving, erdos-renyi, dyad-census).");
        ObjectNode sNull = tNull.putObject("inputSchema");
        sNull.put("type", "object");
        ObjectNode pNull = sNull.putObject("properties");
        pNull.putObject("file").put("type", "string").put("description", "Path to network file");
        pNull.putObject("metric").put("type", "string").put("description", "Metric to test (e.g., transitivity, reciprocity, density)");
        pNull.putObject("model").put("type", "string").put("description", "Null model type ('degree-preserving', 'erdos-renyi', 'dyad-census')");
        pNull.putObject("permutations").put("type", "integer").put("description", "Number of random permutations (default: 500)");
        sNull.putArray("required").add("file").add("metric");

        // 10. agna_capabilities
        ObjectNode tCap = tools.addObject();
        tCap.put("name", "agna_capabilities");
        tCap.put("description", "Introspects all supported graph formats, algorithms, layout engines, generators, and null models.");
        ObjectNode sCap = tCap.putObject("inputSchema");
        sCap.put("type", "object");
        sCap.putObject("properties");

        sendResult(id, res);
    }

    private void handleToolsCall(JsonNode id, JsonNode params) {
        if (params == null || !params.isObject() || params.get("name") == null) {
            sendError(id, -32602, "Invalid params: missing tool 'name'");
            return;
        }

        String name = params.get("name").asText();
        JsonNode args = params.get("arguments");
        if (args == null || !args.isObject()) {
            args = mapper.createObjectNode();
        }

        try {
            ExecutionResult result;
            switch (name) {
                case "agna_validate": {
                    String file = requireString(args, "file");
                    result = executeCli("validate", file, "--json");
                    break;
                }
                case "agna_info": {
                    String file = requireString(args, "file");
                    result = executeCli("info", file, "--json");
                    break;
                }
                case "agna_metrics": {
                    String file = requireString(args, "file");
                    String metric = args.has("metric") ? args.get("metric").asText() : "all";
                    List<String> cliArgs = new ArrayList<>();
                    cliArgs.add("metrics");
                    cliArgs.add(file);
                    cliArgs.add(metric);
                    if (args.has("top") && !args.get("top").isNull()) {
                        cliArgs.add("--top");
                        cliArgs.add(String.valueOf(args.get("top").asInt()));
                    }
                    cliArgs.add("--json");
                    result = executeCli(cliArgs.toArray(new String[0]));
                    break;
                }
                case "agna_advise": {
                    String file = requireString(args, "file");
                    result = executeCli("advise", file, "--json");
                    break;
                }
                case "agna_narrative": {
                    String file = requireString(args, "file");
                    String format = args.has("format") ? args.get("format").asText() : "json";
                    if ("text".equalsIgnoreCase(format)) {
                        result = executeCli("narrative", file);
                    } else {
                        result = executeCli("narrative", file, "--json");
                    }
                    break;
                }
                case "agna_explain": {
                    String metric = args.has("metric") ? args.get("metric").asText() : null;
                    if (metric == null || metric.isEmpty() || "list".equalsIgnoreCase(metric)) {
                        result = executeCli("explain", "--list", "--json");
                    } else {
                        result = executeCli("explain", metric, "--json");
                    }
                    break;
                }
                case "agna_convert": {
                    String inFile = requireString(args, "inFile");
                    String outFile = requireString(args, "outFile");
                    List<String> cliArgs = new ArrayList<>();
                    cliArgs.add("convert");
                    cliArgs.add(inFile);
                    cliArgs.add(outFile);
                    if (args.has("inFormat")) {
                        cliArgs.add("--in-format");
                        cliArgs.add(args.get("inFormat").asText());
                    }
                    if (args.has("outFormat")) {
                        cliArgs.add("--out-format");
                        cliArgs.add(args.get("outFormat").asText());
                    }
                    result = executeCli(cliArgs.toArray(new String[0]));
                    break;
                }
                case "agna_archetype": {
                    String file = requireString(args, "file");
                    result = executeCli("archetype", file, "--format", "json");
                    break;
                }
                case "agna_test_null": {
                    String file = requireString(args, "file");
                    String metric = requireString(args, "metric");
                    String model = args.has("model") ? args.get("model").asText() : "degree-preserving";
                    int reps = args.has("permutations") ? args.get("permutations").asInt() : 500;
                    result = executeCli("test-null", file, "--metric", metric, "--model", model, "-r", String.valueOf(reps), "--format", "json");
                    break;
                }
                case "agna_capabilities": {
                    result = executeCli("capabilities", "--json");
                    break;
                }
                default:
                    sendError(id, -32601, "Unknown tool: " + name);
                    return;
            }

            ObjectNode res = mapper.createObjectNode();
            ArrayNode content = res.putArray("content");
            ObjectNode textContent = content.addObject();
            textContent.put("type", "text");

            boolean isError = result.exitCode != 0;
            String text = result.stdout;
            if (isError && (text == null || text.isEmpty())) {
                text = result.stderr.isEmpty() ? ("Tool failed with exit code " + result.exitCode) : result.stderr;
            }
            textContent.put("text", text);
            res.put("isError", isError);

            sendResult(id, res);
        } catch (IllegalArgumentException e) {
            sendToolError(id, e.getMessage());
        } catch (Exception e) {
            sendToolError(id, "Execution error: " + (e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    private String requireString(JsonNode args, String name) {
        if (!args.has(name) || args.get(name).isNull() || args.get(name).asText().trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: " + name);
        }
        return args.get(name).asText().trim();
    }

    private void sendToolError(JsonNode id, String message) {
        ObjectNode res = mapper.createObjectNode();
        ArrayNode content = res.putArray("content");
        ObjectNode textContent = content.addObject();
        textContent.put("type", "text");
        textContent.put("text", message);
        res.put("isError", true);
        sendResult(id, res);
    }

    public static class ExecutionResult {
        public final int exitCode;
        public final String stdout;
        public final String stderr;

        public ExecutionResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }

    public ExecutionResult executeCli(String... args) {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
        PrintStream subOut = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
        PrintStream subErr = new PrintStream(errBytes, true, StandardCharsets.UTF_8);

        Cli cli = new Cli(subOut, this.in);
        try {
            int code = cli.run(args);
            return new ExecutionResult(code, outBytes.toString(StandardCharsets.UTF_8).trim(), errBytes.toString(StandardCharsets.UTF_8).trim());
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            return new ExecutionResult(1, outBytes.toString(StandardCharsets.UTF_8).trim(), msg);
        }
    }

    private void sendResult(JsonNode id, JsonNode result) {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("result", result);
        sendResponse(response);
    }

    private void sendError(JsonNode id, int code, String message) {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        ObjectNode errNode = response.putObject("error");
        errNode.put("code", code);
        errNode.put("message", message);
        sendResponse(response);
    }

    private void sendParseError(String detail) {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.putNull("id");
        ObjectNode errNode = response.putObject("error");
        errNode.put("code", -32700);
        errNode.put("message", "Parse error: " + detail);
        sendResponse(response);
    }

    private void sendResponse(ObjectNode response) {
        try {
            String json = mapper.writeValueAsString(response);
            synchronized (out) {
                out.print(json);
                out.print("\n");
                out.flush();
            }
        } catch (Exception e) {
            err.println("Agna MCP Server response serialization error: " + e.getMessage());
        }
    }
}
