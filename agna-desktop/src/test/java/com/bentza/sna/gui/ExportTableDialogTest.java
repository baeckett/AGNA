/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.*;

import com.bentza.sna.report.TableData;
import com.bentza.sna.report.TableRegistry;
import java.awt.GraphicsEnvironment;
import org.junit.jupiter.api.Test;

public class ExportTableDialogTest {

    @Test
    public void testDialogCreation() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        TableData td = TableData.builder("Dialog Test")
                .columns("A", "B")
                .addRow("1", "2")
                .build();
        TableRegistry.register(td);

        ExportTableDialog dlg = new ExportTableDialog(null, td);
        assertNotNull(dlg);
        assertEquals("Export Analysis Table - AGNA", dlg.getTitle());
        assertTrue(dlg.getWidth() >= 660, "Dialog width should be at least 660px");
        assertTrue(dlg.getHeight() >= 460, "Dialog height should be at least 460px");
        dlg.dispose();
    }
}
