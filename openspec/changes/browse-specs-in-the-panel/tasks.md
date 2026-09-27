# Tasks

## 0. Before starting

- [ ] 0.1 Confirm `explain-unreadable-follow-ups` is done (all its tasks checked), so this change
  builds on its optional line in the navigatable. If it isn't, stop and tell Derwa

## 1. Reading specs (TDD)

- [ ] 1.1 Write failing tests for `parseSpec` covering:
  - the purpose, as one trimmed paragraph
  - requirement names and 1-based heading lines, in file order
  - a requirement's full text (heading, text and `####` scenarios) up to the next `###`/`##`
    heading, without trailing blank lines
  - headings inside ``` fences are ignored
  - a spec without a Purpose or Requirements section gives an empty purpose and no requirements

  Run them and verify they fail
- [ ] 1.2 Write failing tests for `SpecsSource` on a temporary folder covering:
  - flat and nested capabilities with their ids (`billing`, `identity/user-auth`)
  - a folder that has its own `spec.md` and nested specs, which gives both
  - no `openspec/specs/` folder, which gives no specs
  - a non-UTF-8 `spec.md`, which is listed as unreadable with a short reason

  Run them and verify they fail
- [ ] 1.3 Implement `parseSpec` and `SpecsSource` as the design describes. Verify the tests from 1.1
  and 1.2 pass

## 2. Tree and copy text (TDD)

- [ ] 2.0 Write failing tests in `KeyedTreeTest` for groups that start collapsed:
  - `expandedGroups` leaves a `startsCollapsed` group closed unless it's in the expanded set
  - it expands a default group unless it's in the collapsed set, and skips the children of any
    group that isn't expanded
  - `KeyedTree` stores a user's expand of a `startsCollapsed` group under `<collapsedKey>.expanded`
    and restores it
  - the existing collapsed list keeps working unchanged

  Run them and verify they fail, then add `KeyedNode.startsCollapsed` and the expanded set as the
  design describes, and verify these and all existing tests pass
- [ ] 2.1 Write failing tests for `specNodes` covering:
  - domains as nested groups, with capabilities inside the innermost one
  - ordering by name, with a capability before a domain of the same name
  - namespaced keys, and requirement keys by position
  - capabilities marked to start collapsed, domains not
  - the "No requirements." message row
  - an unreadable spec as a capability without children

  Run them and verify they fail
- [ ] 2.2 Write failing tests for `copyText` and the requirement-text join covering:
  - a domain path, a capability id, and `<id>: <name>` for a requirement
  - one line per row, in tree order
  - full requirement texts joined by a blank line
  - rows that aren't requirements are ignored by the requirement-text join

  Run them and verify they fail
- [ ] 2.3 Implement `specNodes` and the copy text functions. Verify the tests from 2.1 and 2.2 and
  all existing tests pass

## 3. Panel wiring

- [ ] 3.1 Move the refresh triggering (VFS and tool-window-shown listeners, VFS preload, debounce)
  out of `OpenSpecPanel` into a shared helper, with no change in behaviour. Verify that all tests
  pass and that the Workflow tab still refreshes when a change's file is edited in the sandbox IDE
- [ ] 3.2 Add `SpecsPanel`:
  - its own `KeyedTree`, collapsed-state key and renderer: count in grey, purpose tooltip,
    unreadable in the error colour
  - the "No specs yet." message
  - navigatables that open a requirement at its heading line
  - it refreshes through the shared helper

  Verify in the sandbox IDE that the specs of this repository are listed
- [ ] 3.3 Split the tool window into "Workflow" (first) and "Specs" contents and make the Refresh
  title action refresh both. Verify in the sandbox IDE that the panel opens on Workflow and both
  tabs refresh
- [ ] 3.4 Before wiring copy, read the platform source for `COPY_PROVIDER`, the copy-path provider's
  data keys and the copy-reference context-menu group. Record in the task which ones are used
- [ ] 3.5 Provide the copy provider and the spec files' data, and add Jump to Source, the IDE's Copy,
  Copy Requirement Text and the copy-reference group to the Specs tab's context menu. Verify in the
  sandbox IDE that Ctrl+C and Copy Requirement Text put the expected text on the clipboard

## 4. Checks

- [ ] 4.1 Run `./gradlew check verifyPlugin` and confirm it's green
- [ ] 4.2 Manual check in a regular (standalone) sandbox IDE (`./gradlew runIde`), with a test
  project holding the specs `billing`, `identity/user-auth` and `identity/sessions`, and a
  non-UTF-8 `broken/spec.md`:
  - [ ] the panel shows the tabs "Workflow" and "Specs" and opens on Workflow, whose changes and
    follow-ups work as before
  - [ ] Specs lists `billing` and `broken` at the top level and an "identity" group holding
    `sessions` and `user-auth`, each with its requirement count; hovering a capability shows its
    purpose
  - [ ] `broken` is marked unreadable with its reason, and Enter on it opens its file
  - [ ] expanding a capability lists its requirements in the spec's order; double-click on one opens
    the spec with the caret on its heading; Enter on a capability opens the spec at its top, and
    double-clicking a capability collapses or expands it
  - [ ] typing part of a requirement's name selects it
  - [ ] Ctrl+C on a requirement of `identity/sessions` copies `identity/sessions: <name>`; with a
    capability and a requirement selected it copies two lines
  - [ ] Copy Requirement Text copies the heading, text and scenarios; with two requirements
    selected they're separated by a blank line
  - [ ] Copy Path/Reference on a capability offers its `spec.md` path
  - [ ] the context menu offers no action that starts a Claude Code session
  - [ ] adding a requirement to a spec in the editor updates its count and rows without a manual
    refresh
  - [ ] on first opening, the "identity" group is expanded and every capability is collapsed
  - [ ] expanding `billing`, collapsing `identity` and restarting the IDE keeps both that way, and
    the Workflow tab's collapsed groups are as they were before the update
  - [ ] a project with no `openspec/specs/` folder shows "No specs yet." in the Specs tab
  - [ ] with `openspec` removed from the PATH, the Workflow tab reports it and the Specs tab still
    lists the specs

## 5. Follow-up harvest

- [ ] 5.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in
  the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
