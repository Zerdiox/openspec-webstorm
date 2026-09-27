# Tasks

## 1. Platform check

- [x] 1.1 Using the platform's 2026.1.5 source, confirm how to:
  - build a checkbox tree whose root and follow-up nodes are checkbox nodes and whose other nodes
    show no checkbox
  - set a check policy that propagates from a group down to its children but never from children
    up, with no toggle on a row click
  - make the renderer draw a group's box from its children's states, while keeping the current
    coloured-text rendering
  - toggle with Space: which rows it acts on, and whether it leaves typing to find a row alone
  - be told when a node's box changes
  - show an OK/Cancel message dialog with a "Don't ask again" option stored application-wide

  Record the chosen APIs in the implementation notes. Verify that none of them clashes with the
  double-click and Enter open handlers, speed search, or the popup that follows the selection.

## 2. Pure logic (TDD)

- [x] 2.1 Write failing tests for Promote's target:
  - toolbar with shown checked follow-ups: those follow-ups in list order, whatever is selected,
    including a change or a group
  - toolbar with none checked: today's selection rules and reasons
  - context menu: the selected follow-ups only, with checks ignored
  - the label: "Promote" with no target, "Promote F6" for one, "Promote 3" for several
  - confirmation needed only for the toolbar with two or more targets
  - the offer carries the targeted rows' IDs and titles
  - change actions don't change with checks

  Run them and verify they fail
- [x] 2.2 Write failing tests for which checked keys are kept on refresh: a key still among the open
  follow-ups, shown or hidden, is kept, and a key no longer among them is dropped. Run them and
  verify they fail
- [x] 2.3 Implement the selection-function changes and the kept-checks function, and verify that the
  tests from 2.1 and 2.2 and all existing tests pass

## 3. Checks in the reusable tree

- [x] 3.1 Write failing UI tests for the tree wrapper, which still takes no OpenSpec types:
  - only nodes marked checkable get a checkbox
  - checks are restored by key after a rebuild
  - a node missing from one rebuild and back in the next comes back checked
  - toggling a group checks or unchecks only its children in the tree
  - selection and collapsed groups still survive a rebuild

  Run them and verify they fail
- [x] 3.2 Make the tree wrapper a checkbox tree that keeps checked keys as a set and exposes the
  checked keys, then verify that the tests from 3.1 and the existing tree UI tests pass

## 4. Panel wiring

- [x] 4.1 Mark follow-up rows with an ID, and follow-up groups holding any of them, as checkable. Move the renderer onto
  the checkbox tree's renderer, keeping today's text, colours and tooltips. On each refresh, prune
  checks against the open follow-ups. Verify in the sandbox IDE that checkboxes show on follow-ups
  and follow-up groups only
- [x] 4.2 Give the toolbar and the context menu Promote their place, the shown checked follow-ups and
  the dynamic label. Show the confirmation for toolbar promotes that need it, and keep the checks
  on Cancel. Verify in the sandbox IDE that the toolbar label follows the checks and the selection

## 5. Checks

- [x] 5.1 Run `./gradlew check verifyPlugin` and confirm it's green
- [x] 5.2 Manual check in a regular (standalone) sandbox IDE (`./gradlew runIde`):
  - [x] follow-up rows and follow-up groups have checkboxes; change rows, the "Changes" group and
    messages don't; a follow-up without an ID has none
  - [x] clicking a box checks it, and the selection behaves as in the commit panel; clicking a title selects without
    checking; Space toggles the selected row's box; typing a title fragment still finds a row
  - [x] the "Follow-ups" group's box shows partial with some checked, and checks or unchecks all
    shown; the same works for type and capability groups when grouping
  - [x] a checked follow-up hidden by a filter isn't counted in the toolbar's Promote, and is checked
    again once the filter is cleared
  - [x] checks survive a refresh (edit a follow-up file) and a grouping change, and are gone after an
    IDE restart; resolving a checked follow-up drops it
  - [x] toolbar: with F5 and F7 checked and F6 selected, it reads "Promote 2" and, after confirming,
    promotes F5 and F7 in one tab; with none checked and F6 selected, it reads "Promote F6" and
    promotes without asking
  - [x] the confirmation lists each ID and title; Cancel opens no tab and keeps the checks; after
    "Don't ask again", the next multi-promote opens its tab without asking
  - [x] context menu: right-clicking unchecked F6 while others are checked shows "Promote F6" and
    promotes only F6, without asking; right-clicking inside a selection of two promotes both
    without asking
  - [x] double-click, Enter and Jump to Source still open files, and the context menu still selects
    the row it was opened on
