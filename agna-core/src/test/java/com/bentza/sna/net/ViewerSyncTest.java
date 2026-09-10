package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the Network Viewer area must stay in sync with the sociomatrix
 * for both directions of change - edits done on the main frame (scalar
 * transforms, transpose, merge) and node edits the viewer itself drives
 * (add, isolate). The area's node array is rebuilt from the network
 * after every update.
 */
public class ViewerSyncTest
    {
    private static String readSample(String name) throws Exception
        {
        return new String(Files.readAllBytes(new File(
                "samples" + File.separator + name).toPath()),
                StandardCharsets.ISO_8859_1);
        }

    private static FullNet fullWithArea(String sample, String ext)
        throws Exception
        {
        FullNet full = new FullNet();
        full.net_area = new NodeArea();
        full.readNetwork(readSample(sample), ext);
        return full;
        }

    @Test
    public void areaFollowsMainFrameTransforms() throws Exception
        {
        FullNet full = fullWithArea("10 circle.agn", "agn");
        assertEquals(10, full.net_area.my_nodes.length,
                "area built from the loaded network");

        full.addScalar(2f); // wrapper updates the area
        assertEquals(10, full.net_area.my_nodes.length);

        full.transpose();
        assertEquals(10, full.net_area.my_nodes.length);
        for (int i = 0; i < 10; i++)
            {
            assertTrue(full.net_area.my_nodes[i] == full.getNetwork()
                    .getActor(i), "area nodes reference the network actors");
            }
        }

    @Test
    public void areaFollowsMerge() throws Exception
        {
        FullNet a = fullWithArea("4 full.txt", "txt");
        FullNet b = new FullNet();
        b.readNetwork(readSample("10 circle.agn"), "agn");
        a.mergeWith(b.getNetwork(), Network.MERGE_SUM);
        assertEquals(a.getNetwork().getSize(),
                a.net_area.my_nodes.length,
                "area shows the merged network size");
        }

    @Test
    public void viewerSideNodeChangesSyncBack() throws Exception
        {
        FullNet full = fullWithArea("10 star.agn", "agn");
        Network net = full.getNetwork();
        final int n = net.getSize();

        // the viewer adds a node, then refreshes the area
        net.addActor(-1, -1, -1, false);
        full.net_area.updateArea(net);
        assertEquals(n + 1, full.net_area.my_nodes.length);

        // isolating a node zeroes its row and column in the matrix the
        // area paints from
        net.isolateActor(n - 1);
        full.net_area.updateArea(net);
        for (int j = 0; j < net.getSize(); j++)
            {
            if (j != n - 1)
                {
                assertEquals(0f, net.getValue(n - 1, j), 1e-6f);
                assertEquals(0f, net.getValue(j, n - 1), 1e-6f);
                }
            }
        assertEquals(n + 1, full.net_area.my_nodes.length);
        }
    }