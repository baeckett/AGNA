package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the in-app help pages must be generated and consistent - every
 * TOC link resolves to a real page, and the documentation of the new
 * features (formats, merge, session log) is present.
 */
public class HelpContentTest
    {
    private static File helpDir()
        {
        return new File("src/main/resources/help");
        }

    private static String read(File f) throws Exception
        {
        return new String(Files.readAllBytes(f.toPath()),
                StandardCharsets.UTF_8);
        }

    @Test
    public void tocLinksResolveToRealPages() throws Exception
        {
        File dir = helpDir();
        File contents = new File(dir, "help_contents.htm");
        assertTrue(contents.exists(), "help contents page must exist");
        String toc = read(contents);
        Pattern link = Pattern.compile("href=\"([a-z0-9]+\\.htm)\"");
        Matcher m = link.matcher(toc);
        int count = 0;
        while (m.find())
            {
            assertTrue(new File(dir, m.group(1)).exists(),
                    "missing help page " + m.group(1));
            count++;
            }
        assertTrue(count >= 10, "expected a full manual, found " + count
                + " sections");
        }

    @Test
    public void newFeaturesAreDocumented() throws Exception
        {
        String formats = read(new File(helpDir(), "5fileformats.htm"));
        assertTrue(formats.contains("GraphML"));
        assertTrue(formats.contains("GML"));
        assertTrue(formats.contains("GraphSON"));

        String ops = read(new File(helpDir(), "4matrixoperationsformulas.htm"));
        assertTrue(ops.contains("Merge Network"));
        assertTrue(ops.contains("Symmetrize"));
        assertTrue(ops.contains("√"));

        String news = read(new File(helpDir(), "6newin213.htm"));
        assertTrue(news.contains("Session log"));
        }
    }
