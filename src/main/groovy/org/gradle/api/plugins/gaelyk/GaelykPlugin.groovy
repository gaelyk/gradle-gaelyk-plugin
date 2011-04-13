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
import org.gradle.api.Task;
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.WarPluginConvention
import org.gradle.api.plugins.gaelyk.rules.ArgsRule;
import org.gradle.api.plugins.gaelyk.tasks.ArgsTask;
import org.gradle.api.plugins.gaelyk.tasks.GaelykInstallPluginTask;
import org.gradle.api.plugins.gaelyk.tasks.GaelykInstalledPluginsTask;
import org.gradle.api.plugins.gaelyk.tasks.GaelykUninstallPluginTask;
import org.gradle.api.plugins.gaelyk.template.GaelykControllerCreator
import org.gradle.api.plugins.gaelyk.template.GaelykFileCreator
import org.gradle.api.plugins.gaelyk.template.GaelykViewCreator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * <p>A {@link org.gradle.api.Plugin} that provides tasks for managing Gaelyk projects.</p>
 *
 * @author Benjamin Muschko
 */
class GaelykPlugin implements Plugin<Project> {
    static final Logger LOGGER = LoggerFactory.getLogger(GaelykPlugin.class)
    static final String GAELYK_GROUP = "Gaelyk"
    static final String GAELYK_CREATE_CONTROLLER = "gaelykCreateController"
    static final String GAELYK_CREATE_VIEW = "gaelykCreateView"

    @Override
    public void apply(Project project) {
        project.plugins.apply(WarPlugin.class)
        GaelykPluginConvention gaelykPluginConvention = new GaelykPluginConvention()
        project.convention.plugins.gaelyk = gaelykPluginConvention

		project.tasks.addRule new ArgsRule(args: project.task("args", type: ArgsTask), project: project)
		project.task "gaelykInstall", type: GaelykInstallPluginTask
		project.task "gaelykUninstall", type: GaelykUninstallPluginTask
		project.task "gaelykInstalled", type: GaelykInstalledPluginsTask
		
        configureGaelykCreateControllerTask(project)
        configureGaelykCreateViewTask(project)
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

    private WarPluginConvention getWarConvention(Project project) {
        project.convention.getPlugin(WarPluginConvention.class)
    }
}
