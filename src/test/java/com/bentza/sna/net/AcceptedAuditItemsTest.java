package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for the accepted audit items (2.1.3 round 3):
 * Brandes betweenness, per-direction nodal degrees, disconnected refusals
 * for fareness/bavelas, and the removed multipleGeodesics stub.
 */
public class AcceptedAuditItemsTest
    {
    @Test
    public void betweennessOnPathOfFour()
        {
        // undirected path 0-1-2-3: inner nodes carry all 4 ordered pairs,
        // endpoints 0
        Network net = new Network(4);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);
        net.setValue(1f, 2, 3);
        net.setValue(1f, 3, 2);

        float[] cb = new AgnaLib().betweenness(net);
        assertEquals(0f, cb[0]);
        assertEquals(4f, cb[1]);
        assertEquals(4f, cb[2]);
        assertEquals(0f, cb[3]);
        }

    @Test
    public void betweennessOnStar()
        {
        // symmetric star: center 0 between all 6 ordered leaf pairs
        Network net = new Network(4);
        for (int leaf = 1; leaf < 4; leaf++)
            {
            net.setValue(1f, 0, leaf);
            net.setValue(1f, leaf, 0);
            }
        float[] cb = new AgnaLib().betweenness(net);
        assertEquals(6f, cb[0]);
        assertEquals(0f, cb[1]);
        assertEquals(0f, cb[2]);
        assertEquals(0f, cb[3]);
        }

    @Test
    public void nodalDegreeCountsEachDirectionOnAsymmetricNetworks()
        {
        // 0 -> 1 only: node 0 has out=1, node 1 has in=1
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        float[] nd = new AgnaLib().nodalDegree(net);
        assertEquals(1f, nd[0]);
        assertEquals(1f, nd[1]);
        assertEquals(0f, nd[2]);
        }

    @Test
    public void nodalDegreeIsNeighbourCountOnSymmetricNetworks()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        float[] nd = new AgnaLib().nodalDegree(net);
        assertEquals(1f, nd[0]);
        assertEquals(1f, nd[1]);
        }

    @Test
    public void farenessAndBavelasRefuseDisconnectedNetworks()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1); // 1 reaches 0, but nobody reaches 2
        AgnaLib lib = new AgnaLib();
        assertTrue(lib.outFareness(net).contains(
                "WARNING: the network is disconnected"));
        assertTrue(lib.outBavelas(net).contains(
                "WARNING: the network is disconnected"));
        }
    }