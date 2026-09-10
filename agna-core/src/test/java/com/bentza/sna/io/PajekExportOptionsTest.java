package com.bentza.sna.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Vector;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.Network;

/**
 * Pajek export-dialog round (2.1.3): extension replacement semantics and
 * the user-selected vector export.
 */
public class PajekExportOptionsTest
    {
    @Test
    public void setExtensionReplacesNotDoubles()
        {
        assertEquals("example3.net", IOUtils.setExtension("example3.agn", "net"));
        assertEquals("example3.net", IOUtils.setExtension("example3.csv", "net"));
        assertEquals("example3.net", IOUtils.setExtension("example3", "net"));
        assertEquals("example3.net", IOUtils.setExtension("example3.net", "NET"));
        assertEquals("example3.xls", IOUtils.setExtension("example3.net", "xls"));
        }

    @Test
    public void filteredVectorsOnlyEmitSelectedBlocks()
        {
        Network net = new Network(3);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        net.setValue(1f, 1, 2);
        net.setValue(1f, 2, 1);

        Vector selection = new Vector();
        selection.addElement("Betweenness");
        String out = new AgnaLib().getPajekVectors(net, selection);

        assertTrue(out.contains("% Betweenness"));
        assertFalse(out.contains("% Emission Degree"));
        assertFalse(out.contains("% Closeness"));
        }

    @Test
    public void emptySelectionYieldsNoVectors()
        {
        Network net = new Network(2);
        net.setValue(1f, 0, 1);
        net.setValue(1f, 1, 0);
        String out = new AgnaLib().getPajekVectors(net, new Vector());
        assertEquals("", out);
        }

    @Test
    public void setExtensionIgnoresDotsInsideDirectoryNames()
        {
        assertEquals("/Volumes/MacAPFS/Agna_2.1.3/Samples/example3.net",
                IOUtils.setExtension(
                        "/Volumes/MacAPFS/Agna_2.1.3/Samples/example3", "net"));
        assertEquals("a.b/name.txt",
                IOUtils.setExtension("a.b/name", "txt"));
        }

    @Test
    public void getExtensionIgnoresDotsInsideDirectoryNames()
        {
        assertEquals("net",
                IOUtils.getExtension("a.b/name.net"));
        assertEquals(null,
                IOUtils.getExtension("a.b/name"));
        }
    }
