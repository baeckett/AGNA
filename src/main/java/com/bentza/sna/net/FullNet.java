package com.bentza.sna.net;

import com.bentza.sna.io.IOUtils;
import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import com.bentza.sna.gui.AgnaTableModel;
import com.bentza.sna.gui.MainFrame;
import com.bentza.sna.gui.GrNet;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

        public class FullNet // reuneste NodeArea si Network
    {
    public Network my_network;

    public NodeArea net_area;

    private String network_file_name;

    private boolean changed;

    private final char this_char = Environment.fs.charAt(0);

    private final char other_char = getOtherFS().charAt(0);

    // private String str; // string temporar de citire a fisierelor

    public FullNet()
        {
        network_file_name = "";
        changed = false;
        net_area = null;
        }

    public void createDefaultNetwork()
        {
        my_network = new Network();
        // if (isArea())
        net_area = null;
        network_file_name = "";
        changed = false;
        }

    public void createDefaultNetwork(int nn)
        {
        if (nn < 2)
            {
            nn = 2;
            }
        my_network = new Network(nn);
        if (isArea())
            net_area = null;
        network_file_name = "";
        changed = false;
        }

    public boolean getChanged()
        {
        return changed;
        }

    public void setChanged(boolean tmp_changed)
        {
        changed = tmp_changed;
        }

    public String getNetworkFileName()
        {
        return network_file_name;
        }

    public void setNetworkFileName(String tmp_name)
        {
        network_file_name = tmp_name;
        }

    public void writeInitialSettings()
        {
        try (Writer writer = IOUtils.writer(new File(
                    "AgnaDefaultSettings.ini")))
            {
            JTextPane tmp_pane = new JTextPane();
            tmp_pane.setText(getAgna2DefaultSettings(getArea()));
            tmp_pane.write(writer);
            } catch (Exception e)
            {
            AgnaLog.warn("FullNet.writeInitialSettings failed: " + e);
            }
        }

    public void readInitialSettings(NodeArea tmp_area, String file_name)
        {
        try (Reader reader = IOUtils.reader(new File(file_name)))
            {
            JTextPane tmp_pane = new JTextPane();
            tmp_pane.read(reader, null);
            if (tmp_area != null)
                {
                parseAgnaDefaultSettings(tmp_area, tmp_pane.getText());
                } else
                {
                parseAgnaNonGraphicDefaultSettings(tmp_pane.getText());
                }
            } catch (Exception ex) {
      AgnaLog.warn("suppressed exception", ex);
      }
        }

    public void readInitialSettings(String file_name)
        {
        // own NodeArea is implicit here
        // this method is called by GrNet
        readInitialSettings(getArea(), file_name);
        }

    // returns a text-string version of this FullNet ready to be saved
    // tab or comma-separated values
    public String getPlainTextNetwork(boolean tab_separated)
        {
        String lb = System.getProperty("line.separator");
        String separ;
        if (tab_separated)
            separ = "\t";
        else
            separ = ",";

        int nn = this.getNetwork().getSize();
        StringBuffer filestr = new StringBuffer("");
        float[][] mat; // data matrix
        mat = my_network.getMatrix();

        if (tab_separated)
            for (int i = 0; i < nn; i++)
                {
                for (int j = 0; j < nn; j++)
                    {
                    filestr.append(String.valueOf(mat[i][j]));
                    if (j < nn - 1)
                        filestr.append(separ);
                    else
                        filestr.append(lb);
                    }
                } // end for
        else
            { // comma-separated:
            String[] node_names = this.getNetwork().getNodeNamesInQuotes();
            // printing first line of names:
            filestr.append(separ); // matrix' upper-left corner remains empty
            for (int i = 0; i < nn; i++)
                {
                filestr.append(node_names[i]);
                if (i < nn - 1)
                    filestr.append(separ);
                else
                    filestr.append(lb);
                }

            // printing data:
            for (int i = 0; i < nn; i++)
                {
                filestr.append(node_names[i] + separ);
                for (int j = 0; j < nn; j++)
                    {
                    filestr.append(String.valueOf(mat[i][j]));
                    if (j < nn - 1)
                        filestr.append(separ);
                    else
                        filestr.append(lb);
                    }
                } // end for

            node_names = null;
            } // end else
        mat = null;
        return filestr.toString();
        }

    // returns a string representation of a color
    private static String parseColorToString(Color c)
        {
        // converts a byte b to 2-char hex string
        // with possible leading zero:
        // String s2 = Integer.toString( ( b & 0xff ) + 0x100, 16 /* radix*/ )
        // .substring( 1 );
        String s = "#";
        s += Integer.toString((c.getRed() & 0xff) + 0x100, 16).substring(1);
        s += Integer.toString((c.getGreen() & 0xff) + 0x100, 16).substring(1);
        s += Integer.toString((c.getBlue() & 0xff) + 0x100, 16).substring(1);
        return s;
        }

    // returns a Color from a String
    private static Color parseStringToColor(String s) throws ParsingError
        {

        // converts a hex String to internal binary:
        // int i = Integer.parseInt(g .trim(), 16 /* radix */ );
        if (s == null || s.length() < 1)
        // return Color.gray;
            {
            throw new ParsingError();
            }
        int r = Integer.parseInt(s.substring(1, 3).trim(), 16);
        int g = Integer.parseInt(s.substring(3, 5).trim(), 16);
        int b = Integer.parseInt(s.substring(5, 7).trim(), 16);
        return new Color(r, g, b);
        }

    // puts nodearea settings data into a writable string form:
    public String getAgna2DefaultSettings(NodeArea area)
        {
        String fs = System.getProperty("file.separator");
        String lb = "\n";
        StringBuffer filestr = new StringBuffer(""); // string to be returned
        // writing header data:
        filestr.append("Agna Default Settings" + lb);
        filestr.append("Version\t" + Environment.getApplicationVersion() + lb);

        if (area == null)
            {
            if (MainFrame.getWorkingDirectory() != null)
                filestr.append("Working Directory\t"
                        + MainFrame.getWorkingDirectory() + lb);
            return filestr.toString();
            }

        filestr.append("STG Enabled\t"
                + parseBooleanToString(area.isSTGEnabled()) + lb);

        filestr.append("Title Visible\t"
                + parseBooleanToString(area.isTitleVisible()) + lb);

        filestr.append("Edge Value Visible\t"
                + parseBooleanToString(area.isEdgeValueVisible()) + lb);

        filestr.append("Print Names\t"
                + parseBooleanToString(area.getPrintNames()) + lb);

        filestr.append("Grid Enabled\t"
                + parseBooleanToString(area.getGridEnabled()) + lb);

        filestr.append("Face Visible\t"
                + parseBooleanToString(area.isFaceVisible()) + lb);

        filestr.append("Allow Edge Selection\t"
                + parseBooleanToString(area.getAllowES()) + lb);

        filestr.append("Color Fidelity\t"
                + parseBooleanToString(area.getColorFidelity()) + lb);

        filestr.append("Area Width\t" + String.valueOf(area.getWidth()) + lb);

        filestr.append("Names X\t" + String.valueOf(area.getNamesX()) + lb);

        filestr.append("Names Y\t" + String.valueOf(area.getNamesY()) + lb);

        filestr.append("Title X\t" + String.valueOf(area.getTitleX()) + lb);

        filestr.append("Title Y\t" + String.valueOf(area.getTitleY()) + lb);

        filestr
                .append("Separator\t" + String.valueOf(area.getSeparator())
                        + lb);

        filestr.append("Edge Value Position\t"
                + String.valueOf(area.getEdgeValuePosition()) + lb);

        filestr.append("Grid Transparency\t"
                + String.valueOf(area.getGridTransparency()) + lb);

        filestr.append("Max Transparency\t"
                + String.valueOf(area.getMaxTransparency()) + lb);

        filestr.append("Background Image X\t"
                + String.valueOf(area.getBackgroundImageX()) + lb);

        filestr.append("Background Image Y\t"
                + String.valueOf(area.getBackgroundImageY()) + lb);

        filestr.append("Background Image Width\t"
                + String.valueOf(area.getBackgroundImageWidth()) + lb);

        filestr.append("Background Image Height\t"
                + String.valueOf(area.getBackgroundImageHeight()) + lb);

        filestr.append("Grid Space\t" + String.valueOf(area.getGridSpace())
                + lb);

        filestr.append("Edge Value Color\t"
                + parseColorToString(area.getEdgeValueColor()) + lb);

        filestr.append("Grid Color\t" + parseColorToString(area.getGridColor())
                + lb);

        filestr.append("Text Color\t"
                + parseColorToString(area.getNamesColor()) + lb);

        filestr.append("Title Color\t"
                + parseColorToString(area.getTitleColor()) + lb);

        filestr.append("Background Color\t"
                + parseColorToString(area.getBackgroundColor()) + lb);

        filestr.append("Arrow Color\t"
                + parseColorToString(area.getArrowColor()) + lb);

        if (!area.getBackgroundImageSource().equals("Node Faces"))
            filestr.append("Background Image File\t"
                    + area.getBackgroundImageSource() + lb);
        else
            filestr.append("Background Image File\t" + lb);

        filestr.append("Default Image Layout\t"
                + String.valueOf(area.getDefaultImageLayout()) + lb);

        if (MainFrame.getDefaultNodeFaceSource() != null
                || !(MainFrame.getDefaultNodeFaceSource()).equals("-"))
            filestr.append("Default Export Format\t"
                    + String.valueOf(MainFrame.getRememberedExportFormat())
                    + lb);
            filestr.append("Default Node Face\t"
                    + MainFrame.getDefaultNodeFaceSource() + lb);

        if (MainFrame.getWorkingDirectory() != null)
            filestr.append("Working Directory\t"
                    + MainFrame.getWorkingDirectory() + lb);

        return filestr.toString();
        }

    // returns an Agna2-string version of this FullNet ready to be saved
    public String getAgna2TextNetwork()
        {
        // String lb = System.getProperty("line.separator");
        String fs = System.getProperty("file.separator");
        String lb = "\n";
        int nn = my_network.getSize();
        int i = 0;
        // mat = my_network.getMatrix();
        StringBuffer filestr = new StringBuffer(""); // to be returned
        // writing general network data:
        filestr.append("Agna Data File" + lb);
        filestr.append("Version\t" + Environment.getApplicationVersion() + lb);
        filestr.append("Network Name\t" + my_network.getName() + lb);
        filestr.append("Network Size\t" + String.valueOf(nn) + lb);

        // writing isArea():
        filestr.append("Has Viewer\t" + parseBooleanToString(isArea()) + lb);

        filestr.append("Node Names" + lb);
        // writing node names:
        for (i = 0; i < nn; i++)
            {
            filestr.append(my_network.getActorName(i) + "\t");
            }
        filestr.append(lb + "End Node Names" + lb);

        filestr.append("Network Matrix" + lb);

        float[][] mat; // data matrix
        mat = my_network.getMatrix();

        // writing data matrix:
        for (i = 0; i < nn; i++)
            {
            for (int j = 0; j < nn; j++)
                {
                filestr.append(String.valueOf(mat[i][j]));
                if (j < nn - 1)
                    {
                    filestr.append("\t");
                    } else
                    {
                    filestr.append(lb);
                    }
                }
            }
        mat = null;

        filestr.append("End Network Matrix" + lb);

        if (!isArea())
            return filestr.toString();

        // writing NodeArea settings:

        filestr.append("STG Enabled\t"
                + parseBooleanToString(net_area.isSTGEnabled()) + lb);

        filestr.append("Title Visible\t"
                + parseBooleanToString(net_area.isTitleVisible()) + lb);

        filestr.append("Edge Value Visible\t"
                + parseBooleanToString(net_area.isEdgeValueVisible()) + lb);

        filestr.append("Print Names\t"
                + parseBooleanToString(net_area.getPrintNames()) + lb);

        filestr.append("Grid Enabled\t"
                + parseBooleanToString(net_area.getGridEnabled()) + lb);

        filestr.append("Face Visible\t"
                + parseBooleanToString(net_area.isFaceVisible()) + lb);

        filestr.append("Allow Edge Selection\t"
                + parseBooleanToString(net_area.getAllowES()) + lb);

        filestr.append("Color Fidelity\t"
                + parseBooleanToString(net_area.getColorFidelity()) + lb);

        filestr.append("Area Width\t" + String.valueOf(net_area.getWidth())
                + lb);

        filestr.append("Selected Node\t"
                + String.valueOf(net_area.getSelectedActor()) + lb);

        filestr.append("Second Selected Node\t"
                + String.valueOf(net_area.getSecondSelected()) + lb);

        filestr.append("Names X\t" + String.valueOf(net_area.getNamesX()) + lb);

        filestr.append("Names Y\t" + String.valueOf(net_area.getNamesY()) + lb);

        filestr.append("Title X\t" + String.valueOf(net_area.getTitleX()) + lb);

        filestr.append("Title Y\t" + String.valueOf(net_area.getTitleY()) + lb);

        filestr.append("Separator\t" + String.valueOf(net_area.getSeparator())
                + lb);

        filestr.append("Edge Value Position\t"
                + String.valueOf(net_area.getEdgeValuePosition()) + lb);

        filestr.append("Grid Transparency\t"
                + String.valueOf(net_area.getGridTransparency()) + lb);

        filestr.append("Max Transparency\t"
                + String.valueOf(net_area.getMaxTransparency()) + lb);

        filestr.append("Background Image X\t"
                + String.valueOf(net_area.getBackgroundImageX()) + lb);

        filestr.append("Background Image Y\t"
                + String.valueOf(net_area.getBackgroundImageY()) + lb);

        filestr.append("Background Image Width\t"
                + String.valueOf(net_area.getBackgroundImageWidth()) + lb);

        filestr.append("Background Image Height\t"
                + String.valueOf(net_area.getBackgroundImageHeight()) + lb);

        filestr.append("Grid Space\t" + String.valueOf(net_area.getGridSpace())
                + lb);

        filestr.append("Edge Value Color\t"
                + parseColorToString(net_area.getEdgeValueColor()) + lb);

        filestr.append("Grid Color\t"
                + parseColorToString(net_area.getGridColor()) + lb);

        filestr.append("Text Color\t"
                + parseColorToString(net_area.getNamesColor()) + lb);

        filestr.append("Title Color\t"
                + parseColorToString(net_area.getTitleColor()) + lb);

        filestr.append("Background Color\t"
                + parseColorToString(net_area.getBackgroundColor()) + lb);

        filestr.append("Arrow Color\t"
                + parseColorToString(net_area.getArrowColor()) + lb);

        if (!net_area.getBackgroundImageSource().equals("Node Faces"))
            filestr.append("Background Image File\t"
                    + net_area.getBackgroundImageSource() + lb);
        else
            filestr.append("Background Image File\t" + lb);

        // writing node faces:
        filestr.append("Node Faces" + lb);
        for (i = 0; i < nn; i++)
            {
            try
                {
                filestr.append(my_network.getActor(i).getRelativeFaceSource()
                        + "\t");
                } catch (Exception e2)
                {
                filestr.append("-\t");
                }
            }
        filestr.append(lb + "End Node Faces" + lb);

        // writing node coordinates and widths:
        filestr.append("Node Coordinates" + lb);
        Actor tmp_node = null;
        for (i = 0; i < nn; i++)
            {
            try
                {
                tmp_node = my_network.getActor(i);
                filestr.append(String.valueOf(tmp_node
                        .getX(net_area.getWidth()))
                        + "\t"
                        + String.valueOf(tmp_node.getY(net_area.getWidth()))
                        + "\t" + String.valueOf(tmp_node.getSize()) + "\t");
                } catch (Exception e3)
                {
                NodeXY tmp_xy = new NodeXY();
                filestr.append(String.valueOf(tmp_xy.getX()) + "\t"
                        + String.valueOf(tmp_xy.getY()) + "\t" + "16\t");
                }
            }
        filestr.append(lb + "End Node Coordinates" + lb);

        return filestr.toString();
        }

    // parses file-string given by MainFrame's openNetwork()
    public void readNetwork(String str, String extension)
        {
        String error_list = null;
        if (str.indexOf("Agna Data File") >= 0) // Agna 2.0 file
            {
            error_list = parseAgna2TextFile(str);
            } else if (extension.equals("agn") && str.indexOf("NODES:") >= 0) // Agna
                                                                                // 1.0
                                                                                // file
            parseAgna1TextFile(str);
        else if (extension.equals("txt") || extension.equals("text")
                || extension.equals("dat")) // text file
            parsePlainTextFile(str);
        else if (extension.equals("csv")) // csv file
            parseCommaTextFile(str);
        else if (extension.equals("net")) // Pajek file
            parsePajekFile(str);
        else if (extension.equals("ana")) // Ana file
            parseAnaTextFile(str);
        else
            parsePlainTextFile(str);
        if (error_list != null)
            {
            String report = "Open Network File report:\n" + error_list;
            // 2.1.3: headless-safe (no frame to attach a dialog to)
            if (MainFrame.getCurrentFrame() == null
                    || java.awt.GraphicsEnvironment.isHeadless())
                {
                AgnaLog.warn(report);
                } else
                {
                JOptionPane.showMessageDialog(MainFrame.getCurrentFrame(),
                        report, "Error List",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }


    // 2.1.3: minimal Pajek (.net) reader. Supports:
    //   *Vertices N
    //   idx "name" x y ...       (quoted or unquoted names)
    //   *Arcs / *Edges
    //   from to [value] ...      (extra columns ignored)
    private void parsePajekFile(String str)
        {
        int n = -1;
        Vector names = new Vector();
        Vector coords = new Vector(); // float[]{x, y} per vertex
        Vector faces = new Vector();  // String face source per vertex
        Vector arcs = new Vector();   // float[]{from, to, value}
        boolean in_vertices = false;
        boolean in_arcs = false;
        boolean directed = true;
        boolean list_format = false;

        String[] lines = str.replace("\r\n", "\n").split("\n");
        for (int li = 0; li < lines.length; li++)
            {
            String line = lines[li].trim();
            if (line.length() == 0 || line.startsWith("%"))
                continue; // blank or Pajek comment
            if (line.startsWith("*Vertices"))
                {
                in_vertices = true;
                in_arcs = false;
                list_format = false;
                try
                    {
                    n = Integer.parseInt(line.substring(9).trim().split(" ")[0]);
                    } catch (Exception e)
                    {
                    }
                }
            else if (line.startsWith("*Arcs"))
                {
                in_arcs = true;
                in_vertices = false;
                directed = true;
                list_format = line.startsWith("*Arcslist");
                }
            else if (line.startsWith("*Edges"))
                {
                in_arcs = true;
                in_vertices = false;
                directed = false;
                list_format = line.startsWith("*Edgeslist");
                }
            else if (in_vertices)
                {
                // only index-prefixed vertex lines carry data
                if (Character.isDigit(line.charAt(0)))
                    {
                    names.addElement(parsePajekName(line));
                    float[] pos = parsePajekCoords(line);
                    coords.addElement(pos);
                    faces.addElement(parsePajekFace(line));
                    }
                }
            else if (in_arcs)
                {
                String[] tok = line.split("\\s+");
                if (list_format && tok.length >= 3)
                    {
                    // "v k s1 s2 ... sk" adjacency list
                    try
                        {
                        int from = Integer.parseInt(tok[0]) - 1;
                        for (int si = 2; si < tok.length; si++)
                            {
                            int to = Integer.parseInt(tok[si]) - 1;
                            arcs.addElement(new float[] { from, to, 1f });
                            }
                        } catch (Exception e)
                        {
                        AgnaLog.warn("ignoring malformed Pajek list line: " + line);
                        }
                    }
                else if (tok.length >= 2)
                    {
                    try
                        {
                        int from = Integer.parseInt(tok[0]) - 1;
                        int to = Integer.parseInt(tok[1]) - 1;
                        float value = 1f;
                        if (tok.length >= 3 && !isPajekLineAttr(tok[2]))
                            {
                            value = Float.parseFloat(tok[2]);
                            }
                        arcs.addElement(new float[] { from, to, value });
                        } catch (Exception e)
                        {
                        AgnaLog.warn("ignoring malformed Pajek arc line: " + line);
                        }
                    }
                }
            }

        if (n < 2)
            {
            n = Math.max(n, names.size());
            }
        if (n < 2)
            {
            AgnaLog.warn("Pajek file contains no usable network.");
            return;
            }
        my_network = new Network(n);
        for (int i = 0; i < names.size() && i < n; i++)
            {
            my_network.setNodeName((String) names.elementAt(i), i);
            }
        // 2.1.3: apply imported coordinates and face styles when a viewer is
        // available; otherwise they are kept on the actors' coordinate data
        applyPajekVertexData(coords, faces);
        for (int a = 0; a < arcs.size(); a++)
            {
            float[] arc = (float[]) arcs.elementAt(a);
            int from = (int) arc[0];
            int to = (int) arc[1];
            if (from < 0 || from >= n || to < 0 || to >= n)
                continue;
            my_network.setValue(arc[2], from, to);
            if (!directed)
                {
                my_network.setValue(arc[2], to, from);
                }
            }
        }


    // stores imported coordinates/faces on the network actors (used when a
    // viewer/area exists)
    private void applyPajekVertexData(Vector coords, Vector faces)
        {
        if (my_network == null)
            return;
        int area_width = 400;
        if (isArea() && net_area != null)
            {
            area_width = net_area.getWidth();
            }
        int size = my_network.getSize();
        for (int i = 0; i < size; i++)
            {
            Actor actor = my_network.getActor(i);
            if (actor == null)
                continue;
            if (i < coords.size())
                {
                float[] pos = (float[]) coords.elementAt(i);
                // Pajek coordinates are often 0..1; scale up to the viewer
                float scale = (pos[0] <= 1.0f && pos[1] <= 1.0f)
                        ? (float) area_width : 1f;
                actor.moveActor((int) (pos[0] * scale),
                        (int) (pos[1] * scale), area_width, false);
                }
            if (i < faces.size())
                {
                String face = (String) faces.elementAt(i);
                if (face != null)
                    {
                    actor.setFace(face);
                    }
                }
            }
        }

    // parses "ic Color shape Shape [bc ...]" into a bundled face source, or
    // null when nothing matches (2.1.3: Pajek -> Agna face round-trip)
    private String parsePajekFace(String line)
        {
        String color = "Red";
        String shape = "Bullet";
        boolean shadow = false;
        String[] tok = line.split("\\s+");
        for (int i = 2; i < tok.length - 1; i++)
            {
            if (tok[i].equals("ic"))
                {
                String c = tok[i + 1];
                if (c.equals("Green") || c.equals("Blue"))
                    {
                    color = c;
                    }
                }
            else if (tok[i].equals("shape"))
                {
                String s = tok[i + 1];
                if (s.equals("box"))
                    shape = "Square";
                else if (s.equals("cross"))
                    shape = "Star";
                else if (s.equals("triangle"))
                    shape = "Triangle";
                else if (s.equals("diamond"))
                    shape = "Man";
                }
            else if (tok[i].equals("bc") && tok[i + 1].equals("Gray"))
                {
                shadow = true;
                }
            }
        String face_name = color + " " + shape + (shadow ? " Shadow" : "")
                + ".gif";
        java.io.File face_file = new java.io.File(
                com.bentza.sna.Environment.getFacesDirectory()
                        + java.io.File.separator + "Light Background"
                        + java.io.File.separator + face_name);
        if (face_file.exists())
            {
            try
                {
                return face_file.getCanonicalPath();
                } catch (Exception e)
                {
                }
            }
        return null;
        }

    // parses "idx "name" x y ..." into float[]{x, y}, defaults (0,0)
    private float[] parsePajekCoords(String line)
        {
        float[] pos = new float[] { 0f, 0f };
        String[] tok = line.split("\\s+");
        if (tok.length >= 4)
            {
            try
                {
                pos[0] = Float.parseFloat(tok[2]);
                pos[1] = Float.parseFloat(tok[3]);
                } catch (NumberFormatException e)
                {
                }
            }
        return pos;
        }

    // true when the token starts a Pajek line attribute (color `c`, width    // true when the token starts a Pajek line attribute (color `c`, width
    // `w`, style `s`, label `l`) - i.e. the arc line carried no value
    private static boolean isPajekLineAttr(String token)
        {
        return token.equals("c") || token.equals("w") || token.equals("s")
                || token.equals("l");
        }

    private String parsePajekName(String line)
        {
        // "quoted name" possibly followed by coordinates
        int q1 = line.indexOf('"');
        if (q1 >= 0)
            {
            int q2 = line.indexOf('"', q1 + 1);
            if (q2 > q1)
                {
                return line.substring(q1 + 1, q2);
                }
            }
        String[] tok = line.split("\\s+");
        if (tok.length >= 2)
            {
            return tok[1];
            }
        return "Node " + line;
        }

    // parses chain-string and creates a network out of it
    public void readNetworkFromChain(String str, String extension)
        {
        parseTextChain(str);
        }

    private static boolean parseStringToBoolean(String word)
            throws ParsingError
        {
        // 2.1.3: an absent optional setting (parseFindNextWord -> null) is
        // 'no', not a crash
        if (word == null)
            {
            return false;
            }
        if (word.equals("yes"))
            return true;
        else if (word.equals("no"))
            return false;
        else
            throw new ParsingError();
        }

    private String parseBooleanToString(boolean is)
        {
        if (is)
            return "yes";
        return "no";
        }

    // returns the next word as a substring string of str
    private static String parseFindNextWord(String str, String value_name)
        {
        int out = 0;
        int len = str.length();
        StringBuffer tmpstr = new StringBuffer("");
        int in = str.indexOf(value_name);
        if (in < 0 || in >= len)
            return null;
        // throw new ParsingError();
        in += value_name.length();

        // finding beginning of integer
        while ((int) str.charAt(in) < 32 && in < len)
            {
            in++;
            }

        // finding end of integer
        while ((int) str.charAt(in) >= 32 && in < len) // while
                                                        // ((int)str.charAt(in)>=48
                                                        // &&
                                                        // (int)str.charAt(in)<=57)
            {
            tmpstr.append(str.charAt(in));
            in++;
            }
        return tmpstr.toString();
        }

    public static void parseAgnaNonGraphicDefaultSettings(String str)
        {
        // finding default node face source:
        String tmp_val = parseFindNextWord(str, "Default Node Face");
        if (tmp_val != null && !tmp_val.equals("-"))
            MainFrame.setDefaultNodeFaceSource(tmp_val);
        tmp_val = parseFindNextWord(str, "Working Directory");
        if (tmp_val != null && !tmp_val.equals("-"))
            MainFrame.setWorkingDirectory(tmp_val);
        tmp_val = parseFindNextWord(str, "Default Export Format");
        if (tmp_val != null && !tmp_val.equals("-"))
            {
            try
                {
                MainFrame.setRememberedExportFormat(Integer.parseInt(tmp_val));
                } catch (NumberFormatException e)
                {
                AgnaLog.warn("invalid Default Export Format value: " + tmp_val);
                }
            }
        }

    // parses nodearea data from a string:
    public static void parseAgnaDefaultSettings(NodeArea area, String str)
        {
        // new settings:
        // Default Face
        int nn = 0;
        int i = 0; // initial position
        int j = 0; // final position
        int len = str.length();
        String tmpname = "";
        Vector val = null;

        int tmp_width = 400;
        // finding area width:
        try
            {
            tmp_width = Integer.parseInt(parseFindNextWord(str, "Area Width"));
            if (tmp_width > 10)
                area.setWidth(tmp_width);
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }

        // finding area settings:
        String tempstring;

        try
            {
            area.setSTGEnabled(parseStringToBoolean(parseFindNextWord(str,
                    "STG Enabled")));
            } catch (ParsingError e)
            {
            }

        // finding default node face source:
        tempstring = parseFindNextWord(str, "Default Node Face");
        if (tempstring.length() > 1)
            area.setDefaultNodeFaceSource(tempstring);

        try
            {
            area.setTitleVisibility(parseStringToBoolean(parseFindNextWord(str,
                    "Title Visible")));
            } catch (ParsingError e)
            {
            }

        try
            {
            area.setEdgeValueVisible(parseStringToBoolean(parseFindNextWord(
                    str, "Edge Value Visible")));
            } catch (ParsingError e)
            {
            }

        try
            {
            area.setPrintNames(parseStringToBoolean(parseFindNextWord(str,
                    "Print Names")));
            } catch (ParsingError e)
            {
            }

        try
            {
            area.setGridEnabled(parseStringToBoolean(parseFindNextWord(str,
                    "Grid Enabled")));
            } catch (ParsingError e)
            {
            }

        try
            {
            area.setFacesVisible(parseStringToBoolean(parseFindNextWord(str,
                    "Face Visible")));
            } catch (ParsingError e)
            {
            }

        try
            {
            area.setAllowES(parseStringToBoolean(parseFindNextWord(str,
                    "Allow Edge Selection")));
            } catch (ParsingError e)
            {
            }

        try
            {
            area.setColorFidelity(parseStringToBoolean(parseFindNextWord(str,
                    "Color Fidelity")));
            } catch (ParsingError e)
            {
            }

        tmpname = parseFindNextWord(str, "Background Image File");
        if (tmpname.length() > 1)
            area.setBackgroundImage(tmpname);

        // finding default image layout:
        try
            {
            nn = Integer
                    .parseInt(parseFindNextWord(str, "Default Image Layout"));
            area.setDefaultImageLayout((byte) nn);
            } catch (Exception e)
            {
            area.setDefaultImageLayout(NodeArea.CIRCLE_LAYOUT);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Names X"));
            area.setNamesX(nn);
            } catch (Exception e)
            {
            area.setNamesX(3);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Names Y"));
            area.setNamesY(nn);
            } catch (Exception e)
            {
            area.setNamesY(-2);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Title X"));
            area.setTitleX(nn);
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Title y"));
            area.setTitleY(nn);
            } catch (Exception e)
            {
            area.setTitleX(10);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Separator"));
            area.setSeparator(nn);
            } catch (Exception e)
            {
            area.setTitleY(10);
            }

        try
            {
            nn = Integer
                    .parseInt(parseFindNextWord(str, "Edge Value Position"));
            area.setEdgeValuePosition(nn);
            } catch (Exception e)
            {
            area.setEdgeValuePosition(30);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Grid Transparency"));
            area.setGridTransparency(nn);
            } catch (Exception e)
            {
            area.setGridTransparency(25);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Max Transparency"));
            area.setMaxTransparency(nn);
            } catch (Exception e)
            {
            area.setMaxTransparency(35);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Background Image X"));
            area.setBackgroundImageX(nn);
            } catch (Exception e)
            {
            area.setBackgroundImageX(0);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Background Image Y"));
            area.setBackgroundImageY(nn);
            } catch (Exception e)
            {
            area.setBackgroundImageY(0);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str,
                    "Background Image Width"));
            area.setBackgroundImageWidth(nn);
            } catch (Exception e)
            {
            area.setBackgroundImageWidth(400);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str,
                    "Background Image Height"));
            area.setBackgroundImageHeight(nn);
            } catch (Exception e)
            {
            area.setBackgroundImageHeight(400);
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Grid Space"));
            area.setGridSpace(nn);
            } catch (Exception e)
            {
            area.setGridSpace(60);
            }

        tempstring = parseFindNextWord(str, "Edge Value Color");
        try
            {
            area.setEdgeValueColor(parseStringToColor(tempstring));
            } catch (ParsingError e2)
            {
            }

        tempstring = parseFindNextWord(str, "Grid Color");
        try
            {
            area.setGridColor(parseStringToColor(tempstring));
            } catch (ParsingError e2)
            {
            }

        tempstring = parseFindNextWord(str, "Text Color");
        try
            {
            area.setNamesColor(parseStringToColor(tempstring));
            } catch (ParsingError e2)
            {
            }

        tempstring = parseFindNextWord(str, "Title Color");
        try
            {
            area.setTitleColor(parseStringToColor(tempstring));
            } catch (ParsingError e2)
            {
            }

        tempstring = parseFindNextWord(str, "Background Color");
        try
            {
            area.setBackgroundColor(parseStringToColor(tempstring));
            } catch (ParsingError e2)
            {
            }

        tempstring = parseFindNextWord(str, "Arrow Color");
        try
            {
            area.setArrowColor(parseStringToColor(tempstring));
            } catch (ParsingError e2)
            {
            }

        }

    private void parseAgna1TextFile(String str)
        {
        str += "\n";
        int nn = 0;
        int i = 0; // initial position
        int j = 0; // final position
        int len = str.length();
        String tmpname = "";
        Vector val = null;

        // finding size:
        i = 0;
        j = str.indexOf("NODES:");
        if (j > 0 && j < len)
            {
            parseAnaTextFile(str.substring(i, j));
            if (my_network != null)
                {
                nn = my_network.getSize();
                // finding names of nodes:
                i = j + 6;
                if (i > 0 && i < len)
                    {
                    val = parseStringVectorFromString(str.substring(i, len));
                    if (nn != val.size())
                        return;
                    for (i = 0; i < nn; i++)
                        {
                        my_network.getActor(i).setName(
                                (String) val.elementAt(i));
                        }
                    }
                }
            } else
            return;
        }

    private String getOtherFS()
        {
        if (Environment.fs.equals("/"))
            return "\\";
        else
            return "/";
        }

    private String parseAgna2TextFile(String str)
        {
        int nn = 0;
        int i = 0; // initial position
        int j = 0; // final position
        int len = str.length();
        String tmpname = "";
        Vector val = null;
        StringBuffer errors = new StringBuffer("");

        // finding size of network:
        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Network Size"));
            } catch (Exception e)
            {
            errors.append("\nNetwork size not found.");
            return errors.toString();
            }
        if (nn < 2)
            {
            errors.append("\nInvalid network size value.");
            return errors.toString();
            }

        my_network = new Network(nn);

        // finding name of network:
        tmpname = parseFindNextWord(str, "Network Name");
        if (tmpname.length() > 0)
            my_network.setName(tmpname);

        // finding names of nodes:
        i = str.indexOf("Node Names") + 10;
        j = str.indexOf("End Node Names");

        // is_err = true if "Error setting Node Name" occurres at least once
        boolean is_err = false;
        if (i > 0 && i < len)
            {
            if (j > 0)
                {
                val = parseStringVectorFromString(str.substring(i, j));
                if (val.size() != my_network.getSize())
                    {
                    errors
                            .append("\nErrors encountered on reading Node Names.");
                    }

                for (i = 0; i < val.size(); i++)
                    {
                    try
                        {
                        my_network.getActor(i).setName(
                                (String) val.elementAt(i));
                        } catch (Exception e1)
                        {
                        if (!is_err)
                            {
                            errors.append("\nError setting Node Name.");
                            is_err = true;
                            }
                        }
                    }
                } else
                {
                // error here
                errors.append("\nNode Names not found.");
                }

            }
        // finding matrix:
        i = str.indexOf("Network Matrix") + 14;
        j = str.indexOf("End Network Matrix");
        if (i > 0 && j > 0 && i < len)
            {
            val = parseVectorFromString(str.substring(i, j), true);
            nn = val.size();
            if (nn != (int) Math.sqrt(nn) * (int) Math.sqrt(nn)
                    || my_network.getSize() != (int) Math.sqrt(nn))
                {
                // notify here the existence of errors!
                errors.append("\nError reading sociomatrix size.");
                }
            nn = (int) Math.sqrt(nn);
            is_err = false;
            for (i = 0; i < nn; i++)
                {
                for (j = 0; j < nn; j++)
                    {
                    // my_network.setStringValue((String)val.elementAt(i * nn +
                    // j), i, j);
                    try
                        {
                        my_network.setObjectValue(val.elementAt(i * nn + j), i,
                                j);
                        } catch (Exception e2)
                        {
                        if (!is_err)
                            {
                            errors
                                    .append("\nSociomatrix data contains errors.");
                            is_err = true;
                            }
                        }
                    }
                }
            } else
            {
            // errors here
            errors.append("\nSociomatrix data not found.");
            }

        // finding isArea():
        try
            {
            parseStringToBoolean(parseFindNextWord(str, "Has Viewer"));
            attachArea();
            } catch (ParsingError e)
            {
            errors.append("\nInvalid image settings.");
            return errors.toString();
            }

        // finding faces of nodes:
        Actor tmp_node = null;
        i = str.indexOf("Node Faces") + 10;
        j = str.indexOf("End Node Faces");
        File test_file = null;

        if (i > 0 && j > 0 && i < len)
            {
            val = parseStringVectorFromString(str.substring(i, j));
            is_err = false;
            for (i = 0; i < val.size(); i++)
                {
                tmpname = null;
                tmpname = (String) val.elementAt(i);
                tmp_node = my_network.getActor(i);

tmp_node.setFace(tmpname);
                // 2.1.3: the icon-width check was unreliable (an icon may
                // report -1 before its media loads); "source is '-'" already
                // means the file could not be resolved
                if (tmp_node.getFaceSource().equals("-"))
                    {
                    // switching file separator in file path:
                    tmpname = tmpname.replace(other_char, this_char);
                    tmp_node.setFace(tmpname);

                    if (tmp_node.getFaceSource().equals("-"))
                        {
                        // if could by no means read face from file:
                        tmp_node.setFace(MainFrame.getDefaultNodeFaceSource());
                        if (!is_err)
                            {
                            errors
                                    .append("\nErrors encountered on reading Node Faces.");
                            is_err = true;
                            }
                        }
                    }
                }
            }
        int tmp_width = 400;
        // finding area width:
        try
            {
            tmp_width = Integer.parseInt(parseFindNextWord(str, "Area Width"));
            net_area.setWidthSimply(tmp_width);
            } catch (Exception e)
            {
            errors.append("\nInvalid Image Width.");
            tmp_width = 400;
            net_area.setWidthSimply(tmp_width);
            }

        // finding node coordinates and widths:
        i = str.indexOf("Node Coordinates") + 16;
        j = str.indexOf("End Node Coordinates");
        if (i > 0 && i < len)
            {
            if (j > 0)
                {
                val = parseVectorFromString(str.substring(i, j), true);
                nn = val.size();
                if (nn == 3 * (int) ((float) nn / 3)
                        && my_network.getSize() == (int) ((float) nn / 3))
                    {
                    nn = my_network.getSize();
                    for (i = 0; i < nn; i++)
                        {
                        tmp_node = my_network.getActor(i);
                        try
                            {
                            tmp_node.setSize(((Float) val.elementAt(3 * i + 2))
                                    .intValue());
                            tmp_node.setX(((Float) val.elementAt(3 * i))
                                    .intValue(), tmp_width);
                            tmp_node.setY(((Float) val.elementAt(3 * i + 1))
                                    .intValue(), tmp_width);
                            } catch (Exception e2)
                            {
                            errors
                                    .append("\nErrors encountered on reading Node Coordinates.");
                            }
                        // node size cannot exceed 80 % of area width
                        if (tmp_node.getSize() > (int) ((float) net_area
                                .getWidth() * 0.8f))
                            tmp_node.setSize(16);
                        }
                    tmp_node = null;
                    } else
                    {
                    errors.append("\nNode Coordinates not found.");
                    }
                } else
                {
                errors.append("\nNode Coordinates not found.");
                }
            }

        // finding area settings:

        try
            {
            net_area.setSTGEnabled(parseStringToBoolean(parseFindNextWord(str,
                    "STG Enabled")));
            } catch (ParsingError e)
            {
            errors.append("\nInvalid Snap-To-Grid parameter.");
            }

        try
            {
            net_area.setTitleVisibility(parseStringToBoolean(parseFindNextWord(
                    str, "Title Visible")));
            } catch (ParsingError e)
            {
            errors.append("\nInvalid View-Title parameter.");
            }

        try
            {
            net_area
                    .setEdgeValueVisible(parseStringToBoolean(parseFindNextWord(
                            str, "Edge Value Visible")));
            } catch (ParsingError e)
            {
            errors.append("\nInvalid Show-Edge-Value parameter.");
            }

        try
            {
            net_area.setPrintNames(parseStringToBoolean(parseFindNextWord(str,
                    "Print Names")));
            } catch (ParsingError e)
            {
            errors.append("\nInvalid Show-Names parameter.");
            }

        try
            {
            net_area.setGridEnabled(parseStringToBoolean(parseFindNextWord(str,
                    "Grid Enabled")));
            } catch (ParsingError e)
            {
            errors.append("\nInvalid Show-Grid parameter.");
            }

        try
            {
            net_area.setAllowES(parseStringToBoolean(parseFindNextWord(str,
                    "Allow Edge Selection")));
            } catch (ParsingError e)
            {
            errors.append("\nInvalid Allow-Edge-Selection parameter.");
            }

        try
            {
            net_area.setColorFidelity(parseStringToBoolean(parseFindNextWord(
                    str, "Color Fidelity")));
            } catch (ParsingError e)
            {
            errors.append("\nInvalid Edge-Loyalty parameter.");
            }

        tmpname = parseFindNextWord(str, "Background Image File");
        if (tmpname.length() > 0)
            net_area.setBackgroundImage(tmpname);
        else
            {
            errors.append("\nInvalid Background Picture path.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Selected Node"));
            net_area.setSelectedActor(nn);
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Second Selected"));
            if (net_area.getAllowES())
                net_area.setSecondSelected(nn);
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Names X"));
            net_area.setNamesX(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Names-X parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Names Y"));
            net_area.setNamesY(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Names-Y parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Title X"));
            net_area.setTitleX(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Title-X parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Title Y"));
            net_area.setTitleY(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Title-Y parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Separator"));
            net_area.setSeparator(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Edge Separation parameter.");
            }

        try
            {
            nn = Integer
                    .parseInt(parseFindNextWord(str, "Edge Value Position"));
            net_area.setEdgeValuePosition(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Edge Value Position parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Grid Transparency"));
            net_area.setGridTransparency(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Grid Transparency parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Max Transparency"));
            net_area.setMaxTransparency(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Most-Faded-Edge parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Background Image X"));
            net_area.setBackgroundImageX(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Background-Image-X parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Background Image Y"));
            net_area.setBackgroundImageY(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Background-Image-Y parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str,
                    "Background Image Width"));
            net_area.setBackgroundImageWidth(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Background-Image-Width parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str,
                    "Background Image Height"));
            net_area.setBackgroundImageHeight(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Background-Image-Height parameter.");
            }

        try
            {
            nn = Integer.parseInt(parseFindNextWord(str, "Grid Space"));
            net_area.setGridSpace(nn);
            } catch (Exception e)
            {
            errors.append("\nInvalid Grid Space parameter.");
            }

        try
            {
            net_area.setEdgeValueColor(parseStringToColor(parseFindNextWord(
                    str, "Edge Value Color")));
            } catch (ParsingError e2)
            {
            errors.append("\nInvalid Edge Value Color parameter.");
            }

        try
            {
            net_area.setGridColor(parseStringToColor(parseFindNextWord(str,
                    "Grid Color")));
            } catch (ParsingError e2)
            {
            errors.append("\nInvalid Grid Color parameter.");
            }

        try
            {
            net_area.setNamesColor(parseStringToColor(parseFindNextWord(str,
                    "Text Color")));
            } catch (ParsingError e2)
            {
            errors.append("\nInvalid Text Color parameter.");
            }

        try
            {
            net_area.setTitleColor(parseStringToColor(parseFindNextWord(str,
                    "Title Color")));
            } catch (ParsingError e2)
            {
            errors.append("\nInvalid Title Color parameter.");
            }

        try
            {
            net_area.setBackgroundColor(parseStringToColor(parseFindNextWord(
                    str, "Background Color")));
            } catch (ParsingError e2)
            {
            errors.append("\nInvalid Background Color parameter.");
            }

        try
            {
            net_area.setArrowColor(parseStringToColor(parseFindNextWord(str,
                    "Arrow Color")));
            } catch (ParsingError e3)
            {
            errors.append("\nInvalid Edge Color parameter.");
            }
        if (errors.length() < 1)
            return null;

        if (errors != null)
            return errors.toString();
        else
            return null;

        }

    // returns the index of a string element in vector;
    // -1 if not found;
    // probably unused;
    private int getIndexOfStringInVector(Vector tmp_vector, String tmp_string)
        {
        int i = 0;
        final int size = tmp_vector.size();
        while (i < size)
            {
            if (tmp_string.equals((String) tmp_vector.elementAt(i)))
                {
                return i;
                }
            i++;
            }
        return -1;
        }

    private void parseTextChain(String str)
        {
        int len = str.length();
        String tmpstr = "";
        int in = 0;
        int out = 0;
        int i, j;
        my_network = new Network(2);
        my_network.getActor(0).setName("Agna 2 Temporary Node Number One");
        my_network.getActor(1).setName("Agna 2 Temporary Node Number Two");
        j = 1; // nodes 0 and 1 are empty
        while (in < len && out < len)
            {
            // finding beginning of sequence
            while (in < len && (int) str.charAt(in) < 32)
                {
                in++;
                }

            out = in;
            // finding end of sequence
            while (out < len && (int) str.charAt(out) >= 32)
                {
                out++;
                }
            tmpstr = str.substring(in, out);
            if (tmpstr.length() > 0)
                {
                i = my_network.getActor(tmpstr);
                if (i == -1) // new sequence found
                    {
                    my_network.addActor(tmpstr);
                    i = my_network.getSize() - 1;
                    }
                my_network.setValue(my_network.getValue(j, i) + 1, j, i); // growing
                                                                            // value
                                                                            // by 1
                j = i;
                }
            in = out + 1;
            }

        if (my_network.getSize() > 4) // deleting first two empty nodes
            {
            my_network.deleteActor(my_network
                    .getActor("Agna 2 Temporary Node Number One"));
            my_network.deleteActor(my_network
                    .getActor("Agna 2 Temporary Node Number Two"));
            }
        return;
        }

    // parses txt, dat & text files
    private void parsePlainTextFile(String str)
        {
        str += "\t";
        int ni = 0;
        // vector of new values:
        Vector val = parseVectorFromString(str, true);
        if (val == null)
            {
            JOptionPane.showMessageDialog(MainFrame.getCurrentFrame(),
                    "Agna could not read data from specified file.",
                    "Error reading", JOptionPane.ERROR_MESSAGE);
            return;
            }
        ni = val.size();
        // simple matrix
        if (ni == (int) Math.sqrt(ni) * (int) Math.sqrt(ni))
            {
            ni = (int) Math.sqrt(ni);
            my_network = new Network(ni);
            for (int i = 0; i < ni; i++)
                {
                for (int j = 0; j < ni; j++)
                    {
                    my_network.setObjectValue(val.elementAt(i * ni + j), i, j);
                    }
                }
            }

        // ana-style file (n * n + 1):
        else if ((ni == (int) Math.sqrt(ni - 1) * (int) Math.sqrt(ni - 1) + 1)) // &&
                                                                                // (ni
                                                                                // ==
                                                                                // Integer.parseInt((String)val.elementAt(0)))
            {
            ni = Integer.parseInt((String) val.elementAt(0));
            my_network = new Network(ni);
            for (int i = 0; i < ni; i++)
                {
                for (int j = 0; j < ni; j++)
                    {
                    my_network.setObjectValue(val.elementAt(i * ni + j + 1), i,
                            j);
                    }
                }

            }

        else
            // errors here
            {
            if (JOptionPane.showOptionDialog(MainFrame.getCurrentFrame(),
                    "This file contains errors. Attempt to read it anyway?",
                    "Error parsing", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE, null, null, null) != 0)
                return;

            ni = (int) Math.sqrt(ni);
            my_network = new Network(ni);
            for (int i = 0; i < ni; i++)
                {
                for (int j = 0; j < ni; j++)
                    {
                    my_network.setObjectValue(val.elementAt(i * ni + j), i, j);
                    }
                }
            }

        }

    // parses csv files
    private void parseCommaTextFile(String str)
        {
        int ni = 0;
        int i = 0;
        // vector of new values:
        Vector val = parseVectorFromString(str, false);
        ni = val.size();

        // matrix values plus names:
        int tmp_n = (int) Math.sqrt(ni + 1) - 1;
        if (ni == tmp_n * tmp_n + 2 * tmp_n + 1)
            {

            ni = tmp_n;
            my_network = new Network(ni);
            for (i = 1; i <= ni; i++)
                {
                // reading names
                my_network.setNodeName((String) val.elementAt(i), i - 1);
                }

            float tmp_val = 0f;
            for (i = 1; i <= ni; i++)
                {
                for (int j = 1; j <= ni; j++)
                    {
                    try
                        {
                        tmp_val = Float.parseFloat((String) val.elementAt(i
                                * (ni + 1) + j));
                        my_network.setValue(tmp_val, i - 1, j - 1);
                        } catch (Exception e2) {
      AgnaLog.warn("suppressed exception", e2);
      }

                    }
                }
            } else if (ni == (int) Math.sqrt(ni) * (int) Math.sqrt(ni))
            {
            // simple matrix (no names)
            ni = (int) Math.sqrt(ni);
            my_network = new Network(ni);
            for (i = 0; i < ni; i++)
                {
                for (int j = 0; j < ni; j++)
                    {
                    my_network.setObjectValue(val.elementAt(i * ni + j), i, j);
                    }
                }
            }

        else
            // errors here
            {
            if (JOptionPane.showOptionDialog(MainFrame.getCurrentFrame(),
                    "This file contains errors. Attempt to read it anyway?",
                    "Error parsing", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE, null, null, null) != 0)
                return;

            ni = (int) Math.sqrt(ni);
            my_network = new Network(ni);
            for (i = 0; i < ni; i++)
                {
                for (int j = 0; j < ni; j++)
                    {
                    my_network.setObjectValue(val.elementAt(i * ni + j), i, j);
                    }
                }
            }

        }

    // ana-style file:
    private void parseAnaTextFile(String str)
        {
        str += "\t";
        int ni = 0;
        // vector of new values:
        Vector val = parseVectorFromString(str, true);
        if (val == null)
            return;
        ni = val.size();
        if (ni > 2
                && (ni == (int) Math.sqrt((float) ni - 1)
                        * (int) Math.sqrt((float) ni - 1) + 1)) // && (ni ==
                                                                // Integer.parseInt((String)val.elementAt(0)))
            {
            try
                {
                float tmp_val = Float.parseFloat(val.elementAt(0).toString());
                ni = (int) tmp_val;
                } catch (NumberFormatException e1)
                {
                return;
                }
            if (ni < 3)
                return;
            my_network = new Network(ni);
            for (int i = 0; i < ni; i++)
                {
                for (int j = 0; j < ni; j++)
                    {
                    my_network.setObjectValue(val.elementAt(i * ni + j + 1), i,
                            j);
                    }
                }
            } else
            return;
        }

    // returns a vector of float from string str if tab_separated
    // and a vector of strings if comma-separated
    private Vector parseVectorFromString(String str, boolean tab_separated)
        {
        // vector of new values:
        Vector val = new Vector();
        StringBuffer tmpstr = new StringBuffer("");
        float tmp_val = 0;
        int i = 0;
        int j = 0;
        int ni = 0;
        int len = str.length();
        char tmp_char;
        final char separ = ',';
        final char quote = '"';

        if (tab_separated)
            {
            // Fixed in 2.1.3: empty cells (consecutive tabs, e.g. a missing
            // diagonal in a text matrix) used to be silently dropped, which
            // shifted every following value one column left and made the
            // matrix size wrong. Cells are now split preserving empties and an
            // empty cell becomes 0.0. Lines are split on \n / \r; a trailing
            // tab does not produce an extra cell.
            for (int row = 0; row < str.length();)
                {
                // find end of current line
                int line_end = row;
                while (line_end < str.length() && str.charAt(line_end) != '\n'
                        && str.charAt(line_end) != '\r')
                    line_end++;
                String line = str.substring(row, line_end);
                // advance past the line break (and a CRLF pair)
                row = line_end;
                if (row < str.length() && str.charAt(row) == '\r')
                    row++;
                if (row < str.length() && str.charAt(row) == '\n')
                    row++;

                if (line.trim().length() == 0)
                    continue; // blank or whitespace-only line

                String[] cells = line.split("\t", -1);
                for (int k = 0; k < cells.length; k++)
                    {
                    // a trailing tab marks the end of a row, not an empty cell
                    if (k == cells.length - 1 && cells[k].length() == 0)
                        continue;
                    String cell = cells[k].trim();
                    try
                        {
                        tmp_val = cell.length() == 0 ? 0f : Float
                                .parseFloat(cell);
                        val.addElement(new Float(tmp_val));
                        } catch (NumberFormatException e)
                        {
                        // malformed cell: treat as 0.0 (previously the token
                        // was dropped, corrupting the matrix size)
                        val.addElement(new Float(0f));
                        }
                    }
                }
            }

        if (!tab_separated) // comma-separated
            for (i = 0; i < len; i++)
                {
                tmp_char = str.charAt(i);
                if (tmp_char == quote)
                    {
                    // finding nex quote:
                    try
                        {
                        j = str.indexOf(tmp_char, i + 1);
                        if (j > i)
                            {
                            val.addElement(str.substring(i + 1, j));
                            i = j + 1;
                            }
                        } catch (Exception e1) {
      AgnaLog.warn("suppressed exception", e1);
      }
                    } else if ((int) tmp_char < 32)
                    {
                    // end of row character
                    if (tmpstr.length() > 0)
                        {
                        val.addElement(tmpstr.toString());
                        tmpstr.delete(0, tmpstr.length());
                        }

                    } else if (tmp_char == separ)
                    {
                    // comma
                    val.addElement(tmpstr.toString());
                    tmpstr.delete(0, tmpstr.length());
                    } else
                    {
                    // building value
                    tmpstr.append(tmp_char);
                    }

                }

        // last value, if no separator after:
        if (tmpstr.length() > 0)
            {
            val.addElement(tmpstr.toString());
            }

        tmpstr = null;
        ni = val.size();
        if (ni > 1)
            {
            return val;
            } else
            return null;
        }

    // returns a vector of strings (eg, node names) from string str
    private Vector parseStringVectorFromString(String str)
        {
        // vector of new values:
        Vector val = new Vector();
        String tmpstr = null;
        float tmp_val = 0;
        int i = 0;
        int j = 0;
        int ni = 0;
        int len = str.length();

        tmpstr = "";
        for (i = 0; i < len; i++)
            {
            if ((int) str.charAt(i) < 32)
                {
                if (tmpstr.length() > 0)
                    {
                    try
                        {
                        // tmp_val = Float.parseFloat(tmpstr);
                        val.addElement(tmpstr);
                        } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                    }
                tmpstr = "";
                } else
                {
                tmpstr += str.charAt(i);
                }

            }
        ni = val.size();
        if (ni > 1)
            {
            return val;
            } else
            return null;
        }

    public void deleteNodeToNetwork(int tmp_i)
        {
        my_network.deleteActor(tmp_i);
        if (isArea())
            {
            net_area.setSelectedActor(-1);
            net_area.setSecondSelected(-1);
            net_area.updateArea(my_network);
            GrNet.setDefaultStatus();
            }
        }

    public void isolateNodeToNetwork(int tmp_i)
        {
        my_network.isolateActor(tmp_i);
        if (isArea())
            {
            net_area.updateArea(my_network);
            }
        }

    /**
     * Adds a single node to Network; updates NodeArea
     */
    public void addOneNodeToNetwork(int x, int y, int max)
        {
        final boolean is_area = this.isArea();
        my_network.addActor(x, y, max, is_area);
        if (isArea())
            {
            net_area.updateArea(my_network);
            // net_area.getSelectedActor()=my_network.getSize()-1;
            }
        setChanged(true);
        }

    /**
     * Adds a number of nodes to Network; updates NodeArea position not
     * specified;
     */
    public void addNodesToNetwork(int number_of_nodes)
        {
        if (number_of_nodes <= 0)
            return;

        final boolean is_area = this.isArea();
        my_network.addActors(number_of_nodes, is_area);
        if (isArea())
            {
            net_area.updateArea(my_network);
            // net_area.getSelectedActor()=my_network.getSize()-1;
            }
        setChanged(true);
        }

    public void removeOutsidersInNetwork(AgnaTableModel tmp_model)
        {
        my_network.removeOutsiders(tmp_model);
        if (isArea())
            {
            net_area.updateArea(my_network);
            net_area.setSelectedActor(my_network.getSize() - 1);
            }
        setChanged(true);
        }

    public void addScalar(float tmp_scalar)
        {
        AgnaLib agna_lib = new AgnaLib();
        agna_lib.addScalar(my_network, tmp_scalar);
        if (isArea())
            net_area.updateArea(my_network);
        setChanged(true);
        agna_lib = null;
        }

    public void multiplyByScalar(float tmp_scalar)
        {
        AgnaLib agna_lib = new AgnaLib();
        agna_lib.multiplyByScalar(my_network, tmp_scalar);
        if (isArea())
            net_area.updateArea(my_network);
        setChanged(true);
        agna_lib = null;
        }

    public void square()
        {
        AgnaLib agna_lib = new AgnaLib();
        my_network.setMatrix(agna_lib.multiplyNetworks(my_network, my_network));
        if (isArea())
            net_area.updateArea(my_network);
        setChanged(true);
        agna_lib = null;
        }

    public void symmetrize(JFrame tmp_frame)
        {
        // AgnaLib.AgnaLib();
        AgnaLib agna_lib = new AgnaLib();
        Object[] values = { "Maximum", "Minimum", "Maximum Non-Zero",
                "Minimum Non-Zero", "Below Diagonal", "Above Diagonal",
                "Below Diagonal Non-Zero", "Above Diagonal Non-Zero", "Sum",
                "Product", "Product Non-Zero", "Arithmetical Mean",
                "Geometrical Mean" };
        String tmp_str = (String) JOptionPane.showInputDialog(tmp_frame,
                "Symmetrize network by:", "Symmetrization Options",
                JOptionPane.QUESTION_MESSAGE, null, values, "Maximum");
        if (tmp_str == null)
            return;

        else if (tmp_str.equals("Maximum"))
            agna_lib.symmetrizeMaximum(my_network);

        else if (tmp_str.equals("Minimum"))
            agna_lib.symmetrizeMinimum(my_network);

        else if (tmp_str.equals("Maximum Non-Zero"))
            agna_lib.symmetrizeMaximumNonZero(my_network);

        else if (tmp_str.equals("Minimum Non-Zero"))
            agna_lib.symmetrizeMinimumNonZero(my_network);

        else if (tmp_str.equals("Below Diagonal"))
            agna_lib.symmetrizeBelow(my_network);

        else if (tmp_str.equals("Above Diagonal"))
            agna_lib.symmetrizeAbove(my_network);

        else if (tmp_str.equals("Below Diagonal Non-Zero"))
            agna_lib.symmetrizeBelowNonZero(my_network);

        else if (tmp_str.equals("Above Diagonal Non-Zero"))
            agna_lib.symmetrizeAboveNonZero(my_network);

        else if (tmp_str.equals("Sum"))
            agna_lib.symmetrizeSum(my_network);

        else if (tmp_str.equals("Product"))
            agna_lib.symmetrizeProduct(my_network);

        else if (tmp_str.equals("Product Non-Zero"))
            agna_lib.symmetrizeProductNonZero(my_network);

        else if (tmp_str.equals("Arithmetical Mean"))
            agna_lib.symmetrizeAM(my_network);

        else if (tmp_str.equals("Geometrical Mean"))
            agna_lib.symmetrizeGM(my_network);

        if (isArea())
            net_area.updateArea(my_network);
        setChanged(true);
        agna_lib = null;
        }

    public void transpose()
        {
        AgnaLib agna_lib = new AgnaLib();
        agna_lib.transpose(my_network);
        if (isArea())
            net_area.updateArea(my_network);
        setChanged(true);
        agna_lib = null;
        }

    public void normalize()
        {
        AgnaLib agna_lib = new AgnaLib();
        agna_lib.normalize(my_network);
        if (isArea())
            net_area.updateArea(my_network);
        setChanged(true);
        agna_lib = null;
        }

    public void setValueToNetwork(float tmp_value, int i, int j)
        {
        my_network.setValue(tmp_value, i, j);
        if (isArea())
            net_area.updateArea(my_network);
        setChanged(true);
        }

    public void cloneNodeToNetwork()
        {
        int tmp_i = net_area.getSelectedActor();
        if (tmp_i >= 0 && isArea())
            {
            my_network.cloneActor(tmp_i, net_area.getWidth());
            }
        net_area.updateArea(my_network);
        setChanged(true);
        }

    public void cleaning()
        {
        getNetwork().cleaning();
        network_file_name = null;
        my_network = null;
        net_area = null;
        }

    public Network getNetwork()
        {
        return my_network;
        }

    public NodeArea getArea()
        {
        if (isArea())
            return net_area;
        return null;
        }

    public void attachArea()
        {
        net_area = new NodeArea(my_network);
        setChanged(true);
        }

    public boolean isArea() // exista deja un NodeArea nenul?
        {
        if (net_area == null)
            return false;
        else
            return true;
        }
    }