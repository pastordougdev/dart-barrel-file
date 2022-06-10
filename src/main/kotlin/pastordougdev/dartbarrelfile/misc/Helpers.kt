package pastordougdev.dartbarrelfile.misc

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import pastordougdev.dartbarrelfile.dialog.BarrelDialog
import com.intellij.openapi.command.WriteCommandAction
import pastordougdev.dartbarrelfile.misc.BarrelFile

fun getDirName(
    dataContext: DataContext
): String {
    val view = LangDataKeys.IDE_VIEW.getData(dataContext);
    val dir = view?.orChooseDirectory;
    return dir?.name ?: ""
}

fun getAvailableFileNames(
    project: Project,
    dataContext: DataContext
): List<String> {
    val view = LangDataKeys.IDE_VIEW.getData(dataContext);
    val dir = view?.orChooseDirectory;
    val files = dir?.files;
    val dirName = dir?.name ?: ""
    val availableFiles = mutableListOf<String>();
    if(files != null) {
        for(file in files) {
            if(file.name != "$dirName.dart" && !isPartFile(project, file)) {
                availableFiles.add(file.name)
            }
        }
    }
    return availableFiles;
}

fun getAvailableFileNames(
    project: Project,
    dataContext: DataContext,
    existingFileName: String
): List<String> {
    val view = LangDataKeys.IDE_VIEW.getData(dataContext);
    val dir = view?.orChooseDirectory;
    val files = dir?.files;
    val dirName = dir?.name ?: ""
    val availableFiles = mutableListOf<String>();
    if(files != null) {
        for(file in files) {
            if(file.name != existingFileName && !isPartFile(project, file)) {
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
    getAvailableFilesWithSubdirectories(project, dir, availableFiles, "", "$dir.name.dart")
    return availableFiles
}

fun getAvailableFilesTree(
    project: Project,
    dataContext: DataContext,
    existingFileName: String
): MutableList<String> {
    val availableFiles = mutableListOf<String>();
    val view = LangDataKeys.IDE_VIEW.getData(dataContext);
    val dir = view?.orChooseDirectory ?: return availableFiles;
    getAvailableFilesWithSubdirectories(project, dir, availableFiles, "", existingFileName)
    return availableFiles
}

fun getAvailableFilesWithSubdirectories(project: Project, dir: PsiDirectory, availableFiles: MutableList<String>, prefix: String, excludedFileName: String) {
    val files = dir.files;
    val dirName = dir.name;
    if(files != null) {
        for(file in files) {
            //if(file.name != "$prefix$dirName.dart" && file.name.endsWith(".dart") && !isPartFile(project, file)) {
            if(file.name != excludedFileName && file.name.endsWith(".dart") && !isPartFile(project, file)) {
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
        getAvailableFilesWithSubdirectories(project, subDir, availableFiles, p, excludedFileName)
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

fun isDartFile(file: PsiFile) : Boolean {
    return file.name.endsWith(".dart")
}
fun isBarrelFile(project: Project, file: PsiFile) : Boolean {
    val fileContents = PsiDocumentManager.getInstance(project).getDocument(file) ?: return false

    if(fileContents.textLength < BarrelFile.BARREL_FILE_HEADER.length) return false

    val header = fileContents.getText(TextRange(0, BarrelFile.BARREL_FILE_HEADER.length))
    return header == BarrelFile.BARREL_FILE_HEADER
}

fun findExistingBarrelFiles(project: Project, dir: PsiDirectory, barrelFiles: MutableList<PsiFile>) {
    val files = dir.files;
    for(file in files) {
        if(file.name.endsWith(".dart")) {
            val fileContents = PsiDocumentManager.getInstance(project).getDocument(file);
            val header = fileContents?.getText(TextRange(0, BarrelFile.BARREL_FILE_HEADER.length));
            if (header == BarrelFile.BARREL_FILE_HEADER) {
                barrelFiles.add(file)
            }
        }
    }

    if(dir.name == "lib") return
    val nextDir = dir.parentDirectory ?: return
    //if(nextDir.name == "lib") return
    findExistingBarrelFiles(project, nextDir, barrelFiles)
}

fun isInBarrelFile(project: Project, dartFile: PsiFile, barrelFiles: MutableList<PsiFile>) : MutableList<PsiFile> {
    val inBarrelFiles = mutableListOf<PsiFile>()
    for(file in barrelFiles) {
        val fileContents = PsiDocumentManager.getInstance(project).getDocument(file);
        val fileText = fileContents?.text
        if(fileText != null) {
            if(fileText.contains(dartFile.name)) {
                inBarrelFiles.add(file)
            }
        }

    }
    return inBarrelFiles
}

fun addDartFileToBarrelFile(project: Project, fileToAdd: PsiFile, barrelFile: PsiFile) {

    val exportStatement = StringBuilder()
    exportStatement.append("export ")

    val dartFileDir = fileToAdd.containingDirectory
    val barrelFileDir = barrelFile.containingDirectory

    if(PsiManager.getInstance(project).areElementsEquivalent(dartFileDir, barrelFileDir)) {

        exportStatement.append("\'${fileToAdd.name}\'; \n")
    } else {
        val exportPath = StringBuilder()
        buildExportPath(project, dartFileDir, barrelFileDir, exportPath)
        exportPath.insert(0, "\'./")
        exportStatement.append(exportPath.toString())
        exportStatement.append("${fileToAdd.name}\'; \n")

    }

    val barrelFileDoc = PsiDocumentManager.getInstance(project).getDocument(barrelFile);
    if(barrelFileDoc != null) {
        val fileLen = barrelFileDoc.textLength

        WriteCommandAction.runWriteCommandAction(project) {
            barrelFileDoc.insertString(fileLen, exportStatement.toString())
        }
    }

}

fun buildExportPath(project: Project, currentDir: PsiDirectory, targetDir: PsiDirectory, exportPath: StringBuilder) {

    val nextDir = currentDir.parentDirectory
    exportPath.insert(0, "${currentDir.name}/")
    if(!PsiManager.getInstance(project).areElementsEquivalent(nextDir, targetDir)) {
        if (nextDir != null) {
            buildExportPath(project, nextDir, targetDir, exportPath)
        }
    }
}

//fun getProjectDirectory(project: Project) {
//    val virtualProjectDirectory = project.projectFilePath
//    println("Project Directory: $virtualProjectDirectory")
//}