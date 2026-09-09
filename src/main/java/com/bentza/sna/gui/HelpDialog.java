package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

class HelpDialog
    {
    private static AgnaDialog n_frame;

    private static JButton b_contents, b_index, b_close, b_back, b_forward;

    private static JEditorPane editor_pane;

    private static Vector url_list;

    private final int url_list_capacity = 20;

    private static int current_url;

    private HyperlinkListener act_link_listener = new HyperlinkListener()
        {
            public void hyperlinkUpdate(HyperlinkEvent e)
                {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                    {
                    try
                        {
                        editor_pane.setPage(e.getURL());
                        // removing unnecessary elements:
                        while (current_url + 1 <= url_list.size() - 1)
                            {
                            url_list.removeElementAt(current_url + 1);
                            }
                        url_list.addElement(e.getURL());
                        if (url_list.size() > url_list_capacity)
                            {
                            url_list.remove(0);
                            }
                        if (url_list.size() > 0)
                            current_url = url_list.size() - 1;
                        enablingManager();
                        } catch (IOException ex)
                        {
                        }
                    }
                }
        };

    private ActionListener act_dialog = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                // contents button listener:
                final String fs = System.getProperty("file.separator");
        if (e.getSource() == b_contents)
                    {
                    try
                        {
                        editor_pane.setPage(HelpDialog.class
                                .getResource("/help/help_contents.htm"));
                        url_list.addElement(editor_pane.getPage());
                        current_url = url_list.size() - 1;
                        enablingManager();
                        } catch (Exception ex)
                        {
                        editor_pane.setText("Help Contents file not found.");
                        }
                    }

                // back button listener:
                if (e.getSource() == b_back)
                    {
                    try
                        {
                        if (current_url > 0 && url_list.size() > 0)
                            current_url--;
                        editor_pane.setPage((URL) url_list
                                .elementAt(current_url));
                        enablingManager();
                        } catch (Exception ex) {
      AgnaLog.warn("suppressed exception", ex);
      }
                    }

                // forward button listener:
                if (e.getSource() == b_forward)
                    {
                    try
                        {
                        current_url++;
                        editor_pane.setPage((URL) url_list
                                .elementAt(current_url));
                        enablingManager();
                        } catch (Exception ex) {
      AgnaLog.warn("suppressed exception", ex);
      }
                    }

                // close button listener:
                if (e.getSource() == b_close)
                    {
                    n_frame.dispose();
                    n_frame = null;
                    }

                }
        };

    private void enablingManager()
        {
        // JOptionPane.showMessageDialog(null, String.valueOf(current_url) +
        // "\n" + String.valueOf(url_list.size()), "Test",
        // JOptionPane.INFORMATION_MESSAGE);
        if (url_list.size() <= 1)
            {
            b_forward.setEnabled(false);
            b_back.setEnabled(false);
            }

        else if (current_url == 0)
            {
            b_forward.setEnabled(true);
            b_back.setEnabled(false);
            }

        else if (current_url == url_list.size() - 1)
            {
            b_forward.setEnabled(false);
            b_back.setEnabled(true);
            }

        else
            {
            b_forward.setEnabled(true);
            b_back.setEnabled(true);
            }

        }

    public HelpDialog(JFrame where)
        {
        Dimension frame_dim = new Dimension(500, 350);
        // Dimension panel_dim = new Dimension(150, 70);
        n_frame = new AgnaDialog(where, "Agna Help", false);
        n_frame.setTitle("Agna Help");
        // 2.1.3: the help window is resizable
        n_frame.setResizable(true);
        n_frame.setModal(false);

        Container content = n_frame.getContentPane();
        content.setLayout(new BorderLayout());

        JPanel control = new JPanel();
        // control.setBorder(BorderFactory.createRaisedBevelBorder());
        control.setPreferredSize(frame_dim);
        // text editor:
        editor_pane = new JEditorPane();
        editor_pane.setEditable(false);
        editor_pane.addHyperlinkListener(act_link_listener);
        editor_pane.setPreferredSize(frame_dim);
        JScrollPane scroll_pane = new JScrollPane(editor_pane);
        scroll_pane.setPreferredSize(frame_dim);
        try
            {
            editor_pane.setPage(HelpDialog.class
                    .getResource("/help/help_contents.htm"));
            } catch (Exception e)
            {
            editor_pane.setText("Help Contents file not found.");
            }
        url_list = new Vector(1, 1); // list of urls
        url_list.removeAllElements();
        url_list.addElement(editor_pane.getPage());
        current_url = 0; // index of current url
        // scroll_pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // scroll_pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JToolBar button_panel = new JToolBar();
        button_panel.setFloatable(false);
        button_panel.setPreferredSize(new Dimension(frame_dim.width, 30));

        Dimension button_dim = new Dimension(84, 30);

        // placing contents button:
        b_contents = new JButton("Contents");
        b_contents.setPreferredSize(button_dim);
        b_contents.setMaximumSize(button_dim);
        b_contents.addActionListener(act_dialog);
        b_contents.setToolTipText("Main topics");
        b_contents.setBorder(null);
        button_panel.add(b_contents);

        /*
         * // placing index button: b_index = new JButton("Index");
         * b_index.setPreferredSize(button_dim);
         * b_index.setMaximumSize(button_dim);
         * b_index.addActionListener(act_dialog); b_index.setBorder(null);
         * button_panel.add(b_index);
         */

// back/forward button
        b_back = new JButton("Back", Environment
                .getButtonImageIcon("leftArrow.gif"));
        b_forward = new JButton("Forward", Environment
                .getButtonImageIcon("rightArrow.gif"));
        b_back.addActionListener(act_dialog);
        b_forward.addActionListener(act_dialog);
        b_back.setPreferredSize(button_dim);
        b_back.setMaximumSize(button_dim);
        b_forward.setPreferredSize(button_dim);
        b_forward.setMaximumSize(button_dim);
        b_forward.setEnabled(false);
        b_back.setEnabled(false);
        b_back.setToolTipText("Previous page");
        b_forward.setToolTipText("Next page");
        b_forward.setBorder(null);
        b_back.setBorder(null);
        button_panel.add(b_back);
        button_panel.add(b_forward);

        // placing close button:
        b_close = new JButton("Close");
        b_close.addActionListener(act_dialog);
        b_close.setPreferredSize(button_dim);
        b_close.setMaximumSize(button_dim);
        b_back.setToolTipText("Close this frame");
        b_close.setBorder(null);
        button_panel.add(b_close);

        // final remarks...
        control.add(BorderLayout.NORTH, button_panel);
        control.add(BorderLayout.CENTER, scroll_pane);
        control.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));
        content.add(BorderLayout.CENTER, control);

        // n_frame.setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);

        n_frame.pack();
        // placing n_frame in ceneter of screen:
        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        n_frame.setLocation(sdim.width / 2 - n_frame.getSize().width / 2,
                sdim.height / 2 - n_frame.getSize().height / 2);

        }

    public boolean hasFrame()
        {
        if (n_frame == null)
            return false;
        return true;
        }

    public void showDialog()
        {
        n_frame.show();
        }

    public void closeDialog()
        {
        n_frame.dispose();
        n_frame = null;
        }

    }