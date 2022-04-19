package pastordougdev.dartbarrelfile.misc

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import pastordougdev.dartbarrelfile.dialog.BarrelDialog
import pastordougdev.dartbarrelfile.misc.BarrelFile

fun getDirName(
    dataContext: DataContext
): String {
    val view = LangDataKeys.IDE_VIEW.getData(dataContext);
    val dir = view?.orChooseDirectory;
    return dir?.name ?: ""
}

fun getAvailableFileNames(
    dataContext: DataContext
): List<String> {
    val view = LangDataKeys.IDE_VIEW.getData(dataContext);
    val dir = view?.orChooseDirectory;
    val files = dir?.files;
    val dirName = dir?.name ?: ""
    val availableFiles = mutableListOf<String>();
    if(files != null) {
        for(file in files) {
            if(file.name != "$dirName.dart") {
                availableFiles.add(file.name)
            }
        }
    }
    return availableFiles;
}

fun getAvailableFilesTree(
    project: Project,
    dataContext: DataContext
): MutableList<String> {
    val availableFiles = mutableListOf<String>();
    val view = LangDataKeys.IDE_VIEW.getData(dataContext);
    val dir = view?.orChooseDirectory ?: return availableFiles;
    getAvailableFilesWithSubdirectories(project, dir, availableFiles, "")
    return availableFiles
}

fun getAvailableFilesWithSubdirectories(project: Project, dir: PsiDirectory, availableFiles: MutableList<String>, prefix: String) {
    val files = dir.files;
    val dirName = dir.name;
    if(files != null) {
        for(file in files) {
            if(file.name != "$dirName.dart" && file.name.endsWith(".dart") && !isPartFile(project, file)) {
                availableFiles.add("$prefix${file.name}")
            }
        }
    }
    val subDirs = dir.subdirectories
    for(subDir in subDirs) {
        var p = ""
        p = if(prefix.isEmpty()) {
            "./${subDir.name}/"
        } else {
            "$prefix${subDir.name}/"
        }
        getAvailableFilesWithSubdirectories(project, subDir, availableFiles, p)
    }
}

fun buildBarrelFileWithDialog(
    project: Project,
    dirName: String,
    availableFiles: List<String>
): BarrelFile? {
    val dialog = BarrelDialog(project, dirName, availableFiles)

    dialog.show()

    if (!dialog.isOK) return null

    return BarrelFile(dirName, dialog.getBarrelFileName(), dialog.getSelectedFiles());
}

fun createBarrelFile(project: Project, dir: PsiDirectory, barrelFile: BarrelFile) {
    val existingFile = dir.findFile(barrelFile.barrelFileName)
    val barrelContents = barrelFile.generateFileContents()
    val fileType = FileTypeManager.getInstance().getFileTypeByFileName(barrelFile.barrelFileName)
    if(existingFile != null) {
        val document = PsiDocumentManager.getInstance(project).getDocument(existingFile)
        val docLength = document?.textLength ?: 1
        document?.replaceString(0, docLength - 1, barrelContents)
        return
    }
    val newFile = PsiFileFactory.getInstance(project)
        .createFileFromText(barrelFile.barrelFileName, fileType, barrelContents);
    dir.add(newFile)
}

fun isPartFile(project: Project, file: PsiFile) : Boolean {
    val fileContents = PsiDocumentManager.getInstance(project).getDocument(file) ?: return false
    return fileContents.text.contains("part of")
}