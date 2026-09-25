# Proposal

## Why

OpenSpec is now how work is planned across Derwa's projects, and every step of it — explore,
propose, apply, verify, archive, and the follow-ups backlog — starts by typing a slash command with
the right change name into a Claude Code session. Which changes exist, how far along each is, and
which follow-ups are open is only visible by reading folders or running the CLI. A tool window in
the IDE where the work happens turns "which command, on which change" into one click, and keeps the
real Claude Code session — with the project's own skills, permissions and gates — as the place the
conversation happens.

## What Changes

- A new plugin for JetBrains IDEs with an "OpenSpec" tool window, shown only in projects that use
  OpenSpec.
- It lists the project's OpenSpec changes with task progress, and its open follow-ups when the
  project has a follow-ups backlog.
- Buttons start a Claude Code session in a new IDE terminal tab with the right command already
  sent: Explore and Propose (from a short description), Apply, Verify and Archive (per change),
  Backlog review, and Promote (per follow-up).
- Commands follow each project's own OpenSpec setup, so projects installed with skills or with
  OpenSpec's default command files both work.
- The lists refresh on their own as the project's OpenSpec files change, and on demand.

**Not in scope for v1:** Update and Sync buttons, adding follow-ups from the panel, editing anything
in the panel, settings, and automated UI tests.

## Capabilities

### New Capabilities

- `openspec-workflow-panel`: seeing a project's OpenSpec changes and follow-ups inside the IDE and
  starting the OpenSpec workflow steps on them in Claude Code.

### Modified Capabilities

None.

## Impact

- **New repo:** `openspec-webstorm` — Kotlin, Gradle wrapper, IntelliJ Platform Gradle Plugin 2.x.
- **Dependencies at runtime:** the IntelliJ platform and the bundled Terminal plugin only; the
  project's own `openspec` and `claude` command-line tools.
- **Machine:** a JDK 21 (`openjdk-21-jdk` from apt) to build; the plugin is installed on the
  remote-development backend, where the IDE actually runs.
