# Follow-ups

Next ID: FU-0010

Out-of-scope issues noticed during planning, implementation or conversation,
one issue per file. Open follow-ups live in this folder; resolved or dropped
ones live in `resolved/`, which is gitignored — resolved follow-ups are kept
locally, not in the repo.

Like the rest of this folder they are working notes: code, config, the README
and skills never cite an `FU-*` file. If a reason needs to live near the code,
write it in the comment.

## Creating a follow-up

1. Check the open follow-ups in this folder for the same issue. If one
   exists, update it instead of creating a new one.
2. Take the ID from "Next ID" above, then increment "Next ID" by one in the
   same edit. Never reuse a number.
3. Create `FU-NNNN-short-kebab-title.md` in this folder using the template
   below. Use four digits (FU-0007, FU-0042).
4. Refer to follow-ups by ID (e.g. FU-0042) in proposals, commits and chat.

## Resolving a follow-up

- **Fixed by a change:** add `resolved: YYYY-MM-DD` and
  `resolved_by: <change-name>` to the frontmatter and move the file to
  `resolved/`. The change's proposal lists the ID under "Resolves".
- **Dropped:** if a follow-up is not worth doing (e.g. after exploring it),
  add `resolved: YYYY-MM-DD` and `resolved_by: wontfix`, add a one-line
  reason under "Notes", and move the file to `resolved/`. This prevents the
  same idea from being rediscovered and filed again.

Move with plain `mv`, never `git mv`: `resolved/` is gitignored, and `git mv`
would stage the file at its new path and keep it tracked. Never delete
follow-up files; old IDs must stay resolvable.

## Starting work on a follow-up

Point explore at the file explicitly:

    /openspec-explore FU-0042 — read openspec/backlog/followup/FU-0042-*.md and start from that

Related follow-ups can be explored together (e.g. several with the same
`capability`). When scope is agreed, `/openspec-propose` creates the change and
its proposal lists the resolved IDs under "Resolves".

## Merge conflicts on "Next ID"

If two branches both created follow-ups, this README will conflict on the
"Next ID" line. Keep the higher number plus the extra IDs used, and renumber
the newer duplicate follow-up (file name and `id` field).

## Template

    ---
    id: FU-NNNN
    title:
    found: YYYY-MM-DD
    source:          # change name, or "conversation"
    capability:      # matches a folder in openspec/specs/, if any
    location:        # path:line
    type:            # bug | tech-debt | test-gap | idea
    size:            # S | M | L
    # added on resolution:
    # resolved: YYYY-MM-DD
    # resolved_by: <change-name> | wontfix
    ---

    ## What
    One or two sentences describing the problem.

    ## Why it matters
    Impact or risk if left alone.

    ## Notes
    Anything the finder knew that a future reader won't.
