# Design

## Context

Tools are found by searching the PATH of the user's shell environment. That environment is read
from their login shell (`$SHELL`, otherwise `/bin/sh`), so that PATH entries added only by
interactive shell setup (nvm and the like) are included. Windows has neither of these. npm's global
install on Windows writes three launchers for each tool: `openspec` (a Unix shell script),
`openspec.cmd` and `openspec.ps1`. A process can only start the `.cmd`.

## Goals / Non-Goals

**Goals:** find and run `openspec` and `claude` on Windows, and keep macOS and Linux behavior
unchanged.

**Non-Goals:** quoting the terminal command for Windows shells (F17).

## Decisions

- **Look up names with PATHEXT extensions on Windows.** Try the name with each extension in the
  environment's `PATHEXT`, in that order (Windows' own order), falling back to
  `.COM;.EXE;.BAT;.CMD` when the variable is missing. Never accept the bare name, and skip the
  executable-bit check because Windows has no such bit. The platform's
  `PathEnvironmentVariableUtil` was the alternative, but it searches the IDE's PATH rather than the
  map the plugin passes in. Whether the lookup runs as Windows is a parameter, so tests cover
  Windows behavior on Linux.
- **No login shell on Windows.** Choosing the shell returns nothing on Windows, and the IDE's own
  environment is used directly. On Windows that environment is already what a new terminal gets.
- **Run the `.cmd` directly.** The JDK starts `.cmd` and `.bat` files through cmd.exe on its own.
  The arguments passed (`list --json`) contain nothing that cmd.exe would treat specially.

## Risks / Trade-offs

- [The IDE's environment is stale on Windows: a tool installed after the IDE started isn't on its
  PATH] → Restart the IDE. Other Windows programs behave the same way.
