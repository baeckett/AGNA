package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Verifies the clipboard-paste fix (2.1.2 "copy/paste matrix data into agna,
 * one column missing!"): parsing must preserve empty cells and normalize CRLF
 * line endings, so sparse rows keep their columns.
 */
public class ExcelAdapterPasteTest
    {
    @Test
    public void emptyCellInMiddleKeepsColumn()
        {
        // "4\t\t6": the empty middle cell is the symptom of the original bug
        String[][] grid = ExcelAdapter.parsePastedText("1\t2\t3\n4\t\t6\n7\t8\t9\n");

        assertEquals(3, grid.length);
        assertArrayEquals(new String[] { "1", "2", "3" }, grid[0]);
        assertArrayEquals(new String[] { "4", "", "6" }, grid[1]);
        assertArrayEquals(new String[] { "7", "8", "9" }, grid[2]);
        }

    @Test
    public void crlfLineEndingsAreNormalized()
        {
        String[][] grid = ExcelAdapter
                .parsePastedText("1\t2\t3\r\n4\t5\t6\r\n");

        assertEquals(2, grid.length);
        assertArrayEquals(new String[] { "1", "2", "3" }, grid[0]);
        assertArrayEquals(new String[] { "4", "5", "6" }, grid[1]);
        }

    @Test
    public void trailingTabDoesNotAddACell()
        {
        String[][] grid = ExcelAdapter.parsePastedText("1\t2\t\n3\t4\t\n");

        assertEquals(2, grid.length);
        assertArrayEquals(new String[] { "1", "2" }, grid[0]);
        assertArrayEquals(new String[] { "3", "4" }, grid[1]);
        }

    @Test
    public void trailingNewlineDoesNotAddARow()
        {
        String[][] grid = ExcelAdapter.parsePastedText("1\t2\t3\n4\t5\t6\n");

        assertEquals(2, grid.length);
        }

    @Test
    public void nullTextYieldsEmptyGrid()
        {
        assertEquals(0, ExcelAdapter.parsePastedText(null).length);
        }
    }