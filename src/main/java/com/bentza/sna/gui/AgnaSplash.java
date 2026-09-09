package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * 2.1.3: modernised splash screen - the black logo, the wordmark and the
 * version on a clean dark panel; click anywhere or wait five seconds to
 * dismiss it.
 */
public class AgnaSplash extends JWindow
    {
    final int waitTime = 5000;

    public AgnaSplash(Frame f)
        {
        super(f);
        final int w = 640;
        final int h = 420;
        JPanel main_pane = new JPanel();
        main_pane.setBackground(Color.BLACK);
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
            final int logo_size = 260;
            Image scaled = logo_image.getScaledInstance(logo_size, logo_size,
                    Image.SCALE_SMOOTH);
            JLabel logo_label = new JLabel(new ImageIcon(scaled));
            logo_label.setBounds((w - logo_size) / 2, 24, logo_size,
                    logo_size);
            main_pane.add(logo_label);
            }

        JLabel name_label = new JLabel("Agna", SwingConstants.CENTER);
        name_label.setForeground(new Color(0, 200, 83));
        name_label.setFont(new Font("SansSerif", Font.BOLD, 46));
        name_label.setBounds(0, 292, w, 56);
        main_pane.add(name_label);

        String version = "2.1.3";
        try
            {
            version = Environment.getApplicationVersion();
            } catch (Exception e)
            {
            }
        JLabel version_label = new JLabel("Version " + version
                + "  -  Social Network Analysis", SwingConstants.CENTER);
        version_label.setForeground(new Color(190, 190, 190));
        version_label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        version_label.setBounds(0, 354, w, 22);
        main_pane.add(version_label);

        getContentPane().add(main_pane, java.awt.BorderLayout.CENTER);
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
                        } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                    }
            };
        setVisible(true);
        Thread splashThread = new Thread(waitRunner, "SplashThread");
        splashThread.start();
        }
    }