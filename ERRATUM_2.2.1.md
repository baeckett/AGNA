# Erratum: AGNA 2.2.1 (fixed in 2.2.2)

## Summary
Two defects in AGNA 2.2.1 produced incorrect results.

### 1. Distances in `agna metrics`, the MCP tool `agna_metrics` and the Python `metrics()` / `centrality()` calls
Distances were found with a depth-first traversal instead of a breadth-first one, so path lengths were not always shortest paths.
Affected: diameter, eccentricity, closeness, betweenness and the `geodesics` rows of `agna metrics`; the hop count and path written by `agna distance --out`.
Not affected: the desktop application, `agna analyse` and every degree-based measure (degree, emission, reception, status, determination, density).

Checked against NetworkX on the ten bundled networks and twelve random graphs: 2.2.1 was wrong on 8 of 10 samples (diameter, closeness and betweenness), 2.2.2 agrees on all.

| Network | Diameter in 2.2.1 | Correct |
|---|---|---|
| Zachary Karate Club | 6 | 5 |
| Padgett Florentine Families | 7 | 5 |
| random200 | 53 | 4 |

### 2. Closeness on networks that are not strongly connected (all interfaces)
Unreachable pairs were ignored, so an actor in a small separate part of the network could get a standardised closeness above the maximum of 1 and rank as the most central actor.
Results on connected (strongly connected) networks are correct.

## What to do
- Re-run `agna metrics` results, and anything built on them through MCP or Python, with 2.2.2.
- Re-check closeness results on disconnected networks.
- Results from the desktop reports on connected networks do not need to be redone.

## Versions
Affected: 2.1.3 to 2.2.1. Fixed: 2.2.2.

## Other bugs in 2.2.1 (also fixed in 2.2.2)
- Corrupt files (random bytes, NaN or text cells, ragged or non-square matrices, header-less `.agn`) were accepted as valid networks.
- Several `agna analyse` text reports (clustering, transitivity, k-core, triads, structural holes, connectivity, eigenvector, Bonacich, PageRank) printed only a title.
- Louvain modularity was too low (single phase); Bonacich power was wrong for beta at or above 1/lambda-max.
- `agna draw` and image export never finished for network files without viewer settings (grid space 0).
- CSV files with quoted names lost actors without warning (names containing a comma were split).
- `agna generate star` created a random network; `agna matrix --format csv` wrote a misaligned header; every error exited with code 1; `agna metrics` printed notes into JSON output; the MCP server did not answer malformed input.

Results of these analyses from 2.2.1 should be re-run with 2.2.2.
