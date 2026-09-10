Agna 2.1.3 - macOS packaging (pilot)

Everything here is prepared; the final step must run ON A MAC because
jpackage is not a cross-compiler.

1. Copy this jar and the icon next to it:
     mkdir jars && cp agna-2.1.3.jar jars/
   (the manifest already contains Main-Class: com.bentza.sna.Agna)

2. Run (on macOS, with the JDK in PATH):
     jpackage \
       --name Agna \
       --app-version 2.1.3 \
       --input jars \
       --main-jar agna-2.1.3.jar \
       --type dmg \
       --icon packaging/Agna.icns \
       --java-options "-Xmx1024M" \
       --vendor "Marius Ion Benta" \
       --copyright "Copyright (C) 2002-2026 Marius Ion Benta" \
       --about-text "Agna 2.1.3 - Social Network Analysis. Open source (license pending). Website: https://www.netanalysis.co.uk (provisional)"

   This produces Agna-2.1.3.dmg with an embedded JRE - no Java install
   needed on the target machine, and the dock crafts from Agna.icns.

3. First run: macOS may say the app is from an unidentified developer;
   right-click > Open once, or codesign later:
     codesign --deep --force --sign "Apple Development: ..." Agna.app
   (and notarise for wide distribution - requires an Apple developer
   account; optional for a pilot test).

Notes:
- AgnaSettings.ini is written next to the working directory; inside the
  .app the working dir is the app bundle - if you see settings not being
  remembered, we should move the settings path to the user home first
  (planned for the release packaging pass).
- Building a .app only (no dmg): use --type app-image.
