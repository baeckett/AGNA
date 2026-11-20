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

    /**
     * 2.2.2: documented exit codes (AGENTS.md): 1 usage, 2 data/validation,
     * 3 computation. Before 2.2.2 every failure exited with 1.
     */
    static int exitCodeFor(Throwable e)
        {
        if (e instanceof Cli.InputIntegrityException
                || e instanceof java.io.FileNotFoundException
                || e instanceof java.nio.file.NoSuchFileException
                || e instanceof java.nio.charset.CharacterCodingException)
            return 2;
        if (e instanceof ArithmeticException || e instanceof StackOverflowError
                || e instanceof OutOfMemoryError
                || (e instanceof IllegalArgumentException && e.getMessage() != null
                        && e.getMessage().startsWith("Bonacich power")))
            return 3;
        return 1;
        }

    public static void main(String[] args)
        {
        System.setProperty("java.awt.headless", "true");
        int code;
        try
            {
            code = new Cli(System.out).run(args);
            } catch (Exception | StackOverflowError | OutOfMemoryError e)
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
            code = exitCodeFor(e);
            }
        System.exit(code);
        }
    }