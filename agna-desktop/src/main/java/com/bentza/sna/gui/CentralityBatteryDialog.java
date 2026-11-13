/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.net.Network;
import com.bentza.sna.report.CentralityOptions;
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
import javax.swing.JTextField;

/**
 * Battery dialog for configuring and executing Centrality and Prestige measures.
 */
public class CentralityBatteryDialog extends JDialog {
    private final Network network;
    private final Consumer<CentralityOptions> onRun;

    private JCheckBox cbNodal;
    private JCheckBox cbIn;
    private JCheckBox cbOut;
    private JCheckBox cbEmission;
    private JCheckBox cbReception;
    private JCheckBox cbDetermination;

    private JCheckBox cbBetweenness;
    private JCheckBox cbCloseness;
    private JCheckBox cbBavelas;
    private JCheckBox cbFareness;
    private JCheckBox cbPrestige;

    private JCheckBox cbEigenvector;
    private JCheckBox cbBonacich;
    private JTextField tfBonacichBeta;
    private JCheckBox cbPageRank;
    private JTextField tfPageRankDamping;

    public CentralityBatteryDialog(Frame owner, Network network, Consumer<CentralityOptions> onRun) {
        super(owner, "Centrality & Prestige Battery", true);
        this.network = network;
        this.onRun = onRun;

        initComponents();
        pack();
        setMinimumSize(new Dimension(600, 560));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        JLabel titleLabel = new JLabel("Centrality & Prestige Analysis Battery");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(0, 64, 128));

        String netInfo = (network != null)
                ? String.format("Network: <b>%s</b> (%d actors)",
                        network.getName() != null && !network.getName().isEmpty() ? network.getName() : "Current Network",
                        network.getSize())
                : "No active network";

        JLabel subLabel = new JLabel("<html>Select degree, geodesic, and spectral centrality metrics to compute.<br>" + netInfo + "</html>");
        subLabel.setFont(subLabel.getFont().deriveFont(12f));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        // Presets Panel
        JPanel presetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        presetsPanel.setBorder(BorderFactory.createTitledBorder("Presets"));
        JButton btnStandard = new JButton("Standard Centralities");
        JButton btnAll = new JButton("Select All");
        JButton btnClear = new JButton("Clear All");
        presetsPanel.add(btnStandard);
        presetsPanel.add(btnAll);
        presetsPanel.add(btnClear);
        contentPanel.add(presetsPanel);
        contentPanel.add(Box.createVerticalStrut(6));

        // Degree centralities panel
        JPanel degPanel = new JPanel(new GridLayout(0, 2, 8, 4));
        degPanel.setBorder(BorderFactory.createTitledBorder("Degree-Based Centrality & Activity"));
        cbNodal = new JCheckBox("Nodal Degree (Total)", true);
        cbNodal.setToolTipText(EducationalTooltips.getTooltip("nodal_degree"));
        cbIn = new JCheckBox("In-Degree Centrality", true);
        cbIn.setToolTipText(EducationalTooltips.getTooltip("indegree"));
        cbOut = new JCheckBox("Out-Degree Centrality", true);
        cbOut.setToolTipText(EducationalTooltips.getTooltip("outdegree"));
        cbEmission = new JCheckBox("Emission Degree", false);
        cbEmission.setToolTipText(EducationalTooltips.getTooltip("emission_degree"));
        cbReception = new JCheckBox("Reception Degree", false);
        cbReception.setToolTipText(EducationalTooltips.getTooltip("reception_degree"));
        cbDetermination = new JCheckBox("Determination Degree", false);
        cbDetermination.setToolTipText(EducationalTooltips.getTooltip("determination_degree"));
        degPanel.add(cbNodal);
        degPanel.add(cbIn);
        degPanel.add(cbOut);
        degPanel.add(cbEmission);
        degPanel.add(cbReception);
        degPanel.add(cbDetermination);
        contentPanel.add(degPanel);
        contentPanel.add(Box.createVerticalStrut(6));

        // Geodesic centralities panel
        JPanel geoPanel = new JPanel(new GridLayout(0, 2, 8, 4));
        geoPanel.setBorder(BorderFactory.createTitledBorder("Geodesic & Distance Centralities"));
        cbBetweenness = new JCheckBox("Betweenness Centrality", true);
        cbBetweenness.setToolTipText(EducationalTooltips.getTooltip("betweenness"));
        cbCloseness = new JCheckBox("Closeness Centrality", true);
        cbCloseness.setToolTipText(EducationalTooltips.getTooltip("closeness"));
        cbPrestige = new JCheckBox("Prestige & Sociometric Status", true);
        cbPrestige.setToolTipText(EducationalTooltips.getTooltip("prestige"));
        cbBavelas = new JCheckBox("Bavelas-Leavitt Centrality", false);
        cbBavelas.setToolTipText(EducationalTooltips.getTooltip("bavelas"));
        cbFareness = new JCheckBox("Fareness Index", false);
        cbFareness.setToolTipText(EducationalTooltips.getTooltip("fareness"));
        geoPanel.add(cbBetweenness);
        geoPanel.add(cbCloseness);
        geoPanel.add(cbPrestige);
        geoPanel.add(cbBavelas);
        geoPanel.add(cbFareness);
        contentPanel.add(geoPanel);
        contentPanel.add(Box.createVerticalStrut(6));

