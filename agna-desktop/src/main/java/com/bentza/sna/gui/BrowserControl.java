/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.AgnaLog;
import java.awt.Desktop;
import java.net.URI;

/**
 * A simple, static class to display a URL in the system browser. Under Unix,
 * the system browser is hard-coded to be 'netscape'. Netscape must be in your
 * PATH for this to work. This has been tested with the following platforms:
 * AIX, HP-UX and Solaris. D:\Marius\Utca\Java\Agna\Agna_src Under Windows, this
 * will bring up the default browser under windows, usually either Netscape or
 * Microsoft IE. The default browser is determined by the OS. This has been
 * tested under Windows 95/98/NT. Examples:
 * BrowserControl.displayURL("http://www.javaworld.com")
 * BrowserControl.displayURL("file://c:\\docs\\index.html")
 * BrowserContorl.displayURL("file:///user/joe/index.html"); Note - you must
 * include the url type -- either "http://" or "file://".
 */
public class BrowserControl
    {
    /**
     * Display a file in the system browser. If you want to display a file, you
     * must include the absolute path name.
     * 
     * @param url
     *            the file's url (the url must start with either "http://" or
     *            "file://").
     */

    public static final int UNACCEPTED_PLATFORM = -1;

    public static final int WINDOWS_PLATFORM = 0;

    public static final int UNIX_PLATFORM = 1; // or linux

    public static final int MAC_PLATFORM = 2;

    // Used to identify the windows platform.
    private static final String WIN_ID = "Windows";

    // The default system browser under windows.
    private static final String WIN_PATH = "rundll32";

    // The flag to display a url.
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

    // The default browser under unix.
    private static final String UNIX_PATH = "netscape";

    private static final String UNIX_PATH_2 = "mozilla";

    // The flag to display a url.
    private static final String UNIX_FLAG = "-remote openURL";

    public static void displayURL(String url)
        {
        if (url == null || url.trim().isEmpty())
            return;

        // validate protocol: only allow http, https, and file schemes
        String trimmed = url.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")
                && !trimmed.startsWith("file://"))
            {
            AgnaLog.warn("BrowserControl: rejected URL with unsupported protocol: " + url);
            return;
            }

        // modern approach: let the OS open its default browser via java.awt.Desktop
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            {
            try
                {
                Desktop.getDesktop().browse(new URI(trimmed));
                return;
                } catch (Exception e1)
                {
                AgnaLog.warn("BrowserControl: Desktop.browse failed, falling back to process launcher: " + e1.getMessage());
                }
            }

        int platform = getPlatform();
        try
            {
            if (platform == WINDOWS_PLATFORM)
                {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", trimmed).start();
                }
            else if (platform == UNIX_PLATFORM)
                {
                new ProcessBuilder("xdg-open", trimmed).start();
                }
            } catch (Exception e)
            {
            AgnaLog.warn("BrowserControl: failed to launch browser for " + url + ": " + e.getMessage());
            }
        }

    public static boolean isAcceptedPlatform()
        {
        if (getPlatform() == UNACCEPTED_PLATFORM)
            return false;
        else
            return true;
        }

    // returns an index as platform
    public static int getPlatform()
        {
        String os = (System.getProperty("os.name")).toLowerCase();
        if (os == null)
            return UNACCEPTED_PLATFORM;
        if (os.indexOf("windows") >= 0)
            return WINDOWS_PLATFORM; // windows
        if (os.indexOf("unix") >= 0 || os.indexOf("linux") >= 0)
            return UNIX_PLATFORM; // unix or linux
        if (System.getProperty("mrj.version") != null && os.indexOf("mac") >= 0)
            return MAC_PLATFORM; // mac

        return UNACCEPTED_PLATFORM; // other
        }

    }