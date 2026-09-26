---
id: F11
title: An unreadable follow-up's row shows its file name twice
found: 2026-09-26
source: panel-as-tree
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/PanelModel.kt:85
type: bug
size: S
---

## What
For a follow-up the panel can't read, the row's title is its file name and its ID is the ID at the
start of that name, so the row repeats itself: "F40  F40-broken.md  unreadable". A file name with no
leading ID shows in full twice: "notes.md  notes.md  unreadable".

## Why it matters
Cosmetic, but it's the row meant to draw attention to a broken file, and the repetition makes it
look like a rendering bug.

## Notes
The old row-based panel did the same; panel-as-tree kept the behaviour. The ID fallback is also used
for speed search, so the fix is probably to show the file name once in the renderer, or leave the
title empty for unreadable rows.
