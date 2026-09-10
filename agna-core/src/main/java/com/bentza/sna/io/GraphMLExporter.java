package com.bentza.sna.io;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 2.1.3: GraphML export - the standard SNA exchange format understood by
 * Gephi, Cytoscape, NetworkX, igraph and the R SNA packages. The
 * sociomatrix is emitted as a directed graph: nodes carry their actor
 * name, edges their value as the weight. Implemented on the JDK XML API,
 * so no additional dependency is introduced.
 */
public class GraphMLExporter
    {
    public static final String GRAPHML_NS =
            "http://graphml.graphdrawing.org/xmlns";

    public GraphMLExporter()
        {
        }

    public String getGraphML(FullNet tmp_full_net)
        {
        return getGraphML(tmp_full_net.getNetwork());
        }

    public String getGraphML(Network net)
        {
        try
            {
            final int n = net.getSize();
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            Document doc = factory.newDocumentBuilder().newDocument();

            Element root = doc.createElementNS(GRAPHML_NS, "graphml");
            doc.appendChild(root);

            Element key_name = doc.createElementNS(GRAPHML_NS, "key");
            key_name.setAttribute("id", "d0");
            key_name.setAttribute("for", "node");
            key_name.setAttribute("attr.name", "name");
            key_name.setAttribute("attr.type", "string");
            root.appendChild(key_name);

            Element key_weight = doc.createElementNS(GRAPHML_NS, "key");
            key_weight.setAttribute("id", "d1");
            key_weight.setAttribute("for", "edge");
            key_weight.setAttribute("attr.name", "weight");
            key_weight.setAttribute("attr.type", "double");
            root.appendChild(key_weight);

            Element graph = doc.createElementNS(GRAPHML_NS, "graph");
            String net_name = net.getName();
            graph.setAttribute("id", net_name == null
                    || net_name.trim().length() == 0 ? "G" : net_name);
            graph.setAttribute("edgedefault", "directed");
            root.appendChild(graph);

            for (int i = 0; i < n; i++)
                {
                Element node = doc.createElementNS(GRAPHML_NS, "node");
                node.setAttribute("id", "n" + i);
                Element data = doc.createElementNS(GRAPHML_NS, "data");
                data.setAttribute("key", "d0");
                data.setTextContent(net.getActorName(i));
                node.appendChild(data);
                graph.appendChild(node);
                }

            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < n; j++)
                    {
                    float value = net.getValue(i, j);
                    if (i != j && value != 0f)
                        {
                        Element edge = doc.createElementNS(GRAPHML_NS,
                                "edge");
                        edge.setAttribute("source", "n" + i);
                        edge.setAttribute("target", "n" + j);
                        Element data = doc.createElementNS(GRAPHML_NS,
                                "data");
                        data.setAttribute("key", "d1");
                        data.setTextContent(String.valueOf(value));
                        edge.appendChild(data);
                        graph.appendChild(edge);
                        }
                    }
                }

            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc),
                    new StreamResult(writer));
            return writer.toString();
            } catch (Exception e)
            {
            AgnaLog.warn("GraphML export failed: " + e);
            return null;
            }
        }
    }