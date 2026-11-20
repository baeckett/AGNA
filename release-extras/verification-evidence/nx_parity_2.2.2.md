# Agna vs NetworkX parity battery

NetworkX 3.6.1, NumPy 2.4.6. Tolerance: |a-r| <= 1e-05 + 0.0001*|r| unless noted.

**65 networks, 3360 checks: PASS 3353, SKIP 2, WARN 5**

## Networks

| Network | n | type |
|---|---|---|
| D_cycle40 | 40 | dir |
| D_dag150 | 150 | dir |
| D_gnr200 | 200 | dir |
| D_instar25 | 25 | dir |
| D_path30 | 30 | dir |
| D_reciprocal120 | 120 | dir |
| D_scalefree300 | 300 | dir |
| D_tournament12 | 12 | dir |
| R_ba300_m1 | 300 | sym |
| R_ba300_m3 | 300 | sym |
| R_er60_dense | 60 | sym |
| R_geometric200 | 200 | sym |
| R_holmekim300 | 300 | sym |
| R_regular3_100 | 100 | sym |
| R_sbm4x50 | 200 | sym |
| R_union_with_isolates | 26 | sym |
| R_ws200_b0.05 | 200 | sym |
| R_ws200_b0.0 | 200 | sym |
| R_ws200_b0.3 | 200 | sym |
| R_ws200_b1.0 | 200 | sym |
| S_barbell10_5 | 25 | sym |
| S_bintree_d6 | 127 | sym |
| S_caveman6x6 | 36 | sym |
| S_circladder25 | 50 | sym |
| S_complete10 | 10 | sym |
| S_complete3 | 3 | sym |
| S_complete40 | 40 | sym |
| S_cycle10 | 10 | sym |
| S_cycle200 | 200 | sym |
| S_cycle3 | 3 | sym |
| S_dodecahedral | 20 | sym |
| S_empty10 | 10 | sym |
| S_frucht | 12 | sym |
| S_grid10x10 | 100 | sym |
| S_grid6x6x6 | 216 | sym |
| S_heawood | 14 | sym |
| S_hypercube6 | 64 | sym |
| S_ladder30 | 60 | sym |
| S_lollipop8_20 | 28 | sym |
| S_path2 | 2 | sym |
| S_path50 | 50 | sym |
| S_path5 | 5 | sym |
| S_petersen | 10 | sym |
| S_relaxed_caveman | 48 | sym |
| S_ring_of_cliques8x5 | 40 | sym |
| S_star30 | 30 | sym |
| S_star4 | 4 | sym |
| S_torus12x12 | 144 | sym |
| S_turan20_4 | 20 | sym |
| S_tutte | 46 | sym |
| S_wheel20 | 20 | sym |
| S_windmill5x4 | 16 | sym |
| WD_er80 | 80 | dir |
| W_ws150 | 150 | sym |
| REAL_7th_graders | 29 | dir |
| REAL_adjnoun | 112 | sym |
| REAL_celegansneural | 297 | dir |
| REAL_contiguous_usa | 49 | sym |
| REAL_dolphins | 62 | sym |
| REAL_football | 115 | sym |
| REAL_jazz_collab | 198 | sym |
| REAL_lesmis | 77 | sym |
| REAL_polbooks | 105 | sym |
| REAL_train_terrorists | 64 | sym |
| REAL_windsurfers | 43 | sym |

## Summary by index

