/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui.filter;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class DLFilesFilter extends javax.swing.filechooser.FileFilter {
    final static String dl = "dl";

    public boolean accept(File tmp_file) {
        if (tmp_file.isDirectory()) {
            return true;
        }

        String str = tmp_file.getName();
        int i = str.lastIndexOf('.');

        if (i > 0 && i < str.length() - 1) {
            String extension = str.substring(i + 1).toLowerCase();
            return dl.equals(extension);
        }
        return false;
    }

    public String getDescription() {
        return "UCINET DL (*.dl)";
    }
}
