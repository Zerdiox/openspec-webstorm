# Proposal

## Why

A follow-up the panel can't read is listed so the problem isn't hidden, but the row doesn't help
fix it. It repeats its file name ("F40  F40-broken.md  unreadable"), which looks like a rendering
bug. It doesn't say what's wrong or where, so you have to open the file and look for the problem.
It also offers Promote, which would start a Claude Code session on a file whose details can't be
read.

## What Changes

- An unreadable follow-up's row shows its file name once, followed by why it can't be read, in the
  error colour: "F40-broken.md  unreadable: YAML error on line 4". The reasons are:
  - the file has no frontmatter
  - the frontmatter isn't closed
  - the frontmatter has a YAML error on a given line
  - the frontmatter isn't a list of fields
- The row's tooltip gives the full reason, including the YAML parser's own message.
- Opening an unreadable follow-up (double-click, Enter, Jump to Source) opens its file at the line
  of the problem.
- An unreadable follow-up can't be promoted:
  - its row offers no Promote
  - Promote is disabled with the reason "An unreadable follow-up can't be promoted" when one is
    selected
  - it has no checkbox, since only follow-ups that can be promoted have one
- Typing to find a row still matches an unreadable follow-up by its ID or file name.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `openspec-workflow-panel`:
  - "Open follow-ups are listed where the project has a backlog": an unreadable follow-up is shown
    by its file name once, with why it can't be read, and can't be promoted.
  - "A row opens its file": an unreadable follow-up opens at the line of the problem.

## Resolves

- F11: an unreadable follow-up's row shows its file name twice

## Out of Scope / Deferred

- F14: fixing a broken follow-up with Claude Code from the panel. This was considered as a
  replacement for Promote, but it would be the panel's first action that sends a free-text prompt
  instead of an OpenSpec or backlog command.
- Checking the frontmatter's fields against the backlog's template (missing `id`, unknown `type`).
  A follow-up with valid YAML is readable, even if it doesn't follow the template.
- Other open follow-ups for this capability (F5, F6, F7, F10, F13) are unrelated and stay open.

## Impact

- The backend's follow-up reading, panel model, row rendering, Promote logic and navigation, plus
  their tests. No new dependencies and no plugin.xml changes.
- It builds on `check-follow-ups-to-promote`, which is still in progress and changes the same
  Promote logic and panel files. Apply this change after that one is done.
