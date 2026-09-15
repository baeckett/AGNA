package com.bentza.sna.io;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.bentza.sna.core.AppRuntime;
import java.io.File;
import java.util.Date;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * A class that returns a network as a MS Excel String ready to be saved as a
 * file.
 */
public class ExcelExporter
    {

    /**
     * Constructor
     */
    public ExcelExporter()
        {
        }

    /**
     * Writes a network as MS Excel file. Returns null on success, an error
     * String on failure. This class uses Apache POI (Apache-2.0).
     */
    public String saveExcelNetwork(FullNet tmp_full_net, String file_name)
        {

        try
            {
            Thread.sleep(500);
            AppRuntime.setProgress(60);
            } catch (Exception e1) {
      AgnaLog.warn("suppressed exception", e1);
      }

        final Network tmp_network = tmp_full_net.getNetwork();
        final String sheet_name = tmp_network.getName();

        try
            {
            // trying to create the file;
            // very unlikely to generate exception, as file_name has
            // already been tested.
            final int nn = tmp_network.getSize();
            final float[][] mat = tmp_network.getMatrix(); // data matrix
            try (HSSFWorkbook workbook = new HSSFWorkbook();
                    java.io.FileOutputStream out = new java.io.FileOutputStream(
                            file_name))
                {
                HSSFSheet sheet = workbook.createSheet(sheet_name);
                HSSFRow header = sheet.createRow(0);

                AppRuntime.setProgress(75);

                for (int i = 0; i < nn; i++)
                    {
                    String tmp_name = tmp_network.getActorName(i);
                    // writing node names:
                    header.createCell(i + 1).setCellValue(tmp_name);
                    HSSFRow line = sheet.createRow(i + 1);
                    line.createCell(0).setCellValue(tmp_name);
                    for (int j = 0; j < nn; j++)
                        {
                        // writing sociomatrix value (row i+1, column j+1):
                        line.createCell(j + 1).setCellValue(mat[i][j]);
                        }
                    }

                AppRuntime.setProgress(85);

                // cell values are now added to sheet; write the file:
                workbook.write(out);
                }
            return null;
            } catch (Exception e47)
            {
            return "Error creating file " + file_name;
            }
        }

    // End of class
    }