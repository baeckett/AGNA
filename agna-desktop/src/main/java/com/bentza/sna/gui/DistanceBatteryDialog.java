/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.net.Network;
import com.bentza.sna.report.DistanceOptions;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Battery dialog for configuring and executing Geodesic and Path Distance calculations.
 */
public class DistanceBatteryDialog extends JDialog {
    private final Network network;
    private final Consumer<DistanceOptions> onRun;

    private JCheckBox cbGeodesics;
    private JCheckBox cbDiameter;
    private JCheckBox cbEccentricity;
    private JCheckBox cbSpecificPath;
    private JComboBox<String> comboFrom;
    private JComboBox<String> comboTo;
    private JCheckBox cbAllPaths;

    public DistanceBatteryDialog(Frame owner, Network network, Consumer<DistanceOptions> onRun) {
        super(owner, "Distances & Geodesics Battery", true);
        this.network = network;
        this.onRun = onRun;

        initComponents();
        pack();
        setMinimumSize(new Dimension(560, 480));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        JLabel titleLabel = new JLabel("Distances & Geodesics Analysis Battery");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(0, 64, 128));

        String netInfo = (network != null)
                ? String.format("Network: <b>%s</b> (%d actors)",
                        network.getName() != null && !network.getName().isEmpty() ? network.getName() : "Current Network",
                        network.getSize())
                : "No active network";

        JLabel subLabel = new JLabel("<html>Compute shortest paths, geodesic distances, diameter, and node eccentricities.<br>" + netInfo + "</html>");
        subLabel.setFont(subLabel.getFont().deriveFont(12f));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        // Macro distance panel
        JPanel macroPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        macroPanel.setBorder(BorderFactory.createTitledBorder("Distance Matrices & Bounds"));
        cbGeodesics = new JCheckBox("Geodesic Distance Matrix (All pairs shortest path lengths)", true);
        cbGeodesics.setToolTipText(EducationalTooltips.getTooltip("geodesics"));
        cbDiameter = new JCheckBox("Network Diameter & Average Path Length", true);
        cbDiameter.setToolTipText(EducationalTooltips.getTooltip("diameter"));
        cbEccentricity = new JCheckBox("Node Eccentricity, Radius, Center, and Periphery", true);
        cbEccentricity.setToolTipText(EducationalTooltips.getTooltip("eccentricity"));
        macroPanel.add(cbGeodesics);
        macroPanel.add(cbDiameter);
        macroPanel.add(cbEccentricity);
        contentPanel.add(macroPanel);
        contentPanel.add(Box.createVerticalStrut(6));

        // Specific paths panel
        JPanel pathPanel = new JPanel(new GridBagLayout());
        pathPanel.setBorder(BorderFactory.createTitledBorder("Specific Shortest Path Enumeration"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbSpecificPath = new JCheckBox("Enumerate shortest paths between specific pair");
        cbSpecificPath.setToolTipText(EducationalTooltips.getTooltip("shortest_paths"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pathPanel.add(cbSpecificPath, gbc);

        int n = network != null ? network.getSize() : 0;
        String[] actorNames = new String[n];
        for (int i = 0; i < n; i++) {
            actorNames[i] = (i + 1) + ". " + network.getActor(i).getName();
        }

        JPanel pairPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pairPanel.add(new JLabel("  From:"));
        comboFrom = new JComboBox<>(actorNames);
        pairPanel.add(comboFrom);
        pairPanel.add(new JLabel("  To:"));
        comboTo = new JComboBox<>(actorNames);
        if (actorNames.length > 1) comboTo.setSelectedIndex(1);
        pairPanel.add(comboTo);

        gbc.gridy = 1;
        pathPanel.add(pairPanel, gbc);

        cbAllPaths = new JCheckBox("Enumerate all shortest paths for all pairs (Warning: extensive output on large nets)");
        gbc.gridy = 2;
        pathPanel.add(cbAllPaths, gbc);

        contentPanel.add(pathPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRun = new JButton("Run Distance Battery");
        btnRun.setFont(btnRun.getFont().deriveFont(Font.BOLD));
        JButton btnCancel = new JButton("Cancel");
        bottomPanel.add(btnRun);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnRun.addActionListener(e -> onRunClicked());
        getRootPane().setDefaultButton(btnRun);
    }

    private void onRunClicked() {
        boolean any = cbGeodesics.isSelected() || cbDiameter.isSelected() || cbEccentricity.isSelected()
                || cbSpecificPath.isSelected() || cbAllPaths.isSelected();

        if (!any) {
            JOptionPane.showMessageDialog(this, "Please select at least one distance measure.",
                    "No Measures Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DistanceOptions opt = new DistanceOptions();
        opt.setIncludeGeodesicsMatrix(cbGeodesics.isSelected());
        opt.setIncludeDiameter(cbDiameter.isSelected());
        opt.setIncludeEccentricity(cbEccentricity.isSelected());
        opt.setIncludeSpecificShortestPath(cbSpecificPath.isSelected());
        opt.setFromNode(comboFrom.getSelectedIndex());
        opt.setToNode(comboTo.getSelectedIndex());
        opt.setIncludeAllShortestPaths(cbAllPaths.isSelected());

        dispose();
        if (onRun != null) onRun.accept(opt);
    }
}
