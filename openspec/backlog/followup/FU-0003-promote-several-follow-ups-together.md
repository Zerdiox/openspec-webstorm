---
id: FU-0003
title: Promote several follow-ups together from the panel
found: 2026-09-25
source: panel-layout-and-usability
capability: openspec-workflow-panel
location: src/main/kotlin/dev/derwa/openspec/OpenSpecPanel.kt
type: idea
size: M
---

## What
The panel should let the user pick several open follow-ups and promote them in one Claude Code
session. The promote command already accepts several IDs.

## Why it matters
Related follow-ups are meant to be explored together as one change, but the panel can only start
one at a time.

## Notes
Came up while scoping the panel layout and usability change, which deferred it.
