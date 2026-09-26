---
id: FU-0012
title: An action button on a panel row when it's hovered
found: 2026-09-26
source: panel-as-tree
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/OpenSpecPanel.kt:220
type: idea
size: M
---

## What
Show a change's next step (or Promote for a follow-up) as a small button at the end of a row while
the mouse is over it, so a single click runs it without selecting the row first.

## Why it matters
Only worth doing if select-then-act turns out to cost too much in daily use: the tree needs a click
to select a change before its next step is available in the toolbar.

## Notes
Candidate listed in panel-as-tree's proposal and deliberately left out of it. The IDE's trees don't
usually put buttons in rows, so check platform precedent (e.g. hover icons in the commit or
bookmarks trees) before building; a tree renderer paints rather than hosting live components, so
the click needs hit-testing on the tree.
