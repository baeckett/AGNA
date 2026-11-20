/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import com.bentza.sna.core.AppRuntime;
import com.bentza.sna.report.AnalysisReport;
import com.bentza.sna.report.AnalysisReportFactory;
import com.bentza.sna.report.CentralityOptions;
import com.bentza.sna.report.ClusteringOptions;
import com.bentza.sna.report.DistanceOptions;
import com.bentza.sna.report.FullAnalysisOptions;
import com.bentza.sna.report.SociometricsOptions;
import com.bentza.sna.report.TableRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

        public class AgnaLib
    {
    public static String lb, bold, unbold, it, unit, table, untable, tr, untr,
            td, untd, ol, unol, li, unli, blanc;

    // 2.1.3: set by the UI to abort an in-flight clique enumeration
    public static volatile boolean clique_search_cancelled;

    // 2.1.3: safety cap for the clique report; enumeration is exponential in
    // the worst case and a dense network can produce millions of cliques
    public static final int MAX_REPORTED_CLIQUES = 1000;

    // 2.1.3: set when the reported-clique cap was reached (distinct from a
    // user cancellation; outCliques appends the completion note)
    public static volatile boolean clique_truncated;

    // 2.1.3: shortest-path enumeration cap and its completion flag
    private static final int MAX_SHORTEST_PATHS = 10000;
    private static volatile boolean shortest_paths_truncated;

    // -- matrix validation guards (2.1.3 hardening) ---------------------
    private static void requireNonEmptySquare(float[][] m, String name)
        {
        if (m == null || m.length == 0)
            throw new IllegalArgumentException(name + " must be a non-empty matrix");
        int n = m.length;
        for (int i = 0; i < n; i++)
            {
            if (m[i] == null || m[i].length != n)
                throw new IllegalArgumentException(name
                        + " must be a square matrix: row " + i + " has "
                        + (m[i] == null ? "no" : String.valueOf(m[i].length))
                        + " columns, expected " + n);
            }
        }

    private static void requireNonEmptySquare(int[][] m, String name)
        {
        if (m == null || m.length == 0)
            throw new IllegalArgumentException(name + " must be a non-empty matrix");
        int n = m.length;
        for (int i = 0; i < n; i++)
            {
            if (m[i] == null || m[i].length != n)
                throw new IllegalArgumentException(name
                        + " must be a square matrix: row " + i + " has "
                        + (m[i] == null ? "no" : String.valueOf(m[i].length))
                        + " columns, expected " + n);
            }
        }

    private static void requireFiniteNetwork(Network src, String op)
        {
        int n = src.getSize();
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                if (!Float.isFinite(src.getValue(i, j)))
                    throw new IllegalArgumentException(op + ": " + src.getName()
                            + " contains a non-finite tie value (NaN or "
                            + "infinity) at [" + i + "][" + j + "]");
                }
            }
        }

        public static void initAjna()
        {
        // type-dependent text elements:
        // 2.1.3: null-safe (the engine can also be used headless, e.g. in
        // batch runs or tests, before the output pane exists)
        String cont = AppRuntime.outputContentType();
        if (cont.equals("text/plain") || cont.equals("text"))
            {

            lb = System.getProperty("line.separator");
            bold = "";
            unbold = "";
            it = "";
            unit = "";
            table = "";
            untable = "";
            tr = "";
            untr = lb;
            td = "";
            untd = "\t";
            ol = lb + "";
            unol = "";
            li = "*";
            unli = "" + lb;
            blanc = " ";
            } else if (cont.equals("text/html"))
            {
            lb = "<br>";
            bold = "<b>";
            unbold = "</b>";
            it = "<i>";
            unit = "</i>";
            table = "<table border='1'>";
            untable = "</table><br>";
            tr = "<tr>";
            untr = "</tr>";
            td = "<td>";
            untd = "</td>";
            ol = "<ul>";
            unol = "</ul>";
            li = "<li>";
            unli = "</li>";
            blanc = "&nbsp;";
            } else
            {
            lb = "";
            bold = "";
            unbold = "";
            it = "";
            unit = "";
            table = "";
            untable = "";
            tr = "";
            untr = "";
            td = "";
            untd = "";
            ol = "";
            unol = "";
            li = "* ";
            unli = "" + lb;
            blanc = " ";
            }
        cont = null;
        }

    public AgnaLib()
        {
        AgnaLib.initAjna();
        }

    public String getAgnaSignature()
        {
        try
            {
            return it + Environment.getApplicationFullName() + " Report, "
                    + (new Date()).toString() + unit + AgnaLib.lb;
            } catch (Exception e)
            {
            return "";
            }
        }

    // returns the maximum element of an array
    private float arrayMax(float[] src)
        {
        int size = src.length;
        int i;
        float finval = Float.NEGATIVE_INFINITY;
        for (i = 0; i < size; i++)
            {
            if (finval < src[i])
                finval = src[i];
            }
        return finval;
        }

    // returns the maximum element of a matrix
    public float matrixMax(float[][] src)
        {
        requireNonEmptySquare(src, "matrixMax");
        int size = src.length;
        int i, j;
        float finval = Float.NEGATIVE_INFINITY;
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                if (finval < src[i][j])
                    finval = src[i][j];
                }
            }
        return finval;
        }

    public float matrixMax(int[][] src)
        {
        requireNonEmptySquare(src, "matrixMax");
        int size = src.length;
        int i, j;
        int finval = Integer.MIN_VALUE;
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                if (finval < src[i][j])
                    finval = src[i][j];
                }
            }
        return finval;
        }

    // returns a specified row of a matrix
    public float[] getRow(float[][] src, int row)
        {
        int size = src.length;
        int j;
        float[] finval = new float[size];
        for (j = 0; j < size; j++)
            {
            finval[j] = src[row][j];
            }
        return finval;
        }

    // returns a specified row of a matrix
    public float[] getColumn(float[][] src, int col)
        {
        int size = src.length;
        int i;
        float[] finval = new float[size];
        for (i = 0; i < size; i++)
            {
            finval[i] = src[i][col];
            }
        return finval;
        }

    public void transpose(Network src)
        {
        int size = src.getSize();
        int i, j;
        Network result = new Network(size);
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                result.setValue(src.getValue(i, j), j, i);
                }
            }
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                src.setValue(result.getValue(i, j), i, j);
                }
            }
        }

    public float getMinNonZero(float first, float second)
        {
        if (first == 0f)
            return second;
        if (second == 0f)
            return first;
        return Math.min(first, second);
        }

    public float getMaxNonZero(float first, float second)
        {
        if (first == 0f)
            return second;
        if (second == 0f)
            return first;
        return Math.max(first, second);
        }

    public void symmetrizeMaximum(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeMaximum");
        int size = src.getSize();
        int i = 0;
        int j = 0;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = Math.max(src.getValue(i, j), src.getValue(j, i));
                src.setValue(finval, i, j);
                src.setValue(finval, j, i);
                }
            }
        }

    public void symmetrizeMaximumNonZero(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeMaximumNonZero");
        int size = src.getSize();
        int i = 0;
        int j = 0;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = getMaxNonZero(src.getValue(i, j), src.getValue(j, i));
                src.setValue(finval, i, j);
                src.setValue(finval, j, i);
                }
            }
        }

    public void symmetrizeMinimum(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeMinimum");
        int size = src.getSize();
        int i, j;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = Math.min(src.getValue(i, j), src.getValue(j, i));
                src.setValue(finval, i, j);
                src.setValue(finval, j, i);
                }
            }
        }

    public void symmetrizeMinimumNonZero(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeMinimumNonZero");
        int size = src.getSize();
        int i = 0;
        int j = 0;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = getMinNonZero(src.getValue(i, j), src.getValue(j, i));
                src.setValue(finval, i, j);
                src.setValue(finval, j, i);
                }
            }
        }

    // keeps the left side of sociomatrix
    public void symmetrizeBelow(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeBelow");
        int size = src.getSize();
        int i, j;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = src.getValue(j, i);
                src.setValue(finval, i, j);
                }
            }
        }

    // keeps the right side of sociomatrix;
    public void symmetrizeAbove(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeAbove");
        int size = src.getSize();
        int i, j;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = src.getValue(i, j);
                src.setValue(finval, j, i);
                }
            }
        }

    // keeps the left side of sociomatrix;
    // if one element is zero on the left, keeps the right one;
    public void symmetrizeBelowNonZero(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeBelowNonZero");
        int size = src.getSize();
        int i, j;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = src.getValue(j, i);
                if (finval != 0f)
                    src.setValue(finval, i, j);
                else
                    src.setValue(src.getValue(i, j), j, i);
                }
            }
        }

    // keeps the right side of sociomatrix;
    // if one element is zero on the right, keeps the left one;
    public void symmetrizeAboveNonZero(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeAboveNonZero");
        int size = src.getSize();
        int i, j;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = src.getValue(i, j);
                if (finval != 0f)
                    src.setValue(finval, j, i);
                else
                    src.setValue(src.getValue(j, i), i, j);
                }
            }
        }

    public void symmetrizeSum(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeSum");
        int size = src.getSize();
        int i, j;
        double finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = (double) src.getValue(i, j)
                        + (double) src.getValue(j, i);
                src.setValue((float) finval, i, j);
                src.setValue((float) finval, j, i);
                }
            }
        }

    public void symmetrizeProduct(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeProduct");
        int size = src.getSize();
        int i, j;
        double finval = 0;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = (double) src.getValue(i, j)
                        * (double) src.getValue(j, i);
                src.setValue((float) finval, i, j);
                src.setValue((float) finval, j, i);
                }
            }
        }

    public float getProductNonZero(float first, float second)
        {
        if (first == 0f)
            return second;
        if (second == 0f)
            return first;
        return (float) ((double) first * (double) second);
        }

    public void symmetrizeProductNonZero(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeProductNonZero");
        int size = src.getSize();
        int i, j;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = getProductNonZero(src.getValue(i, j), src.getValue(j,
                        i));
                src.setValue(finval, i, j);
                src.setValue(finval, j, i);
                }
            }
        }

    public void symmetrizeAM(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeAM");
        int size = src.getSize();
        int i, j;
        double finval = 0;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = ((double) src.getValue(i, j) + (double) src.getValue(
                        j, i)) / 2;
                src.setValue((float) finval, i, j);
                src.setValue((float) finval, j, i);
                }
            }
        }

    public void symmetrizeGM(Network src)
        {
        requireFiniteNetwork(src, "symmetrizeGM");
        int size = src.getSize();
        int i, j;
        double finval = 0;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                // 2.1.3: the geometric mean is defined for non-negative
                // pairs; guard the product so opposite signs cannot
                // produce NaN cells
                finval = Math.sqrt(Math.max(0d,
                        (double) src.getValue(i, j)
                                * (double) src.getValue(j, i)));
                src.setValue((float) finval, i, j);
                src.setValue((float) finval, j, i);
                }
            }
        }

    public void addScalar(Network src, float scal)
        {
        requireFiniteNetwork(src, "addScalar");
        int size = src.getSize();
        int i, j;
        double finval = 0;
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                // 2.1.3: the diagonal (no self-loops) stays zero
                if (i == j)
                    continue;
                finval = (double) src.getValue(i, j) + (double) scal;
                src.setValue((float) finval, i, j);
                }
            }
        }

    public void multiplyByScalar(Network src, float scal)
        {
        requireFiniteNetwork(src, "multiplyByScalar");
        int size = src.getSize();
        int i, j;
        double finval = 0;
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                // 2.1.3: the diagonal (no self-loops) stays zero
                if (i == j)
                    continue;
                finval = (double) src.getValue(i, j) * (double) scal;
                src.setValue((float) finval, i, j);
                }
            }
        }

    public float[][] multiplyNetworks(Network first, Network second)
        {
        int size = first.getSize();
        int i, j, k;
        double temp = 0; // temporary variable
        float[][] finmat = new float[size][size]; // final result
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                temp = 0;
                for (k = 0; k < size; k++)
                    {
                    temp += (double) first.getValue(i, k)
                            * (double) second.getValue(k, j);
                    // temp += first.getValue(k,i) * second.getValue(j,k); //
                    // veche
                    }
                
                finmat[i][j] = (float) temp;
                }
            }
        return finmat;
        }

    // returns multiplication as boolean matrix
    public boolean[][] multiplyMatricesBoolean(float[][] first, float[][] second)
        {
        requireNonEmptySquare(first, "multiplyMatricesBoolean(first)");
        requireNonEmptySquare(second, "multiplyMatricesBoolean(second)");
        int size = first.length;
        int i, j, k;
        float temp = 0f; // temporary variable
        boolean[][] finmat = new boolean[size][size]; // final result
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                // structural product: true when some k reaches j from i
                finmat[i][j] = false;
                for (k = 0; k < size; k++)
                    {
                    if (first[i][k] != 0f && second[k][j] != 0f)
                        {
                        finmat[i][j] = true;
                        break;
                        }
                    }
                }
            }
        return finmat;
        }

    public float[][] multiplyMatrices(float[][] first, float[][] second)
        {
        requireNonEmptySquare(first, "multiplyMatrices(first)");
        requireNonEmptySquare(second, "multiplyMatrices(second)");
        int size = first.length;
        int i, j, k;
        double temp; // temporary variable
        float[][] finmat = new float[size][size]; // final result
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                temp = 0;
                for (k = 0; k < size; k++)
                    {
                    temp += (double) first[i][k] * (double) second[k][j];
                    }
                finmat[i][j] = (float) temp;
                if (!Float.isFinite(finmat[i][j]))
                    throw new ArithmeticException("multiplyMatrices: non-finite "
                            + "result at [" + i + "][" + j + "]");
                }
            }
        return finmat;
        }

    public int[][] multiplyMatrices(int[][] first, int[][] second)
        {
        requireNonEmptySquare(first, "multiplyMatrices(first)");
        requireNonEmptySquare(second, "multiplyMatrices(second)");
        int size = first.length;
        int i, j, k;
        long temp; // overflow-safe accumulation (2.1.3)
        int[][] finmat = new int[size][size]; // final result
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                temp = 0;
                for (k = 0; k < size; k++)
                    {
                    temp += (long) first[i][k] * (long) second[k][j];
                    }
                if (temp < Integer.MIN_VALUE || temp > Integer.MAX_VALUE)
                    throw new ArithmeticException("multiplyMatrices: integer "
                            + "overflow at [" + i + "][" + j + "]");
                finmat[i][j] = (int) temp;
                }
            }
        return finmat;
        }

    // probably never used:
    public void initializeStringMatrix(String[][] tmp_str)
        {
        int size = tmp_str.length;
        for (int i = 0; i < size; i++)
            {
            for (int j = 0; j < size; j++)
                {
                tmp_str[i][j] = "";
                }
            }
        }

    private int firstOccurence(StringBuffer str, char c, int initial)
        {
        int i = initial;
        int len = str.length();
        int goal = -1;
        if (initial > len)
            return goal;
        do
            {
            if (str.charAt(i) == c)
                {
                goal = i;
                return goal;
                }
            i++;
            } while (goal == -1 || i < len);
        return goal;
        }

    

