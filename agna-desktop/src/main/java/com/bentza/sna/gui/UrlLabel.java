package com.bentza.sna.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.border.Border;

// starts a browser and opens up specified url;
// uses BrowserControl;
// works on windows, mac and unix/linux only.

class UrlLabel extends JButton
    {
    private String label_url;

    private boolean accepted_platform; // true if windows, mac or unix/linux

    private ActionListener act_click = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                BrowserControl.displayURL(label_url);
                }
        };

    // visit_me is the url string
    public UrlLabel(String visit_me)
        {
        label_url = visit_me;
        accepted_platform = BrowserControl.isAcceptedPlatform();
        this.setBorder(null);
        if (label_url != null && label_url != "")
            {
            this
                    .setText("<html><font size = 2 color='#FF0000' face='Arial,Helvetica,Verdana,sans-serif'><a href='"
                            + label_url + "'>" + label_url + "</a>");
            if (accepted_platform)
                {
                this.setToolTipText("Click to visit Agna Homepage");
                this.addActionListener(act_click);
                }
            }
        }

    public UrlLabel()
        {
        label_url = null;
        accepted_platform = BrowserControl.isAcceptedPlatform();
        this.setBorder(null);
        }

    // visit_me is the url string
    public void setUrl(String visit_me)
        {
        label_url = visit_me;
        if (label_url != null && label_url != "")
            {
            this
                    .setText("<html><font size = 2 color='#FF0000' face='Arial,Helvetica,Verdana,sans-serif'><a href='"
                            + label_url + "'>" + label_url + "</a>");
            if (accepted_platform)
                this.addActionListener(act_click);
            }
        }
    }