/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: fuzz - every file parser must survive arbitrary malformed input
 * without throwing (it may log warnings and possibly produce an empty or
 * partial network, but never an uncaught exception). Seeded and bounded
 * so the suite stays deterministic and fast.
 */
public class NetworkFileFuzzerTest
    {
    private static final String[] TEXT_EXTENSIONS = { "agn", "txt", "csv",
            "net", "graphml", "gml", "graphson" };

    private static String baseline() throws Exception
        {
        return new String(
                Files.readAllBytes(new File(
                        "samples/example2.agn").toPath()),
                StandardCharsets.ISO_8859_1);
        }

    /** random gibberish: printable ascii, binary-ish bytes, numerics. */
    private static String randomJunk(Random rnd, int len)
        {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++)
            {
            int mode = rnd.nextInt(4);
            if (mode == 0)
                {
                sb.append((char) (32 + rnd.nextInt(95))); // printable
                } else if (mode == 1)
                {
                sb.append((char) (1 + rnd.nextInt(255)));  // raw bytes
                } else if (mode == 2)
                {
                sb.append(rnd.nextInt(1000000));
                } else
                {
                sb.append("[]{}()\"'<>/\\=,;:\t\n*-".charAt(
                        rnd.nextInt(20)));
                }
            }
        return sb.toString();
        }

    /** mutated copy of a real file: flips/truncates/duplicates lines. */
    private static String mutate(Random rnd, String base)
        {
        String[] lines = base.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines)
            {
            int r = rnd.nextInt(10);
            if (r == 0)
                {
                continue; // dropped line
                }
            if (r == 1)
                {
                sb.append(line).append("\n").append(line).append("\n");
                } else if (r == 2)
                {
                sb.append(randomJunk(rnd, rnd.nextInt(24))).append("\n");
                } else if (r == 3)
                {
                sb.append(line.substring(0,
                        Math.max(0, rnd.nextInt(line.length() + 1))))
                        .append("\n");
                } else
                {
                sb.append(line).append("\n");
                }
            }
        if (rnd.nextBoolean())
            {
            sb.append(randomJunk(rnd, rnd.nextInt(120)));
            }
        return sb.toString();
        }

    @Test
    public void textualParsersNeverThrowOnGarbage() throws Exception
        {
        Random rnd = new Random(20260910L);
        String base = baseline();
        for (String ext : TEXT_EXTENSIONS)
            {
            for (int i = 0; i < 150; i++)
                {
                String content = i % 2 == 0
                        ? randomJunk(rnd, rnd.nextInt(400))
                        : mutate(rnd, base);
                try
                    {
                    FullNet full = new FullNet();
                    full.readNetwork(content, ext);
                    } catch (Throwable t)
                    {
                    java.io.StringWriter sw = new java.io.StringWriter();
                    t.printStackTrace(new java.io.PrintWriter(sw));
                    fail("parser " + ext + " threw on input " + i + ": "
                            + t + "\n" + sw);
                    }
                }
            }
        }

    @Test
    public void excelReaderNeverThrowsOnGarbageBytes() throws Exception
        {
        Random rnd = new Random(20260910L);
        for (int i = 0; i < 60; i++)
            {
            byte[] junk = new byte[rnd.nextInt(800)];
            rnd.nextBytes(junk);
            File f = File.createTempFile("agna_fuzz_" + i, ".xls");
            Files.write(f.toPath(), junk);
            try
                {
                FullNet full = new FullNet();
                full.readExcelFile(f, "xls");
                } catch (Throwable t)
                {
                fail("xls reader threw on junk " + i + ": " + t);
                }
            f.delete();
            }
        }

    @Test
    public void xlsxReaderNeverThrowsOnGarbageBytes() throws Exception
        {
        Random rnd = new Random(20260910L);
        for (int i = 0; i < 60; i++)
            {
            byte[] junk = new byte[rnd.nextInt(800)];
            rnd.nextBytes(junk);
            File f = File.createTempFile("agna_fuzzx_" + i, ".xlsx");
            Files.write(f.toPath(), junk);
            try
                {
                FullNet full = new FullNet();
                full.readExcelFile(f, "xlsx");
                } catch (Throwable t)
                {
                fail("xlsx reader threw on junk " + i + ": " + t);
                }
            f.delete();
            }
        }

    @Test
    public void randomNetworksSurviveTheTransformBattery() throws Exception
        {
        Random rnd = new Random(20260910L);
        AgnaLib lib = new AgnaLib();
        for (int i = 0; i < 80; i++)
            {
            int n = 1 + rnd.nextInt(12);
            Network net = new Network(n);
            for (int r = 0; r < n; r++)
                for (int c = 0; c < n; c++)
                    if (r != c && rnd.nextBoolean())
                        {
                        net.setValue(rnd.nextInt(6), r, c);
                        }
            lib.addScalar(net, rnd.nextInt(5));
            lib.multiplyByScalar(net, rnd.nextBoolean() ? 2 : 3);
            lib.transpose(net);
            lib.symmetrizeMaximum(net);
            lib.normalize(net, rnd.nextBoolean()
                    ? AgnaLib.NORMALIZE_BINARY
                    : AgnaLib.NORMALIZE_THRESHOLD, 1f);
            lib.multiplyNetworks(net, net);
            if (n > 1 && rnd.nextBoolean())
                {
                net.isolateActor(rnd.nextInt(n));
                }
            }
        }
    }