/**
     * Enumerates the maximal n-cliques of the network: maximal sets of nodes
     * whose mutual geodesic distance is at most cdiam.
     * 
     * <p>
     * Implemented in 2.1.3 — the 2.1.2 body was an unimplemented stub
     * (returning null), which is why the menu entry was disabled.
     * 
     * @return a Vector of IntLists, or null for an invalid diameter
     */
    private List<IntList> cliquesMain(Network src, int cdiam)
        {
        int size = src.getSize();
        if (cdiam < 1 || size < 1)
            return null;
        clique_search_cancelled = false;
        clique_truncated = false;

        int[][] geod = geodesics(src);
        // derived graph: two nodes are "adjacent" when their mutual geodesic
        // distance is at most cdiam. NB: geodesics() uses 0 for "no path"
        // (and for the diagonal), so only distances > 0 count as reachable.
        boolean[][] adjacent = new boolean[size][size];
        for (int i = 0; i < size; i++)
            {
            for (int j = 0; j < size; j++)
                {
                if (i != j && geod[i][j] > 0 && geod[j][i] > 0
                        && geod[i][j] <= cdiam && geod[j][i] <= cdiam)
                    {
                    adjacent[i][j] = true;
                    }
                }
            }

        List<IntList> cliques = new ArrayList<>();
        int[] candidates = new int[size];
        for (int i = 0; i < size; i++)
            {
            candidates[i] = i;
            }
        bronKerbosch(adjacent, cliques, new IntList(), candidates, size,
                new int[size], 0);
        return cliques;
        }

    public List<List<Integer>> getCliquesList(Network src, int cdiam)
        {
        List<IntList> raw = cliquesMain(src, cdiam);
        if (raw == null)
            return Collections.emptyList();
        List<List<Integer>> result = new ArrayList<>(raw.size());
        for (IntList il : raw)
            {
            int[] arr = il.toArray();
            List<Integer> list = new ArrayList<>(arr.length);
            for (int val : arr)
                {
                list.add(val);
                }
            result.add(list);
            }
        return result;
        }

    /**
     * Bron-Kerbosch maximal clique enumeration on the derived graph (ordered
     * variant, so every maximal clique is reported exactly once).
     */
    /**
     * 2.1.3: pivot-based (Tomita) Bron-Kerbosch - substantially faster than
     * the ordered variant on mid-density graphs, checks the cancellation flag
     * so the UI can abort long enumerations.
     */
    private void bronKerbosch(boolean[][] adjacent, List<IntList> cliques,
            IntList current, int[] candidates, int candidate_count,
            int[] excluded, int excluded_count)
        {
        if (clique_search_cancelled)
            {
            return; // aborted by the user
            }
        if (candidate_count == 0)
            {
            if (excluded_count == 0 && current.getSize() >= 2)
                {
                if (cliques.size() >= MAX_REPORTED_CLIQUES)
                    {
                    clique_search_cancelled = true; // stop the search
                    clique_truncated = true; // cap reached: list incomplete
                    return;
                    }
                cliques.add(current.getClone());
                }
            return;
            }

        // pivot u in P union X with maximum |P intersect N(u)|
        int pivot = -1;
        int pivot_neighbours = -1;
        for (int pi = 0; pi < candidate_count; pi++)
            {
            int u = candidates[pi];
            int cnt = 0;
            for (int qi = 0; qi < candidate_count; qi++)
                {
                if (adjacent[u][candidates[qi]])
                    {
                    cnt++;
                    }
                }
            if (cnt > pivot_neighbours)
                {
                pivot_neighbours = cnt;
                pivot = u;
                }
            }
        for (int ei = 0; ei < excluded_count; ei++)
            {
            int u = excluded[ei];
            int cnt = 0;
            for (int qi = 0; qi < candidate_count; qi++)
                {
                if (adjacent[u][candidates[qi]])
                    {
                    cnt++;
                    }
                }
            if (cnt > pivot_neighbours)
                {
                pivot_neighbours = cnt;
                pivot = u;
                }
            }

        // iterate over P minus N(pivot)
        for (int k = 0; k < candidate_count; k++)
            {
            int node = candidates[k];
            if (pivot >= 0 && adjacent[pivot][node])
                {
                continue; // skipped by the pivot
                }
            if (clique_search_cancelled)
                {
                return;
                }
            IntList next_current = current.getClone();
            next_current.appendValue(node);

            int[] next_candidates = new int[candidate_count];
            int next_count = 0;
            for (int m = 0; m < candidate_count; m++)
                {
                if (m != k && adjacent[node][candidates[m]])
                    {
                    next_candidates[next_count++] = candidates[m];
                    }
                }
            int[] next_excluded = new int[excluded_count + candidate_count];
            int next_x_count = 0;
            for (int m = 0; m < excluded_count; m++)
                {
                if (adjacent[node][excluded[m]])
                    {
                    next_excluded[next_x_count++] = excluded[m];
                    }
                }
            bronKerbosch(adjacent, cliques, next_current, next_candidates,
                    next_count, next_excluded, next_x_count);

            // move the processed node into the excluded set of this frame
            excluded[excluded_count++] = node;
            }
        }

    /*
     * Shortest-path enumeration based on an earlier AGNA approach by
     * Adrian Duda. The current implementation was rewritten for
     * AGNA 2.1.3.
     */
    private Vector shortestPaths(Network src, int init, int end)
        {
        shortest_paths_truncated = false;
        if (src.hasNoEmission(init) || src.hasNoReception(end))
            return null;
        Vector siruri_actuale = new Vector(1, 1); // contains only one element
        Vector siruri_noi = new Vector(1, 1);
        Vector siruri_bune = new Vector(1, 1);
        siruri_actuale.removeAllElements();
        siruri_noi.removeAllElements();
        siruri_bune.removeAllElements();

        // initialization:
        int nr_siruri = 1; // number of elements in siruri_acuale
        int ind = 0;
        int n = src.getSize(); // size of network/matrix
        boolean[][] mat = new boolean[n][n];
        mat = src.getBooleanMatrix(); // network's boolean matrix
        IntList tmpstr; // lista de lucru
        IntList drum;
        tmpstr = new IntList();
        tmpstr.appendValue(init);
        siruri_actuale.addElement(tmpstr);
        boolean gasit = false;
        while (gasit == false)
            {
            if (siruri_bune.size() >= MAX_SHORTEST_PATHS)
                {
                shortest_paths_truncated = true; // cap reached: incomplete
                break;
                }
            for (int i = 0; i < siruri_actuale.size(); i++)
                {
                tmpstr = (IntList) siruri_actuale.elementAt(i);
                ind = tmpstr.getLastElement().getValue();
                for (int k = 0; k < n; k++)
                    {
                    if (mat[ind][k])
                        {
                        if (tmpstr.getIndexOf(k) < 0)
                            {
                            drum = null;
                            drum = tmpstr.getClone();
                            drum.appendValue(k);
                            siruri_noi.addElement(drum);
                            if (k == end)
                                {
                                gasit = true;
                                siruri_bune.addElement(drum);
                                }
                            }
                        }
                    }
                }
            siruri_actuale.removeAllElements();

            // echivalentul atribuirii: siruri_actuale = siruri_noi
            if (siruri_noi.size() > 0)
                {
                for (int k = 0; k < siruri_noi.size(); k++)
                    {
                    siruri_actuale
                            .addElement((IntList) siruri_noi.elementAt(k));
                    }
                }

            siruri_noi.removeAllElements();
            if (siruri_actuale.size() == 0)
                {
                break; // out of while
                }
            }
        tmpstr.deleteAll();
        tmpstr = null;
        return siruri_bune;
        }

    

    /**
     * 2.1.3: true when every ordered pair of distinct nodes is mutually
     * reachable; uses the geodesics convention (0 = no path).
     */

    /**
     * 2.1.3: Pajek *Vector blocks for the per-node measures (appended to an
     * exported .net file). Connectivity-dependent measures are only included
     * for connected networks.
     */
    public String getPajekVectors(Network src)
        {
        return getPajekVectors(src, null);
        }

    /**
     * 2.1.3: Pajek *Vector export limited to the labels listed in
     * selected (null = all available).
     */
    public String getPajekVectors(Network src, java.util.Vector selected)
        {
        StringBuffer out = new StringBuffer("");
        addPajekVectorIfSelected(out, selected, "Emission Degree",
                emissionDegree(src));
        addPajekVectorIfSelected(out, selected, "Reception Degree",
                receptionDegree(src));
        addPajekVectorIfSelected(out, selected, "Weighted Emission Degree",
                weightedEmissionDegree(src));
        addPajekVectorIfSelected(out, selected, "Sociometric Status",
                sociometricStatus(src));
        addPajekVectorIfSelected(out, selected, "Nodal Degree",
                nodalDegree(src));
        addPajekVectorIfSelected(out, selected, "Betweenness",
                betweenness(src));
        if (isConnected(src))
            {
            addPajekVectorIfSelected(out, selected, "Closeness",
                    closeness(src));
            addPajekVectorIfSelected(out, selected, "Prestige",
                    prestige(src));
            }
        return out.toString();
        }

    private void addPajekVectorIfSelected(StringBuffer out,
            java.util.Vector selected, String label, float[] values)
        {
        if (values == null)
            {
            return;
            }
        if (selected == null || selected.contains(label))
            {
            appendPajekVector(out, label, values);
            }
        }

    private void appendPajekVector(StringBuffer out, String label,
            float[] values)
        {
        out.append("% " + label + "\n" + "*Vector " + values.length + "\n");
        for (int i = 0; i < values.length; i++)
            {
            out.append(String.valueOf(values[i]) + "\n");
            }
        }

    public boolean isConnected(Network src)
        {
        return isWeaklyConnected(src);
        }

    public boolean isWeaklyConnected(Network src)
        {
        int n = src.getSize();
        if (n <= 1)
            return true;
        boolean[] seen = new boolean[n];
        int[] q = new int[n];
        int head = 0, tail = 0;
        q[tail++] = 0;
        seen[0] = true;
        int visited = 0;
        while (head < tail)
            {
            int u = q[head++];
            visited++;
            for (int v = 0; v < n; v++)
                {
                if (!seen[v] && (src.getValue(u, v) != 0f || src.getValue(v, u) != 0f))
                    {
                    seen[v] = true;
                    q[tail++] = v;
                    }
                }
            }
        return visited == n;
        }

    public boolean isStronglyConnected(Network src)
        {
        int size = src.getSize();
        if (size <= 1)
            return true;
        int[][] geod = geodesics(src);
        for (int i = 0; i < size; i++)
            {
            for (int j = 0; j < size; j++)
                {
                if (i != j && geod[i][j] == 0)
                    {
                    return false;
                    }
                }
            }
        return true;
        }

    public int[][] geodesics(Network src)
        {
        int size = src.getSize();
        int i, j, k;
        final int INF = Integer.MAX_VALUE / 2; // sentinel; no overflow on +1
        int[][] ge = new int[size][size];
        boolean[][] bb = src.getBooleanMatrix();

        // distance matrix: 1 hop for direct arcs, INF otherwise, 0 on the
        // diagonal
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                ge[i][j] = (i == j) ? 0 : (bb[i][j] ? 1 : INF);
                }
            }

        // 2.1.3: Floyd-Warshall replaces the successive matrix multiplication
        // (O(n^3) instead of O(n^4), same shortest-hop distances)
        for (k = 0; k < size; k++)
            {
            for (i = 0; i < size; i++)
                {
                for (j = 0; j < size; j++)
                    {
                    if (ge[i][k] + ge[k][j] < ge[i][j])
                        {
                        ge[i][j] = ge[i][k] + ge[k][j];
                        }
                    }
                }
            }

        // legacy convention: 0 = unreachable (and self); distances stay > 0
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                if (ge[i][j] >= INF)
                    {
                    ge[i][j] = 0;
                    }
                }
            }

        return ge;
        }

    


    /**
     * 2.1.3: Full Analysis - the complete standard battery in one report,
     * reusing the per-measure outputs and their conventions. Distance-based
     * measures are only included for connected networks.
     */
    public String outFullAnalysis(Network outsrc)
        {
        return outFullAnalysis(outsrc, FullAnalysisOptions.createStandard());
        }

    public String outFullAnalysis(Network outsrc, FullAnalysisOptions options)
        {
        String legacy = legacyOutFullAnalysis(outsrc, options);
        AnalysisReport report = AnalysisReportFactory.createFullAnalysisReport(outsrc, this, options, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutFullAnalysis(Network outsrc)
        {
        return legacyOutFullAnalysis(outsrc, FullAnalysisOptions.createStandard());
        }

    public String legacyOutFullAnalysis(Network outsrc, FullAnalysisOptions options)
        {
        if (options == null)
            options = FullAnalysisOptions.createStandard();
        StringBuffer out = new StringBuffer("");
        out.append(it + bold + "FULL ANALYSIS" + unbold + " of " + unit
                + outsrc.getName() + lb);
        if (options.isIncludeBasicAndDensity())
            {
            out.append(outBasic(outsrc));
            out.append(outDensity(outsrc));
            }
        if (options.isIncludeClusteringAndCores())
            {
            out.append(outCohesion(outsrc));
            }
        if (options.isIncludeDegreeMetrics())
            {
            out.append(outNodalDegree(outsrc));
            out.append(outEmissionDegree(outsrc));
            out.append(outReceptionDegree(outsrc));
            out.append(outInDegree(outsrc));
            out.append(outOutDegree(outsrc));
            }
        if (options.isIncludePrestige())
            {
            out.append(outSociometricStatus(outsrc));
            out.append(outDeterminationDegree(outsrc));
            out.append(outPrestige(outsrc));
            }
        if (options.isIncludeGeodesicCentralities())
            {
            out.append(outDiameter(outsrc));
            out.append(outBavelas(outsrc));
            out.append(outFareness(outsrc));
            out.append(outCloseness(outsrc));
            out.append(outBetweenness(outsrc));
            out.append(outEccentricity(outsrc));
            out.append(outGeodesics(outsrc));
            if (!isStronglyConnected(outsrc))
                {
                out.append(lb
                        + it
                        + "Note: Network is not strongly connected. Geodesic and "
                        + "distance-based measures reflect reachable paths only." + unit + lb);
                }
            }
        if (options.isIncludeSpectralCentrality())
            {
            out.append(legacyOutEigenvector(outsrc));
            }
        if (options.isIncludeClusteringAndCores())
            {
            out.append(legacyOutLocalClustering(outsrc));
            out.append(legacyOutTransitivity(outsrc));
            out.append(legacyOutKCore(outsrc));
            }
        if (options.isIncludeStructuralHoles())
            {
            out.append(legacyOutStructuralHoles(outsrc));
            }
        if (options.isIncludeConnectivityAndDyad())
            {
            out.append(legacyOutConnectivity(outsrc));
            }
        if (options.isIncludeBonacich())
            {
            out.append(legacyOutBonacich(outsrc, options.getBonacichBeta()));
            }
        if (options.isIncludePageRank())
            {
            out.append(legacyOutPageRank(outsrc, options.getPageRankDamping()));
            }
        if (options.isIncludeTriadCensus())
            {
            out.append(legacyOutTriadCensus(outsrc));
            }
        if (options.isIncludeCliques())
            {
            out.append(outCliques(outsrc, options.getCliqueDiameter()));
            } else
            {
            out.append(lb + it + "Cliques: use Analysis > N-Cliques for the "
                    + "clique report." + unit + lb);
            }
        return out.toString();
        }

    public String outGeodesics(Network outsrc)
        {
        String legacy = legacyOutGeodesics(outsrc);
        AnalysisReport report = AnalysisReportFactory.createGeodesicsReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutGeodesics(Network outsrc)
        {
        AgnaLib.initAjna();
        int size = outsrc.getSize();
        int i, j;
        StringBuffer out = new StringBuffer("");
        int[][] outgeo = new int[size][size];
        outgeo = geodesics(outsrc);
        out.append(it + bold + "Matrix of Geodesics" + unbold + " in " + unit
                + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + "--" + untd);
        for (i = 0; i < size; i++)
            {
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd);
            }
        out.append(untr);
        for (i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd);
            for (j = 0; j < size; j++)
                {
                out.append(td + String.valueOf(outgeo[i][j]) + untd);
                }
            out.append(untr);
            }
        out.append(untable);
        out.append(it
                + "Please note that a zero element in the geodesic matrix" + lb
                + "is only a convention and means that there is no connection"
                + lb + "between the corresponding nodes. In practice, it" + lb
                + "should rather be interpreted as an infinite value." + unit
                + lb + lb);

        // Preaparing geodesic data for statistics:
        int new_size;
        try
            {
            new_size = size * size;
            } catch (Exception e32)
            {
            return out.toString();
            }

        float[] linear_geo = new float[new_size];
        int linear_cursor = 0;
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                if (outgeo[i][j] != 0)
                    {
                    linear_geo[linear_cursor] = (float) outgeo[i][j];
                    linear_cursor++;
                    }
                }
            }
        outgeo = null;
        // matrix data has now been transfered
        // into array linear_geo

        if (linear_cursor < new_size)
            {
            // linear_geo contains zero values at the end;
            // they need to be removed
            float[] new_linear_geo = new float[linear_cursor];
            for (i = 0; i < linear_cursor; i++)
                new_linear_geo[i] = linear_geo[i];
            linear_geo = null;
            out.append(outStatistics(new_linear_geo, true));
            out
                    .append(it
                            + "A number of "
                            + String.valueOf(new_size - linear_cursor)
                            + " matrix elements have been ignored in these"
                            + lb
                            + "statistics, as they refer to unconnected pairs of nodes."
                            + unit + lb);
            } else
            out.append(outStatistics(linear_geo, true));

        return out.toString();
        }

    public String outEccentricity(Network outsrc)
        {
        String legacy = legacyOutEccentricity(outsrc);
        AnalysisReport report = AnalysisReportFactory.createEccentricityReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutEccentricity(Network outsrc)
        {
        int size = outsrc.getSize();
        int i;
        StringBuffer out = new StringBuffer("");
        if (!isStronglyConnected(outsrc))
            {
            out.append(it + "Note: " + outsrc.getName()
                    + " is not strongly connected. Eccentricity is computed on reachable paths only." + unit + lb);
            }
        float[] outecc = new float[size];
        AgnaLib agna_lib = new AgnaLib();
        outecc = agna_lib.eccentricity(outsrc);
        out.append(it + bold + "Distribution of Eccentricity" + unbold + " in "
                + unit + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + it + "Node" + unit + untd + td + it + "Eccentricity"
                + unit + untd + untr);
        for (i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outecc[i]) + untd);
            out.append(untr);
            }
        out.append(untable);
        out.append(outStatistics(outecc, true));

        return out.toString();
        }

    public String outDiameter(Network outsrc)
        {
        String legacy = legacyOutDiameter(outsrc);
        AnalysisReport report = AnalysisReportFactory.createDiameterReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutDiameter(Network outsrc)
        {
        String out = new String("");
        out = it + bold + "Diameter" + unbold + " of " + unit
                + outsrc.getName();
        out += lb + it + "Diameter = " + unit
                + String.valueOf(matrixMax(geodesics(outsrc)));
        out += lb;
        return out;
        }

    public float[] eccentricity(Network src)
        {
        int size = src.getSize();
        int i, j;
        int[][] geomat = new int[size][size];
        geomat = geodesics(src);
        float[] finarray = new float[size];
        for (i = 0; i < size; i++)
            {
            finarray[i] = Float.NEGATIVE_INFINITY;
            }
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                if (finarray[i] < geomat[i][j])
                    {
                    finarray[i] = geomat[i][j];
                    }
                }
            }
        return finarray;
        }

    // sum of all elements of row i in the geodesics matrix
    public float[] fareness(Network src)
        {
        int size = src.getSize();
        int i, j;
        int[][] geomat = geodesics(src);
        float[] finarray = new float[size];
        for (i = 0; i < size; i++)
            {
            finarray[i] = 0f;
            for (j = 0; j < size; j++)
                {
                finarray[i] += (float) geomat[i][j];
                }
            }
        return finarray;
        }

    public String outFareness(Network outsrc)
        {
        String legacy = legacyOutFareness(outsrc);
        AnalysisReport report = AnalysisReportFactory.createFarenessReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutFareness(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        if (!isStronglyConnected(outsrc))
            {
            out.append(it + "Note: " + outsrc.getName()
                    + " is not strongly connected. Fareness is computed on reachable paths only." + unit + lb);
            }
        float[] outarray =  fareness(outsrc);
        out.append(it + bold + "Distribution of Fareness Centrality" + unbold
                + " in " + unit + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + it + "Node" + unit + untd + td + it + "Fareness" + unit
                + untd + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outarray[i]) + untd);
            out.append(untr);
            }
        out.append(untable);

        // freeman centralization:
        out
                .append(it + bold + "Freeman (1979) Fareness Centralization = " + unbold
                        + unit);
        out.append(String.valueOf(freemanGeneralIndex(outarray,
                fareness(new Network(size, 1)))));
        out.append(lb + lb);

        out.append(outStatistics(outarray, true));
        return out.toString();
        }

    // closeness: the inverse of fareness;
    // standard value: closeness multiplied by (size - 1)
    // 2.2.2: Wasserman-Faust correction. For an actor that reaches r of the (size - 1) other actors with
    // total distance f, the standardised value is (r / (size - 1)) * (r / f), always within [0, 1].
    // Here the unstandardised value is returned (the reports multiply by size - 1), i.e. (r / (size - 1))^2 / f.
    // On strongly connected networks r = size - 1 and the result equals 1 / f, as in 2.2.1.
    public float[] closeness(Network src)
        {
        int size = src.getSize();
        int i, j;
        float[] finarray =  fareness(src);
        int[][] geomat = geodesics(src);
        for (i = 0; i < size; i++)
            {
            if (finarray[i] != 0f)
                {
                int reached = 0;
                for (j = 0; j < size; j++)
                    {
                    if (j != i && geomat[i][j] > 0)
                        reached++;
                    }
                double share = size > 1 ? reached / (double) (size - 1) : 1.0;
                finarray[i] = (float) (share * share / (double) finarray[i]);
                }
            }
        return finarray;
        }

    public String outCloseness(Network outsrc)
        {
        String legacy = legacyOutCloseness(outsrc);
        AnalysisReport report = AnalysisReportFactory.createClosenessReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutCloseness(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        if (!isStronglyConnected(outsrc))
            {
            out.append(it + "Note: " + outsrc.getName()
                    + " is not strongly connected. Closeness centrality is computed on reachable paths only (Wasserman-Faust normalisation, so values never exceed 1)." + unit + lb);
            }

        float[] outarray =  closeness(outsrc);
        out.append(it + bold + "Distribution of Closeness Centrality" + unbold
                + " in " + unit + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + it + "Node" + unit + untd + td + it + "Closeness"
                + unit + untd + td + it + "Standard Closeness" + unit + untd
                + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outarray[i]) + untd + td
                    + String.valueOf(outarray[i] * (size - 1)) + untd);
            out.append(untr);
            }
        out.append(untable);

        // freeman centralization:
        out
                .append(it + bold + "Freeman (1979) Closeness Centralization (C_C) = " + unbold
                        + unit);
        out.append(String.valueOf(freemanGeneralIndex(outarray,
                closeness(new Network(size, 1)))));
        out.append(lb + lb);

        // statistics
        out.append(it + bold + "Closeness Statistics" + unbold + unit);
        out.append(lb + outStatistics(outarray, false));
        try
            {
            Thread.sleep(200);
            } catch (Exception ee) {
      AgnaLog.warn("suppressed exception", ee);
      }
        for (int i = 0; i < size; i++)
            {
            outarray[i] = outarray[i] * (size - 1);
            }
        out.append(it + bold + "Standard Closeness Statistics" + unbold + unit);
        out.append(lb + outStatistics(outarray, false));
        return out.toString();
        }

    // returns a matrix whose elements represent the number
    // of geodesics from i to j
    

    // returns a matrix whose elements represent the number
    // of geodesics from i to j


    // betweenness of node k:
    // sum of the ratios of the number of geodesic paths between (any pair) i
    // and j
    // involving node k to the number of all geodesic paths between i and j.
    // 2.1.3: Brandes' algorithm (O(n*m)) replaces the exhaustive
    // shortest-path enumeration; values are identical raw (unnormalized)
    // directed Freeman betweenness.
    public float[] betweenness(Network src)
        {
        int n = src.getSize();
        boolean[][] mat = src.getBooleanMatrix();
        float[] cb = new float[n];
        IntList[] pred = new IntList[n];
        for (int v = 0; v < n; v++)
            pred[v] = new IntList();
        double[] dep = new double[n];
        int[] order = new int[n];
        int[] dist = new int[n];
        double[] sigma = new double[n];

        for (int s = 0; s < n; s++)
            {
            for (int v = 0; v < n; v++)
                {
                pred[v].deleteAll();
                dist[v] = -1;
                sigma[v] = 0.0;
                dep[v] = 0.0;
                }
            sigma[s] = 1.0;
            dist[s] = 0;

            int head = 0;
            int tail = 0;
            order[tail++] = s;

            while (head < tail)
                {
                int v = order[head++];
                boolean[] row = mat[v];
                for (int w = 0; w < n; w++)
                    {
                    if (!row[w])
                        {
                        continue;
                        }
                    if (dist[w] < 0)
                        {
                        dist[w] = dist[v] + 1;
                        order[tail++] = w;
                        }
                    if (dist[w] == dist[v] + 1)
                        {
                        sigma[w] += sigma[v];
                        pred[w].appendValue(v);
                        }
                    }
                }

            for (int i = tail - 1; i > 0; i--)
                {
                int w = order[i];
                IntListElement pre = pred[w].getFirstElement();
                double coeff = (1.0 + dep[w]) / sigma[w];
                while (pre != null)
                    {
                    int v = pre.getValue();
                    dep[v] += sigma[v] * coeff;
                    pre = pre.getNext();
                    }
                if (w != s)
                    {
                    cb[w] += (float) dep[w];
                    }
                }
            }
        return cb;
        }


    

    // string-based;
    

    public String outShortestPaths(Network outsrc, int i_from, int i_to)
        {
        String legacy = legacyOutShortestPaths(outsrc, i_from, i_to);
        AnalysisReport report = AnalysisReportFactory.createShortestPathsReport(outsrc, i_from, i_to, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutShortestPaths(Network outsrc, int i_from, int i_to)
        {
        StringBuffer out = new StringBuffer("");
        IntList path;
        int size = outsrc.getSize();
        Vector sp = shortestPaths(outsrc, i_from, i_to);
        if (sp == null || sp.size() < 1 || i_from == i_to)
            {
            out.append(it + "No path found between selected nodes." + unit);
            return out.toString();
            }
        final int no_paths = sp.size(); // number of shortest paths
        String plural;
        if (no_paths == 1)
            plural = "";
        else
            plural = "s";
        out.append(it + bold + "Shortest Path" + plural + unbold
                + " from node " + unit + outsrc.getActor(i_from).getName() + it
                + " to node " + unit + outsrc.getActor(i_to).getName() + lb);
        // out += ol;
        for (int i = 0; i < no_paths; i++)
            {
            // out += li + blanc+blanc+blanc; // node separation
            out
                    .append(lb + blanc + blanc + blanc + blanc + blanc + "*"
                            + blanc);
            path = (IntList) sp.elementAt(i);
            if (path != null)
                {
                IntListElement cursor = path.getFirstElement();
                while (cursor != null) // so long list end is not reached
                    {
                    try
                        {
                        out
                                .append(outsrc.getActor(cursor.getValue())
                                        .getName());
                        out.append(blanc + blanc + blanc);
                        } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                    cursor = cursor.getNext();
                    } // end while
                } // end if
            // out += unli;
            } // end for i
        // out += unol;
        if (clique_search_cancelled)
            {
            out.append(lb + "*** Clique list incomplete (cancelled or the "
                    + MAX_REPORTED_CLIQUES
                    + "-clique cap was reached); showing what was found so "
                    + "far. ***");
            }
        out.append(lb);
        return out.toString();
        }

    public String outCliques(Network outsrc, int clique_diameter)
        {
        String legacy = legacyOutCliques(outsrc, clique_diameter);
        AnalysisReport report = AnalysisReportFactory.createCliquesReport(outsrc, clique_diameter, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutCliques(Network outsrc, int clique_diameter)
        {
        StringBuffer out = new StringBuffer("");

        IntList clique;
        int size = outsrc.getSize();
        List<IntList> final_cliques = cliquesMain(outsrc, clique_diameter);
        if (final_cliques == null || final_cliques.size() < 1)
            {
            out.append(it + "No " + String.valueOf(clique_diameter)
                    + "-cliques found in current network." + unit);
            return out.toString();
            }
        out.append(it + bold + String.valueOf(clique_diameter) + "-Cliques"
                + unbold + " found in " + unit + outsrc.getName() + lb);
        // out += ol;
        for (int i = 0; i < final_cliques.size(); i++)
            {
            // out += li + blanc+blanc+blanc; // node separation
            out
                    .append(lb + blanc + blanc + blanc + blanc + blanc + "*"
                            + blanc);
            clique = final_cliques.get(i);
            if (final_cliques != null)
                {
                IntListElement cursor = clique.getFirstElement();
                while (cursor != null) // so long list end is not reached
                    {
                    try
                        {
                        out
                                .append(outsrc.getActor(cursor.getValue())
                                        .getName());
                        out.append(blanc + blanc + blanc);
                        } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                    cursor = cursor.getNext();
                    } // end while
                } // end if
            // out += unli;
            } // end for i
        // out += unol;
        if (clique_truncated)
            out.append(lb + it + "Showing the first "
                    + MAX_REPORTED_CLIQUES
                    + " maximal cliques; enumeration was stopped and this "
                    + "list is incomplete." + unit);
        else if (clique_search_cancelled)
            out.append(lb + it + "Clique enumeration was cancelled; this "
                    + "list is incomplete." + unit);
        out.append(lb);

        
        return out.toString();
        }

    public String outBetweenness(Network outsrc)
        {
        String legacy = legacyOutBetweenness(outsrc);
        AnalysisReport report = AnalysisReportFactory.createBetweennessReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutBetweenness(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        if (!isStronglyConnected(outsrc))
            {
            out.append(it + "Note: " + outsrc.getName()
                    + " is not strongly connected. Betweenness is computed on all existing shortest paths." + unit + lb);
            }

        float[] outdet = betweenness(outsrc);
        if (outdet == null)
            return null;
        out.append(it + bold + "Distribution of Betweenness Centrality"
                + unbold + " in " + unit + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + it + "Node" + unit + untd + td + it + "Betweenness"
                + unit + untd + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outdet[i]) + untd);
            out.append(untr);
            }
        out.append(untable);

        // 2.2.2: state the counting convention next to the numbers
        out.append(it + "Note: raw betweenness is summed over ordered pairs (A to B and B to A are counted separately), "
                + "so on an undirected network it is twice the unnormalised value reported by NetworkX or igraph; "
                + "divide by 2 to compare." + unit + lb);

        // freeman centralization:
        out
                .append(it + bold + "Freeman (1979) Betweenness Centralization (C_B) = " + unbold
                        + unit);
        out.append(String.valueOf(freemanGeneralIndex(outdet,
                betweenness(new Network(size, 1)))));
        out.append(lb + lb);

        out.append(outStatistics(outdet, true));
        return out.toString();
        }

    public float[] prestige(Network src)
        {
        // 2.1.3: implemented as proximity prestige (Lin 1976):
        //   P(i) = [I_i / (n-1)] * [ sum_{j in I_i} d(j,i) / I_i ]^-1
        // i.e. (share of nodes that can reach i) x (average closeness of
        // those nodes). The 2.1.2 body was marked "nu-i gata!" (unfinished)
        // and returned row-sums of the geodesics matrix (i.e. fareness).
        int size = src.getSize();
        int[][] geod = geodesics(src);
        float[] finarray = new float[size];
        for (int i = 0; i < size; i++)
            {
            int reach = 0;
            double dist_sum = 0;
            for (int j = 0; j < size; j++)
                {
                if (j != i && geod[j][i] > 0)
                    {
                    reach++;
                    dist_sum += (double) geod[j][i];
                    }
                }
            if (reach == 0 || size < 2)
                {
                finarray[i] = 0f; // nobody can reach i: no prestige
                } else
                {
                finarray[i] = (float) (((double) reach / (double) (size - 1)) * ((double) reach
                        / dist_sum));
                }
            }
        return finarray;
        }

    public String outPrestige(Network outsrc)
        {
        String legacy = legacyOutPrestige(outsrc);
        AnalysisReport report = AnalysisReportFactory.createPrestigeReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutPrestige(Network outsrc)
        {
        // 2.1.3: the 2.1.2 body was an unimplemented stub returning null
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        float[] outpr = prestige(outsrc);
        out.append(it + bold + "Distribution of Proximity Prestige" + unbold
                + " in " + unit + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + it + "Node" + unit + untd + td + it + "Prestige" + unit
                + untd + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outpr[i]) + untd);
            out.append(untr);
            }
        out.append(untable);
        out.append(outStatistics(outpr, true));
        out.append("\n");
        return out.toString();
        }

    // binary (boolean) bavelas:
    // = the total length of all geodesic paths starting or ending in node i
    // divided by the sum of all geodesic paths in the network
    public float[] bavelas(Network src)
        {
        int size = src.getSize();
        int i, j;
        int temp = 0;
        double[] finarray = new double[size];
        double numerator = 0;
        int[][] geomat = geodesics(src);

        for (i = 0; i < size; i++)
            {
            finarray[i] = 0;
            for (j = 0; j < size; j++)
                {
                finarray[i] += (double) geomat[i][j] + (double) geomat[j][i];
                numerator += (double) geomat[i][j];
                }
            }

        float[] to_return = new float[size];
        for (i = 0; i < size; i++)
            {
            if (finarray[i] == 0)
                to_return[i] = 0f;
            else
                to_return[i] = (float) ((double) numerator / (double) finarray[i]);
            }

        return to_return;
        }

    // weighted(boolean) bavelas:
    // = the ration of the sum of all connection values of node i
    // over the summ of all connection values in the network;
    public float[] weightedBavelas(Network src)
        {
        int size = src.getSize();
        int i, j;
        double[] finarray = new double[size];
        double numitor = 0;
        double tmp = 0;
        // float[][] mat = src.getMatrix();
        for (i = 0; i < size; i++)
            {
            finarray[i] = 0;
            for (j = 0; j < size; j++)
                {
                // numitor += (double)mat[i][j];
                // finarray[i] += (double)mat[i][j];
                tmp = (double) src.getValue(i, j);
                numitor += tmp;
                finarray[i] += tmp;
                }
            }
        if (numitor == 0)
            return null;
        float[] to_return = new float[size];
        for (i = 0; i < size; i++)
            {
            // finarray[i] = finarray[i] / numitor;
            to_return[i] = (float) ((double) finarray[i] / (double) numitor);
            }

        return to_return;
        }

    public String outBavelas(Network outsrc)
        {
        String legacy = legacyOutBavelas(outsrc);
        AnalysisReport report = AnalysisReportFactory.createBavelasReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutBavelas(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        if (!isStronglyConnected(outsrc))
            {
            out.append(it + "Note: " + outsrc.getName()
                    + " is not strongly connected. Bavelas-Leavitt centrality is computed on reachable paths only." + unit + lb);
            }

        float[] outarray = new float[size];
        if (outarray == null)
            {
            return it
                    + "Bavelas-Leavitt centrality cannot be computed for an empty network."
                    + unit;
            }
        float[] outrecwei = null;
        outarray = bavelas(outsrc);

        out.append(it + bold + "Distribution of Bavelas-Leavitt Centrality"
                + unbold + " in " + unit + outsrc.getName());
        out.append(lb + table + tr);

        // binary data:
        out.append(td + it + "Node" + unit + untd + td + it + "B-L Centrality"
                + unit + untd + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outarray[i]) + untd);
            out.append(untr);
            }

        /*
         * if (!AppRuntime.getCurrentWeight()) { // binary data: out += td + it +
         * "Node" + unit + untd + td + it + "B-L Centrality" + unit + untd +
         * untr; for (int i = 0; i < size; i++) { out += tr; out += td + it +
         * outsrc.getActor(i).getName() + unit + untd + td +
         * String.valueOf(outarray[i]) + untd; out += untr; } } else { //
         * weighted data: outrecwei = new float[size]; outrecwei =
         * weightedBavelas(outsrc); out += td + it + "Node" + unit + untd + td +
         * it + "B-L Centrality" + unit + untd + td + it + "Weighted B-L
         * Centrality" + unit + untd + untr; for (int i = 0; i < size; i++) {
         * out += tr; out += td + it + outsrc.getActor(i).getName() + unit +
         * untd + td + String.valueOf(outarray[i]) + untd + td +
         * String.valueOf(outrecwei[i]) + untd; out += untr; } }
         */

        out.append(untable);

        // bavelas centralization:
        out
                .append(it + bold + "Bavelas-Leavitt Centralization (C_Bav) = " + unbold
                        + unit);
        out.append(String.valueOf(freemanGeneralIndex(outarray,
                bavelas(new Network(size, 1)))));
        out.append(lb + lb);

        // generating statistics:
        out.append(it + bold + "Bavelas-Leavitt Centrality Statistics" + unbold
                + unit);
        out.append(lb + outStatistics(outarray, false));

        outarray = null;

        
        return out.toString();
        }

    // 2.1.3: normalization modes. The diagonal (no self-loops) always
    // stays zero in every mode.
    // 2.1.3: normalization modes. The diagonal (no self-loops) always
    // stays zero in every mode.
    public static final int NORMALIZE_BINARY = 0;
    public static final int NORMALIZE_THRESHOLD = 1;

    public void normalize(Network src)
        {
        requireFiniteNetwork(src, "normalize");
        normalize(src, NORMALIZE_BINARY, 0f);
        }

    public void normalize(Network src, int mode)
        {
        requireFiniteNetwork(src, "normalize");
        normalize(src, mode, 0f);
        }

    public void normalize(Network src, int mode, float threshold)
        {
        requireFiniteNetwork(src, "normalize");
        final int size = src.getSize();
        if (mode == NORMALIZE_THRESHOLD)
            {
            // 2.1.3: strictly-above dichotomization: x > threshold -> 1,
            // x <= threshold (including equal) -> 0
            for (int i = 0; i < size; i++)
                {
                for (int j = 0; j < size; j++)
                    {
                    if (i == j)
                        {
                        src.setValue(0f, i, j);
                        } else if (src.getValue(i, j) > threshold)
                        {
                        src.setValue(1f, i, j);
                        } else
                        {
                        src.setValue(0f, i, j);
                        }
                    }
                }
            return;
            }
        // binary (default): every non-zero value becomes 1
        for (int i = 0; i < size; i++)
            {
            for (int j = 0; j < size; j++)
                {
                if (i == j)
                    {
                    src.setValue(0f, i, j);
                    } else if (src.getValue(i, j) != 0f)
                    {
                    src.setValue(1f, i, j);
                    } else
                    {
                    src.setValue(0f, i, j);
                    }
                }
            }
        }

    public static final int NORMALIZE_MAXIMUM = 1;
    public static final int NORMALIZE_SUM = 2;
    public static final int NORMALIZE_ROW_MAXIMUM = 3;
    public static final int NORMALIZE_COLUMN_MAXIMUM = 4;



    public int getOusidersNumber(Network src)
        {
        int finval = 0;
        int size = src.getSize();
        for (int i = 0; i < size; i++)
            {
            if (src.isOutsider(i))
                finval++;
            }
        return finval;
        }

    public String outBasic(Network src)
        {
        String legacy = legacyOutBasic(src);
        AnalysisReport report = AnalysisReportFactory.createBasicReport(src, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutBasic(Network src)
        {
        // initial values
        final int size = src.getSize();
        final int n_edges = src.getEdgesNumber();
        final boolean is_symmetric = src.isSymmetric(); // ie, nondirected
        StringBuffer out = new StringBuffer("");
        float min = 0f;
        final int outsiders = getOusidersNumber(src);

        out.append(it + bold + "Basic description" + unbold + " of " + unit
                + src.getName());

        out.append(lb + it + "Number of nodes: " + unit + String.valueOf(size));
        if (size == 0)
            out.append(it + " (null network)." + unit);
        else if (size == 1)
            out.append(it + " (trivial network)." + unit);
        else if (size > 0 && outsiders > 0)
            out.append(lb + it + "Number of outsiders: " + unit
                    + String.valueOf(outsiders));
        if (!is_symmetric)
            out.append(lb + it + "Number of edges: " + unit
                    + String.valueOf(n_edges));
        else
            {
            out.append(lb + it + "Number of nondirected edges: " + unit
                    + String.valueOf((int) (n_edges / 2)));
            out.append(lb + it + "Number of directed edges: " + unit
                    + String.valueOf(n_edges));
            }
        if (n_edges == size * (size - 1))
            out.append(it + " (complete network)." + unit);
        if (n_edges == 0)
            out.append(it + " (empty network)." + unit);
        min = src.getMin();
        if (size != 0 && n_edges != 0)
            {
            out.append(it);
            if (src.getMax() == min)
                {
                out.append(lb + "This network is ");
                if (min == 1f)
                    out
                            .append("binary (ie, it can be represented by a zero-one matrix) ");
                else
                    out.append("uniform (ie, all edge values are equal to "
                            + String.valueOf(min) + ") ");
                } else
                out.append(lb + "This network is weighted (ie, nonuniform) ");
            if (is_symmetric)
                out.append("and symmetric (ie, possibly nondirected).");
            else
                out.append("and nonsymmetric (ie, directed).");
            out.append(unit);
            }
        // 2.1.3: connectivity at a glance
        if (isConnected(src))
            out.append(lb + it + "The network is connected." + unit);
        else
            out.append(lb + it + "The network is disconnected (not all "
                    + "pairs of nodes are mutually reachable)." + unit);
        out.append(lb);

        return out.toString();
        }

    // generates the frequencies of the newly opened chain:
    private int[][] openChainSummary(Network src)
        {
        int i, j, horizontal_1, vertical_1, horizontal_2, vertical_2;
        int size = src.getSize();
        int[][] adjacency = src.getIntegerMatrix();

        // first row: reflections included
        // second row: reflections excluded
        int[][] finarray = new int[2][size];

        for (i = 0; i < size; i++)
            {
            horizontal_1 = 0;
            vertical_1 = 0;
            horizontal_2 = 0;
            vertical_2 = 0;
            finarray[0][i] = 0;
            finarray[1][i] = 0;
            for (j = 0; j < size; j++)
                {
                // reflections included:
                horizontal_1 += adjacency[i][j];
                vertical_1 += adjacency[j][i];
                if (j != i)
                    {
                    // reflections excluded:
                    horizontal_2 += adjacency[i][j];
                    vertical_2 += adjacency[j][i];
                    }
                }
            finarray[0][i] = Math.max(horizontal_1, vertical_1);
            finarray[1][i] = Math.max(horizontal_2, vertical_2);
            }
        adjacency = null;

        return finarray;
        }

    // transforms a distribution of frequencies into a
    // distribution of probabilities
    private float[] frequenciesToProbabilities(int[] frequencies)
        {
        int size = frequencies.length;
        float[] probabilities = new float[size];
        int denominator = 0;

        for (int i = 0; i < size; i++)
            {
            denominator += frequencies[i];
            }

        for (int i = 0; i < size; i++)
            {
            probabilities[i] = (float) ((double) frequencies[i] / (double) denominator);
            }
        frequencies = null;

        return probabilities;
        }

    // transforms two distributions of frequencies into
    // distributions of probabilities
    private float[][] frequenciesToProbabilities(int[][] frequencies, int size)
        {
        float[][] probabilities = new float[2][size];
        int denominator_1 = 0;
        int denominator_2 = 0;

        for (int i = 0; i < size; i++)
            {
            denominator_1 += frequencies[0][i];
            denominator_2 += frequencies[1][i];
            }

        for (int i = 0; i < size; i++)
            {
            probabilities[0][i] = (float) ((double) frequencies[0][i] / (double) denominator_1);
            probabilities[1][i] = (float) ((double) frequencies[1][i] / (double) denominator_2);
            }
        frequencies = null;

        return probabilities;
        }

    // generates a description of the newly opened chain:
    public String outOpenChainSummary(Network outsrc)
        {
        String legacy = legacyOutOpenChainSummary(outsrc);
        AnalysisReport report = AnalysisReportFactory.createOpenChainReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutOpenChainSummary(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        int[][] frequencies = new int[2][size];
        float[][] probabilities = new float[2][size];
        frequencies = openChainSummary(outsrc);
        probabilities = frequenciesToProbabilities(frequencies, size);

        out.append(it + "New network created from chain file." + unit);
        out.append(lb + it + bold + "Network name: " + unbold + unit
                + outsrc.getName());
        out.append(lb + it + bold + "Number of sequence codes identified: "
                + unbold + unit + String.valueOf(size));
        out.append(lb);
        out.append(lb + it + bold + "Chain summary:" + unbold + unit);
        out.append(lb + table + tr);
        out.append(td + it + "Sequence code" + unit + untd + td + it
                + "Frequency" + lb + "(all occurrences)" + unit + untd + td
                + it + "Probability" + lb + "(all occurrences)" + unit + untd
                + td + it + "Frequency" + lb + "(repetitions ignored)" + unit
                + untd + td + it + "Probability" + lb + "(repetitions ignored)"
                + unit + untd + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(frequencies[0][i]) + untd + td
                    + String.valueOf(probabilities[0][i]) + untd + td
                    + String.valueOf(frequencies[1][i]) + untd + td
                    + String.valueOf(probabilities[1][i]) + untd + untr);
            }

        out.append(untable);

        // generating statistics:
        out.append(it + bold
                + "Sequence Frequency Statistics (all occurrences)" + unbold
                + unit);
        out.append(lb + outStatistics(frequencies[0], false));
        out.append(it + bold
                + "Sequence Probability Statistics (all occurrences)" + unbold
                + unit);
        out.append(lb + outStatistics(probabilities[0], false));
        out.append(it + bold
                + "Sequence Frequency Statistics (repetitions ignored)"
                + unbold + unit);
        out.append(lb + outStatistics(frequencies[1], false));
        out.append(it + bold
                + "Sequence Probability Statistics (repetitions ignored)"
                + unbold + unit);
        out.append(lb + outStatistics(probabilities[1], false));

        return out.toString();
        }

    // generates a description of the newly opened chain:
    

    // computes the network cohesion index:
    // divides the number of mutual choices in a binary directed matrix
    // by the maximum possible number of such choices (Knoke)
    public float cohesion(Network src)
        {
        int size = src.getSize();
        double finval = 0;
        if (size == 0)
            return 0f;
        boolean[][] mat = src.getBooleanMatrix();
        for (int i = 0; i < size; i++)
            {
            for (int j = i + 1; j < size; j++)
                if (mat[i][j] && mat[j][i])
                    finval += 1;
            }
        finval = 2 * finval / (double) (size * size - size);

        return (float) finval;
        }

    public String outCohesion(Network outsrc)
        {
        String legacy = legacyOutCohesion(outsrc);
        AnalysisReport report = AnalysisReportFactory.createCohesionReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutCohesion(Network outsrc)
        {
        String out = new String("");
        float val = cohesion(outsrc);

        out = it + bold + "Cohesion" + unbold + " of " + unit
                + outsrc.getName();
        out += lb + it + "Cohesion = " + unit + String.valueOf(val);

        return out;
        }

    // considers network as binary:
    public float[] emissionDegree(Network src)
        {
        int i, j;
        int size = src.getSize();
        float[] emis = new float[size];
        float[] finarray = new float[size];
        for (i = 0; i < size; i++)
            {
            emis = src.getEmissions(i);
            finarray[i] = 0f;
            for (j = 0; j < size; j++)
                {
                if (emis[j] != 0f)
                    finarray[i]++;
                }
            }
        return finarray;
        }

    // considers network as weighted:
    public float[] weightedEmissionDegree(Network src)
        {
        int i, j;
        int size = src.getSize();
        float[] emis = new float[size];
        float[] finarray = new float[size];
        double tmp = 0;
        for (i = 0; i < size; i++)
            {
            emis = src.getEmissions(i);
            // finarray[i] = 0;
            tmp = 0;
            for (j = 0; j < size; j++)
                {
                // finarray[i] += emis[j];
                tmp += (double) emis[j];
                }
            finarray[i] = (float) tmp;
            }

        return finarray;
        }

    public String outEmissionDegree(Network outsrc)
        {
        String legacy = legacyOutEmissionDegree(outsrc);
        AnalysisReport report = AnalysisReportFactory.createEmissionReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutEmissionDegree(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        float[] outemis =  weightedEmissionDegree(outsrc);
        out.append(it + bold + "Distribution of Emission Degree" + unbold
                + " in " + unit + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + it + "Node" + unit + untd + td + it + "Emission" + unit
                + untd + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outemis[i]) + untd);
            out.append(untr);
            }
        out.append(untable);
        out.append(outStatistics(outemis, true));
        return out.toString();
        }

    public String outOutDegree(Network outsrc)
        {
        String legacy = legacyOutOutDegree(outsrc);
        AnalysisReport report = AnalysisReportFactory.createOutDegreeReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutOutDegree(Network outsrc)
        {
        int size = outsrc.getSize();
        int i;
        StringBuffer out = new StringBuffer("");
        float[] outemis = null; // binary data, g
        float[] outemis_star = new float[size]; // binary data, g-1
        float[] outemiswei = null; // weighted data, g
        float[] outemiswei_star = new float[size]; // weighted data, g-1

        outemis = emissionDegree(outsrc);
        for (i = 0; i < size; i++)
            {
            outemis_star[i] = (float) ((double) outemis[i] / (double) (size - 1));
            outemis[i] = (float) ((double) outemis[i] / (double) (size));
            }

        out.append(it + bold + "Distribution of Outdegree" + unbold + " in "
                + unit + outsrc.getName());
        out.append(lb + blanc + blanc + blanc + blanc + blanc + it
                + "* relative to number of all other nodes (self excluded)"
                + unit);
        out.append(lb + blanc + blanc + blanc + blanc + blanc + it
                + "** relative to number of all nodes (self included)" + unit);
        out.append(lb + table + tr);

        if (!AppRuntime.getCurrentWeight())
            {
            // binary data:
            out.append(td + it + "Node" + unit + untd + td + it + "Outdegree*"
                    + unit + untd + td + it + "Outdegree**" + unit + untd
                    + untr);
            for (i = 0; i < size; i++)
                {
                out.append(tr);
                out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                        + td + String.valueOf(outemis[i]) + untd + td
                        + String.valueOf(outemis_star[i]) + untd);
                out.append(untr);
                }
            } else
            {
            // weighted data:
            outemiswei = new float[size];
            outemiswei = weightedEmissionDegree(outsrc);
            for (i = 0; i < size; i++)
                {
                outemiswei_star[i] = (float) ((double) outemiswei[i] / (double) (size - 1));
                outemiswei[i] = (float) ((double) outemiswei[i] / (double) (size));
                }
            out.append(td + it + "Node" + unit + untd + td + it + "Outdegree*"
                    + unit + untd + td + it + "Outdegree**" + unit + untd + td
                    + it + "Weighted Outdegree*" + unit + untd + td + it
                    + "Weighted Outdegree**" + unit + untd + untr);
            for (i = 0; i < size; i++)
                {
                out.append(tr);
                out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                        + td + String.valueOf(outemis_star[i]) + untd + td
                        + String.valueOf(outemis[i]) + untd + td
                        + String.valueOf(outemiswei_star[i]) + untd + td
                        + String.valueOf(outemiswei[i]) + untd);
                out.append(untr);
                }
            }

        out.append(untable);

        // generating statistics:
        out.append(it + bold + "Outdegree* Statistics" + unbold + unit);
        out.append(lb + outStatistics(outemis_star, false));
        out.append(it + bold + "Outdegree** Statistics" + unbold + unit);
        out.append(lb + outStatistics(outemis, false));

        outemis = null;
        outemis_star = null;

        if (AppRuntime.getCurrentWeight())
            {
            out.append(it + bold + "Weighted Outdegree* Statistics" + unbold
                    + unit);
            out.append(lb + outStatistics(outemiswei_star, false));

            out.append(it + bold + "Weighted Outdegree** Statistics" + unbold
                    + unit);
            out.append(lb + outStatistics(outemiswei, false));

            outemiswei = null;
            }

        return out.toString();
        }

    // considers data as binary:
    public float[] receptionDegree(Network src)
        {
        int size = src.getSize();
        int i, j;
        float[] finarray = new float[size];
        // float[][] mat = src.getMatrix();
        for (i = 0; i < size; i++)
            {
            finarray[i] = 0f;

            for (j = 0; j < size; j++)
                {
                if (src.getValue(j, i) != 0f)
                    finarray[i]++;
                }
            }
        return finarray;
        }

    // considers data as weighted:
    public float[] weightedReceptionDegree(Network src)
        {
        int size = src.getSize();
        double[] sums = new double[size];
        for (int i = 0; i < size; i++)
            {
            Actor actor = src.getActor(i);
            if (actor != null && actor.emissions != null)
                {
                float[] em = actor.emissions;
                int limit = Math.min(size, em.length);
                for (int j = 0; j < limit; j++)
                    {
                    sums[j] += em[j];
                    }
                }
            }
        float[] finarray = new float[size];
        for (int j = 0; j < size; j++)
            finarray[j] = (float) sums[j];
        return finarray;
        }

    // returns a string (publicable) version of receptionDegree()
    public String outReceptionDegree(Network outsrc)
        {
        String legacy = legacyOutReceptionDegree(outsrc);
        AnalysisReport report = AnalysisReportFactory.createReceptionReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutReceptionDegree(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        float[] outrec =  weightedReceptionDegree(outsrc);
        out.append(it + bold + "Distribution of Reception Degree" + unbold
                + " in " + unit + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + it + "Node" + unit + untd + td + it + "Reception"
                + unit + untd + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outrec[i]) + untd);
            out.append(untr);
            }
        out.append(untable);
        out.append(outStatistics(outrec, true));
        return out.toString();
        }

    public String outInDegree(Network outsrc)
        {
        String legacy = legacyOutInDegree(outsrc);
        AnalysisReport report = AnalysisReportFactory.createInDegreeReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutInDegree(Network outsrc)
        {
        int size = outsrc.getSize();
        int i;
        StringBuffer out = new StringBuffer("");
        float[] outrec = null; // binary data, g
        float[] outrec_star = new float[size]; // binary data, g-1
        float[] outrecwei = null; // weighted data, g
        float[] outrecwei_star = new float[size]; // weighted data, g-1

        outrec = receptionDegree(outsrc);
        for (i = 0; i < size; i++)
            {
            outrec_star[i] = (float) ((double) outrec[i] / (double) (size - 1));
            outrec[i] = (float) ((double) outrec[i] / (double) (size));
            }

        out.append(it + bold + "Distribution of Indegree" + unbold + " in "
                + unit + outsrc.getName());
        out.append(lb + blanc + blanc + blanc + blanc + blanc + it
                + "* relative to number of all other nodes (self excluded)"
                + unit);
        out.append(lb + blanc + blanc + blanc + blanc + blanc + it
                + "** relative to number of all nodes (self included)" + unit);
        out.append(lb + table + tr);

        if (!AppRuntime.getCurrentWeight())
            {
            // binary data:
            out
                    .append(td + it + "Node" + unit + untd + td + it
                            + "Indegree*" + unit + untd + td + it
                            + "Indegree**" + unit + untd + untr);
            for (i = 0; i < size; i++)
                {
                out.append(tr);
                out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                        + td + String.valueOf(outrec[i]) + untd + td
                        + String.valueOf(outrec_star[i]) + untd);
                out.append(untr);
                }
            } else
            {
            // weighted data:
            outrecwei = new float[size];
            outrecwei = weightedReceptionDegree(outsrc);
            for (i = 0; i < size; i++)
                {
                outrecwei_star[i] = (float) ((double) outrecwei[i] / (double) (size - 1));
                outrecwei[i] = (float) ((double) outrecwei[i] / (double) (size));
                }
            out.append(td + it + "Node" + unit + untd + td + it + "Indegree*"
                    + unit + untd + td + it + "Indegree**" + unit + untd + td
                    + it + "Weighted Indegree*" + unit + untd + td + it
                    + "Weighted Indegree**" + unit + untd + untr);
            for (i = 0; i < size; i++)
                {
                out.append(tr);
                out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                        + td + String.valueOf(outrec_star[i]) + untd + td
                        + String.valueOf(outrec[i]) + untd + td
                        + String.valueOf(outrecwei_star[i]) + untd + td
                        + String.valueOf(outrecwei[i]) + untd);
                out.append(untr);
                }
            }

        out.append(untable);

        // generating statistics:
        out.append(it + bold + "Indegree* Statistics" + unbold + unit);
        out.append(lb + outStatistics(outrec_star, false));
        out.append(it + bold + "Indegree** Statistics" + unbold + unit);
        out.append(lb + outStatistics(outrec, false));

        outrec = null;
        outrec_star = null;

        if (AppRuntime.getCurrentWeight())
            {
            out.append(it + bold + "Weighted Indegree* Statistics" + unbold
                    + unit);
            out.append(lb + outStatistics(outrecwei_star, false));

            out.append(it + bold + "Weighted Indegree** Statistics" + unbold
                    + unit);
            out.append(lb + outStatistics(outrecwei, false));

            outrecwei = null;
            }

        return out.toString();
        }

    public String outInDegreeOLD(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        float[] outrec = new float[size]; // binary data
        float[] outrecwei = null; // weighted data

        outrec = receptionDegree(outsrc);
        for (int i = 0; i < size; i++)
            {
            outrec[i] = (float) ((double) outrec[i] / (double) size);
            }

        out.append(it + bold + "Distribution of Indegree" + unbold + " in "
                + unit + outsrc.getName());
        out.append(lb + table + tr);

        if (!AppRuntime.getCurrentWeight())
            {
            // binary data:
            out.append(td + it + "Node" + unit + untd + td + it + "Indegree"
                    + unit + untd + untr);
            for (int i = 0; i < size; i++)
                {
                out.append(tr);
                out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                        + td + String.valueOf(outrec[i]) + untd);
                out.append(untr);
                }
            } else
            {
            // weighted data:
            outrecwei = new float[size];
            outrecwei = weightedReceptionDegree(outsrc);
            for (int i = 0; i < size; i++)
                {
                outrecwei[i] = outrecwei[i] / size;
                }
            out.append(td + it + "Node" + unit + untd + td + it + "Indegree"
                    + unit + untd + td + it + "Weighted Indegree" + unit + untd
                    + untr);
            for (int i = 0; i < size; i++)
                {
                out.append(tr);
                out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                        + td + String.valueOf(outrec[i]) + untd + td
                        + String.valueOf(outrecwei[i]) + untd);
                out.append(untr);
                }
            }

        out.append(untable);

        // generating statistics:
        out.append(it + bold + "Indegree Statistics" + unbold + unit);
        out.append(lb + outStatistics(outrec, false));

        outrec = null;

        if (AppRuntime.getCurrentWeight())
            {
            out.append(it + bold + "Weighted Indegree Statistics" + unbold
                    + unit);
            out.append(lb + outStatistics(outrecwei, false));
            outrecwei = null;
            }

        return out.toString();
        }

    // counts the number of non-directed edges:
    public float[] nodalDegree(Network src)
        {
        // 2.1.3: symmetric networks keep the classic neighbour count;
        // asymmetric networks count each direction separately (out + in)
        int size = src.getSize();
        boolean symmetric = src.isSymmetric();
        float[] finarray = new float[size];
        for (int i = 0; i < size; i++)
            {
            finarray[i] = 0f;
            for (int j = 0; j < size; j++)
                {
                if (j != i && src.getValue(i, j) != 0f)
                    {
                    finarray[i]++;
                    }
                if (!symmetric && j != i && src.getValue(j, i) != 0f)
                    {
                    finarray[i]++;
                    }
                }
            }
        return finarray;
        }

    public float[] weightedNodalDegree(Network src)
        {
        // 2.1.3: same convention as nodalDegree (per direction when asymmetric)
        int size = src.getSize();
        boolean symmetric = src.isSymmetric();
        float[] finarray = new float[size];
        for (int i = 0; i < size; i++)
            {
            double good_one = 0;
            for (int j = 0; j < size; j++)
                {
                good_one += (double) src.getValue(i, j);
                if (!symmetric && j != i)
                    {
                    good_one += (double) src.getValue(j, i);
                    }
                }
            finarray[i] = (float) good_one;
            }
        return finarray;
        }




    // makes sense for non-directed networks only!
    public String outNodalDegree(Network outsrc)
        {
        String legacy = legacyOutNodalDegree(outsrc);
        AnalysisReport report = AnalysisReportFactory.createNodalDegreeReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutNodalDegree(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        float[] outrec = new float[size];
        float[] outwei = null;
        outrec = nodalDegree(outsrc);
        if (outrec == null)
            {
            out
                    .append(it
                            + "Nodal Degree can be computed for non-directed (symmetric) networks only."
                            + unit);
            return out.toString();
            }

        out.append(it + bold + "Distribution of Nodal Degree" + unbold + " in "
                + unit + outsrc.getName());
        out.append(lb + blanc + blanc + blanc + blanc + blanc + it
                + "* relative to number of all other nodes (self excluded)"
                + unit);
        out.append(lb + blanc + blanc + blanc + blanc + blanc + it
                + "** relative to number of all nodes (self included)" + unit);
        out.append(lb + it + "(The network is now considered as non-directed.)"
                + unit);

        out.append(lb + table + tr);

        if (!AppRuntime.getCurrentWeight())
            {
            // binary data:
            out.append(td + it + "Node" + unit + untd + td + it + "Degree"
                    + unit + untd + td + it + "Relative Degree*" + unit + untd
                    + td + it + "Relative Degree**" + unit + untd + untr);
            for (int i = 0; i < size; i++)
                {
                out.append(tr);
                out.append(td
                        + it
                        + outsrc.getActor(i).getName()
                        + unit
                        + untd
                        + td
                        + String.valueOf(outrec[i])
                        + untd
                        + td
                        + String.valueOf((double) outrec[i]
                                / (double) (size - 1)) + untd + td
                        + String.valueOf((double) outrec[i] / (double) size)
                        + untd);
                out.append(untr);
                }
            outwei = null;
            } else
            {
            // weighted data:
            outwei = new float[size];
            outwei = weightedNodalDegree(outsrc);
            if (outwei != null)
                {
                out.append(td + it + "Node" + unit + untd + td + it + "Degree"
                        + unit + untd + td + it + "Relative Degree*" + unit
                        + untd + td + it + "Relative Degree**" + unit + untd
                        + td + it + "Weighted Degree" + unit + untd + td + it
                        + "Relative Weighted Degree*" + unit + untd + td + it
                        + "Relative Weighted Degree**" + unit + untd + untr);
                for (int i = 0; i < size; i++)
                    {
                    out.append(tr);
                    out.append(td
                            + it
                            + outsrc.getActor(i).getName()
                            + unit
                            + untd
                            + td
                            + String.valueOf(outrec[i])
                            + untd
                            + td
                            + String.valueOf((double) outrec[i]
                                    / (double) (size - 1))
                            + untd
                            + td
                            + String.valueOf((double) outrec[i]
                                    / (double) (size))
                            + untd
                            + td
                            + String.valueOf(outwei[i])
                            + untd
                            + td
                            + String.valueOf((double) outwei[i]
                                    / (double) (size - 1))
                            + untd
                            + td
                            + String.valueOf((double) outwei[i]
                                    / (double) (size)) + untd);
                    out.append(untr);
                    }
                }
            }

        out.append(untable);

        if (size > 2)
            {
            out.append(it + bold + "Freeman (1979) Degree Centralization (C_D) = "
                    + unbold + unit);
            out.append(String.format(Locale.ROOT, "%.4f", freemanDegreeCentralization(outsrc)));
            out.append(lb + lb);
            }

        out.append(it + bold + "Degree Statistics" + unbold + unit);
        out.append(lb + outStatistics(outrec, false));

        float[] tmp = new float[size];
        for (int i = 0; i < size; i++)
            {
            tmp[i] = (float) ((double) outrec[i] / (double) (size - 1));
            }
        out.append(it + bold + "Relative Degree* Statistics" + unbold + unit);
        out.append(lb + outStatistics(tmp, false));

        for (int i = 0; i < size; i++)
            {
            tmp[i] = (float) ((double) outrec[i] / (double) (size));
            }
        out.append(it + bold + "Relative Degree** Statistics" + unbold + unit);
        out.append(lb + outStatistics(tmp, false));

        outrec = null;

        if (outwei != null)
            {
            out
                    .append(it + bold + "Weighted Degree Statistics" + unbold
                            + unit);
            out.append(lb + outStatistics(outwei, false));

            for (int i = 0; i < size; i++)
                {
                tmp[i] = (float) ((double) outwei[i] / (double) (size - 1));
                }
            out.append(it + bold + "Relative Weighted Degree* Statistics"
                    + unbold + unit);
            out.append(lb + outStatistics(tmp, false));

            for (int i = 0; i < size; i++)
                {
                tmp[i] = (float) ((double) outwei[i] / (double) (size));
                }
            out.append(it + bold + "Relative Weighted Degree** Statistics"
                    + unbold + unit);
            out.append(lb + outStatistics(tmp, false));

            outwei = null;
            }

        return out.toString();
        }

    public float density(Network src) // binary data
        {
        int size = src.getSize();
        double dens = 0;
        float[] outrec = new float[size];
        float[] outemis = new float[size];
        outrec = receptionDegree(src);
        for (int i = 0; i < size; i++)
            {
            dens += (double) outrec[i];
            }
        dens = dens / (double) size / ((double) size - 1);
        return (float) dens;
        }

    public float weightedDensity(Network src) // weighted data
        {
        int size = src.getSize();
        double dens = 0;
        float[] outrec =  weightedReceptionDegree(src);
        for (int i = 0; i < size; i++)
            {
            dens += (double) outrec[i];
            }
        dens = dens / (double) size / ((double) size - 1);
        return (float) dens;
        }

    public String outDensity(Network outsrc)
        {
        String legacy = legacyOutDensity(outsrc);
        AnalysisReport report = AnalysisReportFactory.createDensityReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutDensity(Network outsrc)
        {
        String out = new String("");
        out = it + bold + "Density" + unbold + " of " + unit + outsrc.getName();
        out += lb + it + "Density = " + unit + Float.toString(density(outsrc));
        out += lb + it
                + "Density (directed convention; arcs / (n*(n-1))) = " + unit
                + Float.toString(density(outsrc));
        if (outsrc.isSymmetric())
            {
            out += lb + it
                    + "Undirected convention (edges / (n*(n-1)/2)): the value is the same for a symmetric matrix."
                    + unit;
            } else
            {
            out += lb + it
                    + "Note: the matrix is asymmetric; an undirected density is not defined."
                    + unit;
            } // String.valueOf(density(outsrc));

        if (AppRuntime.getCurrentWeight())
            {
            // weighted data:
            out += lb + it + "Weighted Density = " + unit
                    + String.valueOf(weightedDensity(outsrc));
            out += lb;
            }

        return out;
        }

    // considers data as weighted:
    public float[] determinationDegree(Network src)
        {
        int size = src.getSize();
        if (size <= 1)
            return new float[size];
        double[] inSums = new double[size];
        double[] outSums = new double[size];
        for (int i = 0; i < size; i++)
            {
            Actor actor = src.getActor(i);
            if (actor != null && actor.emissions != null)
                {
                float[] em = actor.emissions;
                int limit = Math.min(size, em.length);
                double rowSum = 0;
                for (int j = 0; j < limit; j++)
                    {
                    float val = em[j];
                    rowSum += val;
                    inSums[j] += val;
                    }
                outSums[i] = rowSum;
                }
            }
        float[] finarray = new float[size];
        double denom = (double) size - 1.0;
        for (int i = 0; i < size; i++)
            {
            finarray[i] = (float) ((inSums[i] - outSums[i]) / denom);
            }
        return finarray;
        }

    public String outDeterminationDegree(Network outsrc)
        {
        String legacy = legacyOutDeterminationDegree(outsrc);
        AnalysisReport report = AnalysisReportFactory.createDeterminationReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutDeterminationDegree(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        float[] outdet =  determinationDegree(outsrc);
        out.append(it + bold + "Distribution of Determination Degree" + unbold
                + " in " + unit + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + it + "Node" + unit + untd + td + it + "Determination"
                + unit + untd + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outdet[i]) + untd);
            out.append(untr);
            }
        out.append(untable);
        out.append(outStatistics(outdet, true));
        return out.toString();
        }

    public float[] sociometricStatus(Network src)
        {
        int size = src.getSize();
        int i, j;
        float[] finarray = new float[size];
        // float[][] mat = src.getMatrix();
        double tmp = 0;
        for (i = 0; i < size; i++)
            {
            tmp = 0;

            for (j = 0; j < size; j++)
                {
                // finarray[i] += mat[j][i] + mat [i][j];
                // tmp += (double)mat[j][i] + (double)mat[i][j];
                tmp += (double) src.getValue(j, i)
                        + (double) src.getValue(i, j);
                }
            finarray[i] = (float) ((double) tmp / ((double) size - 1));
            }
        return finarray;
        }

    public String outSociometricStatus(Network outsrc)
        {
        String legacy = legacyOutSociometricStatus(outsrc);
        AnalysisReport report = AnalysisReportFactory.createSociometricStatusReport(outsrc, this, legacy);
        if ("text/html".equals(AppRuntime.outputContentType()))
            return report.toHtmlReport();
        return report.toPlainText();
        }

    public String legacyOutSociometricStatus(Network outsrc)
        {
        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
        float[] outss =  sociometricStatus(outsrc);
        out.append(it + bold + "Distribution of Sociometric Status" + unbold
                + " in " + unit + outsrc.getName());
        out.append(lb + table + tr);
        out.append(td + it + "Node" + unit + untd + td + it + "Status" + unit
                + untd + untr);
        for (int i = 0; i < size; i++)
            {
            out.append(tr);
            out.append(td + it + outsrc.getActor(i).getName() + unit + untd
                    + td + String.valueOf(outss[i]) + untd);
            out.append(untr);
            }
        out.append(untable);
        out.append(outStatistics(outss, true));
        out.append("\n");
        return out.toString();
        }

    /**
     * Computes the Freeman (1979) Degree Centralization index C_D of a network.
     * <p>
     * Defined for non-directed or symmetrized networks as:
     * C_D = \sum_{i=1}^n [ max_j(d_j) - d_i ] / [ (n - 1) * (n - 2) ]
     * where the denominator is the theoretical maximum difference achieved by a star graph S_n.
     * Returns 0.0 for n &le; 2 or when the network has no degree variation.
     *
     * @param net network to evaluate
     * @return Freeman degree centralization index in [0.0, 1.0]
     */
    public float freemanDegreeCentralization(Network net)
        {
        if (net == null || net.getSize() < 3)
            return 0f;
        return (float) com.bentza.sna.stats.NetworkMetric.DEGREE_CENTRALIZATION.compute(net);
        }

    /**
     * Legacy helper for Freeman general star-benchmark comparison.
     * Star_network_index are the centrality indexes of the corresponding star network.
     *
     * @deprecated Prefer {@link #freemanDegreeCentralization(Network)} or {@link com.bentza.sna.stats.NetworkMetric}
     *             centralization indices.
     */
    @Deprecated
    public float freemanGeneralIndex(float[] a, float[] star_network_index)
        {
        int nn = a.length;
        int i = 0;
        float max = Float.NEGATIVE_INFINITY;
        float max_sni = Float.NEGATIVE_INFINITY;
        double finval = 0;

        if (nn != star_network_index.length)
            return 0f;

        // starts computing:
        for (i = 0; i < nn; i++)
            {
            // finding the max of index a
            finval = (double) a[i]; // temporary value
            if (finval > max && finval != 0f)
                {
                max = (float) finval;
                }

            // finding the max of starnetwork_index
            finval = star_network_index[i]; // temporary value
            if (finval > max_sni && finval != 0f)
                {
                max_sni = (float) finval;
                }
            }

        if (max == Float.NEGATIVE_INFINITY
                || max_sni == Float.NEGATIVE_INFINITY)
            return 0f; // no result

        finval = 0; // numerator
        double denominator = 0; // denominator
        for (i = 0; i < nn; i++)
            {
            finval += (double) max - (double) a[i];
            denominator += (double) max_sni - (double) star_network_index[i];
            }

        if (denominator != 0)
            return (float) ((double) finval / (double) denominator);
        else
            return 0f;
        }

    // returns an 11-elements array:
    /*
     * [0] min, [1] max, [2] sum, [3] mean, [4] variance, [5] std distr, [6] abs
     * entr (ln), [7] max entr (ln), [8] rel entr (log-independent), [9] abs
     * entr (log2), [10] max entr (log2)
     */
    public float[] statistics(float[] a)
        {
        int nn = a.length;
        int i;
        final int elements = 11; // number of coefficients computed
        float[] stat = new float[elements];
        double min, max, sum, var, en, tmp;
        // initialization:
        i = 0;
        min = Float.POSITIVE_INFINITY;
        max = Float.NEGATIVE_INFINITY;
        sum = 0;
        var = 0;
        en = 0;
        tmp = 0;
        for (i = 0; i < elements; i++)
            {
            stat[i] = 0f;
            }

        // starts computing:
        for (i = 0; i < nn; i++)
            {
            tmp = (double) a[i];
            sum += tmp;

            if (tmp > max)
                {
                if (tmp != 0)
                    max = tmp;
                }
            if (tmp < min)
                {
                if (tmp != 0)
                    min = tmp;
                }
            }

        tmp = sum / (double) nn; // tmp = mean
        // computing variance:
        for (i = 0; i < nn; i++)
            {
            var += ((double) a[i] - tmp) * ((double) a[i] - tmp);
            }
        var = var / (double) nn;

        // computing entropy:
        boolean is_entropy = true;
        if (min > 0 && sum != 0)
            {
            for (i = 0; i < nn; i++)
                {
                tmp = (double) a[i] / sum;
                if (tmp != 0)
                    en += tmp * Math.log(tmp);
                }
            en = -en; // absolute entropy
            tmp = (double) Math.log((double) nn); // maximum entropy
            } else
            {
            // if entropy cannot be computed:
            is_entropy = false;
            en = 0;
            tmp = (double) Math.log((double) nn); // maximum entropy
            }

        stat[0] = (float) min; // minimum
        stat[1] = (float) max; // maximum
        stat[2] = (float) sum; // sum
        stat[3] = (float) ((double) sum / (double) nn); // mean
        stat[4] = (float) var; // variance
        stat[5] = (float) Math.sqrt((double) var); // standard deviation

        stat[6] = (float) en; // absolute entropy - e-log (null if not
                                // computed)
        stat[7] = (float) tmp; // maximum entropy - e-log

        if (stat[6] == stat[7])
            stat[8] = 0f;
        else
            stat[8] = (float) (100 * ((double) tmp - (double) en) / tmp); // relative
                                                                            // entropy
                                                                            // (null
                                                                            // if
                                                                            // not
                                                                            // computed)

        final double log2 = (double) Math.log((double) 2);
        stat[9] = (float) ((double) en / (double) log2); // absolute entropy
                                                            // - 2-log (null if
                                                            // not computed)
        stat[10] = (float) ((double) tmp / (double) log2); // maximum entropy -
                                                            // 2-log

        return stat;
        }

    // returns a String version of statistics (integer distribution):
    public String outStatistics(int[] outa, boolean is_title)
        {
        int size = outa.length;
        float[] distribution = new float[size];
        for (int i = 0; i < size; i++)
            {
            distribution[i] = (float) outa[i];
            }

        return outStatistics(distribution, is_title);
        }

    // returns a String version of statistics (float distribution):
    public String outStatistics(float[] outa, boolean is_title)
        {
        AgnaLib agna_lib = new AgnaLib();
        StringBuffer out = new StringBuffer("");
        float[] outstat = agna_lib.statistics(outa);
        if (outstat == null)
            return null;

        if (is_title)
            out.append(it + bold + "Statistics" + unbold + unit + lb);
        out.append(table);
        if (outstat[0] != Float.POSITIVE_INFINITY
                && outstat[1] != Float.NEGATIVE_INFINITY)
            {
            out.append(tr);
            out.append(td + it + "Minimum" + unit + untd + td
                    + String.valueOf(outstat[0]) + untd);
            out.append(untr + tr);
            out.append(td + it + "Maximum" + unit + untd + td
                    + String.valueOf(outstat[1]) + untd);
            out.append(untr);
            }
        out.append(tr + td + it + "Sum" + unit + untd + td
                + String.valueOf(outstat[2]) + untd + untr);
        out.append(tr + td + it + "Mean" + unit + untd + td
                + String.valueOf(outstat[3]) + untd + untr);
        out.append(tr + td + it + "Variance" + unit + untd + td
                + String.valueOf(outstat[4]) + untd + untr);
        out.append(tr + td + it + "Standard Deviance" + unit + untd + td
                + String.valueOf(outstat[5]) + untd + untr);
        if (outstat[6] != 0)
            {
            out.append(tr + td + it + "Absolute Entropy (natural log.)" + unit
                    + untd + td + String.valueOf(outstat[6]) + untd + untr);
            out.append(tr + td + it + "Maximum Entropy (natural log.)" + unit
                    + untd + td + String.valueOf(outstat[7]) + untd + untr);

            out.append(tr + td + it + "Absolute Entropy (base 2 log.)" + unit
                    + untd + td + String.valueOf(outstat[9]) + untd + untr);
            out.append(tr + td + it + "Maximum Entropy (base 2 log.)" + unit
                    + untd + td + String.valueOf(outstat[10]) + untd + untr);

            out.append(tr + td + it + "Relative Entropy (%)" + unit + untd + td
                    + String.valueOf(outstat[8]) + untd + untr);
            }
        out.append(untable);
        return out.toString();
        }

    public String outLocalClustering(Network outsrc) {
        String legacy = legacyOutLocalClustering(outsrc);
        AnalysisReport report = AnalysisReportFactory.createLocalClusteringReport(outsrc, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutLocalClustering(Network outsrc) {
        return it + bold + "Watts-Strogatz Local Clustering" + unbold + " of " + unit + outsrc.getName() + lb;
    }

    public String outTransitivity(Network outsrc) {
        String legacy = legacyOutTransitivity(outsrc);
        AnalysisReport report = AnalysisReportFactory.createTransitivityReport(outsrc, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutTransitivity(Network outsrc) {
        return it + bold + "Global Transitivity" + unbold + " of " + unit + outsrc.getName() + lb;
    }

    public String outKCore(Network outsrc) {
        String legacy = legacyOutKCore(outsrc);
        AnalysisReport report = AnalysisReportFactory.createKCoreReport(outsrc, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutKCore(Network outsrc) {
        return it + bold + "k-Core Decomposition" + unbold + " of " + unit + outsrc.getName() + lb;
    }

    public String outTriadCensus(Network outsrc) {
        String legacy = legacyOutTriadCensus(outsrc);
        AnalysisReport report = AnalysisReportFactory.createTriadCensusReport(outsrc, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutTriadCensus(Network outsrc) {
        return it + bold + "Triad & Dyad Census" + unbold + " of " + unit + outsrc.getName() + lb;
    }

    public String outStructuralHoles(Network outsrc) {
        String legacy = legacyOutStructuralHoles(outsrc);
        AnalysisReport report = AnalysisReportFactory.createStructuralHolesReport(outsrc, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutStructuralHoles(Network outsrc) {
        return it + bold + "Social Capital & Structural Holes" + unbold + " of " + unit + outsrc.getName() + lb;
    }

    public String outConnectivity(Network outsrc) {
        String legacy = legacyOutConnectivity(outsrc);
        AnalysisReport report = AnalysisReportFactory.createConnectivityReport(outsrc, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutConnectivity(Network outsrc) {
        return it + bold + "Connectivity & Articulation Points" + unbold + " of " + unit + outsrc.getName() + lb;
    }

    public String outEigenvector(Network outsrc) {
        String legacy = legacyOutEigenvector(outsrc);
        AnalysisReport report = AnalysisReportFactory.createEigenvectorReport(outsrc, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutEigenvector(Network outsrc) {
        return it + bold + "Eigenvector Centrality" + unbold + " of " + unit + outsrc.getName() + lb;
    }

    public String outBonacich(Network outsrc, double beta) {
        String legacy = legacyOutBonacich(outsrc, beta);
        AnalysisReport report = AnalysisReportFactory.createBonacichReport(outsrc, beta, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutBonacich(Network outsrc, double beta) {
        return it + bold + "Bonacich Power Centrality (beta=" + beta + ")" + unbold + " of " + unit + outsrc.getName() + lb;
    }

    public String outPageRank(Network outsrc, double damping) {
        String legacy = legacyOutPageRank(outsrc, damping);
        AnalysisReport report = AnalysisReportFactory.createPageRankReport(outsrc, damping, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutPageRank(Network outsrc, double damping) {
        return it + bold + "PageRank Centrality (d=" + damping + ")" + unbold + " of " + unit + outsrc.getName() + lb;
    }

    public String outFreemanCentralization(Network outsrc) {
        com.bentza.sna.report.TableData table = AnalysisReportFactory.createFreemanCentralizationSummaryTable(outsrc, this);
        if (table == null) {
            return it + "Freeman Centralization indices require N >= 3." + unit + lb;
        }
        AnalysisReport report = new AnalysisReport("Freeman Network Centralization Indices", outsrc.getName());
        report.addTable(table);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String outCentralityBattery(Network outsrc, CentralityOptions options) {
        String legacy = legacyOutCentralityBattery(outsrc, options);
        AnalysisReport report = AnalysisReportFactory.createCentralityBatteryReport(outsrc, this, options, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutCentralityBattery(Network outsrc, CentralityOptions options) {
        if (options == null) options = CentralityOptions.createStandard();
        StringBuffer out = new StringBuffer(it + bold + "Centrality & Prestige Battery" + unbold + " of " + unit + outsrc.getName() + lb);
        if (options.isIncludeNodalDegree()) out.append(outNodalDegree(outsrc));
        if (options.isIncludeInDegree()) out.append(outInDegree(outsrc));
        if (options.isIncludeOutDegree()) out.append(outOutDegree(outsrc));
        if (options.isIncludePrestige()) out.append(outPrestige(outsrc));
        if (options.isIncludeBetweenness()) out.append(outBetweenness(outsrc));
        if (options.isIncludeCloseness()) out.append(outCloseness(outsrc));
        if (options.isIncludeEigenvector()) out.append(legacyOutEigenvector(outsrc));
        if (options.isIncludeBonacich()) out.append(legacyOutBonacich(outsrc, options.getBonacichBeta()));
        if (options.isIncludePageRank()) out.append(legacyOutPageRank(outsrc, options.getPageRankDamping()));
        return out.toString();
    }

    public String outDistanceBattery(Network outsrc, DistanceOptions options) {
        String legacy = legacyOutDistanceBattery(outsrc, options);
        AnalysisReport report = AnalysisReportFactory.createDistanceBatteryReport(outsrc, this, options, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutDistanceBattery(Network outsrc, DistanceOptions options) {
        if (options == null) options = DistanceOptions.createStandard();
        StringBuffer out = new StringBuffer(it + bold + "Distances & Geodesics Battery" + unbold + " of " + unit + outsrc.getName() + lb);
        if (options.isIncludeGeodesicsMatrix()) out.append(outGeodesics(outsrc));
        if (options.isIncludeDiameter()) out.append(outDiameter(outsrc));
        if (options.isIncludeEccentricity()) out.append(outEccentricity(outsrc));
        if (!isStronglyConnected(outsrc)) {
            out.append(lb + it + "Note: Network is not strongly connected; distance measures reflect reachable paths only." + unit + lb);
        }
        return out.toString();
    }

    public String outClusteringBattery(Network outsrc, ClusteringOptions options) {
        String legacy = legacyOutClusteringBattery(outsrc, options);
        AnalysisReport report = AnalysisReportFactory.createClusteringBatteryReport(outsrc, this, options, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutClusteringBattery(Network outsrc, ClusteringOptions options) {
        if (options == null) options = ClusteringOptions.createStandard();
        StringBuffer out = new StringBuffer(it + bold + "Clustering & Cohesion Battery" + unbold + " of " + unit + outsrc.getName() + lb);
        if (options.isIncludeLocalClustering()) out.append(legacyOutLocalClustering(outsrc));
        if (options.isIncludeTransitivity()) out.append(legacyOutTransitivity(outsrc));
        if (options.isIncludeKCore()) out.append(legacyOutKCore(outsrc));
        if (options.isIncludeCohesion()) out.append(outCohesion(outsrc));
        if (options.isIncludeOpenChains()) out.append(outOpenChainSummary(outsrc));
        if (options.isIncludeCliques()) out.append(outCliques(outsrc, options.getCliqueDiameter()));
        return out.toString();
    }

    public String outSociometricsBattery(Network outsrc, SociometricsOptions options) {
        String legacy = legacyOutSociometricsBattery(outsrc, options);
        AnalysisReport report = AnalysisReportFactory.createSociometricsBatteryReport(outsrc, this, options, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutSociometricsBattery(Network outsrc, SociometricsOptions options) {
        if (options == null) options = SociometricsOptions.createStandard();
        StringBuffer out = new StringBuffer(it + bold + "Sociometrics & Basic Battery" + unbold + " of " + unit + outsrc.getName() + lb);
        if (options.isIncludeBasic()) out.append(outBasic(outsrc));
        if (options.isIncludeDensity()) out.append(outDensity(outsrc));
        if (options.isIncludeNodalDegree()) out.append(outNodalDegree(outsrc));
        if (options.isIncludeInDegree()) out.append(outInDegree(outsrc));
        if (options.isIncludeOutDegree()) out.append(outOutDegree(outsrc));
        return out.toString();
    }

    public String outDegreeAssortativity(Network outsrc) {
        String legacy = legacyOutDegreeAssortativity(outsrc);
        AnalysisReport report = AnalysisReportFactory.createDegreeAssortativityReport(outsrc, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutDegreeAssortativity(Network outsrc) {
        com.bentza.sna.stats.DegreeAssortativity.AssortativityResult res = com.bentza.sna.stats.DegreeAssortativity.compute(outsrc);
        StringBuffer out = new StringBuffer(it + bold + "Degree Assortativity & Mixing Patterns" + unbold + " of " + unit + outsrc.getName() + lb);
        out.append("Total Degree Assortativity (r): ").append(String.valueOf(res.totalDegreeAssortativity)).append(lb);
        out.append("Directed Out-In Assortativity: ").append(String.valueOf(res.outInAssortativity)).append(lb);
        out.append("Mixing Regime: ").append(res.mixingRegime).append(lb);
        return out.toString();
    }

    public String outCommunityDetection(Network outsrc, boolean useGirvanNewman) {
        String legacy = legacyOutCommunityDetection(outsrc, useGirvanNewman);
        AnalysisReport report = AnalysisReportFactory.createCommunityDetectionReport(outsrc, useGirvanNewman, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutCommunityDetection(Network outsrc, boolean useGirvanNewman) {
        com.bentza.sna.community.CommunityDetectionService.CommunityResult res = useGirvanNewman ?
                com.bentza.sna.community.CommunityDetectionService.detectGirvanNewman(outsrc) :
                com.bentza.sna.community.CommunityDetectionService.detectLouvain(outsrc);
        StringBuffer out = new StringBuffer(it + bold + "Community Detection (" + res.algorithm + ")" + unbold + " of " + unit + outsrc.getName() + lb);
        out.append("Modularity Q: ").append(String.valueOf(res.modularityQ)).append(lb);
        out.append("Communities: ").append(String.valueOf(res.communityCount)).append(lb);
        for (com.bentza.sna.community.CommunityDetectionService.CommunityInfo info : res.communities) {
            out.append("Community ").append(info.communityId).append(" (n=").append(info.memberCount).append("): ")
                    .append(String.join(", ", info.memberNames)).append(lb);
        }
        return out.toString();
    }

    public String outStructuralEquivalence(Network outsrc, int targetBlocks) {
        String legacy = legacyOutStructuralEquivalence(outsrc, targetBlocks);
        AnalysisReport report = AnalysisReportFactory.createStructuralEquivalenceReport(outsrc, targetBlocks, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutStructuralEquivalence(Network outsrc, int targetBlocks) {
        com.bentza.sna.equivalence.StructuralEquivalenceService.EquivalenceResult res =
                com.bentza.sna.equivalence.StructuralEquivalenceService.analyze(outsrc, targetBlocks);
        StringBuffer out = new StringBuffer(it + bold + "Structural Equivalence & Blockmodeling" + unbold + " of " + unit + outsrc.getName() + lb);
        out.append("Positions / Blocks: ").append(res.blockCount).append(lb);
        for (int b = 0; b < res.blockCount; b++) {
            out.append("Block ").append(b + 1).append(": ").append(String.join(", ", res.blockMembers.get(b))).append(lb);
        }
        return out.toString();
    }

    public String outWeightedMetrics(Network outsrc) {
        String legacy = legacyOutWeightedMetrics(outsrc);
        AnalysisReport report = AnalysisReportFactory.createWeightedMetricsReport(outsrc, this, legacy);
        return "text/html".equals(AppRuntime.outputContentType()) ? report.toHtmlReport() : report.toPlainText();
    }

    public String legacyOutWeightedMetrics(Network outsrc) {
        com.bentza.sna.stats.WeightedMetrics.WeightedSummary sum = com.bentza.sna.stats.WeightedMetrics.compute(outsrc);
        StringBuffer out = new StringBuffer(it + bold + "Valued & Weighted Network Analysis" + unbold + " of " + unit + outsrc.getName() + lb);
        out.append("Binary Density: ").append(String.valueOf(sum.binaryDensity)).append(lb);
        out.append("Valued Density: ").append(String.valueOf(sum.valuedDensity)).append(lb);
        out.append("Mean Barrat Weighted Clustering: ").append(String.valueOf(sum.meanWeightedClustering)).append(lb);
        return out.toString();
    }

    }