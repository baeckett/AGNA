#!/bin/bash
# Builds the AGNA .dmg on macOS (double-click or run from Terminal).
# Requires JDK 17+ on the Mac.
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q -pl agna-desktop -am package -DskipTests
OUT="${OUT:-$(pwd)/installers-out}"; mkdir -p "$OUT"
# Stage a clean input folder: the jar plus exactly the 2.2.1 sample set (never the whole target/ folder or samples/)
STAGE="$(mktemp -d)"
cp agna-desktop/target/agna-2.2.2.jar "$STAGE/"
packaging/stage-samples.sh "$STAGE/samples"
jpackage --type dmg --name AGNA --app-version 2.2.2 --vendor "Marius Ion Bența" \
  --description "AGNA - Applied Graph and Network Analysis Open Source" \
  --icon packaging/Agna.icns --input "$STAGE" \
  --main-jar agna-2.2.2.jar --main-class com.bentza.sna.Agna --dest "$OUT"
echo "installer written to: $OUT/AGNA-2.2.2.dmg"
