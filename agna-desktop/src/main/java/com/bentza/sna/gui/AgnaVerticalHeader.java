/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.net.Network;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

class AgnaVerticalHeader extends JPanel
    {

    public AgnaVerticalHeader()
        {
        }

    

    public void updateFace(int tmp_index)
        {
        try
            {
            // final Image tmp_image = tmp_icon.getImage().getScaledInstance(8,
            // -1, Image.SCALE_FAST);
            VerticalHeaderButton i_button = (VerticalHeaderButton) this
                    .getComponent(tmp_index);
            i_button.setIcon(MainFrame.getCurrentNetwork().getActor(tmp_index)
                    .getSmallFace());
            
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        }

    public void setAllFaces(ImageIcon tmp_icon)
        {
        int n = this.getComponentCount();
        VerticalHeaderButton i_button = (VerticalHeaderButton) this
                .getComponent(0);
        final Dimension i_dim = i_button.getPreferredSize();

        
        this.removeAll();
        final Network tmp_network = MainFrame.getCurrentNetwork();
        for (int i = 0; i < n; i++)
            {
            i_button = HeaderButtonFactory.requestButton(tmp_network
                    .getActor(i).getSmallFace(), i_dim);
            this.add(i_button);
            }
        }

    public void setHeader()
        {
        final Dimension i_dim = new Dimension((int) (MainFrame.col_width * 1),
                MainFrame.getCurrentTable().getRowHeight());
        this.removeAll();
        // JButton[] buttons = new JButton[n];
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final Network tmp_network = MainFrame.getCurrentNetwork();
        final int n = tmp_network.getSize();
        Image tmp_image = null;
        for (int i = 0; i < n; i++)
            {
            
            this.add(HeaderButtonFactory.requestButton(tmp_network.getActor(i)
                    .getSmallFace(), i_dim));

            }
        this.setPreferredSize(new Dimension(i_dim.width, n * i_dim.height));
        }
    }