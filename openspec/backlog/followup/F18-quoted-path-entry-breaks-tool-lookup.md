---
id: F18
title: A quoted PATH entry on Windows breaks finding openspec and claude
found: 2026-09-28
source: start-claude-directly-on-windows
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/PathLookup.kt:25
type: bug
size: S
---

## What
The PATH lookup turns each PATH entry into a path as it is. Windows allows an entry in double quotes
(`"C:\Program Files\x"`) and strips them itself, but a path containing `"` is invalid, so the lookup
throws instead of skipping or unquoting the entry.

## Why it matters
With such an entry on the PATH, the panel never shows its lists and an action opens no tab. The
user gets neither the "not found" message nor any other explanation, only an IDE internal error
blamed on the plugin.

## Notes
Pre-existing, not introduced by the change that found it. Both lookups (`openspec` for the lists,
`claude` for actions) go through the same function, so one fix covers both. Strip surrounding quotes
as Windows does, and skip any entry that still isn't a valid path.
