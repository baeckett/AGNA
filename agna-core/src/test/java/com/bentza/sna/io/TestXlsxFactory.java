package com.bentza.sna.io;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 2.1.3: builds the sociomatrix workbook fixture in code. The xlsx sample
 * that used to ship with the project was removed from the samples folder,
 * so the OOXML reader keeps a regression fixture with the same structure
 * (header row, label column, 10x10 binary matrix, sheet name).
 */
public final class TestXlsxFactory
    {
    private TestXlsxFactory() { }

    /**
     * Creates a temporary xlsx workbook with a {@code size} x {@code size}
     * sociomatrix: header row and label column numbered 1..size, zero
     * diagonal, all other cells 1.
     */
    public static File sociomatrixWorkbook(int size, String sheetName)
        throws Exception
        {
        File temp = File.createTempFile("agna_xlsx_matrix", ".xlsx");
        temp.deleteOnExit();

        StringBuilder sheet = new StringBuilder(
                "<?xml version=\"1.0\"?><worksheet "
                + "xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<sheetData><row r=\"1\"><c r=\"A1\" t=\"inlineStr\"><is><t></t></is></c>");
        for (int c = 0; c < size; c++)
            sheet.append("<c r=\"").append((char) ('B' + c)).append("1\" t=\"s\"><v>")
                    .append(c).append("</v></c>");
        sheet.append("</row>");
        for (int r = 0; r < size; r++)
            {
            sheet.append("<row r=\"").append(r + 2).append("\"><c r=\"A")
                    .append(r + 2).append("\" t=\"s\"><v>").append(r).append("</v></c>");
            for (int c = 0; c < size; c++)
                {
                int v = (r == c) ? 0 : 1;
                sheet.append("<c r=\"").append((char) ('B' + c)).append(r + 2)
                        .append("\"><v>").append(v).append("</v></c>");
                }
            sheet.append("</row>");
            }
        sheet.append("</sheetData></worksheet>");

        StringBuilder sst = new StringBuilder(
                "<?xml version=\"1.0\"?><sst "
                + "xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        for (int i = 0; i < size; i++)
            sst.append("<si><t>").append(i + 1).append("</t></si>");
        sst.append("</sst>");

        StringBuilder workbook = new StringBuilder(
                "<?xml version=\"1.0\"?><workbook "
                + "xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        workbook.append("<sheets><sheet name=\"").append(sheetName)
                .append("\" sheetId=\"1\"/></sheets></workbook>");

        try (ZipOutputStream zip = new ZipOutputStream(
                new FileOutputStream(temp)))
            {
            zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            zip.write(sheet.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
            zip.write(sst.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("xl/workbook.xml"));
            zip.write(workbook.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            }
        return temp;
        }
    }