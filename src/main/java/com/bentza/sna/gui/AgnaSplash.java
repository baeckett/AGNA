package com.bentza.sna.gui;

import com.bentza.sna.Environment;
import com.bentza.sna.Agna;
import com.bentza.sna.gui.MainFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import java.awt.BorderLayout;

public class AgnaSplash extends JWindow
    {
    final int waitTime = 5000;

    public AgnaSplash(Frame f)
        {
        super(f);
        ImageIcon splash_icon_00 = null;
        ImageIcon splash_icon_01 = null;
        try
            {
            splash_icon_00 = Environment.getButtonImageIcon("splash_00.gif");
            splash_icon_01 = Environment.getButtonImageIcon("splash_01.gif");
            // splash_icon_00 = new
            // ImageIcon(Agna.class.getResource(Environment.getButtonsLocation()
            // + "splash_00.gif"));
            // splash_icon_01 = new
            // ImageIcon(Agna.class.getResource(Environment.getButtonsLocation()
            // + "splash_01.gif"));
            } catch (Exception e)
            {
            return;
            }
        JLabel label_00 = new JLabel(splash_icon_00);
        JLabel label_01 = new JLabel(splash_icon_01);
        label_00.setBounds(0, 243, 350, 62); // x, y, width, height
        label_01.setBounds(0, 158, 350, 83);
        JPanel main_pane = new JPanel();
        main_pane.setBackground(new Color(221, 222, 211));
        Dimension labelSize = new Dimension(350, 350);
        main_pane.setPreferredSize(labelSize);
        main_pane.setMinimumSize(labelSize);
        main_pane.setMaximumSize(labelSize);
        main_pane.setLayout(null);
        main_pane.add(label_01);
        main_pane.add(label_00);
        getContentPane().add(main_pane, BorderLayout.CENTER);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2),
                screenSize.height / 2 - (labelSize.height / 2));
        addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                    {
                    setVisible(false);
                    dispose();
                    }
            });
        final int pause = waitTime;
        final Runnable closerRunner = new Runnable()
            {
                public void run()
                    {
                    setVisible(false);
                    dispose();
                    }
            };
        Runnable waitRunner = new Runnable()
            {
                public void run()
                    {
                    try
                        {
                        Thread.sleep(pause);
                        SwingUtilities.invokeAndWait(closerRunner);
                        } catch (Exception e)
                        {
                        }
                    }
            };
        setVisible(true);
        Thread splashThread = new Thread(waitRunner, "SplashThread");
        splashThread.start();
        }
    }