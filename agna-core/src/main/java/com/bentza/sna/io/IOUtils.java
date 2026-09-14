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
     * 2.1.3 (Phase A): decode text as UTF-8 first, falling back to a
     * byte-faithful latin-1 decode for legacy single-byte files written
     * by the original app. ASCII content is identical under both; new
     * UTF-8 files (Romanian, Cyrillic, CJK, ...) decode correctly, old
     * files round-trip losslessly.
     */
    public static String utf8BytesToText(byte[] bytes)
        {
        try
            {
            java.nio.CharBuffer chars = java.nio.charset.StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(
                            java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(
                            java.nio.charset.CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(bytes));
            return chars.toString();
            } catch (java.nio.charset.CharacterCodingException e)
            {
            return new String(bytes,
                    java.nio.charset.StandardCharsets.ISO_8859_1);
            }
        }

    public static java.io.Reader reader(java.io.File file)
            throws java.io.FileNotFoundException
        {
        try
            {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            return new java.io.StringReader(utf8BytesToText(bytes));
            } catch (java.io.IOException e)
            {
            throw new java.io.FileNotFoundException(file.toString());
            }
        }

    /**
     * 2.1.3 (Phase A): all text output is UTF-8 now.
     */
    public static java.io.Writer writer(java.io.File file)
            throws java.io.IOException
        {
        return new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file),
                java.nio.charset.StandardCharsets.UTF_8);
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
     * 2.1.3: returns the file name with its extension replaced by ex_tension
     * (appended when the name has no extension). Unlike addExtension, an
     * existing different extension is replaced, not doubled (e.g.
     * "example3.agn" + "net" -> "example3.net").
     */
    public static String setExtension(String file_name, String ex_tension)
        {
        if (ex_tension == null || ex_tension.length() == 0)
            {
            return file_name;
            }
        // 2.1.3: operate on the basename only - a dot inside a directory
        // name (e.g. "Agna_2.1.3/...") must not be mistaken for the file
        // extension separator
        java.io.File as_file = new java.io.File(file_name);
        String name = as_file.getName();
        int dot = name.lastIndexOf('.');
        String new_name;
        if (dot > 0)
            {
            String current = name.substring(dot + 1).toLowerCase();
            if (current.equals(ex_tension.toLowerCase()))
                {
                return file_name;
                }
            new_name = name.substring(0, dot) + "." + ex_tension;
            } else
            {
            new_name = name + "." + ex_tension;
            }
        String parent = as_file.getParent();
        if (parent == null)
            {
            return new_name;
            }
        return parent + java.io.File.separator + new_name;
        }

    /**
     * Returns the extension (as a String) of a file name given as a parameter.
     */
    public static String getExtension(String filename)
        {
        // 2.1.3: basename-aware (a dot inside a directory name is not the
        // extension separator)
        String name = new java.io.File(filename).getName();
        int i = name.lastIndexOf('.');
        if (i > 0 && i < name.length() - 1)
            {
            return name.substring(i + 1).toLowerCase();
            } else if (i == name.length() - 1)
            {
            return "";
            }
        return null;
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