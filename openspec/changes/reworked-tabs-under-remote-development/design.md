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
- **Backend-to-client messages.** The split-mode docs name RPC as the way the two sides talk: an
  `@Rpc` interface in a shared module, implemented on the backend and registered through a
  `remoteApiProvider`, which the client resolves and calls. A call may return a `Flow`, which lets
  the client subscribe to events the backend pushes. This needs Kotlin's serialization compiler
  plugin and JetBrains' `rpc` compiler plugin, from JetBrains' own Maven repository, with a version
  tied to Kotlin's. The platform's remote topics would have avoided the `rpc` plugin, but the
  listener interface that receives a topic, and `sendToClient`, are `@ApiStatus.Internal` in
  2026.1.5 and 2026.2.3, and the docs don't describe them. The `@Rpc` API (`Rpc`, `RemoteApi`,
  `RemoteApiProvider`, `RemoteApiProviderService.resolve`, `durable`, `projectId()`,
  `findProjectOrNull`) and `ClientId.current` aren't; the generated stubs use experimental API.
- **The reworked tab API we need** (`createTabBuilder`, `workingDirectory`, `tabName`,
  `requestFocus`, `createTab`, and sending text to the tab's view) has the same signatures in
  WebStorm and PhpStorm 2026.1.5 and 2026.2.3.
- **Installation.** The IDE offers to put a plugin on the other side only for Marketplace plugins.
  This one is installed from a zip, so the client copy is installed by hand (see proposal.md).

## Goals / Non-Goals

**Goals:** the behaviour in the delta spec; in a standalone IDE the tab opens directly, the way it
does today, with no RPC involved; `./gradlew check verifyPlugin` green for all four IDE versions; no
internal API.

**Non-Goals:** moving the panel itself to the client, which works as it is; any client-to-backend
call beyond subscribing; changing how the command line is built or how `claude` is found.

## Decisions

**Three content modules in one plugin.** `dev.derwa.openspec.shared` (required), `…backend` and
`…frontend`, as Gradle subprojects `shared/`, `backend/` and `frontend/` wired in with
`pluginModule(implementation(project(…)))` under the `org.jetbrains.intellij.platform.module` plugin,
as in JetBrains' modular plugin template. The root `plugin.xml` keeps id, name, vendor and
description, and gains a `<content>` list.
- *Backend*: everything that exists today except opening the tab. That is the tool window, panel,
  sources, dialog, command resolution, finding `claude`, and the notification group; plus the RPC
  implementation and its provider. It depends on `intellij.platform.backend`, the platform's RPC
  backend module and the shared module. It no longer touches the Terminal plugin at all.
- *Frontend*: the subscriber, the local tab opener and `ReworkedTerminalTab`, unchanged. It depends on
  `intellij.platform.frontend`, the Terminal plugin's `intellij.terminal.frontend` module and the
  shared module.
- *Shared*: the request type, the `@Rpc` interface, and the local tab opener's interface and
  extension point.

Because the tool window is registered by the backend module, a copy installed in the client adds no
panel there. That removes the reason the README gave for not installing it on the client.

Alternatives: moving the whole panel to the client and asking the backend for data. That means
rewriting everything as RPC, for a panel that already works. Or a separate client-only plugin. That
is two plugins to build and keep in sync, where one modular plugin is what the platform intends.

**The client subscribes; the backend pushes to the clicking client.** The shared module defines a
`@Serializable` request (tab name, working directory, command line) and an `@Rpc` interface with
one call, `openTabRequests(projectId): Flow<request>`. On the client a project startup activity
starts collecting it inside `durable { }`, so it resubscribes after a reconnect, in a coroutine
scope that ends with the project. On the backend a project service holds a hot flow of requests,
each tagged with the `ClientId` that clicked. The RPC implementation captures `ClientId.current`
when a client subscribes and passes on only that client's requests. The launcher captures
`ClientId.current` on the EDT when the button is clicked, before the PATH check moves to a pooled
thread. A request with no subscriber is dropped, so without the client copy a click opens nothing
and nothing errors. The spike checks that `ClientId.current` inside the RPC call identifies the
subscribing client; if it doesn't, requests go to every subscriber of the project, like a
broadcast, and that trade-off (Code With Me guests also get the tab) is noted here.

**A standalone IDE opens the tab directly.** The shared module declares a `localTabOpener`
extension point whose interface takes the project and the request; the frontend module registers
the one implementation, which calls `ReworkedTerminalTab`. The backend asks the extension point
when a button is clicked. In a standalone IDE both modules are loaded in one process, so there is
an opener and the backend calls it on the EDT: the same direct call as today, with no RPC, no
subscription and no `ClientId`. On a remote-development backend the frontend module isn't loaded,
so there is no opener, and the request goes to the clicking client over RPC as above. Deciding by
what's loaded needs no API to tell the IDE modes apart. The client's subscription still starts in a
standalone IDE, but nothing is ever sent on it there. Trade-off: in a standalone IDE hosting Code
With Me, the tab opens in the host's UI, as it does today.

Alternatives: the platform's remote topics, a one-way push the backend sends. That's less code and
no `rpc` plugin, but receiving a topic means implementing an internal interface (see Context).

**The backend supplies the working directory.** It sends its own `project.basePath` instead of the
client using the client-side project's. The shell starts where the project lives, and that is the
backend's path. The client's project may not have the same path, or any path.

**No Classic fallback, no Classic code.** `openPreferringReworked` and the Classic tab are deleted
along with their tests. Without the client module a click opens no tab, as agreed. In a standalone
IDE the frontend module is always there, so the local opener is too.

**Kotlin serialization and RPC.** Add the `org.jetbrains.kotlin.plugin.serialization` Gradle plugin
at the Kotlin version already used (2.3.20) and the `rpc` plugin at the version the docs pair with
2026.1 (`2.3.20-RC2-0.1`), resolved from `packages.jetbrains.team/maven/p/ij/intellij-dependencies`.
Both go on every subproject that declares or uses the `@Rpc` interface. The platform bundles the
serialization runtime, so the library is `compileOnly`.

**Build and run.** The existing `pluginVerification` IDE list stays and now checks the modular
plugin. Add a `runIde` registration with `splitMode = true` and `pluginInstallationTarget = BOTH`
for the manual remote-development check. The existing `runIde` covers standalone. The WebStorm
2026.1.5 target, `bundledPlugin("org.jetbrains.plugins.terminal")` and
`bundledModule("intellij.terminal.frontend")` move to the subprojects that need them.

**Testing.** Unit tests are JUnit 4, like today, and move with their code into `backend/`. New
tests: the request round-trips through its serializer; the launcher builds the right request once
`claude` is found and none when it isn't, hands it to the local opener when there is one and to the
remote sender only when there isn't; and the backend's request flow passes each subscriber only its
own client's requests. For the launcher test, the opener and the sender are parameters, the same way
`openPreferringReworked` took lambdas. Delivery over RPC is checked by hand in both run modes.

**Checked against the modular plugin template and the docs (task 1.2).** Where they differ from
the plan above:
- Module descriptors are named after the module (`dev.derwa.openspec.backend.xml`) and sit directly
  in each subproject's `src/main/resources`, not in `META-INF/`.
- The template applies `org.jetbrains.intellij.platform.settings` in `settings.gradle.kts` and
  declares repositories in `dependencyResolutionManagement`. Subprojects apply
  `org.jetbrains.intellij.platform.module` and declare no IDE of their own. The root declares the IDE
  and adds each subproject with `pluginModule(implementation(project(…)))`.
- The template sets `splitMode` and `pluginInstallationTarget` globally. The docs also allow them per
  `intellijPlatformTesting.runIde` registration, which is what we do so the default `runIde` stays
  standalone.
- The template's backend depends on `intellij.platform.backend`, `intellij.platform.kernel.backend`
  and `intellij.platform.rpc.backend` (Gradle `bundledModule`s, and the first two in its
  descriptor), and registers its API with `platform.rpc.backend.remoteApiProvider`.

**What the spike showed (tasks 3.1 and 3.2).** In the split-mode sandbox the backend loads only the
shared and backend modules, and the client only the shared and frontend modules. The request
reached the client's subscriber for the right project 13 ms after the click, on a coroutine worker
thread, so opening the tab has to switch to the EDT. `ClientId.current` inside the RPC call was
the same as the one captured at the click, so filtering by client works as designed. The IntelliJ
Platform Gradle Plugin already has a `runIdeSplitMode` task, but it installs the plugin on the
backend only by default, so ours is registered as `runSplitMode`.

**What `verifyPlugin` showed (task 4.8).** Compatible with WebStorm and PhpStorm 2026.1.5 and
2026.2.3, with no internal API usages. The verifier reports 33 experimental API usages, the same in
all four IDEs. The reworked tab API and `ShellEnvironmentReader` were already among them before this
change; the new ones are all in `fleet.rpc` (`RemoteApiDescriptor`, `RpcSignature`, `RemoteKind`,
`ParameterDescriptor`), called from the stubs the `rpc` compiler plugin generates for `OpenTabApi`,
not from our own code.

## Risks / Trade-offs

- [RPC from the backend might not reach the client's subscriber] → The first spike checks this in
  the split-mode `runIde` before anything else is built on it. If it fails, stop and revisit this
  design with Derwa instead of patching around it.
- [The frontend module might also load on a remote-development backend, giving it a local opener
  that opens tabs nobody sees] → The spike logs whether the backend process finds an opener; it
  mustn't.
- [`ClientId.current` in the RPC call may not identify the subscribing client] → The split-mode
  spike checks it. If it doesn't, send to every subscriber of the project and note the multi-client
  trade-off.
- [The `rpc` plugin is a pre-release tied to Kotlin's version, from a JetBrains repository] → Pin
  the version the docs pair with the oldest supported IDE; `verifyPlugin` against the newest IDEs
  catches a binary mismatch. Moving Kotlin means moving it too.
- [Forgetting the client copy under remote development gives a silent click] → The README says
  to install on both sides. This is accepted in proposal.md. A Marketplace listing would remove it.
- [Moving sources into subprojects makes a large diff] → Move files unchanged in one task, before
  any behaviour change, and keep `check` green at that point.
- [Experimental split-mode APIs change in a later release] → `verifyPlugin` against the newest
  IDEs on every build. The code that uses them is a few small files.

## Migration Plan

Under remote development: install the new zip on the backend (Plugins (Host)) and in the client
(Plugins), then restart both. Standalone: install as before. Rollback: install the previous zip on
the backend and uninstall the client copy.
