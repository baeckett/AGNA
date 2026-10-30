/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * First-class structured tabular result produced by AGNA network analyses.
 * Supports export to CSV (RFC 4180), JSON, TSV, HTML table with modern styling,
 * ASCII plain text, and Swing TableModel.
 */
public final class TableData {
    private final String id;
    private final String title;
    private final String subtitle;
    private final String networkName;
    private final List<String> headers;
    private final List<List<Object>> rows;
    private final boolean[] numericColumns;

    private TableData(Builder builder) {
        this.id = (builder.id != null) ? builder.id : ("tbl-" + UUID.randomUUID().toString());
        this.title = (builder.title != null) ? builder.title : "Analysis Table";
        this.subtitle = (builder.subtitle != null) ? builder.subtitle : "";
        this.networkName = (builder.networkName != null) ? builder.networkName : "";
        this.headers = Collections.unmodifiableList(new ArrayList<>(builder.headers));

        List<List<Object>> copyRows = new ArrayList<>(builder.rows.size());
        for (List<Object> row : builder.rows) {
            copyRows.add(Collections.unmodifiableList(new ArrayList<>(row)));
        }
        this.rows = Collections.unmodifiableList(copyRows);

        if (builder.numericColumns != null && builder.numericColumns.length >= headers.size()) {
            this.numericColumns = builder.numericColumns.clone();
        } else {
            this.numericColumns = detectNumericColumns(headers.size(), copyRows);
        }
    }

    private static boolean[] detectNumericColumns(int cols, List<List<Object>> rows) {
        boolean[] isNum = new boolean[cols];
        if (rows.isEmpty()) {
            return isNum;
        }
        for (int c = 0; c < cols; c++) {
            boolean allNumeric = true;
            int nonNullCount = 0;
            for (List<Object> row : rows) {
                if (c < row.size()) {
                    Object val = row.get(c);
                    if (val != null && !val.toString().trim().isEmpty()) {
                        nonNullCount++;
                        if (!(val instanceof Number)) {
                            try {
                                Double.parseDouble(val.toString().trim());
                            } catch (NumberFormatException e) {
                                allNumeric = false;
                                break;
                            }
                        }
                    }
                }
            }
            isNum[c] = (allNumeric && nonNullCount > 0);
        }
        return isNum;
    }

