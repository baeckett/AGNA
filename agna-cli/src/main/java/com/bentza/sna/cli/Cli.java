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
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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
                + "  info FILE                       network summary\n"
                + "  analyse FILE [--all|NAME...]    run analyses (basic, "
                + "density, cohesion, nodal, indegree, outdegree,\n"
                + "                                  emission, reception, "
                + "determination, status, geodesics, eccentricity,\n"
                + "                                  diameter, bavelas, "
                + "closeness, fareness, betweenness, prestige, cliques, full)\n"
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
                + "square | merge:FILE:POLICY |\n"
                + "                                  delete-node:N | "
                + "delete-nodes:N1,N2,.. | isolate:N |\n"
                + "                                  merge-nodes:N1,N2,.. | "
                + "remove-outsiders\n"
                + "  draw IN --out PNG --layout L   L = circular | random | "
                + "spring | grid | concentric; --size WxH; --labels\n"
                + "  generate --nodes N --out FILE  random (--seed S, "
                + "--degree D) | star | circular\n"
                + "  matrix FILE [--format csv|tsv] print the matrix\n"
                + "  nodes FILE                      node table (degrees + "
                + "coordinates)\n"
                + "  ego FILE --node NAME --out OUT  extract the 1-hop ego "
                + "network\n"
                + "  components FILE                 connected components\n"
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
                return 1;
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
            } else
            {
            out.println("unknown op: " + op);
            return 1;
            }
        write(full, args[2]);
        out.println("transformed -> " + args[2]);
        return 0;
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
        String sep = "\t";
        for (int i = 1; i < args.length; i++)
            {
            if ("--format".equals(args[i]) && i + 1 < args.length)
                {
                String f = args[i + 1];
                sep = "csv".equals(f) ? "," : "\t";
                i++;
                } else if (file == null)
                {
                file = args[i];
                }
            }
        if (file == null)
            {
            out.println("usage: agna matrix FILE [--format csv|tsv]");
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
        out.print(sb);
        return 0;
        }

    private int nodes(String[] args) throws Exception
        {
        if (args.length < 2)
            {
            out.println("usage: agna nodes FILE");
            return 1;
            }
        Network net = open(args[1]).getNetwork();
        out.println("index\tname\tout\tin\tx\ty");
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
            out.println(i + "\t" + net.getActor(i).getName() + "\t" + outDeg
                    + "\t" + inDeg + "\t" + fmt(net.getActor(i).getX())
                    + "\t" + fmt(net.getActor(i).getY()));
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
        if (args.length < 2)
            {
            out.println("usage: agna components FILE");
            return 1;
            }
        Network net = open(args[1]).getNetwork();
        int n = net.getSize();
        boolean[] seen = new boolean[n];
        int count = 0;
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
                }
            out.println("component " + count + " (" + comp.size()
                    + " nodes): " + sb);
            }
        out.println(n + " nodes, " + count + " component"
                + (count == 1 ? "" : "s"));
        return 0;
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