package com.github.pastordougdev.dartbarrelfile.services

import com.intellij.openapi.project.Project
import com.github.pastordougdev.dartbarrelfile.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
