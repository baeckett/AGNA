package com.bentza.sna.gui;

import com.bentza.sna.Agna;
import com.bentza.sna.Environment;
import com.bentza.sna.gui.MainFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

class AboutBox
    {
    private AgnaDialog n_frame;

    private static JLabel text_label, icon_label;

    // private static JButton url_button;
    private static UrlLabel url_button;

    private static JButton ok_button;

    private static JPanel face_panel;

    private ActionListener act_dialog = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                // ok button listener:
                if (e.getSource() == ok_button)
                    {
                    n_frame.dispose();
                    n_frame = null;
                    }

                // url button listener:
                /*
                 * if (e.getSource()==url_button) {
                 * BrowserControl.displayURL(MainFrame.getApplicationUrl());
                 * //n_frame.dispose(); //n_frame = null; }
                 */
                }
        };

    public AboutBox(JFrame where)
        {
        Dimension frame_dim = new Dimension(350, 140);
        Dimension panel_dim = new Dimension(150, 70);
        n_frame = new AgnaDialog(where, "Node Settings", true);
        n_frame.setTitle("About Agna");
        n_frame.setModal(true);

        // n_frame.setIconImage(MainFrame.getMainIcon().getImage());

        ImageIcon splash_icon = null;
        // 2.1.3: the About box follows the white splash palette
        Color background_color = Color.WHITE;
        icon_label = new JLabel();
        try
            {
            // 2.1.3: the official logo (horizontal white lockup)
            java.awt.Image logo = javax.imageio.ImageIO.read(AboutBox.class
                    .getResourceAsStream("/agna_logo.png"));
            int logo_width = 140;
            int logo_height = logo.getHeight(null) * logo_width
                    / Math.max(1, logo.getWidth(null));
            icon_label.setIcon(new ImageIcon(logo.getScaledInstance(
                    logo_width, logo_height, java.awt.Image.SCALE_SMOOTH)));
            } catch (Exception e)
            {
            // classic fallback
            icon_label.setIcon(Environment.getButtonImageIcon("splash_01.gif"));
            }
        text_label = new JLabel(
                "<html><font size = 2 color='#6E6E6E' face='Arial,Helvetica,Verdana,sans-serif'>"
                        + Environment.getApplicationFullName()
                        + "<br>"
                        + Environment.getApplicationCopyright()
                        + "<br>Licensed under the Apache License, Version 2.0"
                        + "<br><br>For the latest version, visit the Agna "
                        + "website:<br><br>"
                        + Environment.getDesktopCitationBlock().replace("\n",
                                "<br>"));
        // url_button = new JButton("<html><font size = 2 color='#FF0000'
        // face='Arial,Helvetica,Verdana,sans-serif'><a href='" +
        // MainFrame.getApplicationUrl() +"'>" + MainFrame.getApplicationUrl() +
        // "</a>");
        url_button = new UrlLabel("https://www.netanalysis.co.uk");
        // url_button.setBorder(null);
        url_button.setBackground(background_color);
        // url_button.addActionListener(act_dialog);
        ok_button = new JButton("Ok");
        ok_button.addActionListener(act_dialog);

        Container content = n_frame.getContentPane();
        content.setLayout(new BorderLayout());

        JPanel control = new JPanel();
        control.setBackground(background_color);
        // control.setBorder(BorderFactory.createRaisedBevelBorder());
        // control.setPreferredSize(frame_dim);

        GridBagLayout c_layout = new GridBagLayout();
        control.setLayout(c_layout);
        GridBagConstraints co = new GridBagConstraints();
        co.anchor = GridBagConstraints.WEST;
        co.insets = new Insets(10, 15, 10, 10); // adding space around
                                                // components

        // placing text_label:
        co.gridx = 2;
        co.gridy = 0;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.5;
        co.weighty = 0.0;
        c_layout.setConstraints(text_label, co);
        control.add(text_label);

        // placing ok_button:
        co.gridx = 2;
        co.gridy = 3;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.5;
        c_layout.setConstraints(ok_button, co);
        control.add(ok_button);

        // placing url_button:
        co.gridx = 2;
        co.gridy = 1;
        co.gridwidth = 1;
        co.gridheight = 1;
        co.weightx = 0.5;
        co.weighty = 0.0;
        c_layout.setConstraints(url_button, co);
        control.add(url_button);

        // placing icon_label:
        co.gridx = 0;
        co.gridy = 2;
        co.gridwidth = 3;
        co.gridheight = 1;
        co.weightx = 0.0;
        co.weighty = 0.0;
        co.anchor = GridBagConstraints.WEST;
        c_layout.setConstraints(icon_label, co);
        control.add(icon_label);

        // finish:
        // etched_panel.add(control);
        control.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));
        content.add(BorderLayout.CENTER, control);

        // n_frame.setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);

        n_frame.pack();
        // placing n_frame in ceneter of screen:
        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        n_frame.setLocation(sdim.width / 2 - n_frame.getSize().width / 2,
                sdim.height / 2 - n_frame.getSize().height / 2);

        ok_button.requestFocus();
        }

    public void showDialog()
        {
        n_frame.show();
        }

    }