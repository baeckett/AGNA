/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

class IntListElement // element of an IntList
    {

    private int val;

    public IntListElement next;

    // constructor
    public IntListElement()
        {
        val = 0;
        }

    // constructor
    public IntListElement(int tmp_val)
        {
        val = tmp_val;
        next = null;
        }

    public int getValue()
        {
        return val;
        }

    public IntListElement getNext()
        {
        return this.next;
        }

    public void setValue(int tmp_val)
        {
        val = tmp_val;
        }
    }