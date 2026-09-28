# Design

## Context

When the user picks an action, the backend checks that `claude` is on the PATH, builds a tab request
(tab name, working directory, a shell line), and either opens the tab itself or sends the request
to the client that clicked. The client-side opener creates a reworked-terminal tab running the
user's configured shell and types the line into it. The request crosses the backend/client RPC, so
both sides share its serialized form.

The reworked terminal's tab builder, from build 261 on, can start any program with an argument list
instead of a shell (`shellCommand`, `processType(NON_SHELL)`), and can keep the tab after the
process ends (`closeOnProcessTermination`). It adds its shell integration only when the program is
bash, zsh, fish or PowerShell, so any other program runs exactly as given.

## Goals / Non-Goals

**Goals:** on Windows, start Claude Code with the command as one argument that no shell or
launcher re-parses. Keep the macOS and Linux path byte-for-byte as it is.

**Non-Goals:** detecting the terminal's configured shell. Direct start makes it irrelevant.

## Decisions

- **The request says what to run, in one of two forms.** It carries either a shell line to type (as
  today) or a program with its arguments. The backend decides which form; the opener only follows
  it. Deciding on the backend keeps the Windows check next to the PATH lookup, and the opener stays
  free of OS logic, which it can't unit-test.
- **The program is the full path the PATH lookup found.** The lookup already runs before every
  action and is PATHEXT-aware, so the path it returns is exactly what Windows would run for
  `claude`. Passing that path, rather than the bare name, keeps the process start from resolving
  `claude` again against a possibly different PATH. The tab's process runs where the backend runs,
  also under remote development, so the path is valid there.
- **Only a program is started: `.exe` or `.com`.** Windows starts a `.cmd` or `.bat` through
  cmd.exe, which re-parses the arguments (`"`, `%`, `&`, `|`, `^`, line breaks), so npm's
  `claude.cmd` couldn't meet the spec. A lookup that finds a launcher is reported with its own
  notification, naming the native installer, and opens no tab. The first match on the PATH decides,
  as it does for Windows itself: a `claude.cmd` ahead of a `claude.exe` is what the user's own
  terminal runs too, so the notification is the honest answer.
- **Whether to start directly is a parameter defaulting to the running OS**, like the PATH lookup,
  so the Windows behavior is tested on Linux.
- **The tab stays open when Claude Code exits** (`closeOnProcessTermination(false)`), so its last
  output stays readable. The user closes it or opens a new tab for a shell.
- **Windows only.** On macOS and Linux Claude Code is typed into the user's shell so it inherits
  what the shell's startup files set up (nvm and the like); a directly started program wouldn't.
  Windows has no such startup environment for the terminal, so nothing is lost there.
- **The process gets the terminal's default environment.** On Windows that is the IDE's own
  environment, the same one a new terminal tab gets.

## Risks / Trade-offs

- [No shell remains after Claude Code exits] → The tab keeps Claude Code's output; a new terminal
  tab gives a shell. This is the cost of no shell touching the text.
- [A Windows user whose terminal shell is WSL used to get the Claude Code inside WSL; now the
  Windows `claude.exe` runs] → The panel already required a Windows `claude` before opening a tab,
  so these users already had one installed. Running WSL's Claude Code is deferred (see proposal).
- [npm-installed Claude Code on Windows stops working from the panel] → The notification names the
  fix: Anthropic's native installer, which is its recommended install.
- [The request's serialized form changes] → Backend and client always run the same plugin build;
  nothing persists a request.
