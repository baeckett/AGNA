/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.Environment;
import java.awt.Dimension;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;

class DoubleButton extends JPanel
    {
    public JButton up_button, down_button;

    private ImageIcon up_icon, down_icon, gray_up_icon, gray_down_icon;

    private Dimension dim, half;

    public DoubleButton()
        {
        dim = new Dimension(23, 23);
        half = new Dimension(16, 11);
        if (MainFrame.isClassicToolbarIcons())
            {
            up_icon = Environment.getButtonImageIcon("upArrow.gif");
            down_icon = Environment.getButtonImageIcon("downArrow.gif");
            gray_up_icon = Environment.getButtonImageIcon("grayUpArrow.gif");
            gray_down_icon = Environment.getButtonImageIcon("grayDownArrow.gif");
            } else
            {
            // 2.1.3: drawn arrows - faded gray at rest, blue on rollover
            up_icon = ModernIcons.get(ModernIcons.UP_ARROW, 11, true);
            down_icon = ModernIcons.get(ModernIcons.DOWN_ARROW, 11, true);
            gray_up_icon = ModernIcons.get(ModernIcons.UP_ARROW, 11);
            gray_down_icon = ModernIcons.get(ModernIcons.DOWN_ARROW, 11);
            }
        up_button = new JButton(gray_up_icon);
        down_button = new JButton(gray_down_icon);
        up_button.setRolloverIcon(up_icon);
        down_button.setRolloverIcon(down_icon);
        up_button.setBorder(null);
        down_button.setBorder(null);

        up_button.setMinimumSize(half);
        up_button.setPreferredSize(half);
        up_button.setMaximumSize(half);
        down_button.setMinimumSize(half);
        down_button.setPreferredSize(half);
        down_button.setMaximumSize(half);
        up_button.setBounds(2, 2, 16, 8);
        down_button.setBounds(2, 10, 16, 8);
        this.setLayout(null);
        this.add(up_button);
        this.add(down_button);
        }
    }