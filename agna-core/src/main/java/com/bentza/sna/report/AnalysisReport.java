/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Modern structured analysis report combining metadata, parameter cards,
 * alerts/notes, and one or more {@link TableData} tables.
 */
public class AnalysisReport {
    private final String title;
    private final String networkName;
    private final Date timestamp;
    private final List<TableData> tables = new ArrayList<>();
    private final Map<String, String> properties = new LinkedHashMap<>();
    private String note;
    private boolean warning;
    private String rawPlainText;

    public AnalysisReport(String title, String networkName) {
        this.title = (title != null) ? title : "Analysis Report";
        this.networkName = (networkName != null) ? networkName : "";
        this.timestamp = new Date();
    }

    public void addTable(TableData table) {
        if (table != null) {
            tables.add(table);
        }
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public void setNote(String note, boolean isWarning) {
        this.note = note;
        this.warning = isWarning;
    }

    public void setRawPlainText(String rawPlainText) {
        this.rawPlainText = rawPlainText;
    }

    public String getTitle() {
        return title;
    }

    public String getNetworkName() {
        return networkName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<TableData> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public String getNote() {
        return note;
    }

    public boolean isWarning() {
        return warning;
    }

    /**
     * Renders a modern HTML report card compatible with Swing HTMLEditorKit.
     */
    public String toHtmlReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:sans-serif; margin:14px 8px; color:#24292f;'>\n");

        // Header Banner
        sb.append("<table style='width:100%; border:none; border-bottom:2px solid #004080; margin-bottom:12px; padding-bottom:6px;'><tr>");
        sb.append("<td style='border:none; padding:0; text-align:left;'>");
        sb.append("<span style='font-size:16px; font-weight:bold; color:#004080;'>AGNA Analysis: ")
          .append(escapeHtml(title)).append("</span>");
        sb.append("</td><td style='border:none; padding:0; text-align:right;'>");
        sb.append("<span style='font-size:11px; color:#656d76;'>");
        if (networkName != null && !networkName.isEmpty()) {
            sb.append("Network: <b>").append(escapeHtml(networkName)).append("</b> &bull; ");
        }
        sb.append(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()).format(timestamp));
        sb.append("</span>");
        sb.append("</td></tr></table>\n");

        // Methodological note / warning if present
        if (note != null && !note.trim().isEmpty()) {
            String bg = warning ? "#fffde7" : "#eef6ff";
            String border = warning ? "#fbc02d" : "#2196f3";
            String color = warning ? "#5f370e" : "#0d47a1";
            sb.append("<div style='margin-bottom:12px; padding:8px 12px; background:").append(bg)
              .append("; border-left:4px solid ").append(border)
              .append("; font-size:12px; color:").append(color).append(";'>\n")
              .append("<b>Note:</b> ").append(escapeHtml(note).replace("\n", "<br>\n"))
              .append("</div>\n");
        }

        // Summary properties table if present
        if (!properties.isEmpty()) {
            sb.append("<table style='border-collapse:collapse; width:100%; max-width:650px; font-size:12px; margin-bottom:12px; border:1px solid #d0d7de;'>\n");
            int r = 0;
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String bg = (r % 2 == 0) ? "#f4f6f9" : "#ffffff";
                sb.append("<tr style='background:").append(bg).append(";'>\n");
                sb.append("  <td style='padding:5px 10px; font-weight:bold; width:40%; border:1px solid #e1e4e8; color:#333;'>")
                  .append(escapeHtml(entry.getKey())).append("</td>\n");
                sb.append("  <td style='padding:5px 10px; border:1px solid #e1e4e8;'>")
                  .append(escapeHtml(entry.getValue())).append("</td>\n");
                sb.append("</tr>\n");
                r++;
            }
            sb.append("</table>\n");
        }

        // Tables
        for (TableData table : tables) {
            sb.append(table.toHtmlTable(true));
        }

        sb.append("</div>\n");
        return sb.toString();
    }

    /**
     * Renders standard plain text representation, falling back to raw plain text if configured.
     */
    public String toPlainText() {
        if (rawPlainText != null && !rawPlainText.isEmpty()) {
            return rawPlainText;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        if (networkName != null && !networkName.isEmpty()) {
            sb.append(" of ").append(networkName);
        }
        sb.append("\n");
        if (note != null && !note.trim().isEmpty()) {
            sb.append(note).append("\n");
        }
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        for (TableData table : tables) {
            sb.append("\n").append(table.getTitle()).append("\n");
            if (table.getSubtitle() != null && !table.getSubtitle().isEmpty()) {
                sb.append(table.getSubtitle()).append("\n");
            }
            sb.append(table.toPlainTextTable()).append("\n");
        }
        return sb.toString();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
