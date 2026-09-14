#!/usr/bin/env python3
"""
md2help.py - converts docs/manual.md into the in-app help pages.

Tiny, dependency-free Markdown subset converter tuned for JEditorPane's
HTML support (Swing HTML 3.2): headings, paragraphs, <b>/<i>, code spans,
lists, tables, blockquotes, and an HTML-comment screenshot marker.

Usage: python3 tools/md2help.py
Output: src/main/resources/help/help_contents.htm + one page per '## ' section

Anchors: heading ids use the same slug algorithm as GitHub's renderer
(lowercase, punctuation dropped, spaces -> '-'), so links written in
manual.md as '#github-style-slug' work both on the GitHub page for
manual.md and - rewritten here to '<page>.htm#slug' - inside the app.
"""
import pathlib, re, html

ROOT = pathlib.Path(__file__).resolve().parent.parent
MANUAL = ROOT / "docs" / "manual.md"
OUT = ROOT / "agna-desktop" / "src" / "main" / "resources" / "help"
CSS = (ROOT / "docs" / "help-styles.css").read_text(encoding="utf-8")

PAGE_THEME = """<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>{title} - Agna Help</title>
<style>{css}</style></head>
<body>
<!-- md2help generated - do not edit by hand; edit docs/manual.md -->
{body}
</body></html>"""


def slug(title):
    """GitHub-style heading anchor: lowercase, drop every character that is
    not a letter/digit/space/hyphen, then spaces -> '-'.
    Example: '4. Methodology — what exactly ...' -> '4-methodology--what-...'
    (the em dash vanishes and its two spaces become two hyphens, exactly as
    GitHub renders it). Verified against GitHub's own rendering."""
    return re.sub(r"[^a-z0-9 \-]", "", title.lower()).replace(" ", "-")


def file_stem(title):
    """Compact filename stem for a generated page (stable naming)."""
    return re.sub(r"[^a-z0-9]+", "", title.lower())


def inline(text, refs):
    text = html.escape(text)
    # classic relative page links (kept for robustness)
    text = re.sub(r"\[([^\]]+?)\]\(([a-z0-9]+\.htm(?:#[a-z0-9]+)?)\)",
            r'<a href="\2">\1</a>', text)
    # github-style '#slug' links: rewrite to the owning chapter page
    def ref_rewrite(m):
        target = refs.get(m.group(2))
        return '<a href="%s">%s</a>' % (target, m.group(1)) if target \
            else '<a href="#%s">%s</a>' % (m.group(2), m.group(1))
    text = re.sub(r"\[([^\]]+?)\]\(#([a-z0-9-]+)\)", ref_rewrite, text)
    text = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", text)
    text = re.sub(r"\*(.+?)\*", r"<i>\1</i>", text)
    text = re.sub(r"`([^`]+?)`", r"<code>\1</code>", text)
    return text


def render_table(rows, refs):
    out = ["<table>"]
    for idx, row in enumerate(rows):
        cells = [c.strip() for c in row.strip().strip("|").split("|")]
        tag = "th" if idx == 0 else "td"
        out.append("<tr>" + "".join("<%s>%s</%s>" % (tag, inline(c, refs), tag)
                for c in cells) + "</tr>")
    out.append("</table>")
    return "\n".join(out)


def render_block(lines, refs):
    """lines: the raw lines of one block; returns HTML."""
    if not lines:
        return ""
    first = lines[0]
    if first.startswith("|"):
        return render_table(lines, refs)
    if first.startswith("> "):
        body = " ".join(l[2:] for l in lines)
        return '<blockquote>%s</blockquote>' % inline(body, refs)
    if first.startswith("- "):
        items = "".join("<li>%s</li>" % inline(l[2:], refs)
                for l in lines if l.startswith("- "))
        return "<ul>%s</ul>" % items
    return "".join("<p>%s</p>" % inline(l, refs) for l in lines)


def split_sections(text):
    """returns list of (title, body_lines) split on '## ' headings; the
    '# ' top title is dropped."""
    sections = []
    current = None
    for raw in text.split("\n"):
        line = raw.rstrip()
        if line.startswith("## "):
            if current is not None:
                sections.append(current)
            current = [line[3:].strip(), []]
        elif line.startswith("# "):
            continue
        else:
            if current is not None:
                current[1].append(line)
    if current is not None:
        sections.append(current)
    return sections


def build_refs(sections):
    """slug -> '<stem>.htm#slug' for every chapter and subsection heading."""
    refs = {}
    for title, lines in sections:
        stem = file_stem(title)
        refs[slug(title)] = "%s.htm#%s" % (stem, slug(title))
        for line in lines:
            if line.startswith("### "):
                sub = line[4:].strip()
                refs[slug(sub)] = "%s.htm#%s" % (stem, slug(sub))
    return refs


def main():
    text = MANUAL.read_text(encoding="utf-8")
    sections = split_sections(text)
    refs = build_refs(sections)

    toc = ["<h1>Agna Help</h1>",
           "<p>Contents of the Agna 2.1.3 manual. Choose a section:</p>",
           "<ul>"]
    for title, _ in sections:
        toc.append('<li><a href="%s.htm">%s</a></li>' % (file_stem(title),
                html.escape(title)))
    toc.append("</ul>")
    (OUT / "help_contents.htm").write_text(PAGE_THEME.format(
        title="Contents", css=CSS, body="\n".join(toc)), encoding="utf-8")

    for title, lines in sections:
        body = ['<h1 id="%s" name="%s">%s</h1>' % (slug(title), slug(title),
                html.escape(title))]
        for line in lines:
            if not line.strip():
                continue
            if line.startswith("### "):
                sub = line[4:].strip()
                # both id and name: JEditorPane's scrollToReference
                # matches the classic name attribute
                body.append('<h2 id="%s" name="%s">%s</h2>' % (slug(sub),
                        slug(sub), html.escape(sub)))
                continue
            body.append(render_block([line], refs))
        (OUT / (file_stem(title) + ".htm")).write_text(PAGE_THEME.format(
            title=title, css=CSS, body="\n".join(body)), encoding="utf-8")

    # self-cleaning: drop pages this generator created in an earlier run
    # that are no longer part of the manual (stale numbering, renamed
    # sections), while leaving unrelated legacy help pages untouched
    current = {"help_contents.htm"}
    current.update(file_stem(t) + ".htm" for t, _ in sections)
    for stale in OUT.glob("*.htm"):
        if stale.name in current:
            continue
        if "md2help generated" in stale.read_text(encoding="utf-8",
                errors="ignore"):
            stale.unlink()

    print("generated:", sorted(p.name for p in OUT.glob("*.htm")))


if __name__ == "__main__":
    main()