## ADDED Requirements

### Requirement: Works on Windows
The panel SHALL list changes and start Claude Code sessions on Windows as it does on macOS and
Linux, with tools installed the usual Windows way, such as npm's global install.

#### Scenario: openspec installed globally with npm on Windows
- **WHEN** the panel is shown in a Windows IDE and `openspec` was installed with `npm install -g`
- **THEN** the project's changes are listed

#### Scenario: No login shell on Windows
- **WHEN** the plugin starts on Windows
- **THEN** it uses the IDE's environment without logging a failure to read a shell's environment
