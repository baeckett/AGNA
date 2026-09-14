# docs/ — Manual and help sources

- `manual.md` — single source of the user manual (in-app help + PDF).
- `QUICKSTART.md` — ten-minute, installation-first guide for new users.
- `cli.md` — command reference for the Agna CLI (including `metrics`).
- `agna.1` — the CLI man page (roff).
- `agnacompletion.bash`, `agnacompletion.zsh` — shell completions.
- `../CITATION.cff`, `../CITATION.bib` — citation metadata for AGNA 2.1.3
  (DOI 10.5281/zenodo.22708199).
- `help-styles.css` — stylesheet for the generated HTML help.
- `manuals/` — generated PDF/Print output (not committed yet).

Planned pipeline:
  manual.md --(Pandoc or md-to-html)--> src/main/resources/help/*.htm
  manual.md --(Pandoc + wkhtmltopdf/weasyprint)--> docs/manuals/Agna-2.1.3-manual.pdf

Screenshots for the Quick Start must be captured on macOS (the GUI cannot
run in the build sandbox); drop them under `docs/screenshots/`.
