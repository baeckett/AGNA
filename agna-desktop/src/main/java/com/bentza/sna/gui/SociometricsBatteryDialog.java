/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.net.Network;
import com.bentza.sna.report.SociometricsOptions;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
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

/**
 * Battery dialog for configuring and executing Sociometrics and Basic Network measures.
 */
public class SociometricsBatteryDialog extends JDialog {
    private final Network network;
    private final Consumer<SociometricsOptions> onRun;

    private JCheckBox cbBasic;
    private JCheckBox cbDensity;
    private JCheckBox cbDyad;

    private JCheckBox cbNodal;
    private JCheckBox cbIn;
    private JCheckBox cbOut;
    private JCheckBox cbEmission;
    private JCheckBox cbReception;
    private JCheckBox cbDetermination;
    private JCheckBox cbSociostatus;
    private JCheckBox cbAssortativity;
    private JCheckBox cbWeighted;

    public SociometricsBatteryDialog(Frame owner, Network network, Consumer<SociometricsOptions> onRun) {
        super(owner, "Sociometrics & Basic Battery", true);
        this.network = network;
        this.onRun = onRun;

        initComponents();
        pack();
        setMinimumSize(new Dimension(580, 500));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        JLabel titleLabel = new JLabel("Sociometrics & Basic Network Battery");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(0, 64, 128));

        String netInfo = (network != null)
                ? String.format("Network: <b>%s</b> (%d actors)",
                        network.getName() != null && !network.getName().isEmpty() ? network.getName() : "Current Network",
                        network.getSize())
                : "No active network";

        JLabel subLabel = new JLabel("<html>Compute macro structural properties, density, dyadic reciprocity, and degree volume.<br>" + netInfo + "</html>");
        subLabel.setFont(subLabel.getFont().deriveFont(12f));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        // Macro Panel
        JPanel macroPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        macroPanel.setBorder(BorderFactory.createTitledBorder("Network-Level Sociometrics"));
        cbBasic = new JCheckBox("Basic Structural Parameters (Nodes, Ties, Directedness, Weighted)", true);
        cbDensity = new JCheckBox("Network Density & Completeness", true);
        cbDyad = new JCheckBox("Reciprocity & MAN Dyad Census (Mutual, Asymmetric, Null)", true);
        cbAssortativity = new JCheckBox("Newman Degree Assortativity & Mixing Coefficients (r)", true);
        cbWeighted = new JCheckBox("Valued & Weighted Analysis (Node Strength & Valued Density)", false);
        macroPanel.add(cbBasic);
        macroPanel.add(cbDensity);
        macroPanel.add(cbDyad);
        macroPanel.add(cbAssortativity);
        macroPanel.add(cbWeighted);
        contentPanel.add(macroPanel);
        contentPanel.add(Box.createVerticalStrut(6));

        // Actor-Level Panel
        JPanel actorPanel = new JPanel(new GridLayout(0, 2, 8, 4));
        actorPanel.setBorder(BorderFactory.createTitledBorder("Actor-Level Degree & Sociometric Volume"));
        cbNodal = new JCheckBox("Nodal Degree Distribution", true);
        cbIn = new JCheckBox("In-Degree Distribution", true);
        cbOut = new JCheckBox("Out-Degree Distribution", true);
        cbEmission = new JCheckBox("Emission Degree", false);
        cbReception = new JCheckBox("Reception Degree", false);
        cbDetermination = new JCheckBox("Determination Degree", false);
        cbSociostatus = new JCheckBox("Sociometric Status Indices", false);

        actorPanel.add(cbNodal);
        actorPanel.add(cbIn);
        actorPanel.add(cbOut);
        actorPanel.add(cbEmission);
        actorPanel.add(cbReception);
        actorPanel.add(cbDetermination);
        actorPanel.add(cbSociostatus);
        contentPanel.add(actorPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRun = new JButton("Run Sociometrics Battery");
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
        boolean any = cbBasic.isSelected() || cbDensity.isSelected() || cbDyad.isSelected()
                || cbNodal.isSelected() || cbIn.isSelected() || cbOut.isSelected()
                || cbEmission.isSelected() || cbReception.isSelected() || cbDetermination.isSelected()
                || cbSociostatus.isSelected() || cbAssortativity.isSelected() || cbWeighted.isSelected();

        if (!any) {
            JOptionPane.showMessageDialog(this, "Please select at least one sociometric measure.",
                    "No Measures Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SociometricsOptions opt = new SociometricsOptions();
        opt.setIncludeBasic(cbBasic.isSelected());
        opt.setIncludeDensity(cbDensity.isSelected());
        opt.setIncludeDyadCensus(cbDyad.isSelected());
        opt.setIncludeNodalDegree(cbNodal.isSelected());
        opt.setIncludeInDegree(cbIn.isSelected());
        opt.setIncludeOutDegree(cbOut.isSelected());
        opt.setIncludeEmission(cbEmission.isSelected());
        opt.setIncludeReception(cbReception.isSelected());
        opt.setIncludeDetermination(cbDetermination.isSelected());
        opt.setIncludeSociostatus(cbSociostatus.isSelected());
        opt.setIncludeAssortativity(cbAssortativity.isSelected());
        opt.setIncludeWeightedMetrics(cbWeighted.isSelected());

        dispose();
        if (onRun != null) onRun.accept(opt);
    }
}
