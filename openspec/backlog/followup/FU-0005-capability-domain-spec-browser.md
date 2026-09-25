---
id: FU-0005
title: A capability/domain spec browser in the OpenSpec panel
found: 2026-09-25
source: conversation
capability: openspec-workflow-panel
location: src/main/kotlin/dev/derwa/openspec/OpenSpecToolWindowFactory.kt:20
type: idea
size: M
---

## What
A second tab in the OpenSpec tool window that browses the project's specs as a tree: domains (spec
folders that group capabilities, e.g. identity/user-auth), then capabilities with their purpose and
requirement count, then each capability's requirements. A flat project shows its capabilities
directly at the top level.

## Why it matters
Specs are the project's statement of intended behaviour, yet the only way to see them is to dig
through the specs folder in the project tree, while changes and follow-ups already have a place in
the panel.

## Notes
The tool window has one unnamed tab today; a second means naming both (e.g. "Workflow" and "Specs").
`openspec list --specs --json` gives capability ids and requirement counts (unverified whether a
nested spec's id carries its domain path; this repo only has flat specs), and `openspec show <id>
--type spec --json` gives purpose and requirements, the same CLI path the changes list uses. Related
to FU-0002: clicking a capability should open its spec file too. May deserve its own capability
("browsing specs") rather than growing openspec-workflow-panel; decide when promoting.
