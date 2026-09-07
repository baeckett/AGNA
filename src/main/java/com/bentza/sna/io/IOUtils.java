package com.bentza.sna.io;

import com.bentza.sna.Environment;

/**
 * A class that handles information about system and paths.
 */
public class IOUtils
    {
    /**
     * Constructor
     */
    public IOUtils()
        {
        }

    /**
     * 2.1.3: reader over a legacy Agna data file with an explicit charset.
     * Old .agn/.txt/.ini files were written with Windows/ISO-8859-1 style
     * encodings; reading them with the platform default (UTF-8 on modern
     * systems) could garble diacritics. ISO-8859-1 maps every byte 1:1, so
     * legacy files round-trip losslessly.
     */
    public static java.io.Reader reader(java.io.File file)
            throws java.io.FileNotFoundException
        {
        return new java.io.InputStreamReader(new java.io.FileInputStream(file),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        }

    /**
     * 2.1.3: writer with an explicit charset (ISO-8859-1, see reader()).
     */
    public static java.io.Writer writer(java.io.File file)
            throws java.io.IOException
        {
        return new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        }

    /**
     * 2.1.3: UTF-8 writer (for XML-based formats such as SVG).
     */
    public static java.io.Writer writerUtf8(java.io.File file)
            throws java.io.IOException
        {
        return new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file),
                java.nio.charset.StandardCharsets.UTF_8);
        }

    /**
     * Takes a file name and an extension as parameters and returns a new string
     * representing the file name with the new extension.
     */
    public static String addExtension(String file_name, String ex_tension)
        {
        String curr_ext = getExtension(file_name);
        String result;
        if (curr_ext == null) // no dot in filename
            result = file_name + "." + ex_tension;
        else if (curr_ext.equals("")) // dot exists and nothing after it
            result = file_name + ex_tension;
        else if (curr_ext.equals(ex_tension)) // dot + good extension already
                                                // exists
            result = new String(file_name);
        else if (curr_ext.equals("htm") && ex_tension.equals("html")) // html
                                                                        // already
                                                                        // existing
            result = new String(file_name);
        else if (curr_ext.equals("html") && ex_tension.equals("htm")) // html
                                                                        // already
                                                                        // existing
            result = new String(file_name);
        else
            // different extension already extsts
            {
            result = file_name + "." + ex_tension;
            }
        return result;
        }

    /**
     * Returns the extension (as a String) of a file name given as a parameter.
     */
    public static String getExtension(String filename)
        {
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1)
            {
            return filename.substring(i + 1).toLowerCase();
            } else if (i == filename.length() - 1)
            {
            return "";
            } else if (i == -1)
            {
            return null;
            }
        return "";
        }

    /**
     * Removes the extension from a file name or a path String. Leaves path
     * unmodified.
     */
    public static String getPathWithoutExtension(String filename)
        {
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1)
            {
            return filename.substring(0, i).toLowerCase();
            }
        return null;
        }

    /**
     * Removes the extension and the path from a given string and leaves only
     * the file name.
     */
    public static String getNameWithoutExtension(String filename)
        {
        int i = filename.lastIndexOf('.');
        int j = filename.lastIndexOf(System.getProperty("file.separator"));
        if (i > 0 && i < filename.length() - 1)
            {
            if (j > 0)
                {
                if (j < i)
                    return filename.substring(j + 1, i);
                } else
                {
                return filename.substring(0, i);
                }
            }
        return null;
        }

    /**
     * Returns the filename as a String from given path. Leaves extension
     * unmodified.
     */
    public static String getNameWithoutPath(String filename)
        {
        int i = filename.lastIndexOf(Environment.fs);
        if (i > 0 && i < filename.length() - 1)
            {
            return filename.substring(i + 1, filename.length());
            } else if (i >= filename.length() - 1)
            {
            return filename;
            } else if (i == -1)
            {
            return filename;
            }
        return null;
        }

    // End of class
    }