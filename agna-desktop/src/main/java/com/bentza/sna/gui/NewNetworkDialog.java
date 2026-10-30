/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.bentza.sna.net.Network;
import com.bentza.sna.net.NetworkGenerator;

/**
 * Enhanced modal dialog for File | New Network... in Agna 2.1.5.
 * Offers rich topology choices, on-the-spot validation, and parameter tuning:
 * 1. Empty network (isolated nodes)
 * 2. Star network (symmetric or asymmetric)
 * 3. Random binary connected network (no outsiders)
 * 4. Random weighted network (weights strictly in (0.0, 1.0))
 * 5. Circular ring network
 * 6. Full binary network (clique)
 * 7. Square lattice N x N (L x L grid)
 * 8. Hexagram lattice network (Star of David)
 */
public class NewNetworkDialog extends JDialog
    {
    private static final long serialVersionUID = 1L;

    public static final String TYPE_EMPTY = "Empty network (isolated nodes)";
    public static final String TYPE_STAR = "Star network";
    public static final String TYPE_RANDOM_CONNECTED = "Random binary connected network (no outsiders)";
    public static final String TYPE_RANDOM_WEIGHTED = "Random weighted network (values strictly between 0 and 1)";
    public static final String TYPE_CIRCULAR = "Circular network";
    public static final String TYPE_FULL = "Full binary network (complete)";
    public static final String TYPE_SQUARE_LATTICE = "Square lattice network (N x N)";
    public static final String TYPE_HEXAGRAM_LATTICE = "Hexagram lattice network (Star of David)";

    private static final String[] TYPES = {
            TYPE_EMPTY,
            TYPE_STAR,
            TYPE_RANDOM_CONNECTED,
            TYPE_RANDOM_WEIGHTED,
            TYPE_CIRCULAR,
            TYPE_FULL,
            TYPE_SQUARE_LATTICE,
            TYPE_HEXAGRAM_LATTICE
    };

    private JComboBox<String> typeCombo;
    private JTextField nodeCountField;
    private JLabel validationLabel;
    private JLabel estimatedNodesLabel;

    // Sub-options
    private JPanel dynamicOptionsPanel;
    private JCheckBox starSymmetricCheck;
    private JCheckBox weightedSymmetricCheck;
    private JSpinner weightedDensitySpinner;
    private JSpinner squareSideSpinner;
    private JSpinner hexagramOrderSpinner;

    private JButton okButton;
    private JButton cancelButton;

    private Network generatedNetwork = null;

    public NewNetworkDialog(Frame parent, int defaultNodes)
        {
        super(parent, "New Network", true);
        initComponents(Math.max(2, defaultNodes));
        pack();
        setMinimumSize(new Dimension(520, 380));
        setLocationRelativeTo(parent);
        }

    private void initComponents(int initialNodes)
        {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Row 0: Network Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Topology type:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        typeCombo = new JComboBox<>(TYPES);
        mainPanel.add(typeCombo, gbc);

        // Row 1: Number of Nodes
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel nodesLabel = new JLabel("Number of nodes:");
        mainPanel.add(nodesLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nodeCountField = new JTextField(String.valueOf(initialNodes), 10);
        mainPanel.add(nodeCountField, gbc);

        // Row 2: Validation / Warning status
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        validationLabel = new JLabel(" ");
        validationLabel.setFont(validationLabel.getFont().deriveFont(11f));
        mainPanel.add(validationLabel, gbc);

        // Row 3: Dynamic topology options
        gbc.gridy = 3;
        dynamicOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        dynamicOptionsPanel.setBorder(BorderFactory.createTitledBorder("Topology Options"));

        starSymmetricCheck = new JCheckBox("Symmetrical (undirected)", true);
        weightedSymmetricCheck = new JCheckBox("Symmetrical (undirected)", true);
        weightedDensitySpinner = new JSpinner(new SpinnerNumberModel(0.20, 0.01, 1.00, 0.05));
        squareSideSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 50, 1));
        hexagramOrderSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));

        estimatedNodesLabel = new JLabel("");
        mainPanel.add(dynamicOptionsPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        okButton = new JButton("Create Network");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        typeCombo.addItemListener(new ItemListener()
            {
            @Override
            public void itemStateChanged(ItemEvent e)
                {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                    updateDynamicPanel();
                    validateInput();
                    }
                }
            });

        nodeCountField.getDocument().addDocumentListener(new DocumentListener()
            {
            @Override
            public void insertUpdate(DocumentEvent e) { validateInput(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateInput(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateInput(); }
            });

        squareSideSpinner.addChangeListener(new ChangeListener()
            {
            @Override
            public void stateChanged(ChangeEvent e)
                {
                int side = (Integer) squareSideSpinner.getValue();
                nodeCountField.setText(String.valueOf(side * side));
                validateInput();
                }
            });

        hexagramOrderSpinner.addChangeListener(new ChangeListener()
            {
            @Override
            public void stateChanged(ChangeEvent e)
                {
                int order = (Integer) hexagramOrderSpinner.getValue();
                int count = calculateHexagramNodeCount(order);
                nodeCountField.setText(String.valueOf(count));
                validateInput();
                }
            });

        cancelButton.addActionListener(new ActionListener()
            {
            @Override
            public void actionPerformed(ActionEvent e)
                {
                generatedNetwork = null;
                dispose();
                }
            });

        okButton.addActionListener(new ActionListener()
            {
            @Override
            public void actionPerformed(ActionEvent e)
                {
                if (handleCreate())
                    {
                    dispose();
                    }
                }
            });

        updateDynamicPanel();
        validateInput();
        }

    private void updateDynamicPanel()
        {
        dynamicOptionsPanel.removeAll();
        String type = (String) typeCombo.getSelectedItem();
        nodeCountField.setEnabled(true);

        if (TYPE_STAR.equals(type))
            {
            dynamicOptionsPanel.add(starSymmetricCheck);
            }
        else if (TYPE_RANDOM_WEIGHTED.equals(type))
            {
            dynamicOptionsPanel.add(weightedSymmetricCheck);
            dynamicOptionsPanel.add(new JLabel("Density / probability:"));
            dynamicOptionsPanel.add(weightedDensitySpinner);
            }
        else if (TYPE_SQUARE_LATTICE.equals(type))
            {
            dynamicOptionsPanel.add(new JLabel("Grid side length (N):"));
            dynamicOptionsPanel.add(squareSideSpinner);
            int side = (Integer) squareSideSpinner.getValue();
            nodeCountField.setText(String.valueOf(side * side));
            nodeCountField.setEnabled(false); // Driven by side length
            }
        else if (TYPE_HEXAGRAM_LATTICE.equals(type))
            {
            dynamicOptionsPanel.add(new JLabel("Lattice order:"));
            dynamicOptionsPanel.add(hexagramOrderSpinner);
            int order = (Integer) hexagramOrderSpinner.getValue();
            int count = calculateHexagramNodeCount(order);
            nodeCountField.setText(String.valueOf(count));
            nodeCountField.setEnabled(false); // Driven by order
            }
        else
            {
            dynamicOptionsPanel.add(new JLabel("Standard options apply."));
            }

        dynamicOptionsPanel.revalidate();
        dynamicOptionsPanel.repaint();
        }

    private boolean validateInput()
        {
        String text = nodeCountField.getText().trim();
        if (text.isEmpty())
            {
            setValidationError("Please enter the number of nodes.");
            return false;
            }

        int val;
        try
            {
            val = Integer.parseInt(text);
            }
        catch (NumberFormatException e)
            {
            setValidationError("Nodes must be a valid whole number (natural number).");
            return false;
            }

        if (val < 2)
            {
            setValidationError("Number of nodes must be an integer greater than 1.");
            return false;
            }

        if (val > NetworkGenerator.MAX_SAFE_NODES)
            {
            setValidationError("Maximum safe network size is "
                    + NetworkGenerator.MAX_SAFE_NODES + " nodes.");
            return false;
            }

        if (val > 300)
            {
            setValidationWarning("Large network (" + val
                    + " nodes). O(N^2) or distance analyses may take several seconds.");
            return true;
            }

        clearValidation();
        return true;
        }

    private void setValidationError(String msg)
        {
        validationLabel.setText("<html><font color='#c00000'><b>Error:</b> " + msg + "</font></html>");
        okButton.setEnabled(false);
        }

    private void setValidationWarning(String msg)
        {
        validationLabel.setText("<html><font color='#c06000'><b>Warning:</b> " + msg + "</font></html>");
        okButton.setEnabled(true);
        }

    private void clearValidation()
        {
        validationLabel.setText(" ");
        okButton.setEnabled(true);
        }

    private boolean handleCreate()
        {
        if (!validateInput())
            {
            return false;
            }

        int n = Integer.parseInt(nodeCountField.getText().trim());

        if (n > 300)
            {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Processing data from " + n + " nodes may result in slower analysis calculations.\n"
                            + "Are you sure you want to proceed?",
                    "Agna Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                {
                return false;
                }
            }

        String type = (String) typeCombo.getSelectedItem();
        try
            {
            if (TYPE_EMPTY.equals(type))
                {
                generatedNetwork = NetworkGenerator.createEmpty(n);
                }
            else if (TYPE_STAR.equals(type))
                {
                generatedNetwork = NetworkGenerator.createStar(n, starSymmetricCheck.isSelected());
                }
            else if (TYPE_RANDOM_CONNECTED.equals(type))
                {
                generatedNetwork = NetworkGenerator.createRandomConnected(n, 0.15, 0);
                }
            else if (TYPE_RANDOM_WEIGHTED.equals(type))
                {
                double dens = (Double) weightedDensitySpinner.getValue();
                generatedNetwork = NetworkGenerator.createRandomWeighted(n, dens, weightedSymmetricCheck.isSelected(), 0);
                }
            else if (TYPE_CIRCULAR.equals(type))
                {
                generatedNetwork = NetworkGenerator.createCircular(n);
                }
            else if (TYPE_FULL.equals(type))
                {
                generatedNetwork = NetworkGenerator.createFullBinary(n);
                }
            else if (TYPE_SQUARE_LATTICE.equals(type))
                {
                int side = (Integer) squareSideSpinner.getValue();
                generatedNetwork = NetworkGenerator.createSquareLattice(side);
                }
            else if (TYPE_HEXAGRAM_LATTICE.equals(type))
                {
                int order = (Integer) hexagramOrderSpinner.getValue();
                generatedNetwork = NetworkGenerator.createHexagramLattice(order);
                }
            return true;
            }
        catch (Exception ex)
            {
            JOptionPane.showMessageDialog(this, "Failed to generate network: " + ex.getMessage(),
                    "Agna Error", JOptionPane.ERROR_MESSAGE);
            return false;
            }
        }

    private static int calculateHexagramNodeCount(int R)
        {
        int count = 0;
        for (int q = -2 * R; q <= 2 * R; q++)
            {
            for (int r = -2 * R; r <= 2 * R; r++)
                {
                int s = -q - r;
                boolean inUp = (q <= R && r <= R && s <= R);
                boolean inDown = (q >= -R && r >= -R && s >= -R);
                if (inUp || inDown)
                    {
                    count++;
                    }
                }
            }
        return count;
        }

    public Network getGeneratedNetwork()
        {
        return generatedNetwork;
        }
    }
