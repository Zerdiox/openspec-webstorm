# Proposal

## Why

On Windows, text the user types for Explore or Propose doesn't reach Claude Code intact. The panel
types `claude '<command>'` into the terminal tab, quoted for a POSIX shell. The IDE's default
Windows shell, Windows PowerShell 5.1, mangles an apostrophe in that text, and cmd.exe splits every
command into broken arguments. Quoting per shell can't fix it: PowerShell 5.1 strips double quotes
from the arguments it hands to a program, and cmd.exe can't carry a line break.

## What Changes

- On Windows, the tab an action opens runs Claude Code itself, with the OpenSpec command as its
  single argument. No shell reads the text, so it arrives exactly as typed.
- When Claude Code exits in such a tab, the tab stays open with its last output, and the user
  closes it. No shell prompt follows, because no shell ran.
- On Windows, a `claude` that is a script launcher (npm's `claude.cmd`) rather than a program is
  refused: no tab opens, and a notification says to install Claude Code with its native installer.
  Windows passes a launcher's arguments through cmd.exe, which would alter the text again.
- macOS and Linux are unchanged: the command is still typed into the user's shell.

## Capabilities

### New Capabilities

### Modified Capabilities
- `openspec-workflow-panel`: "Descriptions reach Claude Code intact" gains a Windows scenario, and
  "A missing tool is reported, not hidden" covers a Claude Code on Windows that can't be started
  directly.

## Resolves

- F17: The claude command is quoted for a POSIX shell, which breaks in Windows terminals

## Out of Scope / Deferred

- Starting Claude Code directly on macOS and Linux too. There, Claude Code would lose the
  environment the user's shell startup files set up (nvm and the like).
- Running the Claude Code that lives inside WSL when the Windows terminal's shell is WSL. The panel
  already requires a Windows `claude`, and this change keeps that.

## Impact

- `backend`: building the tab request (a program and its arguments on Windows, a shell line
  elsewhere) and the refusal of a script launcher, with tests.
- `shared`: the tab request sent to the client carries the program and arguments.
- `frontend`: the tab starts the program directly when the request carries one.
