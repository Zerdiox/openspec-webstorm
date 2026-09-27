# Spec Delta

## Purpose

Seeing the project's specs inside the IDE: its capabilities, grouped by domain, and each
capability's requirements, opening them in the editor and copying them to paste elsewhere.

## ADDED Requirements

### Requirement: The panel has a Specs tab
The OpenSpec panel SHALL have two tabs: "Workflow", holding the changes and follow-ups, and "Specs",
holding the project's specs. The panel SHALL open on the Workflow tab. The Specs tab SHALL be there
in every project that has the panel, including one with no specs yet, where it says there are none.

#### Scenario: Switching to the specs
- **WHEN** the user opens the OpenSpec panel and clicks the Specs tab
- **THEN** the project's specs are shown, and the Workflow tab still holds the changes and follow-ups

#### Scenario: A project without specs
- **WHEN** a project that uses OpenSpec has no specs yet and the user opens the Specs tab
- **THEN** the tab says there are no specs, instead of showing an empty tree

### Requirement: Specs are listed by capability and domain
The Specs tab SHALL list each capability with its name and how many requirements it has, and
SHALL show the capability's purpose in the row's tooltip. A capability nested under a domain SHALL
be listed inside a group for that domain, and domains nested deeper SHALL be nested groups. A
capability that isn't nested SHALL be listed at the top level. Capabilities and domains SHALL be
ordered by name.

#### Scenario: A flat project
- **WHEN** a project has the capabilities "billing" and "search", with 12 and 4 requirements
- **THEN** both are listed at the top level in that order, showing 12 and 4

#### Scenario: A nested project
- **WHEN** a project has the capabilities "identity/user-auth", "identity/sessions" and "billing"
- **THEN** "billing" is listed at the top level, and "sessions" and "user-auth" are listed inside an "identity" group

#### Scenario: Hovering a capability
- **WHEN** the user hovers a capability's row
- **THEN** its tooltip shows the capability's purpose

### Requirement: A capability lists its requirements
Each capability SHALL expand to list its requirements by name, in the order its spec gives them.
A capability without requirements SHALL show that it has none.

#### Scenario: Expanding a capability
- **WHEN** the user expands a capability, collapsed at first, whose spec has three requirements
- **THEN** the three requirement names are listed under it in the spec's order

### Requirement: An unreadable spec is shown, not hidden
A spec the panel can't read SHALL still be listed under its capability's name, marked as unreadable
and why, and SHALL still open from its row.

#### Scenario: A spec that can't be read
- **WHEN** a capability's spec file can't be read
- **THEN** the capability is listed, marked as unreadable with the reason, and opening it opens the spec file

### Requirement: A spec row opens its spec
Pressing Enter or choosing Jump to Source on capability or requirement rows SHALL open the spec of
every selected row in the editor: a capability at the top of its spec, a requirement at its
heading. Double-clicking a requirement SHALL open it the same way. Double-clicking a capability or a
domain SHALL collapse or expand it, as for any group in the IDE's own trees.

#### Scenario: Opening a requirement
- **WHEN** the user double-clicks a requirement row
- **THEN** its capability's spec opens in the editor at that requirement's heading

#### Scenario: Opening a capability
- **WHEN** the user presses Enter on a capability row
- **THEN** the capability's spec opens in the editor at its top

#### Scenario: Double-clicking a capability
- **WHEN** the user double-clicks an expanded capability
- **THEN** it collapses, and no editor opens

### Requirement: Rows can be copied
With rows selected in the Specs tab, the IDE's Copy SHALL copy one line per selected row, in the
order they're listed: a domain's full name, a capability's full name including its domain, or a
requirement as its capability's full name, a colon and the requirement's name. A requirement's
context menu SHALL offer "Copy Requirement Text", which copies the whole text of every selected
requirement: its heading, its text and its scenarios, as the spec gives them, with a blank line
between requirements. The IDE's own Copy Path/Reference SHALL work on capability and requirement
rows and give their spec's file.

#### Scenario: Copying a requirement's name
- **WHEN** the user selects the requirement "Sessions expire after inactivity" of "identity/sessions" and presses the IDE's Copy shortcut
- **THEN** the clipboard holds "identity/sessions: Sessions expire after inactivity"

#### Scenario: Copying several rows
- **WHEN** the user selects the capability "billing" and one of its requirements and copies
- **THEN** the clipboard holds "billing" on one line and "billing: " followed by the requirement's name on the next

#### Scenario: Copying a requirement's text
- **WHEN** the user right-clicks a requirement and chooses Copy Requirement Text
- **THEN** the clipboard holds the requirement's heading, text and scenarios as the spec gives them

#### Scenario: Copying a spec's path
- **WHEN** the user selects a capability and uses the IDE's Copy Path/Reference
- **THEN** the IDE offers the paths of that capability's spec file

### Requirement: The Specs tree behaves like the Workflow tree
The Specs tab SHALL look and behave like the Workflow tab's tree: hovering, selecting,
multi-selecting, keyboard navigation and typing to find a capability or requirement by name SHALL
work as they do there. Domains SHALL start expanded and capabilities collapsed, so the
capabilities are in view and their requirements are one click away. A domain or capability the user
expands or collapses SHALL stay that way across refreshes and when the IDE restarts, independently
of the Workflow tab. The selection SHALL be kept across refreshes for every selected row still
listed.

#### Scenario: Finding a requirement by typing
- **WHEN** the Specs tab has focus and the user types part of a requirement's name
- **THEN** the first matching row is selected

#### Scenario: Opening the Specs tab for the first time
- **WHEN** the user opens the Specs tab in a project with the capabilities "billing" and "identity/user-auth"
- **THEN** the "identity" group is expanded showing "user-auth", and neither capability shows its requirements

#### Scenario: An expanded capability after a restart
- **WHEN** the user expands a capability, collapses a domain and restarts the IDE
- **THEN** the capability is still expanded and the domain still collapsed in the Specs tab, and the Workflow tab's groups are as they were

### Requirement: The Specs tab stays current
The Specs tab SHALL update when the project's spec files change and when the panel is shown, and
the panel's refresh action SHALL refresh it along with the Workflow tab. The Specs tab SHALL work
even where the OpenSpec command-line tool can't be found.

#### Scenario: A spec gains a requirement
- **WHEN** a requirement is added to a spec while the Specs tab is open
- **THEN** the requirement appears under its capability, and the capability's count goes up, without the user doing anything

#### Scenario: The OpenSpec tool is missing
- **WHEN** the OpenSpec command-line tool can't be found
- **THEN** the Workflow tab says it can't read the changes, and the Specs tab still lists the specs

### Requirement: The Specs tab starts no sessions
The Specs tab SHALL be for reading specs only: it SHALL offer no action that starts a Claude Code
session.

#### Scenario: A requirement's context menu
- **WHEN** the user right-clicks a requirement
- **THEN** the menu offers opening and copying, and no action that starts a Claude Code session
