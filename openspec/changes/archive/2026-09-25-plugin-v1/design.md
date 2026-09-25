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
  `~/.local/bin`) are on the PATH of an *interactive* login shell only: nvm is set up in
  `~/.bashrc`, which a plain login shell doesn't read. The remote-dev backend doesn't load a shell
  environment at all, so its own PATH has neither nvm nor anything a shell adds.

## Goals / Non-Goals

**Goals:** the behaviour in the spec, loading in WebStorm and PhpStorm 2026.1+, verified on every
build. **Non-Goals:** see proposal.md; also no chat UI of our own — the terminal tab is the chat;
and no client-side half of the plugin for remote development (a later change), so under remote
development tabs are created by the backend and need one resize.

## Decisions

**Toolchain.** Kotlin; Gradle wrapper; IntelliJ Platform Gradle Plugin 2.x (the current official
build plugin — read its docs for the exact setup rather than a template from memory); JDK 21 from
apt. `sinceBuild` 261 (2026.1). `./gradlew buildPlugin` makes the zip, `runIde` opens a sandbox IDE
through WSLg, `verifyPlugin` runs the Plugin Verifier against current WebStorm and PhpStorm 2026.x —
that is what proves the cross-IDE claim, not the docs alone. Plugin id `dev.derwa.openspec`, name
"OpenSpec", tool window "OpenSpec" on the left.

**Six small units.**
- *Tool window factory* — registers the window; available only when the project has `openspec/`.
- *Changes source* — runs `openspec list --json` in the project root off the UI thread and maps
  `changes[].{name, completedTasks, totalTasks, status}`. Using the CLI rather than parsing change
  folders keeps the plugin from drifting from OpenSpec's own format. Resolves `openspec` through the
  user's shell environment (below); a missing tool or non-zero exit becomes the "couldn't read
  changes" state with the first line of stderr.
- *Follow-ups source* — reads top-level `openspec/backlog/followup/FU-*.md` (never `resolved/`) and
  takes `id`, `title`, `type`, `capability` from the frontmatter with the YAML parser the platform
  already bundles (titles in real backlogs need YAML quoting, so no hand parser). An unparsable file
  becomes an "unreadable" row keyed by file name.
- *Command resolver* — maps an action to its command text per project: `/openspec-<workflow>` when
  `.claude/skills/openspec-*` exists, `/opsx:<workflow>` when `.claude/commands/opsx/` does;
  `/backlog …` only when `.claude/commands/backlog.md` exists (which also decides whether the
  follow-up section and its buttons show).
- *Claude launcher* — checks `claude` resolves on the user's shell environment's PATH (else an IDE notification,
  no tab), then opens a new Terminal tab named `<action>: <target>` in the project root and runs
  `claude '<command>'` with the text POSIX single-quote escaped. Two paths, because Claude Code only
  renders reliably in the reworked terminal: where the Terminal plugin's client-side tab API is
  loaded, open a reworked tab through it; where it isn't, or it fails, fall back to the backend's
  Classic tab. In a standalone IDE this gives a true reworked tab. A remote-development backend
  also loads that API, but the client shows any tab the backend creates through the same relay as
  a Classic one. The reworked API is experimental and differs
  between releases, so use only the part common to every supported release.
- *Shell environment* — the environment of the user's shell started as an interactive login shell,
  the way the IDE's own terminal tabs start it, so tools are found wherever a terminal finds them.
  Read once per IDE session off the UI thread with the platform's shell-environment reader; if
  reading fails, the IDE process environment is used, and a tool not found there is reported.
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
- [Backend-created tab under remote development] → the backend starts it at a fixed size the client
  never corrects until the tab is resized, so Claude's redraws leave residue until one resize. Accepted
  for v1; a client-side half that opens reworked tabs removes it.
- [Tool window under remote development] → Swing tool windows are projected to the client; part
  of the manual checklist on the real backend, not only the sandbox.
- [Shell environment differs from the IDE's] → resolve both tools through the user's interactive
  login shell, and fail visibly when they aren't found. The platform's reader for it is
  experimental; the verifier flags it if it changes.
- [Command names differ per project] → detected from the project's own `.claude/` layout, never
  hard-coded to one delivery mode.

## Migration Plan

New repo, nothing to migrate. Install: "Install plugin from disk" on the remote-dev backend only
(the client's "Plugins (Host)" page); a second copy on the client registers a tool window of the
same name first and hides the backend's. In the new UI a tool window added to an existing layout
starts under View → Tool Windows, not on the stripe. Remove by uninstalling the plugin.
