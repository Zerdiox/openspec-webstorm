package dev.derwa.openspec

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

/**
 * Opens a requested tab in this IDE. Only the frontend module provides one, so a standalone IDE has
 * one and a remote-development backend doesn't: there the request goes to the client instead.
 */
interface LocalTabOpener {
    fun open(project: Project, request: OpenTabRequest)

    companion object {
        val EP_NAME = ExtensionPointName<LocalTabOpener>("dev.derwa.openspec.localTabOpener")
    }
}
