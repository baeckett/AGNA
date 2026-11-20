#!/usr/bin/env python3
# SPDX-License-Identifier: Apache-2.0
"""Agna vs NetworkX parity battery, Python side.

Reads the JSON Lines produced by AgnaMetricsDump (one document per network,
including the adjacency matrix exactly as Agna parsed it), recomputes every
index with NetworkX (or with NumPy on NetworkX shortest paths where
NetworkX has no direct equivalent), and compares.

Each check maps Agna's documented convention onto the NetworkX value
explicitly (e.g. Agna's raw betweenness sums over ordered pairs, i.e. it is
2x NetworkX's unnormalised undirected betweenness). Those mappings are
listed in the report so a reader can audit them.

Usage: python3 compare.py agna.jsonl REPORT.md [results.json]
Exit code: 0 if no FAIL, 1 otherwise.
"""
import json
import math
import sys
from collections import defaultdict

import networkx as nx
import numpy as np

REL = 1e-4   # Agna stores most per-node indices as float32
ABS = 1e-5

PASS, FAIL, SKIP, WARN = "PASS", "FAIL", "SKIP", "WARN"


def fnum(x):
    if isinstance(x, str):
        return {"NaN": math.nan, "Infinity": math.inf, "-Infinity": -math.inf}[x]
    return x


def to_arr(x):
    return np.array([fnum(v) for v in x], dtype=float)


class Battery:
    def __init__(self):
        self.rows = []          # (net, metric, status, err, note)
        self.conventions = {}   # metric -> mapping note

    def add(self, net, metric, status, err=None, note=""):
        self.rows.append((net, metric, status, err, note))

    def vec(self, net, metric, agna, ref, rel=REL, absl=ABS, note="", conv=None):
        if conv:
            self.conventions[metric] = conv
        if agna is None:
            self.add(net, metric, FAIL, None, "Agna returned null " + note)
            return
        if isinstance(agna, dict) and "error" in agna:
            self.add(net, metric, FAIL, None, "Agna error: " + agna["error"])
            return
        a = to_arr(agna) if isinstance(agna, (list, tuple)) else np.array([fnum(agna)], float)
        r = np.asarray(ref, dtype=float).reshape(a.shape) if np.ndim(ref) else np.array([ref], float)
        if a.shape != r.shape:
            self.add(net, metric, FAIL, None, f"shape {a.shape} vs {r.shape}")
            return
        if a.size == 1 and np.isnan(r).all() and (a == 0.0).all():
            # Regular graph / zero variance: Agna returns 0.0 (no bias), NetworkX returns NaN (0/0)
            self.add(net, metric, PASS, 0.0, "zero-variance regular graph: Agna returns 0.0, NX returns NaN")
            return
        both_nan = np.isnan(a) & np.isnan(r)
        diff = np.where(both_nan, 0.0, np.abs(a - r))
        tol = absl + rel * np.abs(r)
        bad = ~(diff <= tol)
        err = float(np.nanmax(diff)) if diff.size else 0.0
        if bad.any():
            i = int(np.argmax(np.where(bad, diff, -1)))
            self.add(net, metric, FAIL, err,
                     f"{int(bad.sum())}/{a.size} differ; e.g. idx {i}: Agna={a.flat[i]:.6g} NX={r.flat[i]:.6g} {note}")
        else:
            self.add(net, metric, PASS, err, note)

    def sets(self, net, metric, agna, ref, note="", conv=None):
        if conv:
            self.conventions[metric] = conv
        if isinstance(agna, dict) and "error" in agna:
            self.add(net, metric, FAIL, None, "Agna error: " + agna["error"])
            return
        a = sorted(sorted(map(int, c)) for c in agna) if agna and isinstance(agna[0], list) \
            else sorted(map(int, agna or []))
        r = sorted(sorted(map(int, c)) for c in ref) if ref and isinstance(next(iter(ref)), (set, list, frozenset, tuple)) \
            else sorted(map(int, ref))
        if a == r:
            self.add(net, metric, PASS, 0.0, note)
        else:
            self.add(net, metric, FAIL, None, f"Agna={a[:8]}... NX={r[:8]}... {note}")


