#!/usr/bin/env python3
"""
md2help.py - converts docs/manual.md into the in-app help pages.

Tiny, dependency-free Markdown subset converter tuned for JEditorPane's
HTML support (Swing HTML 3.2): headings, paragraphs, <b>/<i>, code spans,
lists, tables, blockquotes, and an HTML-comment screenshot marker.

Usage: python3 tools/md2help.py
Output: src/main/resources/help/help_contents.htm + one page per '## ' section
"""
import pathlib, re, html

ROOT = pathlib.Path(__file__).resolve().parent.parent
MANUAL = ROOT / "docs" / "manual.md"
OUT = ROOT / "src" / "main" / "resources" / "help"
CSS = (ROOT / "docs" / "help-styles.css").read_text(encoding="utf-8")

PAGE_THEME = """<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>{title} - Agna Help</title>
<style>{css}</style></head>
<body>
<!-- md2help generated - do not edit by hand; edit docs/manual.md -->
{body}
</body></html>"""


def inline(text):
    text = html.escape(text)
    text = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", text)
    text = re.sub(r"\*(.+?)\*", r"<i>\1</i>", text)
    text = re.sub(r"`([^`]+?)`", r"<code>\1</code>", text)
    text = re.sub(r"\[([^\]]+?)\]\(([a-z0-9]+\.htm(?:#[a-z0-9]+)?)\)",
            r'<a href="\2">\1</a>', text)
    return text


def render_table(rows):
    out = ["<table>"]
    for idx, row in enumerate(rows):
        cells = [c.strip() for c in row.strip().strip("|").split("|")]
        tag = "th" if idx == 0 else "td"
        out.append("<tr>" + "".join("<%s>%s</%s>" % (tag, inline(c), tag) for c in cells) + "</tr>")
    out.append("</table>")
    return "\n".join(out)


def render_block(lines):
    """lines: the raw lines of one block; returns HTML."""
    if not lines:
        return ""
    first = lines[0]
    if first.startswith("|"):
        return render_table(lines)
    if first.startswith("> "):
        body = " ".join(l[2:] for l in lines)
        return '<blockquote>%s</blockquote>' % inline(body)
    if first.startswith("- "):
        items = "".join("<li>%s</li>" % inline(l[2:]) for l in lines if l.startswith("- "))
        return "<ul>%s</ul>" % items
    return "".join("<p>%s</p>" % inline(l) for l in lines)


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


def slug(title):
    return re.sub(r"[^a-z0-9]+", "", title.lower())


def main():
    text = MANUAL.read_text(encoding="utf-8")
    sections = split_sections(text)

    toc = ["<h1>Agna Help</h1>",
           "<p>Contents of the Agna 2.1.3 manual. Choose a section:</p>",
           "<ul>"]
    for title, _ in sections:
        toc.append('<li><a href="%s.htm">%s</a></li>' % (slug(title), html.escape(title)))
    toc.append("</ul>")
    (OUT / "help_contents.htm").write_text(PAGE_THEME.format(
        title="Contents", css=CSS, body="\n".join(toc)), encoding="utf-8")

    for title, lines in sections:
        body = []
        body.append("<h1>%s</h1>" % html.escape(title))
        for line in lines:
            if not line.strip():
                continue
            if line.startswith("### "):
                sub = line[4:].strip()
                body.append('<h2 id="%s">%s</h2>' % (slug(sub),
                        html.escape(sub)))
                continue
            body.append(render_block([line]))
        (OUT / (slug(title) + ".htm")).write_text(PAGE_THEME.format(
            title=title, css=CSS, body="\n".join(body)), encoding="utf-8")

    # self-cleaning: drop pages this generator created in an earlier run
    # that are no longer part of the manual (stale numbering, renamed
    # sections), while leaving unrelated legacy help pages untouched
    current = {"help_contents.htm"}
    current.update(slug(t) + ".htm" for t, _ in sections)
    for stale in OUT.glob("*.htm"):
        if stale.name in current:
            continue
        if "md2help generated" in stale.read_text(encoding="utf-8", errors="ignore"):
            stale.unlink()

    print("generated:", sorted(p.name for p in OUT.glob("*.htm")))


if __name__ == "__main__":
    main()