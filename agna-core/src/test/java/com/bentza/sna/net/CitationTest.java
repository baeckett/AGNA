package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bentza.sna.Environment;

import org.junit.jupiter.api.Test;

/**
 * 2.1.3: the citation metadata is the single source for the DOI, the
 * software title, the APA citation, the desktop block and the one-line
 * footer used across the app and the CLI.
 */
public class CitationTest
    {
    @Test
    public void doiIsTheReleasedZenodoDoi()
        {
        assertEquals("10.5281/zenodo.22708199", Environment.getCitationDoi());
        }

    @Test
    public void softwareTitleMatchesTheBrand()
        {
        assertEquals("AGNA: Applied Graph and Network Analysis Open Source",
                Environment.getSoftwareTitle());
        }

    @Test
    public void citationTextCarriesAuthorTitleAndDoi()
        {
        String text = Environment.getCitationText();
        assertTrue(text.contains("Ben\u021Ba, M. I. (2026)"), text);
        assertTrue(text.contains(Environment.getSoftwareTitle()), text);
        assertTrue(text.contains(
                "https://doi.org/10.5281/zenodo.22708199"), text);
        }

    @Test
    public void desktopBlockListsLicenseAndVersion()
        {
        String block = Environment.getDesktopCitationBlock();
        assertTrue(block.contains("Software and citation"), block);
        assertTrue(block.contains("AGNA Desktop 2.1.3"), block);
        assertTrue(block.contains("Apache License 2.0"), block);
        assertTrue(block.contains("reproducibility"), block);
        }

    @Test
    public void footerIsOneLineWithDoi()
        {
        String footer = Environment.getSoftwareFooter();
        assertTrue(footer.contains("Generated with AGNA"), footer);
        assertTrue(footer.contains("10.5281/zenodo.22708199"), footer);
        }
    }