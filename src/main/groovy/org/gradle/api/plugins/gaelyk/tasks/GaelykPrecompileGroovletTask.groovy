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
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Task for precompiling Groovlets located in the WEB-INF directory.
 *
 * @author Greg Butt, Benjamin Muschko
 */
class GaelykPrecompileGroovletTask extends DefaultTask {
    static final Logger LOGGER = Logging.getLogger(GaelykPrecompileGroovletTask)

    @InputFiles FileCollection groovyClasspath
    @InputFiles FileCollection runtimeClasspath
    @InputDirectory File srcDir
    @OutputDirectory File destDir

    @TaskAction
    def precompile(){
        ant.taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc', classpath: getGroovyClasspath().asPath)

        LOGGER.info 'Precompiling groovy classes...'
        ant.groovyc(srcdir: getSrcDir(), destdir: getDestDir(), classpath: getRuntimeClasspath().asPath)
        LOGGER.info 'Finished precompiling groovy classes.'
    }
}