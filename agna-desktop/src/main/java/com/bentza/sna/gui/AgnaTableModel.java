/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import java.awt.Dimension;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class AgnaTableModel extends DefaultTableModel
        implements com.bentza.sna.core.AgnaRowModel
    {
    private int n;

    private boolean ready;

    private JTable my_table;

    public int getGridSize()
        {
        return n;
        }

    public boolean isReady()
        {
        return ready;
        }

    public void setReady(boolean tmp_ready)
        {
        ready = tmp_ready;
        }

    public void setGrid(JTable tmp_grid)
        {
        my_table = tmp_grid; // accessed by reference
        }

    public void setGridSize(int tmp_n)
        {
        n = tmp_n;
        }

    public void deleteDiagonal()
        {
        n = this.getColumnCount();

        for (int i = 0; i < n; i++)
            {
            if (Float.parseFloat((String) getValueAt(i, i)) != 0f)
                {
                setValueAt("0.0", i, i);
                }
            }
        }

    public void clearAll()
        {
        int n = this.getColumnCount();

        for (int i = n - 1; i >= 0; i--)
            {
            fireTableRowsDeleted(i, i);
            }
        }

/**
     * Deletes row and column i from the grid model.
     * 
     * <p>
     * Fixed in 2.1.3: removing a column from the middle of the grid used to
     * drop the LAST data column (DefaultTableModel.setColumnCount trims from
     * the end) while the intended column stayed in the model data, desyncing
     * the grid from the network and leaving stray zeros after two deletions.
     * The middle column is now removed from every row's data vector and from
     * the column identifiers before the count is adjusted.
     */
    public void delRowCol(int i)
        {
        n = this.getColumnCount();
        if (i < 0 || i >= n)
            return;

        super.removeRow(i);

        // remove the model-data column i from every row (view-only removal is
        // not enough: DefaultTableModel data rows are plain vectors, and
        // setColumnCount alone trims the LAST column instead)
        Vector data = getDataVector();
        for (int r = 0; r < data.size(); r++)
            {
            Vector row = (Vector) data.elementAt(r);
            if (row != null && i < row.size())
                {
                row.remove(i);
                }
            }
        if (columnIdentifiers != null && i < columnIdentifiers.size())
            {
            columnIdentifiers.remove(i);
            }

        // fires a structure change; with the data and the identifiers already
        // n-1 long the JTable rebuilds its columns from the identifiers and
        // view and model stay aligned
        this.setColumnCount(n - 1);

        fireTableRowsDeleted(i, i);

        my_table.setPreferredSize(new Dimension(MainFrame.col_width
                * my_table.getColumnCount(), my_table.getRowHeight()
                * my_table.getRowCount()));
        }

    public void addRowCol()
        {
        super.addColumn(new TableColumn(n));
        MainFrame.setTableCellEditor();
        my_table.validate();
        Vector row_data = new Vector(n + 1);
        super.addRow(row_data);
        fireTableRowsInserted(n, n);
        n = this.getColumnCount();
        for (int j = 0; j < n; j++)
            {
            super.setValueAt("0.0", n - 1, j);
            super.setValueAt("0.0", j, n - 1);
            }
        my_table.setPreferredSize(new Dimension(MainFrame.col_width
                * my_table.getColumnCount(), my_table.getRowHeight()
                * my_table.getRowCount()));
        
        }

    public void setValue(Object tmp_str, int i, int j)
        {
        super.setValueAt(String.valueOf(Float.parseFloat((String) tmp_str)), i,
                j);
        }

    public boolean isCellEditable(int row, int col)
        {
        if (row == col)
            return false;
        return true;
        }

    // constructor
    public AgnaTableModel(int tmp_n) // n: number of nodes
        {
        super(tmp_n, tmp_n);
        ready = false;
        setGridSize(tmp_n);

        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                super.setValueAt("0.0", i, j);
                }
            }
        addTableModelListener(agna_model_listener);
        }

TableModelListener agna_model_listener = new TableModelListener()
        {
            public void tableChanged(TableModelEvent e)
                {
                if (MainFrame.getCurrentFullNet() != null)
                    MainFrame.getCurrentFullNet().setChanged(true);
                if (!isReady())
                    return;

                int r = e.getLastRow();
                int c = e.getColumn();
                try
                    {
                    float value = Float.parseFloat((String) my_table
                            .getValueAt(r, c));
                    } catch (Exception ex)
                    {
                    float value = 0f;
                    String s_value = "k";
                    while (!MainFrame.isFloat(s_value) && s_value != null)
                        {
                        s_value = (String) JOptionPane.showInputDialog(
                                MainFrame.getCurrentFrame(),
                                "Bad value encountered in grid cell!\nPlease enter new edge value from "
                                        + MainFrame.getNodeName(r) + " to "
                                        + MainFrame.getNodeName(c) + ":",
                                "Error parsing value",
                                JOptionPane.ERROR_MESSAGE, null, null, "0.0");
                        }
                    MainFrame.getCurrentFrame().repaint();
                    if (s_value == null)
                        s_value = "0.0";
                    setValue(s_value, r, c);
                    }

                }
        };
    }