package pastordougdev.dartbarrelfile.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import pastordougdev.dartbarrelfile.misc.*

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
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        if(project == null) return

        val view = LangDataKeys.IDE_VIEW.getData(this.dataContext);
        val dir = view?.orChooseDirectory ?: return

        //0.4.0 Functionality - if a dart file is found in another generated
        //barrel file, exclude it from the candidates to put in the
        //new barrel file.

        //Create a list of all exported *.dart file names in this project.
        val exportedFiles = filesAlreadyInBarrelFiles(project, dir)
        //This regex matches *.dart the is preceded by a single quote or slah
        val dartRegex = Regex("['|\\/]([\\w_]*\\.dart)")
        val availableFiles = getAvailableFilesTree(project, this.dataContext)

        //Remove any file name that is already exported in another barrel file.
        availableFiles.removeIf {
            val matchResult = dartRegex.find(it)
            if (matchResult == null) {
                return@removeIf false
            } else {
                return@removeIf exportedFiles.contains(matchResult.groups[1]!!.value)
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
        //0.5.0 Functionality - if other barrel files exist up the directory tree, ask
        //the user to go ahead and add this new barrel file to one of those existing
        //higher barrel files.

        //Would you like to add this new file to a higher barrel file?

        val justCreated = dir!!.findFile(barrelFile.barrelFileName)

        val addToHigherBarrelFile = autoAddToHigherBarrelFile(project, justCreated!!)
        if(!addToHigherBarrelFile) return

        addToExistingBarrelFileFlow(project, justCreated!!)

    }
}