#!/usr/bin/env python3
# SPDX-License-Identifier: Apache-2.0
"""Generate a deterministic corpus of test networks for the Agna/NetworkX
parity battery. Each network is written as an Agna Data File (.agn) so it
is parsed by Agna's own reader exactly as a user's file would be.

Usage: python3 generate_networks.py OUTDIR
"""
import os
import random
import sys

import networkx as nx


def write_agn(path, name, G, weight="weight"):
    nodes = list(G.nodes())
    idx = {v: i for i, v in enumerate(nodes)}
    n = len(nodes)
    M = [[0.0] * n for _ in range(n)]
    directed = G.is_directed()
    for u, v, d in G.edges(data=True):
        w = float(d.get(weight, 1.0)) if weight else 1.0
        M[idx[u]][idx[v]] = w
        if not directed:
            M[idx[v]][idx[u]] = w
    labels = [str(v).replace("\t", " ").strip() or f"n{i}" for i, v in enumerate(nodes)]
    # Agna requires unique labels
    seen = {}
    for i, l in enumerate(labels):
        if l in seen:
            labels[i] = f"{l}_{i}"
        seen[labels[i]] = True
    with open(path, "w", encoding="utf-8", newline="\r\n") as f:
        f.write("Agna Data File\nVersion\t2.1.6\n")
        f.write(f"Network Name\t{name}\nNetwork Size\t{n}\nHas Viewer\tno\n")
        f.write("Node Names\n" + "\t".join(labels) + "\t\nEnd Node Names\n")
        f.write("Network Matrix\n")
        for row in M:
            f.write("\t".join(repr(x) for x in row) + "\n")
        f.write("End Network Matrix\n")


def corpus():
    rnd = random.Random(20261113)
    nets = {}

    # ---- undirected, binary -------------------------------------------
    nets["U_path8"] = nx.path_graph(8)
    nets["U_star11"] = nx.star_graph(10)
    nets["U_cycle9"] = nx.cycle_graph(9)
    nets["U_complete6"] = nx.complete_graph(6)
    nets["U_wheel8"] = nx.wheel_graph(8)
    nets["U_barbell5_2"] = nx.barbell_graph(5, 2)
    nets["U_lollipop5_4"] = nx.lollipop_graph(5, 4)
    nets["U_grid5x5"] = nx.convert_node_labels_to_integers(nx.grid_2d_graph(5, 5))
    nets["U_petersen"] = nx.petersen_graph()
    nets["U_bipartite_K3_4"] = nx.complete_bipartite_graph(3, 4)
    nets["U_tree30"] = nx.random_labeled_tree(30, seed=7) if hasattr(nx, "random_labeled_tree") \
        else nx.random_tree(30, seed=7)
    nets["U_karate"] = nx.karate_club_graph()
    for u, v in nets["U_karate"].edges():
        nets["U_karate"][u][v].pop("weight", None)
    nets["U_florentine"] = nx.florentine_families_graph()
    nets["U_er40"] = nx.gnp_random_graph(40, 0.12, seed=11)
    nets["U_ba60"] = nx.barabasi_albert_graph(60, 2, seed=12)
    nets["U_ws30"] = nx.connected_watts_strogatz_graph(30, 4, 0.2, seed=13)
    nets["U_caveman4x5"] = nx.connected_caveman_graph(4, 5)
    # disconnected: two components + isolates
    g = nx.disjoint_union(nx.cycle_graph(5), nx.path_graph(4))
    g.add_nodes_from([100, 101])
    nets["U_disconnected"] = g

    # ---- undirected, weighted -----------------------------------------
    lm = nx.les_miserables_graph()
    nets["UW_lesmis"] = lm
    g = nx.connected_watts_strogatz_graph(25, 4, 0.3, seed=21)
    for u, v in g.edges():
        g[u][v]["weight"] = rnd.choice([0.5, 1.0, 2.0, 3.0, 5.0])
    nets["UW_ws25"] = g
    dw = nx.bipartite.weighted_projected_graph(
        nx.davis_southern_women_graph(),
        [n for n, d in nx.davis_southern_women_graph().nodes(data=True) if d["bipartite"] == 0])
    nets["UW_davis_women"] = dw

    # ---- directed, binary ---------------------------------------------
    nets["D_cycle7"] = nx.cycle_graph(7, create_using=nx.DiGraph)
    nets["D_er30"] = nx.gnp_random_graph(30, 0.12, seed=31, directed=True)
    sf = nx.DiGraph(nx.scale_free_graph(50, seed=32))
    sf.remove_edges_from(nx.selfloop_edges(sf))
    nets["D_scalefree50"] = sf
    t = nx.DiGraph()
    t.add_nodes_from(range(9))
    for i in range(9):
        for j in range(i + 1, 9):
            t.add_edge(i, j) if rnd.random() < 0.5 else t.add_edge(j, i)
    nets["D_tournament9"] = t
    dag = nx.DiGraph()
    dag.add_nodes_from(range(20))
    for i in range(20):
        for j in range(i + 1, 20):
            if rnd.random() < 0.15:
                dag.add_edge(i, j)
    nets["D_dag20"] = dag
    # high-reciprocity directed network with a few dangling nodes
    r = nx.DiGraph()
    r.add_nodes_from(range(25))
    for i in range(25):
        for j in range(25):
            if i != j and rnd.random() < 0.08:
                r.add_edge(i, j)
                if rnd.random() < 0.6:
                    r.add_edge(j, i)
    nets["D_reciprocal25"] = r
    nets["D_outstar8"] = nx.DiGraph([(0, i) for i in range(1, 8)])

    # ---- directed, weighted -------------------------------------------
    g = nx.gnp_random_graph(20, 0.2, seed=41, directed=True)
    for u, v in g.edges():
        g[u][v]["weight"] = rnd.choice([1.0, 2.0, 3.0, 4.0])
    nets["DW_er20"] = g
    return nets


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else "networks"
    os.makedirs(out, exist_ok=True)
    for name, G in corpus().items():
        weight = "weight" if name.startswith(("UW_", "DW_")) else None
        write_agn(os.path.join(out, name + ".agn"), name, G, weight)
    print(f"wrote {len(corpus())} networks to {out}")


if __name__ == "__main__":
    main()
