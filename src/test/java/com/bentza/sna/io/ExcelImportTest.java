package com.bentza.sna.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;

/**
 * 2.1.3: opening an Excel workbook must produce a proper network —
 * both .xlsx (Numbers output) and legacy .xls (own export, read back).
 */
public class ExcelImportTest
    {
    private static String readSample(String name) throws Exception
        {
        File file = new File("samples" + File.separator + name);
        return new String(Files.readAllBytes(file.toPath()),
                StandardCharsets.ISO_8859_1);
        }

    @Test
    public void importsNumbersXlsxSociomatrix() throws Exception
        {
        FullNet full_net = new FullNet();
        String error = full_net.readExcelFile(
                new File("samples/numbers_example.xlsx"), "xlsx");
        assertNull(error, "import must succeed: " + error);

        Network net = full_net.getNetwork();
        assertEquals(10, net.getSize());
        assertEquals("Example 3", net.getName());
        assertEquals("1", net.getActorName(0));
        assertEquals("10", net.getActorName(9));
        assertEquals(0f, net.getValue(0, 0));
        assertEquals(1f, net.getValue(0, 1));
        assertEquals(1f, net.getValue(0, 2));
        assertEquals(0f, net.getValue(1, 1));
        }

    @Test
    public void importsLegacyXlsExport() throws Exception
        {
        FullNet source = new FullNet();
        source.readNetwork(readSample("4 full.txt"), "txt");

        File xls = File.createTempFile("agna_excel_rt", ".xls");
        xls.deleteOnExit();
        ExcelExporter exporter = new ExcelExporter();
        assertNull(exporter.saveExcelNetwork(source, xls.getAbsolutePath()));

        FullNet back = new FullNet();
        String error = back.readExcelFile(xls, "xls");
        assertNull(error, "legacy import must succeed: " + error);

        Network net = back.getNetwork();
        assertEquals(4, net.getSize());
        assertEquals(2f, net.getValue(0, 1));
        assertEquals(8f, net.getValue(1, 3));
        assertEquals(15f, net.getValue(3, 2));
        }
    }