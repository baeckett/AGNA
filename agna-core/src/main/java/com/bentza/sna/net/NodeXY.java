/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

public class NodeXY
    {
    private float x, y;

    public NodeXY()
        {
        x = 10f + 80f * (float) Math.random();
        y = 10f + 80f * (float) Math.random();
        }

    public float getX()
        {
        return x;
        }

    public float getY()
        {
        return y;
        }

    public int getX(int x_max) // x_max is the width of the nodearea
        {
        if (x_max == 0f)
            return 1;
        return (int) ((float) x_max * x / 100f);
        }

    public int getY(int y_max) // y_max is the height of the nodearea
        {
        if (y_max == 0f)
            return 1;
        return (int) ((float) y_max * y / 100f);
        }

    public void setX(float tmp_x)
        {
        x = tmp_x;
        }

    public void setY(float tmp_y)
        {
        y = tmp_y;
        }

    public void setX(int tmp_x, int x_max)
        {
        x = (float) tmp_x * 100f / (float) x_max;
        }

    public void setY(int tmp_y, int y_max)
        {
        y = (float) tmp_y * 100f / (float) y_max;
        }

    }