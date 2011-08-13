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
package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.gaelyk.tools.PluginManager
import org.gradle.api.tasks.TaskAction

/**
 * Task which prints installed Gaelyk plugins.
 *
 * @author Vladimir Orany
 */
class GaelykListPluginsTask extends DefaultTask {
    static final Logger LOGGER = Logging.getLogger(GaelykListPluginsTask.class)
    
    @TaskAction
    def list(){
        LOGGER.info "Listing available Gaelyk plugins."
        LOGGER.info "Downloading from $PluginManager.CATALOGUE_LOCATION."
        def plugins = new XmlSlurper().parseText(new URL(PluginManager.CATALOGUE_LOCATION).text)
        LOGGER.lifecycle "${'Id'.padRight(20)} Version Name"
        LOGGER.lifecycle "-" * 80
        plugins.plugin.each{ plugin ->
            LOGGER.lifecycle "${(plugin.@id.text()).padRight(20)} ${(plugin.version?.text() ?: 'none').padRight(7)} ${(plugin.name.text())}"
        }
    }
}
