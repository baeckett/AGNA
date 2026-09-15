/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * Verifies the backward-compatible resolution of legacy face references
 * (2.1.2 files): relative ".\Faces\..." paths, Windows-style separators and
 * the old "Shaddow" spelling must all resolve to the bundled faces assets.
 */
public class ImageStockCompatTest
    {
    @Test
    public void legacyShaddowRelativePathResolvesToBundledAsset()
        {
        ImageStock stock = new ImageStock();
        // exactly what the shipped 2.1.2 samples store in "Node Faces"
        ImageItem item = stock
                .requestImageItem(".\\Faces\\Light Background\\Green Triangle Shaddow.gif");

        assertNotNull(item);
        assertFalse(item.getImageSource().equals("-"),
                "legacy face must resolve to a real file");
        File resolved = new File(item.getImageSource());
        assertTrue(resolved.exists(), "resolved file must exist");
        assertTrue(item.getImageSource().indexOf("Shadow") >= 0,
                "legacy spelling must be corrected to Shadow");
        }

    @Test
    public void theUserFacingExampleResolvesToo()
        {
        ImageStock stock = new ImageStock();
        ImageItem item = stock
                .requestImageItem(".\\Faces\\Light Background\\Green Bullet Shaddow.gif");

        assertNotNull(item);
        assertFalse(item.getImageSource().equals("-"));
        }

    @Test
    public void plainExistingPathStillResolvesUnchanged()
        {
        ImageStock stock = new ImageStock();
        ImageItem item = stock.requestImageItem("faces/Light Background/Red Bullet Shadow.gif");

        assertNotNull(item);
        assertFalse(item.getImageSource().equals("-"));
        }
    }