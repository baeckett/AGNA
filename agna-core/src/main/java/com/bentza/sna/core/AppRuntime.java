package com.bentza.sna.core;

import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.ImageStock;
import com.bentza.sna.net.Network;
import java.awt.Component;

/**
 * 2.1.3: the seam between the engine (agna-core) and the desktop
 * application. Everything the engine needs from the outside world is
 * either state owned here or a hook the desktop registers at startup:
 *   - preferences (default node face, working directory, look and feel,
 *     classic icons, Pajek vectors, remembered export format, weighted)
 *   - the image stock
 *   - UI hooks (output pane content type and appends, viewer status
 *     messages, dialog parents, progress reports, the current network)
 * In headless runs (tests, the CLI) the hooks simply default to
 * harmless no-ops, which is what makes the engine usable as a library.
 */
public class AppRuntime
    {
    private static String default_node_face_source;
    private static String working_directory;
    private static int remembered_export_format = 3;
    private static boolean pajek_vectors_enabled = true;
    private static boolean classic_toolbar_icons = false;
    private static String look_and_feel = "system";
    private static boolean current_weight = true;
    private static ImageStock image_stock;

    private static java.util.function.Supplier<String> outputContentType =
            () -> "text/plain";
    private static java.util.function.Consumer<String> appendOutput =
            s -> { };
    private static java.util.function.Consumer<String> htmlStatus =
            s -> { };
    private static java.util.function.Supplier<Component> dialogParent =
            () -> null;
    private static java.util.function.Supplier<Network> currentNetwork =
            () -> null;
    private static java.util.function.Supplier<FullNet> currentFullNet =
            () -> null;
    private static java.util.function.IntConsumer progressSink =
            p -> { };
    private static java.util.function.BiConsumer<com.bentza.sna.net.Actor,
            Integer> nodeDialogHook = (actor, index) -> { };
    private static NodeActions nodeActions = new NodeActions()
        {
        public void addNodeAndFocus(int x, int y, int width)
            {
            }
        public void enableFirstNodeTools()
            {
            }
        public void resetStatus()
            {
            }
        };

    // the node-area interactions the desktop implements; no-ops headless
    public interface NodeActions
        {
        void addNodeAndFocus(int x, int y, int width);

        void enableFirstNodeTools();

        void resetStatus();
        }

    public static void setNodeActions(NodeActions actions)
        {
        if (actions != null)
            nodeActions = actions;
        }

    public static void addNodeAndFocus(int x, int y, int width)
        {
        nodeActions.addNodeAndFocus(x, y, width);
        }

    public static void enableFirstNodeTools()
        {
        nodeActions.enableFirstNodeTools();
        }

    public static void resetStatus()
        {
        nodeActions.resetStatus();
        }

    public static void registerHooks(java.util.function.Supplier<String> ct,
            java.util.function.Consumer<String> append,
            java.util.function.Consumer<String> status,
            java.util.function.Supplier<Component> parent,
            java.util.function.Supplier<Network> netSupplier,
            java.util.function.Supplier<FullNet> fullNetSupplier,
            java.util.function.IntConsumer progress)
        {
        if (ct != null)
            outputContentType = ct;
        if (append != null)
            appendOutput = append;
        if (status != null)
            htmlStatus = status;
        if (parent != null)
            dialogParent = parent;
        if (netSupplier != null)
            currentNetwork = netSupplier;
        if (fullNetSupplier != null)
            currentFullNet = fullNetSupplier;
        if (progress != null)
            progressSink = progress;
        }

    public static void registerHooks(java.util.function.Supplier<String> ct,
            java.util.function.Consumer<String> append,
            java.util.function.Consumer<String> status,
            java.util.function.Supplier<Component> parent,
            java.util.function.Supplier<Network> netSupplier,
            java.util.function.Supplier<FullNet> fullNetSupplier,
            java.util.function.IntConsumer progress,
            java.util.function.BiConsumer<com.bentza.sna.net.Actor,
                    Integer> nodeDialog)
        {
        registerHooks(ct, append, status, parent, netSupplier,
                fullNetSupplier, progress);
        if (nodeDialog != null)
            nodeDialogHook = nodeDialog;
        }

    public static void showNodeDialog(com.bentza.sna.net.Actor actor,
            int index)
        {
        nodeDialogHook.accept(actor, index);
        }

    // ---- preferences (moved here from MainFrame so the engine stands
    // alone; MainFrame keeps its API by delegating to these) ----

    public static String getDefaultNodeFaceSource()
        {
        try
            {
            if (default_node_face_source == null
                    || !(new java.io.File(default_node_face_source)).exists())
                {
                default_node_face_source =
                        com.bentza.sna.Environment.getFacesDirectory()
                                + System.getProperty("file.separator")
                                + "Red Bullet.gif";
                }
            return default_node_face_source;
            } catch (Exception e)
            {
            return "-";
            }
        }

    public static void setDefaultNodeFaceSource(String tmp_face_source)
        {
        if (tmp_face_source == null || tmp_face_source.equals("-")
                || !(new java.io.File(tmp_face_source)).exists())
            {
            default_node_face_source =
                    com.bentza.sna.Environment.getFacesDirectory()
                            + System.getProperty("file.separator")
                            + "Red Bullet.gif";
            } else
            {
            default_node_face_source = tmp_face_source;
            }
        }

    public static String getWorkingDirectory()
        {
        if (working_directory != null
                && (new java.io.File(working_directory)).isDirectory())
            {
            // adding file-separator character at end:
            if (working_directory.lastIndexOf(System
                    .getProperty("file.separator"))
                    != working_directory.length() - 1)
                working_directory += System.getProperty("file.separator");
            return working_directory;
            } else
            return com.bentza.sna.Environment.getCurrentDirectory();
        }

    public static void setWorkingDirectory(String tmp)
        {
        if (tmp != null && (new java.io.File(tmp)).isDirectory())
            {
            working_directory = tmp;
            } else
            {
            working_directory = null;
            }
        }

    public static int getRememberedExportFormat()
        {
        return remembered_export_format;
        }

    public static void setRememberedExportFormat(int tmp)
        {
        remembered_export_format = tmp;
        }

    public static boolean isPajekVectorsEnabled()
        {
        return pajek_vectors_enabled;
        }

    public static void setPajekVectorsEnabled(boolean tmp)
        {
        pajek_vectors_enabled = tmp;
        }

    public static boolean isClassicToolbarIcons()
        {
        return classic_toolbar_icons;
        }

    public static void setClassicToolbarIcons(boolean tmp)
        {
        classic_toolbar_icons = tmp;
        }

    public static String getLookAndFeel()
        {
        return look_and_feel;
        }

    public static void setLookAndFeel(String tmp)
        {
        if (tmp != null)
            {
            look_and_feel = tmp;
            }
        }

    public static boolean getCurrentWeight()
        {
        return current_weight;
        }

    public static void setCurrentWeight(boolean tmp)
        {
        current_weight = tmp;
        }

    public static ImageStock getCurrentImageStock()
        {
        if (image_stock == null)
            {
            image_stock = new ImageStock();
            }
        return image_stock;
        }

    // ---- UI hooks ----

    public static String outputContentType()
        {
        String type = outputContentType.get();
        return type == null ? "text/plain" : type;
        }

    public static void appendToOutput(String text)
        {
        appendOutput.accept(text);
        }

    public static void setHTMLStatus(String text)
        {
        htmlStatus.accept(text);
        }

    public static Component getDialogParent()
        {
        return dialogParent.get();
        }

    public static Network getCurrentNetwork()
        {
        return currentNetwork.get();
        }

    public static FullNet getCurrentFullNet()
        {
        return currentFullNet.get();
        }

    public static void setProgress(int percent)
        {
        progressSink.accept(percent);
        }
    }