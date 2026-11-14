/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.net.Network;
import com.bentza.sna.transform.NetworkRandomizeService;
import com.bentza.sna.transform.RandomizeOptions;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Dialog for populating / randomizing sociomatrix edge values and network topology.
 */
public class RandomizeNetworkDialog extends JDialog {

    public static class RandomizeResult {
        public final RandomizeOptions options;
        public final boolean createNewNetwork;

        public RandomizeResult(RandomizeOptions options, boolean createNewNetwork) {
            this.options = options;
            this.createNewNetwork = createNewNetwork;
        }
    }

    private final Network network;
    private final Consumer<RandomizeResult> onApply;

    private JComboBox<RandomizeOptions.Model> modelCombo;
    private JLabel descLabel;

    // Parameter panels
    private JPanel paramsCardPanel;
    private CardLayout cardLayout;

    // Parameters
    private JSpinner spGnpProb;
    private JCheckBox cbGnpSym;

    private JSpinner spGnmCount;
    private JCheckBox cbGnmSym;

    private JSpinner spWeightProb;
    private JSpinner spWeightMin;
    private JSpinner spWeightMax;
    private JCheckBox cbWeightSym;

    private JSpinner spWattsK;
    private JSpinner spWattsBeta;

    private JSpinner spBarabasiM;

    private JSpinner spConnectedProb;

    private JSpinner spSwapsPerEdge;

    // Output destination
    private JRadioButton rbInPlace;
    private JRadioButton rbNewNetwork;

    public RandomizeNetworkDialog(Frame owner, Network network, Consumer<RandomizeResult> onApply) {
        super(owner, "Populate with Random Edge Values", true);
        this.network = network;
        this.onApply = onApply;

        initComponents();
        pack();
        setMinimumSize(new Dimension(580, 480));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        JLabel titleLabel = new JLabel("Populate Network with Random Edge Values");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(0, 64, 128));

        String netName = network != null ? network.getName() : "Current Network";
        int size = network != null ? network.getSize() : 0;

