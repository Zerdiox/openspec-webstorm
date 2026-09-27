---
id: F16
title: An incremental build can hide test compile errors after a constructor change
found: 2026-09-27
source: explain-unreadable-follow-ups
capability:
location: gradle.properties
type: tech-debt
size: S
---

## What
After `FollowUp`'s constructor changed (a `Boolean` parameter replaced by a `Problem?`), test classes
that still called the old constructor weren't recompiled. `./gradlew :backend:test` reported 31
`NoSuchMethodError` failures, even after `:backend:clean`, instead of compile errors. Only
`--rerun-tasks` made `compileTestKotlin` run and report the real `No parameter with name 'unreadable'`
errors.

## Why it matters
`./gradlew check verifyPlugin` is the gate before every commit. If an incremental or cached build can
leave stale test classes, the gate can report misleading results. Here it failed confusingly, but it
could also pass on stale classes.

## Notes
`gradle.properties` enables both the build cache (`org.gradle.caching=true`) and the configuration
cache. The Kotlin compile runs through the Build Tools API. Which of these caused it wasn't
investigated. To reproduce: change a data class constructor used by tests in `backend`, then run
`:backend:test` without `--rerun-tasks`.
