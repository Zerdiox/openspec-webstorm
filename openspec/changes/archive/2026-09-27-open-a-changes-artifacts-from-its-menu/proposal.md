# Proposal

## Why

Double-clicking a change, pressing Enter or using Jump to Source always opens its proposal. To reach
the change's design, tasks or delta specs, the user has to find them in the Project tree, although
those are the files read most while a change is implemented.

## What Changes

- A change's context menu gains an **Open** submenu straight after Jump to Source, with Proposal,
  Design, Tasks and Specs.
- Specs is itself a submenu with one entry per delta spec in the change, labelled by capability
  (e.g. `openspec-workflow-panel`, or `identity/user-auth` for a nested one).
- Entries for artifacts the change doesn't have yet are shown disabled, with a tooltip saying so.
  Specs is disabled when the change has no delta specs.
- With several changes, or a change together with other rows, selected, the Open submenu is shown
  disabled with the same "select one change" explanation as the change actions.
- The context menu only: the toolbar is unchanged.
- Jump to Source, double-click and Enter keep opening the proposal (or the change's folder while it
  has no proposal).

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `openspec-workflow-panel`: the context menu of a change lists Open, with the change's artifacts,
  between Jump to Source and the change actions.

## Resolves

- F10: Open a change's design, tasks or specs from its context menu

## Out of Scope / Deferred

- Artifacts of workflow schemas other than spec-driven. The entries use spec-driven's file names
  (proposal.md, design.md, tasks.md, specs/); a change using another schema only gets the entries
  whose files exist.
- Opening the current spec of a capability a change modifies. That belongs with the spec browser in
  F5.
- An Open entry in the toolbar.
- Other open follow-ups for this capability (F5, F6, F7, F11, F13, F14) are unrelated and stay open.

## Impact

- `backend/src/main/kotlin/dev/derwa/openspec/PanelActions.kt`: the Open submenu in the context menu.
- `backend/src/main/kotlin/dev/derwa/openspec/PanelModel.kt`: finding a change's artifacts, next to
  the existing open target.
- `backend/src/main/kotlin/dev/derwa/openspec/OpenSpecPanel.kt`: its file-opening navigatable becomes
  usable by the menu's actions.
- The in-progress change `explain-unreadable-follow-ups` modifies another requirement of the same
  spec ("A row opens its file") and touches `OpenSpecPanel.kt` and `PanelModel.kt`; their hunks may
  share files.
- No API, dependency or plugin.xml changes.
