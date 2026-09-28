# Tasks

## 1. Implementation

- [ ] 1.1 Tests for a tab request carrying environment variables: a request with `CLAUDE_CODE_DISABLE_TERMINAL_TITLE=1` round-trips intact for both the shell-line and the program form, and a request built without environment variables has none. New `OpenTabRequestTest` cases fail first
- [ ] 1.2 Let the tab request carry an environment map, empty by default: `OpenTabRequestTest` passes and existing callers compile unchanged
- [ ] 1.3 Tests that every request `startSession` builds sets `CLAUDE_CODE_DISABLE_TERMINAL_TITLE=1`: off Windows with the shell line, on Windows with `claude.exe`, both when opened here and when sent to the client that clicked. Updated `ClaudeLauncherTest` cases fail first
- [ ] 1.4 Set the variable when building the request on the backend: `ClaudeLauncherTest` passes
- [ ] 1.5 In the terminal tab opener, pass the request's environment variables to the tab for both forms: `./gradlew :frontend:compileKotlin` green
- [ ] 1.6 `./gradlew check verifyPlugin` green

## 2. Manual checks (regular client)

- [ ] 2.1 Build the zip (`./gradlew buildPlugin`) and install it from disk in WebStorm on Linux or WSL
- [ ] 2.2 Apply on a change: the tab opens named for applying that change, and it keeps that name after Claude Code has started and answered its first prompt (no `✳` session summary in the tab)
- [ ] 2.3 In that tab, `/exit` Claude Code: the shell prompt returns and `echo $CLAUDE_CODE_DISABLE_TERMINAL_TITLE` prints `1`; a terminal tab opened by hand does not print it
- [ ] 2.4 Promote a follow-up and Explore with a description at the same time: each tab keeps its own name while its session runs
- [ ] 2.5 On Windows (Android Studio, Claude Code from the native installer): Apply on a change and check the tab keeps its name after Claude Code starts, and that after `/exit` the tab stays open with Claude Code's last output

## 3. Follow-up harvest

- [ ] 3.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
