# Proposal

## Why

Each action opens a terminal tab named for the action and its target (for example `apply:
plan-panel-fixes`), but once Claude Code starts it sets the terminal title to its own session
summary, and the tab shows that instead. With several sessions open, the tabs no longer say which
change or follow-up each one is for, which is what naming them was meant to give.

## What Changes

- The tab an action opens tells Claude Code not to set the terminal title, so the tab keeps the name
  the panel gave it for as long as Claude Code runs. This holds on macOS and Linux, where Claude Code
  runs in the user's shell, and on Windows, where Claude Code is started directly.
- Because the setting belongs to the tab, running `claude` again in that tab after the first session
  exits also leaves the tab's name alone. Terminal tabs the user opens themselves are unaffected.

## Capabilities

### New Capabilities

### Modified Capabilities
- `openspec-workflow-panel`: "Each action starts a Claude Code session" gains a scenario that the
  tab keeps its name while Claude Code runs.

## Resolves

- F6: Claude Code replaces the tab name the plugin gives it

## Out of Scope / Deferred

- Showing Claude Code's session summary anywhere else, such as in the tab's tooltip. The tab name
  identifies the change or follow-up; the summary is visible in the session itself.
- Keeping the tab name against any other program run in the tab that sets the terminal title.

## Impact

- `shared`: the tab request sent to the client carries environment variables for the tab.
- `backend`: building the request sets the variable that stops Claude Code from changing the title,
  with tests.
- `frontend`: the terminal tab opener passes the request's environment variables to the tab.
