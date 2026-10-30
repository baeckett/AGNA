/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.net.Network;
import com.bentza.sna.report.ClusteringOptions;
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
import javax.swing.SpinnerNumberModel;

/**
 * Battery dialog for configuring and executing Clustering and Cohesion calculations.
 */
public class ClusteringBatteryDialog extends JDialog {
    private final Network network;
    private final Consumer<ClusteringOptions> onRun;

    private JCheckBox cbLocal;
    private JCheckBox cbTransitivity;
    private JCheckBox cbKCore;
    private JCheckBox cbCohesion;
    private JCheckBox cbOpenChains;
    private JCheckBox cbCliques;
    private JSpinner spCliqueDiameter;

    public ClusteringBatteryDialog(Frame owner, Network network, Consumer<ClusteringOptions> onRun) {
        super(owner, "Clustering & Cohesion Battery", true);
        this.network = network;
        this.onRun = onRun;

        initComponents();
        pack();
        setMinimumSize(new Dimension(580, 520));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        JLabel titleLabel = new JLabel("Clustering & Cohesion Analysis Battery");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(0, 64, 128));

        String netInfo = (network != null)
                ? String.format("Network: <b>%s</b> (%d actors)",
                        network.getName() != null && !network.getName().isEmpty() ? network.getName() : "Current Network",
                        network.getSize())
                : "No active network";

        JLabel subLabel = new JLabel("<html>Analyze triadic closure, clustering coefficients, core-periphery shells, and cohesion.<br>" + netInfo + "</html>");
        subLabel.setFont(subLabel.getFont().deriveFont(12f));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        // Triadic clustering panel
        JPanel triadPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        triadPanel.setBorder(BorderFactory.createTitledBorder("Triadic & Local Clustering"));
        cbLocal = new JCheckBox("Watts-Strogatz Local Clustering (Mean <C>, Node C_i distribution, Triangles)", true);
        cbTransitivity = new JCheckBox("Newman Global Transitivity (3 x Triangles / Connected Triads)", true);
        triadPanel.add(cbLocal);
        triadPanel.add(cbTransitivity);
        contentPanel.add(triadPanel);
        contentPanel.add(Box.createVerticalStrut(6));

        // Cohesion and coreness panel
        JPanel cohPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        cohPanel.setBorder(BorderFactory.createTitledBorder("Coreness & Classical Cohesion"));
        cbKCore = new JCheckBox("k-Core Decomposition (Coreness shells k_i and maximum core)", true);
        cbCohesion = new JCheckBox("Classical Cohesion Index (Knoke mutual tie ratio)", false);
        cbOpenChains = new JCheckBox("Open Chains Summary (Unclosed two-step paths)", false);
        cohPanel.add(cbKCore);
        cohPanel.add(cbCohesion);
        cohPanel.add(cbOpenChains);
        contentPanel.add(cohPanel);
        contentPanel.add(Box.createVerticalStrut(6));

        // Cliques panel
        JPanel clqPanel = new JPanel(new GridBagLayout());
        clqPanel.setBorder(BorderFactory.createTitledBorder("Maximal Subgraphs"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbCliques = new JCheckBox("Maximal Cliques (Bron-Kerbosch)");
        spCliqueDiameter = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JPanel clqLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        clqLine.add(cbCliques);
        clqLine.add(new JLabel("  Diameter:"));
        clqLine.add(spCliqueDiameter);
        gbc.gridx = 0; gbc.gridy = 0;
        clqPanel.add(clqLine, gbc);

        JLabel clqWarn = new JLabel("<html><small style='color:#a04000;'>⚠️ <i>Warning: Clique enumeration is NP-hard and can take long on dense graphs.</i></small></html>");
        gbc.gridy = 1;
        clqPanel.add(clqWarn, gbc);

        contentPanel.add(clqPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRun = new JButton("Run Clustering Battery");
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
        boolean any = cbLocal.isSelected() || cbTransitivity.isSelected() || cbKCore.isSelected()
                || cbCohesion.isSelected() || cbOpenChains.isSelected() || cbCliques.isSelected();

        if (!any) {
            JOptionPane.showMessageDialog(this, "Please select at least one clustering measure.",
                    "No Measures Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClusteringOptions opt = new ClusteringOptions();
        opt.setIncludeLocalClustering(cbLocal.isSelected());
        opt.setIncludeTransitivity(cbTransitivity.isSelected());
        opt.setIncludeKCore(cbKCore.isSelected());
        opt.setIncludeCohesion(cbCohesion.isSelected());
        opt.setIncludeOpenChains(cbOpenChains.isSelected());
        opt.setIncludeCliques(cbCliques.isSelected());
        opt.setCliqueDiameter(((Number) spCliqueDiameter.getValue()).intValue());

        dispose();
        if (onRun != null) onRun.accept(opt);
    }
}
