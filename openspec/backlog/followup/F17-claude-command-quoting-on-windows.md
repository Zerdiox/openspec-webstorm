---
id: F17
title: The claude command is quoted for a POSIX shell, which breaks in Windows terminals
found: 2026-09-27
source: conversation
capability: openspec-workflow-panel
location: backend/src/main/kotlin/dev/derwa/openspec/ShellQuoting.kt:4
type: bug
size: M
---

## What
The command typed into a new terminal tab is quoted for a POSIX shell: `claude '/opsx:apply x'`,
with an apostrophe written as `'\''`. On Windows the terminal usually runs PowerShell, which reads
plain single quotes the same way but escapes an apostrophe as `''`, so any text containing one (for
example a propose description like "Don't …") is mangled. In cmd.exe single quotes aren't quotes at
all, so every command arrives split into broken arguments.

## Why it matters
Starting a Claude session with free text is unreliable on Windows, and fails completely when the
user's terminal shell is cmd.exe.

## Notes
Found while fixing the Windows `openspec` lookup (npm's extensionless script was picked over
`openspec.cmd`). The quoting has to follow the shell the terminal tab actually starts, which is a
terminal setting and can be pwsh, powershell, cmd, Git Bash or WSL. The tab is opened on the client
side, which may be where that setting is known. Sending the text without going through a shell's
parser would avoid quoting entirely.
