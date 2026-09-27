# Design

## Context

The panel's tree is a reusable wrapper that rebuilds every node on each refresh and restores the
selection and the collapsed groups by each node's key. Promote's target is worked out by a pure
function from the selection, which the toolbar and the context menu both call. The context menu
already selects the row that was right-clicked before it opens. See proposal.md for the behaviour
and the delta spec for the scenarios.

## Goals / Non-Goals

**Goals:**
- Checks live alongside the selection in the reusable tree, by key, so they survive the tree being
  rebuilt and a node disappearing and coming back after a filter is cleared.
- Promote's target, label and whether to confirm stay a pure function of the selection, the checks
  and where the action was chosen, so it can be unit-tested without a UI.

**Non-Goals:**
- A settings page, or any way to undo "Don't ask again" (F13).
- Checkboxes for any node that isn't a follow-up.

## Decisions

### Use the platform's checkbox tree, not a hand-made checkbox

The tree gets the platform's checkbox handling, the same helper its checkbox tree is built on. Its
nodes are still ordinary tree nodes, so the key-based rebuild keeps working. Only nodes marked checkable become checkbox nodes: follow-up rows
with an ID, and the groups of follow-ups. Change rows, the "Changes" group and messages stay plain
nodes and show no checkbox. The platform tree provides the commit panel's look, Space toggling that
doesn't interfere with typing to find a row, a three-state box on groups and screen-reader support.
A checkbox drawn by hand in the renderer would have to rebuild all of that.

The check policy propagates from a group to its children in both directions, and never from children
up to parents. A group's box shows its children's states: its own checked flag is kept equal to
"every child checked", so the renderer draws it as checked, unchecked or partial, and a click on a
partial group checks everything. Clicking a row
outside the box doesn't toggle it, so a click still only selects.

*Alternative:* a separate "pick" list outside the tree. Rejected, because it isn't how the IDE does
it and it doubles the UI.

### Checks are a set of keys owned by the tree wrapper

The wrapper keeps the checked keys as a set. When it rebuilds the nodes, a checkable node is checked
if its key is in the set. When a node's box changes, its key is added or removed. A follow-up hidden
by the filter isn't in the tree, so its key simply stays in the set and it comes back checked. The
panel keeps only the checked keys that are still among the open follow-ups, shown or hidden, on each
refresh, so a resolved follow-up drops out. Checks are never written to the project's settings, so
a restart clears them.

Toggling a group changes only the children in the tree, which are the shown ones, so hidden checks
are untouched, as the spec asks.

### Promote's target depends on where it was chosen

The selection function gains the shown checked follow-ups and the place the action was chosen
(toolbar or context menu):
- Toolbar: if any shown follow-up is checked, the target is those follow-ups in list order, whatever
  is selected. Otherwise, the existing selection rules and reasons apply.
- Context menu: the existing selection rules and reasons apply, and checks are ignored.

The promote offer carries the follow-up rows it targets, not only their IDs. The label ("Promote
F6", "Promote 3") and the confirmation's ID and title list come from those rows. With no target the
label stays "Promote". Change actions don't depend on checks.

### Confirmation through the platform's message dialog with "Don't ask again"

Promoting from the toolbar with two or more targets shows the platform's OK/Cancel message dialog
with a "Don't ask again" option. The choice is stored application-wide, since the question is the
same in every project. Choosing Cancel keeps the checks. The context menu and single targets skip
the dialog. Whether the dialog is needed is part of the pure selection result, so tests cover it.

## Risks / Trade-offs

- [The checkbox tree's own key and mouse handling may clash with double-click opening, typing to
  find a row, or the popup that follows the selection] → Check the platform source before wiring
  it. Keep the existing UI tests for selection and collapsed groups green, and add UI tests for
  checks surviving a rebuild and a hidden follow-up coming back checked.
- [Space toggles on the lead row only, not on every selected row, if that's how the platform
  behaves] → The spec only needs one selected row to toggle. Follow the platform, as the commit
  panel does.
- [Group boxes drawn from children: the platform works out a group's partial state from its
  children only if the renderer is set up for it] → Set up the renderer that way, and check the
  group-toggle scenario in a UI test.
- ["Don't ask again" can't be undone] → Accepted for now, tracked as F13.

## Implementation notes

Platform APIs chosen after checking the 2026.1.5 platform:
- Checkboxes: `CheckboxTreeHelper(policy, dispatcher).initTree(tree, tree, renderer)` on the
  existing `Tree`, not a `CheckboxTreeBase`. The helper's click listener consumes every
  double-click on a checkbox node, and `CheckboxTreeBase` sets its toggle click count to -1. Either
  would stop double-click from opening a follow-up or expanding a group. Calling `initTree` after
  the edit-source handlers are installed lets them see a double-click first. The helper and its
  `CheckPolicy` carry no internal-API markers.
- Policy: `CheckPolicy(checkChildrenWithCheckedParent = true, uncheckChildrenWithUncheckedParent =
  true, checkParentWithCheckedChild = false, uncheckParentWithUncheckedChild = false,
  checkByRowClick = false)`.
- Nodes: `CheckedTreeNode` for checkable nodes and the root, and `DefaultMutableTreeNode` for the
  rest. The renderer hides the box for anything that isn't a `CheckedTreeNode`.
- Renderer: `CheckboxTreeCellRendererBase(opaque = false, usePartialStatusForParentNodes = true)`,
  drawing the current text through `textRenderer` in `customizeRenderer`. Given a plain tree and a
  non-default policy, a parent whose own flag differs from its children's shared state draws as
  partial. That's why groups keep their flag equal to "every child checked".
- Space: the helper's key listener toggles the lead row and sets every other selected checkbox
  row to the same state. It stands aside while the speed-search popup is open
  (`SpeedSearchSupply.getSupply` is non-null only then). Clicking a box also selects its row.
- Changes: the dispatcher's `nodeStateChanged`, fired for every node whose flag changes, including
  the children a group click changes.
- Confirmation: `MessageDialogBuilder.okCancel(...).yesText(...).doNotAsk(option).ask(project)`,
  with a `DoNotAskOption.Adapter`. The adapter only sets the dialog's checkbox: the caller skips the
  dialog itself when the stored choice says so. `rememberChoice` stores the choice in the
  application-level `PropertiesComponent`, and only on OK, since `shouldSaveOptionsOnCancel` is
  false.
