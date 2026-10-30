/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;
import com.bentza.sna.transform.NetworkTransformService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * Unified dialog for network symmetrization with comprehensive rules and options.
 */
public class SymmetrizeDialog extends JDialog {
    public static class SymmetrizeResult {
        public final String method;
        public final boolean zeroDiagonal;
        public final boolean createNewNetwork;

        public SymmetrizeResult(String method, boolean zeroDiagonal, boolean createNewNetwork) {
            this.method = method;
            this.zeroDiagonal = zeroDiagonal;
            this.createNewNetwork = createNewNetwork;
        }
    }

    private final Network network;
    private final Consumer<SymmetrizeResult> onApply;

    private JComboBox<String> methodCombo;
    private JLabel descLabel;
    private JCheckBox cbZeroDiagonal;
    private JRadioButton rbInPlace;
    private JRadioButton rbNewNetwork;

    public SymmetrizeDialog(Frame owner, Network network, Consumer<SymmetrizeResult> onApply) {
        super(owner, "Symmetrize Network", true);
        this.network = network;
        this.onApply = onApply;

        initComponents();
        pack();
        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        JLabel titleLabel = new JLabel("Network Symmetrization");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(0, 64, 128));

        String netName = network != null ? network.getName() : "Current Network";
        int size = network != null ? network.getSize() : 0;
        double symRatio = calculateCurrentSymmetryRatio(network);

        JLabel subLabel = new JLabel(String.format(Locale.ROOT,
                "<html>Transform directed network <b>%s</b> (%d actors) into a symmetric matrix.<br>" +
                "Current network symmetry: <b>%.1f%%</b></html>",
                netName, size, symRatio * 100.0));
        subLabel.setFont(subLabel.getFont().deriveFont(12f));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Center Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Method selection
        String[] methods = {
            "Maximum",
            "Minimum",
            "Maximum Non-Zero",
            "Minimum Non-Zero",
            "Below Diagonal (Lower to Upper)",
            "Above Diagonal (Upper to Lower)",
            "Below Diagonal Non-Zero",
            "Above Diagonal Non-Zero",
            "Arithmetic Mean",
            "Geometric Mean",
            "Sum",
            "Product",
            "Product Non-Zero",
            "Boolean OR (Union)",
            "Boolean AND (Intersection)"
        };

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Symmetrization Rule:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        methodCombo = new JComboBox<>(methods);
        methodCombo.setSelectedItem("Maximum");
        formPanel.add(methodCombo, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        descLabel = new JLabel("<html><small style='color:#555;'>Sets tie(i, j) = tie(j, i) = max(tie(i, j), tie(j, i)).</small></html>");
        formPanel.add(descLabel, gbc);

        methodCombo.addActionListener(e -> updateDescription());

        // Options Panel
        JPanel optPanel = new JPanel(new GridBagLayout());
        optPanel.setBorder(BorderFactory.createTitledBorder("Transformation Options"));
        GridBagConstraints gbcOpt = new GridBagConstraints();
        gbcOpt.insets = new Insets(4, 6, 4, 6);
        gbcOpt.anchor = GridBagConstraints.WEST;
        gbcOpt.fill = GridBagConstraints.HORIZONTAL;
        gbcOpt.gridx = 0; gbcOpt.weightx = 1.0;

        cbZeroDiagonal = new JCheckBox("Zero diagonal (remove self-loops a[i][i] = 0)", false);
        gbcOpt.gridy = 0;
        optPanel.add(cbZeroDiagonal, gbcOpt);

        rbInPlace = new JRadioButton("Apply in-place to active network", true);
        rbNewNetwork = new JRadioButton("Create as a new network copy", false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbInPlace);
        bg.add(rbNewNetwork);

        gbcOpt.gridy = 1;
        optPanel.add(rbInPlace, gbcOpt);
        gbcOpt.gridy = 2;
        optPanel.add(rbNewNetwork, gbcOpt);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(optPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnApply = new JButton("Apply Symmetrization");
        btnApply.setFont(btnApply.getFont().deriveFont(Font.BOLD));
        JButton btnCancel = new JButton("Cancel");

        bottomPanel.add(btnApply);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnApply.addActionListener(e -> {
            dispose();
            if (onApply != null) {
                String method = (String) methodCombo.getSelectedItem();
                onApply.accept(new SymmetrizeResult(method, cbZeroDiagonal.isSelected(), rbNewNetwork.isSelected()));
            }
        });

        getRootPane().setDefaultButton(btnApply);
    }

    private void updateDescription() {
        String sel = (String) methodCombo.getSelectedItem();
        if (sel == null) return;
        String desc;
        switch (sel) {
            case "Maximum": desc = "Sets tie(i, j) = tie(j, i) = max(tie(i, j), tie(j, i))."; break;
            case "Minimum": desc = "Sets tie(i, j) = tie(j, i) = min(tie(i, j), tie(j, i))."; break;
            case "Maximum Non-Zero": desc = "Uses max if both ties exist, otherwise keeps the single non-zero tie."; break;
            case "Minimum Non-Zero": desc = "Uses min if both ties exist, otherwise keeps the single non-zero tie."; break;
            case "Below Diagonal (Lower to Upper)": desc = "Copies the lower triangle a[i][j] (i > j) into the upper triangle."; break;
            case "Above Diagonal (Upper to Lower)": desc = "Copies the upper triangle a[i][j] (i < j) into the lower triangle."; break;
            case "Below Diagonal Non-Zero": desc = "Copies lower triangle, falling back to upper triangle if lower is zero."; break;
            case "Above Diagonal Non-Zero": desc = "Copies upper triangle, falling back to lower triangle if upper is zero."; break;
            case "Arithmetic Mean": desc = "Sets tie(i, j) = tie(j, i) = (tie(i, j) + tie(j, i)) / 2."; break;
            case "Geometric Mean": desc = "Sets tie(i, j) = tie(j, i) = sqrt(tie(i, j) * tie(j, i))."; break;
            case "Sum": desc = "Sets tie(i, j) = tie(j, i) = tie(i, j) + tie(j, i)."; break;
            case "Product": desc = "Sets tie(i, j) = tie(j, i) = tie(i, j) * tie(j, i)."; break;
            case "Product Non-Zero": desc = "Sets product if both exist, otherwise 0."; break;
            case "Boolean OR (Union)": desc = "Sets tie = 1.0 if either directed tie exists (weak/loose reciprocity)."; break;
            case "Boolean AND (Intersection)": desc = "Sets tie = 1.0 only if mutual ties exist in both directions (strong reciprocity)."; break;
            default: desc = ""; break;
        }
        descLabel.setText("<html><small style='color:#555;'>" + desc + "</small></html>");
    }

    private static double calculateCurrentSymmetryRatio(Network net) {
        if (net == null || net.getSize() < 2) return 1.0;
        int n = net.getSize();
        int totalPairs = 0;
        int symmetricPairs = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                float vij = net.getValue(i, j);
                float vji = net.getValue(j, i);
                if (vij != 0 || vji != 0) {
                    totalPairs++;
                    if (Math.abs(vij - vji) < 1e-6f) {
                        symmetricPairs++;
                    }
                }
            }
        }
        return totalPairs == 0 ? 1.0 : ((double) symmetricPairs / totalPairs);
    }
}
