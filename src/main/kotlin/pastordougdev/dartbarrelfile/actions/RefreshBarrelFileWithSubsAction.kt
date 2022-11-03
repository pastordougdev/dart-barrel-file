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

        val view = LangDataKeys.IDE_VIEW.getData(this.dataContext);
        val dir = view?.orChooseDirectory ?: return

        //0.4.0 Functionality - if a dart file is found in another generated
        //barrel file, exclude it from the candidates to put in the
        //new barrel file.  UNLESS it is a file already being exported by this
        //barrel file.  In this case, it needs to be listed otherwise it will be
        //wiped out on the refresh.

        //Create a list of all exported *.dart file names in this project.
        val exportedFiles = filesAlreadyInBarrelFiles(project, dir)

        //Create a list of previously exported files IN THIS BARREL FILE

        val exportedByThisBarrelFile = getExportedDartFileNames(project, psiFile)
        println("In this barrel file: $exportedByThisBarrelFile")

        //This regex matches *.dart the is preceded by a single quote or slah
        val dartRegex = Regex("['|\\/]([\\w_]*\\.dart)")

        val availableFiles = getAvailableFilesTree(project, this.dataContext, barrelFileName)
        //Remove any file name that is already exported in another barrel file.
        availableFiles.removeIf {
            val matchResult = dartRegex.find(it)
            if (matchResult == null) {
                return@removeIf false
            } else {
                return@removeIf exportedFiles.contains(matchResult.groups[1]!!.value) &&
                        !exportedByThisBarrelFile.contains(matchResult.groups[1]!!.value)
            }
        }

        val dirName = getDirName(this.dataContext)

        val barrelFile = buildBarrelFileWithDialog(project, dirName, availableFiles)

        if(barrelFile == null) return;


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