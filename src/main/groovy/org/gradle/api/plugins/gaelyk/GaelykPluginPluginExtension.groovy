package org.gradle.api.plugins.gaelyk

import org.gradle.api.Project

class GaelykPluginPluginExtension {
    String webAppDirName = 'src/main/webapp'
    Project project

    GaelykPluginPluginExtension(Project project) {
        this.project = project
    }

    File getWebAppDir() {
        project.file(webAppDirName)
    }
}
