package dev.derwa.openspec

import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiManager
import java.nio.file.Path

/** Opens a file in the editor, at its 1-based [line] when one is given, or selects a folder in the project tree. */
internal class PathNavigatable(private val project: Project, private val path: Path, private val line: Int? = null) : Navigatable {
    override fun navigate(requestFocus: Boolean) {
        // A file Claude Code wrote outside the IDE may not be in the VFS yet.
        val file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path) ?: return
        if (file.isDirectory) {
            ReadAction.computeBlocking<_, RuntimeException> { PsiManager.getInstance(project).findDirectory(file) }
                ?.let { PsiNavigationSupport.getInstance().navigateToDirectory(it, requestFocus) }
        } else {
            val descriptor = if (line != null) OpenFileDescriptor(project, file, line - 1, 0) else OpenFileDescriptor(project, file)
            descriptor.navigate(requestFocus)
        }
    }

    override fun canNavigate() = true
    override fun canNavigateToSource() = true
}
