# Spec Delta

## MODIFIED Requirements

### Requirement: Each action starts a Claude Code session
Every action SHALL open a new terminal tab in the IDE, in the project, named after the action and
its target, and start Claude Code there with the matching OpenSpec command already sent. The
conversation then continues in that tab like any Claude Code session. The tab SHALL keep that name
while Claude Code runs in it.

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

#### Scenario: The tab keeps its name while Claude Code runs
- **WHEN** the user chooses Apply on a change named plan-panel-fixes and Claude Code starts working in the new tab
- **THEN** the tab is still named for applying plan-panel-fixes, not after Claude Code's session
