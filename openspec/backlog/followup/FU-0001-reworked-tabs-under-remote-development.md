---
id: FU-0001
title: Under remote development, Claude tabs need a resize before they render cleanly
found: 2026-09-25
source: plugin-v1
capability: openspec-workflow-panel
location: src/main/kotlin/dev/derwa/openspec/ClaudeLauncher.kt
type: tech-debt
size: L
---

## What
Under JetBrains remote development the plugin runs on the backend, and a terminal tab the backend
creates reaches the client through a relay that starts it at a fixed size. Claude Code draws for
that size and leaves residue on screen until the user resizes the tab once. A standalone IDE opens
a proper reworked tab and has no such problem.

## Why it matters
Every button click under remote development opens a tab that looks broken until it is resized —
the setup the plugin is used in daily.

## Notes
Only the client can create a true reworked tab (the Terminal plugin's client-side
`TerminalToolWindowTabsManager`). The fix is a small client-side half of the plugin that opens the
tab, asked by the backend panel — JetBrains' intended split-mode architecture (shared / frontend /
backend content modules). That means a modular plugin, where `<depends>` is no longer allowed, so
every dependency moves to module descriptors, plus a way for the backend to ask the client to open
a tab. Neither the backend's `TerminalToolWindowManager` (never creates reworked tabs) nor waiting
for the size (the Classic widget doesn't support that signal) helps. The reworked API differs
between 2026.1 and 2026.2; use only the common part.
