---
id: F20
title: The tool window's toolbar actions update on the UI thread
found: 2026-10-14
source: disable-configuration-cache
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/PanelActions.kt:55
type: tech-debt
size: M
---

## What
Every panel action and action group declares `ActionUpdateThread.EDT`. At startup the platform logs
`ActionUpdater - 14874 ms total to grab EDT 14 times to expand ... OpenSpecToolbar ... Use
ActionUpdateThread.BGT`.

## Why it matters
Updating on the UI thread competes with everything else the IDE does there, so a busy UI thread
delays the toolbar and the toolbar adds to the UI thread's load. The platform flags it on every
start.

## Notes
Pre-existing: the warning is in the sandbox log since 2026-09-27. It was worse on a slow WSL startup,
when platform toolbars also logged 3-7 s warnings. The `update` methods likely read Swing state (the
panel's selection), which is why EDT was chosen. Moving to BGT means reading that state through the
action's data context (`e.getData`/a `DataProvider` snapshot) instead of touching components.
