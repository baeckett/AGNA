package com.bentza.sna.net;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import com.bentza.sna.gui.MainFrame;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * A class containing information about a node: name, icon, position and
 * connections with others stored in the Vector emissions; alias: Node;
 */

        public class Actor extends Object
    {
    public String name;

    public int node_size;

    // private ImageIcon face;
    // 2.1.3: emissions stored as a plain float array (was Vector<Float>);
    // eliminates per-cell boxing and elementAt overhead in every matrix pass
    public float[] emissions;

    private int grid_space, face_width, face_height;

    // private String face_source; // path or filename of the face
    private NodeXY xy;

    private ImageItem face_item;

    private static ImageStock image_stock;

    /**
     * Returns the shared image stock: the application-wide stock once the main
     * frame exists, otherwise a private fallback (e.g. headless file parsing,
     * where the GUI singleton has not been created yet). 2.1.3.
     */
    private static ImageStock getImageStockInstance()
        {
        if (image_stock == null)
            {
            ImageStock main_stock = MainFrame.getCurrentImageStock();
            image_stock = main_stock != null ? main_stock : new ImageStock();
            }
        return image_stock;
        }

    public Actor()
        {
        grid_space = 40;
        name = "Node";
face_item = getImageStockInstance().requestImageItem(MainFrame
                .getDefaultNodeFaceSource());
        setSize();
        emissions = new float[10];
        }

    public Actor(String tmp_name)
        {
        grid_space = 40;
        name = tmp_name;
face_item = getImageStockInstance().requestImageItem(MainFrame
                .getDefaultNodeFaceSource());
        setSize();
        emissions = new float[10];
        }

    public Actor(String tmp_name, int tmp_net_size)
        {
        grid_space = 40;
        name = tmp_name;
        try
            {
face_item = getImageStockInstance().requestImageItem(MainFrame
                .getDefaultNodeFaceSource());
            } catch (NullPointerException e31)
            {
            face_item = null;
            }
        setSize();
        emissions = new float[tmp_net_size];
        }

    /*
     * public Actor(String tmp_name, String tmp_face_source) { grid_space=40;
     * name=tmp_name; face_item = image_stock.requestImageItem(tmp_face_source);
     * setSize(); }
     */
    public void createCoordinates()
        {
        xy = new NodeXY();
        }

    public void addEmissionsElement()
        {
        float[] tmp = new float[emissions.length + 1];
        System.arraycopy(emissions, 0, tmp, 0, emissions.length);
        emissions = tmp;
        }

    public void deleteEmissionsElement(int tmp_i)
        {
        if (tmp_i < 0 || tmp_i >= emissions.length)
            return;
        float[] tmp = new float[emissions.length - 1];
        System.arraycopy(emissions, 0, tmp, 0, tmp_i);
        System.arraycopy(emissions, tmp_i + 1, tmp, tmp_i, emissions.length
                - tmp_i - 1);
        emissions = tmp;
        }

    public void setEmissionsValue(float tmp_value, int j)
        {
        if (j < 0 || j >= emissions.length)
            return;
        emissions[j] = tmp_value;
        }

    public void setStringEmissionsValue(String tmp_value, int j)
        {
        if (j < 0 || j >= emissions.length)
            return;
        try
            {
            emissions[j] = Float.parseFloat(tmp_value);
            } catch (NumberFormatException e1)
            {
            AgnaLog.warn("invalid cell value: " + tmp_value);
            }
        }

    public void setStringEmissionsValue(Object tmp_value, int j)
        {
        if (j < 0 || j >= emissions.length)
            return;
        try
            {
            emissions[j] = Float.parseFloat((String) tmp_value);
            } catch (Exception e1)
            {
            AgnaLog.warn("invalid cell value: " + tmp_value);
            }
        }

    // object here is Float!
    public void setObjectEmissionsValue(Object tmp_value, int j)
        {
        if (j < 0 || j >= emissions.length)
            return;
        if (tmp_value instanceof Float)
            {
            emissions[j] = ((Float) tmp_value).floatValue();
            }
        }

    public float getEmissionsValue(int j)
        {
        if (j < 0 || j >= emissions.length)
            return 0f;
        return emissions[j];
        }

    // forces value to integer
    public int getIntegerEmissionsValue(int j)
        {
        if (j < 0 || j >= emissions.length)
            return 0;
        return (int) emissions[j];
        }

    public float[] getEmissionsArray(int nn) // nn is the number of nodes
        {
        final float[] emis = new float[nn];
        int count = Math.min(nn, emissions.length);
        System.arraycopy(emissions, 0, emis, 0, count);
        return emis;
        }

    public boolean hasNoEmission(int nn) // nn is the number of nodes
        {
        boolean has_no_emission = true;
        for (int i = 0; i < nn; i++)
            {
            if (this.getEmissionsValue(i) != 0f)
                {
                has_no_emission = false;
                break;
                }
            }
        return has_no_emission;
        }

    public JLabel getAsJLabel()
        {
        return new JLabel(getName(), getFace(), JLabel.LEFT);
        }

    public void noFace()
        {
        giveUpFace();
        face_item = getImageStockInstance().requestEmptyImageItem();
        node_size = 16;
        }

    public ImageIcon getSmallFace()
        {
        return face_item.small_image_icon;
        }

    public ImageIcon getFace()
        {
        return face_item.image_icon;
        }

    /*
     * public void setFaceSource(String tmp_source) { face_source = tmp_source; }
     */

    public String getFaceSource()
        {
        // 2.1.3: null-safe fallback (face could not be loaded)
        if (face_item == null)
            {
            return "-";
            }
        return face_item.image_source;
        }

    public String getRelativeFaceSource()
        {
        try
            {
            int left_side = Environment.getCurrentDirectory().length();
            if (face_item.image_source.substring(left_side, left_side + 5)
                    .equals("Faces"))
                return "."
                        + face_item.image_source.substring(left_side - 1,
                                face_item.image_source.length());
            else
                return face_item.image_source;
            } catch (Exception e)
            {
            return MainFrame.getDefaultNodeFaceSource();
            }
        }

    public int getFaceWidth()
        {
        return (int) ((double) node_size * face_item.image_icon.getIconWidth() / Math
                .max(face_item.image_icon.getIconWidth(), face_item.image_icon
                        .getIconHeight()));
        }

    public int getFaceHeight()
        {
        return (int) ((double) node_size * face_item.image_icon.getIconHeight() / Math
                .max(face_item.image_icon.getIconWidth(), face_item.image_icon
                        .getIconHeight()));
        }

    public void giveUpFace()
        {
        // 2.1.3: null-safe (an actor whose face failed to load has a null
        // face_item; the old code crashed on deletion in that case)
        if (face_item != null)
            {
            face_item.decreaseClients();
            }
        }

    public void cleaning()
        {
        giveUpFace();
        name = null;
        emissions = null;
        xy = null;
        }

    public void setFace(String tmp_face_source)
        {
        giveUpFace();
        face_item = getImageStockInstance().requestImageItem(tmp_face_source);
        try
            {
            node_size = Math.max(face_item.image_icon.getIconWidth(),
                    face_item.image_icon.getIconHeight());
            } catch (Exception e)
            {
            node_size = 16;
            }
        if (node_size < 3)
            {
            node_size = 16;
            }
        return;
        }

    public void setName(String tmp_name)
        {
        if (tmp_name == null)
            {
            name = null;
            return;
            }
        // 2.1.3: control characters (e.g. tab, newline, CR) would corrupt the
        // tab-separated .agn file format when the network is saved; they are
        // replaced with "_" (the 2.1.2 code computed the replacement but
        // discarded it, so the raw name was stored unchanged)
        StringBuffer filtered = new StringBuffer(tmp_name.length());
        for (int in = 0; in < tmp_name.length(); in++)
            {
            char c = tmp_name.charAt(in);
            filtered.append(c < 32 ? '_' : c);
            }
        name = filtered.toString();
        }

    // assumes that this node belongs to currently open network!
    private boolean getSTGEnabled()
        {
        boolean value = false;
        try
            {
            value = MainFrame.getCurrentFullNet().getArea().isSTGEnabled();
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }

        return value;
        }

    public String getName()
        {
        if (name == null)
            return "";
        return name;

        /*
         * try { return name; } catch (Exception e) { return ""; }
         */
        }

    public String getNameInQuotes()
        {
        if (name == null)
            return "";
        return '"' + name + '"';
        }

    public int getSize()
        {
        try
            {
            return node_size;
            } catch (Exception e)
            {
            return 16;
            }
        }

    public void setSize()
        {
        node_size = 16;
        face_width = 16;
        face_height = 16;
        /*
         * node_size=Math.max(face.getIconWidth(), face.getIconHeight());
         * face_width = face.getIconWidth(); face_height = face.getIconHeight();
         * if (face_width == -1 || face_height == -1) { face_width = 16;
         * face_height = 16; this.noFace(); }
         */
        }

    public void setEmissionsNumber(int tmp_size)
        {
        if (tmp_size == emissions.length)
            return;
        float[] tmp = new float[tmp_size];
        int count = Math.min(tmp_size, emissions.length);
        System.arraycopy(emissions, 0, tmp, 0, count);
        emissions = tmp;
        }

    public void setSize(int tmp_size)
        {
        final ImageIcon node_icon = face_item.image_icon;
        if (node_icon == null)
            return;
        node_size = tmp_size;
        final double node_width = (double) node_icon.getIconWidth();
        final double node_height = (double) node_icon.getIconHeight();
        face_width = (int) ((double) node_size * node_width / Math.max(
                node_width, node_height));
        face_height = (int) ((double) node_size * node_height / Math.max(
                node_width, node_height));
        }

    public float getX()
        {
        try
            {
            return xy.getX();
            } catch (Exception e)
            {
            return 10f + 80f * (float) Math.random();
            }
        }

    public float getY()
        {
        try
            {
            return xy.getY();
            } catch (Exception e)
            {
            return 10f + 80f * (float) Math.random();
            }

        }

    public int getX(int x_max)
        {
        return xy.getX(x_max);
        }

    public int getY(int y_max)
        {
        return xy.getY(y_max);
        }

    public void setX(float tmp_x)
        {
        xy.setX(tmp_x);
        }

    public void setY(float tmp_y)
        {
        xy.setY(tmp_y);
        }

    public void setX(int tmp_x, int x_max)
        {
        if (tmp_x <= 0)
            {
            xy.setX(0f);
            } else if (tmp_x >= x_max - node_size)
            {
            xy.setX(x_max - node_size - 1, x_max);
            } else
            {
            xy.setX(tmp_x, x_max);
            }
        }

    public void setY(int tmp_y, int y_max)
        {
        if (tmp_y <= 0)
            {
            xy.setY(0f);
            } else if (tmp_y >= y_max - node_size)
            {
            xy.setY(y_max - node_size - 1, y_max);
            } else
            {
            xy.setY(tmp_y, y_max);
            }
        }

    public int getGridStep()
        {
        try
            {
            return grid_space;
            } catch (Exception e)
            {
            return 40;
            }
        }

    public void setGridStep(int tmp_grid_space)
        {
        grid_space = tmp_grid_space;
        }

    public void moveActor(int tmp_x, int tmp_y, int x_max, boolean snap)
        {
        int ns = (int) (node_size / 2);

        if (tmp_x <= ns)
            tmp_x = ns + 1;
        else if (tmp_x >= x_max - ns)
            tmp_x = x_max - ns - 1;
        if (tmp_y <= ns)
            tmp_y = ns + 1;
        else if (tmp_y >= x_max - ns)
            tmp_y = x_max - ns - 1;

        // 2.1.3: lazily create coordinates (actors loaded from a file may not
        // have them until the viewer lays them out)
        if (xy == null)
            {
            xy = new NodeXY();
            }

        xy.setX(tmp_x - ns, x_max);
        xy.setY(tmp_y - ns, x_max);

        int node_x = xy.getX(x_max);
        int node_y = xy.getY(x_max);

        // snap to grid:
        if (snap && getSTGEnabled() && grid_space > 0)
            {
            int gravitation = (int) (grid_space / 5);
            if (grid_space < 5)
                gravitation = 2;
            if (node_x % grid_space < gravitation)
                {
                xy.setX(grid_space * (int) (node_x / grid_space), x_max);
                }
            if (grid_space - node_x % grid_space < gravitation)
                {
                xy.setX(grid_space + grid_space * (int) (node_x / grid_space),
                        x_max);
                }
            if (node_y % grid_space < gravitation)
                {
                xy.setY(grid_space * (int) (node_y / grid_space), x_max);
                }
            if (grid_space - node_y % grid_space < gravitation)
                {
                xy.setY(grid_space + grid_space * (int) (node_y / grid_space),
                        x_max);
                }
            }
        }
    }