# Tasks

## 1. Platform check

- [x] 1.1 Using the platform source for 2026.1.5, confirm the APIs to use for:
  - a tool-window panel with a toolbar; a toolbar whose actions show text, and a split action with
    a dropdown in it
  - a coloured-text tree renderer, and tree speed search
  - the edit-source handlers for double-click and Enter on trees
  - providing navigatable data and custom data from a component
  - the Edit Source action's id, and installing a context menu from an action group
  - the platform filter icon, and a popup of toggle actions
  - opening a file in the editor, and selecting a directory in the project tree
  - storing string lists in the project's settings

  Record the chosen APIs in the implementation notes, and verify each compiles in a scratch use
  before task 3.

## 2. Pure logic (TDD)

- [x] 2.1 Write failing tests for the selection's actions:
  - one change gives its ordered actions, with the next step first and Archive never first
  - one or several follow-ups give Promote, with their IDs joined in list order
  - several changes, or a mixed selection, give disabled actions with a reason
  - nothing selected, or a group selected, gives disabled selection actions
  - a project without a Claude Code set-up gives disabled actions with today's reason

  Run them and verify they fail
- [x] 2.2 Write failing tests for filtering and grouping follow-ups:
  - filtering by type and by capability, including the "none" choice
  - unreadable follow-ups always shown, in their own group when grouped
  - grouping by none, type and capability, with a count for each group
  - a filtered flag and a shown count on the "Follow-ups" group
  - capabilities offered by the filter come from the open follow-ups

  Run them and verify they fail
- [x] 2.3 Write failing tests for:
  - node keys (change name, follow-up file name, group kind plus value)
  - the restore-by-key function: kept keys stay selected, missing keys are dropped, collapsed
    groups stay collapsed
  - the change open-target function (proposal when it exists, folder otherwise, using a temporary
    directory)

  Run them and verify they fail
- [x] 2.4 Implement the panel model changes, the selection-actions function, filtering and grouping, node keys, restore-by-key and the open target, and verify all tests from 2.1–2.3 and the existing tests pass

## 3. Reusable keyed-tree set-up

- [x] 3.1 Build the generic helper: it replaces the model from keyed nodes, restores selection, expansion and scroll position by key, stores collapsed group keys under a caller-supplied settings key, and installs speed search and the double-click and Enter edit-source handlers. It must not use any OpenSpec types. Verify it compiles and that the restore-by-key tests still pass

## 4. Panel rebuild

- [x] 4.1 Replace the panel's content with a tool-window panel holding the tree through the helper, with a renderer for groups (name and secondary count, plus a filtered marker), changes (name and secondary progress), follow-ups (ID and title, with secondary type and capability, the unreadable marker in the error colour, and a tooltip with the metadata) and message rows. Verify in the sandbox IDE that the lists show as specified
- [x] 4.2 Provide the selection and the navigatable targets through the panel's data context, and install the context menu (Jump to Source, then the selection's actions). Verify in the sandbox IDE that F4, Enter, double-click and the menu open the right files
- [x] 4.3 Build the toolbar: Explore… and Propose… (keeping the description dialog) and Backlog review, then the selection's next-step split action and Promote, all reading the selection. Verify in the sandbox IDE that the main action follows the selected change's tasks and that Promote on two follow-ups starts one session with both IDs
- [x] 4.4 Add the filter and Group By toolbar actions, store their settings in the project's settings, and rebuild the tree from the last loaded data when they change. Verify in the sandbox IDE that they apply immediately and survive a restart
- [x] 4.5 Remove the old row layout code, the Promote and split buttons, and the width-tracking panel, and verify nothing references them and all tests pass

## 5. Checks

- [x] 5.1 Run `./gradlew check verifyPlugin` and confirm it's green
- [x] 5.2 Manual check in the sandbox IDE (`./gradlew runIde`), New UI, dark theme:
  - [x] "Changes" and "Follow-ups" groups show counts, collapse and expand by click, double-click and Left/Right, and stay collapsed after a refresh and an IDE restart
  - [x] hovering a row highlights it with the normal pointer and no underline; a long row shows in full on hover
  - [x] typing a title fragment with the panel focused selects the matching row
  - [x] double-click and Enter on a change open its proposal whatever its progress; a just-scaffolded change with no proposal selects its folder in the project tree
  - [x] double-click, Enter and F4 on a follow-up open its file, including an unreadable one; Enter with several rows selected opens all of them
  - [x] right-clicking a change with tasks in progress shows Jump to Source (with its shortcut), then Apply, Verify, Archive, Explore; right-clicking a follow-up shows Jump to Source, then Promote
  - [x] selecting a change makes the toolbar's main action Explore, Apply or Verify according to its tasks, never Archive, with the rest in the More (⋮) dropdown beside it
  - [x] two changes, a change plus a follow-up, a group, or nothing selected: the selection's actions in the toolbar are disabled, with a tooltip saying why; the context menu lists only the actions for the kinds of rows selected, disabled where they can't run
  - [x] Promote on FU-0003 and FU-0004 opens one tab running the promote command with both IDs
  - [x] Explore… and Propose… still ask for a description; Backlog review still starts its session
  - [x] in a project without OpenSpec set up for Claude Code, actions are disabled with the "isn't set up" tooltip
  - [x] the filter hides the unchecked types and capabilities, marks the "Follow-ups" group as filtered with the count shown, and never hides an unreadable follow-up
  - [x] Group By type and by capability show one group per value with counts, "none" and "unreadable" groups where needed, and the choice survives a restart
  - [x] with a proposal open and its change selected, typing in the proposal (which refreshes the panel) keeps the selection and the scroll position, without flicker
  - [x] resolving a selected follow-up removes it without errors
  - [x] a panel docked narrow keeps every toolbar action reachable with none overlapping
- [x] 5.3 Repeat the hover, selection, toolbar and context menu checks in the classic UI and in a light theme
- [x] 5.4 Under remote development (`./gradlew runSplitMode`), check that:
  - the toolbar and context menu appear and their actions run
  - double-click and F4 open files in the client's editor
  - a change's folder selection shows in the client's project tree
  - Promote's terminal tab opens in the client

## 6. Follow-up harvest

- [x] 6.1 List each out-of-scope issue discovered during implementation as a follow-up candidate in the completion summary; record them (see openspec/backlog/followup/README.md) on Derwa's go.
