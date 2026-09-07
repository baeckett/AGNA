package com.bentza.sna;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import javax.swing.ImageIcon;

/**
 * Holds application-wide constants and resolves bundled resources (toolbar
 * images, node faces, help pages).
 * 
 * <p>
 * In Agna 2.1.3 all shipped media lives on the classpath under
 * {@code /buttons}, {@code /faces} and {@code /help}. Toolbar images and help
 * pages are read straight from the classpath; node faces are <em>extracted</em>
 * to a per-user asset directory on first run, because face references are
 * stored inside .agn files and downstream code expects real file paths.
 */
public class Environment
    {
    private final static String application_name = "Agna";

    private final static String application_version = "2.1.3";

    /**
     * Historic homepage (defunct). Kept for compatibility with older records.
     */
    private final static String application_url = "http://www.geocities.com/imbenta/agna";

    private final static String application_email = "imbenta@yahoo.co.uk";

    /**
     * Historic copyright line. The licence of the 2.1.3 open-source release is
     * still being decided; see README.
     */
    private final static String application_copyright = "Copyright (C) 2002-2005: Marius Benta";

    public static final String fs = System.getProperty("file.separator");

    private static final String USER_ASSETS_ROOT = System
            .getProperty("user.home") + fs + ".agna";

    private static boolean assets_extracted = false;

    /**
     * Constructor. Ensures bundled assets are available on disk.
     */
    public Environment()
        {
        ensureAssetsExtracted();
        }

    public static String getApplicationCopyright()
        {
        return application_copyright;
        }

    public static String getApplicationEmail()
        {
        return application_email;
        }

    public static String getApplicationFullName()
        {
        return application_name + " " + application_version;
        }

    public static String getApplicationName()
        {
        return application_name;
        }

    public static String getApplicationVersion()
        {
        return application_version;
        }

    public static String getApplicationUrl()
        {
        return application_url;
        }

    /**
     * Loads a toolbar/button image from the classpath ({@code /buttons/}).
     * 
     * @param button_name
     *            image file name, e.g. "OpenNetwork.gif"
     * @return the icon, or null (with a message on stderr) if missing
     */
    public static ImageIcon getButtonImageIcon(String button_name)
        {
        URL url = Agna.class.getResource("/buttons/" + button_name);
        if (url == null)
            {
            AgnaLog.warn("Agna: image not found on classpath: /buttons/"
                    + button_name);
            return null;
            }
        return new ImageIcon(url);
        }

    /**
     * Directory where the bundled node-face images are materialized as real
     * files (defaults under {@code ~/.agna/faces}). Faces are stored by file
     * path inside .agn documents, so they cannot live only on the classpath.
     */
    public static String getFacesDirectory()
        {
        ensureAssetsExtracted();
        return USER_ASSETS_ROOT + fs + "faces";
        }

    /**
     * Extracts the bundled faces from the classpath into the user asset
     * directory exactly once. Safe both for an exploded class directory and for
     * a jar (java.nio.file FileSystems handles both).
     */
    public static synchronized void ensureAssetsExtracted()
        {
        if (assets_extracted)
            return;

        Path target_root = Paths.get(USER_ASSETS_ROOT + fs + "faces");
        URL faces_url = Environment.class.getResource("/faces");
        if (faces_url == null)
            {
            System.err
                    .println("Agna: bundled faces not found on classpath (/faces).");
            assets_extracted = true;
            return;
            }

        try
            {
            if ("jar".equals(faces_url.getProtocol()))
                {
                // running from a jar: walk the zip filesystem
                URI jar_uri = URI.create(faces_url.toString().substring(0,
                        faces_url.toString().indexOf("!/")));
                try (FileSystem jar_fs = FileSystems.newFileSystem(jar_uri,
                        (java.util.Map) java.util.Collections.emptyMap()))
                    {
                    copyTree(Paths.get(faces_url.toURI()), target_root);
                    }
                }
            else
                {
                // running from an exploded class directory
                copyTree(Paths.get(faces_url.toURI()), target_root);
                }
            removeLegacyMisspelledFaces(target_root);
            }
        catch (Exception e)
            {
            AgnaLog.warn("Agna: could not extract bundled faces to "
                    + target_root + ": " + e.getMessage());
            }
        finally
            {
            // extract at most once per JVM run, success or failure
            assets_extracted = true;
            }
        }

    private static void copyTree(Path source_root, Path target_root)
            throws IOException
        {
        if (!Files.exists(target_root))
            Files.createDirectories(target_root);
        try (Stream<Path> stream = Files.walk(source_root))
            {
            for (Path source : (Iterable<Path>) stream::iterator)
                {
                Path relative = source_root.relativize(source);
                if (relative.toString().length() == 0)
                    continue; // the root itself
                Path target = target_root.resolve(relative.toString());
                if (Files.isDirectory(source))
                    {
                    Files.createDirectories(target);
                    }
                else
                    {
                    Path target_parent = target.getParent();
                    if (target_parent != null && !Files.exists(target_parent))
                        Files.createDirectories(target_parent);
                    if (!Files.exists(target))
                        Files.copy(source, target,
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

    /**
     * Removes leftover files from older extractions whose names carry the
     * legacy "Shaddow" misspelling. The bundled assets no longer ship that
     * spelling, so any such file inside the managed faces directory is stale.
     */
    private static void removeLegacyMisspelledFaces(Path target_root)
        {
        if (!Files.exists(target_root))
            return;
        try (Stream<Path> stream = Files.walk(target_root))
            {
            for (Path path : (Iterable<Path>) stream::iterator)
                {
                if (Files.isRegularFile(path) && path.getFileName() != null
                        && path.getFileName().toString().indexOf("Shaddow") >= 0)
                    {
                    Files.deleteIfExists(path);
                    }
                }
            } catch (IOException e)
            {
            AgnaLog.warn("Agna: could not clean legacy face files in "
                    + target_root + ": " + e.getMessage());
            }
        }

    /**
     * Returns the current working directory plus a trailing file separator.
     * Used only for user-file defaults; bundled data never resolves here.
     */
    public static String getCurrentDirectory()
        {
        return System.getProperty("user.dir") + fs;
        }

    // End of class
    }