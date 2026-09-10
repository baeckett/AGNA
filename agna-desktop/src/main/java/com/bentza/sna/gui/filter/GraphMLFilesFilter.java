package com.bentza.sna.gui.filter;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class GraphMLFilesFilter extends FileFilter
    {
    final static String graphml = "graphml";

    public boolean accept(File tmp_file)
        {
        if (tmp_file.isDirectory())
            return true;
        String str = tmp_file.getName();
        int i = str.lastIndexOf('.');
        if (i > 0 && i < str.length() - 1)
            {
            return graphml.equals(str.substring(i + 1).toLowerCase());
            }
        return false;
        }

    public String getDescription()
        {
        return "GraphML files (*.graphml)";
        }
    }
