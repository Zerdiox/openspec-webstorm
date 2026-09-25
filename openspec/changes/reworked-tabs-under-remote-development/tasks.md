# Tasks

## 1. Before starting

- [ ] 1.1 Confirm `panel-layout-and-usability` is archived and merged; verify `openspec list` no longer shows it and `./gradlew check verifyPlugin` is green on the starting point
- [ ] 1.2 Read the modular plugin template (github.com/JetBrains/intellij-platform-modular-plugin-template) and the IntelliJ Platform Gradle Plugin's module docs for the version in use; verify the planned subproject setup (module plugin, `pluginModule`, descriptors, split-mode `runIde`) matches them, and note any difference in design.md

## 2. Split into modules, no behaviour change

- [ ] 2.1 Create `shared/`, `backend/` and `frontend/` Gradle subprojects and module descriptors; move every existing source and test into `backend/` unchanged; replace `<depends>` in the root `plugin.xml` with a `<content>` list, move the `<toolWindow>` and `<notificationGroup>` extensions into the backend module's descriptor (the root descriptor loads on both sides), and declare each module's dependencies in its own descriptor. Verify `./gradlew check` is green and `runIde` shows exactly one OpenSpec panel whose buttons still open a tab
- [ ] 2.2 Move `ReworkedTerminalTab` into `frontend/` and the Terminal plugin dependencies to the subprojects that use them; verify `./gradlew check verifyPlugin` is green for all four IDE versions and note any experimental or internal API warnings

## 3. Spike: the topic in a standalone IDE

- [ ] 3.1 Add the serialization Gradle plugin (Kotlin 2.3.20, runtime `compileOnly`), a minimal `@Serializable` event and `ProjectRemoteTopic` in `shared/`, a listener in `frontend/` that logs it, and a temporary send from the backend on a panel button; verify in `runIde` (standalone) that the listener logs the event with the right project, and record which thread `handleEvent` runs on (whether 4.3 must hop to the EDT). If it doesn't arrive, stop and revisit the design with Derwa
- [ ] 3.2 Add the split-mode `runIde` registration (`splitMode = true`, installed on both); verify the same event arrives in the frontend process there, and that `sendToClient` with `ClientId.current` delivers it (fall back to `broadcast` per design.md if it's internal)

## 4. Opening the tab through the client, tests first

- [ ] 4.1 Test that the tab event (tab name, working directory, command line) round-trips through its serializer unchanged, including quotes, `$` and newlines in the command line; then finish the event until green
- [ ] 4.2 Test that the launcher hands its sender one event with the action's tab name, the backend project's base path and the quoted `claude` command line when `claude` is found, and sends nothing (and notifies) when it isn't; then rework `ClaudeLauncher` to capture `ClientId.current` on the EDT and send the event, until green
- [ ] 4.3 Make the frontend listener open the tab through `ReworkedTerminalTab` with the event's name, working directory and command line; remove the temporary spike send
- [ ] 4.4 Delete `openPreferringReworked`, the Classic tab code and `TabOpeningTest`; verify nothing references `TerminalToolWindowManager` or `createShellWidget` any more
- [ ] 4.5 Update the README: under remote development install on the backend (Plugins (Host)) and in the client (Plugins); remove the note against installing it in the client and the "Known limitation: remote development" section
- [ ] 4.6 `./gradlew check verifyPlugin` green for all four IDE versions

## 5. Manual checks

- [ ] 5.1 Standalone WebStorm sandbox (`runIde`): Apply on a change opens a tab named for it in the project root, Claude Code fills it at full width and height with no residue, and making the terminal taller makes Claude Code taller
- [ ] 5.2 Split-mode sandbox: exactly one OpenSpec panel; Apply, Explore (with a quoted description) and Promote each open a reworked tab in the client, in the project root, with Claude Code at full size and following height and width changes
- [ ] 5.3 Real remote-dev backend (WSL), zip installed on the backend and in the client: one OpenSpec panel; a button opens a tab where Claude Code fills the full height and width at once, and grows when the window is enlarged
- [ ] 5.4 Real remote-dev backend with the client copy uninstalled: the panel still shows and clicking opens no tab, and nothing errors on the backend
- [ ] 5.5 PhpStorm sandbox: the plugin loads and a button opens a correctly sized tab
- [ ] 5.6 With `claude` removed from PATH, a button shows the "Claude Code wasn't found" notification and opens no tab

## 6. Finish

- [ ] 6.1 Resolve FU-0001 per openspec/backlog/followup/README.md: add the height finding (height never corrects after a resize; black below Claude Code's status line) to its Notes, add `resolved` and `resolved_by: reworked-tabs-under-remote-development`, and move it to `resolved/` with plain `mv`; verify it's gone from the open follow-ups

## 7. Follow-up harvest

- [ ] 7.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
