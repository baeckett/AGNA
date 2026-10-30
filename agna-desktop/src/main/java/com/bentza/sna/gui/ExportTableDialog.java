/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.Environment;
import com.bentza.sna.report.TableData;
import com.bentza.sna.report.TableRegistry;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Interactive Swing dialog allowing users to browse, live-preview, and export
 * any generated analysis table to CSV, JSON, TSV, or HTML.
 */
public class ExportTableDialog extends JDialog {

    private JComboBox<TableData> tableCombo;
    private JTable previewTable;
    private JLabel statusLabel;
    private JRadioButton rCsv;
    private JRadioButton rJson;
    private JRadioButton rTsv;
    private JRadioButton rHtml;
    private JLabel feedbackLabel;

    public static void showDialog(Frame parent, TableData preselected) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        List<TableData> recent = TableRegistry.getRecentTables();
        if (recent.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "No analysis tables have been generated yet in this session.\n"
                    + "Please run an analysis first from the Analysis menu.",
                    "No Analysis Tables", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ExportTableDialog dlg = new ExportTableDialog(parent, preselected);
        dlg.setVisible(true);
    }

    public ExportTableDialog(Frame parent, TableData preselected) {
        super(parent, "Export Analysis Table - AGNA", true);
        initComponents(preselected);
    }

    private void initComponents(TableData preselected) {
        setLayout(new BorderLayout(8, 8));
        setSize(700, 520);
        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(getParent());

        // Top Panel: Table selector
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 12, 6, 12),
                BorderFactory.createTitledBorder("Select Table to Export")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Analysis Table:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        List<TableData> tables = TableRegistry.getRecentTables();
        tableCombo = new JComboBox<>(tables.toArray(new TableData[0]));
        tableCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TableData) {
                    TableData td = (TableData) value;
                    String net = td.getNetworkName().isEmpty() ? "" : "[" + td.getNetworkName() + "] ";
                    setText(net + td.getTitle() + " (" + td.getRowCount() + " rows)");
                }
                return this;
            }
        });

        if (preselected != null) {
            for (int i = 0; i < tableCombo.getItemCount(); i++) {
                TableData td = tableCombo.getItemAt(i);
                if (td.getId().equals(preselected.getId())) {
                    tableCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        topPanel.add(tableCombo, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Preview Table & Status
        JPanel centerPanel = new JPanel(new BorderLayout(4, 4));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        previewTable = new JTable();
        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        previewTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(previewTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(feedbackLabel.getFont().deriveFont(Font.BOLD, 11f));
        feedbackLabel.setForeground(new java.awt.Color(0, 120, 40));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(feedbackLabel, BorderLayout.EAST);
        centerPanel.add(statusPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel: Format & Actions
        JPanel bottomPanel = new JPanel(new BorderLayout(6, 6));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));

        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        formatPanel.setBorder(BorderFactory.createTitledBorder("Export Format"));

        rCsv = new JRadioButton("CSV (*.csv)", true);
        rJson = new JRadioButton("JSON (*.json)");
        rTsv = new JRadioButton("TSV (*.tsv / *.txt)");
        rHtml = new JRadioButton("HTML Table (*.html)");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rCsv);
        bg.add(rJson);
        bg.add(rTsv);
        bg.add(rHtml);

        formatPanel.add(rCsv);
        formatPanel.add(rJson);
        formatPanel.add(rTsv);
        formatPanel.add(rHtml);
        bottomPanel.add(formatPanel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnCopy = new JButton("Copy to Clipboard");
        JButton btnSave = new JButton("Save Table As...");
        btnSave.setFont(btnSave.getFont().deriveFont(Font.BOLD));
        JButton btnClose = new JButton("Close");

        actionPanel.add(btnCopy);
        actionPanel.add(btnSave);
        actionPanel.add(btnClose);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Event Listeners
        tableCombo.addActionListener(e -> updatePreview());
        btnCopy.addActionListener(e -> copyToClipboard());
        btnSave.addActionListener(e -> saveToFile());
        btnClose.addActionListener(e -> dispose());

        updatePreview();
    }

    private TableData getSelectedTable() {
        return (TableData) tableCombo.getSelectedItem();
    }

    private void updatePreview() {
        TableData td = getSelectedTable();
        if (td != null) {
            previewTable.setModel(td.toTableModel());
            statusLabel.setText("Rows: " + td.getRowCount() + " | Columns: " + td.getColumnCount()
                    + (td.getNetworkName().isEmpty() ? "" : " | Network: " + td.getNetworkName()));
            feedbackLabel.setText(" ");
        }
    }

    private String getFormattedContent(TableData td) {
        if (rJson.isSelected()) {
            return td.toJson();
        } else if (rTsv.isSelected()) {
            return td.toTsv();
        } else if (rHtml.isSelected()) {
            return td.toHtmlTable(false);
        } else {
            return td.toCsv();
        }
    }

    private String getSelectedExtension() {
        if (rJson.isSelected()) return "json";
        if (rTsv.isSelected()) return "tsv";
        if (rHtml.isSelected()) return "html";
        return "csv";
    }

    private void copyToClipboard() {
        TableData td = getSelectedTable();
        if (td == null) return;
        try {
            String text = getFormattedContent(td);
            StringSelection sel = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            String ext = getSelectedExtension().toUpperCase();
            feedbackLabel.setText("Copied to clipboard in " + ext + " format!");
        } catch (Exception ex) {
            AgnaLog.error("clipboard copy failed", ex);
            JOptionPane.showMessageDialog(this, "Could not copy to clipboard: " + ex.getMessage(),
                    "Copy Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveToFile() {
        TableData td = getSelectedTable();
        if (td == null) return;

        String ext = getSelectedExtension();
        String netSlug = td.getNetworkName().replaceAll("[^a-zA-Z0-9_-]", "_");
        if (netSlug.isEmpty()) netSlug = "network";
        String titleSlug = td.getTitle().replaceAll("[^a-zA-Z0-9_-]", "_");
        String suggestedName = netSlug + "_" + titleSlug + "." + ext;

        File defaultDir = Environment.getDefaultOpenDirectory();
        JFileChooser chooser = new JFileChooser(defaultDir);
        chooser.setSelectedFile(new File(defaultDir, suggestedName));
        chooser.setDialogTitle("Save Analysis Table As " + ext.toUpperCase());

        FileNameExtensionFilter filter;
        if ("json".equals(ext)) {
            filter = new FileNameExtensionFilter("JSON Files (*.json)", "json");
        } else if ("tsv".equals(ext)) {
            filter = new FileNameExtensionFilter("Tab-Separated Files (*.tsv, *.txt)", "tsv", "txt");
        } else if ("html".equals(ext)) {
            filter = new FileNameExtensionFilter("HTML Files (*.html, *.htm)", "html", "htm");
        } else {
            filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        }
        chooser.setFileFilter(filter);

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith("." + ext)) {
                target = new File(target.getParentFile(), target.getName() + "." + ext);
            }
            try {
                String content = getFormattedContent(td);
                Files.write(target.toPath(), content.getBytes(StandardCharsets.UTF_8));
                feedbackLabel.setText("Saved to " + target.getName() + "!");
                JOptionPane.showMessageDialog(this,
                        "Table successfully exported to:\n" + target.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                AgnaLog.error("file save failed", ex);
                JOptionPane.showMessageDialog(this,
                        "Failed to save file: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
