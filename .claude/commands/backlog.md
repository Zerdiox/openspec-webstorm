---
description: Capture, review or promote OpenSpec follow-ups
---
Follow-ups live in openspec/backlog/followup/, one file per issue. First
read openspec/backlog/followup/README.md and follow its rules for IDs,
naming, the template and resolving.

Argument: $ARGUMENTS

- "add" or empty: find out-of-scope issues from this conversation that we
  discussed but did not resolve. For each, check open follow-ups for the
  same issue first. List what you would create or update, with the next ID
  from the README, and wait for my go before writing any file. Then show
  me the IDs you created or changed.

- "review [capability]": read the frontmatter of all open follow-ups
  (top-level folder only, not resolved/), optionally filtered by
  capability. Group them by capability, then type. Flag any that look
  already fixed in the code or that duplicate each other. Suggest up to 3
  to promote, preferring clusters that could become one change.

- "promote <ID> [<ID> ...]": read those follow-up files and start the
  /openspec-explore flow using them as the brief. When we agree on scope,
  run /openspec-propose; the proposal's "Resolves" section must list these
  IDs. If we conclude a follow-up isn't worth doing, resolve it as wontfix
  per the README.
