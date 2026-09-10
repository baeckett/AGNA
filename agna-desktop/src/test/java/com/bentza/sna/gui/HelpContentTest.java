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
 * TOC link resolves to a real page, the menu reference links to the
 * methodology/operations pages, and the new features are documented.
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
    public void menuReferenceLinksResolveAndReachTheMath() throws Exception
        {
        File dir = helpDir();
        String menu = read(new File(dir, "3menureference.htm"));
        Pattern link = Pattern.compile("href=\"([a-z0-9]+\\.htm(?:#[a-z0-9]+)?)\"");
        Matcher m = link.matcher(menu);
        int links = 0;
        int to_math = 0;
        while (m.find())
            {
            String href = m.group(1);
            links++;
            String page = href.split("#")[0];
            assertTrue(new File(dir, page).exists(),
                    "menu link target missing: " + href);
            if (href.startsWith("4methodology") || href.startsWith("5matrix"))
                {
                to_math++;
                }
            String frag = href.contains("#")
                    ? href.substring(href.indexOf("#") + 1) : null;
            if (frag != null)
                {
                String pageText = read(new File(dir, page));
                assertTrue(
                        pageText.contains("name=\"" + frag + "\"")
                                || pageText.contains("id=\"" + frag + "\""),
                        "anchor " + frag + " missing in " + page);
                }
            }
        assertTrue(links >= 15,
                "the menu reference should be link-rich, found " + links);
        assertTrue(to_math >= 15,
                "most menu items should explain their math, found " + to_math);
        }

    @Test
    public void newFeaturesAreDocumented() throws Exception
        {
        String formats = read(new File(helpDir(), "6fileformats.htm"));
        assertTrue(formats.contains("GraphML"));
        assertTrue(formats.contains("GML"));
        assertTrue(formats.contains("GraphSON"));

        String ops = read(new File(helpDir(),
                "5matrixoperationsformulas.htm"));
        assertTrue(ops.contains("Merge Network"));
        assertTrue(ops.contains("Symmetrize"));
        assertTrue(ops.contains("√"));

        String news = read(new File(helpDir(), "7newin213.htm"));
        assertTrue(news.contains("Session log"));
        }
    }