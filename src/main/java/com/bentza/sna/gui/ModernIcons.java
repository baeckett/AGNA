package com.bentza.sna.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
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
 * Each glyph is a neutral outline taken from the current look-and-feel
 * foreground, with one accent element in the brand blue. Renders crisply
 * at any size and adapts to dark themes. The icon meanings mirror the
 * classic GIFs they replace: network, open, save, viewer, output, etc.
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

    private static final int KIND_COUNT = 11;

    private static Color outline()
        {
        Color c = UIManager.getColor("Button.foreground");
        return c != null ? c : new Color(72, 80, 92);
        }

    private static final Color ACCENT = new Color(37, 99, 235);

    public static ImageIcon get(int kind, int size)
        {
        if (kind < 0 || kind >= KIND_COUNT)
            {
            kind = NEW_NETWORK;
            }
        BufferedImage img = new BufferedImage(size, size,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        glyph(g, kind, size);
        g.dispose();
        return new ImageIcon(img);
        }

    private static void glyph(Graphics2D g, int kind, int s)
        {
        float m = s * 0.20f;
        float w = s - 2 * m;
        Color gray = outline();
        g.setColor(gray);
        g.setStroke(new BasicStroke(Math.max(2.2f, s / 14f),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (kind)
            {
            case NEW_NETWORK:
                // a small sociomatrix grid, like the classic icon
                int cg = Math.max(2, Math.round(s * 0.13f));
                int co = Math.round(s * 0.24f);
                int cs = cg + Math.round(s * 0.10f);
                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        {
                        g.setColor(gray);
                        g.drawRect(co + i * cs, co + j * cs, cg, cg);
                        if (i == 1 && j == 1)
                            {
                            g.setColor(ACCENT);
                            g.fillRect(co + i * cs, co + j * cs, cg, cg);
                            }
                        }
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
                g.draw(f);
                g.setColor(ACCENT);
                g.fill(f);
                break;
            case NEW_FROM_CHAIN:
                g.setColor(gray);
                g.draw(new Ellipse2D.Float(m, m + s * 0.10f, w * 0.55f,
                        w * 0.55f));
                g.draw(new Ellipse2D.Float(s - m - w * 0.55f, m + s * 0.10f,
                        w * 0.55f, w * 0.55f));
                g.drawLine((int) (m + w * 0.55f), (int) (m + s * 0.37f),
                        (int) (s - m - w * 0.55f), (int) (m + s * 0.37f));
                g.setColor(ACCENT);
                g.fill(new Ellipse2D.Float(s / 2 - s * 0.07f,
                        m + s * 0.30f, s * 0.14f, s * 0.14f));
                break;
            case SAVE_NETWORK:
                g.setColor(gray);
                g.drawRoundRect((int) m, (int) m, (int) w, (int) w, 6, 6);
                g.drawRect((int) (m + s * 0.11f), (int) (m + s * 0.09f),
                        (int) (w - s * 0.22f), (int) (w - s * 0.46f));
                g.drawRect((int) (m + s * 0.11f), (int) (s - m - s * 0.13f),
                        (int) (w - s * 0.22f), (int) (s * 0.11f));
                g.setColor(ACCENT);
                g.fillRect((int) (m + s * 0.11f), (int) (m + s * 0.09f),
                        (int) (w - s * 0.22f), (int) (w - s * 0.46f));
                break;
            case VIEWER:
                g.setColor(gray);
                g.drawRoundRect((int) m, (int) m, (int) w, (int) w, 6, 6);
                g.draw(new Ellipse2D.Float(s * 0.26f, s * 0.32f, s * 0.48f,
                        s * 0.24f));
                g.setColor(ACCENT);
                g.fill(new Ellipse2D.Float(s * 0.455f, s * 0.40f, s * 0.09f,
                        s * 0.09f));
                break;
            case OPEN_OUTPUT:
                // a document page with content lines, like the classic icon
                g.setColor(gray);
                g.drawRoundRect((int) (s * 0.20f), (int) (s * 0.16f),
                        (int) (s * 0.60f), (int) (s * 0.68f), 3, 3);
                g.drawLine((int) (s * 0.28f), (int) (s * 0.34f),
                        (int) (s * 0.72f), (int) (s * 0.34f));
                g.drawLine((int) (s * 0.28f), (int) (s * 0.46f),
                        (int) (s * 0.64f), (int) (s * 0.46f));
                g.drawLine((int) (s * 0.28f), (int) (s * 0.58f),
                        (int) (s * 0.72f), (int) (s * 0.58f));
                g.setColor(ACCENT);
                g.drawLine((int) (s * 0.20f), (int) (s * 0.16f),
                        (int) (s * 0.20f), (int) (s * 0.84f));
                break;
            case CLEAR_OUTPUT:
                // a document page struck through: clear the output
                g.setColor(gray);
                g.drawRoundRect((int) (s * 0.20f), (int) (s * 0.16f),
                        (int) (s * 0.60f), (int) (s * 0.68f), 3, 3);
                g.drawLine((int) (s * 0.28f), (int) (s * 0.34f),
                        (int) (s * 0.72f), (int) (s * 0.34f));
                g.drawLine((int) (s * 0.28f), (int) (s * 0.58f),
                        (int) (s * 0.72f), (int) (s * 0.58f));
                g.setColor(ACCENT);
                g.drawLine((int) (s * 0.35f), (int) (s * 0.30f),
                        (int) (s * 0.65f), (int) (s * 0.60f));
                g.drawLine((int) (s * 0.65f), (int) (s * 0.30f),
                        (int) (s * 0.35f), (int) (s * 0.60f));
                break;
            case SAVE_OUTPUT:
                // a document page above a floppy: save the document
                g.setColor(gray);
                g.drawRoundRect((int) (s * 0.26f), (int) (s * 0.14f),
                        (int) (s * 0.48f), (int) (s * 0.28f), 3, 3);
                g.drawLine((int) (s * 0.34f), (int) (s * 0.24f),
                        (int) (s * 0.66f), (int) (s * 0.24f));
                g.setColor(ACCENT);
                g.drawRoundRect((int) (s * 0.26f), (int) (s * 0.52f),
                        (int) (s * 0.48f), (int) (s * 0.34f), 3, 3);
                g.drawRect((int) (s * 0.34f), (int) (s * 0.52f),
                        (int) (s * 0.14f), (int) (s * 0.16f));
                break;
            case TRANSPOSE:
                // a clockwise rotation arrow: rotating the matrix idea
                // (mirrors the classic curved-arrow glyph)
                double cx = s * 0.50d;
                double cy = s * 0.52d;
                double r2 = s * 0.30d;
                g.setColor(gray);
                g.draw(new Arc2D.Float((float) (cx - r2), (float) (cy - r2),
                        (float) (2 * r2), (float) (2 * r2), 45f, 250f,
                        Arc2D.OPEN));
                g.fill(new Ellipse2D.Float((float) (cx - s * 0.045f),
                        (float) (cy - s * 0.045f), s * 0.09f, s * 0.09f));
                double end = Math.toRadians(45d + 250d);
                double tipX = cx + r2 * Math.cos(end);
                double tipY = cy + r2 * Math.sin(end);
                double a1 = Math.toRadians(45d + 250d - 22d);
                double a2 = Math.toRadians(45d + 250d + 22d);
                g.setColor(ACCENT);
                g.drawLine((int) Math.round(tipX), (int) Math.round(tipY),
                        (int) Math.round(cx + r2 * Math.cos(a1)),
                        (int) Math.round(cy + r2 * Math.sin(a1)));
                g.drawLine((int) Math.round(tipX), (int) Math.round(tipY),
                        (int) Math.round(cx + r2 * Math.cos(a2)),
                        (int) Math.round(cy + r2 * Math.sin(a2)));
                break;
            case SYMMETRIZE:
                // two arrows pushing toward a center mirror axis: making
                // the two sides equal (the symmetrization idea)
                float sy = s * 0.55f;
                g.setColor(gray);
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
                g.setColor(ACCENT);
                g.drawLine((int) (s * 0.86f), Math.round(sy),
                        (int) (s * 0.62f), Math.round(sy));
                g.drawLine((int) (s * 0.62f), Math.round(sy),
                        (int) (s * 0.70f), Math.round(sy - s * 0.11f));
                g.drawLine((int) (s * 0.62f), Math.round(sy),
                        (int) (s * 0.70f), Math.round(sy + s * 0.11f));
                break;
            case RENUMBER:
                // literal "123" digits, like the classic icon
                g.setColor(gray);
                g.setFont(new java.awt.Font(java.awt.Font.DIALOG,
                        java.awt.Font.BOLD, Math.round(s * 0.52f)));
                String digits = "123";
                int tw = g.getFontMetrics().stringWidth(digits);
                g.drawString(digits,
                        Math.round((s - tw) / 2f), Math.round(s * 0.70f));
                g.setColor(ACCENT);
                g.drawLine((int) (s * 0.24f), (int) (s * 0.76f),
                        (int) (s * 0.76f), (int) (s * 0.76f));
                break;
            default:
                break;
            }
        }

    
    }