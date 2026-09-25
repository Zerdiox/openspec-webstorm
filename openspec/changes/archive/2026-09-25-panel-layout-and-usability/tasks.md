# Tasks

## 1. Change actions in the panel model (tests first)

- [x] 1.1 Update the change row test in `PanelModelTest` so a change with 3 of 10 tasks done offers Apply, Verify, Archive, Explore in that order, each targeting the change; run `./gradlew test` and see it fail
- [x] 1.2 Add `PanelModelTest` cases for a change with no tasks (Explore, Apply, Verify, Archive), with all tasks done (Verify, Archive, Apply, Explore), and with a done count above its total (treated as all done); run them and see them fail
- [x] 1.3 Update the "not set up" test so every change action, Explore included, is disabled; run it and see it fail
- [x] 1.4 Order a change row's actions in `panelModel` by its tasks, default first, until all `PanelModelTest` tests pass
- [x] 1.5 Add a `PanelModelTest` case that `tabName(Action.EXPLORE, "plan-panel-fixes")` is `explore: plan-panel-fixes`, and a `CommandResolverTest` case that exploring a change sends `/openspec-explore plan-panel-fixes` (skills) and `/opsx:explore plan-panel-fixes` (opsx); both pass with no code change

## 2. Rows that fit the panel

- [x] 2.1 Replace the plain panel inside the scroll pane with a small panel that implements `Scrollable` and tracks the viewport's width only; verify in the IDE that a narrow panel no longer scrolls sideways
- [x] 2.2 Give each change row's name label a tooltip with the full name; verify by hovering a shortened change name
- [x] 2.3 Show a follow-up's type and capability in small muted text under its title, with the row's actions centred beside both lines and some space above and below each row; verify in the IDE that a follow-up row is two lines, rows are clearly separated, and its Promote button is centred

## 3. Split button for change actions

- [x] 3.1 Build each change row's actions as one `JBOptionButton`: the first action is the main action, the rest are options, labelled as today, each launching as today; disabled with the existing "isn't set up" tooltip when the actions are disabled; verify in the IDE that the main action and every dropdown entry open the right terminal tab
- [x] 3.2 Give the Promote button a tooltip saying it explores the follow-up as a candidate change; verify by hovering it
- [x] 3.3 List the split button's dropdown options without separators; verify in the IDE by opening a change's dropdown

## 4. Refresh in the title bar

- [x] 4.1 Set a refresh title action (platform refresh icon, "Refresh" tooltip) on the tool window from the factory, calling the panel's refresh; remove the bottom Refresh row and its spacer; verify in the IDE that the icon refreshes the lists

## 5. Description dialog padding

- [x] 5.1 Add a scaled empty border to the Explore/Propose text area; verify in the IDE that typed text no longer touches the edge, including after scrolling a long description

## 6. Checks

- [x] 6.1 Run `./gradlew check verifyPlugin` and confirm it's green
- [x] 6.2 Manual check in the sandbox IDE (`./gradlew runIde`), New UI:
  - [x] a follow-up and a change with titles longer than the panel show "…", keep their buttons fully visible, and show the full title on hover
  - [x] resizing the panel narrower and wider re-lays the rows with no sideways scroll bar
  - [x] a change with no tasks shows `[Explore | v]`, one in progress shows `[Apply | v]`, one with all tasks done shows `[Verify | v]`, and Archive is only in the dropdown
  - [x] Explore on a change opens a tab named `explore: <change>` running the explore command with the change's name, with no dialog
  - [x] the title bar's refresh icon highlights on hover and refreshes the lists; there's no Refresh row at the bottom
  - [x] the Explore and Propose dialogs have padding around the text
  - [x] follow-up rows show type and capability under the title, and the split button's dropdown has no separators
  - [x] in a project without OpenSpec set up for Claude Code, the split buttons are disabled with the "isn't set up" tooltip
- [x] 6.3 Repeat the split button and long-title checks with the classic UI and with a light theme

## 7. Follow-up harvest

- [x] 7.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
