---
id: FU-0007
title: With the plugin only on the backend, a button click silently does nothing
found: 2026-09-26
source: reworked-tabs-under-remote-development
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/OpenTabRequests.kt:21
type: idea
size: M
---

## What
Under remote development, if the plugin is installed through Plugins (Host) but not in the client,
the panel shows and its buttons work, but the request to open a tab has no subscriber and is
dropped. No tab opens and nothing tells the user why.

## Why it matters
Installing on the backend only is the obvious thing to try, and it looks like the plugin is broken.
The README explains it, but only if the user reads it.

## Notes
Checked in task 5.4 of reworked-tabs-under-remote-development. The backend knows when nobody is
subscribed for the clicking client (`MutableSharedFlow.subscriptionCount`, or `tryEmit` into a flow
filtered per `ClientId`), so it could show a notification such as "Install OpenSpec in the client
too" instead of dropping the request. Mind the short window after the client connects, before its
subscriber is up.
