/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import java.io.Serializable;

/**
 * Options for the Sociometrics & Basic Battery in Agna.
 */
public class SociometricsOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean includeBasic = true;
    private boolean includeDensity = true;
    private boolean includeDyadCensus = true;
    private boolean includeNodalDegree = true;
    private boolean includeInDegree = true;
    private boolean includeOutDegree = true;
    private boolean includeEmission = false;
    private boolean includeReception = false;
    private boolean includeDetermination = false;
    private boolean includeSociostatus = false;

    public SociometricsOptions() {
    }

    public static SociometricsOptions createStandard() {
        return new SociometricsOptions();
    }

    public static SociometricsOptions createAll() {
        SociometricsOptions opt = new SociometricsOptions();
        opt.setIncludeBasic(true);
        opt.setIncludeDensity(true);
        opt.setIncludeDyadCensus(true);
        opt.setIncludeNodalDegree(true);
        opt.setIncludeInDegree(true);
        opt.setIncludeOutDegree(true);
        opt.setIncludeEmission(true);
        opt.setIncludeReception(true);
        opt.setIncludeDetermination(true);
        opt.setIncludeSociostatus(true);
        return opt;
    }

    public boolean isIncludeBasic() { return includeBasic; }
    public void setIncludeBasic(boolean includeBasic) { this.includeBasic = includeBasic; }

    public boolean isIncludeDensity() { return includeDensity; }
    public void setIncludeDensity(boolean includeDensity) { this.includeDensity = includeDensity; }

    public boolean isIncludeDyadCensus() { return includeDyadCensus; }
    public void setIncludeDyadCensus(boolean includeDyadCensus) { this.includeDyadCensus = includeDyadCensus; }

    public boolean isIncludeNodalDegree() { return includeNodalDegree; }
    public void setIncludeNodalDegree(boolean includeNodalDegree) { this.includeNodalDegree = includeNodalDegree; }

    public boolean isIncludeInDegree() { return includeInDegree; }
    public void setIncludeInDegree(boolean includeInDegree) { this.includeInDegree = includeInDegree; }

    public boolean isIncludeOutDegree() { return includeOutDegree; }
    public void setIncludeOutDegree(boolean includeOutDegree) { this.includeOutDegree = includeOutDegree; }

    public boolean isIncludeEmission() { return includeEmission; }
    public void setIncludeEmission(boolean includeEmission) { this.includeEmission = includeEmission; }

    public boolean isIncludeReception() { return includeReception; }
    public void setIncludeReception(boolean includeReception) { this.includeReception = includeReception; }

    public boolean isIncludeDetermination() { return includeDetermination; }
    public void setIncludeDetermination(boolean includeDetermination) { this.includeDetermination = includeDetermination; }

    public boolean isIncludeSociostatus() { return includeSociostatus; }
    public void setIncludeSociostatus(boolean includeSociostatus) { this.includeSociostatus = includeSociostatus; }
}
