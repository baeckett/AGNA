package com.bentza.sna.gui;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;

/**
 * A visual component displayed in the AgnaVerticalHeader; allows the user to
 * change an actor's name; implemented as a lightweight component.
 */
public class VerticalHeaderButton extends JButton
    {

    /**
     * Constructor: sets this button's properties.
     */
    public VerticalHeaderButton(ImageIcon tmp_icon, Dimension tmp_dim)
        {
        this.setMinimumSize(tmp_dim);
        this.setPreferredSize(tmp_dim);
        this.setMaximumSize(tmp_dim);
        this.setHorizontalAlignment(SwingConstants.LEFT);
        this.setVerticalAlignment(SwingConstants.CENTER);
        this.setMargin(new Insets(0, 0, 1, 0));
        // this.setToolTipText(names[i]);
        this.addActionListener(act_button);
        this.setIcon(tmp_icon);
        }

    /**
     * Overriding the method to customize the button. Trying to identify the
     * position of this button on the panel in order to infer actor name.
     */
    public String getText()
        {
        final String actor_name = MainFrame.getCurrentNetwork().getActorName(
                this.getIndex());
        this.setToolTipText(actor_name);
        return actor_name;
        }

    /**
     * Returns the (index) position of this button on the panel.
     */
    public int getIndex()
        {
        // assuming this is the height of every
        // button on panel:
        final int h = this.getHeight();
        if (h == 0)
            return 0; // false return!
        // coordinate of first button on the panel:
        final int min_y = this.getParent().getComponent(0).getY();
        // index (position) of this button on panel:
        return (int) ((float) (this.getY() - min_y) / h);
        }

    /*
     * public String getToolTipText() { return super.getText(); }
     */

    // buttons's actionlistener:
    private ActionListener act_button = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                // create new network
                if (e.getSource() instanceof VerticalHeaderButton)
                    {
                    String tmp_str, tmp_name;
                    final int tmp_index = getIndex();
                    // VerticalHeaderButton b =
                    // (VerticalHeaderButton)e.getSource();
                    tmp_name = MainFrame.getCurrentNetwork().getActorName(
                            tmp_index);
                    tmp_str = (String) JOptionPane.showInputDialog(MainFrame
                            .getCurrentFrame(), "Enter a new name for node [ "
                            + tmp_name + " ]:", "Node name",
                            JOptionPane.QUESTION_MESSAGE, null, null, tmp_name);
                    if (tmp_str != null)
                        {
                        // int tmp_index = getIndex(b);
                        MainFrame.setNodeName(tmp_str, tmp_index);
                        }
                    }
                }
        };

    } // end of class
