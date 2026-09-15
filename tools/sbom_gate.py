#!/usr/bin/env python3
"""
sbom_gate.py - SBOM license/vuln policy gate for AGNA releases.

Reads a CycloneDX aggregate BOM (JSON) and fails (exit 1) when:

1. any external component has a license outside the allowlist below,
   or has no resolvable license;
2. any component is a SNAPSHOT or dynamic/range version;
3. any component appears on the banned list (known-vulnerable or
   end-of-life artifacts that must not sneak back in).

Pass the BOM path as the only argument:

    python3 tools/sbom_gate.py target/bom.json

The project's own artifacts (group com.bentza.sna) are skipped. This is
the blocking step of the 'sbom-gate' CI job; the OWASP dependency-check
scan remains an optional, heavier local gate (see the sbom-gate Maven
profile and the CI workflow comments).
"""
import json
import sys

# Groups that belong to this project and are always acceptable.
OWN_GROUPS = {"com.bentza.sna"}

# License IDs (SPDX) acceptable for runtime dependencies. All runtime
# dependencies are Apache License 2.0 by policy (the Excel .xls path moved
# from JExcelAPI/LGPL to Apache POI); anything else fails the gate.
ALLOWED_LICENSE_IDS = {
    "Apache-2.0",
}

# Banned coordinates: group:artifact (any version). log4j 1.x is
# end-of-life with known CVEs and must never return.
BANNED = {"log4j:log4j"}


def license_of(component):
    """Return a normalized license label, or None when unknown."""
    licenses = component.get("licenses") or component.get("license") or []
    if isinstance(licenses, dict):
        licenses = [licenses]
    for entry in licenses:
        license = entry.get("license", {}) or {}
        lic_id = license.get("id") or entry.get("id")
        name = license.get("name") or entry.get("name")
        if lic_id:
            return lic_id
        if name:
            return name
    expression = component.get("licenseExpression")
    if expression:
        return expression
    return None


def main():
    if len(sys.argv) != 2:
        print("usage: python3 tools/sbom_gate.py <bom.json>", file=sys.stderr)
        return 2
    with open(sys.argv[1], encoding="utf-8") as fh:
        bom = json.load(fh)

    failures = []
    components = bom.get("components", [])
    for c in components:
        group = (c.get("group") or "").strip()
        name = c.get("name") or "?"
        version = c.get("version") or "?"
        coordinate = f"{group}:{name}:{version}" if group else f"{name}:{version}"
        if group in OWN_GROUPS:
            continue
        if BANNED & {f"{group}:{name}"}:
            failures.append(f"{coordinate}: banned component")
            continue
        if version.endswith("-SNAPSHOT") or any(ch in version for ch in "[,]("):
            failures.append(f"{coordinate}: SNAPSHOT/range version")
            continue
        license = license_of(c)
        ok = license in ALLOWED_LICENSE_IDS
        if not ok:
            failures.append(
                f"{coordinate}: license {license!r} not in allowlist "
                f"(or unresolvable)")

    if failures:
        print("SBOM gate FAILED:", file=sys.stderr)
        for f in failures:
            print("  -", f, file=sys.stderr)
        print(f"{len(failures)} violation(s) in {len(components)} components",
              file=sys.stderr)
        return 1

    print(f"SBOM gate PASSED: {len(components)} components, "
          f"all licenses allowlisted, no banned/SNAPSHOT components")
    return 0


if __name__ == "__main__":
    sys.exit(main())