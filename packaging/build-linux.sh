#!/usr/bin/env bash
# Builds the AGNA Linux installers (.deb and/or .rpm) with jpackage.
# Usage:   packaging/build-linux.sh [deb|rpm|both]   (default: deb)
# Requires JDK 17+ and the distro packaging tools (dpkg-deb for deb,
# rpmbuild for rpm - install with: sudo apt-get install -y fakeroot rpm).
set -euo pipefail
AGNA_JAR="${AGNA_JAR:-agna-desktop/target/agna-2.1.3.jar}"
[ -f "$AGNA_JAR" ] || { echo "jar not found: $AGNA_JAR (run: mvn -pl agna-desktop -am package -DskipTests)" >&2; exit 1; }
TMP="$(mktemp -d)"; OUT="${OUT:-$(pwd)/installers-out}"; mkdir -p "$TMP/pkg" "$OUT"
cp "$AGNA_JAR" "$TMP/pkg/agna-2.1.3.jar"
ARGS=( --name agna --app-version 2.1.3 --vendor "Marius Ion Bența"
       --description "AGNA - Applied Graph and Network Analysis Open Source"
       --input "$TMP/pkg" --main-jar agna-2.1.3.jar
       --main-class com.bentza.sna.Agna --dest "$OUT" )
case "${1:-deb}" in
  deb)  jpackage --type deb "${ARGS[@]}" ;;
  rpm)  jpackage --type rpm "${ARGS[@]}" ;;
  both) jpackage --type deb "${ARGS[@]}"; jpackage --type rpm "${ARGS[@]}" ;;
  *) echo "usage: $0 [deb|rpm|both]" >&2; exit 2 ;;
esac
echo "installers written to: $OUT"; ls -la "$OUT"/*.deb "$OUT"/*.rpm 2>/dev/null || true
