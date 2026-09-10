package com.bentza.sna.io;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.bentza.sna.core.AppRuntime;
import java.io.File;
import java.util.Date;
import jxl.*;
import jxl.write.*;

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
     * String on failure. This class uses the JExcelAPI OS library (LGPL).
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
            final File excel_file = new File(file_name);
            WritableWorkbook workbook = Workbook.createWorkbook(new File(
                    file_name));
            WritableSheet sheet = workbook.createSheet(sheet_name, 0);

            final int nn = tmp_network.getSize();
            final float[][] mat = tmp_network.getMatrix(); // data matrix
            String tmp_name = null;
            jxl.write.Number cell_value = null;
            jxl.write.Label cell_name = null;

            AppRuntime.setProgress(75);

            for (int i = 0; i < nn; i++)
                {
                tmp_name = tmp_network.getActorName(i);
                // writing node names:
                cell_name = new jxl.write.Label(i + 1, 0, tmp_name);
                sheet.addCell(cell_name);
                cell_name = new jxl.write.Label(0, i + 1, tmp_name);
                sheet.addCell(cell_name);
                for (int j = 0; j < nn; j++)
                    {
                    // writing sociomatrix value:
                    cell_value = new jxl.write.Number(j + 1, i + 1, mat[i][j]); // i =
                                                                                // line,
                                                                                // j =
                                                                                // column
                    sheet.addCell(cell_value);
                    }
                }

            AppRuntime.setProgress(85);

            // cell values are now added to sheet.
            // write the file:
            workbook.write();
            workbook.close();
            workbook = null;
            sheet = null;
            cell_value = null;
            cell_name = null;
            return null;
            } catch (Exception e47)
            {
            return "Error creating file " + file_name;
            }
        }

    // End of class
    }