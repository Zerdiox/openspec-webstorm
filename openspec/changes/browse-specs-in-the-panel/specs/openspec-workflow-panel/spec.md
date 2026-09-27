# Spec Delta

## MODIFIED Requirements

### Requirement: The lists are a tree like the IDE's own
The panel's Workflow tab SHALL show its changes and follow-ups as a tree that looks and behaves like
the IDE's own trees, such as the commit panel. "Changes" and, where the project has a backlog,
"Follow-ups" SHALL be groups that show how many items they hold and that collapse and expand.
Hovering, selecting, keyboard navigation and typing to find a row SHALL work as they do in the IDE's
own trees. A change's row SHALL show its name and progress. A follow-up's row SHALL show its ID and
title, with its type and capability after them when there's room and in the row's tooltip always.
Rows SHALL NOT look like links: no underline and no hand cursor. The selection SHALL be kept when
the panel refreshes, for every selected item still listed. Groups SHALL start expanded, and
collapsed groups SHALL stay collapsed across refreshes and when the IDE restarts.

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

#### Scenario: Changes and follow-ups are on the Workflow tab
- **WHEN** the user opens the OpenSpec panel
- **THEN** the Workflow tab is shown, holding the "Changes" and "Follow-ups" groups
