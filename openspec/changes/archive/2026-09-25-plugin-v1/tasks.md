# Tasks

## 1. Toolchain

- [x] 1.1 Derwa installs `openjdk-21-jdk` in his own terminal (`sudo apt install -y openjdk-21-jdk`); verify with `java -version` showing 21
- [x] 1.2 Scaffold the Gradle project from the IntelliJ Platform Gradle Plugin 2.x docs (wrapper, Kotlin, `sinceBuild` 261, depends on `com.intellij.modules.platform` and `org.jetbrains.plugins.terminal` only); verify `./gradlew buildPlugin` produces a zip
- [x] 1.3 Configure `verifyPlugin` against current WebStorm and PhpStorm 2026.x; verify it passes on the empty plugin
- [x] 1.4 Capture a real `openspec list --json` output (with and without changes) as a test fixture

## 2. Logic, tests first

- [x] 2.1 Command resolver: tests for skills delivery, `/opsx:` delivery, and with/without `/backlog`, for every action; then implement until green
- [x] 2.2 Shell quoting: tests for quotes, `$`, backticks and newlines reaching the command unchanged; then implement
- [x] 2.3 Changes source: tests parsing the captured fixture, an empty list, a non-zero exit and a missing tool; then implement
- [x] 2.4 Follow-ups source: tests for quoted titles, missing fields, broken YAML (shown as unreadable), `resolved/` excluded and no backlog folder; then implement

## 3. UI and launching

- [x] 3.1 Tool window factory, available only when the project has `openspec/`
- [x] 3.2 Panel: the agreed layout, Explore…/Propose… input dialog, per-row buttons, hidden follow-up section without a backlog, "couldn't read changes" state
- [x] 3.3 Claude launcher: read the installed Terminal plugin's API first, then open a named tab in the project root running the quoted command; notification when `claude` isn't found
- [x] 3.4 Refresh on show, on debounced `openspec/` file changes, and on the Refresh button
- [x] 3.5 Launcher opens a reworked tab where the client-side terminal API is loaded, and falls back to the Classic tab where it isn't: tests for the fallback first

## 4. Manual checks

- [x] 4.1 In the `runIde` WebStorm sandbox, open atg-milsim: its changes and open follow-ups are listed with the right progress and types
- [x] 4.2 Apply on a change opens a tab named "apply: <change>" with Claude Code running `/openspec-apply-change <change>`; Explore with a quoted description arrives intact; Promote on a follow-up sends `/backlog promote FU-NNNN`
- [x] 4.3 Archiving a change or moving a follow-up to `resolved/` updates the lists without pressing Refresh
- [x] 4.4 A project without `openspec/` shows no panel; one without a backlog shows no follow-ups section
- [x] 4.5 Installed from disk on the real remote-dev backend: the panel renders in the client and a button opens a working Claude tab
- [x] 4.6 The PhpStorm sandbox loads the plugin and the panel behaves the same
- [x] 4.7 In the WebStorm sandbox, Explore opens a reworked tab (like one opened by hand) and Claude renders without residue; on the remote-dev backend the button still opens a working tab (usable after one resize)

## 5. Finish

- [x] 5.1 `./gradlew check verifyPlugin` green
