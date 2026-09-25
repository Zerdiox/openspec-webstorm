# Design

## Context

See proposal.md for why. Constraints that shape the design:

- **Where the IDE runs.** Derwa uses JetBrains Remote Development: the IDE backend
  (`remote-dev-server`, WebStorm 2026.2) runs inside WSL, a thin JetBrains Client on Windows shows
  it. A plugin installs on the backend — where files and terminals live — and its tool window is
  projected to the client. The Swing tool window must work in that split; verify in the manual check.
- **Cross-IDE.** A plugin that declares only `com.intellij.modules.platform` loads in every
  IntelliJ-based product (plugins.jetbrains.com/docs/intellij/plugin-compatibility.html). The only
  other dependency is the bundled Terminal plugin (`org.jetbrains.plugins.terminal`), present in
  WebStorm here and depended on by PhpStorm's own plugins. Nothing from the JavaScript plugin.
- **The Agent SDK doesn't apply.** It is TypeScript/Python; a JVM plugin drives the `claude` CLI
  instead, which also keeps Derwa's subscription login, the project's `.claude/` skills and
  permission rules, and every gate exactly as in a terminal.
- **Tools the plugin calls.** `openspec` (1.13.1, installed under nvm) and `claude` (2.1.282, in
  `~/.local/bin`) are on the *login-shell* PATH; the IDE backend isn't started from a login shell.

## Goals / Non-Goals

**Goals:** the behaviour in the spec, loading in WebStorm and PhpStorm 2026.1+, verified on every
build. **Non-Goals:** see proposal.md; also no chat UI of our own — the terminal tab is the chat.

## Decisions

**Toolchain.** Kotlin; Gradle wrapper; IntelliJ Platform Gradle Plugin 2.x (the current official
build plugin — read its docs for the exact setup rather than a template from memory); JDK 21 from
apt. `sinceBuild` 261 (2026.1). `./gradlew buildPlugin` makes the zip, `runIde` opens a sandbox IDE
through WSLg, `verifyPlugin` runs the Plugin Verifier against current WebStorm and PhpStorm 2026.x —
that is what proves the cross-IDE claim, not the docs alone. Plugin id `dev.derwa.openspec`, name
"OpenSpec", tool window "OpenSpec" on the right.

**Six small units.**
- *Tool window factory* — registers the window; available only when the project has `openspec/`.
- *Changes source* — runs `openspec list --json` in the project root off the UI thread and maps
  `changes[].{name, completedTasks, totalTasks, status}`. Using the CLI rather than parsing change
  folders keeps the plugin from drifting from OpenSpec's own format. Resolves `openspec` through the
  platform's login-shell environment; a missing tool or non-zero exit becomes the "couldn't read
  changes" state with the first line of stderr.
- *Follow-ups source* — reads top-level `openspec/backlog/followup/FU-*.md` (never `resolved/`) and
  takes `id`, `title`, `type`, `capability` from the frontmatter with the YAML parser the platform
  already bundles (titles in real backlogs need YAML quoting, so no hand parser). An unparsable file
  becomes an "unreadable" row keyed by file name.
- *Command resolver* — maps an action to its command text per project: `/openspec-<workflow>` when
  `.claude/skills/openspec-*` exists, `/opsx:<workflow>` when `.claude/commands/opsx/` does;
  `/backlog …` only when `.claude/commands/backlog.md` exists (which also decides whether the
  follow-up section and its buttons show).
- *Claude launcher* — checks `claude` resolves on the login-shell PATH (else an IDE notification,
  no tab), then opens a new Terminal tab named `<action>: <target>` in the project root and runs
  `claude '<command>'` with the text POSIX single-quote escaped. Use the smallest Terminal API that
  opens a named tab and runs a command; the plugin's API was reworked in recent releases, so read
  the installed version's API before choosing.
- *Panel* — the layout agreed below; "Explore…"/"Propose…" use a small multi-line input dialog.

```
[Explore...] [Propose...]                 [Backlog review]
CHANGES
  astro-7-upgrade    0/27   [Apply][Verify][Archive]
  plan-panel-fixes   0/15   [Apply][Verify][Archive]
FOLLOW-UPS (open)
  FU-0033  test-gap  board-milestones      [Promote]
                                           [Refresh]
```

**Refresh.** On tool-window show, on VFS changes under `openspec/` (debounced so a burst of writes
triggers one read), and a manual Refresh button.

## Risks / Trade-offs

- [Terminal API churn between 2026.1 and 2026.3] → one small launcher unit, chosen after reading
  the installed API; the verifier flags incompatible use per IDE version.
- [Tool window under remote development] → Swing tool windows are projected to the client; part
  of the manual checklist on the real backend, not only the sandbox.
- [Login-shell environment differs from the IDE's] → resolve both tools through the platform's
  shell environment, and fail visibly when they aren't found.
- [Command names differ per project] → detected from the project's own `.claude/` layout, never
  hard-coded to one delivery mode.

## Migration Plan

New repo, nothing to migrate. Install: "Install plugin from disk" on the remote-dev backend; remove
by uninstalling the plugin.
