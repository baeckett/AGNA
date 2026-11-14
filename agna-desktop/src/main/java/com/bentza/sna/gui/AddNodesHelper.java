/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Shared helper for adding new nodes to a network across both MainFrame and GrNet (Network Viewer).
 * Ensures identical behavior, upper limits, and confirmation warnings.
 */
public final class AddNodesHelper {

    /**
     * Threshold below which any number of nodes is added immediately without any confirmation warning.
     */
    public static final int LIMIT_WITHOUT_WARNING = 250;

    /**
     * Total resulting network size above which a warning is issued before allocating large matrices.
     */
    public static final int TOTAL_SIZE_WARNING_THRESHOLD = 500;

    /**
     * Maximum safe nodes supported by the generator and matrix engine.
     */
    public static final int MAX_NODES = 5000;

    private AddNodesHelper() {
    }

    /**
     * Prompts the user for the number of nodes to add with validation and safety warnings.
     *
     * @param parent parent GUI component
     * @param currentSize current number of actors in the network
     * @return positive number of nodes to add, or 0 if cancelled or invalid
     */
    public static int promptNumberOfNodes(Component parent, int currentSize) {
        String promptMsg = (currentSize > 0)
                ? String.format("Current network has %d actor(s).\n\nEnter number of nodes to add (1 - %d):", currentSize, MAX_NODES)
                : String.format("Enter number of nodes to add (1 - %d):", MAX_NODES);

        String input = (String) JOptionPane.showInputDialog(
                parent,
                promptMsg,
                "Add New Nodes",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "1"
        );

        if (input == null) {
            return 0; // User clicked Cancel or closed dialog
        }

        input = input.trim();
        if (input.isEmpty()) {
            return 0;
        }

        int count;
        try {
            count = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    parent,
                    "\"" + input + "\" is not a valid number. Please enter a positive integer.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
            return 0;
        }

        if (count <= 0) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please enter a positive whole number (at least 1).",
                    "Invalid Number",
                    JOptionPane.WARNING_MESSAGE
            );
            return 0;
        }

        if (count > MAX_NODES || (currentSize + count) > MAX_NODES) {
            JOptionPane.showMessageDialog(
                    parent,
                    String.format("The requested addition would exceed the maximum supported limit of %d nodes.", MAX_NODES),
                    "Limit Exceeded",
                    JOptionPane.WARNING_MESSAGE
            );
            return 0;
        }

        // Check if warning is required for large node additions
        if (count > LIMIT_WITHOUT_WARNING || (currentSize + count) > TOTAL_SIZE_WARNING_THRESHOLD) {
            int newTotal = currentSize + count;
            String warnMsg = String.format(
                    "You are requesting to add %d nodes, bringing the total network size to %d actors.\n"
                    + "Manipulating and rendering large sociomatrices in the table and viewer may require noticeable memory and computation time.\n\n"
                    + "Do you wish to proceed?",
                    count, newTotal
            );
            int choice = JOptionPane.showConfirmDialog(
                    parent,
                    warnMsg,
                    "Confirm Large Node Addition",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice != JOptionPane.YES_OPTION) {
                return 0;
            }
        }

        return count;
    }
}
