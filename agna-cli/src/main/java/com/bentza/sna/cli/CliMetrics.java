package com.bentza.sna.cli;

import com.bentza.sna.net.Network;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 2.1.3: structured metrics for the CLI. Values follow the standard
 * definitions used in the SNA literature (and Agna's conventions: a
 * geodesic of 0 means no path; the distance-based measures refuse
 * disconnected networks). The analyse command keeps printing Agna's own
 * rich report; this class produces the machine-readable CSV/JSON
 * counterpart with one row per metric, node or pair.
 */
public final class CliMetrics
    {
    private static final String HEADER = "metric,node1,node2,value";

    private CliMetrics()
        {
        }

    /** true when every node is reachable from every other (undirected) */
    public static boolean isConnected(Network net)
        {
        int n = net.getSize();
        if (n < 2)
            {
            return true;
            }
        boolean[] seen = new boolean[n];
        List<Integer> queue = new ArrayList<>();
        queue.add(0);
        seen[0] = true;
        while (!queue.isEmpty())
            {
            int v = queue.remove(queue.size() - 1);
            for (int w = 0; w < n; w++)
                {
                if (!seen[w] && (net.getValue(v, w) != 0f
                        || net.getValue(w, v) != 0f))
                    {
                    seen[w] = true;
                    queue.add(w);
                    }
                }
            }
        for (boolean s : seen)
            {
            if (!s)
                {
                return false;
                }
            }
        return true;
        }

    /** all-pairs hop distances (BFS); 0 means no path (Agna convention) */
    private static int[][] hops(Network net)
        {
        int n = net.getSize();
        int[][] d = new int[n][n];
        for (int s = 0; s < n; s++)
            {
            Arrays.fill(d[s], 0);
            int[] dist = new int[n];
            Arrays.fill(dist, -1);
            dist[s] = 0;
            List<Integer> queue = new ArrayList<>();
            queue.add(s);
            while (!queue.isEmpty())
                {
                int v = queue.remove(queue.size() - 1);
                for (int w = 0; w < n; w++)
                    {
                    if (dist[w] == -1 && net.getValue(v, w) != 0f)
                        {
                        dist[w] = dist[v] + 1;
                        queue.add(w);
                        }
                    }
                }
            for (int i = 0; i < n; i++)
                {
                d[s][i] = dist[i] == -1 ? 0 : dist[i];
                }
            }
        return d;
        }

    /** Brandes algorithm: dependency accumulation on shortest paths */
    private static float[] betweenness(Network net)
        {
        int n = net.getSize();
        float[] bet = new float[n];
        for (int s = 0; s < n; s++)
            {
            int[] dist = new int[n];
            Arrays.fill(dist, -1);
            float[] sigma = new float[n];
            sigma[s] = 1f;
            dist[s] = 0;
            List<Integer> order = new ArrayList<>();
            List<Integer> queue = new ArrayList<>();
            queue.add(s);
            List<List<Integer>> pred = new ArrayList<>();
            for (int i = 0; i < n; i++)
                {
                pred.add(new ArrayList<>());
                }
            while (!queue.isEmpty())
                {
                int v = queue.remove(queue.size() - 1);
                order.add(v);
                for (int w = 0; w < n; w++)
                    {
                    if (net.getValue(v, w) == 0f)
                        {
                        continue;
                        }
                    if (dist[w] == -1)
                        {
                        dist[w] = dist[v] + 1;
                        queue.add(w);
                        pred.get(w).add(v);
                        sigma[w] = sigma[v];
                        } else if (dist[w] == dist[v] + 1)
                        {
                        pred.get(w).add(v);
                        sigma[w] += sigma[v];
                        }
                    }
                }
            float[] delta = new float[n];
            for (int i = order.size() - 1; i >= 0; i--)
                {
                int w = order.get(i);
                for (int v : pred.get(w))
                    {
                    delta[v] += (sigma[v] / sigma[w]) * (1f + delta[w]);
                    }
                if (w != s)
                    {
                    bet[w] += delta[w];
                    }
                }
            }
        return bet;
        }

    /**
     * CSV lines with the uniform header metric,node1,node2,value.
     * Scalar metrics use empty node cells; node metrics one node; pair
     * metrics (geodesics) both. Distance-based measures are skipped for
     * disconnected networks. When wanted is non-null only the named
     * metrics are emitted.
     */
    public static List<String> csv(Network net)
        {
        return csv(net, null);
        }

    public static List<String> csv(Network net, java.util.Set<String> wanted)
        {
        List<String> rows = new ArrayList<>();
        rows.add(HEADER);
        int n = net.getSize();
        boolean connected = isConnected(net);

        // density: arcs / (n * (n-1)), Agna's primary convention
        double density = n > 1 ? arcs(net) / (double) (n * (n - 1)) : 0.0;
        rows.add(row("density", "", "", density));

        int[][] hops = hops(net);
        if (connected)
            {
            int diameter = 0;
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    diameter = Math.max(diameter, hops[i][j]);
                    }
                }
            rows.add(row("diameter", "", "", diameter));
            for (int i = 0; i < n; i++)
                {
                // eccentricity: longest reachable path (isolates: 0)
                int ecc = 0;
                for (int j = 0; j < n; j++)
                    {
                    ecc = Math.max(ecc, hops[i][j]);
                    }
                rows.add(row("eccentricity", node(net, i), "", ecc));
                }
            for (int i = 0; i < n; i++)
                {
                double reach = 0.0;
                for (int j = 0; j < n; j++)
                    {
                    if (i != j && hops[i][j] > 0)
                        {
                        reach += hops[i][j];
                        }
                    }
                // closeness: (n-1) / sum of reachable distances
                double closeness = reach > 0 ? (n - 1.0) / reach : 0.0;
                rows.add(row("closeness", node(net, i), "", closeness));
                }
            float[] bet = betweenness(net);
            for (int i = 0; i < n; i++)
                {
                rows.add(row("betweenness", node(net, i), "", bet[i]));
                }
            }

        for (int i = 0; i < n; i++)
            {
            int in = 0;
            int out = 0;
            double emission = 0.0;
            double reception = 0.0;
            for (int j = 0; j < n; j++)
                {
                float vOut = net.getValue(i, j);
                float vIn = net.getValue(j, i);
                if (vOut != 0f)
                    {
                    out++;
                    emission += vOut;
                    }
                if (vIn != 0f)
                    {
                    in++;
                    reception += vIn;
                    }
                }
            rows.add(row("indegree", node(net, i), "", in));
            rows.add(row("outdegree", node(net, i), "", out));
            rows.add(row("total-degree", node(net, i), "", in + out));
            rows.add(row("emission", node(net, i), "", emission));
            rows.add(row("reception", node(net, i), "", reception));
            rows.add(row("status", node(net, i), "", reception - emission));
            double den = emission + reception;
            rows.add(row("determination", node(net, i), "",
                    den == 0 ? 0.0 : (emission - reception) / den));
            }

        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                if (i == j)
                    {
                    continue;
                    }
                rows.add(row("geodesics", node(net, i), node(net, j),
                        hops[i][j]));
                }
            }
        if (wanted == null)
            {
            return rows;
            }
        List<String> filtered = new ArrayList<>();
        filtered.add(HEADER);
        for (String line : rows)
            {
            if (line.equals(HEADER))
                {
                continue;
                }
            if (wanted.contains(splitCsv(line)[0]))
                {
                filtered.add(line);
                }
            }
        return filtered;
        }

    public static String json(Network net)
        {
        return json(net, null);
        }

    public static String json(Network net, java.util.Set<String> wanted)
        {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("network", net.getName());
        ArrayNode metrics = root.putArray("metrics");
        for (String line : csv(net, wanted))
            {
            if (line.equals(HEADER))
                {
                continue;
                }
            String[] c = splitCsv(line);
            ObjectNode m = metrics.addObject();
            m.put("metric", c[0]);
            if (c[1].length() > 0)
                {
                m.put("node1", c[1]);
                }
            if (c[2].length() > 0)
                {
                m.put("node2", c[2]);
                }
            try
                {
                m.put("value", Double.parseDouble(c[3]));
                } catch (NumberFormatException e)
                {
                m.put("value", c[3]);
                }
            }
        try
            {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                    root);
            } catch (Exception e)
            {
            return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }

    private static double arcs(Network net)
        {
        int n = net.getSize();
        double total = 0.0;
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                if (i != j && net.getValue(i, j) != 0f)
                    {
                    total++;
                    }
                }
            }
        return total;
        }

    private static String node(Network net, int i)
        {
        return quote(net.getActor(i).getName());
        }

    private static String row(String metric, String a, String b, double v)
        {
        return quote(metric) + "," + a + "," + b + ","
                + String.format(Locale.ROOT, "%.4f", v);
        }

    // RFC 4180 quoting for names that contain separators
    private static String quote(String s)
        {
        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0)
            {
            return '"' + s.replace("\"", "\"\"") + '"';
            }
        return s;
        }

    private static String[] splitCsv(String line)
        {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++)
            {
            char ch = line.charAt(i);
            if (inQuotes)
                {
                if (ch == '"')
                    {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"')
                        {
                        cur.append('"');
                        i++;
                        } else
                        {
                        inQuotes = false;
                        }
                    } else
                    {
                    cur.append(ch);
                    }
                } else if (ch == '"')
                {
                inQuotes = true;
                } else if (ch == ',')
                {
                out.add(cur.toString());
                cur.setLength(0);
                } else
                {
                cur.append(ch);
                }
            }
        out.add(cur.toString());
        return out.toArray(new String[0]);
        }
    }