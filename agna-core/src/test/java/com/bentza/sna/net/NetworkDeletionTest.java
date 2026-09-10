package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * Verifies the node-deletion fix (2.1.2 "delete two nodes, and the matrix gets
 * some extra zeros"): deleting actors must remove exactly their row and column
 * from the matrix, preserving every other value.
 */
public class NetworkDeletionTest
    {
    /**
     * Matrix from the bundled sample "4 full.txt".
     */
    private static final float[][] SAMPLE_4 = new float[][] { { 0f, 2f, 3f, 4f },
            { 5f, 0f, 7f, 8f }, { 9f, 10f, 0f, 12f },
            { 13f, 14f, 15f, 0f } };

    private static float value(Network net, int i, int j)
        {
        return net.getValue(i, j);
        }

    @Test
    public void deleteActorRemovesRowAndColumn()
        {
        Network net = new Network(SAMPLE_4);
        assertEquals(4, net.getSize());

        net.deleteActor(1);

        assertEquals(3, net.getSize());
        // values around the deleted row/column must be untouched
        assertEquals(0f, value(net, 0, 0)); // original (0,0)
        assertEquals(3f, value(net, 0, 1)); // original (0,2)
        assertEquals(4f, value(net, 0, 2)); // original (0,3)
        assertEquals(9f, value(net, 1, 0)); // original (2,0)
        assertEquals(0f, value(net, 1, 1)); // original (2,2)
        assertEquals(12f, value(net, 1, 2)); // original (2,3)
        assertEquals(13f, value(net, 2, 0)); // original (3,0)
        assertEquals(15f, value(net, 2, 1)); // original (3,2)
        assertEquals(0f, value(net, 2, 2)); // original (3,3)
        }

    @Test
    public void deleteTwoNodesKeepsMatrixIntact()
        {
        Network net = new Network(SAMPLE_4);

        // simulate a user deleting two different nodes (the reported bug)
        net.deleteActor(1);
        net.deleteActor(0);

        assertEquals(2, net.getSize());
        assertEquals(0f, value(net, 0, 0)); // original (2,2)
        assertEquals(12f, value(net, 0, 1)); // original (2,3)
        assertEquals(15f, value(net, 1, 0)); // original (3,2)
        assertEquals(0f, value(net, 1, 1)); // original (3,3)
        }

    @Test
    public void deleteLastNodePreservesValues()
        {
        Network net = new Network(SAMPLE_4);

        net.deleteActor(3);

        assertEquals(3, net.getSize());
        assertEquals(0f, value(net, 0, 0));
        assertEquals(2f, value(net, 0, 1));
        assertEquals(9f, value(net, 2, 0));
        assertEquals(0f, value(net, 2, 2));
        }

    @Test
    public void deleteBelowMinimumSizeIsIgnored()
        {
        Network net = new Network(SAMPLE_4);
        net.deleteActor(1);
        net.deleteActor(1);
        // two deletions from 4 nodes reach the 2-node floor (guard: size < 3)
        assertEquals(2, net.getSize());
        // a third deletion must be ignored
        net.deleteActor(0);
        assertEquals(2, net.getSize());
        }
    }