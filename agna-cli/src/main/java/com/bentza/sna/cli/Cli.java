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
import com.bentza.sna.net.NodeArea;
import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * The Agna command-line surface (2.1.3):
 *   info, analyse, convert, transform, draw, version, generate,
 *   matrix, nodes, ego, components.
 * Everything runs against the engine (agna-core) - no desktop needed.
 * A file argument of "-" reads from stdin (agn text by default, override
 * with --in-format); an output of "-" writes to stdout (text formats).
 */
public final class Cli
    {
    private final PrintStream out;
    private final InputStream in;
    private String stdinFormat = "agn";
    private String stdoutFormat = null;

    public Cli(PrintStream out)
        {
        this(out, System.in);
        }

    public Cli(PrintStream out, InputStream in)
        {
        this.out = out;
        this.in = in;
        }

    public String help()
        {
        return "Agna CLI 2.1.3\n"
                + "usage: agna <command> [options] FILE\n\n"
                + "commands:\n"
                + "  version                         print version\n"
                + "  info FILE [--out FILE]           network summary (incl. "
                + "connectivity and outsiders)\n"
                + "  analyse FILE [--all|NAME...|cliques:N] [--out FILE]\n"
                + "                                  run analyses (basic, "
                + "density, cohesion, nodal, indegree, outdegree,\n"
                + "                                  emission, reception, "
                + "determination, status, geodesics, eccentricity,\n"
                + "                                  diameter, bavelas, "
                + "closeness, fareness, betweenness, prestige, cliques,\n"
                + "                                  open-chain, full); --all "
                + "runs every metric; --out saves the report\n"
                + "  convert IN OUT [--in-format F] [--out-format F]\n"
                + "                                  convert format "
                + "(agn/txt/csv/net/graphml/gml/graphson/xls; xls needs a\n"
                + "                                  real file, the rest "
                + "accept '-' for stdout)"
                + "  transform IN OUT --op OP       transpose | symmetrize-max "
                + "| symmetrize-sum | symmetrize-below |\n"
                + "                                  normalize-binary | "
                + "normalize-threshold:V | add-scalar:V |\n"
                + "                                  multiply-scalar:V | "
+ "                                  square | merge:FILE:POLICY |\n"
                + "                                  delete-node:N | "
                + "delete-nodes:N1,N2,.. | isolate:N |\n"
                + "                                  merge-nodes:N1,N2,.. | "
                + "remove-outsiders | renumber | add-nodes:N | clone-node:N\n"
                + "  draw IN --out PNG --layout L   L = circular | random | "
                + "spring | grid | concentric; --size WxH; --labels;\n"
                + "                                  --background #rrggbb; "
                + "--no-faces; --edge-values; prints the used layout\n"
                + "                                  coordinates (index, "
                + "name, x, y) to stdout\n"
                + "  generate --nodes N --out FILE  random (--seed S, "
                + "--degree D) | star | circular\n"
                + "  matrix FILE [--format csv|tsv] [--out FILE]\n"
                + "                                  print the matrix\n"
                + "  nodes FILE [--out FILE]         node table (degrees + "
                + "coordinates)\n"
                + "  ego FILE --node NAME --out OUT  extract the 1-hop ego "
                + "network\n"
                + "  components FILE [--out CSV]      connected components\n"
                + "  metrics FILE [METRIC...] [--all] [--format csv|json] [--out FILE]\n"
                + "                                  structured metrics; "
                + "pick by name (density, diameter, eccentricity,\n"
                + "                                  closeness, betweenness, "
                + "indegree, outdegree, total-degree, emission,\n"
                + "                                  reception, status, "
                + "determination, geodesics) or --all (default)\n"
                + "  distance FILE --from A --to B [--out CSV]\n"
                + "                                  shortest path between "
                + "two nodes\n"
                + "  diff A B [--out FILE.csv]       structural comparison\n"
                + "network commands (each saves a new file via --out):\n"
                + "  add-scalar FILE V --out OUT     add V to every cell\n"
                + "  multiply-scalar FILE V --out OUT\n"
                + "  transpose FILE --out OUT        transpose the matrix\n"
                + "  symmetrize FILE [--mode M] --out OUT   M = below|sum|max\n"
                + "  normalize FILE [--binary|--threshold V] --out OUT\n"
                + "  square FILE --out OUT           matrix square\n"
                + "  merge-networks A B [--mode M] --out OUT  M = sum|max|keep\n"
                + "  remove-outsiders FILE --out OUT\n"
                + "  delete-nodes FILE N1,N2 --out OUT\n"
                + "  add-nodes FILE --count N --out OUT\n"
                + "  renumber FILE --out OUT         name nodes 1..n\n"
                + "  from-chain FILE --out OUT        create a network from a "
                + "chain file\n"
                + "  layout FILE --layout L --out OUT\n"
                + "                                  apply a layout "
                + "(circular|random|spring|grid|concentric|star) and save\n"
                + "                                  the new coordinates "
                + "without rendering\n"
                + "  set FILE --out OUT [--flag VALUE ...]\n"
                + "                                  edit viewer attributes "
                + "(Image/Edge menu properties, default face);\n"
                + "                                  run 'agna set' with no "
                + "flags for the full flag list\n"
                + "  --help                          this text\n"
                + "use '-' as IN/OUT for stdin/stdout (stdin defaults to "
                + "agn text)\n";
        }

    public FullNet open(String file) throws Exception
        {
        FullNet full = new FullNet();
        String ext = extOf(file);
        if ("-".equals(file))
            {
            byte[] bytes = in.readAllBytes();
            full.readNetwork(new String(bytes, StandardCharsets.ISO_8859_1),
                    stdinFormat);
            return full;
            }
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

    public void write(FullNet full, String file) throws Exception
        {
        String ext = stdoutFormat != null ? stdoutFormat : extOf(file);
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
        if ("-".equals(file))
            {
            if (text == null)
                {
                throw new Exception("stdout output supports text formats "
                        + "(not xls)");
                }
            out.print(text);
            return;
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
        if ("version".equals(cmd) || "--version".equals(cmd))
            {
            return version();
            }
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
        if ("generate".equals(cmd))
            {
            return generate(args);
            }
        if ("matrix".equals(cmd))
            {
            return matrix(args);
            }
        if ("nodes".equals(cmd))
            {
            return nodes(args);
            }
        if ("ego".equals(cmd))
            {
            return ego(args);
            }
        if ("components".equals(cmd))
            {
            return components(args);
            }
        if ("metrics".equals(cmd))
            {
            return metrics(args);
            }
        if ("distance".equals(cmd))
            {
            return distance(args);
            }
        if ("diff".equals(cmd))
            {
            return diff(args);
            }
        if ("add-scalar".equals(cmd))
            {
            return scalarNetOp(args, "add-scalar");
            }
        if ("multiply-scalar".equals(cmd))
            {
            return scalarNetOp(args, "multiply-scalar");
            }
        if ("transpose".equals(cmd))
            {
            return simpleNetOp(args, "transpose");
            }
        if ("symmetrize".equals(cmd))
            {
            return symmetrize(args);
            }
        if ("normalize".equals(cmd))
            {
            return normalize(args);
            }
        if ("square".equals(cmd))
            {
            return simpleNetOp(args, "square");
            }
        if ("merge-networks".equals(cmd))
            {
            return mergeNetworks(args);
            }
        if ("remove-outsiders".equals(cmd))
            {
            return simpleNetOp(args, "remove-outsiders");
            }
        if ("delete-nodes".equals(cmd))
            {
            return deleteNodesCommand(args);
            }
        if ("add-nodes".equals(cmd))
            {
            return addNodes(args);
            }
        if ("renumber".equals(cmd))
            {
            return simpleNetOp(args, "renumber");
            }
        if ("from-chain".equals(cmd))
            {
            return fromChain(args);
            }
        if ("layout".equals(cmd))
            {
            return layoutCommand(args);
            }
        if ("set".equals(cmd))
            {
            return set(args);
            }
        out.println("unknown command: " + cmd);
        out.print(help());
        return 1;
        }

    private int version()
        {
        out.println("Agna CLI 2.1.3");
        return 0;
        }

    private int info(String[] args) throws Exception
        {
        String file = null;
        String outFile = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null)
            {
            out.println("usage: agna info FILE [--out FILE]");
            return 1;
            }
        FullNet full = open(file);
        Network net = full.getNetwork();
        StringBuilder sb = new StringBuilder();
        sb.append("name:   ").append(net.getName()).append('\n');
        sb.append("nodes:  ").append(net.getSize()).append('\n');
        sb.append("edges:  ").append(net.getEdgesNumber()).append('\n');
        sb.append(new AgnaLib().outBasic(net));
        if (outFile != null)
            {
            Files.write(new File(outFile).toPath(), sb.toString().getBytes(
                    StandardCharsets.UTF_8));
            out.println("wrote summary to " + outFile);
            } else
            {
            out.print(sb);
            }
        return 0;
        }

    private int analyse(String[] args) throws Exception
        {
        if (args.length < 2)
            {
            out.println("usage: agna analyse FILE [--all|NAME...] "
                    + "[--out FILE]");
            return 1;
            }
        FullNet full = open(args[1]);
        Network net = full.getNetwork();
        AgnaLib lib = new AgnaLib();
        boolean all = false;
        String outFile = null;
        java.util.List<String> names = new java.util.ArrayList<>();
        for (int i = 2; i < args.length; i++)
            {
            if ("--all".equals(args[i]))
                {
                all = true;
                } else if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
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
            // every metric the engine offers
            names.addAll(java.util.Arrays.asList("basic", "density",
                    "cohesion", "nodal", "indegree", "outdegree", "emission",
                    "reception", "determination", "status", "geodesics",
                    "open-chain", "eccentricity", "diameter", "bavelas",
                    "closeness", "fareness", "betweenness", "prestige",
                    "cliques", "full"));
            }
        StringBuilder report = new StringBuilder();
        for (String name : names)
            {
            String text;
            if (name.startsWith("cliques:"))
                {
                text = lib.outCliques(net, Integer.parseInt(name.substring(
                        name.indexOf(':') + 1)));
                } else
                {
                text = analysisText(lib, net, name);
                }
            if (text == null)
                {
                out.println("unknown analysis: " + name);
                return 1;
                }
            report.append(text);
            report.append('\n');
            }
        if (outFile != null)
            {
            Files.write(new File(outFile).toPath(), report.toString()
                    .getBytes(StandardCharsets.UTF_8));
            out.println("wrote analysis report to " + outFile);
            } else
            {
            out.print(report);
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
            case "open-chain": return lib.outOpenChainSummary(net);
            case "full": return lib.outFullAnalysis(net);
            default: return null;
            }
        }

    private int convert(String[] args) throws Exception
        {
        if (args.length < 3)
            {
            out.println("usage: agna convert IN OUT [--in-format F]");
            return 1;
            }
        stdinFormat = "agn";
        stdoutFormat = null;
        for (int i = 3; i < args.length; i++)
            {
            if ("--in-format".equals(args[i]) && i + 1 < args.length)
                {
                stdinFormat = args[i + 1];
                }
            if ("--out-format".equals(args[i]) && i + 1 < args.length)
                {
                stdoutFormat = args[i + 1];
                }
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
        stdinFormat = "agn";
        stdoutFormat = null;
        for (int i = 0; i < args.length; i++)
            {
            if ("--op".equals(args[i]) && i + 1 < args.length)
                {
                op = args[i + 1];
                }
            if ("--in-format".equals(args[i]) && i + 1 < args.length)
                {
                stdinFormat = args[i + 1];
                }
            if ("--out-format".equals(args[i]) && i + 1 < args.length)
                {
                stdoutFormat = args[i + 1];
                }
            }
        if (op == null)
            {
            out.println("missing --op");
            return 1;
            }
        FullNet full = open(args[1]);
        if (!applyOp(full, op))
            {
            return 1;
            }
        write(full, args[2]);
        out.println("transformed -> " + args[2]);
        return 0;
        }

    // shared engine-op runner: transform and the dedicated network
    // commands (add-scalar, transpose, ...) all go through here
    private boolean applyOp(FullNet full, String op) throws Exception
        {
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
                return false;
                }
            int policy = "max".equals(parts[1]) ? Network.MERGE_MAX
                    : "keep".equals(parts[1]) ? Network.MERGE_KEEP_FIRST
                            : Network.MERGE_SUM;
            FullNet other = open(parts[0]);
            full.mergeWith(other.getNetwork(), policy);
            } else if (op.startsWith("delete-node:"))
            {
            deleteNodes(net, op.substring(op.indexOf(':') + 1));
            } else if (op.startsWith("delete-nodes:"))
            {
            deleteNodes(net, op.substring(op.indexOf(':') + 1));
            } else if (op.startsWith("isolate:"))
            {
            int i = findActor(net, op.substring(op.indexOf(':') + 1));
            if (i < 0)
                {
                out.println("no such node: " + op.substring(op.indexOf(':')
                        + 1));
                return false;
                }
            for (int j = 0; j < net.getSize(); j++)
                {
                net.setValue(0f, i, j);
                net.setValue(0f, j, i);
                }
            } else if (op.startsWith("merge-nodes:"))
            {
            mergeNodes(net, op.substring(op.indexOf(':') + 1).split(","));
            } else if ("remove-outsiders".equals(op))
            {
            for (int i = net.getSize() - 1; i >= 0; i--)
                {
                if (net.isOutsider(i))
                    {
                    net.deleteActor(i);
                    }
                }
            } else if ("renumber".equals(op))
            {
            for (int i = 0; i < net.getSize(); i++)
                {
                net.getActor(i).setName(String.valueOf(i + 1));
                }
            } else if (op.startsWith("add-nodes:"))
            {
            net.addActors(Integer.parseInt(op.substring(op.indexOf(':')
                    + 1)), false);
            } else if (op.startsWith("clone-node:"))
            {
            // matrix-based clone: copies incoming and outgoing ties
            String cname = op.substring(op.indexOf(':') + 1);
            int src = findActor(net, cname);
            if (src < 0)
                {
                out.println("no such node: " + cname);
                return false;
                }
            net.addActor(-1, -1, 400, true);
            int nw = net.getSize() - 1;
            net.getActor(nw).setName("Clone of "
                    + net.getActor(src).getName());
            net.getActor(nw).setFace(net.getActor(src).getFaceSource());
            for (int j = 0; j < nw; j++)
                {
                net.setValue(net.getValue(src, j), nw, j);
                net.setValue(net.getValue(j, src), j, nw);
                }
            net.setValue(0f, nw, src);
            net.setValue(0f, src, nw);
            } else
            {
            out.println("unknown op: " + op);
            return false;
            }
        return true;
        }

    private static void deleteNodes(Network net, String list)
        {
        String[] names = list.split(",");
        List<Integer> indices = new ArrayList<>();
        for (String name : names)
            {
            int i = findActor(net, name.trim());
            if (i >= 0)
                {
                indices.add(i);
                }
            }
        indices.sort(java.util.Comparator.reverseOrder());
        for (int i : indices)
            {
            net.deleteActor(i);
            }
        }

    // merge the listed actors into the first: ties are summed, the other
    // actors are removed (their own outgoing ties are folded in too)
    private static void mergeNodes(Network net, String[] names)
        {
        int target = findActor(net, names[0].trim());
        if (target < 0)
            {
            return;
            }
        List<Integer> others = new ArrayList<>();
        for (int k = 1; k < names.length; k++)
            {
            int i = findActor(net, names[k].trim());
            if (i >= 0 && i != target && !others.contains(i))
                {
                others.add(i);
                }
            }
        for (int i : others)
            {
            for (int j = 0; j < net.getSize(); j++)
                {
                float outVal = net.getValue(i, j);
                float inVal = net.getValue(j, i);
                if (outVal != 0f)
                    {
                    net.setValue(net.getValue(target, j) + outVal, target, j);
                    }
                if (inVal != 0f)
                    {
                    net.setValue(net.getValue(j, target) + inVal, j, target);
                    }
                }
            }
        others.sort(java.util.Comparator.reverseOrder());
        for (int i : others)
            {
            net.deleteActor(i);
            }
        }

    private static int findActor(Network net, String name)
        {
        for (int i = 0; i < net.getSize(); i++)
            {
            if (net.getActor(i).getName().equalsIgnoreCase(name))
                {
                return i;
                }
            }
        return -1;
        }

    private int draw(String[] args) throws Exception
        {
        String input = null;
        String output = null;
        int layout = NetworkLayouts.CIRCULAR;
        int width = 1200;
        int height = 900;
        boolean labels = false;
        Color background = null;
        boolean noFaces = false;
        boolean edgeValues = false;
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
                    } else if ("grid".equals(l))
                    {
                    layout = NetworkLayouts.GRID;
                    } else if ("concentric".equals(l))
                    {
                    layout = NetworkLayouts.CONCENTRIC;
                    } else if (!"circular".equals(l))
                    {
                    out.println("unknown layout: " + l);
                    return 1;
                    }
                } else if ("--size".equals(a) && i + 1 < args.length)
                {
                String[] wh = args[++i].toLowerCase().split("x");
                if (wh.length == 2)
                    {
                    width = Integer.parseInt(wh[0]);
                    height = Integer.parseInt(wh[1]);
                    }
                } else if ("--background".equals(a) && i + 1 < args.length)
                {
                background = colorOf(args[++i]);
                } else if ("--labels".equals(a))
                {
                labels = true;
                } else if ("--no-faces".equals(a))
                {
                noFaces = true;
                } else if ("--edge-values".equals(a))
                {
                edgeValues = true;
                } else if (input == null)
                {
                input = a;
                }
            }
        if (input == null || output == null)
            {
            out.println("usage: agna draw IN --out PNG [--layout L] "
                    + "[--size WxH] [--labels] [--background #rrggbb] "
                    + "[--no-faces] [--edge-values]");
            return 1;
            }
        FullNet full = open(input);
        NetworkRenderer.RenderOptions opts = new NetworkRenderer.RenderOptions();
        opts.labels = labels;
        opts.background = background;
        opts.facesVisible = noFaces ? Boolean.FALSE : null;
        opts.edgeValues = edgeValues ? Boolean.TRUE : null;
        boolean ok = NetworkRenderer.renderToImage(full, new File(output),
                width, height, layout, opts);
        if (!ok)
            {
            out.println("draw failed: " + output);
            return 1;
            }
        out.println("drew " + output + " (layout " + layoutName(layout)
                + ", " + width + "x" + height + ")");
        // the coordinates the render used, for scripts to reuse
        Network net = full.getNetwork();
        out.println("index\tname\tx\ty");
        for (int i = 0; i < net.getSize(); i++)
            {
            out.println(i + "\t" + net.getActor(i).getName() + "\t"
                    + fmt(net.getActor(i).getX()) + "\t"
                    + fmt(net.getActor(i).getY()));
            }
        return 0;
        }

    private static String layoutName(int layout)
        {
        if (layout == NetworkLayouts.RANDOM)
            {
            return "random";
            }
        if (layout == NetworkLayouts.SPRING)
            {
            return "spring";
            }
        if (layout == NetworkLayouts.GRID)
            {
            return "grid";
            }
        if (layout == NetworkLayouts.CONCENTRIC)
            {
            return "concentric";
            }
        return "circular";
        }

    private int generate(String[] args) throws Exception
        {
        String outFile = null;
        String type = "random";
        int nodes = 10;
        long seed = 20260910L;
        int degree = 2;
        for (int i = 1; i < args.length; i++)
            {
            String a = args[i];
            if ("--nodes".equals(a) && i + 1 < args.length)
                {
                nodes = Integer.parseInt(args[++i]);
                } else if ("--out".equals(a) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--type".equals(a) && i + 1 < args.length)
                {
                type = args[++i];
                } else if ("--seed".equals(a) && i + 1 < args.length)
                {
                seed = Long.parseLong(args[++i]);
                } else if ("--degree".equals(a) && i + 1 < args.length)
                {
                degree = Integer.parseInt(args[++i]);
                }
            }
        if (outFile == null)
            {
            out.println("usage: agna generate --nodes N --out FILE "
                    + "[--type random|star|circular] [--seed S] [--degree D]");
            return 1;
            }
        if (nodes < 1)
            {
            out.println("--nodes must be >= 1");
            return 1;
            }
        FullNet full = new FullNet();
        if ("star".equals(type))
            {
            Network net = new Network(nodes);
            net.setName("Star Network");
            for (int j = 1; j < nodes; j++)
                {
                net.setValue(1f, 0, j);
                net.setValue(1f, j, 0);
                }
            NetworkLayouts.apply(net, NetworkLayouts.STAR, 800, 600);
            full.setNetwork(net);
            } else if ("circular".equals(type))
            {
            Network net = new Network(nodes);
            net.setName("Circular Network");
            NetworkLayouts.apply(net, NetworkLayouts.CIRCULAR, 800, 600);
            full.setNetwork(net);
            } else
            {
            // random: seeded Erdos-Renyi, reproducible with --seed
            Network net = new Network(nodes);
            net.setName("Random Network");
            Random rnd = new Random(seed);
            for (int i = 0; i < nodes; i++)
                {
                for (int j = i + 1; j < nodes; j++)
                    {
                    if (rnd.nextDouble() < (double) degree
                            / Math.max(1, nodes - 1))
                        {
                        net.setValue(1f, i, j);
                        net.setValue(1f, j, i);
                        }
                    }
                }
            NetworkLayouts.apply(net, NetworkLayouts.SPRING, 800, 600);
            full.setNetwork(net);
            }
        write(full, outFile);
        out.println("generated " + type + " network with " + nodes
                + " nodes -> " + outFile);
        return 0;
        }

    private int matrix(String[] args) throws Exception
        {
        String file = null;
        String outFile = null;
        String sep = "\t";
        for (int i = 1; i < args.length; i++)
            {
            if ("--format".equals(args[i]) && i + 1 < args.length)
                {
                String f = args[i + 1];
                sep = "csv".equals(f) ? "," : "\t";
                i++;
                } else if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null)
            {
            out.println("usage: agna matrix FILE [--format csv|tsv] "
                    + "[--out FILE]");
            return 1;
            }
        Network net = open(file).getNetwork();
        int n = net.getSize();
        StringBuilder sb = new StringBuilder(128 + 8 * n * n);
        for (int j = 0; j < n; j++)
            {
            if (j > 0)
                {
                sb.append(sep);
                }
            sb.append(net.getActor(j).getName());
            }
        sb.append('\n');
        for (int i = 0; i < n; i++)
            {
            sb.append(net.getActor(i).getName());
            for (int j = 0; j < n; j++)
                {
                sb.append(sep).append(fmt(net.getValue(i, j)));
                }
            sb.append('\n');
            }
        if (outFile != null)
            {
            Files.write(new File(outFile).toPath(), sb.toString().getBytes(
                    StandardCharsets.UTF_8));
            out.println("wrote matrix to " + outFile);
            } else
            {
            out.print(sb);
            }
        return 0;
        }

    private int nodes(String[] args) throws Exception
        {
        String file = null;
        String outFile = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null)
            {
            out.println("usage: agna nodes FILE [--out FILE]");
            return 1;
            }
        Network net = open(file).getNetwork();
        StringBuilder sb = new StringBuilder();
        sb.append("index\tname\tout\tin\tx\ty\n");
        for (int i = 0; i < net.getSize(); i++)
            {
            int outDeg = 0;
            int inDeg = 0;
            for (int j = 0; j < net.getSize(); j++)
                {
                if (net.getValue(i, j) != 0f)
                    {
                    outDeg++;
                    }
                if (net.getValue(j, i) != 0f)
                    {
                    inDeg++;
                    }
                }
            sb.append(i).append('\t').append(net.getActor(i).getName())
                    .append('\t').append(outDeg).append('\t').append(inDeg)
                    .append('\t').append(fmt(net.getActor(i).getX()))
                    .append('\t').append(fmt(net.getActor(i).getY()))
                    .append('\n');
            }
        if (outFile != null)
            {
            Files.write(new File(outFile).toPath(), sb.toString().getBytes(
                    StandardCharsets.UTF_8));
            out.println("wrote node table to " + outFile);
            } else
            {
            out.print(sb);
            }
        return 0;
        }

    private int ego(String[] args) throws Exception
        {
        String file = null;
        String name = null;
        String outFile = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--node".equals(args[i]) && i + 1 < args.length)
                {
                name = args[++i];
                } else if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null || name == null || outFile == null)
            {
            out.println("usage: agna ego FILE --node NAME --out OUT");
            return 1;
            }
        Network net = open(file).getNetwork();
        int egoIdx = findActor(net, name);
        if (egoIdx < 0)
            {
            out.println("no such node: " + name);
            return 1;
            }
        List<Integer> neighbors = new ArrayList<>();
        for (int j = 0; j < net.getSize(); j++)
            {
            if (j != egoIdx && (net.getValue(egoIdx, j) != 0f
                    || net.getValue(j, egoIdx) != 0f))
                {
                neighbors.add(j);
                }
            }
        int k = neighbors.size() + 1;
        Network ego = new Network(k);
        ego.setName(name + " ego (" + (k - 1) + " neighbours)");
        ego.getActor(0).setName(net.getActor(egoIdx).getName());
        for (int i = 0; i < neighbors.size(); i++)
            {
            int idx = neighbors.get(i);
            ego.getActor(i + 1).setName(net.getActor(idx).getName());
            for (int j = 0; j <= i; j++)
                {
                int jdx = neighbors.get(j);
                float v = net.getValue(idx, jdx);
                if (v != 0f)
                    {
                    ego.setValue(v, i + 1, j + 1);
                    ego.setValue(v, j + 1, i + 1);
                    }
                }
            float toEgo = net.getValue(idx, egoIdx);
            if (toEgo != 0f)
                {
                ego.setValue(toEgo, i + 1, 0);
                ego.setValue(toEgo, 0, i + 1);
                }
            }
        NetworkLayouts.apply(ego, NetworkLayouts.SPRING, 800, 600);
        FullNet full = new FullNet();
        full.setNetwork(ego);
        write(full, outFile);
        out.println("extracted " + k + "-node ego network -> " + outFile);
        return 0;
        }

    private int components(String[] args) throws Exception
        {
        String file = null;
        String outFile = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null)
            {
            out.println("usage: agna components FILE [--out FILE.csv]");
            return 1;
            }
        Network net = open(file).getNetwork();
        int n = net.getSize();
        boolean[] seen = new boolean[n];
        int count = 0;
        List<String> csvRows = new ArrayList<>();
        for (int s = 0; s < n; s++)
            {
            if (seen[s])
                {
                continue;
                }
            count++;
            List<Integer> comp = new ArrayList<>();
            List<Integer> queue = new ArrayList<>();
            queue.add(s);
            seen[s] = true;
            while (!queue.isEmpty())
                {
                int u = queue.remove(queue.size() - 1);
                comp.add(u);
                for (int v = 0; v < n; v++)
                    {
                    if (!seen[v] && (net.getValue(u, v) != 0f
                            || net.getValue(v, u) != 0f))
                        {
                        seen[v] = true;
                        queue.add(v);
                        }
                    }
                }
            StringBuilder sb = new StringBuilder();
            for (int idx : comp)
                {
                if (sb.length() > 0)
                    {
                    sb.append(", ");
                    }
                sb.append(net.getActor(idx).getName());
                csvRows.add(count + "," + quoteCsv(net.getActor(idx)
                        .getName()));
                }
            out.println("component " + count + " (" + comp.size()
                    + " nodes): " + sb);
            }
        out.println(n + " nodes, " + count + " component"
                + (count == 1 ? "" : "s"));
        if (outFile != null)
            {
            List<String> rows = new ArrayList<>();
            rows.add("component,node");
            rows.addAll(csvRows);
            Files.write(new File(outFile).toPath(), String.join("\n", rows)
                    .getBytes(StandardCharsets.UTF_8));
            out.println("wrote components to " + outFile);
            }
        return 0;
        }

    private int metrics(String[] args) throws Exception
        {
        String file = null;
        String outFile = null;
        String format = "csv";
        boolean all = false;
        java.util.Set<String> wanted = new java.util.LinkedHashSet<>();
        java.util.Set<String> known = java.util.Set.of("density",
                "diameter", "eccentricity", "closeness", "betweenness",
                "indegree", "outdegree", "total-degree", "emission",
                "reception", "status", "determination", "geodesics");
        for (int i = 1; i < args.length; i++)
            {
            String a = args[i];
            if ("--out".equals(a) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--format".equals(a) && i + 1 < args.length)
                {
                format = args[++i];
                } else if ("--all".equals(a))
                {
                all = true;
                } else if (file == null)
                {
                file = a;
                } else if (known.contains(a))
                {
                wanted.add(a);
                } else
                {
                out.println("unknown metric: " + a + " (run 'agna --help' "
                        + "for the metric list)");
                return 1;
                }
            }
        if (file == null)
            {
            out.println("usage: agna metrics FILE [METRIC...] [--all] "
                    + "[--format csv|json] [--out FILE]");
            return 1;
            }
        if (all || wanted.isEmpty())
            {
            wanted = null; // every metric
            }
        Network net = open(file).getNetwork();
        boolean wantsDistance = wanted == null || wanted.contains("diameter")
                || wanted.contains("eccentricity")
                || wanted.contains("closeness")
                || wanted.contains("betweenness");
        if (wantsDistance && !CliMetrics.isConnected(net))
            {
            out.println("note: the network is disconnected; the "
                    + "distance-based measures (diameter, eccentricity, "
                    + "closeness, betweenness) are omitted");
            }
        String text = "json".equals(format) ? CliMetrics.json(net, wanted)
                : String.join("\n", CliMetrics.csv(net, wanted)) + "\n";
        if (outFile != null)
            {
            Files.write(new File(outFile).toPath(), text.getBytes(
                    StandardCharsets.UTF_8));
            out.println("wrote metrics to " + outFile);
            } else
            {
            out.print(text);
            }
        return 0;
        }

    private int distance(String[] args) throws Exception
        {
        String file = null;
        String from = null;
        String to = null;
        String outFile = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--from".equals(args[i]) && i + 1 < args.length)
                {
                from = args[++i];
                } else if ("--to".equals(args[i]) && i + 1 < args.length)
                {
                to = args[++i];
                } else if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null || from == null || to == null)
            {
            out.println("usage: agna distance FILE --from NAME --to NAME "
                    + "[--out FILE.csv]");
            return 1;
            }
        Network net = open(file).getNetwork();
        int f = findActor(net, from);
        int t = findActor(net, to);
        if (f < 0 || t < 0)
            {
            out.println("no such node: " + (f < 0 ? from : to));
            return 1;
            }
        if (outFile != null)
            {
            String[] hopsAndPath = shortestPath(net, f, t);
            String row = from + "," + to + "," + hopsAndPath[0] + ","
                    + hopsAndPath[1];
            Files.write(new File(outFile).toPath(), ("from,to,hops,path\n"
                    + row).getBytes(StandardCharsets.UTF_8));
            out.println("wrote distance to " + outFile);
            return 0;
            }
        out.print(new AgnaLib().outShortestPaths(net, f, t));
        return 0;
        }

    // unweighted hop count and one shortest path (names); "0" = no path
    private static String[] shortestPath(Network net, int from, int to)
        {
        int n = net.getSize();
        int[] dist = new int[n];
        int[] prev = new int[n];
        java.util.Arrays.fill(dist, -1);
        java.util.Arrays.fill(prev, -1);
        dist[from] = 0;
        List<Integer> queue = new ArrayList<>();
        queue.add(from);
        while (!queue.isEmpty())
            {
            int v = queue.remove(queue.size() - 1);
            for (int w = 0; w < n; w++)
                {
                if (dist[w] == -1 && net.getValue(v, w) != 0f)
                    {
                    dist[w] = dist[v] + 1;
                    prev[w] = v;
                    queue.add(w);
                    }
                }
            }
        if (dist[to] < 0)
            {
            return new String[] { "0", "-" };
            }
        StringBuilder path = new StringBuilder();
        for (int v = to; v != -1; v = prev[v])
            {
            if (path.length() > 0)
                {
                path.insert(0, " > ");
                }
            path.insert(0, net.getActor(v).getName());
            }
        return new String[] { String.valueOf(dist[to]), path.toString() };
        }

    private int diff(String[] args) throws Exception
        {
        if (args.length < 3)
            {
            out.println("usage: agna diff A B [--out FILE.csv]");
            return 1;
            }
        String outFile = null;
        for (int i = 3; i < args.length; i++)
            {
            if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[i + 1];
                }
            }
        Network na = open(args[1]).getNetwork();
        Network nb = open(args[2]).getNetwork();
        java.util.Set<String> namesA = new java.util.LinkedHashSet<>();
        java.util.Set<String> namesB = new java.util.LinkedHashSet<>();
        for (int i = 0; i < na.getSize(); i++)
            {
            namesA.add(na.getActor(i).getName());
            }
        for (int i = 0; i < nb.getSize(); i++)
            {
            namesB.add(nb.getActor(i).getName());
            }
        List<String> rows = new ArrayList<>();
        rows.add("kind,node1,node2,valueA,valueB");
        int added = 0;
        int removed = 0;
        for (String name : namesA)
            {
            if (!namesB.contains(name))
                {
                rows.add("node-removed," + quoteCsv(name) + ",,,");
                removed++;
                }
            }
        for (String name : namesB)
            {
            if (!namesA.contains(name))
                {
                rows.add("node-added," + quoteCsv(name) + ",,,");
                added++;
                }
            }
        List<String> shared = new ArrayList<>();
        for (String name : namesA)
            {
            if (namesB.contains(name))
                {
                shared.add(name);
                }
            }
        int edgeDiffs = 0;
        for (int i = 0; i < shared.size(); i++)
            {
            int ia = findActor(na, shared.get(i));
            int ib = findActor(nb, shared.get(i));
            for (int j = 0; j < shared.size(); j++)
                {
                int ja = findActor(na, shared.get(j));
                int jb = findActor(nb, shared.get(j));
                float va = na.getValue(ia, ja);
                float vb = nb.getValue(ib, jb);
                if (va != vb)
                    {
                    rows.add("edge-diff," + quoteCsv(shared.get(i)) + ","
                            + quoteCsv(shared.get(j)) + ","
                            + String.format(Locale.ROOT, "%.4f", va) + ","
                            + String.format(Locale.ROOT, "%.4f", vb));
                    edgeDiffs++;
                    }
                }
            }
        String summary = added + " node(s) added, " + removed
                + " node(s) removed, " + edgeDiffs
                + " edge value difference(s)";
        if (outFile != null)
            {
            Files.write(new File(outFile).toPath(), String.join("\n", rows)
                    .getBytes(StandardCharsets.UTF_8));
            out.println(summary);
            out.println("wrote diff to " + outFile);
            } else
            {
            out.println(summary);
            out.println(String.join("\n", rows));
            }
        return 0;
        }

    private static String quoteCsv(String s)
        {
        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0)
            {
            return '"' + s.replace("\"", "\"\"") + '"';
            }
        return s;
        }

    // ---- dedicated network-operation commands (mirror the desktop's
    // Network menu); every op ends up in the shared applyOp runner ----

    private int simpleNetOp(String[] args, String op) throws Exception
        {
        String file = null;
        String outFile = null;
        stdoutFormat = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--out-format".equals(args[i]) && i + 1 < args.length)
                {
                stdoutFormat = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null || outFile == null)
            {
            out.println("usage: agna " + args[0] + " FILE --out OUT "
                    + "[--out-format F]");
            return 1;
            }
        FullNet full = open(file);
        if (!applyOp(full, op))
            {
            return 1;
            }
        write(full, outFile);
        out.println(args[0] + " -> " + outFile);
        return 0;
        }

    private int scalarNetOp(String[] args, String op) throws Exception
        {
        if (args.length < 4)
            {
            out.println("usage: agna " + args[0] + " FILE VALUE --out OUT");
            return 1;
            }
        stdoutFormat = null;
        float v;
        try
            {
            v = Float.parseFloat(args[2]);
            } catch (NumberFormatException e)
            {
            out.println("invalid number: " + args[2]);
            return 1;
            }
        String outFile = scanOut(args, 3);
        if (outFile == null)
            {
            out.println("usage: agna " + args[0] + " FILE VALUE --out OUT");
            return 1;
            }
        FullNet full = open(args[1]);
        if (!applyOp(full, op + ":" + v))
            {
            return 1;
            }
        write(full, outFile);
        out.println(args[0] + " -> " + outFile);
        return 0;
        }

    private int symmetrize(String[] args) throws Exception
        {
        String mode = "below";
        String file = null;
        String outFile = null;
        stdoutFormat = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--mode".equals(args[i]) && i + 1 < args.length)
                {
                mode = args[++i];
                } else if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--out-format".equals(args[i]) && i + 1 < args.length)
                {
                stdoutFormat = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null || outFile == null)
            {
            out.println("usage: agna symmetrize FILE [--mode below|sum|max] "
                    + "--out OUT");
            return 1;
            }
        FullNet full = open(file);
        if (!applyOp(full, "symmetrize-" + mode))
            {
            return 1;
            }
        write(full, outFile);
        out.println("symmetrize -> " + outFile);
        return 0;
        }

    private int normalize(String[] args) throws Exception
        {
        String op = "normalize-binary";
        String file = null;
        String outFile = null;
        stdoutFormat = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--threshold".equals(args[i]) && i + 1 < args.length)
                {
                op = "normalize-threshold:" + args[++i];
                } else if ("--binary".equals(args[i]))
                {
                op = "normalize-binary";
                } else if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--out-format".equals(args[i]) && i + 1 < args.length)
                {
                stdoutFormat = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null || outFile == null)
            {
            out.println("usage: agna normalize FILE [--binary | "
                    + "--threshold V] --out OUT");
            return 1;
            }
        FullNet full = open(file);
        if (!applyOp(full, op))
            {
            return 1;
            }
        write(full, outFile);
        out.println("normalize -> " + outFile);
        return 0;
        }

    private int mergeNetworks(String[] args) throws Exception
        {
        if (args.length < 3)
            {
            out.println("usage: agna merge-networks A B [--mode sum|max|keep] "
                    + "--out OUT");
            return 1;
            }
        String mode = "sum";
        String outFile = null;
        stdoutFormat = null;
        for (int i = 3; i < args.length; i++)
            {
            if ("--mode".equals(args[i]) && i + 1 < args.length)
                {
                mode = args[++i];
                } else if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--out-format".equals(args[i]) && i + 1 < args.length)
                {
                stdoutFormat = args[++i];
                }
            }
        if (outFile == null)
            {
            out.println("usage: agna merge-networks A B [--mode sum|max|keep] "
                    + "--out OUT");
            return 1;
            }
        FullNet full = open(args[1]);
        if (!applyOp(full, "merge:" + args[2] + ":" + mode))
            {
            return 1;
            }
        write(full, outFile);
        out.println("merge-networks -> " + outFile);
        return 0;
        }

    private int deleteNodesCommand(String[] args) throws Exception
        {
        if (args.length < 3)
            {
            out.println("usage: agna delete-nodes FILE N1,N2,... --out OUT");
            return 1;
            }
        stdoutFormat = null;
        String outFile = scanOut(args, 3);
        if (outFile == null)
            {
            out.println("usage: agna delete-nodes FILE N1,N2,... --out OUT");
            return 1;
            }
        FullNet full = open(args[1]);
        if (!applyOp(full, "delete-nodes:" + args[2]))
            {
            return 1;
            }
        write(full, outFile);
        out.println("delete-nodes -> " + outFile);
        return 0;
        }

    private int addNodes(String[] args) throws Exception
        {
        String file = null;
        String outFile = null;
        int count = 0;
        boolean hasCount = false;
        stdoutFormat = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--count".equals(args[i]) && i + 1 < args.length)
                {
                count = Integer.parseInt(args[++i]);
                hasCount = true;
                } else if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--out-format".equals(args[i]) && i + 1 < args.length)
                {
                stdoutFormat = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null || outFile == null || !hasCount)
            {
            out.println("usage: agna add-nodes FILE --count N --out OUT");
            return 1;
            }
        FullNet full = open(file);
        if (!applyOp(full, "add-nodes:" + count))
            {
            return 1;
            }
        write(full, outFile);
        out.println("add-nodes -> " + outFile);
        return 0;
        }

    private String scanOut(String[] args, int from)
        {
        for (int i = from; i < args.length; i++)
            {
            if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                return args[i + 1];
                }
            if ("--out-format".equals(args[i]) && i + 1 < args.length)
                {
                stdoutFormat = args[i + 1];
                }
            }
        return null;
        }

    // create a network from a chain file (whitespace-separated
    // sequences; arcs follow the transitions), mirroring the desktop's
    // "Create a network from a chain file"
    private int fromChain(String[] args) throws Exception
        {
        String file = null;
        String outFile = null;
        stdoutFormat = null;
        for (int i = 1; i < args.length; i++)
            {
            if ("--out".equals(args[i]) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--out-format".equals(args[i]) && i + 1 < args.length)
                {
                stdoutFormat = args[++i];
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null || outFile == null)
            {
            out.println("usage: agna from-chain FILE --out OUT");
            return 1;
            }
        FullNet full = new FullNet();
        byte[] bytes = Files.readAllBytes(new File(file).toPath());
        full.readNetworkFromChain(new String(bytes,
                StandardCharsets.ISO_8859_1), extOf(file));
        write(full, outFile);
        out.println("created network from chain -> " + outFile);
        return 0;
        }

    // apply a layout to an existing network and save the new coordinates
    // (no image is rendered)
    private int layoutCommand(String[] args) throws Exception
        {
        String file = null;
        String outFile = null;
        String layoutName = "spring";
        int width = 1200;
        int height = 900;
        stdoutFormat = null;
        for (int i = 1; i < args.length; i++)
            {
            String a = args[i];
            if ("--layout".equals(a) && i + 1 < args.length)
                {
                layoutName = args[++i];
                } else if ("--size".equals(a) && i + 1 < args.length)
                {
                String[] wh = args[++i].toLowerCase().split("x");
                if (wh.length == 2)
                    {
                    width = Integer.parseInt(wh[0]);
                    height = Integer.parseInt(wh[1]);
                    }
                } else if ("--out".equals(a) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--out-format".equals(a) && i + 1 < args.length)
                {
                stdoutFormat = args[++i];
                } else if (file == null)
                {
                file = a;
                }
            }
        if (file == null || outFile == null)
            {
            out.println("usage: agna layout FILE --layout L --out OUT "
                    + "[--size WxH]");
            return 1;
            }
        int layoutInt;
        if ("circular".equals(layoutName))
            {
            layoutInt = NetworkLayouts.CIRCULAR;
            } else if ("random".equals(layoutName))
            {
            layoutInt = NetworkLayouts.RANDOM;
            } else if ("spring".equals(layoutName))
            {
            layoutInt = NetworkLayouts.SPRING;
            } else if ("grid".equals(layoutName))
            {
            layoutInt = NetworkLayouts.GRID;
            } else if ("concentric".equals(layoutName))
            {
            layoutInt = NetworkLayouts.CONCENTRIC;
            } else if ("star".equals(layoutName))
            {
            layoutInt = NetworkLayouts.STAR;
            } else
            {
            out.println("unknown layout: " + layoutName);
            return 1;
            }
        FullNet full = open(file);
        NetworkLayouts.apply(full.getNetwork(), layoutInt, width, height);
        write(full, outFile);
        out.println("layout (" + layoutName + ") -> " + outFile);
        return 0;
        }

    // edit the viewer attributes of an existing network file (the
    // Network Viewer's Image and Edge menu properties) and save a new
    // file. Every flag maps onto a NodeArea property that the agn
    // format persists; defaults come from the file itself.
    private int set(String[] args) throws Exception
        {
        String file = null;
        String outFile = null;
        stdoutFormat = null;
        java.util.Map<String, String> opts = new java.util.LinkedHashMap<>();
        for (int i = 1; i < args.length; i++)
            {
            String a = args[i];
            if (a.startsWith("--") && i + 1 < args.length
                    && !"--out-format".equals(a) && !"--out".equals(a))
                {
                opts.put(a.substring(2), args[++i]);
                } else if ("--out".equals(a) && i + 1 < args.length)
                {
                outFile = args[++i];
                } else if ("--out-format".equals(a) && i + 1 < args.length)
                {
                stdoutFormat = args[++i];
                } else if (file == null)
                {
                file = a;
                }
            }
        if (file == null || outFile == null)
            {
            out.println("usage: agna set FILE --out OUT [--flag VALUE ...]");
            out.println("flags: --name T --names-visible on|off --names-x N "
                    + "--names-y N --title-visible on|off --title-x N "
                    + "--title-y N");
            out.println("       --grid-visible on|off --grid-step N "
                    + "--separator N --grid-transparency N");
            out.println("       --max-transparency N --edge-value-visible "
                    + "on|off --edge-value-position N --edge-value-color "
                    + "#rrggbb --edge-color #rrggbb --names-color #rrggbb "
                    + "--grid-color #rrggbb --title-color #rrggbb "
                    + "--background-color #rrggbb");
            out.println("       --background-image FILE "
                    + "--background-image-x N --background-image-y N "
                    + "--background-image-width N --background-image-height N");
            out.println("       --faces-visible on|off "
                    + "--allow-edge-selection on|off --color-fidelity on|off "
                    + "--snap-to-grid on|off --default-face FILE");
            return 1;
            }
        FullNet full = open(file);
        NodeArea area = full.getArea();
        java.util.Map<String, String> F = opts;
        if (F.containsKey("name"))
            {
            full.getNetwork().setName(F.get("name"));
            }
        if (F.containsKey("names-visible"))
            {
            area.setPrintNames(onOff(F.get("names-visible")));
            }
        if (F.containsKey("names-x"))
            {
            area.setNamesX(intOf(F.get("names-x")));
            }
        if (F.containsKey("names-y"))
            {
            area.setNamesY(intOf(F.get("names-y")));
            }
        if (F.containsKey("title-visible"))
            {
            area.setTitleVisible(onOff(F.get("title-visible")));
            }
        if (F.containsKey("title-x"))
            {
            area.setTitleX(intOf(F.get("title-x")));
            }
        if (F.containsKey("title-y"))
            {
            area.setTitleY(intOf(F.get("title-y")));
            }
        if (F.containsKey("grid-visible"))
            {
            area.setGridEnabled(onOff(F.get("grid-visible")));
            }
        if (F.containsKey("grid-step"))
            {
            area.setGridSpace(intOf(F.get("grid-step")));
            }
        if (F.containsKey("separator"))
            {
            area.setSeparator(intOf(F.get("separator")));
            }
        if (F.containsKey("grid-transparency"))
            {
            area.setGridTransparency(intOf(F.get("grid-transparency")));
            }
        if (F.containsKey("max-transparency"))
            {
            area.setMaxTransparency(intOf(F.get("max-transparency")));
            }
        if (F.containsKey("edge-value-visible"))
            {
            area.setEdgeValueVisible(onOff(F.get("edge-value-visible")));
            }
        if (F.containsKey("edge-value-position"))
            {
            area.setEdgeValuePosition(intOf(F.get("edge-value-position")));
            }
        if (F.containsKey("edge-value-color"))
            {
            area.setEdgeValueColor(colorOf(F.get("edge-value-color")));
            }
        if (F.containsKey("edge-color"))
            {
            area.setArrowColor(colorOf(F.get("edge-color")));
            }
        if (F.containsKey("names-color"))
            {
            area.setNamesColor(colorOf(F.get("names-color")));
            }
        if (F.containsKey("grid-color"))
            {
            area.setGridColor(colorOf(F.get("grid-color")));
            }
        if (F.containsKey("title-color"))
            {
            area.setTitleColor(colorOf(F.get("title-color")));
            }
        if (F.containsKey("background-color"))
            {
            area.setBackgroundColor(colorOf(F.get("background-color")));
            }
        if (F.containsKey("background-image"))
            {
            area.setBackgroundImage(F.get("background-image"));
            }
        if (F.containsKey("background-image-x"))
            {
            area.setBackgroundImageX(intOf(F.get("background-image-x")));
            }
        if (F.containsKey("background-image-y"))
            {
            area.setBackgroundImageY(intOf(F.get("background-image-y")));
            }
        if (F.containsKey("background-image-width"))
            {
            area.setBackgroundImageWidth(intOf(F.get(
                    "background-image-width")));
            }
        if (F.containsKey("background-image-height"))
            {
            area.setBackgroundImageHeight(intOf(F.get(
                    "background-image-height")));
            }
        if (F.containsKey("faces-visible"))
            {
            area.setFacesVisible(onOff(F.get("faces-visible")));
            }
        if (F.containsKey("allow-edge-selection"))
            {
            area.setAllowES(onOff(F.get("allow-edge-selection")));
            }
        if (F.containsKey("color-fidelity"))
            {
            area.setColorFidelity(onOff(F.get("color-fidelity")));
            }
        if (F.containsKey("snap-to-grid"))
            {
            area.setSTGEnabled(onOff(F.get("snap-to-grid")));
            }
        if (F.containsKey("default-face"))
            {
            String face = F.get("default-face");
            for (int i = 0; i < full.getNetwork().getSize(); i++)
                {
                full.getNetwork().getActor(i).setFace(face);
                }
            }
        write(full, outFile);
        out.println("attributes set -> " + outFile);
        return 0;
        }

    private static boolean onOff(String v)
        {
        return v.equalsIgnoreCase("on") || v.equalsIgnoreCase("yes")
                || v.equalsIgnoreCase("true");
        }

    private static int intOf(String v)
        {
        return Integer.parseInt(v.trim());
        }

    private static Color colorOf(String v) throws Exception
        {
        String h = v.startsWith("#") ? v.substring(1) : v;
        if (h.length() != 6)
            {
            throw new Exception("expected a #rrggbb color, got: " + v);
            }
        return new Color(Integer.parseInt(h, 16));
        }

    // compact number formatting: 1.0 -> 1, 0.25 -> 0.25
    private static String fmt(float v)
        {
        if (v == (long) v)
            {
            return String.valueOf((long) v);
            }
        String s = String.valueOf(v);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
        }
    }