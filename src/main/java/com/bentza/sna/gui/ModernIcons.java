package com.bentza.sna.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
                float r = s * 0.13f;
                Ellipse2D n0 = new Ellipse2D.Float(m + s * 0.03f, m + s * 0.14f,
                        r, r);
                Ellipse2D n1 = new Ellipse2D.Float(s / 2 - r / 2, m, r, r);
                Ellipse2D n2 = new Ellipse2D.Float(s - m - r - s * 0.03f,
                        m + s * 0.14f, r, r);
                g.drawLine((int) (m + s * 0.10f), (int) (m + s * 0.20f),
                        (int) (s / 2), (int) (m + r / 2));
                g.drawLine((int) (s / 2), (int) (m + r / 2),
                        (int) (s - m - s * 0.10f), (int) (m + s * 0.20f));
                g.draw(n0);
                g.draw(n1);
                g.draw(n2);
                g.setColor(ACCENT);
                g.fill(n1);
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
                doc(g, s);
                g.setColor(ACCENT);
                g.drawLine((int) (s / 2), (int) (m + s * 0.16f),
                        (int) (s / 2), (int) (m + s * 0.50f));
                g.drawLine((int) (s / 2), (int) (m + s * 0.50f),
                        (int) (s * 0.35f), (int) (m + s * 0.36f));
                g.drawLine((int) (s / 2), (int) (m + s * 0.50f),
                        (int) (s * 0.65f), (int) (m + s * 0.36f));
                break;
            case CLEAR_OUTPUT:
                doc(g, s);
                g.setColor(ACCENT);
                g.drawLine((int) (s * 0.35f), (int) (s * 0.30f),
                        (int) (s * 0.65f), (int) (s * 0.60f));
                g.drawLine((int) (s * 0.65f), (int) (s * 0.30f),
                        (int) (s * 0.35f), (int) (s * 0.60f));
                break;
            case SAVE_OUTPUT:
                doc(g, s);
                g.setColor(ACCENT);
                g.fillRoundRect((int) (s * 0.40f), (int) (s * 0.42f),
                        (int) (s * 0.20f), (int) (s * 0.20f), 4, 4);
                break;
            case TRANSPOSE:
                int gs = (int) (s * 0.10f);
                int off = (int) (m + s * 0.05f);
                int step = gs + (int) (s * 0.05f);
                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        {
                        g.setColor(gray);
                        g.drawRect(off + i * step, off + j * step, gs, gs);
                        if (i == 1 && j == 1)
                            {
                            g.setColor(ACCENT);
                            g.fillRect(off + i * step, off + j * step, gs, gs);
                            }
                        }
                break;
            case SYMMETRIZE:
                // top double-arrow in the neutral outline, mirrored one
                // below in the accent (mirroring reads as "symmetrize")
                g.setColor(gray);
                g.drawLine((int) (s * 0.16f), (int) (s * 0.40f),
                        (int) (s * 0.84f), (int) (s * 0.40f));
                g.drawLine((int) (s * 0.70f), (int) (s * 0.30f),
                        (int) (s * 0.84f), (int) (s * 0.40f));
                g.drawLine((int) (s * 0.84f), (int) (s * 0.40f),
                        (int) (s * 0.70f), (int) (s * 0.50f));
                g.drawLine((int) (s * 0.30f), (int) (s * 0.32f),
                        (int) (s * 0.16f), (int) (s * 0.40f));
                g.drawLine((int) (s * 0.16f), (int) (s * 0.40f),
                        (int) (s * 0.30f), (int) (s * 0.48f));
                g.setColor(ACCENT);
                g.drawLine((int) (s * 0.16f), (int) (s * 0.62f),
                        (int) (s * 0.84f), (int) (s * 0.62f));
                g.drawLine((int) (s * 0.30f), (int) (s * 0.54f),
                        (int) (s * 0.16f), (int) (s * 0.62f));
                g.drawLine((int) (s * 0.16f), (int) (s * 0.62f),
                        (int) (s * 0.30f), (int) (s * 0.70f));
                g.drawLine((int) (s * 0.70f), (int) (s * 0.54f),
                        (int) (s * 0.84f), (int) (s * 0.62f));
                g.drawLine((int) (s * 0.84f), (int) (s * 0.62f),
                        (int) (s * 0.70f), (int) (s * 0.70f));
                break;
            case RENUMBER:
                for (int i = 0; i < 3; i++)
                    {
                    float y = s * 0.26f + i * s * 0.24f;
                    g.setColor(gray);
                    g.drawLine((int) (s * 0.34f), Math.round(y),
                            (int) (s * 0.80f), Math.round(y));
                    g.setColor(ACCENT);
                    g.fill(new Ellipse2D.Float(s * 0.19f, y - s * 0.05f,
                            s * 0.13f, s * 0.13f));
                    }
                break;
            default:
                break;
            }
        }

    private static void doc(Graphics2D g, int s)
        {
        float m = s * 0.20f;
        g.setColor(outline());
        g.drawRoundRect((int) (m + s * 0.05f), (int) (m + s * 0.05f),
                (int) (s * 0.55f), (int) (s * 0.70f), 5, 5);
        g.drawLine((int) (m + s * 0.15f), (int) (s - m - s * 0.05f),
                (int) (m + s * 0.15f), (int) (s * 0.15f));
        }
    }