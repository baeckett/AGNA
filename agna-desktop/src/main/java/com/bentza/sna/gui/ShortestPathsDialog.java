/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.net.Network;
import java.io.*;
import java.awt.*;
import java.lang.Math.*;
import java.lang.System.*;
import java.lang.Object.*;
import java.awt.event.*;
import java.awt.Event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.JFileChooser;
import java.awt.image.*;
import java.awt.Graphics2D.*;
import java.util.*;
import java.awt.datatransfer.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.text.rtf.*;
import javax.swing.text.html.*;
import javax.swing.text.*;
import java.lang.reflect.Array;
import javax.swing.plaf.*;
import java.text.*;
import java.awt.image.*;
import javax.swing.filechooser.*;
import javax.swing.text.*;
import java.beans.*;

import java.net.*;

class ShortestPathsDialog
    {
    private static AgnaDialog n_frame;

    private static JComboBox from_box, to_box;

    private static JButton ok_button, cancel_button;

    private static boolean ready;

    private static IndexPair node_pair;

    private WindowListener sp_window_listener = new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
                {
                node_pair = null;
                n_frame.dispose();
                n_frame = null;
                }
        };

    private ActionListener act_dialog = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                // ok button listener:
                if (e.getSource() == ok_button)
                    {
                    node_pair.first_index = from_box.getSelectedIndex();
                    node_pair.second_index = to_box.getSelectedIndex();
                    n_frame.dispose();
                    n_frame = null;
                    }

                // cancel button listener:
                if (e.getSource() == cancel_button)
                    {
                    node_pair = null;
                    n_frame.dispose();
                    n_frame = null;
                    }

                // from_box:
                if (e.getSource() == from_box && ready)
                    {
                    int bad = to_box.getSelectedIndex();
                    if (from_box.getSelectedIndex() == bad)
                        {
                        if (bad == to_box.getItemCount() - 1)
                            {
                            bad = -1;
                            }
                        ok_button.setEnabled(false);
                        } else
                        ok_button.setEnabled(true);
                    }

                // to_box:
                if (e.getSource() == to_box && ready)
                    {
                    int bad = from_box.getSelectedIndex();
                    if (to_box.getSelectedIndex() == bad)
                        {
                        if (bad == from_box.getItemCount() - 1)
                            {
                            bad = -1;
                            }
                        ok_button.setEnabled(false);
                        } else
                        ok_button.setEnabled(true);
                    }
                }
        };

    public ShortestPathsDialog(Network src)
        {
        ready = false;
        node_pair = new IndexPair(); // -1, -1
        Dimension frame_dim = new Dimension(350, 140);
        Dimension panel_dim = new Dimension(150, 70);
        n_frame = new AgnaDialog(GrNet.getCurrentFrame(), "Nodes Selection",
                true);
        n_frame.addWindowListener(sp_window_listener);
        n_frame.setTitle("Nodes Selection");
        n_frame.setResizable(false);
        n_frame.setModal(true);

        Container content = n_frame.getContentPane();
        content.setLayout(new BorderLayout());
        content.setBackground(Color.white);

        JPanel control = new JPanel();
        GridBagLayout c_layout = new GridBagLayout();
        control.setLayout(c_layout);
        GridBagConstraints co = new GridBagConstraints();
        co.anchor = GridBagConstraints.WEST;

        co.insets = new Insets(4, 4, 4, 4); // adding space around components

        // placing main_label:
        JLabel main_label = new JLabel("Find shortest paths in current network");
        main_label.setHorizontalAlignment(JLabel.LEFT);
        co.gridx = 0;
        co.gridy = 0;
        co.gridwidth = 2;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        co.fill = GridBagConstraints.HORIZONTAL;
        co.anchor = GridBagConstraints.CENTER;
        c_layout.setConstraints(main_label, co);
        control.add(main_label);

        // placing from_label:
        JLabel from_label = new JLabel("from node:");
        co.gridx = 0;
        co.gridy = 1;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        co.anchor = GridBagConstraints.WEST;
        c_layout.setConstraints(from_label, co);
        control.add(from_label);

        // placing to_label:
        JLabel to_label = new JLabel("to node:");
        co.gridx = 0;
        co.gridy = 2;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        c_layout.setConstraints(to_label, co);
        control.add(to_label);

        // placing from_box:
        from_box = new JComboBox();
        from_box.addActionListener(act_dialog);
        for (int i = 0; i < src.getSize(); i++)
            {
            from_box.addItem(src.getActor(i).getName());
            }
        co.gridx = 1;
        co.gridy = 1;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        c_layout.setConstraints(from_box, co);
        control.add(from_box);

        // placing to_box:
        to_box = new JComboBox();
        to_box.addActionListener(act_dialog);
        for (int i = 0; i < src.getSize(); i++)
            {
            to_box.addItem(src.getActor(i).getName());
            }
        to_box.setSelectedIndex(1);
        co.gridx = 1;
        co.gridy = 2;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        c_layout.setConstraints(to_box, co);
        control.add(to_box);

        // placing ok_button:
        ok_button = new JButton("Ok");
        ok_button.addActionListener(act_dialog);
        co.gridx = 0;
        co.gridy = 3;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        co.fill = GridBagConstraints.NONE;
        co.anchor = GridBagConstraints.EAST;
        c_layout.setConstraints(ok_button, co);
        control.add(ok_button);

        // placing cancel_button:
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(act_dialog);
        co.gridx = 1;
        co.gridy = 3;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        co.fill = GridBagConstraints.NONE;
        co.anchor = GridBagConstraints.WEST;
        c_layout.setConstraints(cancel_button, co);
        control.add(cancel_button);

        control.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));
        content.add(BorderLayout.CENTER, control);
        n_frame.pack();
        // placing n_frame in ceneter of screen:
        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        n_frame.setLocation(sdim.width / 2 - n_frame.getSize().width / 2,
                sdim.height / 2 - n_frame.getSize().height / 2);

        ready = true;
        }

    public IndexPair getNodePair()
        {
        return node_pair;
        }

    public void showDialog()
        {
        n_frame.show();
        }

    }