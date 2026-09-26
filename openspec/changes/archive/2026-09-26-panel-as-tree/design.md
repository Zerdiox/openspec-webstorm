# Design

## Context

The panel builds its content as a vertical stack of plain Swing panels: a heading panel per section
and, for each change or follow-up, a row panel with labels and inline buttons. It clears and
rebuilds that stack on every refresh, and a refresh runs whenever a file under `openspec/`
changes, including while the user types in a proposal. The panel's model is pure. It turns the
project set-up, the changes and the follow-ups into rows with action buttons, and it is unit-tested.
The panel runs in the backend module, so under remote development it runs on the backend. The
behaviour required is in the spec delta. The motivation is in proposal.md, under Why.

## Goals / Non-Goals

**Goals:**
- Use the platform's tree, toolbar, context menu, Edit Source action and double-click and Enter
  handlers, so the panel behaves like other tool windows without recreating any of them.
- Actions, filtering and grouping as pure functions of the model and the selection, unit-tested
  without Swing.
- A reusable tree set-up that knows nothing about OpenSpec.

**Non-Goals:**
- Drag and drop.
- Hover buttons on rows (see proposal, Out of Scope).
- Filtering changes.
- Changing the commands sent or how terminal tabs open, except that Promote can carry several IDs.

## Decisions

### A platform tree with a renderer, rows as data
The panel becomes a tool-window panel with a toolbar on top and a platform tree in a scroll pane.
Each node holds plain data (a group, a change, a follow-up or a message), and a coloured-text
renderer draws it. The name or title uses normal text and the metadata uses secondary text, the way
the commit panel shows file paths. Hover, selection colours, focus, keyboard navigation, speed
search, expansion and accessibility come from the tree. A row too long for the panel shows in full
on hover through the tree's own expandable-row popup.
*Alternative:* keep Swing rows and recreate list behaviour around them. Rejected, because it would
recreate what the tree already does, and the rows' live buttons were the only reason for it.

### Actions are IDE actions reading the selection from the data context
The panel provides its selection (the selected changes and follow-ups, in list order) to the data
context under the panel's own data key. It also provides the selected rows' navigation targets as
the standard navigatable data. Every action reads the selection from there, so the same action
instances fill both the toolbar and the context menu:
- Explore…, Propose… and Backlog review for the project.
- The selection's next step, shown as a text button, followed by a "More" dropdown holding the
  selected change's other actions. Both come from the same change-action ordering as today. The
  platform's toolbar split button shows only an icon, never a text label, so a text button plus an
  adjacent dropdown is the native way to keep "Apply", "Verify" or "Explore" readable in the
  toolbar.
- Promote.
- The platform's Edit Source action, first in the context menu.

What an action does for a given selection is decided by a pure function over the model. That
function returns, for each action, whether it is offered, whether it is enabled, the reason when it
isn't, and its target: a change name, or follow-up IDs joined by spaces. Unit tests cover that
function; the actions only display its answer.
*Alternative:* Swing buttons in a panel above the tree. Rejected, because the IDE's toolbar handles
overflow in a narrow panel (FU-0004), follows the user's toolbar settings, and shares its actions
with the context menu.

### Opening uses the platform's handlers
Double-click and Enter are installed with the platform's edit-source handlers for trees, which also
leave double-click on a group node collapsing or expanding it. F4 and the context menu's Jump to
Source come from the platform action and the user's keymap, through the navigatable data. For
several selected rows, the data holds every selected row's target, so all of them open.

### Which file a change opens is decided when it's opened
A change node carries its change folder. When it's opened, a small function checks whether the
proposal exists: if it does, the proposal is the target; otherwise the target is the folder, which
is selected in the project tree. Checking at open time covers a proposal created since the last
refresh. The function is tested against a temporary directory, so the panel model stays pure. A
follow-up node carries its file. A target that has disappeared by the time it is opened does
nothing. The panel refreshes on the same file change anyway.

