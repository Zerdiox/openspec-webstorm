package dev.derwa.openspec

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.nio.file.Path
import kotlin.io.path.isDirectory

class OpenSpecToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun shouldBeAvailable(project: Project): Boolean =
        project.basePath?.let { Path.of(it, "openspec").isDirectory() } ?: false

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = OpenSpecPanel(project, Path.of(project.basePath!!))
        val content = ContentFactory.getInstance().createContent(panel.component, null, false)
        content.setDisposer(panel)
        toolWindow.contentManager.addContent(content)
    }
}
