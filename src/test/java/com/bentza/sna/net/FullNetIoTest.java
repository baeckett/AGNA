package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * I/O round trips over the bundled sample files and the text-matrix parser
 * (2.1.2 "errors on open tab-text data if diagonal does not exist (null
 * values)").
 */
public class FullNetIoTest
    {
    private static String readSample(String name) throws IOException
        {
        File file = new File("samples" + File.separator + name);
        assertTrue(file.exists(), "sample file exists: " + file);
        return new String(Files.readAllBytes(file.toPath()),
                StandardCharsets.ISO_8859_1);
        }

    @Test
    public void readsBundledAgnaSampleFile() throws IOException
        {
        FullNet full_net = new FullNet();
        full_net.readNetwork(readSample("example2.agn"), "agn");

        assertNotNull(full_net.getNetwork());
        assertEquals(9, full_net.getNetwork().getSize());
        // row 0 of example2.agn: 0 0 1 0 0 0 1 0 1
        assertEquals(0f, full_net.getNetwork().getValue(0, 0));
        assertEquals(1f, full_net.getNetwork().getValue(0, 2));
        assertEquals(1f, full_net.getNetwork().getValue(0, 6));
        assertEquals(1f, full_net.getNetwork().getValue(0, 8));
        // row 8: 1 1 0 1 0 0 0 0 0
        assertEquals(1f, full_net.getNetwork().getValue(8, 0));
        assertEquals(1f, full_net.getNetwork().getValue(8, 3));
        }

    @Test
    public void readsBundledPlainTextSample() throws IOException
        {
        FullNet full_net = new FullNet();
        full_net.readNetwork(readSample("4 full.txt"), "txt");

        assertNotNull(full_net.getNetwork());
        assertEquals(4, full_net.getNetwork().getSize());
        assertEquals(2f, full_net.getNetwork().getValue(0, 1));
        assertEquals(8f, full_net.getNetwork().getValue(1, 3));
        assertEquals(15f, full_net.getNetwork().getValue(3, 2));
        }

    @Test
    public void tabTextRoundTripIsLossless()
        {
        FullNet source = new FullNet();
        source.createDefaultNetwork(3);
        source.getNetwork().setValue(1f, 0, 1);
        source.getNetwork().setValue(1f, 1, 0);
        source.getNetwork().setValue(2.5f, 1, 2);
        source.getNetwork().setValue(0.125f, 2, 1);

        String text = source.getPlainTextNetwork(true);

        FullNet copy = new FullNet();
        copy.readNetwork(text, "txt");

        assertEquals(source.getNetwork().getSize(), copy.getNetwork().getSize());
        for (int i = 0; i < 3; i++)
            {
            for (int j = 0; j < 3; j++)
                {
                assertEquals(source.getNetwork().getValue(i, j),
                        copy.getNetwork().getValue(i, j),
                        "cell (" + i + "," + j + ")");
                }
            }
        }

    @Test
    public void missingDiagonalParsesWithZeroFilledCells()
        {
        // the diagonal of the first row is empty: 0 _ 1
        FullNet full_net = new FullNet();
        full_net.readNetwork("0\t\t1\n1\t0\t1\n1\t1\t0\n", "txt");

        assertEquals(3, full_net.getNetwork().getSize());
        assertEquals(0f, full_net.getNetwork().getValue(0, 0));
        assertEquals(0f, full_net.getNetwork().getValue(0, 1)); // empty cell
        assertEquals(1f, full_net.getNetwork().getValue(0, 2));
        assertEquals(1f, full_net.getNetwork().getValue(1, 0));
        assertEquals(0f, full_net.getNetwork().getValue(2, 2));
        }
    }