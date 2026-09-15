/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.net.Actor;
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

/**
 * A JButton with a reference to an associate Actor; used as a component of
 * VerticalToolBar;
 */
public class IndexedJButton extends JButton implements MouseListener,
        MouseMotionListener
    {
    /**
     * a reference to this's associate Actor
     */
    private Actor associate;

    /**
     * Creates a new IndexedJButton from specified actor and dimension
     */
    public IndexedJButton(Actor tmp_actor, Dimension tmp_dimension)
        {
        associate = tmp_actor;
        this.setText(tmp_actor.getName());
        this.setContentAreaFilled(false);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        if (tmp_dimension != null)
            {
            this.setMinimumSize(tmp_dimension);
            this.setPreferredSize(tmp_dimension);
            }
        this.setHorizontalAlignment(SwingConstants.LEFT);
        this.setMargin(new Insets(0, 0, 1, 0));
        this.setBorder(null);
        /*
         * Image tmp_image; try { tmp_image =
         * tmp_actor.getFace().getImage().getScaledInstance(8, -1,
         * Image.SCALE_FAST); this.setIcon(new ImageIcon(tmp_image)); }
         * catch (Exception e) { AgnaLog.warn("suppressed exception", e); }
         */
        }

    /**
     * Overrides the original method to make possible updating of button
     */
    public Icon getIcon()
        {
        /*
         * if ( !MainFrame.getCurrentFullNet().getChanged() ) return
         * super.getIcon();
         */
        try
            {
            Image tmp_image = associate.getFace().getImage().getScaledInstance(
                    8, -1, Image.SCALE_FAST);
            return new ImageIcon(tmp_image);
            } catch (Exception e)
            {
            return null;
            }
        }

    /**
     * Overrides the original method to make possible updating of button / /*
     * public String getText() { return getToolTipText(); try { return
     * associate.getName(); } catch(Exception e) { return "No name"; } }
     */

    /**
     * Overrides the original method to make possible updating the button
     */
    public String getToolTipText()
        {
        try
            {
            return associate.getName();
            } catch (NullPointerException e)
            {
            return null;
            }
        }

    public Actor getAssociateActor()
        {
        return associate;
        }

    /**
     * Returns the index of the associate Actor in current Network; -1 if
     * (accidentally) associate is null
     */
    public int getActorIndex()
        {
        if (associate == null)
            return -1;
        return MainFrame.getCurrentNetwork().getActorIndex(associate);
        }

    public void mousePressed(MouseEvent e)
        {
        if (associate == null)
            return;
        GrNet.doSelectActor(this.getActorIndex());
        }

    public void mouseEntered(MouseEvent e)
        {
        }

    public void mouseExited(MouseEvent e)
        {
        }

    /**
     * shows up node settings dialog on double-click
     */
    public void mouseClicked(MouseEvent e)
        {
        if (associate == null)
            return;
        MainFrame.getCurrentArea().mouseClicked(e);
        }

    public void mouseReleased(MouseEvent e)
        {
        }

    public void mouseDragged(MouseEvent e)
        {
        }

    public void mouseMoved(MouseEvent e)
        {
        }

    /**
     * Overriding this method to avoid painting a button when its associate has
     * disappeared
     */
    public void paintComponent(Graphics g)
        {
        super.paintComponent(g);
        if (associate == null || associate.getName() == null)
            {
            // suicide:
            GrNet.getCurrentVerticalToolBar().removeIndexedJButton(this);
            } else
            {
            // updating name:
            this.setText(associate.getName());
            }
        }

    }