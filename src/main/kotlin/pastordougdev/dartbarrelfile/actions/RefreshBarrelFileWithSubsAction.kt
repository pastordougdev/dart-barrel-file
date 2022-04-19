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
        //Shows on the ProjectViewPopupMenu only if a file is selected.
        val psiFile = event.getData(CommonDataKeys.PSI_FILE);
        event.presentation.isEnabledAndVisible = psiFile != null

        //TODO: Maybe read the file for the generated header?  Is this too much overhead for this method?
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project;
        this.dataContext = e.dataContext;
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return;
        if(project == null) return

        val fileContents = PsiDocumentManager.getInstance(project).getDocument(psiFile)
        val header = fileContents?.getText(TextRange(0, BarrelFile.BARREL_FILE_HEADER.length));
        if(header != BarrelFile.BARREL_FILE_HEADER) {
            println("Not Barrel File")
            println(header)
            val notDialog = NotBarrelFileDialogDialog()
            notDialog.show()
            if(!notDialog.isOK) return
        }

        val availableFiles = getAvailableFilesTree(project, this.dataContext)
        val dirName = getDirName(this.dataContext)

        val barrelFile = buildBarrelFileWithDialog(project, dirName, availableFiles)

        println(barrelFile?.generateFileContents())

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