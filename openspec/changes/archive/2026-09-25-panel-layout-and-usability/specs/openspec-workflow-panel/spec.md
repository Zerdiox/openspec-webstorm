# Spec Delta

## MODIFIED Requirements

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

## ADDED Requirements

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
