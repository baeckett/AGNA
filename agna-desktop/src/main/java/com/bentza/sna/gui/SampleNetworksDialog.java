/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2001-2026 Marius Ion Bența
 */

package com.bentza.sna.gui;

import com.bentza.sna.Environment;
import com.bentza.sna.net.FullNet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;

/**
 * Educational dialog offering curated case studies of seminal social network datasets,
 * complete with sociological context, theoretical significance, and guided questions.
 */
public class SampleNetworksDialog extends JDialog {

    public static class SampleCaseStudy {
        public final String title;
        public final String category;
        public final String filename;
        public final String citation;
        public final String overview;
        public final String guidedQuestionsHtml;

        public SampleCaseStudy(String title, String category, String filename,
                               String citation, String overview, String guidedQuestionsHtml) {
            this.title = title;
            this.category = category;
            this.filename = filename;
            this.citation = citation;
            this.overview = overview;
            this.guidedQuestionsHtml = guidedQuestionsHtml;
        }

        @Override
        public String toString() {
            return title + " [" + category + "]";
        }
    }

    private final Frame owner;
    private final Consumer<File> onLoadFile;
    private final List<SampleCaseStudy> studies = new ArrayList<>();

    private JList<SampleCaseStudy> list;
    private JTextPane detailPane;

    public SampleNetworksDialog(Frame owner, Consumer<File> onLoadFile) {
        super(owner, "Sample Networks & Guided Case Studies", true);
        this.owner = owner;
        this.onLoadFile = onLoadFile;

        initCaseStudies();
        initComponents();
        pack();
        setSize(780, 560);
        setLocationRelativeTo(owner);
    }

