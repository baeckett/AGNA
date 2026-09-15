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
        if (url == null)
            return;

        // modern approach first: let the OS open its default browser
        if (Desktop.isDesktopSupported())
            {
            try
                {
                Desktop.getDesktop().browse(new URI(url));
                return;
                } catch (Exception e1)
                {
                // fall through to the legacy helpers
                }
            }

        int platform = getPlatform();
        Process p = null;
        String cmd = null;

        if (platform == WINDOWS_PLATFORM)// windows
            {
            // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
            cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
            try
                {
                p = Runtime.getRuntime().exec(cmd);
                } catch (Exception e1)
                {
                return;
                }
            }

        else if (platform == UNIX_PLATFORM)// unix or linux
            {
            // Under Unix, Netscape has to be running for the "-remote"
            // command to work. So, we try sending the command and
            // check for an exit value. If the exit command is 0,
            // it worked, otherwise we need to start the browser.
            // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
            cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
            try
                {
                p = Runtime.getRuntime().exec(cmd);
                } catch (Exception e23) // java.lang.NullPointerException e20)
                {
                cmd = UNIX_PATH_2 + " " + url;
                try
                    {
                    p = Runtime.getRuntime().exec(cmd);
                    } catch (Exception e45) {
      AgnaLog.warn("suppressed exception", e45);
      }
                }
            if (p == null)
                return;
            try
                {
                // wait for exit code -- if it's 0, command worked,
                // otherwise we need to start the browser up.
                int exitCode = p.waitFor();
                if (exitCode != 0)
                    {
                    // Command failed, start up the browser
                    cmd = UNIX_PATH + " " + url;
                    try
                        {
                        p = Runtime.getRuntime().exec(cmd);
                        } catch (Exception e5)
                        {
                        return;
                        }
                    }
                } catch (InterruptedException e3)
                {
                return;
                }
            }

        else if (platform == MAC_PLATFORM)// mac
            {
            // The MRJ API used here in 2.1.2 no longer exists; Desktop.browse
            // above already covers macOS on any modern JDK, so nothing to do.
            }

        }// end of method

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