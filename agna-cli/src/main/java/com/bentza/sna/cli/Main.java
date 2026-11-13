/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.cli;

/**
 * Entry point for the Agna command-line interface.
 */
public final class Main
    {
    private Main()
        {
        }

    public static void main(String[] args)
        {
        int code;
        try
            {
            code = new Cli(System.out).run(args);
            } catch (Exception e)
            {
            boolean wantsJson = false;
            if (args != null)
                {
                for (String arg : args)
                    {
                    if ("--json".equals(arg) || "json".equals(arg))
                        {
                        wantsJson = true;
                        break;
                        }
                    }
                }
            if (wantsJson)
                {
                System.err.println("{\"error\": \""
                        + (e.getMessage() != null ? e.getMessage().replace("\"", "\\\"").replace("\n", " ") : "Unknown error")
                        + "\", \"hint\": \"Run 'agna --help' or 'agna validate <file>' to check your inputs.\"}");
                } else
                {
                System.err.println("Agna CLI error: " + e.getMessage());
                System.err.println("Hint: Run 'agna --help' for usage, or 'agna validate <file>' to inspect the network.");
                }
            code = 1;
            }
        System.exit(code);
        }
    }