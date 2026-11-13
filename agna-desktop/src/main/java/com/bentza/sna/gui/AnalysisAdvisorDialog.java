/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Educational wizard that guides beginners from their empirical research question
 * to the appropriate SNA analytical battery and algorithms.
 */
public class AnalysisAdvisorDialog extends JDialog {

    public enum AdvisorAction {
        OPEN_CENTRALITY,
        OPEN_CLUSTERING,
        OPEN_SOCIOMETRICS,
        OPEN_DISTANCES,
        OPEN_HOLES,
        OPEN_HYPOTHESIS_TESTING,
        OPEN_FULL_ANALYSIS
    }

    private final Consumer<AdvisorAction> onSelectAction;

    public AnalysisAdvisorDialog(Frame owner, Consumer<AdvisorAction> onSelectAction) {
        super(owner, "Analysis Advisor — What Should I Compute?", true);
        this.onSelectAction = onSelectAction;

        initComponents();
        pack();
        setSize(740, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));
        JLabel title = new JLabel("Social Network Analysis Advisor");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(0, 64, 128));

        JLabel sub = new JLabel("<html>Not sure which metric to run? Select the sociological or operational question "
                + "you want to answer to launch the recommended analysis battery:</html>");
        sub.setFont(sub.getFont().deriveFont(12f));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Content Scroll Pane with Cards
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));

        // Question 1: Activity & Leadership
        contentPanel.add(createAdvisorCard(
                "👑 Who are the most active, popular, or prominent actors?",
                "Identify high-volume participants, opinion leaders, and recipients of deference or nominations.",
                "<b>Recommended Algorithms:</b> Nodal Degree, In-Degree (Prestige/Popularity), Out-Degree (Expansiveness), and Eigenvector Centrality.<br>"
                + "<i>Interpretation:</i> In-degree identifies who is sought after for advice; out-degree measures communication initiative.",
                "Open Centrality & Prestige...",
                AdvisorAction.OPEN_CENTRALITY
        ));
        contentPanel.add(Box.createVerticalStrut(10));

        // Question 2: Brokers & Gatekeepers
        contentPanel.add(createAdvisorCard(
                "🌉 Who controls information flow or acts as an indispensable broker between groups?",
                "Detect structural bottlenecks, strategic gatekeepers, and actors with non-redundant bridging ties.",
                "<b>Recommended Algorithms:</b> Freeman Betweenness Centrality and Ronald Burt's Social Capital & Structural Holes.<br>"
                + "<i>Interpretation:</i> High betweenness and low constraint pinpoint brokers who profit from information arbitrage and bridge structural holes.",
                "Open Centrality & Prestige...",
                AdvisorAction.OPEN_CENTRALITY
        ));
        contentPanel.add(Box.createVerticalStrut(10));

        // Question 3: Subgroups & Factions
        contentPanel.add(createAdvisorCard(
                "👥 Are there cohesive subgroups, informal teams, or faction splits?",
                "Discover tightly-knit cliques, community clusters, and core-periphery structures automatically.",
                "<b>Recommended Algorithms:</b> Community Detection (Louvain Modularity Q), Watts-Strogatz Local Clustering, and k-Core Decomposition.<br>"
                + "<i>Interpretation:</i> Modularity Q > 0.3 proves non-random community clustering; k-cores reveal the resilient organizational inner circle.",
                "Open Clustering & Cohesion...",
                AdvisorAction.OPEN_CLUSTERING
        ));
        contentPanel.add(Box.createVerticalStrut(10));

        // Question 4: Hierarchy vs Equality
        contentPanel.add(createAdvisorCard(
                "⚖️ Is the network a top-down hierarchy or an egalitarian community?",
                "Determine whether power and ties are concentrated around a single central hub or dispersed evenly.",
                "<b>Recommended Algorithms:</b> Freeman (1979) Global Centralization (C_D, C_B, C_C) and Newman Degree Assortativity (r).<br>"
                + "<i>Interpretation:</i> Centralization close to 1.0 denotes a star-like autocracy; centralization near 0.0 indicates decentralized equality.",
                "Open Sociometric & Basic...",
                AdvisorAction.OPEN_SOCIOMETRICS
        ));
        contentPanel.add(Box.createVerticalStrut(10));

        // Question 5: Diffusion Speed & Distance
        contentPanel.add(createAdvisorCard(
                "⚡ How quickly can messages or rumors diffuse across the network?",
                "Measure overall communication efficiency, network diameter, and independence from intermediaries.",
                "<b>Recommended Algorithms:</b> Closeness Centrality, Geodesic Distance Matrix, and Network Diameter.<br>"
                + "<i>Interpretation:</i> High closeness actors need the fewest steps to reach the entire population; small diameter indicates rapid diffusion.",
                "Open Distances & Geodesics...",
                AdvisorAction.OPEN_DISTANCES
        ));
        contentPanel.add(Box.createVerticalStrut(10));

        // Question 6: Hypothesis Testing & Statistical Significance
        contentPanel.add(createAdvisorCard(
                "🎲 Are observed network patterns statistically significant or just random chance?",
                "Benchmark empirical network parameters (clustering, density, reciprocity) against 1,000 synthetic random graphs.",
                "<b>Recommended Algorithms:</b> Monte Carlo Hypothesis Testing with Degree-Preserving or Erdős–Rényi Null Models.<br>"
                + "<i>Interpretation:</i> Reports empirical p-values and standardized Z-scores to confirm scientific validity before publishing.",
                "Open Hypothesis Testing...",
                AdvisorAction.OPEN_HYPOTHESIS_TESTING
        ));
        contentPanel.add(Box.createVerticalStrut(10));

        // Question 7: Full Analysis
        contentPanel.add(createAdvisorCard(
                "📋 Run everything in one comprehensive analytical pass",
                "Execute the complete suite of sociometric, centrality, geodesic, clustering, and structural hole metrics.",
                "<b>Recommended:</b> Full Analysis with customizable modules and automated Plain-English Takeaways.",
                "Open Full Analysis...",
                AdvisorAction.OPEN_FULL_ANALYSIS
        ));

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnClose = new JButton("Close");
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());
    }

    private JPanel createAdvisorCard(String question, String subtitle, String detailsHtml,
                                     String buttonLabel, AdvisorAction action) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 233), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout(2, 2));
        top.setOpaque(false);
        JLabel qLbl = new JLabel(question);
        qLbl.setFont(qLbl.getFont().deriveFont(Font.BOLD, 13f));
        qLbl.setForeground(new Color(0, 64, 128));

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(subLbl.getFont().deriveFont(Font.ITALIC, 11f));
        subLbl.setForeground(new Color(101, 109, 118));
        top.add(qLbl, BorderLayout.NORTH);
        top.add(subLbl, BorderLayout.CENTER);

        JLabel detLbl = new JLabel("<html><div style='font-size:11px; line-height:1.4; color:#24292f; margin-top:4px;'>" + detailsHtml + "</div></html>");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        right.setOpaque(false);
        JButton btn = new JButton(buttonLabel);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 11f));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            dispose();
            if (onSelectAction != null) {
                onSelectAction.accept(action);
            }
        });
        right.add(btn);

        card.add(top, BorderLayout.NORTH);
        card.add(detLbl, BorderLayout.CENTER);
        card.add(right, BorderLayout.SOUTH);

        return card;
    }
}
