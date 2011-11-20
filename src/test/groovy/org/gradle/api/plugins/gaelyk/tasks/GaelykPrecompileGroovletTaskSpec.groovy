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

import org.gradle.api.Project
import org.gradle.api.plugins.gaelyk.tools.TempDir
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import org.gradle.api.plugins.GroovyBasePlugin

class GaelykPrecompileGroovletTaskSpec extends Specification {
    def "Test precompile task"() {
        given:
            Project project = ProjectBuilder.builder().build()
            GroovyBasePlugin groovyBasePlugin = new GroovyBasePlugin()
            groovyBasePlugin.apply(project)
            def configuration = project.configurations.getByName(GroovyBasePlugin.GROOVY_CONFIGURATION_NAME)

            def dir = TempDir.createNew("precompile-task")
            def groovySrcDir = new File(dir, '/groovy')
            groovySrcDir.mkdirs()
            new File(groovySrcDir, 'datetime.groovy').append('new Date().toString()')
            def classDestDir = new File(dir, '/classes')
            classDestDir.mkdirs()

            GaelykPrecompileGroovletTask task = project.task('precompile', type: GaelykPrecompileGroovletTask)
            task.groovyClasspath = configuration.asFileTree
            task.runtimeClasspath = project.files([])
            task.srcDir = groovySrcDir
            task.destDir = classDestDir

        when:
            task.precompile()

        then:
            new File(classDestDir, "datetime.class").exists()
    }
}
