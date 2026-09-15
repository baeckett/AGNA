/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.NodeArea;
import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the application persists the viewer state as "Key<TAB>value"
 * lines (the bundle-ready AgnaDefaultSettings.ini format). This battery
 * verifies the full settings surface round-trips through the serializer
 * and the parser, both in memory and through a real file.
 */
public class SettingsRoundTripTest
    {
    private NodeArea freshArea()
        {
        com.bentza.sna.core.AppRuntime.setCurrentFullNet(new FullNet());
        com.bentza.sna.core.AppRuntime.setCurrentNetwork(
                new com.bentza.sna.net.Network());
        FullNet full = new FullNet();
        full.createDefaultNetwork(3);
        full.attachArea();
        return full.getArea();
        }

    private NodeArea parseBack(String text)
        {
        NodeArea area = freshArea();
        FullNet.parseAgnaDefaultSettings(area, text);
        return area;
        }

    @Test
    public void defaultSettingsRoundTripUnchanged()
        {
        NodeArea a = freshArea();
        NodeArea b = parseBack(serialize(a));
        assertEquals(a.getSeparator(), b.getSeparator());
        assertEquals(a.getMaxTransparency(), b.getMaxTransparency());
        assertEquals(a.getPrintNames(), b.getPrintNames());
        assertEquals(a.getGridEnabled(), b.getGridEnabled());
        assertEquals(a.getNamesX(), b.getNamesX());
        assertEquals(a.getTitleY(), b.getTitleY());
        }

    @Test
    public void booleanFlagsRoundTrip()
        {
        NodeArea a = freshArea();
        a.setPrintNames(true);
        a.setGridEnabled(false);
        a.setSTGEnabled(false);
        a.setTitleVisible(true);
        a.setEdgeValueVisible(true);
        a.setFacesVisible(false);
        a.setAllowES(true);
        a.setColorFidelity(true);
        NodeArea b = parseBack(serialize(a));
        assertTrue(b.getPrintNames(), "print names");
        assertFalse(b.getGridEnabled(), "grid disabled");
        assertFalse(b.isSTGEnabled(), "snap to grid disabled");
        assertTrue(b.isTitleVisible(), "title");
        assertTrue(b.isEdgeValueVisible(), "edge values");
        assertFalse(b.isFaceVisible(), "faces hidden");
        assertTrue(b.getAllowES(), "edge selection");
        assertTrue(b.getColorFidelity(), "color fidelity");
        }

    @Test
    public void integerSettingsRoundTrip()
        {
        NodeArea a = freshArea();
        a.setSeparator(7);
        a.setGridSpace(41);
        a.setMaxTransparency(200);
        a.setGridTransparency(90);
        a.setEdgeValuePosition(3);
        a.setNamesX(12);
        a.setNamesY(-4);
        a.setTitleX(33);
        a.setTitleY(2);
        a.setBackgroundImageX(10);
        a.setBackgroundImageY(20);
        a.setBackgroundImageWidth(300);
        a.setBackgroundImageHeight(200);
        NodeArea b = parseBack(serialize(a));
        assertEquals(7, b.getSeparator(), "separator");
        assertEquals(41, b.getGridSpace(), "grid space");
        assertEquals(200, b.getMaxTransparency(), "max transparency");
        assertEquals(90, b.getGridTransparency(), "grid transparency");
        assertEquals(3, b.getEdgeValuePosition(), "edge value position");
        assertEquals(12, b.getNamesX());
        assertEquals(-4, b.getNamesY());
        assertEquals(33, b.getTitleX());
        assertEquals(2, b.getTitleY());
        assertEquals(10, b.getBackgroundImageX());
        assertEquals(20, b.getBackgroundImageY());
        assertEquals(300, b.getBackgroundImageWidth());
        assertEquals(200, b.getBackgroundImageHeight());
        }

    @Test
    public void colorsRoundTripExactly()
        {
        NodeArea a = freshArea();
        a.setEdgeValueColor(new Color(0x12, 0x34, 0x56));
        a.setGridColor(new Color(0x0a, 0xd2, 0x0a));
        a.setNamesColor(new Color(0x11, 0x22, 0x33));
        a.setTitleColor(new Color(0xaa, 0xbb, 0xcc));
        a.setBackgroundColor(new Color(0xff, 0x00, 0x33));
        a.setArrowColor(new Color(0x12, 0x90, 0xab));
        NodeArea b = parseBack(serialize(a));
        assertEquals(0x123456, b.getEdgeValueColor().getRGB() & 0xffffff);
        assertEquals(0x0ad20a, b.getGridColor().getRGB() & 0xffffff);
        assertEquals(0x112233, b.getNamesColor().getRGB() & 0xffffff);
        assertEquals(0xaabbcc, b.getTitleColor().getRGB() & 0xffffff);
        assertEquals(0xff0033, b.getBackgroundColor().getRGB() & 0xffffff);
        assertEquals(0x1290ab, b.getArrowColor().getRGB() & 0xffffff);
        }

    @Test
    public void backgroundImageSourceAndLayoutRoundTrip()
        {
        NodeArea a = freshArea();
        a.setBackgroundImage("backdrop.png");
        a.setDefaultImageLayout((byte) 2);
        NodeArea b = parseBack(serialize(a));
        assertEquals("backdrop.png", b.getBackgroundImageSource());
        assertEquals(2, b.getDefaultImageLayout());
        }

    @Test
    public void settingsFileRoundTripThroughTheRealPath() throws Exception
        {
        // the settings file lives per-call under user.home/.agna; point
        // user.home at a scratch dir so no real settings are touched
        String oldHome = System.getProperty("user.home");
        try
            {
            Path home = Files.createTempDirectory("agna_settings_home");
            System.setProperty("user.home", home.toString());
            NodeArea a = freshArea();
            a.setSeparator(9);
            a.setMaxTransparency(77);
            a.setGridColor(new Color(1, 2, 3));
            a.setPrintNames(true);

            FullNet full = new FullNet();
            full.createDefaultNetwork(3);
            full.attachArea();
            NodeArea storage = full.getArea();
            FullNet.parseAgnaDefaultSettings(storage, serialize(a));
            full.writeInitialSettings();

            FullNet full2 = new FullNet();
            full2.createDefaultNetwork(3);
            full2.attachArea();
            NodeArea b = full2.getArea();
            full2.readInitialSettings(b,
                    com.bentza.sna.Environment.getSettingsFile()
                            .getAbsolutePath());
            assertEquals(9, b.getSeparator(), "separator via file");
            assertEquals(77, b.getMaxTransparency(), "transparency via file");
            assertTrue(b.getPrintNames(), "names via file");
            assertEquals(0x010203, b.getGridColor().getRGB() & 0xffffff,
                    "grid color via file");
            } finally
            {
            System.setProperty("user.home", oldHome);
            }
        }

    @Test
    public void settingsTextCarriesTheGlobalPreferences()
        {
        String text = serialize(freshArea());
        assertTrue(text.contains("Classic Toolbar Icons"), text);
        assertTrue(text.contains("Look And Feel"), text);
        assertTrue(text.contains("Pajek Vectors Enabled"), text);
        assertTrue(text.contains("Default Export Format"), text);
        assertTrue(text.contains("Default Node Face"), text);
        assertTrue(text.contains("Working Directory"), text);
        }

    @Test
    public void bundledDefaultFaceIsUsable()
        {
        String face = com.bentza.sna.net.NodeArea.getDefaultNodeFaceSource();
        assertTrue(face != null && face.length() > 0,
                "a default face source is configured");
        }

    private String serialize(NodeArea area)
        {
        FullNet full = new FullNet();
        full.createDefaultNetwork(3);
        full.attachArea();
        return full.getAgna2DefaultSettings(area);
        }

    @Test
    public void areaConstructionSurvivesSettingsFileWithoutHooks()
            throws Exception
        {
        // regression: a real installation has ~/.agna/AgnaDefaultSettings.ini
        // and the CLI (headless) never registers an AppRuntime current
        // network; the viewer constructor must not dereference it
        String oldHome = System.getProperty("user.home");
        java.util.function.Supplier<FullNet> prevFull =
                com.bentza.sna.core.AppRuntime.currentFullNetSupplier();
        java.util.function.Supplier<com.bentza.sna.net.Network> prevNet =
                com.bentza.sna.core.AppRuntime.currentNetworkSupplier();
        try
            {
            Path home = Files.createTempDirectory("agna_no_hooks_home");
            System.setProperty("user.home", home.toString());
            Path agna = home.resolve(".agna");
            Files.createDirectories(agna);
            Files.write(agna.resolve("AgnaDefaultSettings.ini"),
                    "Print Names\tyes\nSeparator\t10\n".getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));
            assertTrue(com.bentza.sna.Environment.getSettingsFile().exists(),
                    "settings file present, as on a real machine");
            // the CLI registers no hooks: the current network is null
            com.bentza.sna.core.AppRuntime.setCurrentFullNetSupplier(
                    () -> null);
            com.bentza.sna.core.AppRuntime.setCurrentNetworkSupplier(
                    () -> null);

            FullNet full = new FullNet();
            full.createDefaultNetwork(3);
            full.attachArea(); // must not throw without AppRuntime hooks
            assertTrue(full.getArea() != null, "area constructed");
            } finally
            {
            System.setProperty("user.home", oldHome);
            com.bentza.sna.core.AppRuntime.setCurrentFullNetSupplier(prevFull);
            com.bentza.sna.core.AppRuntime.setCurrentNetworkSupplier(prevNet);
            }
        }
    }