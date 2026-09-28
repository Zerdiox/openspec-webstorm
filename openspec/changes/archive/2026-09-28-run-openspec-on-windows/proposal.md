# Proposal

## Why

On Windows the panel never shows any changes. It starts the extensionless Unix script that npm puts
beside `openspec.cmd`, and Windows can't run that script. It also tries to read a POSIX login
shell's environment, which fails and adds a log entry on every startup. Both problems were found
while trying the plugin in Android Studio on Windows.

## What Changes

- On Windows, the `openspec` and `claude` tools are found the way Windows finds commands: a name
  with one of the executable extensions the system lists (`.cmd`, `.exe`, …), never a file with no
  extension.
- On Windows, the plugin doesn't try to read a login shell's environment. It uses the IDE's own
  environment.
- Android Studio is added to the IDEs the plugin is verified against.

## Capabilities

### New Capabilities

### Modified Capabilities
- `openspec-workflow-panel`: adds a requirement that the panel works on Windows as it does on macOS
  and Linux.

## Out of Scope / Deferred

- F17: the Claude Code command typed into a terminal tab is quoted for a POSIX shell, which breaks
  in PowerShell (text containing an apostrophe) and in cmd.exe (every command).

## Impact

- `backend`: tool lookup on PATH and the shell-environment read, with their tests.
- `build.gradle.kts`: plugin verification also runs against Android Studio 2026.1.4.7.
