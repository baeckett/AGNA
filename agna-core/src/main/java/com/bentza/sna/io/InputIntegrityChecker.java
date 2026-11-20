/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.io;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 2.2.2: structural integrity checks on raw network text, run before the
 * (deliberately lenient) legacy parsers. The parsers silently coerce bad
 * input (random bytes read as a plain-text matrix, "yes"/"no" cells read as
 * 0, NaN cells counted as ties, arcs to non-existent Pajek vertices dropped,
 * truncated .agn files completed with zeros). This class reports such
 * problems explicitly so that `agna validate` can say "invalid" and other
 * commands can refuse to analyse corrupted data.
 *
 * <p>Only unambiguous problems are reported as ERROR; questionable but
 * loadable input is a WARNING.
 */
public final class InputIntegrityChecker
    {
    public enum Severity { ERROR, WARNING }

    public static final class Issue
        {
        public final Severity severity;
        public final String code;
        public final String message;

        public Issue(Severity severity, String code, String message)
            {
            this.severity = severity;
            this.code = code;
            this.message = message;
            }

        @Override
        public String toString()
            {
            return severity + " [" + code + "] " + message;
            }
        }

    private InputIntegrityChecker()
        {
        }

    public static boolean hasErrors(List<Issue> issues)
        {
        for (Issue i : issues)
            if (i.severity == Severity.ERROR)
                return true;
        return false;
        }

    public static List<Issue> check(String text, String extension)
        {
        List<Issue> out = new ArrayList<>();
        String ext = extension == null ? "agn" : extension.toLowerCase(Locale.ROOT);
        if (text == null || text.trim().isEmpty())
            {
            out.add(new Issue(Severity.ERROR, "EMPTY_FILE", "The file is empty."));
            return out;
            }
        if (looksBinary(text))
            {
            out.add(new Issue(Severity.ERROR, "BINARY_CONTENT",
                    "The file contains binary (non-text) data; it is not a network file of type '" + ext + "'."));
            return out;
            }
        switch (ext)
            {
            case "agn":
                checkAgn(text, out);
                break;
            case "csv":
                checkMatrix(text, CsvMatrixParser.detectSeparator(text, ','), out);
                break;
            case "tsv":
                checkMatrix(text, '\t', out);
                break;
            case "net":
                checkPajek(text, out);
                break;
            default:
                break;
            }
        return out;
        }

    private static boolean looksBinary(String s)
        {
        int n = Math.min(s.length(), 8192);
        int bad = 0;
        for (int i = 0; i < n; i++)
            {
            char c = s.charAt(i);
            if (c == '\uFFFD' || (c < 0x20 && c != '\n' && c != '\r' && c != '\t' && c != '\f'))
                bad++;
            }
        return n > 0 && bad > Math.max(2, n / 100);
        }

    // ------------------------------------------------------------ .agn
    private static void checkAgn(String text, List<Issue> out)
        {
        if (text.indexOf("Agna Data File") < 0)
            {
            if (text.indexOf("NODES:") >= 0)
                return; // Agna 1.0 legacy file
            out.add(new Issue(Severity.ERROR, "AGN_NO_HEADER",
                    "Not an Agna file: the 'Agna Data File' header is missing. "
                    + "If this is a plain matrix, save it with a .txt, .csv or .tsv extension."));
            return;
            }
        String[] lines = text.split("\r?\n", -1);
        int declared = -1;
        int start = -1, end = -1;
        for (int i = 0; i < lines.length; i++)
            {
            String l = lines[i].trim();
            if (l.startsWith("Network Size"))
                {
                try
                    {
                    declared = Integer.parseInt(l.substring("Network Size".length()).trim());
                    }
                catch (NumberFormatException e)
                    {
                    out.add(new Issue(Severity.ERROR, "AGN_BAD_SIZE", "Unreadable 'Network Size' line: " + l));
                    }
                }
            else if (l.equals("Network Matrix") && start < 0)
                start = i;
            else if (l.equals("End Network Matrix") && end < 0)
                end = i;
            }
        if (start < 0)
            {
            out.add(new Issue(Severity.ERROR, "AGN_NO_MATRIX", "The 'Network Matrix' section is missing."));
            return;
            }
        if (end < 0)
            {
            out.add(new Issue(Severity.ERROR, "AGN_TRUNCATED",
                    "The file is truncated: 'End Network Matrix' was not found."));
            return;
            }
        // the reader treats the block as one whitespace-separated token
        // stream, so check the token count (N x N) and that every token is
        // a finite number
        int tokens = 0;
        for (int i = start + 1; i < end; i++)
            {
            String l = lines[i].trim();
            if (l.isEmpty())
                continue;
            for (String c : l.split("\\s+"))
                {
                tokens++;
                String bad = badNumber(c);
                if (bad != null)
                    {
                    out.add(new Issue(Severity.ERROR, "AGN_BAD_VALUE",
                            "Network Matrix, line " + (i - start) + ": " + bad + "."));
                    return;
                    }
                }
            }
        if (declared >= 0 && tokens != declared * declared)
            out.add(new Issue(Severity.ERROR, "AGN_MATRIX_SIZE", "The Network Matrix holds " + tokens
                    + " values; 'Network Size' " + declared + " requires " + (declared * declared) + "."));
        }

    // ------------------------------------------------------- csv / tsv
    private static List<String> splitRow(String line, char sep)
        {
        List<String> cells = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < line.length(); i++)
            {
            char c = line.charAt(i);
            if (c == '"')
                {
                if (q && i + 1 < line.length() && line.charAt(i + 1) == '"')
                    {
                    cur.append('"');
                    i++;
                    }
                else
                    q = !q;
                }
            else if (c == sep && !q)
                {
                cells.add(cur.toString().trim());
                cur.setLength(0);
                }
            else
                cur.append(c);
            }
        cells.add(cur.toString().trim());
        // trailing empty cells (Excel exports) are not data
        while (cells.size() > 1 && cells.get(cells.size() - 1).isEmpty())
            cells.remove(cells.size() - 1);
        return cells;
        }

    private static boolean hasQuotedSeparator(String line, char sep)
        {
        boolean q = false;
        for (int i = 0; i < line.length(); i++)
            {
            char c = line.charAt(i);
            if (c == '"')
                q = !q;
            else if (c == sep && q)
                return true;
            }
        return false;
        }

    private static boolean isNumber(String s)
        {
        return badNumber(s) == null && !s.isEmpty();
        }

    /** null when s is a finite number (comma decimal accepted); otherwise a description. */
    private static String badNumber(String s)
        {
        if (s.isEmpty())
            return null;
        String t = s.replace(',', '.');
        double v;
        try
            {
            v = Double.parseDouble(t);
            }
        catch (NumberFormatException e)
            {
            return "non-numeric value '" + abbreviate(s) + "'";
            }
        if (Double.isNaN(v) || Double.isInfinite(v))
            return "value '" + s + "' is not a finite number";
        return null;
        }

    private static String abbreviate(String s)
        {
        return s.length() > 40 ? s.substring(0, 37) + "..." : s;
        }

    private static void checkMatrix(String text, char sep, List<Issue> out)
        {
        List<List<String>> rows = CsvMatrixParser.records(text, sep);
        if (rows.isEmpty())
            {
            out.add(new Issue(Severity.ERROR, "EMPTY_FILE", "The file contains no rows."));
            return;
            }
        // edge-list CSVs (source,target[,weight]) are not matrices
        List<String> first = rows.get(0);
        if (first.size() >= 2 && first.size() <= 3)
            {
            String h0 = first.get(0).toLowerCase(Locale.ROOT);
            if (h0.equals("source") || h0.equals("from") || h0.equals("ego"))
                return;
            }
        boolean header = false;
        for (int j = 1; j < first.size(); j++)
            if (!isNumber(first.get(j)))
                header = true;
        if (first.size() == 1 && !isNumber(first.get(0)))
            header = true;
        if (!header && first.size() > 1 && first.get(0).isEmpty() && rows.size() == first.size())
            header = true; // numeric actor names under an empty corner cell
        List<List<String>> data = header ? rows.subList(1, rows.size()) : rows;
        boolean labels = header && first.get(0).isEmpty();
        if (!labels)
            for (List<String> r : data)
                if (!r.isEmpty() && !isNumber(r.get(0)) && !r.get(0).isEmpty())
                    labels = true;
        int n = data.size();
        int expectedCols = header ? first.size() - (labels ? 1 : 0) : -1;
        Set<String> seen = new HashSet<>();
        List<String> dups = new ArrayList<>();
        if (header)
            for (int j = labels ? 1 : 0; j < first.size(); j++)
                if (!seen.add(first.get(j)) && !first.get(j).isEmpty())
                    dups.add(first.get(j));
        int lens = -1;
        for (int i = 0; i < n; i++)
            {
            List<String> r = data.get(i);
            int cols = r.size() - (labels ? 1 : 0);
            if (lens < 0)
                lens = cols;
            if (cols != lens || (expectedCols >= 0 && cols > expectedCols))
                {
                out.add(new Issue(Severity.ERROR, "RAGGED_ROWS", "Data row " + (i + 1) + " has " + cols
                        + " values, but " + (cols != lens ? "data row 1 has " + lens : "the header names " + expectedCols + " actors")
                        + ". Check for a separator inside an actor name or a missing cell."));
                return;
                }
            for (int j = labels ? 1 : 0; j < r.size(); j++)
                {
                String bad = badNumber(r.get(j));
                if (bad != null)
                    {
                    out.add(new Issue(Severity.ERROR, "BAD_CELL", "Row " + (i + 1) + ", column "
                            + (j + (labels ? 0 : 1)) + ": " + bad + "."));
                    return;
                    }
                }
            }
        int cols = expectedCols >= 0 ? expectedCols : lens;
        if (cols != n)
            {
            out.add(new Issue(Severity.ERROR, "NOT_SQUARE", "The sociomatrix is not square: " + n
                    + " rows x " + cols + " columns. A one-mode network needs an N x N matrix "
                    + "(for affiliation data, use a two-mode import)."));
            return;
            }
        if (!dups.isEmpty())
            out.add(new Issue(Severity.WARNING, "DUPLICATE_LABELS",
                    "Duplicate actor labels: " + dups + ". Results that name actors will be ambiguous."));
        }

    // ----------------------------------------------------------- Pajek
    private static void checkPajek(String text, List<Issue> out)
        {
        int nv = -1;
        boolean inArcs = false;
        boolean list = false;
        int lineNo = 0;
        for (String raw : text.split("\r?\n"))
            {
            lineNo++;
            String l = raw.trim();
            if (l.isEmpty() || l.startsWith("%"))
                continue;
            String low = l.toLowerCase(Locale.ROOT);
            if (low.startsWith("*vertices"))
                {
                String[] t = l.split("\\s+");
                try
                    {
                    nv = Integer.parseInt(t[1]);
                    }
                catch (Exception e)
                    {
                    out.add(new Issue(Severity.ERROR, "PAJEK_BAD_VERTICES", "Line " + lineNo + ": unreadable *Vertices count."));
                    return;
                    }
                inArcs = false;
                continue;
                }
            if (low.startsWith("*arcs") || low.startsWith("*edges"))
                {
                inArcs = true;
                list = false;
                continue;
                }
            if (low.startsWith("*arcslist") || low.startsWith("*edgeslist"))
                {
                inArcs = true;
                list = true;
                continue;
                }
            if (l.startsWith("*"))
                {
                inArcs = false;
                continue;
                }
            if (!inArcs || nv < 0)
                continue;
            String[] t = l.split("\\s+");
            int lim = list ? t.length : Math.min(2, t.length);
            if (!list && t.length < 2)
                {
                out.add(new Issue(Severity.ERROR, "PAJEK_BAD_LINE", "Line " + lineNo + ": malformed tie '" + abbreviate(l) + "'."));
                return;
                }
            for (int k = 0; k < lim; k++)
                {
                int v;
                try
                    {
                    v = Integer.parseInt(t[k]);
                    }
                catch (NumberFormatException e)
                    {
                    out.add(new Issue(Severity.ERROR, "PAJEK_BAD_LINE", "Line " + lineNo + ": malformed tie '" + abbreviate(l) + "'."));
                    return;
                    }
                if (v < 1 || v > nv)
                    {
                    out.add(new Issue(Severity.ERROR, "PAJEK_VERTEX_RANGE", "Line " + lineNo + ": vertex " + v
                            + " does not exist (*Vertices " + nv + ")."));
                    return;
                    }
                }
            if (!list && t.length >= 3)
                {
                String bad = badNumber(t[2]);
                if (bad != null)
                    {
                    out.add(new Issue(Severity.ERROR, "PAJEK_BAD_WEIGHT", "Line " + lineNo + ": " + bad + "."));
                    return;
                    }
                }
            }
        }
    }