| Index | PASS | FAIL | WARN | SKIP | NetworkX reference / convention |
|---|---|---|---|---|---|
| `out_degree` | 65 | 0 | 0 | 0 | binary row count incl. diagonal == D.out_degree() with self-loops |
| `in_degree` | 65 | 0 | 0 | 0 | binary column count incl. diagonal == D.in_degree() with self-loops |
| `nodal_degree` | 65 | 0 | 0 | 0 | symmetric: nx degree; asymmetric: out+in (no loops) |
| `w_out_degree` | 65 | 0 | 0 | 0 | row sums incl. diagonal == out_degree(weight) |
| `w_in_degree` | 65 | 0 | 0 | 0 |  |
| `density` | 65 | 0 | 0 | 0 | sum of binary ties / n(n-1); == nx.density when no self-loops |
| `density_vs_nx.density` | 65 | 0 | 0 | 0 |  |
| `weighted_density` | 65 | 0 | 0 | 0 |  |
| `cohesion` | 65 | 0 | 0 | 0 | share of ordered pairs in mutual dyads (reciprocated arcs / n(n-1)) |
| `connected` | 65 | 0 | 0 | 0 | undirected: nx.is_connected; directed: nx.is_weakly_connected (Agna convention) |
| `geodesics` | 65 | 0 | 0 | 0 | nx.all_pairs_shortest_path_length, 0 = unreachable |
| `eccentricity` | 65 | 0 | 0 | 0 | nx.eccentricity (connected); max reachable distance otherwise |
| `fareness` | 65 | 0 | 0 | 0 | row sum of distances |
| `closeness*(n-1)` | 65 | 0 | 0 | 0 | Agna closeness x(n-1) == nx.closeness_centrality(wf_improved=True) (on G.reverse() for digraphs, since NX uses incoming distance) |
| `betweenness` | 65 | 0 | 0 | 0 | raw Brandes over ordered pairs: undirected = 2 x nx unnormalised, directed = 1 x |
| `prestige(Lin)` | 65 | 0 | 0 | 0 | Lin (1976) proximity prestige computed from NX incoming distances |
| `bavelas` | 65 | 0 | 0 | 0 | sum(all d) / (sum d(i,.) + sum d(.,i)) from NX distances |
| `freeman_C_D(directed,out)` | 11 | 0 | 0 | 0 | directed out-degree centralization: sum(max-d_out)/(n-1)^2 (Wasserman & Faust 1994, 5.3) |
| `freeman_C_B(report)` | 54 | 0 | 0 | 0 | sum(max-b_norm)/(n-1) with nx normalised betweenness |
| `metric_betweenness_centralization` | 54 | 0 | 0 | 0 |  |
| `metric_density` | 65 | 0 | 0 | 0 |  |
| `metric_reciprocity` | 64 | 0 | 0 | 0 | nx.overall_reciprocity |
| `metric_transitivity` | 65 | 0 | 0 | 0 | nx.transitivity on the symmetrised graph |
| `metric_average_distance` | 64 | 0 | 0 | 0 | mean over reachable ordered pairs (== nx.average_shortest_path_length when connected) |
| `eigenvector` | 63 | 0 | 0 | 2 | principal eigenvector of (W + W^T), max = 1 (== nx.eigenvector_centrality_numpy) |
| `bonacich_0.10` | 65 | 0 | 0 | 0 | (I - beta A)^-1 A 1 on symmetrised binary A (== (katz(alpha=beta,beta=1)-1)/beta) |
| `bonacich_0.05` | 65 | 0 | 0 | 0 | (I - beta A)^-1 A 1 on symmetrised binary A (== (katz(alpha=beta,beta=1)-1)/beta) |
| `pagerank` | 65 | 0 | 0 | 0 | nx.pagerank(weight=None, alpha=0.85), dangling mass spread uniformly |
| `local_clustering` | 65 | 0 | 0 | 0 | nx.clustering on symmetrised binary graph |
| `triangles` | 65 | 0 | 0 | 0 |  |
| `mean_clustering` | 65 | 0 | 0 | 0 | nx.average_clustering (count_zeros=True) |
| `transitivity` | 65 | 0 | 0 | 0 |  |
| `coreness` | 65 | 0 | 0 | 0 | nx.core_number |
| `dyad_census` | 65 | 0 | 0 | 0 | MAN counts from NX DiGraph |
| `triad_census` | 59 | 0 | 0 | 0 | nx.triadic_census (Holland-Leinhardt codes) |
| `effective_size` | 65 | 0 | 0 | 0 | Burt (1992, eq 2.9) effective size (q != i, j) (== nx.effective_size unweighted) |
| `constraint` | 65 | 0 | 0 | 0 | nx.constraint(weight='weight') on symmetrised graph (isolates -> 0) |
| `efficiency` | 65 | 0 | 0 | 0 | effective_size / degree |
| `weak_components` | 65 | 0 | 0 | 0 | nx.(weakly_)connected_components |
| `strong_components` | 65 | 0 | 0 | 0 | nx.strongly_connected_components |
| `articulation_points` | 65 | 0 | 0 | 0 | nx.articulation_points on symmetrised graph |
| `bridges` | 65 | 0 | 0 | 0 | nx.bridges on symmetrised graph |
| `assortativity_out_in` | 11 | 0 | 0 | 0 | nx.degree_assortativity_coefficient(Dpos, x=, y=) |
| `assortativity_in_in` | 11 | 0 | 0 | 0 | nx.degree_assortativity_coefficient(Dpos, x=, y=) |
| `assortativity_out_out` | 11 | 0 | 0 | 0 | nx.degree_assortativity_coefficient(Dpos, x=, y=) |
| `assortativity_in_out` | 11 | 0 | 0 | 0 | nx.degree_assortativity_coefficient(Dpos, x=, y=) |
| `in_strength` | 65 | 0 | 0 | 0 | positive column sums excl. diagonal |
| `out_strength` | 65 | 0 | 0 | 0 |  |
| `valued_density` | 65 | 0 | 0 | 0 |  |
| `louvain_Q_of_own_partition` | 64 | 0 | 0 | 0 | nx.community.modularity of Agna's own partition |
| `louvain_Q_vs_nx_louvain` | 59 | 0 | 5 | 0 | quality check: Agna Louvain Q within 0.02 of best nx Louvain (heuristic, not exact) |
| `girvan_newman_Q_of_own_partition` | 34 | 0 | 0 | 0 | nx.community.modularity of Agna's own partition |
| `freeman_C_D` | 53 | 0 | 0 | 0 | undirected: sum(max-d)/((n-1)(n-2)) |
| `metric_degree_centralization` | 53 | 0 | 0 | 0 |  |
| `freeman_C_C(report)` | 50 | 0 | 0 | 0 | Freeman: sum(max-C')/[(n-1)(n-2)/(2n-3)], C' = nx closeness |
| `metric_closeness_centralization` | 50 | 0 | 0 | 0 |  |
| `assortativity_total` | 53 | 0 | 0 | 0 | nx.degree_assortativity_coefficient |
| `barrat_w_clustering` | 54 | 0 | 0 | 0 | Barrat et al. (2004) C_i^w from NX neighbourhoods (NX's own weighted clustering is Onnela's geometric mean, a different index) |

## Non-passing checks

| Network | Index | Status | max err | Detail |
|---|---|---|---|---|
| D_reciprocal120 | `louvain_Q_vs_nx_louvain` | WARN | 0.0326 | Agna Q=0.3585, best of 10 nx.louvain_communities=0.3911 |
| R_regular3_100 | `louvain_Q_vs_nx_louvain` | WARN | 0.0284 | Agna Q=0.5746, best of 10 nx.louvain_communities=0.6030 |
| S_barbell10_5 | `eigenvector` | SKIP |  | leading eigenvalue degenerate (gap 7.21e-06); vector not unique |
| S_cycle10 | `louvain_Q_vs_nx_louvain` | WARN | 0.02 | Agna Q=0.3400, best of 10 nx.louvain_communities=0.3600 |
| S_dodecahedral | `louvain_Q_vs_nx_louvain` | WARN | 0.0433 | Agna Q=0.3250, best of 10 nx.louvain_communities=0.3683 |
| S_empty10 | `eigenvector` | SKIP |  | leading eigenvalue degenerate (gap 0.00e+00); vector not unique |
| REAL_adjnoun | `louvain_Q_vs_nx_louvain` | WARN | 0.0236 | Agna Q=0.2810, best of 10 nx.louvain_communities=0.3046 |
