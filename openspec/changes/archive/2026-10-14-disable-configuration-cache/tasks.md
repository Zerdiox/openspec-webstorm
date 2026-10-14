# Tasks

No plugin behaviour changes, so there are no new tests. A throwaway ABI change in main code stands in
for the failing test: it must show the stale build before the fix and a correct one after.

## 1. Reproduce (red)

- [x] 1.1 With the working tree clean, run `./gradlew :backend:compileTestKotlin` to warm the build.
  Then add a trailing defaulted parameter to `FollowUp` in
  `backend/src/main/kotlin/dev/derwa/openspec/FollowUpsSource.kt` (e.g. `val probe: Long = 1L,`) and
  run `./gradlew :backend:compileTestKotlin` again. Verify it reports `compileTestKotlin UP-TO-DATE`,
  and that `javap -c -p -cp backend/build/classes/kotlin/test dev.derwa.openspec.FollowUpViewTest`
  still calls a `FollowUp.<init>` without the probe's type (no `J` before
  `ILkotlin/jvm/internal/DefaultConstructorMarker`).
  Keep the probe parameter for 2.2

## 2. Fix

- [x] 2.1 In `gradle.properties`, set `org.gradle.configuration-cache=false` with a one-line comment
  above it: with the configuration cache on, test compilation doesn't see the module's own main
  classes as an input, so tests can compile and run against stale classes. Verify that
  `./gradlew help` no longer prints `Configuration cache entry reused` or `stored`
- [x] 2.2 With the probe parameter still in place, run `./gradlew :backend:compileTestKotlin` and
  verify the task executes (not UP-TO-DATE or FROM-CACHE) and that `javap` now shows the new
  constructor. Then run `./gradlew :backend:clean :backend:test` and verify it passes with no
  `NoSuchMethodError`
- [x] 2.3 Remove the probe parameter and verify `git status` shows only `gradle.properties` changed

## 3. Checks

- [x] 3.1 Run `./gradlew check verifyPlugin` and confirm it's green
- [x] 3.2 Manual check in a regular (standalone) sandbox IDE (`./gradlew runIde`):
  - [x] the sandbox WebStorm starts and the OpenSpec tool window lists this project's changes and
    follow-ups
  - [x] the idea.log has no new warnings or errors from the plugin after opening the project

## 4. Follow-up harvest

- [x] 4.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in
  the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
