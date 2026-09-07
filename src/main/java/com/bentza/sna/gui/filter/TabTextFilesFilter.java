package com.bentza.sna.gui.filter;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class TabTextFilesFilter extends javax.swing.filechooser.FileFilter
    {
    final static String txt = "txt";

    final static String text = "text";

    final static String dat = "dat";

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
            if (txt.equals(extension) || text.equals(extension)
                    || dat.equals(extension))
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
        return "Text tab-separated (*.txt; *.dat; *.text)";
        }
    }