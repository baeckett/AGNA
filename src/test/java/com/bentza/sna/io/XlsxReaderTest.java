package com.bentza.sna.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the bundled OOXML reader must read a real xlsx produced by
 * Apple Numbers (a sociomatrix exported by Agna), and a hand-crafted
 * sparse sheet (missing cells become empty strings).
 */
public class XlsxReaderTest
    {
    @Test
    public void readsNumbersExportedSociomatrix() throws Exception
        {
        String[][] grid = XlsxReader.readFirstSheet(
                new File("samples/numbers_example.xlsx"));
        assertEquals(11, grid.length);
        assertEquals(11, grid[0].length);
        // header row and label column share the shared strings
        assertEquals("1", grid[0][1]);
        assertEquals("1", grid[1][0]);
        // matrix values seen in the workbook: B2=0 C2=1 D2=1 E2=0, B3=1 C3=0
        assertEquals("0", grid[1][1]);
        assertEquals("1", grid[1][2]);
        assertEquals("1", grid[1][3]);
        assertEquals("1", grid[2][1]);
        assertEquals("0", grid[2][2]);
        assertEquals("Example 3", XlsxReader.readFirstSheetName(
                new File("samples/numbers_example.xlsx")));
        }

    @Test
    public void sparseCellsBecomeEmptyStrings() throws Exception
        {
        File temp = File.createTempFile("agna_xlsx_sparse", ".xlsx");
        temp.deleteOnExit();
        String sheet = "<?xml version=\"1.0\"?><worksheet "
                + "xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<sheetData>"
                + "<row r=\"1\"><c r=\"A1\" t=\"inlineStr\"><is><t>n</t></is></c>"
                + "<c r=\"B1\" t=\"s\"><v>0</v></c><c r=\"C1\" t=\"s\"><v>1</v></c></row>"
                + "<row r=\"2\"><c r=\"A2\" t=\"s\"><v>2</v></c>"
                + "<c r=\"C2\"><v>7</v></c></row>"
                + "</sheetData></worksheet>";
        String sst = "<?xml version=\"1.0\"?><sst "
                + "xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<si><t>a</t></si><si><t>b</t></si><si><t>x</t></si></sst>";
        String workbook = "<?xml version=\"1.0\"?><workbook "
                + "xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<sheets><sheet name=\"Gappy\" sheetId=\"1\"/></sheets></workbook>";
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(
                temp)))
            {
            zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            zip.write(sheet.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
            zip.write(sst.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("xl/workbook.xml"));
            zip.write(workbook.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            }

        String[][] grid = XlsxReader.readFirstSheet(temp);
        assertEquals("n", grid[0][0]);
        assertEquals("a", grid[0][1]);
        assertEquals("b", grid[0][2]);
        assertEquals("x", grid[1][0]);
        assertEquals("", grid[1][1]); // missing cell B2
        assertEquals("7", grid[1][2]);
        assertEquals("Gappy", XlsxReader.readFirstSheetName(temp));
        assertTrue(grid.length == 2 && grid[0].length == 3);
        }
    }