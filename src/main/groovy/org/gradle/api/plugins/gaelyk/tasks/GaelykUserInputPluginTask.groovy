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
import org.gradle.api.InvalidUserDataException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.gaelyk.tools.PluginManager

/**
 * Abstract plugin task.
 *
 * @author Benjamin Muschko
 */
class GaelykUserInputPluginTask extends DefaultTask {
    static final Logger LOGGER = Logging.getLogger(GaelykUserInputPluginTask.class)
    PluginManager manager = new PluginManager()
    String plugin

    void validateUserInput() {
        if(!getPlugin()) {
            throw new InvalidUserDataException("Plugin URI was not provided. Please use the -P command line parameter with key 'plugin'.")
        }
        else {
            LOGGER.info "Plugin URI to be installed = ${getPlugin()}"
        }
    }

    public String getPlugin() {
        plugin
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin
    }
}
