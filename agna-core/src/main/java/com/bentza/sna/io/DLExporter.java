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

    public static void exportDL(Network net, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            int n = net.getSize();
            writer.write("dl");
            writer.newLine();
            writer.write("n=" + n);
            writer.newLine();
            writer.write("format=fullmatrix");
            writer.newLine();
            writer.write("labels:");
            writer.newLine();

            // Write labels comma-separated or space-separated
            for (int i = 0; i < n; i++) {
                String name = net.getActor(i).getName().replaceAll("[ ,\\t\\r\\n]", "_");
                writer.write(name);
                if (i < n - 1) {
                    writer.write(", ");
                    if ((i + 1) % 10 == 0) writer.newLine();
                }
            }
            writer.newLine();
            writer.write("data:");
            writer.newLine();

            float[][] mat = net.getMatrix();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    float val = mat[i][j];
                    if (val == (long) val) {
                        writer.write(String.valueOf((long) val));
                    } else {
                        writer.write(String.format(java.util.Locale.ROOT, "%.4f", val));
                    }
                    if (j < n - 1) writer.write(" ");
                }
                writer.newLine();
            }
        }
    }
}
