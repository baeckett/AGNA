#!/bin/bash
# Builds the AGNA .dmg on macOS (double-click or run from Terminal).
# Requires JDK 17+ on the Mac.
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q -pl agna-desktop -am package -DskipTests
OUT="${OUT:-$(pwd)/installers-out}"; mkdir -p "$OUT"
jpackage --type dmg --name AGNA --app-version 2.1.3 --vendor "Marius Ion Bența" \
  --description "AGNA - Applied Graph and Network Analysis Open Source" \
  --icon packaging/Agna.icns --input agna-desktop/target \
  --main-jar agna-2.1.3.jar --main-class com.bentza.sna.Agna --dest "$OUT"
echo "installer written to: $OUT/AGNA-2.1.3.dmg"
