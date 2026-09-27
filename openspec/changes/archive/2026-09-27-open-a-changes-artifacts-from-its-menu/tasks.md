# Tasks

## 1. A change's artifacts (TDD)

- [x] 1.1 Write failing `PanelModelTest` tests for the function that lists a change's artifacts from
  its folder:
  - proposal, design and tasks paths when the files exist, and null for each that doesn't
  - no delta specs when `specs/` is missing or empty
  - one entry per `specs/<capability>/spec.md`, labelled with the capability, sorted by it
  - a nested spec is labelled with its domain path, `/`-separated (`identity/user-auth`)
  - other files under `specs/`, and folders without a `spec.md`, are ignored

  Run them and verify they fail
- [x] 1.2 Implement the function next to `changeOpenTarget` and verify the 1.1 tests and all
  existing tests pass

## 2. Context menu

- [x] 2.1 Move the panel's file-opening navigatable to an internal top-level class, used by the panel's
  navigatable data as before. Verify that the existing tests pass and that double-clicking a change
  still opens its proposal in the sandbox IDE
- [x] 2.2 Add the Open popup group to `contextMenuActions()` straight after Jump to Source. It holds
  Proposal, Design, Tasks and a Specs popup built from the selected change's artifacts. Missing
  artifacts are disabled with "No proposal yet", "No design yet", "No tasks yet" or "No specs yet",
  and the group is disabled with `SELECT_ONE_CHANGE` unless exactly one change and nothing else is
  selected. It is visible in the context menu only when the selection includes a change, and never
  in the toolbar. Verify in the sandbox IDE that a change's context menu reads Jump to Source, Open,
  then its actions, and that a follow-up's menu has no Open
- [x] 2.3 Run `./gradlew check verifyPlugin` and verify it is green

## 3. Manual checks (sandbox IDE)

- [x] 3.1 On a change with proposal, design, tasks and two delta specs, Open > Proposal, Design and
  Tasks each open the right file, and Open > Specs lists both capabilities in name order and opens
  each one's spec
- [x] 3.2 On a freshly scaffolded change (no files yet), Proposal, Design and Tasks are disabled with
  their "No … yet" tooltips and Specs is disabled with "No specs yet"
- [x] 3.3 Have Claude Code (or a shell) write `design.md` into a change without one. With no manual
  refresh, right-click the change: Design is enabled and opens the new file
- [x] 3.4 With two changes selected, right-clicking shows Open disabled with "Select one change";
  with a change and a follow-up selected, Open is disabled too
- [x] 3.5 Double-click, Enter and F4 on a change with all artifacts still open its proposal
- [x] 3.6 The toolbar has no Open entry, with a change selected or not

## 4. Follow-up harvest

- [x] 4.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in
  the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
