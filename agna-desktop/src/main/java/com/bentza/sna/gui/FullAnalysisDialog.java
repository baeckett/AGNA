/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.net.Network;
import com.bentza.sna.report.FullAnalysisOptions;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Interactive dialog allowing users to configure the batteries of analyses
 * included in the Full Analysis report.
 */
public class FullAnalysisDialog extends JDialog {
    private final Network network;
    private final Consumer<FullAnalysisOptions> onRun;

    // Standard checkboxes
    private JCheckBox cbBasic;
    private JCheckBox cbDegree;
    private JCheckBox cbPrestige;
    private JCheckBox cbCentrality;
    private JCheckBox cbSpectral;
    private JCheckBox cbClustering;
    private JCheckBox cbHoles;
    private JCheckBox cbConnectivity;
    private JCheckBox cbAssortativity;
    private JCheckBox cbCommunities;

    // Extended checkboxes & inputs
    private JCheckBox cbBonacich;
    private JTextField tfBonacichBeta;
    private JCheckBox cbPageRank;
    private JTextField tfPageRankDamping;
    private JCheckBox cbTriad;
    private JCheckBox cbCliques;
    private JSpinner spCliqueDiameter;
    private JCheckBox cbEquivalence;
    private JCheckBox cbWeighted;

    public FullAnalysisDialog(Frame owner, Network network, Consumer<FullAnalysisOptions> onRun) {
        super(owner, "Full Analysis Battery Configuration", true);
        this.network = network;
        this.onRun = onRun;

        initComponents();
        pack();
        setMinimumSize(new Dimension(640, 560));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));

        JLabel titleLabel = new JLabel("Full Analysis Battery");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(0, 64, 128));

        String netInfo = (network != null)
                ? String.format("Network: <b>%s</b> (%d actors)",
                        network.getName() != null && !network.getName().isEmpty() ? network.getName() : "Current Network",
                        network.getSize())
                : "No active network";

        JLabel subLabel = new JLabel("<html>Select the sociometric modules to execute for this analysis run.<br>" + netInfo + "</html>");
        subLabel.setFont(subLabel.getFont().deriveFont(12f));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content with ScrollPane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        // Presets bar
        JPanel presetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        presetsPanel.setBorder(BorderFactory.createTitledBorder("Quick Selection Presets"));
        JButton btnStandard = new JButton("Standard Profile (Recommended)");
        JButton btnAll = new JButton("Select All");
        JButton btnClear = new JButton("Clear All");

        presetsPanel.add(btnStandard);
        presetsPanel.add(btnAll);
        presetsPanel.add(btnClear);
        contentPanel.add(presetsPanel);
        contentPanel.add(Box.createVerticalStrut(6));

        // Standard Panel
        JPanel stdPanel = new JPanel(new GridLayout(0, 1, 2, 4));
        stdPanel.setBorder(BorderFactory.createTitledBorder("Standard Modules (Polynomial & Fast)"));

        cbBasic = new JCheckBox("Basic Structural Metrics & Density (Nodes, Density, Reciprocity)", true);
        cbBasic.setToolTipText(EducationalTooltips.getTooltip("basic_metrics"));
        cbDegree = new JCheckBox("Degree Distributions (Nodal Degree, In-Degree, Out-Degree)", true);
        cbDegree.setToolTipText(EducationalTooltips.getTooltip("nodal_degree"));
        cbPrestige = new JCheckBox("Sociometric Status & Prestige Indices", true);
        cbPrestige.setToolTipText(EducationalTooltips.getTooltip("prestige"));
        cbCentrality = new JCheckBox("Centrality & Distances (Betweenness, Closeness, Geodesics Matrix, Diameter)", true);
        cbCentrality.setToolTipText(EducationalTooltips.getTooltip("betweenness"));
        cbSpectral = new JCheckBox("Spectral Centrality (Principal Eigenvector Centrality)", true);
        cbSpectral.setToolTipText(EducationalTooltips.getTooltip("eigenvector"));
        cbClustering = new JCheckBox("Clustering & Cohesion (Watts-Strogatz Local Clustering, Transitivity, k-Cores)", true);
        cbClustering.setToolTipText(EducationalTooltips.getTooltip("local_clustering"));
        cbHoles = new JCheckBox("Social Capital & Structural Holes (Burt's Constraint, Effective Size, Efficiency)", true);
        cbHoles.setToolTipText(EducationalTooltips.getTooltip("open_chains"));
        cbConnectivity = new JCheckBox("Connectivity & Dyads (Components, Cut-Vertices, Bridges, MAN Dyad Census)", true);
        cbConnectivity.setToolTipText(EducationalTooltips.getTooltip("dyad_census"));
        cbAssortativity = new JCheckBox("Degree Assortativity & Mixing Patterns (Newman r)", true);
        cbAssortativity.setToolTipText(EducationalTooltips.getTooltip("assortativity"));
        cbCommunities = new JCheckBox("Community Detection & Modularity Partitioning (Louvain Q)", true);
        cbCommunities.setToolTipText(EducationalTooltips.getTooltip("communities"));

        stdPanel.add(cbBasic);
        stdPanel.add(cbDegree);
        stdPanel.add(cbPrestige);
        stdPanel.add(cbCentrality);
        stdPanel.add(cbSpectral);
        stdPanel.add(cbClustering);
        stdPanel.add(cbHoles);
        stdPanel.add(cbConnectivity);
        stdPanel.add(cbAssortativity);
        stdPanel.add(cbCommunities);
        contentPanel.add(stdPanel);
        contentPanel.add(Box.createVerticalStrut(6));

        // Extended Panel
        JPanel extPanel = new JPanel(new GridBagLayout());
        extPanel.setBorder(BorderFactory.createTitledBorder("Extended Modules (Parameterized / Computationally Intensive)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Bonacich
        cbBonacich = new JCheckBox("Bonacich Power Centrality");
        cbBonacich.setToolTipText(EducationalTooltips.getTooltip("bonacich"));
        tfBonacichBeta = new JTextField("0.10", 5);
        JPanel bonaP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bonaP.add(cbBonacich);
        bonaP.add(new JLabel("  Attenuation (β): "));
        bonaP.add(tfBonacichBeta);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        extPanel.add(bonaP, gbc);

        // PageRank
        cbPageRank = new JCheckBox("PageRank Centrality");
        cbPageRank.setToolTipText(EducationalTooltips.getTooltip("pagerank"));
        tfPageRankDamping = new JTextField("0.85", 5);
        JPanel prP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        prP.add(cbPageRank);
        prP.add(new JLabel("  Damping factor (d): "));
        prP.add(tfPageRankDamping);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        extPanel.add(prP, gbc);

        // Triad Census
        cbTriad = new JCheckBox("Holland-Leinhardt 16 Triad Census");
        cbTriad.setToolTipText(EducationalTooltips.getTooltip("dyad_census"));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        extPanel.add(cbTriad, gbc);

        // Maximal Cliques
        cbCliques = new JCheckBox("Maximal Cliques (Bron-Kerbosch)");
        cbCliques.setToolTipText(EducationalTooltips.getTooltip("cliques"));
        spCliqueDiameter = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JPanel clqP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        clqP.add(cbCliques);
        clqP.add(new JLabel("  Diameter: "));
        clqP.add(spCliqueDiameter);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        extPanel.add(clqP, gbc);

        cbEquivalence = new JCheckBox("Structural Equivalence & Blockmodeling (Burt profile correlation / distance)");
        cbEquivalence.setToolTipText(EducationalTooltips.getTooltip("structural_equivalence"));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        extPanel.add(cbEquivalence, gbc);

        cbWeighted = new JCheckBox("Valued & Weighted Analysis (Node Strength & Valued Density)");
        cbWeighted.setToolTipText(EducationalTooltips.getTooltip("weighted_metrics"));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        extPanel.add(cbWeighted, gbc);

        JLabel clqWarn = new JLabel("<html><small style='color:#a04000;'>⚠️ <i>Warning: Clique enumeration is NP-hard and may be slow on dense networks.</i></small></html>");
        gbc.insets = new Insets(4, 24, 2, 0);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        extPanel.add(clqWarn, gbc);

        contentPanel.add(extPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Button Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRun = new JButton("Run Selected Battery");
        btnRun.setFont(btnRun.getFont().deriveFont(Font.BOLD));
        JButton btnCancel = new JButton("Cancel");

        bottomPanel.add(btnRun);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        // Preset actions
        btnStandard.addActionListener(e -> setStandardPreset());
        btnAll.addActionListener(e -> setAllPreset(true));
        btnClear.addActionListener(e -> setAllPreset(false));

        btnCancel.addActionListener(e -> dispose());
        btnRun.addActionListener(e -> onRunClicked());

        getRootPane().setDefaultButton(btnRun);
    }

    private void setStandardPreset() {
        cbBasic.setSelected(true);
        cbDegree.setSelected(true);
        cbPrestige.setSelected(true);
        cbCentrality.setSelected(true);
        cbSpectral.setSelected(true);
        cbClustering.setSelected(true);
        cbHoles.setSelected(true);
        cbConnectivity.setSelected(true);
        cbAssortativity.setSelected(true);
        cbCommunities.setSelected(true);

        cbBonacich.setSelected(false);
        cbPageRank.setSelected(false);
        cbTriad.setSelected(false);
        cbCliques.setSelected(false);
        cbEquivalence.setSelected(false);
        cbWeighted.setSelected(false);
    }

    private void setAllPreset(boolean selected) {
        cbBasic.setSelected(selected);
        cbDegree.setSelected(selected);
        cbPrestige.setSelected(selected);
        cbCentrality.setSelected(selected);
        cbSpectral.setSelected(selected);
        cbClustering.setSelected(selected);
        cbHoles.setSelected(selected);
        cbConnectivity.setSelected(selected);
        cbAssortativity.setSelected(selected);
        cbCommunities.setSelected(selected);
        cbBonacich.setSelected(selected);
        cbPageRank.setSelected(selected);
        cbTriad.setSelected(selected);
        cbCliques.setSelected(selected);
        cbEquivalence.setSelected(selected);
        cbWeighted.setSelected(selected);
    }

    private void onRunClicked() {
        // Validate at least one selection
        boolean anySelected = cbBasic.isSelected() || cbDegree.isSelected() || cbPrestige.isSelected()
                || cbCentrality.isSelected() || cbSpectral.isSelected() || cbClustering.isSelected()
                || cbHoles.isSelected() || cbConnectivity.isSelected() || cbBonacich.isSelected()
                || cbPageRank.isSelected() || cbTriad.isSelected() || cbCliques.isSelected()
                || cbAssortativity.isSelected() || cbCommunities.isSelected()
                || cbEquivalence.isSelected() || cbWeighted.isSelected();

        if (!anySelected) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one analysis module to run.",
                    "No Modules Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double beta = 0.10;
        if (cbBonacich.isSelected()) {
            try {
                beta = Double.parseDouble(tfBonacichBeta.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid numeric value for Bonacich Beta (e.g. 0.10).",
                        "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
                tfBonacichBeta.requestFocus();
                return;
            }
        }

        double damping = 0.85;
        if (cbPageRank.isSelected()) {
            try {
                damping = Double.parseDouble(tfPageRankDamping.getText().trim());
                if (damping <= 0.0 || damping >= 1.0) {
                    JOptionPane.showMessageDialog(this,
                            "PageRank damping factor must be between 0.0 and 1.0 (typically 0.85).",
                            "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
                    tfPageRankDamping.requestFocus();
                    return;
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid numeric value for PageRank damping (e.g. 0.85).",
                        "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
                tfPageRankDamping.requestFocus();
                return;
            }
        }

        int diameter = ((Number) spCliqueDiameter.getValue()).intValue();

        FullAnalysisOptions options = new FullAnalysisOptions();
        options.setIncludeBasicAndDensity(cbBasic.isSelected());
        options.setIncludeDegreeMetrics(cbDegree.isSelected());
        options.setIncludePrestige(cbPrestige.isSelected());
        options.setIncludeGeodesicCentralities(cbCentrality.isSelected());
        options.setIncludeSpectralCentrality(cbSpectral.isSelected());
        options.setIncludeClusteringAndCores(cbClustering.isSelected());
        options.setIncludeStructuralHoles(cbHoles.isSelected());
        options.setIncludeConnectivityAndDyad(cbConnectivity.isSelected());

        options.setIncludeBonacich(cbBonacich.isSelected());
        options.setBonacichBeta(beta);

        options.setIncludePageRank(cbPageRank.isSelected());
        options.setPageRankDamping(damping);

        options.setIncludeTriadCensus(cbTriad.isSelected());

        options.setIncludeCliques(cbCliques.isSelected());
        options.setCliqueDiameter(diameter);

        options.setIncludeAssortativity(cbAssortativity.isSelected());
        options.setIncludeCommunityDetection(cbCommunities.isSelected());
        options.setIncludeStructuralEquivalence(cbEquivalence.isSelected());
        options.setIncludeWeightedMetrics(cbWeighted.isSelected());

        dispose();
        if (onRun != null) {
            onRun.accept(options);
        }
    }
}