    private void initCaseStudies() {
        studies.add(new SampleCaseStudy(
                "Padgett's Florentine Families (1434)",
                "Political Power & Brokerage",
                "Padgett Florentine Families.agn",
                "Padgett, J. F., & Ansell, C. K. (1993). Robust action and the rise of the Medici, 1400-1434. American Journal of Sociology, 98(6), 1259-1319.",
                "In 15th-century Renaissance Florence, the Medici family rose to political hegemony despite having less wealth than the Strozzi and fewer patrician ancestors than the Albizzi. Padgett and Ansell demonstrated that Cosimo de' Medici's political power stemmed from strategic network positioning: marriage alliances placed the Medici at the center of a structural hole, bridging opposing aristocratic factions who refused to ally with one another directly.",
                "<ul>" +
                "<li><b>Betweenness Centrality:</b> Run <i>Analysis ▸ Centrality & Prestige...</i>. Notice that the Medici family has a betweenness score of 0.522—more than double any other family—confirming their role as the indispensable political broker.</li>" +
                "<li><b>Structural Holes:</b> Run <i>Analysis ▸ Social Capital & Structural Holes (Burt)</i>. Observe Medici's exceptionally low constraint (0.209), proving their access to non-redundant communication channels.</li>" +
                "<li><b>Ego Network:</b> Use <i>Network ▸ Extract Ego Network</i> for 'Medici' to see their direct coalition.</li>" +
                "</ul>"
        ));

        studies.add(new SampleCaseStudy(
                "Zachary's Karate Club (1977)",
                "Community Split & Modularity",
                "Zachary Karate Club.agn",
                "Zachary, W. W. (1977). An information flow model for conflict and fission in small groups. Journal of Anthropological Research, 33(4), 452-473.",
                "Wayne Zachary spent two years observing social interactions among 34 members of a university karate club. During the study, a dispute over lesson fees erupted between the club's martial arts instructor (Mr. Hi / Member 1) and its president (John A. / Member 34). The club subsequently fractured into two distinct organizations. This dataset is the world's most famous benchmark for community detection and network fission models.",
                "<ul>" +
                "<li><b>Community Detection:</b> Run <i>Analysis ▸ Clustering & Cohesion...</i> with <b>Community Detection (Louvain)</b> checked. Compare the discovered two communities to the historical split centered around Member 1 and Member 34.</li>" +
                "<li><b>High Modularity:</b> Check Newman's modularity score Q. A score above 0.35 proves significant community structure.</li>" +
                "<li><b>Coreness:</b> Run <i>k-Core Decomposition</i> to identify the loyal inner cores surrounding each leader.</li>" +
                "</ul>"
        ));

        studies.add(new SampleCaseStudy(
                "Davis's Southern Women (1941)",
                "Social Circles & Clique Overlap",
                "Davis Southern Women.agn",
                "Davis, A., Gardner, B. B., & Gardner, M. R. (1941). Deep South: A social anthropological study of caste and class. University of Chicago Press.",
                "A foundational anthropological study of social stratification in Natchez, Mississippi. Researchers recorded 18 women attending 14 informal social events (teas, parties, bridge games) over nine months. The one-mode projection records shared social event co-attendance, illustrating how overlapping informal circles structure social class and prestige.",
                "<ul>" +
                "<li><b>Maximal Cliques:</b> Run <i>Analysis ▸ Clustering & Cohesion...</i> with <b>Maximal Cliques</b> enabled. Notice how the women partition into two major social cliques with several bridging liaisons (e.g., Ruth, Pearl).</li>" +
                "<li><b>Density & Cohesion:</b> Run <i>Analysis ▸ Basic Description</i> to observe the unusually high network density of high-society co-attendance.</li>" +
                "<li><b>Eigenvector Centrality:</b> Discover who occupies the core of the social socialite elite.</li>" +
                "</ul>"
        ));

        studies.add(new SampleCaseStudy(
                "Fathers of Sociology",
                "Theoretical Foundations",
                "Sociologists.agn",
                "Bența, M. I. (2001-2026). AGNA Classic Sample Network.",
                "A clean 3-actor network linking classical sociological thinkers (Max Weber, Émile Durkheim, and Karl Marx) with mutual and asymmetric theoretical dialogues. Perfect for beginners testing triad closure and sociometric status formulas.",
                "<ul>" +
                "<li><b>Triad Census:</b> Run <i>Analysis ▸ Triad & Dyad Census</i> to examine complete triadic closure (Configuration 300).</li>" +
                "<li><b>Status & Determination:</b> Test Agna's proprietary Sociometric Status and Determination degree formulas.</li>" +
                "</ul>"
        ));

        studies.add(new SampleCaseStudy(
                "10-Node Star Network",
                "Theoretical Maximizer of Centralization",
                "10 star.agn",
                "Freeman, L. C. (1979). Centrality in social networks: Conceptual clarification. Social Networks, 1(3), 215-239.",
                "The canonical star network consists of one central hub connected to 9 peripheral spokes with zero ties among the spokes. Freeman proved mathematically that the star network is the unique graph structure that simultaneously maximizes degree centralization (1.0), betweenness centralization (1.0), and closeness centralization (1.0).",
                "<ul>" +
                "<li><b>Centralization Verification:</b> Run <i>Analysis ▸ Centrality & Prestige...</i>. Confirm that all Freeman centralization indices are exactly 1.0000.</li>" +
                "<li><b>Diameter & Radius:</b> Notice that the center has eccentricity 1 while all spokes have eccentricity 2 (diameter = 2).</li>" +
                "</ul>"
        ));

        studies.add(new SampleCaseStudy(
                "10-Node Circle Network",
                "Egalitarian Regular Lattice",
                "10 circle.agn",
                "Watts, D. J., & Strogatz, S. H. (1998). Collective dynamics of 'small-world' networks. Nature, 393(6684), 440-442.",
                "A 2-regular ring lattice where every actor connects symmetrically to exactly two neighbors. Represents the complete opposite of a star: total equality, zero centralization (0.0), and homogeneous local structure.",
                "<ul>" +
                "<li><b>Zero Centralization:</b> Run <i>Analysis ▸ Centrality & Prestige...</i>. Notice that degree and closeness centralizations are exactly 0.0000.</li>" +
                "<li><b>Transitivity:</b> Check transitivity on a simple ring (0.0000, since no 3-cycles exist without chord shortcuts).</li>" +
                "</ul>"
        ));
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        JLabel title = new JLabel("Curated Case Studies & Empirical Benchmark Networks");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(0, 64, 128));

