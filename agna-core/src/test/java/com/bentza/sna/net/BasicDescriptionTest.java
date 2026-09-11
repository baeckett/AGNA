package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the basic description must state connectivity and outsiders at
 * a glance, and the disconnected-network warnings must name the network
 * and the actual index being refused.
 */
public class BasicDescriptionTest
    {
    private Network read(String file) throws Exception
        {
        FullNet full = new FullNet();
        full.readNetwork(new String(Files.readAllBytes(new File(file)
                .toPath()), StandardCharsets.ISO_8859_1), "agn");
        return full.getNetwork();
        }

    @Test
    public void connectedNetworkIsReportedAsConnected() throws Exception
        {
        String text = new AgnaLib().outBasic(read("samples/example2.agn"));
        assertTrue(text.contains("The network is connected."), text);
        }

    @Test
    public void disconnectedNetworkIsReportedAsDisconnected() throws Exception
        {
        // four nodes, two separate pairs
        Network net = new Network(4);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 2, 3);
        net.setValue(1f, 3, 2);
        String text = new AgnaLib().outBasic(net);
        assertTrue(text.contains("The network is disconnected"), text);
        }

    @Test
    public void warningsNameTheNetworkAndTheIndex() throws Exception
        {
        Network net = new Network(4);
        net.setName("Split");
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 2, 3);
        net.setValue(1f, 3, 2);
        String bavelas = new AgnaLib().outBavelas(net);
        assertTrue(bavelas.contains("WARNING: Split is disconnected"),
                bavelas);
        assertTrue(
                bavelas.contains("the Bavelas-Leavitt centrality index "
                        + "cannot be computed"), bavelas);
        String closeness = new AgnaLib().outCloseness(net);
        assertTrue(closeness.contains("WARNING: Split is disconnected"),
                closeness);
        assertTrue(
                closeness.contains("the closeness centrality index cannot "
                        + "be computed"), closeness);
        String betweenness = new AgnaLib().outBetweenness(net);
        assertTrue(
                betweenness.contains("the betweenness centrality index "
                        + "cannot be computed"), betweenness);
        }
    }