---
id: FU-0006
title: Claude Code replaces the tab name the plugin gives it
found: 2026-09-26
source: reworked-tabs-under-remote-development
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/ShellQuoting.kt:7
type: bug
size: S
---

## What
A button opens a tab named for the action and its target (e.g. `Apply: <change>`), but once Claude
Code starts it sets the terminal title to `✳ <session summary>`, and the tab shows that instead.

## Why it matters
With several sessions open, the tab names no longer say which change or follow-up each one is for,
which is what naming them was meant to give.

## Notes
Seen in the standalone and split-mode sandboxes: the tab starts with the plugin's name and changes
when Claude Code starts. Not caused by the change that found it. Likely fix: prefix the command line
with `CLAUDE_CODE_DISABLE_TERMINAL_TITLE=1` so Claude Code leaves the title alone; check that the
reworked terminal then keeps the tab name, and update the command-line tests.
