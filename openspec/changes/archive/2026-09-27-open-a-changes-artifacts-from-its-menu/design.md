# Design

## Context

The context menu is a flat group built by `contextMenuActions()`: Jump to Source, a separator, the
four change-action slots and Promote. Each slot decides its own visibility and enabled state from
the panel's selection. A change row knows its folder. Opening a path goes through a navigatable
that is private to `OpenSpecPanel`: it refreshes the VFS for the path, opens a file in the editor,
or selects a folder in the Project tree. `changeOpenTarget` chooses what the row itself opens
(its proposal, or its folder while it has none) and has unit tests.

## Goals / Non-Goals

**Goals:**
- Work out which artifacts a change has in one small, pure function that can be unit-tested with a
  temporary folder, like `changeOpenTarget`.
- Reuse the panel's existing path opening, so artifact entries and double-click behave the same.

**Non-Goals:**
- Any change to what double-click, Enter or Jump to Source open.
- Asking the OpenSpec CLI about a change's artifacts.

## Decisions

**Fixed spec-driven file names, read from disk.** A change's artifacts are `proposal.md`,
`design.md`, `tasks.md` and every `spec.md` under `specs/`. The alternative, `openspec status
--change <name> --json`, knows a schema's real artifact paths, but it would start a CLI process
every time the menu opens, or add a per-change cache to the list refresh. The panel already assumes
`proposal.md` by name, and entries for files that don't exist are simply disabled, so another
schema degrades to fewer entries rather than wrong ones.

**One function returns the whole set.** A function next to `changeOpenTarget` takes the change's
folder and returns the proposal, design and tasks paths (null when missing) and the delta specs as
(capability, path) pairs, sorted by capability. The capability is the spec's folder relative to
`specs/` with `/` separators, so a nested spec reads `identity/user-auth`. Only `spec.md` files
count. Other files under `specs/` are not artifacts, and neither is a folder with no `spec.md`.

**An "Open" popup group in the context menu, after Jump to Source.** It is a popup `ActionGroup`
whose children are built in `getChildren` from the selected change. Proposal, Design and Tasks are
fixed actions, and Specs is a nested popup group with one action per spec. As with the change
actions, the group is visible in the context menu only when the selection includes a change row. It
is enabled only when one change is selected and nothing else, the same condition
`selectionActions` uses for change actions, and otherwise it shows `SELECT_ONE_CHANGE`. The
alternative, flat entries such as "Open Design" in the main menu, would put up to four entries
plus a variable number of specs between Jump to Source and the actions.

**Disabled entries carry a reason.** Missing proposal, design or tasks show "No proposal yet", "No
design yet" or "No tasks yet". Specs with no delta specs is shown disabled with "No specs yet"
instead of being hidden, so the menu keeps the same shape for every change.

**The navigatable moves out of the panel.** `PathNavigatable` becomes an internal top-level class,
so the menu's actions open paths the same way double-click does, including the VFS refresh for
files written by Claude Code outside the IDE.

## Risks / Trade-offs

- [The menu checks the file system on the EDT when it updates] → That means three `isRegularFile`
  calls and a walk of one change's `specs/` folder, which holds a few files, on a right-click only.
  Double-click already checks the proposal the same way.
- [A change on a non-spec-driven schema shows spec-driven entries] → Its entries are disabled where
  the files don't exist. This is noted as out of scope in the proposal.
- [`explain-unreadable-follow-ups` edits `OpenSpecPanel.kt` and `PanelModel.kt` too] → The edits
  touch different code (follow-up rows vs. change artifacts). Whichever change lands second rebases
  its hunks.
