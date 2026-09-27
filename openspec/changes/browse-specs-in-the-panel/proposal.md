# Proposal

## Why

Specs are the project's statement of intended behaviour, yet the IDE shows them only as files deep
in the project tree. Changes and follow-ups already have a place in the OpenSpec panel; the specs,
which say what the project already claims to do, don't. Looking up what a capability requires means
knowing which folder it lives in and scrolling through its file.

## What Changes

- The OpenSpec panel gets two tabs: "Workflow", holding today's tree of changes and follow-ups
  unchanged, and a new "Specs" tab.
- The Specs tab lists the project's specs as a tree:
  - A capability shows its name and how many requirements it has; its tooltip gives its purpose.
  - Under each capability, its requirements, by name, in the order the spec lists them.
  - Nested specs are grouped under their domain (e.g. "identity" holding "user-auth"); a flat
    project lists its capabilities at the top level.
  - A spec that can't be read is still listed, marked as unreadable and why.
- A row opens its spec: a capability at the top of its spec, a requirement at its heading, with
  the same double-click, Enter and Jump to Source as the Workflow tab.
- The Specs tree behaves like the Workflow tree: typing finds a row, collapsed groups are
  remembered, and the selection survives a refresh.
- Copying from the Specs tab:
  - Copy (Ctrl+C) copies what the rows show: a capability's full name (with its domain), or a
    requirement as `capability: requirement name`, one row per line.
  - "Copy Requirement Text" in a requirement's context menu copies the whole requirement: its
    heading, its text and its scenarios.
  - The IDE's own Copy Path/Reference works on the rows and gives the spec's file.
- The specs are read from the project's spec files, so the Specs tab works even where the
  `openspec` command isn't found.
- The Specs tab is read-only: it starts no Claude Code sessions.
- The panel's refresh action and its automatic refresh on file changes update both tabs.

## Capabilities

### New Capabilities

- `browsing-specs`: seeing the project's specs, their capabilities and requirements, in the
  OpenSpec panel, opening them and copying from them.

### Modified Capabilities

- `openspec-workflow-panel`:
  - "The lists are a tree like the IDE's own": the tree of changes and follow-ups is the panel's
    Workflow tab.

## Resolves

- F5: a capability/domain spec browser in the OpenSpec panel

## Out of Scope / Deferred

- Actions that start Claude Code sessions from the Specs tab. Decided against: the tab is for
  seeing specs, and the Workflow tab stays the one place to start a session.
- Remembering which tab was shown last across restarts. The panel opens on the Workflow tab.
- Showing a change's pending spec deltas in the Specs tab. The Specs tab shows the project's
  current specs only; a change's specs open from its Open menu.
- Other open follow-ups for the panel (F6, F7, F11, F13, F14, F15) are unrelated and stay open.

## Impact

- The backend's tool window set-up (two tabs), a new source that reads spec files, a new Specs tab
  built on the existing tree support, its renderer, copy support and navigation, plus their tests.
- No new dependencies and no plugin.xml changes expected.
- `explain-unreadable-follow-ups` is in flight and gives navigation an optional line to open at,
  which this change needs for requirements. Apply this change after that one and reuse it.
