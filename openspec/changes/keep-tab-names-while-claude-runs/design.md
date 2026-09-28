# Design

## Context

A tab request travels from the backend, which knows it is starting Claude Code, to the client-side
code that opens the reworked terminal tab. The request says either to type a shell line into the
user's shell (macOS, Linux) or to start a program directly with no shell (Windows). Claude Code
leaves the terminal title alone when the environment variable `CLAUDE_CODE_DISABLE_TERMINAL_TITLE`
is set to `1`. The reworked terminal's tab builder accepts extra environment variables for the tab's
process in every supported release (261 and later), and adds them to the environment the tab gets
anyway rather than replacing it.

## Goals / Non-Goals

**Goals:**
- One mechanism that works for both the shell-line and the direct-program request.

**Non-Goals:**
- Changing how the command line is quoted or how the program is started.

## Decisions

- **Set the variable in the tab's environment, not on the command line.** A `VAR=1 claude ...`
  prefix only works where the command is typed into a POSIX-style shell: nushell and older fish
  reject it, and on Windows no shell runs, so there is nothing to prefix. An environment variable on
  the tab's process reaches Claude Code in both cases: started directly, it gets it itself; typed
  into a shell, it inherits it from the shell.
- **The backend chooses the variables; the request carries them as a map.** Knowing that the tab
  runs Claude Code, and which variable Claude Code reads, belongs with the code that builds the
  Claude Code command. The client-side opener stays generic: it applies whatever environment the
  request carries. A plain map keeps the request serializable as it is today. The field defaults to
  an empty map so building a request without environment variables stays short.

## Risks / Trade-offs

- [The variable stays set in the tab's shell after Claude Code exits] → Intended: the tab is for
  Claude Code, and a second `claude` run there should not rename it either. Tabs the user opens
  themselves never get the variable.
- [A future Claude Code renames or drops the variable] → The tab would show Claude Code's title
  again, as it does today; nothing else breaks. The manual check catches it for the installed
  version.
- [The user's shell startup files unset or override the variable] → Unlikely; Claude Code would then
  set the title as today.
