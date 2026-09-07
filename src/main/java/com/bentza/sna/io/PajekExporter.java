package com.bentza.sna.io;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Actor;
import com.bentza.sna.net.Network;
import com.bentza.sna.gui.MainFrame;
import java.awt.Color;
import java.lang.StringBuffer;
import java.io.File;
import java.util.Date;
import java.util.Vector;
import jxl.*;
import jxl.write.*;

/**
 * A class that returns a network as a MS Excel String ready to be saved as a
 * file. UNFINISHED!
 */
public class PajekExporter
    {

    /**
     * Constructor
     */
    public PajekExporter()
        {
        }

    private StringBuffer getArcsDescription(int size, FullNet full_net, int i,
            Actor cursor_actor, Color arrow_color)
        {
        StringBuffer out = new StringBuffer("");
        String edges_mat_runner = null;
        final String blanc = " ";
        for (int j = 0; j < size; j++)
            {
            edges_mat_runner = String.valueOf((byte) full_net.getArea()
                    .getEdgesMat(i, j));
            out.append(String.valueOf(i + 1) + blanc + String.valueOf(j + 1)
                    + blanc + cursor_actor.emissions.elementAt(j) + blanc + "c"
                    + " rgba(" + String.valueOf(arrow_color.getRed()) + ","
                    + String.valueOf(arrow_color.getGreen()) + ","
                    + String.valueOf(arrow_color.getBlue()) + ","
                    + edges_mat_runner + ")" + "\n");
            }
        return out;
        }

    /**
     * returns a String as a Pajek representation of a network.
     */
    public String getPajekNetwork(FullNet tmp_full_net)
        {

        try
            {
            Thread.sleep(500);
            MainFrame.progress_dialog.setPercent(60);
            } catch (Exception e1)
            {
            }

        final Network tmp_network = tmp_full_net.getNetwork();

        // edge alpha channel:
        String edges_mat_runner = "255";

        Color arrow_color = null;
        final boolean is_area = tmp_full_net.isArea();
        if (is_area)
            {
            arrow_color = tmp_full_net.getArea().getArrowColor();
            }

        final int size = tmp_network.getSize();
        final Vector nodes = tmp_network.getNodes();
        final String blanc = " ";
        Actor cursor_actor = null;
        float tmp_value = 0f;
        final float hundred = 100f;

        // first stores vertices info,
        // then receives arcs as well.
        StringBuffer out = new StringBuffer("*Vertices " + String.valueOf(size)
                + "\n");
        StringBuffer arcs = new StringBuffer("*Arcs\n");
        StringBuffer face_description = new StringBuffer("");
        String face_path = null;

        if (is_area)
            {
            for (int i = 0; i < size; i++)
                {
                cursor_actor = (Actor) nodes.elementAt(i);
                face_path = cursor_actor.getFaceSource();

                // finding colour:
                if (face_path.indexOf("Red") > 0)
                    face_description.append("ic Red ");
                else if (face_path.indexOf("Green") > 0)
                    face_description.append("ic Green ");
                else if (face_path.indexOf("Blue") > 0)
                    face_description.append("ic Blue ");
                else
                    face_description.append("ic Red ");

                // finding shape:
                if (face_path.indexOf("Bullet") > 0)
                    face_description.append("shape ellipse ");
                else if (face_path.indexOf("Square") > 0)
                    face_description.append("shape box ");
                else if (face_path.indexOf("Star") > 0)
                    face_description.append("shape cross ");
                else if (face_path.indexOf("Man") > 0
                        || face_path.indexOf("Woman") > 0)
                    face_description.append("shape diamond ");
                else
                    face_description.append("shape ellipse ");

                // finding shade:
                if (face_path.indexOf("Shadow") > 0
                        || face_path.indexOf("Shaddow") > 0)
                    face_description.append("bc Gray ");

                // adding info on actor i:
                out.append(String.valueOf(i + 1) + blanc
                        + cursor_actor.getNameInQuotes() + blanc
                        + String.valueOf(cursor_actor.getX() / hundred) + blanc
                        + String.valueOf(cursor_actor.getY() / hundred) + blanc
                        + face_description + "\n");
                } // end for
            } // end if
        else
            { // no area here:
            for (int i = 0; i < size; i++)
                {
                cursor_actor = (Actor) nodes.elementAt(i);
                // adding info on actor i:
                out.append(String.valueOf(i + 1) + blanc
                        + cursor_actor.getNameInQuotes() + "\n");
                } // end for
            }// end else

        out.append(arcs);
        return out.toString();
        }

    // End of class
    }