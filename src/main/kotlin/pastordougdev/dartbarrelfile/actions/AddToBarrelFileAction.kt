package pastordougdev.dartbarrelfile.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import pastordougdev.dartbarrelfile.dialog.AlreadyInBarrelFileDialog
import pastordougdev.dartbarrelfile.dialog.NoBarrelFileFoundDialog
import pastordougdev.dartbarrelfile.dialog.SelectBarrelFileDialog
import pastordougdev.dartbarrelfile.misc.*

class AddToBarrelFileAction : AnAction() {

    private lateinit var dataContext: DataContext

    override fun update(event: AnActionEvent) {

        event.presentation.isEnabledAndVisible = false

        print("Trying to show Add to Action")
        val psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if(psiFile == null) {
            return;
        }
        val n = psiFile.name
        print(" -- filename is $n")
        if(!isDartFile(psiFile)) {
            return
        }

        val project = event.project ?: return
        if(isPartFile(project, psiFile)) return

        event.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return;
        this.dataContext = e.dataContext;
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return;


        val barrelFiles = mutableListOf<PsiFile>()


        val dartFileName = psiFile.name;
        val dir = psiFile.containingDirectory;

        findExistingBarrelFiles(project, dir, barrelFiles)

        //Were any barrel files found?
        if(barrelFiles.isEmpty()) {
            val emptyDialog = NoBarrelFileFoundDialog()
            emptyDialog.show()
            return
        }

        val inExistingBarrelFiles = isInBarrelFile(project, psiFile, barrelFiles)

        //If this file is already in other barrel files, show a dialog
        var doContinue = true;
        if(inExistingBarrelFiles.isNotEmpty()) {
            val existingDialog = AlreadyInBarrelFileDialog(psiFile, inExistingBarrelFiles)
            existingDialog.show()
            doContinue = existingDialog.isOK
        }

        //exit if user chose to cancel.
        if(!doContinue) return

        val dialog = SelectBarrelFileDialog(project, barrelFiles)
        dialog.show()

        //did the user hit cancel on the file picker dialog?
        if(!dialog.isOK) return

        var selectedBarrelFiles = dialog.getSelectedFiles()
        if(selectedBarrelFiles.isEmpty()) return

        val selectedBarrelFile = selectedBarrelFiles[0]

        //if this dart file is already in the selected barrel file, do nothing
        if(inExistingBarrelFiles.contains(selectedBarrelFile)) {
            return
        }

        var barrelFileDocument = PsiDocumentManager.getInstance(project).getDocument(selectedBarrelFile)

        addDartFileToBarrelFile(project, psiFile, selectedBarrelFile)




    }
}