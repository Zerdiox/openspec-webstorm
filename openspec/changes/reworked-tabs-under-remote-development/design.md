# Design

## Context

See proposal.md for why. What shapes the approach:

- **Who can create which tab.** Under remote development the plugin runs on the backend. The only
  tab the backend can open is a Classic one, which reaches the client through a relay that never
  passes on the tab's real height. The tab Claude Code renders correctly in (the reworked terminal)
  can only be created on the client, through the Terminal plugin's client-side tab manager. The
  backend's own terminal tab store is internal, and the client only reads it to restore tabs; it
  can't be used to push a new tab to the client.
- **How a plugin gets code onto the client.** A plugin runs code on the client only as a modular
  plugin: a root descriptor that lists content modules, and each module declares its own
  dependencies. `<depends>` isn't allowed there. A module that depends on the platform's backend
  module loads only where the project lives; one that depends on the frontend module loads only
  where the UI is. In a standalone IDE both load.
- **Backend-to-client messages.** The platform's remote topics are an event type with a
  serializer. The backend sends an event and a listener registered on the client receives it along
  with the client-side `Project`. They exist in every supported release (checked in 2026.1.5 and
  2026.2.3). They need only Kotlin's serialization compiler plugin. The two-way `@Rpc` interfaces
  also need JetBrains' separate `rpc` compiler plugin, from its own Maven repository, with versions
  tied to Kotlin's.
- **The reworked tab API we need** (`createTabBuilder`, `workingDirectory`, `tabName`,
  `requestFocus`, `createTab`, and sending text to the tab's view) has the same signatures in
  WebStorm and PhpStorm 2026.1.5 and 2026.2.3.
- **Installation.** The IDE offers to put a plugin on the other side only for Marketplace plugins.
  This one is installed from a zip, so the client copy is installed by hand (see proposal.md).

## Goals / Non-Goals

**Goals:** the behaviour in the delta spec; the same code path in a standalone IDE and under remote
development; `./gradlew check verifyPlugin` green for all four IDE versions.

**Non-Goals:** moving the panel itself to the client, which works as it is; any two-way RPC;
changing how the command line is built or how `claude` is found.

## Decisions

**Three content modules in one plugin.** `dev.derwa.openspec.shared` (required), `…backend` and
`…frontend`, as Gradle subprojects `shared/`, `backend/` and `frontend/` wired in with
`pluginModule(implementation(project(…)))` under the `org.jetbrains.intellij.platform.module` plugin,
as in JetBrains' modular plugin template. The root `plugin.xml` keeps id, name, vendor and
description, and gains a `<content>` list.
- *Backend*: everything that exists today except opening the tab. That is the tool window, panel,
  sources, dialog, command resolution, finding `claude`, and the notification group. It depends on
  `intellij.platform.backend` and the shared module. It no longer touches the Terminal plugin at
  all.
- *Frontend*: the topic listener and `ReworkedTerminalTab`, unchanged. It depends on
  `intellij.platform.frontend`, the Terminal plugin's `intellij.terminal.frontend` module and the
  shared module.
- *Shared*: the event and the topic.

Because the tool window is registered by the backend module, a copy installed in the client adds no
panel there. That removes the reason the README gave for not installing it on the client.

Alternatives: moving the whole panel to the client and asking the backend for data. That means
rewriting everything as RPC, for a panel that already works. Or a separate client-only plugin. That
is two plugins to build and keep in sync, where one modular plugin is what the platform intends.

**Remote topic, sent to the clicking client.** The shared module defines a `@Serializable` event
with the tab name, the working directory and the command line, and a `ProjectRemoteTopic` for it.
The backend sends it with `sendToClient(project, event, clientId)`. `clientId` is `ClientId.current`,
captured on the EDT when the button is clicked, before the PATH check moves to a pooled thread.
`broadcast` would open the tab for every client connected to the backend, for example Code With Me
guests. Alternative: an `@Rpc` API the client calls. It would let the backend confirm the tab
opened, but that confirmation is only useful for the fallback we ruled out, and it brings the extra
compiler plugin.

**The backend supplies the working directory.** It sends its own `project.basePath` instead of the
client using the client-side project's. The shell starts where the project lives, and that is the
backend's path. The client's project may not have the same path, or any path.

**No Classic fallback, no Classic code.** `openPreferringReworked` and the Classic tab are deleted
along with their tests. Without the client module a click opens no tab, as agreed. In a standalone
IDE the frontend module is always there.

**Kotlin serialization.** Add the `org.jetbrains.kotlin.plugin.serialization` Gradle plugin at the
Kotlin version already used (2.3.20). The platform bundles the serialization runtime, so the
library is `compileOnly`.

**Build and run.** The existing `pluginVerification` IDE list stays and now checks the modular
plugin. Add a `runIde` registration with `splitMode = true` and `pluginInstallationTarget = BOTH`
for the manual remote-development check. The existing `runIde` covers standalone. The WebStorm
2026.1.5 target, `bundledPlugin("org.jetbrains.plugins.terminal")` and
`bundledModule("intellij.terminal.frontend")` move to the subprojects that need them.

**Testing.** Unit tests are JUnit 4, like today, and move with their code into `backend/`. New
tests: the event round-trips through its serializer, and the launcher hands a sender the right event
once `claude` is found and none when it isn't. For the launcher test, the sender is a function
parameter, the same way `openPreferringReworked` took lambdas. The topic delivery itself is checked
by hand in both run modes.

## Risks / Trade-offs

- [A topic sent in a standalone IDE might not reach a listener in the same process] → The first
  task checks this in `runIde` before anything else is built on it. If it fails, stop and revisit
  this design with Derwa instead of patching around it.
- [The remote topic and `ClientId` APIs may be marked experimental or internal] → `verifyPlugin`
  reports that. Experimental use is acceptable, since the whole split-mode API is experimental. If
  `sendToClient` or `ClientId` turns out to be internal, use `broadcast` and note the
  multi-client trade-off.
- [Forgetting the client copy under remote development gives a silent click] → The README says
  to install on both sides. This is accepted in proposal.md. A Marketplace listing would remove it.
- [Moving sources into subprojects makes a large diff] → Move files unchanged in one task, before
  any behaviour change, and keep `check` green at that point.
- [Experimental split-mode APIs change in a later release] → `verifyPlugin` against the newest
  IDEs on every build. The code that uses them is two small files.

## Migration Plan

Under remote development: install the new zip on the backend (Plugins (Host)) and in the client
(Plugins), then restart both. Standalone: install as before. Rollback: install the previous zip on
the backend and uninstall the client copy.
