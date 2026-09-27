# Proposal

## Why

Promoting several follow-ups together already works, but only through Ctrl/Shift-click
multi-select, which few users think to try. The commit panel shows the IDE's own answer: a checkbox
on each row for "include this", kept apart from the selection, which stays "what I'm looking at".

## What Changes

- Each follow-up row gets a checkbox. Changes don't get one, since a change action always has a
  single target.
  - The "Follow-ups" group, and the type or capability groups when grouping, get a three-state
    checkbox. It checks or unchecks the follow-ups shown under it and shows whether none, some or
    all of them are checked.
  - Space toggles the checkbox of the selected rows, as in the commit panel. Clicking a row still
    selects it without checking it. Selection, opening, keyboard navigation and typing to find a row
    are unchanged.
  - A follow-up without an ID, which can't be promoted, has no checkbox.
- The toolbar's Promote acts on the checked follow-ups that are shown. With none checked, it falls
  back to the selected follow-ups, as today. Otherwise it is disabled with a reason.
  - Its label says what it will promote: "Promote F6" for one, "Promote 3" for several.
  - The context menu's Promote acts only on the rows right-clicked, or the rows selected at the time,
    whatever is checked. It is labelled the same way.
- A follow-up hidden by the filter keeps its check but isn't promoted while it's hidden.
- Checks survive the panel's refreshes. A resolved follow-up drops out. Checks are not remembered
  when the IDE restarts.
- Promoting several follow-ups from the toolbar first asks for confirmation. The dialog lists each
  follow-up's ID and title, with Cancel and Promote, and the IDE's usual "Don't ask again". Promoting
  one follow-up, or promoting from the context menu, never asks.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `openspec-workflow-panel`:
  - New "Follow-ups can be checked": checkboxes on follow-up rows and follow-up groups, toggled
    with Space, kept across refreshes and filters but not restarts.
  - "Actions act on the selection": the toolbar's Promote acts on the checked follow-ups, falling
    back to the selection. The context menu's Promote acts on the rows clicked.
  - "Several follow-ups can be promoted together": through checkboxes, with a confirmation.

## Impact

- The panel's tree gains checkboxes. The reusable tree set-up keeps checks by key across refreshes,
  as it already does for the selection and collapsed groups, so it stays free of OpenSpec knowledge.
- The logic that works out Promote's target for the toolbar and for the context menu changes, and
  so do its tests.
- A new confirmation dialog for promoting several follow-ups, whose "Don't ask again" choice is
  stored for the IDE, not per project.
- Code changes are in the backend module only. Manual checks are done in a standalone IDE only.
- No new dependencies: the checkbox tree and the "Don't ask again" dialog are the platform's own.

## Out of Scope / Deferred

- Existing follow-ups this change leaves open:
  - F13: once "Don't ask again" is ticked, nothing brings the confirmation back. That needs a
    settings page for the plugin, which is bigger than this change.
  - F5, F6, F7, F8, F9, F10 and F11 are unrelated and stay open.
- Considered and rejected:
  - Remembering checks across IDE restarts: a check is a scratch pick, not a setting.
  - Promoting hidden follow-ups that are still checked: only what you can see is promoted.
  - Checkboxes on changes: no change action takes several changes.
  - Showing the targets in a tooltip without a confirmation: a hover is easy to miss, and a
    promote starts a Claude Code session.

## Resolves

None. F3, promoting several follow-ups together, was resolved by the change that added
multi-select. This change makes that easier to find.
