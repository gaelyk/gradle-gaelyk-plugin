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
package org.gradle.api.plugins.gaelyk.template

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Gaelyk creator for create a controller (Groovlet).
 *
 * @author Benjamin Muschko
 */
class GaelykControllerCreator extends GaelykFileCreatorTemplate {
    static final Logger logger = LoggerFactory.getLogger(GaelykControllerCreator.class)

    @Override
    File getBaseDir() {
        def controllerPath = File.separator << "WEB-INF" << File.separator << "groovy"
        new File(getWebAppDir(), controllerPath.toString())
    }

    @Override
    String getFileExtension() {
        ".groovy"
    }

    @Override
    String getTemplate() {
        "/template/controller.template"
    }

    @Override
    Map getBinding(File destFile) {
        ["filename": destFile.name]
    }
}
