package com.bentza.sna.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.JOptionPane;
import javax.swing.DefaultCellEditor;

class AgnaTable extends JTable
    {
    final Color selection_background_color = getSelectionBackgroundColor();

    final Color bad_selection_background_color = new Color(250, 50, 30, 100);

    final Color bad_background_color = new Color(250, 50, 30, 20);

    private Color getSelectionBackgroundColor()
        {
        // 2.1.3: under a light look and feel the button background is nearly
        // white, and the old translucent derivation made the selection
        // invisible. Light themes get a discreet light blue; darker themes
        // keep the tinted translucent look.
        Color sbc = (new JButton()).getBackground();
        float[] hsb = Color.RGBtoHSB(sbc.getRed(), sbc.getGreen(),
                sbc.getBlue(), null);
        if (hsb[2] > 0.75f)
            {
            return new Color(204, 224, 244);
            }
        return new Color(sbc.getRed(), sbc.getGreen(), sbc.getBlue(), 100);
        }

    public AgnaTable()
        {
        // enables identifying selected cell:
        getTableHeader().setReorderingAllowed(false);

        setToolTipText("Sociomatrix area");
        setAutoResizeMode(0);
        setCellSelectionEnabled(true);
        setSelectionMode(1);
        setSelectionBackground(selection_background_color);
        setSelectionForeground(Color.black);
        // 2.1.3: grid lines stay on under every look and feel (the
        // UIManager keys are set at look-and-feel install time)
        setShowGrid(true);
        // setSelectionForeground(Color.red);
        } // AgnaTable constructor


    // 2.1.3: some native look and feels (Aqua on macOS) never paint the
    // grid even when showGrid is on; draw the lines ourselves, on top of
    // whatever the look and feel painted, so the sociomatrix grid is
    // visible everywhere.
    protected void paintComponent(java.awt.Graphics g)
        {
        super.paintComponent(g);
        if (!getShowHorizontalLines() && !getShowVerticalLines())
            {
            return;
            }
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        try
            {
            java.awt.Rectangle clip = g.getClipBounds();
            g2.setColor(getGridColor());
            int firstRow = Math.max(0, rowAtPoint(clip.getLocation()));
            int lastRow = Math.min(getRowCount() - 1,
                    rowAtPoint(new java.awt.Point(clip.x,
                            clip.y + clip.height)));
            if (lastRow < firstRow)
                {
                lastRow = getRowCount() - 1;
                }
            int firstCol = Math.max(0, columnAtPoint(clip.getLocation()));
            int lastCol = Math.min(getColumnCount() - 1,
                    columnAtPoint(new java.awt.Point(clip.x + clip.width,
                            clip.y)));
            if (lastCol < firstCol)
                {
                lastCol = getColumnCount() - 1;
                }
            for (int r = firstRow; r <= lastRow; r++)
                {
                if (getShowHorizontalLines())
                    {
                    int y = getCellRect(r, 0, true).y;
                    g2.drawLine(clip.x, y, clip.x + clip.width, y);
                    }
                }
            for (int c = firstCol; c <= lastCol; c++)
                {
                if (getShowVerticalLines())
                    {
                    int x = getCellRect(0, c, true).x;
                    g2.drawLine(x, clip.y, x, clip.y + clip.height);
                    }
                }
            } finally
            {
            g2.dispose();
            }
        }

    public TableCellRenderer getCellRenderer(int row, int col)
        {
        Component c = (Component) super.getCellRenderer(row, col);

        if (row == col)
            {
            c.setBackground(bad_background_color);
            c.setForeground(Color.black);
            return (TableCellRenderer) c;
            }

        // row != col here:
        if (isCellSelected(row, col))
            {
            c.setBackground(selection_background_color);
            c.setForeground(Color.black);
            } else
            {
            c.setForeground(getForeground());
            c.setBackground(getBackground());
            }

        return (TableCellRenderer) c;
        }

    /**
     * // Only works in JRE 1.4 or higher public Component
     * prepareRenderer(TableCellRenderer r, int row, int col) { if (
     * !((AgnaTableModel)this.getModel()).isReady() ) return null; Component c =
     * super.prepareRenderer(r, row, col); if (isCellSelected(row, col)) { if
     * (row != col) { c.setBackground(selection_background_color);
     * c.setForeground(Color.black); } else {
     * c.setBackground(bad_selection_background_color);
     * c.setForeground(Color.black); } } else {
     * c.setForeground(getForeground()); if (row != col) {
     * c.setBackground(getBackground()); } else {
     * c.setBackground(bad_background_color); } } return c; }
     */

    public String getToolTipText(MouseEvent e)
        {
        if (e == null)
            return null;
        try
            {
            int row = rowAtPoint(e.getPoint());
            int col = columnAtPoint(e.getPoint());
            Object o = getValueAt(row, col);

            String from_to = "Cell: " + MainFrame.getNodeName(row) + " --> "
                    + MainFrame.getNodeName(col);

            /*
             * XX: String from_to ="Cell: " +
             * MainFrame.getVerticalHeaderName(row) + " --> " +
             * MainFrame.getHorizontalHeaderName(col);
             */
            if (row == col)
                {
                return "<html><font size = 2 face='Arial,Helvetica,Verdana,sans-serif' color='red'>"
                        + from_to + "<br>Noneditable</font>";
                } else if (o == null || o.toString().equals(""))
                {
                return "<html><font size = 2 face='Arial,Helvetica,Verdana,sans-serif'>"
                        + from_to + "</font>";
                } else
                {
                return "<html><font size = 2 face='Arial,Helvetica,Verdana,sans-serif'>"
                        + from_to + "<br>Value: " + o.toString() + "</font>";

                }
            } catch (Exception ex)
            {
            return null;
            }
        }

    public Object getValueAt(int i, int j)
        {
        return super.getValueAt(i, j);
        }

    // checks whether grid contains real-type data;
    // determines the binary/weighted type of data
    // not to be used inside threads!
    public boolean doCheckAllValues()
        {
        boolean tmp_weight = false;
        if (this.isEditing())
            {
            doStopEditingTable();
            }
        int nu = this.getModel().getRowCount();
        float tmpval = 0f;
        for (int i = 0; i < nu; i++)
            {
            for (int j = 0; j < nu; j++)
                {
                try
                    {
                    tmpval = Float.parseFloat((String) super.getValueAt(i, j));
                    } catch (Exception ee)
                    {
                    JOptionPane.showMessageDialog(MainFrame.getCurrentFrame(),
                            "Bad value encountered in grid cell!",
                            "Parsing Error", JOptionPane.ERROR_MESSAGE);
                    this.editCellAt(i, j);
                    return false;
                    }
                if (tmpval != 0f && tmpval != 1f)
                    tmp_weight = true;
                }
            }

        MainFrame.setCurrentWeight(tmp_weight);
        return true;
        }

    public void doStopEditingTable()
        {
        DefaultCellEditor editor = (DefaultCellEditor) this.getCellEditor();
        if (editor != null)
            editor.stopCellEditing();
        editor = null;
        }

    /*
     * public String getToolTipText(MouseEvent e) { if (e == null) return null;
     * int where = whereAreWe(e.getX(), e.getY()); if (where == -1) return null;
     * else return my_nodes[where].name; } public Class getColumnClass(int
     * column) { if (column < 1) { return String.class; } return Integer.class; }
     */
    }