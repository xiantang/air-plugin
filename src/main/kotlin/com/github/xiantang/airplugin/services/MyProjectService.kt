package com.github.xiantang.airplugin.services

import com.intellij.openapi.project.Project
import com.github.xiantang.airplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