        JLabel subLabel = new JLabel(String.format(
                "<html>Populate or randomize edge values for <b>%s</b> (%d actors).<br>" +
                "Existing node names, positions, and identities will be preserved.</html>",
                netName, size));
        subLabel.setFont(subLabel.getFont().deriveFont(12f));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Model selector
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        contentPanel.add(new JLabel("Random Network Model:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        modelCombo = new JComboBox<>(RandomizeOptions.Model.values());
        modelCombo.setSelectedItem(RandomizeOptions.Model.ERDOS_RENYI_GNP);
        contentPanel.add(modelCombo, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        descLabel = new JLabel();
        descLabel.setFont(descLabel.getFont().deriveFont(11f));
        contentPanel.add(descLabel, gbc);

        // Dynamic Parameters Panel (CardLayout)
        cardLayout = new CardLayout();
        paramsCardPanel = new JPanel(cardLayout);
        paramsCardPanel.setBorder(BorderFactory.createTitledBorder("Model Parameters"));

        initCards();

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        contentPanel.add(paramsCardPanel, gbc);

        // Options Panel
        JPanel optPanel = new JPanel(new GridBagLayout());
        optPanel.setBorder(BorderFactory.createTitledBorder("Target Network"));
        GridBagConstraints gbcOpt = new GridBagConstraints();
        gbcOpt.insets = new Insets(4, 6, 4, 6);
        gbcOpt.anchor = GridBagConstraints.WEST;
        gbcOpt.fill = GridBagConstraints.HORIZONTAL;
        gbcOpt.gridx = 0; gbcOpt.weightx = 1.0;

        rbInPlace = new JRadioButton("Apply in-place to active network", true);
        rbNewNetwork = new JRadioButton("Create as a new network copy", false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbInPlace);
        bg.add(rbNewNetwork);

        gbcOpt.gridy = 0;
        optPanel.add(rbInPlace, gbcOpt);
        gbcOpt.gridy = 1;
        optPanel.add(rbNewNetwork, gbcOpt);

        JLabel noteDiag = new JLabel("<html><small style='color:#666;'>Note: Diagonal self-loops a[i][i] are always strictly zeroed.</small></html>");
        gbcOpt.gridy = 2;
        optPanel.add(noteDiag, gbcOpt);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        contentPanel.add(optPanel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnApply = new JButton("Populate Ties");
        btnApply.setFont(btnApply.getFont().deriveFont(Font.BOLD));
        JButton btnCancel = new JButton("Cancel");

        bottomPanel.add(btnApply);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnApply.addActionListener(e -> onApplyClicked());
        modelCombo.addActionListener(e -> onModelChanged());

        onModelChanged();
        getRootPane().setDefaultButton(btnApply);
    }

    private void initCards() {
        int n = network != null ? network.getSize() : 10;
        int maxDirectedEdges = Math.max(1, n * (n - 1));

        // 1. GNP
        JPanel gnpCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        gnpCard.add(new JLabel("Tie Probability (p):"));
        spGnpProb = new JSpinner(new SpinnerNumberModel(0.20, 0.01, 1.0, 0.05));
        gnpCard.add(spGnpProb);
        cbGnpSym = new JCheckBox("Symmetric (Undirected ties)", false);
        gnpCard.add(cbGnpSym);
        paramsCardPanel.add(gnpCard, RandomizeOptions.Model.ERDOS_RENYI_GNP.name());

        // 2. GNM
        JPanel gnmCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        gnmCard.add(new JLabel("Number of Ties (m):"));
        int defaultM = Math.min(2 * n, maxDirectedEdges);
        spGnmCount = new JSpinner(new SpinnerNumberModel(defaultM, 1, maxDirectedEdges, 1));
        gnmCard.add(spGnmCount);
        cbGnmSym = new JCheckBox("Symmetric (Undirected ties)", false);
        gnmCard.add(cbGnmSym);
        paramsCardPanel.add(gnmCard, RandomizeOptions.Model.ERDOS_RENYI_GNM.name());

        // 3. RANDOM WEIGHTED
        JPanel weightCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        weightCard.add(new JLabel("Density (p):"));
        spWeightProb = new JSpinner(new SpinnerNumberModel(0.30, 0.01, 1.0, 0.05));
        weightCard.add(spWeightProb);
        weightCard.add(new JLabel("Min Weight:"));
        spWeightMin = new JSpinner(new SpinnerNumberModel(0.10, 0.01, 1000.0, 0.10));
        weightCard.add(spWeightMin);
        weightCard.add(new JLabel("Max Weight:"));
        spWeightMax = new JSpinner(new SpinnerNumberModel(1.00, 0.01, 1000.0, 0.10));
        weightCard.add(spWeightMax);
        cbWeightSym = new JCheckBox("Symmetric", false);
        weightCard.add(cbWeightSym);
        paramsCardPanel.add(weightCard, RandomizeOptions.Model.RANDOM_WEIGHTED.name());

        // 4. WATTS STROGATZ
        JPanel wsCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        wsCard.add(new JLabel("Lattice Neighborhood (k):"));
        int maxK = Math.max(2, 2 * ((n - 1) / 2));
        spWattsK = new JSpinner(new SpinnerNumberModel(Math.min(2, maxK), 2, maxK, 2));
        wsCard.add(spWattsK);
        wsCard.add(new JLabel("Rewiring Probability (β):"));
        spWattsBeta = new JSpinner(new SpinnerNumberModel(0.20, 0.0, 1.0, 0.05));
        wsCard.add(spWattsBeta);
        paramsCardPanel.add(wsCard, RandomizeOptions.Model.WATTS_STROGATZ.name());

        // 5. BARABASI ALBERT
        JPanel baCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        baCard.add(new JLabel("Edges Added per Actor (m):"));
        int maxM = Math.max(1, n - 1);
        spBarabasiM = new JSpinner(new SpinnerNumberModel(1, 1, maxM, 1));
        baCard.add(spBarabasiM);
        paramsCardPanel.add(baCard, RandomizeOptions.Model.BARABASI_ALBERT.name());

        // 6. RANDOM CONNECTED
        JPanel connCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        connCard.add(new JLabel("Spanning tree guaranteed (0 isolates). Extra Tie Probability:"));
        spConnectedProb = new JSpinner(new SpinnerNumberModel(0.20, 0.0, 1.0, 0.05));
        connCard.add(spConnectedProb);
        paramsCardPanel.add(connCard, RandomizeOptions.Model.RANDOM_CONNECTED.name());

        // 7. DEGREE PRESERVING
        JPanel dpCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        dpCard.add(new JLabel("Swaps per Tie (Q):"));
        spSwapsPerEdge = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        dpCard.add(spSwapsPerEdge);
        paramsCardPanel.add(dpCard, RandomizeOptions.Model.DEGREE_PRESERVING.name());

        // 8. WEIGHT PERMUTATION
        JPanel wpCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        wpCard.add(new JLabel("Shuffles tie values among existing edges, preserving the exact binary network structure."));
        paramsCardPanel.add(wpCard, RandomizeOptions.Model.WEIGHT_PERMUTATION.name());
    }

    private void onModelChanged() {
        RandomizeOptions.Model sel = (RandomizeOptions.Model) modelCombo.getSelectedItem();
        if (sel == null) return;

        cardLayout.show(paramsCardPanel, sel.name());

        String desc;
        switch (sel) {
            case ERDOS_RENYI_GNP:
                desc = "<html><small style='color:#555;'>Each node pair has an independent probability <i>p</i> of being connected by a tie.</small></html>";
                break;
            case ERDOS_RENYI_GNM:
                desc = "<html><small style='color:#555;'>Places exactly <i>m</i> ties uniformly at random across all non-diagonal pairs.</small></html>";
                break;
            case RANDOM_WEIGHTED:
                desc = "<html><small style='color:#555;'>Connects pairs with probability <i>p</i>, assigning continuous edge weights in [min, max].</small></html>";
                break;
            case WATTS_STROGATZ:
                desc = "<html><small style='color:#555;'>Generates small-world clustering and short path lengths via random ring lattice rewiring.</small></html>";
                break;
            case BARABASI_ALBERT:
                desc = "<html><small style='color:#555;'>Scale-free network generated via preferential attachment, creating hubs with power-law degree.</small></html>";
                break;
            case RANDOM_CONNECTED:
                desc = "<html><small style='color:#555;'>Constructs a connected random spanning tree across all actors, ensuring 0 isolated nodes.</small></html>";
                break;
            case DEGREE_PRESERVING:
                desc = "<html><small style='color:#555;'>Preserves each actor's exact in- and out-degree sequence via Maslov–Sneppen edge swaps.</small></html>";
                break;
            case WEIGHT_PERMUTATION:
                desc = "<html><small style='color:#555;'>Randomly permutes tie weights across existing edges while preserving the binary adjacency graph.</small></html>";
                break;
            default:
                desc = "";
                break;
        }
        descLabel.setText(desc);
    }

    private void onApplyClicked() {
        RandomizeOptions.Model sel = (RandomizeOptions.Model) modelCombo.getSelectedItem();
        RandomizeOptions opt = new RandomizeOptions();
        opt.setModel(sel);
        opt.setCreateNewNetwork(rbNewNetwork.isSelected());

        switch (sel) {
            case ERDOS_RENYI_GNP:
                opt.setProbability(((Number) spGnpProb.getValue()).doubleValue());
                opt.setSymmetric(cbGnpSym.isSelected());
                break;
            case ERDOS_RENYI_GNM:
                opt.setTieCount(((Number) spGnmCount.getValue()).intValue());
                opt.setSymmetric(cbGnmSym.isSelected());
                break;
            case RANDOM_WEIGHTED:
                opt.setProbability(((Number) spWeightProb.getValue()).doubleValue());
                opt.setMinWeight(((Number) spWeightMin.getValue()).doubleValue());
                opt.setMaxWeight(((Number) spWeightMax.getValue()).doubleValue());
                opt.setSymmetric(cbWeightSym.isSelected());
                break;
            case WATTS_STROGATZ:
                opt.setWattsStrogatzK(((Number) spWattsK.getValue()).intValue());
                opt.setWattsStrogatzBeta(((Number) spWattsBeta.getValue()).doubleValue());
                break;
            case BARABASI_ALBERT:
                opt.setBarabasiAlbertM(((Number) spBarabasiM.getValue()).intValue());
                break;
            case RANDOM_CONNECTED:
                opt.setExtraEdgeProb(((Number) spConnectedProb.getValue()).doubleValue());
                break;
            case DEGREE_PRESERVING:
                opt.setSwapsPerEdge(((Number) spSwapsPerEdge.getValue()).intValue());
                break;
            case WEIGHT_PERMUTATION:
                break;
        }

        dispose();
        if (onApply != null) {
            onApply.accept(new RandomizeResult(opt, rbNewNetwork.isSelected()));
        }
    }
}
