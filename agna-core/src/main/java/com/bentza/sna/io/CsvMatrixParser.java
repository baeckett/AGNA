/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.io;

import java.util.ArrayList;
import java.util.List;

/**
 * 2.2.2: structured RFC 4180 reader for labelled or unlabelled sociomatrices
 * in CSV / TSV / semicolon-separated text.
 *
 * <p>Supports quoted fields (separators, quotes and line breaks inside actor
 * names), a UTF-8 byte-order mark, decimal commas inside quoted cells,
 * Excel's trailing separators, and auto-detection of ',' / ';' / tab. The
 * layout rules are the ones checked by {@link InputIntegrityChecker}: an
 * optional header row of actor names (first cell empty or a label) and an
 * optional first column of row labels.
 */
public final class CsvMatrixParser
    {
    public static final class Result
        {
        public final String[] names;
        public final float[][] values;

        Result(String[] names, float[][] values)
            {
            this.names = names;
            this.values = values;
            }
        }

    private CsvMatrixParser()
        {
        }

    /** Guesses the separator from the first non-empty line (outside quotes). */
    public static char detectSeparator(String text, char fallback)
        {
        String first = null;
        for (String l : text.split("\r?\n"))
            if (!l.trim().isEmpty())
                {
                first = l;
                break;
                }
        if (first == null)
            return fallback;
        int comma = 0, semi = 0, tab = 0;
        boolean q = false;
        for (char c : first.toCharArray())
            {
            if (c == '"')
                q = !q;
            else if (!q)
                {
                if (c == ',') comma++;
                else if (c == ';') semi++;
                else if (c == '\t') tab++;
                }
            }
        if (tab > comma && tab > semi) return '\t';
        if (semi > comma) return ';';
        if (comma > 0) return ',';
        return fallback;
        }

    /** Splits text into records of fields (RFC 4180: quotes may span lines). */
    public static List<List<String>> records(String text, char sep)
        {
        if (text.startsWith("\uFEFF"))
            text = text.substring(1);
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean q = false, any = false;
        for (int i = 0; i < text.length(); i++)
            {
            char c = text.charAt(i);
            if (q)
                {
                if (c == '"')
                    {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"')
                        {
                        cur.append('"');
                        i++;
                        }
                    else
                        q = false;
                    }
                else
                    cur.append(c);
                continue;
                }
            if (c == '"')
                {
                q = true;
                any = true;
                }
            else if (c == sep)
                {
                row.add(cur.toString().trim());
                cur.setLength(0);
                any = true;
                }
            else if (c == '\n' || c == '\r')
                {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n')
                    i++;
                row.add(cur.toString().trim());
                cur.setLength(0);
                addRow(rows, row, any);
                row = new ArrayList<>();
                any = false;
                }
            else
                {
                cur.append(c);
                if (!Character.isWhitespace(c))
                    any = true;
                }
            }
        row.add(cur.toString().trim());
        addRow(rows, row, any);
        return rows;
        }

    private static void addRow(List<List<String>> rows, List<String> row, boolean any)
        {
        if (!any)
            return;
        while (row.size() > 1 && row.get(row.size() - 1).isEmpty())
            row.remove(row.size() - 1);
        rows.add(row);
        }

    static Float number(String s)
        {
        if (s == null || s.isEmpty())
            return 0f;
        try
            {
            float v = Float.parseFloat(s.replace(',', '.'));
            return (Float.isNaN(v) || Float.isInfinite(v)) ? null : v;
            }
        catch (NumberFormatException e)
            {
            return null;
            }
        }

    /**
     * Parses a square sociomatrix. Returns null when the content is not a
     * well-formed square matrix (callers then report or fall back).
     */
    public static Result parse(String text, char sep)
        {
        List<List<String>> rows = records(text, sep);
        if (rows.isEmpty())
            return null;
        List<String> first = rows.get(0);
        boolean header = first.size() == 1 ? number(first.get(0)) == null : false;
        for (int j = 1; j < first.size(); j++)
            if (number(first.get(j)) == null)
                header = true;
        // an empty corner cell above a first column of labels marks a header
        // even when the actor names are numbers ("1","2",... as Agna writes)
        if (!header && first.size() > 1 && first.get(0).isEmpty() && rows.size() == first.size())
            header = true;
        List<List<String>> data = header ? rows.subList(1, rows.size()) : rows;
        int n = data.size();
        if (n == 0)
            return null;
        boolean labels = header && first.get(0).isEmpty();
        if (!labels)
            for (List<String> r : data)
                if (!r.isEmpty() && !r.get(0).isEmpty() && number(r.get(0)) == null)
                    labels = true;
        int off = labels ? 1 : 0;
        float[][] v = new float[n][n];
        String[] names = new String[n];
        for (int i = 0; i < n; i++)
            {
            List<String> r = data.get(i);
            if (r.size() - off > n)
                return null;
            if (r.size() - off < n && !(r.size() - off == 0))
                {
                // short rows are only accepted when the missing cells are trailing empties
                for (int j = r.size(); j < n + off; j++)
                    { /* treated as 0 */ }
                }
            for (int j = 0; j < n; j++)
                {
                String cell = (j + off) < r.size() ? r.get(j + off) : "";
                Float f = number(cell);
                if (f == null)
                    return null;
                v[i][j] = f;
                }
            names[i] = labels ? r.get(0) : null;
            }
        if (header)
            {
            int hoff = labels ? 1 : (first.size() == n + 1 ? 1 : 0);
            if (first.size() - hoff != n)
                return null;
            for (int j = 0; j < n; j++)
                if (names[j] == null || names[j].isEmpty())
                    names[j] = first.get(j + hoff);
            }
        for (int i = 0; i < n; i++)
            if (names[i] == null || names[i].isEmpty())
                names[i] = String.valueOf(i + 1);
        return new Result(names, v);
        }
    }
