# Proposal

## Why

Under remote development every button in the OpenSpec panel opens a terminal tab in which Claude
Code draws at the wrong size. Widening the window fixes the width, but the height stays stuck: below
Claude Code's status line there's only black, however large the window is. A terminal tab the user
opens on the client renders Claude Code correctly at full width and height. Remote development is
the setup the plugin is used in every day, so each click opens a tab that doesn't work properly.

## What Changes

- Under remote development, the panel's actions open the same kind of terminal tab the user gets
  from the Terminal tool window's own "+" button. Claude Code fills it and follows its size from
  the first frame, with no resize needed.
- The plugin is split into a part that runs where the project lives (the panel, finding the tools,
  starting the session) and a small part that runs in the client and opens the tab. In a standalone
  IDE both parts run in one IDE and the tab opens directly, as it does today.
- **BREAKING** (installation): under remote development the plugin must also be installed in the
  client, next to the copy on the backend. Without the client copy a click opens no tab at all; it
  doesn't fall back to the old broken one. The client copy no longer adds a second OpenSpec panel
  that hides the backend's, which is what used to stop it being installed there.
- The Classic terminal tab is no longer used anywhere.
- README: installation for remote development changes to "install on both sides", and the known
  limitation about resizing the tab is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `openspec-workflow-panel`: the session an action starts is shown in a terminal tab that Claude Code
  fills and resizes with, in a standalone IDE and under remote development alike; and under remote
  development the plugin works when installed on both the backend and the client.

## Impact

- The plugin's packaging: one plugin with separate backend, client and shared parts, each declaring
  its own dependencies; the plugin descriptor, the Gradle build (new subprojects, Kotlin
  serialization and JetBrains' `rpc` compiler plugin) and the Plugin Verifier setup change with it.
- Opening a Claude Code tab: the client subscribes to the backend over the platform's RPC, and the
  backend asks the client that clicked to open the tab; the backend no longer opens terminal tabs
  itself.
- Still depends only on the platform and the bundled Terminal plugin; supported IDE versions are
  unchanged (2026.1 and later).
- Must land after `panel-layout-and-usability`, which changes the same panel and plugin descriptor.

## Out of Scope / Deferred

- Detecting a missing client copy and falling back to a Classic tab: the Classic tab is the broken
  experience this change removes, and detection would need the backend to wait for the client to
  confirm each tab.
- New follow-up candidates for Derwa to approve:
  - Publish the plugin on JetBrains Marketplace, so the IDE offers to install the client copy by
    itself under remote development instead of it being installed by hand.

## Resolves

- FU-0001
