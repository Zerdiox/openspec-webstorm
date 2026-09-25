package dev.derwa.openspec

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/** Asks for the short description that Explore and Propose start from. */
class DescriptionDialog(project: Project, action: Action) : DialogWrapper(project) {
    private val text = JBTextArea(6, 50).apply { lineWrap = true; wrapStyleWord = true }

    val description: String get() = text.text

    init {
        title = action.label.replaceFirstChar { it.uppercase() }
        init()
    }

    override fun createCenterPanel(): JComponent = JPanel(BorderLayout(0, JBUI.scale(6))).apply {
        add(JBLabel("Describe it in a few words:"), BorderLayout.NORTH)
        add(JBScrollPane(text), BorderLayout.CENTER)
    }

    override fun getPreferredFocusedComponent(): JComponent = text
}
