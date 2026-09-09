package com.bentza.sna.net;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import com.bentza.sna.gui.MainFrame;
import com.bentza.sna.gui.AgnaTextPane;
import java.util.Date;
import java.util.Vector;

        public class AgnaLib
    {
    public static String lb, bold, unbold, it, unit, table, untable, tr, untr,
            td, untd, ol, unol, li, unli, blanc;

    // 2.1.3: set by the UI to abort an in-flight clique enumeration
    public static volatile boolean clique_search_cancelled;

        public static void initAjna()
        {
        // type-dependent text elements:
        // 2.1.3: null-safe (the engine can also be used headless, e.g. in
        // batch runs or tests, before the output pane exists)
        String cont = "text/plain";
        AgnaTextPane pane = MainFrame.getCurrentOutputPane();
        if (pane != null)
            {
            cont = pane.getContentType();
            }
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
        int size = src.getSize();
        int i, j;
        double finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = (double) src.getValue(i, j)
                        + (double) src.getValue(j, i);
                // finval = 1.0 + 1.0;
                src.setValue((float) finval, i, j);
                src.setValue((float) finval, j, i);
                }
            }
        }

    public void symmetrizeProduct(Network src)
        {
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
        int size = src.getSize();
        int i, j;
        double finval = 0;
        for (i = 0; i < size; i++)
            {
            for (j = i + 1; j < size; j++)
                {
                finval = Math.sqrt((double) src.getValue(i, j)
                        * (double) src.getValue(j, i));
                src.setValue((float) finval, i, j);
                src.setValue((float) finval, j, i);
                }
            }
        }

    public void addScalar(Network src, float scal)
        {
        int size = src.getSize();
        int i, j;
        double finval = 0;
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                finval = (double) src.getValue(i, j) + (double) scal;
                src.setValue((float) finval, i, j);
                }
            }
        }

    public void multiplyByScalar(Network src, float scal)
        {
        int size = src.getSize();
        int i, j;
        double finval = 0;
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
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
                /*
                 * if (temp != 0) finmat[i][j]=1; else finmat[i][j]=0;
                 */
                finmat[i][j] = (float) temp;
                }
            }
        return finmat;
        }

    // returns multiplication as boolean matrix
    public boolean[][] multiplyMatricesBoolean(float[][] first, float[][] second)
        {
        int size = first.length;
        int i, j, k;
        float temp = 0f; // temporary variable
        boolean[][] finmat = new boolean[size][size]; // final result
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                temp = 0;
                for (k = 0; k < size; k++)
                    {
                    temp += first[i][k] * second[k][j];
                    }
                if (temp != 0)
                    finmat[i][j] = true;
                else
                    finmat[i][j] = false;
                }
            }
        return finmat;
        }

    public float[][] multiplyMatrices(float[][] first, float[][] second)
        {
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
                }
            }
        return finmat;
        }

    public int[][] multiplyMatrices(int[][] first, int[][] second)
        {
        int size = first.length;
        int i, j, k;
        int temp; // temporary variable
        int[][] finmat = new int[size][size]; // final result
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                temp = 0;
                for (k = 0; k < size; k++)
                    {
                    temp += first[i][k] * second[k][j];
                    }
                finmat[i][j] = temp;
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

    // string-based; deprecated;
    // returns a vector of strings, ecah string being
    // one of the possible shortest paths between init and end;
    // method written by Adrian Duda;
    /*
     * private static Vector shortestPathsOLD(Network src, int init, int end) {
     * Vector siruri_actuale = new Vector(1, 1); // contains only one element
     * Vector siruri_noi = new Vector(1, 1); Vector siruri_bune = new Vector(1,
     * 1); siruri_actuale.removeAllElements(); siruri_noi.removeAllElements();
     * siruri_bune.removeAllElements(); // initialization: int nr_siruri = 1; //
     * number of elements in siruri_acuale int ind; int n = src.getSize(); //
     * size of network/matrix boolean[][] mat = new boolean[n][n]; mat =
     * src.getBooleanMatrix(); // network's boolean matrix String tmpstr; //
     * string de lucru String drum; tmpstr = new String(""); tmpstr +=
     * (char)init; siruri_actuale.addElement(tmpstr); boolean gasit = false;
     * while (gasit == false) { for (int i = 0; i < siruri_actuale.size(); i++) {
     * tmpstr = (String)siruri_actuale.elementAt(i); ind =
     * (int)tmpstr.charAt(tmpstr.length() - 1); // ind = last character of the
     * string for (int k = 0; k < n; k++) { if (mat[ind][k]) { if
     * (tmpstr.indexOf((char)k) < 0) { drum = new String(""); drum = tmpstr +
     * (char)k; siruri_noi.addElement(drum); if (k == end) { gasit = true;
     * siruri_bune.addElement(drum); } } } } }
     * siruri_actuale.removeAllElements(); // echivalentul atribuirii:
     * siruri_actuale = siruri_noi if (siruri_noi.size() > 0) { for (int k = 0;
     * k < siruri_noi.size(); k++) {
     * siruri_actuale.addElement((String)siruri_noi.elementAt(k)); } }
     * siruri_noi.removeAllElements(); if (siruri_actuale.size() == 0) { break; //
     * out of while } } return siruri_bune; }
     */

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
    private Vector cliquesMain(Network src, int cdiam)
        {
        int size = src.getSize();
        if (cdiam < 1 || size < 1)
            return null;

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

        Vector cliques = new Vector();
        int[] candidates = new int[size];
        for (int i = 0; i < size; i++)
            {
            candidates[i] = i;
            }
        bronKerbosch(adjacent, cliques, new IntList(), candidates, size,
                new int[size], 0);
        return cliques;
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
    private void bronKerbosch(boolean[][] adjacent, Vector cliques,
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
                cliques.addElement(current.getClone());
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

    // returns a vector of IntLists, ecah string being
    // one of the possible shortest paths between init and end;
    // method written by Adrian Duda;
    private Vector shortestPaths(Network src, int init, int end)
        {
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
                            // drum.deleteAll();
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

    /*
     * private static String[][] detailedGeodesics(Network src) { int size =
     * src.getSize(); int i, j, vsize; // vsize = size of vector vsize = 0;
     * String[][] sp = new String[size][size]; // shortest paths for a specific
     * node Vector spv = null; // vector of shortest paths for (i = 0; i < size;
     * i++) { for (j = 0; j < size; j++) { if (i == j) { sp[i][j] = new
     * String(""); } else { spv = AgnaLib.shortestPaths(src, i, j); vsize =
     * spv.size(); sp[i][j] = new String(""); for (int k = 0; k < vsize; k++) {
     * sp[i][j] += (String)spv.elementAt(k); sp[i][j] += (char)255; } } } }
     * return sp; }
     */

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
        if (isConnected(src))
            {
            addPajekVectorIfSelected(out, selected, "Betweenness",
                    betweenness(src));
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

    private boolean isConnected(Network src)
        {
        int size = src.getSize();
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

    private int[][] geodesics(Network src)
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

    /*
     * private int[][] geodesicsOLD(Network src) { int size = src.getSize(); int
     * i, j, codarc; int[][] ge = new int[size][size]; // final result -
     * geodesics float[][] b = src.getMatrix(); float[][] mTmp = new
     * float[size][size]; for (i = 0; i < size; i++) { for (j = 0; j < size;
     * j++) { ge[i][j] = Integer.MAX_VALUE; } } for (i = 0; i < size; i++) { for
     * (j = 0; j < size; j++) { if (b[i][j] != 0f) mTmp[i][j]=1f; else
     * mTmp[i][j] = 0f; } } for (codarc = 1; codarc <= size; codarc++) //codarc
     * este lungimea unui arc *) { for (i = 0; i < size; i++) { for (j = 0; j <
     * size; j++) { if (ge[i][j] > codarc && b[i][j] != 0f) ge[i][j]=codarc; } }
     * b = multiplyMatrices(mTmp,b); for (i = 0; i < size; i++) b[i][i]=0f; }
     * for (i = 0; i < size; i++) { for (j = 0; j < size; j++) if
     * (ge[i][j]>Integer.MAX_VALUE - 1) ge[i][j]=0; } return ge; }
     */

    public String outGeodesics(Network outsrc)
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
        // 2.1.3: refuse disconnected networks (0 = no path
        // would silently corrupt the result)
        if (!isConnected(outsrc))
            {
            return it
                    + "WARNING: the network is disconnected; eccentricity cannot be computed\n"
                    + "(not all pairs of nodes are mutually reachable)." + unit + lb;
            }

        int size = outsrc.getSize();
        int i;
        StringBuffer out = new StringBuffer("");
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
        String out = new String("");
        out = it + bold + "Diameter" + unbold + " of " + unit
                + outsrc.getName();
        out += lb + it + "Diameter = " + unit
                + String.valueOf(matrixMax(geodesics(outsrc)));
        out += lb;
        return out;
        }

    private float[] eccentricity(Network src)
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
    private float[] fareness(Network src)
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
        // 2.1.3: refuse disconnected networks (0 = no path)
        if (!isConnected(outsrc))
            {
            return it
                    + "WARNING: the network is disconnected; fareness cannot be computed\n"
                    + "(not all pairs of nodes are mutually reachable)." + unit + lb;
            }

        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
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

        // freeman generalization:
        out
                .append(it + bold + "Freeman General Coefficient = " + unbold
                        + unit);
        out.append(String.valueOf(freemanGeneralIndex(outarray,
                fareness(new Network(size, 1)))));
        out.append(lb + lb);

        out.append(outStatistics(outarray, true));
        return out.toString();
        }

    // closeness: the inverse of fareness;
    // standard value: closeness multiplied by (size - 1)
    public float[] closeness(Network src)
        {
        int size = src.getSize();
        int i;
        float[] finarray =  fareness(src);
        for (i = 0; i < size; i++)
            {
            if (finarray[i] != 0f)
                finarray[i] = (float) (1 / (double) finarray[i]);
            }
        return finarray;
        }

    public String outCloseness(Network outsrc)
        {
        // 2.1.3: refuse disconnected networks (0 = no path
        // would silently corrupt the result)
        if (!isConnected(outsrc))
            {
            return it
                    + "WARNING: the network is disconnected; closeness cannot be computed\n"
                    + "(not all pairs of nodes are mutually reachable)." + unit + lb;
            }

        int size = outsrc.getSize();

        StringBuffer out = new StringBuffer("");
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

        // freeman generalization:
        out
                .append(it + bold + "Freeman General Coefficient = " + unbold
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
    /*
     * public float[][] multipleGeodesics(Network src) { // nu-i gata! int size =
     * src.getSize(); int i, j, codarc; // codarc = length of a geodesic path i =
     * 0; j = 0; codarc = 0; float[][] geomat = geodesics(src); // matrix of
     * geodesics float[][] srcmat = src.getMatrix();// matrix of src float[][]
     * powermat = new float[size][size]; // src successively multiplied by
     * itself float[][] athens = new float[size][size]; // athens[i][j] = number
     * of geodesics between i and j // ie, the matrix of multiple geodesics; to
     * be returned for (i = 0; i < size; i++) { for (j = 0; j < size; j++) {
     * athens[i][j] = 0f; } } // initializations end here powermat = srcmat; //
     * first step: power = 1 for (codarc = 1; codarc <= size; codarc++) { for (i =
     * 0; i < size; i++) { for (j = 0; j < size; j++) { if (geomat[i][j] ==
     * codarc) { athens[i][j] = powermat[i][j]; } } } powermat =
     * multiplyMatrices(powermat, srcmat); // next step } geomat = null; srcmat =
     * null; powermat = null;  return athens; }
     */

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
        double[] dep = new double[n];
        IntList stack = new IntList();
        IntList queue = new IntList();

        for (int s = 0; s < n; s++)
            {
            int[] sigma = new int[n];
            int[] dist = new int[n];
            for (int v = 0; v < n; v++)
                {
                pred[v] = new IntList();
                dist[v] = -1;
                }
            sigma[s] = 1;
            dist[s] = 0;
            queue.deleteAll();
            stack.deleteAll();
            queue.appendValue(s);

            int q = 0;
            while (q < queue.getSize())
                {
                int v = queue.getElementAt(q++).getValue();
                stack.appendValue(v);
                for (int w = 0; w < n; w++)
                    {
                    if (!mat[v][w])
                        {
                        continue;
                        }
                    if (dist[w] < 0)
                        {
                        dist[w] = dist[v] + 1;
                        queue.appendValue(w);
                        }
                    if (dist[w] == dist[v] + 1)
                        {
                        sigma[w] += sigma[v];
                        pred[w].appendValue(v);
                        }
                    }
                }

            for (int v = 0; v < n; v++)
                {
                dep[v] = 0;
                }
            while (stack.getSize() > 1)
                {
                int w = stack.getLastElement().getValue();
                IntListElement pre = pred[w].getFirstElement();
                while (pre != null)
                    {
                    int v = pre.getValue();
                    dep[v] += ((double) sigma[v] / (double) sigma[w])
                            * (1.0 + dep[w]);
                    pre = pre.getNext();
                    }
                stack.deleteElementAt(stack.getSize() - 1);
                }
            for (int v = 0; v < n; v++)
                {
                if (v != s)
                    {
                    cb[v] += (float) dep[v];
                    }
                }
            }
        return cb;
        }


    /*
     * public static String outDetailedGeodesics(Network outsrc) { AgnaLib();
     * String out = new String(""); int size = outsrc.getSize(); int k, lmax;
     * char kchar; boolean no_geodesic = true; String[][] outarray = new
     * String[size][size]; outarray = AgnaLib.detailedGeodesics(outsrc); out = it +
     * bold + "Detailed Geodesics" + unbold + " in "+ unit + outsrc.getName() +
     * lb; for (int i = 0; i < size; i++) { for (int j = 0; j < size; j++) { if
     * (outarray[i][j] != null && outarray[i][j].length() > 0) { no_geodesic =
     * false; lmax = outarray[i][j].length(); out += lb + it + "Shortest path(s)
     * from " + unit + outsrc.getActor(i).getName() + it + " to " + unit +
     * outsrc.getActor(j).getName() + it + ":" + unit; out += ol + li; for (k =
     * 0; k < lmax; k++) { kchar = outarray[i][j].charAt(k); out +=
     * blanc+blanc+blanc; // node separation if (kchar == (char)255) { //out +=
     * lb ; // path separation if (k < lmax-1) { out += unli; out += li; } }
     * else { try { //out += "\t" + String.valueOf((int)kchar + 1); out +=
     * outsrc.getActor((int)kchar).getName(); } catch(Exception e) { } } } out +=
     * unli + unol + blanc; } } } if (no_geodesic) { out += lb + it + "No
     * geodesic path found in this network." + unit + lb; } return out; }
     */

    // string-based;
    /*
     * public static String outShortestPathsOLD(Network outsrc, int i_from, int
     * i_to) { AgnaLib(); String out = new String(""); String path; int size =
     * outsrc.getSize(); char kchar; Vector sp = shortestPaths(outsrc, i_from,
     * i_to); if (sp.size() < 1 || i_from == i_to) return null; out = it + bold +
     * "Shortest Path(s)" + unbold + " from node "+ unit +
     * outsrc.getActor(i_from).getName() + it + " to node " + unit +
     * outsrc.getActor(i_to).getName() + lb; //out += ol; for (int i = 0; i <
     * sp.size(); i++) { //out += li + blanc+blanc+blanc; // node separation out +=
     * lb + blanc + blanc + blanc + blanc + blanc + "*" + blanc; path =
     * (String)sp.elementAt(i); if (path != null && path.length() > 1) { for
     * (int k = 0; k < path.length(); k++) { kchar = path.charAt(k); try { out +=
     * outsrc.getActor((int)kchar).getName(); out += blanc + blanc + blanc; }
     * catch(Exception e) { } } // end for k } // end if //out += unli; } // end
     * for i //out += unol; out += lb; return out; }
     */

    public String outShortestPaths(Network outsrc, int i_from, int i_to)
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
        out.append(lb);
        return out.toString();
        }

    public String outCliques(Network outsrc, int clique_diameter)
        {
        StringBuffer out = new StringBuffer("");

        IntList clique;
        int size = outsrc.getSize();
        Vector final_cliques = cliquesMain(outsrc, clique_diameter);
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
            clique = (IntList) final_cliques.elementAt(i);
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
        out.append(lb);

        /*
         * Clique my_clique = new Clique();
         * out.append(my_clique.cliquesMain(outsrc, clique_diameter));
         */
        return out.toString();
        }

    public String outBetweenness(Network outsrc)
        {
        // 2.1.3: refuse disconnected networks (0 = no path
        // would silently corrupt the result)
        if (!isConnected(outsrc))
            {
            return it
                    + "WARNING: the network is disconnected; betweenness cannot be computed\n"
                    + "(not all pairs of nodes are mutually reachable)." + unit + lb;
            }

        int size = outsrc.getSize();
        StringBuffer out = new StringBuffer("");
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

        // freeman generalization:
        out
                .append(it + bold + "Freeman General Coefficient = " + unbold
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
    private float[] bavelas(Network src)
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
        // 2.1.3: refuse disconnected networks (0 = no path)
        if (!isConnected(outsrc))
            {
            return it
                    + "WARNING: the network is disconnected; bavelas cannot be computed\n"
                    + "(not all pairs of nodes are mutually reachable)." + unit + lb;
            }

        int size = outsrc.getSize();
        float[] outarray = new float[size];
        if (outarray == null)
            {
            return it
                    + "Bavelas-Leavitt centrality cannot be computed for an empty network."
                    + unit;
            }
        StringBuffer out = new StringBuffer("");
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
         * if (!MainFrame.getCurrentWeight()) { // binary data: out += td + it +
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

        // freeman generalization:
        out
                .append(it + bold + "Freeman General Coefficient = " + unbold
                        + unit);
        out.append(String.valueOf(freemanGeneralIndex(outarray,
                bavelas(new Network(size, 1)))));
        out.append(lb + lb);

        // generating statistics:
        out.append(it + bold + "Bavelas-Leavitt Centrality Statistics" + unbold
                + unit);
        out.append(lb + outStatistics(outarray, false));

        outarray = null;

        /*
         * if (MainFrame.getCurrentWeight()) { out += it + bold + "Weighted
         * Bavelas-Leavitt Centrality Statistics" + unbold + unit; out += lb +
         * outStatistics(outrecwei, false); outrecwei = null; }
         */
        return out.toString();
        }

    public void normalize(Network src)
        {
        int size = src.getSize();
        int i, j;
        float finval = 0f;
        for (i = 0; i < size; i++)
            {
            for (j = 0; j < size; j++)
                {
                if (src.getValue(i, j) != 0)
                    {
                    finval = 1f;
                    } else
                    finval = 0f;
                src.setValue(finval, i, j);
                }
            }
        }

    private int getOusidersNumber(Network src)
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
        // AgnaLib();
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
        // AgnaLib();
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
    /*
     * public String outOpenChainSummaryOLD(Network outsrc) { //AgnaLib(); int size =
     * outsrc.getSize(); StringBuffer out = new StringBuffer(""); int[]
     * frequencies = openChainSummary(outsrc); float[] probabilities =
     * frequenciesToProbabilities(frequencies); out.append(it + "New network
     * created from chain file." + unit); out.append(lb + it + bold + "Network
     * name: " + unbold + unit + outsrc.getName()); out.append(lb + it + bold +
     * "Number of sequence codes identified: " + unbold + unit +
     * String.valueOf(size)); out.append(lb + it + bold + "Chain summary:" +
     * unbold + unit); out.append(lb + table + tr); out.append(td + it +
     * "Sequence code" + unit + untd + td + it + "Frequency" + unit + untd + td +
     * it + "Probability" + unit + untd + untr); for (int i = 0; i < size; i++) {
     * out.append(tr); out.append(td + it + outsrc.getActor(i).getName() + unit +
     * untd + td + String.valueOf(frequencies[i]) + untd + td +
     * String.valueOf(probabilities[i]) +untd); out.append(untr); }
     * out.append(untable); // generating statistics: out.append(it + bold +
     * "Sequence Frequency Statistics" + unbold + unit); out.append(lb +
     * outStatistics(frequencies, false)); out.append(it + bold + "Sequence
     * Probability Statistics" + unbold + unit); out.append(lb +
     * outStatistics(probabilities, false)); return out.toString(); }
     */

    // computes the network cohesion index:
    // divides the number of mutual choices in a binary directed matrix
    // by the maximum possible number of such choices (Knoke)
    private float cohesion(Network src)
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
        // AgnaLib();
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
        // AgnaLib();
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

        if (!MainFrame.getCurrentWeight())
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

        if (MainFrame.getCurrentWeight())
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
                // if (mat[j][i] != 0f)
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
        int i, j;
        float[] finarray = new float[size];
        float[][] mat = src.getMatrix();
        double tmp = 0;
        for (i = 0; i < size; i++)
            {
            // finarray[i] = 0f;
            tmp = 0;
            for (j = 0; j < size; j++)
                {
                // finarray[i] += mat[j][i];
                tmp += (double) mat[j][i];
                }
            finarray[i] = (float) tmp;
            }
        return finarray;
        }

    // returns a string (publicable) version of receptionDegree()
    public String outReceptionDegree(Network outsrc)
        {
        // AgnaLib();
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
        // AgnaLib();
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

        if (!MainFrame.getCurrentWeight())
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

        if (MainFrame.getCurrentWeight())
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
        // AgnaLib();
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

        if (!MainFrame.getCurrentWeight())
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

        if (MainFrame.getCurrentWeight())
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
                if (src.getValue(i, j) != 0f)
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
        // AgnaLib();
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

        if (!MainFrame.getCurrentWeight())
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

    private float density(Network src) // binary data
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

    private float weightedDensity(Network src) // weighted data
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

        if (MainFrame.getCurrentWeight())
            {
            // weighted data:
            out += lb + it + "Weighted Density = " + unit
                    + String.valueOf(weightedDensity(outsrc));
            out += lb;
            }

        return out;
        }

    // considers data as weighted:
    private float[] determinationDegree(Network src)
        {
        int size = src.getSize();
        int i, j;
        float[] finarray = new float[size];
        float[][] mat = src.getMatrix();
        double tmp = 0;
        for (i = 0; i < size; i++)
            {
            // finarray [i] = 0f;
            tmp = 0;
            for (j = 0; j < size; j++)
                {
                // finarray[i] += mat[j][i] - mat [i][j];
                tmp += (double) mat[j][i] - (double) mat[i][j];
                }
            finarray[i] = (float) ((double) tmp / ((double) size - 1));
            }

        return finarray;
        }

    public String outDeterminationDegree(Network outsrc)
        {
        // AgnaLib();
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
        // AgnaLib();
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

    // returns freeman's general index;
    // star_network_index are the centrality indexes of the corresponding star
    // network;
    private float freemanGeneralIndex(float[] a, float[] star_network_index)
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
    private float[] statistics(float[] a)
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

    /*
     * private void doNap() { try { Thread.sleep(400); } catch(Exception e) {}
     * ProgressDialog tmp_pd = MainFrame.getCurrentProgressDialog(); if (tmp_pd ==
     * null || tmp_pd.getStop()) {
     * MainFrame.getCurrentFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     * MainFrame.setCurrentStatus(MainFrame.default_status);
     * //tmp_pd.setPercent(-1); if (Thread.currentThread() != null) { Thread
     * tmp_thread = Thread.currentThread(); if (tmp_thread.isAlive()) { try {
     * tmp_pd.stopPane(); tmp_thread.interrupt(); tmp_thread = null; } catch
     * (Exception e) { } } } } else { tmp_pd.setPercent(tmp_pd.getPercent() +
     * 20); } }
     */

    }