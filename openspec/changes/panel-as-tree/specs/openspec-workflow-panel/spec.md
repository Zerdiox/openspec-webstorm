# Spec Delta

## ADDED Requirements

### Requirement: The lists are a tree like the IDE's own
The panel SHALL show its changes and follow-ups as a tree that looks and behaves like the IDE's own
trees, such as the commit panel. "Changes" and, where the project has a backlog, "Follow-ups" SHALL
be groups that show how many items they hold and that collapse and expand. Hovering, selecting,
keyboard navigation and typing to find a row SHALL work as they do in the IDE's own trees. A change's
row SHALL show its name and progress. A follow-up's row SHALL show its ID and title, with its type and
capability after them when there's room and in the row's tooltip always. Rows SHALL NOT look like
links: no underline and no hand cursor. The selection SHALL be kept when the panel refreshes, for
every selected item still listed. Groups SHALL start expanded, and collapsed groups SHALL stay
collapsed across refreshes and when the IDE restarts.

#### Scenario: Hovering and selecting a row
- **WHEN** the user moves the mouse over a follow-up row and clicks it
- **THEN** the row is highlighted while hovered, becomes selected after the click, and the cursor stays the normal pointer

#### Scenario: Finding a row by typing
- **WHEN** the panel has focus and the user types part of a follow-up's title
- **THEN** the first matching row is selected

#### Scenario: A collapsed group after a refresh or restart
- **WHEN** the user collapses the "Changes" group, then the panel refreshes or the IDE restarts
- **THEN** the group is still collapsed and still shows how many changes there are

#### Scenario: The panel refreshes while a row is selected
- **WHEN** a change is selected and the panel refreshes because a file under OpenSpec changed
- **THEN** the same change is still selected

#### Scenario: The selected row disappears
- **WHEN** the selected follow-up is resolved and the panel refreshes
- **THEN** it is no longer listed or selected, and the panel shows no error

