package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.net.Network;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

class AgnaVerticalHeader extends JPanel
    {

    public AgnaVerticalHeader()
        {
        }

    /*
     * public int XXgetIndex(VerticalHeaderButton tmp_button) { int n =
     * this.getComponentCount(); int i = 0; while (tmp_button !=
     * (VerticalHeaderButton)this.getComponent(i)) { i++; if (i >= n) { return
     * -1; } } return i; } public String XXgetName(int tmp_i) { try {
     * VerticalHeaderButton i_button =
     * (VerticalHeaderButton)this.getComponent(tmp_i); return
     * i_button.getText(); } catch(Exception e) { return null; } } public
     * String[] XXgetNames() { int n = this.getComponentCount(); String[] names =
     * new String[n]; VerticalHeaderButton i_button = null; for (int i = 0; i <
     * n; i++) { try { i_button = (VerticalHeaderButton)this.getComponent(i);
     * names[i] = i_button.getText(); } catch(Exception e) { return null; } }
     * return names; } public void setName(String tmp_name, int tmp_index) {
     * VerticalHeaderButton i_button = null; try { i_button =
     * (VerticalHeaderButton)this.getComponent(tmp_index);
     * i_button.setText(tmp_name); i_button.setToolTipText(tmp_name); }
     * catch(Exception e) {} }
     */

    public void updateFace(int tmp_index)
        {
        try
            {
            // final Image tmp_image = tmp_icon.getImage().getScaledInstance(8,
            // -1, Image.SCALE_FAST);
            VerticalHeaderButton i_button = (VerticalHeaderButton) this
                    .getComponent(tmp_index);
            i_button.setIcon(MainFrame.getCurrentNetwork().getActor(tmp_index)
                    .getSmallFace());
            /*
             * final Dimension i_dim = (
             * (VerticalHeaderButton)this.getComponent(tmp_index)
             * ).getPreferredSize(); this.remove(tmp_index);
             * VerticalHeaderButton i_button =
             * HeaderButtonFactory.requestButton(
             * MainFrame.getCurrentNetwork().getActor(tmp_index).getSmallFace(),
             * i_dim ); this.add(i_button, tmp_index);
             */
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        }

    public void setAllFaces(ImageIcon tmp_icon)
        {
        int n = this.getComponentCount();
        VerticalHeaderButton i_button = (VerticalHeaderButton) this
                .getComponent(0);
        final Dimension i_dim = i_button.getPreferredSize();

        /*
         * try { tmp_icon = new ImageIcon(
         * tmp_icon.getImage().getScaledInstance(8, -1, Image.SCALE_FAST) ); }
         * catch(Exception e) { }
         */
        this.removeAll();
        final Network tmp_network = MainFrame.getCurrentNetwork();
        for (int i = 0; i < n; i++)
            {
            i_button = HeaderButtonFactory.requestButton(tmp_network
                    .getActor(i).getSmallFace(), i_dim);
            this.add(i_button);
            }
        }

    public void setHeader()
        {
        final Dimension i_dim = new Dimension((int) (MainFrame.col_width * 1),
                MainFrame.getCurrentTable().getRowHeight());
        this.removeAll();
        // JButton[] buttons = new JButton[n];
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final Network tmp_network = MainFrame.getCurrentNetwork();
        final int n = tmp_network.getSize();
        Image tmp_image = null;
        for (int i = 0; i < n; i++)
            {
            /*
             * try { tmp_image =
             * tmp_network.getActor(i).getFace().getImage().getScaledInstance(8,
             * -1, Image.SCALE_FAST); } catch(Exception e) {}
             */
            this.add(HeaderButtonFactory.requestButton(tmp_network.getActor(i)
                    .getSmallFace(), i_dim));

            }
        this.setPreferredSize(new Dimension(i_dim.width, n * i_dim.height));
        }
    }