/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.Environment;
import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the desktop's runtime environment - asset extraction, settings
 * location, version and the bundled resources the UI depends on.
 */
public class EnvironmentAndAssetsTest
    {
    @Test
    public void facesDirectoryExtractsOnceAndHasFaces()
        {
        String first = Environment.getFacesDirectory();
        String second = Environment.getFacesDirectory();
        assertEquals(first, second, "extraction is idempotent");
        File dir = new File(first);
        assertTrue(dir.isDirectory(), "faces directory exists: " + first);
        String[] faces = dir.list();
        assertNotNull(faces);
        assertTrue(faces.length > 5, "bundled faces extracted: "
                + faces.length);
        }

    @Test
    public void settingsFileLivesInDotAgna()
        {
        String path = Environment.getSettingsFile().getAbsolutePath();
        assertTrue(path.contains(".agna"), path);
        assertTrue(path.endsWith("AgnaDefaultSettings.ini"), path);
        }

    @Test
    public void splashAndLogoAssetsShipOnTheClasspath()
        {
        assertNotNull(load("agna_splash_screen.gif"), "splash");
        assertNotNull(load("agna_logo.png"), "logo");
        assertNotNull(load("agna_icon.png"), "icon");
        }

    @Test
    public void classicButtonIconsAreBundled()
        {
        // the classic-GIF toolbar set still ships for the
        // "Classic Toolbar Icons" preference
        for (String name : new String[] { "CircularLayout.gif",
                "RandomLayout.gif", "AddNodes.gif", "DeleteNodes.gif" })
            {
            assertNotNull(Environment.getButtonImageIcon(name), name);
            }
        }

    @Test
    public void versionIs213()
        {
        assertEquals("2.1.3", Environment.getApplicationVersion());
        }

    private static java.net.URL load(String name)
        {
        return EnvironmentAndAssetsTest.class.getClassLoader()
                .getResource(name);
        }
    }