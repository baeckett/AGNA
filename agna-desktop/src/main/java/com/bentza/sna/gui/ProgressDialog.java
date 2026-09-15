/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.Agna;
import com.bentza.sna.Environment;
import com.bentza.sna.gui.MainFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class ProgressDialog
    {
    private int progress_index;

    public ProgressDialog()
        {
        progress_index = -1;
        }

    public ProgressDialog(String tmp_message)
        {
        progress_index = -1;
        }


    /**
     * starts the progress dialog; in specified frame
     */
    public void startPane(String tmp_title, String tmp_message)
        {
        progress_index = 0;
        }


    public void setPercent(int tmp_percent)
        {
        progress_index = tmp_percent;
        }

    public int getPercent()
        {
        return progress_index;
        }

    public boolean getStop() // should we stop running?
        {
        if (progress_index < 0)
            return true;
        else
            return false;
        }

    public boolean getStart() // is it safe to start?
        {
        return true;
        }

    }