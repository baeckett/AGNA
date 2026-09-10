package com.bentza.sna.gui.filter;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ExcelFilesFilter extends javax.swing.filechooser.FileFilter
    {
    final static String xls = "xls";
    final static String xlsx = "xlsx";

    public boolean accept(File tmp_file)
        {
        if (tmp_file.isDirectory())
            {
            return true;
            }

        String str = tmp_file.getName();
        int i = str.lastIndexOf('.');

        if (i > 0 && i < str.length() - 1)
            {
            String extension = str.substring(i + 1).toLowerCase();
            if (xls.equals(extension) || xlsx.equals(extension))
                {
                return true;
                } else
                {
                return false;
                }

            }
        return false;
        }

    public String getDescription()
        {
        return "Microsoft Excel Spreadsheet (*.xls)";
        }
    }