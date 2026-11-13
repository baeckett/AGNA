/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.gui.EducationalTooltips.MetricGuide;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Interactive in-app Social Network Analysis Glossary and Formula Cheat-Sheet.
 * Provides searchable definitions, sociological intuition, and practical rules
 * of thumb for all analytical indices computed in AGNA.
 */
public class SnaCheatSheetDialog extends JDialog {

    private final Map<String, MetricGuide> allGuides = EducationalTooltips.getAll();
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JPanel listPanel;
    private JLabel countLabel;

    public SnaCheatSheetDialog(Frame owner) {
        super(owner, "SNA Quick Reference & Concept Cheat-Sheet", false);

        initComponents();
        pack();
        setSize(780, 620);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));

        JLabel titleLabel = new JLabel("Social Network Analysis (SNA) Concept Reference");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(0, 64, 128));

        JLabel subLabel = new JLabel("<html>Search definitions, sociological meanings, and practical rules of thumb "
                + "for all metrics implemented in AGNA.</html>");
        subLabel.setFont(subLabel.getFont().deriveFont(12f));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.CENTER);

        // Search & Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.add(new JLabel("Search:"));
        searchField = new JTextField(18);
        searchField.setToolTipText("Filter by metric name, concept, or keyword");
        filterBar.add(searchField);

        filterBar.add(new JLabel("Category:"));
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        for (MetricGuide g : allGuides.values()) {
            if (!categories.contains(g.category)) {
                categories.add(g.category);
            }
        }
        categoryCombo = new JComboBox<>(categories.toArray(new String[0]));
        filterBar.add(categoryCombo);

        countLabel = new JLabel();
        countLabel.setFont(countLabel.getFont().deriveFont(Font.ITALIC, 11f));
        countLabel.setForeground(new Color(101, 109, 118));
        filterBar.add(countLabel);

        headerPanel.add(filterBar, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel with Cards
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        // Filter triggers
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshList(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshList(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshList(); }
        });
        categoryCombo.addActionListener(e -> refreshList());

        refreshList();

        // Bottom Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnClose = new JButton("Close");
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());
    }

    private void refreshList() {
        listPanel.removeAll();
        String filter = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String cat = (String) categoryCombo.getSelectedItem();
        if (cat == null) cat = "All Categories";

        int matches = 0;
        for (MetricGuide g : allGuides.values()) {
            if (!cat.equals("All Categories") && !g.category.equals(cat)) {
                continue;
            }

            if (!filter.isEmpty()) {
                boolean matchTitle = g.title.toLowerCase(Locale.ROOT).contains(filter);
                boolean matchWhat = g.whatItMeasures.toLowerCase(Locale.ROOT).contains(filter);
                boolean matchWhy = g.whyUseIt.toLowerCase(Locale.ROOT).contains(filter);
                boolean matchInterp = g.interpretation != null && g.interpretation.toLowerCase(Locale.ROOT).contains(filter);
                if (!matchTitle && !matchWhat && !matchWhy && !matchInterp) {
                    continue;
                }
            }

            listPanel.add(createConceptCard(g));
            listPanel.add(Box.createVerticalStrut(8));
            matches++;
        }

        countLabel.setText(String.format(Locale.ROOT, "(Showing %d of %d concepts)", matches, allGuides.size()));
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createConceptCard(MetricGuide g) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        card.setBackground(Color.WHITE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        header.setOpaque(false);
        JLabel title = new JLabel(g.title);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setForeground(new Color(0, 64, 128));
        header.add(title);

        JLabel catBadge = new JLabel(" " + g.category + " ");
        catBadge.setFont(catBadge.getFont().deriveFont(Font.BOLD, 10f));
        catBadge.setOpaque(true);
        catBadge.setBackground(new Color(238, 243, 250));
        catBadge.setForeground(new Color(30, 80, 160));
        catBadge.setBorder(BorderFactory.createLineBorder(new Color(190, 210, 235), 1));
        header.add(catBadge);
        card.add(header, BorderLayout.NORTH);

        StringBuilder html = new StringBuilder();
        html.append("<html><div style='font-family:sans-serif; font-size:11px; line-height:1.4; color:#24292f;'>");
        html.append("<b>What it measures:</b> ").append(g.whatItMeasures).append("<br>");
        html.append("<b>Why use it / Meaning:</b> ").append(g.whyUseIt).append("<br>");
        if (g.interpretation != null && !g.interpretation.isEmpty()) {
            html.append("<b>Interpretation:</b> ").append(g.interpretation).append("<br>");
        }
        if (g.technicalNote != null && !g.technicalNote.isEmpty()) {
            html.append("<small style='color:#656d76;'><i>Technical Note:</i> ").append(g.technicalNote).append("</small>");
        }
        html.append("</div></html>");

        JLabel body = new JLabel(html.toString());
        card.add(body, BorderLayout.CENTER);

        return card;
    }
}
