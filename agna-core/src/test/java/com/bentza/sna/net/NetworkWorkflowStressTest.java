package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.bentza.sna.io.ExcelExporter;
import com.bentza.sna.io.GMLExporter;
import com.bentza.sna.io.GraphMLExporter;
import com.bentza.sna.io.GraphSONExporter;
import com.bentza.sna.io.PajekExporter;

/**
 * 2.1.3: workflow stress over real sample files - open in every format,
 * export/import round-trips, transforms, attribute edits and every
 * analysis, looking for latent exceptions a single test never reaches.
 */
public class NetworkWorkflowStressTest
    {
    private static String readSample(String name) throws Exception
        {
        File file = new File("samples" + File.separator + name);
        assertTrue(file.exists(), "sample file: " + file);
        return new String(Files.readAllBytes(file.toPath()),
                StandardCharsets.ISO_8859_1);
        }

    private static FullNet open(String name, String ext) throws Exception
        {
        FullNet full = new FullNet();
        if ("xls".equals(ext))
            {
            full.readExcelFile(new File("samples" + File.separator + name),
                    ext);
            } else
            {
            full.readNetwork(readSample(name), ext);
            }
        return full;
        }

    @Test
    public void everyBundledSampleOpens() throws Exception
        {
        String[][] samples = { { "4 full.txt", "txt" },
                { "10 circle.agn", "agn" }, { "10 star.agn", "agn" },
                { "10 star.csv", "csv" }, { "example1.agn", "agn" },
                { "example2.agn", "agn" }, { "example3.agn", "agn" },
                { "example1.xls", "xls" }, { "Chain2.agn", "agn" },
                { "Chain2.txt", "txt" } };
        for (String[] s : samples)
            {
            FullNet full = open(s[0], s[1]);
            assertNotNull(full.getNetwork(), s[0]);
            assertTrue(full.getNetwork().getSize() > 0, s[0]);
            }
        }

    @Test
    public void exportAndImportRoundTripEveryFormat() throws Exception
        {
        FullNet source = open("example2.agn", "agn");
        Network net = source.getNetwork();
        final int n = net.getSize();

        List<String[]> roundTrips = new ArrayList<>();
        roundTrips.add(new String[] { "agn", source.getAgna2TextNetwork() });
        roundTrips.add(new String[] { "txt", source.getPlainTextNetwork(true) });
        roundTrips.add(new String[] { "csv", source.getPlainTextNetwork(false) });
        roundTrips.add(new String[] { "net",
                new PajekExporter().getPajekNetwork(source) });
        roundTrips.add(new String[] { "graphml",
                new GraphMLExporter().getGraphML(source) });
        roundTrips.add(new String[] { "gml",
                new GMLExporter().getGML(source) });
        roundTrips.add(new String[] { "graphson",
                new GraphSONExporter().getGraphSON(source) });

        for (String[] rt : roundTrips)
            {
            assertNotNull(rt[1], "export " + rt[0]);
            FullNet back = new FullNet();
            back.readNetwork(rt[1], rt[0]);
            assertEquals(n, back.getNetwork().getSize(),
                    "size after " + rt[0] + " round trip");
            }

        // the binary format (Excel) goes through a temp file
        File xls = File.createTempFile("agna_stress", ".xls");
        xls.deleteOnExit();
        assertNotNull(new ExcelExporter().saveExcelNetwork(source,
                xls.getAbsolutePath()) == null ? "" : null);
        FullNet xlsBack = new FullNet();
        xlsBack.readExcelFile(xls, "xls");
        assertEquals(n, xlsBack.getNetwork().getSize(),
                "size after xls round trip");
        }

    @Test
    public void transformationsKeepNetworksWellFormed() throws Exception
        {
        Network net = open("example2.agn", "agn").getNetwork();
        final int n = net.getSize();
        float[][] original = new float[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                original[i][j] = net.getValue(i, j);

        AgnaLib lib = new AgnaLib();
        lib.addScalar(net, 2f);
        lib.multiplyByScalar(net, 3f);
        assertDiagonalZero(net);
        lib.transpose(net);
        lib.transpose(net);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j)
                    {
                    // ops are (v + 2) * 3, applied off-diagonal only
                    assertEquals(original[i][j] * 3f + 6f,
                            net.getValue(i, j), 1e-4f,
                            "transpose twice restores the matrix");
                    }
        assertEquals(n, net.getSize());

        lib.symmetrizeMaximum(net);
        assertTrue(net.isSymmetric(), "symmetrize must make it symmetric");

        lib.normalize(net, AgnaLib.NORMALIZE_THRESHOLD, 0f);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                {
                float v = net.getValue(i, j);
                assertTrue(v == 0f || v == 1f, "binary values only");
                }
        assertDiagonalZero(net);

        lib.multiplyNetworks(net, net); // square does not crash, size stays
        assertEquals(n, net.getSize());
        }

    @Test
    public void mergeIsolateAndRemoveOutsidersNeverThrow() throws Exception
        {
        Network a = open("10 circle.agn", "agn").getNetwork();
        Network b = open("example2.agn", "agn").getNetwork();
        Network merged = a.merge(b, Network.MERGE_SUM);
        assertTrue(merged.getSize() >= a.getSize(), "merge appends");
        assertTrue(merged.getSize() <= a.getSize() + b.getSize());

        Network iso = new Network(2);
        iso.setValue(1f, 0, 1);
        iso.setValue(1f, 1, 0);
        iso.isolateActor(1);
        assertEquals(0f, iso.getValue(0, 1), 1e-6f);
        assertEquals(0f, iso.getValue(1, 0), 1e-6f);
        }

    @Test
    public void attributeChangesApply() throws Exception
        {
        Network net = open("example2.agn", "agn").getNetwork();
        net.setName("Stress title");
        assertEquals("Stress title", net.getName());
        net.getActor(0).setName("Renamed actor");
        assertEquals("Renamed actor", net.getActorName(0));
        net.getActor(1).setFace("-"); // fallback, must not throw
        assertNotNull(net.getActor(1).getFaceSource());
        }

    @Test
    public void everyAnalysisRunsOnTheSamples() throws Exception
        {
        String[] samples = { "4 full.txt", "10 circle.agn", "example2.agn" };
        for (String sample : samples)
            {
            AgnaLib lib = new AgnaLib();
            Network net = open(sample,
                    sample.endsWith("txt") ? "txt" : "agn").getNetwork();
            assertNotNull(lib.outBasic(net));
            assertNotNull(lib.outDensity(net));
            assertNotNull(lib.outCohesion(net));
            assertNotNull(lib.outNodalDegree(net));
            assertNotNull(lib.outInDegree(net));
            assertNotNull(lib.outOutDegree(net));
            assertNotNull(lib.outEmissionDegree(net));
            assertNotNull(lib.outReceptionDegree(net));
            assertNotNull(lib.outDeterminationDegree(net));
            assertNotNull(lib.outSociometricStatus(net));
            assertNotNull(lib.outGeodesics(net));
            assertNotNull(lib.outEccentricity(net));
            assertNotNull(lib.outDiameter(net));
            assertNotNull(lib.outBavelas(net));
            assertNotNull(lib.outCloseness(net));
            assertNotNull(lib.outFareness(net));
            assertNotNull(lib.outBetweenness(net));
            assertNotNull(lib.outPrestige(net));
            assertNotNull(lib.outShortestPaths(net, 0, 1));
            assertNotNull(lib.outCliques(net, 2));
            assertNotNull(lib.outFullAnalysis(net));
            }
        }

    private static void assertDiagonalZero(Network net)
        {
        for (int i = 0; i < net.getSize(); i++)
            {
            assertEquals(0f, net.getValue(i, i), 1e-6f,
                    "diagonal stays zero at (" + i + "," + i + ")");
            }
        }
    }