/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import java.util.Vector;

class IntList // ordered list of integers;
// uses IntListElement;
// has minimum one element;
    {
    private IntListElement first;

    // new empty list
    public IntList()
        {
        first = null;
        }

    // new list with one element
    public IntList(int tmp_val)
        {
        first = new IntListElement(tmp_val);
        }

    // new list with two elements
    public IntList(int tmp_first, int tmp_second)
        {
        first = new IntListElement(tmp_first);
        first.next = new IntListElement(tmp_second);
        }

    // new list from array
    public IntList(int[] tmp_array)
        {
        for (int i = 0; i < tmp_array.length; i++)
            {
            this.appendValue(tmp_array[i]);
            }
        }

    public boolean matches(IntList tmp_list)
        {
        if (this.first == null && tmp_list.getFirstElement() == null)
            {
            return true;
            }

        IntListElement cursor1 = this.first;
        IntListElement cursor2 = tmp_list.first;
        while (cursor1 != null && cursor2 != null)
            {
            if (cursor1.getValue() != cursor2.getValue())
                return false; // different value encountered
            cursor1 = cursor1.next;
            cursor2 = cursor2.next;
            }
        if (cursor1 != null || cursor2 != null)
            return false; // different size

        return true;
        }

        public IntList getClone()
        {
        if (this.first == null)
            {
            // 2.1.3: an empty list clones to an empty list, not null
            return new IntList();
            }
        IntList clone = new IntList();
        IntListElement cursor = this.first;
        while (cursor != null)
            {
            clone.appendValue(cursor.getValue());
            cursor = cursor.next;
            }
        return clone;
        }

        public int[] toArray()
        {
        if (first == null)
            return new int[0];
        int n = this.getSize();
        int[] values = new int[n];
        IntListElement cursor = this.first;
        int i = 0;
        // 2.1.3: the old loop stopped before the last element
        while (cursor != null && i < n)
            {
            values[i] = cursor.getValue();
            i++;
            cursor = cursor.next;
            }
        return values;
        }

    // returns the size of the list
    public int getSize()
        {
        if (first == null)
            return 0;
        int i = 0;
        IntListElement cursor = first;
        while (cursor.next != null)
            {
            i++;
            cursor = cursor.next;
            }
        return i + 1;
        }

    private Vector getIntListElementVector()
        {
        if (first == null)
            return null;
        int i = 0;
        Vector finvector = new Vector(1);
        IntListElement cursor = first;
        finvector.addElement(cursor);
        while (cursor.next != null)
            {
            i++;
            cursor = cursor.next;
            finvector.addElement(cursor);
            }
        return finvector;
        }

    public void inserElementBefore(int tmp_i, int tmp_val)
        {
        if (first == null)
            {
            appendValue(tmp_val);
            return;
            }
        if (tmp_i == 0)
            {
            IntListElement new_element = new IntListElement(tmp_val);
            new_element.next = this.first;
            this.first = new_element;
            return;
            }
        IntListElement new_element = new IntListElement(tmp_val);
        IntListElement left = this.getElementAt(tmp_i - 1);
        IntListElement right = left.next;
        left.next = new_element;
        if (right == null) // no element at tmp_i
            {
            return;
            }
        new_element.next = right;
        }

    public void deleteAll()
        {
        if (first == null)
            return;
        first = null;
        
        

        }

    public void deleteElementAt(int tmp_i)
        {
        if (first == null)
            return;
        if (tmp_i == 0)
            {
            if (first.next == null)
                {
                first = null;
                return;
                }
            IntListElement new_first = first.next;
            first = null;
            first = new_first;
            return;
            }
        IntListElement left = this.getElementAt(tmp_i - 1);
        IntListElement right = left.next.next;
        left.next = null;
        left.next = right;
        }

    // returns the index of the first element
    // whose value is tmp_val
    public int getIndexOf(int tmp_val)
        {
        if (first == null)
            return -1;
        int i = 0;
        IntListElement cursor = first;
        while (cursor.next != null)
            {
            if (cursor.getValue() == tmp_val)
                {
                return i;
                }
            i++;
            cursor = cursor.next;
            }
        return -1;
        }

    public IntListElement getElementAt(int tmp_i)
        {
        if (first == null)
            return null;
        int i = 0;
        IntListElement cursor = first;
        while (cursor.next != null && i < tmp_i)
            {
            i++;
            cursor = cursor.next;
            }
        if (i == tmp_i)
            return cursor;
        else
            return null; // no element at position tmp_i
        }

    public IntListElement getLastElement()
        {
        if (first == null)
            return null;
        IntListElement cursor = first;
        while (cursor.next != null)
            {
            cursor = cursor.next;
            }
        return cursor;
        }

    public IntListElement getFirstElement()
        {
        return first;
        }

    // appends a new element whose value is tmp_val:
    public void appendValue(int tmp_val)
        {
        if (first == null)
            {
            first = new IntListElement(tmp_val);
            return;
            }
        getLastElement().next = new IntListElement(tmp_val);
        }
    }