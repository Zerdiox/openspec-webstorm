---
id: F19
title: Report upstream that the configuration cache leaves test compilation stale
found: 2026-10-14
source: disable-configuration-cache
capability:
location: gradle.properties
type: tech-debt
size: M
---

## What
With Gradle's configuration cache on, `compileTestKotlin` doesn't treat the module's own main
classes as an input, so tests compile and run against stale main classes. The configuration cache
was turned off to work around it. Which plugin drops the main classes from the test compile's
classpath snapshot (Kotlin Gradle Plugin or IntelliJ Platform Gradle Plugin) isn't known.

## Why it matters
Until the bug is fixed upstream, the build can't use the configuration cache, so every Gradle run
and IDE sync configures the build from scratch and starts more slowly.

## Notes
Reproduction from the change: warm `:backend:compileTestKotlin`, add a trailing defaulted parameter
to a data class constructor used by tests (e.g. `FollowUp`), run it again. With the configuration
cache on it reports UP-TO-DATE (cache key unchanged across constructors) and `javap` on a test class
shows the old `<init>`; with `--no-configuration-cache` it recompiles. Narrow it down in a minimal
project (Kotlin JVM alone, then with the IntelliJ Platform plugin) and report to whichever owns it.
Once fixed, turn `org.gradle.configuration-cache` back on.
