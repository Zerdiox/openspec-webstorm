# OpenSpec for JetBrains IDEs

An **OpenSpec** tool window for WebStorm, PhpStorm and every other IntelliJ-based IDE (2026.1 and
later). It lists a project's in-flight OpenSpec changes with their task progress, and its open
follow-ups. Its buttons start a Claude Code session in a new terminal tab with the right OpenSpec
command already sent:

- **Explore…** and **Propose…**, from a short description
- **Apply**, **Verify** and **Archive**, per change
- **Backlog review**, and **Promote** per follow-up, in projects with a follow-ups backlog

The conversation then carries on in that tab like any Claude Code session, with the project's own
skills, permissions and gates.

## What a project needs

- An `openspec/` folder. Without one the tool window isn't offered.
- OpenSpec installed for Claude Code, either as skills (`.claude/skills/openspec-*`) or as
  OpenSpec's default commands (`.claude/commands/opsx/`). The buttons send whichever the project
  uses; without either, they are disabled.
- Optionally a follow-ups backlog: a `/backlog` command (`.claude/commands/backlog.md`) and
  follow-up files in `openspec/backlog/followup/`. Only then does the panel show follow-ups,
  Backlog review and Promote.

On the machine where the IDE runs, `openspec` and `claude` must be on the PATH of your interactive
shell — where a terminal tab finds them. The plugin reads that shell's environment once per IDE
session, so tools set up in `~/.bashrc` (nvm, for example) are found. If `openspec` isn't found the
panel says so; if `claude` isn't found, a notification says so and no tab opens.

## Installing

Build the plugin (below), or take a built `openspec-webstorm-<version>.zip`, then:

**Standalone IDE:** Settings → Plugins → ⚙ → Install Plugin from Disk… → choose the zip.

**Remote development (JetBrains Gateway / Toolbox, IDE backend on another machine or in WSL):**
install it on the **backend only** — in the client, Settings → **Plugins (Host)** → ⚙ → Install
Plugin from Disk…, then restart the backend when asked. Don't also install it in the client's own
"Plugins" page: a client copy registers a tool window with the same name first and hides the
backend's.

**Finding the tool window:** in the new UI, a tool window added to an existing layout starts under
View → Tool Windows → OpenSpec (or the ⋯ button on the tool-window bar), not on the bar itself. Open
it once and drag its icon onto the left bar; the IDE remembers where you put it.

To remove it, uninstall the plugin from the same Plugins page.

## Known limitation: remote development

In a standalone IDE the buttons open a normal (reworked) terminal tab and Claude Code renders
cleanly. Under remote development the tab is created by the backend and reaches the client through
a relay that starts it at a fixed size, so Claude Code's screen shows leftover lines until you
resize the tab once — drag the terminal's edge a little and it redraws correctly from then on.

## Building

Needs JDK 21 (`sudo apt install openjdk-21-jdk`); Gradle comes with the wrapper.

    ./gradlew buildPlugin              # the zip, in build/distributions/
    ./gradlew check verifyPlugin       # tests and the Plugin Verifier — the gate before a commit
    ./gradlew runIde                   # a sandbox WebStorm with the plugin
    ./gradlew runPhpStorm              # a sandbox PhpStorm with the plugin

The first run of the verifier and of each sandbox downloads the IDEs, about 1 GB each. The sandboxes
need a display (WSLg under WSL) and ask for a JetBrains login or trial on first start.
