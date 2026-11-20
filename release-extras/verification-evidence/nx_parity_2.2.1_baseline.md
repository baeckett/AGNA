# Agna vs NetworkX parity battery

NetworkX 3.6.1, NumPy 2.4.6. Tolerance: |a-r| <= 1e-05 + 0.0001*|r| unless noted.

**65 networks, 3360 checks: FAIL 68, PASS 3280, SKIP 2, WARN 10**

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
| `closeness*(n-1)` | 56 | 9 | 0 | 0 | Agna closeness x(n-1) == nx.closeness_centrality(wf_improved=True) (on G.reverse() for digraphs, since NX uses incoming distance) |
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
| `eigenvector` | 58 | 5 | 0 | 2 | principal eigenvector of (W + W^T), max = 1 (== nx.eigenvector_centrality_numpy) |
| `bonacich_0.10` | 46 | 19 | 0 | 0 | (I - beta A)^-1 A 1 on symmetrised binary A (== (katz(alpha=beta,beta=1)-1)/beta) |
| `bonacich_0.05` | 60 | 5 | 0 | 0 | (I - beta A)^-1 A 1 on symmetrised binary A (== (katz(alpha=beta,beta=1)-1)/beta) |
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
| `louvain_Q_vs_nx_louvain` | 24 | 30 | 10 | 0 | quality check: Agna Louvain Q within 0.02 of best nx Louvain (heuristic, not exact) |
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
| D_cycle40 | `louvain_Q_vs_nx_louvain` | FAIL | 0.23 | Agna Q=0.4500, best of 10 nx.louvain_communities=0.6800 |
| D_dag150 | `closeness*(n-1)` | FAIL | 149 | 117/150 differ; e.g. idx 62: Agna=149 NX=0.00671141  |
| D_dag150 | `louvain_Q_vs_nx_louvain` | FAIL | 0.185 | Agna Q=0.2606, best of 10 nx.louvain_communities=0.4453 |
| D_gnr200 | `closeness*(n-1)` | FAIL | 199 | 199/200 differ; e.g. idx 1: Agna=199 NX=0.00502513  |
| D_gnr200 | `louvain_Q_vs_nx_louvain` | FAIL | 0.227 | Agna Q=0.6273, best of 10 nx.louvain_communities=0.8539 |
| D_instar25 | `closeness*(n-1)` | FAIL | 24 | 24/25 differ; e.g. idx 0: Agna=24 NX=0.0416667  |
| D_path30 | `closeness*(n-1)` | FAIL | 29 | 28/30 differ; e.g. idx 28: Agna=29 NX=0.0344828  |
| D_path30 | `eigenvector` | FAIL | 0.00811 | 18/30 differ; e.g. idx 5: Agna=0.580108 NX=0.572002  |
| D_path30 | `louvain_Q_vs_nx_louvain` | FAIL | 0.209 | Agna Q=0.4501, best of 10 nx.louvain_communities=0.6593 |
| D_reciprocal120 | `closeness*(n-1)` | FAIL | 0.00653 | 117/120 differ; e.g. idx 53: Agna=0.390164 NX=0.383634  |
| D_reciprocal120 | `louvain_Q_vs_nx_louvain` | FAIL | 0.122 | Agna Q=0.2688, best of 10 nx.louvain_communities=0.3911 |
| D_scalefree300 | `closeness*(n-1)` | FAIL | 299 | 256/300 differ; e.g. idx 9: Agna=299 NX=0.00334448  |
| D_scalefree300 | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0714: the series diverges; Agna returns a truncated value instead of rejecting beta |
| D_scalefree300 | `louvain_Q_vs_nx_louvain` | FAIL | 0.0838 | Agna Q=0.4106, best of 10 nx.louvain_communities=0.4944 |
| D_tournament12 | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0909: the series diverges; Agna returns a truncated value instead of rejecting beta |
| R_ba300_m1 | `louvain_Q_vs_nx_louvain` | FAIL | 0.211 | Agna Q=0.6564, best of 10 nx.louvain_communities=0.8673 |
| R_ba300_m3 | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0857: the series diverges; Agna returns a truncated value instead of rejecting beta |
| R_ba300_m3 | `louvain_Q_vs_nx_louvain` | FAIL | 0.129 | Agna Q=0.2487, best of 10 nx.louvain_communities=0.3781 |
| R_er60_dense | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0325: the series diverges; Agna returns a truncated value instead of rejecting beta |
| R_er60_dense | `bonacich_0.05` | FAIL |  | beta=0.05 >= 1/lambda_max=0.0325: the series diverges; Agna returns a truncated value instead of rejecting beta |
| R_geometric200 | `closeness*(n-1)` | FAIL | 33.1 | 200/200 differ; e.g. idx 37: Agna=33.1667 NX=0.0301508  |
| R_geometric200 | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0844: the series diverges; Agna returns a truncated value instead of rejecting beta |
| R_geometric200 | `louvain_Q_vs_nx_louvain` | WARN | 0.0449 | Agna Q=0.7504, best of 10 nx.louvain_communities=0.7952 |
| R_holmekim300 | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0801: the series diverges; Agna returns a truncated value instead of rejecting beta |
| R_holmekim300 | `louvain_Q_vs_nx_louvain` | FAIL | 0.108 | Agna Q=0.3620, best of 10 nx.louvain_communities=0.4699 |
| R_regular3_100 | `louvain_Q_vs_nx_louvain` | FAIL | 0.227 | Agna Q=0.3756, best of 10 nx.louvain_communities=0.6030 |
| R_sbm4x50 | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0652: the series diverges; Agna returns a truncated value instead of rejecting beta |
| R_union_with_isolates | `closeness*(n-1)` | FAIL | 6.09 | 23/26 differ; e.g. idx 0: Agna=6.25 NX=0.16  |
| R_union_with_isolates | `louvain_Q_vs_nx_louvain` | FAIL | 0.121 | Agna Q=0.5984, best of 10 nx.louvain_communities=0.7189 |
| R_ws200_b0.05 | `eigenvector` | FAIL | 0.00757 | 97/200 differ; e.g. idx 180: Agna=0.797193 NX=0.789623  |
| R_ws200_b0.05 | `louvain_Q_vs_nx_louvain` | FAIL | 0.0581 | Agna Q=0.7138, best of 10 nx.louvain_communities=0.7719 |
| R_ws200_b0.0 | `louvain_Q_vs_nx_louvain` | FAIL | 0.16 | Agna Q=0.6367, best of 10 nx.louvain_communities=0.7970 |
| R_ws200_b0.3 | `louvain_Q_vs_nx_louvain` | WARN | 0.0375 | Agna Q=0.5548, best of 10 nx.louvain_communities=0.5923 |
| R_ws200_b1.0 | `louvain_Q_vs_nx_louvain` | FAIL | 0.169 | Agna Q=0.2249, best of 10 nx.louvain_communities=0.3934 |
| S_barbell10_5 | `eigenvector` | SKIP |  | leading eigenvalue degenerate (gap 7.21e-06); vector not unique |
| S_bintree_d6 | `louvain_Q_vs_nx_louvain` | FAIL | 0.173 | Agna Q=0.6498, best of 10 nx.louvain_communities=0.8231 |
| S_circladder25 | `louvain_Q_vs_nx_louvain` | FAIL | 0.379 | Agna Q=0.2933, best of 10 nx.louvain_communities=0.6720 |
| S_complete40 | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0256: the series diverges; Agna returns a truncated value instead of rejecting beta |
| S_complete40 | `bonacich_0.05` | FAIL |  | beta=0.05 >= 1/lambda_max=0.0256: the series diverges; Agna returns a truncated value instead of rejecting beta |
| S_cycle10 | `louvain_Q_vs_nx_louvain` | FAIL | 0.06 | Agna Q=0.3000, best of 10 nx.louvain_communities=0.3600 |
| S_cycle200 | `louvain_Q_vs_nx_louvain` | FAIL | 0.366 | Agna Q=0.4900, best of 10 nx.louvain_communities=0.8562 |
| S_dodecahedral | `louvain_Q_vs_nx_louvain` | FAIL | 0.117 | Agna Q=0.2517, best of 10 nx.louvain_communities=0.3683 |
| S_empty10 | `eigenvector` | SKIP |  | leading eigenvalue degenerate (gap 0.00e+00); vector not unique |
| S_grid10x10 | `louvain_Q_vs_nx_louvain` | FAIL | 0.383 | Agna Q=0.2730, best of 10 nx.louvain_communities=0.6555 |
| S_grid6x6x6 | `louvain_Q_vs_nx_louvain` | FAIL | 0.409 | Agna Q=0.2242, best of 10 nx.louvain_communities=0.6330 |
| S_heawood | `louvain_Q_vs_nx_louvain` | WARN | 0.0408 | Agna Q=0.1905, best of 10 nx.louvain_communities=0.2313 |
| S_hypercube6 | `louvain_Q_vs_nx_louvain` | FAIL | 0.258 | Agna Q=0.1354, best of 10 nx.louvain_communities=0.3939 |
| S_ladder30 | `eigenvector` | FAIL | 0.0148 | 48/60 differ; e.g. idx 5: Agna=0.586846 NX=0.572002  |
| S_ladder30 | `louvain_Q_vs_nx_louvain` | FAIL | 0.41 | Agna Q=0.3073, best of 10 nx.louvain_communities=0.7169 |
| S_lollipop8_20 | `louvain_Q_vs_nx_louvain` | FAIL | 0.107 | Agna Q=0.4225, best of 10 nx.louvain_communities=0.5297 |
| S_path50 | `eigenvector` | FAIL | 0.0862 | 46/50 differ; e.g. idx 9: Agna=0.664239 NX=0.578048  |
| S_path50 | `louvain_Q_vs_nx_louvain` | FAIL | 0.262 | Agna Q=0.4700, best of 10 nx.louvain_communities=0.7316 |
| S_petersen | `louvain_Q_vs_nx_louvain` | WARN | 0.0333 | Agna Q=0.1333, best of 10 nx.louvain_communities=0.1667 |
| S_torus12x12 | `louvain_Q_vs_nx_louvain` | FAIL | 0.383 | Agna Q=0.2361, best of 10 nx.louvain_communities=0.6194 |
| S_turan20_4 | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0667: the series diverges; Agna returns a truncated value instead of rejecting beta |
| S_tutte | `louvain_Q_vs_nx_louvain` | FAIL | 0.29 | Agna Q=0.3015, best of 10 nx.louvain_communities=0.5914 |
| S_wheel20 | `louvain_Q_vs_nx_louvain` | WARN | 0.0499 | Agna Q=0.1773, best of 10 nx.louvain_communities=0.2271 |
| WD_er80 | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0630: the series diverges; Agna returns a truncated value instead of rejecting beta |
| W_ws150 | `louvain_Q_vs_nx_louvain` | FAIL | 0.0615 | Agna Q=0.6451, best of 10 nx.louvain_communities=0.7066 |
| REAL_7th_graders | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0532: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_7th_graders | `bonacich_0.05` | FAIL | 0.751 | 29/29 differ; e.g. idx 7: Agna=419.278 NX=420.029  |
| REAL_adjnoun | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0760: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_adjnoun | `louvain_Q_vs_nx_louvain` | FAIL | 0.0682 | Agna Q=0.2364, best of 10 nx.louvain_communities=0.3046 |
| REAL_celegansneural | `closeness*(n-1)` | FAIL | 296 | 294/297 differ; e.g. idx 25: Agna=296 NX=0.00337838  |
| REAL_celegansneural | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0410: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_celegansneural | `bonacich_0.05` | FAIL |  | beta=0.05 >= 1/lambda_max=0.0410: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_celegansneural | `louvain_Q_vs_nx_louvain` | WARN | 0.0327 | Agna Q=0.3699, best of 10 nx.louvain_communities=0.4026 |
| REAL_contiguous_usa | `louvain_Q_vs_nx_louvain` | FAIL | 0.0551 | Agna Q=0.5419, best of 10 nx.louvain_communities=0.5970 |
| REAL_dolphins | `louvain_Q_vs_nx_louvain` | WARN | 0.0289 | Agna Q=0.4952, best of 10 nx.louvain_communities=0.5241 |
| REAL_football | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0928: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_football | `louvain_Q_vs_nx_louvain` | WARN | 0.0235 | Agna Q=0.5811, best of 10 nx.louvain_communities=0.6046 |
| REAL_jazz_collab | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0250: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_jazz_collab | `bonacich_0.05` | FAIL |  | beta=0.05 >= 1/lambda_max=0.0250: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_lesmis | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0833: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_polbooks | `eigenvector` | FAIL | 0.0149 | 38/105 differ; e.g. idx 8: Agna=0.320456 NX=0.305588  |
| REAL_polbooks | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0838: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_polbooks | `louvain_Q_vs_nx_louvain` | WARN | 0.0437 | Agna Q=0.4833, best of 10 nx.louvain_communities=0.5270 |
| REAL_train_terrorists | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0739: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_windsurfers | `bonacich_0.10` | FAIL |  | beta=0.1 >= 1/lambda_max=0.0552: the series diverges; Agna returns a truncated value instead of rejecting beta |
| REAL_windsurfers | `louvain_Q_vs_nx_louvain` | WARN | 0.0204 | Agna Q=0.2378, best of 10 nx.louvain_communities=0.2581 |