### A reusable keyed-tree set-up
One generic helper, with no OpenSpec types, owns the tree's lifecycle:
- It takes a list of keyed nodes on every refresh and replaces the tree's model with them.
- It restores the selection and expansion by key, so the panel needs no refresh logic of its own.
- It stores collapsed group keys under a caller-supplied settings key in the project's settings.
- It installs speed search and the double-click and Enter handlers.

A change's key is its name. A follow-up's key is its file name, because an unreadable follow-up has
no reliable ID. A group's key is its kind plus its value, for example the type group "bug", so
switching the grouping doesn't mix up collapsed states. The part that works out which keys to
reselect and re-expand after an update is a pure function with unit tests.
*Alternative:* the platform's saved tree state. Rejected, because it matches nodes by their
displayed text, which changes whenever a count or title changes.

### Filtering and grouping are part of the model
The panel model takes the follow-up view settings (types and capabilities to show, and the
grouping) and produces the follow-up groups:
- The capability filter offers the capabilities of the open follow-ups.
- Follow-ups with no type or no capability fall under a "none" choice in the filter and a "none"
  group in the grouping.
- Unreadable follow-ups are never filtered out, and when grouped they get their own "unreadable"
  group.

The settings are stored in the project's settings and read when the panel is created. The toolbar
offers a filter action (a popup of checkable types and capabilities, with the platform's filter
icon) and a Group By action (a popup of None, Type and Capability). Changing either rebuilds the
tree from the last loaded data without reloading.

### Promote carries several IDs
The launcher already takes a free-text target. Promote on several follow-ups passes their IDs,
joined by spaces in list order, as that target. The command becomes the backlog promote command
followed by the IDs. The tab name comes from the same target, shortened as it is today.

## Risks / Trade-offs

- [Select-then-act adds a click compared with today's row buttons] → The next step is the toolbar's
  main action as soon as a change is selected, and the proposal lists hover buttons on rows as a
  candidate follow-up if this proves costly.
- [Toolbar actions show icons by default, so text labels such as "Apply" may need a toolbar
  presentation flag, and that flag differs between releases] → Checked against the platform
  source for the oldest supported release before building the toolbar (task 1).
- [Under remote development the panel is backend UI shown in the client, and whether F4, the
  toolbar, context menus and opening editors all reach the client is unverified] → Manual check in
  split mode.
- [A refresh while the context menu is open replaces the tree's model] → Actions read the selection
  when they run, and the selection is restored by key, so the menu still acts on the same rows. If
  a row disappeared, its action is disabled on update.
- [Replacing the model on every refresh could flicker or reset scrolling] → The helper keeps the
  scroll position together with the selection and expansion. The manual check includes typing in a
  proposal while the panel is visible.

## Implementation notes

Platform APIs chosen after checking the 2026.1.5 platform (all compile against it):
- Panel and toolbar: `SimpleToolWindowPanel` with `setToolbar`/`setContent`, and
  `ActionManager.createActionToolbar` with its target component set to the tree. Text in the
  toolbar comes from the `ActionUtil.SHOW_TEXT_IN_TOOLBAR` presentation property.
- Tree: `Tree` with a `ColoredTreeCellRenderer`, `TreeSpeedSearch.installOn`, and
  `EditSourceOnDoubleClickHandler.install` / `EditSourceOnEnterKeyHandler.install`.
- Data: `UiDataProvider.uiDataSnapshot` on the panel. Navigation targets go under
  `CommonDataKeys.NAVIGATABLE_ARRAY`, lazily; the selection goes under the panel's own `DataKey`.
- Context menu: `PopupHandler.installPopupMenu` with a group that starts with
  `IdeActions.ACTION_EDIT_SOURCE`.
- Icons: `AllIcons.General.Filter`, `AllIcons.Actions.GroupBy` and `AllIcons.Actions.More`.
- Opening: `OpenFileDescriptor` for files, and `PsiManager.findDirectory` +
  `PsiNavigationSupport.navigateToDirectory` for a change folder.
- Settings: `PropertiesComponent.getInstance(project)` with `getList`/`setList`.
