/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.io;

import com.bentza.sna.net.Network;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Exporter for UCINET DL (.dl) network data files in matrix format.
 */
public final class DLExporter {

    private DLExporter() {}

    public static String toDLString(Network net) {
        StringBuilder sb = new StringBuilder();
        int n = net.getSize();
        sb.append("dl\n");
        sb.append("n=").append(n).append("\n");
        sb.append("format=fullmatrix\n");
        sb.append("labels:\n");

        for (int i = 0; i < n; i++) {
            String name = net.getActor(i).getName().replaceAll("[ ,\\t\\r\\n]", "_");
            sb.append(name);
            if (i < n - 1) {
                sb.append(", ");
                if ((i + 1) % 10 == 0) sb.append("\n");
            }
        }
        sb.append("\ndata:\n");

        float[][] mat = net.getMatrix();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                float val = mat[i][j];
                if (val == (long) val) {
                    sb.append((long) val);
                } else {
                    sb.append(String.format(java.util.Locale.ROOT, "%.4f", val));
                }
                if (j < n - 1) sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void exportDL(Network net, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(toDLString(net));
        }
    }
}
