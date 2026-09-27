# Tasks

## 0. Before starting

- [ ] 0.1 Confirm `check-follow-ups-to-promote` is done (all its tasks checked), so this change
  builds on its Promote logic and checkbox wiring. If it isn't, stop and tell Derwa

## 1. Reading the reason (TDD)

- [ ] 1.1 Write failing tests in `FollowUpsSourceTest` for an unreadable file's problem:
  - no frontmatter: "no frontmatter", line 1
  - frontmatter not closed: "frontmatter isn't closed", line 1
  - a YAML error on the 3rd frontmatter line: "YAML error on line 4", line 4, and the parser's
    problem text as the message
  - frontmatter that is a list or a scalar, and empty frontmatter: "frontmatter isn't a list of
    fields", line 2
  - the ID from the file name is still set, and a readable file has no problem

  Update the existing unreadable tests to the new shape. Run them and verify the new ones fail
- [ ] 1.2 Replace `FollowUp.unreadable` with `problem: Problem?`, keeping `unreadable` as a computed
  property, and have the source fill it in as the design describes (line shift, clamped to the
  frontmatter's closing line). Verify the tests from 1.1 and all existing tests pass

## 2. Row and Promote (TDD)

- [ ] 2.1 Write failing `PanelModelTest` tests: an unreadable follow-up's row keeps the ID as `id` and
  the file name as `title`, has detail "unreadable: <reason>", carries the problem's line and has no
  Promote, including when its file name has an ID. Run them and verify they fail
- [ ] 2.2 Write failing `SelectionActionsTest` tests: selecting an unreadable follow-up, alone or with
  readable ones, disables Promote with "An unreadable follow-up can't be promoted", from the toolbar
  and from the context menu. This reason wins over "no ID" for a file name without one. Run them and
  verify they fail
- [ ] 2.3 Implement the model and selection changes and verify the tests from 2.1 and 2.2 and all
  existing tests pass

## 3. Panel wiring

- [ ] 3.1 In the renderer, draw an unreadable row as its file name only, then its detail in the error
  colour, with the tooltip `<file> · <reason>: <message>` (or without the message). Verify in the
  sandbox IDE that a broken follow-up's row shows its file name once
- [ ] 3.2 Pass the row's line to the navigatable and open the file at that line when one is set.
  Verify in the sandbox IDE that Enter on a broken follow-up puts the caret on the problem's line

## 4. Checks

- [ ] 4.1 Run `./gradlew check verifyPlugin` and confirm it's green
- [ ] 4.2 Manual check in a regular (standalone) sandbox IDE (`./gradlew runIde`), with a project
  whose backlog holds these test files:
  - [ ] `F40-broken.md` with a YAML error on line 4 reads "F40-broken.md  unreadable: YAML error on
    line 4" in red, and hovering it shows the parser's message
  - [ ] `notes.md` without frontmatter reads "notes.md  unreadable: no frontmatter", with its name once
  - [ ] a file with an unclosed frontmatter reads "unreadable: frontmatter isn't closed"
  - [ ] double-click, Enter and Jump to Source on `F40-broken.md` open it with the caret on line 4;
    readable follow-ups and changes open as before
  - [ ] unreadable rows have no checkbox; selecting one shows Promote disabled with "An unreadable
    follow-up can't be promoted" in the toolbar and the context menu
  - [ ] typing "F40" or "broken" finds the unreadable row
  - [ ] fixing the file's YAML turns the row back into a normal follow-up with a checkbox and Promote

## 5. Follow-up harvest

- [ ] 5.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in
  the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
