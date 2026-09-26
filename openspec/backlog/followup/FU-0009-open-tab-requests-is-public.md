---
id: FU-0009
title: OpenTabRequests is public while the other new backend classes are internal
found: 2026-09-26
source: reworked-tabs-under-remote-development
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/OpenTabRequests.kt:16
type: tech-debt
size: S
---

## What
The project service `OpenTabRequests` is `public`, while `BackendOpenTabApi`, `OpenTabApiProvider`
and the launcher's helpers are `internal`.

## Why it matters
Only consistency: nothing outside the backend module needs it, so it widens the module's API for
no reason.

## Notes
Make it `internal` unless the platform's project-service instantiation needs it public; check with
`./gradlew check verifyPlugin` and a `runIde` click after the change.