        JLabel sub = new JLabel("<html>Select a seminal social network dataset to inspect its historical context, "
                + "theoretical significance, and guided analytical exercises.</html>");
        sub.setFont(sub.getFont().deriveFont(12f));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Center Split Pane
        DefaultListModel<SampleCaseStudy> model = new DefaultListModel<>();
        for (SampleCaseStudy s : studies) model.addElement(s);

        list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);

        detailPane = new JTextPane();
        detailPane.setContentType("text/html");
        detailPane.setEditable(false);
        detailPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetailPane();
        });
        updateDetailPane();

        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setPreferredSize(new Dimension(240, 360));
        listScroll.setBorder(BorderFactory.createTitledBorder("Benchmark Case Studies"));

        JScrollPane detailScroll = new JScrollPane(detailPane);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Sociological Context & Guided Exploration"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, detailScroll);
        splitPane.setDividerLocation(260);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        add(splitPane, BorderLayout.CENTER);

        // Bottom Button Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnLoad = new JButton("Load Dataset into Agna");
        btnLoad.setFont(btnLoad.getFont().deriveFont(Font.BOLD));
        JButton btnCancel = new JButton("Close");

        bottomBar.add(btnLoad);
        bottomBar.add(btnCancel);
        add(bottomBar, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnLoad.addActionListener(e -> onLoadSelected());
        getRootPane().setDefaultButton(btnLoad);
    }

    private void updateDetailPane() {
        SampleCaseStudy s = list.getSelectedValue();
        if (s == null) {
            detailPane.setText("");
            return;
        }

        StringBuilder html = new StringBuilder(1024);
        html.append("<html><div style='font-family:sans-serif; padding:6px; color:#24292f; font-size:11px;'>");
        html.append("<h2 style='margin:0; color:#004080; font-size:14px;'>").append(s.title).append("</h2>");
        html.append("<span style='background:#eef6ff; color:#0056b3; padding:2px 6px; font-size:10px; font-weight:bold; border-radius:3px;'>")
            .append(s.category).append("</span>");

        html.append("<p style='margin:8px 0 4px 0; color:#57606a; font-style:italic; font-size:10px;'>")
            .append("<b>Reference:</b> ").append(s.citation).append("</p>");

        html.append("<div style='margin-top:10px; font-size:11px; line-height:1.4;'>");
        html.append("<b>Historical Context & Sociological Meaning:</b><br>");
        html.append(s.overview);
        html.append("</div>");

        html.append("<div style='margin-top:12px; background:#fafbfc; border:1px solid #e1e4e8; border-radius:4px; padding:8px 10px;'>");
        html.append("<b style='color:#004080;'>💡 Guided Exploration / What to Try in Agna:</b>");
        html.append(s.guidedQuestionsHtml);
        html.append("</div>");

        html.append("</div></html>");
        detailPane.setText(html.toString());
        detailPane.setCaretPosition(0);
    }

    private void onLoadSelected() {
        SampleCaseStudy s = list.getSelectedValue();
        if (s == null) return;

        File target = resolveSampleFile(s.filename);
        if (target == null || !target.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Could not find sample dataset file: " + s.filename + "\nPlease check that samples/ folder is present.",
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();
        if (onLoadFile != null) {
            onLoadFile.accept(target);
        }
    }

    private static File resolveSampleFile(String filename) {
        File f = new File("samples", filename);
        if (f.exists()) return f;

        File alt = new File(Environment.getDefaultOpenDirectory(), filename);
        if (alt.exists()) return alt;

        // Fallback: extract from bundled classpath resources
        try (java.io.InputStream in = SampleNetworksDialog.class.getResourceAsStream("/samples/" + filename)) {
            if (in != null) {
                File tmp = File.createTempFile("agna_sample_", "_" + filename);
                tmp.deleteOnExit();
                java.nio.file.Files.copy(in, tmp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return tmp;
            }
        } catch (Exception ignored) {
        }

        return f;
    }
}
