package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.bentza.sna.Environment;

/**
 * 2.1.3: settings must resolve to a stable home-directory location (so
 * the packaged app works from inside its bundle) while honouring a file
 * left in the working directory by earlier versions.
 */
public class SettingsPathTest
    {
    @Test
    public void settingsFileHasTheStableName()
        {
        assertEquals("AgnaDefaultSettings.ini",
                Environment.getSettingsFile().getName());
        }

    @Test
    public void settingsFileLivesUnderTheUserHome()
        {
        File settings = Environment.getSettingsFile();
        String path = settings.getAbsolutePath();
        String home = System.getProperty("user.home");
        assertTrue(path.startsWith(home), "settings under user home: "
                + path);
        // when no legacy cwd file exists the home path contains .agna
        if (!new File("AgnaDefaultSettings.ini").exists())
            {
            assertTrue(path.contains(".agna"), "home settings directory: "
                    + path);
            }
        }

    @Test
    public void writeAndReadRoundTripThroughTheSettingsFile()
        throws Exception
        {
        File settings = Environment.getSettingsFile();
        if (settings.getParentFile() != null)
            {
            settings.getParentFile().mkdirs();
            }
        Files.write(settings.toPath(),
                "Look And Feel\tflatlaf\n".getBytes(StandardCharsets.ISO_8859_1));
        String content = new String(Files.readAllBytes(settings.toPath()),
                StandardCharsets.ISO_8859_1);
        assertTrue(content.contains("Look And Feel"));
        Files.delete(settings.toPath());
        }
    }
