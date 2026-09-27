# Design

## Context

The tool window factory adds a single unnamed content holding `OpenSpecPanel`. That panel owns its
refresh: a VFS listener for `openspec/` and `.claude/`, a tool-window-shown listener, and a
debounced pooled-thread alarm that loads changes (via the `openspec` CLI) and follow-ups (read
straight from their files), then renders a `KeyedTree`. `KeyedTree` and `KeyedNode` know nothing
about OpenSpec. They keep the selection, collapsed groups and scroll position by key across a
wholesale `setNodes`, and wire speed search, double-click and Enter. Opening goes through
`NAVIGATABLE_ARRAY` built from `PathNavigatable`.

`openspec show --json` returns requirements as `{text, scenarios}` only: no heading names and no
line numbers. `openspec list --specs --json` does give nested ids with their domain path
(`identity/user-auth`), which confirms the layout: a capability is a folder under `openspec/specs/`
holding a `spec.md`.

`explain-unreadable-follow-ups` (in flight) gives `PathNavigatable` an optional line. This change
needs the same thing to open a requirement at its heading.

## Goals / Non-Goals

**Goals:**
- Read specs with no process start, so a refresh stays as cheap as it is today.
- Keep the spec parsing and the tree building as pure functions, tested without the IDE.
- Reuse `KeyedTree`, extended only so a group can start collapsed.

**Non-Goals:**
- Validating specs. A spec whose headings don't follow the format just shows fewer requirements.
- Any toolbar in the Specs tab. The Specs tab has no actions of its own apart from copying.

## Decisions

### Specs are read from files, not from the CLI
A `SpecsSource` walks `openspec/specs/` and treats every folder holding a `spec.md` as a capability.
Its id is the folder's path relative to `openspec/specs/`, joined with `/`. It then parses each file.
The CLI can't give requirement names or lines, so parsing is needed anyway. Once we parse, asking the
CLI only for ids would add one process per refresh and make the Specs tab fail wherever `openspec`
isn't on the PATH. Follow-ups are already read this way.

### Parsing is line-based and ignores fenced code
`parseSpec(text)` returns the purpose and the requirements, each with its name, its 1-based heading
line and its full text:
- **Purpose:** the lines after `## Purpose` up to the next `## ` heading, trimmed and joined into
  one paragraph for the tooltip.
- **Requirements:** each `### Requirement: <name>` line under `## Requirements`. A requirement's text
  runs from its heading to the line before the next `### ` or `## ` heading, with trailing blank
  lines dropped. Scenarios (`#### `) stay inside it, which is what Copy Requirement Text copies.
- Lines inside ``` fences are skipped when looking for headings.

A Markdown parser was considered. The format OpenSpec itself validates is this heading structure, so
matching lines is enough, and it keeps line numbers trivial.

A spec is unreadable only when the file can't be read as UTF-8 (an I/O error or malformed input).
The file is decoded strictly (`Files.readString` throws on malformed input, while `readText`
silently replaces it).
The reason comes from the exception, kept short: "can't be read" or "isn't UTF-8 text". A readable
file with no recognised headings is a capability with 0 requirements.

### The tree is built by a pure function from the specs
`specNodes(specs): List<KeyedNode>` splits each id on `/`. Every segment but the last becomes a
domain group, and the capability goes inside the innermost one. Siblings are ordered by name. When a
domain and a capability have the same name, the capability comes first. Keys are namespaced so that
a folder holding both its own `spec.md` and nested specs gives two distinct nodes:
- `spec-domain:<path>`
- `spec:<id>`
- `spec-req:<id>:<n>`, where `n` is the requirement's position in its spec. A requirement name isn't
  guaranteed unique, and the position is stable enough for keeping the selection across a refresh.

Capabilities are groups that start collapsed; domains start expanded. A capability with no
requirements holds one message row, "No requirements."

### A group can start collapsed
`KeyedTree` remembers only the groups the user collapsed and expands every other group, so every
group starts expanded. `KeyedNode` gets `startsCollapsed: Boolean = false`. For such a node,
`KeyedTree` remembers the opposite: the keys the user expanded, in a second list stored under
`<collapsedKey>.expanded`. `expandedGroups` takes both sets: a group is expanded when it starts
expanded and isn't in `collapsed`, or when it starts collapsed and is in `expanded`. It still skips
everything inside a group that isn't expanded. Leaving the existing collapsed list as it is keeps
the Workflow tab's remembered state valid after an update. The alternative was one list of groups
"toggled from their default", but it would have reinterpreted that stored list.

### The Specs tab is its own component with its own tree
A `SpecsPanel` builds its own `KeyedTree`, with its own collapsed-state key and its own renderer, and
provides its own data (navigatables, copy, virtual files). Hosting specs inside `OpenSpecPanel`
would mix two unrelated models behind one selection and one set of actions.

### Refresh triggering is shared
The VFS listener, the tool-window-shown listener, the VFS preload and the debounce move out of
`OpenSpecPanel` into a small helper that both panels use, each with its own load function. The
factory's Refresh title action calls both panels' `refreshNow()`. Duplicating the listeners in the
Specs panel was the alternative, but the rules for what counts as an OpenSpec file and how the
folder gets into the VFS should stay in one place.

### The factory adds two named contents
The factory creates the "Workflow" content first, so the panel opens on it, and then the "Specs"
content. Each content disposes its own panel.

### Copying goes through the IDE's copy, not a custom Ctrl+C action
The Specs panel provides `PlatformDataKeys.COPY_PROVIDER`, so the IDE's own Copy action and shortcut
work and show in the context menu. It copies the text from a pure `copyText(selection)`: one line
per row, a domain as its path, a capability as its id, a requirement as `<id>: <name>`. "Copy
Requirement Text" is a context-menu action: it joins the selected requirements' full text with a
blank line, and is disabled when no requirement is selected. For Copy Path/Reference, the panel
provides `VIRTUAL_FILE_ARRAY` for the selected capabilities and requirements, one entry per spec
file, and adds the platform's copy-reference group to the context menu. The exact data keys and
group id must be checked against the platform source before relying on them.

## Risks / Trade-offs

- [OpenSpec changes its spec format, for example the requirement heading] → The parser is one small
  pure function with tests, so it's easy to update. A mismatch shows capabilities with 0
  requirements rather than failing.
- [Copy Path/Reference may want PSI data rather than virtual files] → Check the platform's copy-path
  provider first. If it needs PSI, add `PSI_ELEMENT_ARRAY` from the files as well.
- [Large spec folders make a refresh slower] → Reading a few dozen small Markdown files on a pooled
  thread is cheap next to the CLI call the Workflow tab already makes. If it matters, the Specs tab
  could skip a refresh when no event was under `openspec/specs/`.
- [This change and `explain-unreadable-follow-ups` both touch `OpenSpecPanel` and `PathNavigatable`]
  → Apply this change after that one and reuse its line support.
