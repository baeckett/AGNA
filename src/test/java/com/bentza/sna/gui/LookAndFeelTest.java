package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.FullNet;

/**
 * 2.1.3: the FlatLaf dependency must be bundled and the look-and-feel
 * preference must round-trip through the settings parser.
 */
public class LookAndFeelTest
    {
    @Test
    public void flatlafClassesAreOnTheClasspath() throws Exception
        {
        assertNotNull(Class.forName("com.formdev.flatlaf.FlatDarkLaf"));
        assertNotNull(Class.forName("com.formdev.flatlaf.FlatLightLaf"));
        }

    @Test
    public void lookAndFeelPreferenceRoundTrips()
        {
        String settings = "Look And Feel\tflatlaf\n"
                + "Pajek Vectors Enabled\tyes\n"
                + "Default Export Format\t3\n";
        FullNet.parseAgnaNonGraphicDefaultSettings(settings);
        assertEquals("flatlaf", MainFrame.getLookAndFeel());
        }
    }