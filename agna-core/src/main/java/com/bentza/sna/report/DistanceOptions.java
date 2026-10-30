/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.report;

import java.io.Serializable;

/**
 * Options for the Distances & Geodesics Battery in Agna.
 */
public class DistanceOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean includeGeodesicsMatrix = true;
    private boolean includeDiameter = true;
    private boolean includeEccentricity = true;
    private boolean includeSpecificShortestPath = false;
    private int fromNode = 0;
    private int toNode = 1;
    private boolean includeAllShortestPaths = false;

    public DistanceOptions() {
    }

    public static DistanceOptions createStandard() {
        return new DistanceOptions();
    }

    public static DistanceOptions createAll() {
        DistanceOptions opt = new DistanceOptions();
        opt.setIncludeGeodesicsMatrix(true);
        opt.setIncludeDiameter(true);
        opt.setIncludeEccentricity(true);
        opt.setIncludeSpecificShortestPath(true);
        opt.setIncludeAllShortestPaths(true);
        return opt;
    }

    public boolean isIncludeGeodesicsMatrix() { return includeGeodesicsMatrix; }
    public void setIncludeGeodesicsMatrix(boolean includeGeodesicsMatrix) { this.includeGeodesicsMatrix = includeGeodesicsMatrix; }

    public boolean isIncludeDiameter() { return includeDiameter; }
    public void setIncludeDiameter(boolean includeDiameter) { this.includeDiameter = includeDiameter; }

    public boolean isIncludeEccentricity() { return includeEccentricity; }
    public void setIncludeEccentricity(boolean includeEccentricity) { this.includeEccentricity = includeEccentricity; }

    public boolean isIncludeSpecificShortestPath() { return includeSpecificShortestPath; }
    public void setIncludeSpecificShortestPath(boolean includeSpecificShortestPath) { this.includeSpecificShortestPath = includeSpecificShortestPath; }

    public int getFromNode() { return fromNode; }
    public void setFromNode(int fromNode) { this.fromNode = fromNode; }

    public int getToNode() { return toNode; }
    public void setToNode(int toNode) { this.toNode = toNode; }

    public boolean isIncludeAllShortestPaths() { return includeAllShortestPaths; }
    public void setIncludeAllShortestPaths(boolean includeAllShortestPaths) { this.includeAllShortestPaths = includeAllShortestPaths; }
}
