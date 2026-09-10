package com.bentza.sna.gui;

import javax.swing.JButton;
// import java.util.HashMap;
import javax.swing.ImageIcon;
import java.awt.Dimension;

/**
 * Generates VerticalHeaderButton's
 */
public class HeaderButtonFactory
    {
    /**
     * HashMap that stores reusable buttons.
     */
    // private static final HashMap buttons_by_icon = new HashMap();
    /**
     * Empty constructor.
     */
    public HeaderButtonFactory()
        {
        }

    public static VerticalHeaderButton requestButton(ImageIcon icon,
            Dimension button_dim)
        {
        if (icon == null)
            icon = new ImageIcon();
        VerticalHeaderButton button = new VerticalHeaderButton(icon, button_dim);
        return button;
        }

    /*
     * public static VerticalHeaderButton requestButtonOLD(ImageIcon icon,
     * Dimension button_dim) { if (icon == null) icon = new ImageIcon();
     * VerticalHeaderButton button =
     * (VerticalHeaderButton)buttons_by_icon.get(icon); if(button == null) {
     * button = new VerticalHeaderButton(icon, button_dim);
     * buttons_by_icon.put(icon, button); System.out.println("Creating button"); }
     * return button; }
     */
    }