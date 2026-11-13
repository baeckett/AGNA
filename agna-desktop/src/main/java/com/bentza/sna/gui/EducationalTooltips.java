/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.edu.EducationalKnowledge;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Educational repository of SNA definitions, sociological interpretations,
 * and practical guidance for novice users and researchers.
 * Powers rich HTML tooltips across all computational battery dialogs and the
 * in-app SNA Glossary / Cheat Sheet.
 * Delegates to EducationalKnowledge in agna-core.
 */
public final class EducationalTooltips {

    public static class MetricGuide extends EducationalKnowledge.MetricGuide {
        public MetricGuide(EducationalKnowledge.MetricGuide g) {
            super(g.id, g.title, g.category, g.whatItMeasures, g.whyUseIt, g.interpretation, g.technicalNote);
        }

        public MetricGuide(String id, String title, String category,
                           String whatItMeasures, String whyUseIt,
                           String interpretation, String technicalNote) {
            super(id, title, category, whatItMeasures, whyUseIt, interpretation, technicalNote);
        }
    }

    private static final Map<String, MetricGuide> WRAPPED = new LinkedHashMap<>();

    static {
        for (EducationalKnowledge.MetricGuide g : EducationalKnowledge.getAll().values()) {
            WRAPPED.put(g.id.toLowerCase(Locale.ROOT), new MetricGuide(g));
        }
    }

    public static MetricGuide get(String id) {
        if (id == null) return null;
        return WRAPPED.get(id.toLowerCase(Locale.ROOT));
    }

    public static String getTooltip(String id) {
        EducationalKnowledge.MetricGuide g = EducationalKnowledge.get(id);
        return g != null ? g.toHtmlTooltip() : null;
    }

    public static Map<String, MetricGuide> getAll() {
        return Collections.unmodifiableMap(WRAPPED);
    }

    private EducationalTooltips() {}
}
