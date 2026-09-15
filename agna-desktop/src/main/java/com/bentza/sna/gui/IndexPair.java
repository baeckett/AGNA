/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

class IndexPair
    {
    public int first_index, second_index;

    public IndexPair()
        {
        first_index = -1;
        second_index = -1;
        }

    public IndexPair(int tmp_first, int tmp_second)
        {
        first_index = tmp_first;
        second_index = tmp_second;
        }

    }