# ----------------------------------------------------------------------
def build(doc):
    W = np.array([[fnum(x) for x in row] for row in doc["matrix"]], dtype=float)
    n = W.shape[0]
    sym = bool(doc["symmetric"])
    B = (W != 0).astype(float)
    Bnl = B.copy()
    np.fill_diagonal(Bnl, 0)
    Wnl = W.copy()
    np.fill_diagonal(Wnl, 0)
    D = nx.DiGraph()
    D.add_nodes_from(range(n))
    for i in range(n):
        for j in range(n):
            if i != j and W[i, j] != 0:
                D.add_edge(i, j, weight=W[i, j])
    # undirected binary view (symmetrised: tie if either direction)
    U = nx.Graph()
    U.add_nodes_from(range(n))
    U.add_edges_from((i, j) for i, j in D.edges())
    # graph used for distances: the directed one, or undirected if symmetric
    G = U if sym else D
    return W, B, Bnl, Wnl, D, U, G, n, sym


def dist_matrix(G, n):
    M = np.zeros((n, n))
    for s, dd in nx.all_pairs_shortest_path_length(G):
        for t, d in dd.items():
            M[s, t] = d
    return M


def check_network(bat, doc):
    net = doc["name"] or doc["file"]
    net = doc["file"].split("/")[-1].rsplit(".", 1)[0]
    W, B, Bnl, Wnl, D, U, G, n, sym = build(doc)
    loops = int(np.trace(B))
    tag = ("sym" if sym else "dir") + (f",{loops} loops" if loops else "")

    # ---------------- degree family ----------------
    bat.vec(net, "out_degree", doc["out_degree"], B.sum(1),
            conv="binary row count incl. diagonal == D.out_degree() with self-loops")
    bat.vec(net, "in_degree", doc["in_degree"], B.sum(0),
            conv="binary column count incl. diagonal == D.in_degree() with self-loops")
    nd = np.array([U.degree(i) for i in range(n)], float) if sym else \
        np.array([D.out_degree(i) + D.in_degree(i) for i in range(n)], float)
    bat.vec(net, "nodal_degree", doc["nodal_degree"], nd,
            conv="symmetric: nx degree; asymmetric: out+in (no loops)")
    bat.vec(net, "w_out_degree", doc["w_out_degree"], W.sum(1),
            conv="row sums incl. diagonal == out_degree(weight)")
    bat.vec(net, "w_in_degree", doc["w_in_degree"], W.sum(0))
    if n > 1:
        bat.vec(net, "density", doc["density"], B.sum() / (n * (n - 1)),
                conv="sum of binary ties / n(n-1); == nx.density when no self-loops")
        if loops == 0:
            bat.vec(net, "density_vs_nx.density", doc["density"], nx.density(G))
        bat.vec(net, "weighted_density", doc["weighted_density"], W.sum() / (n * (n - 1)))
        mutual = sum(1 for i, j in D.edges() if D.has_edge(j, i))
        bat.vec(net, "cohesion", doc["cohesion"], mutual / (n * (n - 1)),
                conv="share of ordered pairs in mutual dyads (reciprocated arcs / n(n-1))")

    # ---------------- distances ----------------
    Dm = dist_matrix(G, n)
    # 2.2.2: Agna's "connected" flag is weak connectivity (documented);
    # strong connectivity is checked separately by the analyses
    weak = nx.is_connected(U) if sym else nx.is_weakly_connected(D)
    connected = nx.is_connected(U) if sym else nx.is_strongly_connected(D)
    bat.vec(net, "connected", [1.0 if doc["connected"] else 0.0], [1.0 if weak else 0.0],
            conv="undirected: nx.is_connected; directed: nx.is_weakly_connected (Agna convention)")
    bat.vec(net, "geodesics", np.array(doc["geodesics"], float).ravel(), Dm.ravel(),
            conv="nx.all_pairs_shortest_path_length, 0 = unreachable")
    ecc_ref = Dm.max(1)
    if connected:
        e = nx.eccentricity(G)
        ecc_ref = np.array([e[i] for i in range(n)], float)
    bat.vec(net, "eccentricity", doc["eccentricity"], ecc_ref,
            conv="nx.eccentricity (connected); max reachable distance otherwise")
    far = Dm.sum(1)
    bat.vec(net, "fareness", doc["fareness"], far, conv="row sum of distances")
    # 2.2.2: Agna closeness uses the Wasserman-Faust correction, which is
    # exactly nx.closeness_centrality(wf_improved=True), on every network
    cc = nx.closeness_centrality(G.reverse() if not sym else G, wf_improved=True)
    bat.vec(net, "closeness*(n-1)", to_arr(doc["closeness"]) * (n - 1),
            [cc[i] for i in range(n)],
            conv="Agna closeness x(n-1) == nx.closeness_centrality(wf_improved=True) "
                 "(on G.reverse() for digraphs, since NX uses incoming distance)")
    bc = nx.betweenness_centrality(G, normalized=False)
    bref = np.array([bc[i] for i in range(n)]) * (2.0 if sym else 1.0)
    bat.vec(net, "betweenness", doc["betweenness"], bref,
            conv="raw Brandes over ordered pairs: undirected = 2 x nx unnormalised, directed = 1 x")
    # Lin proximity prestige from incoming NX distances
    pr = np.zeros(n)
    for i in range(n):
        col = Dm[:, i]
        reach = np.count_nonzero(col)
        if reach and n > 1:
            pr[i] = (reach / (n - 1)) * (reach / col.sum())
    bat.vec(net, "prestige(Lin)", doc["prestige"], pr,
            conv="Lin (1976) proximity prestige computed from NX incoming distances")
    tot = Dm.sum()
    bav = np.array([tot / (Dm[i].sum() + Dm[:, i].sum()) if (Dm[i].sum() + Dm[:, i].sum()) else 0
                    for i in range(n)])
    bat.vec(net, "bavelas", doc["bavelas"], bav,
            conv="sum(all d) / (sum d(i,.) + sum d(.,i)) from NX distances")

    # ---------------- centralization ----------------
    if n > 2:
        if sym:
            deg = np.array([U.degree(i) for i in range(n)], float)
            cd = (deg.max() - deg).sum() / ((n - 1) * (n - 2))
            bat.vec(net, "freeman_C_D", doc["freeman_degree_centralization"], cd,
                    conv="undirected: sum(max-d)/((n-1)(n-2))")
            bat.vec(net, "metric_degree_centralization", doc["metric_degree_centralization"], cd)
        else:
            od = np.array([D.out_degree(i) for i in range(n)], float)
            cd = (od.max() - od).sum() / ((n - 1) ** 2)
            bat.vec(net, "freeman_C_D(directed,out)", doc["freeman_degree_centralization"], cd,
                    conv="directed out-degree centralization: sum(max-d_out)/(n-1)^2 (Wasserman & Faust 1994, 5.3)")
        if connected:
            bn = nx.betweenness_centrality(G, normalized=True)
            bv = np.array([bn[i] for i in range(n)])
            cb = (bv.max() - bv).sum() / (n - 1)
            bat.vec(net, "freeman_C_B(report)", doc.get("report_betweenness_centralization"), cb,
                    conv="sum(max-b_norm)/(n-1) with nx normalised betweenness")
            bat.vec(net, "metric_betweenness_centralization", doc["metric_betweenness_centralization"], cb)
            cl = nx.closeness_centrality(G.reverse() if not sym else G)
            cv = np.array([cl[i] for i in range(n)])
            ccz = (cv.max() - cv).sum() / ((n - 1) * (n - 2) / (2 * n - 3))
            if sym:
                bat.vec(net, "freeman_C_C(report)", doc.get("report_closeness_centralization"), ccz,
                        conv="Freeman: sum(max-C')/[(n-1)(n-2)/(2n-3)], C' = nx closeness")
                bat.vec(net, "metric_closeness_centralization", doc["metric_closeness_centralization"], ccz)
    if n > 1:
        bat.vec(net, "metric_density", doc["metric_density"], nx.density(G) if loops == 0 else
                B.sum() / (n * (n - 1)))
    if D.number_of_edges():
        bat.vec(net, "metric_reciprocity", doc["metric_reciprocity"], nx.overall_reciprocity(D),
                conv="nx.overall_reciprocity")
    bat.vec(net, "metric_transitivity", doc["metric_transitivity"], nx.transitivity(U),
            conv="nx.transitivity on the symmetrised graph")
    pairs = Dm[Dm > 0]
    if pairs.size:
        bat.vec(net, "metric_average_distance", doc["metric_average_distance"], pairs.mean(),
                conv="mean over reachable ordered pairs (== nx.average_shortest_path_length when connected)")

    # ---------------- spectral ----------------
    A = Wnl + Wnl.T
    A = np.where((Wnl != 0) | (Wnl.T != 0), A, 0.0)
    vals, vecs = np.linalg.eigh(A)
    ev = np.abs(vecs[:, -1])
    gap = vals[-1] - vals[-2] if n > 1 else 1
    if ev.max() > 0:
        ev = ev / ev.max()
    if np.any(W < 0):
        bat.add(net, "eigenvector", SKIP, None, "negative tie values: Perron vector undefined")
    elif gap < 1e-6 * max(1, abs(vals[-1])):
        bat.add(net, "eigenvector", SKIP, None, f"leading eigenvalue degenerate (gap {gap:.2e}); vector not unique")
    else:
        bat.vec(net, "eigenvector", doc["eigenvector"], ev, rel=2e-3, absl=2e-3,
                conv="principal eigenvector of (W + W^T), max = 1 (== nx.eigenvector_centrality_numpy)")
    Abin = ((Bnl + Bnl.T) > 0).astype(float)
    lam = np.max(np.abs(np.linalg.eigvals(Abin))) if n else 0
    for beta in (0.10, 0.05):
        key = f"bonacich_{beta:.2f}"
        if beta * lam >= 1:
            rejected = not (doc.get(key) and not isinstance(doc[key], dict))
            bat.add(net, key, PASS if rejected else FAIL, None,
                    f"beta={beta} >= 1/lambda_max={1/lam:.4f}: the series diverges; "
                    + ("Agna correctly rejects beta (2.2.2)" if rejected
                       else "Agna returns a truncated value instead of rejecting beta"))
            continue
        ref = np.linalg.solve(np.eye(n) - beta * Abin, Abin @ np.ones(n))
        bat.vec(net, key, doc[key], ref, rel=1e-4, absl=1e-4,
                conv="(I - beta A)^-1 A 1 on symmetrised binary A (== (katz(alpha=beta,beta=1)-1)/beta)")
    if n > 1:
        prk = nx.pagerank(D, alpha=0.85, weight=None, tol=1e-12, max_iter=1000)
        bat.vec(net, "pagerank", doc["pagerank"], [prk[i] for i in range(n)], rel=1e-4, absl=1e-6,
                conv="nx.pagerank(weight=None, alpha=0.85), dangling mass spread uniformly")

    # ---------------- clustering / cores ----------------
    lc = doc["local_clustering"]
    if isinstance(lc, dict) and "values" in lc:
        cl = nx.clustering(U)
        bat.vec(net, "local_clustering", lc["values"], [cl[i] for i in range(n)],
                conv="nx.clustering on symmetrised binary graph")
        tri = nx.triangles(U)
        bat.vec(net, "triangles", lc["triangles"], [tri[i] for i in range(n)])
        bat.vec(net, "mean_clustering", lc["mean"], nx.average_clustering(U),
                conv="nx.average_clustering (count_zeros=True)")
    else:
        bat.add(net, "local_clustering", FAIL, None, str(lc))
    tr = doc["transitivity"]
    bat.vec(net, "transitivity", tr.get("value") if isinstance(tr, dict) else None, nx.transitivity(U))
    core = nx.core_number(U)
    bat.vec(net, "coreness", doc["coreness"], [core[i] for i in range(n)], conv="nx.core_number")

    # ---------------- census ----------------
    dc = doc["dyad_census"]
    M = sum(1 for i in range(n) for j in range(i + 1, n) if D.has_edge(i, j) and D.has_edge(j, i))
    Asy = sum(1 for i in range(n) for j in range(i + 1, n) if D.has_edge(i, j) != D.has_edge(j, i))
    bat.vec(net, "dyad_census", [dc["M"], dc["A"], dc["N"]], [M, Asy, n * (n - 1) // 2 - M - Asy],
            absl=0, rel=0, conv="MAN counts from NX DiGraph")
    if n >= 3 and n <= 250:
        tc = nx.triadic_census(D)
        codes = list(doc["triad_census"].keys())
        bat.vec(net, "triad_census", [doc["triad_census"][c] for c in codes], [tc[c] for c in codes],
                absl=0, rel=0, conv="nx.triadic_census (Holland-Leinhardt codes)")

    # ---------------- structural holes ----------------
    sh = doc["structural_holes"]
    if isinstance(sh, dict) and "error" not in sh:
        # For constraint: use weighted graph when weights are present
        is_weighted = bool(np.any((Wnl != 0) & (Wnl != 1)))
        Gw = nx.Graph()
        Gw.add_nodes_from(range(n))
        for i in range(n):
            for j in range(i + 1, n):
                w = abs(Wnl[i, j]) + abs(Wnl[j, i])
                if w > 0:
                    Gw.add_edge(i, j, weight=w)
        # 2.2.2: directed data use mutual weights z_ij + z_ji (Burt 1992;
        # == nx on the DiGraph). The binarised undirected view U is only
        # equivalent for symmetric binary data.
        use_mutual = is_weighted or not sym
        co = nx.constraint(Gw, weight="weight") if use_mutual else nx.constraint(U)

        # For effective size: on unweighted graphs, nx.effective_size matches Burt's formula (q != i, j).
        # On weighted graphs, nx's sparse matrix implementation includes the diagonal q=j, whereas
        # Burt (1992, eq 2.9) explicitly requires q != i, j.
        if use_mutual:
            Ws = Wnl + Wnl.T
            p = np.zeros((n, n))
            m = np.zeros((n, n))
            for i in range(n):
                s = Ws[i].sum()
                if s > 0: p[i] = Ws[i] / s
                mx = Ws[i].max()
                if mx > 0: m[i] = Ws[i] / mx
            es_arr = np.zeros(n)
            for i in range(n):
                for j in range(n):
                    if Ws[i, j] > 0:
                        red = sum(p[i, q] * m[j, q] for q in range(n) if q != i and q != j)
                        es_arr[i] += (1.0 - red)
            es_ref = es_arr
        else:
            es = nx.effective_size(U)
            es_ref = [es[i] if not math.isnan(es[i]) else 0.0 for i in range(n)]

        bat.vec(net, "effective_size", sh["effective_size"], es_ref,
                conv="Burt (1992, eq 2.9) effective size (q != i, j) (== nx.effective_size unweighted)")
        bat.vec(net, "constraint", sh["constraint"],
                [co[i] if not math.isnan(co[i]) else 0.0 for i in range(n)],
                conv="nx.constraint(weight='weight') on symmetrised graph (isolates -> 0)")
        deg_denom = [np.count_nonzero(Wnl[i] + Wnl[:, i]) for i in range(n)]
        eff = [es_ref[i] / deg_denom[i] if deg_denom[i] else 0.0 for i in range(n)]
        bat.vec(net, "efficiency", sh["efficiency"], eff, conv="effective_size / degree")
    else:
        bat.add(net, "structural_holes", FAIL, None, str(sh))

    # ---------------- connectivity ----------------
    cn = doc["connectivity"]
    if isinstance(cn, dict) and "error" not in cn:
        bat.sets(net, "weak_components", cn["weak"], [list(c) for c in nx.connected_components(U)],
                 conv="nx.(weakly_)connected_components")
        bat.sets(net, "strong_components", cn["strong"],
                 [list(c) for c in (nx.strongly_connected_components(D))],
                 conv="nx.strongly_connected_components")
        bat.sets(net, "articulation_points", cn["articulation"], list(nx.articulation_points(U)),
                 conv="nx.articulation_points on symmetrised graph")
        bat.sets(net, "bridges", [sorted(b) for b in cn["bridges"]], [sorted(b) for b in nx.bridges(U)],
                 conv="nx.bridges on symmetrised graph")
    else:
        bat.add(net, "connectivity", FAIL, None, str(cn))

    # ---------------- assortativity ----------------
    asr = doc["assortativity"]
    if isinstance(asr, dict) and "error" not in asr and U.number_of_edges() > 0:
        def nxr(f):
            try:
                with np.errstate(all="ignore"):
                    return f()
            except Exception:
                return math.nan
        if sym:
            bat.vec(net, "assortativity_total", asr["total"],
                    nxr(lambda: nx.degree_assortativity_coefficient(U)), rel=1e-3, absl=1e-4,
                    conv="nx.degree_assortativity_coefficient")
        else:
            Dpos = nx.DiGraph()
            Dpos.add_nodes_from(range(n))
            for i in range(n):
                for j in range(n):
                    if i != j and W[i, j] > 0:
                        Dpos.add_edge(i, j)
            for k, (x, y) in {"out_in": ("out", "in"), "in_in": ("in", "in"),
                              "out_out": ("out", "out"), "in_out": ("in", "out")}.items():
                bat.vec(net, f"assortativity_{k}", asr[k],
                        nxr(lambda: nx.degree_assortativity_coefficient(Dpos, x=x, y=y)), rel=1e-3, absl=1e-4,
                        conv="nx.degree_assortativity_coefficient(Dpos, x=, y=)")

    # ---------------- weighted ----------------
    wm = doc["weighted"]
    if isinstance(wm, dict) and "error" not in wm:
        Wpos = np.where(Wnl > 0, Wnl, 0.0)
        bat.vec(net, "in_strength", wm["in_strength"], Wpos.sum(0), conv="positive column sums excl. diagonal")
        bat.vec(net, "out_strength", wm["out_strength"], Wpos.sum(1))
        if n > 1:
            bat.vec(net, "valued_density", wm["valued_density"], Wpos.sum() / (n * (n - 1)))
        # Barrat et al. (2004) weighted clustering on the symmetrised graph
        Ws = (Wnl + Wnl.T) / np.where(((Wnl != 0) & (Wnl.T != 0)), 2.0, 1.0) if not sym else Wnl
        bc_ = np.zeros(n)
        for i in range(n):
            nb = list(U.neighbors(i))
            k = len(nb)
            if k < 2:
                continue
            s = sum(Ws[i, j] for j in nb)
            acc = 0.0
            for a_ in range(k):
                for b_ in range(k):
                    j, h = nb[a_], nb[b_]
                    if j != h and U.has_edge(j, h):
                        acc += (Ws[i, j] + Ws[i, h]) / 2
            bc_[i] = acc / (s * (k - 1))
        if sym:
            bat.vec(net, "barrat_w_clustering", wm["w_clustering"], bc_,
                    conv="Barrat et al. (2004) C_i^w from NX neighbourhoods (NX's own weighted "
                         "clustering is Onnela's geometric mean, a different index)")

    # ---------------- community ----------------
    for algo in ("louvain", "girvan_newman"):
        cr = doc.get(algo)
        if not isinstance(cr, dict) or "error" in cr:
            if cr is not None:
                bat.add(net, algo, FAIL, None, str(cr))
            continue
        if U.number_of_edges() == 0:
            continue
        groups = defaultdict(set)
        for i, c in enumerate(cr["assignments"]):
            groups[c].add(i)
        part = list(groups.values())
        Uw = nx.Graph()
        Uw.add_nodes_from(range(n))
        for i in range(n):
            for j in range(i + 1, n):
                w = max(abs(W[i, j]), abs(W[j, i]))
                if w > 0:
                    Uw.add_edge(i, j, weight=w)
        q_bin = nx.community.modularity(U, part)
        q_w = nx.community.modularity(Uw, part, weight="weight") if Uw.number_of_edges() else 0.0
        aq = fnum(cr["Q"])
        which = "weighted" if abs(aq - q_w) <= 1e-4 else ("binary" if abs(aq - q_bin) <= 1e-4 else None)
        if which:
            bat.add(net, f"{algo}_Q_of_own_partition", PASS, 0.0, f"matches nx.modularity ({which})")
        else:
            bat.add(net, f"{algo}_Q_of_own_partition", FAIL, abs(aq - q_bin),
                    f"Agna Q={aq:.5f}, nx.modularity binary={q_bin:.5f} weighted={q_w:.5f}")
        bat.conventions[f"{algo}_Q_of_own_partition"] = "nx.community.modularity of Agna's own partition"
        if algo == "louvain":
            best = max(nx.community.modularity(U, nx.community.louvain_communities(U, seed=s))
                       for s in range(10))
            st = PASS if aq >= best - 0.02 else (WARN if aq >= best - 0.05 else FAIL)
            bat.add(net, "louvain_Q_vs_nx_louvain", st, best - aq,
                    f"Agna Q={aq:.4f}, best of 10 nx.louvain_communities={best:.4f}")
            bat.conventions["louvain_Q_vs_nx_louvain"] = \
                "quality check: Agna Louvain Q within 0.02 of best nx Louvain (heuristic, not exact)"
    return tag


def main():
    src, rep = sys.argv[1], sys.argv[2]
    out_json = sys.argv[3] if len(sys.argv) > 3 else None
    bat = Battery()
    tags = {}
    for line in open(src, encoding="utf-8"):
        doc = json.loads(line)
        name = doc["file"].split("/")[-1].rsplit(".", 1)[0]
        if "error" in doc and "matrix" not in doc:
            bat.add(name, "load", FAIL, None, doc["error"])
            continue
        try:
            tags[name] = (doc["n"], check_network(bat, doc))
        except Exception as e:  # a crash in the comparator is itself a finding
            import traceback
            bat.add(name, "comparator", FAIL, None, traceback.format_exc(limit=2).replace("\n", " | "))

    counts = defaultdict(int)
    for r in bat.rows:
        counts[r[2]] += 1
    by_metric = defaultdict(lambda: defaultdict(int))
    for net, m, st, _, _ in bat.rows:
        by_metric[m][st] += 1

    with open(rep, "w", encoding="utf-8") as f:
        f.write("# Agna vs NetworkX parity battery\n\n")
        f.write(f"NetworkX {nx.__version__}, NumPy {np.__version__}. "
                f"Tolerance: |a-r| <= {ABS} + {REL}*|r| unless noted.\n\n")
        f.write(f"**{len(tags)} networks, {len(bat.rows)} checks: "
                + ", ".join(f"{k} {v}" for k, v in sorted(counts.items())) + "**\n\n")
        f.write("## Networks\n\n| Network | n | type |\n|---|---|---|\n")
        for k, (n, t) in tags.items():
            f.write(f"| {k} | {n} | {t} |\n")
        f.write("\n## Summary by index\n\n| Index | PASS | FAIL | WARN | SKIP | NetworkX reference / convention |\n"
                "|---|---|---|---|---|---|\n")
        for m, c in by_metric.items():
            f.write(f"| `{m}` | {c[PASS]} | {c[FAIL]} | {c[WARN]} | {c[SKIP]} | {bat.conventions.get(m, '')} |\n")
        f.write("\n## Non-passing checks\n\n| Network | Index | Status | max err | Detail |\n|---|---|---|---|---|\n")
        for net, m, st, err, note in bat.rows:
            if st != PASS:
                e = "" if err is None else f"{err:.3g}"
                f.write(f"| {net} | `{m}` | {st} | {e} | {note.replace('|', '/')} |\n")
    if out_json:
        json.dump([dict(zip(("network", "index", "status", "err", "note"), r)) for r in bat.rows],
                  open(out_json, "w"), indent=1)
    print(", ".join(f"{k} {v}" for k, v in sorted(counts.items())))
    return 1 if counts[FAIL] else 0


if __name__ == "__main__":
    sys.exit(main())
