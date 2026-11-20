package com.bentza.sna.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.Network;

/** 2.2.2 regression tests: breadth-first distances and bounded closeness in `agna metrics`. */
public class CliMetricsRegressionTest
    {
    private static Network ring(int n)
        {
        Network net = new Network(n);
        for (int i = 0; i < n; i++)
            {
            net.setValue(1f, i, (i + 1) % n);
            net.setValue(1f, (i + 1) % n, i);
            }
        return net;
        }

    private static String find(List<String> rows, String prefix)
        {
        for (String r : rows) if (r.startsWith(prefix)) return r;
        return null;
        }

    private static double value(String row)
        {
        String[] c = row.split(",");
        return Double.parseDouble(c[c.length - 1]);
        }

    @Test
    public void ringOfSixHasDiameterThreeAndExactDistances()
        {
        // 2.2.1 (depth-first) reported diameter 4 and a geodesic of 4 between actors 0 and 2
        Network net = ring(6);
        List<String> rows = CliMetrics.csv(net, null);
        assertEquals(3.0, value(find(rows, "diameter,")), 1e-9);
        String a = net.getActor(0).getName(), c = net.getActor(2).getName(), d = net.getActor(3).getName();
        assertEquals(2.0, value(find(rows, "geodesics," + a + "," + c + ",")), 1e-9);
        assertEquals(3.0, value(find(rows, "geodesics," + a + "," + d + ",")), 1e-9);
        for (String r : rows)
            if (r.startsWith("eccentricity,")) assertEquals(3.0, value(r), 1e-9, r);
        }

    @Test
    public void ringBetweennessIsSymmetricAndExact()
        {
        // ordered-pair convention of `agna metrics` (unchanged since 2.2.1): each actor of a 6-ring scores 4.0
        List<String> rows = CliMetrics.csv(ring(6), null);
        int seen = 0;
        for (String r : rows)
            {
            if (!r.startsWith("betweenness,")) continue;
            assertEquals(4.0, value(r), 1e-6, "all actors of a ring are equivalent: " + r);
            seen++;
            }
        assertEquals(6, seen);
        }

    @Test
    public void closenessStaysWithinOneWhenSomeActorsCannotReachOthers()
        {
        Network net = new Network(4);              // weakly connected chain 0 -> 1 -> 2 -> 3
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 3);
        List<String> rows = CliMetrics.csv(net, null);
        for (String r : rows)
            {
            if (!r.startsWith("closeness,")) continue;
            String[] c = r.split(",");
            double v = Double.parseDouble(c[c.length - 1]);
            assertTrue(v >= 0 && v <= 1.0 + 1e-9, r);
            }
        assertFalse(rows.stream().anyMatch(r -> r.startsWith("diameter,") || r.startsWith("eccentricity,")),
                "diameter and eccentricity are undefined when some actors cannot reach others");
        }
    }
