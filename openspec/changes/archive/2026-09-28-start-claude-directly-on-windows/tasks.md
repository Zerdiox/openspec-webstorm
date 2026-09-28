# Tasks

## 1. Implementation

- [x] 1.1 Tests for the tab request that runs a program with arguments: a request carrying `[C:\...\claude.exe, <command with quotes, $, apostrophes and line breaks>]` round-trips intact, and the existing shell-line cases still do. New `OpenTabRequestTest` cases fail first
- [x] 1.2 Let the tab request carry either a shell line or a program with its arguments: `OpenTabRequestTest` passes
- [x] 1.3 Tests for building the request from the found `claude`, parameterized on Windows: on Windows a `claude.exe` (and `.com`) path gives the program form `[path, command]` with the command untouched; off Windows the shell line is unchanged (`claude '<quoted>'`); on Windows a `claude.cmd` or `claude.bat` gives no request and the launcher notification, not the not-found one; nothing found still gives the not-found notification. New `ClaudeLauncherTest` cases fail first
- [x] 1.4 Build the request from the found path on the backend, and add the notification that Claude Code on Windows must be installed with its native installer: `ClaudeLauncherTest` passes
- [x] 1.5 In the terminal tab opener, start a program request directly (non-shell process, tab kept open after the process ends, nothing typed) and keep typing a shell-line request as today: `./gradlew :frontend:compileKotlin` green
- [x] 1.6 `./gradlew check verifyPlugin` green

## 2. Manual checks (regular client)

- [x] 2.1 Build the zip (`./gradlew buildPlugin`) and install it from disk in Android Studio on Windows, with the terminal on Windows PowerShell 5.1 and Claude Code from the native installer (`claude.exe`)
- [x] 2.2 Propose with the description `Don't show $cost when it's "0"`: a tab opens running Claude Code at the tab's full width and height, its first prompt shows exactly that text, and enlarging the terminal re-fits Claude Code to the new size
- [x] 2.3 Explore with a description of two lines, the second containing `& | % ^ < >`: Claude Code's first prompt shows both lines exactly as typed
- [x] 2.4 Apply on a change: the tab runs Claude Code with the apply command; after `/exit` the tab stays open showing Claude Code's last output
- [x] 2.5 With `claude.cmd` from `npm install -g @anthropic-ai/claude-code` ahead of `claude.exe` on the PATH (restart the IDE after changing PATH), choose an action: a notification says to use the native installer, and no tab opens. Undo the PATH change afterwards
- [x] 2.6 On Linux or WSL (WebStorm): Propose with `Don't show $cost when it's 0` still opens a tab where the shell runs Claude Code with exactly that text, and exiting Claude Code returns to the shell prompt

## 3. Follow-up harvest

- [x] 3.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
