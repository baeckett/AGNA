/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generators for standard and synthetic network topologies.
 * Supported topologies:
 * - Empty network (isolated nodes)
 * - Star network (symmetric or asymmetric directed from central node)
 * - Circular network (undirected ring)
 * - Full binary network (complete clique without self-loops)
 * - Random binary connected network with no outsiders
 * - Random weighted network (weights strictly in (0.0, 1.0))
 * - Square lattice network N x N (L x L = N grid with 4-neighbour connectivity)
 * - Hexagram lattice network (Star-of-David triangular/hexagonal lattice)
 */
public class NetworkGenerator
    {
    public static final int MAX_SAFE_NODES = 5000;

    private NetworkGenerator()
        {
        }

    /**
     * Creates an empty network with isolated nodes.
     */
    public static Network createEmpty(int n)
        {
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName("Empty Network (" + n + " nodes)");
        NetworkLayouts.apply(net, NetworkLayouts.CIRCULAR, 600, 600);
        return net;
        }

    /**
     * Creates a star network.
     * Central node is node 0.
     * If symmetric is true, edges connect both 0 <-> j.
     * If symmetric is false, edges are directed 0 -> j (central emissions).
     */
    public static Network createStar(int n, boolean symmetric)
        {
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName(symmetric ? "Symmetric Star Network" : "Asymmetric Star Network");
        for (int j = 1; j < n; j++)
            {
            net.setValue(1.0f, 0, j);
            if (symmetric)
                {
                net.setValue(1.0f, j, 0);
                }
            }
        net.setSymmetry(symmetric);
        NetworkLayouts.apply(net, NetworkLayouts.STAR, 600, 600);
        return net;
        }

    /**
     * Creates a circular ring network (undirected).
     */
    public static Network createCircular(int n)
        {
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName("Circular Network (" + n + " nodes)");
        for (int i = 0; i < n; i++)
            {
            int next = (i + 1) % n;
            net.setValue(1.0f, i, next);
            net.setValue(1.0f, next, i);
            }
        net.setSymmetry(true);
        NetworkLayouts.apply(net, NetworkLayouts.CIRCULAR, 600, 600);
        return net;
        }

    /**
     * Creates a complete full binary network (all pairs connected, no self loops).
     * Positioned in star layout with central node in the center as requested.
     */
    public static Network createFullBinary(int n)
        {
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName("Full Network (" + n + " nodes)");
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                if (i != j)
                    {
                    net.setValue(1.0f, i, j);
                    }
                }
            }
        net.setSymmetry(true);
        NetworkLayouts.apply(net, NetworkLayouts.STAR, 600, 600);
        return net;
        }

    /**
     * Creates a random connected binary network with NO outsiders (no isolated or zero-degree nodes).
     * Algorithm:
     * 1. Constructs a random spanning tree across all nodes to guarantee connectedness and 0 outsiders.
     * 2. Adds additional random edges based on target density/probability.
     */
    public static Network createRandomConnected(int n, double extraEdgeProb, long seed)
        {
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName("Connected Random Network (" + n + " nodes)");
        Random rng = seed != 0 ? new Random(seed) : new Random();

        // Step 1: Random spanning tree (permuted sequence)
        List<Integer> perm = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            {
            perm.add(i);
            }
        Collections.shuffle(perm, rng);

        for (int i = 1; i < n; i++)
            {
            int u = perm.get(i);
            int v = perm.get(rng.nextInt(i)); // connect to any previously connected node
            net.setValue(1.0f, u, v);
            net.setValue(1.0f, v, u);
            }

        // Step 2: Extra edges
        double p = Math.max(0.0, Math.min(1.0, extraEdgeProb));
        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                if (net.getValue(i, j) == 0.0f && rng.nextDouble() < p)
                    {
                    net.setValue(1.0f, i, j);
                    net.setValue(1.0f, j, i);
                    }
                }
            }
        net.setSymmetry(true);
        NetworkLayouts.apply(net, NetworkLayouts.SPRING, 600, 600);
        return net;
        }

    /**
     * Creates a random weighted network.
     * Edge values are strictly between 0 and 1 (0 < weight < 1, e.g. 0.001 to 0.999),
     * never touching margin values 0.0 or 1.0.
     */
    public static Network createRandomWeighted(int n, double density, boolean symmetric, long seed)
        {
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName("Random Weighted Network (" + n + " nodes)");
        Random rng = seed != 0 ? new Random(seed) : new Random();
        double p = Math.max(0.01, Math.min(1.0, density));

        for (int i = 0; i < n; i++)
            {
            int startJ = symmetric ? i + 1 : 0;
            for (int j = startJ; j < n; j++)
                {
                if (i != j && rng.nextDouble() < p)
                    {
                    // Float strictly within (0.0, 1.0): minimum 0.001, maximum 0.999
                    float val = 0.001f + rng.nextFloat() * 0.998f;
                    net.setValue(val, i, j);
                    if (symmetric)
                        {
                        net.setValue(val, j, i);
                        }
                    }
                }
            }
        net.setSymmetry(symmetric);
        NetworkLayouts.apply(net, NetworkLayouts.SPRING, 600, 600);
        return net;
        }

    /**
     * Creates an L x L square lattice network with 4-neighbour connectivity.
     * Total nodes = L * L.
     */
    public static Network createSquareLattice(int sideLength)
        {
        if (sideLength < 2)
            {
            throw new IllegalArgumentException("Square lattice side length must be at least 2.");
            }
        int n = sideLength * sideLength;
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName("Square Lattice (" + sideLength + "x" + sideLength + ")");

        for (int r = 0; r < sideLength; r++)
            {
            for (int c = 0; c < sideLength; c++)
                {
                int u = r * sideLength + c;
                // Horizontal neighbour
                if (c + 1 < sideLength)
                    {
                    int right = r * sideLength + (c + 1);
                    net.setValue(1.0f, u, right);
                    net.setValue(1.0f, right, u);
                    }
                // Vertical neighbour
                if (r + 1 < sideLength)
                    {
                    int down = (r + 1) * sideLength + c;
                    net.setValue(1.0f, u, down);
                    net.setValue(1.0f, down, u);
                    }
                }
            }
        net.setSymmetry(true);
        applySquareLatticeLayout(net, sideLength, 600, 600);
        return net;
        }

    /**
     * Creates a Hexagonal (bee-hive / honeycomb) lattice network of radius R.
     * Radius R corresponds to concentric rings of regular hexagonal cells around a central hexagon:
     * - R=1: 1 hexagon, 6 nodes
     * - R=2: 7 hexagons, 24 nodes
     * - R=3: 19 hexagons, 54 nodes
     * - R=4: 37 hexagons, 96 nodes
     * - R=5: 61 hexagons, 150 nodes
     * Total nodes = 6 * R^2.
     * Edges follow the hexagonal cell boundaries (each interior node has degree 3).
     * By default assigns positions in the Network Viewer in a bee-hive layout.
     */
    public static Network createBeeHiveLattice(int radius)
        {
        if (radius < 1)
            {
            throw new IllegalArgumentException("Bee-hive lattice radius must be at least 1.");
            }
        double S = 100.0;
        List<HexPoint> hexes = new ArrayList<>();
        for (int q = -radius; q <= radius; q++)
            {
            for (int r = -radius; r <= radius; r++)
                {
                if (Math.max(Math.max(Math.abs(q), Math.abs(r)), Math.abs(q + r)) < radius)
                    {
                    hexes.add(new HexPoint(q, r));
                    }
                }
            }

        List<Point2D> vertices = new ArrayList<>();
        List<int[]> edges = new ArrayList<>();

        for (HexPoint hp : hexes)
            {
            double xc = S * Math.sqrt(3.0) * (hp.q + hp.r / 2.0);
            double yc = S * 1.5 * hp.r;
            int[] hexV = new int[6];
            for (int k = 0; k < 6; k++)
                {
                double angle = Math.PI / 6.0 + k * Math.PI / 3.0;
                double vx = xc + S * Math.cos(angle);
                double vy = yc + S * Math.sin(angle);
                hexV[k] = getOrCreateVertex(vertices, vx, vy, 0.1 * S);
                }
            for (int k = 0; k < 6; k++)
                {
                int u = hexV[k];
                int v = hexV[(k + 1) % 6];
                if (u != v)
                    {
                    edges.add(new int[]{ Math.min(u, v), Math.max(u, v) });
                    }
                }
            }

        int n = vertices.size();
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName("Hexagonal Bee-Hive Lattice (" + n + " nodes, " + hexes.size() + " cells)");

        for (int[] e : edges)
            {
            net.setValue(1.0f, e[0], e[1]);
            net.setValue(1.0f, e[1], e[0]);
            }
        net.setSymmetry(true);
        applyBeeHiveLayout(net, vertices, 600, 600);
        return net;
        }

    /**
     * Alias for createBeeHiveLattice.
     */
    public static Network createHexagonalLattice(int radius)
        {
        return createBeeHiveLattice(radius);
        }

    private static int getOrCreateVertex(List<Point2D> vertices, double x, double y, double eps)
        {
        for (int i = 0; i < vertices.size(); i++)
            {
            Point2D p = vertices.get(i);
            if (Math.hypot(p.x - x, p.y - y) < eps)
                {
                return i;
                }
            }
        vertices.add(new Point2D(x, y));
        return vertices.size() - 1;
        }

    public static void applyBeeHiveLayout(Network net, List<Point2D> points, int width, int height)
        {
        int n = points.size();
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (int i = 0; i < n; i++)
            {
            Point2D p = points.get(i);
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
            }

        double spanX = Math.max(1e-4, maxX - minX);
        double spanY = Math.max(1e-4, maxY - minY);
        double maxSpan = Math.max(spanX, spanY);

        int margin = 50;
        double usableSize = Math.min(width, height) - 2 * margin;
        double scale = usableSize / maxSpan;

        double cx = width / 2.0;
        double cy = height / 2.0;

        for (int i = 0; i < n; i++)
            {
            Point2D p = points.get(i);
            int x = (int) Math.round(cx + (p.x - (minX + spanX / 2.0)) * scale);
            int y = (int) Math.round(cy + (p.y - (minY + spanY / 2.0)) * scale);
            net.getActor(i).createCoordinatesIfMissing();
            net.getActor(i).setX(x, width);
            net.getActor(i).setY(y, height);
            }
        }

    public static void applySquareLatticeLayout(Network net, int L, int width, int height)
        {
        int margin = 50;
        int usableW = width - 2 * margin;
        int usableH = height - 2 * margin;
        double stepX = L > 1 ? (double) usableW / (L - 1) : 0;
        double stepY = L > 1 ? (double) usableH / (L - 1) : 0;

        for (int r = 0; r < L; r++)
            {
            for (int c = 0; c < L; c++)
                {
                int idx = r * L + c;
                int x = (int) Math.round(margin + c * stepX);
                int y = (int) Math.round(margin + r * stepY);
                net.getActor(idx).createCoordinatesIfMissing();
                net.getActor(idx).setX(x, width);
                net.getActor(idx).setY(y, height);
                }
            }
        }

    /**
     * Creates a Hexagram (Star-of-David) lattice network.
     * Order k creates a 6-pointed star triangular lattice:
     * - k=1: central hexagon + 6 outer tips (12 vertices, 13 points total or perimeter ring)
     * For order k:
     * We generate a regular hexagram lattice by taking triangular grid coordinates (q, r)
     * forming a union of two overlapping opposing triangles (the hexagram).
     * Connected by triangular lattice adjacencies (distance = 1 in triangular coordinates).
     */
    public static Network createHexagramLattice(int order)
        {
        if (order < 1)
            {
            throw new IllegalArgumentException("Hexagram lattice order must be at least 1.");
            }
        // Triangular lattice bounds for hexagram of radius R = 2*order:
        // A point (x, y) belongs to the hexagram if it lies in Triangle 1 (pointing up)
        // OR Triangle 2 (pointing down).
        List<HexPoint> points = new ArrayList<>();
        int R = order;
        // Generate points in hexagonal coordinate frame:
        // A standard Star of David consists of 1 central hexagon of radius R plus 6 triangular points of size R.
        // We can cleanly index coordinates:
        for (int q = -2 * R; q <= 2 * R; q++)
            {
            for (int r = -2 * R; r <= 2 * R; r++)
                {
                int s = -q - r;
                // Upright triangle: q <= R and r <= R and s <= R
                // Inverted triangle: q >= -R and r >= -R and s >= -R
                boolean inUp = (q <= R && r <= R && s <= R);
                boolean inDown = (q >= -R && r >= -R && s >= -R);
                if (inUp || inDown)
                    {
                    points.add(new HexPoint(q, r));
                    }
                }
            }

        int n = points.size();
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName("Hexagram Lattice (Order " + order + ", " + n + " nodes)");

        // Add edges between adjacent points in triangular grid (q, r distance 1):
        // (dq, dr) in {(1, 0), (-1, 0), (0, 1), (0, -1), (1, -1), (-1, 1)}
        for (int i = 0; i < n; i++)
            {
            HexPoint p1 = points.get(i);
            for (int j = i + 1; j < n; j++)
                {
                HexPoint p2 = points.get(j);
                int dq = p1.q - p2.q;
                int dr = p1.r - p2.r;
                int ds = (-p1.q - p1.r) - (-p2.q - p2.r);
                if ((Math.abs(dq) <= 1 && Math.abs(dr) <= 1 && Math.abs(ds) <= 1)
                        && (dq != 0 || dr != 0))
                    {
                    net.setValue(1.0f, i, j);
                    net.setValue(1.0f, j, i);
                    }
                }
            }
        net.setSymmetry(true);
        applyHexagramLayout(net, points, 600, 600);
        return net;
        }

    private static void checkNodeCount(int n)
        {
        if (n < 2)
            {
            throw new IllegalArgumentException("Network size must be an integer greater than 1.");
            }
        if (n > MAX_SAFE_NODES)
            {
            throw new IllegalArgumentException("Network size (" + n
                    + ") exceeds maximum safe limit of " + MAX_SAFE_NODES + " nodes.");
            }
        }

    /**
     * Creates a Watts-Strogatz small-world network.
     * Starts with a ring lattice of n nodes where each node connects to k nearest neighbors (k/2 on each side).
     * With probability beta, each edge is rewired to a randomly chosen node without self-loops or duplicate edges.
     * Default layout: Circular layout displaying the perimeter ring and chord shortcuts.
     *
     * @see <a href="https://doi.org/10.1038/30918">Watts, D. J., &amp; Strogatz, S. H. (1998). Collective dynamics of 'small-world' networks. Nature, 393(6684), 440-442.</a>
     */
    public static Network createWattsStrogatz(int n, int k, double beta, long seed)
        {
        checkNodeCount(n);
        if (k < 2 || k >= n || k % 2 != 0)
            {
            throw new IllegalArgumentException("Nearest neighbor degree k must be an even integer with 2 <= k < n.");
            }
        beta = Math.max(0.0, Math.min(1.0, beta));
        Network net = new Network(n);
        net.setName("Watts-Strogatz Small-World (N=" + n + ", k=" + k + ", p=" + String.format(java.util.Locale.US, "%.2f", beta) + ")");
        Random rng = seed != 0 ? new Random(seed) : new Random();

        boolean[][] adj = new boolean[n][n];
        // 1. Regular ring lattice
        for (int i = 0; i < n; i++)
            {
            for (int j = 1; j <= k / 2; j++)
                {
                int target = (i + j) % n;
                adj[i][target] = true;
                adj[target][i] = true;
                }
            }

        // 2. Rewire edges
        for (int j = 1; j <= k / 2; j++)
            {
            for (int i = 0; i < n; i++)
                {
                int oldTarget = (i + j) % n;
                if (rng.nextDouble() < beta)
                    {
                    java.util.ArrayList<Integer> candidates = new java.util.ArrayList<>();
                    for (int w = 0; w < n; w++)
                        {
                        if (w != i && !adj[i][w])
                            {
                            candidates.add(w);
                            }
                        }
                    if (!candidates.isEmpty())
                        {
                        adj[i][oldTarget] = false;
                        adj[oldTarget][i] = false;
                        int newTarget = candidates.get(rng.nextInt(candidates.size()));
                        adj[i][newTarget] = true;
                        adj[newTarget][i] = true;
                        }
                    }
                }
            }

        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                if (adj[i][j])
                    {
                    net.setValue(1.0f, i, j);
                    }
                }
            }
        net.setSymmetry(true);
        int canvasSize = NodeArea.recommendedCanvasSize(n);
        NetworkLayouts.apply(net, NetworkLayouts.CIRCULAR, canvasSize, canvasSize);
        return net;
        }

    /**
     * Creates a Barabási-Albert scale-free network via preferential attachment.
     * Starts with an initial clique seed of m+1 nodes, then iteratively adds nodes each connecting to m
     * existing nodes chosen proportionally to their degree.
     * Default layout: Concentric layout with highest-degree hubs in center.
     *
     * @see <a href="https://doi.org/10.1126/science.286.5439.509">Barabási, A.-L., &amp; Albert, R. (1999). Emergence of scaling in random networks. Science, 286(5439), 509-512.</a>
     */
    public static Network createBarabasiAlbert(int n, int m, long seed)
        {
        checkNodeCount(n);
        if (m < 1 || m >= n)
            {
            throw new IllegalArgumentException("Attachment edges m must satisfy 1 <= m < n.");
            }
        Network net = new Network(n);
        net.setName("Barabási-Albert Scale-Free (N=" + n + ", m=" + m + ")");
        Random rng = seed != 0 ? new Random(seed) : new Random();

        int m0 = m + 1;
        java.util.ArrayList<Integer> repeatedNodes = new java.util.ArrayList<>();

        // Initial connected clique seed of m0 nodes
        for (int i = 0; i < m0; i++)
            {
            for (int j = i + 1; j < m0; j++)
                {
                net.setValue(1.0f, i, j);
                net.setValue(1.0f, j, i);
                repeatedNodes.add(i);
                repeatedNodes.add(j);
                }
            }

        // Preferential attachment growth
        for (int i = m0; i < n; i++)
            {
            java.util.HashSet<Integer> chosen = new java.util.HashSet<>();
            while (chosen.size() < m)
                {
                int pick = repeatedNodes.get(rng.nextInt(repeatedNodes.size()));
                if (pick != i)
                    {
                    chosen.add(pick);
                    }
                }
            for (int target : chosen)
                {
                net.setValue(1.0f, i, target);
                net.setValue(1.0f, target, i);
                repeatedNodes.add(i);
                repeatedNodes.add(target);
                }
            }
        net.setSymmetry(true);
        int canvasSize = NodeArea.recommendedCanvasSize(n);
        NetworkLayouts.apply(net, NetworkLayouts.CONCENTRIC, canvasSize, canvasSize);
        return net;
        }

    /**
     * Creates a Stochastic Block Model (SBM) network with clustered communities.
     * Vertices are partitioned into numBlocks communities with higher within-community density (pIn)
     * and lower cross-community density (pOut).
     * Default layout: Clustered community layout with visible group separation.
     *
     * @see <a href="https://doi.org/10.1016/0378-8733(83)90021-7">Holland, P. W., Laskey, K. B., &amp; Leinhardt, S. (1983). Stochastic blockmodels: First steps. Social Networks, 5(2), 109-137.</a>
     */
    public static Network createStochasticBlockModel(int n, int numBlocks, double pIn, double pOut, long seed)
        {
        checkNodeCount(n);
        if (numBlocks < 2 || numBlocks > n)
            {
            throw new IllegalArgumentException("Number of community blocks must satisfy 2 <= numBlocks <= n.");
            }
        pIn = Math.max(0.0, Math.min(1.0, pIn));
        pOut = Math.max(0.0, Math.min(1.0, pOut));
        Network net = new Network(n);
        net.setName("Stochastic Block Model (" + n + " nodes, " + numBlocks + " Communities)");
        Random rng = seed != 0 ? new Random(seed) : new Random();

        int[] community = new int[n];
        for (int i = 0; i < n; i++)
            {
            community[i] = (int) Math.min(numBlocks - 1, (long) i * numBlocks / n);
            }

        for (int i = 0; i < n; i++)
            {
            for (int j = i + 1; j < n; j++)
                {
                double p = (community[i] == community[j]) ? pIn : pOut;
                if (rng.nextDouble() < p)
                    {
                    net.setValue(1.0f, i, j);
                    net.setValue(1.0f, j, i);
                    }
                }
            }
        net.setSymmetry(true);
        int canvasSize = NodeArea.recommendedCanvasSize(n);
        applyClusteredCommunityLayout(net, numBlocks, canvasSize, canvasSize);
        return net;
        }

    /**
     * Creates an Erdős-Rényi G(n, p) random graph.
     * Each pair of vertices is connected with independent probability p.
     * Default layout: Circular layout.
     *
     * @see <a href="https://doi.org/10.1214/aoms/1177706098">Gilbert, E. N. (1959). Random graphs. The Annals of Mathematical Statistics, 30(4), 1141-1144.</a>
     * @see Erdős, P., &amp; Rényi, A. (1959). On random graphs. I. Publicationes Mathematicae (Debrecen), 6, 290-297.
     */
    public static Network createErdosRenyi(int n, double p, boolean symmetric, long seed)
        {
        checkNodeCount(n);
        p = Math.max(0.0, Math.min(1.0, p));
        Network net = new Network(n);
        net.setName("Erdős-Rényi G(n, p) (N=" + n + ", p=" + String.format(java.util.Locale.US, "%.2f", p) + ")");
        Random rng = seed != 0 ? new Random(seed) : new Random();

        for (int i = 0; i < n; i++)
            {
            int startJ = symmetric ? i + 1 : 0;
            for (int j = startJ; j < n; j++)
                {
                if (i != j && rng.nextDouble() < p)
                    {
                    net.setValue(1.0f, i, j);
                    if (symmetric)
                        {
                        net.setValue(1.0f, j, i);
                        }
                    }
                }
            }
        net.setSymmetry(symmetric);
        int canvasSize = NodeArea.recommendedCanvasSize(n);
        NetworkLayouts.apply(net, NetworkLayouts.CIRCULAR, canvasSize, canvasSize);
        return net;
        }

    /**
     * Creates a linear chain / pathway network.
     * Nodes 0 <-> 1 <-> 2 <-> ... <-> n-1.
     * Default layout: Linear pathway layout (horizontal or snake multi-row for large n).
     */
    public static Network createLinearChain(int n, boolean symmetric)
        {
        checkNodeCount(n);
        Network net = new Network(n);
        net.setName(symmetric ? "Linear Chain Network (" + n + " nodes)" : "Directed Path Network (" + n + " nodes)");
        for (int i = 0; i < n - 1; i++)
            {
            net.setValue(1.0f, i, i + 1);
            if (symmetric)
                {
                net.setValue(1.0f, i + 1, i);
                }
            }
        net.setSymmetry(symmetric);
        int canvasSize = NodeArea.recommendedCanvasSize(n);
        applyLinearChainLayout(net, canvasSize, canvasSize);
        return net;
        }

    /**
     * Creates a Hierarchical Tree / Corporate Hierarchy (Organigram) network.
     * Root node 0 sits at the top, branching down with branchingFactor children per node.
     * Optional lateral ties simulate informal cross-departmental peer connections.
     * Default layout: Hierarchical Tree Layout.
     *
     * @see Krackhardt, D. (1994). Graph theoretical dimensions of informal organizations. In Computational Organization Theory (pp. 89-111).
     * @see Simon, H. A. (1962). The architecture of complexity. Proceedings of the American Philosophical Society, 106(6), 467-482.
     */
    public static Network createHierarchicalTree(int n, int branchingFactor, boolean includeLateralTies, boolean directed)
        {
        checkNodeCount(n);
        if (branchingFactor < 2)
            {
            throw new IllegalArgumentException("Branching factor must be at least 2.");
            }
        Network net = new Network(n);
        net.setName("Hierarchical Tree / Organigram (" + n + " nodes, b=" + branchingFactor + ")");

        for (int i = 0; i < n; i++)
            {
            int firstChild = i * branchingFactor + 1;
            int lastChild = Math.min(n - 1, (i + 1) * branchingFactor);
            for (int c = firstChild; c <= lastChild; c++)
                {
                net.setValue(1.0f, i, c);
                if (!directed)
                    {
                    net.setValue(1.0f, c, i);
                    }
                }
            }

        if (includeLateralTies)
            {
            // Connect horizontal peers sharing same parent
            for (int i = 0; i < n; i++)
                {
                int firstChild = i * branchingFactor + 1;
                int lastChild = Math.min(n - 1, (i + 1) * branchingFactor);
                for (int c = firstChild; c < lastChild; c++)
                    {
                    net.setValue(1.0f, c, c + 1);
                    net.setValue(1.0f, c + 1, c);
                    }
                }
            }
        net.setSymmetry(!directed);
        int canvasSize = NodeArea.recommendedCanvasSize(n);
        NetworkLayouts.apply(net, NetworkLayouts.HIERARCHICAL, canvasSize, canvasSize);
        return net;
        }

    public static void applyLinearChainLayout(Network net, int width, int height)
        {
        int n = net.getSize();
        if (n == 0) return;
        for (int i = 0; i < n; i++) net.getActor(i).createCoordinatesIfMissing();
        if (n == 1)
            {
            net.getActor(0).setX(width / 2, width);
            net.getActor(0).setY(height / 2, height);
            return;
            }
        int margin = Math.max(40, width / 12);
        if (n <= 20)
            {
            int y = height / 2;
            for (int i = 0; i < n; i++)
                {
                int x = (int) Math.round(margin + (double) i * (width - 2 * margin) / (n - 1));
                net.getActor(i).setX(x, width);
                net.getActor(i).setY(y, height);
                }
            }
        else
            {
            int cols = Math.min(20, (int) Math.ceil(Math.sqrt(n * 2.5)));
            int rows = (int) Math.ceil((double) n / cols);
            int usableW = width - 2 * margin;
            int usableH = height - 2 * margin;
            for (int i = 0; i < n; i++)
                {
                int r = i / cols;
                int colInRow = (r % 2 == 0) ? (i % cols) : ((cols - 1) - (i % cols));
                int x = (cols > 1) ? (int) Math.round(margin + (double) colInRow * usableW / (cols - 1)) : width / 2;
                int y = (rows > 1) ? (int) Math.round(margin + (double) r * usableH / (rows - 1)) : height / 2;
                net.getActor(i).setX(x, width);
                net.getActor(i).setY(y, height);
                }
            }
        }

    public static void applyClusteredCommunityLayout(Network net, int numBlocks, int width, int height)
        {
        int n = net.getSize();
        if (n == 0) return;
        for (int i = 0; i < n; i++) net.getActor(i).createCoordinatesIfMissing();
        if (numBlocks < 2 || n <= numBlocks)
            {
            NetworkLayouts.apply(net, NetworkLayouts.CIRCULAR, width, height);
            return;
            }
        double cx = width / 2.0;
        double cy = height / 2.0;
        double mainRadius = Math.min(width, height) * 0.35;
        double clusterRadius = mainRadius * Math.sin(Math.PI / numBlocks) * 0.85;

        java.util.ArrayList<java.util.ArrayList<Integer>> communities = new java.util.ArrayList<>();
        for (int c = 0; c < numBlocks; c++) communities.add(new java.util.ArrayList<Integer>());
        for (int i = 0; i < n; i++)
            {
            int comm = (int) Math.min(numBlocks - 1, (long) i * numBlocks / n);
            communities.get(comm).add(i);
            }

        for (int c = 0; c < numBlocks; c++)
            {
            double angleC = 2 * Math.PI * c / numBlocks - Math.PI / 2.0;
            double commCenterX = cx + mainRadius * Math.cos(angleC);
            double commCenterY = cy + mainRadius * Math.sin(angleC);

            java.util.ArrayList<Integer> members = communities.get(c);
            int mCount = members.size();
            for (int k = 0; k < mCount; k++)
                {
                int node = members.get(k);
                double angleM = 2 * Math.PI * k / Math.max(1, mCount);
                double rLocal = (mCount > 1) ? clusterRadius * (0.35 + 0.65 * (k % 2)) : 0.0;
                int x = (int) Math.round(commCenterX + rLocal * Math.cos(angleM));
                int y = (int) Math.round(commCenterY + rLocal * Math.sin(angleM));
                net.getActor(node).setX(x, width);
                net.getActor(node).setY(y, height);
                }
            }
        }

    public static class Point2D
        {
        public final double x;
        public final double y;

        public Point2D(double x, double y)
            {
            this.x = x;
            this.y = y;
            }
        }

    private static void applyHexagramLayout(Network net, List<HexPoint> points, int width, int height)
        {
        int n = points.size();
        double cx = width / 2.0;
        double cy = height / 2.0;

        // Convert axial (q, r) to cartesian (x, y):
        // x = sqrt(3) * (q + r/2)
        // y = 3/2 * r
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        double[] rawX = new double[n];
        double[] rawY = new double[n];

        for (int i = 0; i < n; i++)
            {
            HexPoint p = points.get(i);
            rawX[i] = Math.sqrt(3.0) * (p.q + p.r / 2.0);
            rawY[i] = 1.5 * p.r;
            minX = Math.min(minX, rawX[i]);
            maxX = Math.max(maxX, rawX[i]);
            minY = Math.min(minY, rawY[i]);
            maxY = Math.max(maxY, rawY[i]);
            }

        double spanX = Math.max(1e-4, maxX - minX);
        double spanY = Math.max(1e-4, maxY - minY);
        double scale = Math.min((width - 100) / spanX, (height - 100) / spanY);

        for (int i = 0; i < n; i++)
            {
            int x = (int) Math.round(cx + (rawX[i] - (minX + spanX / 2.0)) * scale);
            int y = (int) Math.round(cy + (rawY[i] - (minY + spanY / 2.0)) * scale);
            net.getActor(i).createCoordinatesIfMissing();
            net.getActor(i).setX(x, width);
            net.getActor(i).setY(y, height);
            }
        }

    private static class HexPoint
        {
        final int q;
        final int r;

        HexPoint(int q, int r)
            {
            this.q = q;
            this.r = r;
            }
        }
    }
