/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.audit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * 2.2.2: installers ship exactly the sample networks that AGNA 2.2.1 shipped (the ten files embedded in the jar).
 * The repository's samples/ folder also holds test fixtures (Chain1.txt, "4 full.txt", ...) that must never reach
 * an installer; packaging/stage-samples.sh copies only what packaging/samples-manifest.txt lists.
 */
public class InstallerSamplesManifestTest
    {
    private static List<String> manifest() throws Exception
        {
        return Files.readAllLines(new File("packaging/samples-manifest.txt").toPath(), StandardCharsets.UTF_8)
                .stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        }

    @Test
    public void manifestIsExactlyTheSetEmbeddedInTheJar() throws Exception
        {
        String[] embedded = new File("agna-core/src/main/resources/samples").list();
        assertTrue(embedded != null, "run the tests from the repository root");
        assertEquals(new TreeSet<>(Arrays.asList(embedded)), new TreeSet<>(manifest()),
                "installer samples must equal the sample set embedded in the jar (the 2.2.1 set)");
        assertEquals(10, manifest().size());
        }

    @Test
    public void manifestFilesAreByteIdenticalToTheEmbeddedOnes() throws Exception
        {
        for (String name : manifest())
            {
            byte[] shipped = Files.readAllBytes(new File("samples", name).toPath());
            byte[] embedded = Files.readAllBytes(new File("agna-core/src/main/resources/samples", name).toPath());
            assertArrayEquals(embedded, shipped, name);
            }
        }

    @Test
    public void testFixturesAreNeverListed() throws Exception
        {
        for (String name : manifest())
            {
            String n = name.toLowerCase();
            assertTrue(!n.startsWith("chain") && !n.contains("full"), "test fixture in installer manifest: " + name);
            }
        }
    }
