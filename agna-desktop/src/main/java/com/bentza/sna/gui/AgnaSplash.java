/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * 2.1.3: modernised splash screen - the white lockup, the wordmark and
 * the version on a clean light panel; click anywhere or wait a few
 * seconds to dismiss it.
 */
public class AgnaSplash extends JWindow
    {
    final int waitTime = 4000;
    private final AtomicBoolean dismissed = new AtomicBoolean(false);
    private AWTEventListener globalMouseListener;

    public void dismiss()
        {
        if (dismissed.compareAndSet(false, true))
            {
            SwingUtilities.invokeLater(() ->
                {
                try
                    {
                    if (globalMouseListener != null)
                        {
                        Toolkit.getDefaultToolkit().removeAWTEventListener(globalMouseListener);
                        globalMouseListener = null;
                        }
                    } catch (Exception ignored)
                    {
                    }
                setVisible(false);
                dispose();
                });
            }
        }

    private void attachMouseDismissRecursively(Component comp)
        {
        if (comp == null)
            return;
        comp.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                    {
                    dismiss();
                    }
            });
        if (comp instanceof Container)
            {
            for (Component child : ((Container) comp).getComponents())
                {
                attachMouseDismissRecursively(child);
                }
            }
        }

    public AgnaSplash(Frame f)
        {
        super(f);
        final int w = 640;
        final int h = 420;
        JPanel main_pane = new JPanel();
        // 2.1.3: the final logo is a white lockup - the splash panel
        // matches it
        main_pane.setBackground(Color.WHITE);
        main_pane.setLayout(null);
        Dimension labelSize = new Dimension(w, h);
        main_pane.setPreferredSize(labelSize);
        main_pane.setMinimumSize(labelSize);
        main_pane.setMaximumSize(labelSize);

        Image logo_image = null;
        try
            {
            logo_image = ImageIO.read(AgnaSplash.class
                    .getResourceAsStream("/agna_logo.png"));
            } catch (Exception e)
            {
            AgnaLog.warn("splash logo unavailable: " + e);
            }
        if (logo_image != null)
            {
            // the lockup already carries the wordmark - fit it centrally
            final int logo_width = 480;
            int logo_height = logo_image.getHeight(null) * logo_width
                    / logo_image.getWidth(null);
            Image scaled = logo_image.getScaledInstance(logo_width,
                    logo_height, Image.SCALE_SMOOTH);
            JLabel logo_label = new JLabel(new ImageIcon(scaled));
            logo_label.setBounds((w - logo_width) / 2, 60, logo_width,
                    logo_height);
            main_pane.add(logo_label);
            }

        String version = "2.1.3";
        try
            {
            version = Environment.getApplicationVersion();
            } catch (Exception e)
            {
            }
        JLabel version_label = new JLabel("Version " + version
                + " - Applied Graph & Network Analysis Open Source",
                SwingConstants.CENTER);
        version_label.setForeground(new Color(110, 110, 110));
        version_label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        version_label.setBounds(0, 280, w, 22);
        main_pane.add(version_label);

        // 2.1.3: the official footer - exact text, source stays ASCII-safe
        // (\u2013 = en dash, \u021B = t with comma below)
        JLabel bottom_label = new JLabel(
                "<html><center>Copyright 2001\u20132026 Marius Ion "
                        + "Ben\u021Ba<br>www.netanalysis.co.uk<br><br>"
                        + "AGNA is licensed under the Apache License, "
                        + "Version 2.0.<br>You may obtain a copy of the "
                        + "License at:<br>https://www.apache.org/licenses/"
                        + "LICENSE-2.0</center></html>",
                SwingConstants.CENTER);
        bottom_label.setForeground(new Color(110, 110, 110));
        bottom_label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bottom_label.setBounds(0, 300, w, 112);
        main_pane.add(bottom_label);

        getContentPane().add(main_pane, java.awt.BorderLayout.CENTER);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2),
                screenSize.height / 2 - (labelSize.height / 2));

        // Dismiss on direct click on splash window or any of its child panels/labels
        attachMouseDismissRecursively(this);
        attachMouseDismissRecursively(main_pane);

        // Global AWT listener: dismiss splash if user clicks anywhere in the app
        try
            {
            globalMouseListener = event ->
                {
                if (event.getID() == MouseEvent.MOUSE_PRESSED)
                    {
                    dismiss();
                    }
                };
            Toolkit.getDefaultToolkit().addAWTEventListener(globalMouseListener, AWTEvent.MOUSE_EVENT_MASK);
            } catch (Exception e)
            {
            AgnaLog.warn("Could not register global mouse listener on splash: " + e);
            }

        final int pause = waitTime;
        Runnable waitRunner = () ->
            {
            try
                {
                Thread.sleep(pause);
                } catch (InterruptedException ignored)
                {
                Thread.currentThread().interrupt();
                } catch (Exception e)
                {
                AgnaLog.warn("suppressed exception", e);
                }
            dismiss();
            };
        setVisible(true);
        Thread splashThread = new Thread(waitRunner, "SplashThread");
        splashThread.setDaemon(true);
        splashThread.start();
        }
    }