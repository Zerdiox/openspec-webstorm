---
id: FU-0010
title: Open a change's design, tasks or specs from its context menu
found: 2026-09-26
source: panel-as-tree
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/PanelActions.kt:37
type: idea
size: S
---

## What
Right-clicking a change in the OpenSpec panel should offer an "Open" submenu with Proposal, Design,
Tasks and Specs. Entries for artifacts the change doesn't have yet are shown disabled.

## Why it matters
Double-click, Enter and F4 always open the proposal. Reaching a change's tasks or design means going
through the project tree, although those are the files read most while a change is implemented.

## Notes
Asked for by Derwa during the manual check of panel-as-tree. It doesn't reopen that change's rejected
option: the default open target stays the proposal, and this only adds menu entries. Each entry can
reuse the panel's file-opening navigatable. Specs is a folder (specs/**), so it could select the
folder in the project tree the way a change without a proposal does, or list the spec files.