        // Spectral centralities panel
        JPanel specPanel = new JPanel(new GridBagLayout());
        specPanel.setBorder(BorderFactory.createTitledBorder("Spectral & Influence Centralities"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        cbEigenvector = new JCheckBox("Principal Eigenvector Centrality", true);
        cbEigenvector.setToolTipText(EducationalTooltips.getTooltip("eigenvector"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        specPanel.add(cbEigenvector, gbc);

        cbBonacich = new JCheckBox("Bonacich Power Centrality");
        cbBonacich.setToolTipText(EducationalTooltips.getTooltip("bonacich"));
        tfBonacichBeta = new JTextField("0.10", 5);
        JPanel bonaP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bonaP.add(cbBonacich);
        bonaP.add(new JLabel("  Attenuation (β): "));
        bonaP.add(tfBonacichBeta);
        gbc.gridy = 1;
        specPanel.add(bonaP, gbc);

        cbPageRank = new JCheckBox("PageRank Centrality");
        cbPageRank.setToolTipText(EducationalTooltips.getTooltip("pagerank"));
        tfPageRankDamping = new JTextField("0.85", 5);
        JPanel prP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        prP.add(cbPageRank);
        prP.add(new JLabel("  Damping factor (d): "));
        prP.add(tfPageRankDamping);
        gbc.gridy = 2;
        specPanel.add(prP, gbc);

        contentPanel.add(specPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRun = new JButton("Run Centrality Battery");
        btnRun.setFont(btnRun.getFont().deriveFont(Font.BOLD));
        JButton btnCancel = new JButton("Cancel");
        bottomPanel.add(btnRun);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        btnStandard.addActionListener(e -> setStandardPreset());
        btnAll.addActionListener(e -> setAllPreset(true));
        btnClear.addActionListener(e -> setAllPreset(false));

        btnCancel.addActionListener(e -> dispose());
        btnRun.addActionListener(e -> onRunClicked());
        getRootPane().setDefaultButton(btnRun);
    }

    private void setStandardPreset() {
        cbNodal.setSelected(true);
        cbIn.setSelected(true);
        cbOut.setSelected(true);
        cbEmission.setSelected(false);
        cbReception.setSelected(false);
        cbDetermination.setSelected(false);
        cbBetweenness.setSelected(true);
        cbCloseness.setSelected(true);
        cbPrestige.setSelected(true);
        cbBavelas.setSelected(false);
        cbFareness.setSelected(false);
        cbEigenvector.setSelected(true);
        cbBonacich.setSelected(false);
        cbPageRank.setSelected(false);
    }

    private void setAllPreset(boolean sel) {
        cbNodal.setSelected(sel);
        cbIn.setSelected(sel);
        cbOut.setSelected(sel);
        cbEmission.setSelected(sel);
        cbReception.setSelected(sel);
        cbDetermination.setSelected(sel);
        cbBetweenness.setSelected(sel);
        cbCloseness.setSelected(sel);
        cbPrestige.setSelected(sel);
        cbBavelas.setSelected(sel);
        cbFareness.setSelected(sel);
        cbEigenvector.setSelected(sel);
        cbBonacich.setSelected(sel);
        cbPageRank.setSelected(sel);
    }

    private void onRunClicked() {
        boolean any = cbNodal.isSelected() || cbIn.isSelected() || cbOut.isSelected()
                || cbEmission.isSelected() || cbReception.isSelected() || cbDetermination.isSelected()
                || cbBetweenness.isSelected() || cbCloseness.isSelected() || cbPrestige.isSelected()
                || cbBavelas.isSelected() || cbFareness.isSelected() || cbEigenvector.isSelected()
                || cbBonacich.isSelected() || cbPageRank.isSelected();

        if (!any) {
            JOptionPane.showMessageDialog(this, "Please select at least one centrality measure.",
                    "No Measures Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double beta = 0.10;
        if (cbBonacich.isSelected()) {
            try {
                beta = Double.parseDouble(tfBonacichBeta.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid Bonacich Beta parameter.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        double damping = 0.85;
        if (cbPageRank.isSelected()) {
            try {
                damping = Double.parseDouble(tfPageRankDamping.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid PageRank damping parameter.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        CentralityOptions opt = new CentralityOptions();
        opt.setIncludeNodalDegree(cbNodal.isSelected());
        opt.setIncludeInDegree(cbIn.isSelected());
        opt.setIncludeOutDegree(cbOut.isSelected());
        opt.setIncludeEmission(cbEmission.isSelected());
        opt.setIncludeReception(cbReception.isSelected());
        opt.setIncludeDetermination(cbDetermination.isSelected());
        opt.setIncludeBetweenness(cbBetweenness.isSelected());
        opt.setIncludeCloseness(cbCloseness.isSelected());
        opt.setIncludePrestige(cbPrestige.isSelected());
        opt.setIncludeBavelas(cbBavelas.isSelected());
        opt.setIncludeFareness(cbFareness.isSelected());
        opt.setIncludeEigenvector(cbEigenvector.isSelected());
        opt.setIncludeBonacich(cbBonacich.isSelected());
        opt.setBonacichBeta(beta);
        opt.setIncludePageRank(cbPageRank.isSelected());
        opt.setPageRankDamping(damping);

        dispose();
        if (onRun != null) onRun.accept(opt);
    }
}
