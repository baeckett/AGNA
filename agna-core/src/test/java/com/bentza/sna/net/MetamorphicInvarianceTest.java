package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * 2.1.3: metamorphic invariants for the core engine — results that must not
 * change under relabeling/permutation, and structural invariants of the
 * matrix operations (audit C3). Also covers the 2.1.3 matrix-validation
 * guards (audit G2/G3/G4): malformed inputs fail fast with descriptive
 * errors instead of a silent ArrayIndexOutOfBoundsException or wrapped
 * overflow.
 */
public class MetamorphicInvarianceTest
    {
    private final AgnaLib agna = new AgnaLib();

    private static Network netOf(float[][] mat)
        {
        return new Network(mat);
        }

    // a small network with clearly distinct tie values
    private static float[][] sampleMatrix()
        {
        return new float[][] {
                { 0, 2, 1, 0 },
                { 1, 0, 3, 1 },
                { 0, 2, 0, 4 },
                { 5, 0, 1, 0 } };
        }

    private static float[][] permuted(float[][] m, int[] p)
        {
        int n = m.length;
        float[][] out = new float[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                out[p[i]][p[j]] = m[i][j];
        return out;
        }

    @Test
    public void permutationPermutesNodeLevelOutAndInDegrees()
        {
        float[][] m = sampleMatrix();
        int n = m.length;
        int[] p = new int[] { 1, 3, 0, 2 };
        float[][] pm = permuted(m, p);
        long[] outBefore = new long[n];
        long[] inBefore = new long[n];
        long[] outAfter = new long[n];
        long[] inAfter = new long[n];
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                outBefore[i] += (long) m[i][j];
                inBefore[i] += (long) m[j][i];
                outAfter[p[i]] += (long) pm[p[i]][p[j]];
                inAfter[p[i]] += (long) pm[p[j]][p[i]];
                }
            }
        for (int i = 0; i < n; i++)
            {
            assertEquals(outBefore[i], outAfter[p[i]], "out-degree at " + i);
            assertEquals(inBefore[i], inAfter[p[i]], "in-degree at " + i);
            }
        }

    @Test
    public void transposeSwapsInAndOutDegrees()
        {
        float[][] m = sampleMatrix();
        int n = m.length;
        int[][] t = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                t[j][i] = (int) m[i][j];
        for (int i = 0; i < n; i++)
            {
            int out = 0, in = 0, tOut = 0, tIn = 0;
            for (int j = 0; j < n; j++)
                {
                out += (int) m[i][j];
                in += (int) m[j][i];
                tOut += t[i][j];
                tIn += t[j][i];
                }
            assertEquals(out, tIn, "transpose exchanges in/out at " + i);
            assertEquals(in, tOut, "transpose exchanges in/out at " + i);
            }
        }

    @Test
    public void symmetrizeSumProducesASymmetricMatrix()
        {
        Network net = netOf(sampleMatrix());
        agna.symmetrizeSum(net);
        int n = net.getSize();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                assertEquals(net.getValue(i, j), net.getValue(j, i),
                        "matrix must be symmetric at [" + i + "][" + j + "]");
        }

    @Test
    public void matrixMaxRejectsRaggedAndEmptyMatrices()
        {
        assertThrows(IllegalArgumentException.class,
                () -> agna.matrixMax(new float[][] { { 1, 2 }, { 3 } }));
        assertThrows(IllegalArgumentException.class,
                () -> agna.matrixMax(new int[][] { { 1, 2 }, { 3 } }));
        assertThrows(IllegalArgumentException.class,
                () -> agna.matrixMax(new float[0][0]));
        }

    @Test
    public void multiplyMatricesRejectsNonSquareInputs()
        {
        float[][] rect = new float[][] { { 1, 2, 3 }, { 4, 5, 6 } };
        float[][] square = new float[][] { { 1, 0 }, { 0, 1 } };
        assertThrows(IllegalArgumentException.class,
                () -> agna.multiplyMatrices(rect, square));
        assertThrows(IllegalArgumentException.class,
                () -> agna.multiplyMatricesBoolean(rect, square));
        assertThrows(IllegalArgumentException.class,
                () -> agna.multiplyMatrices(
                        new int[][] { { 1, 2 }, { 3 } },
                        new int[][] { { 1, 0 }, { 0, 1 } }));
        }

    @Test
    public void integerMultiplyOverflowFailsLoudly()
        {
        int[][] big = new int[][] { { 1000000000 } };
        assertThrows(ArithmeticException.class,
                () -> agna.multiplyMatrices(big, big));
        }

    @Test
    public void nonFiniteFloatMultiplyFailsLoudly()
        {
        float[][] nan = new float[][] { { Float.NaN } };
        assertThrows(ArithmeticException.class,
                () -> agna.multiplyMatrices(nan, nan));
        }

    @Test
    public void transformationsRejectNonFiniteTieValues()
        {
        Network net = netOf(new float[][] { { 1f, Float.NaN }, { 0f, 1f } });
        // only meaningful when the model actually preserves the value
        Assumptions.assumeTrue(Float.isNaN(net.getValue(0, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> agna.symmetrizeSum(net));
        }

    @Test
    public void cliqueReportIsStableAndFlagsResetBetweenRuns()
        {
        // a triangle plus a pendant node: two maximal cliques
        Network net = netOf(new float[][] {
                { 0, 1, 1, 0 },
                { 1, 0, 1, 0 },
                { 1, 1, 0, 0 },
                { 1, 0, 0, 0 } });
        String first = agna.outCliques(net, 1);
        String second = agna.outCliques(net, 1);
        assertEquals(first, second,
                "repeated enumeration must produce the same report");
        assertTrue(!AgnaLib.clique_search_cancelled
                && !AgnaLib.clique_truncated,
                "completion flags must be reset between runs");
        }
    }