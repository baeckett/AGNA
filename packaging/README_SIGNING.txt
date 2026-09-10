Agna 2.1.3 - macOS code signing and notarisation

Optional for a pilot test; required before wide distribution. Runs on
your Mac, needs an Apple Developer account for the notarisation step.

1. Ad-hoc signing (local use only)
   codesign --deep --force --sign - Agna.app

2. Developer ID signing (distribution)
   - In Xcode > Settings > Accounts, install your "Developer ID
     Application" certificate.
   - Sign the app bundle (and the nested runtime):
     codesign --deep --force --options runtime \
       --sign "Developer ID Application: <Your Name (TEAMID)>" Agna.app
   - Verify:
     codesign --verify --deep --strict Agna.app

3. Notarisation (so Gatekeeper trusts it on other Macs)
   - Create an app-specific password at appleid.apple.com for your
     developer account.
   - Upload:
     xcrun notarytool submit Agna-2.1.3.dmg \
       --apple-id <you@example.com> \
       --team-id <TEAMID> \
       --password <app-specific-password> \
       --wait
   - Staple the ticket into the dmg:
     xcrun stapler staple Agna-2.1.3.dmg

4. Distribution
   The stapled, signed dmg can be hosted anywhere; users may still see a
   first-run prompt for an app that has not been notarised or that ships
   from a brand-new identity.

Notes: re-run signing after every rebuild; the jpackage step produces
Agna.app under dist/ (use --type app-image first if you need the bare
bundle for signing before assembling the dmg).