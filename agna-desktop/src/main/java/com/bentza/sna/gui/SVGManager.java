package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.io.IOUtils;
import com.bentza.sna.Environment;
import com.bentza.sna.net.NodeArea;
import com.bentza.sna.net.Actor;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

/**
 * A class responsible with exporting network images to SVG; it takes the
 * current NodeArea and generates SVG code accordingly;
 */
class SVGManager
    {
    private String area_w, files_folder;

    private NodeArea this_area;

    private Actor[] my_nodes;

    private int nodes_count;

    private int[][] edges_mat;

    public SVGManager()
        {
        area_w = "";
        }

    /**
     * Returns the header of the SVG file
     */
    public String getHeader()
        {
        return "<?xml version=\"1.0\" standalone=\"no\"?>"
                + "\n<!-- Generator: "
                + Environment.getApplicationFullName()
                + " -->"
                + "\n<!DOCTYPE svg PUBLIC "
                + "\"-//W3C//DTD SVG 20001102//EN\" "
                + "\"http://www.w3.org/TR/2000/CR-SVG-20001102/DTD/svg-20001102.dtd\">"
                + "\n";
        }

    /**
     * Returns the tags: svg, desc and title
     */
    public String getSVGSpace()
        {
        area_w = String.valueOf(this_area.getWidth());
        return "<svg viewbox= \"0 0 " + String.valueOf(area_w) + " "
                + String.valueOf(area_w) + "\" >" + "\n<desc>"
                + MainFrame.getCurrentFullNet().getNetwork().getName()
                + "</desc>" + "\n<title>" + this_area.getTitle() + "</title>"
                + "\n";
        }

    /**
     * Returns the code for background rectangle and background picture (based
     * on external file)
     */
    public StringBuffer getBackground()
        {
        Color back_color = this_area.getBackgroundColor();
        StringBuffer back_string = new StringBuffer("");

        back_string
                .append("<!--   BACKGROUND COLOR RECTANGLE: --> \n<rect style=\"fill:rgb("
                        + String.valueOf(back_color.getRed())
                        + ","
                        + String.valueOf(back_color.getGreen())
                        + ","
                        + String.valueOf(back_color.getBlue())
                        + ")\" x=\"0\"  y=\"0\" width=\""
                        + area_w
                        + "\" height=\""
                        + area_w
                        + "\"/>"
                        + "\n"
                        // border:
                        + "<!--   IMAGE FRAME RECTANGLE: --> \n<rect fill=\"none\" stroke=\"gray\" x=\"0\"  y=\"0\" width=\""
                        + area_w + "\" height=\"" + area_w + "\"/>" + "\n");

        String tmp_path = this_area.getBackgroundImageSource();
        if (tmp_path == null || tmp_path.equals("-")
                || this_area.getBackgroundImage() == null)
            return back_string;

        back_string.append("<!--   BACKGROUND PICTURE: --> \n<image x=\""
                + String.valueOf(this_area.getBackgroundImageX()) + "\" y=\""
                + String.valueOf(this_area.getBackgroundImageY())
                + "\" width=\""
                + String.valueOf(this_area.getBackgroundImageWidth())
                + "\" height=\""
                + String.valueOf(this_area.getBackgroundImageHeight())
                + "\" xlink:href=\"" + files_folder + "/"
                + IOUtils.getNameWithoutPath(tmp_path) + "\" /> " + "\n");

        return back_string;
        }

    /**
     * Returns the tag for image title
     */
    public String getImageTitle()
        {
        if (!this_area.isTitleVisible())
            return "";
        Color tmp_color = this_area.getTitleColor();
        return "<text><tspan x=\"" + String.valueOf(this_area.getTitleX())
                + "\" y=\"" + String.valueOf(this_area.getTitleY())
                + "\" style=\"fill:rgb(" + String.valueOf(tmp_color.getRed())
                + "," + String.valueOf(tmp_color.getGreen()) + ","
                + String.valueOf(tmp_color.getBlue()) + ")\"> "
                + this_area.getTitle() + "</tspan> </text>" + "\n";

        }

    /**
     * Returns the tags corresponding to node faces and node names; node faces
     * are referenced by external files;
     */
    public StringBuffer getNodes()
        {
        StringBuffer nodes_string = new StringBuffer("");
        int i, tmp_x, tmp_y, tmp_size;
        int area_width = this_area.getWidth();
        tmp_x = 0;
        tmp_y = 0;
        tmp_size = 0;
        i = 0;
        int names_x = this_area.getNamesX();
        int names_y = this_area.getNamesY();
        boolean print_names = this_area.getPrintNames();
        boolean faces_visible = this_area.isFaceVisible();
        Color names_color = this_area.getNamesColor();
        nodes_string.append("<!--   NODES: --> \n");
        for (i = 0; i < nodes_count; i++)
            {
            tmp_x = my_nodes[i].getX(area_width);
            tmp_y = my_nodes[i].getY(area_width);
            tmp_size = my_nodes[i].getSize();
            if (faces_visible)
                nodes_string.append("<image x=\""
                        + String.valueOf(tmp_x)
                        + "\" y=\""
                        + String.valueOf(tmp_y)
                        + "\" width=\""
                        + String.valueOf(my_nodes[i].getFaceWidth())
                        + "\" height=\""
                        + String.valueOf(my_nodes[i].getFaceHeight())

                        + "\" xlink:href=\""
                        + files_folder
                        + "/"
                        + IOUtils.getNameWithoutPath(my_nodes[i]
                                .getFaceSource()) + "\" /> " + "\n");

            // painting node names
            if (print_names)
                nodes_string.append("<text><tspan x=\""
                        + String.valueOf(tmp_x + tmp_size + names_x)
                        + "\" y=\""
                        + String.valueOf(tmp_y + tmp_size + names_y)
                        + "\" style=\"fill:rgb("
                        + String.valueOf(names_color.getRed()) + ","
                        + String.valueOf(names_color.getGreen()) + ","
                        + String.valueOf(names_color.getBlue()) + ")\"> "
                        + my_nodes[i].name + "</tspan> </text>" + "\n");
            }

        return nodes_string;
        }

    /**
     * returns the tags corresponding to edge lines
     */
    public StringBuffer getEdges()
        {
        StringBuffer edges_file = new StringBuffer("");
        my_nodes = this_area.getNodes();
        nodes_count = my_nodes.length;
        edges_mat = this_area.getEdgesMat();
        int separ = this_area.getSeparator();
        int area_width = this_area.getWidth();
        int small_i, small_j;
        edges_file.append("<!--   EDGES: --> \n");

        for (int i = 0; i < nodes_count; i++)
            {
            for (int j = 0; j < nodes_count; j++)
                {
                small_i = (int) (my_nodes[i].getSize() / 2);
                small_j = (int) (my_nodes[j].getSize() / 2);
                if (edges_mat[i][j] != 0)
                    {
                    if (separ > 0 && edges_mat[i][j] != 0
                            && edges_mat[j][i] != 0)
                        {
                        edges_file.append(getDoubleArrow(my_nodes[i]
                                .getX(area_width)
                                + small_i, my_nodes[i].getY(area_width)
                                + small_i, my_nodes[j].getX(area_width)
                                + small_j, my_nodes[j].getY(area_width)
                                + small_j, (byte) edges_mat[i][j], i, j));
                        } else
                        {
                        edges_file.append(getSimpleArrow(my_nodes[i]
                                .getX(area_width)
                                + small_i, my_nodes[i].getY(area_width)
                                + small_i, my_nodes[j].getX(area_width)
                                + small_j, my_nodes[j].getY(area_width)
                                + small_j, (byte) edges_mat[i][j], i, j));
                        }
                    }
                }
            }

        return edges_file;
        }

    /**
     * Returns the tag corresponding to a double arrow; by calling
     * getSimpleArrow();
     */
    public StringBuffer getDoubleArrow(int x1, int y1, int x2, int y2,
            byte tmp_transparency, int i_node, int j_node)
        {
        double cos_alfa, sin_alfa, raza;
        int separ = this_area.getSeparator();
        raza = Math
                .sqrt(((double) x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        cos_alfa = 2 * ((double) x2 - x1) / raza;
        sin_alfa = 2 * ((double) y2 - y1) / raza;
        return getSimpleArrow(x1 + (int) (sin_alfa * separ), y1
                - (int) (cos_alfa * separ), x2 + (int) (sin_alfa * separ), y2
                - (int) (cos_alfa * separ), tmp_transparency, i_node, j_node);
        }

    /**
     * Returns the tags corresponding to a unidirectional arrow and the edge
     * value text;
     */
    public StringBuffer getSimpleArrow(int x1, int y1, int x2, int y2,
            byte tmp_transparency, int i_node, int j_node)
        {
        StringBuffer arrow_string = new StringBuffer("");
        int r_i, r, new_x1, new_x2, new_y1, new_y2;
        double radical, fractie, sin15, cos15;
        float transparency = 1f;
        Color edge_value_color = this_area.getEdgeValueColor();
        Color arrow_color = this_area.getArrowColor();
        boolean print_edge_value = this_area.isEdgeValueVisible();
        int edge_value_position = this_area.getEdgeValuePosition();
        if (this_area.getColorFidelity() == false)
            transparency = 255;
        else
            {
            transparency = (float) tmp_transparency / 255f;
            }

        fractie = 0;
        r = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        radical = (double) r;
        radical = Math.sqrt(r);
        r_i = my_nodes[i_node].getSize() * 3 / 4;
        r = my_nodes[j_node].getSize() * 3 / 4; // r = razele bulinutzelor

        // coordonate pentru edge_value:
        new_x1 = (int) ((double) x1 + (double) edge_value_position / 100
                * (x2 - x1));
        new_y1 = (int) ((double) y1 + (double) edge_value_position / 100
                * (y2 - y1));
        // deseneaza edge_value:
        if (print_edge_value)
            arrow_string.append("<text><tspan x=\""
                    + String.valueOf(new_x1)
                    + "\" y=\""
                    + String.valueOf(new_y1)
                    + "\" style=\"fill:rgb("
                    + String.valueOf(edge_value_color.getRed())
                    + ","
                    + String.valueOf(edge_value_color.getGreen())
                    + ","
                    + String.valueOf(edge_value_color.getBlue())
                    + ")\"> "
                    + String.valueOf(MainFrame.getCurrentNetwork().getValue(
                            i_node, j_node)) + "</tspan> </text>" + "\n");

        // coordonate pt linia lunga:
        new_x1 = (int) ((double) x1 + r_i * (x2 - x1) / radical);
        new_y1 = (int) ((double) y1 + r_i * (y2 - y1) / radical);
        new_x2 = (int) ((double) x2 - r * (x2 - x1) / radical);
        new_y2 = (int) ((double) y2 - r * (y2 - y1) / radical);
        // drage linia lunga:
        arrow_string.append("<path stroke-width=\"1\" style=\"stroke:rgb("
                + String.valueOf(arrow_color.getRed()) + ","
                + String.valueOf(arrow_color.getGreen()) + ","
                + String.valueOf(arrow_color.getBlue()) + "); opacity:"
                + String.valueOf(transparency) + "\" " + "d=\"M "
                + String.valueOf(new_x1) + " " + String.valueOf(new_y1) + " L "
                + String.valueOf(new_x2) + " " + String.valueOf(new_y2)
                + "\" />" + "\n");

        // sin(15)=0.2588190451; cos(15)=0.9659258262;
        sin15 = 0.2588190451;
        cos15 = 0.9659258262;
        r = (new_x2 - new_x1) * (new_x2 - new_x1) + (new_y2 - new_y1)
                * (new_y2 - new_y1);
        radical = (double) r;
        radical = Math.sqrt(r);
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
            int[] xx = { x1, x2, new_x2 };
            int[] yy = { y1, y2, new_y2 };
            arrow_string.append("<path style=\"fill:rgb("
                    + String.valueOf(arrow_color.getRed()) + ","
                    + String.valueOf(arrow_color.getGreen()) + ","
                    + String.valueOf(arrow_color.getBlue()) + "); opacity:"
                    + String.valueOf(transparency) + "\" " + "d=\"M "
                    + String.valueOf(x1) + " " + String.valueOf(y1) + " L "
                    + String.valueOf(x2) + " " + String.valueOf(y2) + " L"
                    + String.valueOf(new_x2) + " " + String.valueOf(new_y2)
                    + " Z\" />" + "\n");

            }

        return arrow_string;
        }

/**
     * Copies a file from one location to a different location on disk; A method
     * taken from Sun:
     * http://developer.java.sun.com/developer/onlineTraining/new2java/supplements/2001/CopyFile.java
     */
    public void copyFile(String infile, String outfile)
        {
        // 2.1.3: buffered copy with try-with-resources (was a byte-by-byte
        // read/write loop, very slow for large images)
        try (FileInputStream fis = new FileInputStream(infile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                FileOutputStream fos = new FileOutputStream(outfile);
                BufferedOutputStream bos = new BufferedOutputStream(fos))
            {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = bis.read(buffer)) != -1)
                {
                bos.write(buffer, 0, read);
                }
            } catch (IOException e)
            {
            AgnaLog.warn("SVGManager.copyFile failed: " + infile
                    + " -> " + outfile + " (" + e.getMessage() + ")");
            }
        }

    /**
     * saves all images from ImageStock into given directory; used to create
     * external files
     */
    public void saveAllImages(File directory)
        {
        if (directory != null && directory.exists() && directory.isDirectory())
            {
            String[] image_paths = MainFrame.getCurrentImageStock()
                    .getAllImageSources();
            String directory_path = null;
            try
                {
                directory_path = directory.getCanonicalPath();
                } catch (IOException e3)
                {
                return;
                } catch (SecurityException e4)
                {
                return;
                }

            final String f_s = Environment.fs;
            File tmp_file = null;

            // saving background image (because not included in image_paths):
            String back_path = this_area.getBackgroundImageSource();
            if (back_path != null && !back_path.equals("-"))
                {
                tmp_file = new File(back_path);
                copyFile(back_path, directory_path + f_s + tmp_file.getName());
                }

            // saving rest of images:

            if (image_paths == null || image_paths.length == 0)
                return;
            final int array_size = image_paths.length;
            for (int i = 0; i < array_size; i++)
                {
                try
                    {
                    tmp_file = new File(image_paths[i]);
                    copyFile(image_paths[i], directory_path + f_s
                            + tmp_file.getName());
                    } catch (NullPointerException e1)
                    {
                    }
                }

            }
        }

    /**
     * Returns the content of a SVG file from the current NodeArea data; the
     * file path must contain a file name plus svg extension; creates an
     * auxiliary folder with image files for reference from svg code;
     */
    public String getSVGContent(NodeArea tmp_area, File new_file)
        {
        if (tmp_area == null)
            return null;

        this_area = tmp_area;

        // auxiliary folder to be created
        files_folder = IOUtils.getNameWithoutExtension(new_file.getName())
                + " files";

        // creates auxiliary folder only if faces are visible
        // and there are image files in ImageStock:
        if (this_area.isFaceVisible()
                && MainFrame.getCurrentImageStock().getSize() > 0)
            try
                {
                // creating new folder for image files:
                File new_folder = new File(new_file.getParent()
                        + Environment.fs + files_folder);
                if (!new_folder.exists())
                    {
                    try
                        {
                        if (new_folder.mkdir())
                            saveAllImages(new_folder); // saving all image
                                                        // files into new folder
                        } catch (SecurityException e1)
                        {
                        }
                    }
                } catch (SecurityException e20)
                {
                } catch (NullPointerException e30)
                {
                }

        StringBuffer content = new StringBuffer("");

        content.append(this.getHeader());

        content.append(this.getSVGSpace());

        content.append(this.getBackground());

        content.append(this.getImageTitle());

        content.append(this.getEdges());

        content.append(this.getNodes());

        // end:

        content.append("</svg>\n");

        return content.toString();

        }

    }