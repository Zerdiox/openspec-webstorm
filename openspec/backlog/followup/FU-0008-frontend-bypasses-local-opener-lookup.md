---
id: FU-0008
title: The frontend opens requested tabs through ReworkedTerminalTab directly
found: 2026-09-26
source: reworked-tabs-under-remote-development
capability: openspec-workflow-panel
location: frontend/src/main/kotlin/dev/derwa/openspec/OpenTabSubscriber.kt:27
type: tech-debt
size: S
---

## What
`OpenTabSubscriber` opens tabs asked for by the backend with `ReworkedTerminalTab().open(...)`
instead of looking up the registered `localTabOpener` extension, which is what the backend does in
a standalone IDE.

## Why it matters
Today both paths reach the same class, so behaviour is identical. If a second or different opener
is ever registered, the remote path would ignore it while the standalone path uses it.

## Notes
Fix: take the opener from `LocalTabOpener.EP_NAME.extensionList.firstOrNull()` in the subscriber,
as `ClaudeLauncher` does.
