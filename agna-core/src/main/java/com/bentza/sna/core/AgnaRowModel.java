/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.core;

/**
 * 2.1.3: the row operations the engine needs from the grid currently
 * displayed. The desktop table model implements this; headless
 * consumers (CLI, tests) can provide a no-op implementation.
 */
public interface AgnaRowModel
    {
    /** Removes the given row and column (index) from the grid. */
    void delRowCol(int index);
    }