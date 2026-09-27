# Spec Delta

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

The context menu SHALL list Jump to Source first, then only the entries that apply to the kinds of
rows selected: Open and the change actions when a change is selected, and Promote when a follow-up
is selected. The context menu's Promote SHALL act on the selected follow-ups only, whatever is
checked. An entry listed that can't run for the selection, such as a change action or Open with
several changes selected, SHALL be shown disabled. An action that isn't set up in the project SHALL
be disabled with the same explanation as today.

#### Scenario: The context menu of a change
- **WHEN** the user right-clicks a change with tasks in progress
- **THEN** the change is selected and a menu opens with Jump to Source, then Open, then Apply, Verify, Archive and Explore

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
- **THEN** the change actions in the toolbar are disabled with a tooltip saying to select one change, the context menu shows them and Open disabled, and Jump to Source is still available

#### Scenario: Nothing selected
- **WHEN** no row is selected and no follow-up is checked
- **THEN** Explore, Propose and Backlog review are available in the toolbar, and the selection's actions are disabled

## ADDED Requirements

### Requirement: A change's artifacts open from its context menu
A change's context menu SHALL offer an Open submenu with Proposal, Design, Tasks and Specs, which
open the change's proposal, design and task list in the editor. Specs SHALL be a submenu with one
entry per spec the change adds or modifies, labelled with the capability's name (including its
domain for a nested capability) and ordered by that name; choosing one opens that spec in the
editor. An entry whose file the change doesn't have yet SHALL be shown disabled with a tooltip
saying so; Specs SHALL be shown disabled when the change has no specs. A file written outside the
IDE SHALL be offered and opened without the user having to refresh. Double-clicking a change,
pressing Enter and Jump to Source SHALL keep opening its proposal.

#### Scenario: Opening a change's tasks
- **WHEN** the user right-clicks a change that has a task list and chooses Open, then Tasks
- **THEN** the change's task list opens in the editor

#### Scenario: Opening one of a change's specs
- **WHEN** the user right-clicks a change that modifies the specs of two capabilities and opens Open, then Specs
- **THEN** the submenu lists both capabilities by name, and choosing one opens that change's spec for it in the editor

#### Scenario: An artifact the change doesn't have yet
- **WHEN** the user right-clicks a change that has a proposal but no design and opens Open
- **THEN** Design is shown disabled with a tooltip saying the change has no design yet, and Proposal, Tasks and Specs follow what the change has

#### Scenario: A change without specs
- **WHEN** the user right-clicks a change that has no specs yet and opens Open
- **THEN** Specs is shown disabled with a tooltip saying the change has no specs yet

#### Scenario: Several changes selected
- **WHEN** the user selects two changes and right-clicks one of them
- **THEN** Open is shown disabled with a tooltip saying to select one change

#### Scenario: Double-click still opens the proposal
- **WHEN** the user double-clicks a change that has a proposal, a design and a task list
- **THEN** its proposal opens in the editor
