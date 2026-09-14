package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.io.GMLExporter;
import com.bentza.sna.io.GraphMLExporter;
import com.bentza.sna.io.IOUtils;
import com.bentza.sna.io.PajekExporter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 2.1.3 Phase A: all text I/O is UTF-8. Diacritics (Romanian and CJK)
 * must survive every text format round-trip, the IOUtils channels must
 * be UTF-8, and legacy latin-1 files must still decode byte-faithfully
 * through the fallback.
 */
public class EncodingRoundTripTest
    {
    @TempDir
    Path tmp;

    private FullNet unicodeNetwork()
        {
        FullNet full = new FullNet();
        Network net = new Network(5);
        net.setName("Rețea de rețele 网络");
        String[] names = { "Bența", "București", "夏季", "Măriuca", "Ștefan" };
        for (int i = 0; i < 5; i++)
            {
            net.getActor(i).setName(names[i]);
            net.setValue(1f, i, (i + 1) % 5);
            net.setValue(1f, (i + 1) % 5, i);
            }
        full.setNetwork(net);
        return full;
        }

    private void assertNamesSurvive(FullNet back, boolean checkName)
        {
        if (checkName)
            {
            assertEquals("Rețea de rețele 网络", back.getNetwork().getName(),
                    "network name");
            }
        String[] names = { "Bența", "București", "夏季", "Măriuca", "Ștefan" };
        for (int i = 0; i < 5; i++)
            {
            assertEquals(names[i], back.getNetwork().getActor(i).getName(),
                    "actor " + i);
            }
        }

    @Test
    public void unicodeNamesSurviveThePlainTextFormats() throws Exception
        {
        FullNet full = unicodeNetwork();
        // note: the tab-separated "txt" export is matrix-only by original
        // design (no node names); names survive via the other formats
        for (String ext : new String[] { "agn", "csv", "net", "graphml",
                "gml" })
            {
            String text;
            if ("agn".equals(ext))
                {
                text = full.getAgna2TextNetwork();
                } else if ("txt".equals(ext))
                {
                text = full.getPlainTextNetwork(true);
                } else if ("csv".equals(ext))
                {
                text = full.getPlainTextNetwork(false);
                } else if ("net".equals(ext))
                {
                text = new PajekExporter().getPajekNetwork(full);
                } else if ("graphml".equals(ext))
                {
                text = new GraphMLExporter().getGraphML(full);
                } else
                {
                text = new GMLExporter().getGML(full);
                }
            FullNet back = new FullNet();
            try
                {
                back.readNetwork(text, ext);
                } catch (Exception e)
                {
                throw new AssertionError(ext + " read failed", e);
                }
            assertEquals(5, back.getNetwork().getSize(),
                    "size after reading " + ext + " back; head: "
                            + text.substring(0, Math.min(240,
                                    text.length())));
            // txt/csv/net do not carry the network title, only the names
            assertNamesSurvive(back, "agn".equals(ext)
                    || "graphml".equals(ext));
            }
        }

    @Test
    public void ioUtilsRoundTripsUtf8Text() throws Exception
        {
        File f = tmp.resolve("settings.ini").toFile();
        String content = "București Țărână 夏季 网络 analysis";
        try (java.io.Writer w = IOUtils.writer(f))
            {
            w.write(content);
            }
        StringBuilder read = new StringBuilder();
        try (java.io.Reader r = IOUtils.reader(f))
            {
            int c;
            while ((c = r.read()) != -1)
                {
                read.append((char) c);
                }
            }
        assertEquals(content, read.toString());
        }

    @Test
    public void legacyLatin1BytesFallBackFaithfully()
        {
        // 0xE8 is 'è' in latin-1 and invalid as a lone UTF-8 byte
        byte[] legacy = { 'B', 'u', (byte) 0xE8, ',' };
        assertEquals("Buè,", IOUtils.utf8BytesToText(legacy));
        // valid UTF-8 decodes normally (no fallback)
        byte[] utf8 = "București".getBytes(StandardCharsets.UTF_8);
        assertEquals("București", IOUtils.utf8BytesToText(utf8));
        }

    @Test
    public void gmlExporterAlsoCarriesUnicode() throws Exception
        {
        String text = new GMLExporter().getGML(unicodeNetwork());
        assertTrue(text.contains("București"), "gml keeps diacritics");
        }
    }