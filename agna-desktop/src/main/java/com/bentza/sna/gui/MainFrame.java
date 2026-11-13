/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.core.AppRuntime;
import com.bentza.sna.io.HTMLParser;
import com.bentza.sna.io.IOUtils;
import com.bentza.sna.io.ExcelExporter;
import com.bentza.sna.io.GMLExporter;
import com.bentza.sna.io.GraphMLExporter;
import com.bentza.sna.io.GraphSONExporter;
import com.bentza.sna.io.PajekExporter;
import com.bentza.sna.net.*;
import com.bentza.sna.report.CentralityOptions;
import com.bentza.sna.report.ClusteringOptions;
import com.bentza.sna.report.DistanceOptions;
import com.bentza.sna.report.FullAnalysisOptions;
import com.bentza.sna.report.SociometricsOptions;
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

public class MainFrame //
    {

    private static JFrame my_frame;

    private static HelpDialog h_dialog;

    private static AboutBox about_box;


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
            mPreferences;

    private static JMenuItem f_new, f_open, f_save, f_simply_save, f_create,
            f_quit, o_open, o_save, o_simply_save, o_clear, e_cut, e_copy,
            e_paste, e_delete, e_select_all, d_title, d_add_nodes,
            d_delete_nodes, d_add_scalar, d_multiply_scalar, d_transpose,
            d_symmetrize, d_normalize, d_remove_out, d_renumber_nodes, d_merge,
            d_multiply_network, d_dichotomize, d_zero_diagonal,
            d_set_diagonal, d_ego_network, d_network_algebra, d_sort_actors, a_basic,
            a_nodal_degree, a_indegree, a_outdegree, a_density, a_cohesion,
            a_emissions, a_receptions, a_determinations, a_sociostatus,
            a_eccentricity, a_diameter, a_geodesics, a_shortest_paths,
            a_all_shortest_paths, a_cliques, a_local_clustering, a_transitivity,
            a_kcore, a_triad_census, a_structural_holes, a_connectivity,
            a_bavelas, a_closeness, a_fareness, a_betweenness, a_prestige,
            a_eigenvector, a_bonacich, a_pagerank, a_full_analysis,
            a_centrality, a_sociometrics, a_distance, a_clustering,
            a_archetypes, a_hypothesis_testing, a_benchmarks, a_export_table, o_export_table, v_viewer,
            v_close, v_hide_output, v_view_output, p_working_directory,
            p_save_as_default, p_laf_native, p_laf_flatlaf,
            p_laf_flatlaf_dark, h_contents, h_about_agna,
            f_samples, a_advisor, h_cheat_sheet, h_samples, h_advisor;

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

    // 2.1.3: last used export format (0 agn, 1 txt, 2 csv, 3 net, 4 xls),
    // persisted in AgnaDefaultSettings.ini

    public static int getRememberedExportFormat()
        {
        return AppRuntime.getRememberedExportFormat();
        }

    public static void setRememberedExportFormat(int tmp_format)
        {
        AppRuntime.setRememberedExportFormat(tmp_format);
        }
    // 2.1.3: whether Pajek exports append the per-node *Vector blocks;
    // persisted so a Gephi-compatible (no-vector) choice sticks

    public static boolean isPajekVectorsEnabled()
        {
        return AppRuntime.isPajekVectorsEnabled();
        }

    public static void setPajekVectorsEnabled(boolean tmp_enabled)
        {
        AppRuntime.setPajekVectorsEnabled(tmp_enabled);
        }


    private JTextField edit_cell;

    private DefaultCellEditor grid_editor;

    private String str;

    private File net_file;

    private static JLabel status_bar;



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
                MainFrame.setCurrentStatus(MainFrame.default_status);
                my_full_net.setChanged(true);
                }

            public void editingCanceled(ChangeEvent e)
                {
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

                // exports analysis table
                if (e.getSource() == o_export_table
                        || e.getSource() == a_export_table)
                    {
                    if (output_edit != null)
                        {
                        for (com.bentza.sna.report.TableData td : com.bentza.sna.report.TableRegistry.getRecentTables())
                            {
                            output_edit.syncTableFromDocument(td);
                            }
                        }
                    ExportTableDialog.showDialog(my_frame, null);
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

                if (e.getSource() == a_sociometrics)
                    {
                    doSociometricsBattery();
                    }

                if (e.getSource() == a_centrality)
                    {
                    doCentralityBattery();
                    }

                if (e.getSource() == a_distance)
                    {
                    doDistanceBattery();
                    }

                if (e.getSource() == a_clustering)
                    {
                    doClusteringBattery();
                    }

                if (e.getSource() == a_archetypes)
                    {
                    doArchetypeDiagnostic();
                    }

                if (e.getSource() == a_hypothesis_testing)
                    {
                    doHypothesisTesting();
                    }

                if (e.getSource() == a_benchmarks)
                    {
                    doAlgorithmBenchmark();
                    }

                if (e.getSource() == a_local_clustering)
                    {
                    doLocalClustering();
                    }

                if (e.getSource() == a_transitivity)
                    {
                    doTransitivity();
                    }

                if (e.getSource() == a_kcore)
                    {
                    doKCore();
                    }

                if (e.getSource() == a_triad_census)
                    {
                    doTriadCensus();
                    }

                if (e.getSource() == a_structural_holes)
                    {
                    doStructuralHoles();
                    }

                if (e.getSource() == a_connectivity)
                    {
                    doConnectivity();
                    }

                if (e.getSource() == a_eigenvector)
                    {
                    doEigenvector();
                    }

                if (e.getSource() == a_bonacich)
                    {
                    doBonacich();
                    }

                if (e.getSource() == a_pagerank)
                    {
                    doPageRank();
                    }

                if (e.getSource() == d_dichotomize)
                    {
                    doDichotomize();
                    }

                if (e.getSource() == d_zero_diagonal)
                    {
                    doZeroDiagonal();
                    }

                if (e.getSource() == d_set_diagonal)
                    {
                    doSetDiagonal();
                    }

                if (e.getSource() == d_ego_network)
                    {
                    doExtractEgo();
                    }

                if (e.getSource() == d_network_algebra)
                    {
                    doNetworkAlgebra();
                    }

                if (e.getSource() == d_sort_actors)
                    {
                    doSortActors();
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

                // 2.1.3: toggles the visibility of the analysis output
                if (e.getSource() == v_hide_output)
                    {
                    if (output_edit != null)
                        {
                        output_edit.setVisible(false);
                        }
                    my_frame.validate();
                    my_frame.repaint();
                    }
                if (e.getSource() == v_view_output)
                    {
                    if (output_edit != null)
                        {
                        output_edit.setVisible(true);
                        }
                    my_frame.validate();
                    my_frame.repaint();
                    }

                if (e.getSource() == p_save_as_default)
                    {
                    doSaveAsDefaultSettings();
                    }

                // sets working directory preference:
                if (e.getSource() == p_laf_native)
                    {
                    chooseLookAndFeel("system");
                    } else if (e.getSource() == p_laf_flatlaf)
                    {
                    chooseLookAndFeel("flatlaf");
                    } else if (e.getSource() == p_laf_flatlaf_dark)
                    {
                    chooseLookAndFeel("flatlaf-dark");
                    }

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

                if (e.getSource() == f_samples || e.getSource() == h_samples)
                    {
                    doOpenSamples();
                    }

                if (e.getSource() == a_advisor || e.getSource() == h_advisor)
                    {
                    doAnalysisAdvisor();
                    }

                if (e.getSource() == h_cheat_sheet)
                    {
                    doSnaCheatSheet();
                    }

                if (e.getSource() == h_about_agna)
                    {
                    // tring about_text = "<html>" +
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
        int currentSize = getCurrentNetwork() != null ? getCurrentNetwork().getSize() : 10;
        grid_model.setReady(false);

        NewNetworkDialog dialog = new NewNetworkDialog(my_frame, currentSize);
        dialog.setVisible(true);

        Network created = dialog.getGeneratedNetwork();
        if (created == null)
            {
            return;
            }

        saveChangedNetworks(true);

        boolean wasViewerOpen = (my_grafic != null && my_grafic.getCurrentFrame() != null
                && my_grafic.getCurrentFrame().isVisible());
        if (my_grafic != null)
            {
            if (my_grafic.getCurrentFrame() != null)
                my_grafic.getCurrentFrame().dispose();
            my_grafic = null;
            }

        FullNet new_full_net = new FullNet();
        new_full_net.setNetwork(created);
        new_full_net.attachArea();

        my_full_net.cleaning();
        my_full_net = new_full_net;
        setTableCellEditor();
        updateMatrix();

        refreshFrameTitle();

        appendOutputLog("Created a new " + created.getName() + " with "
                + created.getSize() + " nodes.");
        my_full_net.setChanged(false);
        my_frame.validate();
        my_frame.repaint();

        if (wasViewerOpen)
            {
            view_graph();
            }
        }

    public void saveNetwork(FullNet tmp_full_net, String tmp_file_name)
        {
        AgnaLog.info("saveNetwork: writing '" + tmp_file_name + "'");
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
            } else if (filestr.equals("gml"))
            {
            // GML: the classic igraph/NetworkX/Gephi text format
            GMLExporter gml_exporter = new GMLExporter();
            writestr = gml_exporter.getGML(tmp_full_net);
            } else if (filestr.equals("graphson"))
            {
            // GraphSON: the JSON graph format (Jackson, Apache-2.0)
            GraphSONExporter graphson_exporter = new GraphSONExporter();
            writestr = graphson_exporter.getGraphSON(tmp_full_net);
            } else if (filestr.equals("graphml"))
            {
            // GraphML: the standard SNA exchange format (Gephi, NetworkX,
            // igraph, R SNA packages)
            GraphMLExporter graphml_exporter = new GraphMLExporter();
            writestr = graphml_exporter.getGraphML(tmp_full_net);
            } else if (filestr.equals("dl"))
            {
            // UCINET DL format
            try
                {
                com.bentza.sna.io.DLExporter.exportDL(tmp_full_net.getNetwork(), new File(file_name));
                return;
                } catch (Exception ex)
                {
                AgnaLog.warn("UCINET DL export failed: " + ex);
                }
            } else
            {
            // agn file
            writestr = tmp_full_net.getAgna2TextNetwork();
            }

        try
            {
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
        appendOutputLog("Saved network to '" + tmp_file_name + "'.");
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

        // setting name of file. 2.1.3: self-healing - a missing, directory
        // like, or orphaned (parent missing) stored name falls back to the
        // network's own name, so stale junk names cannot stick around
        String network_file_name = my_full_net.getNetworkFileName();
        boolean stored_name_usable = network_file_name != null
                && network_file_name.length() > 0
                && !network_file_name.endsWith(java.io.File.separator)
                && !(new java.io.File(network_file_name).isDirectory());
        java.io.File stored_parent = stored_name_usable ? new java.io.File(
                network_file_name).getParentFile() : null;

        java.io.File chooser_dir = null;
        if (stored_name_usable && stored_parent != null && stored_parent.exists())
            {
            chooser_dir = stored_parent;
            file = new File(network_file_name);
            } else
            {
            chooser_dir = Environment.getDefaultOpenDirectory();
            String net_name = (my_full_net.getNetwork() != null
                    && my_full_net.getNetwork().getName() != null
                    && my_full_net.getNetwork().getName().length() > 0) ? my_full_net
                    .getNetwork().getName() : "network";
            file = new File(chooser_dir, net_name + ".agn");
            }
        AgnaLog.info("Save As: stored name '" + network_file_name
                + "' -> using '" + file.getAbsolutePath() + "'");

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String stripped_name = IOUtils.getNameWithoutExtension(file.getName());
        if (stripped_name == null || stripped_name.length() == 0)
            {
            stripped_name = "network";
            }
        final String defaultBaseName = stripped_name;
        java.io.File selection = new File(chooser_dir, defaultBaseName + ".agn");
        chooser.setCurrentDirectory(chooser_dir);
        chooser.setSelectedFile(selection);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Type file name and click here");

        chooser.setDialogTitle("Save Network As...");
        // Default to Agna AGN format and allow switching to other formats
        AgnaFilesFilter agnaFilter = new AgnaFilesFilter();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(agnaFilter);
        chooser.addChoosableFileFilter(new PajekFilesFilter());
        chooser.addChoosableFileFilter(new CommaTextFilesFilter());
        chooser.addChoosableFileFilter(new TabTextFilesFilter());
        chooser.addChoosableFileFilter(new ExcelFilesFilter());
        chooser.addChoosableFileFilter(new GraphMLFilesFilter());
        chooser.addChoosableFileFilter(new GMLFilesFilter());
        chooser.addChoosableFileFilter(new GraphSONFilesFilter());
        chooser.addChoosableFileFilter(new DLFilesFilter());
        chooser.setFileFilter(agnaFilter);

        // Dynamically update file extension in the name field when format filter changes
        chooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, evt ->
            {
            javax.swing.filechooser.FileFilter newFf = (javax.swing.filechooser.FileFilter) evt.getNewValue();
            if (newFf == null)
                return;
            String newExt = null;
            if (newFf instanceof AgnaFilesFilter)
                newExt = "agn";
            else if (newFf instanceof PajekFilesFilter)
                newExt = "net";
            else if (newFf instanceof CommaTextFilesFilter)
                newExt = "csv";
            else if (newFf instanceof TabTextFilesFilter)
                newExt = "txt";
            else if (newFf instanceof ExcelFilesFilter)
                newExt = "xlsx";
            else if (newFf instanceof GraphMLFilesFilter)
                newExt = "graphml";
            else if (newFf instanceof GMLFilesFilter)
                newExt = "gml";
            else if (newFf instanceof GraphSONFilesFilter)
                newExt = "graphson";
            else if (newFf instanceof DLFilesFilter)
                newExt = "dl";

            if (newExt != null)
                {
                File cur = chooser.getSelectedFile();
                String base = (cur != null && cur.getName() != null && !cur.getName().isEmpty())
                        ? IOUtils.getNameWithoutExtension(cur.getName()) : defaultBaseName;
                if (base == null || base.isEmpty())
                    base = "network";
                File parent = (cur != null && cur.getParentFile() != null)
                        ? cur.getParentFile() : chooser.getCurrentDirectory();
                chooser.setSelectedFile(new File(parent, base + "." + newExt));
                }
            });
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

        // 2.1.3: on some platforms the approved selection can come back as a
        // stale absolute path (e.g. from a previous session's file); keep the
        // name inside the chooser's current directory
        java.io.File approved_dir = chooser.getCurrentDirectory();
        java.io.File approved_file = new java.io.File(filename);
        if (approved_dir != null && approved_file.isAbsolute()
                && approved_file.getParentFile() != null
                && !approved_file.getParentFile().equals(approved_dir))
            {
            AgnaLog.warn("Save As: approved path outside current directory ("
                    + filename + "); rebuilding inside " + approved_dir);
            filename = new java.io.File(approved_dir,
                    approved_file.getName()).getAbsolutePath();
            }
        AgnaLog.info("Save As approved: directory="
                + (approved_dir != null ? approved_dir.getAbsolutePath()
                        : "null") + " file=" + filename);

        // 2.1.3: reliable, platform-independent format choice: one options
        // dialog selects the format (and, for Pajek, the vectors); the
        // extension is appended here so the saved file always matches the
        // visible choice
        // 2.1.3: an explicit non-Pajek format filter saves directly, with no
        // options dialog; Pajek and "All Files" open the format dialog
        javax.swing.filechooser.FileFilter ff = chooser.getFileFilter();
        int chosen_format = -1;
        final String[] format_extensions = { "agn", "txt", "csv", "net",
                "xls", "graphml", "gml", "graphson", "dl" };
        if (ff instanceof AgnaFilesFilter)
            chosen_format = 0;
        else if (ff instanceof TabTextFilesFilter)
            chosen_format = 1;
        else if (ff instanceof CommaTextFilesFilter)
            chosen_format = 2;
        else if (ff instanceof ExcelFilesFilter)
            chosen_format = 4;
        else if (ff instanceof GraphMLFilesFilter)
            chosen_format = 5;
        else if (ff instanceof GMLFilesFilter)
            chosen_format = 6;
        else if (ff instanceof GraphSONFilesFilter)
            chosen_format = 7;
        else if (ff instanceof DLFilesFilter)
            chosen_format = 8;
        if (chosen_format < 0)
            {
            int default_format = getRememberedExportFormat();
            if (ff instanceof PajekFilesFilter)
                {
                default_format = 3;
                }
            chosen_format = askExportFormatDialog(default_format);
            if (chosen_format < 0)
                {
                grid_model.setReady(true);
                my_frame.validate();
                my_frame.repaint();
                return;
                }
            }
        setRememberedExportFormat(chosen_format);
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
        AgnaLog.info("Save As: thread target = " + thread_filename);

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

            refreshFrameTitle();

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

                        String file_ext = IOUtils.getExtension(t_file
                                .getName());
                        String excel_error = null;
                        if ("xls".equals(file_ext) || "xlsx".equals(file_ext))
                            {
                            // 2.1.3: binary Excel needs the file itself
                            excel_error = t_new_full_net.readExcelFile(t_file,
                                    file_ext);
                            tmp_pane = null;
                            } else if ("dl".equals(file_ext))
                            {
                            Network parsedDL = com.bentza.sna.io.DLImporter.importDL(t_file);
                            t_new_full_net.setNetwork(parsedDL);
                            tmp_pane = null;
                            } else
                            {
                            t_new_full_net.readNetwork(
                                    tmp_pane == null ? ""
                                            : tmp_pane.getText(), file_ext);
                            }
                        if (excel_error != null)
                            {
                            String report = "Open Network File report:\n"
                                    + excel_error;
                            if (MainFrame.getCurrentFrame() == null
                                    || java.awt.GraphicsEnvironment
                                            .isHeadless())
                                {
                                AgnaLog.warn(report);
                                } else
                                {
                                JOptionPane.showMessageDialog(
                                        MainFrame.getCurrentFrame(), report,
                                        "Error List",
                                        JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
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
                        appendOutputLog("Opened " + t_file.getName() + " ("
                                + t_new_full_net.getNetwork().getSize()
                                + " nodes).");
                        my_full_net.cleaning();
                        my_full_net = t_new_full_net;
                        updateMatrix();
                        progress_dialog.setPercent(90);
                        my_full_net.setChanged(false);
                        // 2.1.3: analyses print the network name - make it
                        // match the opened file name when nothing explicit
                        // is stored, and keep the frame title in lockstep
                        Network open_net = my_full_net.getNetwork();
                        String open_name = open_net.getName();
                        if (open_name == null
                                || open_name.trim().length() == 0
                                || open_name.equals("New Network"))
                            {
                            open_net.setName(IOUtils
                                    .getNameWithoutExtension(my_full_net
                                            .getNetworkFileName()));
                            }
                        refreshFrameTitle();
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
            file = Environment.getDefaultOpenDirectory();
            }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Network File");
        chooser.setCurrentDirectory(file);
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText("Select file and click here");
        chooser.addChoosableFileFilter(new CommaTextFilesFilter());
        chooser.addChoosableFileFilter(new TabTextFilesFilter());
        chooser.addChoosableFileFilter(new AgnaFilesFilter());
        chooser.addChoosableFileFilter(new ExcelFilesFilter());
        chooser.addChoosableFileFilter(new GraphMLFilesFilter());
        chooser.addChoosableFileFilter(new GMLFilesFilter());
        chooser.addChoosableFileFilter(new GraphSONFilesFilter());
        chooser.addChoosableFileFilter(new DLFilesFilter());

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
        if (my_full_net.getNetworkFileName().length() > 0)
            {
            file = new File(my_full_net.getNetworkFileName());
            } else
            {
            file = Environment.getDefaultOpenDirectory();
            }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Chain File");
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
        if (my_frame != null)
            {
            my_frame.setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
            }
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

    private void doOpenSamples()
        {
        SampleNetworksDialog dlg = new SampleNetworksDialog(my_frame, file -> {
            stopEditingTable();
            grid_model.setReady(false);
            saveChangedNetworks(true);
            doSimplyOpenNetwork(file);
        });
        dlg.setVisible(true);
        }

    private void doAnalysisAdvisor()
        {
        AnalysisAdvisorDialog dlg = new AnalysisAdvisorDialog(my_frame, action -> {
            switch (action) {
                case OPEN_CENTRALITY: doCentralityBattery(); break;
                case OPEN_CLUSTERING: doClusteringBattery(); break;
                case OPEN_SOCIOMETRICS: doSociometricsBattery(); break;
                case OPEN_DISTANCES: doDistanceBattery(); break;
                case OPEN_HOLES: doStructuralHoles(); break;
                case OPEN_HYPOTHESIS_TESTING: doHypothesisTesting(); break;
                case OPEN_FULL_ANALYSIS: doFullAnalysis(); break;
            }
        });
        dlg.setVisible(true);
        }

    private void doSnaCheatSheet()
        {
        SnaCheatSheetDialog dlg = new SnaCheatSheetDialog(my_frame);
        dlg.setVisible(true);
        }

    private void doOpenOutput() // new version
        {
        // setting name of file:
        File file = null;
        if (output_edit.getFileName() != null && output_edit.getFileName().length() > 0)
            {
            File existing = new File(output_edit.getFileName());
            if (existing.exists() || (existing.getParentFile() != null && existing.getParentFile().exists()))
                {
                file = existing.isDirectory() ? existing : existing.getParentFile();
                }
            }
        if (file == null)
            {
            String netName = my_full_net.getNetworkFileName();
            if (netName != null && netName.length() > 0)
                {
                File netFile = new File(netName);
                if (netFile.getParentFile() != null && netFile.getParentFile().exists())
                    {
                    file = netFile.getParentFile();
                    }
                }
            }
        if (file == null)
            {
            file = Environment.getDefaultOpenDirectory();
            }

        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Existing Output File");
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
        File dir = null;
        String outFileName = output_edit.getFileName();
        if (outFileName != null && outFileName.length() > 0)
            {
            File existing = new File(outFileName);
            if (existing.getParentFile() != null && existing.getParentFile().exists())
                {
                dir = existing.getParentFile();
                }
            }
        if (dir == null)
            {
            String netFileName = my_full_net.getNetworkFileName();
            if (netFileName != null && netFileName.length() > 0)
                {
                File netFile = new File(netFileName);
                if (netFile.getParentFile() != null && netFile.getParentFile().exists())
                    {
                    dir = netFile.getParentFile();
                    }
                }
            }
        if (dir == null)
            {
            dir = Environment.getDefaultOpenDirectory();
            }

        String defaultName = (my_full_net.getNetwork() != null
                && my_full_net.getNetwork().getName() != null
                && my_full_net.getNetwork().getName().length() > 0)
                ? my_full_net.getNetwork().getName() + "_output.html"
                : "output.html";
        file = new File(dir, defaultName);

        chooser.setDialogTitle("Save Current Output");
        chooser.setCurrentDirectory(dir);
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

        final String defaultOutBase = defaultName;
        chooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, evt ->
            {
            javax.swing.filechooser.FileFilter newFf = (javax.swing.filechooser.FileFilter) evt.getNewValue();
            if (newFf == null)
                return;
            String newExt = (newFf instanceof HTMLFilesFilter) ? "html" : "txt";
            File cur = chooser.getSelectedFile();
            String base = (cur != null && cur.getName() != null && !cur.getName().isEmpty())
                    ? IOUtils.getNameWithoutExtension(cur.getName())
                    : IOUtils.getNameWithoutExtension(defaultOutBase);
            if (base == null || base.isEmpty())
                base = "output";
            File parent = (cur != null && cur.getParentFile() != null)
                    ? cur.getParentFile() : chooser.getCurrentDirectory();
            chooser.setSelectedFile(new File(parent, base + "." + newExt));
            });

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
if (my_frame != null) my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
                    // JOptionPane.INFORMATION_MESSAGE);
                    o_edit.setText(tmptext);
                    } catch (Exception e)
                    {
                    MainFrame.setCurrentStatus(MainFrame.default_status);
                    if (my_frame != null) my_frame.setCursor(Cursor
                            .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if (my_frame != null) my_frame.validate();
                    if (my_frame != null) my_frame.repaint();
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
            o_edit.setFileName(file_name);
            MainFrame.setCurrentStatus(MainFrame.default_status);
            if (my_frame != null) my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            if (my_frame != null) my_frame.validate();
            if (my_frame != null) my_frame.repaint();
            return true;
            } catch (Exception e)
            {
            MainFrame.setCurrentStatus(MainFrame.default_status);
            if (my_frame != null) my_frame.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            if (my_frame != null) my_frame.validate();
            if (my_frame != null) my_frame.repaint();
            return false;
            }

        // inserts a link into output_edit
        // HTMLEditorKit kit = ((HTMLEditorKit) output_edit.getEditorKit());
        // HTMLDocument doc = ((HTMLDocument) output_edit.getDocument());
        // try {
        // }
        // {
        // }
        // {
        // }
        }

    public static String getNodeName(int tmp_i)
        {
        return my_full_net.getNetwork().getActor(tmp_i).getName();
        }

    public void readInitialSettings()
        {
        try (Reader reader = IOUtils.reader(
                    Environment.getSettingsFile()))
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
        AppRuntime.setDefaultNodeFaceSource(Environment.getFacesDirectory()
                + System.getProperty("file.separator") + "Red Bullet.gif");
        }

    public static void setDefaultNodeFaceSource(String tmp_face_source)
        {
        AppRuntime.setDefaultNodeFaceSource(tmp_face_source);
        }

    public static String getDefaultNodeFaceSource()
        {
        return AppRuntime.getDefaultNodeFaceSource();
        }

    public static boolean getCurrentWeight()
        {
        return AppRuntime.getCurrentWeight();
        }

    public static void setCurrentWeight(boolean tmp_weight)
        {
        AppRuntime.setCurrentWeight(tmp_weight);
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
        return AppRuntime.getCurrentImageStock();
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
        MainFrame.getCurrentFullNet().setChanged(true);
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
        // 2.1.3: headless-safe (tests, CLI)
        if (status_bar != null)
            {
            status_bar.setText(tmp_status);
            }
        }

    // returns working directory plus file separator character
    public static String getWorkingDirectory()
        {
        return AppRuntime.getWorkingDirectory();
        }

    public static void setWorkingDirectory(String tmp_directory)
        {
        AppRuntime.setWorkingDirectory(tmp_directory);
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

    // 2.1.3: the Output pane doubles as a session log; every operation
    // appends a timestamped line so non-expert users can follow what
    // happened and in which order
    private static void refreshFrameTitle()
        {
        String name = Environment.getApplicationFullName();
        if (my_full_net != null && my_full_net.getNetwork() != null)
            {
            String net_name = my_full_net.getNetwork().getName();
            if (net_name != null && net_name.trim().length() > 0)
                {
                name = net_name + " - " + name;
                }
            }
        my_frame.setTitle(name);
        }

    private static void appendOutputLog(String message)
        {
        if (output_edit == null)
            return;
        // 2.1.3: the Output pane is an HTML document - setText() would put
        // the line outside its structure where it is never rendered; the
        // pane's own append API (used by the analyses) inserts at the end
        String time = java.time.LocalTime.now().withNano(0).toString();
        output_edit.appendString("> [" + time + "] " + message + "\n");
        }

    private static String currentNetworkLabel()
        {
        try
            {
            Network net = MainFrame.getCurrentNetwork();
            if (net == null)
                return "the current network";
            String name = net.getName();
            if (name == null || name.trim().length() == 0)
                return "the current network (" + net.getSize() + " nodes)";
            return "'" + name + "' (" + net.getSize() + " nodes)";
            } catch (Exception e)
            {
            return "the current network";
            }
        }

    private static void appendTransformLog(int type, int mode, float value)
        {
        String message = null;
        if (type == 1)
            message = "Added scalar " + value
                    + " to all off-diagonal values";
        else if (type == 2)
            message = "Multiplied all off-diagonal values by " + value;
        else if (type == 3)
            message = "Squared the sociomatrix (multiplied it by itself)";
        else if (type == 4)
            message = mode == AgnaLib.NORMALIZE_THRESHOLD
                    ? "Normalized by threshold " + value
                            + " (values above it became 1)"
                    : "Normalized to binary (all non-zero values became 1)";
        else if (type == 5)
            message = "Removed isolated nodes (outsiders)";
        else if (type == 6)
            message = "Transposed the sociomatrix";
        if (message != null)
            appendOutputLog(message + " in " + currentNetworkLabel() + ".");
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
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < 2)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least 2 actors to symmetrize.",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return;
            }
        if (!checkAllValues())
            return;

        SymmetrizeDialog dlg = new SymmetrizeDialog(my_frame, my_full_net.getNetwork(), res ->
            {
            grid_model.setReady(false);
            Network netToTransform = res.createNewNetwork ? my_full_net.getNetwork().copy() : my_full_net.getNetwork();
            if (res.createNewNetwork)
                {
                netToTransform.setName(my_full_net.getNetwork().getName() + "_Symmetric");
                }

            applySymmetrizationRule(netToTransform, res.method);
            if (res.zeroDiagonal)
                {
                com.bentza.sna.transform.NetworkTransformService.zeroDiagonal(netToTransform);
                }

            if (res.createNewNetwork)
                {
                FullNet newNet = new FullNet();
                newNet.setNetwork(netToTransform);
                my_full_net = newNet;
                }
            my_full_net.setChanged(true);
            refreshFrameTitle();
            updateNetwork();
            updateMatrix();
            grid_model.setReady(true);
            appendOutputLog("Symmetrized " + currentNetworkLabel() + " by "
                    + res.method + (res.zeroDiagonal ? " (zero diagonal)" : "") + ".");
            });
        dlg.setVisible(true);
        }

    private void applySymmetrizationRule(Network net, String method)
        {
        AgnaLib lib = new AgnaLib();
        switch (method)
            {
            case "Maximum": lib.symmetrizeMaximum(net); break;
            case "Minimum": lib.symmetrizeMinimum(net); break;
            case "Maximum Non-Zero": lib.symmetrizeMaximumNonZero(net); break;
            case "Minimum Non-Zero": lib.symmetrizeMinimumNonZero(net); break;
            case "Below Diagonal (Lower to Upper)": lib.symmetrizeBelow(net); break;
            case "Above Diagonal (Upper to Lower)": lib.symmetrizeAbove(net); break;
            case "Below Diagonal Non-Zero": lib.symmetrizeBelowNonZero(net); break;
            case "Above Diagonal Non-Zero": lib.symmetrizeAboveNonZero(net); break;
            case "Arithmetic Mean": lib.symmetrizeAM(net); break;
            case "Geometric Mean": lib.symmetrizeGM(net); break;
            case "Sum": lib.symmetrizeSum(net); break;
            case "Product": lib.symmetrizeProduct(net); break;
            case "Product Non-Zero": lib.symmetrizeProductNonZero(net); break;
            case "Boolean OR (Union)":
                com.bentza.sna.transform.NetworkTransformService.symmetrize(net,
                        com.bentza.sna.transform.NetworkTransformService.SymmetrizeMode.UNION);
                break;
            case "Boolean AND (Intersection)":
                com.bentza.sna.transform.NetworkTransformService.symmetrize(net,
                        com.bentza.sna.transform.NetworkTransformService.SymmetrizeMode.INTERSECT);
                break;
            default: lib.symmetrizeMaximum(net); break;
            }
        }

    private void doNormalize()
        {
        // 2.1.3: two options only - plain binary, or dichotomize by a
        // user-supplied threshold (x > threshold -> 1)
        Object[] options = { "Binary (non-zero -> 1)", "By threshold..." };
        String choice = (String) JOptionPane.showInputDialog(my_frame,
                "Normalize the sociomatrix by:", "Normalization Options",
                JOptionPane.QUESTION_MESSAGE, null, options,
                "Binary (non-zero -> 1)");
        if (choice == null)
            {
            return;
            }
        if ("By threshold...".equals(choice))
            {
            String tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                    "Threshold value (values above it become 1):",
                    "Normalization Options", JOptionPane.QUESTION_MESSAGE,
                    null, null, "1.0");
            if (tmp_str == null)
                {
                return;
                }
            float threshold = 0f;
            try
                {
                threshold = Float.parseFloat(tmp_str);
                } catch (Exception e)
                {
                JOptionPane.showMessageDialog(my_frame,
                        "Error reading threshold value.", "Parsing error",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
                }
            doTransform((byte) 4, "Dichotomizing sociomatrix by threshold...",
                    AgnaLib.NORMALIZE_THRESHOLD, (byte) 0, threshold);
            } else
            {
            doTransform((byte) 4, "Converting sociomatrix to binary data...",
                    AgnaLib.NORMALIZE_BINARY, (byte) 0, 0f);
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
        appendOutputLog("Replaced node names with 1.." + n + " in "
                + currentNetworkLabel() + ".");
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

        // 2.1.3: pick the second network file, then the conflict policy
        File file = null;
        if (my_full_net.getNetworkFileName().length() > 0)
            {
            file = new File(my_full_net.getNetworkFileName());
            } else
            {
            file = Environment.getDefaultOpenDirectory();
            }

        final javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Merge With Network File");
        chooser.setCurrentDirectory(file);
        chooser.addChoosableFileFilter(new CommaTextFilesFilter());
        chooser.addChoosableFileFilter(new TabTextFilesFilter());
        chooser.addChoosableFileFilter(new AgnaFilesFilter());
        chooser.addChoosableFileFilter(new ExcelFilesFilter());
        chooser.addChoosableFileFilter(new PajekFilesFilter());
        chooser.addChoosableFileFilter(new GraphMLFilesFilter());
        chooser.addChoosableFileFilter(new GMLFilesFilter());
        chooser.addChoosableFileFilter(new GraphSONFilesFilter());
        chooser.addChoosableFileFilter(new DLFilesFilter());
        if (chooser.showOpenDialog(my_frame) != javax.swing.JFileChooser.APPROVE_OPTION)
            {
            return;
            }
        final File merge_file = chooser.getSelectedFile();

        Object[] policies = { "Sum", "Maximum", "Keep current value" };
        String choice = (String) JOptionPane.showInputDialog(my_frame,
                "<html>Actors are joined by name - actors present in both "
                        + "networks are merged into one, actors found only "
                        + "in the second file are appended.<br><br>"
                        + "When a tie exists in both networks, combine it "
                        + "by:</html>",
                "Merge Option", JOptionPane.QUESTION_MESSAGE, null, policies,
                "Sum");
        if (choice == null)
            {
            return;
            }
        final int policy = "Maximum".equals(choice) ? Network.MERGE_MAX
                : "Keep current value".equals(choice) ? Network.MERGE_KEEP_FIRST
                        : Network.MERGE_SUM;

        // read the second network
        FullNet other_full_net = new FullNet();
        final String ext = IOUtils.getExtension(merge_file.getName());
        String read_error = null;
        try
            {
            if ("xls".equals(ext) || "xlsx".equals(ext))
                {
                read_error = other_full_net.readExcelFile(merge_file, ext);
                } else
                {
                try (Reader reader = IOUtils.reader(merge_file))
                    {
                    JTextPane tmp_pane = new JTextPane();
                    tmp_pane.read(reader, null);
                    other_full_net.readNetwork(tmp_pane.getText(), ext);
                    }
                }
            } catch (Exception e2)
            {
            read_error = "Could not read " + merge_file.getName() + ": " + e2;
            }
        if (read_error != null || other_full_net.getNetwork() == null)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Merge failed:\n" + (read_error == null ? "No network read."
                            : read_error),
                    "Merge Error", JOptionPane.ERROR_MESSAGE);
            return;
            }

        final FullNet other_net = other_full_net;
        grid_model.setReady(false);
        my_full_net.mergeWith(other_net.getNetwork(), policy);
        appendOutputLog("Merged '" + other_net.getNetwork().getName()
                + "' (" + other_net.getNetwork().getSize()
                + " actors) into " + currentNetworkLabel() + " by " + choice
                + ".");
        updateMatrix();
        grid_model.setReady(true);
        setCurrentStatus("Merged " + other_net.getNetwork().getSize()
                + " actors into the current network (policy: " + choice
                + ").");
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
        // finding clique diameter. 2.1.3: bounded - maximal-clique
        // enumeration is exponential in the worst case, so only useful
        // diameters are offered and large networks get a warning
        int max_number = Math.min(my_full_net.getNetwork().getSize() - 1, 6);
        if (my_full_net.getNetwork().getSize() > 80)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Large network: wide diameters may be slow - "
                            + "press Cancel in the progress dialog if needed.",
                    "N-Cliques", JOptionPane.INFORMATION_MESSAGE);
            }
        String tmp_str = "";
        Object[] values = new Object[max_number];
        for (int i = 0; i < max_number; i++)
            {
            values[i] = (Object) String.valueOf(i + 1);
            }
        tmp_str = (String) JOptionPane.showInputDialog(my_frame,
                "Please select clique diameter:", "Cliques",
                JOptionPane.QUESTION_MESSAGE, null, values, "1");
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
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < 2)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least 2 actors to run Full Analysis.",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return;
            }
        final AgnaLib agna_lib = new AgnaLib();
        FullAnalysisDialog dlg = new FullAnalysisDialog(my_frame, my_full_net.getNetwork(), options ->
            {
            runAnalysisInBackground("Running Full Analysis...", () ->
                {
                return agna_lib.getAgnaSignature() + agna_lib.outFullAnalysis(my_full_net.getNetwork(), options);
                });
            });
        dlg.setVisible(true);
        }

    private void doSociometricsBattery()
        {
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < 2)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least 2 actors to run Sociometrics.",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return;
            }
        final AgnaLib agna_lib = new AgnaLib();
        SociometricsBatteryDialog dlg = new SociometricsBatteryDialog(my_frame, my_full_net.getNetwork(), options ->
            {
            runAnalysisInBackground("Running Sociometrics Analysis...", () ->
                {
                return agna_lib.getAgnaSignature() + agna_lib.outSociometricsBattery(my_full_net.getNetwork(), options);
                });
            });
        dlg.setVisible(true);
        }

    private void doCentralityBattery()
        {
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < 2)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least 2 actors to run Centrality.",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return;
            }
        final AgnaLib agna_lib = new AgnaLib();
        CentralityBatteryDialog dlg = new CentralityBatteryDialog(my_frame, my_full_net.getNetwork(), options ->
            {
            runAnalysisInBackground("Running Centrality Analysis...", () ->
                {
                return agna_lib.getAgnaSignature() + agna_lib.outCentralityBattery(my_full_net.getNetwork(), options);
                });
            });
        dlg.setVisible(true);
        }

    private void doDistanceBattery()
        {
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < 2)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least 2 actors to run Distance calculations.",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return;
            }
        final AgnaLib agna_lib = new AgnaLib();
        DistanceBatteryDialog dlg = new DistanceBatteryDialog(my_frame, my_full_net.getNetwork(), options ->
            {
            runAnalysisInBackground("Running Distance Analysis...", () ->
                {
                return agna_lib.getAgnaSignature() + agna_lib.outDistanceBattery(my_full_net.getNetwork(), options);
                });
            });
        dlg.setVisible(true);
        }

    private void doClusteringBattery()
        {
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < 2)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least 2 actors to run Clustering.",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return;
            }
        final AgnaLib agna_lib = new AgnaLib();
        ClusteringBatteryDialog dlg = new ClusteringBatteryDialog(my_frame, my_full_net.getNetwork(), options ->
            {
            runAnalysisInBackground("Running Clustering Analysis...", () ->
                {
                return agna_lib.getAgnaSignature() + agna_lib.outClusteringBattery(my_full_net.getNetwork(), options);
                });
            });
        dlg.setVisible(true);
        }

    private void doArchetypeDiagnostic()
        {
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < 3)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least 3 actors to run structural archetypes diagnostic.",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return;
            }
        com.bentza.sna.theory.ArchetypeMetrics metrics = com.bentza.sna.theory.ArchetypeClassifier.computeMetrics(my_full_net.getNetwork());
        com.bentza.sna.report.AnalysisReport report = com.bentza.sna.theory.ArchetypeClassifier.createDiagnosticReport(my_full_net.getNetwork(), metrics);
        doAppendParagraphToOutput(report.toHtmlReport());
        }

    private void doHypothesisTesting()
        {
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < 3)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least 3 actors to run hypothesis tests.",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return;
            }
        HypothesisTestingDialog dlg = new HypothesisTestingDialog(my_frame, my_full_net.getNetwork(), html ->
            {
            doAppendParagraphToOutput(html);
            });
        dlg.setVisible(true);
        }

    private void doAlgorithmBenchmark()
        {
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < 2)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least 2 actors to run algorithm benchmarks.",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return;
            }
        java.util.List<com.bentza.sna.benchmark.BenchmarkResult> results =
                com.bentza.sna.benchmark.AlgorithmBenchmarkSuite.runBenchmark(my_full_net.getNetwork(), 5);
        com.bentza.sna.report.AnalysisReport report =
                com.bentza.sna.benchmark.AlgorithmBenchmarkSuite.createBenchmarkReport(my_full_net.getNetwork(), results, 5);
        doAppendParagraphToOutput(report.toHtmlReport());
        }

    private boolean requireActiveNetwork(String opName, int minNodes)
        {
        if (my_full_net == null || my_full_net.getNetwork() == null || my_full_net.getNetwork().getSize() < minNodes)
            {
            JOptionPane.showMessageDialog(my_frame,
                    "Please load or create a network with at least " + minNodes + " actors to perform " + opName + ".",
                    "No Network", JOptionPane.WARNING_MESSAGE);
            return false;
            }
        return true;
        }

    private void doLocalClustering()
        {
        if (!requireActiveNetwork("Local Clustering", 2)) return;
        doAppendParagraphToOutput(new AgnaLib().outLocalClustering(my_full_net.getNetwork()));
        }

    private void doTransitivity()
        {
        if (!requireActiveNetwork("Global Transitivity", 3)) return;
        doAppendParagraphToOutput(new AgnaLib().outTransitivity(my_full_net.getNetwork()));
        }

    private void doKCore()
        {
        if (!requireActiveNetwork("k-Core Decomposition", 2)) return;
        doAppendParagraphToOutput(new AgnaLib().outKCore(my_full_net.getNetwork()));
        }

    private void doTriadCensus()
        {
        if (!requireActiveNetwork("Triad & Dyad Census", 3)) return;
        doAppendParagraphToOutput(new AgnaLib().outTriadCensus(my_full_net.getNetwork()));
        }

    private void doStructuralHoles()
        {
        if (!requireActiveNetwork("Structural Holes", 2)) return;
        doAppendParagraphToOutput(new AgnaLib().outStructuralHoles(my_full_net.getNetwork()));
        }

    private void doConnectivity()
        {
        if (!requireActiveNetwork("Connectivity Analysis", 2)) return;
        doAppendParagraphToOutput(new AgnaLib().outConnectivity(my_full_net.getNetwork()));
        }

    private void doEigenvector()
        {
        if (!requireActiveNetwork("Eigenvector Centrality", 2)) return;
        doAppendParagraphToOutput(new AgnaLib().outEigenvector(my_full_net.getNetwork()));
        }

    private void doBonacich()
        {
        if (!requireActiveNetwork("Bonacich Power Centrality", 2)) return;
        String val = JOptionPane.showInputDialog(my_frame,
                "Enter attenuation parameter beta (e.g., 0.1 for influence, -0.1 for bargaining power):",
                "0.1");
        if (val == null) return;
        try {
            double beta = Double.parseDouble(val.trim());
            doAppendParagraphToOutput(new AgnaLib().outBonacich(my_full_net.getNetwork(), beta));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(my_frame, "Invalid numeric beta value.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        }

    private void doPageRank()
        {
        if (!requireActiveNetwork("PageRank Centrality", 2)) return;
        String val = JOptionPane.showInputDialog(my_frame,
                "Enter damping factor d (0.0 < d < 1.0, standard default is 0.85):",
                "0.85");
        if (val == null) return;
        try {
            double d = Double.parseDouble(val.trim());
            if (d <= 0.0 || d >= 1.0) throw new NumberFormatException();
            doAppendParagraphToOutput(new AgnaLib().outPageRank(my_full_net.getNetwork(), d));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(my_frame, "Damping factor must be between 0.0 and 1.0.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        }

    private void doDichotomize()
        {
        if (!requireActiveNetwork("Dichotomize", 1)) return;
        String val = JOptionPane.showInputDialog(my_frame,
                "Enter cut-off threshold (ties >= threshold become 1.0, others 0.0):",
                "1.0");
        if (val == null) return;
        try {
            float thresh = Float.parseFloat(val.trim());
            updateNetwork();
            grid_model.setReady(false);
            com.bentza.sna.transform.NetworkTransformService.dichotomize(my_full_net.getNetwork(), thresh, true);
            updateMatrix();
            grid_model.setReady(true);
            appendOutputLog("Dichotomized " + currentNetworkLabel() + " at threshold >= " + thresh);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(my_frame, "Invalid numeric threshold.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        }

    private void doZeroDiagonal()
        {
        if (!requireActiveNetwork("Zero Diagonal", 1)) return;
        updateNetwork();
        grid_model.setReady(false);
        com.bentza.sna.transform.NetworkTransformService.zeroDiagonal(my_full_net.getNetwork());
        updateMatrix();
        grid_model.setReady(true);
        appendOutputLog("Set all diagonal elements (self-loops) to zero in " + currentNetworkLabel());
        }

    private void doSetDiagonal()
        {
        if (!requireActiveNetwork("Set Diagonal", 1)) return;
        String val = JOptionPane.showInputDialog(my_frame,
                "Enter scalar value to set on all diagonal elements:",
                "1.0");
        if (val == null) return;
        try {
            float scalar = Float.parseFloat(val.trim());
            updateNetwork();
            grid_model.setReady(false);
            com.bentza.sna.transform.NetworkTransformService.setDiagonal(my_full_net.getNetwork(), scalar);
            updateMatrix();
            grid_model.setReady(true);
            appendOutputLog("Set diagonal elements to " + scalar + " in " + currentNetworkLabel());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(my_frame, "Invalid numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        }

    private void doExtractEgo()
        {
        if (!requireActiveNetwork("Extract Ego Network", 2)) return;
        int n = my_full_net.getNetwork().getSize();
        String[] actorNames = new String[n];
        for (int i = 0; i < n; i++) actorNames[i] = (i + 1) + ". " + my_full_net.getNetwork().getActorName(i);
        Object picked = JOptionPane.showInputDialog(my_frame, "Select Ego Node:", "Ego Network",
                JOptionPane.QUESTION_MESSAGE, null, actorNames, actorNames[0]);
        if (picked == null) return;
        int egoIdx = -1;
        for (int i = 0; i < n; i++) {
            if (picked.equals(actorNames[i])) { egoIdx = i; break; }
        }
        if (egoIdx < 0) return;

        Object[] radii = {"1 hop (immediate alters)", "2 hops (alters of alters)"};
        Object rPick = JOptionPane.showInputDialog(my_frame, "Neighborhood Radius:", "Radius",
                JOptionPane.QUESTION_MESSAGE, null, radii, radii[0]);
        if (rPick == null) return;
        int radius = rPick.equals(radii[1]) ? 2 : 1;

        updateNetwork();
        com.bentza.sna.net.Network egoNet = com.bentza.sna.transform.NetworkTransformService.extractEgoNetwork(
                my_full_net.getNetwork(), egoIdx, radius, true);
        FullNet newFull = new FullNet();
        newFull.setNetwork(egoNet);
        my_full_net = newFull;
        grid_model.setReady(false);
        updateMatrix();
        grid_model.setReady(true);
        appendOutputLog("Extracted " + egoNet.getName() + " with " + egoNet.getSize() + " nodes.");
        }

    private void doNetworkAlgebra()
        {
        if (!requireActiveNetwork("Network Algebra", 2)) return;
        JFileChooser fc = new JFileChooser(Environment.getDefaultOpenDirectory());
        fc.setDialogTitle("Select Second Network to Combine with " + my_full_net.getNetwork().getName());
        int ret = fc.showOpenDialog(my_frame);
        if (ret != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        FullNet otherFull = new FullNet();
        final String ext = IOUtils.getExtension(f.getName());
        try {
            if ("xls".equals(ext) || "xlsx".equals(ext)) {
                otherFull.readExcelFile(f, ext);
            } else {
                try (Reader reader = IOUtils.reader(f)) {
                    JTextPane tmp_pane = new JTextPane();
                    tmp_pane.read(reader, null);
                    otherFull.readNetwork(tmp_pane.getText(), ext);
                }
            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(my_frame, "Could not read " + f.getName() + ": " + e2.getMessage(), "Read Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] ops = {
            "Matrix Addition (A + B)",
            "Matrix Difference (A - B)",
            "Intersection Minimum (min(A, B))",
            "Hadamard Product (A * B)"
        };
        Object opPick = JOptionPane.showInputDialog(my_frame, "Select Algebraic Operation:",
                "Network Matrix Algebra", JOptionPane.QUESTION_MESSAGE, null, ops, ops[0]);
        if (opPick == null) return;
        com.bentza.sna.transform.NetworkTransformService.AlgebraOp op;
        if (opPick.equals(ops[1])) op = com.bentza.sna.transform.NetworkTransformService.AlgebraOp.SUBTRACT;
        else if (opPick.equals(ops[2])) op = com.bentza.sna.transform.NetworkTransformService.AlgebraOp.INTERSECTION_MIN;
        else if (opPick.equals(ops[3])) op = com.bentza.sna.transform.NetworkTransformService.AlgebraOp.HADAMARD;
        else op = com.bentza.sna.transform.NetworkTransformService.AlgebraOp.ADD;

        updateNetwork();
        com.bentza.sna.net.Network combined = com.bentza.sna.transform.NetworkTransformService.combine(
                my_full_net.getNetwork(), otherFull.getNetwork(), op);
        FullNet newFull = new FullNet();
        newFull.setNetwork(combined);
        my_full_net = newFull;
        grid_model.setReady(false);
        updateMatrix();
        grid_model.setReady(true);
        appendOutputLog("Combined networks via " + op.getLabel() + " -> " + combined.getName());
        }

    private void doSortActors()
        {
        if (!requireActiveNetwork("Sort Actors", 2)) return;
        Object[] criteria = {
            "Actor Name (Alphabetical A-Z)",
            "Actor Name (Alphabetical Z-A)",
            "Total Degree (Highest First)",
            "Total Degree (Lowest First)",
            "In-Degree / Popularity (Highest First)",
            "Out-Degree / Activity (Highest First)"
        };
        Object picked = JOptionPane.showInputDialog(my_frame, "Select Sorting Criterion:",
                "Reorder Sociomatrix", JOptionPane.QUESTION_MESSAGE, null, criteria, criteria[0]);
        if (picked == null) return;
        com.bentza.sna.transform.NetworkTransformService.SortCriterion crit;
        boolean asc = true;
        if (picked.equals(criteria[0])) { crit = com.bentza.sna.transform.NetworkTransformService.SortCriterion.NAME; asc = true; }
        else if (picked.equals(criteria[1])) { crit = com.bentza.sna.transform.NetworkTransformService.SortCriterion.NAME; asc = false; }
        else if (picked.equals(criteria[2])) { crit = com.bentza.sna.transform.NetworkTransformService.SortCriterion.DEGREE; asc = false; }
        else if (picked.equals(criteria[3])) { crit = com.bentza.sna.transform.NetworkTransformService.SortCriterion.DEGREE; asc = true; }
        else if (picked.equals(criteria[4])) { crit = com.bentza.sna.transform.NetworkTransformService.SortCriterion.INDEGREE; asc = false; }
        else { crit = com.bentza.sna.transform.NetworkTransformService.SortCriterion.OUTDEGREE; asc = false; }

        updateNetwork();
        grid_model.setReady(false);
        com.bentza.sna.transform.NetworkTransformService.sortActors(my_full_net.getNetwork(), crit, asc);
        updateMatrix();
        grid_model.setReady(true);
        appendOutputLog("Reordered sociomatrix by " + crit.getLabel() + " (" + (asc ? "ascending" : "descending") + ")");
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

    private int askExportFormatDialog(int default_format)
        {
        final String[] labels = { "Agna (.agn)", "Tab-separated (.txt)",
                "Comma-separated (.csv)", "Pajek (.net)", "Excel (.xls)",
                "GraphML (.graphml)", "GML (.gml)", "GraphSON (.json)",
                "UCINET DL (.dl)" };
        final String[] vector_options = { "Emission Degree",
                "Reception Degree", "Weighted Emission Degree",
                "Sociometric Status", "Nodal Degree", "Betweenness",
                "Closeness", "Prestige" };
        final int[] result = new int[] { -1 };
        final JDialog d = new JDialog(my_frame, "Export format", true);
        d.setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
        final JPanel vector_panel = new JPanel();
        vector_panel.setLayout(new BoxLayout(vector_panel, BoxLayout.Y_AXIS));
        final JRadioButton[] radios = new JRadioButton[labels.length];
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < labels.length; i++)
            {
            radios[i] = new JRadioButton(labels[i], i == default_format);
            final int radio_index = i;
            radios[i].addItemListener(new java.awt.event.ItemListener()
                {
                    public void itemStateChanged(java.awt.event.ItemEvent e)
                        {
                        vector_panel.setVisible(radio_index == 3);
                        }
                });
            group.add(radios[i]);
            d.add(radios[i]);
            }
        vector_panel.add(new JLabel(
                "Pajek: per-node measure vectors to append"));
        final java.util.Hashtable boxes = new java.util.Hashtable();
        for (int i = 0; i < vector_options.length; i++)
            {
            JCheckBox box = new JCheckBox(vector_options[i], true);
            boxes.put(vector_options[i], box);
            vector_panel.add(box);
            }
        vector_panel.setVisible(default_format == 3);
        d.add(vector_panel);
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

    // 2.1.3: applies a chosen look and feel immediately, persists it and
    // re-skins every open window
    private void chooseLookAndFeel(String tmp_choice)
        {
        setLookAndFeel(tmp_choice);
        setNativeLookAndFeel();
        persistLookAndFeelPreference();
        javax.swing.SwingUtilities.updateComponentTreeUI(my_frame);
        if (my_grafic != null && my_grafic.gr_frame != null)
            {
            javax.swing.SwingUtilities.updateComponentTreeUI(my_grafic.gr_frame);
            }
        my_frame.repaint();
        selectLookAndFeelMenuItems(tmp_choice);
        }

    private void selectLookAndFeelMenuItems(String choice)
        {
        p_laf_native.setSelected("system".equals(choice));
        p_laf_flatlaf.setSelected("flatlaf".equals(choice));
        p_laf_flatlaf_dark.setSelected("flatlaf-dark".equals(choice));
        }

    private void persistLookAndFeelPreference()
        {
        try
            {
            java.io.File ini = Environment.getSettingsFile();
            if (ini.getParentFile() != null)
                {
                ini.getParentFile().mkdirs();
                }
            String content = "";
            if (ini.exists())
                {
                content = new String(java.nio.file.Files.readAllBytes(ini
                        .toPath()),
                        java.nio.charset.StandardCharsets.ISO_8859_1);
                }
            String line = "Look And Feel\t" + getLookAndFeel();
            String[] lines = content.replace("\r\n", "\n").split("\n");
            StringBuffer sb = new StringBuffer();
            boolean replaced = false;
            for (int i = 0; i < lines.length; i++)
                {
                if (lines[i].startsWith("Look And Feel\t"))
                    {
                    sb.append(line).append("\n");
                    replaced = true;
                    } else if (lines[i].length() > 0)
                    {
                    sb.append(lines[i]).append("\n");
                    }
                }
            if (!replaced)
                {
                sb.append(line).append("\n");
                }
            java.nio.file.Files.write(ini.toPath(), sb.toString().getBytes(
                    java.nio.charset.StandardCharsets.ISO_8859_1));
            } catch (Exception e)
            {
            AgnaLog.warn("could not persist look and feel preference", e);
            }
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

        // 2.1.3: analyses run on a background thread; the UI shows a
        // cancellable progress dialog and stays responsive
        if (analysis_type == 21)
            {
            AgnaLib.clique_search_cancelled = false;
            }
        final java.util.concurrent.Callable<String> task = new java.util.concurrent.Callable<String>()
            {
                public String call()
                    {
                    return buildAnalysisOutput(analysis_type, init, end);
                    }
            };
        runAnalysisInBackground(decoration, task);
        }

    private String buildAnalysisOutput(byte analysis_type, int init, int end)
        {
        AgnaLib agna_lib = new AgnaLib();
        String tmp_str = new String("");
        if (!"text/html".equals(AppRuntime.outputContentType()))
            tmp_str += agna_lib.getAgnaSignature();
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

                        case 22:
                        tmp_str += agna_lib.outFullAnalysis(my_full_net
                                .getNetwork());
                        break;

                        }
        return tmp_str;
        }

    // 2.1.3: runs an analysis on a SwingWorker with a cancellable progress
    // dialog; the output is appended on the EDT when done
    private void runAnalysisInBackground(final String decoration,
            final java.util.concurrent.Callable<String> task)
        {
        final JDialog box = new JDialog(my_frame, "Agna - analysis", false);
        box.setLayout(new BoxLayout(box.getContentPane(), BoxLayout.Y_AXIS));
        box.add(new JLabel(decoration != null ? decoration : "Computing..."));
        final JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setIndeterminate(true);
        box.add(bar);
        final JButton cancel_button = new JButton("Cancel");
        box.add(cancel_button);
        box.pack();
        box.setLocationRelativeTo(my_frame);

        final SwingWorker<String, Void> worker = new SwingWorker<String, Void>()
            {
                protected String doInBackground()
                    {
                    try
                        {
                        return task.call();
                        } catch (OutOfMemoryError oom)
                        {
                        // 2.1.3: never let an over-eager analysis kill the app
                        AgnaLog.error("analysis ran out of memory", oom);
                        return "\n*** Analysis stopped: ran out of memory "
                                + "(too many results). Try a smaller diameter. ***";
                        } catch (Throwable e)
                        {
                        AgnaLog.error("analysis failed", e);
                        return null;
                        }
                    }

                protected void done()
                    {
                    try
                        {
                        String out = get();
                        if (out != null)
                            {
                            output_edit.appendBlock(out);
                            }
                        } catch (java.util.concurrent.CancellationException ce)
                        {
                        output_edit
                                .appendBlock("\n*** Analysis cancelled by user. ***");
                        } catch (Exception e)
                        {
                        AgnaLog.error("analysis output failed", e);
                        }
                    box.dispose();
                    MainFrame.setCurrentStatus(MainFrame.default_status);
                    my_frame.setCursor(Cursor
                            .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    grid_model.setReady(true);
                    my_frame.repaint();
                    }
            };
        cancel_button.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                    AgnaLib.clique_search_cancelled = true;
                    worker.cancel(true);
                    }
            });
        box.setVisible(true);
        worker.execute();
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
                        appendTransformLog(1, param_1, param_3);
                        break;

                        case 2:
                        my_full_net.multiplyByScalar(param_3);
                        appendTransformLog(2, param_1, param_3);
                        break;

                        case 3:
                        my_full_net.square();
                        appendTransformLog(3, param_1, param_3);
                        break;

                        case 4:
                        my_full_net.normalize(param_1, param_3);
                        appendTransformLog(4, param_1, param_3);
                        break;

                        case 5:
                        my_full_net.removeOutsidersInNetwork(grid_model);
                        appendTransformLog(5, param_1, param_3);
                        break;

                        case 6:
                        my_full_net.transpose();
                        appendTransformLog(6, param_1, param_3);
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
            // - 1);
            grid_model.setColumnCount(nu);
            grid_model.fireTableStructureChanged();
            if (nu != grid_model.getRowCount())
                {
                grid_model.setRowCount(nu);
                }
            grid_model.fireTableRowsDeleted(nu, nu + dif - 1);
            }
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
        Network tmp_network = my_full_net.getNetwork();
        DefaultTableModel tmp_model = (DefaultTableModel) my_grid.getModel();
        if (nu != tmp_network.getSize())
            tmp_network.setSize(nu);
        for (int i = 0; i < nu; i++)
            {
            for (int j = 0; j < nu; j++)
                {
                tmp_network.setObjectValue(my_grid.getValueAt(i, j), i, j);
                }
            }
        if (my_full_net.isArea())
            my_full_net.net_area.updateArea(my_full_net.getNetwork());
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
                // + " - Agna 2.0: Network Viewer");
                my_grafic.my_area.paintEdges();
                my_grafic.getCurrentFrame().pack();
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
        setEnabledCloseViewerMenu();
        my_grafic.my_area.paintEdges();
        // + " - Agna 2.0: Network Viewer");
        my_grafic.getCurrentFrame().toFront();
        my_grafic.getCurrentFrame().setState(JFrame.NORMAL);
        setCurrentStatus(default_status);
        my_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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

    // set look & feel:: *********************

    // 2.1.3: preferred look and feel, persisted in the settings:
    //   flatlaf-dark   FlatLaf dark
    //   flatlaf        FlatLaf light
    //   system         native per platform (default)

    public static String getLookAndFeel()
        {
        return AppRuntime.getLookAndFeel();
        }

    public static void setLookAndFeel(String tmp_laf)
        {
        AppRuntime.setLookAndFeel(tmp_laf);
        }

    // 2.1.3: easy revert - the classic GIF icons stay available behind
    // this preference ("Classic Toolbar Icons" in the settings file)

    public static boolean isClassicToolbarIcons()
        {
        return AppRuntime.isClassicToolbarIcons();
        }

    public static void setClassicToolbarIcons(boolean tmp_value)
        {
        AppRuntime.setClassicToolbarIcons(tmp_value);
        }

    public static void setNativeLookAndFeel()
        {
        String choice = getLookAndFeel();
        try
            {
            if ("flatlaf".equals(choice))
                {
                // 2.1.3: the grid-line keys travel as theme setup
                // properties because FlatLaf's theme defaults shadow
                // plain UIManager.put values
                com.formdev.flatlaf.FlatLaf.setGlobalExtraDefaults(
                        gridLineDefaults());
                com.formdev.flatlaf.FlatLightLaf.setup();
                }
            else if ("flatlaf-dark".equals(choice))
                {
                com.formdev.flatlaf.FlatLaf.setGlobalExtraDefaults(
                        gridLineDefaults());
                com.formdev.flatlaf.FlatDarkLaf.setup();
                }
            else
                {
                UIManager.setLookAndFeel(UIManager
                        .getSystemLookAndFeelClassName());
                }
            } catch (Exception e)
            {
            System.out.println(e.toString());
            try
                {
                UIManager.setLookAndFeel(UIManager
                        .getSystemLookAndFeelClassName());
                } catch (Exception e2)
                {
                }
            }

        // 2.1.3: the sociomatrix grid lines must stay visible under every
        // look and feel. These puts cover LAFs (Aqua, Metal) that read the
        // keys when the table UI is installed; FlatLaf gets them via the
        // theme setup properties above.
        UIManager.put("Table.showHorizontalLines", Boolean.TRUE);
        UIManager.put("Table.showVerticalLines", Boolean.TRUE);
        }

    private void doSaveAsDefaultSettings()
        {
        if (my_full_net != null)
            {
            my_full_net.writeInitialSettings();
            }
        setCurrentStatus("Settings saved as default.");
        }

    private static java.util.Map<String, String> gridLineDefaults()
        {
        java.util.Map<String, String> lafProps = new java.util.HashMap<>();
        lafProps.put("Table.showHorizontalLines", "true");
        lafProps.put("Table.showVerticalLines", "true");
        return lafProps;
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

        // 2.1.3: the desktop registers its UI hooks with the engine
        AppRuntime.registerHooks(
                () -> output_edit != null ? output_edit.getContentType()
                        : "text/plain",
                s -> { if (output_edit != null)
                        output_edit.appendString(s); },
                s -> { if (my_grafic != null
                        && my_grafic.gr_frame != null)
                        { try { GrNet.setHTMLStatus(s); }
                        catch (Exception e) { } } },
                () -> my_frame,
                () -> getCurrentNetwork(),
                () -> my_full_net,
                p -> { if (progress_dialog != null)
                        progress_dialog.setPercent(p); },
                (actor, index) -> { new NodeSettingsDialog(actor, index)
                        .showDialog(); });
        AppRuntime.setNodeActions(new AppRuntime.NodeActions()
            {
            public void addNodeAndFocus(int x, int y, int width)
                {
                GrNet.doAddNodeHere(x, y, width);
                }
            public void enableFirstNodeTools()
                {
                GrNet.enableFirst();
                }
            public void resetStatus()
                {
                GrNet.setDefaultStatus();
                }
            });

        // default settings:

        if (Environment.getSettingsFile().exists())
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
        // 2.1.3: the new black logo doubles as the window/dock icon
        main_icon = new ImageIcon(Agna.class.getResource("/agna_icon.png"));

        // 2.1.3: replace the generic Java dock icon on macOS even when the
        // app is started from a jar (the bundle path uses the .icns)
        try
            {
            Class<?> app_class = Class.forName("com.apple.eawt.Application");
            Object mac_app = app_class.getMethod("getApplication")
                    .invoke(null);
            app_class.getMethod("setDockIconImage", java.awt.Image.class)
                    .invoke(mac_app, main_icon.getImage());
            } catch (Exception e_mac_icon)
            {
            // not macOS (or a newer JDK without the old eawt API) - the
            // frame icon still applies on other systems
            }

        // read icon from current jar archive:

        my_frame.setIconImage(main_icon.getImage());
        fContent = my_frame.getContentPane();
        controlArea = new JPanel(); // panel principal
        controlArea.setPreferredSize(max_dim); // new Dimension(600, 200));
        controlArea.setLayout(new BorderLayout());

        output_edit = new AgnaTextPane(); // componenta output
        output_edit.addFocusListener(flst);
        output_edit.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                String desc = e.getDescription();
                if (desc != null && desc.startsWith("export:")) {
                    String tableId = desc.substring("export:".length());
                    com.bentza.sna.report.TableData td = com.bentza.sna.report.TableRegistry.get(tableId);
                    if (td != null) {
                        if (output_edit != null) {
                            td = output_edit.syncTableFromDocument(td);
                        }
                        ExportTableDialog.showDialog(my_frame, td);
                    }
                }
            }
        });

        scrollEditRight = new JScrollPane(output_edit);
        scrollEditRight.setPreferredSize(new Dimension(
                (int) (max_dim.width / 5), max_dim.height));

        ttm = ToolTipManager.sharedInstance();
        ttm.setInitialDelay(400);
        ttm.setDismissDelay(7000);

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

        // table header:
        grid_header = new AgnaTableHeader();
        scrollEditLeft.setRowHeaderView(grid_header.getAgnaVerticalHeader());
        ttm.registerComponent(grid_header.getAgnaVerticalHeader());

        updateMatrix();
        grid_model.setReady(true);

        upleft_button = new JButton(MainFrame.isClassicToolbarIcons()
                ? Environment.getButtonImageIcon("AddNodes.gif")
                : ModernIcons.get(ModernIcons.ADD_NODE, 22));
        upleft_button.setRolloverIcon(MainFrame.isClassicToolbarIcons()
                ? Environment.getButtonImageIcon("rAddNodes.gif")
                : ModernIcons.get(ModernIcons.ADD_NODE, 22, true));
        upleft_button.setToolTipText("Add new nodes to current network Ctrl+P");
        upleft_button.addActionListener(act_menu);
        upleft_button.setBorder(new LineBorder(Color.gray, 1));
        upleft_button.setPreferredSize(new Dimension((int) (col_width * 2 / 3),
                my_grid.getRowHeight()));
        lowerleft_button = new JButton();
        if (MainFrame.isClassicToolbarIcons())
            {
            lowerleft_button
                    .setText("<html><font size = 1 color='#298C8C' "
                            + "face='Arial,Helvetica,Verdana,sans-serif'>"
                            + "Help</font>");
            lowerleft_button.setBorder(new LineBorder(Color.gray, 1));
            } else
            {
            lowerleft_button.setIcon(ModernIcons.get(ModernIcons.HELP, 20));
            lowerleft_button.setRolloverIcon(
                    ModernIcons.get(ModernIcons.HELP, 20, true));
            lowerleft_button.setBorder(null);
            }
        lowerleft_button.addActionListener(act_menu);
        lowerleft_button.setToolTipText("Display the Agna Help frame Ctrl+H");
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
        mEdit = new JMenu("Edit");
        mData = new JMenu("Network");
        mAnalysis = new JMenu("Analysis");
        mView = new JMenu("View");
        mPreferences = new JMenu("Preferences");
        mHelp = new JMenu("Help");

        mFile.setMnemonic('f');
        mEdit.setMnemonic('e');
        mData.setMnemonic('n');
        mAnalysis.setMnemonic('y');
        mView.setMnemonic('v');
        mPreferences.setMnemonic('p');
        mHelp.setMnemonic('h');

        mFile.setToolTipText("File operations: new, open, save, samples, and export");
        mEdit.setToolTipText("Cell editing and clipboard operations");
        mData.setToolTipText("Matrix transformations, ego networks, algebra, and node management");
        mAnalysis.setToolTipText("Social Network Analysis algorithms, batteries, and advisors");
        mView.setToolTipText("Visual Network Viewer and output pane controls");
        mPreferences.setToolTipText("Working directory, look & feel, and default preferences");
        mHelp.setToolTipText("Help manual, SNA glossary cheat-sheet, and guided case studies");

        // submeniuri din FILE:
        f_new = new JMenuItem("New Network...");
        f_open = new JMenuItem("Open Network...");
        f_samples = new JMenuItem("Open Sample Network & Case Study...");
        d_merge = new JMenuItem("Merge Network...");
        // 2.1.3: Merge Network lives in the File menu, after Open
        f_save = new JMenuItem("Save Network As...");
        f_simply_save = new JMenuItem("Save Network");
        f_create = new JMenuItem("New From Chain...");
        f_quit = new JMenuItem("Quit");

        f_new.setToolTipText("Create a new empty network");
        f_open.setToolTipText("Open an existing network file");
        f_samples.setToolTipText("Open classic empirical and pedagogical benchmark networks with guided case studies");
        d_merge.setToolTipText("Merge another network file into current sociomatrix");
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
        f_samples.addActionListener(act_menu);
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
        mFile.add(f_samples);
        mFile.add(d_merge);
        mFile.addSeparator();
        mFile.add(f_create);
        mFile.addSeparator();
        mFile.add(f_save);
        mFile.add(f_simply_save);
        mFile.addSeparator();

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
        e_delete.setToolTipText("Clear content in selected sociomatrix cell(s)");
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
        d_normalize = new JMenuItem("Normalize (Binarize)...");
        d_remove_out = new JMenuItem("Remove Outsiders");
        d_renumber_nodes = new JMenuItem("Renumber Nodes");
        d_multiply_network = new JMenuItem("Square Matrix");
        d_dichotomize = new JMenuItem("Dichotomize / Threshold Ties...");
        d_zero_diagonal = new JMenuItem("Zero Diagonal (Remove Self-Loops)");
        d_set_diagonal = new JMenuItem("Set Diagonal to Value...");
        d_ego_network = new JMenuItem("Extract Ego Network...");
        d_network_algebra = new JMenuItem("Network Algebra (Combine Networks)...");
        d_sort_actors = new JMenuItem("Reorder Sociomatrix / Sort Actors...");

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
        d_dichotomize.setToolTipText("Threshold tie values at a cut-off to convert matrix into binary 0/1 ties");
        d_zero_diagonal.setToolTipText("Reset all diagonal elements to 0 to remove self-loops");
        d_set_diagonal.setToolTipText("Set all diagonal matrix elements to a specified constant value");
        d_ego_network.setToolTipText("Extract the 1-hop or 2-hop ego subnetwork centered on a focal actor");
        d_network_algebra.setToolTipText("Combine two networks using element-wise addition, subtraction, intersection min, or product");
        d_sort_actors.setToolTipText("Permute rows and columns of sociomatrix by actor name, degree, indegree, or outdegree");

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
        d_dichotomize.addActionListener(act_menu);
        d_zero_diagonal.addActionListener(act_menu);
        d_set_diagonal.addActionListener(act_menu);
        d_ego_network.addActionListener(act_menu);
        d_network_algebra.addActionListener(act_menu);
        d_sort_actors.addActionListener(act_menu);

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
        mData.add(d_dichotomize);
        mData.addSeparator();
        mData.add(d_zero_diagonal);
        mData.addSeparator();
        mData.add(d_ego_network);
        mData.add(d_network_algebra);
        mData.add(d_sort_actors);
        mData.addSeparator();
        mData.add(d_multiply_network);

        // Submeniuri din ANALYSIS:
        a_basic = new JMenuItem("Basic Description");
        a_sociometrics = new JMenuItem("Sociometric & Basic...");
        a_nodal_degree = new JMenuItem("Nodal Degree");
        a_indegree = new JMenuItem("Indegree");
        a_outdegree = new JMenuItem("Outdegree");
        a_density = new JMenuItem("Density");
        a_cohesion = new JMenuItem("Cohesion");
        a_emissions = new JMenuItem("Emission Degree");
        a_receptions = new JMenuItem("Reception Degree");
        a_determinations = new JMenuItem("Determination Degree");
        a_sociostatus = new JMenuItem("Sociometric Status");
        a_distance = new JMenuItem("Distances & Geodesics...");
        a_eccentricity = new JMenuItem("Eccentricity");
        a_diameter = new JMenuItem("Diameter");
        a_geodesics = new JMenuItem("Geodesic Matrix");
        a_shortest_paths = new JMenuItem("Shortest Paths...");
        a_all_shortest_paths = new JMenuItem("All Shortest Paths");
        a_cliques = new JMenuItem("N-Cliques...");
        a_centrality = new JMenuItem("Centrality & Prestige...");
        a_bavelas = new JMenuItem("Bavelas-Leavitt");
        a_closeness = new JMenuItem("Closeness");
        a_fareness = new JMenuItem("Fareness");
        a_betweenness = new JMenuItem("Betweenness");
        a_prestige = new JMenuItem("Prestige");
        a_full_analysis = new JMenuItem("Full Analysis...");
        a_clustering = new JMenuItem("Clustering & Cohesion...");
        a_local_clustering = new JMenuItem("Local Clustering (Watts-Strogatz)");
        a_transitivity = new JMenuItem("Global Transitivity (Triads)");
        a_kcore = new JMenuItem("k-Core Decomposition");
        a_triad_census = new JMenuItem("Triad & Dyad Census (Holland-Leinhardt)");
        a_structural_holes = new JMenuItem("Social Capital & Structural Holes (Burt)");
        a_connectivity = new JMenuItem("Connectivity & Articulation Points");
        a_eigenvector = new JMenuItem("Eigenvector Centrality (Bonacich)");
        a_bonacich = new JMenuItem("Bonacich Power Centrality c(alpha, beta)...");
        a_pagerank = new JMenuItem("PageRank Centrality...");
        a_archetypes = new JMenuItem("Structural Archetypes Diagnostic");
        a_hypothesis_testing = new JMenuItem("Hypothesis Testing (Null Models)...");
        a_benchmarks = new JMenuItem("Algorithm Benchmarks (Centrality, Clustering, Paths)...");
        a_export_table = new JMenuItem("Export Analysis Table...");

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
        a_centrality.setToolTipText("Degree, geodesic, prestige, and spectral centrality measures");
        a_bavelas
                .setToolTipText("Compute Bavelas-Leavitt coefficient for each node");
        a_closeness
                .setToolTipText("Compute closeness coefficient for each node");
        a_fareness.setToolTipText("Compute fareness coefficient for each node");
        a_betweenness
                .setToolTipText("Compute betweenness coefficient for each node");
        a_prestige.setToolTipText("Compute proximity prestige for each node in directed networks");
        a_cliques.setToolTipText("Enumerate maximal complete subgraphs (all members tied to each other)");
        a_full_analysis.setToolTipText("Execute a configurable full analysis suite with customizable modules and executive narrative");
        a_clustering.setToolTipText("Clustering coefficients, transitivity, k-core, communities, and structural equivalence");
        a_local_clustering.setToolTipText("Compute Watts-Strogatz local clustering coefficient for each node");
        a_transitivity.setToolTipText("Compute Newman global network transitivity (triad closure ratio)");
        a_kcore.setToolTipText("Compute k-core structural percolation decomposition and coreness shells");
        a_triad_census.setToolTipText("Compute complete Holland-Leinhardt 16 triad census and dyad reciprocity");
        a_structural_holes.setToolTipText("Ronald Burt's social capital: constraint, effective size, efficiency, and hierarchy");
        a_connectivity.setToolTipText("Identify connected components, cut-vertices (articulation points), and bridge ties");
        a_eigenvector.setToolTipText("Compute Bonacich eigenvector centrality via principal eigenvalue decomposition");
        a_bonacich.setToolTipText("Compute Bonacich power centrality c(alpha, beta) for prestige or bargaining exchange power");
        a_pagerank.setToolTipText("Compute PageRank centrality with random walk damping");

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
        a_archetypes.addActionListener(act_menu);
        a_archetypes.setMnemonic('t');
        a_archetypes.setToolTipText("Diagnose fit against classic social theories: small-world, preferential attachment, homophily, hierarchy");
        a_hypothesis_testing.addActionListener(act_menu);
        a_hypothesis_testing.setMnemonic('h');
        a_hypothesis_testing.setToolTipText("Compare observed network metrics against chance distributions generated by null models");
        a_benchmarks.addActionListener(act_menu);
        a_benchmarks.setMnemonic('k');
        a_benchmarks.setToolTipText("Benchmark execution runtime, throughput, and correctness of centrality, clustering, and path algorithms");
        a_export_table.addActionListener(act_menu);
        a_export_table.setToolTipText("Export analysis result tables to CSV, JSON, TSV, or HTML");

        a_local_clustering.addActionListener(act_menu);
        a_transitivity.addActionListener(act_menu);
        a_kcore.addActionListener(act_menu);
        a_triad_census.addActionListener(act_menu);
        a_structural_holes.addActionListener(act_menu);
        a_connectivity.addActionListener(act_menu);
        a_eigenvector.addActionListener(act_menu);
        a_bonacich.addActionListener(act_menu);
        a_pagerank.addActionListener(act_menu);

        a_basic.setMnemonic('b');
        a_distance.setMnemonic('d');
        a_diameter.setMnemonic('d');
        a_eccentricity.setMnemonic('e');
        a_geodesics.setMnemonic('g');
        a_shortest_paths.setMnemonic('s');
        a_all_shortest_paths.setMnemonic('a');
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
        a_basic.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
                ActionEvent.ALT_MASK));
        a_diameter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                ActionEvent.ALT_MASK));
        a_geodesics.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                ActionEvent.ALT_MASK));
        a_all_shortest_paths.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, ActionEvent.ALT_MASK));
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

        a_sociometrics.addActionListener(act_menu);
        a_centrality.addActionListener(act_menu);
        a_distance.addActionListener(act_menu);
        a_clustering.addActionListener(act_menu);
        a_structural_holes.addActionListener(act_menu);
        a_connectivity.addActionListener(act_menu);
        a_full_analysis.addActionListener(act_menu);
        a_archetypes.addActionListener(act_menu);
        a_hypothesis_testing.addActionListener(act_menu);
        a_benchmarks.addActionListener(act_menu);
        a_export_table.addActionListener(act_menu);

        a_advisor = new JMenuItem("Analysis Advisor (What Should I Compute?)...");
        a_advisor.setToolTipText("Interactive wizard helping beginners select the right algorithms for their research question");
        a_advisor.addActionListener(act_menu);

        mAnalysis.add(a_advisor);
        mAnalysis.add(a_full_analysis);
        mAnalysis.addSeparator();
        mAnalysis.add(a_sociometrics);
        mAnalysis.add(a_centrality);
        mAnalysis.add(a_distance);
        mAnalysis.add(a_clustering);
        mAnalysis.add(a_structural_holes);
        mAnalysis.add(a_connectivity);
        mAnalysis.addSeparator();
        mAnalysis.add(a_archetypes);
        mAnalysis.add(a_hypothesis_testing);
        mAnalysis.add(a_benchmarks);
        mAnalysis.add(a_export_table);

        // Submeniuri din VIEW:
        v_viewer = new JMenuItem("Network Viewer");
        v_close = new JMenuItem("Close Network Viewer");
        v_hide_output = new JMenuItem("Hide Output");
        v_view_output = new JMenuItem("Show Output");

        v_viewer.setToolTipText("Display Network Viewer frame");
        v_close.setToolTipText("Close Network Viewer frame");
        v_hide_output.setToolTipText("Hide the right-hand output results pane");
        v_view_output.setToolTipText("Show the right-hand output results pane");

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

        // submeniuri din OUTPUT: -- am renuntat la el; trecute in FILE
        o_open = new JMenuItem("Open Output File...");
        o_save = new JMenuItem("Save Output As...");
        o_simply_save = new JMenuItem("Save Output");
        o_clear = new JMenuItem("Clear Output");
        o_export_table = new JMenuItem("Export Analysis Table...");

        o_open.setToolTipText("Open existing output file");
        o_save.setToolTipText("Save current output as specified file");
        o_simply_save.setToolTipText("Save current output");
        o_clear.setToolTipText("Clear all content of current output area");
        o_export_table.setToolTipText("Export selected analysis table to CSV, JSON, TSV, or HTML");

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
        o_export_table.addActionListener(act_menu);

        mFile.add(o_open); // !!
        mFile.add(o_save);
        mFile.add(o_simply_save);
        mFile.add(o_export_table);
        mFile.addSeparator();
        mFile.add(f_quit);

        mEdit.addSeparator();
        mEdit.add(o_clear);

        // submeniuri din PREFERENCES:
        p_working_directory = new JMenuItem("Working Directory...");
        p_save_as_default = new JMenuItem("Save Settings As Default...");
        p_working_directory.setToolTipText("Choose default working directory");
        p_save_as_default.setToolTipText("Save current display, layout, and analysis configuration as application defaults");

        p_working_directory.addActionListener(act_menu);

        // 2.1.3: look and feel picker
        p_laf_native = new JRadioButtonMenuItem("Native (system)");
        p_laf_flatlaf = new JRadioButtonMenuItem("FlatLaf (light)");
        p_laf_flatlaf_dark = new JRadioButtonMenuItem("FlatLaf (dark)");
        p_laf_native.setToolTipText("Use operating system native look and feel");
        p_laf_flatlaf.setToolTipText("Use FlatLaf modern light theme");
        p_laf_flatlaf_dark.setToolTipText("Use FlatLaf modern dark theme");
        ButtonGroup laf_group = new ButtonGroup();
        laf_group.add(p_laf_native);
        laf_group.add(p_laf_flatlaf);
        laf_group.add(p_laf_flatlaf_dark);
        p_laf_native.addActionListener(act_menu);
        p_laf_flatlaf.addActionListener(act_menu);
        p_laf_flatlaf_dark.addActionListener(act_menu);
        selectLookAndFeelMenuItems(getLookAndFeel());
        mPreferences.add(p_laf_native);
        mPreferences.add(p_laf_flatlaf);
        mPreferences.add(p_laf_flatlaf_dark);
        p_working_directory.setMnemonic('w');
        p_save_as_default.addActionListener(act_menu);
        mPreferences.add(p_working_directory);
        mPreferences.add(p_save_as_default);

        // submeniuri din HELP:
        h_contents = new JMenuItem("Contents...");
        h_cheat_sheet = new JMenuItem("SNA Quick Reference & Concept Cheat-Sheet...");
        h_advisor = new JMenuItem("Analysis Advisor...");
        h_samples = new JMenuItem("Guided Case Studies & Sample Networks...");
        h_about_agna = new JMenuItem("About Agna...");

        h_contents.setToolTipText("Display the Agna Help frame");
        h_cheat_sheet.setToolTipText("Searchable glossary of Social Network Analysis concepts, definitions, and interpretations");
        h_advisor.setToolTipText("Wizard guiding you to the right analysis battery for your research question");
        h_samples.setToolTipText("Explore seminal empirical datasets with guided sociological questions");
        h_about_agna.setToolTipText("Display Agna copyright information");

        h_contents.addActionListener(act_menu);
        h_cheat_sheet.addActionListener(act_menu);
        h_advisor.addActionListener(act_menu);
        h_samples.addActionListener(act_menu);
        h_about_agna.addActionListener(act_menu);

        h_contents.setMnemonic('c');
        h_about_agna.setMnemonic('a');
        h_contents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                ActionEvent.CTRL_MASK));

        mHelp.add(h_contents);
        mHelp.add(h_cheat_sheet);
        mHelp.add(h_advisor);
        mHelp.add(h_samples);
        mHelp.addSeparator();
        mHelp.add(h_about_agna);

        //

        mb = new JMenuBar();
        mb.add(mFile);
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
        // 2.1.3: modern duotone toolbar icons (drawn, LAF-aware)
        ImageIcon i_new_network = ModernIcons.get(ModernIcons.NEW_NETWORK, 22);
        i_open_network = ModernIcons.get(ModernIcons.OPEN_NETWORK, 22);
        ImageIcon i_save_network = ModernIcons.get(ModernIcons.SAVE_NETWORK, 22);
        i_network_viewer = ModernIcons.get(ModernIcons.VIEWER, 22);
        ImageIcon i_open_output = ModernIcons.get(ModernIcons.OPEN_OUTPUT, 22);
        ImageIcon i_clear_output = ModernIcons.get(ModernIcons.CLEAR_OUTPUT, 22);
        ImageIcon i_save_output = ModernIcons.get(ModernIcons.SAVE_OUTPUT, 22);
        ImageIcon i_transpose = ModernIcons.get(ModernIcons.TRANSPOSE, 22);
        ImageIcon i_symmetrize = ModernIcons.get(ModernIcons.SYMMETRIZE, 22);
        ImageIcon i_new_from_chain = ModernIcons.get(
                ModernIcons.NEW_FROM_CHAIN, 22);
        ImageIcon i_renumber_nodes = ModernIcons.get(ModernIcons.RENUMBER, 22);

        // 2.1.3: with the classic preference, the old GIFs take over the
        // rest state too (easy revert path)
        if (MainFrame.isClassicToolbarIcons())
            {
            i_new_network = Environment.getButtonImageIcon("NewNetwork.gif");
            i_open_network = Environment.getButtonImageIcon("OpenNetwork.gif");
            i_save_network = Environment.getButtonImageIcon("SaveNetwork.gif");
            i_network_viewer = Environment
                    .getButtonImageIcon("NetworkViewer.gif");
            i_open_output = Environment.getButtonImageIcon("OpenOutput.gif");
            i_clear_output = Environment.getButtonImageIcon("ClearOutput.gif");
            i_save_output = Environment.getButtonImageIcon("SaveOutput.gif");
            i_transpose = Environment.getButtonImageIcon("Transpose.gif");
            i_symmetrize = Environment.getButtonImageIcon("Symmetrize.gif");
            i_new_from_chain = Environment
                    .getButtonImageIcon("NewFromChain.gif");
            i_renumber_nodes = Environment
                    .getButtonImageIcon("RenumberNodes.gif");
            }

        // 2.1.3: rollover = a clearly more colorful variant; the classic
        // GIF rollovers are used when the preference is set
        ImageIcon ir_new_network;
        ImageIcon ir_open_network;
        ImageIcon ir_new_from_chain;
        ImageIcon ir_open_output;
        ImageIcon ir_clear_output;
        ImageIcon ir_save_output;
        ImageIcon ir_transpose;
        ImageIcon ir_symmetrize;
        ImageIcon ir_renumber_nodes;
        if (MainFrame.isClassicToolbarIcons())
            {
            ir_new_network = Environment
                    .getButtonImageIcon("rNewNetwork.gif");
            ir_open_network = Environment
                    .getButtonImageIcon("rOpenNetwork.gif");
            ImageIcon ir_new_from_chain_cl = Environment
                    .getButtonImageIcon("rNewFromChain.gif");
            ir_new_from_chain = ir_new_from_chain_cl;
            ir_save_network = Environment
                    .getButtonImageIcon("rSaveNetwork.gif");
            ir_network_viewer = Environment
                    .getButtonImageIcon("rNetworkViewer.gif");
            ImageIcon ir_open_output_cl = Environment
                    .getButtonImageIcon("rOpenOutput.gif");
            ir_open_output = ir_open_output_cl;
            ImageIcon ir_clear_output_cl = Environment
                    .getButtonImageIcon("rClearOutput.gif");
            ir_clear_output = ir_clear_output_cl;
            ImageIcon ir_save_output_cl = Environment
                    .getButtonImageIcon("rSaveOutput.gif");
            ir_save_output = ir_save_output_cl;
            ImageIcon ir_transpose_cl = Environment
                    .getButtonImageIcon("rTranspose.gif");
            ir_transpose = ir_transpose_cl;
            ImageIcon ir_symmetrize_cl = Environment
                    .getButtonImageIcon("rSymmetrize.gif");
            ir_symmetrize = ir_symmetrize_cl;
            ImageIcon ir_renumber_nodes_cl = Environment
                    .getButtonImageIcon("rRenumberNodes.gif");
            ir_renumber_nodes = ir_renumber_nodes_cl;
            } else
            {
            ir_new_network = ModernIcons.get(ModernIcons.NEW_NETWORK, 22,
                    true);
            ir_open_network = ModernIcons.get(ModernIcons.OPEN_NETWORK, 22,
                    true);
            ir_new_from_chain = ModernIcons.get(ModernIcons.NEW_FROM_CHAIN,
                    22, true);
            ir_save_network = ModernIcons.get(ModernIcons.SAVE_NETWORK, 22,
                    true);
            ir_network_viewer = ModernIcons.get(ModernIcons.VIEWER, 22, true);
            ir_open_output = ModernIcons.get(ModernIcons.OPEN_OUTPUT, 22,
                    true);
            ir_clear_output = ModernIcons.get(ModernIcons.CLEAR_OUTPUT, 22,
                    true);
            ir_save_output = ModernIcons.get(ModernIcons.SAVE_OUTPUT, 22,
                    true);
            ir_transpose = ModernIcons.get(ModernIcons.TRANSPOSE, 22, true);
            ir_symmetrize = ModernIcons.get(ModernIcons.SYMMETRIZE, 22, true);
            ir_renumber_nodes = ModernIcons.get(ModernIcons.RENUMBER, 22,
                    true);
            }

        tool_new_network = new JButton(i_new_network);
        tool_open_network = new JButton(i_open_network);
        tool_new_from_chain = new JButton(i_new_from_chain);
        tool_save_network = new JButton(i_save_network);
        tool_viewer = new JButton(i_network_viewer);
        tool_open_output = new JButton(i_open_output);
        tool_clear_output = new JButton(i_clear_output);
        tool_save_output = new JButton(i_save_output);
        tool_transpose = new JButton(i_transpose);
        tool_symmetrize = new JButton(i_symmetrize);
        tool_renumber_nodes = new JButton(i_renumber_nodes);

        tool_new_network.setRolloverIcon(ir_new_network);
        tool_open_network.setRolloverIcon(ir_open_network);
        tool_new_from_chain.setRolloverIcon(ir_new_from_chain);
        tool_save_network.setRolloverIcon(ir_save_network);
        tool_viewer.setRolloverIcon(ir_network_viewer);
        tool_open_output.setRolloverIcon(ir_open_output);
        tool_clear_output.setRolloverIcon(ir_clear_output);
        tool_save_output.setRolloverIcon(ir_save_output);
        tool_transpose.setRolloverIcon(ir_transpose);
        tool_symmetrize.setRolloverIcon(ir_symmetrize);
        tool_renumber_nodes.setRolloverIcon(ir_renumber_nodes);

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
        JPanel tools_panel = new JPanel();
        tools_panel.setLayout(new BorderLayout());
        tools_panel.add(tools, BorderLayout.WEST);

        controlArea.add(tools_panel, BorderLayout.NORTH);
        my_frame.pack();
        my_frame.setVisible(true);
        my_full_net.setChanged(false);
        }
    }