---
id: F15
title: The Open menu of a change follows its workflow schema's artifacts
found: 2026-09-27
source: open-a-changes-artifacts-from-its-menu
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/PanelModel.kt
type: idea
size: M
---

## What
A change's Open submenu assumes spec-driven's file names (proposal.md, design.md, tasks.md,
specs/**/spec.md). A change using another schema only gets the entries whose files happen to exist,
and never sees artifacts with other names.

## Why it matters
Projects with a custom schema get a partly disabled or incomplete menu, although the files are
there.

## Notes
`openspec status --change <name> --json` lists each artifact with its `artifactPaths`
(`existingOutputPaths`), so the menu could follow the schema. The catch is cost: running it for
every change on each right-click, or caching it during the list refresh. The fixed names were chosen
to avoid that; see the design of open-a-changes-artifacts-from-its-menu. This repo only uses
spec-driven, so nothing here needs it yet.
