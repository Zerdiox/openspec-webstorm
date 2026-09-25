# openspec-workflow-panel Specification

## Purpose

Seeing a project's OpenSpec changes and follow-ups inside the IDE, and starting each OpenSpec
workflow step on them in a Claude Code session with one click.

## Requirements

### Requirement: The panel appears only where OpenSpec is used
The IDE SHALL offer an "OpenSpec" panel in a project that uses OpenSpec, and SHALL NOT offer it in a
project that doesn't, so it never adds clutter where it can do nothing.

#### Scenario: A project with OpenSpec
- **WHEN** a project that uses OpenSpec is opened
- **THEN** an "OpenSpec" panel is available

#### Scenario: A project without OpenSpec
- **WHEN** a project that doesn't use OpenSpec is opened
- **THEN** no "OpenSpec" panel is offered

### Requirement: Changes are listed with their progress
The panel SHALL list the project's in-flight OpenSpec changes, each with how many of its tasks are
done out of how many there are.

#### Scenario: Two changes in flight
- **WHEN** a project has two in-flight changes, one with 3 of 10 tasks done
- **THEN** both are listed, and that one shows 3 of 10

#### Scenario: Changes can't be read
- **WHEN** the project's changes can't be read
- **THEN** the panel says so and why, instead of showing an empty list

### Requirement: Open follow-ups are listed where the project has a backlog
In a project with a follow-ups backlog, the panel SHALL list the open follow-ups with their ID,
title, type and the capability they concern. It SHALL NOT list resolved follow-ups, and SHALL show a
follow-up it can't fully read rather than leave it out.

#### Scenario: A project with open and resolved follow-ups
- **WHEN** a project has two open follow-ups and one resolved
- **THEN** the two open ones are listed and the resolved one is not

#### Scenario: A follow-up that can't be read
- **WHEN** an open follow-up's details are unreadable
- **THEN** it is still listed, identified by its file and marked as unreadable

#### Scenario: A project without a backlog
- **WHEN** a project has no follow-ups backlog
- **THEN** the panel shows no follow-ups section and no follow-up actions

### Requirement: Each action starts a Claude Code session
Every action SHALL open a new terminal tab in the IDE, in the project, named after the action and
its target, and start Claude Code there with the matching OpenSpec command already sent. The
conversation then continues in that tab like any Claude Code session.

#### Scenario: Applying a change
- **WHEN** the user chooses Apply on a change named plan-panel-fixes
- **THEN** a new terminal tab named for applying plan-panel-fixes opens, running Claude Code with the command to apply that change

#### Scenario: Exploring an idea
- **WHEN** the user chooses Explore and enters a short description
- **THEN** a new terminal tab opens running Claude Code with the explore command and that description

#### Scenario: Exploring a change
- **WHEN** the user chooses Explore on a change named plan-panel-fixes
- **THEN** without being asked for a description, a new terminal tab named for exploring plan-panel-fixes opens, running Claude Code with the explore command and that change's name

#### Scenario: Promoting a follow-up
- **WHEN** the user chooses Promote on follow-up FU-0033
- **THEN** a new terminal tab opens running Claude Code with the command to promote FU-0033

### Requirement: The available actions
The panel SHALL offer Explore, Propose and Backlog review for the project; Explore, Apply, Verify
and Archive for each change; and Promote for each open follow-up. Backlog review and Promote SHALL
be offered only where the project has a follow-ups backlog.

#### Scenario: A change row
- **WHEN** a change is listed
- **THEN** Explore, Apply, Verify and Archive are offered for it

### Requirement: Commands match the project's own setup
The command each action sends SHALL match how OpenSpec is set up in that project, so projects set up
with OpenSpec's skills and projects set up with its default commands both work unchanged.

#### Scenario: A project set up with OpenSpec's default commands
- **WHEN** the user chooses Apply in a project set up with OpenSpec's default commands
- **THEN** the session is started with that setup's apply command, not another setup's

### Requirement: Descriptions reach Claude Code intact
Text the user types for Explore or Propose SHALL reach Claude Code exactly as typed, whatever quotes,
symbols or line breaks it contains.

#### Scenario: A description with quotes and symbols
- **WHEN** the user proposes "Don't show $cost when it's 0"
- **THEN** Claude Code receives exactly that text

### Requirement: A missing tool is reported, not hidden
If Claude Code can't be found, an action SHALL say so rather than open a terminal tab that fails.

#### Scenario: Claude Code not installed
- **WHEN** the user chooses an action and Claude Code can't be found
- **THEN** the IDE reports that Claude Code wasn't found, and no terminal tab is opened

### Requirement: The lists stay current
The lists SHALL update when the project's OpenSpec files change and when the panel is shown, and the
user SHALL be able to refresh them on demand.

#### Scenario: A change is archived
- **WHEN** a change is archived while the panel is open
- **THEN** it disappears from the list without the user doing anything

### Requirement: Works in every JetBrains IDE
The plugin SHALL work the same in every IntelliJ-based IDE, including WebStorm and PhpStorm.

#### Scenario: Installed in PhpStorm
- **WHEN** the plugin is installed in PhpStorm
- **THEN** it loads, and the panel behaves as it does in WebStorm

### Requirement: A change's likely next step is one click away
Each change SHALL offer one of its actions as a single click, chosen from its tasks: Explore while it
has no tasks, Apply while some of its tasks remain, and Verify once all of them are done. Its other
actions SHALL be one more click away. Archive SHALL never be the single-click action, since it is the
one that is hard to undo.

#### Scenario: A change still being planned
- **WHEN** a change has no tasks yet
- **THEN** Explore is its one-click action, and Apply, Verify and Archive are one more click away

#### Scenario: A change being implemented
- **WHEN** a change has 3 of 10 tasks done
- **THEN** Apply is its one-click action, and Explore, Verify and Archive are one more click away

#### Scenario: A change with all tasks done
- **WHEN** a change has 10 of 10 tasks done
- **THEN** Verify is its one-click action, and Archive is one more click away

### Requirement: Rows keep their actions in view
Every change and follow-up row SHALL fit the panel's width, so its actions stay visible without
scrolling sideways however long its title is. A title that doesn't fit SHALL be shortened, and the
user SHALL be able to see the full title by hovering it.

#### Scenario: A follow-up with a long title
- **WHEN** a follow-up's title is longer than the panel is wide
- **THEN** its title is shown shortened, its Promote action is fully visible, and hovering the title shows it in full

#### Scenario: A change with a long name
- **WHEN** a change's name is longer than the panel is wide
- **THEN** its name is shown shortened, its actions are fully visible, and hovering the name shows it in full
