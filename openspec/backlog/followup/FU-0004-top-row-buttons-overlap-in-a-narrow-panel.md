---
id: FU-0004
title: In a narrow panel, the top row's buttons overlap
found: 2026-09-25
source: panel-layout-and-usability
capability: openspec-workflow-panel
location: src/main/kotlin/dev/derwa/openspec/OpenSpecPanel.kt:107
type: bug
size: S
---

## What
The panel's top row puts Explore and Propose on the left and Backlog review on the right, each side
at its full width. Now that the panel follows the tool window's width, a panel too narrow for all
three makes Backlog review overlap Propose instead of scrolling sideways.

## Why it matters
With the tool window docked narrow, a button is hidden behind another, and a click can land on the
wrong one.

## Notes
Appeared once rows started following the viewport's width. Options: let the row wrap onto two
lines, or move Backlog review onto its own line when there's no room. Headings use the same row
layout but have nothing on the right, so they're unaffected.
