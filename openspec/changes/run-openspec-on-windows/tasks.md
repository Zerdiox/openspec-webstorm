# Tasks

## 1. Implementation

- [x] 1.1 Tests for the PATH lookup on Windows (PATHEXT order, no bare name, default PATHEXT) and on POSIX (executable bit): `PathLookupTest` fails first
- [x] 1.2 Look up tools with PATHEXT extensions on Windows: `PathLookupTest` passes
- [x] 1.3 Tests for choosing the login shell, including none on Windows: new `ShellEnvironmentTest` cases fail first
- [x] 1.4 Skip reading a login shell's environment on Windows: `ShellEnvironmentTest` passes
- [x] 1.5 Add Android Studio 2026.1.4.7 to plugin verification: `verifyPlugin` reports it Compatible
- [x] 1.6 `./gradlew check verifyPlugin` green

## 2. Manual checks (Windows, regular client)

- [ ] 2.1 Build the zip (`./gradlew buildPlugin`) and install it from disk in Android Studio on Windows, with `openspec` installed through `npm install -g`
- [ ] 2.2 Open a project with an `openspec/` folder: the panel lists its changes
- [ ] 2.3 `idea.log` has no `CreateProcess error=193` and no "Couldn't read the environment of /bin/sh" entry
- [ ] 2.4 Start an action without an apostrophe in its text (e.g. Apply on a change): a terminal tab opens with Claude Code running the command
- [ ] 2.5 On Linux or WSL (WebStorm): the panel still lists changes, and an action still opens Claude Code

## 3. Follow-up harvest

- [ ] 3.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
