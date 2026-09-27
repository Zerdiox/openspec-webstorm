# Proposal

## Why

Two small inconsistencies were left in the code that opens Claude Code tabs. In a standalone IDE the
backend looks up the registered tab opener, but under remote development the client hard-codes the
opener class. The backend's request queue is also more visible than it needs to be. Neither is a
bug today. Fixing them now, while the code is fresh, is cheap and keeps the two paths from drifting.

## What Changes

- The client opens tabs requested by the backend with the tab opener registered through the plugin's
  extension point, the same lookup the backend uses in a standalone IDE, instead of constructing the
  terminal-tab class directly. If no opener is registered, the request is logged as a warning and
  dropped instead of failing silently.
- The backend's per-project queue of open-tab requests becomes module-internal, matching the other
  classes added for remote development.
- Every other top-level declaration in the backend module becomes module-internal too. Nothing
  outside the module uses them, and making them all internal together keeps Kotlin's exposure rules
  satisfied.
- No user-visible behaviour changes.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
<!-- none: pure refactor, so the change sets `skip_specs: true` -->

## Resolves

- F8: the frontend opens requested tabs through ReworkedTerminalTab directly
- F9: OpenTabRequests is public while the other new backend classes are internal

## Out of Scope / Deferred

- Visibility in the shared and frontend modules. Shared declarations are used across modules, and
  the frontend's are already internal.
- Other open follow-ups for this capability (F5, F6, F7, F10, F11, F13) are unrelated and stay open.

## Impact

- `frontend/src/main/kotlin/dev/derwa/openspec/OpenTabSubscriber.kt`: opener lookup.
- `backend/src/main/kotlin/dev/derwa/openspec/*.kt`: visibility only. Six of these files also carry
  uncommitted edits from `check-follow-ups-to-promote`, so the two changes' hunks share files.
- No API, dependency or plugin.xml changes.
