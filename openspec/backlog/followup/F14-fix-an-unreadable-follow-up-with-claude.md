---
id: F14
title: Fix an unreadable follow-up with Claude Code from the panel
found: 2026-09-27
source: conversation
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/SelectionActions.kt
type: idea
size: M
---

## What
Offer an action on an unreadable follow-up that opens a Claude Code tab and asks it to fix the
file's frontmatter against the backlog README's template, where Promote used to be.

## Why it matters
explain-unreadable-follow-ups shows why a follow-up can't be read and jumps to the line, but
fixing it is still manual. For a broken YAML block a Claude session is one click away.

## Notes
Considered while exploring F11 and deferred. It would be the panel's first action that sends a
free-text prompt instead of an OpenSpec or /backlog command, so it needs its own decisions:
tab naming, how the prompt is worded, and whether it only makes sense where the project has a
backlog. A `/backlog validate` mode was rejected: backlog.md belongs to each project, so the
plugin can't count on the mode existing.
