package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import com.bentza.sna.net.Actor;
import com.bentza.sna.net.Network;
import com.bentza.sna.AgnaThread;
import java.io.*;
import java.awt.*;
import java.lang.Math.*;
import java.lang.System.*;
import java.lang.Object.*;
import java.awt.event.*;
import java.awt.Event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.JFileChooser;
import java.awt.image.*;
import java.awt.Graphics2D.*;
import java.util.*;
import java.awt.datatransfer.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.text.rtf.*;
import javax.swing.text.html.*;
import javax.swing.text.*;
import java.lang.reflect.Array;
import javax.swing.plaf.*;
import java.text.*;
import java.awt.image.*;
import javax.swing.filechooser.*;
import javax.swing.text.*;
import java.beans.*;

import java.net.*;

/**
 * A toolbar displayed vertically on Network Viewer's left side; used for the
 * search-nodes option; a subclass of JPanel;
 */
public class VerticalToolBar extends JPanel
    {

    private static final int this_width = 150;

    /**
     * Dimension of this VerticalToolBar
     */
    private static final Dimension this_dimension = new Dimension(this_width,
            200);

    private final String START_TOOL_TIP = "Start node search";

    private final String STOP_TOOL_TIP = "Stop searching";

    /**
     * Dimension of the IndexedJButton componets used to display search results
     */
    private static final Dimension list_button_dim = new Dimension(
            this_width - 10, 15);

    /**
     * Dimension of search_field, results_panel and top_panel
     */
    private static final Dimension field_dimension = new Dimension(
            this_width - 10, 25);

    /**
     * Indicates whether the Thread in doSearchNodes() is running or not;
     */
    private boolean is_searching;

    private final JButton search_button, clear_all_button;

    private final JTextField search_field;

    private final JPanel results_panel;

    private final JCheckBox clear_before_box, caps_box, match_name_box;

    private final JLabel title_label;

    /**
     * Listens to the VerticalToolBar's components
     */
    private ActionListener act_vertical_toolbar = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
                {

                if (e.getSource() == search_button
                        || e.getSource() == search_field)
                    {
                    // begin search
                    doManageSearch();
                    }

                if (e.getSource() == clear_all_button)
                    {
                    // clear results panel
                    doClearResults();
                    }

                }
        };

    /**
     * Listens to change in search_field's focus and starts searching if needed
     */
    private FocusListener focus_change = new FocusListener()
        {
            public void focusLost(FocusEvent e)
                {
                /*
                 * if (e.getSource()==search_field) { // begin search
                 * doManageSearch(); }
                 */
                }

            public void focusGained(FocusEvent e)
                {
                if (e.getSource() == search_field)
                    {
                    search_field.selectAll();
                    }
                }
        };

    /**
     * Removes all result-IndexedJButton from the results_panel
     */
    public void clearResults()
        {
        results_panel.removeAll();
        results_panel.setPreferredSize(field_dimension);
        // this.repaint();

        
        }

    public Dimension getThisDimension()
        {
        return this_dimension;
        }

    /**
     * Inserts a JLabel on the results_panel with the text "No results"
     */
    private void addNoResultsButton()
        {
        // adding No Results label
        JLabel no_results_label = new JLabel("No results", SwingConstants.LEFT);
        no_results_label.setBorder(null);
        results_panel.add(no_results_label);
        }

    /**
     * Inserts an IndexedJButton on the results_panel corresponding to a
     * specific node
     */
    private void addFoundNode(Actor new_node)
        {
        IndexedJButton new_button;
        if (new_node != null)
            {
            new_button = new IndexedJButton(new_node, list_button_dim);
            results_panel.add(new_button);

            // allow overriding IndexedJButton's getToolTipText():
            ToolTipManager.sharedInstance().registerComponent(new_button);
            }

        // this.setPreferredSize(new Dimension( (int)this_dimension.getWidth(),
        // (int)(this.getPreferredSize().getHeight() + 16) ));
        // results_panel.setPreferredSize(new Dimension(
        // (int)this_dimension.getWidth(),
        // (int)(results_panel.getPreferredSize().getHeight() + 16) ));
        }

    /**
     * Removes an IndexedJButton from the results_panel
     */
    public void removeIndexedJButton(IndexedJButton tmp_button)
        {
        results_panel.remove(tmp_button);
        int results_count = results_panel.getComponentCount();
        if (results_count < 0)
            results_count = 0;
        // adjusting height according to number of nodes found:
        setPreferredSize(new Dimension(
                (int) this_dimension.getWidth(),
                (int) (list_button_dim.getHeight() * ((double) results_count + 4))));
        this.revalidate();
        this.repaint();
        }

    /**
     * affects the boolean variable is_searching and search_button's appearance;
     * will be accessed from the doSearchNode() thread;
     */
    private void setSearching(boolean tmp_searching)
        {
        is_searching = tmp_searching;
        search_button.setSelected(tmp_searching);
        if (tmp_searching)
            {
            search_button.setToolTipText(STOP_TOOL_TIP);
            search_field.setEnabled(false);
            caps_box.setEnabled(false);
            clear_before_box.setEnabled(false);
            match_name_box.setEnabled(false);
            clear_all_button.setEnabled(false);
            } else
            {
            search_button.setToolTipText(START_TOOL_TIP);
            search_field.setEnabled(true);
            caps_box.setEnabled(true);
            clear_before_box.setEnabled(true);
            match_name_box.setEnabled(true);
            clear_all_button.setEnabled(true);
            }
        }

    private boolean isSearching()
        {
        return is_searching;
        }

    /**
     * Starts or stops the search actions
     */
    private void doManageSearch()
        {
        if (isSearching())
            {
            setSearching(false);
            } else
            {
            setSearching(true);
            doSearchNodes();
            }

        }

    /**
     * Clears results_panel
     */
    private void doClearResults()
        {
        if (!isSearching())
            {
            results_panel.removeAll();
            results_panel.repaint();
            }
        }

    /**
     * Updates the text of each IndexedJButton corresponding to a given Actor
     */
    private void updateButtonText(Actor tmp_actor)
        {
        int component_count = (int) results_panel.getComponentCount();
        IndexedJButton tmp_button = null;
        for (int i = 0; i < component_count; i++)
            {
            try
                {
                tmp_button = (IndexedJButton) results_panel.getComponent(i);
                if (tmp_button == null)
                    break;
                if (tmp_actor.equals(tmp_button.getAssociateActor()))
                    tmp_button.setText(tmp_actor.getName());
                } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
            }
        }

    /**
     * Searches the Network for specific nodes in a Thread; calls addFoundNode()
     * when a node is found
     */
    private void doSearchNodes()
        {
        final String name_piece = search_field.getText();
        if (name_piece == null || name_piece.equals(""))
            {
            setSearching(false);
            return;
            }

        if (clear_before_box.isSelected())
            results_panel.removeAll();

        // if name_piece exists:

        AgnaThread runner = new AgnaThread()
            {
                public void run()
                    {
                    setSearching(true);
                    // ProgressDialog pd = MainFrame.progress_dialog;
                    // pd.setPercent(2);

                    Network this_network = MainFrame.getCurrentNetwork();
                    final float percent_step = 100f / this_network.getSize();
                    int node_index = 0;
                    boolean at_least_one_node_found = false;
                    this.nap(); // allow cancel button to be pressed
                    final boolean match_name = match_name_box.isSelected();

                    if (caps_box.isSelected())
                        {

                        while (node_index != -1)// && !pd.getStop())
                            {
                            title_label
                                    .setText("Searching... "
                                            + String
                                                    .valueOf((int) (percent_step * node_index))
                                            + " %");
                            node_index = this_network
                                    .searchNextActorCapsSensitive(name_piece,
                                            node_index, match_name);
                            // adding node to list:
                            if (node_index != -1)
                                {
                                addFoundNode(this_network.getActor(node_index));
                                node_index++;
                                at_least_one_node_found = true;
                                }

                            this.nap(); // allow cancel button to be pressed
                            if (!isSearching())
                                {
                                break;
                                }

                            } // end while
                        } // end if
                    else
                        while (node_index != -1)// && !pd.getStop())
                            {
                            title_label
                                    .setText("Searching... "
                                            + String
                                                    .valueOf((int) (percent_step * node_index))
                                            + " %");
                            node_index = this_network
                                    .searchNextActorNotCapsSensitive(
                                            name_piece, node_index, match_name);
                            // adding node to list:
                            if (node_index != -1)
                                {
                                addFoundNode(this_network.getActor(node_index));
                                node_index++;
                                at_least_one_node_found = true;
                                }

                            this.nap(); // allow cancel button to be pressed
                            if (!isSearching())
                                {
                                break;
                                }

                            } // end while

                    /*
                     * if (pd.getStop()) { this.undecorate(); return; // exit
                     * without reading network }
                     */

                    if (!at_least_one_node_found)
                        {
                        addNoResultsButton();
                        }

                    // this.finish();
                    setSearching(false);
                    title_label.setText("Search");
                    // adjusting height according to number of nodes found:
                    setPreferredSize(new Dimension(
                            (int) this_dimension.getWidth(),
                            (int) (list_button_dim.getHeight() * ((double) results_panel
                                    .getComponentCount() + 4))));

                    GrNet.getCurrentFrame().validate();
                    GrNet.getCurrentFrame().repaint();

                    }
            };
        runner.go();

        }

    /**
     * The sole constructor of this component; The VerticalToolBar contains 2
     * basic components: top_panel and results_panel, separated by separator;
     */
    public VerticalToolBar()
        {
        final Dimension button_dimension = new Dimension(25, 25);
        final Dimension results_dimension = new Dimension(this_width - 10, 100);

        this.setPreferredSize(this_dimension);

        JPanel top_panel = new JPanel();
        // top_panel.setBorder(new EmptyBorder(1,2,2,2));
        top_panel.setBorder(BorderFactory
                .createEtchedBorder(EtchedBorder.LOWERED));

        // top_panel.setLayout(new BoxLayout(top_panel, BoxLayout.X_AXIS));
        top_panel.setPreferredSize(results_dimension);

        GridBagLayout c_layout = new GridBagLayout();
        top_panel.setLayout(c_layout);
        GridBagConstraints co = new GridBagConstraints();
        co.insets = new Insets(2, 2, 1, 2); // adding space around components

        // making components:

        // placing title_label:
        title_label = new JLabel("Search");
        co.gridx = 0;
        co.gridy = 0;
        co.gridwidth = 6;
        co.gridheight = 1;
        co.weightx = 0.0;
        // co.weighty = 0.0;
        co.fill = GridBagConstraints.NONE;
        co.anchor = GridBagConstraints.WEST;
        c_layout.setConstraints(title_label, co);
        top_panel.add(title_label);

        // placing search_field:
        search_field = new JTextField("");
        // search_field.setPreferredSize(field_dimension);
        // search_field.setMaximumSize(field_dimension);
        search_field.setToolTipText("Type string to search nodes by name");
        search_field.addActionListener(act_vertical_toolbar);
        search_field.addFocusListener(focus_change);
        search_field.setEnabled(true);
        co.gridx = 0;
        co.gridy = 1;
        co.gridwidth = 6;
        co.gridheight = 1;
        co.weightx = 0.9;
        // co.weighty = 0.8;
        co.fill = GridBagConstraints.HORIZONTAL;
        co.anchor = GridBagConstraints.WEST;
        c_layout.setConstraints(search_field, co);
        top_panel.add(search_field);

        // placing search_button:
        search_button = new JButton(Environment
                .getButtonImageIcon("SearchNodes.gif"));
        // ImageIcon search_button_image =
        // Environment.getButtonImageIcon("SearchNodes.gif");
        search_button.setRolloverIcon(Environment
                .getButtonImageIcon("rSearchNodes.gif"));
        search_button.setSelectedIcon(Environment
                .getButtonImageIcon("StopSearching.gif"));
        search_button.setRolloverSelectedIcon(Environment
                .getButtonImageIcon("StopSearching.gif"));
        // search_button.setPreferredSize(button_dimension);
        // search_button.setMaximumSize(button_dimension);
        search_button.setToolTipText(START_TOOL_TIP);
        search_button.setBorder(null);
        // search_button.setMargin(new Insets(0,3,0,0));
        search_button.addActionListener(act_vertical_toolbar);
        search_button.setEnabled(true);
        co.gridx = 0;
        co.gridy = 2;
        co.gridwidth = 2;
        co.gridheight = 1;
        co.weightx = 0.0;
        // co.weighty = 0.0;
        co.fill = GridBagConstraints.NONE;
        co.anchor = GridBagConstraints.CENTER;
        c_layout.setConstraints(search_button, co);
        top_panel.add(search_button);

        // placing caps_box:
        caps_box = new JCheckBox(Environment
                .getButtonImageIcon("CapsSensitive.gif"));
        caps_box.setSelectedIcon(Environment
                .getButtonImageIcon("rCapsSensitive.gif"));
        caps_box.setToolTipText("Case-sensitive");
        caps_box.setSelected(false);
        caps_box.setEnabled(true);
        co.gridx = 0;
        co.gridy = 3;
        co.gridwidth = 2;
        co.gridheight = 1;
        co.weightx = 0.0;
        // co.weighty = 0.0;
        co.anchor = GridBagConstraints.WEST;
        c_layout.setConstraints(caps_box, co);
        top_panel.add(caps_box);

        // placing clear_before_box:
        clear_before_box = new JCheckBox(Environment
                .getButtonImageIcon("ClearBefore.gif"));
        clear_before_box.setSelectedIcon(Environment
                .getButtonImageIcon("rClearBefore.gif"));
        clear_before_box.setToolTipText("Clear old results on search");
        clear_before_box.setSelected(false);
        clear_before_box.setEnabled(true);
        co.gridx = 2;
        co.gridy = 3;
        co.gridwidth = 2;
        co.gridheight = 1;
        co.weightx = 0.0;
        // co.weighty = 0.0;
        c_layout.setConstraints(clear_before_box, co);
        top_panel.add(clear_before_box);

        // placing match_name_box:
        match_name_box = new JCheckBox(Environment
                .getButtonImageIcon("MatchName.gif"));
        match_name_box.setSelectedIcon(Environment
                .getButtonImageIcon("rMatchName.gif"));
        match_name_box.setToolTipText("Match exact node name");
        match_name_box.setSelected(false);
        match_name_box.setEnabled(true);
        co.gridx = 4;
        co.gridy = 3;
        co.gridwidth = 2;
        co.gridheight = 1;
        co.weightx = 0.0;
        // co.weighty = 0.0;
        c_layout.setConstraints(match_name_box, co);
        top_panel.add(match_name_box);

        // placing clear_all_button:
        clear_all_button = new JButton(Environment
                .getButtonImageIcon("ClearAllResults.gif"));
        clear_all_button.setRolloverIcon(Environment
                .getButtonImageIcon("rClearAllResults.gif"));
        clear_all_button.setBorder(null);
        clear_all_button.setToolTipText("Clear results area");
        clear_all_button.addActionListener(act_vertical_toolbar);
        clear_all_button.setEnabled(true);
        co.gridx = 2;
        co.gridy = 2;
        co.gridwidth = 2;
        co.gridheight = 1;
        co.weightx = 0.0;
        // co.weighty = 0.0;
        co.anchor = GridBagConstraints.CENTER;
        c_layout.setConstraints(clear_all_button, co);
        top_panel.add(clear_all_button);

        // building results_panel:

        results_panel = new JPanel();
        results_panel.setMinimumSize(results_dimension);
        // results_panel.setPreferredSize(new Dimension(100,25));
        results_panel.setBorder(null);
        results_panel.setLayout(new BoxLayout(results_panel, BoxLayout.Y_AXIS));

        JLabel separator = new JLabel();
        separator.setPreferredSize(new Dimension(this_width - 10, 3));

        // adding components:
        this.add(top_panel);
        this.add(separator);
        this.add(results_panel);

        this.setSearching(false);

        }

    }