package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.io.HTMLParser;
import com.bentza.sna.io.IOUtils;
import com.bentza.sna.Environment;
import com.bentza.sna.net.NodeArea;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.gui.filter.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import com.bentza.sna.io.JpegUtils;

        public class GrNet
    {
    public static JFrame gr_frame;

    public boolean gr_exists;

    public static NodeArea my_area;

    public static boolean update_network_needed;

    public static JPanel gControlArea;

    public static JScrollPane scroll_graph;

    public static Dimension dim_frame, dim_inner_frame, max_dim;

    public static Container gContent;

    public static JPanel area_panel;

    private static FullNet gr_full_net;

    private static AgnaTableModel gr_model;

    private static AgnaTextPane gr_output_pane;

    private KeyStroke up_x_key, down_x_key, up_y_key, down_y_key,
            up_edge_value_key, down_edge_value_key;

    private static JMenu gFile, gImage, e_names_properties, e_grid, e_title,
            gNodes, n_set_face, gData, gPreferences, gHelp;

    private static JMenuItem f_open, f_save_as, f_simply_save, f_insert,
            f_quit, e_image_width, e_scale_image, e_background_color,
            e_background_image, e_background_x, e_background_y,
            e_scale_background, e_fit_background_to_image, e_background_width,
            e_background_height, e_no_background_image, e_edge_separator,
            e_edge_color, e_max_transparency, e_show_edge_value,
            e_hide_edge_value, e_edge_value_position, e_edge_value_color,
            e_view_title, e_hide_title, e_set_title, e_title_x, e_title_y,
            e_title_color, e_set_loyalty, e_remove_loyalty, e_view_names,
            e_hide_names, e_names_color, e_names_font, e_view_faces,
            e_hide_faces, e_change_faces, e_set_edge, d_add_node, d_clone_node,
            d_isolate_node, d_delete_node, d_transpose, d_symmetrize,
            d_remove_outsiders, n_set_x, n_set_y, e_random_layout,
            e_circular_layout, n_change_name, n_face_width, n_change_face,
            e_change_faces_width, n_no_face, n_default_face,
            n_select_next_node, n_select_next_arrow, e_names_x, e_names_y,
            e_view_grid, e_hide_grid, e_grid_step, e_grid_color,
            e_grid_transparency, e_stg, e_dstg, e_allow_ES, e_disallow_ES,
            p_default_face, p_default_style, p_copy_style, h_contents, h_about;

    private static JMenuBar g_mb;

    private static JToolBar gtools;

    private static ImageIcon gi_tool1, gi_tool2, gi_tool3, gi_tool_separator;

    private static JButton gtool_edge_color, gtool_view_names,
            gtool_names_color, gtool_loyalty, gtool_image_width,
            gtool_select_next, gtool_export_image, gtool_insert_in_output,
            gtool_allow_edge_selection, gtool_show_connection_value,
            gtool_add_nodes, gtool_delete_nodes, gtool_circular_layout,
            gtool_random_layout, border_button;

    private Dimension dim_tool;

    private static JTextField gfield_x, gfield_y, gfield_separator,
            gfield_edge_value;

    private static DoubleButton gtool_separator, gtool_change_x,
            gtool_change_y, gtool_edge_value;

    private static JLabel glabel_x, glabel_y, glabel_edge_value;

    private static JPanel tool_panel, status_panel, left_panel;

    private static JScrollPane left_scroll;

    private static JLabel status_bar;

    private static VerticalToolBar vertical_tools;

    private FlowLayout status_layout;

    private JFileChooser file_chooser;

    final static String DEFAULT_STATUS = "<html><font size = 2 face='Arial,Helvetica,Verdana,sans-serif'>Drag and drop nodes to change position.";

    final ImageIcon main_icon;

    private WindowListener gr_window_listener = new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
                {
                gr_frame.dispose();
                gr_frame = null;
                gr_exists = false;
                MainFrame.setDisabledCloseViewerMenu();
                
                }

            public void windowActivated(WindowEvent e)
                {
                if (!update_network_needed)
                    return;
                update_network_needed = false;
                if (MainFrame.checkAllValues())
                    MainFrame.updateNetwork();
                // MainFrame.updateNetwork();
                if (gr_full_net.isArea())
                    my_area.updateArea(MainFrame.getCurrentNetwork());
                int new_width = my_area.getWidth();
                Dimension new_dimension = new Dimension(new_width, new_width);
                my_area.setPreferredSize(new_dimension);
                if (gr_full_net.getNetworkFileName().equals(""))
                    gr_frame.setTitle(MainFrame.getCurrentNetwork().getName()
                            + " - Network Viewer - "
                            + Environment.getApplicationFullName());
                else
                    gr_frame.setTitle(IOUtils.getNameWithoutExtension(MainFrame
                            .getCurrentFullNet().getNetworkFileName())
                            + " - Network Viewer - "
                            + Environment.getApplicationFullName());
                try
                    {
                    my_area.paintEdges();
                    } catch (Exception ex) {
      AgnaLog.warn("suppressed exception", ex);
      }

                area_panel.setPreferredSize(new Dimension(new_width + 10,
                        new_width + 10));
                area_panel.revalidate();
                MainFrame.setEnabledCloseViewerMenu();
                // my_area.repaint();
                }
        };

    private ActionListener act_change_image = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {

                // export current image as:
                if (e.getSource() == f_save_as
                        || e.getSource() == gtool_export_image)
                    {
                    doSaveImageAs();
                    }

                // insert image in output file:
                if (e.getSource() == f_insert
                        || e.getSource() == gtool_insert_in_output)
                    {
                    doInsertInOutput();
                    }

                // view names
                if (e.getSource() == gtool_view_names
                        || e.getSource() == e_view_names
                        || e.getSource() == e_hide_names)
                    {
                    doViewNames();
                    }

                // change edge color
                if (e.getSource() == gtool_edge_color
                        || e.getSource() == e_edge_color)
                    {
                    doChangeEdgeColor();
                    }

                // change loyalty maximum transparency value
                if (e.getSource() == e_max_transparency)
                    {
                    doMaxTransparency();
                    }

                // show/hide edge value:
                if (e.getSource() == e_show_edge_value
                        || e.getSource() == e_hide_edge_value
                        || e.getSource() == gtool_show_connection_value)
                    {
                    doShowEdgeValue();
                    }

                // change edge value position
                if (e.getSource() == e_edge_value_position)
                    {
                    doEdgeValuePosition();
                    }

                // change edge value color
                if (e.getSource() == e_edge_value_color)
                    {
                    doEdgeValueColor();
                    }

                // change background color
                if (e.getSource() == e_background_color)
                    {
                    doChangeBackgroundColor();
                    }

                // change background image source file
                if (e.getSource() == e_background_image)
                    {
                    doChangeBackgroundImage();
                    }

                // change background image x_coordinate
                if (e.getSource() == e_background_x)
                    {
                    doChangeBackgroundX();
                    }

                // change background image y-coordinate
                if (e.getSource() == e_background_y)
                    {
                    doChangeBackgroundY();
                    }

                // change background image width
                if (e.getSource() == e_background_width)
                    {
                    doChangeBackgroundWidth();
                    }

                if (e.getSource() == e_fit_background_to_image)
                    {
                    doFitBackgroundToImage();
                    }

                // scale background image
                if (e.getSource() == e_scale_background)
                    {
                    doScaleBackground();
                    }

                // change background image height
                if (e.getSource() == e_background_height)
                    {
                    doChangeBackgroundHeight();
                    }

                if (e.getSource() == e_no_background_image)
                    {
                    doNoBackgroundImage();
                    }

                // change edge separator
                if (e.getSource() == e_edge_separator)
                    {
                    doSeparator();
                    }

                // change names color
                if (e.getSource() == gtool_names_color
                        || e.getSource() == e_names_color)
                    {
                    doChangeNamesColor();
                    }

                // change title color
                if (e.getSource() == e_title_color)
                    {
                    doChangeTitleColor();
                    }

                // color loyalty
                if (e.getSource() == gtool_loyalty
                        || e.getSource() == e_set_loyalty
                        || e.getSource() == e_remove_loyalty)
                    {
                    doColorLoyalty();
                    }

                // change image width
                if (e.getSource() == gtool_image_width
                        || e.getSource() == e_image_width)
                    {
                    doChangeImageWidth();
                    }

                // scale image
                if (e.getSource() == e_scale_image)
                    {
                    doScaleImage();
                    }

                // view grid
                if (e.getSource() == e_view_grid
                        || e.getSource() == e_hide_grid)
                    {
                    doViewGrid();
                    }

                // change grid step
                if (e.getSource() == e_grid_step)
                    {
                    doGridStep();
                    }

                // change grid color
                if (e.getSource() == e_grid_color)
                    {
                    doGridColor();
                    }

                // change grid transparency
                if (e.getSource() == e_grid_transparency)
                    {
                    doGridTransparency();
                    }

                // snap to grid (or not)
                if (e.getSource() == e_stg || e.getSource() == e_dstg)
                    {
                    doSTG();
                    }

                // quit network viewer
                if (e.getSource() == f_quit)
                    {
                    doQuitNetworkViewer();
                    }

                // add a new node
                if (e.getSource() == d_add_node
                        || e.getSource() == gtool_add_nodes)
                    {
                    doAddActor();
                    }

                // transpose network's matrix
                if (e.getSource() == d_transpose)
                    {
                    doTranspose();
                    }

                if (e.getSource() == d_symmetrize)
                    {
                    doSymmetrize();
                    }

                // clone selected node
                if (e.getSource() == d_clone_node)
                    {
                    doCloneActor();
                    }

                // isolate selected node
                if (e.getSource() == d_isolate_node)
                    {
                    doIsolateActor();
                    }

                // delete selected node
                if (e.getSource() == d_delete_node
                        || e.getSource() == gtool_delete_nodes)
                    {
                    doDeleteActor();
                    }

                if (e.getSource() == d_remove_outsiders)
                    {
                    doRemoveOutsiders();
                    }

                // change names X
                if (e.getSource() == e_names_x)
                    {
                    doNamesX();
                    }

                // change names Y
                if (e.getSource() == e_names_y)
                    {
                    doNamesY();
                    }

                // change separator
                if (e.getSource() == gfield_separator)
                    {
                    doChangeSeparator();
                    }

                // change separator up
                if (e.getSource() == gtool_separator.up_button)
                    {
                    doUpDownSeparator(true);
                    }

                // change separator down
                if (e.getSource() == gtool_separator.down_button)
                    {
                    doUpDownSeparator(false);
                    }

                // set random layout
                if (e.getSource() == e_random_layout
                        || e.getSource() == gtool_random_layout)
                    {
                    doRandomLayout();
                    }

                // sets circular layout
                if (e.getSource() == e_circular_layout
                        || e.getSource() == gtool_circular_layout)
                    {
                    doCircularLayout();
                    }

                // select next node
                if (e.getSource() == gtool_select_next)
                    {
                    doSelectNextActor();
                    }

                // view/hide faces
                if (e.getSource() == e_view_faces
                        || e.getSource() == e_hide_faces)
                    {
                    doViewFaces();
                    }

                // change all faces
                if (e.getSource() == e_change_faces)
                    {
                    doChangeFaces();
                    }

                // change all faces width
                if (e.getSource() == e_change_faces_width)
                    {
                    doChangeFacesWidth();
                    }

                // allow/disallow edge selection (ES)
                if (e.getSource() == e_allow_ES
                        || e.getSource() == e_disallow_ES
                        || e.getSource() == gtool_allow_edge_selection)
                    {
                    doAllowES();
                    }

                // change title
                if (e.getSource() == e_set_title)
                    {
                    doSetTitle();
                    }

                // change title's visibility
                if (e.getSource() == e_view_title
                        || e.getSource() == e_hide_title)
                    {
                    doViewTitle();
                    }

                // change title's x-coordinate
                if (e.getSource() == e_title_x)
                    {
                    doTitleX();
                    }

                // change title's y-coordinate
                if (e.getSource() == e_title_y)
                    {
                    doTitleY();
                    }

                // setting default node face source:
                if (e.getSource() == p_default_face)
                    {
                    doDefaultFace();
                    }

                // saving default settings:
                if (e.getSource() == p_default_style)
                    {
                    doDefaultStyle();
                    }

                // copying default settings:
                if (e.getSource() == p_copy_style)
                    {
                    doCopyStyle();
                    }

                // help contents:
                if (e.getSource() == h_contents)
                    {
                    doHelpContents();
                    }

                // help about box:
                if (e.getSource() == h_about)
                    {
                    doHelpAboutBox();
                    }

                // border button:
                if (e.getSource() == border_button)
                    {
                    doSetLeftPanelVisibility();
                    }

                }
        };

    // metodele asociate lui act_change_image

    private void doSetTitle()
        {
        String tmp_str, tmp_title;
        tmp_title = my_area.getTitle();
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter a new title for current network",
                "Network title", JOptionPane.QUESTION_MESSAGE, null, null,
                tmp_title);
        gr_frame.repaint();
        if (tmp_str != null)
            {
            gr_full_net.getNetwork().setName(tmp_str);
            my_area.setTitle(tmp_str);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            }
        gr_frame.repaint();
        }

    private void doViewTitle()
        {
        if (e_view_title.isEnabled())
            {
            e_view_title.setEnabled(false);
            e_hide_title.setEnabled(true);
            my_area.setTitleVisibility(true);
            } else
            {
            e_view_title.setEnabled(true);
            e_hide_title.setEnabled(false);
            my_area.setTitleVisibility(false);
            }
        gr_full_net.setChanged(true);

        my_area.paintEdges();
        gr_frame.repaint();
        }

    private void doTitleX()
        {
        int new_x;
        String tmp_str;
        new_x = my_area.getTitleX();
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new x-coordinate for the title (pixels)",
                "Title horizontal position", JOptionPane.QUESTION_MESSAGE,
                null, null, String.valueOf(new_x));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            new_x = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            // showErrorParsing();
            }
        if (new_x > 0 && new_x < my_area.getWidth())
            {
            my_area.setTitleX(new_x);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();
            } else
            {
            // showErrorParsing();
            }
        }

    private void doTitleY()
        {
        int new_y;
        String tmp_str;
        new_y = my_area.getTitleY();
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new y-coordinate for the title (pixels)",
                "Title horizontal position", JOptionPane.QUESTION_MESSAGE,
                null, null, String.valueOf(new_y));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            new_y = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            // showErrorParsing();
            }
        if (new_y > 0 && new_y < my_area.getWidth())
            {
            my_area.setTitleY(new_y);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();
            } else
            {
            // showErrorParsing();
            }
        }

    // almost unnecesary method (double of MainFrame's):
    public static boolean saveOutput()
        {
        JFileChooser chooser = new JFileChooser();

        File file = null;
        // setting name of output file:
        if (gr_output_pane.getFileName().equals(""))
            {
            try
                {
                file = new File(MainFrame.getWorkingDirectory() + "Output.html");
                } catch (Exception e1)
                {
                file = new File("Output.html");
                }
            } else
            file = new File(gr_output_pane.getFileName());

        chooser.setSelectedFile(file);
        chooser.setDialogTitle("Save Current Output");
        chooser.setCurrentDirectory(file);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Type file name and click here");
        chooser.addChoosableFileFilter(new PlainTextFilesFilter());
        chooser.addChoosableFileFilter(new HTMLFilesFilter());
        if (chooser.showSaveDialog(gr_frame) != JFileChooser.APPROVE_OPTION)
            return false;
        gr_frame.repaint();
        EditorKit kit = null;
        Document doc = null;
        String filestr = null;
        OutputStream fileout = null;
        file = chooser.getSelectedFile();

        // reading file path
        try
            {
            filestr = file.getCanonicalPath();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame, "Error locating file!",
                    "Output Error", JOptionPane.ERROR_MESSAGE);
            return false;
            }

        // if no extension, html is added as default:
        if (IOUtils.getExtension(file.getName()) == null)
            {
            file = new File(filestr + ".html");
            } else if (IOUtils.getExtension(file.getName()).equals(""))
            {
            file = new File(filestr + "html");
            }

        // warning if file already exists
        if (file.exists())
            {
            int confirm = JOptionPane
                    .showOptionDialog(
                            gr_frame,
                            "File "
                                    + file.getName()
                                    + " already exists.\nDo you want to replace existing file?",
                            "Agna Output Message", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, null, null);
            if (confirm != 0)
                {
                return false;
                }
            }

        // filestr is now the extension!
        filestr = IOUtils.getExtension(file.getName());

        if (filestr.equals("rtf"))
            {
            kit = gr_output_pane.getEditorKitForContentType("text/rtf");
            doc = gr_output_pane.getStyledDocument();
            } else if (filestr.equals("htm") || filestr.equals("html"))
            {
            if (!gr_output_pane.getContentType().equals("text/html"))
                {
                String tmptext = gr_output_pane.getText();
                try
                    {
                    gr_output_pane.setContentType("text/html");
                    kit = ((HTMLEditorKit) gr_output_pane.getEditorKit());
                    doc = ((HTMLDocument) gr_output_pane.getDocument());
                    tmptext = HTMLParser.parseTextToHTML(tmptext);
                    gr_output_pane.setText(tmptext);
                    } catch (Exception e)
                    {
                    return false;
                    }
                tmptext = null;
                } else
                {
                kit = gr_output_pane.getEditorKitForContentType("text/html");
                doc = (HTMLDocument) gr_output_pane.getDocument();
                }
            } else
            {
            kit = gr_output_pane.getEditorKitForContentType("text/plain");
            doc = gr_output_pane.getDocument();
            }
        try
            {
            final EditorKit t_kit = kit;
            final Document t_doc = doc;
            final File t_file = file;

            // Thread runner = new Thread()
            // {
            // public void run()
            // {
            gr_output_pane.setCursor(Cursor
                    .getPredefinedCursor(Cursor.WAIT_CURSOR));
            setStatus("Writing output file...");
            try (FileOutputStream t_fileout = new FileOutputStream(file
                    .getCanonicalPath()))
                {
                t_kit.write(t_fileout, t_doc, 0, t_doc.getLength());
                t_fileout.flush();
                gr_output_pane.setFileName(t_file.getCanonicalPath());
                } catch (Exception e)
                {
                AgnaLog.warn("GrNet.saveOutput write failed: " + e);
                }
            gr_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            gr_output_pane.setChanged(false);
            gr_output_pane.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            setDefaultStatus();
            
            // }
            // }; //end of thread
            // runner.start();
            return true;
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame, "Error writing file!",
                    "Output Error", JOptionPane.ERROR_MESSAGE);
            
            return false;
            }
        }

    /**
     * Creates a SVG file from my_area's settings and saves it on given path;
     * the path must contain a file name with svg extension;
     */
    public void saveImageAsSVG(String tmp_file_name)
        {
        if (tmp_file_name == null || tmp_file_name.equals(""))
            return;

        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SVGManager svg = new SVGManager();
        File outfile = null; // svg file to be created

        try
            {
            outfile = new File(tmp_file_name);
            // warning if file already exists
            if (outfile.exists())
                {
                int confirm = JOptionPane
                        .showOptionDialog(
                                gr_frame,
                                "File "
                                        + outfile.getName()
                                        + " already exists.\nDo you want to replace existing file?",
                                "Agna Output Message",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE, null, null, null);
                if (confirm != 0)
                    {
                    gr_frame.setCursor(Cursor
                            .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;
                    }
                }

            String file_content = svg.getSVGContent(my_area, outfile);
            try (Writer writer = IOUtils.writerUtf8(outfile))
                {
                JTextPane tmp_pane = new JTextPane();
                tmp_pane.setText(file_content);
                tmp_pane.write(writer);
                }
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        svg = null;
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

    /**
     * Creates a JPEG file from a BufferedImage and saves it on given path
     */
    public static void saveImageAsJPG(BufferedImage tmp_imag,
            String tmp_file_name)
        {
        final BufferedImage imag = tmp_imag;
        final String file_name = tmp_file_name;

gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Writing image file...");
        try
            {
            // saves image as jpeg (2.1.3: standard ImageIO API replaces the
            // removed com.sun.image.codec.jpeg internals; quality kept at 1.0)
            File file = new File(file_name);
            try (FileOutputStream out = new FileOutputStream(file))
                {
                JpegUtils.writeJpeg(imag, out, 1.0f);
                }
            } catch (FileNotFoundException fx)
            {
            setDefaultStatus();
            gr_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
            } catch (IOException iox)
            {
            setDefaultStatus();
            gr_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
            }
        setDefaultStatus();
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        }

    // setting default node face source:
    private void doDefaultFace()
        {
        String tmp_image_name = inputImageFile(MainFrame
                .getDefaultNodeFaceSource());
        if (tmp_image_name == null || tmp_image_name.length() < 2)
            return;
        try
            {
            if ((new File(tmp_image_name)).exists())
                {
                MainFrame.setDefaultNodeFaceSource(tmp_image_name);
                n_default_face.setName(tmp_image_name);
                try
                    {
                    n_default_face.setIcon(new ImageIcon(tmp_image_name));
                    } catch (Exception e1) {
      AgnaLog.warn("suppressed exception", e1);
      }
                }
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        }

    // saving default style/settings to ini file:
    private void doDefaultStyle()
        {
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MainFrame.getCurrentFullNet().writeInitialSettings();
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

    // reading default style/settings from an agn file:
    private void doCopyStyle()
        {
        File file = null;
        if (MainFrame.getCurrentFullNet().getNetworkFileName().length() > 0)
            {
            file = new File(MainFrame.getCurrentFullNet().getNetworkFileName());
            } else
            {
            try
                {
                file = new File(MainFrame.getWorkingDirectory());
                } catch (Exception e1)
                {
                try
                    {
                    file = new File(Environment.getCurrentDirectory()
                            + Environment.fs + "Samples");
                    } catch (Exception e2)
                    {
                    file = new File(".");
                    }
                }
            }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Network File");
        chooser.setCurrentDirectory(file);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Select file and click here");
        chooser.addChoosableFileFilter(new AgnaFilesFilter());
        int return_val = chooser.showOpenDialog(gr_frame);
        if (return_val == JFileChooser.CANCEL_OPTION)
            return;
        gr_frame.repaint();
        file = chooser.getSelectedFile();
        final File t_file = file;

        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
            {
            MainFrame.getCurrentFullNet().readInitialSettings(
                    t_file.getCanonicalPath());
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Errors encountered on reading!", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            gr_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        my_area.paintEdges();
        int new_width = my_area.getWidth();
        Dimension new_dimension = new Dimension(new_width, new_width);
        my_area.setPreferredSize(new_dimension);
        area_panel.setPreferredSize(new Dimension(new_width + 10,
                new_width + 10));
        area_panel.revalidate();
        // my_area.repaint();
        gr_frame.repaint();
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        }

    private void doSaveImageAs()
        {
        JFileChooser chooser = new JFileChooser();
        String filestr = null;
        chooser.setDialogTitle("Export Image As");
        File file = new File(MainFrame.getWorkingDirectory()
                + gr_full_net.getNetwork().getName() + " image");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setSelectedFile(file);
        chooser.setCurrentDirectory(file);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Type file name and click here");
        chooser.addChoosableFileFilter(new JpgFilesFilter());
        chooser.addChoosableFileFilter(new SvgFilesFilter());
        if (chooser.showSaveDialog(gr_frame) != JFileChooser.APPROVE_OPTION)
            return;
        gr_frame.repaint();
        file = chooser.getSelectedFile();
        try
            {
            filestr = file.getCanonicalPath();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame, "Error locating file!",
                    "Output Error", JOptionPane.ERROR_MESSAGE);
            return;
            }

        // SVG files was selected:
        if (chooser.getFileFilter() instanceof SvgFilesFilter)
            {
            file = new File(IOUtils.addExtension(filestr, "svg"));
            try
                {
                saveImageAsSVG(file.getCanonicalPath());
                } catch (IOException e3)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Error accessing disk!", "Output Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
                } catch (SecurityException e4)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Error accessing disk!", "Output Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
                }
            }
        // JPEG files was selected:
        else if (chooser.getFileFilter() instanceof JpgFilesFilter)
            {
            file = new File(IOUtils.addExtension(filestr, "jpg"));
            // warning if file already exists
            if (file.exists())
                {
                int confirm = JOptionPane
                        .showOptionDialog(
                                gr_frame,
                                "File "
                                        + file.getName()
                                        + " already exists.\nDo you want to replace existing file?",
                                "Agna Output Message",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE, null, null, null);
                if (confirm != 0)
                    {
                    return;
                    }
                }
            try
                {
                saveImageAsJPG(my_area.getOutputImage(), file
                        .getCanonicalPath());
                } catch (IOException e3)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Error accessing disk!", "Output Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
                } catch (SecurityException e4)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Error accessing disk!", "Output Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
                }
            }

        
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

    private void doInsertInOutput()
        {
        BufferedImage img = my_area.getOutputImage();
        gr_output_pane.appendBufferedImage(img);
        // gr_frame.repaint();
        
        }

    private void doViewNames()
        {

        if (my_area.print_names)
            {
            my_area.setPrintNames(false);
            gtool_view_names.setToolTipText("Show names");
            e_view_names.setEnabled(true);
            e_hide_names.setEnabled(false);
            } else
            {
            my_area.setPrintNames(true);
            gtool_view_names.setToolTipText("Hide names");
            e_view_names.setEnabled(false);
            e_hide_names.setEnabled(true);
            }
        gr_full_net.setChanged(true);
        gr_frame.repaint();
        }

    private void doChangeEdgeColor()
        {
        Color tmp_color = showChooser();
        if (tmp_color != null)
            {
            my_area.setArrowColor(tmp_color);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();

            }
        // gr_frame.repaint();
        }

    private void doMaxTransparency()
        {
        String tmp_transparency, tmp_str;
        // transforming into percents:
        tmp_transparency = String
                .valueOf((int) (100f - 100f / 255f * (float) my_area
                        .getMaxTransparency()));
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter transparency value for the most faded edge (%)",
                "Edge loyalty setting", JOptionPane.QUESTION_MESSAGE, null,
                null, tmp_transparency);
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            if (Integer.parseInt(tmp_str) < 0
                    || Integer.parseInt(tmp_str) > 100)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Transparency value must range between 0 and 100.",
                        "Parsing error", JOptionPane.INFORMATION_MESSAGE);
                return;
                }
            // back into 0-255 value
            my_area
                    .setMaxTransparency((int) (255f - 255f / 100f * (float) Integer
                            .parseInt(tmp_str)));
            my_area.updateArea(gr_full_net.my_network);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            gr_frame.repaint();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Transparency value must range between 0 and 100.",
                    "Parsing error", JOptionPane.INFORMATION_MESSAGE);
            }
        }

    private void doShowEdgeValue()
        {
        if (e_show_edge_value.isEnabled())
            {
            e_show_edge_value.setEnabled(false);
            e_hide_edge_value.setEnabled(true);
            my_area.setEdgeValueVisible(true);
            gtool_show_connection_value.setToolTipText("Hide Edge Value");
            } else
            {
            e_show_edge_value.setEnabled(true);
            e_hide_edge_value.setEnabled(false);
            my_area.setEdgeValueVisible(false);
            gtool_show_connection_value.setToolTipText("Show Edge Value");
            }
        gr_full_net.setChanged(true);

        my_area.paintEdges();
        gr_frame.repaint();
        }

    private void doEdgeValuePosition()
        {
        int position = 0;
        String tmp_position, tmp_str;
        tmp_position = String.valueOf(my_area.getEdgeValuePosition());
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new position of Edge Value (%)",
                "Edge Value position", JOptionPane.QUESTION_MESSAGE, null,
                null, tmp_position);
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            position = (int) Float.parseFloat(tmp_str);
            if (position < 0 || position > 100)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Position must range between 0 and 100.",
                        "Parsing error", JOptionPane.INFORMATION_MESSAGE);
                return;
                }
            my_area.setEdgeValuePosition(position);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            // gr_frame.repaint();
            my_area.repaint();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Position must range between 0 and 100.", "Parsing error",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }

    private void doEdgeValueColor()
        {
        Color tmp_color = showChooser();
        if (tmp_color != null)
            {
            my_area.setEdgeValueColor(tmp_color);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();
            }
        // gr_frame.repaint();
        }

    private void doChangeBackgroundColor()
        {
        Color tmp_color = showChooser();
        if (tmp_color != null)
            {
            my_area.setBackgroundColor(tmp_color);
            my_area.paintEdges();
            my_area.repaint();
            gr_full_net.setChanged(true);
            }
        // gr_frame.repaint();
        }

    private void doChangeBackgroundImage()
        {
        String tmp_image_name = inputImageFile(".");
        ImageIcon tmp_icon = null;
        try
            {
            tmp_icon = new ImageIcon(tmp_image_name);
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }

        if (tmp_icon == null)
            return;
        else
            {
            my_area.setBackgroundImage(tmp_image_name);
            gr_full_net.setChanged(true);
            // gr_frame.repaint();
            my_area.paintEdges();
            my_area.repaint();
            }
        }

    private void doChangeBackgroundX()
        {
        int new_value;
        String tmp_str;
        new_value = my_area.getBackgroundImageX();
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new x-coordinate for background image (pixels)",
                "Title horizontal position", JOptionPane.QUESTION_MESSAGE,
                null, null, String.valueOf(new_value));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            new_value = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            // showErrorParsing();
            }
        if (new_value > 0 && new_value < my_area.getWidth())
            {
            my_area.setBackgroundImageX(new_value);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();
            } else
            {
            // showErrorParsing();
            }
        }

    private void doChangeBackgroundY()
        {
        int new_value;
        String tmp_str;
        new_value = my_area.getBackgroundImageY();
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new y-coordinate for background image (pixels)",
                "Title horizontal position", JOptionPane.QUESTION_MESSAGE,
                null, null, String.valueOf(new_value));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            new_value = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            // showErrorParsing();
            }
        if (new_value > 0 && new_value < my_area.getWidth())
            {
            my_area.setBackgroundImageY(new_value);
            my_area.paintEdges();
            gr_full_net.setChanged(true);
            my_area.repaint();
            } else
            {
            // showErrorParsing();
            }
        }

    private void doChangeBackgroundWidth()
        {
        int new_value;
        String tmp_str;
        new_value = my_area.getBackgroundImageWidth();
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new width for background image (pixels)",
                "Background picture width", JOptionPane.QUESTION_MESSAGE, null,
                null, String.valueOf(new_value));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            new_value = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            // showErrorParsing();
            }
        if (new_value > 0)
            {
            my_area.setBackgroundImageWidth(new_value);
            my_area.paintEdges();
            gr_full_net.setChanged(true);
            my_area.repaint();
            } else
            {
            // showErrorParsing();
            }
        }

    private void doScaleBackground()
        {
        String tmp_str = "100";
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Enter scale factor for background picture (%)",
                "Scale background picture", JOptionPane.QUESTION_MESSAGE, null,
                null, tmp_str);
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        float scale_factor = 0f;
        try
            {
            scale_factor = Float.parseFloat(tmp_str);
            } catch (Exception e)
            {
            // showErrorParsing();
            return;
            }
        if (scale_factor > 1)
            {
            my_area.setBackgroundImageWidth((int) (scale_factor
                    * (float) my_area.getBackgroundImageWidth() / 100));
            my_area.setBackgroundImageHeight((int) (scale_factor
                    * (float) my_area.getBackgroundImageHeight() / 100));
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();
            } else
            {
            // showErrorParsing();
            }
        }

    private void doFitBackgroundToImage()
        {
        // final int old_size =
        // Math.max(my_area.getBackgroundImageOriginalWidth(),
        // my_area.getBackgroundImageOriginalHeight());
        // final int new_size = Math.min(my_area.getWidth(),
        // my_area.getHeight());
        float scale_factor = 100f
                * (float) (Math.min(my_area.getWidth(), my_area.getHeight()))
                / (float) (Math.max(my_area.getBackgroundImageOriginalWidth(),
                        my_area.getBackgroundImageOriginalHeight()));

        my_area.setBackgroundImageWidth((int) (scale_factor
                * (float) my_area.getBackgroundImageOriginalWidth() / 100));
        my_area.setBackgroundImageHeight((int) (scale_factor
                * (float) my_area.getBackgroundImageOriginalHeight() / 100));
        my_area.setBackgroundImageX(0);
        my_area.setBackgroundImageY(0);
        gr_full_net.setChanged(true);
        my_area.paintEdges();
        my_area.repaint();
        }

    private void doChangeBackgroundHeight()
        {
        int new_value;
        String tmp_str;
        new_value = my_area.getBackgroundImageHeight();
        JOptionPane tmp_pane = new JOptionPane();
        tmp_str = (String) tmp_pane.showInputDialog(gr_frame,
                "Please enter new height for background image (pixels)",
                "Background picture height", JOptionPane.QUESTION_MESSAGE,
                null, null, String.valueOf(new_value));
        gr_frame.repaint();
        tmp_pane = null;
        // tmp_str = (String) JOptionPane.showInputDialog(gr_frame,"Enter new
        // height for background image (pixels)","Title horizontal position",
        // JOptionPane.QUESTION_MESSAGE,null, null,String.valueOf(new_value));
        if (tmp_str == null)
            return;
        try
            {
            new_value = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            // showErrorParsing();
            }
        if (new_value > 0)
            {
            my_area.setBackgroundImageHeight(new_value);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();
            } else
            {
            // showErrorParsing();
            }
        }

    private void doNoBackgroundImage()
        {
        my_area.setBackgroundImage("");
        // gr_frame.repaint();
        gr_full_net.setChanged(true);
        my_area.paintEdges();
        my_area.repaint();
        }

    private void doChangeNamesColor()
        {
        Color tmp_color = showChooser();
        if (tmp_color != null)
            {
            my_area.setNamesColor(tmp_color);
            }
        gr_full_net.setChanged(true);
        my_area.paintEdges();
        my_area.repaint();
        
        }

    private void doChangeTitleColor()
        {
        Color tmp_color = showChooser();
        if (tmp_color != null)
            {
            my_area.setTitleColor(tmp_color);
            }
        gr_full_net.setChanged(true);
        my_area.paintEdges();
        gr_frame.repaint();
        
        }

    private void doColorLoyalty()
        {
        if (my_area.getColorFidelity() == true)
            {
            my_area.setColorFidelity(false);
            gtool_loyalty.setToolTipText("Set value-loyalty by colors   Alt+L");
            e_set_loyalty.setEnabled(true);
            e_remove_loyalty.setEnabled(false);
            } else
            {
            my_area.setColorFidelity(true);
            gtool_loyalty
                    .setToolTipText("Remove value-loyalty by colors   Alt+L");
            e_set_loyalty.setEnabled(false);
            e_remove_loyalty.setEnabled(true);
            }
        gr_full_net.setChanged(true);
        my_area.paintEdges();
        // my_area.repaint();
        gr_frame.repaint();
        }

    private void doSetLeftPanelVisibility()
        {
        if (left_scroll.isVisible())
            {
            left_scroll.setVisible(false);
            border_button.setToolTipText("Click to show left tools");
            } else
            {
            left_scroll.setVisible(true);
            border_button.setToolTipText("Click to hide left tools");
            }
        left_panel.revalidate();
        // left_panel.repaint();
        gr_frame.repaint();
        }

    public static void setAreaPanelWidth(int new_width)
        {
        Dimension new_dimension = new Dimension(new_width, new_width);
        my_area.setPreferredSize(new_dimension);
        area_panel.setPreferredSize(new Dimension(new_width + 10,
                new_width + 10));
        area_panel.revalidate();
        }

    private void doChangeImageWidth()
        {
        int old_width;
        String tmp_str;
        old_width = my_area.getWidth();
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new image width (pixels)", "Image dimension",
                JOptionPane.QUESTION_MESSAGE, null, null, String
                        .valueOf(old_width));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            final int new_width = Integer.parseInt(tmp_str);
            if (new_width < 32)
                return;
            gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            setStatus("Rebuilding network image...");
            my_area.setWidth(new_width);
            my_area.paintEdges();
            Dimension new_dimension = new Dimension(new_width, new_width);
            my_area.setPreferredSize(new_dimension);
            area_panel.setPreferredSize(new Dimension(new_width + 10,
                    new_width + 10));
            area_panel.revalidate();
            gr_full_net.setChanged(true);
            gr_frame.repaint();
            setDefaultStatus();
            gr_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } catch (Exception e)
            {
            return;
            }
        }

    private void doScaleImage()
        {
        String tmp_str = "100";
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter scale factor (%)", "Scale image",
                JOptionPane.QUESTION_MESSAGE, null, null, tmp_str);
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            final int new_width = (int) ((float) my_area.getWidth()
                    * Float.parseFloat(tmp_str) / 100);
            if (new_width < 1)
                return;
            gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            setStatus("Rebuilding network image...");
            my_area.setWidth(new_width);
            my_area.paintEdges();
            Dimension new_dimension = new Dimension(new_width, new_width);
            my_area.setPreferredSize(new_dimension);
            area_panel.setPreferredSize(new Dimension(new_width + 10,
                    new_width + 10));
            gr_full_net.setChanged(true);
            area_panel.revalidate();
            gr_frame.repaint();
            setDefaultStatus();
            gr_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } catch (Exception e)
            {
            return;
            }
        }

    private void doViewGrid()
        {
        boolean is = e_hide_grid.isEnabled();
        e_view_grid.setEnabled(is);
        e_hide_grid.setEnabled(!is);
        my_area.setGridEnabled(!is);
        my_area.paintEdges();
        gr_full_net.setChanged(true);
        // my_area.repaint();
        gr_frame.repaint();
        }

    private void doSTG()
        {
        boolean is = e_dstg.isEnabled();
        e_stg.setEnabled(is);
        e_dstg.setEnabled(!is);
        my_area.setSTGEnabled(!is);
        gr_full_net.setChanged(true);
        gr_frame.repaint();
        }

    private void doGridStep()
        {
        int grid_space = 0;
        String tmp_grid_step, tmp_str;
        tmp_grid_step = String.valueOf(my_area.getGridSpace());
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new grid step (pixels)", "Grid step value",
                JOptionPane.QUESTION_MESSAGE, null, null, tmp_grid_step);
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            grid_space = Integer.parseInt(tmp_str);
            if (grid_space < 1)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Separation value must be a positive integer.",
                        "Parsing error", JOptionPane.INFORMATION_MESSAGE);
                return;
                }
            my_area.setGridSpace(grid_space);
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            // gr_frame.repaint();
            my_area.repaint();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Separation value must be an integer.", "Parsing error",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }

    private void doGridColor()
        {
        Color tmp_color = showChooser();
        if (tmp_color != null)
            {
            my_area.setGridColor(tmp_color);
            }
        gr_full_net.setChanged(true);
        my_area.paintEdges();
        // gr_frame.repaint();
        my_area.repaint();
        }

    private void doGridTransparency()
        {
        String tmp_grid_transparency, tmp_str;
        // transforming into percents:
        tmp_grid_transparency = String
                .valueOf((int) (100f - 100f / 255f * (float) my_area
                        .getGridTransparency()));
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new grid transparency (%)",
                "Grid transparency value", JOptionPane.QUESTION_MESSAGE, null,
                null, tmp_grid_transparency);
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            if (Integer.parseInt(tmp_str) < 0
                    || Integer.parseInt(tmp_str) > 255)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Transparency value must range between 0 and 100.",
                        "Parsing error", JOptionPane.INFORMATION_MESSAGE);
                return;
                }
            // back into 0-255 value
            my_area
                    .setGridTransparency((int) (255f - 255f / 100f * (float) Integer
                            .parseInt(tmp_str)));
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Transparency value must range between 0 and 100.",
                    "Parsing error", JOptionPane.INFORMATION_MESSAGE);
            }
        }

    private void doQuitNetworkViewer()
        {
        gr_frame.dispose();
        gr_frame = null;
        MainFrame.setDisabledCloseViewerMenu();
        
        }

    private void doHelpContents()
        {
        MainFrame.doHelpContents(this.getCurrentFrame());
        }

    private void doHelpAboutBox()
        {
        MainFrame.showAboutBox(this.getCurrentFrame());
        }

    public static void doAddNodeHere(int tmp_x, int tmp_y, int tmp_max)
        {
        final int max = tmp_max;
        final int x = tmp_x;
        final int y = tmp_y;
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Adding new node...");
        gr_model.setReady(false);
        gr_full_net.addOneNodeToNetwork(x, y, max);
        gr_model.addRowCol();
        my_area.unselectSecond();
        setMatrix();
        MainFrame.setTableCellEditor();
        gr_model.setReady(true);
        gr_full_net.setChanged(true);
        // my_area.paintEdges();
        setDefaultStatus();
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        try
            {
            my_area.repaint();
            } catch (Exception e)
            {
            setDefaultStatus();
            gr_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

    private void doAddActor()
        {
        // gr_frame.repaint();
        String tmp_str = "";
        Object[] values = new Object[10];
        for (int i = 0; i < 10; i++)
            {
            values[i] = (Object) String.valueOf(i + 1);
            }
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please select number of nodes to be added:", "Add new nodes",
                JOptionPane.QUESTION_MESSAGE, null, values, "1");
        gr_frame.repaint();
        if (tmp_str == null)
            return;

        final int nn = Integer.parseInt(tmp_str);
        // adding nodes inside thread
        /*
         * Thread add_nodes_thread = new Thread() { public void run() {
         */
        gr_model.setReady(false);
        gr_full_net.addNodesToNetwork(nn); // no position specified
        for (int i = 1; i <= nn; i++)
            {
            // gr_full_net.addNodeToNetwork(-1, -1, -1); // no position
            // specified
            // updates table:
            gr_model.addRowCol();
            }
        my_area.unselectSecond();
        setMatrix();

        my_area.paintEdges();
        gr_frame.repaint();
        gr_full_net.setChanged(true);
        gr_model.setReady(true);
        MainFrame.setTableCellEditor();
        
        /*
         * } }; add_nodes_thread.start();
         */
        }

    private void doCloneActor()
        {
        // gr_frame.repaint();
        gr_model.setReady(false);
        gr_full_net.cloneNodeToNetwork();
        gr_model.addRowCol();
        setMatrix();
        gr_model.setReady(true);
        gr_full_net.setChanged(true);
        MainFrame.setTableCellEditor();
        my_area.paintEdges();
        my_area.repaint();
        }

    private void doIsolateActor()
        {
        int isol_node = my_area.getSelectedActor();
        gr_model.setReady(false);
        gr_full_net.isolateNodeToNetwork(isol_node);
        // gr_frame.repaint();
        my_area.paintEdges();
        gr_full_net.setChanged(true);
        gr_model.setReady(true);

        setMatrix();
        my_area.repaint();
        }

    private void doDeleteActor()
        {
        int del_node = my_area.getSelectedActor();
        gr_model.setReady(false);
        gr_full_net.deleteNodeToNetwork(del_node);
        gr_model.delRowCol(del_node);

        my_area.paintEdges();
        gr_full_net.setChanged(true);
        MainFrame.setTableHeaders();
        MainFrame.setTableCellEditor();
        gr_model.setReady(true);
        disableFirst();
        gr_frame.repaint();
        }

    private void doRemoveOutsiders()
        {
        /*
         * Thread runner = new Thread() { public void run() {
         */
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Removing isolated nodes from network...");
        gr_model.setReady(false);
        gr_full_net.removeOutsidersInNetwork(gr_model);

        my_area.paintEdges();
        // my_area.repaint();
        setDefaultStatus();
        setMatrix();
        gr_full_net.setChanged(true);
        gr_model.setReady(true);
        if (my_area.getSelectedActor() > -1)
            enableFirst();
        // MainFrame.setTableHeaders();
        MainFrame.setTableCellEditor();
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        gr_frame.repaint();
        /*
         * } }; runner.start();
         */
        }

    private void doTranspose()
        {
        /*
         * Thread runner = new Thread() { public void run() {
         */
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Transposing network's sociomatrix...");
        gr_model.setReady(false);
        gr_full_net.transpose();
        setMatrix();

        gr_full_net.setChanged(true);
        my_area.paintEdges();
        gr_model.setReady(true);
        my_area.repaint();
        setDefaultStatus();
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        /*
         * } }; runner.start();
         */
        }

    private void doSymmetrize()
        {
        /*
         * Thread runner = new Thread() { public void run() {
         */
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Symmetrizing network's sociomatrix...");
        gr_model.setReady(false);
        gr_full_net.symmetrize(gr_frame);
        setMatrix();
        // gr_frame.repaint();
        my_area.paintEdges();
        my_area.repaint();
        gr_full_net.setChanged(true);
        setDefaultStatus();
        gr_model.setReady(true);
        gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        /*
         * } }; runner.start();
         */
        }

    private void doNamesX()
        {
        String tmp_str = "";
        int new_x = my_area.names_x;
        tmp_str = String.valueOf(new_x);
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new x-coordinate for names",
                "Names vertical position", JOptionPane.QUESTION_MESSAGE, null,
                null, String.valueOf(new_x));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            new_x = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "X-coordinate must be an integer.", "Parsing error",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
            }
        my_area.names_x = new_x;
        gr_full_net.setChanged(true);
        if (my_area.print_names)
            {
            my_area.paintEdges();
            my_area.repaint();
            }
        }

    private void doNamesY()
        {
        String tmp_str = "";
        int new_y = my_area.names_y;
        tmp_str = String.valueOf(new_y);
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new y-coordinate for names",
                "Names vertical position", JOptionPane.QUESTION_MESSAGE, null,
                null, String.valueOf(new_y));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            new_y = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Y-coordinate must be an integer.", "Parsing error",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
            }
        my_area.names_y = new_y;
        gr_full_net.setChanged(true);
        if (my_area.print_names)
            {
            my_area.paintEdges();
            my_area.repaint();
            }
        }

    private void doChangeSeparator()
        {
        String tmp_separator;
        tmp_separator = gfield_separator.getText();
        if (tmp_separator == null)
            return;
        try
            {
            my_area.setSeparator(Integer.parseInt(tmp_separator));
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            gr_frame.repaint();
            } catch (Exception e)
            {
            gfield_separator.setText(String.valueOf(my_area.getSeparator()));
            JOptionPane.showMessageDialog(gr_frame,
                    "Separation value must be an integer.", "Parsing error",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }

    private void doSeparator()
        {
        String tmp_separator, tmp_str;
        tmp_separator = String.valueOf(my_area.getSeparator());
        tmp_str = (String) JOptionPane
                .showInputDialog(
                        gr_frame,
                        "Please enter new separation value (set zero if no separation)",
                        "Edges separation", JOptionPane.QUESTION_MESSAGE, null,
                        null, tmp_separator);
        gr_frame.repaint();
        if (tmp_str == null)
            return;

        try
            {
            if (Integer.parseInt(tmp_str) < 0)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Separation value must be an integer.",
                        "Parsing error", JOptionPane.INFORMATION_MESSAGE);
                return;
                }
            my_area.setSeparator(Integer.parseInt(tmp_str));
            my_area.paintEdges();
            gr_full_net.setChanged(true);
            gr_frame.repaint();
            gfield_separator.setText(String.valueOf(my_area.getSeparator()));
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Separation value must be an integer.", "Parsing error",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }

    private void doUpDownSeparator(boolean up) // grows separator by 1
        {
        int new_s = my_area.getSeparator();
        if (up)
            { // separation cannot be bigger than 16
            // or less than zero
            /*
             * if (new_s>=16) {return;}
             */
            new_s++;
            } else
            {
            if (new_s <= 0)
                {
                return;
                }
            new_s--;
            }
        my_area.setSeparator(new_s);
        gr_full_net.setChanged(true);
        my_area.paintEdges();
        my_area.repaint();
        gfield_separator.setText(String.valueOf(new_s));
        }

    private void doLagrangeLayout()
        {
        my_area.setLagrangeLayout();
        gr_full_net.setChanged(true);
        gr_frame.repaint();
        }

    private void doRandomLayout() // randomize positions of all nodes
        {
        // gr_frame.repaint();
        my_area.setRandomLayout();
        // my_area.setLagrangeLayout();
        my_area.paintEdges();
        my_area.repaint();
        gr_full_net.setChanged(true);
        }

    private void doCircularLayout()
        {
        my_area.setCircleLayout();
        gr_full_net.setChanged(true);
        // gr_frame.repaint();
        my_area.paintEdges();
        my_area.repaint();
        }

    /**
     * static method allowing to change the selected actor; not to be placed in
     * threads!
     */
    public static void doSelectActor(int tmp_index) // select next node
        {
        if (my_area.selectActor(tmp_index))
            {
            doManageEnablingComponentsStatic();
            // my_area.repaint();
            gr_frame.repaint();
            }
        }

    private void doSelectNextActor() // select next node
        {
        my_area.selectNextActor();
        // gr_frame.repaint();
        my_area.repaint();
        }

    private static void doSelectNextArrow() // select next arrow
        {
        my_area.selectNextArrow();
        // gr_frame.repaint();
        my_area.repaint();
        }

    private void doViewFaces()
        {
        if (my_area.isFaceVisible())
            {
            my_area.setFacesVisible(false);
            e_view_faces.setEnabled(true);
            e_hide_faces.setEnabled(false);
            } else
            {
            my_area.setFacesVisible(true);
            e_view_faces.setEnabled(false);
            e_hide_faces.setEnabled(true);
            }
        gr_full_net.setChanged(true);
        gr_frame.repaint();
        }

    private void doChangeFaces()
        {
        final String tmp_image_name = inputImageFile(my_area.my_nodes[0]
                .getFaceSource());
        if (tmp_image_name == null)
            return;
        ImageIcon first_icon = null;
        try
            {
            first_icon = new ImageIcon(tmp_image_name);
            } catch (Exception e)
            {
            first_icon = null;
            }
        if (first_icon == null)
            return;
        else
            {
            final ImageIcon tmp_icon = first_icon;

            gr_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            setStatus("Rebuilding network image...");
            my_area.changeAllFaces(tmp_image_name);
            gr_full_net.setChanged(true);
            // my_area.repaint();
            gr_frame.repaint();
            MainFrame.setAllNodeFaces(tmp_icon);
            setDefaultStatus();
            gr_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            }
        }

    private void doChangeFacesWidth()
        {
        String face_width, tmp_str;
        String tmp_face_width = String.valueOf(my_area.my_nodes[0].getSize());
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new width all nodes faces (px)",
                "All Nodes faces width", JOptionPane.QUESTION_MESSAGE, null,
                null, tmp_face_width);
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            if (Integer.parseInt(tmp_str) <= 1)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Faces can't be smaller than 1 px.", "Parsing error",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
                }
            if (Integer.parseInt(tmp_str) > 1000)
                return;
            my_area.changeAllFacesWidth(Integer.parseInt(tmp_str));
            gr_full_net.setChanged(true);
            gr_frame.repaint();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Face width must be an integer.", "Parsing error",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }

    private void doAllowES()
        {
        if (my_area.allow_ES)
            {
            my_area.allow_ES = false;
            e_allow_ES.setEnabled(true);
            e_disallow_ES.setEnabled(false);
            n_select_next_arrow.setEnabled(false);
            gtool_allow_edge_selection.setToolTipText("Allow edge selection");
            } else
            {
            my_area.allow_ES = true;
            e_allow_ES.setEnabled(false);
            e_disallow_ES.setEnabled(true);
            n_select_next_arrow.setEnabled(true);
            gtool_allow_edge_selection
                    .setToolTipText("Disallow edge selection");
            }
        gr_full_net.setChanged(true);
        // my_area.repaint();
        gr_frame.repaint();
        }

    private FocusListener focus_change = new FocusListener()
        {
            public void focusLost(FocusEvent e)
                {
                if (e.getSource() == gfield_x)
                    {
                    changeX();
                    }
                if (e.getSource() == gfield_y)
                    {
                    changeY();
                    }
                if (e.getSource() == gfield_separator)
                    {
                    doChangeSeparator();
                    }
                if (e.getSource() == gfield_edge_value)
                    {
                    changeEdgeValue();
                    }
                }

            public void focusGained(FocusEvent e)
                {
                if (e.getSource() == gfield_x)
                    {
                    gfield_x.selectAll();
                    }
                if (e.getSource() == gfield_y)
                    {
                    gfield_y.selectAll();
                    }
                if (e.getSource() == gfield_separator)
                    {
                    gfield_separator.selectAll();
                    }
                if (e.getSource() == gfield_edge_value)
                    {
                    gfield_edge_value.selectAll();
                    }
                }
        };

    // miscarile nodului generate prin keys:

    private ActionListener act_x_up = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                doUpDownX(true);
                }
        };

    private ActionListener act_x_down = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                doUpDownX(false);
                }
        };

    private ActionListener act_y_up = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                doUpDownY(true);
                }
        };

    private ActionListener act_y_down = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                doUpDownY(false);
                }
        };

    private ActionListener act_edge_value_up = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                doUpDownEdgeValue(true);
                }
        };

    private ActionListener act_edge_value_down = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                doUpDownEdgeValue(false);
                }
        };

    private ActionListener act_face_menu = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                int selnode = my_area.getSelectedActor();
                if (selnode == -1)
                    return;
                JMenuItem tmp_item = (JMenuItem) e.getSource();
                String tmp_image_name = tmp_item.getName();
                tmp_item = null;
                if (tmp_image_name == null)
                    return;
                ImageIcon tmp_icon = null;
                try
                    {
                    tmp_icon = new ImageIcon(tmp_image_name);
                    } catch (Exception ex)
                    {
                    JOptionPane.showMessageDialog(gr_frame,
                            "Error reading image file!", "File Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                    }
                if (tmp_icon == null)
                    return;
                my_area.my_nodes[selnode].setFace(tmp_image_name);
                my_area.paintEdges();
                my_area.repaint();
                MainFrame.setNodeFace(tmp_icon, selnode);
                GrNet.getCurrentVerticalToolBar().repaint();
                }
        };

    private ActionListener act_change_node = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                if (e.getSource() == gfield_x)
                    {
                    changeX();
                    }

                if (e.getSource() == gfield_y)
                    {
                    changeY();
                    }

                if (e.getSource() == gfield_edge_value)
                    {
                    changeEdgeValue();
                    }

                if (e.getSource() == n_set_x)
                    {
                    doChangeX();
                    }

                if (e.getSource() == n_set_y)
                    {
                    doChangeY();
                    }

                if (e.getSource() == gtool_change_x.up_button)
                    {
                    doUpDownX(true);
                    } // true = up

                if (e.getSource() == gtool_change_x.down_button)
                    {
                    doUpDownX(false);
                    } // false = down

                if (e.getSource() == gtool_change_y.up_button)
                    {
                    doUpDownY(true);
                    }

                if (e.getSource() == gtool_change_y.down_button)
                    {
                    doUpDownY(false);
                    }

                if (e.getSource() == gtool_edge_value.up_button)
                    {
                    doUpDownEdgeValue(true);
                    } // true = up

                if (e.getSource() == gtool_edge_value.down_button)
                    {
                    doUpDownEdgeValue(false);
                    } // false = down

                if (e.getSource() == n_change_name)
                    {
                    doChangeNodeName();
                    }

                if (e.getSource() == n_change_face)
                    {
                    doChangeFace();
                    }

                if (e.getSource() == n_face_width)
                    {
                    doFaceWidth();
                    }

                if (e.getSource() == n_no_face)
                    {
                    my_area.my_nodes[my_area.getSelectedActor()].noFace();
                    MainFrame.setNodeFace(new ImageIcon(""), my_area
                            .getSelectedActor());
                    }
                }
        };

    /*
     * private MouseAdapter adapt_area_clicked = new MouseAdapter() { public
     * void mouseClicked(MouseEvent e) { if (e.getClickCount() > 1) { if
     * (my_area.getSelectedActor() < 10000) { // double-click inside node: int
     * selnode = my_area.getSelectedActor(); Node candidate =
     * my_area.my_nodes[selnode]; if (node_dialog == null) { node_dialog = new
     * NodeSettingsDialog(candidate, selnode); node_dialog.showDialog(); } //
     * node_dialog = null; } else { // double-click outside node: try { int x =
     * e.getX(); int y = e.getY(); doAddNodeHere(x, y, my_area.getWidth()); }
     * catch (Exception exc) {} } } } };
     */

    // block to be used in act_area_clicked and constructor:
    public static void enableFirst()
        {
        glabel_x.setEnabled(true);
        glabel_y.setEnabled(true);
        gfield_x.setEnabled(true);
        gfield_y.setEnabled(true);
        gtool_change_x.up_button.setEnabled(true);
        gtool_change_x.down_button.setEnabled(true);
        gtool_change_y.up_button.setEnabled(true);
        gtool_change_y.down_button.setEnabled(true);
        gtool_delete_nodes.setEnabled(true);
        n_set_x.setEnabled(true);
        n_set_y.setEnabled(true);
        d_clone_node.setEnabled(true);
        d_isolate_node.setEnabled(true);
        d_delete_node.setEnabled(true);
        n_change_name.setEnabled(true);
        n_change_face.setEnabled(true);
        n_set_face.setEnabled(true);
        n_face_width.setEnabled(true);
        n_no_face.setEnabled(true);
        gfield_x.setText(String.valueOf(my_area.my_nodes[my_area
                .getSelectedActor()].getX(my_area.getWidth())));
        gfield_y.setText(String.valueOf(my_area.my_nodes[my_area
                .getSelectedActor()].getY(my_area.getWidth())));
        }

    // block to be used in act_area_clicked and constructor:
    public static void disableFirst()
        {
        gfield_x.setText("");
        gfield_y.setText("");
        e_set_edge.setEnabled(false);
        d_clone_node.setEnabled(false);
        d_isolate_node.setEnabled(false);
        d_delete_node.setEnabled(false);
        glabel_x.setEnabled(false);
        glabel_y.setEnabled(false);
        gfield_x.setEnabled(false);
        gfield_y.setEnabled(false);
        gtool_change_x.up_button.setEnabled(false);
        gtool_change_x.down_button.setEnabled(false);
        gtool_change_y.up_button.setEnabled(false);
        gtool_change_y.down_button.setEnabled(false);
        gtool_delete_nodes.setEnabled(false);
        n_set_x.setEnabled(false);
        n_set_y.setEnabled(false);
        n_change_name.setEnabled(false);
        n_change_face.setEnabled(false);
        n_set_face.setEnabled(false);
        n_face_width.setEnabled(false);
        n_no_face.setEnabled(false);
        glabel_edge_value.setEnabled(false);
        gfield_edge_value.setEnabled(false);
        gfield_edge_value.setText("");
        gtool_edge_value.setEnabled(false);
        gtool_edge_value.up_button.setEnabled(false);
        gtool_edge_value.down_button.setEnabled(false);
        }

    // block to be used in act_area_clicked and constructor:
    private static void enableSecond()
        {
        glabel_edge_value.setEnabled(true);
        gfield_edge_value.setEnabled(true);
        gtool_edge_value.up_button.setEnabled(true);
        gtool_edge_value.down_button.setEnabled(true);
        gfield_edge_value.setText(String.valueOf(gr_full_net.my_network
                .getValue(my_area.getSecondSelected(), my_area
                        .getSelectedActor())));
        e_set_edge.setEnabled(true);
        }

    // block to be used in act_area_clicked and constructor:
    private static void disableSecond()
        {
        e_set_edge.setEnabled(false);
        glabel_edge_value.setEnabled(false);
        gfield_edge_value.setEnabled(false);
        gfield_edge_value.setText("");
        gtool_edge_value.setEnabled(false);
        gtool_edge_value.up_button.setEnabled(false);
        gtool_edge_value.down_button.setEnabled(false);
        }

    private ActionListener act_area_clicked = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {
                boolean second_unsolved = true; // does second need to move one
                                                // step?
                if (e.getSource() == n_select_next_node
                        || e.getSource() == gtool_select_next)
                    {
                    doSelectNextActor();
                    }
                if (e.getSource() == n_select_next_arrow)
                    {
                    doSelectNextArrow();
                    second_unsolved = false;
                    }
                doManageEnablingComponents(e, second_unsolved);

                /*
                 * if (my_area.getSelectedActor() >=0) // if end-node is
                 * selected { enableFirst(); if (my_area.getSecondSelected() >=
                 * 0) // if start-node is selected { enableSecond(); if
                 * (e.getSource()==e_set_edge) { doChangeEdgeValue(); } if
                 * (second_unsolved && e.getSource()==n_select_next_arrow) {
                 * doSelectNextArrow(); } } else { disableSecond(); } } else {
                 * disableFirst(); }
                 */
                }
        };

    /**
     * Manages the enable/disable function for the menu items and toolbar
     * buttons, depending on edge and node selection
     */
    private void doManageEnablingComponents(ActionEvent e,
            boolean second_unsolved)
        {
        if (doManageEnablingComponentsStatic() && e != null)
            {
            if (e.getSource() == e_set_edge)
                {
                doChangeEdgeValueMenu();
                }
            if (second_unsolved && e.getSource() == n_select_next_arrow)
                {
                doSelectNextArrow();
                }
            }
        }

    /**
     * Manages the enable/disable function for the menu items and toolbar
     * buttons, depending on edge and node selection; returns true if arrow is
     * selected;
     */
    private static boolean doManageEnablingComponentsStatic()
        {
        boolean arrow_selected = false;
        if (my_area.getSelectedActor() >= 0) // if end-node is selected
            {
            enableFirst();
            if (my_area.getSecondSelected() >= 0) // if start-node is selected
                {
                enableSecond();
                arrow_selected = true;
                } else
                {
                disableSecond();
                }
            } else
            {
            disableFirst();
            }

        return arrow_selected;
        }

    public void showErrorParsing()
        {
        JOptionPane.showMessageDialog(null,
                "Coordinate values must be integers between 1 and "
                        + String
                                .valueOf(my_area.getWidth()
                                        - my_area.my_nodes[my_area
                                                .getSelectedActor()].node_size
                                        - 1), "Invalid value",
                JOptionPane.ERROR_MESSAGE);
        }

    private void doChangeX() // input din meniu
        {
        int new_x;
        String tmp_str;
        new_x = my_area.my_nodes[my_area.getSelectedActor()].getX(my_area
                .getWidth());
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new x-coordinate for the selected node (pixels)",
                "Horizontal position", JOptionPane.QUESTION_MESSAGE, null,
                null, String.valueOf(new_x));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            new_x = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            showErrorParsing();
            }
        if (new_x > 0
                && new_x < my_area.getWidth()
                        - my_area.my_nodes[my_area.getSelectedActor()]
                                .getSize())
            {
            my_area.my_nodes[my_area.getSelectedActor()].setX(new_x, my_area
                    .getWidth());
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();
            gfield_x.setText(String.valueOf(new_x));
            } else
            {
            showErrorParsing();
            }
        }

    private void doChangeY() // input din meniu
        {
        int new_y;
        String tmp_str;
        new_y = my_area.my_nodes[my_area.getSelectedActor()].getY(my_area
                .getWidth());
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new y-coordinate for the selected node (pixels)",
                "Vertical position", JOptionPane.QUESTION_MESSAGE, null, null,
                String.valueOf(new_y));
        gr_frame.repaint();
        if (tmp_str == null)
            return;

        try
            {
            new_y = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            showErrorParsing();
            }
        if (new_y > 0
                && new_y < my_area.getWidth()
                        - my_area.my_nodes[my_area.getSelectedActor()]
                                .getSize())
            {
            my_area.my_nodes[my_area.getSelectedActor()].setY(new_y, my_area
                    .getWidth());
            gr_full_net.setChanged(true);
            my_area.paintEdges();
            my_area.repaint();
            gfield_y.setText(String.valueOf(new_y));
            } else
            {
            showErrorParsing();
            }
        }

    private void doChangeEdgeValueMenu() // input valoarea arcului prin meniu
        {
        int i, j;
        float new_edge;
        String tmp_str;
        i = my_area.getSecondSelected();
        j = my_area.getSelectedActor();
        new_edge = gr_full_net.my_network.getValue(i, j);
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new value of connection from [ "
                        + my_area.my_nodes[i].name + " ] to [ "
                        + my_area.my_nodes[j].name + " ]", "Edge Value",
                JOptionPane.QUESTION_MESSAGE, null, null, String
                        .valueOf(new_edge));
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        if (tmp_str.equals(""))
            tmp_str = "0.0";
        try
            {
            new_edge = Float.parseFloat(tmp_str);
            gr_full_net.setValueToNetwork(new_edge, i, j);
            gr_model.setValue(tmp_str, i, j);
            gfield_edge_value.setText(String.valueOf(new_edge));
            } catch (NumberFormatException e1)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Edge value must be an integer or a real number.",
                    "Parsing Error", JOptionPane.ERROR_MESSAGE);
            }

        try
            {
            setHTMLStatus("<html><font size = 2 face='Arial,Helvetica,Verdana,sans-serif'>Edge selected: <font size = 2 color='#298C8C' face='Arial,Helvetica,Verdana,sans-serif'> "
                    + String.valueOf(my_area.my_nodes[my_area
                            .getSecondSelected()].name)
                    + " </font> --> <font size = 2 color='#298C8C'> "
                    + String.valueOf(my_area.my_nodes[my_area
                            .getSelectedActor()].name)
                    + " </font>.  Edge Value: <font size = 2 color='#298C8C'>"
                    + String.valueOf(new_edge));
            } catch (Exception e2)
            {
            setDefaultStatus();
            }

        my_area.paintEdges();
        // my_area.repaint();
        gr_frame.repaint();
        }

    // change Edge Value by textfield
    private void changeEdgeValue()
        {
        int i, j;
        float new_edge;
        i = my_area.getSecondSelected();
        j = my_area.getSelectedActor();
        if (i >= 0)
            {
            String tmp_str = gfield_edge_value.getText();
            if (tmp_str == null || tmp_str.equals("")) // no string to parse
                {
                new_edge = gr_full_net.my_network.getValue(i, j);
                gfield_edge_value.setText(String.valueOf(new_edge));
                gfield_edge_value.selectAll();
                return;
                }
            new_edge = 0f;
            try
                {
                new_edge = Float.parseFloat(tmp_str);
                gr_full_net.setValueToNetwork(new_edge, i, j);
                gr_model.setValue(tmp_str, i, j);
                MainFrame.getCurrentFullNet().setChanged(true);
                } catch (NumberFormatException e)
                {
                new_edge = gr_full_net.my_network.getValue(i, j);
                gfield_edge_value.setText(String.valueOf(new_edge));
                gfield_edge_value.selectAll();
                }
            try
                {
                setHTMLStatus("<html><font size = 2 face='Arial,Helvetica,Verdana,sans-serif'>Edge selected: <font size = 2 color='#298C8C' face='Arial,Helvetica,Verdana,sans-serif'> "
                        + String.valueOf(my_area.my_nodes[my_area
                                .getSecondSelected()].name)
                        + " </font> --> <font size = 2 color='#298C8C'> "
                        + String.valueOf(my_area.my_nodes[my_area
                                .getSelectedActor()].name)
                        + " </font>.  Edge Value: <font size = 2 color='#298C8C'>"
                        + String.valueOf(gr_full_net.my_network.getValue(
                                my_area.getSecondSelected(), my_area
                                        .getSelectedActor())));
                } catch (Exception e2)
                {
                setDefaultStatus();
                }

            my_area.paintEdges();
            my_area.repaint();
            }
        }

    // change x-cordinate
    private void changeX() // input din textfield
        {
        if (my_area.getSelectedActor() >= 0)
            {
            String tmp_str = gfield_x.getText();
            int tmp_int;
            tmp_int = 0;
            try
                {
                tmp_int = Integer.parseInt(tmp_str);
                } catch (Exception e)
                {
                tmp_int = my_area.my_nodes[my_area.getSelectedActor()]
                        .getX(my_area.getWidth());
                gfield_x.setText(String.valueOf(tmp_int));
                showErrorParsing();
                }
            if (tmp_int > 0
                    && tmp_int < my_area.getWidth()
                            - my_area.my_nodes[my_area.getSelectedActor()]
                                    .getSize())
                {
                my_area.my_nodes[my_area.getSelectedActor()].setX(tmp_int,
                        my_area.getWidth());
                MainFrame.getCurrentFullNet().setChanged(true);
                my_area.paintEdges();
                my_area.repaint();
                } else
                {
                gfield_x.setText(String.valueOf(my_area.my_nodes[my_area
                        .getSelectedActor()].getX(my_area.getWidth())));
                showErrorParsing();
                }
            }
        }

    private void changeY() // input din textfield
        {
        if (my_area.getSelectedActor() >= 0)
            {
            String tmp_str = gfield_y.getText();
            int tmp_int;
            tmp_int = 0;
            try
                {
                tmp_int = Integer.parseInt(tmp_str);
                } catch (Exception e)
                {
                tmp_int = my_area.my_nodes[my_area.getSelectedActor()]
                        .getY(my_area.getWidth());
                gfield_y.setText(String.valueOf(tmp_int));
                showErrorParsing();
                }
            if (tmp_int > 0
                    && tmp_int < my_area.getWidth()
                            - my_area.my_nodes[my_area.getSelectedActor()]
                                    .getSize())
                {
                my_area.my_nodes[my_area.getSelectedActor()].setY(tmp_int,
                        my_area.getWidth());
                MainFrame.getCurrentFullNet().setChanged(true);
                my_area.paintEdges();
                my_area.repaint();
                } else
                {
                gfield_y.setText(String.valueOf(my_area.my_nodes[my_area
                        .getSelectedActor()].getY(my_area.getWidth())));
                showErrorParsing();
                }
            }
        }

    private void doUpDownX(boolean up) // grows x by 1
        {
        int new_x = my_area.my_nodes[my_area.getSelectedActor()].getX(my_area
                .getWidth());
        if (up)
            {
            new_x++;
            } else
            {
            new_x--;
            }
        my_area.my_nodes[my_area.getSelectedActor()].setX(new_x, my_area
                .getWidth());
        setHTMLStatus("<html><font size = 2 face='Arial,Helvetica,Verdana,sans-serif'>"
                + my_area.my_nodes[my_area.getSelectedActor()].name
                + "  -  Current position: ( "
                + String.valueOf(new_x)
                + " , "
                + String.valueOf(my_area.my_nodes[my_area.getSelectedActor()]
                        .getY(my_area.getWidth())) + " ) ");
        gr_full_net.setChanged(true);
        my_area.paintEdges();
        my_area.repaint();
        gfield_x.setText(String.valueOf(new_x));
        }

    private void doUpDownY(boolean up) // grows y by 1
        {
        int new_y = my_area.my_nodes[my_area.getSelectedActor()].getY(my_area
                .getWidth());
        if (up)
            {
            new_y++;
            } else
            {
            new_y--;
            }
        my_area.my_nodes[my_area.getSelectedActor()].setY(new_y, my_area
                .getWidth());
        setHTMLStatus("<html><font size = 2 face='Arial,Helvetica,Verdana,sans-serif'>"
                + my_area.my_nodes[my_area.getSelectedActor()].name
                + "  -  Current position: ( "
                + String.valueOf(my_area.my_nodes[my_area.getSelectedActor()]
                        .getX(my_area.getWidth()))
                + " , "
                + String.valueOf(new_y) + " ) ");
        gr_full_net.setChanged(true);
        my_area.paintEdges();
        my_area.repaint();
        gfield_y.setText(String.valueOf(new_y));
        }

    private void doUpDownEdgeValue(boolean up) // grows connection by 1
        {
        int i = my_area.getSecondSelected();
        int j = my_area.getSelectedActor();
        float new_edge = gr_full_net.my_network.getValue(i, j);
        if (up)
            {
            new_edge = new_edge + 1;
            } else
            {
            new_edge = new_edge - 1;
            }
        gfield_edge_value.setText(String.valueOf(new_edge));
        gr_full_net.setValueToNetwork(new_edge, i, j);
        gr_model.setValue(String.valueOf(new_edge), i, j);
        gr_full_net.setChanged(true);
        try
            {
            setHTMLStatus("<html><font size = 2 face='Arial,Helvetica,Verdana,sans-serif'>Edge selected: <font size = 2 color='#298C8C' face='Arial,Helvetica,Verdana,sans-serif'> "
                    + String.valueOf(my_area.my_nodes[my_area
                            .getSecondSelected()].name)
                    + " </font> --> <font size = 2 color='#298C8C'> "
                    + String.valueOf(my_area.my_nodes[my_area
                            .getSelectedActor()].name)
                    + " </font>.  Edge Value: <font size = 2 color='#298C8C'>"
                    + String.valueOf(gr_full_net.my_network.getValue(my_area
                            .getSecondSelected(), my_area.getSelectedActor())));
            } catch (Exception e3) {
      AgnaLog.warn("suppressed exception", e3);
      }
        my_area.paintEdges();
        my_area.repaint();
        }

    private void doChangeNodeName() // change node name
        {
        String tmp_str, tmp_name;
        int selnode = my_area.getSelectedActor();
        tmp_name = my_area.my_nodes[selnode].getName();
        tmp_str = (String) JOptionPane
                .showInputDialog(gr_frame,
                        "Please enter a new name for node [ " + tmp_name
                                + " ]:", "Node name",
                        JOptionPane.QUESTION_MESSAGE, null, null, tmp_name);
        gr_frame.repaint();
        if (tmp_str != null)
            {
            // my_area.my_nodes[selnode].setName(tmp_str);
            gr_full_net.getNetwork().setNodeName(tmp_str, selnode);
            gr_full_net.setChanged(true);
            MainFrame.setNodeName(tmp_str, selnode);
            }
        }

    private void doChangeFace()
        {
        int selnode = my_area.getSelectedActor();
        if (selnode == -1)
            return;
        String tmp_image_name = inputImageFile(my_area.my_nodes[selnode]
                .getFaceSource());
        if (tmp_image_name == null)
            return;
        ImageIcon tmp_icon = null;
        try
            {
            tmp_icon = new ImageIcon(tmp_image_name);
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        if (tmp_icon == null)
            return;
        else
            {
            my_area.my_nodes[selnode].setFace(tmp_image_name);
            MainFrame.setNodeFace(tmp_icon, selnode);
            gr_full_net.setChanged(true);
            }
        }

    private void doFaceWidth()
        {
        String face_width, tmp_str;
        String tmp_face_width = String.valueOf(my_area.my_nodes[my_area
                .getSelectedActor()].getSize());
        tmp_str = (String) JOptionPane.showInputDialog(gr_frame,
                "Please enter new face width for selected node (px)",
                "Node face width", JOptionPane.QUESTION_MESSAGE, null, null,
                tmp_face_width);
        gr_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            if (Integer.parseInt(tmp_str) <= 1)
                {
                JOptionPane.showMessageDialog(gr_frame,
                        "Faces can't be smaller than 1 px.", "Parsing error",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
                }
            if (Integer.parseInt(tmp_str) > 1000)
                return;
            my_area.my_nodes[my_area.getSelectedActor()].setSize(Integer
                    .parseInt(tmp_str));
            gr_full_net.setChanged(true);
            gr_frame.repaint();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(gr_frame,
                    "Face width must be an integer.", "Parsing error",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }

    public static VerticalToolBar getCurrentVerticalToolBar()
        {
        return vertical_tools;
        }

    public static JFrame getCurrentFrame()
        {
        return gr_frame;
        }

    public ImageIcon getMainIcon()
        {
        return main_icon;
        }

    public static int getAreaWidth()
        {
        return my_area.getWidth();
        }

    public static String inputImageFile(String initial_directory)
        {
        String tmp_image_name = "";
        File image_file = new File(initial_directory);
        JFileChooser image_chooser = new JFileChooser();
        image_chooser.setDialogTitle("Open image file");
        image_chooser.setCurrentDirectory(image_file);
        image_chooser.setSelectedFile(image_file);
        image_chooser.setMultiSelectionEnabled(false);
        image_chooser.setApproveButtonToolTipText("Select file and click here");
        image_chooser.addChoosableFileFilter(new GifJpgFilesFilter());
        image_chooser.setAccessory(new Previewer(image_chooser));
        int return_val = image_chooser.showOpenDialog(gr_frame);
        if (return_val == JFileChooser.CANCEL_OPTION)
            return null;
        image_file = image_chooser.getSelectedFile();
        gr_frame.repaint();
        try
            {
            tmp_image_name = image_file.getCanonicalPath();
            } catch (Exception esc)
            {
            return null;
            }
        image_chooser = null;
        image_file = null;
        int ns;
        ImageIcon tmp_icon = new ImageIcon();
        if (tmp_image_name != null && !tmp_image_name.equals(""))
            {
            try
                {
                tmp_icon = new ImageIcon(tmp_image_name);
                return tmp_image_name;
                } catch (Exception e)
                {
                JOptionPane.showMessageDialog(gr_frame, "Error reading image "
                        + tmp_image_name, "Image Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
                }
            }
        return tmp_image_name;

        }

    public static void setStatus(String str)
        {
        status_bar
                .setText("<html><font size=2 face='Arial,Helvetica,Verdana,sans-serif'>"
                        + str);
        }

    public static void setHTMLStatus(String str)
        {
        status_bar.setText(str);
        }

    public static void setDefaultStatus()
        {
        status_bar.setText(DEFAULT_STATUS);
        }

    // places network matrix into table-grid
    public static void setMatrix()
        {
        gr_model.setReady(false);
        MainFrame.updateMatrix();
        gr_model.setReady(true);
        
        }

    private void makeFacesMenuItems(String directory, JMenu actual_menu)
        {
        String actual_dir_name = directory;
        String image_name;
        File actual_dir = new File(actual_dir_name);
        if (!actual_dir.exists())
            return;
        File[] childs = actual_dir.listFiles();
        ImageIcon mi = null;
        JMenuItem fi = null;
        JMenu ki = null;
        for (int i = 0; i < childs.length; i++)
            {
            if (childs[i].exists())
                {
                if (!childs[i].isDirectory())
                    {
                    try
                        {
                        mi = new ImageIcon(childs[i].getCanonicalPath());
                        mi = new ImageIcon(mi.getImage().getScaledInstance(16,
                                -1, Image.SCALE_FAST));
                        image_name = childs[i].getName();
                        fi = new JMenuItem(image_name, mi);
                        fi.setName(childs[i].getCanonicalPath());
                        fi.addActionListener(act_face_menu);
                        image_name = IOUtils.getExtension(image_name);
                        if (image_name.equals("gif")
                                || image_name.equals("jpg")
                                || image_name.equals("jpeg"))
                            actual_menu.add(fi);
                        } catch (Exception e)
                        {
                        mi = null;
                        }
                    } else
                    {
                    ki = new JMenu(childs[i].getName());
                    // actual_menu.add(ki);
                    actual_menu.insert(ki, 2);
                    try
                        {
                        makeFacesMenuItems(childs[i].getCanonicalPath(), ki);
                        } catch (Exception ee) {
      AgnaLog.warn("suppressed exception", ee);
      }
                    }
                }
            }
        mi = null;
        fi = null;
        }

    // jmenu created at runtime from directory list:
    private void makeFacesMenu()
        {
        String root_directory_name = Environment.getCurrentDirectory()
                + "Faces" + System.getProperty("file.separator");
        makeFacesMenuItems(root_directory_name, n_set_face);
        }

    private Color showChooser()
        {
        JColorChooser tmp_chooser = new JColorChooser();
        tmp_chooser.setPreviewPanel(null);
        tmp_chooser.revalidate();
        Color tmp_color = tmp_chooser.showDialog(gr_frame, "Color Chooser",
                my_area.arrow_color);
        return tmp_color;
        }

    public GrNet(FullNet tmp_net, AgnaTableModel tmp_model,
            AgnaTextPane tmp_pane)
        {
        final String current_dir = Environment.getCurrentDirectory();
        final String fs = System.getProperty("file.separator");
        update_network_needed = false;
        gr_full_net = tmp_net;
        gr_model = tmp_model;
        gr_output_pane = tmp_pane;
        gr_exists = true;

        gr_frame = new JFrame();

        if (gr_full_net.getNetworkFileName().equals(""))
            gr_frame.setTitle(gr_full_net.getNetwork().getName()
                    + " - Network Viewer - "
                    + Environment.getApplicationFullName());
        else
            gr_frame.setTitle(IOUtils.getNameWithoutExtension(gr_full_net
                    .getNetworkFileName())
                    + " - Network Viewer - "
                    + Environment.getApplicationFullName());
        // my_area=new NodeArea();
        my_area = tmp_net.net_area;
        ToolTipManager.sharedInstance().registerComponent(my_area);
        int a_width = my_area.getWidth();

        dim_frame = new Dimension(new Dimension(600, 470));
        max_dim = gr_frame.getToolkit().getScreenSize();
        dim_inner_frame = new Dimension(a_width, a_width);
        gr_frame.setSize(dim_frame);
        gr_frame.setLocation(100, 50);
        main_icon = Environment.getButtonImageIcon("Agna_icon.gif");
        gr_frame.setIconImage(main_icon.getImage());

        gContent = gr_frame.getContentPane();
        gControlArea = new JPanel();
        gControlArea.setLayout(new BorderLayout());
        // gContent.setBackground(Color.white);
        gControlArea.setPreferredSize(dim_frame);
        my_area.setPreferredSize(dim_inner_frame);
        my_area.addActionListener(act_area_clicked);
        // my_area.addMouseListener(adapt_area_clicked);

        // genereaza miscarile nodului prin keys:
        up_x_key = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
                InputEvent.ALT_MASK, false);
        down_x_key = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
                InputEvent.ALT_MASK, false);
        down_y_key = KeyStroke.getKeyStroke(KeyEvent.VK_UP,
                InputEvent.ALT_MASK, false);
        up_y_key = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
                InputEvent.ALT_MASK, false);
        up_edge_value_key = KeyStroke.getKeyStroke(KeyEvent.VK_A,
                InputEvent.ALT_MASK, false);
        down_edge_value_key = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                InputEvent.ALT_MASK, false);

        gr_frame.getRootPane().registerKeyboardAction(act_x_up, up_x_key,
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        gr_frame.getRootPane().registerKeyboardAction(act_x_down, down_x_key,
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        gr_frame.getRootPane().registerKeyboardAction(act_y_up, up_y_key,
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        gr_frame.getRootPane().registerKeyboardAction(act_y_down, down_y_key,
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        gr_frame.getRootPane().registerKeyboardAction(act_edge_value_up,
                up_edge_value_key,
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        gr_frame.getRootPane().registerKeyboardAction(act_edge_value_down,
                down_edge_value_key,
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // asociaza key cu actionlistener:
        // KeyStroke my_key=KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
        // InputEvent.ALT_MASK, false);
        // gr_frame.getRootPane().registerKeyboardAction(act_area_clicked,
        // my_key, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // meniu si toolbar:
        g_mb = new JMenuBar();

        gFile = new JMenu("File");
        gImage = new JMenu("Image"); // fostul Edit
        gNodes = new JMenu("Node");
        JMenu e_edge = new JMenu("Edge");
        gData = new JMenu("Data");
        gPreferences = new JMenu("Preferences");
        gHelp = new JMenu("Help");

        gFile.setMnemonic('f');
        gImage.setMnemonic('i');
        gNodes.setMnemonic('n');
        gHelp.setMnemonic('h');

        // submeniuri din FILE:
        f_save_as = new JMenuItem("Export Image...");
        f_insert = new JMenuItem("Insert In Output");
        f_quit = new JMenuItem("Quit Network Viewer");

        f_save_as.setToolTipText("Save current image as JPEG file");
        f_insert.setToolTipText("Insert image in current output");
        f_quit.setToolTipText("Close Network Viewer's frame");

        f_save_as.setMnemonic('x');
        f_insert.setMnemonic('i');
        f_quit.setMnemonic('q');
        f_save_as.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                ActionEvent.CTRL_MASK));
        f_insert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                ActionEvent.CTRL_MASK));
        f_quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                ActionEvent.CTRL_MASK));

        f_save_as.setEnabled(true);
        f_quit.setEnabled(true);

        f_quit.addActionListener(act_change_image);

        f_save_as.addActionListener(act_change_image);
        f_insert.addActionListener(act_change_image);

        gFile.add(f_save_as);
        gFile.add(f_insert);
        gFile.addSeparator();
        gFile.add(f_quit);

        // submeniuri din IMAGE & EDGE:
        e_image_width = new JMenuItem("Image Width...");
        e_scale_image = new JMenuItem("Scale Image...");
        e_random_layout = new JMenuItem("Random Layout");
        e_circular_layout = new JMenuItem("Circular Layout");
        JMenu e_background_i = new JMenu("Background Picture");
        e_background_color = new JMenuItem("Background Color...");
        e_background_image = new JMenuItem("From File...");
        e_background_x = new JMenuItem("X-coordinate...");
        e_background_y = new JMenuItem("Y-coordinate...");
        e_background_width = new JMenuItem("Width...");
        e_scale_background = new JMenuItem("Scale Picture...");
        e_fit_background_to_image = new JMenuItem("Fit To Area");
        e_background_height = new JMenuItem("Height");
        e_no_background_image = new JMenuItem("No Background Picture");
        e_allow_ES = new JMenuItem("Allow Edge Selection");
        e_disallow_ES = new JMenuItem("Disallow Edge Selection");
        e_edge_separator = new JMenuItem("Edge Separator...");
        e_edge_color = new JMenuItem("Edge Color...");
        e_max_transparency = new JMenuItem("Most Faded Edge...");
        e_set_loyalty = new JMenuItem("Set Edge Loyalty");
        e_remove_loyalty = new JMenuItem("Remove Edge Loyalty");
        e_set_edge = new JMenuItem("Edge Value...");
        e_show_edge_value = new JMenuItem("Show Edge Value");
        e_hide_edge_value = new JMenuItem("Hide Edge Value");
        e_edge_value_position = new JMenuItem("Edge Value Position...");
        e_edge_value_color = new JMenuItem("Edge Value Color...");
        e_names_properties = new JMenu("Names");
        e_view_names = new JMenuItem("Show Names");
        e_hide_names = new JMenuItem("Hide Names");
        e_names_color = new JMenuItem("Names Color...");
        // e_names_font=new JMenuItem("Names Font...");
        e_names_x = new JMenuItem("X-coordinate...");
        e_names_y = new JMenuItem("Y-coordinate...");
        e_grid = new JMenu("Grid");
        e_grid_step = new JMenuItem("Grid Step...");
        e_grid_transparency = new JMenuItem("Grid Transparency...");
        e_view_grid = new JMenuItem("Show Grid");
        e_hide_grid = new JMenuItem("Hide Grid");
        e_grid_color = new JMenuItem("Grid Color...");
        e_stg = new JMenuItem("Snap To Grid");
        e_dstg = new JMenuItem("Don't Snap To Grid");
        e_title = new JMenu("Title");
        e_view_title = new JMenuItem("Show Title");
        e_hide_title = new JMenuItem("Hide Title");
        e_set_title = new JMenuItem("Change Title...");
        e_title_color = new JMenuItem("Title Color...");
        e_title_x = new JMenuItem("X-coordinate...");
        e_title_y = new JMenuItem("Y-coordinate...");
        e_view_faces = new JMenuItem("Show All Faces");
        e_hide_faces = new JMenuItem("Hide All Faces");
        e_change_faces = new JMenuItem("All Faces...");
        e_change_faces_width = new JMenuItem("All Faces Width...");

        e_image_width.setToolTipText("Change image size");
        e_scale_image.setToolTipText("Scale image proportionally");
        e_random_layout.setToolTipText("Spread all nodes randomly");
        e_circular_layout.setToolTipText("Arrange all nodes in a circle");
        e_background_i.setToolTipText("Background image settings");
        e_background_color.setToolTipText("Change background color");
        e_background_image
                .setToolTipText("Set a picture from file as background");
        e_background_x
                .setToolTipText("Change background picture's horizontal coordinate");
        e_background_y
                .setToolTipText("Change background picture's vertical coordinate");
        e_background_width.setToolTipText("Change background picture's width");
        e_scale_background
                .setToolTipText("Scale background picture by specified percent");
        e_fit_background_to_image
                .setToolTipText("Scale background picture to image size");
        e_background_height
                .setToolTipText("Change background picture's height");
        e_no_background_image.setToolTipText("Destroy background picture");
        e_allow_ES
                .setToolTipText("Allow edge selectionon successive node-click");
        e_disallow_ES.setToolTipText("No edge selection allowed");
        e_edge_separator
                .setToolTipText("Change distance between two symmetric edges");
        e_edge_color.setToolTipText("Change edge color");
        e_max_transparency
                .setToolTipText("Change transparency rate of most faded edge");
        e_set_loyalty
                .setToolTipText("Apply edge transparency relative to edge value");
        e_remove_loyalty.setToolTipText("Remove all edge-transparency");
        e_set_edge.setToolTipText("Change value of selected edge");
        e_show_edge_value.setToolTipText("Paint all edge values");
        e_hide_edge_value.setToolTipText("Don't paint edge values");
        e_edge_value_position.setToolTipText("Change edge value position");
        e_edge_value_color.setToolTipText("Change edge value color");
        e_names_properties.setToolTipText("Node names settings");
        e_view_names.setToolTipText("Paint all node names");
        e_hide_names.setToolTipText("Don't paint node names");
        e_names_color.setToolTipText("Change node names color");
        e_names_x
                .setToolTipText("Change horizontal position of node names relative to nodes");
        e_names_y
                .setToolTipText("Change vertical position of node names relative to nodes");
        e_grid.setToolTipText("Grid settings");
        e_grid_step.setToolTipText("Change grid step");
        e_grid_transparency.setToolTipText("Change grid transparency");
        e_view_grid.setToolTipText("Paint grid");
        e_hide_grid.setToolTipText("Don't paint grid");
        e_grid_color.setToolTipText("Change grid's color");
        e_stg.setToolTipText("Snap nodes to grid lines");
        e_dstg.setToolTipText("Don't snap nodes to grid lines");
        e_title.setToolTipText("Title settings");
        e_view_title.setToolTipText("Paint network tile");
        e_hide_title.setToolTipText("Don't paint network title");
        e_set_title.setToolTipText("Change network title");
        e_title_color.setToolTipText("Change network title's color");
        e_title_x
                .setToolTipText("Change network title's horizontal coordinate");
        e_title_y.setToolTipText("Change network title's vertical coordinate");
        e_view_faces.setToolTipText("Paint all node faces");
        e_hide_faces.setToolTipText("Don't paint node faces");
        e_change_faces.setToolTipText("Set picture as face for all nodes");
        e_change_faces_width.setToolTipText("Change all nodes size");

        e_image_width.setMnemonic('w');
        e_scale_image.setMnemonic('s');
        e_random_layout.setMnemonic('r');
        e_circular_layout.setMnemonic('c');
        e_background_i.setMnemonic('b');
        e_background_color.setMnemonic('k');
        e_background_image.setMnemonic('f');
        e_background_x.setMnemonic('x');
        e_background_y.setMnemonic('y');
        e_background_width.setMnemonic('w');
        e_scale_background.setMnemonic('s');
        e_fit_background_to_image.setMnemonic('a');
        e_background_height.setMnemonic('h');
        e_no_background_image.setMnemonic('p');
        e_allow_ES.setMnemonic('a');
        e_disallow_ES.setMnemonic('d');
        e_edge_separator.setMnemonic('s');
        e_edge_color.setMnemonic('c');
        e_max_transparency.setMnemonic('m');
        e_set_loyalty.setMnemonic('l');
        e_remove_loyalty.setMnemonic('g');
        e_set_edge.setMnemonic('v');
        e_show_edge_value.setMnemonic('w');
        e_hide_edge_value.setMnemonic('h');
        e_edge_value_position.setMnemonic('p');
        e_edge_value_color.setMnemonic('u');
        e_names_properties.setMnemonic('n');
        e_view_names.setMnemonic('s');
        e_hide_names.setMnemonic('h');
        e_names_color.setMnemonic('c');
        e_names_x.setMnemonic('x');
        e_names_y.setMnemonic('y');
        e_grid.setMnemonic('g');
        e_grid_step.setMnemonic('e');
        e_grid_transparency.setMnemonic('t');
        e_view_grid.setMnemonic('s');
        e_hide_grid.setMnemonic('h');
        e_grid_color.setMnemonic('c');
        e_stg.setMnemonic('g');
        e_dstg.setMnemonic('d');
        e_title.setMnemonic('t');
        e_view_title.setMnemonic('s');
        e_hide_title.setMnemonic('h');
        e_set_title.setMnemonic('t');
        e_title_color.setMnemonic('c');
        e_title_x.setMnemonic('x');
        e_title_y.setMnemonic('y');
        e_view_faces.setMnemonic('o');
        e_hide_faces.setMnemonic('h');
        e_change_faces.setMnemonic('a');
        e_change_faces_width.setMnemonic('w');

        // shortcut: (nu mere)
        // e_max_transparency.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
        // ActionEvent.CTRL_MASK));
        e_set_edge.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                ActionEvent.CTRL_MASK));
        e_stg.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.CTRL_MASK));
        e_dstg.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                ActionEvent.CTRL_MASK));
        e_allow_ES.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                ActionEvent.CTRL_MASK));
        e_disallow_ES.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                ActionEvent.CTRL_MASK));

        e_image_width.setEnabled(true);
        e_scale_image.setEnabled(true);
        e_background_color.setEnabled(true);
        e_background_image.setEnabled(true);
        e_edge_color.setEnabled(true);
        e_names_color.setEnabled(true);
        // e_names_font.setEnabled(true);
        e_set_edge.setEnabled(false);

        boolean is = false; // temporary boolean variable;

        is = my_area.getGridEnabled();
        e_hide_grid.setEnabled(is);
        e_view_grid.setEnabled(!is);

        is = my_area.getPrintNames();
        e_view_names.setEnabled(!is);
        e_hide_names.setEnabled(is);

        is = my_area.getColorFidelity();
        e_set_loyalty.setEnabled(!is);
        e_remove_loyalty.setEnabled(is);

        is = my_area.isSTGEnabled();
        e_stg.setEnabled(!is);
        e_dstg.setEnabled(is);

        is = my_area.isFaceVisible();
        e_view_faces.setEnabled(!is);
        e_hide_faces.setEnabled(is);

        is = my_area.isEdgeValueVisible();
        e_show_edge_value.setEnabled(!is);
        e_hide_edge_value.setEnabled(is);

        is = my_area.allow_ES;
        e_allow_ES.setEnabled(!is);
        e_disallow_ES.setEnabled(is);

        is = my_area.isTitleVisible();
        e_view_title.setEnabled(!is);
        e_hide_title.setEnabled(is);

        e_image_width.addActionListener(act_change_image);
        e_scale_image.addActionListener(act_change_image);
        e_random_layout.addActionListener(act_change_image);
        e_circular_layout.addActionListener(act_change_image);
        e_background_color.addActionListener(act_change_image);
        e_background_image.addActionListener(act_change_image);
        e_background_x.addActionListener(act_change_image);
        e_background_y.addActionListener(act_change_image);
        e_background_width.addActionListener(act_change_image);
        e_scale_background.addActionListener(act_change_image);
        e_fit_background_to_image.addActionListener(act_change_image);
        e_background_height.addActionListener(act_change_image);
        e_no_background_image.addActionListener(act_change_image);
        e_allow_ES.addActionListener(act_change_image);
        e_disallow_ES.addActionListener(act_change_image);
        e_edge_separator.addActionListener(act_change_image);
        e_edge_color.addActionListener(act_change_image);
        e_max_transparency.addActionListener(act_change_image);
        e_set_loyalty.addActionListener(act_change_image);
        e_remove_loyalty.addActionListener(act_change_image);
        e_set_edge.addActionListener(act_area_clicked);
        e_show_edge_value.addActionListener(act_change_image);
        e_hide_edge_value.addActionListener(act_change_image);
        e_edge_value_position.addActionListener(act_change_image);
        e_edge_value_color.addActionListener(act_change_image);
        e_view_names.addActionListener(act_change_image);
        e_hide_names.addActionListener(act_change_image);
        e_names_color.addActionListener(act_change_image);
        // e_names_font.addActionListener(act_change_image);
        e_names_x.addActionListener(act_change_image);
        e_names_y.addActionListener(act_change_image);
        e_grid_step.addActionListener(act_change_image);
        e_grid_transparency.addActionListener(act_change_image);
        e_view_grid.addActionListener(act_change_image);
        e_hide_grid.addActionListener(act_change_image);
        e_grid_color.addActionListener(act_change_image);
        e_stg.addActionListener(act_change_image);
        e_dstg.addActionListener(act_change_image);
        e_view_title.addActionListener(act_change_image);
        e_hide_title.addActionListener(act_change_image);
        e_set_title.addActionListener(act_change_image);
        e_title_color.addActionListener(act_change_image);
        e_title_x.addActionListener(act_change_image);
        e_title_y.addActionListener(act_change_image);
        e_view_faces.addActionListener(act_change_image);
        e_hide_faces.addActionListener(act_change_image);
        e_change_faces.addActionListener(act_change_image);
        e_change_faces_width.addActionListener(act_change_image);

        e_background_i.add(e_background_image);
        e_background_i.addSeparator();
        e_background_i.add(e_background_width);
        e_background_i.add(e_background_height);
        e_background_i.addSeparator();
        e_background_i.add(e_scale_background);
        e_background_i.add(e_fit_background_to_image);
        e_background_i.addSeparator();
        e_background_i.add(e_background_x);
        e_background_i.add(e_background_y);

        e_names_properties.add(e_view_names);
        e_names_properties.add(e_hide_names);
        e_names_properties.addSeparator();
        e_names_properties.add(e_names_color);
        // e_names_properties.add(e_names_font);
        e_names_properties.addSeparator();
        e_names_properties.add(e_names_x);
        e_names_properties.add(e_names_y);

        e_grid.add(e_grid_step);
        e_grid.add(e_grid_transparency);
        e_grid.add(e_grid_color);
        e_grid.addSeparator();
        e_grid.add(e_view_grid);
        e_grid.add(e_hide_grid);
        e_grid.addSeparator();
        e_grid.add(e_stg);
        e_grid.add(e_dstg);

        e_edge.setMnemonic('e');
        e_edge.add(e_set_edge);
        e_edge.addSeparator();
        e_edge.add(e_allow_ES);
        e_edge.add(e_disallow_ES);
        e_edge.addSeparator();
        e_edge.add(e_edge_separator);
        e_edge.add(e_edge_color);
        e_edge.add(e_max_transparency);
        e_edge.addSeparator();
        e_edge.add(e_set_loyalty);
        e_edge.add(e_remove_loyalty);
        e_edge.addSeparator();
        e_edge.add(e_show_edge_value);
        e_edge.add(e_hide_edge_value);
        e_edge.addSeparator();
        e_edge.add(e_edge_value_color);
        e_edge.add(e_edge_value_position);

        e_title.add(e_view_title);
        e_title.add(e_hide_title);
        e_title.addSeparator();
        e_title.add(e_set_title);
        e_title.add(e_title_color);
        e_title.addSeparator();
        e_title.add(e_title_x);
        e_title.add(e_title_y);

        gImage.add(e_image_width);
        gImage.add(e_scale_image);
        gImage.addSeparator();
        gImage.add(e_random_layout);
        gImage.add(e_circular_layout);
        gImage.addSeparator();
        gImage.add(e_title);
        gImage.add(e_grid);
        gImage.add(e_names_properties);
        gImage.addSeparator();
        gImage.add(e_background_color);
        gImage.add(e_background_i);
        gImage.add(e_no_background_image);
        gImage.addSeparator();
        gImage.add(e_view_faces);
        gImage.add(e_hide_faces);
        gImage.addSeparator();
        gImage.add(e_change_faces);
        gImage.add(e_change_faces_width);

        // submeniuri din NODES
        n_set_x = new JMenuItem("X-coordinate...");
        n_set_y = new JMenuItem("Y-coordinate...");
        n_change_name = new JMenuItem("Name...");
        n_set_face = new JMenu("Face");
        try
            {
            n_default_face = new JMenuItem("Default Face", new ImageIcon(
                    MainFrame.getDefaultNodeFaceSource()));
            } catch (Exception e1)
            {
            n_default_face = new JMenuItem("Default Face");
            }
        n_default_face.setName(MainFrame.getDefaultNodeFaceSource());
        n_change_face = new JMenuItem("From File..."); // open from file
        n_no_face = new JMenuItem("No Face");
        n_face_width = new JMenuItem("Face Width...");
        n_select_next_node = new JMenuItem("Next End-node");
        n_select_next_arrow = new JMenuItem("Next Start-node");

        n_set_x.setToolTipText("Change selected node's horizontal coordinate");
        n_set_y.setToolTipText("Change selected node's vertical coordinate");
        n_change_name.setToolTipText("Change selected node's name");
        n_set_face.setToolTipText("Node face settings");
        n_default_face.setToolTipText("Apply default face to selected node");
        n_change_face
                .setToolTipText("Change selected node's horizontal coordinate");
        n_no_face.setToolTipText("Destroy selected node's face");
        n_face_width.setToolTipText("Change selected node's size");
        n_select_next_node.setToolTipText("Select next (End-)node");
        n_select_next_arrow.setToolTipText("Select next Start-node");

        n_set_face.setMnemonic('f');
        n_face_width.setMnemonic('w');
        n_set_x.setMnemonic('x');
        n_set_y.setMnemonic('y');
        n_change_name.setMnemonic('n');
        n_change_face.setMnemonic('f');
        n_no_face.setMnemonic('o');
        n_select_next_node.setMnemonic('e');
        n_select_next_arrow.setMnemonic('s');

        n_select_next_node.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                ActionEvent.ALT_MASK));
        n_select_next_arrow.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, ActionEvent.ALT_MASK));

        n_set_x.addActionListener(act_change_node);
        n_set_y.addActionListener(act_change_node);
        n_change_name.addActionListener(act_change_node);
        n_change_face.addActionListener(act_change_node);
        n_face_width.addActionListener(act_change_node);
        n_no_face.addActionListener(act_change_node);
        n_default_face.addActionListener(act_face_menu);
        n_select_next_node.addActionListener(act_area_clicked);
        n_select_next_arrow.addActionListener(act_area_clicked);
        n_set_x.setEnabled(false);
        n_set_y.setEnabled(false);
        n_change_name.setEnabled(false);
        n_face_width.setEnabled(false);
        n_set_face.setEnabled(false);
        n_change_face.setEnabled(false);
        n_no_face.setEnabled(false);

        n_select_next_arrow.setEnabled(my_area.allow_ES);

        n_set_face.add(n_change_face);
        n_set_face.addSeparator();
        n_set_face.addSeparator();
        n_set_face.add(n_default_face);
        n_set_face.addSeparator();
        // adding face file names:
        makeFacesMenu();

        gNodes.addSeparator();
        gNodes.add(n_change_name);
        gNodes.addSeparator();
        gNodes.add(n_set_face);
        gNodes.add(n_no_face);
        gNodes.add(n_face_width);
        gNodes.addSeparator();
        gNodes.add(n_set_x);
        gNodes.add(n_set_y);
        gNodes.addSeparator();
        gNodes.add(n_select_next_node);
        gNodes.add(n_select_next_arrow);

        // submeniuri din PREFERENCES:
        p_default_style = new JMenuItem("Save Settings As Default");
        p_copy_style = new JMenuItem("Copy Settings From...");
        p_default_face = new JMenuItem("Default Node Face...");

        p_default_style.setToolTipText("Save current settings as default");
        p_copy_style.setToolTipText("Copy settings from existing network file");
        p_default_face.setToolTipText("Change default face picture");

        p_default_style.setMnemonic('d');
        p_copy_style.setMnemonic('s');
        p_default_face.setMnemonic('c');

        p_default_style.addActionListener(act_change_image);
        p_copy_style.addActionListener(act_change_image);
        p_default_face.addActionListener(act_change_image);

        gPreferences.setMnemonic('p');
        gPreferences.add(p_default_face);
        gPreferences.addSeparator();
        gPreferences.add(p_default_style);
        gPreferences.add(p_copy_style);

        // submeniuri din HELP:
        h_contents = new JMenuItem("Contents...");
        h_about = new JMenuItem("About Agna...");

        h_contents.setToolTipText("Display the Agna Help frame");
        // generates strange error:
        // h_about.setToolTipText("Display Agna copyright information");

        h_contents.setMnemonic('c');
        h_about.setMnemonic('a');
        h_contents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                ActionEvent.CTRL_MASK));

        h_contents.addActionListener(act_change_image);
        h_about.addActionListener(act_change_image);

        gHelp.add(h_contents);
        gHelp.add(h_about);

        // Submeniuri din DATA:
        d_add_node = new JMenuItem("Add Nodes");
        d_clone_node = new JMenuItem("Clone Node");
        d_isolate_node = new JMenuItem("Isolate Node");
        d_delete_node = new JMenuItem("Delete Node");
        d_transpose = new JMenuItem("Transpose");
        d_symmetrize = new JMenuItem("Symmetrize...");
        d_remove_outsiders = new JMenuItem("Remove Outsiders");

        d_add_node.setToolTipText("Add new nodes to current network");
        d_clone_node.setToolTipText("Add a clone of the selected node");
        d_isolate_node
                .setToolTipText("Delete all connections of the selected node");
        d_delete_node.setToolTipText("Remove selected node from network");
        d_transpose.setToolTipText("Transpose current network");
        d_symmetrize.setToolTipText("Symmetrize current network");
        d_remove_outsiders.setToolTipText("Remove all isolated nodes");

        d_delete_node.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
                0));
        d_add_node.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                ActionEvent.CTRL_MASK));
        d_clone_node.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                ActionEvent.CTRL_MASK));
        d_remove_outsiders.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                ActionEvent.CTRL_MASK));
        d_symmetrize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                ActionEvent.CTRL_MASK));
        d_add_node.setMnemonic('a');
        d_delete_node.setMnemonic('d');
        d_clone_node.setMnemonic('c');
        d_isolate_node.setMnemonic('i');
        d_transpose.setMnemonic('t');
        d_symmetrize.setMnemonic('s');
        d_remove_outsiders.setMnemonic('r');

        d_add_node.addActionListener(act_change_image);
        d_clone_node.addActionListener(act_change_image);
        d_isolate_node.addActionListener(act_change_image);
        d_delete_node.addActionListener(act_change_image);
        d_transpose.addActionListener(act_change_image);
        d_symmetrize.addActionListener(act_change_image);
        d_remove_outsiders.addActionListener(act_change_image);

        d_clone_node.setEnabled(false);
        d_isolate_node.setEnabled(false);
        d_delete_node.setEnabled(false);

        gData.setMnemonic('t');
        gData.addSeparator();
        gData.add(d_add_node);
        gData.add(d_clone_node);
        gData.add(d_isolate_node);
        gData.add(d_delete_node);
        gData.addSeparator();
        gData.add(d_remove_outsiders);
        gData.addSeparator();
        gData.add(d_transpose);
        gData.add(d_symmetrize);

        // gata submeniurile

        gr_frame.setJMenuBar(g_mb);

        // Toolbar:
        gtools = new JToolBar();
        gtools.setFloatable(false);
        dim_tool = new Dimension(23, 23);
        Dimension dim_fields = new Dimension(35, 20);
        // gtools.setPreferredSize(new Dimension(400,25));
        ImageIcon gi_edge_color = Environment
                .getButtonImageIcon("EdgeColor.gif");
        ImageIcon gi_view_names = Environment
                .getButtonImageIcon("ViewNames.gif");
        ImageIcon gi_names_color = Environment
                .getButtonImageIcon("NamesColor.gif");
        ImageIcon gi_loyalty = Environment.getButtonImageIcon("Loyalty.gif");
        ImageIcon gi_image_width = Environment
                .getButtonImageIcon("ImageWidth.gif");
        ImageIcon gi_select_next = Environment
                .getButtonImageIcon("SelectNext.gif");
        ImageIcon gi_export_image = Environment
                .getButtonImageIcon("ExportImage.gif");
        ImageIcon gi_insert_in_output = Environment
                .getButtonImageIcon("InsertInOutput.gif");
        ImageIcon gi_allow_edge_selection = Environment
                .getButtonImageIcon("AllowEdgeSelection.gif");
        ImageIcon gi_show_connection_value = Environment
                .getButtonImageIcon("ShowConnectionValue.gif");

        ImageIcon gir_edge_color = Environment
                .getButtonImageIcon("rEdgeColor.gif");
        ImageIcon gir_view_names = Environment
                .getButtonImageIcon("rViewNames.gif");
        ImageIcon gir_names_color = Environment
                .getButtonImageIcon("rNamesColor.gif");
        ImageIcon gir_loyalty = Environment.getButtonImageIcon("rLoyalty.gif");
        ImageIcon gir_image_width = Environment
                .getButtonImageIcon("rImageWidth.gif");
        ImageIcon gir_select_next = Environment
                .getButtonImageIcon("rSelectNext.gif");
        ImageIcon gir_export_image = Environment
                .getButtonImageIcon("rExportImage.gif");
        ImageIcon gir_insert_in_output = Environment
                .getButtonImageIcon("rInsertInOutput.gif");
        ImageIcon gir_allow_edge_selection = Environment
                .getButtonImageIcon("rAllowEdgeSelection.gif");
        ImageIcon gir_show_connection_value = Environment
                .getButtonImageIcon("rShowConnectionValue.gif");

        // 2.1.3: modern duotone icons for the viewer toolbar (unless the
        // classic preference is set); rollovers become blue-colored
        if (!MainFrame.isClassicToolbarIcons())
            {
            gi_edge_color = ModernIcons.get(ModernIcons.EDGE_COLOR, 22);
            gi_view_names = ModernIcons.get(ModernIcons.VIEW_NAMES, 22);
            gi_names_color = ModernIcons.get(ModernIcons.NAMES_COLOR, 22);
            gi_loyalty = ModernIcons.get(ModernIcons.LOYALTY, 22);
            gi_image_width = ModernIcons.get(ModernIcons.IMAGE_WIDTH, 22);
            gi_select_next = ModernIcons.get(ModernIcons.SELECT_NEXT, 22);
            gi_export_image = ModernIcons.get(ModernIcons.EXPORT_IMAGE, 22);
            gi_insert_in_output = ModernIcons.get(ModernIcons.INSERT_IN_OUTPUT,
                    22);
            gi_allow_edge_selection = ModernIcons.get(
                    ModernIcons.ALLOW_EDGE_SELECTION, 22);
            gi_show_connection_value = ModernIcons.get(
                    ModernIcons.SHOW_CONNECTION_VALUE, 22);
            gir_edge_color = ModernIcons.get(ModernIcons.EDGE_COLOR, 22, true);
            gir_view_names = ModernIcons.get(ModernIcons.VIEW_NAMES, 22, true);
            gir_names_color = ModernIcons.get(ModernIcons.NAMES_COLOR, 22,
                    true);
            gir_loyalty = ModernIcons.get(ModernIcons.LOYALTY, 22, true);
            gir_image_width = ModernIcons.get(ModernIcons.IMAGE_WIDTH, 22,
                    true);
            gir_select_next = ModernIcons.get(ModernIcons.SELECT_NEXT, 22,
                    true);
            gir_export_image = ModernIcons.get(ModernIcons.EXPORT_IMAGE, 22,
                    true);
            gir_insert_in_output = ModernIcons.get(
                    ModernIcons.INSERT_IN_OUTPUT, 22, true);
            gir_allow_edge_selection = ModernIcons.get(
                    ModernIcons.ALLOW_EDGE_SELECTION, 22, true);
            gir_show_connection_value = ModernIcons.get(
                    ModernIcons.SHOW_CONNECTION_VALUE, 22, true);
            }

        gtool_edge_color = new JButton(gi_edge_color);
        gtool_view_names = new JButton(gi_view_names);
        gtool_names_color = new JButton(gi_names_color);
        gtool_loyalty = new JButton(gi_loyalty);
        gtool_image_width = new JButton(gi_image_width);
        gtool_circular_layout = new JButton(MainFrame
                .isClassicToolbarIcons() ? Environment
                        .getButtonImageIcon("CircularLayout.gif")
                : ModernIcons.get(ModernIcons.CIRCULAR_LAYOUT, 22));
        gtool_random_layout = new JButton(MainFrame
                .isClassicToolbarIcons() ? Environment
                        .getButtonImageIcon("RandomLayout.gif")
                : ModernIcons.get(ModernIcons.RANDOM_LAYOUT, 22));
        gtool_select_next = new JButton(gi_select_next);
        gtool_export_image = new JButton(gi_export_image);
        gtool_insert_in_output = new JButton(gi_insert_in_output);
        gtool_allow_edge_selection = new JButton(gi_allow_edge_selection);
        gtool_show_connection_value = new JButton(gi_show_connection_value);
        gtool_add_nodes = new JButton(MainFrame.isClassicToolbarIcons()
                ? Environment.getButtonImageIcon("AddNodes.gif")
                : ModernIcons.get(ModernIcons.ADD_NODE, 22));
        gtool_delete_nodes = new JButton(MainFrame.isClassicToolbarIcons()
                ? Environment.getButtonImageIcon("DeleteNodes.gif")
                : ModernIcons.get(ModernIcons.DELETE_NODE, 22));

        JLabel glabel_separator = new JLabel(Environment
                .getButtonImageIcon("Separation.gif"));
        glabel_edge_value = new JLabel("E:");
        gfield_separator = new JTextField(String
                .valueOf(my_area.getSeparator()));
        gfield_edge_value = new JTextField();
        gtool_separator = new DoubleButton();
        gtool_edge_value = new DoubleButton();
        glabel_x = new JLabel("x:");
        glabel_y = new JLabel("y:");
        gfield_x = new JTextField();
        gfield_y = new JTextField();
        gtool_change_x = new DoubleButton();
        gtool_change_y = new DoubleButton();

        gtool_edge_color.setRolloverIcon(gir_edge_color);
        gtool_view_names.setRolloverIcon(gir_view_names);
        gtool_names_color.setRolloverIcon(gir_names_color);
        gtool_loyalty.setRolloverIcon(gir_loyalty);
        gtool_image_width.setRolloverIcon(gir_image_width);
        gtool_circular_layout.setRolloverIcon(
                MainFrame.isClassicToolbarIcons() ? Environment
                        .getButtonImageIcon("rCircularLayout.gif")
                : ModernIcons.get(ModernIcons.CIRCULAR_LAYOUT, 22, true));
        gtool_random_layout.setRolloverIcon(MainFrame
                .isClassicToolbarIcons() ? Environment
                        .getButtonImageIcon("rRandomLayout.gif")
                : ModernIcons.get(ModernIcons.RANDOM_LAYOUT, 22, true));
        gtool_select_next.setRolloverIcon(gir_select_next);
        gtool_export_image.setRolloverIcon(gir_export_image);
        gtool_insert_in_output.setRolloverIcon(gir_insert_in_output);
        gtool_allow_edge_selection.setRolloverIcon(gir_allow_edge_selection);
        gtool_show_connection_value.setRolloverIcon(gir_show_connection_value);
        gtool_add_nodes.setRolloverIcon(MainFrame.isClassicToolbarIcons()
                ? Environment.getButtonImageIcon("rAddNodes.gif")
                : ModernIcons.get(ModernIcons.ADD_NODE, 22, true));
        gtool_delete_nodes.setRolloverIcon(MainFrame
                .isClassicToolbarIcons() ? Environment
                        .getButtonImageIcon("rDeleteNodes.gif")
                : ModernIcons.get(ModernIcons.DELETE_NODE, 22, true));

        gtool_edge_color.setBorder(null);
        gtool_view_names.setBorder(null);
        gtool_names_color.setBorder(null);
        gtool_loyalty.setBorder(null);
        gtool_image_width.setBorder(null);
        gtool_circular_layout.setBorder(null);
        gtool_random_layout.setBorder(null);
        gtool_select_next.setBorder(null);
        gtool_export_image.setBorder(null);
        gtool_insert_in_output.setBorder(null);
        gtool_allow_edge_selection.setBorder(null);
        gtool_show_connection_value.setBorder(null);
        gtool_add_nodes.setBorder(null);
        gtool_delete_nodes.setBorder(null);

        gtool_edge_color.setPreferredSize(dim_tool);
        gtool_view_names.setPreferredSize(dim_tool);
        gtool_names_color.setPreferredSize(dim_tool);
        gtool_export_image.setPreferredSize(dim_tool);
        gtool_insert_in_output.setPreferredSize(dim_tool);
        gtool_allow_edge_selection.setPreferredSize(dim_tool);
        gtool_show_connection_value.setPreferredSize(dim_tool);
        gtool_add_nodes.setPreferredSize(dim_tool);
        gtool_delete_nodes.setPreferredSize(dim_tool);
        gtool_circular_layout.setPreferredSize(dim_tool);
        gtool_random_layout.setPreferredSize(dim_tool);

        gtool_edge_color.setMaximumSize(dim_tool);
        gtool_view_names.setMaximumSize(dim_tool);
        gtool_names_color.setMaximumSize(dim_tool);
        gtool_export_image.setMaximumSize(dim_tool);
        gtool_insert_in_output.setMaximumSize(dim_tool);
        gtool_allow_edge_selection.setMaximumSize(dim_tool);
        gtool_show_connection_value.setMaximumSize(dim_tool);
        gtool_add_nodes.setMaximumSize(dim_tool);
        gtool_delete_nodes.setMaximumSize(dim_tool);
        gtool_circular_layout.setMaximumSize(dim_tool);
        gtool_random_layout.setMaximumSize(dim_tool);

        gtool_separator.setPreferredSize(dim_tool);
        gtool_edge_value.setPreferredSize(dim_tool);
        gtool_change_x.setPreferredSize(dim_tool);
        gtool_change_y.setPreferredSize(dim_tool);
        gtool_separator.setMinimumSize(dim_tool);
        gtool_edge_value.setMinimumSize(dim_tool);
        gtool_change_x.setMinimumSize(dim_tool);
        gtool_change_y.setMinimumSize(dim_tool);
        gtool_separator.setMaximumSize(dim_tool);
        gtool_edge_value.setMaximumSize(dim_tool);
        gtool_change_x.setMaximumSize(dim_tool);
        gtool_change_y.setMaximumSize(dim_tool);

        gtool_select_next.setMaximumSize(dim_tool);

        gfield_separator.setMinimumSize(dim_fields);
        gfield_edge_value.setMinimumSize(dim_fields);
        gfield_x.setMinimumSize(dim_fields);
        gfield_y.setMinimumSize(dim_fields);

        gfield_separator.setPreferredSize(dim_fields);
        gfield_edge_value.setPreferredSize(dim_fields);
        gfield_x.setPreferredSize(dim_fields);
        gfield_y.setPreferredSize(dim_fields);

        gfield_separator.setMaximumSize(dim_fields);
        gfield_edge_value.setMaximumSize(dim_fields);
        gfield_x.setMaximumSize(dim_fields);
        gfield_y.setMaximumSize(dim_fields);

        gtool_edge_color.setMinimumSize(dim_tool);

        gtool_edge_color.setToolTipText("Edge color");
        gtool_names_color.setToolTipText("Names color");

        if (my_area.getPrintNames())
            gtool_view_names.setToolTipText("Hide names Alt + V");
        else
            gtool_view_names.setToolTipText("Show names Alt + V");

        if (my_area.getColorFidelity())
            gtool_loyalty.setToolTipText("Remove value-loyalty Alt + L");
        else
            gtool_loyalty.setToolTipText("Turn value-loyalty on Alt + L");

        gtool_image_width.setToolTipText("Image width");
        gtool_circular_layout.setToolTipText("Circular Layout");
        gtool_random_layout.setToolTipText("Random Layout");
        gfield_separator.setToolTipText("Edge separation (px)");
        gfield_edge_value.setToolTipText("Edge value");
        gtool_separator.up_button
                .setToolTipText("Increase separation  Alt + D");
        gtool_edge_value.up_button
                .setToolTipText("Increase connection  Alt + A");
        gtool_edge_value.down_button
                .setToolTipText("Decrease connection  Alt + Z");
        gtool_separator.down_button
                .setToolTipText("Decrease separation  Alt + C");
        gfield_x.setToolTipText("Horizontal coordinate (px)");
        gfield_y.setToolTipText("Vertical coordinate (px)");
        gtool_change_x.up_button
                .setToolTipText("Increase horizontal coordinate  Alt + Rgt Ar");
        gtool_change_x.down_button
                .setToolTipText("Decrease horizontal coordinate  Alt + Lft Ar");
        gtool_change_y.up_button
                .setToolTipText("Increase vertical coordinate  Alt + Dwn Ar");
        gtool_change_y.down_button
                .setToolTipText("Decrease vertical coordinate  Alt + Up Ar");
        gtool_select_next.setToolTipText("Next node");
        gtool_export_image.setToolTipText("Export image");
        gtool_insert_in_output.setToolTipText("Insert in output");
        if (my_area.isEdgeValueVisible())
            gtool_show_connection_value.setToolTipText("Hide edge value");
        else
            gtool_show_connection_value.setToolTipText("Show edge value");

        gtool_add_nodes.setToolTipText("Add nodes");
        gtool_delete_nodes.setToolTipText("Delete node");

        gtool_view_names.setMnemonic('v');
        gtool_loyalty.setMnemonic('l');
        gtool_separator.up_button.setMnemonic('d');
        gtool_separator.down_button.setMnemonic('c');
        gtool_select_next.setMnemonic('w');

        gtool_edge_color.addActionListener(act_change_image);
        gtool_view_names.addActionListener(act_change_image);
        gtool_names_color.addActionListener(act_change_image);
        gtool_loyalty.addActionListener(act_change_image);
        gtool_image_width.addActionListener(act_change_image);
        gtool_circular_layout.addActionListener(act_change_image);
        gtool_random_layout.addActionListener(act_change_image);
        gfield_separator.addActionListener(act_change_image);
        gfield_edge_value.addActionListener(act_change_node);
        gtool_edge_value.up_button.addActionListener(act_change_node);
        gtool_edge_value.down_button.addActionListener(act_change_node);
        gtool_separator.up_button.addActionListener(act_change_image);
        gtool_separator.down_button.addActionListener(act_change_image);
        gtool_edge_value.up_button.addActionListener(act_change_image);
        gtool_edge_value.down_button.addActionListener(act_change_image);
        gfield_x.addActionListener(act_change_node);
        gfield_y.addActionListener(act_change_node);
        gtool_change_x.up_button.addActionListener(act_change_node);
        gtool_change_x.down_button.addActionListener(act_change_node);
        gtool_change_y.up_button.addActionListener(act_change_node);
        gtool_change_y.down_button.addActionListener(act_change_node);
        gtool_select_next.addActionListener(act_area_clicked);
        gtool_export_image.addActionListener(act_change_image);
        gtool_insert_in_output.addActionListener(act_change_image);
        gtool_allow_edge_selection.addActionListener(act_change_image);
        gtool_show_connection_value.addActionListener(act_change_image);
        gtool_add_nodes.addActionListener(act_change_image);
        gtool_delete_nodes.addActionListener(act_change_image);

        gfield_separator.addFocusListener(focus_change);
        gfield_edge_value.addFocusListener(focus_change);
        gfield_x.addFocusListener(focus_change);
        gfield_y.addFocusListener(focus_change);

        glabel_x.setEnabled(false);
        glabel_y.setEnabled(false);
        gfield_x.setEnabled(false);
        gfield_y.setEnabled(false);
        gtool_change_x.up_button.setEnabled(false);
        gtool_change_x.down_button.setEnabled(false);
        gtool_change_y.up_button.setEnabled(false);
        gtool_change_y.down_button.setEnabled(false);

        is = (my_area.getSelectedActor() >= 0);
        glabel_x.setEnabled(is);
        glabel_y.setEnabled(is);
        gfield_x.setEnabled(is);
        gfield_y.setEnabled(is);
        gtool_change_x.up_button.setEnabled(is);
        gtool_change_x.down_button.setEnabled(is);
        gtool_change_y.up_button.setEnabled(is);
        gtool_change_y.down_button.setEnabled(is);
        gtool_delete_nodes.setEnabled(is);

        glabel_edge_value.setEnabled(false);
        gfield_edge_value.setEnabled(false);
        gtool_edge_value.up_button.setEnabled(false);
        gtool_edge_value.down_button.setEnabled(false);

        is = (my_area.getSecondSelected() >= 0);
        glabel_edge_value.setEnabled(is);
        gfield_edge_value.setEnabled(is);
        gtool_edge_value.up_button.setEnabled(is);
        gtool_edge_value.down_button.setEnabled(is);

        if (my_area.getAllowES())
            gtool_allow_edge_selection
                    .setToolTipText("Disallow edge selection");
        else
            gtool_allow_edge_selection.setToolTipText("Allow edge selection");

        gtools.add(gtool_export_image);
        gtools.add(gtool_insert_in_output);
        gtools.addSeparator();
        gtools.add(gtool_image_width);
        gtools.addSeparator();
        gtools.add(gtool_circular_layout);
        gtools.add(gtool_random_layout);
        gtools.addSeparator();
        gtools.add(gtool_add_nodes);
        gtools.add(gtool_delete_nodes);
        gtools.addSeparator();
        gtools.add(gtool_names_color);
        gtools.add(gtool_view_names);
        gtools.addSeparator();
        gtools.add(gtool_select_next);
        gtools.addSeparator();
        gtools.add(glabel_separator);
        gtools.add(gfield_separator);
        gtools.add(gtool_separator);
        gtools.addSeparator();
        gtools.add(gtool_loyalty);
        gtools.add(gtool_edge_color);
        gtools.addSeparator();
        gtools.add(gtool_allow_edge_selection);
        gtools.add(gtool_show_connection_value);
        gtools.addSeparator();
        gtools.add(glabel_edge_value);
        gtools.add(gfield_edge_value);
        gtools.add(gtool_edge_value);
        gtools.addSeparator();
        gtools.add(glabel_x);
        gtools.add(gfield_x);
        gtools.add(gtool_change_x);
        gtools.add(glabel_y);
        gtools.add(gfield_y);
        gtools.add(gtool_change_y);

        // Enable/Disable blocks:
        if (my_area.getSelectedActor() >= 0) // if end-node is selected
            {
            enableFirst();
            if (my_area.getSecondSelected() >= 0) // if start-node is selected
                {
                enableSecond();
                } else
                {
                disableSecond();
                }
            } else
            {
            disableFirst();
            }

        // Add menus
        g_mb.add(gFile);
        g_mb.add(gImage);
        g_mb.add(gNodes);
        g_mb.add(e_edge);
        g_mb.add(gData);
        g_mb.add(gPreferences);
        g_mb.add(gHelp);

        tool_panel = new JPanel();
        tool_panel.setLayout(new BorderLayout());
        tool_panel.add(gtools, BorderLayout.WEST);
        gControlArea.add(tool_panel, BorderLayout.NORTH);
        // gata cu meniu si toolbar

        area_panel = new JPanel();
        FlowLayout area_layout = new FlowLayout();
        area_layout.setAlignment(FlowLayout.LEFT);
        area_panel.setLayout(area_layout);
        area_panel.setPreferredSize(dim_inner_frame);

        area_panel.add(my_area);
        // setAreaPanelWidth(a_width);
        scroll_graph = new JScrollPane(area_panel);

        status_bar = new JLabel(DEFAULT_STATUS);
        status_panel = new JPanel();
        // status_panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        status_panel.setMinimumSize(dim_tool);
        status_layout = new FlowLayout();
        status_layout.setAlignment(FlowLayout.LEFT);
        status_panel.setLayout(status_layout);
        status_panel.add(status_bar);

        file_chooser = new JFileChooser();
        // file_chooser.setCurrentDirectory(".");

        gControlArea.add(scroll_graph, BorderLayout.CENTER);
        gControlArea.add(status_panel, BorderLayout.SOUTH);

        // creating second toolbar:
        vertical_tools = new VerticalToolBar();
        /*
         * JPanel v_tool_panel=new JPanel(); v_tool_panel.setLayout(new
         * BorderLayout()); v_tool_panel.add(vertical_tools,
         * BorderLayout.NORTH);
         * v_tool_panel.setMaximumSize(vertical_tools.getThisDimension());
         * left_scroll = new JScrollPane(v_tool_panel);
         */

        left_scroll = new JScrollPane(vertical_tools);

        left_panel = new JPanel();
        left_panel.setLayout(new BorderLayout());
        // left_panel.setPreferredSize(new Dimension(120,100));

        border_button = new JButton();
        border_button.addActionListener(act_change_image);
        border_button.setPreferredSize(new Dimension(6, 40));
        // border_button.setBackground(new Color(46,138,141));
        border_button.setBackground(Color.white);
        border_button.setToolTipText("Click to hide left tools");
        left_panel.add(left_scroll, BorderLayout.WEST);
        left_panel.add(border_button, BorderLayout.EAST);

        gControlArea.add(tool_panel, BorderLayout.NORTH);
        gControlArea.add(left_panel, BorderLayout.WEST);

        gContent.add(gControlArea);

        gr_frame.pack();
        gr_frame.setVisible(true);

        gr_frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        gr_frame.addWindowListener(gr_window_listener);
        }
    }