# Design

## Context

See proposal.md for the motivation and specs/openspec-workflow-panel/spec.md for the behaviour.

The panel's rows are stacked in a plain panel inside a scroll pane. A plain panel isn't `Scrollable`,
so the scroll pane lets it take its preferred width: the full title plus every button. A label only
shortens itself with "…" when its layout gives it less width than it asks for, so a long title never
shrinks. The whole row just grows past the viewport and the buttons land out of view.

The plugin may depend only on the platform module and the bundled Terminal plugin, so every control
here has to come from the platform itself.

## Goals / Non-Goals

**Goals:**
- Rows that follow the tool window's width, using the layout the panel already has.
- A change row's right side narrow enough to fit a typical side tool window.
- Only platform controls, so the panel looks right in the New UI, the classic UI, and light or dark
  themes without extra work.

**Non-Goals:**
- Custom painting, hover animations or cursor changes.
- A different layout for very narrow panels (wrapping or stacking rows). Shortening the title is
  enough once the actions are a single split button.

## Decisions

### The rows' container tracks the viewport's width
The panel inside the scroll pane implements `Scrollable` and reports that it tracks the viewport's
width, but not its height. The row layout then gets the real available width, the title label in
the centre of each row takes what's left, and Swing shortens it with "…".

- *Alternative:* a horizontal scroll bar policy of "never". This only hides the scroll bar; the
  content stays wider than the viewport and gets clipped.
- *Alternative:* the platform's `ScrollablePanel`. It sits in an implementation module outside the
  platform's public API, so a small panel of our own is safer across IDEs.

Every row with a title that can be shortened gets a tooltip with the full title. Follow-up rows
already have one; change rows gain one with the change's name.

### A follow-up's type and capability sit under its title
A follow-up row has two lines: its ID and title, and below it the type and capability in small,
muted text. On the right they took width away from the title; underneath they cost only height. The
row's actions are centred beside both lines. A change's task count stays on the right: it is short,
and it explains which action the split button offers.

### A change's actions are one `JBOptionButton`
`JBOptionButton` is the platform's split button (the one on the Commit dialog). Its main action is
the change's likely next step, and its options are the other actions. It keeps the platform's own
look, hover and focus behaviour, and needs about a third of the width of three separate buttons.

- *Alternative:* action links. They are native and narrower, but three of them still take more room
  than one split button, and a link reads as navigation rather than as starting a session.
- *Alternative:* a small toolbar of icon buttons. It's narrowest, but apply, verify and archive have
  no icons that explain themselves.

Its dropdown lists the options without separators between them: with at most three short entries,
lines between each one add noise rather than grouping.

When OpenSpec isn't set up for Claude Code, the whole button is disabled with the existing
"isn't set up" tooltip, as the separate buttons are today.

### The panel model orders a change's actions, with the default first
The model's change row keeps a list of actions, ordered so the first is the one-click action and the
rest follow in the dropdown's order. The rule lives in the model so it is unit tested alongside the
existing panel model tests, and the view only splits the list into the main action and the options.

| Tasks          | One-click | Dropdown                  |
|----------------|-----------|---------------------------|
| none           | Explore   | Apply, Verify, Archive    |
| some remaining | Apply     | Verify, Archive, Explore  |
| all done       | Verify    | Archive, Apply, Explore   |

The dropdown puts the next most likely step first and Explore last once implementation has started.
"All done" means the done count is at least the total and the total is above zero.

### Explore on a change reuses the explore command with the change's name
The command for exploring already takes one argument, which the top Explore button fills with a
description. A change passes its name instead, so the command resolver and the tab naming need no
new cases. No dialog is shown: the conversation continues in the terminal, where Claude has already
read the change.

### Refresh is a title action on the tool window
The tool window factory sets one title action: a refresh action with the platform's refresh icon and
a "Refresh" tooltip, which calls the panel's existing refresh. The title bar gives it the IDE's own
hover highlight and frees the panel's bottom row.

- *Alternative:* an action registered in the plugin descriptor. It would be bindable in the keymap
  and show in Find Action, but it would need a way to find the panel of the current project. That's
  more code for a fallback, since the panel refreshes itself on file changes and when shown.

### Padding goes on the description text area itself
An empty border on the text area, scaled for the screen, keeps the padding inside the scrolling
area, so the text stays clear of the edge when it scrolls.

## Risks / Trade-offs

- [The split button looks or sizes differently in the New UI and the classic UI] → a manual check in
  both, listed in the tasks.
- [Apply, Verify or Archive now takes two clicks when it isn't the default] → the default follows
  the change's tasks, so the common next step is still one click. Archive takes two by design.
- [A change reported with a done count above its total] → treated as all done, so it never falls
  back to Apply by accident.
