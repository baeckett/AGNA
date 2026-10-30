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
