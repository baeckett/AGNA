package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** 2.2.2: closeness stays within [0, 1] when some actors cannot reach others. */
public class ClosenessBoundTest
    {
    @Test
    public void standardisedClosenessNeverExceedsOne()
        {
        // 0 <-> 1 reach each other; 2 -> 3 is a separate pair; 4 is isolated
        Network net = new Network(5);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 2, 3);
        net.setValue(1f, 3, 2);
        float[] c = new AgnaLib().closeness(net);
        for (int i = 0; i < 5; i++)
            {
            double std = c[i] * (5 - 1);
            assertTrue(std >= 0 && std <= 1.0 + 1e-6, "actor " + i + " standardised closeness " + std);
            }
        // actor 0 reaches 1 of 4 others at distance 1: (1/4) * (1/1) = 0.25 (2.2.1 reported 4 * 1/1 = 4.0)
        assertEquals(0.25, c[0] * 4, 1e-6);
        assertEquals(0.0, c[4], 1e-9);
        }

    @Test
    public void connectedNetworksAreUnchanged()
        {
        // path 0 - 1 - 2: actor 1 has fareness 2, closeness 1/2, standardised 1.0
        Network net = new Network(3);
        net.setValue(1f, 0, 1); net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2); net.setValue(1f, 2, 1);
        float[] c = new AgnaLib().closeness(net);
        assertEquals(0.5, c[1], 1e-6);
        assertEquals(1.0 / 3.0, c[0], 1e-6);
        }
    }
