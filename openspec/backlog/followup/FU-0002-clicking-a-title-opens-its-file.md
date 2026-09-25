---
id: FU-0002
title: Clicking a change or follow-up title opens its file
found: 2026-09-25
source: conversation
capability: openspec-workflow-panel
location: src/main/kotlin/dev/derwa/openspec/OpenSpecPanel.kt
type: idea
size: S
---

## What
Clicking a row's title in the OpenSpec panel should open the relevant file in the editor: a
follow-up's own file, and for a change its proposal (or the change folder when there is none).

## Why it matters
Reading a change or follow-up from the panel today means finding its file in the project tree.

## Notes
Still to decide: whether a change opens its proposal, or its tasks while it is being implemented.
A clickable title reads as a link, which is where a hand cursor and hover underline are native; it
must keep the "…" shortening and full-title tooltip that long titles get. An unreadable follow-up
still has a file, so it can still be opened.
