package pastordougdev.dartbarrelfile.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.psi.PsiDocumentManager
import com.intellij.openapi.util.TextRange
import pastordougdev.dartbarrelfile.misc.*
import pastordougdev.dartbarrelfile.dialog.NotBarrelFileDialogDialog

class RefreshBarrelFileWithSubsAction : AnAction() {

    private lateinit var dataContext: DataContext

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        event.presentation.isEnabledAndVisible = false
        //Shows on the ProjectViewPopupMenu only if a file is selected.
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return

        if(!isDartFile(psiFile)) return

        //Do not show action if the file is not a barrel file
        if(!isBarrelFile(project, psiFile)) return

        event.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project;
        this.dataContext = e.dataContext;
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return;
        if(project == null) return

        val barrelFileName = psiFile.name
        val fileContents = PsiDocumentManager.getInstance(project).getDocument(psiFile)
        val header = fileContents?.getText(TextRange(0, BarrelFile.BARREL_FILE_HEADER.length));
        if(header != BarrelFile.BARREL_FILE_HEADER) {
            val notDialog = NotBarrelFileDialogDialog()
            notDialog.show()
            if(!notDialog.isOK) return
        }

        val availableFiles = getAvailableFilesTree(project, this.dataContext, barrelFileName)
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