package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: Full Analysis battery - one report from the standard measures,
 * honouring the disconnected-network convention.
 */
public class FullAnalysisTest
    {
    @Test
    public void connectedNetworkReceivesTheFullBattery()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);

        String out = new AgnaLib().outFullAnalysis(net);
        assertTrue(out.contains("FULL ANALYSIS"));
        assertTrue(out.contains("Density"));
        assertTrue(out.contains("Nodal Degree"));
        assertTrue(out.contains("Betweenness"));
        assertTrue(out.contains("Prestige"));
        assertTrue(out.contains("Geodesic"));
        assertTrue(!out.contains("measures skipped"), "connected net: no skips");
        }

    @Test
    public void disconnectedNetworkSkipsDistanceMeasuresWithANote()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1); // 1 reaches nothing back -> disconnected

        String out = new AgnaLib().outFullAnalysis(net);
        assertTrue(out.contains("measures skipped: the network is disconnected"));
        assertTrue(!out.contains("Distribution of Betweenness"),
                "betweenness must be skipped when disconnected");
        }
    }