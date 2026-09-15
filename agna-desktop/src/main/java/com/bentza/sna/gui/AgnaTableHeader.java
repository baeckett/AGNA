/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.net.Network;
import java.awt.Color;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

class AgnaTableHeader
    {
    private JTableHeader h;

    private AgnaVerticalHeader v;

    public AgnaTableHeader()
        {
        h = MainFrame.getCurrentTable().getTableHeader();
        v = new AgnaVerticalHeader();
        // Dimension v_dim = h.getPreferredSize();

        }

    public void setBothHeaders(String[] names)
        {
        // setting vertical header names:
        // OLD VERSION: v.setHeader(names);
        v.setHeader();

        // working with horizontal header:
        ImageIcon icon = null;
        JTable tmp_table = h.getTable();
        Network tmp_network = MainFrame.getCurrentNetwork();
        DefaultTableCellRenderer hr = null;

        TableColumnModel colmod = tmp_table.getColumnModel();
        TableColumn tab_col = null;
        final int n = tmp_table.getColumnCount();
        ImageIcon tmp_icon = null;
        final Color header_background_color = (new JButton()).getBackground();
        for (int i = 0; i < n; i++)
            {
            hr = new DefaultTableCellRenderer();
            try
                {
                tmp_icon = new ImageIcon(tmp_network.getActor(i).getFace()
                        .getImage().getScaledInstance(8, -1, Image.SCALE_FAST));
                hr.setIcon(tmp_icon);
                hr.setBackground(header_background_color);
                } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
            tab_col = colmod.getColumn(i);
            tab_col.setHeaderRenderer(hr);
            try
                {
                tab_col.setHeaderValue(names[i]);
                hr.setToolTipText(names[i]);
                } catch (Exception e2)
                {
                String node_name = tmp_network.getNodeName(i);
                tab_col.setHeaderValue(node_name);
                hr.setToolTipText(node_name);
                }
            tab_col.setResizable(false);
            }
        }

    public JTableHeader getHorizontalHeader()
        {
        return h;
        }

    public AgnaVerticalHeader getAgnaVerticalHeader()
        {
        return v;
        }

    public void setName(String tmp_name, int tmp_index)
        {
        // OLD VERSION v.setName(tmp_name, tmp_index);

        // working with horizontal header:
        TableColumnModel colmod = h.getTable().getColumnModel();
        colmod.getColumn(tmp_index).setHeaderValue(tmp_name);
        h.getTable().repaint();
        }

    public void setFace(ImageIcon tmp_icon, int tmp_index)
        {
        v.updateFace(tmp_index);

        // working with horizontal header:
        final TableColumnModel colmod = h.getTable().getColumnModel();
        final String actor_name = (String) colmod.getColumn(tmp_index)
                .getHeaderValue();
        DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
        try
            {
            Image tmp_image = tmp_icon.getImage().getScaledInstance(8, -1,
                    Image.SCALE_FAST);
            hr.setIcon(new ImageIcon(tmp_image));
            // setting background as native button color:
            hr.setBackground((new JButton()).getBackground());
            hr.setToolTipText(MainFrame.getCurrentNetwork().getNodeName(
                    tmp_index));
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }

        colmod.getColumn(tmp_index).setHeaderRenderer(hr);
        colmod.getColumn(tmp_index).setHeaderValue(actor_name);
        h.getTable().repaint();
        }

    public void setAllFaces(ImageIcon tmp_icon)
        {
        v.setAllFaces(tmp_icon);

        // working with horizontal header:
        TableColumnModel colmod = h.getTable().getColumnModel();
        DefaultTableCellRenderer hr = null;
        ImageIcon new_icon = null;
        String actor_name = "";
        for (int i = 0; i < colmod.getColumnCount(); i++)
            {
            hr = new DefaultTableCellRenderer();
            try
                {
                Image tmp_image = tmp_icon.getImage().getScaledInstance(8, -1,
                        Image.SCALE_FAST);
                new_icon = new ImageIcon(tmp_image);
                hr.setIcon(new_icon);
                } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
            actor_name = (String) colmod.getColumn(i).getHeaderValue();
            colmod.getColumn(i).setHeaderRenderer(hr);
            colmod.getColumn(i).setHeaderValue(actor_name);
            }
        h.getTable().repaint();
        }

    

    }