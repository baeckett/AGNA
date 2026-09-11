package com.bentza.sna.net;

import java.util.Random;

/**
 * 2.1.3: deterministic network layouts for headless rendering (the CLI and
 * any batch consumer). Positions are set on the actors as pixel
 * coordinates; the viewer and the image renderer both read them from the
 * same percent-based NodeXY storage.
 */
public class NetworkLayouts
    {
    public static final int CIRCULAR = 0;
    public static final int RANDOM = 1;
    public static final int SPRING = 2;
    public static final int GRID = 3;
    public static final int CONCENTRIC = 4;
    public static final int STAR = 5;

    public static final int SPRING_ITERATIONS = 60;
    public static final int SPRING_BH_ABOVE = 256;
    public static final float SPRING_THETA = 0.7f;

    private NetworkLayouts()
        {
        }

    public static void apply(Network net, int layout, int width, int height)
        {
        apply(net, layout, width, height, SPRING_ITERATIONS);
        }

    public static void apply(Network net, int layout, int width, int height,
            int iterations)
        {
        int n = net.getSize();
        if (n == 0)
            {
            return;
            }
        // freshly built networks may carry actors with no coordinates yet
        for (int i = 0; i < n; i++)
            {
            net.getActor(i).createCoordinatesIfMissing();
            }
        if (layout == GRID)
            {
            applyGrid(net, width, height);
            return;
            }
        if (layout == CONCENTRIC)
            {
            applyConcentric(net, width, height);
            return;
            }
        if (layout == STAR)
            {
            applyStar(net, width, height);
            return;
            }
        if (layout == CIRCULAR)
            {
            double cx = width / 2.0;
            double cy = height / 2.0;
            double radius = Math.min(width, height) * 0.42;
            for (int i = 0; i < n; i++)
                {
                double a = 2 * Math.PI * i / n;
                net.getActor(i).setX((int) Math.round(cx + radius * Math.cos(a)),
                        width);
                net.getActor(i).setY((int) Math.round(cy + radius * Math.sin(a)),
                        height);
                }
            return;
            }
        Random rnd = new Random(20260910L);
        if (layout == RANDOM)
            {
            for (int i = 0; i < n; i++)
                {
                net.getActor(i).setX(10 + rnd.nextInt(Math.max(1, width - 20)),
                        width);
                net.getActor(i).setY(10 + rnd.nextInt(Math.max(1, height - 20)),
                        height);
                }
            return;
            }
        // SPRING (Fruchterman-Reingold style, deterministic; Barnes-Hut
        // quadtree repulsion turns the per-iteration cost quasi-linear
        // above SPRING_BH_ABOVE nodes)
        float[] px = new float[n];
        float[] py = new float[n];
        for (int i = 0; i < n; i++)
            {
            px[i] = 10 + rnd.nextInt(Math.max(1, width - 20));
            py[i] = 10 + rnd.nextInt(Math.max(1, height - 20));
            }
        double area = (double) width * height;
        double k = Math.sqrt(area / Math.max(1, n));
        int iters = Math.max(1, iterations);
        boolean barnesHut = n > SPRING_BH_ABOVE;
        // sparse neighbour rows, built once: the attraction model is
        // unchanged, only the per-iteration matrix scan is removed
        int[][] neighbors = buildNeighbors(net);
        for (int iter = 0; iter < iters; iter++)
            {
            float[] fx = new float[n];
            float[] fy = new float[n];
            // classic Fruchterman-Reingold cooling: large moves early,
            // gentle settling later
            float temp = 1f - (float) iter / iters;
            if (barnesHut)
                {
                bhRepulsion(fx, fy, px, py, k, width, height, SPRING_THETA);
                } else
                {
                pairRepulsion(fx, fy, px, py, k);
                }
            for (int i = 0; i < n; i++)
                {
                for (int j = 0; j < neighbors[i].length; j++)
                    {
                    int v = neighbors[i][j];
                    float dx = px[i] - px[v];
                    float dy = py[i] - py[v];
                    float dist = (float) Math.max(1.0, Math.hypot(dx, dy));
                    float attr = (float) (dist * dist / k);
                    float nx = dx / dist;
                    float ny = dy / dist;
                    fx[i] -= attr * nx;
                    fy[i] -= attr * ny;
                    }
                }
            for (int i = 0; i < n; i++)
                {
                // 2.1.3: gentle moves (0.2 factor): the earlier full
                // temperature blew nodes into the canvas clamp, where
                // sparse graphs could not pull them back - the classic
                // "nodes stuck on the border" artifact
                float maxDisp = (float) (temp * k * 0.2);
                float dx = Math.max(-maxDisp, Math.min(maxDisp, fx[i]));
                float dy = Math.max(-maxDisp, Math.min(maxDisp, fy[i]));
                px[i] = Math.max(10, Math.min(width - 10, px[i] + dx));
                py[i] = Math.max(10, Math.min(height - 10, py[i] + dy));
                }
            }
        // 2.1.3: fit the final layout into the canvas, like the desktop
        // viewer does after every layout: spread the occupied bounding
        // box across the full area with a margin, so no cluster is left
        // pinned to an edge
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++)
            {
            minX = Math.min(minX, px[i]);
            maxX = Math.max(maxX, px[i]);
            minY = Math.min(minY, py[i]);
            maxY = Math.max(maxY, py[i]);
            }
        if (maxX - minX > 1f && maxY - minY > 1f)
            {
            float spanX = width - 20f;
            float spanY = height - 20f;
            for (int i = 0; i < n; i++)
                {
                px[i] = 10f + (px[i] - minX) * spanX / (maxX - minX);
                py[i] = 10f + (py[i] - minY) * spanY / (maxY - minY);
                }
            }
        for (int i = 0; i < n; i++)
            {
            net.getActor(i).setX((int) Math.round(px[i]), width);
            net.getActor(i).setY((int) Math.round(py[i]), height);
            }
        }

    // exact O(n^2) all-pairs repulsion (used below SPRING_BH_ABOVE),
    // bit-identical to the original embedder
    private static void pairRepulsion(float[] fx, float[] fy, float[] px,
            float[] py, double k)
        {
        int n = px.length;
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                float dx = px[i] - px[j];
                float dy = py[i] - py[j];
                float dist = (float) Math.max(1.0, Math.hypot(dx, dy));
                float rep = (float) (k * k / dist);
                float nx = dx / dist;
                float ny = dy / dist;
                fx[i] += rep * nx;
                fy[i] += rep * ny;
                fx[j] -= rep * nx;
                fy[j] -= rep * ny;
                }
            }
        }

    // Barnes-Hut quadtree repulsion: distant cells act as one body at
    // their centre of mass; a cell of side s at distance d is opened only
    // while s / d >= theta. Deterministic: nodes are inserted in index
    // order and children are visited in a fixed order.
    private static void bhRepulsion(float[] fx, float[] fy, float[] px,
            float[] py, double k, int width, int height, float theta)
        {
        int n = px.length;
        java.util.ArrayList<BhCell> cells = new java.util.ArrayList<>(4 * n);
        BhCell root = new BhCell();
        root.minx = 0f;
        root.miny = 0f;
        root.half = Math.max(width, height) / 2f;
        cells.add(root);
        for (int i = 0; i < n; i++)
            {
            bhInsert(cells, root, i, px, py, 0);
            }
        bhAccumulate(cells, root, px, py);
        double k2 = k * k;
        for (int i = 0; i < n; i++)
            {
            bhQuery(cells, 0, i, px[i], py[i], px, py, k2, theta, fx, fy);
            }
        }

    private static void bhInsert(java.util.ArrayList<BhCell> cells,
            BhCell cell, int node, float[] px, float[] py, int depth)
        {
        if (cell.occupant >= 0)
            {
            if (cell.occupant == node || (cell.extras != null
                    && cell.extras.contains(node)))
                {
                return;
                }
            if (depth >= 64)
                {
                // coordinates too close for halving to separate them
                // (identical floats); pack into the leaf
                if (cell.extras == null)
                    {
                    cell.extras = new java.util.ArrayList<>();
                    }
                cell.extras.add(node);
                return;
                }
            int held = cell.occupant;
            cell.occupant = -1;
            java.util.ArrayList<Integer> heldExtras = cell.extras;
            cell.extras = null;
            bhPush(cells, cell, held, px, py, depth);
            if (heldExtras != null)
                {
                for (int extra : heldExtras)
                    {
                    bhPush(cells, cell, extra, px, py, depth);
                    }
                }
            }
        bhPlace(cells, cell, node, px, py, depth);
        }

    private static void bhPlace(java.util.ArrayList<BhCell> cells,
            BhCell cell, int node, float[] px, float[] py, int depth)
        {
        if (cell.occupant == -1 && cell.extras == null
                && !cell.isInternal())
            {
            cell.occupant = node;
            return;
            }
        bhPush(cells, cell, node, px, py, depth);
        }

    // descend: the cell is (or just became) internal; the node goes into
    // its quadrant child, created on demand - never back into the parent
    private static void bhPush(java.util.ArrayList<BhCell> cells,
            BhCell cell, int node, float[] px, float[] py, int depth)
        {
        float x = px[node];
        float y = py[node];
        int q = (x >= cell.minx + cell.half ? 1 : 0)
                + (y >= cell.miny + cell.half ? 2 : 0);
        if (cell.children[q] == -1)
            {
            BhCell child = new BhCell();
            child.minx = cell.minx + (q % 2 == 0 ? 0f : cell.half);
            child.miny = cell.miny + (q / 2 == 0 ? 0f : cell.half);
            child.half = cell.half / 2f;
            cell.children[q] = cells.size();
            cells.add(child);
            }
        bhInsert(cells, cells.get(cell.children[q]), node, px, py,
                depth + 1);
        }

    private static void bhAccumulate(java.util.ArrayList<BhCell> cells,
            BhCell cell, float[] px, float[] py)
        {
        if (cell.occupant >= 0)
            {
            cell.mass = 1;
            cell.cmx = px[cell.occupant];
            cell.cmy = py[cell.occupant];
            if (cell.extras != null)
                {
                double sx = cell.cmx;
                double sy = cell.cmy;
                for (int extra : cell.extras)
                    {
                    sx += px[extra];
                    sy += py[extra];
                    }
                cell.mass = 1 + cell.extras.size();
                cell.cmx = (float) (sx / cell.mass);
                cell.cmy = (float) (sy / cell.mass);
                }
            return;
            }
        int m = 0;
        double sx = 0.0;
        double sy = 0.0;
        for (int i = 0; i < 4; i++)
            {
            int ci = cell.children[i];
            if (ci != -1)
                {
                BhCell child = cells.get(ci);
                bhAccumulate(cells, child, px, py);
                m += child.mass;
                sx += (double) child.cmx * child.mass;
                sy += (double) child.cmy * child.mass;
                }
            }
        cell.mass = m;
        if (m > 0)
            {
            cell.cmx = (float) (sx / m);
            cell.cmy = (float) (sy / m);
            }
        }

    private static void bhQuery(java.util.ArrayList<BhCell> cells, int ci,
            int node, float xi, float yi, float[] px, float[] py, double k2,
            float theta, float[] fx, float[] fy)
        {
        BhCell cell = cells.get(ci);
        if (cell.occupant >= 0 || cell.extras != null)
            {
            if (cell.occupant >= 0 && cell.occupant != node)
                {
                repulseOne(fx, fy, node, xi, yi, px[cell.occupant],
                        py[cell.occupant], k2);
                }
            if (cell.extras != null)
                {
                for (int other : cell.extras)
                    {
                    if (other != node)
                        {
                        repulseOne(fx, fy, node, xi, yi, px[other],
                                py[other], k2);
                        }
                    }
                }
            return;
            }
        // the macro-body rule never applies to the cell containing the
        // query node (its mass includes the node itself)
        if (xi >= cell.minx && xi < cell.minx + 2 * cell.half
                && yi >= cell.miny && yi < cell.miny + 2 * cell.half)
            {
            openChildren(cells, cell, node, xi, yi, px, py, k2, theta, fx,
                    fy);
            return;
            }
        double dx = xi - cell.cmx;
        double dy = yi - cell.cmy;
        double dist = Math.hypot(dx, dy);
        if (cell.mass > 0 && dist > 1e-3
                && (2.0 * cell.half) / dist < theta)
            {
            double d2 = Math.max(1.0, dist * dist);
            float c = (float) (k2 * cell.mass / d2);
            fx[node] += c * (float) dx;
            fy[node] += c * (float) dy;
            return;
            }
        openChildren(cells, cell, node, xi, yi, px, py, k2, theta, fx, fy);
        }

    private static void openChildren(java.util.ArrayList<BhCell> cells,
            BhCell cell, int node, float xi, float yi, float[] px,
            float[] py, double k2, float theta, float[] fx, float[] fy)
        {
        for (int i = 0; i < 4; i++)
            {
            if (cell.children[i] != -1)
                {
                bhQuery(cells, cell.children[i], node, xi, yi, px, py, k2,
                        theta, fx, fy);
                }
            }
        }

    // one pair contribution k2 * dx / dist^2 - the same form as the
    // exact pair loop, applied to the query node only
    private static void repulseOne(float[] fx, float[] fy, int node,
            float xi, float yi, float xj, float yj, double k2)
        {
        float dx = xi - xj;
        float dy = yi - yj;
        float dist = Math.max(1f, (float) Math.hypot(dx, dy));
        float c = (float) (k2 / (dist * dist));
        fx[node] += c * dx;
        fy[node] += c * dy;
        }

    // deterministic sparse neighbour rows (ascending), built once
    private static int[][] buildNeighbors(Network net)
        {
        int n = net.getSize();
        int[] counts = new int[n];
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                if (net.getValue(i, j) != 0f)
                    {
                    counts[i]++;
                    counts[j]++;
                    }
                }
            }
        int[][] neighbors = new int[n][];
        for (int i = 0; i < n; i++)
            {
            neighbors[i] = new int[counts[i]];
            }
        int[] fill = new int[n];
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                if (net.getValue(i, j) != 0f)
                    {
                    neighbors[i][fill[i]++] = j;
                    neighbors[j][fill[j]++] = i;
                    }
                }
            }
        return neighbors;
        }

    // test hook: exact vs Barnes-Hut repulsion over a fixed configuration
    static float[] repulsionForces(int n, float[] px, float[] py, double k,
            int width, int height, boolean barnesHut)
        {
        float[] fx = new float[n];
        float[] fy = new float[n];
        if (barnesHut)
            {
            bhRepulsion(fx, fy, px, py, k, width, height, SPRING_THETA);
            } else
            {
            pairRepulsion(fx, fy, px, py, k);
            }
        float[] out = new float[2 * n];
        for (int i = 0; i < n; i++)
            {
            out[2 * i] = fx[i];
            out[2 * i + 1] = fy[i];
            }
        return out;
        }

    private static final class BhCell
        {
        float minx, miny, half; // cell bounds: [minx, minx + 2*half)
        int occupant = -1; // first leaf node
        java.util.ArrayList<Integer> extras; // packed co-located nodes
        float cmx, cmy; // centre of mass (post-build)
        int mass; // node count beneath this cell
        final int[] children = { -1, -1, -1, -1 };

        boolean isInternal()
            {
            return children[0] != -1 || children[1] != -1
                    || children[2] != -1 || children[3] != -1;
            }
        }

    // deterministic row/column lattice
    private static void applyGrid(Network net, int width, int height)
        {
        int n = net.getSize();
        int cols = (int) Math.ceil(Math.sqrt(n));
        int rows = (int) Math.ceil((double) n / cols);
        float mx = width * 0.08f;
        float my = height * 0.08f;
        float cellW = (width - 2 * mx) / cols;
        float cellH = (height - 2 * my) / rows;
        for (int k = 0; k < n; k++)
            {
            int col = k % cols;
            int row = k / cols;
            net.getActor(k).setX((int) Math.round(mx + col * cellW
                    + cellW / 2), width);
            net.getActor(k).setY((int) Math.round(my + row * cellH
                    + cellH / 2), height);
            }
        }

    // hub-and-spoke: the highest-degree actor sits in the centre, the
    // rest fan out on concentric rings (ring r holds ranks r^2..(r+1)^2)
    private static void applyConcentric(Network net, int width, int height)
        {
        int n = net.getSize();
        Integer[] rank = new Integer[n];
        for (int i = 0; i < n; i++)
            {
            rank[i] = i;
            }
        java.util.Arrays.sort(rank, new java.util.Comparator<Integer>()
            {
            public int compare(Integer a, Integer b)
                {
                return degree(net, b) - degree(net, a);
                }
            });
        double cx = width / 2.0;
        double cy = height / 2.0;
        int rings = (int) Math.ceil(Math.sqrt(n));
        double maxR = Math.min(width, height) * 0.42;
        for (int i = 0; i < n; i++)
            {
            int r = (int) Math.floor(Math.sqrt(i));
            double radius = rings > 1 ? maxR * r / (rings - 1) : 0.0;
            double angle = (i - r * r) * 2 * Math.PI
                    / Math.max(1, 2 * r + 1) + r * 0.5;
            if (r == 0)
                {
                radius = 0.0;
                }
            net.getActor(rank[i]).setX((int) Math.round(cx + radius
                    * Math.cos(angle)), width);
            net.getActor(rank[i]).setY((int) Math.round(cy + radius
                    * Math.sin(angle)), height);
            }
        }

    private static int degree(Network net, int i)
        {
        int d = 0;
        for (int j = 0; j < net.getSize(); j++)
            {
            if (i != j && net.getValue(i, j) != 0f)
                {
                d++;
                }
            }
        return d;
        }

    // hub-and-spoke: node 0 at the centre, everyone else on a ring
    private static void applyStar(Network net, int width, int height)
        {
        int n = net.getSize();
        double cx = width / 2.0;
        double cy = height / 2.0;
        double radius = Math.min(width, height) * 0.38;
        net.getActor(0).setX((int) Math.round(cx), width);
        net.getActor(0).setY((int) Math.round(cy), height);
        for (int i = 1; i < n; i++)
            {
            double a = 2 * Math.PI * (i - 1) / Math.max(1, n - 1);
            net.getActor(i).setX((int) Math.round(cx + radius * Math.cos(a)),
                    width);
            net.getActor(i).setY((int) Math.round(cy + radius * Math.sin(a)),
                    height);
            }
        }
    }
