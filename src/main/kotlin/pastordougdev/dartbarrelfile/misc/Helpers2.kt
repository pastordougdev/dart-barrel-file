package pastordougdev.dartbarrelfile.misc

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

fun filesAlreadyInBarrelFiles(project: Project, dir: PsiDirectory): MutableList<String> {
    val exportedFileNames = mutableListOf<String>()
    val dartRegex = Regex("['|\\/]([\\w_]*\\.dart)")
    val libDir = getLibDirectory(project, dir) ?: return mutableListOf<String>()
    val barrelFiles = getAllBarrelFiles(project, libDir)
    for(bf in barrelFiles) {
        val document = PsiDocumentManager.getInstance(project).getDocument(bf)
        var lines = document?.lineCount ?: 0
        if(lines > 0) {
            lines -= 1
            for(i in 0..lines) {
                val start = document?.getLineStartOffset(i) ?: 0
                val end = document?.getLineEndOffset(i) ?: 0
                val lineText = document?.getText(TextRange(start, end)) ?: ""
                val matchResult = dartRegex.find(lineText)
                if (matchResult != null) {
                    exportedFileNames.add(matchResult.groups[1]!!.value)
                }
            }
        }
    }
    return exportedFileNames

}

fun getLibDirectory(project: Project, dir: PsiDirectory) : PsiDirectory? {
    println("Looking for lib director")
    if(dir.name == "lib") return dir
    var found = false
    var currDir = dir
    var dirFound: PsiDirectory? = null
    while(!found) {
        val nextDirUp = currDir.parentDirectory ?: break
        if(nextDirUp.name == "lib") {
            dirFound = nextDirUp
            found = true
        } else {
            currDir = nextDirUp
        }
    }

    return dirFound
}

fun getAllBarrelFiles(project: Project, dir: PsiDirectory) : MutableList<PsiFile> {
    val barrelFiles = mutableListOf<PsiFile>()
    getBarrelFileInDirectory(project, dir, barrelFiles)
    return barrelFiles

}

fun getBarrelFileInDirectory(project: Project, dir: PsiDirectory, barrelFiles: MutableList<PsiFile>) {
    val files = dir.files
    for(file in files) {
        if(isDartFile(file) && isBarrelFile(project, file)) {
            barrelFiles.add(file)
        }
    }
    val dirs = dir.subdirectories
    for(subDir in dirs) {
        getBarrelFileInDirectory(project, subDir, barrelFiles)
    }
}

fun getExportedDartFileNames(project: Project, barrelFile: PsiFile): MutableList<String> {
    val exportedFileNames = mutableListOf<String>()
    val dartFullPathRegex = Regex("export\\s(\\'\\w*.dart\\')")
    val dartRegex = Regex("['|\\/]([\\w_]*\\.dart)")
    val document = PsiDocumentManager.getInstance(project).getDocument(barrelFile)
    var lines = document?.lineCount ?: 0
    if(lines > 0) {
        lines -= 1
        for(i in 0..lines) {
            val start = document?.getLineStartOffset(i) ?: 0
            val end = document?.getLineEndOffset(i) ?: 0
            val lineText = document?.getText(TextRange(start, end)) ?: ""
            val matchResult = dartRegex.find(lineText)
            if (matchResult != null) {
                exportedFileNames.add(matchResult.groups[1]!!.value)
            }
        }
    }
    return exportedFileNames
}