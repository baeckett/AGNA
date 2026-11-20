#!/usr/bin/env bash
# Copies exactly the sample networks listed in packaging/samples-manifest.txt (the set shipped with AGNA 2.2.1)
# into DEST. Nothing else from samples/ is copied: that folder also holds test fixtures
# (Chain1.txt, Chain2.txt, "4 full.txt", ...) that must not go into an installer.
# Usage: packaging/stage-samples.sh DEST
set -euo pipefail
[ $# -eq 1 ] || { echo "usage: $0 DEST" >&2; exit 2; }
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEST="$1"; mkdir -p "$DEST"
n=0
while IFS= read -r name || [ -n "$name" ]; do
  [ -z "${name// }" ] && continue
  [ -f "$ROOT/samples/$name" ] || { echo "stage-samples: missing samples/$name" >&2; exit 1; }
  cp "$ROOT/samples/$name" "$DEST/$name"; n=$((n+1))
done < "$ROOT/packaging/samples-manifest.txt"
echo "staged $n sample networks into $DEST"
