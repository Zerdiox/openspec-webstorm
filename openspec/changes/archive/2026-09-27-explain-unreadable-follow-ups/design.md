# Design

## Context

The follow-ups source tells a readable file from an unreadable one, but for an unreadable file it
keeps only a flag. It turns every failure into `null`, including a caught YAML exception, so the
reason and the position are lost before they reach the panel model. The panel model fills in an
unreadable row's `id` (its ID from the file name, or else the file name) and its `title` (the file
name). The renderer always draws `id  title`, which is why the file name shows twice (F11). The
`id` is also used by the row's speed-search text and by Promote.

## Goals / Non-Goals

**Goals:**
- Keep why a file couldn't be read, and which line of the file to open at, from reading all the way
  to the row, its tooltip and its navigation.
- Let the model decide that an unreadable follow-up can't be promoted, and let the checkbox and the
  selection logic follow from that.

**Non-Goals:**
- Checking the frontmatter's fields against the template. Valid YAML stays readable.
- Changing the row key or the speed-search text.

## Decisions

### The reason travels as one value, not as a flag plus loose fields
`FollowUp.unreadable: Boolean` becomes `problem: Problem?`, where
`Problem(reason: String, line: Int, message: String? = null)`:
- `reason` is the short text shown on the row.
- `line` is the 1-based line of the file.
- `message` is the parser's own text, when there is one.

`unreadable` stays as a computed property, so the grouping, filtering and checkbox code keep
working unchanged. A sealed type with one case per failure was considered. Nothing branches on the
kind of failure, though: every consumer only shows the text or jumps to the line, so plain data is
enough.

| Failure | reason | line |
|---|---|---|
| First line isn't `---` | `no frontmatter` | 1 |
| No closing `---` | `frontmatter isn't closed` | 1 |
| YAMLException with a problem mark | `YAML error on line N` | N |
| YAMLException without a mark | `YAML error` | 2 |
| YAML parses to anything but a map (including empty) | `frontmatter isn't a list of fields` | 2 |

### A YAML error's line is shifted to count from the top of the file
The parser only sees the text between the two `---` lines, and its problem mark counts lines from
0. The file line is therefore `mark.line + 2`: one to count from 1, and one for the opening `---`.
When the mark is a `MarkedYAMLException`, `message` comes from `problem` (for example "mapping
values are not allowed here"). Its full `message` is left out, because it adds a multi-line snippet
of the YAML, which is too much for a tooltip.

### The file name shows once in the renderer; the model keeps `id`
The model keeps `id` as it is (the ID from the file name, or else the file name), because speed
search and the Promote label read it. For an unreadable row the renderer draws only the file name
(`title`), then `unreadable: <reason>` in the error colour. The tooltip is
`<file> · <reason>: <message>`, or `<file> · <reason>` without a message.

An empty title in the model was considered. It would drop the file name for files that have an ID,
and the file name is what you need to go and fix the file.

### The row carries the problem; navigation reads its line
`FollowUpRow` replaces `unreadable: Boolean` with `problem: Problem? = null`, copied from the follow-up,
and derives `unreadable` and `line` from it. The row needs more than the line: the renderer also
shows the reason and the parser's message in the tooltip. Copying only `line` and `message` onto the
row would split one value into loose fields again. The open handler gives the navigatable an optional
line and uses `OpenFileDescriptor(project, file, line - 1, 0)` when one is set, so readable follow-ups
and changes open as they do today.

### Promote is removed in the model; the selection logic gives the reason
The model sets `promote = null` for an unreadable follow-up, whether or not the file name has an
ID. `check-follow-ups-to-promote` already gives a checkbox only to rows with a `promote`, so an
unreadable row loses its checkbox and can't be one of the checked rows the toolbar acts on. In the
selection logic, a selected unreadable follow-up is checked before the "no ID" case and gives the
new reason, `An unreadable follow-up can't be promoted`. With a file name that has no ID, the
unreadable reason is the more useful of the two.

## Risks / Trade-offs

- [This change touches the same Promote and panel code as `check-follow-ups-to-promote`, which is
  still in progress] → Apply this change only after that one is done, and build on its code.
- [A YAML problem mark could point past the frontmatter, for example at its end] → Clamp the line
  to the frontmatter's closing line. The editor opens a line past the end of the file at its end
  anyway.
