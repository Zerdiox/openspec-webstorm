# Tasks

## 1. Before starting

- [x] 1.1 Confirm `panel-layout-and-usability` is archived and merged; verify `openspec list` no longer shows it and `./gradlew check verifyPlugin` is green on the starting point
- [x] 1.2 Read the modular plugin template (github.com/JetBrains/intellij-platform-modular-plugin-template) and the IntelliJ Platform Gradle Plugin's module docs for the version in use; verify the planned subproject setup (module plugin, `pluginModule`, descriptors, split-mode `runIde`) matches them, and note any difference in design.md

## 2. Split into modules, no behaviour change

- [x] 2.1 Create `shared/`, `backend/` and `frontend/` Gradle subprojects and module descriptors; move every existing source and test into `backend/` unchanged; replace `<depends>` in the root `plugin.xml` with a `<content>` list, move the `<toolWindow>` and `<notificationGroup>` extensions into the backend module's descriptor (the root descriptor loads on both sides), and declare each module's dependencies in its own descriptor. Verify `./gradlew check verifyPlugin` is green for all four IDE versions and `runIde` shows exactly one OpenSpec panel whose buttons still open a tab

## 3. Spike: RPC from backend to client

- [x] 3.1 Add the serialization and `rpc` Gradle plugins (Kotlin 2.3.20, `rpc` 2.3.20-RC2-0.1, serialization runtime `compileOnly`), a minimal `@Serializable` request and an `@Rpc` interface with `openTabRequests(projectId): Flow<…>` in `shared/`, its backend implementation and `remoteApiProvider`, a frontend startup activity that collects it inside `durable { }` and logs each request, a temporary send from the backend on a panel button, and the split-mode `runIde` registration (`splitMode = true`, installed on both); verify there that the frontend process logs the request for the right project, and record which thread the collector runs on (whether 4.5 must switch to the EDT). If it doesn't arrive, stop and revisit the design with Derwa
- [x] 3.2 In the split-mode sandbox, log `ClientId.current` at the click and inside the RPC call to check they identify the same client (if not, fall back to every subscriber per design.md and note it there)

## 4. Opening the tab through the client, tests first

- [x] 4.1 Test that the request (tab name, working directory, command line) round-trips through its serializer unchanged, including quotes, `$` and newlines in the command line; then finish the request until green
- [x] 4.2 Test that the backend's request flow passes a subscriber only the requests tagged with its `ClientId`, and drops requests nobody subscribes to; then finish the project service and RPC implementation until green
- [x] 4.3 Test that the launcher builds one request with the action's tab name, the backend project's base path and the quoted `claude` command line when `claude` is found, hands it to the local opener when there is one and to the remote sender (with the `ClientId` captured at the click) only when there isn't, and opens nothing (and notifies) when `claude` isn't found; then rework `ClaudeLauncher` to capture `ClientId.current` on the EDT and route the request that way, until green
- [x] 4.4 Declare the `localTabOpener` extension point and its interface in `shared/`, and have the backend look it up for the launcher
- [x] 4.5 Move `ReworkedTerminalTab` into `frontend/` with the Terminal plugin dependencies (`bundledPlugin`, `bundledModule("intellij.terminal.frontend")` and the descriptor entries), taking them out of `backend/`; register the frontend's local opener, which calls it, and make the frontend collector open the tab through it with the request's name, working directory and command line; remove the temporary spike send and log, and verify in the split-mode sandbox that the backend process finds no local opener
- [x] 4.6 Delete `openPreferringReworked`, the Classic tab code and `TabOpeningTest`; verify nothing references `TerminalToolWindowManager` or `createShellWidget` any more
- [x] 4.7 Update the README: under remote development install on the backend (Plugins (Host)) and in the client (Plugins); remove the note against installing it in the client and the "Known limitation: remote development" section
- [x] 4.8 `./gradlew check verifyPlugin` green for all four IDE versions, with no internal API usages reported; note any new experimental API warnings

## 5. Manual checks

- [x] 5.1 Standalone WebStorm sandbox (`runIde`): the tab opens through the local opener, not RPC; Apply on a change opens a tab named for it in the project root, Claude Code fills it at full width and height with no residue, and making the terminal taller makes Claude Code taller
- [x] 5.2 Split-mode sandbox: exactly one OpenSpec panel; Apply, Explore (with a quoted description) and Promote each open a reworked tab in the client, in the project root, with Claude Code at full size and following height and width changes
- [x] 5.3 Real remote-dev backend (WSL), zip installed on the backend and in the client: one OpenSpec panel; a button opens a tab where Claude Code fills the full height and width at once, and grows when the window is enlarged
- [x] 5.4 Real remote-dev backend with the client copy uninstalled: the panel still shows and clicking opens no tab, and nothing errors on the backend
- [x] 5.5 PhpStorm sandbox: the plugin loads and a button opens a correctly sized tab
- [x] 5.6 With `claude` removed from PATH, a button shows the "Claude Code wasn't found" notification and opens no tab

## 6. Finish

- [x] 6.1 Resolve FU-0001 per openspec/backlog/followup/README.md: add the height finding (height never corrects after a resize; black below Claude Code's status line) to its Notes, add `resolved` and `resolved_by: reworked-tabs-under-remote-development`, and move it to `resolved/` with plain `mv`; verify it's gone from the open follow-ups

## 7. Follow-up harvest

- [x] 7.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
