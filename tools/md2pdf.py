#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Render the phase 2 design document as a right-to-left PDF.

Xcode shows a .md file as raw text, so no tag inside the document can make it
read right-to-left. A rendered PDF is the only form that looks the same
everywhere, so the docs stay plain Markdown and this produces a readable copy.

Output goes next to the Markdown source, inside the repository, because the
brief asks for the design PDF to be in the repository.

Run from the repository root:  python3 tools/md2pdf.py
"""
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.join(HERE, "..")
PROJECT = os.path.join(REPO, "docs")
OUT = PROJECT
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

DOCS = ["DESIGN-PHASE2.md"]

CSS = """
@page { size: A4; margin: 16mm 14mm; }
body { font-family: 'Vazirmatn','SF Arabic','Geeza Pro',Tahoma,sans-serif;
       direction: rtl; text-align: right; color: #15181d;
       font-size: 11.5pt; line-height: 1.9; }
h1 { font-size: 20pt; margin: 0 0 6px; border-bottom: 3px solid #4a6fa5;
     padding-bottom: 8px; }
h2 { font-size: 15pt; margin: 26px 0 8px; color: #2c4a73;
     page-break-after: avoid; }
h3 { font-size: 12.5pt; margin: 18px 0 6px; page-break-after: avoid; }
h4 { font-size: 11.5pt; margin: 14px 0 6px; color: #2c4a73;
     page-break-after: avoid; }
p, li { text-align: right; }
ul, ol { padding-right: 22px; padding-left: 0; }
table { border-collapse: collapse; width: 100%; margin: 12px 0 18px;
        font-size: 10.5pt; page-break-inside: avoid; }
th, td { border: 1px solid #d3d9e2; padding: 7px 10px; text-align: right;
         vertical-align: top; }
th { background: #eef2f7; font-weight: 700; }
blockquote { margin: 12px 0; padding: 10px 16px 10px 12px;
             border-right: 4px solid #c99a2e; background: #fdf8ee;
             border-radius: 0 6px 6px 0; }
blockquote p { margin: 4px 0; }
/* Persian inside a code span must keep its own direction and a font that
   shapes it; a monospace-only stack leaves the letters disconnected. */
code { font-family: 'SF Mono', Menlo, 'SF Arabic', 'Geeza Pro', Tahoma, monospace;
       font-size: 10pt; background: #f1f3f6; padding: 1px 5px;
       border-radius: 4px; unicode-bidi: plaintext; }
/* A code block is source, so it reads left to right even on an RTL page. */
pre { direction: ltr; text-align: left; background: #f6f8fa; padding: 12px 14px;
      border: 1px solid #e3e8ef; border-radius: 6px; overflow-x: auto;
      font-size: 9.5pt; line-height: 1.55; page-break-inside: avoid; }
pre code { background: none; padding: 0; unicode-bidi: plaintext; }
hr { border: none; border-top: 1px solid #dde2e9; margin: 22px 0; }
strong { font-weight: 700; color: #111; }
.ltr { direction: ltr; unicode-bidi: isolate; display: inline-block; }
a { color: #2c4a73; }
/* A diagram must fit a page. Without a max-height, a tall PlantUML export is
   taller than A4 and Chrome drops it rather than splitting it. */
img { max-width: 100%; max-height: 235mm; width: auto; height: auto;
      display: block; margin: 12px auto; }
"""

# A run of Latin letters/digits long enough to confuse bidi. Parentheses are
# deliberately excluded: swallowing a closing ")" into the isolated run flips
# it to the wrong side and renders "(Activity)" as "((Activity".
LATIN = re.compile(r'(?<![\w/])([A-Za-z][A-Za-z0-9 .\-_/→]*[A-Za-z0-9])')

# What a Latin run must never be found inside. Order matters, because the first
# alternative to match at a position wins:
#   * whole <pre> and <code> blocks, before the plain-tag rule can match just
#     their opening tag. Both allow attributes, since marked emits
#     <code class="language-java">;
#   * any other tag;
#   * HTML entities. Without this, "&lt;" is read as the Latin run "lt" and comes
#     back as "&<span>lt</span>;", which a browser then shows as the literal
#     text "&lt;" — so every generic in the document, Map<UserId, User> and the
#     rest, printed wrong.
SKIP = re.compile(r'<pre[^>]*>.*?</pre>|<code[^>]*>.*?</code>|<[^>]+>|&[a-zA-Z]+;|&#\d+;',
                  re.S)


def isolate_latin(html):
    """Wrap Latin runs in <span class=ltr> — but never inside a tag, code or entity."""
    out, i = [], 0
    for m in SKIP.finditer(html):
        out.append(LATIN.sub(r'<span class="ltr">\1</span>', html[i:m.start()]))
        out.append(m.group(0))
        i = m.end()
    out.append(LATIN.sub(r'<span class="ltr">\1</span>', html[i:]))
    return "".join(out)


def convert(name):
    md_path = os.path.join(PROJECT, name)
    body = subprocess.run(["npx", "--yes", "marked", "--gfm", "-i", md_path],
                          capture_output=True, text=True, check=True).stdout
    body = isolate_latin(body)
    html = (f'<!doctype html><html lang="fa" dir="rtl"><meta charset="utf-8">'
            f'<style>{CSS}</style><body>{body}</body></html>')

    base = os.path.join(OUT, os.path.splitext(name)[0])
    html_path = base + ".rtl.html"
    with open(html_path, "w", encoding="utf-8") as fh:
        fh.write(html)

    pdf_path = base + ".pdf"
    subprocess.run([
        CHROME, "--headless", "--disable-gpu", "--no-pdf-header-footer",
        f"--print-to-pdf={pdf_path}", "--virtual-time-budget=15000",
        f"file://{os.path.abspath(html_path)}",
    ], check=True, capture_output=True)
    return pdf_path


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for doc in sys.argv[1:] or DOCS:
        print("wrote", os.path.relpath(convert(doc), REPO))
