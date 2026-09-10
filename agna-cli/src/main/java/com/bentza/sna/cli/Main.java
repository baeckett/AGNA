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
            System.err.println("Agna CLI error: " + e.getMessage());
            code = 1;
            }
        System.exit(code);
        }
    }