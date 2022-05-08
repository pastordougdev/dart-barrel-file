package pastordougdev.dartbarrelfile.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import pastordougdev.dartbarrelfile.misc.buildBarrelFileWithDialog
import pastordougdev.dartbarrelfile.misc.createBarrelFile
import pastordougdev.dartbarrelfile.misc.getAvailableFilesTree
import pastordougdev.dartbarrelfile.misc.getDirName

class NewBarrelFileWithSubsAction : AnAction() {

    private lateinit var dataContext: DataContext

    override fun update(event: AnActionEvent) {
        //Shows on the New group only if a directory is selected.
        val psiFile = event.getData(CommonDataKeys.PSI_FILE);
        event.presentation.isEnabledAndVisible = psiFile == null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project;
        this.dataContext = e.dataContext;
        val psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if(project == null) return

        val availableFiles = getAvailableFilesTree(project, this.dataContext)
        val dirName = getDirName(this.dataContext)

        val barrelFile = buildBarrelFileWithDialog(project, dirName, availableFiles)

        if(barrelFile == null) return;

        val view = LangDataKeys.IDE_VIEW.getData(this.dataContext);
        val dir = view?.orChooseDirectory;
        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(
                project,
                {
                    createBarrelFile(project, dir!!, barrelFile)
                },
                "Generate Barrel File",
                null
            )
        }

    }
}