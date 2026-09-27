---
id: F13
title: A settings page, starting with bringing back "Don't ask again" confirmations
found: 2026-09-26
source: check-follow-ups-to-promote
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/PanelActions.kt
type: idea
size: M
---

## What
Promoting several follow-ups from the toolbar asks for confirmation, with a "Don't ask again"
checkbox. Once ticked, nothing in the IDE brings the confirmation back. The plugin has no settings
page where it could be turned on again.

## Why it matters
A user who ticked it by accident, or wants the safety net back, is stuck with no way to undo it.
Any further plugin options will need the same settings page anyway.

## Notes
Deferred from check-follow-ups-to-promote. The settings page should be built for more than this one
toggle, since it will also hold whatever options come next.
