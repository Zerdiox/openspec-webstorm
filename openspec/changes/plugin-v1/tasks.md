# Tasks

## 1. Toolchain

- [ ] 1.1 Derwa installs `openjdk-21-jdk` in his own terminal (`sudo apt install -y openjdk-21-jdk`); verify with `java -version` showing 21
- [ ] 1.2 Scaffold the Gradle project from the IntelliJ Platform Gradle Plugin 2.x docs (wrapper, Kotlin, `sinceBuild` 261, depends on `com.intellij.modules.platform` and `org.jetbrains.plugins.terminal` only); verify `./gradlew buildPlugin` produces a zip
- [ ] 1.3 Configure `verifyPlugin` against current WebStorm and PhpStorm 2026.x; verify it passes on the empty plugin
- [ ] 1.4 Capture a real `openspec list --json` output (with and without changes) as a test fixture

## 2. Logic, tests first

- [ ] 2.1 Command resolver: tests for skills delivery, `/opsx:` delivery, and with/without `/backlog`, for every action; then implement until green
- [ ] 2.2 Shell quoting: tests for quotes, `$`, backticks and newlines reaching the command unchanged; then implement
- [ ] 2.3 Changes source: tests parsing the captured fixture, an empty list, a non-zero exit and a missing tool; then implement
- [ ] 2.4 Follow-ups source: tests for quoted titles, missing fields, broken YAML (shown as unreadable), `resolved/` excluded and no backlog folder; then implement

## 3. UI and launching

- [ ] 3.1 Tool window factory, available only when the project has `openspec/`
- [ ] 3.2 Panel: the agreed layout, Explore…/Propose… input dialog, per-row buttons, hidden follow-up section without a backlog, "couldn't read changes" state
- [ ] 3.3 Claude launcher: read the installed Terminal plugin's API first, then open a named tab in the project root running the quoted command; notification when `claude` isn't found
- [ ] 3.4 Refresh on show, on debounced `openspec/` file changes, and on the Refresh button

## 4. Manual checks

- [ ] 4.1 In the `runIde` WebStorm sandbox, open atg-milsim: its changes and open follow-ups are listed with the right progress and types
- [ ] 4.2 Apply on a change opens a tab named "apply: <change>" with Claude Code running `/openspec-apply-change <change>`; Explore with a quoted description arrives intact; Promote on a follow-up sends `/backlog promote FU-NNNN`
- [ ] 4.3 Archiving a change or moving a follow-up to `resolved/` updates the lists without pressing Refresh
- [ ] 4.4 A project without `openspec/` shows no panel; one without a backlog shows no follow-ups section
- [ ] 4.5 Installed from disk on the real remote-dev backend: the panel renders in the client and a button opens a working Claude tab
- [ ] 4.6 The PhpStorm sandbox loads the plugin and the panel behaves the same

## 5. Finish

- [ ] 5.1 `./gradlew check verifyPlugin` green
