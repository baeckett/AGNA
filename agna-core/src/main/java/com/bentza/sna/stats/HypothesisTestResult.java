/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.stats;

import com.bentza.sna.report.TableData;
import com.bentza.sna.report.TableRegistry;
import java.util.Arrays;
import java.util.Locale;

/**
 * Encapsulates the results of a Monte Carlo statistical hypothesis test
 * comparing an observed social network metric against a null model distribution.
 */
public class HypothesisTestResult
    {
    private final String networkName;
    private final NetworkMetric metric;
    private final NullModel nullModel;
    private final int replications;
    private final double observedValue;
    private final double nullMean;
    private final double nullStdDev;
    private final double zScore;
    private final double pValueTwoTailed;
    private final double pValueGreater;
    private final double pValueLess;
    private final double ciLower95;
    private final double ciUpper95;
    private final double[] nullDistribution;

    public HypothesisTestResult(String networkName, NetworkMetric metric, NullModel nullModel,
            int replications, double observedValue, double nullMean, double nullStdDev,
            double zScore, double pValueTwoTailed, double pValueGreater, double pValueLess,
            double ciLower95, double ciUpper95, double[] nullDistribution)
        {
        this.networkName = networkName != null ? networkName : "Network";
        this.metric = metric;
        this.nullModel = nullModel;
        this.replications = replications;
        this.observedValue = observedValue;
        this.nullMean = nullMean;
        this.nullStdDev = nullStdDev;
        this.zScore = zScore;
        this.pValueTwoTailed = pValueTwoTailed;
        this.pValueGreater = pValueGreater;
        this.pValueLess = pValueLess;
        this.ciLower95 = ciLower95;
        this.ciUpper95 = ciUpper95;
        this.nullDistribution = nullDistribution != null ? nullDistribution.clone() : new double[0];
        }

    public String getNetworkName()
        {
        return networkName;
        }

    public NetworkMetric getMetric()
        {
        return metric;
        }

    public NullModel getNullModel()
        {
        return nullModel;
        }

    public int getReplications()
        {
        return replications;
        }

    public double getObservedValue()
        {
        return observedValue;
        }

    public double getNullMean()
        {
        return nullMean;
        }

    public double getNullStdDev()
        {
        return nullStdDev;
        }

    public double getZScore()
        {
        return zScore;
        }

    public double getPValueTwoTailed()
        {
        return pValueTwoTailed;
        }

    public double getPValueGreater()
        {
        return pValueGreater;
        }

    public double getPValueLess()
        {
        return pValueLess;
        }

    public double getCiLower95()
        {
        return ciLower95;
        }

    public double getCiUpper95()
        {
        return ciUpper95;
        }

    public double[] getNullDistribution()
        {
        return nullDistribution.clone();
        }

    public String getSignificanceInterpretation()
        {
        String pStr = pValueTwoTailed < 0.001 ? "p < 0.001" : String.format(Locale.ROOT, "p = %.4f", pValueTwoTailed);
        if (pValueTwoTailed < 0.05)
            {
            String direction = zScore > 0 ? "significantly HIGHER" : "significantly LOWER";
            return String.format(Locale.ROOT, "Statistically significant (%s, Z = %+.2f): observed %s is %s than expected under %s.",
                    pStr, zScore, metric.getDisplayName(), direction, nullModel.getDisplayName());
            }
        else
            {
            return String.format(Locale.ROOT, "Not statistically significant (%s, Z = %+.2f): observed %s is consistent with chance under %s.",
                    pStr, zScore, metric.getDisplayName(), nullModel.getDisplayName());
            }
        }

    /**
     * Generates an ASCII histogram of the null model distribution,
     * highlighting the position of the observed empirical value.
     */
    public String toAsciiHistogram(int numBins, int barWidth)
        {
        if (nullDistribution.length == 0)
            return "";

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double v : nullDistribution)
            {
            if (v < min) min = v;
            if (v > max) max = v;
            }
        min = Math.min(min, observedValue);
        max = Math.max(max, observedValue);
        if (max - min < 1e-9)
            max = min + 1.0;

        int[] counts = new int[numBins];
        for (double v : nullDistribution)
            {
            int b = (int) ((v - min) / (max - min) * numBins);
            if (b >= numBins) b = numBins - 1;
            if (b < 0) b = 0;
            counts[b]++;
            }

        int maxCount = 0;
        for (int c : counts)
            {
            if (c > maxCount) maxCount = c;
            }

        int obsBin = (int) ((observedValue - min) / (max - min) * numBins);
        if (obsBin >= numBins) obsBin = numBins - 1;
        if (obsBin < 0) obsBin = 0;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Null Distribution Histogram (N = %d replications):\n", replications));
        for (int b = 0; b < numBins; b++)
            {
            double binStart = min + (double) b * (max - min) / numBins;
            double binEnd = min + (double) (b + 1) * (max - min) / numBins;
            int barLen = maxCount > 0 ? (int) Math.round((double) counts[b] / maxCount * barWidth) : 0;
            StringBuilder bar = new StringBuilder();
            for (int k = 0; k < barLen; k++)
                bar.append("#");

            String marker = (b == obsBin) ? " <== OBSERVED (" + String.format(Locale.ROOT, "%.4f", observedValue) + ")" : "";
            sb.append(String.format(Locale.ROOT, "[%6.3f - %6.3f] | %-25s (%4d)%s\n",
                    binStart, binEnd, bar.toString(), counts[b], marker));
            }
        return sb.toString();
        }

    public String toTextReport()
        {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append(" AGNA STATISTICAL HYPOTHESIS TEST: NULL MODEL EVALUATION\n");
        sb.append("================================================================================\n");
        sb.append(String.format(Locale.ROOT, "Network:         %s\n", networkName));
        sb.append(String.format(Locale.ROOT, "Tested Metric:   %s (%s)\n", metric.getDisplayName(), metric.getDescription()));
        sb.append(String.format(Locale.ROOT, "Null Model:      %s\n", nullModel.getDisplayName()));
        sb.append(String.format(Locale.ROOT, "Replications:    %d\n", replications));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format(Locale.ROOT, "Observed Value (M_obs):    %10.4f\n", observedValue));
        sb.append(String.format(Locale.ROOT, "Null Mean (mu_null):       %10.4f\n", nullMean));
        sb.append(String.format(Locale.ROOT, "Null Std Dev (sigma_null): %10.4f\n", nullStdDev));
        sb.append(String.format(Locale.ROOT, "Standardized Z-Score:      %+10.4f\n", zScore));
        sb.append(String.format(Locale.ROOT, "Empirical p-value (2-tail):%10.4f\n", pValueTwoTailed));
        sb.append(String.format(Locale.ROOT, "Empirical p-value (P >=):  %10.4f\n", pValueGreater));
        sb.append(String.format(Locale.ROOT, "Empirical p-value (P <=):  %10.4f\n", pValueLess));
        sb.append(String.format(Locale.ROOT, "95%% Null Interval:        [%7.4f, %7.4f]\n", ciLower95, ciUpper95));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("Verdict: ").append(getSignificanceInterpretation()).append("\n\n");
        sb.append(toAsciiHistogram(12, 25));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("Notes:\n");
        sb.append(String.format(Locale.ROOT, "a. Empirical p-values estimated via Monte Carlo simulation across N = %d null graph realizations.\n", replications));
        sb.append("b. Two-tailed p-value calculated as 2 * min(P(M_null >= M_obs), P(M_null <= M_obs)).\n");
        sb.append("c. Standardized Z-Score represents (M_obs - mu_null) / sigma_null.\n");
        sb.append(String.format(Locale.ROOT, "d. Null model reference: %s (%s).\n", nullModel.getDisplayName(), nullModel.getDescription()));
        sb.append("================================================================================\n");
        return sb.toString();
        }

    public String toHtmlReport()
        {
        TableData td = TableData.builder("Null Model Comparison: " + metric.getDisplayName())
                .network(networkName)
                .columns("Network", "Tested Metric", "Null Model", "Replications", "Observed Value", "Null Mean", "Null Std Dev", "Z-Score", "p-value (two-tailed)", "95% CI Lower", "95% CI Upper")
                .isNumeric(false, false, false, true, true, true, true, true, true, true, true)
                .addRow(networkName, metric.getDisplayName(), nullModel.getDisplayName(), replications, observedValue, nullMean, nullStdDev, zScore, pValueTwoTailed, ciLower95, ciUpper95)
                .build();
        TableRegistry.register(td);

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:sans-serif; margin:15px; color:#222;'>");
        sb.append("<table style='width:100%; border:none; margin-bottom:4px;'><tr>");
        sb.append("<td style='border:none; padding:0; text-align:left;'><h2 style='color:#004080; border-bottom:2px solid #004080; padding-bottom:5px; margin:0;'>AGNA Hypothesis Test: Null Model Comparison</h2></td>");
        sb.append("<td style='border:none; padding:0; text-align:right;'><a href='export:").append(td.getId()).append("' style='font-size:11px; color:#0055aa; text-decoration:none;'><b>[Export Table (CSV/JSON)]</b></a></td>");
        sb.append("</tr></table>");
        sb.append("<table style='border-collapse:collapse; width:100%; max-width:650px; font-size:13px;'>");
        sb.append("<tr style='background:#f4f6f9;'><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>Network</td>")
          .append("<td style='padding:6px; border:1px solid #ddd;'>").append(networkName).append("</td></tr>");
        sb.append("<tr><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>Tested Metric</td>")
          .append("<td style='padding:6px; border:1px solid #ddd;'><b>").append(metric.getDisplayName())
          .append("</b><br><small style='color:#555;'>").append(metric.getDescription()).append("</small></td></tr>");
        sb.append("<tr style='background:#f4f6f9;'><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>Null Model</td>")
          .append("<td style='padding:6px; border:1px solid #ddd;'><b>").append(nullModel.getDisplayName())
          .append("</b><br><small style='color:#555;'>").append(nullModel.getDescription()).append("</small></td></tr>");
        sb.append("<tr><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>Replications</td>")
          .append("<td style='padding:6px; border:1px solid #ddd;'>").append(replications).append("</td></tr>");
        sb.append("<tr style='background:#eef6ff;'><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>Observed Value (M<sub>obs</sub>)</td>")
          .append("<td style='padding:6px; font-weight:bold; color:#0055aa; border:1px solid #ddd;'>")
          .append(String.format(Locale.ROOT, "%.4f", observedValue)).append("</td></tr>");
        sb.append("<tr><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>Null Mean (&mu;<sub>null</sub>)</td>")
          .append("<td style='padding:6px; border:1px solid #ddd;'>")
          .append(String.format(Locale.ROOT, "%.4f", nullMean)).append("</td></tr>");
        sb.append("<tr style='background:#f4f6f9;'><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>Null Std. Dev (&sigma;<sub>null</sub>)</td>")
          .append("<td style='padding:6px; border:1px solid #ddd;'>")
          .append(String.format(Locale.ROOT, "%.4f", nullStdDev)).append("</td></tr>");
        sb.append("<tr><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>Standardized Z-Score</td>")
          .append("<td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>")
          .append(String.format(Locale.ROOT, "%+.3f", zScore)).append("</td></tr>");
        sb.append("<tr style='background:#f4f6f9;'><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>Empirical p-value (two-tailed)</td>")
          .append("<td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>")
          .append(pValueTwoTailed < 0.001 ? "p &lt; 0.001" : String.format(Locale.ROOT, "p = %.4f", pValueTwoTailed))
          .append("</td></tr>");
        sb.append("<tr><td style='padding:6px; font-weight:bold; border:1px solid #ddd;'>95% Null Confidence Interval</td>")
          .append("<td style='padding:6px; border:1px solid #ddd;'>[")
          .append(String.format(Locale.ROOT, "%.4f, %.4f", ciLower95, ciUpper95)).append("]</td></tr>");
        sb.append("</table>");

        boolean sig = pValueTwoTailed < 0.05;
        String alertBg = sig ? "#e8f5e9" : "#fffde7";
        String alertBorder = sig ? "#2e7d32" : "#fbc02d";
        sb.append("<div style='margin-top:12px; padding:10px; background:").append(alertBg)
          .append("; border-left:4px solid ").append(alertBorder).append("; font-size:13px;'>")
          .append("<b>Statistical Conclusion:</b> ").append(getSignificanceInterpretation())
          .append("</div>");

        sb.append("<div style='margin-top:15px;'><pre style='background:#f8f9fa; border:1px solid #e1e4e8; padding:10px; font-size:11px; border-radius:4px;'>")
          .append(toAsciiHistogram(12, 30))
          .append("</pre></div>");

        // SPSS-style Notes / Methodological Remarks Block
        sb.append("<table style='width:100%; max-width:650px; border-collapse:collapse; margin-top:14px; font-size:11px; background:#fafbfc; border:1px solid #d0d7de;'>\n");
        sb.append("  <tr style='background:#f3f4f6; border-bottom:1px solid #d0d7de;'>\n");
        sb.append("    <td colspan='2' style='padding:5px 8px; font-weight:bold; color:#24292f; text-transform:uppercase; letter-spacing:0.5px;'>\n");
        sb.append("      <b>Notes / Methodological Remarks</b>\n");
        sb.append("    </td>\n");
        sb.append("  </tr>\n");
        sb.append("  <tr>\n");
        sb.append("    <td style='width:24px; padding:3px 4px 3px 8px; vertical-align:top; font-weight:bold; color:#57606a;'>a.</td>\n");
        sb.append("    <td style='padding:3px 8px; vertical-align:top; color:#57606a;'>Empirical p-values estimated via Monte Carlo simulation across N = ").append(replications).append(" null graph realizations.</td>\n");
        sb.append("  </tr>\n");
        sb.append("  <tr>\n");
        sb.append("    <td style='width:24px; padding:3px 4px 3px 8px; vertical-align:top; font-weight:bold; color:#57606a;'>b.</td>\n");
        sb.append("    <td style='padding:3px 8px; vertical-align:top; color:#57606a;'>Two-tailed p-value calculated as 2 &times; min(P(M<sub>null</sub> &ge; M<sub>obs</sub>), P(M<sub>null</sub> &le; M<sub>obs</sub>)).</td>\n");
        sb.append("  </tr>\n");
        sb.append("  <tr>\n");
        sb.append("    <td style='width:24px; padding:3px 4px 3px 8px; vertical-align:top; font-weight:bold; color:#57606a;'>c.</td>\n");
        sb.append("    <td style='padding:3px 8px; vertical-align:top; color:#57606a;'>Standardized Z-Score represents (M<sub>obs</sub> - &mu;<sub>null</sub>) / &sigma;<sub>null</sub>.</td>\n");
        sb.append("  </tr>\n");
        sb.append("  <tr>\n");
        sb.append("    <td style='width:24px; padding:3px 4px 3px 8px; vertical-align:top; font-weight:bold; color:#57606a;'>d.</td>\n");
        sb.append("    <td style='padding:3px 8px; vertical-align:top; color:#57606a;'>Null model reference: ").append(nullModel.getDisplayName()).append(" (").append(nullModel.getDescription()).append(").</td>\n");
        sb.append("  </tr>\n");
        sb.append("  <tr style='border-top:1px dashed #e1e4e8; background:#ffffff;'>\n");
        sb.append("    <td colspan='2' style='padding:4px 8px; font-size:10px; color:#8c959f; font-style:italic;'>\n");
        sb.append("      Active Dataset: ").append(networkName).append(" &bull; AGNA 2.1.6 Hypothesis Testing Engine &bull; Table available for export above\n");
        sb.append("    </td>\n");
        sb.append("  </tr>\n");
        sb.append("</table>\n");

        sb.append("</div>");
        return sb.toString();
        }

    public String toJson()
        {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"network\": \"").append(networkName).append("\",\n");
        sb.append("  \"metric\": \"").append(metric.name()).append("\",\n");
        sb.append("  \"null_model\": \"").append(nullModel.name()).append("\",\n");
        sb.append("  \"replications\": ").append(replications).append(",\n");
        sb.append(String.format(Locale.ROOT, "  \"observed_value\": %.6f,\n", observedValue));
        sb.append(String.format(Locale.ROOT, "  \"null_mean\": %.6f,\n", nullMean));
        sb.append(String.format(Locale.ROOT, "  \"null_std_dev\": %.6f,\n", nullStdDev));
        sb.append(String.format(Locale.ROOT, "  \"z_score\": %.6f,\n", zScore));
        sb.append(String.format(Locale.ROOT, "  \"p_value_two_tailed\": %.6f,\n", pValueTwoTailed));
        sb.append(String.format(Locale.ROOT, "  \"p_value_greater\": %.6f,\n", pValueGreater));
        sb.append(String.format(Locale.ROOT, "  \"p_value_less\": %.6f,\n", pValueLess));
        sb.append(String.format(Locale.ROOT, "  \"ci_lower_95\": %.6f,\n", ciLower95));
        sb.append(String.format(Locale.ROOT, "  \"ci_upper_95\": %.6f,\n", ciUpper95));
        sb.append("  \"significant\": ").append(pValueTwoTailed < 0.05).append("\n");
        sb.append("}");
        return sb.toString();
        }
    }
