package org.gradle.api.plugins.gaelyk

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.GroovyPlugin

import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME
import static org.gradle.api.plugins.JavaPlugin.JAR_TASK_NAME

class GaelykPluginPlugin extends GaelykPluginBase implements Plugin<Project> {

    static final String GAELYK_PLUGIN_EXTENSION = 'gaelykplugin'

    void apply(Project project) {
        project.plugins.apply(GroovyPlugin)

        project.extensions.create(GAELYK_PLUGIN_EXTENSION, GaelykPluginPluginExtension, project)

        configureGaelykPrecompileGroovlet(project, null)
        configureGaelykPrecompileTemplate(project, null)
    }

    protected void weavePrecompileTaskIntoGraph(Project project, Task precompileTask, pluginConvention) {
        precompileTask.dependsOn(project.tasks.findByName(CLASSES_TASK_NAME))
        project.tasks.findByName(JAR_TASK_NAME).dependsOn(precompileTask)
    }

    protected File getWebAppDir(Project project) {
        project.extensions.findByType(GaelykPluginPluginExtension).webAppDir
    }
}
