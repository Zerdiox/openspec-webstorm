# Proposal

## Why

A change or follow-up with a long title pushes its buttons past the right edge of the OpenSpec panel,
so the user has to scroll sideways to reach them. Each change row also spends most of its width on
three side-by-side buttons. The Explore/Propose text box has its text pressed against the edge, and
the panel's controls don't behave like the IDE's own tool windows. This change is about the panel's
layout and usability: every action should stay reachable, the likely next step should be one click
away, and the panel should look and feel native.

## What Changes

- Rows fit the panel's width. A title too long for the row is shortened with "…", and hovering it
  shows the full title, for changes as well as follow-ups. A row's actions stay visible however long
  its title is.
- A change's actions become one native split button: the likely next step is the button, and the
  other actions are in its dropdown. The likely next step follows the change's tasks: Explore while
  it has none, Apply while some remain, Verify once all are done. Archive is never the one-click
  action.
- New action: Explore on a change, which starts a Claude Code session exploring that change by name,
  with no description asked for.
- Promote stays the only action on a follow-up, with a tooltip that says it explores the follow-up
  as a candidate change.
- Refresh moves from its own row at the bottom of the panel to an icon in the panel's title bar.
- The Explore/Propose description box gets inner padding.
- Controls keep the IDE's own look and hover behaviour: no custom animations or hand cursors.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `openspec-workflow-panel`: changes gain an Explore action; a change's likely next step is offered
  as its one-click action with the rest one click further; rows keep their actions in view whatever
  the title's length.

## Impact

- Panel layout and controls in the tool window, the tool window's title bar, and the Explore/Propose
  description dialog.
- The panel's model of a change row: which actions it offers and which one is the default.
- Existing panel model tests change with the row's actions; no new dependencies, still only the
  platform module and the bundled Terminal plugin.

## Out of Scope / Deferred

- FU-0001 (reworked terminal tabs under remote development) is unrelated and stays open.
- Custom hover effects, transitions or hand cursors on buttons: the IDE's own buttons don't have
  them, and the panel should match.
- A keymap entry or Find Action entry for Refresh: the panel already refreshes itself when files
  change and when it's shown, so manual refresh is a fallback.
- FU-0003 (promote several follow-ups together from the panel) is deferred.
- FU-0002 (clicking a title opens its file) is related, since it touches the same row titles, but
  is deferred.

## Resolves

None.
