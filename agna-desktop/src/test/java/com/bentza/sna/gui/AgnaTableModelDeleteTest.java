package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.JTable;

import org.junit.jupiter.api.Test;

/**
 * Verifies the grid-model deletion fix: delRowCol must remove the MIDDLE
 * column from the model data (the 2.1.2 code trimmed the last column instead,
 * desyncing the grid from the network and leaving stray zeros).
 */
public class AgnaTableModelDeleteTest
    {
    @Test
    public void delRowColRemovesMiddleColumnFromData()
        {
        AgnaTableModel model = new AgnaTableModel(4);
        JTable table = new JTable(model);
        model.setGrid(table);

        // fill a distinct pattern so every cell is identifiable:
        // 10 11 12 13
        // 20 21 22 23
        // 30 31 32 33
        // 40 41 42 43
        for (int i = 0; i < 4; i++)
            {
            for (int j = 0; j < 4; j++)
                {
                model.setValueAt(String.valueOf((i + 1) * 10 + j), i, j);
                }
            }

        model.delRowCol(1);

        assertEquals(3, model.getRowCount());
        assertEquals(3, model.getColumnCount());
        // row 0: the middle column (11) is gone, row now 10,12,13
        assertEquals("10", model.getValueAt(0, 0));
        assertEquals("12", model.getValueAt(0, 1));
        assertEquals("13", model.getValueAt(0, 2));
        // row 2 (the "20" row was deleted): 40,42,43
        assertEquals("40", model.getValueAt(2, 0));
        assertEquals("42", model.getValueAt(2, 1));
        assertEquals("43", model.getValueAt(2, 2));
        }

    @Test
    public void delRowColTwiceKeepsDataAligned()
        {
        AgnaTableModel model = new AgnaTableModel(4);
        JTable table = new JTable(model);
        model.setGrid(table);

        for (int i = 0; i < 4; i++)
            {
            for (int j = 0; j < 4; j++)
                {
                model.setValueAt(String.valueOf((i + 1) * 10 + j), i, j);
                }
            }

        model.delRowCol(1);
        model.delRowCol(1);

        assertEquals(2, model.getRowCount());
        assertEquals(2, model.getColumnCount());
        // after removing column 1 twice (rows 0 and 1 deleted): "10,13" / "40,43"
        assertEquals("10", model.getValueAt(0, 0));
        assertEquals("13", model.getValueAt(0, 1));
        assertEquals("40", model.getValueAt(1, 0));
        assertEquals("43", model.getValueAt(1, 1));
        }
    }