package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the drawn toolbar icons must render a non-blank duotone glyph
 * for every button kind (main window and viewer), at the toolbar size,
 * always carrying both hues - a neutral outline and a blue accent element.
 * The rollover variant must be predominantly blue (the "more colorful"
 * hover version).
 */
public class ModernIconsTest
    {
    private static int[] stats(javax.swing.ImageIcon icon, int size)
        {
        BufferedImage img = new BufferedImage(size, size,
                BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        int painted = 0;
        int blue = 0;
        int gray = 0;
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
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
                    blue++;
                    } else if (Math.abs(r - gr) < 40
                            && Math.abs(gr - b) < 40)
                    {
                    gray++;
                    }
                }
        return new int[] { painted, blue, gray };
        }

    @Test
    public void everyKindRendersNonBlankDuotone()
        {
        for (int kind = 0; kind < ModernIcons.KIND_COUNT; kind++)
            {
            javax.swing.ImageIcon icon = ModernIcons.get(kind, 22);
            assertEquals(22, icon.getIconWidth());
            assertEquals(22, icon.getIconHeight());
            int[] st = stats(icon, 22);
            assertTrue(st[0] > 40,
                    "kind " + kind + " should paint a glyph");
            assertTrue(st[1] >= 4,
                    "kind " + kind + " needs a blue accent element");
            assertTrue(st[2] >= 4,
                    "kind " + kind + " needs a neutral outline");
            }
        }

    @Test
    public void rolloverVariantIsPredominantlyBlue()
        {
        for (int kind = 0; kind < ModernIcons.KIND_COUNT; kind++)
            {
            javax.swing.ImageIcon icon = ModernIcons.get(kind, 22, true);
            int[] st = stats(icon, 22);
            assertTrue(st[0] > 40, "rollover kind " + kind + " painted");
            assertTrue(st[1] >= 20,
                    "rollover kind " + kind
                            + " should be mostly blue (blue=" + st[1]
                            + ")");
            }
        }
    }