# Spec Delta

## MODIFIED Requirements

### Requirement: Open follow-ups are listed where the project has a backlog
In a project with a follow-ups backlog, the panel SHALL list the open follow-ups with their ID,
title, type and the capability they concern. It SHALL NOT list resolved follow-ups, and SHALL show a
follow-up it can't fully read rather than leave it out. A follow-up it can't read SHALL be shown by
its file name, once, marked as unreadable with why: the file has no frontmatter, its frontmatter
isn't closed, its frontmatter has a YAML error on a given line, or its frontmatter isn't a list of
fields. Hovering the row SHALL show the full reason. A follow-up it can't read SHALL NOT be offered
for promoting.

#### Scenario: A project with open and resolved follow-ups
- **WHEN** a project has two open follow-ups and one resolved
- **THEN** the two open ones are listed and the resolved one is not

#### Scenario: A follow-up that can't be read
- **WHEN** an open follow-up's details are unreadable
- **THEN** it is still listed, identified by its file name shown once, and marked as unreadable with why

#### Scenario: A follow-up with a YAML error
- **WHEN** the follow-up file F40-broken.md has a YAML error on line 4 of the file
- **THEN** its row reads "F40-broken.md", marked as unreadable because of a YAML error on line 4, and hovering it shows the YAML error's full message

#### Scenario: A follow-up without frontmatter
- **WHEN** the follow-up file notes.md has no frontmatter
- **THEN** its row reads "notes.md" once, marked as unreadable because it has no frontmatter

#### Scenario: An unreadable follow-up can't be promoted
- **WHEN** the user selects a follow-up marked as unreadable
- **THEN** its row has no checkbox, and Promote is disabled with a tooltip saying an unreadable follow-up can't be promoted

#### Scenario: A project without a backlog
- **WHEN** a project has no follow-ups backlog
- **THEN** the panel shows no follow-ups section and no follow-up actions

### Requirement: A row opens its file
Double-clicking a change or follow-up row, pressing Enter, or choosing Jump to Source (from the
context menu, or with the IDE's Jump to Source shortcut) SHALL open the file of every selected row
in the editor. For a change this SHALL always be its proposal. For a change with no proposal yet,
its folder SHALL be selected in the project tree instead. For a follow-up it SHALL be the
follow-up's own file, including a follow-up the panel can't read, which SHALL open at the line of
the problem. Double-clicking a group SHALL collapse or expand it, as in the IDE's own trees.

#### Scenario: Opening a change
- **WHEN** the user double-clicks a change that has a proposal, whatever its progress
- **THEN** the change's proposal opens in the editor

#### Scenario: Opening a change without a proposal
- **WHEN** the user presses Enter on a change that has no proposal yet
- **THEN** the change's folder is selected in the project tree

#### Scenario: Opening an unreadable follow-up
- **WHEN** the user selects a follow-up marked as unreadable because of a YAML error on line 4 and uses the IDE's Jump to Source shortcut
- **THEN** its file opens in the editor with the caret on line 4

#### Scenario: Opening several rows
- **WHEN** the user selects two follow-ups and a change and presses Enter
- **THEN** both follow-ups' files and the change's proposal open in the editor
