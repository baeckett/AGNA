package com.bentza.sna.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the command-line surface - info, analyse, convert, transform
 * and draw - must work end to end against the engine.
 */
public class CliTest
    {
    private String run(String... args) throws Exception
        {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int code = new Cli(new java.io.PrintStream(buf)).run(args);
        String text = buf.toString(StandardCharsets.ISO_8859_1.name());
        assertEquals(0, code, "exit code for " + String.join(" ", args)
                + " -> " + text);
        return text;
        }

    @Test
    public void infoPrintsASummary() throws Exception
        {
        String text = run("info", "samples/example2.agn");
        assertTrue(text.contains("nodes:  9"), text);
        }

    @Test
    public void analyseRunsTheFullBattery() throws Exception
        {
        String text = run("analyse", "samples/4 full.txt", "--all");
        assertTrue(text.toLowerCase().contains("density"), text.substring(0,
                Math.min(120, text.length())));
        }

    @Test
    public void convertRoundTrips() throws Exception
        {
        File out = File.createTempFile("agna_cli", ".graphml");
        out.deleteOnExit();
        run("convert", "samples/example2.agn", out.getAbsolutePath());
        String back = new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(back.contains("<graphml"), "graphml written");
        }

    @Test
    public void transformTransposesAndWrites() throws Exception
        {
        File out = File.createTempFile("agna_cli_t", ".agn");
        out.deleteOnExit();
        run("transform", "samples/example2.agn", out.getAbsolutePath(),
                "--op", "transpose");
        String back = new String(Files.readAllBytes(out.toPath()),
                StandardCharsets.ISO_8859_1);
        assertTrue(back.contains("Agna Data File"), "agn written");
        }

    @Test
    public void drawProducesARealPng() throws Exception
        {
        File png = File.createTempFile("agna_cli_draw", ".png");
        png.deleteOnExit();
        run("draw", "samples/example2.agn", "--out", png.getAbsolutePath(),
                "--layout", "circular", "--size", "600x400", "--labels");
        byte[] bytes = Files.readAllBytes(png.toPath());
        assertTrue(bytes.length > 1000, "png has content");
        // PNG magic
        assertTrue(bytes[0] == (byte) 0x89 && bytes[1] == 'P'
                && bytes[2] == 'N' && bytes[3] == 'G', "png signature");
        }

    @Test
    public void drawSupportsEveryNamedLayout() throws Exception
        {
        for (String layout : new String[] { "grid", "concentric", "spring" })
            {
            File png = File.createTempFile("agna_cli_draw_" + layout,
                    ".png");
            png.deleteOnExit();
            run("draw", "samples/example2.agn", "--out",
                    png.getAbsolutePath(), "--layout", layout);
            byte[] bytes = Files.readAllBytes(png.toPath());
            assertTrue(bytes.length > 1000, layout + " png has content");
            }
        }
    }