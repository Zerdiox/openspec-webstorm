package dev.derwa.openspec

import com.intellij.openapi.diagnostic.logger

private val LOG = logger<ClaudeLauncher>()

/**
 * Opens a tab with [reworked], or with [classic] when the reworked terminal can't be used here: its
 * classes aren't loaded (a remote-development backend) or it fails to create the tab.
 */
fun openPreferringReworked(reworked: () -> Unit, classic: () -> Unit) {
    try {
        reworked()
    } catch (e: LinkageError) {
        LOG.info("The reworked terminal isn't loaded here; opening a Classic tab: $e")
        classic()
    } catch (e: RuntimeException) {
        LOG.info("Couldn't open a reworked terminal tab; opening a Classic one", e)
        classic()
    }
}
