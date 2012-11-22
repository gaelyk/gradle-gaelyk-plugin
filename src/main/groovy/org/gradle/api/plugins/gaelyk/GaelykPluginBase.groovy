package org.gradle.api.plugins.gaelyk

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection

import org.gradle.api.plugins.gaelyk.tasks.GaelykPrecompileGroovletTask
import org.gradle.api.plugins.gaelyk.tasks.GaelykPrecompileTemplateTask

abstract class GaelykPluginBase {
    static final String GAELYK_GROUP = "Gaelyk"

    static final String GAELYK_PRECOMPILE_GROOVLET = "gaelykPrecompileGroovlet"
    static final String GAELYK_PRECOMPILE_TEMPLATE = "gaelykPrecompileTemplate"
    static final String GROOVLET_DIRECTORY_RELATIVE_PATH = 'WEB-INF/groovy'

    protected void configureGaelykPrecompileGroovlet(Project project, pluginConvention) {
        project.tasks.withType(GaelykPrecompileGroovletTask).whenTaskAdded { GaelykPrecompileGroovletTask gaelykPrecompileGroovletTask ->
            gaelykPrecompileGroovletTask.conventionMapping.map("groovyClasspath") { project.configurations.groovy.asFileTree }
            gaelykPrecompileGroovletTask.conventionMapping.map("runtimeClasspath") { createRuntimeClasspath(project) }
            gaelykPrecompileGroovletTask.conventionMapping.map("srcDir") { new File(getWebAppDir(project), GROOVLET_DIRECTORY_RELATIVE_PATH) }
            gaelykPrecompileGroovletTask.conventionMapping.map("destDir") { project.sourceSets.main.output.classesDir }
        }

        def gaelykPrecompileGroovletTask = project.tasks.add(GAELYK_PRECOMPILE_GROOVLET, GaelykPrecompileGroovletTask)
        gaelykPrecompileGroovletTask.description = "Precompiles Groovlets."
        gaelykPrecompileGroovletTask.group = GAELYK_GROUP

        weavePrecompileTaskIntoGraph(project, gaelykPrecompileGroovletTask, pluginConvention)
    }

    protected void configureGaelykPrecompileTemplate(Project project, pluginConvention) {
        project.tasks.withType(GaelykPrecompileTemplateTask).whenTaskAdded { GaelykPrecompileTemplateTask gaelykPrecompileTemplateTask ->
            gaelykPrecompileTemplateTask.conventionMapping.map("groovyClasspath") { project.configurations.groovy.asFileTree }
            gaelykPrecompileTemplateTask.conventionMapping.map("runtimeClasspath") { createRuntimeClasspath(project) }
            gaelykPrecompileTemplateTask.conventionMapping.map("srcDir") { getWebAppDir(project) }
            gaelykPrecompileTemplateTask.conventionMapping.map("destDir") { project.sourceSets.main.output.classesDir }
        }

        def gaelykPrecompileTemplateTask = project.tasks.add(GAELYK_PRECOMPILE_TEMPLATE, GaelykPrecompileTemplateTask)
        gaelykPrecompileTemplateTask.description = "Precompiles Templates."
        gaelykPrecompileTemplateTask.group = GAELYK_GROUP

        weavePrecompileTaskIntoGraph(project, gaelykPrecompileTemplateTask, pluginConvention)
    }

    abstract protected void weavePrecompileTaskIntoGraph(Project project, Task precompileTask, pluginConvention)

    abstract protected File getWebAppDir(Project project)

    /**
     * Creates classpath from classes directory and runtime classpath.
     *
     * @return Classpath
     */
    protected FileCollection createRuntimeClasspath(Project project) {
        FileCollection runtimeClasspath = project.files(project.sourceSets.main.output.classesDir)
        runtimeClasspath += project.configurations.runtime
        runtimeClasspath
    }
}
