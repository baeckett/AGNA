package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Regression: the grid pushes raw String cells into the network
 * (MainFrame.updateNetwork). setObjectValue used to cast them to Float,
 * throwing a silent ClassCastException per cell and zeroing the whole
 * matrix on every update - a 2.1.2 data-loss bug.
 */
public class StringCellPushTest
    {
    @Test
    public void stringCellsAreParsedIntoValues()
        {
        Network net = new Network(3);
        net.setObjectValue("1.0", 0, 1);
        net.setObjectValue("0.0", 1, 0);
        net.setObjectValue("2.5", 2, 2);
        assertEquals(1.0f, net.getValue(0, 1));
        assertEquals(0.0f, net.getValue(1, 0));
        assertEquals(0.0f, net.getValue(2, 2)); // diagonal stays untouched
        }

    @Test
    public void floatCellsStillWork()
        {
        Network net = new Network(2);
        net.setObjectValue(new Float(3.2f), 0, 1);
        assertEquals(3.2f, net.getValue(0, 1));
        }

    @Test
    public void nonNumericStringLeavesCellUnchanged()
        {
        Network net = new Network(2);
        net.setValue(5f, 0, 1);
        net.setObjectValue("not-a-number", 0, 1);
        assertEquals(5f, net.getValue(0, 1));
        }
    }