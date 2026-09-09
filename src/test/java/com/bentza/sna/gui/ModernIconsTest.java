package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the drawn toolbar icons must render a non-blank duotone glyph
 * for every button kind, at the toolbar size, and always carry both hues -
 * a neutral outline and a blue accent element.
 */
public class ModernIconsTest
    {
    @Test
    public void everyKindRendersNonBlankDuotone()
        {
        for (int kind = 0; kind <= ModernIcons.RENUMBER; kind++)
            {
            javax.swing.ImageIcon icon = ModernIcons.get(kind, 22);
            assertEquals(22, icon.getIconWidth());
            assertEquals(22, icon.getIconHeight());

            BufferedImage img = new BufferedImage(22, 22,
                    BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = img.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();

            int painted = 0;
            int blue = 0;
            int gray = 0;
            for (int y = 0; y < 22; y++)
                for (int x = 0; x < 22; x++)
                    {
                    int rgb = img.getRGB(x, y);
                    int alpha = (rgb >>> 24) & 0xFF;
                    if (alpha == 0)
                        continue;
                    painted++;
                    int r = (rgb >> 16) & 0xFF;
                    int gr = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    if (b > r + 30 && b > gr + 20)
                        {
                        blue++; // the brand-blue accent element
                        } else if (Math.abs(r - gr) < 40
                                && Math.abs(gr - b) < 40)
                        {
                        gray++; // the neutral outline (black under Metal)
                        }
                    }
            assertTrue(painted > 60,
                    "kind " + kind + " should paint a glyph");
            assertTrue(blue >= 4,
                    "kind " + kind + " needs a blue accent element");
            assertTrue(gray >= 4,
                    "kind " + kind + " needs a neutral outline");
            }
        }
    }