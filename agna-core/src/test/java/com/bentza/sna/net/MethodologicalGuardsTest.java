package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the 2.1.3 methodology work: disconnected networks are refused for
 * eccentricity/closeness/betweenness with a warning, density states its
 * convention explicitly, and the Floyd-Warshall geodesics keep correct
 * distances (validated through the distance-limited clique analysis).
 */
public class MethodologicalGuardsTest
    {
    private static Network directedChain()
        {
        // 0 -> 1 -> 2 (one-way): 2 cannot reach anybody -> disconnected
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 2);
        return net;
        }

    private static Network symmetricTriangle()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 0, 2);
        net.setValue(1f, 2, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);
        return net;
        }

    @Test
    public void disconnectedNetworkIsRefusedForEccentricityClosenessBetweenness()
        {
        Network net = directedChain();
        AgnaLib lib = new AgnaLib();
        assertTrue(lib.outEccentricity(net).contains(
                "WARNING: the network is disconnected"));
        assertTrue(lib.outCloseness(net).contains(
                "WARNING: the network is disconnected"));
        assertTrue(lib.outBetweenness(net).contains(
                "WARNING: the network is disconnected"));
        }

    @Test
    public void connectedNetworkComputesNormally()
        {
        Network net = symmetricTriangle();
        AgnaLib lib = new AgnaLib();
        assertFalse(lib.outCloseness(net).contains("WARNING"));
        }

    @Test
    public void densityStatesItsConventionExplicitly()
        {
        AgnaLib lib = new AgnaLib();

        String symmetric = lib.outDensity(symmetricTriangle());
        assertTrue(symmetric.contains("directed convention"));
        assertTrue(symmetric.contains("Undirected convention"));

        String asymmetric = lib.outDensity(directedChain());
        assertTrue(asymmetric.contains("an undirected density is not defined"));
        }

    @Test
    public void floydWarshallDistancesDriveCliquesCorrectly()
        {
        // undirected path 0-1-2: pairwise distances are 1,1,2 -> a 2-clique
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);

        AgnaLib lib = new AgnaLib();
        String out = lib.outCliques(net, 2);
        assertTrue(out.contains("2-Cliques"), "path of three must be a 2-clique: " + out);
        }
    }