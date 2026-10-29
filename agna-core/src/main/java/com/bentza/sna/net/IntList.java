/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

public class IntList implements Iterable<Integer>
    {
    private IntListElement first;
    private IntListElement last;
    private int size;

    // new empty list
    public IntList()
        {
        first = null;
        last = null;
        size = 0;
        }

    // new list with one element
    public IntList(int tmp_val)
        {
        this();
        appendValue(tmp_val);
        }

    // new list with two elements
    public IntList(int tmp_first, int tmp_second)
        {
        this();
        appendValue(tmp_first);
        appendValue(tmp_second);
        }

    // new list from array
    public IntList(int[] tmp_array)
        {
        this();
        if (tmp_array != null)
            {
            for (int i = 0; i < tmp_array.length; i++)
                {
                this.appendValue(tmp_array[i]);
                }
            }
        }

    public boolean matches(IntList tmp_list)
        {
        if (tmp_list == null)
            return false;
        if (this.size != tmp_list.size)
            return false;
        if (this.first == null && tmp_list.first == null)
            return true;

        IntListElement cursor1 = this.first;
        IntListElement cursor2 = tmp_list.first;
        while (cursor1 != null && cursor2 != null)
            {
            if (cursor1.getValue() != cursor2.getValue())
                return false;
            cursor1 = cursor1.next;
            cursor2 = cursor2.next;
            }
        return cursor1 == null && cursor2 == null;
        }

    public IntList getClone()
        {
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
        if (first == null || size == 0)
            return new int[0];
        int[] values = new int[size];
        IntListElement cursor = this.first;
        int i = 0;
        while (cursor != null && i < size)
            {
            values[i++] = cursor.getValue();
            cursor = cursor.next;
            }
        return values;
        }

    // returns the size of the list in O(1) time
    public int getSize()
        {
        return size;
        }

    private Vector getIntListElementVector()
        {
        if (first == null)
            return null;
        Vector finvector = new Vector(Math.max(1, size));
        IntListElement cursor = first;
        while (cursor != null)
            {
            finvector.addElement(cursor);
            cursor = cursor.next;
            }
        return finvector;
        }

    public void inserElementBefore(int tmp_i, int tmp_val)
        {
        if (first == null || tmp_i <= 0)
            {
            if (first == null)
                {
                appendValue(tmp_val);
                return;
                }
            IntListElement new_element = new IntListElement(tmp_val);
            new_element.next = this.first;
            this.first = new_element;
            size++;
            return;
            }
        IntListElement left = this.getElementAt(tmp_i - 1);
        if (left == null)
            {
            appendValue(tmp_val);
            return;
            }
        IntListElement new_element = new IntListElement(tmp_val);
        new_element.next = left.next;
        left.next = new_element;
        if (new_element.next == null)
            last = new_element;
        size++;
        }

    public void deleteAll()
        {
        first = null;
        last = null;
        size = 0;
        }

    public void deleteElementAt(int tmp_i)
        {
        if (first == null || tmp_i < 0 || tmp_i >= size)
            return;
        if (tmp_i == 0)
            {
            first = first.next;
            if (first == null)
                last = null;
            size--;
            return;
            }
        IntListElement left = this.getElementAt(tmp_i - 1);
        if (left == null || left.next == null)
            return;
        left.next = left.next.next;
        if (left.next == null)
            last = left;
        size--;
        }

    // returns the index of the first element whose value is tmp_val
    public int getIndexOf(int tmp_val)
        {
        if (first == null)
            return -1;
        int i = 0;
        IntListElement cursor = first;
        while (cursor != null)
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
        if (first == null || tmp_i < 0 || tmp_i >= size)
            return null;
        if (tmp_i == size - 1)
            return last;
        int i = 0;
        IntListElement cursor = first;
        while (cursor != null && i < tmp_i)
            {
            i++;
            cursor = cursor.next;
            }
        return cursor;
        }

    public IntListElement getLastElement()
        {
        return last;
        }

    public IntListElement getFirstElement()
        {
        return first;
        }

    // appends a new element whose value is tmp_val in O(1) time
    public void appendValue(int tmp_val)
        {
        IntListElement el = new IntListElement(tmp_val);
        if (first == null)
            {
            first = el;
            last = el;
            } else
            {
            last.next = el;
            last = el;
            }
        size++;
        }

    @Override
    public Iterator<Integer> iterator()
        {
        return new Iterator<Integer>()
            {
            private IntListElement cursor = first;

            @Override
            public boolean hasNext()
                {
                return cursor != null;
                }

            @Override
            public Integer next()
                {
                if (cursor == null)
                    throw new NoSuchElementException();
                int val = cursor.getValue();
                cursor = cursor.next;
                return val;
                }
            };
        }
    }
