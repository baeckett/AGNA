/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Minimal application logging facade (2.1.3).
 * 
 * <p>
 * Routes warnings and errors through java.util.logging with a compact format.
 * Previously, most failures were swallowed by empty catch blocks
 * ({@code catch (Exception e) { AgnaLog.warn("suppressed exception", e); }}) or dumped via {@code System.err}; both
 * behaviours are replaced by this class so that problems become visible in
 * the console and can be redirected to a file later without touching every
 * call site.
 */
public final class AgnaLog
    {
    private static final Logger LOGGER = Logger.getLogger("com.bentza");

    static
        {
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new Formatter()
            {
                public String format(LogRecord record)
                    {
                    String message = record.getMessage();
                    if (record.getThrown() != null)
                        {
                        message += " (" + record.getThrown() + ")";
                        }
                    return "Agna: " + record.getLevel().getName().toLowerCase()
                            + ": " + message + System.lineSeparator();
                    }
            });
        handler.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);
        }

    private AgnaLog()
        {
        }

    public static void warn(String message)
        {
        LOGGER.warning(message);
        }

    public static void warn(String message, Throwable thrown)
        {
        if (thrown == null)
            {
            LOGGER.warning(message);
            } else
            {
            LOGGER.log(Level.WARNING, message, thrown);
            }
        }

    public static void error(String message, Throwable thrown)
        {
        LOGGER.log(Level.SEVERE, message, thrown);
        }

    public static void info(String message)
        {
        LOGGER.info(message);
        }
    }