    public static Builder builder(String title) {
        return new Builder(title);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getNetworkName() {
        return networkName;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return headers.size();
    }

    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= rows.size()) return "";
        List<Object> r = rows.get(row);
        if (col < 0 || col >= r.size()) return "";
        return r.get(col);
    }

    public boolean isNumericColumn(int col) {
        if (col >= 0 && col < numericColumns.length) {
            return numericColumns[col];
        }
        return false;
    }

    /**
     * Serializes to RFC 4180 CSV.
     */
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(escapeCsv(headers.get(i)));
        }
        sb.append("\n");

        for (List<Object> row : rows) {
            for (int i = 0; i < headers.size(); i++) {
                if (i > 0) sb.append(",");
                Object val = (i < row.size()) ? row.get(i) : "";
                sb.append(escapeCsv(formatValue(val, isNumericColumn(i))));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Serializes to Tab-Separated Values (TSV).
     */
    public String toTsv() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) sb.append("\t");
            sb.append(headers.get(i).replace("\t", " ").replace("\n", " "));
        }
        sb.append("\n");

        for (List<Object> row : rows) {
            for (int i = 0; i < headers.size(); i++) {
                if (i > 0) sb.append("\t");
                Object val = (i < row.size()) ? row.get(i) : "";
                sb.append(formatValue(val, isNumericColumn(i)).replace("\t", " ").replace("\n", " "));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Serializes to clean, formatted JSON.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"id\": \"").append(escapeJson(id)).append("\",\n");
        sb.append("  \"title\": \"").append(escapeJson(title)).append("\",\n");
        if (subtitle != null && !subtitle.isEmpty()) {
            sb.append("  \"subtitle\": \"").append(escapeJson(subtitle)).append("\",\n");
        }
        if (networkName != null && !networkName.isEmpty()) {
            sb.append("  \"network\": \"").append(escapeJson(networkName)).append("\",\n");
        }
        sb.append("  \"columns\": [");
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(escapeJson(headers.get(i))).append("\"");
        }
        sb.append("],\n");
        sb.append("  \"rowCount\": ").append(rows.size()).append(",\n");
        sb.append("  \"rows\": [\n");
        for (int r = 0; r < rows.size(); r++) {
            List<Object> row = rows.get(r);
            sb.append("    [");
            for (int c = 0; c < headers.size(); c++) {
                if (c > 0) sb.append(", ");
                Object val = (c < row.size()) ? row.get(c) : null;
                if (val == null) {
                    sb.append("null");
                } else if (val instanceof Number) {
                    if (val instanceof Double || val instanceof Float) {
                        double d = ((Number) val).doubleValue();
                        if (Double.isNaN(d) || Double.isInfinite(d)) {
                            sb.append("\"").append(d).append("\"");
                        } else {
                            sb.append(String.format(Locale.ROOT, "%.4f", d));
                        }
                    } else {
                        sb.append(val);
                    }
                } else {
                    sb.append("\"").append(escapeJson(val.toString())).append("\"");
                }
            }
            sb.append("]");
            if (r < rows.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Serializes to a modern styled HTML table.
     * Compatible with Swing HTMLEditorKit.
     */
    public String toHtmlTable(boolean withExportLink) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='margin-top:12px; margin-bottom:14px;'>\n");
        sb.append("<table style='width:100%; border:none; margin-bottom:4px;'><tr>");
        sb.append("<td style='border:none; padding:0; text-align:left;'>");
        sb.append("<span style='font-size:13px; font-weight:bold; color:#004080;'>").append(escapeHtml(title)).append("</span>");
        if (subtitle != null && !subtitle.isEmpty()) {
            sb.append("<span style='font-size:11px; color:#666666; margin-left:8px;'>").append(escapeHtml(subtitle)).append("</span>");
        }
        sb.append("</td>");

        if (withExportLink && id != null) {
            sb.append("<td style='border:none; padding:0; text-align:right;'>");
            sb.append("<a href='export:").append(id).append("' style='font-size:11px; color:#0055aa; text-decoration:none;'><b>[Export Table (CSV/JSON)]</b></a>");
            sb.append("</td>");
        }
        sb.append("</tr></table>\n");

        sb.append("<table style='border-collapse:collapse; width:100%; max-width:750px; font-size:12px; border:1px solid #d0d7de;'>\n");
        sb.append("<thead><tr style='background:#f0f4f8; color:#1f2328;'>\n");
        for (int i = 0; i < headers.size(); i++) {
            boolean num = isNumericColumn(i);
            sb.append("  <th style='padding:6px 10px; border:1px solid #d0d7de; font-weight:bold; text-align:")
              .append(num ? "right" : "left").append(";'>")
              .append(escapeHtml(headers.get(i)))
              .append("</th>\n");
        }
        sb.append("</tr></thead>\n<tbody>\n");

        for (int r = 0; r < rows.size(); r++) {
            List<Object> row = rows.get(r);
            String bg = (r % 2 == 0) ? "#ffffff" : "#f9fafb";
            sb.append("<tr style='background:").append(bg).append(";'>\n");
            for (int c = 0; c < headers.size(); c++) {
                boolean num = isNumericColumn(c);
                Object val = (c < row.size()) ? row.get(c) : "";
                String strVal = formatValue(val, num);
                sb.append("  <td style='padding:5px 10px; border:1px solid #e1e4e8; text-align:")
                  .append(num ? "right" : "left").append(";'>")
                  .append(escapeHtml(strVal))
                  .append("</td>\n");
            }
            sb.append("</tr>\n");
        }
        sb.append("</tbody>\n</table>\n");
        sb.append("</div>\n");
        return sb.toString();
    }

    /**
     * Serializes to formatted ASCII plain text table with aligned columns.
     */
    public String toPlainTextTable() {
        int cols = headers.size();
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) {
            widths[i] = headers.get(i).length();
        }
        for (List<Object> row : rows) {
            for (int i = 0; i < cols; i++) {
                Object val = (i < row.size()) ? row.get(i) : "";
                String str = formatValue(val, isNumericColumn(i));
                if (str.length() > widths[i]) {
                    widths[i] = str.length();
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        // Top border
        appendAsciiLine(sb, widths);
        // Headers
        sb.append("|");
        for (int i = 0; i < cols; i++) {
            boolean num = isNumericColumn(i);
            sb.append(" ").append(pad(headers.get(i), widths[i], num)).append(" |");
        }
        sb.append("\n");
        // Header separator
        appendAsciiLine(sb, widths);

        // Data rows
        for (List<Object> row : rows) {
            sb.append("|");
            for (int i = 0; i < cols; i++) {
                boolean num = isNumericColumn(i);
                Object val = (i < row.size()) ? row.get(i) : "";
                String str = formatValue(val, num);
                sb.append(" ").append(pad(str, widths[i], num)).append(" |");
            }
            sb.append("\n");
        }
        // Bottom border
        appendAsciiLine(sb, widths);

        return sb.toString();
    }

    private static void appendAsciiLine(StringBuilder sb, int[] widths) {
        sb.append("+");
        for (int w : widths) {
            for (int k = 0; k < w + 2; k++) {
                sb.append("-");
            }
            sb.append("+");
        }
        sb.append("\n");
    }

    private static String pad(String s, int width, boolean rightAlign) {
        if (s == null) s = "";
        int padding = width - s.length();
        if (padding <= 0) return s;
        StringBuilder sb = new StringBuilder();
        if (rightAlign) {
            for (int i = 0; i < padding; i++) sb.append(" ");
            sb.append(s);
        } else {
            sb.append(s);
            for (int i = 0; i < padding; i++) sb.append(" ");
        }
        return sb.toString();
    }

    public TableModel toTableModel() {
        return new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return rows.size();
            }

            @Override
            public int getColumnCount() {
                return headers.size();
            }

            @Override
            public String getColumnName(int col) {
                return headers.get(col);
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return TableData.this.getValueAt(rowIndex, columnIndex);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (isNumericColumn(columnIndex)) {
                    return Double.class;
                }
                return String.class;
            }
        };
    }

    private static String formatValue(Object val, boolean isNumeric) {
        if (val == null) return "";
        if (val instanceof Float || val instanceof Double) {
            double d = ((Number) val).doubleValue();
            if (Double.isNaN(d)) return "NaN";
            if (Double.isInfinite(d)) return (d > 0) ? "+Inf" : "-Inf";
            return String.format(Locale.ROOT, "%.4f", d);
        }
        return val.toString();
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!needQuotes) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
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

    public static final class Builder {
        private String id;
        private final String title;
        private String subtitle = "";
        private String networkName = "";
        private final List<String> headers = new ArrayList<>();
        private final List<List<Object>> rows = new ArrayList<>();
        private boolean[] numericColumns;

        private Builder(String title) {
            this.title = title;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder network(String networkName) {
            this.networkName = networkName;
            return this;
        }

        public Builder columns(String... headers) {
            this.headers.clear();
            for (String h : headers) {
                this.headers.add(h);
            }
            return this;
        }

        public Builder columns(List<String> headers) {
            this.headers.clear();
            this.headers.addAll(headers);
            return this;
        }

        public Builder isNumeric(boolean... numeric) {
            this.numericColumns = numeric.clone();
            return this;
        }

        public Builder addRow(Object... cells) {
            List<Object> row = new ArrayList<>(cells.length);
            for (Object c : cells) {
                row.add(c);
            }
            this.rows.add(row);
            return this;
        }

        public Builder addRow(List<Object> cells) {
            this.rows.add(new ArrayList<>(cells));
            return this;
        }

        public TableData build() {
            return new TableData(this);
        }
    }
}
