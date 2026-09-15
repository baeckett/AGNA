/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import javax.swing.JComponent;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 * Modified version of the original class written by: Ashok Banerjee
 * (ashok.banerjee@javaworld.com) and Jignesh Mehta
 * (jignesh.mehta@javaworld.com)
 */
class ExcelAdapter implements ActionListener
    {
    private String rowstring, value;

    private Clipboard system;

    private StringSelection stsel;

    private JTable adapter_table;

    /**
     * The Excel Adapter is constructed with a JTable on which it enables
     * Copy-Paste and acts as a Clipboard listener.
     */

    public ExcelAdapter(JTable myJTable)
        {
        adapter_table = myJTable;
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                ActionEvent.CTRL_MASK, false);
        KeyStroke edit = KeyStroke.getKeyStroke(KeyEvent.VK_K,
                ActionEvent.CTRL_MASK, false);
        KeyStroke select_all = KeyStroke.getKeyStroke(KeyEvent.VK_A,
                ActionEvent.CTRL_MASK, false);

        // Identifying the copy KeyStroke user can modify this
        // to copy on some other Key combination.
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                ActionEvent.CTRL_MASK, false);

        // Identifying the Paste KeyStroke user can modify this
        // to copy on some other Key combination.

        adapter_table.registerKeyboardAction(this, "Copy", copy,
                JComponent.WHEN_FOCUSED);

        adapter_table.registerKeyboardAction(this, "Edit", edit,
                JComponent.WHEN_FOCUSED);

        adapter_table.registerKeyboardAction(this, "Select All", select_all,
                JComponent.WHEN_FOCUSED);

        adapter_table.registerKeyboardAction(this, "Paste", paste,
                JComponent.WHEN_FOCUSED);

        system = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

    /**
     * Public Accessor methods for the Table on which this adapter acts.
     */
    public JTable getJTable()
        {
        return adapter_table;
        }

    public void setJTable(JTable adapter_table)
        {
        this.adapter_table = adapter_table;
        }

    /**
     * This method is activated on the Keystrokes we are listening to in this
     * implementation. Here it listens for Copy and Paste ActionCommands.
     * Selections comprising non-adjacent cells result in invalid selection and
     * then copy action cannot be performed. Paste is done by aligning the upper
     * left corner of the selection with the 1st element in the current
     * selection of the JTable.
     */
    public void actionPerformed(ActionEvent e)
        {
        if (e.getActionCommand().compareTo("Copy") == 0)
            {
            if (MainFrame.getCurrentOutputPane().hasFocus())
                {
                MainFrame.getCurrentOutputPane().copy();
                return;
                }
            StringBuffer sbf = new StringBuffer();

            // Check to ensure we have selected only a contiguous block of
            // cells
            int numcols = adapter_table.getSelectedColumnCount();
            int numrows = adapter_table.getSelectedRowCount();
            int[] rowsselected = adapter_table.getSelectedRows();
            int[] colsselected = adapter_table.getSelectedColumns();

            if (!((numrows - 1 == rowsselected[rowsselected.length - 1]
                    - rowsselected[0] && numrows == rowsselected.length) && (numcols - 1 == colsselected[colsselected.length - 1]
                    - colsselected[0] && numcols == colsselected.length)))
                {
                JOptionPane.showMessageDialog(MainFrame.getCurrentFrame(),
                        "Invalid Copy Selection", "Invalid Copy Selection",
                        JOptionPane.ERROR_MESSAGE);

                return;
                }

            for (int i = 0; i < numrows; i++)
                {
                for (int j = 0; j < numcols; j++)
                    {
                    sbf.append(adapter_table.getValueAt(rowsselected[i],
                            colsselected[j]));
                    if (j < numcols - 1)
                        sbf.append("\t");
                    }
                sbf.append("\n");
                }

            stsel = new StringSelection(sbf.toString());
            system = Toolkit.getDefaultToolkit().getSystemClipboard();
            system.setContents(stsel, stsel);
            }

        // ****************************************************

        if (e.getActionCommand().compareTo("Cut") == 0)
            {
            if (MainFrame.getCurrentOutputPane().hasFocus())
                {
                MainFrame.getCurrentOutputPane().cut();
                return;
                }

            StringBuffer sbf = new StringBuffer();

            // Check to ensure we have selected only a contiguous block of
            // cells
            int numcols = adapter_table.getSelectedColumnCount();
            int numrows = adapter_table.getSelectedRowCount();
            int[] rowsselected = adapter_table.getSelectedRows();
            int[] colsselected = adapter_table.getSelectedColumns();

            if (!((numrows - 1 == rowsselected[rowsselected.length - 1]
                    - rowsselected[0] && numrows == rowsselected.length) && (numcols - 1 == colsselected[colsselected.length - 1]
                    - colsselected[0] && numcols == colsselected.length)))
                {
                JOptionPane.showMessageDialog(MainFrame.getCurrentFrame(),
                        "Invalid Copy Selection", "Invalid Cut Selection",
                        JOptionPane.ERROR_MESSAGE);

                return;
                }

            MainFrame.getCurrentFullNet().setChanged(true);
            MainFrame.getCurrentTableModel().setReady(false);

            // starting copy & delete:
            for (int i = 0; i < numrows; i++)
                {
                for (int j = 0; j < numcols; j++)
                    {
                    sbf.append(adapter_table.getValueAt(rowsselected[i],
                            colsselected[j]));
                    adapter_table.setValueAt("0.0", rowsselected[i],
                            colsselected[j]);
                    if (j < numcols - 1)
                        sbf.append("\t");
                    }
                sbf.append("\n");
                }

            stsel = new StringSelection(sbf.toString());
            system = Toolkit.getDefaultToolkit().getSystemClipboard();
            system.setContents(stsel, stsel);

            MainFrame.getCurrentTableModel().setReady(true);

            }

        // ****************************************************
        if (e.getActionCommand().compareTo("Edit") == 0)
            {
            int row = adapter_table.getSelectedRow();
            int col = adapter_table.getSelectedColumn();
            adapter_table.editCellAt(row, col);
            }

        // ********************************
        if (e.getActionCommand().compareTo("Select All") == 0)
            {
            if (MainFrame.getCurrentOutputPane().hasFocus())
                {
                MainFrame.getCurrentOutputPane().selectAll();
                return;
                }

            adapter_table.selectAll();
            }

// ********************************

        if (e.getActionCommand().compareTo("Paste") == 0)
            {
            if (MainFrame.getCurrentOutputPane().hasFocus())
                {
                MainFrame.getCurrentOutputPane().paste();
                return;
                }

            int startRow = (adapter_table.getSelectedRows())[0];
            int startCol = (adapter_table.getSelectedColumns())[0];
            try
                {
                String trstring = (String) (system.getContents(this)
                        .getTransferData(DataFlavor.stringFlavor));

                MainFrame.getCurrentFullNet().setChanged(true);
                MainFrame.getCurrentTableModel().setReady(false);

                String[][] cells_grid = parsePastedText(trstring);
                for (int i = 0; i < cells_grid.length; i++)
                    {
                    String[] cells = cells_grid[i];
                    for (int j = 0; j < cells.length; j++)
                        {
                        // DIAGONAL MUST REMAIN UNCHANGED!
                        if (startRow + i < adapter_table.getRowCount()
                                && startCol + j < adapter_table
                                        .getColumnCount()
                                && startRow + i != startCol + j)
                            {
                            String value = cells[j];
                            if (value.length() == 0)
                                value = "0.0";
                            adapter_table.setValueAt(value, startRow + i,
                                    startCol + j);
                            }
                        }
                    }
                MainFrame.getCurrentTableModel().setReady(true);
                MainFrame.checkAllValues();
                } catch (Exception ex)
                {
                MainFrame.getCurrentTableModel().setReady(true);
                }

            }
        }

    /**
     * Parses tab-separated clipboard text into a grid of cells.
     * 
     * <p>
     * Fixed in 2.1.3: StringTokenizer used to silently skip empty cells
     * (consecutive tabs), so a row with one empty cell lost a column and every
     * following value shifted one cell left ("one column missing"). This
     * parser preserves empty cells; CRLF/CR line endings are normalized; a
     * trailing tab (or a final newline) does not produce extra cells.
     * 
     * @param text
     *            clipboard text, e.g. "1\t2\t3\r\n4\t\t6\r\n"
     * @return rows of cells; empty cells are kept as empty strings
     */
    static String[][] parsePastedText(String text)
        {
        if (text == null)
            return new String[0][0];

        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);

        // count real rows (a trailing newline produces one empty line)
        int row_count = lines.length;
        if (row_count > 0 && lines[row_count - 1].length() == 0)
            row_count--;

        String[][] grid = new String[row_count][];
        for (int i = 0; i < row_count; i++)
            {
            String[] cells = lines[i].split("\t", -1);
            // a trailing tab marks the end of a row, not an empty cell
            if (cells.length > 0 && cells[cells.length - 1].length() == 0)
                {
                String[] trimmed = new String[cells.length - 1];
                System.arraycopy(cells, 0, trimmed, 0, trimmed.length);
                cells = trimmed;
                }
            grid[i] = cells;
            }
        return grid;
        }
    }