# Spec Delta

## ADDED Requirements

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
