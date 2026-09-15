/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui.filter;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class GraphSONFilesFilter extends FileFilter
    {
    final static String graphson = "graphson";
    final static String json = "json";

    public boolean accept(File tmp_file)
        {
        if (tmp_file.isDirectory())
            return true;
        String str = tmp_file.getName();
        int i = str.lastIndexOf('.');
        if (i > 0 && i < str.length() - 1)
            {
            String ext = str.substring(i + 1).toLowerCase();
            return graphson.equals(ext) || json.equals(ext);
            }
        return false;
        }

    public String getDescription()
        {
        return "GraphSON/JSON files (*.json, *.graphson)";
        }
    }
