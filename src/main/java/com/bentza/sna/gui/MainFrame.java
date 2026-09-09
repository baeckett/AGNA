package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.io.HTMLParser;
import com.bentza.sna.io.IOUtils;
import com.bentza.sna.io.ExcelExporter;
import com.bentza.sna.io.PajekExporter;
import com.bentza.sna.net.*;
import com.bentza.sna.Agna;
import com.bentza.sna.Environment;
import com.bentza.sna.AgnaThread;
import com.bentza.sna.gui.filter.*;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.*;
import javax.swing.KeyStroke;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

public class MainFrame //
    {

    private static JFrame my_frame;

    private static HelpDialog h_dialog;

    private static AboutBox about_box;

    private static String default_node_face_source;

    private JSplitPane m_sp; // panelul care imparte ecranul in 2

    public static Dimension max_dim, dim_tool; // max_dim = dimensiunea
                                                // ecranului

    private JPanel controlArea; // panel principal

    private JScrollPane scrollEditRight;

    private static JScrollPane scrollEditLeft;

    private ToolTipManager ttm;

    private JPanel left_panel, right_panel, status_panel;

    private static AgnaTable my_grid;

    private static AgnaTableHeader grid_header;

    private ExcelAdapter my_adapter;

    private static JMenu mFile, mOutput, mEdit, mData, mAnalysis, mView, mHelp,
            mPreferences, a_centrality, a_sociometrics, a_distance;

    private static JMenuItem f_new, f_open, f_save, f_simply_save, f_create,
            f_quit, o_open, o_save, o_simply_save, o_clear, e_cut, e_copy,
            e_paste, e_delete, e_select_all, d_title, d_add_nodes,
            d_delete_nodes, d_add_scalar, d_multiply_scalar, d_transpose,
            d_symmetrize, d_normalize, d_remove_out, d_renumber_nodes, d_merge,
            d_multiply_network, d_boolean_multiplication, a_basic,
            a_nodal_degree, a_indegree, a_outdegree, a_density, a_cohesion,
            a_emissions, a_receptions, a_determinations, a_sociostatus,
            a_eccentricity, a_diameter, a_geodesics, a_shortest_paths,
            a_all_shortest_paths, a_cliques, a_bavelas, a_closeness,
            a_fareness, a_betweenness, a_prestige, a_full_analysis, v_viewer,
            v_close, v_hide_output, v_view_output, p_working_directory,
            p_save_as_default, h_contents, h_about_agna;

    private static JMenuBar mb;

    private Container fContent;

    private static JToolBar tools;

    private ImageIcon ir_save_network, ir_network_viewer, i_open_network,
            i_network_viewer;

    private JButton tool_save_network, tool_open_network, tool_new_from_chain,
            tool_new_network, tool_open_output, tool_clear_output,
            tool_save_output, tool_viewer, tool_transpose, tool_symmetrize,
            tool_renumber_nodes, lowerleft_button, upleft_button;

    private static GrNet my_grafic;

    // 2.1.3: vectors selected in the Save As format dialog
    private java.util.Vector saved_pajek_vectors;

    private JTextField edit_cell;

    private DefaultCellEditor grid_editor;

    private String str;

    private File net_file;

    private static JLabel status_bar;

    private static String working_directory;

    private static boolean weight; // is weighted or not

    private FlowLayout status_layout;

    public static final String default_status = "Click grid cell to edit sociomatrix. For Network Viewer, press Ctrl + Z.";

    protected int m_xStart = -1; // used by output_edit's listeners

    protected int m_xFinish = -1;

    static AgnaTextPane output_edit; // editorul de text
    // final Grafic graph_zone ;

    public static AgnaTableModel grid_model;

    public static ImageIcon main_icon; // iconul programului

    public static FullNet my_full_net;

    public static final int col_width = 60;

    public static ProgressDialog progress_dialog;

    public static ImageStock image_stock;

    public static void showAboutBox(JFrame where)
        {
        about_box = null;
        if (about_box == null)
            {
            about_box = new AboutBox(where);
            about_box.showDialog();
            }
        about_box = null;
        }

    /*
     * // returns the node name as appearing on the table's vertical header //
     * returns null if wrong v_pos public static String
     * XXgetVerticalHeaderName(int v_pos) { return
     * grid_header.getAgnaVerticalHeader().getName(v_pos); }
     */

    // returns the node name as appearing on the table's horizontal header
    // returns null if wrong h_pos
    public static String getHorizontalHeaderName(int h_pos)
        {
        try
            {
            // return grid_header.getHorizontalHeader().
            return my_grid.getColumnModel().getColumn(h_pos).getHeaderValue()
                    .toString();
            } catch (Exception e)
            {
            return null;
            }
        }

    private class SelectionListener implements ListSelectionListener
        {
    private JTable grid;

    public SelectionListener(JTable tmp_grid)
            {
            grid = tmp_grid;
            }

    public void valueChanged(ListSelectionEvent e)
            {
            if (!grid_model.isReady())
                return;
            if (!e.getValueIsAdjusting())
                {
                ListSelectionModel selection_model = grid.getSelectionModel();
                ListSelectionModel column_selected = grid.getColumnModel()
                        .getSelectionModel();
                int row = selection_model.getAnchorSelectionIndex();
                int column = column_selected.getAnchorSelectionIndex();

                Network tmp_network = MainFrame.getCurrentNetwork();

                if (row != column)
                    {
                    MainFrame.setCurrentStatus("Selected cell: "
                            + tmp_network.getActor(row).getName() + " --> "
                            + tmp_network.getActor(column).getName());
                    } else
                    MainFrame.setCurrentStatus("Non-editable cell: "
                            + tmp_network.getActor(row).getName() + " --> "
                            + tmp_network.getActor(column).getName());

                }
            }
        };

    private CellEditorListener act_cell_listener = new CellEditorListener()
        {

            public void editingStopped(ChangeEvent e)
                {
                // MainFrame.setCurrentStatus("");
                MainFrame.setCurrentStatus(MainFrame.default_status);
                my_full_net.setChanged(true);
                }

            public void editingCanceled(ChangeEvent e)
                {
                // MainFrame.setCurrentStatus("");
                MainFrame.setCurrentStatus(MainFrame.default_status);
                }
        };

    // actionlistener pentru meniu
    private ActionListener act_menu = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {

                // create new netwwork
                if (e.getSource() == f_new || e.getSource() == tool_new_network)
                    {
                    doNewNetwork();
                    }

                // open network
                if (e.getSource() == f_open
                        || e.getSource() == tool_open_network)
                    {
                    doOpenNetwork();
                    }

                // save current network as
                if (e.getSource() == f_save)
                    {
                    doSaveNetwork(true);
                    }

                // save current network
                if (e.getSource() == f_simply_save
                        || e.getSource() == tool_save_network)
                    {
                    doSimplySaveNetwork();
                    }

                // create network from chain
                if (e.getSource() == f_create
                        || e.getSource() == tool_new_from_chain)
                    {
                    doCreateFromChain();
                    }

                // quits Agna
                if (e.getSource() == f_quit)
                    {
                    doQuit();
                    }

                // procedures from Output:

                // opens output file
                if (e.getSource() == o_open
                        || e.getSource() == tool_open_output)
                    {
                    doOpenOutput();
                    }

                // clears output file
                if (e.getSource() == o_clear
                        || e.getSource() == tool_clear_output)
                    {
                    doClearOutput();
                    }

                // saves output file as
                if (e.getSource() == o_save)
                    {
                    doSaveOutput(true);
                    }

                // simply saves output file
                if (e.getSource() == o_simply_save
                        || e.getSource() == tool_save_output)
                    {
                    doSimplySaveOutput();
                    }

                // procedures from Network:
                if (e.getSource() == d_title)
                    {
                    doChangeTitle();
                    }

                if (e.getSource() == d_add_nodes
                        || e.getSource() == upleft_button)
                    {
                    doAddNodes();
                    }

                if (e.getSource() == d_delete_nodes)
                    {
                    doDeleteNodes();
                    }

                if (e.getSource() == d_add_scalar)
                    {
                    doAddScalar();
                    }

                if (e.getSource() == d_multiply_scalar)
                    {
                    doMultiplyByScalar();
                    }

                if (e.getSource() == d_transpose
                        || e.getSource() == tool_transpose)
                    {
                    doTranspose();
                    }

                if (e.getSource() == d_symmetrize
                        || e.getSource() == tool_symmetrize)
                    {
                    doSymmetrize();
                    }

                if (e.getSource() == d_normalize)
                    {
                    doNormalize();
                    }

                if (e.getSource() == d_remove_out)
                    {
                    doRemoveOut();
                    }

                if (e.getSource() == d_renumber_nodes
                        || e.getSource() == tool_renumber_nodes)
                    {
                    doRenumberNodes();
                    }

                if (e.getSource() == d_merge)
                    {
                    doMerge();
                    }

                if (e.getSource() == d_multiply_network)
                    {
                    doMultiplyNetwork();
                    }

                // methods from ANALYSIS:
                if (e.getSource() == a_basic)
                    {
                    doBasic();
                    }

                if (e.getSource() == a_indegree)
                    {
                    doIndegree();
                    }

                if (e.getSource() == a_nodal_degree)
                    {
                    doNodalDegree();
                    }

                if (e.getSource() == a_outdegree)
                    {
                    doOutdegree();
                    }

                if (e.getSource() == a_density)
                    {
                    doDensity();
                    }

                if (e.getSource() == a_cohesion)
                    {
                    doCohesion();
                    }

                if (e.getSource() == a_eccentricity)
                    {
                    doEccentricity();
                    }

                if (e.getSource() == a_diameter)
                    {
                    doDiameter();
                    }

                if (e.getSource() == a_geodesics)
                    {
                    doGeodesics();
                    }

                if (e.getSource() == a_shortest_paths)
                    {
                    doShortestPaths();
                    }

                if (e.getSource() == a_all_shortest_paths)
                    {
                    doAllShortestPaths();
                    }

                if (e.getSource() == a_cliques)
                    {
                    doCliques();
                    }

                if (e.getSource() == a_closeness)
                    {
                    doCloseness();
                    }

                if (e.getSource() == a_fareness)
                    {
                    doFareness();
                    }

                if (e.getSource() == a_betweenness)
                    {
                    doBetweenness();
                    }

                if (e.getSource() == a_prestige)
                    {
                    doPrestige();
                    }

                if (e.getSource() == a_bavelas)
                    {
                    doBavelas();
                    }

                if (e.getSource() == a_full_analysis)
                    {
                    doFullAnalysis();
                    }

                if (e.getSource() == a_emissions)
                    {
                    doEmissions();
                    }

                if (e.getSource() == a_receptions)
                    {
                    doReceptions();
                    }

                if (e.getSource() == a_determinations)
                    {
                    doDeterminations();
                    }
                if (e.getSource() == a_sociostatus)
                    {
                    doSociostatus();
                    }

                // opens Network Viewer
                if (e.getSource() == v_viewer || e.getSource() == tool_viewer)
                    {
                    view_graph();
                    }

                // closes Network Viewer
                if (e.getSource() == v_close)
                    {
                    if (my_grafic.gr_frame != null)
                        {
                        my_grafic.getCurrentFrame().dispose();
                        my_grafic.gr_frame = null;
                        setDisabledCloseViewerMenu();
                        
                        }
                    }

                // sets working directory preference:
                if (e.getSource() == p_working_directory)
                    {
                    doWorkingDirectory();
                    }

                // Help Contents:
                if (e.getSource() == h_contents
                        || e.getSource() == lowerleft_button)
                    {
                    doHelpContents(my_frame);
                    }

                if (e.getSource() == h_about_agna)
                    {
                    // tring about_text = "<html>" +
                    // Environment.getApplicationFullName() +"<br>" +
                    // MainFrame.getApplicationCopyright() + "<br>Please visit
                    // Agna website for the latest version:<br><a href='" +
                    // MainFrame.getApplicationUrl() +"'>" +
                    // MainFrame.getApplicationUrl() + "</a>";
                    // JOptionPane.showMessageDialog(my_frame, about_text,
                    // "About Agna", JOptionPane.INFORMATION_MESSAGE,
                    // getMainIcon());
                    MainFrame.showAboutBox(my_frame);
                    }

                }
        };

    private void doWorkingDirectory() // mere de-ampulea
        {

        File dir;
        String tmp_str;
        // setting name of directory:
        tmp_str = MainFrame.getWorkingDirectory();
        tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                "Please enter new working directory path:",
                "Working Directory", JOptionPane.QUESTION_MESSAGE, null, null,
                tmp_str);
        my_frame.repaint();
        if (tmp_str != null)
            {
            dir = new File(tmp_str);
            if (dir.exists() && dir.isDirectory())
                {
                try
                    {
                    setWorkingDirectory(dir.getCanonicalPath());
                    } catch (Exception e)
                    {
                    JOptionPane.showMessageDialog(my_frame,
                            "Failed to set working directory.",
                            "Agna 2 Error Message", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        /*
         * JFileChooser chooser = new JFileChooser() { public boolean
         * accept(File f) { if (f.isDirectory()) return true; return false; } };
         * chooser.setDialogTitle("Set Working Directory");
         * chooser.setCurrentDirectory(dir);
         * chooser.setMultiSelectionEnabled(false);
         * chooser.setApproveButtonToolTipText("Chose directory and click
         * here"); chooser.setFileHidingEnabled(true);
         * chooser.setAcceptAllFileFilterUsed(false);
         * chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         * //chooser.setSelectedFile(dir); //chooser.addChoosableFileFilter(new
         * TabTextFilesFilter()); //chooser.addChoosableFileFilter(new
         * AgnaFilesFilter()); int approve = chooser.showDialog(my_frame, "Ok");
         * if (approve == 0) { dir = chooser.getSelectedFile(); if
         * (!dir.exists()) { try {
         * setWorkingDirectory(dir.getParentFile().getCanonicalPath()); }
         * catch(Exception e) { JOptionPane.showMessageDialog(my_frame, "Failed
         * to set working directory.", "Agna 2 Error Message",
         * JOptionPane.ERROR_MESSAGE); } } else if (dir.isDirectory() &&
         * dir.exists()) { try { setWorkingDirectory(dir.getCanonicalPath()); }
         * catch(Exception e) { JOptionPane.showMessageDialog(my_frame, "Failed
         * to set working directory.", "Agna 2 Error Message",
         * JOptionPane.ERROR_MESSAGE); } } }
         */
        }

    private void doNewNetwork()
        {
        int nnodes = getCurrentNetwork().getSize();
        grid_model.setReady(false);
        String tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                "Please enter number of nodes:", "Network size",
                JOptionPane.QUESTION_MESSAGE, null, null, String
                        .valueOf(nnodes));
        my_frame.repaint();
        if (tmp_str == null)
            return;
        try
            {
            nnodes = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            try
                {
                nnodes = (int) Float.parseFloat(tmp_str);
                } catch (Exception e1)
                {
                JOptionPane.showMessageDialog(my_frame, "Error reading value.",
                        "Agna 2 Error Message", JOptionPane.ERROR_MESSAGE);
                return;
                }
            }

        if (nnodes < 2)
            nnodes = 2;
        int confirm = 1;
        while (nnodes > 300 && confirm == 1)
            {
            confirm = JOptionPane
                    .showOptionDialog(
                            my_frame,
                            "Processing data from such a large network may result in very slow operations.\nAre you sure you want to continue?",
                            "Agna 2 Message", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, null, null);
            if (confirm != 0)
                {
                tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                        "Enter number of nodes:", "Network size",
                        JOptionPane.QUESTION_MESSAGE, null, null, String
                                .valueOf(nnodes));
                my_frame.repaint();
                if (tmp_str == null)
                    return;
                try
                    {
                    nnodes = Integer.parseInt(tmp_str);
                    } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                }
            }
        saveChangedNetworks(true);
        final int t_nnodes = nnodes;

        FullNet new_full_net = new FullNet();
        new_full_net.createDefaultNetwork(t_nnodes);

        if (my_grafic != null)
            {
            if (my_grafic.getCurrentFrame() != null)
                my_grafic.getCurrentFrame().dispose();
            my_grafic = null;
            }

        my_full_net.cleaning();
        my_full_net = new_full_net;
        setTableCellEditor();
        updateMatrix();

        if (my_full_net.getNetworkFileName().equals(""))
            my_frame.setTitle(MainFrame.my_full_net.getNetwork().getName()
                    + " - " + Environment.getApplicationFullName());
        else
            my_frame.setTitle(IOUtils.getNameWithoutExtension(my_full_net
                    .getNetworkFileName())
                    + " - " + Environment.getApplicationFullName());

        my_full_net.setChanged(false);
        my_frame.validate();
        my_frame.repaint();

        }

    public void saveNetwork(FullNet tmp_full_net, String tmp_file_name)
        {
        String file_name = tmp_file_name;
        String filestr = IOUtils.getExtension(file_name);
        String writestr = null;

        if (filestr == null)
            {
            filestr = "agn";
            file_name = file_name + ".agn";
            } else if (filestr.equals("net"))
            {
            // pajek file
            PajekExporter pajek_exporter = new PajekExporter();
            writestr = pajek_exporter.getPajekNetwork(tmp_full_net);
            // 2.1.3: user-selectable per-node measure vectors (*Vector blocks);
            // the Save As options dialog supplies them when available
            java.util.Vector selected_vectors = saved_pajek_vectors;
            saved_pajek_vectors = null;
            if (selected_vectors == null)
                {
                selected_vectors = askPajekVectors();
                }
            if (selected_vectors != null)
                {
                writestr += new AgnaLib().getPajekVectors(tmp_full_net
                        .getNetwork(), selected_vectors);
                }
            } else if (filestr.equals("txt") || filestr.equals("text")
                || filestr.equals("dat"))
            {
            // tab-separated values
            writestr = tmp_full_net.getPlainTextNetwork(true);
            } else if (filestr.equals("csv"))
            {
            // comma-separated values
            writestr = tmp_full_net.getPlainTextNetwork(false);
            } else if (filestr.equals("xls"))
            {
            // Excel
            ExcelExporter excel_exporter = new ExcelExporter();
            String errors = excel_exporter.saveExcelNetwork(tmp_full_net,
                    file_name);
            return;
            } else
            {
            // agn file
            writestr = tmp_full_net.getAgna2TextNetwork();
            }

        try
            {
            // Thread.sleep(500);
            progress_dialog.setPercent(60);
            } catch (Exception e1) {
      AgnaLog.warn("suppressed exception", e1);
      }
        try
            {
            try (Writer writer = IOUtils.writer(new File(file_name)))
                {
                JTextPane tmp_pane = new JTextPane();
                tmp_pane.setText(writestr);
                tmp_pane.write(writer);
                tmp_full_net.setNetworkFileName(file_name);
                tmp_full_net.setChanged(false);
                }
            } catch (Exception e)
            {
            AgnaLog.warn("saveNetwork failed for " + file_name + ": " + e);
            }
        }

    // saves when network has a name
    private void doSimplySaveNetwork()
        {
        if (my_full_net.getNetworkFileName().length() > 0
                && IOUtils.getExtension(my_full_net.getNetworkFileName())
                        .equals("agn"))
            {
            if (checkAllValues())
                updateNetwork();
            else
                return;
            saveNetwork(my_full_net, my_full_net.getNetworkFileName());
            } else
            doSaveNetwork(true);
        }

    private void doSaveNetwork(boolean save_in_thread)
        {
        if (checkAllValues())
            {
            updateNetwork();
            } else
            return;

        grid_model.setReady(false);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Current Network");
        File file = null;

        // setting name of file:
        if (my_full_net.getNetworkFileName().equals(""))
            {
            try
                {
                file = new File(MainFrame.getWorkingDirectory()
                        + my_full_net.getNetwork().getName() + ".agn");
                } catch (Exception e1)
                {
                file = new File(my_full_net.getNetwork().getName() + ".agn");
                }
            } else
            file = new File(my_full_net.getNetworkFileName());

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setSelectedFile(file);
        chooser.setCurrentDirectory(file);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Type file name and click here");

        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setDialogTitle("Save Network As...");
        // 2.1.3: the format is chosen in the options dialog AFTER the name;
        // the chooser itself takes any name and Agna appends the extension
        if (chooser.showSaveDialog(my_frame) != JFileChooser.APPROVE_OPTION)
            {
            grid_model.setReady(true);
            my_frame.validate();
            my_frame.repaint();
            return;
            }

        String filename = null;
        file = chooser.getSelectedFile();
        my_frame.repaint();
        try
            {
            filename = file.getCanonicalPath();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Error finding path specified.", "Output Error",
                    JOptionPane.ERROR_MESSAGE);
            grid_model.setReady(true);
            my_frame.validate();
            my_frame.repaint();
            return;
            }

        // 2.1.3: reliable, platform-independent format choice: one options
        // dialog selects the format (and, for Pajek, the vectors); the
        // extension is appended here so the saved file always matches the
        // visible choice
        int chosen_format = askExportFormatDialog();
        if (chosen_format < 0)
            {
            grid_model.setReady(true);
            my_frame.validate();
            my_frame.repaint();
            return;
            }
        final String[] format_extensions = { "agn", "txt", "csv", "net", "xls" };
        filename = IOUtils.setExtension(filename,
                format_extensions[chosen_format]);

        // warning if file already exists - checked on the FINAL name, after
        // the chosen format's extension was applied (2.1.3 fix)
        java.io.File final_file = new java.io.File(filename);
        if (final_file.exists())
            {
            int confirm = JOptionPane
                    .showOptionDialog(
                            my_frame,
                            "File "
                                    + final_file.getName()
                                    + " already exists.\nDo you want to replace existing file?",
                            "Agna Output Message", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, null, null);
            if (confirm != 0)
                {
                grid_model.setReady(true);
                my_frame.validate();
                my_frame.repaint();
                return;
                }
            }

        final String thread_filename = filename;

        AgnaThread runner = new AgnaThread()
            {
                public void run()
                    {
                    this.decorate("Writing file. Please wait...");
                    progress_dialog.setPercent(10);
                    this.nap(); // allow cancel button to be pressed

                    if (progress_dialog.getStop())
                        {
                        this.undecorate();
                        grid_model.setReady(true);
                        my_frame.validate();
                        my_frame.repaint();
                        return; // exit without reading network
                        }
                    progress_dialog.setPercent(40);

                    saveNetwork(my_full_net, thread_filename);

                    this.nap(); // allow cancel button to be pressed
                    if (progress_dialog.getStop())
                        {
                        this.undecorate();
                        grid_model.setReady(true);
                        my_frame.validate();
                        my_frame.repaint();
                        return; // exit without saving network
                        }
                    progress_dialog.setPercent(85);

                    if (my_full_net.getNetworkFileName().equals(""))
                        my_frame.setTitle(MainFrame.my_full_net.getNetwork()
                                .getName()
                                + " - " + Environment.getApplicationFullName());
                    else
                        my_frame.setTitle(IOUtils
                                .getNameWithoutExtension(my_full_net
                                        .getNetworkFileName())
                                + " - " + Environment.getApplicationFullName());

                    grid_model.setReady(true);
                    my_frame.validate();
                    my_frame.repaint();
                    this.finish();
                    }
            }; // end of thread

        if (save_in_thread)
            runner.go();
        else
            { // the same as in runner.go(), but no thread
            saveNetwork(my_full_net, thread_filename);

            if (my_full_net.getNetworkFileName().equals(""))
                my_frame.setTitle(MainFrame.my_full_net.getNetwork().getName()
                        + " - " + Environment.getApplicationFullName());
            else
                my_frame.setTitle(IOUtils.getNameWithoutExtension(my_full_net
                        .getNetworkFileName())
                        + " - " + Environment.getApplicationFullName());

            my_frame.validate();
            my_frame.repaint();
            grid_model.setReady(true);
            }
        }

    public void doSimplyOpenNetwork(File tmp_file)
        {
        final File t_file = tmp_file;
        final FullNet t_new_full_net = new FullNet();

        AgnaThread runner = new AgnaThread()
            {
                public void run()
                    {
                    this.decorate("Reading file. Please wait...");
                    progress_dialog.setPercent(10);
                    my_frame.repaint();
        try
                        {
                        JTextPane tmp_pane = null;
                        try (Reader reader = IOUtils.reader(t_file))
                            {
                            tmp_pane = new JTextPane();
                            tmp_pane.read(reader, null);
                            }

                        this.nap(); // allow cancel button to be pressed
                        if (progress_dialog.getStop())
                            {
                            grid_model.setReady(true);
                            my_frame.validate();
                            my_frame.repaint();
                            this.undecorate();
                            return; // exit without reading network
                            }
                        progress_dialog.setPercent(40);

                        t_new_full_net.readNetwork(tmp_pane == null ? ""
                                : tmp_pane.getText(), IOUtils.getExtension(t_file
                                .getName()));
                        tmp_pane = null;
                        } catch (Exception e)
                        {
                        AgnaLog.warn("Failed reading "
                                + t_file.getAbsolutePath() + ": " + e);
                        }

                    this.nap(); // allow cancel button to be pressed
                    if (progress_dialog.getStop())
                        {
                        grid_model.setReady(true);
                        my_frame.validate();
                        my_frame.repaint();
                        this.undecorate();
                        return; // exit without reading network
                        }
                    progress_dialog.setPercent(60);

                    if (t_new_full_net.getNetwork() != null)
                        {
                        if (my_grafic != null)
                            {
                            if (my_grafic.getCurrentFrame() != null)
                                my_grafic.getCurrentFrame().dispose();
                            my_grafic = null;
                            }
                        try
                            {
                            t_new_full_net.setNetworkFileName(t_file
                                    .getCanonicalPath());
                            } catch (Exception e1)
                            {
                            t_new_full_net.setNetworkFileName("");
                            }
                        t_new_full_net.setChanged(false);
                        my_full_net.cleaning();
                        my_full_net = t_new_full_net;
                        updateMatrix();
                        progress_dialog.setPercent(90);
                        my_full_net.setChanged(false);
                        grid_model.setReady(true);
                        if (my_full_net.getNetworkFileName().equals(""))
                            my_frame.setTitle(MainFrame.my_full_net
                                    .getNetwork().getName()
                                    + " - "
                                    + Environment.getApplicationFullName());
                        else
                            my_frame.setTitle(IOUtils
                                    .getNameWithoutExtension(my_full_net
                                            .getNetworkFileName())
                                    + " - "
                                    + Environment.getApplicationFullName());

                        // to avoid error generated by my_frame.repaint() :
                        byte contor = 0;
                        while (!grid_model.isReady() && contor < 10)
                            {
                            this.nap();
                            contor++;
                            }
                        my_frame.repaint();
                        } else
                        {
                        // error reading:
                        grid_model.setReady(true);
                        my_frame.validate();
                        my_frame.repaint();
                        }

                    this.finish();
                    }
            }; // end of thread
        runner.go();

        }

    private void doOpenNetwork()
        {
        File file = null;
        if (my_full_net.getNetworkFileName().length() > 0)
            {
            file = new File(my_full_net.getNetworkFileName());
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
        chooser.setDialogTitle("Open Network File");
        // chooser.setSelectedFile(file);
        chooser.setCurrentDirectory(file);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Select file and click here");
        chooser.addChoosableFileFilter(new CommaTextFilesFilter());
        chooser.addChoosableFileFilter(new TabTextFilesFilter());
        chooser.addChoosableFileFilter(new AgnaFilesFilter());

        int return_val = chooser.showOpenDialog(my_frame);
        my_frame.repaint();

        if (return_val == 1)
            {
            return;
            }

        stopEditingTable();
        grid_model.setReady(false);

        file = chooser.getSelectedFile();

        saveChangedNetworks(true);

        doSimplyOpenNetwork(file);

        /*
         * str = ""; try { FileInputStream inputStream = new
         * FileInputStream(file.getCanonicalPath()); int b = 0; while(b != -1) {
         * b = inputStream.read(); str += (char)b; } inputStream.close(); }
         * catch(Exception esc) { }
         */
        }// end of method

    // creates a new network from a chain-file:
    private void doCreateFromChain()
        {
        File file = null;
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

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Chain File");
        // chooser.setSelectedFile(file);
        chooser.setCurrentDirectory(file);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Select file and click here");
        chooser.addChoosableFileFilter(new TabTextFilesFilter());

        int return_val = chooser.showOpenDialog(my_frame);

        if (return_val == JFileChooser.CANCEL_OPTION)
            return;

        stopEditingTable();
        grid_model.setReady(false);
        my_frame.repaint();
        file = chooser.getSelectedFile();

        saveChangedNetworks(true);
        final File t_file = file;
        final FullNet t_new_full_net = new FullNet();
        MainFrame.setCurrentStatus("Reading chain file. Please wait...");
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        JTextPane tmp_pane = new JTextPane();
        try (Reader reader = IOUtils.reader(t_file))
            {
            tmp_pane.read(reader, null);
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Errors encountered on reading!", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            MainFrame.setCurrentStatus(MainFrame.default_status);
            my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        t_new_full_net.readNetworkFromChain(tmp_pane.getText(), IOUtils
                .getExtension(t_file.getName()));
        tmp_pane = null;
        if (t_new_full_net.getNetwork() != null)
            {
            // JOptionPane.showMessageDialog(null, "Serus!", "Test",
            // JOptionPane.INFORMATION_MESSAGE);
            t_new_full_net.setChanged(false);
            if (my_grafic != null)
                {
                if (my_grafic.getCurrentFrame() != null)
                    my_grafic.getCurrentFrame().dispose();
                my_grafic = null;
                }
            my_full_net.cleaning();
            my_full_net = t_new_full_net;
            my_full_net.getNetwork().setName(
                    IOUtils.getNameWithoutExtension(t_file.getName()));

            // generating report:
            String tmp_str = new String("");
            AgnaLib agna_lib = new AgnaLib();
            tmp_str = agna_lib.getAgnaSignature()
                    + agna_lib.outOpenChainSummary(my_full_net.getNetwork());
            output_edit.appendBlock(tmp_str);

            updateMatrix(); // diagonal is erased here
            grid_model.setReady(true);
            my_frame.setTitle(my_full_net.getNetwork().getName() + " - "
                    + Environment.getApplicationFullName());
            }
        MainFrame.setCurrentStatus(MainFrame.default_status);
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        my_frame.validate();
        my_frame.repaint();
        }

    public static void doHelpContents(JFrame where)
        {
        if (where == MainFrame.getCurrentFrame())
            stopEditingTable();
        if (h_dialog != null)
            {
            if (h_dialog.hasFrame())
                h_dialog.closeDialog();
            h_dialog = null;
            }
        h_dialog = new HelpDialog(where);
        h_dialog.showDialog();
        }

    private void doQuit()
        {
        if (show_dialog() == 0)
            {
            my_frame.dispose();
            System.out.println("");
            System.out.println("    Thank you for using Agna.");
            System.out
                    .println("    For any comments, questions or suggestions please e-mail:");
            System.out.println("    " + Environment.getApplicationEmail());
            System.exit(0);
            }
        }

    /*
     * private void doOpenOutputOLD() { File file=new File("."); JFileChooser
     * chooser=new JFileChooser(); chooser.setDialogTitle("Open Existing Output
     * File"); //chooser.setSelectedFile(file);
     * chooser.setCurrentDirectory(file);
     * chooser.setMultiSelectionEnabled(false);
     * chooser.setApproveButtonToolTipText("Select file and click here");
     * chooser.addChoosableFileFilter(new OutputFilesFilter()); int return_val =
     * chooser.showOpenDialog(null); if (return_val ==
     * JFileChooser.CANCEL_OPTION) return; file=chooser.getSelectedFile(); str =
     * ""; try { FileInputStream inputStream = new
     * FileInputStream(file.getCanonicalPath()); int b = 0; while(b != -1) { b =
     * inputStream.read(); str += (char)b; } inputStream.close();
     * output_edit.setCaretPosition(output_edit.getText().length());
     * output_edit.replaceSelection(str); } catch(Exception esc) { } }
     */

    private void doOpenOutput() // new version
        {
        // setting name of file:
        File file = null;
        if (output_edit.getFileName().length() < 1)
            {
            try
                {
                file = new File(MainFrame.getWorkingDirectory());
                } catch (Exception e1)
                {
                file = new File(".");
                }
            } else
            {
            file = new File(output_edit.getFileName());
            }

        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Existing Output File");
        // chooser.setSelectedFile(file);
        chooser.setCurrentDirectory(file);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Select file and click here");
        chooser.addChoosableFileFilter(new PlainTextFilesFilter());
        chooser.addChoosableFileFilter(new HTMLFilesFilter());

        int return_val = chooser.showOpenDialog(my_frame);
        if (return_val == JFileChooser.CANCEL_OPTION)
            return;
        file = null;
        my_frame.repaint();

        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MainFrame.setCurrentStatus("Reading file. Please wait...");
        my_frame.validate();
        my_frame.repaint();
        file = chooser.getSelectedFile();
        String extension = IOUtils.getExtension(file.getName());
        FileInputStream in = null;
        JTextPane tmp_pane = null;
        HTMLEditorKit kit = null;
        // AgnaEditorKit kit = null;
        HTMLDocument doc = null;

        if (extension.equals(null) || extension.equals("txt")
                || extension.equals("text") || extension.equals("")
                || extension.equals("out") || extension.equals("java"))
            {
            // open file as TEXT
            try
                {
                if (!output_edit.getContentType().equals("text/plain"))
                    {
                    output_edit.setContentType("text");
                    }
                output_edit.setText("");
                try (Reader reader = IOUtils.reader(file))
                    {
                    tmp_pane = new JTextPane();
                    tmp_pane.read(reader, null);
                    }
                try
                    {
                    output_edit.setFileName(file.getCanonicalPath());
                    output_edit.setChanged(false);
                    doAppendParagraphToOutput(tmp_pane.getText());
                    } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                tmp_pane = null;
                } catch (IOException e)
                {
                }
            }

        kit = null;
        doc = null;
        in = null;
        if (extension.equals("htm") || extension.equals("html"))
            {
            // open file as HTML
            try
                {
                in = new FileInputStream(file.getCanonicalPath());
                if (!output_edit.getContentType().equals("text/plain"))
                    {
                    output_edit.setContentType("text");
                    }
                output_edit.setText("");
                output_edit.setContentType("text/html");
                // output_edit.setText("<br>");
                kit = ((HTMLEditorKit) output_edit.getEditorKit());
                doc = ((HTMLDocument) output_edit.getDocument());
                kit.read(in, doc, 0);
                output_edit.setFileName(file.getCanonicalPath());
                output_edit.setChanged(false);
                } catch (IOException e)
                {
                } catch (BadLocationException e)
                {
                } finally
                {
                try
                    {
                    in.close();
                    } catch (Throwable t)
                    {
                    }
                }
            }

        RTFEditorKit rtfkit = null;
        DefaultStyledDocument rtfdoc = null;
        in = null;
        if (extension.equals("rtf"))
            {
            // open file as RTF
            try
                {
                in = new FileInputStream(file.getCanonicalPath());
                if (!output_edit.getContentType().equals("text/plain"))
                    {
                    output_edit.setContentType("text");
                    }
                output_edit.setText("");
                output_edit.setContentType("text/rtf");
                rtfkit = ((RTFEditorKit) output_edit.getEditorKit());
                rtfdoc = ((DefaultStyledDocument) output_edit.getDocument());
                rtfkit.read(in, rtfdoc, 0);
                output_edit.setChanged(false);
                } catch (IOException e)
                {
                } catch (BadLocationException e)
                {
                } finally
                {
                try
                    {
                    in.close();
                    } catch (Throwable t)
                    {
                    }
                }
            }
        MainFrame.setCurrentStatus(MainFrame.default_status);
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        my_frame.validate();
        my_frame.repaint();
        

        // opens html file:
        // File file = new File("htmltest.html");
        // try
        // {
        // URL fileURL = file.toURL();
        // output_edit.setPage(fileURL);
        // } catch(Exception e) {}

        }

    // saves when output has a name
    public static void doSimplySaveOutput()
        {
        if (output_edit.getFileName().length() > 0)
            {
            saveOutput(output_edit, output_edit.getFileName());
            } else
            doSaveOutput(true);
        }

    private static void doSaveOutput(boolean save_in_thread)
        {
        JFileChooser chooser = new JFileChooser();

        File file = null;
        // setting name of output file:
        if (output_edit.getFileName().equals(""))
            {
            try
                {
                file = new File(MainFrame.getWorkingDirectory()
                        + my_full_net.getNetwork().getName() + "_output.html");
                } catch (Exception e1)
                {
                file = new File(my_full_net.getNetwork().getName()
                        + "_output.html");
                }
            } else
            {
            file = new File(output_edit.getFileName());
            }

        chooser.setDialogTitle("Save Current Output");
        chooser.setCurrentDirectory(file);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setSelectedFile(file);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Type file name and click here");
        chooser.addChoosableFileFilter(new PlainTextFilesFilter());
        chooser.addChoosableFileFilter(new HTMLFilesFilter());

        javax.swing.filechooser.FileFilter[] filters_list = chooser
                .getChoosableFileFilters();

        // setting the appropriate FileFilter according to file extension:
        if (filters_list.length >= 2)
            try
                {
                final String tmp_extension = IOUtils.getExtension(file
                        .getName());

                if (tmp_extension == null)
                    chooser.setFileFilter(filters_list[1]); // html filter
                else if (tmp_extension.equals(""))
                    chooser.setFileFilter(filters_list[1]); // html filter
                else if (tmp_extension.equals("htm")
                        || tmp_extension.equals("html"))
                    chooser.setFileFilter(filters_list[1]); // html filter
                else if (tmp_extension.equals("txt")
                        || tmp_extension.equals("text")
                        || tmp_extension.equals("dat"))
                    chooser.setFileFilter(filters_list[0]); // text filter
                } catch (SecurityException e10)
                {
                }

        if (chooser.showSaveDialog(my_frame) != JFileChooser.APPROVE_OPTION)
            return;
        my_frame.repaint();
        String filestr = null;
        file = chooser.getSelectedFile();
        try
            {
            filestr = file.getCanonicalPath();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(my_frame, "Error locating file!",
                    "Output Error", JOptionPane.ERROR_MESSAGE);
            return;
            }

        // html files was selected:
        if (chooser.getFileFilter() instanceof HTMLFilesFilter)
            {
            file = new File(IOUtils.addExtension(filestr, "html"));
            }
        // text files was selected:
        else if (chooser.getFileFilter() instanceof PlainTextFilesFilter)
            {
            file = new File(IOUtils.addExtension(filestr, "txt"));
            }

        // warning if file already exists
        if (file.exists())
            {
            int confirm = JOptionPane
                    .showOptionDialog(
                            my_frame,
                            "File "
                                    + file.getName()
                                    + " already exists.\nDo you want to replace existing file?",
                            "Agna Output Message", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, null, null);
            if (confirm != 0)
                {
                return;
                }
            }

        try
            {
            filestr = file.getCanonicalPath();
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(my_frame, "Error writing file!",
                    "Output Error", JOptionPane.ERROR_MESSAGE);
            }

        final String thread_filestr = filestr;

        if (!saveOutput(output_edit, thread_filestr))
            {
            JOptionPane.showMessageDialog(my_frame, "Error writing file!",
                    "Output Error", JOptionPane.ERROR_MESSAGE);
            }
        

        }

    private void saveChangedNetworks(boolean save_in_thread)
        {
        if (my_grid.isEditing())
            this.stopEditingTable();
        if (my_full_net.getChanged())
            {
            int confirm = JOptionPane.showOptionDialog(my_frame,
                    "Do you want to save changes to current network?",
                    "Agna 2 Message", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (confirm != 0)
                {
                } else if (my_full_net.getNetworkFileName().length() > 0)
                {
                if (checkAllValues())
                    updateNetwork();
                else
                    {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "One or more bad values encountered in grid.\nFile may not have been saved properly!",
                                    "Error parsing value",
                                    JOptionPane.ERROR_MESSAGE);
                    }
                saveNetwork(my_full_net, my_full_net.getNetworkFileName());
                } else
                {
                doSaveNetwork(save_in_thread);
                }
            }
        }

    public void saveChangedFiles(boolean save_in_thread)
        {
        saveChangedNetworks(save_in_thread);
        saveChangedOutput(save_in_thread);
        }

    private void saveChangedOutput(boolean save_in_thread)
        {
        if (output_edit.getChanged())
            {
            int confirm = JOptionPane.showOptionDialog(my_frame,
                    "Do you want to save changes to current output?",
                    "Agna 2 Output Message", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (confirm != 0)
                {
                } else if (output_edit.getFileName().length() > 0)
                {
                saveOutput(output_edit, output_edit.getFileName());
                } else
                doSaveOutput(save_in_thread);
            }
        }

    // saves output when path is known:
    public static boolean saveOutput(AgnaTextPane o_edit, String file_name)
        {
my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MainFrame.setCurrentStatus("Writing file. Please wait...");
        EditorKit kit = null;
        Document doc = null;

        String filestr = IOUtils.getExtension(file_name);

        if (filestr.equals("rtf"))
            {
            kit = o_edit.getEditorKitForContentType("text/rtf");
            doc = o_edit.getStyledDocument();
            } else if (filestr.equals("htm") || filestr.equals("html"))
            {
            if (!o_edit.getContentType().equals("text/html"))
                {
                String tmptext = o_edit.getText();
                try
                    {
                    o_edit.setContentType("text/html");
                    kit = ((HTMLEditorKit) output_edit.getEditorKit());
                    doc = ((HTMLDocument) output_edit.getDocument());
                    tmptext = HTMLParser.parseTextToHTML(tmptext);
                    // JOptionPane.showMessageDialog(null, tmptext, "Test",
                    // JOptionPane.INFORMATION_MESSAGE);
                    // o_edit.appendParagraph(tmptext);
                    o_edit.setText(tmptext);
                    } catch (Exception e)
                    {
                    MainFrame.setCurrentStatus(MainFrame.default_status);
                    my_frame.setCursor(Cursor
                            .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    my_frame.validate();
                    my_frame.repaint();
                    return false;
                    }
                tmptext = null;
                } else
                {
                kit = o_edit.getEditorKitForContentType("text/html");
                doc = (HTMLDocument) o_edit.getDocument();
                }

            } else
            {
            // 2.1.3: the two branches were identical; collapsed
            kit = o_edit.getEditorKitForContentType("text/plain");
            doc = o_edit.getDocument();
            }

        try (OutputStream fileout = new FileOutputStream(file_name))
            {
            kit.write(fileout, doc, 0, doc.getLength());
            fileout.flush();
            o_edit.setChanged(false);
            // if (filestr.equals("htm") || filestr.equals("html"))
            o_edit.setFileName(file_name);
            MainFrame.setCurrentStatus(MainFrame.default_status);
            my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            my_frame.validate();
            my_frame.repaint();
            return true;
            } catch (Exception e)
            {
            MainFrame.setCurrentStatus(MainFrame.default_status);
            my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            my_frame.validate();
            my_frame.repaint();
            return false;
            }

        // inserts a link into output_edit
        // HTMLEditorKit kit = ((HTMLEditorKit) output_edit.getEditorKit());
        // HTMLDocument doc = ((HTMLDocument) output_edit.getDocument());
        // try {
        // kit.insertHTML(doc, doc.getLength(), "<a
        // HREF='http://java.sun.com'>java.sun.com</a>", 0, 0, HTML.Tag.A);
        // }
        // catch (BadLocationException be)
        // {
        // System.err.println(be.toString());
        // }
        // catch (IOException ie)
        // {
        // System.err.println(ie.toString());
        // }
        }

    public static String getNodeName(int tmp_i)
        {
        return my_full_net.getNetwork().getActor(tmp_i).getName();
        }

    public void readInitialSettings()
        {
        try (Reader reader = IOUtils.reader(new File(
                    "AgnaDefaultSettings.ini")))
            {
            JTextPane tmp_pane = new JTextPane();
            tmp_pane.read(reader, null);
            my_full_net.parseAgnaNonGraphicDefaultSettings(tmp_pane.getText());
            } catch (Exception ex)
            {
            setInitialSettings();
            }
        }

    public static void setInitialSettings()
        {
        default_node_face_source = Environment.getFacesDirectory()
                + System.getProperty("file.separator") + "Red Bullet.gif";
        }

    public static void setDefaultNodeFaceSource(String tmp_face_source)
        {
        if (tmp_face_source == null || tmp_face_source.equals("-")
                || !(new File(tmp_face_source)).exists())
            {
            default_node_face_source = Environment.getFacesDirectory()
                    + System.getProperty("file.separator")
                    + "Red Bullet.gif";
            } else
            {
            default_node_face_source = tmp_face_source;
            }
        }

    public static String getDefaultNodeFaceSource()
        {
        try
            {
            File face_file = new File(default_node_face_source);
            if (!(face_file).exists())
                return "-";
            return default_node_face_source;
            } catch (Exception e)
            {
            return "-";
            }
        }

    public static boolean getCurrentWeight()
        {
        return weight;
        }

    public static void setCurrentWeight(boolean tmp_weight)
        {
        weight = tmp_weight;
        }

    public static Network getCurrentNetwork()
        {
        // 2.1.3: null-safe (returns null instead of throwing before the main
        // frame is initialized, e.g. when parsing a file headless)
        if (my_full_net == null)
            {
            return null;
            }
        return my_full_net.getNetwork();
        }

    public static AgnaTextPane getCurrentOutputPane()
        {
        return output_edit;
        }

    public static FullNet getCurrentFullNet()
        {
        return my_full_net;
        }

    public static ImageIcon getMainIcon()
        {
        return main_icon;
        }

    public static NodeArea getCurrentArea()
        {
        if (!my_full_net.isArea())
            return null;
        return my_full_net.getArea();
        }

    public static JFrame getCurrentFrame()
        {
        return my_frame;
        }

    public static JTable getCurrentTable()
        {
        return my_grid;
        }

    public static ImageStock getCurrentImageStock()
        {
        return image_stock;
        }

    public static ProgressDialog getCurrentProgressDialog()
        {
        return progress_dialog;
        }

    public static AgnaTableModel getCurrentTableModel()
        {
        return grid_model;
        }

    public static TableCellEditor getTableCellEditor()
        {
        return cell_editor;
        }

    /*
     * // reads node names from header and updates // them into network and
     * area: public static void XXupdateNodeNames() { String names[] =
     * grid_header.getNames(); int n = names.length; if
     * (my_full_net.getNetwork().getSize() != n) return; Network tmp_network =
     * MainFrame.getCurrentNetwork(); for (int i = 0; i < n; i++) {
     * tmp_network.setNodeName(names[i], i); } if (my_full_net.isArea()) {
     * NodeArea tmp_area = MainFrame.getCurrentArea(); for (int i = 0; i < n;
     * i++) { tmp_area.my_nodes[i].setName(names[i]); } } }
     */

    // changes node name in headers:
    public static void setNodeName(String tmp_name, int tmp_node_index)
        {
        if (tmp_node_index < 0)
            return;
        MainFrame.getCurrentNetwork().setNodeName(tmp_name, tmp_node_index);
        // XX:
        grid_header.setName(tmp_name, tmp_node_index);
        my_grid.getTableHeader().validate();
        // my_grid.validate();
        MainFrame.getCurrentFullNet().setChanged(true);
        // MainFrame.updateNodeNames();
        // XX MainFrame.setTableHeaders();

        my_frame.validate();
        my_frame.repaint();
        }

    // changes node face in headers:
    public static void setNodeFace(ImageIcon tmp_icon, int tmp_node_index)
        {
        if (tmp_node_index < 0)
            return;
        grid_header.setFace(tmp_icon, tmp_node_index);
        my_grid.validate();
        }

    // changes all node faces in headers:
    public static void setAllNodeFaces(ImageIcon tmp_icon)
        {
        grid_header.setAllFaces(tmp_icon);
        my_grid.validate();
        }

    // updates the array of node names in headers:
    public static void setTableHeaders()
        {
        grid_header.setBothHeaders(my_full_net.getNetwork().getNodeNames());
        my_grid.getTableHeader().validate();
        my_grid.validate();
        }

    public static void setTableCellEditor()
        {
        int columns_number = my_grid.getModel().getColumnCount();
        TableColumn table_column;
        for (int i = 0; i < columns_number; i++)
            {
            table_column = my_grid.getColumnModel().getColumn(i);
            table_column.setCellEditor(cell_editor);
            }
        my_grid.validate();
        }

    public static String getCurrentStatus()
        {
        return status_bar.getText();
        }

    public static void setCurrentStatus(String tmp_status)
        {
        status_bar.setText(tmp_status);
        }

    // returns working directory plus file separator character
    public static String getWorkingDirectory()
        {
        if (working_directory != null
                && (new File(working_directory)).isDirectory())
            {
            // adding file-separator character at end:
            if (working_directory.lastIndexOf(System
                    .getProperty("file.separator")) != working_directory
                    .length() - 1)
                working_directory += System.getProperty("file.separator");
            return working_directory;
            } else
            return Environment.getCurrentDirectory();
        }

    public static void setWorkingDirectory(String tmp_directory)
        {
        if ((new File(tmp_directory)).isDirectory())
            {
            working_directory = tmp_directory;
            } else
            working_directory = null;
        }

    public static void setEnabledCloseViewerMenu()
        {
        v_close.setEnabled(true);
        }

    public static void setDisabledCloseViewerMenu()
        {
        v_close.setEnabled(false);
        }

    public static boolean isFloat(String str)
        {
        try
            {
            float value = Float.parseFloat(str);
            return true;
            } catch (Exception ex)
            {
            return false;
            }
        }

    private static boolean isEditingFloat()
        {
        JTable my_table = MainFrame.getCurrentTable();
        int row = my_table.getSelectedRow();
        int col = my_table.getSelectedColumn();
        try
            {
            float value = Float.parseFloat((String) my_table.getValueAt(row,
                    col));
            my_table.setValueAt(String.valueOf(value), row, col);
            return true;
            } catch (Exception ex)
            {
            return false;
            }
        }

    /**
     * returns the top directory name + filename as a String
     */
    public static String getTopFolder(String filename)
        {
        int i = filename.lastIndexOf(Environment.fs);
        if (i > 0 && i < filename.length() - 1)
            {
            return filename.substring(i + 1).toLowerCase();
            } else if (i >= filename.length() - 1)
            {
            return "";
            } else if (i == -1)
            {
            return null;
            }
        return null;
        }

    private void doAppendImageToOutput(BufferedImage tmp_image)
        {
        output_edit.appendBufferedImage(tmp_image);
        }

    private void doAppendParagraphToOutput(String str)
        {
        output_edit.appendParagraph(str);
        }

    private void doClearOutput()
        {
        output_edit.clearAll();
        my_frame.repaint();
        }

    private FocusListener flst = new FocusListener()
        {
            public void focusGained(FocusEvent e)
                {
                if (m_xStart >= 0 && m_xFinish >= 0)
                    if (output_edit.getCaretPosition() == m_xStart)
                        {
                        output_edit.setCaretPosition(m_xFinish);
                        output_edit.moveCaretPosition(m_xStart);
                        } else
                        output_edit.select(m_xStart, m_xFinish);
                }

            public void focusLost(FocusEvent e)
                {
                m_xStart = output_edit.getSelectionStart();
                m_xFinish = output_edit.getSelectionEnd();
                }
        };

    private JComboBox makeFontFamilyCombo(final AgnaTextPane textPane)
        {

        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        String[] n;
        try
            {
            n = ge.getAvailableFontFamilyNames();
            } catch (Exception e)
            {
            return null;
            }

        for (int i = 0; i < n.length; i++)
            {
            comboModel.addElement(n[i]);
            }

        JComboBox combo = new JComboBox(comboModel);

        combo.setPreferredSize(new Dimension(200, 23));
        combo.setMaximumSize(new Dimension(200, 23));
        combo.setSelectedIndex(1);
        combo.addActionListener(new ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                    {

                    MutableAttributeSet set = new SimpleAttributeSet();
                    int start = textPane.getSelectionStart();
                    String text = textPane.getSelectedText();
                    JComboBox combo = (JComboBox) e.getSource();
                    String choice = (String) combo.getSelectedItem();

                    try
                        {
                        StyleConstants.setFontFamily(set, choice);
                        // textPane.setFont(set.toString().substring(7,
                        // set.toString().length()), start, text.length());
                        textPane.getStyledDocument().setCharacterAttributes(
                                start, text.length(), set, false);
                        } catch (Exception e1) {
      AgnaLog.warn("suppressed exception", e1);
      }
                    }
            });

        return combo;
        }

    private JComboBox makeFontSizeCombo(final AgnaTextPane textPane)
        {

        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        String[] n = { "8", "9", "10", "11", "12", "13", "14", "15", "16",
                "17", "18", "19", "20", "21", "22", };

        for (int i = 0; i < n.length; i++)
            {
            comboModel.addElement(n[i]);
            }

        JComboBox combo = new JComboBox(comboModel);

        combo.setPreferredSize(new Dimension(50, 23));
        combo.setMaximumSize(new Dimension(50, 23));
        combo.setSelectedIndex(4);
        combo.addActionListener(new ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                    {

                    MutableAttributeSet set = new SimpleAttributeSet();
                    int start = textPane.getSelectionStart();
                    String text = textPane.getSelectedText();
                    JComboBox combo = (JComboBox) e.getSource();
                    int choice = Integer.parseInt((String) combo
                            .getSelectedItem());

                    StyleConstants.setFontSize(set, choice);
                    textPane.getStyledDocument().setCharacterAttributes(start,
                            text.length(), set, false);
                    }
            });

        return combo;
        }

    // changing network name:
    private void doChangeTitle()
        {
        String tmp_str, tmp_title;
        tmp_title = my_full_net.getNetwork().getName();
        tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                "Please enter a new name for current network", "Network name",
                JOptionPane.QUESTION_MESSAGE, null, null, tmp_title);
        my_frame.repaint();
        if (tmp_str != null)
            {
            my_full_net.getNetwork().setName(tmp_str);
            my_frame.setTitle(tmp_str + " - "
                    + Environment.getApplicationFullName());
            if (my_full_net.isArea())
                {
                my_full_net.getArea().setTitle(tmp_str);
                }
            }
        }

    private void doAddNodes()
        {
        if (checkAllValues())
            updateNetwork();
        else
            return;
        my_frame.repaint();
        String tmp_str = "";
        Object[] values = new Object[10];
        for (int i = 0; i < 10; i++)
            {
            values[i] = (Object) String.valueOf(i + 1);
            }
        tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                "Please select number of nodes to be added:", "Add new nodes",
                JOptionPane.QUESTION_MESSAGE, null, values, "1");
        my_frame.repaint();
        if (tmp_str == null)
            return;
        final int nn = Integer.parseInt(tmp_str);

        // adding nodes inside thread
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCurrentStatus("Adding " + String.valueOf(nn)
                + " Actor(s) to current network...");
        grid_model.setReady(false);

        my_full_net.addNodesToNetwork(nn); // no position specified
        for (int i = 1; i <= nn; i++)
            {
            // my_full_net.addNodeToNetwork(-1, -1, -1); // no position
            // specified
            grid_model.addRowCol();
            }

        updateMatrix();
        grid_model.setReady(true);
        MainFrame.setTableCellEditor();
        setCurrentStatus(default_status);
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        }

    private void doDeleteNodes()
        {
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int delnode;
        delnode = my_grid.getSelectedColumn();
        if (delnode < 0 || delnode > my_full_net.getNetwork().getSize())
            {
            setCurrentStatus(default_status);
            my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
            }
        setCurrentStatus("Deleting node "
                + getCurrentNetwork().getActor(delnode).getName() + "...");
        if (checkAllValues())
            updateNetwork();
        else
            {
            setCurrentStatus(default_status);
            my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
            }
        grid_model.setReady(false);
        try
            {
            my_full_net.deleteNodeToNetwork(delnode);
            updateMatrix();
            grid_model.setReady(true);
            if (my_grafic != null && my_grafic.gr_frame != null)
                GrNet.disableFirst();
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        setCurrentStatus(default_status);
        MainFrame.setTableCellEditor();
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

    private void doAddScalar()
        {
        my_frame.repaint();
        float scalar = 0f;
        String tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                "Please enter value to be added", "Add scalar value",
                JOptionPane.QUESTION_MESSAGE, null, null, "0.0");
        if (tmp_str == null)
            return;
        try
            {
            scalar = Float.parseFloat(tmp_str);
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(my_frame, "Error reading value.",
                    "Parsing error", JOptionPane.INFORMATION_MESSAGE);
            return;
            }

        doTransform((byte) 1, "Adding scalar value to sociomatrix...", -1,
                (byte) 0, scalar);
        }

    private void doMultiplyByScalar()
        {
        my_frame.repaint();
        float scalar = 0f;
        final String tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                "Please enter multiplication factor", "Multiply by scalar",
                JOptionPane.QUESTION_MESSAGE, null, null, "1.0");
        if (tmp_str == null)
            return;
        try
            {
            scalar = Float.parseFloat(tmp_str);
            } catch (Exception e)
            {
            JOptionPane.showMessageDialog(my_frame, "Error reading value.",
                    "Parsing error", JOptionPane.INFORMATION_MESSAGE);
            return;
            }
        doTransform((byte) 2, "Multiplying sociomatrix with scalar...", -1,
                (byte) 0, scalar);
        }

    private void doTranspose()
        {
        doTransform((byte) 6, "Transposing current network's sociomatrix...",
                -1, (byte) 0, 0f);
        }

    private void doSymmetrize()
        {
        /*
         * Thread runner = new Thread() { public void run() {
         */
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCurrentStatus("Symmetrizing current network's sociomatrix...");
        if (checkAllValues())
            updateNetwork();
        else
            {
            setCurrentStatus(default_status);
            my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
            }
        grid_model.setReady(false);
        my_full_net.symmetrize(my_frame);
        updateMatrix();
        grid_model.setReady(true);
        setCurrentStatus(default_status);
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        /*
         * } }; //end of thread runner.start();
         */
        }

    private void doNormalize()
        {
        int confirm = JOptionPane.showOptionDialog(my_frame,
                "All non-zero values will be replaced by 1!\nContinue?",
                "Confirm binarize operation", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (confirm == 0)
            {
            doTransform((byte) 4, "Converting sociomatrix to binary data...",
                    -1, (byte) 0, 0f);
            }
        }

    private void doRemoveOut()
        {
        doTransform((byte) 5,
                "Removing isolated nodes from current network...", -1,
                (byte) 0, 0f);
        }

    private void doRenumberNodes()
        {
        if (checkAllValues())
            updateNetwork();
        else
            return;
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCurrentStatus("Replacing node names with numbers...");
        Network tmp_network = getCurrentNetwork();
        int n = tmp_network.getSize();
        for (int i = 0; i < n; i++)
            {
            tmp_network.getActor(i).setName(String.valueOf(i + 1));
            }
        updateMatrix();
        my_full_net.setChanged(true);
        setCurrentStatus(default_status);
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        my_frame.repaint();
        }

    private void doMerge()
        {
        if (checkAllValues())
            updateNetwork();
        else
            return;
        }

    private void doMultiplyNetwork()
        {
        // JUST A TEST!
        doTransform((byte) 3, "Multiplying current sociomatrix by itself", -1,
                (byte) 0, 0f);
        }

    private void doBasic()
        {
        doAnalysis((byte) 1, "Analysing current network...", -1, -1);
        }

    private void doNodalDegree()
        {
        doAnalysis((byte) 15, "Computing Nodal Degrees of current network...",
                -1, -1);
        }

    private void doIndegree()
        {
        doAnalysis((byte) 14,
                "Computing Indegree coefficients for current network...", -1,
                -1);
        }

    private void doOutdegree()
        {
        doAnalysis((byte) 16,
                "Computing Outdegree coefficients for current network...", -1,
                -1);
        }

    private void doDensity()
        {
        doAnalysis((byte) 6, "Computing Density of current network...", -1, -1);
        }

    private void doCohesion()
        {
        doAnalysis((byte) 5, "Computing Cohesion index of current network...",
                -1, -1);
        }

    private void doEccentricity()
        {
        doAnalysis((byte) 10, "Computing Eccentricity coefficients...", -1, -1);
        }

    private void doDiameter()
        {
        doAnalysis((byte) 9, "Computing Diameter of current network...", -1, -1);
        }

    private void doGeodesics()
        {
        doAnalysis((byte) 13,
                "Computing matrix of Geodesics for current network...", -1, -1);
        }

    private void doShortestPaths()
        {
        ShortestPathsDialog sp_dialog = new ShortestPathsDialog(
                getCurrentNetwork());
        sp_dialog.showDialog();
        IndexPair node_pair = sp_dialog.getNodePair();
        if (node_pair == null)
            return;
        final int init = node_pair.first_index;
        final int end = node_pair.second_index;
        if (init == -1 || end == -1)
            return;
        //
        sp_dialog = null;
        doAnalysis((byte) 19, "Finding shortest paths in current network...",
                init, end);
        }

    private void doCliques()
        {
        my_frame.repaint();
        // finding clique diameter
        int max_number = my_full_net.getNetwork().getSize() - 1;
        String tmp_str = "";
        Object[] values = new Object[max_number];
        for (int i = 0; i < max_number; i++)
            {
            values[i] = (Object) String.valueOf(i + 1);
            }
        tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                "Please select clique diameter:", "Cliques",
                JOptionPane.QUESTION_MESSAGE, null, values, "1");
        // my_frame.repaint();
        if (tmp_str == null)
            return;

        try
            {
            // clique diameter:
            max_number = Integer.parseInt(tmp_str);
            } catch (Exception e)
            {
            return;
            }

        doAnalysis((byte) 21, "Finding " + tmp_str
                + "-cliques in current network...", max_number, -1);
        }

    private void doAllShortestPaths()
        {
        my_frame.repaint();

        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCurrentStatus("Finding all shortest paths in current network...");
        my_frame.validate();
        my_frame.repaint();
        if (checkAllValues())
            updateNetwork();
        else
            {
            setCurrentStatus(default_status);
            my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
            }
        AgnaLib.initAjna();
        final AgnaLib agna_lib = new AgnaLib();
        if (my_full_net.getNetwork().getEdgesNumber() == 0)
            {
            String outstr = "";
            outstr += agna_lib.getAgnaSignature() + AgnaLib.lb;
            if (AgnaLib.it != null)
                outstr += AgnaLib.it;
            outstr += "Current network is empty.";
            if (AgnaLib.unit != null)
                outstr += AgnaLib.unit;
            output_edit.appendBlock(outstr);
            outstr = null;
            setCurrentStatus(default_status);
            my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
            }

        final Network working_network = MainFrame.getCurrentNetwork();
        final int network_size = working_network.getSize();

        AgnaThread runner = new AgnaThread()
            {
                public void run()
                    {
                    this.decorate(null);
                    String tmp_str = new String("");
                    progress_dialog.setPercent(0);
                    output_edit.appendString(agna_lib.getAgnaSignature());
                    for (int i = 0; i < network_size; i++)
                        {

                        this.nap(); // allow cancel button to be pressed
                        if (progress_dialog.getStop())
                            {
                            this.undecorate();
                            return; // exit without reading network
                            }
                        progress_dialog.setPercent((int) ((float) (i + 1)
                                / network_size * 100f));

                        for (int j = 0; j < network_size; j++)
                            {
                            if (j == i)
                                continue;
                            tmp_str = agna_lib.outShortestPaths(working_network, i,
                                    j);
                            if (tmp_str != null
                                    && tmp_str.indexOf("No path found") < 0)
                                output_edit.appendString(tmp_str);
                            }
                        }
                    this.undecorate();
                    this.finish();
                    }
            };
        runner.go();
        }

    private void doFullAnalysis()
        {
        if (checkAllValues())
            updateNetwork();
        else
            return;
        }

    private void doEmissions()
        {
        doAnalysis((byte) 11, "Computing sociometric Emission coefficients...",
                -1, -1);
        }

    private void doReceptions()
        {
        doAnalysis((byte) 18,
                "Computing sociometric Reception coefficients...", -1, -1);
        }

    private void doDeterminations()
        {
        doAnalysis((byte) 7,
                "Computing Sociometric Determination coefficients...", -1, -1);
        }

    private void doSociostatus()
        {
        doAnalysis((byte) 20, "Computing sociometric Status coefficients...",
                -1, -1);
        }

    private void doCloseness()
        {
        doAnalysis((byte) 4, "Computing Closeness centrality coefficients...",
                -1, -1);
        }

    private void doBetweenness()
        {
        doAnalysis((byte) 3,
                "Computing Betweenness centrality coefficients...", -1, -1);
        }

    private void doFareness()
        {
        doAnalysis((byte) 12, "Computing Fareness centrality coefficients...",
                -1, -1);
        }

    private void doPrestige()
        {
        doAnalysis((byte) 17,
                "Computing Prestige coefficients for current network...", -1,
                -1);
        }

    private java.util.Vector askPajekVectors()
        {
        final String[] options = { "Emission Degree", "Reception Degree",
                "Weighted Emission Degree", "Sociometric Status",
                "Nodal Degree", "Betweenness", "Closeness", "Prestige" };
        final JDialog d = new JDialog(my_frame, "Pajek export - vectors",
                true);
        d.setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
        final java.util.Hashtable boxes = new java.util.Hashtable();
        final java.util.Vector cancelled = new java.util.Vector();
        cancelled.addElement("__CANCEL__");
        for (int i = 0; i < options.length; i++)
            {
            JCheckBox box = new JCheckBox(options[i], true);
            boxes.put(options[i], box);
            d.add(box);
            }
        JPanel buttons = new JPanel(new FlowLayout());
        JButton ok = new JButton("Export");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);
        d.add(buttons);
        final java.util.Vector result = new java.util.Vector();
        ok.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                    for (int i = 0; i < options.length; i++)
                        {
                        JCheckBox box = (JCheckBox) boxes.get(options[i]);
                        if (box.isSelected())
                            {
                            result.addElement(options[i]);
                            }
                        }
                    d.dispose();
                    }
            });
        cancel.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                    result.removeAllElements();
                    result.addElement("__CANCEL__");
                    d.dispose();
                    }
            });
        d.pack();
        d.setLocationRelativeTo(my_frame);
        d.setVisible(true);
        if (result.size() > 0 && result.elementAt(0).equals("__CANCEL__"))
            {
            return null;
            }
        return result;
        }

    private int askExportFormatDialog()
        {
        final String[] labels = { "Agna (.agn)", "Tab-separated (.txt)",
                "Comma-separated (.csv)", "Pajek (.net)", "Excel (.xls)" };
        final String[] vector_options = { "Emission Degree",
                "Reception Degree", "Weighted Emission Degree",
                "Sociometric Status", "Nodal Degree", "Betweenness",
                "Closeness", "Prestige" };
        final int[] result = new int[] { -1 };
        final JDialog d = new JDialog(my_frame, "Export format", true);
        d.setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
        final JRadioButton[] radios = new JRadioButton[labels.length];
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < labels.length; i++)
            {
            radios[i] = new JRadioButton(labels[i], i == 3); // Pajek default
            group.add(radios[i]);
            d.add(radios[i]);
            }
        d.add(new JLabel("Pajek: per-node measure vectors to append"));
        final java.util.Hashtable boxes = new java.util.Hashtable();
        for (int i = 0; i < vector_options.length; i++)
            {
            JCheckBox box = new JCheckBox(vector_options[i], true);
            boxes.put(vector_options[i], box);
            d.add(box);
            }
        JPanel buttons = new JPanel(new FlowLayout());
        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);
        d.add(buttons);
        ok.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                    for (int i = 0; i < radios.length; i++)
                        {
                        if (radios[i].isSelected())
                            {
                            result[0] = i;
                            break;
                            }
                        }
                    saved_pajek_vectors = new java.util.Vector();
                    for (int i = 0; i < vector_options.length; i++)
                        {
                        JCheckBox box = (JCheckBox) boxes
                                .get(vector_options[i]);
                        if (box.isSelected())
                            {
                            saved_pajek_vectors.addElement(vector_options[i]);
                            }
                        }
                    d.dispose();
                    }
            });
        cancel.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                    d.dispose();
                    }
            });
        d.pack();
        d.setLocationRelativeTo(my_frame);
        d.setVisible(true);
        return result[0];
        }

    // template for most analysis methods
    private void doAnalysis(byte tmp_analysis_type, String decoration,
            int param_1, int param_2)
        {
        if (checkAllValues())
            updateNetwork();
        else
            return;
        final byte analysis_type = tmp_analysis_type;
        final int init = param_1;
        final int end = param_2;
        AgnaThread runner = new AgnaThread()
            {
                public void run()
                    {
                    this.decorate(null);
                    AgnaLib agna_lib = new AgnaLib();

                    progress_dialog.setPercent(1);

                    this.nap(); // allow cancel button to be pressed
                    if (progress_dialog.getStop())
                        {
                        this.undecorate();
                        return; // exit without reading network
                        }
                    progress_dialog.setPercent(40);

                    String tmp_str = new String("");
                    tmp_str += agna_lib.getAgnaSignature();

                    // deciding the type of analysis:

                    switch (analysis_type)
                        {

                        case 1:
                        tmp_str += agna_lib.outBasic(my_full_net.getNetwork());
                        break;

                        case 2:
                        tmp_str += agna_lib.outBavelas(my_full_net.getNetwork());
                        break;

                        case 3:
                        tmp_str += agna_lib.outBetweenness(my_full_net.getNetwork());
                        break;

                        case 4:
                        tmp_str += agna_lib.outCloseness(my_full_net.getNetwork());
                        break;

                        case 5:
                        tmp_str += agna_lib.outCohesion(my_full_net.getNetwork());
                        break;

                        case 6:
                        tmp_str += agna_lib.outDensity(my_full_net.getNetwork());
                        break;

                        case 7:
                        tmp_str += agna_lib.outDeterminationDegree(my_full_net
                                .getNetwork());
                        break;

                        case 9:
                        tmp_str += agna_lib.outDiameter(my_full_net.getNetwork());
                        break;

                        case 10:
                        tmp_str += agna_lib
                                .outEccentricity(my_full_net.getNetwork());
                        break;

                        case 11:
                        tmp_str += agna_lib.outEmissionDegree(my_full_net
                                .getNetwork());
                        break;

                        case 12:
                        tmp_str += agna_lib.outFareness(my_full_net.getNetwork());
                        break;

                        case 13:
                        tmp_str += agna_lib.outGeodesics(my_full_net.getNetwork());
                        break;

                        case 14:
                        tmp_str += agna_lib.outInDegree(my_full_net.getNetwork());
                        break;

                        case 15:
                        tmp_str += agna_lib.outNodalDegree(my_full_net.getNetwork());
                        break;

                        case 16:
                        tmp_str += agna_lib.outOutDegree(my_full_net.getNetwork());
                        break;

                        case 17:
                        tmp_str += agna_lib.outPrestige(my_full_net.getNetwork());
                        break; // inexistent yet

                        case 18:
                        tmp_str += agna_lib.outReceptionDegree(my_full_net
                                .getNetwork());
                        break;

                        case 19:
                        tmp_str += agna_lib.outShortestPaths(my_full_net
                                .getNetwork(), init, end);
                        break;

                        case 20:
                        tmp_str += agna_lib.outSociometricStatus(my_full_net
                                .getNetwork());
                        break;

                        case 21:
                        tmp_str += agna_lib.outCliques(my_full_net.getNetwork(),
                                init);
                        break;

                        }

                    this.nap(); // allow cancel button to be pressed
                    if (progress_dialog.getStop())
                        {
                        this.undecorate();
                        return; // exit without reading network
                        }
                    progress_dialog.setPercent(90);

                    output_edit.appendBlock(tmp_str);

                    this.finish();
                    }
            };
        runner.go();
        }

    // template for most analysis methods
    private void doTransform(byte tmp_analysis_type, String decoration,
            int tmp_param_1, byte tmp_param_2, float tmp_param_3)
        {
        if (checkAllValues())
            updateNetwork();
        else
            return;
        final byte analysis_type = tmp_analysis_type;
        final int param_1 = tmp_param_1;
        final byte param_2 = tmp_param_2;
        final float param_3 = tmp_param_3;

        AgnaThread runner = new AgnaThread()
            {
                public void run()
                    {
                    this.decorate(null);
                    AgnaLib agna_lib = new AgnaLib();

                    progress_dialog.setPercent(1);

                    grid_model.setReady(false);

                    this.nap(); // allow cancel button to be pressed
                    if (progress_dialog.getStop())
                        {
                        this.undecorate();
                        return; // exit without reading network
                        }
                    progress_dialog.setPercent(40);

                    // deciding the type of analysis:
                    switch (analysis_type)
                        {

                        case 1:
                        my_full_net.addScalar(param_3);
                        break;

                        case 2:
                        my_full_net.multiplyByScalar(param_3);
                        break;

                        case 3:
                        my_full_net.square();
                        break;

                        case 4:
                        my_full_net.normalize();
                        break;

                        case 5:
                        my_full_net.removeOutsidersInNetwork(grid_model);
                        break;

                        case 6:
                        my_full_net.transpose();
                        break;

                        }

                    progress_dialog.setPercent(90);
                    updateMatrix();
                    grid_model.setReady(true);

                    this.undecorate();
                    this.finish();
                    }
            };
        runner.go();
        }

    private void doBavelas()
        {
        doAnalysis((byte) 2, "Computing Bavelas centrality coefficients...",
                -1, -1);
        }

    // places network matrix into table-grid -- GrNet.setMatrix()
    public static void updateMatrix()
        {
        // nu = matrix dimension
        grid_model.setReady(false);
        Network tmp_network = my_full_net.getNetwork();
        tmp_network.deleteReflections();
        int nu = tmp_network.getSize();
        grid_model = (AgnaTableModel) my_grid.getModel();
        int dif = nu - grid_model.getColumnCount();
        if (dif != 0)
            {
            // my_grid.removeColumnSelectionInterval(nu,grid_model.getColumnCount()
            // - 1);
            grid_model.setColumnCount(nu);
            grid_model.fireTableStructureChanged();
            if (nu != grid_model.getRowCount())
                {
                grid_model.setRowCount(nu);
                }
            grid_model.fireTableRowsDeleted(nu, nu + dif - 1);
            }
        // my_grid.getTableHeader().getColumnModel().setColumnCount(nu);
        // my_grid.validate();
        MainFrame.setTableCellEditor();
        my_grid.setPreferredSize(new Dimension(MainFrame.col_width
                * my_grid.getColumnCount(), my_grid.getRowHeight()
                * my_grid.getRowCount()));
        for (int i = 0; i < nu; i++)
            {
            for (int j = 0; j < nu; j++)
                {
                my_grid.setValueAt(String.valueOf(tmp_network.getValue(i, j)),
                        i, j);
                /*
                 * if (i != j)
                 * my_grid.setValueAt(String.valueOf(tmp_network.getValue(i,j)),i,j);
                 * else my_grid.setValueAt("0.0",i,j);
                 */
                }
            }
        MainFrame.setTableHeaders();
        my_frame.validate();
        grid_model.setReady(true);
        }

    // checks whether grid contains real-type data;
    // determines the binary/weighted type of data
    // not to be used inside threads!
    public static boolean checkAllValues()
        {
        return my_grid.doCheckAllValues();
        }

    private static void stopEditingTable()
        {
        my_grid.doStopEditingTable();
        }

    /**
     * send current grid data to network; always use checkAllValues() before
     * updateNetwork()!
     */
    public static void updateNetwork()
        {
        int nu = my_grid.getModel().getColumnCount();
        grid_model.deleteDiagonal();
        // XXupdateNodeNames();
        Network tmp_network = my_full_net.getNetwork();
        DefaultTableModel tmp_model = (DefaultTableModel) my_grid.getModel();
        if (nu != tmp_network.getSize())
            tmp_network.setSize(nu);
        for (int i = 0; i < nu; i++)
            {
            for (int j = 0; j < nu; j++)
                {
                // tmp_network.setObjectValue(new
                // Float((String)tmp_model.getValueAt(i,j)), i, j);
                tmp_network.setObjectValue(my_grid.getValueAt(i, j), i, j);
                }
            }
        if (my_full_net.isArea())
            my_full_net.net_area.updateArea(my_full_net.getNetwork());
        // grid_model.setReady(true);
        
        }

    // view graph form: *********************
    public void view_graph()
        {
        if (checkAllValues())
            updateNetwork();
        else
            return;
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCurrentStatus("Preparing Network Viewer...");
        // my_grafic e o componenta grafica GrNet
        if (my_grafic != null)
            {
            if (my_grafic.gr_frame != null)
                {
                // my_grafic.getCurrentFrame().setTitle(my_full_net.getNetwork().getName()
                // + " - Agna 2.0: Network Viewer");
                my_grafic.my_area.paintEdges();
                my_grafic.getCurrentFrame().pack();
                // my_grafic.getCurrentFrame().show();
                } else
                {
                my_grafic = null;
                
                }
            }
        if (my_grafic == null)
            {
            if (!my_full_net.isArea())
                {
                my_full_net.attachArea();
                }
            my_grafic = new GrNet(my_full_net, grid_model, output_edit);
            }
        // this.repaint();
        setEnabledCloseViewerMenu();
        my_grafic.my_area.paintEdges();
        // my_grafic.getCurrentFrame().setTitle(my_full_net.getNetwork().getName()
        // + " - Agna 2.0: Network Viewer");
        my_grafic.getCurrentFrame().toFront();
        my_grafic.getCurrentFrame().setState(JFrame.NORMAL);
        setCurrentStatus(default_status);
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // my_grafic.gr_frame.repaint();
        }

    // dialog pre-quit: *********************

    public int show_dialog()
        {
        JOptionPane confirm_d = new JOptionPane();
        int confirm = confirm_d.showOptionDialog(my_frame, "Quit Agna?",
                "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        confirm_d = null;
        return confirm;
        };

    // set native look & feel:: *********************

    public static void setNativeLookAndFeel()
        {
        try
            {
            // 2.1.3: theme packs ship on the classpath instead of the working
            // directory, so they resolve from the jar too
            Skin theSkinToUse = SkinLookAndFeel.loadThemePack(Agna.class
                    .getResourceAsStream("/themepacks/aquathemepack.zip"));
            SkinLookAndFeel.setSkin(theSkinToUse);
            // finally set the Skin Look And Feel
            UIManager.setLookAndFeel(new SkinLookAndFeel());

            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e)
            {
            System.out.println(e.toString());
            }
        }

    // focus listener of the table:
    private FocusListener grid_focus_listener = new FocusListener()
        {
            public void focusLost(FocusEvent e)
                {
                if (e.getSource() == MainFrame.getCurrentTable())
                    {
                    MainFrame.stopEditingTable();
                    MainFrame.checkAllValues();
                    }
                }

            public void focusGained(FocusEvent e)
                {
                }
        };

    // defining the cell editor:
    private static TableCellEditor cell_editor = new DefaultCellEditor(
            new JTextField(7))
        {
            public Component getTableCellEditorComponent(JTable my_table,
                    Object value, boolean isSelected, int row, int column)
                {
                super.getTableCellEditorComponent(my_table, value, isSelected,
                        row, column);
                JTextField editor_field = (JTextField) getComponent();
                editor_field.selectAll();
                setClickCountToStart(1);
                Network tmp_network = MainFrame.getCurrentNetwork();
                MainFrame.setCurrentStatus("Editing cell: "
                        + tmp_network.getActor(row).getName() + " --> "
                        + tmp_network.getActor(column).getName());
                // editor_field.setCaretColor(Colors.MRed);
                // editor_field.setBackground(Colors.lightrose);
                editor_field.getCaret().setSelectionVisible(true);
                return editor_field;
                }
        };

    // frame principal constructor: *********************

    public MainFrame()

        {
        progress_dialog = new ProgressDialog();
        image_stock = new ImageStock();
        about_box = null;

        // default settings:

        if ((new File("AgnaDefaultSettings.ini")).exists())
            readInitialSettings();// settings form file
        else
            setInitialSettings(); // settings generated

        // initial definitions:
        setNativeLookAndFeel();
        MainFrame.setCurrentWeight(false);
        my_frame = new JFrame();
        my_frame.setTitle(Environment.getApplicationFullName());
        max_dim = my_frame.getToolkit().getScreenSize();
        max_dim.width -= 7;
        max_dim.height -= 80;
        my_frame.setSize(max_dim);
        my_frame.setLocation(0, 0);
        // old location:
        main_icon = Environment.getButtonImageIcon("Agna_icon.gif");

        // read icon from current jar archive:
        // main_icon = new ImageIcon(Agna.class.getResource("Agna_icon.gif"));

        my_frame.setIconImage(main_icon.getImage());
        fContent = my_frame.getContentPane();
        // fContent.setBackground(Color.white);
        controlArea = new JPanel(); // panel principal
        controlArea.setPreferredSize(max_dim); // new Dimension(600, 200));
        controlArea.setLayout(new BorderLayout());

        output_edit = new AgnaTextPane(); // componenta output
        output_edit.addFocusListener(flst);

        scrollEditRight = new JScrollPane(output_edit);
        scrollEditRight.setPreferredSize(new Dimension(
                (int) (max_dim.width / 5), max_dim.height));

        ttm = ToolTipManager.sharedInstance();
        ttm.setInitialDelay(400);
        ttm.setDismissDelay(1300);

        my_full_net = new FullNet();
        my_full_net.createDefaultNetwork(); // default network with 10 nodes
        my_frame.setTitle(my_full_net.getNetwork().getName() + " - "
                + Environment.getApplicationFullName());

        // initializing JTable:
        my_grid = new AgnaTable();
        ttm.registerComponent(my_grid);

        grid_model = new AgnaTableModel(my_full_net.getNetwork().getSize());
        my_grid.setModel(grid_model);
        grid_model.setGrid(my_grid);
        // on enter, select next cell:
        KeyStroke enter_key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        my_grid.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                enter_key, "selectNextColumnCell");
        // my_grid.setBackground(Color.white);
        my_grid.setMinimumSize(new Dimension((int) (max_dim.width / 2),
                max_dim.height));
        my_grid.setPreferredSize(new Dimension(MainFrame.col_width
                * my_grid.getColumnCount(), my_grid.getRowHeight()
                * my_grid.getRowCount()));
        int tmp_resize = 0;
        SelectionListener act_selection_listener = new SelectionListener(
                my_grid);
        // selection listener for row movements of cursor:
        my_grid.getSelectionModel().addListSelectionListener(
                act_selection_listener);
        // selection listener for column movements of cursor:
        my_grid.getColumnModel().getSelectionModel().addListSelectionListener(
                act_selection_listener);

        // editor:
        cell_editor.addCellEditorListener(act_cell_listener);
        this.setTableCellEditor();

        my_adapter = new ExcelAdapter(my_grid);
        scrollEditLeft = new JScrollPane(my_grid);
        // scrollEditLeft.getViewport().setBackground(Color.red);

        // table header:
        grid_header = new AgnaTableHeader();
        scrollEditLeft.setRowHeaderView(grid_header.getAgnaVerticalHeader());
        ttm.registerComponent(grid_header.getAgnaVerticalHeader());

        updateMatrix();
        grid_model.setReady(true);

        upleft_button = new JButton(Environment
                .getButtonImageIcon("AddNodes.gif"));
        upleft_button.setRolloverIcon(Environment
                .getButtonImageIcon("rAddNodes.gif"));
        upleft_button.setToolTipText("Add new nodes to current network Ctrl+P");
        upleft_button.addActionListener(act_menu);
        upleft_button.setBorder(new LineBorder(Color.gray, 1));
        upleft_button.setPreferredSize(new Dimension((int) (col_width * 2 / 3),
                my_grid.getRowHeight()));
        // upleft_button.setEnabled(false);
        lowerleft_button = new JButton(
                "<html><font size = 1 color='#298C8C' face='Arial,Helvetica,Verdana,sans-serif'>Help</font>");
        lowerleft_button.addActionListener(act_menu);
        lowerleft_button.setToolTipText("Display the Agna Help frame Ctrl+H");
        lowerleft_button.setBorder(new LineBorder(Color.gray, 1));
        // lowerleft_button.setEnabled(false);
        JButton upright_button = new JButton();
        upright_button.setBorder(new LineBorder(Color.gray, 1));
        upright_button.setEnabled(false);
        JButton lowerright_button = new JButton();
        lowerright_button.setBorder(new LineBorder(Color.gray, 1));
        lowerright_button.setEnabled(false);

        scrollEditLeft.setCorner(JScrollPane.UPPER_LEFT_CORNER, upleft_button);
        scrollEditLeft.setCorner(JScrollPane.LOWER_LEFT_CORNER,
                lowerleft_button);
        scrollEditLeft
                .setCorner(JScrollPane.UPPER_RIGHT_CORNER, upright_button);
        scrollEditLeft.setCorner(JScrollPane.LOWER_RIGHT_CORNER,
                lowerright_button);
        // scrollEditLeft.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // scrollEditLeft.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollEditLeft.setPreferredSize(new Dimension(
                (int) (max_dim.width / 2), max_dim.height));

        left_panel = new JPanel(); // panelul pentru tabel
        right_panel = new JPanel(); // panelul pentru output
        left_panel.setLayout(new BorderLayout());
        right_panel.setLayout(new BorderLayout());
        right_panel.add(scrollEditRight);
        left_panel.add(scrollEditLeft);
        // splitter:
        m_sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left_panel,
                right_panel);

        // status bar:
        status_panel = new JPanel();
        // status_panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        status_panel.setMinimumSize(new Dimension(40, 25));
        status_panel.setMaximumSize(new Dimension(max_dim.width, 25));
        status_panel.setPreferredSize(new Dimension(max_dim.width, 25));
        status_layout = new FlowLayout();
        status_layout.setAlignment(FlowLayout.LEFT);
        status_panel.setLayout(status_layout);
        status_bar = new JLabel(default_status);
        status_panel.add(status_bar);
        controlArea.add(m_sp, BorderLayout.CENTER);
        controlArea.add(status_panel, BorderLayout.SOUTH);
        fContent.add(controlArea);
        AgnaLib.initAjna();

        // Meniuri:
        mFile = new JMenu("File");
        // mOutput=new JMenu("Output");
        mEdit = new JMenu("Edit");
        mData = new JMenu("Network");
        mAnalysis = new JMenu("Analysis");
        mView = new JMenu("View");
        mPreferences = new JMenu("Preferences");
        mHelp = new JMenu("Help");

        mFile.setMnemonic('f');
        // mOutput.setMnemonic('o');
        mEdit.setMnemonic('e');
        mData.setMnemonic('n');
        mAnalysis.setMnemonic('y');
        mView.setMnemonic('v');
        mPreferences.setMnemonic('p');
        mHelp.setMnemonic('h');

        // submeniuri din FILE:
        f_new = new JMenuItem("New Network");
        f_open = new JMenuItem("Open Network");
        f_save = new JMenuItem("Save Network As...");
        f_simply_save = new JMenuItem("Save Network");
        f_create = new JMenuItem("New From Chain...");
        f_quit = new JMenuItem("Quit");

        f_new.setToolTipText("Create a new empty network");
        f_open.setToolTipText("Open an existing network file");
        f_save.setToolTipText("Save current network as a specified file");
        f_simply_save.setToolTipText("Save current network");
        f_create.setToolTipText("Create a network from a chain file");
        f_quit.setToolTipText("Completely exit Agna");

        f_open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                ActionEvent.CTRL_MASK));
        f_new.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                ActionEvent.CTRL_MASK));
        f_simply_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.CTRL_MASK));

        f_new.addActionListener(act_menu);
        f_open.addActionListener(act_menu);
        f_save.addActionListener(act_menu);
        f_simply_save.addActionListener(act_menu);
        f_create.addActionListener(act_menu);
        f_quit.addActionListener(act_menu);

        f_new.setMnemonic('n');
        f_open.setMnemonic('o');
        f_save.setMnemonic('a');
        f_simply_save.setMnemonic('s');
        f_create.setMnemonic('f');
        f_quit.setMnemonic('q');

        mFile.add(f_new);
        mFile.add(f_open);
        mFile.addSeparator();
        mFile.add(f_create);
        mFile.addSeparator();
        mFile.add(f_save);
        mFile.add(f_simply_save);
        mFile.addSeparator();
        // mFile.add(f_quit); // not yet

        // Submeniuri din EDIT:
        e_cut = new JMenuItem("Cut");
        e_copy = new JMenuItem("Copy");
        e_paste = new JMenuItem("Paste");
        e_delete = new JMenuItem("Delete");
        e_select_all = new JMenuItem("Select All");

        e_cut
                .setToolTipText("Delete selected text/data and store it into clipboard");
        e_copy.setToolTipText("Store selected text/data into clipboard");
        e_paste.setToolTipText("Insert clipboard content at cursor");
        e_select_all.setToolTipText("Select all text/data");

        e_cut.addActionListener(new ExcelAdapter(my_grid));
        e_copy.addActionListener(new ExcelAdapter(my_grid));
        e_paste.addActionListener(new ExcelAdapter(my_grid));
        e_delete.addActionListener(new ExcelAdapter(my_grid));
        e_select_all.addActionListener(new ExcelAdapter(my_grid));

        e_cut.setMnemonic('t');
        e_copy.setMnemonic('c');
        e_paste.setMnemonic('p');
        e_delete.setMnemonic('d');
        e_select_all.setMnemonic('a');

        e_cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                ActionEvent.CTRL_MASK));
        e_copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                ActionEvent.CTRL_MASK));
        e_paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                ActionEvent.CTRL_MASK));
        e_select_all.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                ActionEvent.CTRL_MASK));

        mEdit.add(e_cut);
        mEdit.add(e_copy);
        mEdit.add(e_paste);
        // mEdit.add(e_delete);
        mEdit.addSeparator();
        mEdit.add(e_select_all);

        // Submeniuri din NETWORK (fostul DATA):
        d_title = new JMenuItem("Title...");
        d_add_nodes = new JMenuItem("Add Nodes...");
        d_delete_nodes = new JMenuItem("Delete Selected Node");
        d_add_scalar = new JMenuItem("Add Scalar...");
        d_multiply_scalar = new JMenuItem("Scalar Multiplication...");
        d_transpose = new JMenuItem("Transpose");
        d_symmetrize = new JMenuItem("Symmetrize...");
        d_normalize = new JMenuItem("Binarize");
        d_remove_out = new JMenuItem("Remove Outsiders");
        d_renumber_nodes = new JMenuItem("Renumber Nodes...");
        d_merge = new JMenuItem("Merge Network...");
        d_multiply_network = new JMenuItem("Square Matrix");
        d_boolean_multiplication = new JMenuItem("Boolean Multiplication...");

        d_title.setToolTipText("Change current network's title");
        d_add_nodes.setToolTipText("Add new nodes to current network");
        d_delete_nodes
                .setToolTipText("Delete the node corresponding to selected cell's column");
        d_add_scalar.setToolTipText("Add a scalar value to sociomatrix");
        d_multiply_scalar
                .setToolTipText("Multiply sociomatrix by a scalar value");
        d_transpose.setToolTipText("Transpose current network's sociomatrix");
        d_symmetrize.setToolTipText("Symmetrize current network's sociomatrix");
        d_normalize
                .setToolTipText("Replace all non-zero values in sociomatrix by 1");
        d_remove_out
                .setToolTipText("Remove all isolated nodes from current network");
        d_renumber_nodes.setToolTipText("Give all nodes number-names");
        d_multiply_network.setToolTipText("Multiply sociomatrix by itself");

        d_add_scalar.addActionListener(act_menu);
        d_title.addActionListener(act_menu);
        d_add_nodes.addActionListener(act_menu);
        d_delete_nodes.addActionListener(act_menu);
        d_renumber_nodes.addActionListener(act_menu);
        d_multiply_scalar.addActionListener(act_menu);
        d_transpose.addActionListener(act_menu);
        d_symmetrize.addActionListener(act_menu);
        d_normalize.addActionListener(act_menu);
        d_remove_out.addActionListener(act_menu);
        d_merge.addActionListener(act_menu);
        d_multiply_network.addActionListener(act_menu);
        d_boolean_multiplication.addActionListener(act_menu);

        d_title.setMnemonic('n');
        d_add_scalar.setMnemonic('l');
        d_add_nodes.setMnemonic('a');
        d_delete_nodes.setMnemonic('d');
        d_multiply_scalar.setMnemonic('m');
        d_transpose.setMnemonic('t');
        d_symmetrize.setMnemonic('s');
        d_normalize.setMnemonic('b');
        d_remove_out.setMnemonic('r');
        d_renumber_nodes.setMnemonic('o');
        d_merge.setMnemonic('e');
        d_multiply_network.setMnemonic('u');
        d_boolean_multiplication.setMnemonic('q');
        d_add_nodes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                ActionEvent.CTRL_MASK));
        d_add_scalar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                ActionEvent.CTRL_MASK));
        d_multiply_scalar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                ActionEvent.CTRL_MASK));
        d_normalize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
                ActionEvent.CTRL_MASK));
        d_transpose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                ActionEvent.CTRL_MASK));
        d_symmetrize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                ActionEvent.CTRL_MASK));

        mData.add(d_title);
        mData.addSeparator();
        mData.add(d_add_nodes);
        mData.add(d_delete_nodes);
        mData.addSeparator();
        mData.add(d_renumber_nodes);
        mData.add(d_remove_out);
        mData.addSeparator();
        mData.add(d_add_scalar);
        mData.add(d_multiply_scalar);
        mData.addSeparator();
        mData.add(d_transpose);
        mData.add(d_symmetrize);
        mData.add(d_normalize);
        mData.addSeparator();
        // mData.add(d_merge);
        mData.add(d_multiply_network);
        // mData.add(d_boolean_multiplication);

        // Submeniuri din ANALYSIS:
        a_basic = new JMenuItem("Basic Description");
        a_sociometrics = new JMenu("Sociometrics");
        a_nodal_degree = new JMenuItem("Nodal Degree");
        a_indegree = new JMenuItem("Indegree");
        a_outdegree = new JMenuItem("Outdegree");
        a_density = new JMenuItem("Density");
        a_cohesion = new JMenuItem("Cohesion");
        a_emissions = new JMenuItem("Emission Degree");
        a_receptions = new JMenuItem("Reception Degree");
        a_determinations = new JMenuItem("Determination Degree");
        a_sociostatus = new JMenuItem("Sociometric Status");
        a_distance = new JMenu("Distance");
        a_eccentricity = new JMenuItem("Eccentricity");
        a_diameter = new JMenuItem("Diameter");
        a_geodesics = new JMenuItem("Geodesic Matrix");
        a_shortest_paths = new JMenuItem("Shortest Paths...");
        a_all_shortest_paths = new JMenuItem("All Shortest Paths");
        a_cliques = new JMenuItem("N-Cliques");
        a_centrality = new JMenu("Centrality");
        a_bavelas = new JMenuItem("Bavelas-Leavitt");
        a_closeness = new JMenuItem("Closeness");
        a_fareness = new JMenuItem("Fareness");
        a_betweenness = new JMenuItem("Betweenness");
        a_prestige = new JMenuItem("Prestige");
        a_full_analysis = new JMenuItem("Full Analysis");

        a_basic.setToolTipText("Give a short description of current network");
        a_sociometrics.setToolTipText("Sociometric coefficients");
        a_nodal_degree.setToolTipText("Compute the nodal degree of each node");
        a_indegree.setToolTipText("Compute indegree for each node");
        a_outdegree.setToolTipText("Compute outdegree for each node");
        a_density.setToolTipText("Compute the density of current network");
        a_cohesion
                .setToolTipText("Compute the cohesion index of current network");
        a_emissions.setToolTipText("Compute emission degree for each node");
        a_receptions.setToolTipText("Compute reception degree for each node");
        a_determinations
                .setToolTipText("Compute determination degree for each node");
        a_sociostatus
                .setToolTipText("Compute sociometric status for each node");
        a_distance.setToolTipText("Distance-related analysis");
        a_eccentricity.setToolTipText("Compute eccentricity for each node");
        a_diameter.setToolTipText("Compute current network's diameter");
        a_geodesics
                .setToolTipText("Compute current network's matrix of geodesic distances");
        a_shortest_paths
                .setToolTipText("Find shortest paths between two nodes");
        a_all_shortest_paths
                .setToolTipText("Find shortest paths for all pairs of nodes");
        // a_cliques.setToolTipText("Find n-cliques in current network");
        a_centrality.setToolTipText("Centrality-related coefficients");
        a_bavelas
                .setToolTipText("Compute Bavelas-Leavitt coefficient for each node");
        a_closeness
                .setToolTipText("Compute closeness coefficient for each node");
        a_fareness.setToolTipText("Compute fareness coefficient for each node");
        a_betweenness
                .setToolTipText("Compute betweenness  coefficient for each node");
        // a_prestige.setToolTipText("Compute prestige coefficient for each
        // node");

        a_emissions.addActionListener(act_menu);
        a_receptions.addActionListener(act_menu);
        a_determinations.addActionListener(act_menu);
        a_sociostatus.addActionListener(act_menu);
        a_basic.addActionListener(act_menu);
        a_nodal_degree.addActionListener(act_menu);
        a_indegree.addActionListener(act_menu);
        a_outdegree.addActionListener(act_menu);
        a_density.addActionListener(act_menu);
        a_cohesion.addActionListener(act_menu);
        a_eccentricity.addActionListener(act_menu);
        a_diameter.addActionListener(act_menu);
        a_geodesics.addActionListener(act_menu);
        a_shortest_paths.addActionListener(act_menu);
        a_all_shortest_paths.addActionListener(act_menu);
        a_cliques.addActionListener(act_menu);
        a_bavelas.addActionListener(act_menu);
        a_closeness.addActionListener(act_menu);
        a_fareness.addActionListener(act_menu);
        a_betweenness.addActionListener(act_menu);
        a_prestige.addActionListener(act_menu);
        a_full_analysis.addActionListener(act_menu);

        a_basic.setMnemonic('b');
        a_distance.setMnemonic('d');
        a_diameter.setMnemonic('d');
        a_eccentricity.setMnemonic('e');
        a_geodesics.setMnemonic('g');
        a_shortest_paths.setMnemonic('s');
        a_all_shortest_paths.setMnemonic('a');
        // a_cliques.setMnemonic('n');
        a_sociometrics.setMnemonic('s');
        a_outdegree.setMnemonic('o');
        a_nodal_degree.setMnemonic('n');
        a_indegree.setMnemonic('i');
        a_density.setMnemonic('t');
        a_cohesion.setMnemonic('c');
        a_emissions.setMnemonic('e');
        a_receptions.setMnemonic('r');
        a_determinations.setMnemonic('d');
        a_sociostatus.setMnemonic('s');
        a_centrality.setMnemonic('c');
        a_bavelas.setMnemonic('b');
        a_closeness.setMnemonic('c');
        a_fareness.setMnemonic('f');
        a_betweenness.setMnemonic('w');
        // a_prestige.setMnemonic('p');
        a_basic.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
                ActionEvent.ALT_MASK));
        a_diameter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                ActionEvent.ALT_MASK));
        a_geodesics.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                ActionEvent.ALT_MASK));
        a_all_shortest_paths.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, ActionEvent.ALT_MASK));
        // a_cliques.setAccelerator (KeyStroke.getKeyStroke(KeyEvent.VK_Q,
        // ActionEvent.ALT_MASK));
        a_density.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
                ActionEvent.ALT_MASK));
        a_cohesion.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                ActionEvent.ALT_MASK));
        a_sociostatus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                ActionEvent.ALT_MASK));
        a_bavelas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                ActionEvent.ALT_MASK));
        a_betweenness.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                ActionEvent.ALT_MASK));

        a_sociometrics.add(a_nodal_degree);
        a_sociometrics.add(a_outdegree);
        a_sociometrics.add(a_indegree);
        a_sociometrics.addSeparator();
        a_sociometrics.add(a_density);
        a_sociometrics.add(a_cohesion);
        a_sociometrics.addSeparator();
        a_sociometrics.add(a_emissions);
        a_sociometrics.add(a_receptions);
        a_sociometrics.add(a_determinations);
        a_sociometrics.add(a_sociostatus);

        a_distance.add(a_diameter);
        a_distance.add(a_eccentricity);
        a_distance.addSeparator();
        a_distance.add(a_geodesics);
        a_distance.addSeparator();
        a_distance.add(a_shortest_paths);
        a_distance.add(a_all_shortest_paths);
        // a_distance.addSeparator();
        // a_distance.add(a_cliques);

        a_centrality.add(a_bavelas);
        a_centrality.addSeparator();
        a_centrality.add(a_closeness);
        a_centrality.add(a_fareness);
        a_centrality.addSeparator();
        a_centrality.add(a_betweenness);
        a_centrality.addSeparator();
        a_centrality.add(a_prestige);

        mAnalysis.add(a_basic);
        mAnalysis.addSeparator();
        mAnalysis.add(a_distance);
        mAnalysis.add(a_cliques);
        mAnalysis.add(a_sociometrics);
        mAnalysis.add(a_centrality);
        // mAnalysis.addSeparator();
        mAnalysis.add(a_full_analysis);

        // Submeniuri din VIEW:
        v_viewer = new JMenuItem("Network Viewer");
        v_close = new JMenuItem("Close Network Viewer");
        v_hide_output = new JMenuItem("Hide Output");
        v_view_output = new JMenuItem("Show Output");

        v_viewer.setToolTipText("Display Network Viewer frame");
        v_close.setToolTipText("Close Network Viewer frame");

        v_close.setEnabled(false);

        v_viewer.addActionListener(act_menu);
        v_close.addActionListener(act_menu);
        v_hide_output.addActionListener(act_menu);
        v_view_output.addActionListener(act_menu);

        v_viewer.setMnemonic('v');
        v_close.setMnemonic('c');
        v_hide_output.setMnemonic('i');
        v_view_output.setMnemonic('o');
        v_viewer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                ActionEvent.CTRL_MASK));

        mView.add(v_viewer);
        mView.add(v_close);
        // mView.addSeparator();
        // mView.add(v_hide_output);
        // mView.add(v_view_output);

        // submeniuri din OUTPUT: -- am renuntat la el; trecute in FILE
        o_open = new JMenuItem("Open Output File");
        o_save = new JMenuItem("Save Output As...");
        o_simply_save = new JMenuItem("Save Output");
        o_clear = new JMenuItem("Clear Output");

        o_open.setToolTipText("Open existing output file");
        o_save.setToolTipText("Save current output as specified file");
        o_simply_save.setToolTipText("Save current output");
        o_clear.setToolTipText("Clear all content of current output area");

        o_open.setMnemonic('p');
        o_save.setMnemonic('v');
        o_simply_save.setMnemonic('e');
        o_clear.setMnemonic('e');
        o_simply_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                ActionEvent.CTRL_MASK));

        o_open.addActionListener(act_menu);
        o_save.addActionListener(act_menu);
        o_simply_save.addActionListener(act_menu);
        o_clear.addActionListener(act_menu);

        mFile.add(o_open); // !!
        mFile.add(o_save);
        mFile.add(o_simply_save);
        mFile.addSeparator();
        mFile.add(f_quit);

        mEdit.addSeparator();
        mEdit.add(o_clear);

        // submeniuri din PREFERENCES:
        p_working_directory = new JMenuItem("Working Directory...");
        p_save_as_default = new JMenuItem("Save Settings As Default...");
        p_working_directory.setToolTipText("Choose default working directory");

        p_working_directory.addActionListener(act_menu);
        p_working_directory.setMnemonic('w');
        p_save_as_default.addActionListener(act_menu);
        mPreferences.add(p_working_directory);
        // mPreferences.add(p_save_as_default);

        // submeniuri din HELP:
        h_contents = new JMenuItem("Contents...");
        h_about_agna = new JMenuItem("About Agna...");
        h_contents.setToolTipText("Display the Agna Help frame");
        h_about_agna.setToolTipText("Display Agna copyright information");

        h_contents.addActionListener(act_menu);
        h_about_agna.addActionListener(act_menu);
        h_contents.setMnemonic('c');
        h_about_agna.setMnemonic('a');
        h_contents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                ActionEvent.CTRL_MASK));
        mHelp.add(h_contents);
        mHelp.add(h_about_agna);

        //

        mb = new JMenuBar();
        mb.add(mFile);
        // mb.add(mOutput);
        mb.add(mEdit);
        mb.add(mData);
        mb.add(mAnalysis);
        mb.add(mView);
        mb.add(mPreferences);
        mb.add(mHelp);
        my_frame.setJMenuBar(mb);

        // toolbar: ***********************
        tools = new JToolBar();
        tools.setFloatable(false);
        dim_tool = new Dimension(23, 23);
        ImageIcon i_new_network = Environment
                .getButtonImageIcon("NewNetwork.gif");
        i_open_network = Environment.getButtonImageIcon("OpenNetwork.gif");
        ImageIcon i_save_network = Environment
                .getButtonImageIcon("SaveNetwork.gif");
        i_network_viewer = Environment.getButtonImageIcon("NetworkViewer.gif");
        ImageIcon i_open_output = Environment
                .getButtonImageIcon("OpenOutput.gif");
        ImageIcon i_clear_output = Environment
                .getButtonImageIcon("ClearOutput.gif");
        ImageIcon i_save_output = Environment
                .getButtonImageIcon("SaveOutput.gif");
        ImageIcon i_transpose = Environment.getButtonImageIcon("Transpose.gif");
        ImageIcon i_symmetrize = Environment
                .getButtonImageIcon("Symmetrize.gif");

        ImageIcon ir_new_network = Environment
                .getButtonImageIcon("rNewNetwork.gif");
        ImageIcon ir_open_network = Environment
                .getButtonImageIcon("rOpenNetwork.gif");
        ir_save_network = Environment.getButtonImageIcon("rSaveNetwork.gif");
        ir_network_viewer = Environment
                .getButtonImageIcon("rNetworkViewer.gif");
        ImageIcon ir_open_output = Environment
                .getButtonImageIcon("rOpenOutput.gif");
        ImageIcon ir_clear_output = Environment
                .getButtonImageIcon("rClearOutput.gif");
        ImageIcon ir_save_output = Environment
                .getButtonImageIcon("rSaveOutput.gif");
        ImageIcon ir_transpose = Environment
                .getButtonImageIcon("rTranspose.gif");
        ImageIcon ir_symmetrize = Environment
                .getButtonImageIcon("rSymmetrize.gif");

        tool_new_network = new JButton(Environment
                .getButtonImageIcon("NewNetwork.gif"));
        tool_open_network = new JButton(Environment
                .getButtonImageIcon("OpenNetwork.gif"));
        tool_new_from_chain = new JButton(Environment
                .getButtonImageIcon("NewFromChain.gif"));
        tool_save_network = new JButton(Environment
                .getButtonImageIcon("SaveNetwork.gif"));
        tool_viewer = new JButton(Environment
                .getButtonImageIcon("NetworkViewer.gif"));
        tool_open_output = new JButton(Environment
                .getButtonImageIcon("OpenOutput.gif"));
        tool_clear_output = new JButton(i_clear_output);
        tool_save_output = new JButton(i_save_output);
        tool_transpose = new JButton(i_transpose);
        tool_symmetrize = new JButton(i_symmetrize);
        tool_renumber_nodes = new JButton(Environment
                .getButtonImageIcon("RenumberNodes.gif"));

        tool_new_network.setRolloverIcon(Environment
                .getButtonImageIcon("rNewNetwork.gif"));
        tool_open_network.setRolloverIcon(ir_open_network);
        tool_new_from_chain.setRolloverIcon(Environment
                .getButtonImageIcon("rNewFromChain.gif"));
        tool_save_network.setRolloverIcon(ir_save_network);
        tool_viewer.setRolloverIcon(ir_network_viewer);
        tool_open_output.setRolloverIcon(ir_open_output);
        tool_clear_output.setRolloverIcon(ir_clear_output);
        tool_save_output.setRolloverIcon(ir_save_output);
        tool_transpose.setRolloverIcon(ir_transpose);
        tool_symmetrize.setRolloverIcon(ir_symmetrize);
        tool_renumber_nodes.setRolloverIcon(Environment
                .getButtonImageIcon("rRenumberNodes.gif"));

        tool_new_network.setBorder(null);
        tool_save_network.setBorder(null);
        tool_open_network.setBorder(null);
        tool_new_from_chain.setBorder(null);
        tool_viewer.setBorder(null);
        tool_open_output.setBorder(null);
        tool_clear_output.setBorder(null);
        tool_save_output.setBorder(null);
        tool_transpose.setBorder(null);
        tool_symmetrize.setBorder(null);
        tool_renumber_nodes.setBorder(null);

        tool_new_network.setPreferredSize(dim_tool);
        tool_save_network.setPreferredSize(dim_tool);
        tool_open_network.setPreferredSize(dim_tool);
        tool_new_from_chain.setPreferredSize(dim_tool);
        tool_viewer.setPreferredSize(dim_tool);
        tool_open_output.setPreferredSize(dim_tool);
        tool_clear_output.setPreferredSize(dim_tool);
        tool_save_output.setPreferredSize(dim_tool);
        tool_transpose.setPreferredSize(dim_tool);
        tool_symmetrize.setPreferredSize(dim_tool);
        tool_renumber_nodes.setPreferredSize(dim_tool);
        tool_new_network.setMaximumSize(dim_tool);
        tool_save_network.setMaximumSize(dim_tool);
        tool_open_network.setMaximumSize(dim_tool);
        tool_new_from_chain.setMaximumSize(dim_tool);
        tool_viewer.setMaximumSize(dim_tool);
        tool_open_output.setMaximumSize(dim_tool);
        tool_clear_output.setMaximumSize(dim_tool);
        tool_save_output.setMaximumSize(dim_tool);
        tool_transpose.setMaximumSize(dim_tool);
        tool_symmetrize.setMaximumSize(dim_tool);
        tool_renumber_nodes.setMaximumSize(dim_tool);

        tool_new_network.addActionListener(act_menu);
        tool_save_network.addActionListener(act_menu);
        tool_open_network.addActionListener(act_menu);
        tool_new_from_chain.addActionListener(act_menu);
        tool_viewer.addActionListener(act_menu);
        tool_open_output.addActionListener(act_menu);
        tool_clear_output.addActionListener(act_menu);
        tool_save_output.addActionListener(act_menu);
        tool_transpose.addActionListener(act_menu);
        tool_symmetrize.addActionListener(act_menu);
        tool_renumber_nodes.addActionListener(act_menu);

        tool_new_network.setToolTipText("Create new network");
        tool_save_network.setToolTipText("Save current network");
        tool_open_network.setToolTipText("Open existing network");
        tool_new_from_chain
                .setToolTipText("Create new network from chain file");
        tool_viewer.setToolTipText("Network Viewer");
        tool_open_output.setToolTipText("Open existing output file");
        tool_clear_output.setToolTipText("Clear output area");
        tool_save_output.setToolTipText("Save current output");
        tool_transpose.setToolTipText("Transpose current network");
        tool_symmetrize.setToolTipText("Symmetrize current network");
        tool_renumber_nodes.setToolTipText("Renumber nodes");

        tools.add(tool_new_network);
        tools.add(tool_open_network);
        tools.add(tool_new_from_chain);
        tools.add(tool_save_network);
        tools.addSeparator();
        tools.add(tool_viewer);
        tools.addSeparator();
        tools.add(tool_renumber_nodes);
        tools.add(tool_transpose);
        tools.add(tool_symmetrize);
        tools.addSeparator();
        tools.add(tool_open_output);
        tools.add(tool_clear_output);
        tools.add(tool_save_output);
        // tools.addSeparator();

        JComboBox font_family_combo = makeFontFamilyCombo(output_edit);
        if (font_family_combo != null)
            tools.add(font_family_combo);
        // tools.add(makeFontSizeCombo(output_edit));

        JPanel tools_panel = new JPanel();
        tools_panel.setLayout(new BorderLayout());
        tools_panel.add(tools, BorderLayout.WEST);

        controlArea.add(tools_panel, BorderLayout.NORTH);
        my_frame.pack();
        my_frame.setVisible(true);
        my_full_net.setChanged(false);
        }
    }