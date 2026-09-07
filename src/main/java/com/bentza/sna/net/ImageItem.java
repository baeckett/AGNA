package com.bentza.sna.net;

import com.bentza.sna.gui.MainFrame;
import javax.swing.ImageIcon;
import java.awt.Image;

/**
 * a class to be used as member of ImageStock
 */
class ImageItem
    {
    public ImageIcon image_icon;

    public ImageIcon small_image_icon;

    public String image_source;

    private int clients; // number of clients

    // creates an empty ImageItem
    public ImageItem()
        {
        image_icon = new ImageIcon("");
        small_image_icon = new ImageIcon("");
        image_source = "-";
        clients = 0;
        }

    /**
     * validity of ImageIcon is not checked here; must be checked in
     * ImageStock.requestImageItem()!
     */
    public ImageItem(ImageIcon tmp_icon, String tmp_source)
        {
        clients = 0;
        image_source = tmp_source;
        image_icon = tmp_icon;
        // creating scaled instance to be used in VerticalHeaderButtons
        small_image_icon = new ImageIcon(tmp_icon.getImage().getScaledInstance(
                8, -1, Image.SCALE_FAST));
        }

    /**
     * returns the number of clients
     */
    public int getClients()
        {
        return clients;
        }

    /**
     * returns the image file path
     */
    public String getImageSource()
        {
        return image_source;
        }

    public void increaseClients()
        {
        clients++;
        }

    public void decreaseClients()
        {
        if (clients >= 2)
            clients--;
        else if (clients == 1)
            suicide();
        }

    private void suicide()
        {
        // 2.1.3: null-safe (the main frame's stock does not exist while
        // parsing headless; the item's own stock then owns the cleanup)
        ImageStock stock = MainFrame.getCurrentImageStock();
        if (stock != null)
            {
            stock.deleteImageItem(this);
            }
        }

    }