# Proposal

## Why

With Gradle's configuration cache on, compiling a module's tests doesn't treat that module's own main
classes as an input. After a change to main code, test compilation reports UP-TO-DATE, or the build
cache restores its output, and the tests run against classes compiled for the old main code. This
happened even after `clean`. The failure can be a confusing `NoSuchMethodError`, but a stale build
can also pass `./gradlew check verifyPlugin`, the gate before every commit, when it should fail. The
cause was reproduced: with the configuration cache on, the test compile's cache key stayed the same
across three different `FollowUp` constructors. With `--no-configuration-cache` the task went out of
date and recompiled correctly.

## What Changes

- Turn off Gradle's configuration cache for this build in `gradle.properties`, with a short comment
  saying why, so it isn't turned back on for speed.
- The build cache stays on. With the configuration cache off, the test compile's cache key tracks
  the main classes again, so stale entries stored under the old key are no longer reached.
- No plugin behaviour changes.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
<!-- none: build tooling only, so the change sets `skip_specs: true` -->

## Resolves

- F16: an incremental build can hide test compile errors after a constructor change

## Out of Scope / Deferred

- Keeping the configuration cache and declaring the main classes as an extra input to test
  compilation. Untested, and it would paper over a bug that belongs to the Kotlin Gradle Plugin or
  the IntelliJ Platform Gradle Plugin.
- Reporting the bug upstream. Candidate follow-up: narrow down which plugin drops the main classes
  from the test compile's classpath snapshot under the configuration cache, and report it with a
  minimal reproduction. Once it's fixed, the configuration cache can come back.
- Clearing the local build cache. The stale entries become unreachable, and Gradle's own cleanup
  removes them eventually.

## Impact

- `gradle.properties`: one property and its comment.
- Every Gradle invocation, including IDE sync, configures the build again instead of reusing a
  cached configuration, so builds take a little longer to start.
- No code, API, dependency or plugin.xml changes.
