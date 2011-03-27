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
 * Gaelyk file creator template.
 *
 * @author Benjamin Muschko
 */
abstract class GaelykFileCreatorTemplate implements GaelykFileCreator {
    static final Logger logger = LoggerFactory.getLogger(GaelykFileCreatorTemplate.class)
    private File webAppDir

    @Override
    void create(String contextDir, String name) {
        File destDir = getBaseDir()

        if(contextDir) {
            contextDir = !contextDir.startsWith(File.separator) ? File.separator.plus(contextDir) : contextDir
            destDir = new File(destDir, contextDir)
        }

        if(!destDir.exists()) {
            destDir.mkdirs()
        }

        File destFile = getFullDestFile(destDir, name)
        String content = createContentFromTemplate(destFile)

        if(!destFile.exists()) {
            boolean success = destFile.createNewFile()

            if(success) {
                destFile.append content
                logger.info "Successfully created file: ${destFile.canonicalPath}"
            }
            else {
                logger.error "Unable to create file: ${destFile.canonicalPath}"
            }
        }
        else {
            logger.warn "The file you want to create already exists: ${destFile.canonicalPath}"
        }
    }

    private File getFullDestFile(File destDir, String name) {
        // Make sure file name start with lower case character
        name = name[0].toLowerCase() + name.substring(1)
        // Build full path and filename
        new File(destDir, name.plus(getFileExtension()))
    }

    private String createContentFromTemplate(File destFile) {
        def templateReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(getTemplate())))
        def engine = new GStringOnlyTemplateEngine()
        def template = engine.createTemplate(templateReader).make(getBinding(destFile))
        template.toString()
    }

    @Override
    public File getWebAppDir() {
        webAppDir
    }

    @Override
    public void setWebAppDir(File webAppDir) {
        this.webAppDir = webAppDir
    }

    abstract String getTemplate()
    abstract Map getBinding(File destFile)
}
