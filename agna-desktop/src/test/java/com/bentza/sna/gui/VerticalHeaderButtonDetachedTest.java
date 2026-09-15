/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;

import org.junit.jupiter.api.Test;

/**
 * Regression test for the "New Network" crash on JDK 17: while the vertical
 * header is rebuilt (AgnaVerticalHeader.setHeader -> removeAll), Swing fires a
 * property change on each button and queries its text while the button is
 * already detached (parent == null). getText() must not dereference the
 * missing parent.
 */
public class VerticalHeaderButtonDetachedTest
    {
    @Test
    public void getTextOnDetachedButtonDoesNotThrow()
        {
        VerticalHeaderButton button = new VerticalHeaderButton(null,
                new Dimension(20, 20));
        assertEquals("", button.getText());
        }

    @Test
    public void getIndexOnDetachedButtonIsNegative()
        {
        VerticalHeaderButton button = new VerticalHeaderButton(null,
                new Dimension(20, 20));
        assertEquals(-1, button.getIndex());
        }
    }