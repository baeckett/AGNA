package com.bentza.sna.net;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.core.AppRuntime;
import com.bentza.sna.Environment;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.Border;

public class NodeArea extends JButton implements MouseListener,
        MouseMotionListener
    {
    public int node_x = 0, node_y = 0, moving_node, selected_node,
            colored_node, nodes_count, small_x, small_y, area_width, names_x,
            names_y, edge_value_position;

    public Color names_color, title_color, arrow_color, background_color;

    public Actor[] my_nodes = null;

    public boolean colorize, print_names, allow_ES, print_edge_value;

    public float scale_factor;

    public ImageIcon background_image;

    private String title, background_image_source;

    private int separ, grid_transparency, max_transparency,
            inverse_max_transparency, second_selected, title_x, title_y,
            background_image_x, background_image_y, background_image_width,
            background_image_height;

    private Color grid_color, edge_value_color;

    private boolean is_color_fidelity, grid_enabled, paint_to_screen,
            faces_visible, view_title;

    private BufferedImage edges_image;

    private Graphics2D edges_gr;

    private int[][] edges_mat;

    public static final byte CIRCLE_LAYOUT = 0;

    public static final byte RANDOM_LAYOUT = 1;

    private static final String M00 = "-";

    private static final String M01 = "<html><font size = 2 face='Helvetica,Verdana,sans-serif'>";

    private static final String M02 = "  -  Current position: ( ";

    private static final String M03 = " , ";

    private static final String M04 = " ) ";

    private static final String M05 = "Edge selected: ";

    private static final String M06 = "<font size = 2 color='#298C8C' face='Helvetica,Verdana,sans-serif'> ";

    private static final String M07 = " </font>";

    private static final String M08 = " --> ";

    private static final String M09 = ". ";

    private static final String M10 = "Edge Value: ";

    private static final String M11 = "  -  New position: ( ";

    private static final String M12 = " - Use ALT + arrow keys to change position.";

    private static final String M13 = "AgnaDefaultSettings.ini";

    private static final String M14 = "New Network";

    private byte default_image_layout;

    private boolean stg_enabled;

    final Color emphasize_color = Color.red;


    final double sin15 = 0.2588190451;

    final double cos15 = 0.9659258262;

    NodeArea()
        {
        setInitialSettings();
        }

    NodeArea(Network tmp_network)
        {
        int i;
        this.setBorder(null);
        moving_node = -1;
        colored_node = -1;
        nodes_count = 10;
        small_x = 0;
        small_y = 0;
        nodes_count = tmp_network.getSize(); //
        scale_factor = 1f;
        my_nodes = new Actor[nodes_count];
        edges_mat = new int[nodes_count][nodes_count];

        addMouseMotionListener(this);
        addMouseListener(this);
        edges_image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        setInitialSettings(); // settings generated;
        // ini file exists?
        if (Environment.getSettingsFile().exists())
            {
            // 2.1.3: headless contexts (CLI, library) may have no
            // AppRuntime current network; skip the settings then
            FullNet current_full = AppRuntime.getCurrentFullNet();
            if (current_full != null)
                current_full.readInitialSettings(this, Environment
                        .getSettingsFile().getAbsolutePath());
            }
                                                                            // form
                                                                            // file
        if (edges_image.getWidth() != area_width)
            edges_image = new BufferedImage(area_width, area_width,
                    BufferedImage.TYPE_INT_RGB);
        title = tmp_network.getName();
        this.updateArea(tmp_network);
        edges_gr = edges_image.createGraphics();
        setImageLayout(default_image_layout);

        // paintEdges();
        }

    public void setInitialSettings()
        {
        selected_node = -1; //
        second_selected = -1;//
        area_width = 400; //
        names_x = 3; //
        names_y = -2; //
        title_x = (int) ((float) area_width / 15); //
        title_y = 30; //
        try
            {
            title = AppRuntime.getCurrentNetwork().getName(); //
            } catch (Exception e1)
            {
            title = M14;
            }

        view_title = false;
        separ = 2; //
        print_edge_value = false; //
        edge_value_position = 30; //
        edge_value_color = new Color(0, 191, 166, 255); //
        grid_transparency = 25; // byte
        max_transparency = 35; //
        grid_color = new Color(10, 210, 10, grid_transparency); //
        print_names = true; //
        grid_enabled = true; //
        faces_visible = true; //
        stg_enabled = true;
        allow_ES = true; //
        is_color_fidelity = true; //
        names_color = Color.darkGray; //
        title_color = Color.darkGray; //
        background_color = Color.white; //
        background_image_source = M00;
        background_image = new ImageIcon(""); //
        background_image_x = 0;
        background_image_y = 0;
        background_image_width = 0;
        background_image_height = 0;
        arrow_color = new Color(137, 160, 160, 85); //
        colorize = false; //
        paint_to_screen = true;
        default_image_layout = CIRCLE_LAYOUT;
        if (my_nodes == null || my_nodes[0] == null)
            createVisualNodes();
        else
            for (int i = 0; i < nodes_count; i++)
                {
                my_nodes[i].createCoordinates();
                }
        }

    private void createVisualNodes()
        {
        Network tmp_network = AppRuntime.getCurrentNetwork();
        // 2.1.3: null-safe (the viewer can be constructed headless or before
        // the main frame is ready); existing nodes are left untouched and
        // updateArea() populates them
        if (tmp_network == null)
            return;
        my_nodes = null;
        nodes_count = tmp_network.getSize();
        my_nodes = new Actor[nodes_count];
        for (int i = 0; i < nodes_count; i++)
            {
            my_nodes[i] = tmp_network.getActor(i);
            my_nodes[i].createCoordinates();
            }
        }

    public void updateArea(Network tmp_network)
        {
        // 2.1.3: also (re)build the node array when its elements are missing
        // (e.g. the viewer was constructed before the main frame was ready,
        // which leaves the array allocated but empty)
        boolean nodes_missing = my_nodes == null
                || my_nodes.length != tmp_network.getSize()
                || (tmp_network.getSize() > 0 && my_nodes[0] == null);
        if (nodes_missing) // redimensioneaza vectorii
            {
            nodes_count = tmp_network.getSize();
            my_nodes = null;
            edges_mat = null;
            my_nodes = new Actor[nodes_count];
            edges_mat = new int[nodes_count][nodes_count];
            }
        float minmat = tmp_network.getMin();
        // float maxmat=tmp_network.getMax();
        float dif = tmp_network.getMax() - minmat;
        inverse_max_transparency = 256 - max_transparency;
        // de aici

        if (dif == 0f)
            {
            for (int i = 0; i < nodes_count; i++)
                {
                // 2.1.3: fill the node references here too (the loop below
                // that does it is skipped in the all-zero case)
                my_nodes[i] = tmp_network.getActor(i);
                for (int j = 0; j < nodes_count; j++)
                    {
                    if (tmp_network.getValue(i, j) == 0f)
                        edges_mat[i][j] = 0;
                    else
                        edges_mat[i][j] = 255;
                    }
                }
            return;
            }

        // pina aici ar tre sters

        // temporary variable:
        float connection_val = 0f;
        for (int i = 0; i < nodes_count; i++) // actualizeaza vectorii
            {
            my_nodes[i] = tmp_network.getActor(i);
            for (int j = 0; j < nodes_count; j++)
                {
                connection_val = tmp_network.getValue(i, j);
                // connection_val = my_nodes[i].getEmissionsValue(j);
                if (connection_val == 0f)
                    edges_mat[i][j] = 0;
                else if (connection_val == minmat)
                    {
                    edges_mat[i][j] = max_transparency;
                    } else if (connection_val == minmat + dif)
                    edges_mat[i][j] = 255;
                else
                    edges_mat[i][j] = max_transparency
                            + (int) (inverse_max_transparency
                                    * ((float) connection_val - minmat) / dif);

                }
            }
        }

    public void setColorFidelity(boolean tmp_val)
        {
        this.is_color_fidelity = tmp_val;
        }

    public boolean getColorFidelity()
        {
        try
            {
            return is_color_fidelity;
            } catch (Exception e)
            {
            return true;
            }
        }

    public void setImageLayout(byte image_layout)
        {
        if (image_layout == CIRCLE_LAYOUT)
            {
            setCircleLayout();
            } else if (image_layout == RANDOM_LAYOUT)
            {
            setRandomLayout();
            }
        }

    // bring all node coordinates inside the interval [1, 99]
    private void adjustNodesCoordinates()
        {
        float max_x, max_y, min_x, min_y;
        max_x = Float.NEGATIVE_INFINITY;
        max_y = Float.NEGATIVE_INFINITY;
        min_y = Float.POSITIVE_INFINITY;
        min_x = Float.POSITIVE_INFINITY;

        for (int i = 0; i < nodes_count; i++)
            {
            if (my_nodes[i].getX() > max_x)
                max_x = my_nodes[i].getX();
            if (my_nodes[i].getY() > max_y)
                max_y = my_nodes[i].getY();
            if (my_nodes[i].getX() < min_x)
                min_x = my_nodes[i].getX();
            if (my_nodes[i].getY() < min_y)
                min_y = my_nodes[i].getY();
            }
        for (int i = 0; i < nodes_count; i++)
            {
            my_nodes[i].setX(10 + 80f * (my_nodes[i].getX() - min_x)
                    / (max_x - min_x));
            my_nodes[i].setY(10 + 80f * (my_nodes[i].getY() - min_y)
                    / (max_y - min_y));
            }

        }

    public void setLagrangeLayout()
        {
        int i, j;
        float force_x, force_y, anti_x, anti_y;
        float current_x, current_y;

        final byte step = 4;
        float intensity = 3f;
        float rejection_intensity = 1.8f;
        boolean[][] mat = AppRuntime.getCurrentNetwork().getBooleanMatrix();

        for (i = 0; i < nodes_count; i++)
            {
            // i = (int)(nodes_count * Math.random());
            force_x = 0f;
            force_y = 0f;
            anti_x = 0f;
            anti_y = 0f;
            current_x = my_nodes[i].getX();
            current_y = my_nodes[i].getY();
            for (j = 0; j < nodes_count; j++)
                {
                if (mat[j][i] || mat[i][j])
                    {
                    // attraction resulting force:
                    force_x += intensity * (my_nodes[j].getX() - current_x);
                    force_y += intensity * (my_nodes[j].getY() - current_y);
                    }
                // rejection forces:
                anti_x += rejection_intensity
                        / (my_nodes[j].getX() - current_x + 0.1f)
                        / (my_nodes[j].getX() - current_x + 0.1f);
                anti_y += rejection_intensity
                        / (my_nodes[j].getY() - current_y + 0.1f)
                        / (my_nodes[j].getY() - current_y + 0.1f);
                }

            if (force_x > anti_x)
                my_nodes[i].setX(current_x - (float) step * force_x
                        / Math.abs(force_x));
            else if (force_x < anti_x)
                my_nodes[i].setX(current_x + (float) step * force_x
                        / Math.abs(force_x));

            if (force_y > anti_y)
                my_nodes[i].setY(current_y - (float) step * force_y
                        / Math.abs(force_y));
            else if (force_y < anti_y)
                my_nodes[i].setY(current_y + (float) step * force_y
                        / Math.abs(force_y));
            }
        adjustNodesCoordinates();
        paintEdges();
        repaint();
        }

    public void setRandomLayout()
        {
        for (int i = 0; i < nodes_count; i++)
            {
            my_nodes[i].moveActor(
                    10 + (int) ((area_width - 20) * Math.random()),
                    10 + (int) ((area_width - 20) * Math.random()), area_width,
                    false);
            }
        }

    public void setCircleLayout()
        {
        float fi0 = (float) ((double) Math.PI / (double) nodes_count);
        float fi = fi0;
        float r = (int) ((float) area_width / 2.5f);
        int x, y;
        for (int i = 0; i < nodes_count; i++)
            {
            x = (int) ((float) area_width / 2 + (float) r * Math.sin(fi));
            y = (int) ((float) area_width / 2 - (float) r * Math.cos(fi));
            my_nodes[i].moveActor(x, y, area_width, false);
            fi += 2 * fi0;
            }
        }

    // 2.1.3: the two new layouts share the deterministic core
    // implementations (also used by the CLI renderer)
    public void setGridLayout()
        {
        NetworkLayouts.apply(AppRuntime.getCurrentNetwork(),
                NetworkLayouts.GRID, area_width, area_width);
        adjustNodesCoordinates();
        paintEdges();
        repaint();
        }

    public void setConcentricLayout()
        {
        NetworkLayouts.apply(AppRuntime.getCurrentNetwork(),
                NetworkLayouts.CONCENTRIC, area_width, area_width);
        adjustNodesCoordinates();
        paintEdges();
        repaint();
        }

    public void setSpringLayout()
        {
        NetworkLayouts.apply(AppRuntime.getCurrentNetwork(),
                NetworkLayouts.SPRING, area_width, area_width);
        adjustNodesCoordinates();
        paintEdges();
        repaint();
        }

    public String getTitle()
        {
        return this.title;
        }

    public void setTitle(String tmp_title)
        {
        this.title = tmp_title;
        }

    public void setTitleVisible(boolean tmp)
        {
        view_title = tmp;
        }

    public boolean getPrintNames()
        {
        try
            {
            return print_names;
            } catch (Exception e)
            {
            return true;
            }
        }

    public void setPrintNames(boolean tmp_value)
        {
        print_names = tmp_value;
        repaint();
        }

    public void changeAllFaces(String tmp_image_name)
        {
        for (int i = 0; i < nodes_count; i++)
            {
            my_nodes[i].setFace(tmp_image_name);
            }
        }

    public void changeAllFacesWidth(int tmp_int)
        {
        for (int i = 0; i < nodes_count; i++)
            {
            my_nodes[i].setSize(tmp_int);
            }
        }

    public boolean selectActor(int tmp_index)
        {
        if (tmp_index < 0 || tmp_index >= nodes_count)
            return false;

        if (selected_node >= 0)
            second_selected = selected_node;
        selected_node = tmp_index;
        if (selected_node == -1 || selected_node == second_selected)
            second_selected = -1;
        // if (second_selected == -1)
        AppRuntime.setHTMLStatus(M01 + my_nodes[selected_node].name + M02
                + String.valueOf(my_nodes[selected_node].getX(area_width))
                + M03
                + String.valueOf(my_nodes[selected_node].getY(area_width))
                + M04);
        return true;
        }

    public void selectNextActor()
        {
        if (selected_node < nodes_count - 1)
            selected_node++;
        else
            selected_node = 0;
        if (selected_node == second_selected)
            {
            if (selected_node < nodes_count - 1)
                selected_node++;
            else
                selected_node = 0;
            }
        if (second_selected >= 0 && allow_ES) // arrow selected
            {
            AppRuntime.setHTMLStatus(M01
                    + M05
                    + M06
                    + String.valueOf(my_nodes[second_selected].name)
                    + M07
                    + M08
                    + M06
                    + String.valueOf(my_nodes[selected_node].name)
                    + M07
                    + M09
                    + M10
                    + M06
                    + String.valueOf(AppRuntime.getCurrentNetwork().getValue(
                            second_selected, selected_node)));
            } else
            { // no arrow selected
            AppRuntime.setHTMLStatus(M01 + my_nodes[selected_node].name + M02
                    + String.valueOf(my_nodes[selected_node].getX(area_width))
                    + M03
                    + String.valueOf(my_nodes[selected_node].getY(area_width))
                    + M04);
            }
        }

    public void selectNextArrow()
        {
        if (selected_node > nodes_count || selected_node == -1)
            {
            selected_node = 0;
            }

        if (second_selected < 0)
            {
            second_selected = 0;
            } else if (second_selected >= 0
                && second_selected < nodes_count - 1)
            {
            second_selected++;
            } else if (second_selected == nodes_count - 1)
            {
            second_selected = 0;
            }

        if (second_selected == selected_node)
            {
            if (second_selected < nodes_count - 1)
                {
                second_selected++;
                } else
                {
                second_selected = 0;
                }
            }

        if (second_selected >= 0 && allow_ES)
            {
            try
                {
                AppRuntime.setHTMLStatus(M01
                        + M05
                        + M06
                        + String.valueOf(my_nodes[second_selected].name)
                        + M07
                        + M08
                        + M06
                        + String.valueOf(my_nodes[selected_node].name)
                        + M07
                        + M09
                        + M10
                        + M06
                        + String.valueOf(AppRuntime.getCurrentNetwork()
                                .getValue(second_selected, selected_node))
                        + M07);
                } catch (Exception e2) {
      AgnaLog.warn("suppressed exception", e2);
      }
            }
        }

    public int getWidth()
        {
        try
            {
            return this.area_width;
            } catch (Exception e)
            {
            return 400;
            }
        }

    public int getHeight()
        {
        return this.area_width;
        }

    public void setWidthParameter(int tmp_width)
        {
        area_width = tmp_width;
        // paintEdges();
        }

    public void setWidthSimply(int tmp_width)
        {
        area_width = tmp_width;
        edges_image = null;
        edges_gr = null;
        edges_image = new BufferedImage(area_width, area_width,
                BufferedImage.TYPE_INT_RGB);
        edges_gr = edges_image.createGraphics();
        // paintEdges();
        }

    public void setWidth(int tmp_width)
        {
        if (tmp_width < 32)
            return;
        scale_factor = ((float) tmp_width / area_width);
        this.area_width = tmp_width;
        /*
         * for (int i=0; i<nodes_count; i++) {
         * //my_nodes[i].moveActor((int)((float)my_nodes[i].getX(area_width)*scale_factor),
         * (int)((float)my_nodes[i].getY(area_width)*scale_factor), area_width,
         * false); my_nodes[i].setX(scale_factor * my_nodes[i].getX());
         * my_nodes[i].setY(scale_factor * my_nodes[i].getY()); }
         */
        background_image_x = (int) ((float) background_image_x * scale_factor);
        background_image_y = (int) ((float) background_image_y * scale_factor);
        background_image_width = (int) ((float) background_image_width * scale_factor);
        background_image_height = (int) ((float) background_image_height * scale_factor);
        title_x = (int) ((float) title_x * scale_factor);
        title_y = (int) ((float) title_y * scale_factor);
        edges_image = null;
        edges_gr = null;
        edges_image = new BufferedImage(area_width, area_width,
                BufferedImage.TYPE_INT_RGB);
        edges_gr = edges_image.createGraphics();
        // paintEdges();
        // repaint();
        }

    public void setFacesVisible(boolean tmp_val)
        {
        faces_visible = tmp_val;
        }

    public void setGridEnabled(boolean tmp_val)
        {
        grid_enabled = tmp_val;
        }

    public boolean isFaceVisible()
        {
        try
            {
            return faces_visible;
            } catch (Exception e)
            {
            return true;
            }
        }

    public boolean isTitleVisible()
        {
        try
            {
            return view_title;
            } catch (Exception e)
            {
            return false;
            }
        }

    public void setTitleVisibility(boolean tmp_is)
        {
        view_title = tmp_is;
        }

    public boolean getGridEnabled()
        {
        try
            {
            return grid_enabled;
            } catch (Exception e)
            {
            return false;
            }
        }

    public void setSTGEnabled(boolean tmp_val) // STG = Snap To Grid
        {
        stg_enabled = tmp_val;
        }

    public boolean isSTGEnabled()
        {
        boolean value = false;
        try
            {
            // just in case stg_enabled has never been initialized:
            value = stg_enabled;
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }

        return value;
        }

    public int getGridSpace()
        {
        try
            {
            return my_nodes[0].getGridStep();
            } catch (Exception e)
            {
            return 40;
            }
        }

    public int getEdgesMat(int i, int j)
        {
        return edges_mat[i][j];
        }

    public void setGridSpace(int tmp_grid_space)
        {
        for (int i = 0; i < nodes_count; i++)
            my_nodes[i].setGridStep(tmp_grid_space);
        }

    public void setTitleX(int tmp_x)
        {
        title_x = tmp_x;
        }

    public void setTitleY(int tmp_y)
        {
        title_y = tmp_y;
        }

    public int getTitleX()
        {
        try
            {
            return title_x;
            } catch (Exception e)
            {
            return 10;
            }
        }

    public int getTitleY()
        {
        try
            {
            return title_y;
            } catch (Exception e)
            {
            return 10;
            }
        }

    public Actor[] getNodes()
        {
        return my_nodes;
        }

    public int[][] getEdgesMat()
        {
        return edges_mat;
        }

    public void setMaxTransparency(int tmp_transparency)
        {
        if (tmp_transparency > 255)
            tmp_transparency = 255;
        if (tmp_transparency < 0)
            tmp_transparency = 0;
        max_transparency = tmp_transparency;
        }

    public int getMaxTransparency()
        {
        try
            {
            return max_transparency;
            } catch (Exception e)
            {
            return 35;
            }
        }

    public int getEdgeValuePosition()
        {
        try
            {
            return edge_value_position;
            } catch (Exception e)
            {
            return 30;
            }
        }

    public void setEdgeValuePosition(int tmp)
        {
        edge_value_position = tmp;
        }

    public boolean isEdgeValueVisible()
        {
        // return print_edge_value;
        try
            {
            return print_edge_value;
            } catch (Exception e)
            {
            return false;
            }
        }

    public void setEdgeValueVisible(boolean tmp)
        {
        print_edge_value = tmp;
        }

    public Color getEdgeValueColor()
        {
        try
            {
            return edge_value_color;
            } catch (Exception e)
            {
            return Color.black;
            }
        }

    public void setEdgeValueColor(Color tmp_color)
        {
        edge_value_color = tmp_color;
        }

    public void setGridTransparency(int tmp_transparency)
        {
        if (tmp_transparency > 255)
            tmp_transparency = 255;
        if (tmp_transparency < 0)
            tmp_transparency = 0;
        grid_transparency = tmp_transparency;
        }

    public int getGridTransparency()
        {
        try
            {
            return grid_transparency;
            } catch (Exception e)
            {
            return 25;
            }
        }

    public void setSeparator(int tmp_separ)
        {
        this.separ = tmp_separ;
        }

    public int getSelectedActor()
        {
        try
            {
            return selected_node;
            } catch (Exception e)
            {
            return 0;
            }
        }

    public void setSelectedActor(int tmp_selected)
        {
        if (tmp_selected >= 0 && tmp_selected < nodes_count)
            selected_node = tmp_selected;
        else
            selected_node = -1;
        }

    public int getSecondSelected()
        {
        try
            {
            return second_selected;
            } catch (Exception e)
            {
            return -1;
            }
        }

    public void setSecondSelected(int tmp_i)
        {
        if (tmp_i >= 0 && tmp_i < nodes_count)
            second_selected = tmp_i;
        else
            second_selected = -1;
        }

    public void unselectSecond()
        {
        second_selected = -1;
        }

    public void setAllowES(boolean tmp_value)
        {
        allow_ES = tmp_value;
        }

    public boolean getAllowES()
        {
        try
            {
            return allow_ES;
            } catch (Exception e)
            {
            return false;
            }
        }

    public int getNamesX()
        {
        try
            {
            return names_x;
            } catch (Exception e)
            {
            return 3;
            }
        }

    public void setNamesX(int tmp_value)
        {
        names_x = tmp_value;
        }

    public int getNamesY()
        {
        try
            {
            return names_y;
            } catch (Exception e)
            {
            return -2;
            }
        }

    public void setNamesY(int tmp_value)
        {
        names_y = tmp_value;
        }

    public int getSeparator()
        {
        try
            {
            return this.separ;
            } catch (Exception e)
            {
            return 0;
            }
        }

    public Color getArrowColor()
        {
        try
            {
            return arrow_color;
            } catch (Exception e)
            {
            return Color.blue;
            }
        }

    public void setArrowColor(Color tmp_color)
        {
        this.arrow_color = tmp_color;
        }

    public Color getNamesColor()
        {
        try
            {
            return names_color;
            } catch (Exception e)
            {
            return Color.black;
            }
        }

    public void setNamesColor(Color tmp_color)
        {
        this.names_color = tmp_color;
        this.repaint();
        }

    public Color getTitleColor()
        {
        try
            {
            return title_color;
            } catch (Exception e)
            {
            return Color.black;
            }
        }

    public void setTitleColor(Color tmp_color)
        {
        this.title_color = tmp_color;
        }

    public Color getGridColor()
        {
        try
            {
            return grid_color;
            } catch (Exception e)
            {
            return Color.black;
            }
        }

    public void setGridColor(Color tmp_color)
        {
        this.grid_color = tmp_color;
        this.repaint();
        }

    public Color getBackgroundColor()
        {
        try
            {
            return background_color;
            } catch (Exception e)
            {
            return Color.white;
            }
        }

    public void setBackgroundColor(Color tmp_color)
        {
        this.background_color = tmp_color;
        }

    public ImageIcon getBackgroundImage()
        {
        return background_image;
        }

    public String getBackgroundImageSource()
        {
        try
            {
            return background_image_source;
            } catch (NullPointerException e)
            {
            return M00;
            }
        }

    public void setBackgroundImage(String tmp_image_name)
        {
        if (tmp_image_name != null && !tmp_image_name.equals("")
                && !tmp_image_name.equals(M00))
            {
            try
                {
                background_image = new ImageIcon(tmp_image_name);
                } catch (Exception e)
                {
                background_image = null;
                background_image_source = M00;
                return;
                }
            background_image_width = background_image.getIconWidth();
            background_image_height = background_image.getIconHeight();
            background_image_source = tmp_image_name;
            } else
            {
            background_image = null;
            background_image_source = M00;
            }
        }

    public void setBackgroundImageX(int tmp_x)
        {
        background_image_x = tmp_x;
        }

    public int getBackgroundImageX()
        {
        try
            {
            return background_image_x;
            } catch (Exception e)
            {
            return 0;
            }
        }

    public int getBackgroundImageOriginalWidth()
        {
        try
            {
            return (new ImageIcon(background_image_source)).getIconWidth();
            } catch (Exception e)
            {
            return getBackgroundImageWidth();
            }
        }

    public void setBackgroundImageY(int tmp_y)
        {
        background_image_y = tmp_y;
        }

    public int getBackgroundImageY()
        {
        try
            {
            return background_image_y;
            } catch (Exception e)
            {
            return 0;
            }
        }

    public int getBackgroundImageOriginalHeight()
        {
        // return background_image.getIconHeight();
        try
            {
            return (new ImageIcon(background_image_source)).getIconHeight();
            } catch (Exception e)
            {
            return getBackgroundImageHeight();
            }
        }

    public void setBackgroundImageWidth(int tmp_width)
        {
        background_image_width = tmp_width;
        }

    public int getBackgroundImageWidth()
        {
        try
            {
            return background_image_width;
            } catch (Exception e)
            {
            return 0;
            }
        }

    public void setBackgroundImageHeight(int tmp_height)
        {
        background_image_height = tmp_height;
        }

    public int getBackgroundImageHeight()
        {
        try
            {
            return background_image_height;
            } catch (Exception e)
            {
            return 0;
            }
        }

    public void setDefaultImageLayout(byte tmp_default_image_layout)
        {
        default_image_layout = tmp_default_image_layout;
        // modify here ini file as well!!
        }

    public byte getDefaultImageLayout()
        {
        return default_image_layout;
        }

    public static void setDefaultNodeFaceSource(String tmp_face_source)
        {
        AppRuntime.setDefaultNodeFaceSource(tmp_face_source);
        }

    public static String getDefaultNodeFaceSource()
        {
        return AppRuntime.getDefaultNodeFaceSource();
        }

    public void paintArrow(int x1, int y1, int x2, int y2,
            byte tmp_transparency, int i_node, int j_node, Graphics tg)
        {
        int r_i, r, new_x1, new_x2, new_y1, new_y2;
        double radical, fractie;
        int transparency = 255;
        // 2.1.3: decode the byte as unsigned (0..255). Values above 127
        // arrived as negative bytes, so the old "< 256 && > 0" test silently
        // ignored them and strong edges stayed at full opacity.
        int raw_transparency = tmp_transparency & 0xFF;
        if (!this.getColorFidelity())
            transparency = 255;
        else
            {
            if (raw_transparency < 256 && raw_transparency > 0)
                transparency = raw_transparency;
            }

        fractie = 0;
        r = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        // radical=(double)r;
        radical = Math.sqrt((double) r);
        r_i = my_nodes[i_node].getSize() * 3 / 4;
        r = my_nodes[j_node].getSize() * 3 / 4; // r = razele bulinutzelor

        // coordonate pentru edge_value:
        new_x1 = (int) ((double) x1 + (double) edge_value_position / 100
                * (x2 - x1));
        new_y1 = (int) ((double) y1 + (double) edge_value_position / 100
                * (y2 - y1));

        // deseneaza edge_value:
        if (print_edge_value && tg == edges_gr)
            {
            tg.setColor(new Color(edge_value_color.getRed(), edge_value_color
                    .getGreen(), edge_value_color.getBlue(), 255));
            tg.drawString(String.valueOf(AppRuntime.getCurrentNetwork()
                    .getValue(i_node, j_node)), new_x1, new_y1);
            }

        // coordonate pt linia lunga:
        new_x1 = (int) ((double) x1 + r_i * (x2 - x1) / radical);
        new_y1 = (int) ((double) y1 + r_i * (y2 - y1) / radical);
        new_x2 = (int) ((double) x2 - r * (x2 - x1) / radical);
        new_y2 = (int) ((double) y2 - r * (y2 - y1) / radical);

        // trage linia lunga:
        if (tg == edges_gr)
            {
            tg.setColor(new Color(arrow_color.getRed(), arrow_color.getGreen(),
                    arrow_color.getBlue(), transparency));
            }
        tg.drawLine(new_x1, new_y1, new_x2, new_y2);

        // sin(15)=0.2588190451; cos(15)=0.9659258262;

        r = (new_x2 - new_x1) * (new_x2 - new_x1) + (new_y2 - new_y1)
                * (new_y2 - new_y1);

        // radical=(double)r;
        radical = Math.sqrt((double) r);
        r = 8; // r = lungimea virfului de sageata

        radical = (double) r * (new_x2 - new_x1) / radical;
        fractie = (double) (new_y2 - new_y1) / (new_x2 - new_x1);
        if (new_x2 != new_x1)
            {
            fractie = (double) (new_y2 - new_y1) / (new_x2 - new_x1);
            x1 = (int) ((double) new_x2 - radical * (cos15 - sin15 * fractie));
            y1 = (int) ((double) new_y2 - radical * (sin15 + cos15 * fractie));
            x2 = (int) ((double) new_x2 - radical * (cos15 + sin15 * fractie));
            y2 = (int) ((double) new_y2 + radical * (sin15 - cos15 * fractie));
            } else
            {
            x1 = new_x2 - 2;
            x2 = new_x2 + 2;
            if (new_y2 > new_y1)
                {
                y1 = new_y2 - 7;
                y2 = new_y2 - 7;
                } else
                {
                y1 = new_y2 + 7;
                y2 = new_y2 + 7;
                }
            }

        // deseneaza capul de sageata:
        if ((x1 != 0) && (y1 != 0) && (x2 != 0) && (y2 != 0))
            {
            final int[] xx = { x1, x2, new_x2 };
            final int[] yy = { y1, y2, new_y2 };
            if (tg == edges_gr)
                {
                tg.setColor(new Color(arrow_color.getRed(), arrow_color
                        .getGreen(), arrow_color.getBlue(), transparency));
                }
            tg.fillPolygon(xx, yy, 3);
            }
        }

    public void doubleArrow(int x1, int y1, int x2, int y2,
            byte tmp_transparency, int i_node, int j_node, Graphics tg)
        {
        // double cos_alfa, sin_alfa, raza;
        final double raza = Math.sqrt(((double) x2 - x1) * (x2 - x1)
                + (y2 - y1) * (y2 - y1));
        final double cos_alfa = 2 * ((double) x2 - x1) / raza;
        final double sin_alfa = 2 * ((double) y2 - y1) / raza;
        paintArrow(x1 + (int) (sin_alfa * separ),
                y1 - (int) (cos_alfa * separ), x2 + (int) (sin_alfa * separ),
                y2 - (int) (cos_alfa * separ), tmp_transparency, i_node,
                j_node, tg);
        }

    public void paintEdges()
        {
        int small_i, small_j;
        String tmp = new String("");
        edges_gr.setColor(background_color);
        edges_gr.fillRect(0, 0, area_width - 1, area_width - 1);
        if (background_image != null)
            {
            try
                {
                edges_gr.drawImage(background_image.getImage(),
                        background_image_x, background_image_y,
                        background_image_width, background_image_height, this);
                } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
            }

        // background_image.paintIcon(this,edges_gr,0,0);
        if (grid_enabled && paint_to_screen)
            {
            paintGrids(edges_gr);
            }

        // painting border:
        edges_gr.setColor(Color.gray);
        edges_gr.drawRect(0, 0, area_width - 2, area_width - 2);

        // painting title:
        if (view_title)
            {
            edges_gr.setColor(new Color(title_color.getRed(), title_color
                    .getGreen(), title_color.getBlue(), 150));
            edges_gr.drawString(title, title_x, title_y);
            }

        // temporary variable to store the value of edges_mat[i][j]
        byte em_tmp = 0;

        for (int i = 0; i < nodes_count; i++)
            {
            for (int j = 0; j < nodes_count; j++)
                {
                small_i = (int) (my_nodes[i].getSize() / 2);
                small_j = (int) (my_nodes[j].getSize() / 2);
                em_tmp = (byte) edges_mat[i][j];
                if (em_tmp != 0)
                    {
                    if (separ > 0 && edges_mat[j][i] != 0)
                        {
                        doubleArrow(my_nodes[i].getX(area_width) + small_i,
                                my_nodes[i].getY(area_width) + small_i,
                                my_nodes[j].getX(area_width) + small_j,
                                my_nodes[j].getY(area_width) + small_j, em_tmp,
                                i, j, edges_gr);
                        } else
                        {
                        paintArrow(my_nodes[i].getX(area_width) + small_i,
                                my_nodes[i].getY(area_width) + small_i,
                                my_nodes[j].getX(area_width) + small_j,
                                my_nodes[j].getY(area_width) + small_j, em_tmp,
                                i, j, edges_gr);
                        }
                    }
                }
            }
        }

    public void paintSelectedEdge(Graphics g)
        {
        // paints selected edge
        if (second_selected < 0 || selected_node < 0
                || second_selected >= nodes_count
                || selected_node >= nodes_count)
            return;

        final int ss = (int) (my_nodes[second_selected].node_size / 2);
        final int sn = (int) (my_nodes[selected_node].node_size / 2);

        final int x1 = my_nodes[second_selected].getX(area_width) + ss;
        final int y1 = my_nodes[second_selected].getY(area_width) + ss;
        final int x2 = my_nodes[selected_node].getX(area_width) + sn;
        final int y2 = my_nodes[selected_node].getY(area_width) + sn;

        if (separ > 0 && edges_mat[selected_node][second_selected] != 0) // &&
                                                                            // edges_mat[second_selected][selected_node]!=0)
            {
            doubleArrow(x1, y1, x2, y2, (byte) 255, second_selected,
                    selected_node, g);
            } else
            {
            paintArrow(x1, y1, x2, y2, (byte) 255, second_selected,
                    selected_node, g);
            }
        }

    public void paintGrids(Graphics2D g)
        {
        int contor = 0;
        final int step = this.getGridSpace();
        int text_transparency = grid_transparency;
        grid_color = new Color(grid_color.getRed(), grid_color.getGreen(),
                grid_color.getBlue(), grid_transparency);
        text_transparency = grid_transparency + 25;
        if (text_transparency > 255)
            text_transparency = 255;
        Color grid_names_color = new Color(210, 10, 10, text_transparency);
        for (int gi = 0; gi < area_width - 1; gi += step)
            {
            g.setColor(grid_color);
            g.drawLine(gi, 1, gi, area_width - 1); // drawing gridline
            if (gi - contor >= 50)
                {
                g.setColor(grid_names_color);
                g.drawLine(gi, 1, gi, 4);
                g.drawString(String.valueOf(gi), gi - 3, 16);
                contor = gi;
                }
            }
        contor = 0;
        for (int gi = 0; gi < area_width - 1; gi += step)
            {
            g.setColor(grid_color);
            g.drawLine(1, gi, area_width - 1, gi);
            if (gi - contor >= 50)
                {
                g.setColor(grid_names_color);
                g.drawLine(1, gi, 4, gi);
                g.drawString(String.valueOf(gi), 5, gi + 5);
                contor = gi;
                }
            }
        }

    // returns an image ready to be saved (JPG)
    public BufferedImage getOutputImage()
        {
        BufferedImage out_im = new BufferedImage(area_width, area_width,
                BufferedImage.TYPE_INT_RGB);
        Graphics out_gr = out_im.createGraphics();
        paint_to_screen = false;
        paintEdges();
        paintAll(out_gr);
        paint_to_screen = true;
        paintEdges();
        return out_im;
        }

    public void paintComponent(Graphics g)
        {
        super.paintComponent(g);
        paint_to_screen = true;
        paintAll(g);
        }

    public void paintAll(Graphics g)
        {
        int i, tmp_x, tmp_y, tmp_size;
        tmp_x = 0;
        tmp_y = 0;
        tmp_size = 0;
        i = 0;
        // super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        try
            {
            g2.drawImage(this.edges_image, 0, 0, this);
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        for (i = 0; i < nodes_count; i++)
            {
            tmp_x = my_nodes[i].getX(area_width);
            tmp_y = my_nodes[i].getY(area_width);
            tmp_size = my_nodes[i].getSize();
            // if (faces_visible)
            // my_nodes[i].face.paintIcon(this,g,tmp_x,tmp_y);
            // SCALABLE IMAGE!:
            if (faces_visible)
                {
                try
                    {
                    // painting node face:
                    g.drawImage(my_nodes[i].getFace().getImage(), tmp_x, tmp_y,
                            my_nodes[i].getFaceWidth(), my_nodes[i]
                                    .getFaceHeight(), this);
                    } catch (Exception e1) {
      AgnaLog.warn("suppressed exception", e1);
      }
                }
            // painting node names
            g.setColor(names_color);
            // drawstring on normal coordinates:
            // g.drawString("Hello World" , 0,
            // getFontMetrics(getFont()).getAscent());
            if (print_names)
                g.drawString(my_nodes[i].name, (tmp_x + tmp_size + names_x),
                        (tmp_y + tmp_size + names_y));
            if (paint_to_screen)
                {
                if (colored_node == i)
                    {
                    g.setColor(emphasize_color);
                    colorize = true;
                    g.drawRect(tmp_x, tmp_y, tmp_size, tmp_size);
                    }
                if (selected_node == i)
                    {
                    g.setColor(emphasize_color);
                    g.fillRect(tmp_x - 1, tmp_y - 1, 2, 2);
                    g.fillRect(tmp_x + tmp_size + 1, tmp_y - 1, 2, 2);
                    g.fillRect(tmp_x - 1, tmp_y + tmp_size + 1, 2, 2);
                    g
                            .fillRect(tmp_x + tmp_size + 1, tmp_y + tmp_size
                                    + 1, 2, 2);
                    }
                if (second_selected == i && allow_ES)
                    {
                    g.setColor(emphasize_color);
                    paintSelectedEdge(g);
                    }
                }
            }
        }

    public void mousePressed(MouseEvent e)
        {
        if (e == null)
            return;
        int x, y, i, mx, my, ms;
        String tmp_status = "";
        x = e.getX();
        y = e.getY();
        if (allow_ES)
            second_selected = selected_node;
        this.selected_node = -1;
        for (i = 0; i < nodes_count; i++)
            {
            mx = my_nodes[i].getX(area_width);
            my = my_nodes[i].getY(area_width);
            ms = my_nodes[i].getSize();
            if (mx <= x && x <= mx + ms && my <= y && y <= my + ms)
                {
                this.moving_node = i;
                this.selected_node = i;
                this.validate();
                small_x = x - mx - (int) (ms / 2);
                small_y = y - my - (int) (ms / 2);
                AppRuntime
                        .setHTMLStatus(M01
                                + my_nodes[i].name
                                + M02
                                + String.valueOf(my_nodes[moving_node]
                                        .getX(area_width))
                                + M03
                                + String.valueOf(my_nodes[moving_node]
                                        .getY(area_width)) + M04);
                break;
                }
            }
        if (selected_node == -1 || selected_node == second_selected)
            second_selected = -1;
        }

    public void mouseEntered(MouseEvent e)
        {
        }

    public void mouseExited(MouseEvent e)
        {
        }

    public void mouseClicked(MouseEvent e)
        {
        if (e.getClickCount() > 1)
            {
            // doNodeSettingsDialog();
            int selnode = getSelectedActor();
            if (selnode >= 0)
                {
                // double-click inside node:

                Actor candidate = my_nodes[selnode];
                                if (my_nodes != null && my_nodes[selnode] != null)
                    {
                    // 2.1.3: the desktop registers the node settings
                    // dialog; headless runs (CLI, tests) ignore it
                    AppRuntime.showNodeDialog(my_nodes[selnode], selnode);
                    }
                } else
                {
                // double-click outside node:
                try
                    {
                    int x = e.getX();
                    int y = e.getY();
                    AppRuntime.addNodeAndFocus(e.getX(), e.getY(), getWidth());
                    try
                        {
                        // selecting the newly created node:
                        setSelectedActor(my_nodes.length - 1);
                        AppRuntime.enableFirstNodeTools();
                        } catch (Exception e2) {
      AgnaLog.warn("suppressed exception", e2);
      }
                    } catch (Exception exc) {
      AgnaLog.warn("suppressed exception", exc);
      }
                }
            return;
            }
        /*
         * int x,y,i, mx, my, ms; x=e.getX(); y=e.getY(); if (selected_node >=
         * 0) { ms=my_nodes[selected_node].node_size; } else
         * ms=my_nodes[0].node_size; this.selected_node= -1; for (i=0;i<nodes_count;i++) {
         * mx=my_nodes[i].getX(area_width); my=my_nodes[i].getY(area_width); if
         * (mx <= x && x <= mx + ms && my <= y && y <= my + ms) {
         * this.selected_node=i; this.validate(); small_x=x-mx; small_y=y-my;
         * AppRuntime.setHTMLStatus("<html><font size = 2
         * face='Arial,Helvetica,Verdana,sans-serif'>" + my_nodes[i].name+" -
         * Current position: ( " + String.valueOf(mx)+" , " + String.valueOf(my) +" )
         * "); break; } }
         */
        if (second_selected >= 0 && allow_ES)
            {
            try
                {
                AppRuntime.setHTMLStatus(M01
                        + M05
                        + M06
                        + String.valueOf(my_nodes[second_selected].name)
                        + M07
                        + M08
                        + M06
                        + String.valueOf(my_nodes[selected_node].name)
                        + M07
                        + M09
                        + M10
                        + M06
                        + String.valueOf(AppRuntime.getCurrentNetwork()
                                .getValue(second_selected, selected_node))
                        + M07);
                } catch (Exception e1) {
      AgnaLog.warn("suppressed exception", e1);
      }
            }
        // repaint();
        return;
        }

    public void mouseReleased(MouseEvent e)
        {
        if (moving_node >= 0)
            paintEdges();
        moving_node = -1;
        small_x = 0;
        small_y = 0;
        AppRuntime.resetStatus();
        }

    public void mouseDragged(MouseEvent e)
        {
        int x, y;

        repaint();
        x = e.getX() - small_x;
        y = e.getY() - small_y;
        if (this.moving_node != -1)
            {
            my_nodes[moving_node].moveActor(x, y, area_width, true);
            AppRuntime.setHTMLStatus(M01 + my_nodes[moving_node].name + M11
                    + String.valueOf(my_nodes[moving_node].getX(area_width))
                    + M03
                    + String.valueOf(my_nodes[moving_node].getY(area_width))
                    + M04);
            }

        return;
        }

    /*
     * public void mouseMoved(MouseEvent e) { int x,y,i,nx,ny,ns; x=e.getX();
     * y=e.getY(); for (i=0;i<nodes_count;i++) { ns=my_nodes[i].getSize();
     * nx=my_nodes[i].getX(area_width); ny=my_nodes[i].getY(area_width); if (nx <=
     * x && x <= nx + ns && ny <= y && y <= ny + ns) { // we are in actor zone
     * colored_node=i; status_bar.setText("<html><font size=2
     * face='Arial,Helvetica,Verdana,sans-serif'>" + my_nodes[i].name);
     * this.setToolTipText(my_nodes[i].name); repaint(); break; } else {
     * colored_node=-1; if (colorize) { if (selected_node >= 0) {
     * status_bar.setText("<html><font size=2
     * face='Arial,Helvetica,Verdana,sans-serif'>" +
     * my_nodes[selected_node].name + " - Use ALT + arrow keys to change
     * position."); if (second_selected >= 0) status_bar.setText("<html><font
     * size = 2 face='Arial,Helvetica,Verdana,sans-serif'>Edge selected: <font
     * size = 2 color='#298C8C' face='Arial,Helvetica,Verdana,sans-serif'>" +
     * String.valueOf(my_nodes[second_selected].name) + " </font> --> <font size =
     * 2 color='#298C8C'> " + String.valueOf(my_nodes[selected_node].name) + "
     * </font>. Edge Value: <font size = 2 color='#298C8C'>" +
     * String.valueOf(AppRuntime.getCurrentNetwork().getValue(second_selected,
     * selected_node))); } else status_bar.setText(default_status); repaint();
     * colorize=false; } } } }
     */

    public void mouseMoved(MouseEvent e)
        {
        if (e == null)
            return;
        int where = whereAreWe(e.getX(), e.getY());
        if (where != -1)
            {
            // we are in actor zone
            colored_node = where;
            AppRuntime.setHTMLStatus(M01 + my_nodes[where].name);
            repaint();
            } else
            {
            colored_node = -1;
            if (colorize)
                {
                if (selected_node >= 0)
                    {
                    if (second_selected >= 0 && allow_ES) // edge selected
                        AppRuntime
                                .setHTMLStatus(M01
                                        + M05
                                        + M06
                                        + String
                                                .valueOf(my_nodes[second_selected].name)
                                        + M07
                                        + M08
                                        + M06
                                        + String
                                                .valueOf(my_nodes[selected_node].name)
                                        + M07
                                        + M09
                                        + M10
                                        + M06
                                        + String.valueOf(AppRuntime
                                                .getCurrentNetwork().getValue(
                                                        second_selected,
                                                        selected_node)));
                    else
                        // edge not selected
                        AppRuntime.setHTMLStatus(M01 + my_nodes[selected_node].name
                                + M12);
                    } else
                    AppRuntime.resetStatus();
                repaint();
                colorize = false;
                }
            }
        }

    public String getToolTipText(MouseEvent e)
        {
        if (e == null)
            return null;
        int where = whereAreWe(e.getX(), e.getY());
        if (where == -1)
            return null;
        else
            return M01 + my_nodes[where].name + M07;
        }

    // returns actor index if mouse over actor;
    // returns -1 if outside actors;
    private int whereAreWe(int x, int y)
        {
        int i, nx, ny, ns;
        for (i = 0; i < nodes_count; i++)
            {
            ns = my_nodes[i].getSize();
            nx = my_nodes[i].getX(area_width);
            ny = my_nodes[i].getY(area_width);
            if (nx <= x && x <= nx + ns && ny <= y && y <= ny + ns)
                {
                // we are in actor zone
                return i;
                }
            }
        return -1;
        }

    }