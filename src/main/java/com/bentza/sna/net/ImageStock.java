package com.bentza.sna.net;

import com.bentza.sna.Environment;
import java.io.File;
import java.util.Vector;
import javax.swing.ImageIcon;

/**
 * a stock of images to be used by Actor and NodeArea built to avoid creating
 * different image objects with identical sources
 */
public class ImageStock
    {
    private int size;

    private Vector stock;

    private final ImageItem empty = new ImageItem(); // empty ImageItem

    public ImageStock()
        {
        stock = new Vector(1, 1);
        stock.removeAllElements();
        stock.addElement(empty); // one empty ImageIcon; index 0
        }

    /**
     * deletes the specified ImageItem, but does not remove its reference in the
     * stock Vector; called by ImageItem.suicide() via
     * MainFrame.getCurrentImageStock()
     */
    public void deleteImageItem(ImageItem tmp_image_item)
        {
        // empty ImageItem cannot be deleted;
        if (tmp_image_item == empty)
            return;

        int index = getImageItemIndex(tmp_image_item.image_source);
        if (index == -1)
            return;

        tmp_image_item.image_icon = null;
        tmp_image_item.small_image_icon = null;
        tmp_image_item.image_source = null;
        tmp_image_item = null;

        stock.remove(index);
        }

    public ImageItem getEmptyImageItem()
        {
        return empty;
        }

    public ImageItem requestEmptyImageItem()
        {
        empty.increaseClients();
        return empty;
        }

/**
     * Returns an array of Strings containing the image paths of all ImageItems
     * available in stock; returns null if stock contains no element;
     */
    public String[] getAllImageSources()
        {
        if (stock == null || stock.size() == 0)
            return null;

        int stock_size = this.getSize();
        String[] paths = new String[stock_size];
        String tmp_path = null;
        for (int i = 0; i < stock_size; i++)
            {
            tmp_path = ((ImageItem) stock.elementAt(i)).getImageSource();
            if (tmp_path != null && !tmp_path.equals("-")
                    && !tmp_path.equals(""))
                paths[i] = tmp_path;
            }
        return paths;
        }

    /**
     * probably used for testing purposes only
     */
    public int getSize()
        {
        return stock.size();
        }

    /**
     * returns an integer if there is already an ImageIcon with tmp_image_source
     * in the stock Vector; returns -1 otherwise; may alter the string as
     * canonical path!
     */
    private int getImageItemIndex(String tmp_image_source)
        {
        ImageItem tmp_image_item = null;

        int stock_size = stock.size();
        if (stock_size == 0)
            return -1;
        if (tmp_image_source.equals("-"))
            return 0; // empty ImageItem
        for (int i = 1; i < stock_size; i++) // i = 1 not i = 0 because first
                                                // element is always the empty
                                                // ImageItem
            {
            tmp_image_item = (ImageItem) stock.elementAt(i);
            while (tmp_image_item == null)
                { // cleaning unused vectors:
                if (i >= stock.size())
                    {
                    return -1;// no string found
                    }
                stock.remove(i);
                tmp_image_item = (ImageItem) stock.elementAt(i);
                }
            if (tmp_image_item.image_source.equals(tmp_image_source))
                return i;
            }

        // no string found:
        return -1;
        }

/**
     * returns the ImageItem corresponding to a source; creates ImageItems only
     * if needed;
     */
    public ImageItem requestImageItem(String tmp_image_source)
        {
        if (tmp_image_source == null || tmp_image_source.equals(""))
            {
            empty.increaseClients();
            return empty;
            }

        String resolved = resolveImagePath(tmp_image_source);
        if (resolved == null)
            {
            // no candidate path could be resolved to a real file
            empty.increaseClients();
            return empty;
            }
        tmp_image_source = resolved;

        int tmp_index = getImageItemIndex(tmp_image_source);
        if (tmp_index >= 0)
            {
            ImageItem existing_item = (ImageItem) stock.elementAt(tmp_index);
            existing_item.increaseClients();
            return existing_item;
            } else
            try
                {
                // tmp_index = -1
                // trying to build a new ImageIcon
                ImageIcon tmp_image_icon = new ImageIcon(tmp_image_source);
                ImageItem new_item = new ImageItem(tmp_image_icon,
                        tmp_image_source);
                new_item.increaseClients();
                stock.addElement(new_item);
                return new_item;
                } catch (Exception e1)
                {
                empty.increaseClients();
                return empty;
                }
        }

    /**
     * Resolves a face-file reference stored in a document to a real file,
     * applying three kinds of backward compatibility (2.1.3):
     * <ul>
     * <li>legacy separator characters (\ or /) are normalized to the platform
     * separator, so Windows-stored paths work on other systems and vice
     * versa;</li>
     * <li>legacy relative references (e.g. ".\Faces\..." written by older Agna
     * versions) are tried against the working directory and against the
     * bundled faces assets;</li>
     * <li>the legacy "Shaddow" spelling is corrected to "Shadow" (files
     * created with older Agna versions, whose Faces folder shipped with that
     * misspelling).</li>
     * </ul>
     * Returns the canonical path of an existing file, or null when no
     * candidate resolves.
     */
    private String resolveImagePath(String source)
        {
        if (source == null || source.length() == 0)
            return null;

        String normalized = source.replace('\\', File.separatorChar);
        normalized = normalized.replace('/', File.separatorChar);

        Vector candidates = new Vector();
        candidates.addElement(normalized);
        File direct = new File(normalized);
        if (!direct.isAbsolute())
            {
            // relative reference: try the working directory (historical
            // behavior) and the bundled faces assets
            candidates.addElement(System.getProperty("user.dir")
                    + File.separator + normalized);
            String faces_dir = Environment.getFacesDirectory();
            int faces_begin = normalized.toLowerCase().indexOf(
                    "faces" + File.separator);
            if (faces_begin >= 0)
                {
                // e.g. ".\Faces\..." or "faces/..." -> inside the faces dir
                candidates.addElement(faces_dir
                        + normalized.substring(faces_begin + 5));
                }
            }

        int candidates_size = candidates.size();
        for (int i = 0; i < candidates_size; i++)
            {
            String candidate = (String) candidates.elementAt(i);
            String resolved = canonicalIfExists(candidate);
            if (resolved != null)
                return resolved;
            String shadow_fixed = candidate.replace("Shaddow", "Shadow");
            if (!shadow_fixed.equals(candidate))
                {
                resolved = canonicalIfExists(shadow_fixed);
                if (resolved != null)
                    return resolved;
                }
            }
        return null;
        }

    private String canonicalIfExists(String path)
        {
        try
            {
            File file = new File(path);
            if (file.exists())
                {
                return file.getCanonicalPath();
                }
            } catch (Exception e)
            {
            }
        return null;
        }
    }