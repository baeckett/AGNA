package com.bentza.sna.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 2.1.3: minimal .xlsx (OOXML) reader, JDK-only. Reads the first worksheet
 * of a workbook produced by Excel, Numbers or LibreOffice into a dense
 * String[][] grid (missing cells become empty strings). Supports numbers,
 * shared strings, inline strings and booleans; rich-text runs are
 * concatenated.
 */
public class XlsxReader
    {
    public static String[][] readFirstSheet(File file) throws Exception
        {
        try (ZipFile zip = new ZipFile(file))
            {
            List<String> shared = parseSharedStrings(readEntry(zip,
                    "xl/sharedStrings.xml"));
            String sheet_xml = readEntry(zip, "xl/worksheets/sheet1.xml");
            if (sheet_xml == null)
                throw new Exception("not an xlsx workbook (no worksheet found)");
            return parseSheet(sheet_xml, shared);
            }
        }

    public static String readFirstSheetName(File file) throws Exception
        {
        try (ZipFile zip = new ZipFile(file))
            {
            String workbook_xml = readEntry(zip, "xl/workbook.xml");
            if (workbook_xml == null)
                return "";
            Document doc = parseXml(workbook_xml);
            NodeList sheets = doc.getElementsByTagName("sheet");
            if (sheets.getLength() > 0)
                {
                Node name = sheets.item(0).getAttributes()
                        .getNamedItem("name");
                if (name != null)
                    return name.getNodeValue();
                }
            return "";
            }
        }

    private static String readEntry(ZipFile zip, String name) throws Exception
        {
        java.util.zip.ZipEntry entry = zip.getEntry(name);
        if (entry == null)
            return null;
        return new String(zip.getInputStream(entry).readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);
        }

    private static Document parseXml(String xml) throws Exception
        {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true);
            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false);
            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false);
        return factory.newDocumentBuilder().parse(
                new java.io.ByteArrayInputStream(xml.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8)));
        }

    private static String localName(Node node)
        {
        String local = node.getLocalName();
        return local != null ? local : node.getNodeName();
        }

    private static String textOf(Element element)
        {
        StringBuilder sb = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
            {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE)
                {
                sb.append(child.getNodeValue());
                } else if (child.getNodeType() == Node.ELEMENT_NODE
                        && "t".equals(localName(child)))
                {
                sb.append(child.getTextContent());
                }
            }
        return sb.toString();
        }

    private static List<String> parseSharedStrings(String xml)
            throws Exception
        {
        List<String> shared = new ArrayList<>();
        if (xml == null)
            return shared;
        Document doc = parseXml(xml);
        NodeList items = doc.getElementsByTagName("si");
        for (int i = 0; i < items.getLength(); i++)
            {
            shared.add(textOf((Element) items.item(i)));
            }
        return shared;
        }

    private static int columnOf(String ref)
        {
        int column = 0;
        for (int i = 0; i < ref.length(); i++)
            {
            char c = ref.charAt(i);
            if (c < 'A' || c > 'Z')
                break;
            column = column * 26 + (c - 'A' + 1);
            }
        return column - 1; // zero based
        }

    private static int rowOf(String ref)
        {
        for (int i = 0; i < ref.length(); i++)
            {
            char c = ref.charAt(i);
            if (c >= '0' && c <= '9')
                {
                return Integer.parseInt(ref.substring(i)) - 1;
                }
            }
        return -1;
        }

    private static String cellValue(Element cell, List<String> shared)
        {
        String type = cell.getAttribute("t");
        NodeList children = cell.getChildNodes();
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < children.getLength(); i++)
            {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;
            String name = localName(child);
            if ("v".equals(name))
                {
                value.append(child.getTextContent());
                } else if ("is".equals(name))
                {
                value.append(textOf((Element) child));
                }
            }
        String raw = value.toString();
        if ("s".equals(type))
            {
            try
                {
                int index = Integer.parseInt(raw.trim());
                if (index >= 0 && index < shared.size())
                    return shared.get(index);
                } catch (NumberFormatException e)
                {
                }
            return "";
            }
        return raw;
        }

    private static String[][] parseSheet(String sheet_xml,
            List<String> shared) throws Exception
        {
        Document doc = parseXml(sheet_xml);
        NodeList rows = doc.getElementsByTagName("row");
        int max_row = -1;
        int max_col = -1;
        for (int r = 0; r < rows.getLength(); r++)
            {
            Element row = (Element) rows.item(r);
            NodeList cells = row.getElementsByTagName("c");
            for (int c = 0; c < cells.getLength(); c++)
                {
                Element cell = (Element) cells.item(c);
                String ref = cell.getAttribute("r");
                if (ref.length() == 0)
                    continue;
                max_row = Math.max(max_row, rowOf(ref));
                max_col = Math.max(max_col, columnOf(ref));
                }
            }
        String[][] grid = new String[max_row + 1][max_col + 1];
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[r].length; c++)
                grid[r][c] = "";
        for (int r = 0; r < rows.getLength(); r++)
            {
            Element row = (Element) rows.item(r);
            NodeList cells = row.getElementsByTagName("c");
            for (int c = 0; c < cells.getLength(); c++)
                {
                Element cell = (Element) cells.item(c);
                String ref = cell.getAttribute("r");
                if (ref.length() == 0)
                    continue;
                int rr = rowOf(ref);
                int cc = columnOf(ref);
                if (rr < 0 || cc < 0)
                    continue;
                grid[rr][cc] = cellValue(cell, shared);
                }
            }
        return grid;
        }
    }
