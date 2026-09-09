package com.bentza.sna.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.FullNet;

import jxl.Cell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;

/**
 * 2.1.3: the Excel export must produce a workbook that round-trips: same
 * sheet name, same node names on the header row and column, and identical
 * sociomatrix values, readable by JExcelAPI itself.
 */
public class ExcelRoundTripTest
    {
    private static String readSample(String name) throws Exception
        {
        File file = new File("samples" + File.separator + name);
        assertTrue(file.exists(), "sample file exists: " + file);
        return new String(Files.readAllBytes(file.toPath()),
                StandardCharsets.ISO_8859_1);
        }

    @Test
    public void excelExportRoundTripsThroughJExcelApi() throws Exception
        {
        FullNet full_net = new FullNet();
        full_net.readNetwork(readSample("4 full.txt"), "txt");
        assertNotNull(full_net.getNetwork());
        assertEquals(4, full_net.getNetwork().getSize());

        File xls = File.createTempFile("agna_excel_rt", ".xls");
        xls.deleteOnExit();
        ExcelExporter exporter = new ExcelExporter();
        String error = exporter.saveExcelNetwork(full_net,
                xls.getAbsolutePath());
        assertNull(error, "export must succeed: " + error);

        Workbook wb = Workbook.getWorkbook(xls);
        try
            {
            Sheet sheet = wb.getSheet(0);
            assertNotNull(sheet);
            assertEquals(full_net.getNetwork().getName(), sheet.getName());

            final int nn = full_net.getNetwork().getSize();
            final float[][] mat = full_net.getNetwork().getMatrix();
            for (int i = 0; i < nn; i++)
                {
                Cell name_h = sheet.getCell(i + 1, 0);
                Cell name_v = sheet.getCell(0, i + 1);
                assertEquals(full_net.getNetwork().getActorName(i),
                        name_h.getContents());
                assertEquals(full_net.getNetwork().getActorName(i),
                        name_v.getContents());
                for (int j = 0; j < nn; j++)
                    {
                    Cell cell = sheet.getCell(j + 1, i + 1);
                    double value = ((NumberCell) cell).getValue();
                    assertEquals(mat[i][j], value, 1e-6,
                            "cell (" + i + "," + j + ")");
                    }
                }
            } finally
            {
            wb.close();
            }
        }
    }
