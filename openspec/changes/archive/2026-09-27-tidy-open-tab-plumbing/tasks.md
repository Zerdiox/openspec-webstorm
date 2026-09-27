# Tasks

No behaviour changes, so there are no new tests. The existing tests and `verifyPlugin` show that
nothing broke.

## 1. Backend visibility (F9)

- [x] 1.1 Make `OpenTabRequests` `internal` in
  `backend/src/main/kotlin/dev/derwa/openspec/OpenTabRequests.kt`. Verify with
  `./gradlew :backend:test` (`OpenTabRequestsTest` still compiles and passes)

- [x] 1.2 Make every remaining public top-level declaration in `backend/src/main/kotlin` `internal`
  (classes, objects, enums, interfaces, top-level functions). Verify with `./gradlew :backend:test`
  and that a search for public top-level declarations in the module finds none

## 2. Frontend opener lookup (F8)

- [x] 2.1 In `frontend/src/main/kotlin/dev/derwa/openspec/OpenTabSubscriber.kt`, open each request
  with `LocalTabOpener.EP_NAME.extensionList.firstOrNull()` instead of `ReworkedTerminalTab()`,
  looked up per request, as `ClaudeLauncher` does. If there is none, log a warning (the
  `logger<...>()` style used in `ShellEnvironment`) and drop the request. Verify with
  `./gradlew :frontend:compileKotlin`, and check that nothing but the extension registration in
  `dev.derwa.openspec.frontend.xml` still references `ReworkedTerminalTab`

## 3. Checks

- [x] 3.1 Run `./gradlew check verifyPlugin` and confirm it's green
- [x] 3.2 Manual check in a regular (standalone) sandbox IDE (`./gradlew runIde`):
  - [x] Propose, Apply and Explore from the panel each still open a Claude Code terminal tab named
    as before, running in the project directory
  - [x] the idea.log has no new warnings or errors from the plugin after opening a project and
    launching a tab

## 4. Follow-up harvest

- [x] 4.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in
  the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
