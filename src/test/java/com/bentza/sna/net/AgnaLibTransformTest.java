package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3 audit: every sociomatrix transformation in AgnaLib is verified
 * against its mathematical definition — transpose, the symmetrization
 * family, scalar operations, matrix/boolean multiplication, binarisation
 * and the outsider/isolate logic.
 */
public class AgnaLibTransformTest
    {
    private static void assertValue(float expected, Network net, int i, int j)
        {
        assertEquals(expected, net.getValue(i, j), 1e-4f,
                "cell (" + i + "," + j + ")");
        }

    private static Network netOf(float[][] mat)
        {
        return new Network(mat);
        }

    private final AgnaLib agna = new AgnaLib();

    private static final float[][] M = { { 0f, 1f, 2f }, { 3f, 0f, 4f },
            { 5f, 6f, 0f } };

    @Test
    public void transposeMirrorsTheMatrix()
        {
        Network net = netOf(M);
        agna.transpose(net);
        assertValue(3f, net, 0, 1); // M[1][0]
        assertValue(1f, net, 1, 0); // M[0][1]
        assertValue(5f, net, 0, 2);
        assertValue(4f, net, 2, 1);
        assertValue(6f, net, 1, 2);
        assertValue(0f, net, 1, 1); // diagonal unchanged
        }

    @Test
    public void symmetrizeMaximumTakesThePairMaximum()
        {
        Network net = netOf(M);
        agna.symmetrizeMaximum(net);
        assertValue(3f, net, 0, 1);
        assertValue(3f, net, 1, 0);
        assertValue(5f, net, 0, 2);
        assertValue(6f, net, 1, 2);
        assertValue(6f, net, 2, 1);
        }

    @Test
    public void symmetrizeMinimumTakesThePairMinimum()
        {
        Network net = netOf(M);
        agna.symmetrizeMinimum(net);
        assertValue(1f, net, 0, 1);
        assertValue(1f, net, 1, 0);
        assertValue(2f, net, 0, 2);
        assertValue(2f, net, 2, 0);
        assertValue(4f, net, 1, 2);
        assertValue(4f, net, 2, 1);
        }

    @Test
    public void symmetrizeBelowKeepsTheLowerTriangle()
        {
        Network net = netOf(M);
        agna.symmetrizeBelow(net);
        assertValue(3f, net, 0, 1); // lower value copied up
        assertValue(3f, net, 1, 0);
        assertValue(5f, net, 0, 2);
        assertValue(6f, net, 1, 2);
        }

    @Test
    public void symmetrizeAboveKeepsTheUpperTriangle()
        {
        Network net = netOf(M);
        agna.symmetrizeAbove(net);
        assertValue(1f, net, 1, 0); // upper value copied down
        assertValue(2f, net, 2, 0);
        assertValue(4f, net, 2, 1);
        assertValue(1f, net, 0, 1);
        }

    @Test
    public void symmetrizeSumAndProduct()
        {
        Network sum = netOf(M);
        agna.symmetrizeSum(sum);
        assertValue(4f, sum, 0, 1);
        assertValue(4f, sum, 1, 0);
        assertValue(7f, sum, 0, 2);
        assertValue(10f, sum, 1, 2);

        Network prod = netOf(M);
        agna.symmetrizeProduct(prod);
        assertValue(3f, prod, 0, 1);
        assertValue(3f, prod, 1, 0);
        assertValue(10f, prod, 0, 2);
        assertValue(24f, prod, 1, 2);
        }

    @Test
    public void symmetrizeArithmeticAndGeometricMean()
        {
        Network am = netOf(M);
        agna.symmetrizeAM(am);
        assertValue(2f, am, 0, 1);
        assertValue(3.5f, am, 0, 2);
        assertValue(5f, am, 1, 2);

        Network gm = netOf(M);
        agna.symmetrizeGM(gm);
        assertValue((float) Math.sqrt(3d), gm, 0, 1);
        assertValue((float) Math.sqrt(10d), gm, 0, 2);
        assertValue((float) Math.sqrt(24d), gm, 1, 2);
        }

    @Test
    public void geometricMeanWithOppositeSignsCannotProduceNaN()
        {
        float[][] neg = { { 0f, -4f }, { 9f, 0f } };
        Network net = netOf(neg);
        agna.symmetrizeGM(net);
        float v = net.getValue(0, 1);
        assertFalse(Float.isNaN(v), "GM of (-4,9) must not be NaN");
        assertEquals(0f, v, 1e-6f);
        assertEquals(0f, net.getValue(1, 0), 1e-6f);
        }

    @Test
    public void nonZeroVariantsKeepTheSingleNonZeroValue()
        {
        float[][] sparse = { { 0f, 0f, 2f }, { 5f, 0f, 0f }, { 0f, 3f, 0f } };
        Network mx = netOf(sparse);
        agna.symmetrizeMaximumNonZero(mx);
        assertValue(5f, mx, 0, 1);
        assertValue(2f, mx, 0, 2);
        assertValue(3f, mx, 1, 2);

        Network mn = netOf(sparse);
        agna.symmetrizeMinimumNonZero(mn);
        assertValue(5f, mn, 0, 1);
        assertValue(2f, mn, 0, 2);
        assertValue(3f, mn, 1, 2);

        Network pr = netOf(sparse);
        agna.symmetrizeProductNonZero(pr);
        assertValue(5f, pr, 0, 1);
        assertValue(2f, pr, 0, 2);
        assertValue(3f, pr, 1, 2);
        }

    @Test
    public void addAndMultiplyByScalar()
        {
        Network add = netOf(M);
        agna.addScalar(add, 2f);
        assertValue(3f, add, 0, 1);
        assertValue(6f, add, 1, 2);
        assertValue(0f, add, 1, 1); // the diagonal stays zero (no self-loops)

        Network mul = netOf(M);
        agna.multiplyByScalar(mul, 2f);
        assertValue(2f, mul, 0, 1);
        assertValue(8f, mul, 1, 2);
        assertValue(0f, mul, 0, 0);
        }

    @Test
    public void squareOfAdjacencyIsReachabilityByTwoSteps()
        {
        float[][] adj = { { 0f, 1f }, { 1f, 0f } };
        float[][] sq = agna.multiplyNetworks(netOf(adj), netOf(adj));
        assertEquals(1f, sq[0][0], 1e-6f);
        assertEquals(0f, sq[0][1], 1e-6f);
        assertEquals(1f, sq[1][1], 1e-6f);

        float[][] weighted = { { 0f, 2f }, { 3f, 0f } };
        float[][] ws = agna.multiplyNetworks(netOf(weighted), netOf(weighted));
        assertEquals(6f, ws[0][0], 1e-6f);
        assertEquals(0f, ws[0][1], 1e-6f);
        assertEquals(6f, ws[1][1], 1e-6f);
        }

    @Test
    public void booleanMultiplicationIsReachability()
        {
        float[][] adj = { { 0f, 1f }, { 1f, 0f } };
        boolean[][] res = agna.multiplyMatricesBoolean(adj, adj);
        assertTrue(res[0][0]);
        assertFalse(res[0][1]);
        assertTrue(res[1][1]);
        }

    @Test
    public void normalizeBinarisesAllNonZeroValues()
        {
        float[][] w = { { 0f, 2f, 0f }, { 4f, 0f, 6f }, { 0f, 8f, 0f } };
        Network net = netOf(w);
        agna.normalize(net);
        assertEquals(0f, net.getValue(0, 0), 1e-6f);
        assertEquals(1f, net.getValue(0, 1), 1e-6f);
        assertEquals(1f, net.getValue(1, 0), 1e-6f);
        assertEquals(1f, net.getValue(1, 2), 1e-6f);
        assertEquals(1f, net.getValue(2, 1), 1e-6f);
        }

    @Test
    public void outsiderDetectionAndIsolation()
        {
        float[][] e = { { 0f, 0f, 0f }, { 1f, 0f, 0f }, { 0f, 0f, 0f } };
        Network net = netOf(e);
        assertFalse(net.isOutsider(0)); // receives from 1
        assertFalse(net.isOutsider(1)); // emits to 0
        assertTrue(net.isOutsider(2));  // neither emits nor receives

        float[][] pair = { { 0f, 1f }, { 1f, 0f } };
        Network iso = netOf(pair);
        iso.isolateActor(1);
        assertValue(0f, iso, 0, 1);
        assertValue(0f, iso, 1, 0);
        }
    }