# Proposal

## Why

The OpenSpec panel is a stack of rows with buttons that doesn't behave like any other tool window in
the IDE. You can't select, open, right-click or keyboard-navigate a row. Reading a change or
follow-up means finding its file in the project tree. You can act on only one follow-up at a time.
Buttons crowd a narrow panel. The commit panel and the other tree-based tool windows set the
convention this panel should follow.

## What Changes

- **BREAKING (behaviour):** the panel becomes a tree, like the commit panel. "Changes" and
  "Follow-ups" are group nodes that show how many items they hold, and they collapse and expand.
  Hover, selection, keyboard navigation, speed search (typing to find a row) and screen-reader
  support are the IDE's own.
  - A change's row shows its name and progress.
  - A follow-up's row shows its ID and title, with its type and capability in secondary text when
    there's room, and in the row's tooltip always.
- **BREAKING (behaviour):** actions move from buttons on each row to a toolbar at the top of the
  panel and to the context menu, and act on the selected rows.
  - The toolbar holds Explore…, Propose… and Backlog review, then the selection's actions.
  - With one change selected, the main button is its likely next step, and its dropdown holds the
    rest. The next step is chosen from the change's tasks as it is today.
  - The context menu shows Jump to Source, then the same actions.
- Opening files:
  - Double-click, Enter or Jump to Source (F4) opens the selected rows' files.
  - A change always opens its proposal. A change with no proposal yet has its folder selected in
    the project tree.
  - A follow-up opens its own file, including one the panel can't read.
- Multi-select:
  - The IDE's usual Ctrl/Shift-click and Shift-arrow select several rows.
  - Several follow-ups can be promoted together in one Claude Code session.
  - With several changes selected, or changes and follow-ups mixed, only opening is available, and
    the actions say why they're disabled.
- Follow-ups can be filtered from the toolbar by type and by capability, and grouped by type, by
  capability or not at all. Filter and grouping are remembered for the project. Grouping shows the
  metadata as group headings, so it stays visible however narrow the panel is.
- The selection, the collapsed groups, the filter and the grouping survive the panel's automatic
  refreshes. The collapsed groups, the filter and the grouping are also remembered when the IDE
  restarts.
- Nothing looks like a link: there are no underlines or hand cursors.
- The tree set-up is written with no knowledge of OpenSpec, so other lists in the plugin can reuse
  it: nodes kept by key, selection and expansion restored by key, collapsed groups remembered, and
  opening wired up. FU-0005, the spec browser, is the likely next user.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `openspec-workflow-panel`:
  - The lists become a tree with selectable, openable rows, multi-select, and filtering and
    grouping for follow-ups.
  - Actions act on the selection from a toolbar and the context menu.
  - "A change's likely next step is one click away" now applies once the change is selected.
  - "Rows keep their actions in view" becomes "Actions stay in view": the toolbar never scrolls
    away, and a row too long for the panel shows in full on hover instead of being shortened with
    "…".

## Impact

- The panel's layout code is rewritten: a tree with a renderer, a toolbar, a context menu, and
  filter and grouping actions. The tool window's title-bar Refresh action stays.
- The panel's model:
  - Each row gains a stable key and the file or folder it opens.
  - The actions are worked out for a selection rather than for a single row, including Promote with
    several follow-up IDs.
  - Filtering and grouping of follow-ups are added.
- Existing panel model tests change with the model. The command sent for Promote with one
  follow-up is unchanged.
- Code runs in the backend module only, where the panel lives. Opening editors, the toolbar and the
  context menu need checking under remote development (split mode).
- No new dependencies: still only the platform module and the bundled Terminal plugin.

## Out of Scope / Deferred

- Considered and rejected:
  - Opening a change's tasks instead of its proposal while it's being implemented, because a row's
    target shouldn't change as the change progresses.
  - Link styling for titles (hand cursor, hover underline), because the IDE's trees don't do this.
  - Running a change action on several changes at once, for example one session per change. It
    would start several Claude Code sessions from one click; the actions stay disabled instead.
  - Filtering or grouping changes: changes have no type or capability to filter on.
- Candidate follow-ups, not recorded:
  - An action button that appears on a row when you hover it, if select-then-act turns out to cost
    too much in daily use.
  - FU-0002's `location`, FU-0003's and FU-0004's all still point at the panel's old path under
    `src/`; they're resolved by this change anyway.
- Existing follow-ups this change leaves open:
  - FU-0005, the spec browser, stays open. It's the expected second user of the reusable tree
    set-up.
  - FU-0006, FU-0007 and FU-0008 are unrelated and stay open.

## Resolves

- FU-0002: clicking a change or follow-up opens its file. It does, through the IDE's tree
  convention (select, then double-click, Enter or F4) rather than a link.
- FU-0003: promote several follow-ups together from the panel, through multi-select and Promote.
- FU-0004: in a narrow panel, the top row's buttons overlap. The buttons move to the IDE's toolbar,
  which handles a narrow panel itself.
