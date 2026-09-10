package com.bentza.sna.io;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;

/**
 * 2.1.3: GML (Graph Modelling Language) export - the classic text format
 * read by igraph, NetworkX and Gephi. Nodes keep their order and names
 * (label), edges carry their value. Strings are escaped per the GML
 * rules (backslash and double quote).
 */
public class GMLExporter
    {
    public GMLExporter()
        {
        }

    public String getGML(FullNet tmp_full_net)
        {
        return getGML(tmp_full_net.getNetwork());
        }

    public String getGML(Network net)
        {
        try
            {
            final int n = net.getSize();
            StringBuilder sb = new StringBuilder();
            sb.append("graph [\n");
            sb.append("  directed 1\n");
            for (int i = 0; i < n; i++)
                {
                sb.append("  node [\n");
                sb.append("    id ").append(i).append("\n");
                sb.append("    label \"").append(escape(net.getActorName(i)))
                        .append("\"\n");
                sb.append("  ]\n");
                }
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    float value = net.getValue(i, j);
                    if (i != j && value != 0f)
                        {
                        sb.append("  edge [\n");
                        sb.append("    source ").append(i).append("\n");
                        sb.append("    target ").append(j).append("\n");
                        sb.append("    value ").append(value).append("\n");
                        sb.append("  ]\n");
                        }
                    }
                }
            sb.append("]\n");
            return sb.toString();
            } catch (Exception e)
            {
            AgnaLog.warn("GML export failed: " + e);
            return null;
            }
        }

    private static String escape(String s)
        {
        if (s == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++)
            {
            char c = s.charAt(i);
            if (c == '\\')
                {
                sb.append("\\\\");
                } else if (c == '"')
                {
                sb.append("\\\"");
                } else if (c == '\n' || c == '\r')
                {
                sb.append(' ');
                } else
                {
                sb.append(c);
                }
            }
        return sb.toString();
        }
    }