package com.bentza.sna.io;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 2.1.3: GraphSON 2.0 export - the JSON graph format from the TinkerPop
 * family, readable by Gephi, Cytoscape and JSON toolchains (NetworkX
 * users can navigate the simple vertices/edges structure directly).
 * Implemented with Jackson (Apache-2.0).
 */
public class GraphSONExporter
    {
    public GraphSONExporter()
        {
        }

    public String getGraphSON(FullNet tmp_full_net)
        {
        return getGraphSON(tmp_full_net.getNetwork());
        }

    public String getGraphSON(Network net)
        {
        try
            {
            final int n = net.getSize();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            ObjectNode graph = root.putObject("graph");
            ArrayNode vertices = graph.putArray("vertices");
            for (int i = 0; i < n; i++)
                {
                ObjectNode vertex = vertices.addObject();
                vertex.put("id", i);
                vertex.put("label", net.getActorName(i));
                }
            ArrayNode edges = graph.putArray("edges");
            int edge_id = 0;
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    float value = net.getValue(i, j);
                    if (i != j && value != 0f)
                        {
                        ObjectNode edge = edges.addObject();
                        edge.put("id", edge_id++);
                        edge.put("source", i);
                        edge.put("target", j);
                        edge.put("value", value);
                        }
                    }
                }
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(root);
            } catch (Exception e)
            {
            AgnaLog.warn("GraphSON export failed: " + e);
            return null;
            }
        }
    }