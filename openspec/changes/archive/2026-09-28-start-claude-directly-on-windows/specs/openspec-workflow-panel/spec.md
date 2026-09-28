## MODIFIED Requirements

### Requirement: Descriptions reach Claude Code intact
Text the user types for Explore or Propose SHALL reach Claude Code exactly as typed, whatever quotes,
symbols or line breaks it contains.

#### Scenario: A description with quotes and symbols
- **WHEN** the user proposes "Don't show $cost when it's 0"
- **THEN** Claude Code receives exactly that text

#### Scenario: A description on Windows
- **WHEN** the user proposes a description with quotes, symbols and line breaks in an IDE on Windows, whichever shell its terminal runs
- **THEN** Claude Code receives exactly that text

### Requirement: A missing tool is reported, not hidden
If Claude Code can't be found, an action SHALL say so rather than open a terminal tab that fails.

#### Scenario: Claude Code not installed
- **WHEN** the user chooses an action and Claude Code can't be found
- **THEN** the IDE reports that Claude Code wasn't found, and no terminal tab is opened

#### Scenario: Claude Code installed with npm on Windows
- **WHEN** the user chooses an action on Windows and the only Claude Code found is npm's command-line launcher rather than a program
- **THEN** the IDE reports that this Claude Code can't be started and to install it with its native installer, and no terminal tab is opened
