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
- **WHEN** the user chooses Promote on follow-up F33
- **THEN** a new terminal tab opens running Claude Code with the command to promote F33

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
Once a change is selected, one of its actions SHALL be a single click away as the toolbar's main
action, chosen from the change's tasks: Explore while it has no tasks, Apply while some of its tasks
remain, and Verify once all of them are done. Its other actions SHALL be one more click away, in a
dropdown beside that action and in the change's context menu. Archive SHALL never be the single-click
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

### Requirement: Claude Code fills its terminal tab
The terminal tab an action opens SHALL show Claude Code at the tab's full width and height from its
first screen, and SHALL keep it fitted whenever the tab is resized, without the user doing anything.
This holds in a standalone IDE and under remote development alike.

#### Scenario: A tab opened under remote development
- **WHEN** the user chooses Apply on a change in an IDE used through remote development
- **THEN** the new tab shows Claude Code at the tab's full width and height, with nothing left over from an earlier screen and no resize needed

#### Scenario: The tab is made taller
- **WHEN** the user enlarges the terminal holding a Claude Code tab the panel opened
- **THEN** Claude Code grows to fill the new height as well as the new width

#### Scenario: A standalone IDE
- **WHEN** the user chooses an action in a standalone IDE
- **THEN** the new tab shows Claude Code at the tab's full width and height

### Requirement: Works under remote development when installed on both sides
Under remote development the plugin SHALL work when it is installed both on the backend and in the
client: the OpenSpec panel appears once, and an action's terminal tab opens for the user who chose
the action.

#### Scenario: Installed on the backend and in the client
- **WHEN** the plugin is installed on the backend and in the client, and the user opens a project that uses OpenSpec
- **THEN** exactly one OpenSpec panel is available, and its actions open their terminal tabs in that user's client

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
