# Spec Delta

## ADDED Requirements

### Requirement: Follow-ups can be checked
Each follow-up row that can be promoted SHALL have a checkbox, as the commit panel has for its
files. A follow-up without an ID SHALL have none, and change rows SHALL have none. The "Follow-ups"
group, and each group of follow-ups when grouping that holds a follow-up that can be promoted,
SHALL have a checkbox that shows whether none,
some or all of the follow-ups shown under it are checked, and that checks or unchecks all of them.
Clicking the checkbox or pressing Space SHALL toggle it. Clicking elsewhere on a row SHALL select
the row without changing its checkbox. A check SHALL be kept when the panel refreshes, when the
grouping changes, and while a filter hides the follow-up. It SHALL NOT be kept when the IDE
restarts, and a resolved follow-up SHALL no longer count as checked.

#### Scenario: Checking a follow-up
- **WHEN** the user clicks a follow-up's checkbox
- **THEN** the follow-up is checked, and the selection behaves as it does when checking a file in the commit panel

#### Scenario: Checking with the keyboard
- **WHEN** a follow-up row is selected and the user presses Space
- **THEN** its checkbox toggles

#### Scenario: Selecting a row doesn't check it
- **WHEN** the user clicks a follow-up's title
- **THEN** the row is selected and its checkbox doesn't change

#### Scenario: A group's checkbox
- **WHEN** two of the three follow-ups shown under the "Follow-ups" group are checked
- **THEN** the group's checkbox shows that some are checked, and clicking it checks all three

#### Scenario: A checked follow-up hidden by a filter
- **WHEN** a checked follow-up is hidden by a filter, and the filter is later cleared
- **THEN** it is not counted as checked while hidden, and is shown checked again once the filter is cleared

#### Scenario: Checks after a restart
- **WHEN** the user checks two follow-ups and restarts the IDE
- **THEN** no follow-up is checked

#### Scenario: A follow-up without an ID
- **WHEN** a follow-up has no ID
- **THEN** its row has no checkbox

## MODIFIED Requirements

### Requirement: Actions act on the selection
The panel SHALL offer its actions in a toolbar at the top of the panel and in each row's context
menu. The toolbar SHALL always offer Explore, Propose and, where the project has a backlog, Backlog
review. The toolbar SHALL offer the actions for the current selection:
- With one change selected: its actions.
- Promote, for the checked follow-ups that are shown. With none checked, for the selected
  follow-ups when one or more follow-ups and nothing else are selected.
- Otherwise, with nothing selected, a group selected, several changes selected, or changes and
  follow-ups selected together: the selection's actions SHALL be shown disabled, with a tooltip
  saying why.

Promote SHALL say what it will promote: the follow-up's ID for one, and how many for several.

The context menu SHALL list Jump to Source first, then only the actions that apply to the kinds of
rows selected: the change actions when a change is selected, and Promote when a follow-up is
selected. The context menu's Promote SHALL act on the selected follow-ups only, whatever is checked.
An action listed that can't run for the selection, such as a change action with several changes
selected, SHALL be shown disabled. An action that isn't set up in the project SHALL be disabled with
the same explanation as today.

#### Scenario: The context menu of a change
- **WHEN** the user right-clicks a change with tasks in progress
- **THEN** the change is selected and a menu opens with Jump to Source, then Apply, Verify, Archive and Explore

#### Scenario: The context menu of a follow-up
- **WHEN** the user right-clicks follow-up F6
- **THEN** the follow-up is selected and a menu opens with Jump to Source, then Promote labelled with F6, and no change actions

#### Scenario: The context menu ignores checks
- **WHEN** follow-ups F5 and F7 are checked, and the user right-clicks F6 and chooses Promote
- **THEN** only F6 is promoted

#### Scenario: The toolbar promotes the checked follow-ups
- **WHEN** follow-ups F5 and F7 are checked and F6 is selected
- **THEN** the toolbar's Promote says it will promote 2, and promotes F5 and F7

#### Scenario: The toolbar falls back to the selection
- **WHEN** no follow-up is checked and F6 is selected
- **THEN** the toolbar's Promote is labelled with F6 and promotes F6

#### Scenario: Several changes selected
- **WHEN** the user selects two changes and no follow-up is checked
- **THEN** the change actions in the toolbar are disabled with a tooltip saying to select one change, the context menu shows them disabled, and Jump to Source is still available

#### Scenario: Nothing selected
- **WHEN** no row is selected and no follow-up is checked
- **THEN** Explore, Propose and Backlog review are available in the toolbar, and the selection's actions are disabled

### Requirement: Several follow-ups can be promoted together
The user SHALL be able to check several follow-ups, or select them with the IDE's usual multi-select
gestures, and promote them in one Claude Code session, whose promote command carries all their IDs
in the order they are listed. Before promoting several follow-ups from the toolbar, the panel SHALL
ask for confirmation, listing each follow-up's ID and title, with the IDE's usual choice not to be
asked again. Promoting a single follow-up, or promoting from the context menu, SHALL NOT ask.

#### Scenario: Promoting two follow-ups
- **WHEN** the user checks follow-ups F3 and F4, chooses Promote in the toolbar and confirms
- **THEN** one new terminal tab opens running Claude Code with the command to promote F3 and F4

#### Scenario: Cancelling the confirmation
- **WHEN** the user checks two follow-ups, chooses Promote in the toolbar and cancels the confirmation
- **THEN** no terminal tab opens and both follow-ups stay checked

#### Scenario: Not asked again
- **WHEN** the user has confirmed a promote with the choice not to be asked again, then promotes several follow-ups from the toolbar
- **THEN** the terminal tab opens without asking

#### Scenario: Promoting one follow-up
- **WHEN** the user checks only F3 and chooses Promote in the toolbar
- **THEN** the terminal tab opens without asking

#### Scenario: Promoting several from the context menu
- **WHEN** the user selects F3 and F4, right-clicks one of them and chooses Promote
- **THEN** the terminal tab opens without asking, with the command to promote F3 and F4
