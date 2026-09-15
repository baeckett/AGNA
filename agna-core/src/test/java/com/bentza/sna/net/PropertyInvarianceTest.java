package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: property-based invariants over many random small networks
 * (audit C3/P1) — deterministic seeded randomness so failures are
 * reproducible. Each property must hold for every generated network,
 * not just hand-picked examples.
 */
public class PropertyInvarianceTest
    {
    private final AgnaLib agna = new AgnaLib();

    private static Network randomNet(Random rnd)
        {
        int n = 2 + rnd.nextInt(7);
        float[][] m = new float[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                m[i][j] = rnd.nextInt(6);
        return new Network(m);
        }

    private static float[][] deepCopy(float[][] src)
        {
        float[][] out = new float[src.length][];
        for (int i = 0; i < src.length; i++)
            out[i] = src[i].clone();
        return out;
        }

    private static float[][] matrixOf(Network net)
        {
        int n = net.getSize();
        float[][] m = new float[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                m[i][j] = net.getValue(i, j);
        return m;
        }

    @Test
    public void symmetrizeSumAlwaysYieldsASymmetricMatrix()
        {
        for (int t = 0; t < 25; t++)
            {
            Network net = randomNet(new Random(t));
            agna.symmetrizeSum(net);
            float[][] m = matrixOf(net);
            for (int i = 0; i < m.length; i++)
                for (int j = 0; j < m.length; j++)
                    assertEquals(m[i][j], m[j][i], 0f,
                            "symmetric at [" + i + "][" + j + "] seed " + t);
            }
        }

    @Test
    public void transposeTwiceRestoresTheOriginalMatrix()
        {
        for (int t = 0; t < 25; t++)
            {
            float[][] orig = matrixOf(randomNet(new Random(t)));
            Network net = new Network(deepCopy(orig));
            agna.transpose(net);
            agna.transpose(net);
            float[][] m = matrixOf(net);
            for (int i = 0; i < m.length; i++)
                for (int j = 0; j < m.length; j++)
                    assertEquals(orig[i][j], m[i][j], 0f,
                            "cell [" + i + "][" + j + "] seed " + t);
            }
        }

    @Test
    public void addScalarRoundTripRestoresTheOriginalValues()
        {
        for (int t = 0; t < 25; t++)
            {
            Network net = randomNet(new Random(t));
            float[][] before = deepCopy(matrixOf(net));
            float k = t - 12;
            agna.addScalar(net, k);
            agna.addScalar(net, -k);
            float[][] after = matrixOf(net);
            for (int i = 0; i < after.length; i++)
                for (int j = 0; j < after.length; j++)
                    assertEquals(before[i][j], after[i][j], 0f,
                            "cell [" + i + "][" + j + "] seed " + t);
            }
        }

    @Test
    public void normalizeLeavesAllValuesInTheUnitRange()
        {
        for (int t = 0; t < 25; t++)
            {
            Network net = randomNet(new Random(t));
            agna.normalize(net);
            float[][] m = matrixOf(net);
            for (int i = 0; i < m.length; i++)
                for (int j = 0; j < m.length; j++)
                    {
                    assertTrue(m[i][j] >= 0f && m[i][j] <= 1f,
                            "value [" + i + "][" + j + "] = " + m[i][j]
                                    + " seed " + t);
                    }
            }
        }
    }