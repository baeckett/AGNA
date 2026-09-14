# Building the macOS and Windows installers

AGNA Desktop is packaged into native installers with **jpackage** (JDK 17+).
Nothing here needs to run in a continuous-integration service — each
installer is built **on its own operating system** (jpackage cannot
cross-compile):

- **macOS installer** → built on a Mac → produces a `.dmg`
- **Windows installer** → built on Windows → produces `.exe` and/or `.msi`
- Linux (.deb/.rpm/tar.gz) → built on Linux (see the Linux notes at the end)

## What you need

- The desktop jar: `AGNA-2.1.3-desktop.jar` (from the
  [latest release](https://github.com/baeckett/AGNA/releases), or built with
  `mvn package` — the shaded jar lands in
  `agna-desktop/target/agna-2.1.3.jar`).
- JDK 17 or newer installed **on the build machine**.
- The icons in this folder: `Agna.icns` (macOS) and `Agna.ico` (Windows).
- Internet access the first time jpackage runs (it auto-downloads the WiX
  toolset on Windows when required).

## macOS — .dmg

On a Mac with JDK 17+:

```bash
# one folder holding the jar under a stable name
mkdir -p ~/pkg && cp AGNA-2.1.3-desktop.jar ~/pkg/agna-2.1.3.jar

jpackage \
  --type dmg \
  --name AGNA \
  --app-version 2.1.3 \
  --vendor "Marius Ion Bența" \
  --description "AGNA - Applied Graph and Network Analysis Open Source" \
  --icon packaging/Agna.icns \
  --input ~/pkg \
  --main-jar agna-2.1.3.jar \
  --main-class com.bentza.sna.Agna \
  --dest ~/pkg-out
```

The result is `~/pkg-out/AGNA-2.1.3.dmg`. Test it: open the dmg, drag AGNA
to Applications, launch it.

Optional signing and notarization (needed only if you want to avoid the
"unidentified developer" warning for users): with an Apple Developer
account:

```bash
codesign --deep --force --options runtime \
  --sign "Developer ID Application: YOUR-NAME (TEAMID)" \
  ~/pkg-out/AGNA.app

# notarize (ad-hoc apps distributed outside the App Store)
xcrun notarytool submit ~/pkg-out/AGNA-2.1.3.dmg \
  --apple-id you@example.com --team-id TEAMID --password app-specific-password \
  --wait
xcrun stapler staple ~/pkg-out/AGNA-2.1.3.dmg
```

Signing must happen **before** the dmg is created (sign the `.app` inside
the app-image), so for a signed build unpack the app-image first:
`jpackage --type app-image ...` → sign `AGNA.app` → then
`hdiutil create` yourself, or re-run `jpackage --type dmg --app-image AGNA.app`
(from the same input) after signing. See `packaging/README_SIGNING.txt`.

## Windows — .exe / .msi

On a Windows machine with JDK 17+ (open PowerShell):

```powershell
# one folder holding the jar under a stable name
New-Item -ItemType Directory -Force $env:USERPROFILE\pkg
Copy-Item AGNA-2.1.3-desktop.jar $env:USERPROFILE\pkg\agna-2.1.3.jar

jpackage `
  --type msi `
  --name AGNA `
  --app-version 2.1.3 `
  --vendor "Marius Ion Bența" `
  --description "AGNA - Applied Graph and Network Analysis Open Source" `
  --icon packaging\Agna.ico `
  --input $env:USERPROFILE\pkg `
  --main-jar agna-2.1.3.jar `
  --main-class com.bentza.sna.Agna `
  --dest $env:USERPROFILE\pkg-out
```

First run downloads the WiX toolset automatically. For a leaner installer
use `--type msi` (recommended for distribution); `--type exe` produces an
executable installer instead. Results land in `%USERPROFILE%\pkg-out\`.
Test by installing and launching AGNA from the Start menu.

Optional code signing (removes SmartScreen warnings): sign
`AGNA-2.1.3.msi` with a code-signing certificate:

```powershell
signtool sign /fd SHA256 /a /f your-certificate.pfx /p PASSWORD `
  $env:USERPROFILE\pkg-out\AGNA-2.1.3.msi
```

(signtool ships with the Windows SDK; `/a` picks the best certificate.)

## Uploading the installers

Attach the finished files to the GitHub release so the website's Download
links stay valid:

1. Open https://github.com/baeckett/AGNA/releases/tag/v2.1.3 → *Edit*.
2. In *Assets*, drop in `AGNA-2.1.3.dmg`, `AGNA-2.1.3.msi` (and/or
   `AGNA-2.1.3.exe`).
3. Update `SHA256SUMS.txt` on the release with the new hashes
   (`sha256sum AGNA-2.1.3.dmg` on macOS, `Get-FileHash` on Windows), then
   replace the file.
4. If you want the website rows to point straight at the installers,
   swap the macOS/Windows download links in `website/index.html` to
   `releases/latest/download/AGNA-2.1.3.dmg` (and `.msi`), commit and push.

## Linux (reference)

`jpackage --type deb` / `--type rpm` on a Debian/RPM system, or
`--type app-image` + `tar -czf` for the portable archive used in the
release today (`AGNA-2.1.3-Linux-x64.tar.gz`). The `.deb`/`.rpm` builders
need the distro's packaging tools (`fakeroot`, `rpmbuild`).

## Notes per platform

- macOS: linking against older SDK versions can be tuned with
  `--java-options -Xdock:name=AGNA` (dock name) and
  `--java-options -Xmx512m` if memory should be capped.
- Windows: the JDK on the build machine determines the bundled runtime;
  use the same major version you test with.
- System requirements for the produced apps: same as the jar (Java
  runtime is bundled, ~150-250 MB installed).