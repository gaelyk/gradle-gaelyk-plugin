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
 * Task to precompile groovy classes located in the WEB-INF directory.
 * 
 * NOTE: There is a compileGroovy task in the groovy plugin for gradle. 
 * It might be better to reuse this task in the build.gradle of the template project rather than creating a new task
 *
 */
class GaelykPrecompileGroovyTask extends DefaultTask {
    static final Logger LOGGER = Logging.getLogger(this.class)
	
	// we want to reference the groovy version used to compile the gaelyk project
	def groovyClassPath = project.configurations.groovy.asFileTree.asPath
	def groovySrcDir = 'war/WEB-INF/groovy'
	def classDestDir = 'war/WEB-INF/classes'
	
    @TaskAction
    def precompile(){
		// define ant task for groovyc
		ant.taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc', classpath: groovyClassPath )
		
        LOGGER.info "Precompiling groovy classes..."
		ant.groovyc(srcdir: groovySrcDir, destdir: classDestDir)
		LOGGER.info "Finished precompiling groovy classes."
    }
}