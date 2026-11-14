#!/usr/bin/env bash
#
# Build standalone GraalVM Native Image binary for Agna CLI
#

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# Locate native-image binary
NATIVE_IMAGE_BIN=""
if command -v native-image >/dev/null 2>&1; then
  NATIVE_IMAGE_BIN="$(command -v native-image)"
elif [ -n "${GRAALVM_HOME:-}" ] && [ -x "$GRAALVM_HOME/bin/native-image" ]; then
  NATIVE_IMAGE_BIN="$GRAALVM_HOME/bin/native-image"
elif [ -d "/Volumes/MacAPFS/tools/graalvm/Contents/Home/bin" ]; then
  NATIVE_IMAGE_BIN="/Volumes/MacAPFS/tools/graalvm/Contents/Home/bin/native-image"
elif [ -d "$HOME/.graalvm" ]; then
  NATIVE_IMAGE_BIN="$(find "$HOME/.graalvm" -name "native-image" -type f -perm +111 2>/dev/null | head -n 1)"
fi

if [ -z "$NATIVE_IMAGE_BIN" ] || [ ! -x "$NATIVE_IMAGE_BIN" ]; then
  echo "Error: GraalVM 'native-image' binary not found." >&2
  echo "Please set GRAALVM_HOME or install GraalVM JDK 21+ with native-image on PATH." >&2
  exit 1
fi

echo "==> Using GraalVM Native Image at: $NATIVE_IMAGE_BIN"
"$NATIVE_IMAGE_BIN" --version

# Ensure CLI JAR is present
CLI_JAR="agna-cli/target/agna-cli-2.2.0.jar"
if [ ! -f "$CLI_JAR" ]; then
  echo "==> Packaging $CLI_JAR..."
  if command -v mvn >/dev/null 2>&1; then
    mvn -B -ntp -pl agna-cli -am package -DskipTests
  fi
fi

if [ ! -f "$CLI_JAR" ]; then
  echo "Error: $CLI_JAR not found. Please compile the project first." >&2
  exit 1
fi

OUTPUT_BIN="agna-cli/target/agna"
echo "==> Compiling standalone native binary: $OUTPUT_BIN"

"$NATIVE_IMAGE_BIN" \
  --no-fallback \
  -H:+ReportExceptionStackTraces \
  --enable-url-protocols=http,https \
  -Djava.awt.headless=true \
  -jar "$CLI_JAR" \
  "$OUTPUT_BIN"

chmod +x "$OUTPUT_BIN"
echo "==> Native binary compiled successfully: $OUTPUT_BIN"
echo "==> Verifying execution:"
"$OUTPUT_BIN" --version
