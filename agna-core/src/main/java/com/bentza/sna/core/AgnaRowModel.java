package com.bentza.sna.core;

/**
 * 2.1.3: the rows-of-a-sociomatrix operations the engine needs from
 * whatever grid is showing. The desktop table model implements this;
 * headless consumers (CLI, tests) can provide a no-op implementation.
 */
public interface AgnaRowModel
    {
    /** Removes the given row and column (index) from the grid. */
    void delRowCol(int index);
    }