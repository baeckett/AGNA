/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import com.bentza.sna.gui.AgnaDialog;
import com.bentza.sna.gui.GrNet;
import com.bentza.sna.net.Actor;
import com.bentza.sna.net.Network;
import com.bentza.sna.net.NodeXY;
import com.bentza.sna.gui.MainFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

class NodeSettingsDialog
    {
    private AgnaDialog n_frame;

    private static JCheckBox no_face_box;

    private static JLabel width_label, path_label, name_label, x_label,
            y_label;

    private static JTextField width_field, path_field, name_field, x_field,
            y_field;

    private static JButton path_button, ok_button, cancel_button;

    private static JPanel face_panel, control;

    private static Actor src_node;

    private static int src_node_index;

    private static boolean apply_changes;

    private FocusListener focus_dialog = new FocusListener()
        {
            public void focusLost(FocusEvent e)
                {
                }

            public void focusGained(FocusEvent e)
                {
                }
        };

    private ActionListener act_dialog = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                // ok button listener:
                if (e.getSource() == ok_button)
                    {
                    if (no_face_box.isSelected())
                        {
                        src_node.noFace();
                        } else
                        {
                        src_node.setFace(path_field.getText());
                        }
                    src_node.setName(name_field.getText());
                    int node_size = 16;
                    try
                        {
                        node_size = Integer.parseInt(width_field.getText());
                        } catch (Exception e3) {
      AgnaLog.warn("suppressed exception", e3);
      }
                    src_node.setSize(node_size);
                    int node_x = src_node.getX(GrNet.getAreaWidth());
                    int node_y = src_node.getY(GrNet.getAreaWidth());
                    try
                        {
                        node_x = Integer.parseInt(x_field.getText());
                        src_node.setX(node_x, GrNet.getAreaWidth());
                        } catch (Exception ex) {
      AgnaLog.warn("suppressed exception", ex);
      }
                    try
                        {
                        node_y = Integer.parseInt(y_field.getText());
                        src_node.setY(node_y, GrNet.getAreaWidth());
                        } catch (Exception ey) {
      AgnaLog.warn("suppressed exception", ey);
      }
                    apply_changes = true;
                    MainFrame.setNodeName(src_node.getName(), src_node_index);
                    MainFrame.setNodeFace(src_node.getFace(), src_node_index);
                    n_frame.dispose();
                    }

                // cancel button listener:
                else if (e.getSource() == cancel_button)
                    {
                    apply_changes = false;
                    n_frame.dispose();
                    }

                // path button listener:
                else if (e.getSource() == path_button)
                    {
                    String tmp_image_name = src_node.getFaceSource();
                    if (tmp_image_name.equals("-"))
                        tmp_image_name = GrNet.inputImageFile(MainFrame
                                .getDefaultNodeFaceSource());
                    else
                        tmp_image_name = GrNet.inputImageFile(tmp_image_name);
                    if (tmp_image_name == null)
                        {
                        return;
                        }
                    ImageIcon tmp_icon = null;
                    try
                        {
                        tmp_icon = new ImageIcon(tmp_image_name);
                        } catch (Exception e2)
                        {
                        return;
                        }
                    if (tmp_icon == null)
                        {
                        return;
                        }
                    try
                        {
                        path_label.setIcon(new ImageIcon(tmp_icon.getImage()
                                .getScaledInstance(16, -1, Image.SCALE_FAST)));
                        } catch (Exception e1) {
      AgnaLog.warn("suppressed exception", e1);
      }
                    path_field.setText(tmp_image_name);
                    }

                // checkbox listener:
                else if (e.getSource() == no_face_box)
                    {
                    boolean is = !no_face_box.isSelected();
                    path_label.setEnabled(is);
                    path_field.setEnabled(is);
                    path_button.setEnabled(is);
                    width_label.setEnabled(is);
                    width_field.setEnabled(is);
                    }

                }
        };

    public NodeSettingsDialog(Actor src, int node_index)
        {
        apply_changes = false;
        src_node = src;
        src_node_index = node_index;
        Dimension frame_dim = new Dimension(350, 140);
        Dimension panel_dim = new Dimension(150, 70);
        n_frame = new AgnaDialog(GrNet.getCurrentFrame(), "Node Settings", true);
        n_frame.addFocusListener(focus_dialog);
        n_frame.setTitle("Node Settings");
        n_frame.setResizable(false);
        n_frame.setModal(true);
        Container content = n_frame.getContentPane();
        content.setLayout(new BorderLayout());
        content.setBackground(Color.white);

        control = new JPanel();
        GridBagLayout c_layout = new GridBagLayout();
        control.setLayout(c_layout);
        GridBagConstraints co = new GridBagConstraints();
        co.anchor = GridBagConstraints.WEST;

        co.insets = new Insets(10, 4, 4, 4); // adding space around
                                                // components

        // placing name_label:
        name_label = new JLabel("Name:");
        co.gridx = 1;
        co.gridy = 0;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        c_layout.setConstraints(name_label, co);
        control.add(name_label);

        // placing name_field:
        name_field = new JTextField(src.getName());
        name_field.setColumns(7);
        co.gridx = 2;
        co.gridy = 0;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 1.0;
        co.weighty = 0.0;
        co.fill = GridBagConstraints.HORIZONTAL;
        c_layout.setConstraints(name_field, co);
        control.add(name_field);

        co.insets = new Insets(4, 4, 4, 4); // adding space around components

        // placing x_label:
        x_label = new JLabel("X (pixels):");
        co.gridx = 1;
        co.gridy = 1;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        c_layout.setConstraints(x_label, co);
        co.fill = GridBagConstraints.NONE;
        control.add(x_label);

        // placing x_field:
        x_field = new JTextField(String.valueOf(src.getX(GrNet.getAreaWidth())));
        co.gridx = 2;
        co.gridy = 1;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 1.0;
        co.weighty = 0.0;
        co.fill = GridBagConstraints.HORIZONTAL;
        c_layout.setConstraints(x_field, co);
        control.add(x_field);

        // placing y_label:
        y_label = new JLabel("Y (pixels):");
        co.gridx = 1;
        co.gridy = 2;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        co.fill = GridBagConstraints.NONE;
        c_layout.setConstraints(y_label, co);
        control.add(y_label);

        // placing y_field:
        y_field = new JTextField(String.valueOf(src.getY(GrNet.getAreaWidth())));
        co.gridx = 2;
        co.gridy = 2;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 1.0;
        co.weighty = 0.0;
        co.fill = GridBagConstraints.HORIZONTAL;
        c_layout.setConstraints(y_field, co);
        control.add(y_field);

        // placing ok_button:
        ok_button = new JButton("Ok");
        ok_button.addActionListener(act_dialog);
        co.gridx = 1;
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
        co.gridx = 2;
        co.gridy = 3;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        co.fill = GridBagConstraints.NONE;
        co.anchor = GridBagConstraints.WEST;
        c_layout.setConstraints(cancel_button, co);
        control.add(cancel_button);

        // defining face_panel:
        face_panel = new JPanel();
        face_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Face"));

        // face panel components:
        GridBagLayout f_layout = new GridBagLayout();
        face_panel.setLayout(f_layout);
        GridBagConstraints fc = new GridBagConstraints();
        fc.insets = new Insets(2, 2, 2, 2); // adding space around components
        fc.anchor = GridBagConstraints.WEST;

        // placing no_face_box:
        no_face_box = new JCheckBox("No face");
        no_face_box.setSelected(false);
        no_face_box.addActionListener(act_dialog);
        fc.gridx = 0;
        fc.gridy = 0;
        fc.gridwidth = 1;
        fc.gridheight = 1;
        fc.weightx = 0.0;
        fc.weighty = 0.0;
        fc.fill = GridBagConstraints.NONE;
        f_layout.setConstraints(no_face_box, fc);
        face_panel.add(no_face_box);

        // placing width_label:
        width_label = new JLabel("Width (px):");
        width_label.setMinimumSize(new Dimension(40, 70));
        fc.gridx = 0;
        fc.gridy = 1;
        fc.gridwidth = 1;
        fc.gridheight = 1;
        fc.weightx = 0.0;
        fc.weighty = 0.0;
        fc.fill = GridBagConstraints.NONE;
        f_layout.setConstraints(width_label, fc);
        face_panel.add(width_label);

        // placing width_field:
        width_field = new JTextField(String.valueOf(src.getSize()));
        width_field.setColumns(5);
        fc.gridx = 1;
        fc.gridy = 1;
        fc.gridwidth = 1;
        fc.gridheight = 1;
        fc.weightx = 0.0;
        fc.weighty = 0.0;
        fc.fill = GridBagConstraints.NONE;
        f_layout.setConstraints(width_field, fc);
        face_panel.add(width_field);

        // placing path_label:
        path_label = new JLabel("File path:");
        try
            {
            path_label.setIcon(new ImageIcon(src.getFace().getImage()
                    .getScaledInstance(16, -1, Image.SCALE_REPLICATE)));
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        path_label.setMaximumSize(new Dimension(150, 16));
        path_label.setPreferredSize(new Dimension(150, 16));
        fc.gridx = 0;
        fc.gridy = 2;
        fc.gridwidth = 1;
        fc.gridheight = 1;
        fc.weightx = 0.0;
        fc.weighty = 0.0;
        fc.fill = GridBagConstraints.NONE;
        f_layout.setConstraints(path_label, fc);
        face_panel.add(path_label);

        // placing path_button:
        ImageIcon path_icon = Environment.getButtonImageIcon("OpenNetwork.gif");
        path_button = new JButton(path_icon);
        path_button.addActionListener(act_dialog);
        Dimension path_button_dimension = new Dimension(20, 20);
        path_button.setPreferredSize(path_button_dimension);
        path_button.setMinimumSize(path_button_dimension);
        path_button.setMaximumSize(path_button_dimension);
        path_icon = null;
        path_button_dimension = null;
        fc.gridx = 1;
        fc.gridy = 2;
        fc.gridwidth = 1;
        fc.gridheight = 1;
        fc.weightx = 0.0;
        fc.weighty = 0.0;
        fc.fill = GridBagConstraints.NONE;
        f_layout.setConstraints(path_button, fc);
        face_panel.add(path_button);

        // placing path_field:
        path_field = new JTextField(src.getFaceSource());
        path_field.setHorizontalAlignment(JTextField.LEFT);
        path_field.setColumns(17);
        fc.gridx = 0;
        fc.gridy = 3;
        fc.gridwidth = 3;
        fc.gridheight = 1;
        fc.weightx = 1.0;
        fc.weighty = 0.0;
        fc.fill = GridBagConstraints.HORIZONTAL;
        f_layout.setConstraints(path_field, fc);
        face_panel.add(path_field);

        // placing face_panel:
        co.gridx = 0;
        co.gridy = 0;
        co.weightx = 1.0;
        co.weighty = 1.0;
        co.gridwidth = 1;
        co.gridheight = 4;
        co.fill = GridBagConstraints.BOTH;
        c_layout.setConstraints(face_panel, co);
        control.add(face_panel);

        // node has no face:
        boolean is = true;
        if (src.getFaceSource().equals("-"))
            {
            is = false;
            } else
            {
            is = true;
            }
        no_face_box.setSelected(!is);
        path_label.setEnabled(is);
        path_field.setEnabled(is);
        path_button.setEnabled(is);
        width_label.setEnabled(is);
        width_field.setEnabled(is);

        // finish:
        control.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));
        content.add(BorderLayout.CENTER, control);
        n_frame.pack();
        // placing n_frame in ceneter of screen:
        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        n_frame.setLocation(sdim.width / 2 - n_frame.getSize().width / 2,
                sdim.height / 2 - n_frame.getSize().height / 2);

        }

    public static boolean getApplyChanges()
        {
        return apply_changes;
        }

    public void showDialog()
        {
        try
            {
            n_frame.show();
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        }

    }