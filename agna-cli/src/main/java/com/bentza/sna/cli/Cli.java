package com.bentza.sna.cli;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.io.ExcelExporter;
import com.bentza.sna.io.GMLExporter;
import com.bentza.sna.io.GraphMLExporter;
import com.bentza.sna.io.GraphSONExporter;
import com.bentza.sna.io.PajekExporter;
import com.bentza.sna.net.AgnaLib;
import com.bentza.sna.net.FullNet;
import com.bentza.sna.net.Network;
import com.bentza.sna.net.NetworkLayouts;
import com.bentza.sna.net.NetworkRenderer;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * The Agna command-line surface (2.1.3):
 *   info, analyse, convert, transform, draw.
 * Everything runs against the engine (agna-core) - no desktop needed.
 */
public final class Cli
    {
    private final java.io.PrintStream out;

    public Cli(java.io.PrintStream out)
        {
        this.out = out;
        }

    public String help()
        {
        return "Agna CLI 2.1.3\n"
                + "usage: agna <command> [options] FILE\n\n"
                + "commands:\n"
                + "  info FILE                       network summary\n"
                + "  analyse FILE [--all|NAME...]    run analyses (basic, "
                + "density, cohesion, nodal, indegree, outdegree,\n"
                + "                                  emission, reception, "
                + "determination, status, geodesics, eccentricity,\n"
                + "                                  diameter, bavelas, "
                + "closeness, fareness, betweenness, prestige, cliques, full)\n"
                + "  convert IN OUT                  convert format "
                + "(agn/txt/csv/net/graphml/gml/graphson/xls)\n"
                + "  transform IN OUT --op OP       transpose | symmetrize-max "
                + "| symmetrize-sum | symmetrize-below |\n"
                + "                                  normalize-binary | "
                + "normalize-threshold:V | add-scalar:V |\n"
                + "                                  multiply-scalar:V | "
                + "square | merge:FILE:POLICY\n"
                + "  draw IN --out PNG --layout L   L = circular | random | "
                + "spring; --size WxH; --labels\n"
                + "  --help                          this text\n";
        }

    public static FullNet open(String file) throws Exception
        {
        FullNet full = new FullNet();
        String ext = extOf(file);
        if ("xls".equals(ext) || "xlsx".equals(ext))
            {
            full.readExcelFile(new File(file), ext);
            } else
            {
            byte[] bytes = Files.readAllBytes(new File(file).toPath());
            full.readNetwork(new String(bytes, StandardCharsets.ISO_8859_1),
                    ext);
            }
        return full;
        }

    public static String extOf(String file)
        {
        int i = file.lastIndexOf('.');
        return i > 0 && i < file.length() - 1 ? file.substring(i + 1)
                .toLowerCase() : "agn";
        }

    public static void write(FullNet full, String file) throws Exception
        {
        String ext = extOf(file);
        String text = null;
        if ("agn".equals(ext))
            {
            text = full.getAgna2TextNetwork();
            } else if ("txt".equals(ext))
            {
            text = full.getPlainTextNetwork(true);
            } else if ("csv".equals(ext))
            {
            text = full.getPlainTextNetwork(false);
            } else if ("net".equals(ext))
            {
            text = new PajekExporter().getPajekNetwork(full);
            } else if ("graphml".equals(ext))
            {
            text = new GraphMLExporter().getGraphML(full);
            } else if ("gml".equals(ext))
            {
            text = new GMLExporter().getGML(full);
            } else if ("graphson".equals(ext))
            {
            text = new GraphSONExporter().getGraphSON(full);
            }
        if (text != null)
            {
            Files.write(new File(file).toPath(), text.getBytes(
                    StandardCharsets.ISO_8859_1));
            } else if ("xls".equals(ext))
            {
            String error = new ExcelExporter().saveExcelNetwork(full, file);
            if (error != null)
                {
                throw new Exception(error);
                }
            } else
            {
            throw new Exception("unsupported output format: " + ext);
            }
        }

    public int run(String[] args) throws Exception
        {
        if (args.length == 0 || "--help".equals(args[0]))
            {
            out.print(help());
            return args.length == 0 ? 1 : 0;
            }
        String cmd = args[0];
        if ("info".equals(cmd))
            {
            return info(args);
            }
        if ("analyse".equals(cmd))
            {
            return analyse(args);
            }
        if ("convert".equals(cmd))
            {
            return convert(args);
            }
        if ("transform".equals(cmd))
            {
            return transform(args);
            }
        if ("draw".equals(cmd))
            {
            return draw(args);
            }
        out.println("unknown command: " + cmd);
        out.print(help());
        return 1;
        }

    private int info(String[] args) throws Exception
        {
        if (args.length < 2)
            {
            out.println("usage: agna info FILE");
            return 1;
            }
        FullNet full = open(args[1]);
        Network net = full.getNetwork();
        out.println("name:   " + net.getName());
        out.println("nodes:  " + net.getSize());
        out.println("edges:  " + net.getEdgesNumber());
        out.print(new AgnaLib().outBasic(net));
        return 0;
        }

    private int analyse(String[] args) throws Exception
        {
        if (args.length < 2)
            {
            out.println("usage: agna analyse FILE [--all|NAME...]");
            return 1;
            }
        FullNet full = open(args[1]);
        Network net = full.getNetwork();
        AgnaLib lib = new AgnaLib();
        boolean all = false;
        java.util.List<String> names = new java.util.ArrayList<>();
        for (int i = 2; i < args.length; i++)
            {
            if ("--all".equals(args[i]))
                {
                all = true;
                } else
                {
                names.add(args[i]);
                }
            }
        if (names.isEmpty())
            {
            all = true;
            }
        if (all)
            {
            names.addAll(java.util.Arrays.asList("basic", "density",
                    "cohesion", "nodal", "geodesics", "eccentricity",
                    "diameter", "bavelas", "closeness", "fareness",
                    "betweenness", "prestige", "full"));
            }
        for (String name : names)
            {
            String text = analysisText(lib, net, name);
            if (text == null)
                {
                out.println("unknown analysis: " + name);
                return 1;
                }
            out.print(text);
            }
        return 0;
        }

    private static String analysisText(AgnaLib lib, Network net, String name)
        {
        switch (name)
            {
            case "basic": return lib.outBasic(net);
            case "density": return lib.outDensity(net);
            case "cohesion": return lib.outCohesion(net);
            case "nodal": return lib.outNodalDegree(net);
            case "indegree": return lib.outInDegree(net);
            case "outdegree": return lib.outOutDegree(net);
            case "emission": return lib.outEmissionDegree(net);
            case "reception": return lib.outReceptionDegree(net);
            case "determination": return lib.outDeterminationDegree(net);
            case "status": return lib.outSociometricStatus(net);
            case "geodesics": return lib.outGeodesics(net);
            case "eccentricity": return lib.outEccentricity(net);
            case "diameter": return lib.outDiameter(net);
            case "bavelas": return lib.outBavelas(net);
            case "closeness": return lib.outCloseness(net);
            case "fareness": return lib.outFareness(net);
            case "betweenness": return lib.outBetweenness(net);
            case "prestige": return lib.outPrestige(net);
            case "cliques": return lib.outCliques(net, 2);
            case "full": return lib.outFullAnalysis(net);
            default: return null;
            }
        }

    private int convert(String[] args) throws Exception
        {
        if (args.length < 3)
            {
            out.println("usage: agna convert IN OUT");
            return 1;
            }
        FullNet full = open(args[1]);
        write(full, args[2]);
        out.println("converted " + args[1] + " -> " + args[2]);
        return 0;
        }

    private int transform(String[] args) throws Exception
        {
        if (args.length < 4)
            {
            out.println("usage: agna transform IN OUT --op OP");
            return 1;
            }
        String op = null;
        for (int i = 0; i < args.length; i++)
            {
            if ("--op".equals(args[i]) && i + 1 < args.length)
                {
                op = args[i + 1];
                }
            }
        if (op == null)
            {
            out.println("missing --op");
            return 1;
            }
        FullNet full = open(args[1]);
        Network net = full.getNetwork();
        AgnaLib lib = new AgnaLib();
        if ("transpose".equals(op))
            {
            lib.transpose(net);
            } else if ("symmetrize-max".equals(op))
            {
            lib.symmetrizeMaximum(net);
            } else if ("symmetrize-sum".equals(op))
            {
            lib.symmetrizeSum(net);
            } else if ("symmetrize-below".equals(op))
            {
            lib.symmetrizeBelow(net);
            } else if ("normalize-binary".equals(op))
            {
            lib.normalize(net, AgnaLib.NORMALIZE_BINARY);
            } else if (op.startsWith("normalize-threshold:"))
            {
            float t = Float.parseFloat(op.substring(op.indexOf(':') + 1));
            lib.normalize(net, AgnaLib.NORMALIZE_THRESHOLD, t);
            } else if (op.startsWith("add-scalar:"))
            {
            lib.addScalar(net, Float.parseFloat(op.substring(op.indexOf(':')
                    + 1)));
            } else if (op.startsWith("multiply-scalar:"))
            {
            lib.multiplyByScalar(net, Float.parseFloat(op.substring(op
                    .indexOf(':') + 1)));
            } else if ("square".equals(op))
            {
            net.setMatrix(lib.multiplyNetworks(net, net));
            } else if (op.startsWith("merge:"))
            {
            String[] parts = op.substring(6).split(":", 2);
            if (parts.length < 2)
                {
                out.println("merge requires FILE:POLICY");
                return 1;
                }
            int policy = "max".equals(parts[1]) ? Network.MERGE_MAX
                    : "keep".equals(parts[1]) ? Network.MERGE_KEEP_FIRST
                            : Network.MERGE_SUM;
            FullNet other = open(parts[0]);
            full.mergeWith(other.getNetwork(), policy);
            } else
            {
            out.println("unknown op: " + op);
            return 1;
            }
        write(full, args[2]);
        out.println("transformed -> " + args[2]);
        return 0;
        }

    private int draw(String[] args) throws Exception
        {
        String input = null;
        String output = null;
        int layout = NetworkLayouts.CIRCULAR;
        int width = 1200;
        int height = 900;
        boolean labels = false;
        for (int i = 1; i < args.length; i++)
            {
            String a = args[i];
            if ("--out".equals(a) && i + 1 < args.length)
                {
                output = args[++i];
                } else if ("--layout".equals(a) && i + 1 < args.length)
                {
                String l = args[++i];
                if ("random".equals(l))
                    {
                    layout = NetworkLayouts.RANDOM;
                    } else if ("spring".equals(l))
                    {
                    layout = NetworkLayouts.SPRING;
                    }
                } else if ("--size".equals(a) && i + 1 < args.length)
                {
                String[] wh = args[++i].toLowerCase().split("x");
                if (wh.length == 2)
                    {
                    width = Integer.parseInt(wh[0]);
                    height = Integer.parseInt(wh[1]);
                    }
                } else if ("--labels".equals(a))
                {
                labels = true;
                } else if (input == null)
                {
                input = a;
                }
            }
        if (input == null || output == null)
            {
            out.println("usage: agna draw IN --out PNG [--layout L] "
                    + "[--size WxH] [--labels]");
            return 1;
            }
        FullNet full = open(input);
        boolean ok = NetworkRenderer.renderToImage(full, new File(output),
                width, height, layout, labels);
        out.println(ok ? "drew " + output : "draw failed: " + output);
        return ok ? 0 : 1;
        }
    }