### Requirement: A row opens its file
Double-clicking a change or follow-up row, pressing Enter, or choosing Jump to Source (from the
context menu, or with the IDE's Jump to Source shortcut) SHALL open the file of every selected row
in the editor. For a change this SHALL always be its proposal. For a change with no proposal yet,
its folder SHALL be selected in the project tree instead. For a follow-up it SHALL be the
follow-up's own file, including a follow-up the panel can't read. Double-clicking a group SHALL
collapse or expand it, as in the IDE's own trees.

#### Scenario: Opening a change
- **WHEN** the user double-clicks a change that has a proposal, whatever its progress
- **THEN** the change's proposal opens in the editor

#### Scenario: Opening a change without a proposal
- **WHEN** the user presses Enter on a change that has no proposal yet
- **THEN** the change's folder is selected in the project tree

#### Scenario: Opening an unreadable follow-up
- **WHEN** the user selects a follow-up marked as unreadable and uses the IDE's Jump to Source shortcut
- **THEN** its file opens in the editor

#### Scenario: Opening several rows
- **WHEN** the user selects two follow-ups and a change and presses Enter
- **THEN** both follow-ups' files and the change's proposal open in the editor

### Requirement: Actions act on the selection
The panel SHALL offer its actions in a toolbar at the top of the panel and in each row's context
menu. The toolbar SHALL always offer Explore, Propose and, where the project has a backlog, Backlog
review. The toolbar and the context menu SHALL offer the actions for the current selection:
- With one change selected: its actions.
- With one or more follow-ups selected, and nothing else: Promote.
- With nothing selected, a group selected, several changes selected, or changes and follow-ups
  selected together: the selection's actions SHALL be shown disabled, with a tooltip saying why.

The context menu SHALL list Jump to Source first, then the selection's actions. An action that isn't
set up in the project SHALL be disabled with the same explanation as today.

#### Scenario: The context menu of a change
- **WHEN** the user right-clicks a change with tasks in progress
- **THEN** the change is selected and a menu opens with Jump to Source, then Apply, Verify, Archive and Explore

#### Scenario: Several changes selected
- **WHEN** the user selects two changes
- **THEN** the change actions in the toolbar and context menu are disabled with a tooltip saying to select one change, and Jump to Source is still available

#### Scenario: Nothing selected
- **WHEN** no row is selected
- **THEN** Explore, Propose and Backlog review are available in the toolbar, and the selection's actions are disabled

### Requirement: Several follow-ups can be promoted together
The user SHALL be able to select several follow-ups with the IDE's usual multi-select gestures and
promote them in one Claude Code session, whose promote command carries all their IDs in the order
they are listed.

#### Scenario: Promoting two follow-ups
- **WHEN** the user selects follow-ups FU-0003 and FU-0004 and chooses Promote
- **THEN** one new terminal tab opens running Claude Code with the command to promote FU-0003 and FU-0004

### Requirement: Follow-ups can be filtered and grouped
From the toolbar, the user SHALL be able to filter the follow-ups by type and by capability, and to
group them by type, by capability or not at all. Group headings SHALL show how many follow-ups they
hold. While a filter hides any follow-ups, the "Follow-ups" group SHALL show that it is filtered,
and its count SHALL be the number of follow-ups shown. A follow-up the panel can't read SHALL always
be shown, in a group of its own when grouping. Filtering and grouping SHALL be remembered for the
project, across refreshes and when the IDE restarts. Changes SHALL NOT be affected.

#### Scenario: Filtering by type
- **WHEN** the user filters follow-ups to show only bugs and tech-debt
- **THEN** only follow-ups of those types are listed, and the "Follow-ups" group shows it is filtered with the number shown

#### Scenario: Grouping by type
- **WHEN** the user groups follow-ups by type
- **THEN** the follow-ups are listed under one group per type, each showing its count

#### Scenario: An unreadable follow-up under a filter
- **WHEN** a filter is active that matches no unreadable follow-up
- **THEN** an unreadable follow-up is still listed

#### Scenario: Filter and grouping after a restart
- **WHEN** the user groups follow-ups by capability, filters out ideas, and restarts the IDE
- **THEN** the follow-ups are still grouped by capability with ideas filtered out

## RENAMED Requirements

- FROM: `### Requirement: Rows keep their actions in view`
- TO: `### Requirement: Actions stay in view`

## MODIFIED Requirements

### Requirement: A change's likely next step is one click away
Once a change is selected, one of its actions SHALL be a single click away as the toolbar's main
action, chosen from the change's tasks: Explore while it has no tasks, Apply while some of its tasks
remain, and Verify once all of them are done. Its other actions SHALL be one more click away, in
that action's dropdown and in the change's context menu. Archive SHALL never be the single-click
action, since it is the one that is hard to undo.

#### Scenario: A change still being planned
- **WHEN** a change with no tasks yet is selected
- **THEN** Explore is the toolbar's main action, and Apply, Verify and Archive are one more click away

#### Scenario: A change being implemented
- **WHEN** a change with 3 of 10 tasks done is selected
- **THEN** Apply is the toolbar's main action, and Explore, Verify and Archive are one more click away

#### Scenario: A change with all tasks done
- **WHEN** a change with 10 of 10 tasks done is selected
- **THEN** Verify is the toolbar's main action, and Archive is one more click away

### Requirement: Actions stay in view
The panel's actions SHALL stay visible however long the listed titles are and however narrow the
panel is: the toolbar SHALL NOT scroll away with the list, and actions that don't fit the toolbar's
width SHALL stay reachable from it. A row too long for the panel's width SHALL show in full when the
user hovers it.

#### Scenario: A follow-up with a long title
- **WHEN** a follow-up's title is longer than the panel is wide
- **THEN** the toolbar's actions stay visible, and hovering the row shows it in full

#### Scenario: A change with a long name
- **WHEN** a change's name is longer than the panel is wide
- **THEN** the toolbar's actions stay visible, and hovering the change's row shows its name in full

#### Scenario: A narrow panel
- **WHEN** the panel is made narrower than its toolbar's actions
- **THEN** no action overlaps another, and every action is still reachable from the toolbar
