/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.plugins.gaelyk

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.WarPluginConvention
import org.gradle.api.plugins.gaelyk.template.GaelykControllerCreator
import org.gradle.api.plugins.gaelyk.template.GaelykFileCreator
import org.gradle.api.plugins.gaelyk.template.GaelykViewCreator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.gradle.api.plugins.gaelyk.tasks.*

/**
 * <p>A {@link org.gradle.api.Plugin} that provides tasks for managing Gaelyk projects.</p>
 *
 * @author Benjamin Muschko
 */
class GaelykPlugin implements Plugin<Project> {
    static final Logger LOGGER = LoggerFactory.getLogger(GaelykPlugin)
    static final String GAELYK_GROUP = "Gaelyk"
    static final String GAELYK_INSTALL_PLUGIN = "gaelykInstallPlugin"
    static final String GAELYK_UNINSTALL_PLUGIN = "gaelykUninstallPlugin"
    static final String GAELYK_LIST_INSTALLED_PLUGINS = "gaelykListInstalledPlugins"
    static final String GAELYK_LIST_PLUGINS = "gaelykListPlugins"
    static final String GAELYK_CREATE_CONTROLLER = "gaelykCreateController"
    static final String GAELYK_CREATE_VIEW = "gaelykCreateView"
    static final String GAELYK_PRECOMPILE_GROOVLET = "gaelykPrecompileGroovlet"

    @Override
    public void apply(Project project) {
        project.plugins.apply(WarPlugin.class)
        GaelykPluginConvention gaelykPluginConvention = new GaelykPluginConvention()
        project.convention.plugins.gaelyk = gaelykPluginConvention

        configureGaelykInstallPluginTask(project)
        configureGaelykUninstallPluginTask(project)
        configureGaelykListInstalledPluginsTask(project)
        configureGaelykListPluginsTask(project)
        configureGaelykCreateControllerTask(project)
        configureGaelykCreateViewTask(project)
        configureGaelykPrecompileGroovlet(project)
    }

    private void configureGaelykInstallPluginTask(final Project project) {
        project.tasks.withType(GaelykInstallPluginTask.class).whenTaskAdded { GaelykInstallPluginTask gaelykInstallPluginTask ->
            gaelykInstallPluginTask.conventionMapping.map("plugin") { getPluginProperty(project) }
        }

        GaelykInstallPluginTask gaelykInstallPluginTask = project.tasks.add(GAELYK_INSTALL_PLUGIN, GaelykInstallPluginTask.class)
        gaelykInstallPluginTask.description = "Installs Gaelyk plugin."
        gaelykInstallPluginTask.group = GAELYK_GROUP
    }

    private void configureGaelykUninstallPluginTask(final Project project) {
        project.tasks.withType(GaelykUninstallPluginTask.class).whenTaskAdded { GaelykUninstallPluginTask gaelykUninstallPluginTask ->
            gaelykUninstallPluginTask.conventionMapping.map("plugin") { getPluginProperty(project) }
        }

        GaelykUninstallPluginTask gaelykUninstallPluginTask = project.tasks.add(GAELYK_UNINSTALL_PLUGIN, GaelykUninstallPluginTask)
        gaelykUninstallPluginTask.description = "Uninstalls Gaelyk plugin."
        gaelykUninstallPluginTask.group = GAELYK_GROUP
    }

    private void configureGaelykListInstalledPluginsTask(final Project project) {
        GaelykListInstalledPluginsTask gaelykListInstalledPluginsTask = project.tasks.add(GAELYK_LIST_INSTALLED_PLUGINS, GaelykListInstalledPluginsTask)
        gaelykListInstalledPluginsTask.description = "Lists installed Gaelyk plugins."
        gaelykListInstalledPluginsTask.group = GAELYK_GROUP
    }

    private void configureGaelykListPluginsTask(final Project project) {
        GaelykListPluginsTask gaelykListPluginsTask = project.tasks.add(GAELYK_LIST_PLUGINS, GaelykListPluginsTask)
        gaelykListPluginsTask.description = "Lists available Gaelyk plugins from catalogue."
        gaelykListPluginsTask.group = GAELYK_GROUP
    }

    private void configureGaelykPrecompileGroovlet(final Project project) {
        project.tasks.withType(GaelykPrecompileGroovletTask).whenTaskAdded { GaelykPrecompileGroovletTask gaelykPrecompileGroovletTask ->
            gaelykPrecompileGroovletTask.conventionMapping.map("groovyClasspath") { project.configurations.groovy.asFileTree }
            gaelykPrecompileGroovletTask.conventionMapping.map("runtimeClasspath") { createRuntimeClasspath(project) }
            gaelykPrecompileGroovletTask.conventionMapping.map("srcDir") { new File(getWarConvention(project).webAppDir, 'WEB-INF/groovy') }
            gaelykPrecompileGroovletTask.conventionMapping.map("destDir") { new File(getWarConvention(project).webAppDir, 'WEB-INF/classes') }
        }

        def gaelykPrecompileGroovletTask = project.tasks.add(GAELYK_PRECOMPILE_GROOVLET, GaelykPrecompileGroovletTask)
        gaelykPrecompileGroovletTask.description = "Precompiles Groovlets."
        gaelykPrecompileGroovletTask.group = GAELYK_GROUP
    }

    private void configureGaelykCreateControllerTask(final Project project) {
        project.tasks.addRule("Pattern: $GAELYK_CREATE_CONTROLLER<ControllerName>: Creates a Gaelyk controller (Groovlet).") { String taskName ->
            createGaelykFile(project, taskName, GAELYK_CREATE_CONTROLLER, new GaelykControllerCreator())
        }
    }

    private void configureGaelykCreateViewTask(final Project project) {
        project.tasks.addRule("Pattern: $GAELYK_CREATE_VIEW<ViewName>: Creates a Gaelyk view (Groovy template).") { String taskName ->
            createGaelykFile(project, taskName, GAELYK_CREATE_VIEW, new GaelykViewCreator())
        }
    }

    private void createGaelykFile(final Project project, final String taskName, final String taskBaseName, final GaelykFileCreator gaelykFileCreator) {
        if(taskName.startsWith(taskBaseName) && taskName.length() > taskBaseName.length()) {
            project.task(taskName) << {
                String viewName = (taskName - taskBaseName)
                String viewDir = getDirProperty(project)
                gaelykFileCreator.setWebAppDir(getWarConvention(project).webAppDir)
                gaelykFileCreator.create(viewDir, viewName)
            }
        }
    }

    private String getDirProperty(final Project project) {
        project.hasProperty("dir") ? project.property("dir") : null
    }

    private String getPluginProperty(final Project project) {
        project.hasProperty("plugin") ? project.property("plugin") : null
    }

    private WarPluginConvention getWarConvention(Project project) {
        project.convention.getPlugin(WarPluginConvention)
    }

    /**
     * Creates classpath from classes directory and runtime classpath.
     *
     * @return Classpath
     */
    private FileCollection createRuntimeClasspath(Project project) {
        FileCollection runtimeClasspath = project.files(project.sourceSets.main.output.classesDir)
        runtimeClasspath += project.files { new File(getWarConvention(project).webAppDir, 'WEB-INF/lib').listFiles().findAll { it.name.endsWith('.jar') } }
        runtimeClasspath
    }
}
