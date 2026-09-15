/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 * 2.1.3: modern duotone toolbar icons, drawn in code (no image assets).
 * Rest version: a neutral outline taken from the look-and-feel foreground
 * with one brand-blue accent element. Rollover version: the whole glyph in
 * the brand blue (clearly more colorful on hover). The icon meanings
 * mirror the classic GIFs they replace - matrices, rotation, mirroring,
 * documents, "123", nodes, layouts, etc. Renders crisply at any size and
 * adapts to dark themes.
 */
public class ModernIcons
    {
    public static final int NEW_NETWORK = 0;
    public static final int OPEN_NETWORK = 1;
    public static final int NEW_FROM_CHAIN = 2;
    public static final int SAVE_NETWORK = 3;
    public static final int VIEWER = 4;
    public static final int OPEN_OUTPUT = 5;
    public static final int CLEAR_OUTPUT = 6;
    public static final int SAVE_OUTPUT = 7;
    public static final int TRANSPOSE = 8;
    public static final int SYMMETRIZE = 9;
    public static final int RENUMBER = 10;
    // Network Viewer toolbar:
    public static final int ADD_NODE = 11;
    public static final int DELETE_NODE = 12;
    public static final int VIEW_NAMES = 13;
    public static final int NAMES_COLOR = 14;
    public static final int LOYALTY = 15;
    public static final int IMAGE_WIDTH = 16;
    public static final int CIRCULAR_LAYOUT = 17;
    public static final int RANDOM_LAYOUT = 18;
    public static final int SELECT_NEXT = 19;
    public static final int EXPORT_IMAGE = 20;
    public static final int INSERT_IN_OUTPUT = 21;
    public static final int ALLOW_EDGE_SELECTION = 22;
    public static final int SHOW_CONNECTION_VALUE = 23;
    public static final int EDGE_COLOR = 24;
    // spin-arrow buttons (coordinate/value panels)
    public static final int UP_ARROW = 25;
    public static final int DOWN_ARROW = 26;
    // node-search panel (Network Viewer search area)
    public static final int SEARCH = 27;
    public static final int STOP = 28;
    public static final int MATCH = 29;
    public static final int CLEAR_AREA = 30;
    public static final int HELP = 31;
    public static final int SEPARATION = 32;
    public static final int X_COORD = 33;
    public static final int Y_COORD = 34;
    public static final int EDGE_VALUE = 35;
    public static final int GRID_LAYOUT = 36;
    public static final int CONCENTRIC_LAYOUT = 37;
    public static final int SPRING_LAYOUT = 38;

    public static final int KIND_COUNT = 39;

    public static Color accent()
        {
        return ACCENT;
        }

    // solid muted gray for text controls (labels, toggles): the outline
    // blended toward the surface, matching the faded icon rest state
    public static Color restText()
        {
        Color fg = outline();
        float a = 0.34f;
        int r = Math.round(fg.getRed() * a + 245 * (1 - a));
        int g = Math.round(fg.getGreen() * a + 245 * (1 - a));
        int b = Math.round(fg.getBlue() * a + 247 * (1 - a));
        return new Color(r, g, b);
        }

    // 2.1.3: vivid saturated blue (was a muted medium blue)
    private static final Color ACCENT = new Color(0, 102, 255);
    private static final Color ACCENT_DEEP = new Color(10, 60, 210);

    private static Color outline()
        {
        Color c = UIManager.getColor("Button.foreground");
        return c != null ? c : new Color(72, 80, 92);
        }

    // 2.1.3: the rest state uses the outline at reduced opacity so the
    // blue rollover version stands out clearly (a truly faded gray)
    private static Color fade(Color c)
        {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 80);
        }

    public static ImageIcon get(int kind, int size)
        {
        return get(kind, size, false);
        }

    public static ImageIcon get(int kind, int size, boolean rollover)
        {
        if (kind < 0 || kind >= KIND_COUNT)
            {
            kind = NEW_NETWORK;
            }
        // rest state: both hues faded, so hover (full blue) contrasts hard
        Color line = rollover ? ACCENT : fade(outline());
        Color accent = rollover ? ACCENT_DEEP : fade(ACCENT);
        BufferedImage img = new BufferedImage(size, size,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        glyph(g, kind, size, line, accent, rollover);
        g.dispose();
        return new ImageIcon(img);
        }

    private static void glyph(Graphics2D g, int kind, int s,
            Color line, Color accent, boolean rollover)
        {
        float m = s * 0.20f;
        float w = s - 2 * m;
        g.setColor(line);
        g.setStroke(new BasicStroke(Math.max(2.2f, s / 14f),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (kind)
            {
            case NEW_NETWORK:
                // airy diagonal of three squares (a matrix) with a small
                // plus: "create a new network" - much less clutter than a
                // full 3x3 grid at toolbar size
                int dq = Math.max(3, Math.round(s * 0.17f));
                g.setColor(line);
                g.fillRoundRect(Math.round(s * 0.26f), Math.round(s * 0.22f),
                        dq, dq, 3, 3);
                g.fillRoundRect(Math.round(s * 0.44f), Math.round(s * 0.40f),
                        dq, dq, 3, 3);
                g.fillRoundRect(Math.round(s * 0.62f), Math.round(s * 0.58f),
                        dq, dq, 3, 3);
                g.setColor(accent);
                g.drawLine((int) (s * 0.74f), (int) (s * 0.15f),
                        (int) (s * 0.74f), (int) (s * 0.31f));
                g.drawLine((int) (s * 0.66f), (int) (s * 0.23f),
                        (int) (s * 0.82f), (int) (s * 0.23f));
                break;
            case OPEN_NETWORK:
                Path2D f = new Path2D.Float();
                f.moveTo(m, m + s * 0.24f);
                f.lineTo(m + s * 0.26f, m + s * 0.24f);
                f.lineTo(m + s * 0.32f, m + s * 0.12f);
                f.lineTo(m + s * 0.56f, m + s * 0.12f);
                f.lineTo(m + s * 0.56f, m + s * 0.24f);
                f.lineTo(s - m, m + s * 0.30f);
                f.lineTo(s - m - s * 0.11f, s - m);
                f.lineTo(m + s * 0.09f, s - m);
                f.closePath();
                g.setColor(line);
                g.draw(f);
                g.setColor(accent);
                g.fill(f);
                break;
            case NEW_FROM_CHAIN:
                g.setColor(line);
                g.draw(new Ellipse2D.Float(m, m + s * 0.10f, w * 0.55f,
                        w * 0.55f));
                g.draw(new Ellipse2D.Float(s - m - w * 0.55f, m + s * 0.10f,
                        w * 0.55f, w * 0.55f));
                g.drawLine((int) (m + w * 0.55f), (int) (m + s * 0.37f),
                        (int) (s - m - w * 0.55f), (int) (m + s * 0.37f));
                g.setColor(accent);
                g.fill(new Ellipse2D.Float(s / 2 - s * 0.07f,
                        m + s * 0.30f, s * 0.14f, s * 0.14f));
                break;
            case SAVE_NETWORK:
                g.setColor(line);
                g.drawRoundRect((int) m, (int) m, (int) w, (int) w, 6, 6);
                g.drawRect((int) (m + s * 0.11f), (int) (m + s * 0.09f),
                        (int) (w - s * 0.22f), (int) (w - s * 0.46f));
                g.drawRect((int) (m + s * 0.11f), (int) (s - m - s * 0.13f),
                        (int) (w - s * 0.22f), (int) (s * 0.11f));
                g.setColor(accent);
                g.fillRect((int) (m + s * 0.11f), (int) (m + s * 0.09f),
                        (int) (w - s * 0.22f), (int) (w - s * 0.46f));
                break;
            case VIEWER:
                g.setColor(line);
                g.drawRoundRect((int) m, (int) m, (int) w, (int) w, 6, 6);
                g.draw(new Ellipse2D.Float(s * 0.26f, s * 0.32f, s * 0.48f,
                        s * 0.24f));
                g.setColor(accent);
                g.fill(new Ellipse2D.Float(s * 0.43f, s * 0.385f,
                        s * 0.14f, s * 0.14f));
                break;
            case OPEN_OUTPUT:
                g.setColor(line);
                g.drawRoundRect((int) (s * 0.20f), (int) (s * 0.16f),
                        (int) (s * 0.60f), (int) (s * 0.68f), 3, 3);
                g.drawLine((int) (s * 0.28f), (int) (s * 0.34f),
                        (int) (s * 0.72f), (int) (s * 0.34f));
                g.drawLine((int) (s * 0.28f), (int) (s * 0.46f),
                        (int) (s * 0.64f), (int) (s * 0.46f));
                g.drawLine((int) (s * 0.28f), (int) (s * 0.58f),
                        (int) (s * 0.72f), (int) (s * 0.58f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.20f), (int) (s * 0.16f),
                        (int) (s * 0.20f), (int) (s * 0.84f));
                break;
            case CLEAR_OUTPUT:
                g.setColor(line);
                g.drawRoundRect((int) (s * 0.20f), (int) (s * 0.16f),
                        (int) (s * 0.60f), (int) (s * 0.68f), 3, 3);
                g.drawLine((int) (s * 0.28f), (int) (s * 0.34f),
                        (int) (s * 0.72f), (int) (s * 0.34f));
                g.drawLine((int) (s * 0.28f), (int) (s * 0.58f),
                        (int) (s * 0.72f), (int) (s * 0.58f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.35f), (int) (s * 0.30f),
                        (int) (s * 0.65f), (int) (s * 0.60f));
                g.drawLine((int) (s * 0.65f), (int) (s * 0.30f),
                        (int) (s * 0.35f), (int) (s * 0.60f));
                break;
            case SAVE_OUTPUT:
                g.setColor(line);
                g.drawRoundRect((int) (s * 0.26f), (int) (s * 0.14f),
                        (int) (s * 0.48f), (int) (s * 0.28f), 3, 3);
                g.drawLine((int) (s * 0.34f), (int) (s * 0.24f),
                        (int) (s * 0.66f), (int) (s * 0.24f));
                g.setColor(accent);
                g.drawRoundRect((int) (s * 0.26f), (int) (s * 0.52f),
                        (int) (s * 0.48f), (int) (s * 0.34f), 3, 3);
                g.drawRect((int) (s * 0.34f), (int) (s * 0.52f),
                        (int) (s * 0.14f), (int) (s * 0.16f));
                break;
            case TRANSPOSE:
                double cx = s * 0.50d;
                double cy = s * 0.52d;
                double r2 = s * 0.30d;
                g.setColor(line);
                g.draw(new Arc2D.Float((float) (cx - r2), (float) (cy - r2),
                        (float) (2 * r2), (float) (2 * r2), 45f, 250f,
                        Arc2D.OPEN));
                g.fill(new Ellipse2D.Float((float) (cx - s * 0.06f),
                        (float) (cy - s * 0.06f), s * 0.12f, s * 0.12f));
                double end = Math.toRadians(45d + 250d);
                double tipX = cx + r2 * Math.cos(end);
                double tipY = cy + r2 * Math.sin(end);
                double a1 = Math.toRadians(45d + 250d - 22d);
                double a2 = Math.toRadians(45d + 250d + 22d);
                g.setColor(accent);
                g.drawLine((int) Math.round(tipX), (int) Math.round(tipY),
                        (int) Math.round(cx + r2 * Math.cos(a1)),
                        (int) Math.round(cy + r2 * Math.sin(a1)));
                g.drawLine((int) Math.round(tipX), (int) Math.round(tipY),
                        (int) Math.round(cx + r2 * Math.cos(a2)),
                        (int) Math.round(cy + r2 * Math.sin(a2)));
                break;
            case SYMMETRIZE:
                float sy = s * 0.55f;
                g.setColor(line);
                g.drawLine((int) (s * 0.14f), Math.round(sy),
                        (int) (s * 0.38f), Math.round(sy));
                g.drawLine((int) (s * 0.38f), Math.round(sy),
                        (int) (s * 0.30f), Math.round(sy - s * 0.11f));
                g.drawLine((int) (s * 0.38f), Math.round(sy),
                        (int) (s * 0.30f), Math.round(sy + s * 0.11f));
                g.drawLine((int) (s * 0.50f), (int) (s * 0.24f),
                        (int) (s * 0.50f), (int) (s * 0.40f));
                g.drawLine((int) (s * 0.50f), (int) (s * 0.68f),
                        (int) (s * 0.50f), (int) (s * 0.84f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.86f), Math.round(sy),
                        (int) (s * 0.62f), Math.round(sy));
                g.drawLine((int) (s * 0.62f), Math.round(sy),
                        (int) (s * 0.70f), Math.round(sy - s * 0.11f));
                g.drawLine((int) (s * 0.62f), Math.round(sy),
                        (int) (s * 0.70f), Math.round(sy + s * 0.11f));
                break;
            case RENUMBER:
                g.setColor(line);
                g.setFont(new Font(Font.DIALOG, Font.BOLD,
                        Math.round(s * 0.52f)));
                String digits = "123";
                int tw = g.getFontMetrics().stringWidth(digits);
                g.drawString(digits, Math.round((s - tw) / 2f),
                        Math.round(s * 0.70f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.24f), (int) (s * 0.76f),
                        (int) (s * 0.76f), (int) (s * 0.76f));
                break;
            case ADD_NODE:
                g.setColor(line);
                g.draw(new Ellipse2D.Float(s * 0.16f, s * 0.36f, s * 0.30f,
                        s * 0.30f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.66f), (int) (s * 0.24f),
                        (int) (s * 0.66f), (int) (s * 0.40f));
                g.drawLine((int) (s * 0.58f), (int) (s * 0.32f),
                        (int) (s * 0.74f), (int) (s * 0.32f));
                break;
            case DELETE_NODE:
                g.setColor(line);
                g.draw(new Ellipse2D.Float(s * 0.16f, s * 0.36f, s * 0.30f,
                        s * 0.30f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.58f), (int) (s * 0.24f),
                        (int) (s * 0.74f), (int) (s * 0.40f));
                g.drawLine((int) (s * 0.74f), (int) (s * 0.24f),
                        (int) (s * 0.58f), (int) (s * 0.40f));
                break;
            case VIEW_NAMES:
                g.setColor(line);
                g.setFont(new Font(Font.DIALOG, Font.BOLD,
                        Math.round(s * 0.46f)));
                String aa = "Aa";
                int aw = g.getFontMetrics().stringWidth(aa);
                g.drawString(aa, Math.round((s - aw) / 2f),
                        Math.round(s * 0.64f));
                g.setColor(accent);
                int nd = Math.max(3, Math.round(s * 0.14f));
                g.fillRect(Math.round(s * 0.60f), Math.round(s * 0.24f),
                        nd, nd);
                break;
            case NAMES_COLOR:
                g.setColor(line);
                g.setFont(new Font(Font.DIALOG, Font.BOLD,
                        Math.round(s * 0.46f)));
                String ab = "Aa";
                int bw = g.getFontMetrics().stringWidth(ab);
                g.drawString(ab, Math.round((s - bw) / 2f),
                        Math.round(s * 0.66f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.24f), (int) (s * 0.76f),
                        (int) (s * 0.76f), (int) (s * 0.76f));
                break;
            case LOYALTY:
                Path2D heart = new Path2D.Float();
                heart.moveTo(s * 0.50f, s * 0.70f);
                heart.curveTo(s * 0.18f, s * 0.46f, s * 0.28f, s * 0.18f,
                        s * 0.50f, s * 0.36f);
                heart.curveTo(s * 0.72f, s * 0.18f, s * 0.82f, s * 0.46f,
                        s * 0.50f, s * 0.70f);
                g.setColor(line);
                g.draw(heart);
                g.setColor(accent);
                g.fill(heart);
                break;
            case IMAGE_WIDTH:
                g.setColor(line);
                g.drawLine((int) (s * 0.16f), (int) (s * 0.30f),
                        (int) (s * 0.16f), (int) (s * 0.70f));
                g.drawLine((int) (s * 0.84f), (int) (s * 0.30f),
                        (int) (s * 0.84f), (int) (s * 0.70f));
                g.drawLine((int) (s * 0.30f), (int) (s * 0.50f),
                        (int) (s * 0.70f), (int) (s * 0.50f));
                g.drawLine((int) (s * 0.58f), (int) (s * 0.42f),
                        (int) (s * 0.70f), (int) (s * 0.50f));
                g.drawLine((int) (s * 0.70f), (int) (s * 0.50f),
                        (int) (s * 0.58f), (int) (s * 0.58f));
                g.setColor(accent);
                int iw = Math.max(3, Math.round(s * 0.17f));
                g.fillRect(Math.round(s * 0.415f), Math.round(s * 0.415f),
                        iw, iw);
                break;
            case CIRCULAR_LAYOUT:
                g.setColor(line);
                g.draw(new Ellipse2D.Float(s * 0.22f, s * 0.22f, s * 0.56f,
                        s * 0.56f));
                float[][] circDots = { { 0.50f, 0.20f }, { 0.80f, 0.50f },
                        { 0.50f, 0.80f }, { 0.20f, 0.50f } };
                for (int i = 0; i < 4; i++)
                    {
                    g.setColor(i == 0 ? accent : line);
                    int cd = Math.max(3, Math.round(s * 0.14f));
                    g.fillRect(Math.round(circDots[i][0] * s - s * 0.07f),
                            Math.round(circDots[i][1] * s - s * 0.07f), cd,
                            cd);
                    }
                break;
            case RANDOM_LAYOUT:
                float[][] dots = { { 0.30f, 0.30f }, { 0.72f, 0.28f },
                        { 0.56f, 0.68f }, { 0.30f, 0.68f }, { 0.74f, 0.70f } };
                for (int i = 0; i < 5; i++)
                    {
                    g.setColor(i == 1 ? accent : line);
                    g.fill(new Ellipse2D.Float(
                            dots[i][0] * s - s * 0.08f,
                            dots[i][1] * s - s * 0.08f, s * 0.16f,
                            s * 0.16f));
                    }
                break;
            case GRID_LAYOUT:
                g.setColor(line);
                float gw = s * 0.58f;
                float gx = s * 0.21f;
                float gy = s * 0.21f;
                for (int r = 1; r < 4; r++)
                    {
                    g.drawLine(Math.round(gx + gw * r / 4),
                            Math.round(gy), Math.round(gx + gw * r / 4),
                            Math.round(gy + gw));
                    g.drawLine(Math.round(gx),
                            Math.round(gy + gw * r / 4),
                            Math.round(gx + gw),
                            Math.round(gy + gw * r / 4));
                    }
                g.setColor(accent);
                int gd = Math.max(3, Math.round(s * 0.12f));
                g.fillRect(Math.round(gx + gw / 2 - gd / 2),
                        Math.round(gy + gw / 2 - gd / 2), gd, gd);
                break;
            case CONCENTRIC_LAYOUT:
                g.setColor(line);
                float[][] ringR = { { 0.50f, 0.50f, 0.10f },
                        { 0.50f, 0.50f, 0.26f }, { 0.50f, 0.50f, 0.42f } };
                for (int i = 0; i < 3; i++)
                    {
                    g.draw(new Ellipse2D.Float(
                            ringR[i][0] * s - ringR[i][2] * s,
                            ringR[i][1] * s - ringR[i][2] * s,
                            ringR[i][2] * s * 2, ringR[i][2] * s * 2));
                    }
                g.setColor(accent);
                float cd = s * 0.16f;
                g.fill(new Ellipse2D.Float(s * 0.50f - cd / 2,
                        s * 0.50f - cd / 2, cd, cd));
                break;
            case SPRING_LAYOUT:
                g.setColor(line);
                float[][] sNodes = { { 0.26f, 0.30f }, { 0.74f, 0.28f },
                        { 0.50f, 0.78f } };
                for (int e = 0; e < 3; e++)
                    {
                    int n0 = e;
                    int n1 = (e + 1) % 3;
                    g.drawLine(Math.round(sNodes[n0][0] * s),
                            Math.round(sNodes[n0][1] * s),
                            Math.round(sNodes[n1][0] * s),
                            Math.round(sNodes[n1][1] * s));
                    }
                for (int i = 0; i < 3; i++)
                    {
                    g.setColor(i == 2 ? accent : line);
                    float snd = s * 0.20f;
                    g.fill(new Ellipse2D.Float(sNodes[i][0] * s - snd / 2,
                            sNodes[i][1] * s - snd / 2, snd, snd));
                    }
                break;
            case SELECT_NEXT:
                g.setColor(line);
                g.draw(new Ellipse2D.Float(s * 0.16f, s * 0.42f, s * 0.20f,
                        s * 0.20f));
                g.drawLine((int) (s * 0.42f), (int) (s * 0.52f),
                        (int) (s * 0.70f), (int) (s * 0.52f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.70f), (int) (s * 0.52f),
                        (int) (s * 0.60f), (int) (s * 0.44f));
                g.drawLine((int) (s * 0.70f), (int) (s * 0.52f),
                        (int) (s * 0.60f), (int) (s * 0.60f));
                break;
            case EXPORT_IMAGE:
                g.setColor(line);
                g.drawRoundRect((int) (s * 0.22f), (int) (s * 0.18f),
                        (int) (s * 0.56f), (int) (s * 0.40f), 3, 3);
                g.setColor(accent);
                int sd = Math.max(3, Math.round(s * 0.16f));
                g.fillRect(Math.round(s * 0.56f), Math.round(s * 0.20f),
                        sd, sd);
                g.setColor(line);
                g.drawLine((int) (s * 0.50f), (int) (s * 0.62f),
                        (int) (s * 0.50f), (int) (s * 0.72f));
                g.drawLine((int) (s * 0.42f), (int) (s * 0.66f),
                        (int) (s * 0.50f), (int) (s * 0.72f));
                g.drawLine((int) (s * 0.58f), (int) (s * 0.66f),
                        (int) (s * 0.50f), (int) (s * 0.72f));
                g.drawLine((int) (s * 0.24f), (int) (s * 0.76f),
                        (int) (s * 0.76f), (int) (s * 0.76f));
                break;
            case INSERT_IN_OUTPUT:
                g.setColor(line);
                g.drawRoundRect((int) (s * 0.26f), (int) (s * 0.14f),
                        (int) (s * 0.48f), (int) (s * 0.34f), 3, 3);
                g.drawLine((int) (s * 0.30f), (int) (s * 0.52f),
                        (int) (s * 0.70f), (int) (s * 0.52f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.50f), (int) (s * 0.56f),
                        (int) (s * 0.50f), (int) (s * 0.70f));
                g.drawLine((int) (s * 0.42f), (int) (s * 0.64f),
                        (int) (s * 0.50f), (int) (s * 0.70f));
                g.drawLine((int) (s * 0.58f), (int) (s * 0.64f),
                        (int) (s * 0.50f), (int) (s * 0.70f));
                break;
            case ALLOW_EDGE_SELECTION:
                Path2D cursor = new Path2D.Float();
                cursor.moveTo(s * 0.32f, s * 0.22f);
                cursor.lineTo(s * 0.36f, s * 0.62f);
                cursor.lineTo(s * 0.44f, s * 0.56f);
                cursor.lineTo(s * 0.50f, s * 0.68f);
                cursor.lineTo(s * 0.58f, s * 0.64f);
                cursor.lineTo(s * 0.52f, s * 0.52f);
                cursor.lineTo(s * 0.62f, s * 0.50f);
                cursor.closePath();
                g.setColor(line);
                g.draw(cursor);
                g.setColor(accent);
                g.fill(cursor);
                break;
            case SHOW_CONNECTION_VALUE:
                g.setColor(line);
                g.draw(new Ellipse2D.Float(s * 0.14f, s * 0.44f, s * 0.14f,
                        s * 0.14f));
                g.draw(new Ellipse2D.Float(s * 0.72f, s * 0.44f, s * 0.14f,
                        s * 0.14f));
                g.drawLine((int) (s * 0.28f), (int) (s * 0.51f),
                        (int) (s * 0.72f), (int) (s * 0.51f));
                g.setColor(accent);
                int vm = Math.max(2, Math.round(s * 0.13f));
                int vh = Math.max(3, Math.round(s * 0.16f));
                g.fillRect(Math.round(s * 0.44f), Math.round(s * 0.42f),
                        vm, vh);
                break;
            case EDGE_COLOR:
                g.setColor(line);
                g.drawLine((int) (s * 0.16f), (int) (s * 0.50f),
                        (int) (s * 0.78f), (int) (s * 0.50f));
                g.setColor(accent);
                g.fill(new Ellipse2D.Float(s * 0.60f, s * 0.40f, s * 0.20f,
                        s * 0.20f));
                break;
            case MATCH:
                // "equals" match: neutral bars + blue dot (exact match)
                g.setColor(line);
                g.drawLine((int) (s * 0.26f), (int) (s * 0.42f),
                        (int) (s * 0.70f), (int) (s * 0.42f));
                g.drawLine((int) (s * 0.26f), (int) (s * 0.62f),
                        (int) (s * 0.70f), (int) (s * 0.62f));
                g.setColor(accent);
                int md = Math.max(3, Math.round(s * 0.16f));
                g.fillRect(Math.round(s * 0.70f), Math.round(s * 0.44f),
                        md, md);
                break;
            case CLEAR_AREA:
                // circled cross: clear the results area
                g.setColor(line);
                g.draw(new Ellipse2D.Float(s * 0.22f, s * 0.22f, s * 0.56f,
                        s * 0.56f));
                g.setColor(accent);
                g.drawLine((int) (s * 0.34f), (int) (s * 0.34f),
                        (int) (s * 0.66f), (int) (s * 0.66f));
                g.drawLine((int) (s * 0.66f), (int) (s * 0.34f),
                        (int) (s * 0.34f), (int) (s * 0.66f));
                break;
            case HELP:
                // circled question mark: the universal help metaphor; the
                // question mark stays readable in the faded state
                g.setColor(line);
                g.draw(new Ellipse2D.Float(s * 0.16f, s * 0.16f, s * 0.68f,
                        s * 0.68f));
                g.setColor(rollover ? accent : new Color(ACCENT.getRed(),
                        ACCENT.getGreen(), ACCENT.getBlue(), 175));
                g.setFont(new Font(Font.DIALOG, Font.BOLD,
                        Math.round(s * 0.52f)));
                String q = "?";
                int qw = g.getFontMetrics().stringWidth(q);
                g.drawString(q, Math.round((s - qw) / 2f),
                        Math.round(s * 0.66f));
                break;
            case SEPARATION:
                // two parallel yet opposite arrows with a small gap:
                // the distance separating two symmetrical edges
                g.setColor(line);
                g.drawLine((int) (s * 0.20f), (int) (s * 0.36f),
                        (int) (s * 0.68f), (int) (s * 0.36f));
                g.drawLine((int) (s * 0.68f), (int) (s * 0.36f),
                        (int) (s * 0.58f), (int) (s * 0.26f));
                g.drawLine((int) (s * 0.68f), (int) (s * 0.36f),
                        (int) (s * 0.58f), (int) (s * 0.46f));
                g.drawLine((int) (s * 0.80f), (int) (s * 0.64f),
                        (int) (s * 0.32f), (int) (s * 0.64f));
                g.drawLine((int) (s * 0.32f), (int) (s * 0.64f),
                        (int) (s * 0.42f), (int) (s * 0.54f));
                g.drawLine((int) (s * 0.32f), (int) (s * 0.64f),
                        (int) (s * 0.42f), (int) (s * 0.74f));
                g.setColor(accent);
                int spd = Math.max(3, Math.round(s * 0.16f));
                g.fillRect(Math.round(s * 0.42f), Math.round(s * 0.43f),
                        spd, spd);
                break;
            case X_COORD:
                // x-axis arrow: the x coordinate caption
                g.setColor(line);
                g.drawLine((int) (s * 0.16f), (int) (s * 0.62f),
                        (int) (s * 0.78f), (int) (s * 0.62f));
                g.drawLine((int) (s * 0.78f), (int) (s * 0.62f),
                        (int) (s * 0.68f), (int) (s * 0.52f));
                g.drawLine((int) (s * 0.78f), (int) (s * 0.62f),
                        (int) (s * 0.68f), (int) (s * 0.72f));
                g.drawLine((int) (s * 0.16f), (int) (s * 0.50f),
                        (int) (s * 0.16f), (int) (s * 0.74f));
                g.setColor(accent);
                int xd = Math.max(3, Math.round(s * 0.16f));
                g.fillRect(Math.round(s * 0.10f), Math.round(s * 0.36f),
                        xd, xd);
                break;
            case Y_COORD:
                // y-axis arrow: the y coordinate caption
                g.setColor(line);
                g.drawLine((int) (s * 0.44f), (int) (s * 0.72f),
                        (int) (s * 0.44f), (int) (s * 0.20f));
                g.drawLine((int) (s * 0.44f), (int) (s * 0.20f),
                        (int) (s * 0.34f), (int) (s * 0.30f));
                g.drawLine((int) (s * 0.44f), (int) (s * 0.20f),
                        (int) (s * 0.54f), (int) (s * 0.30f));
                g.drawLine((int) (s * 0.30f), (int) (s * 0.72f),
                        (int) (s * 0.58f), (int) (s * 0.72f));
                g.setColor(accent);
                int yd = Math.max(3, Math.round(s * 0.16f));
                g.fillRect(Math.round(s * 0.37f), Math.round(s * 0.72f),
                        yd, yd);
                break;
            case EDGE_VALUE:
                // two nodes joined by a tie with the digit "1" above it:
                // an edge carrying a value (distinct from the "show
                // connection value" toggle next to it)
                g.setColor(line);
                g.fill(new Ellipse2D.Float(s * 0.14f, s * 0.48f, s * 0.20f,
                        s * 0.20f));
                g.fill(new Ellipse2D.Float(s * 0.66f, s * 0.48f, s * 0.20f,
                        s * 0.20f));
                g.drawLine((int) (s * 0.34f), (int) (s * 0.58f),
                        (int) (s * 0.66f), (int) (s * 0.58f));
                // the digit stays readable in the faded state: a stronger
                // alpha at rest, the full blue on hover
                g.setColor(rollover ? accent : new Color(ACCENT.getRed(),
                        ACCENT.getGreen(), ACCENT.getBlue(), 175));
                g.setFont(new Font(Font.DIALOG, Font.BOLD,
                        Math.round(s * 0.34f)));
                String one = "1";
                java.awt.FontMetrics fm = g.getFontMetrics();
                int ow = fm.stringWidth(one);
                g.drawString(one, Math.round((s - ow) / 2f),
                        Math.round(s * 0.42f));
                break;
            case SEARCH:
                // magnifying glass: neutral lens + handle, blue focus dot
                g.setColor(line);
                g.draw(new Ellipse2D.Float(s * 0.18f, s * 0.18f, s * 0.46f,
                        s * 0.46f));
                g.drawLine((int) (s * 0.56f), (int) (s * 0.56f),
                        (int) (s * 0.80f), (int) (s * 0.80f));
                g.setColor(accent);
                g.fill(new Ellipse2D.Float(s * 0.38f, s * 0.38f, s * 0.13f,
                        s * 0.13f));
                break;
            case STOP:
                // stop sign: neutral frame + blue core (search-in-progress)
                g.setColor(line);
                g.drawRoundRect((int) (s * 0.22f), (int) (s * 0.22f),
                        (int) (s * 0.56f), (int) (s * 0.56f), 4, 4);
                g.setColor(accent);
                g.fillRoundRect((int) (s * 0.36f), (int) (s * 0.36f),
                        (int) (s * 0.28f), (int) (s * 0.28f), 3, 3);
                break;
            case UP_ARROW:
            case DOWN_ARROW:
                // monochrome filled spin arrows (faded at rest, blue on
                // rollover - exactly the two-state convention)
                Path2D arrow = new Path2D.Float();
                if (kind == UP_ARROW)
                    {
                    arrow.moveTo(s * 0.5f, s * 0.22f);
                    arrow.lineTo(s * 0.80f, s * 0.74f);
                    arrow.lineTo(s * 0.20f, s * 0.74f);
                    } else
                    {
                    arrow.moveTo(s * 0.5f, s * 0.78f);
                    arrow.lineTo(s * 0.80f, s * 0.26f);
                    arrow.lineTo(s * 0.20f, s * 0.26f);
                    }
                arrow.closePath();
                g.setColor(line);
                g.fill(arrow);
                break;
            default:
                break;
            }
        }
    }