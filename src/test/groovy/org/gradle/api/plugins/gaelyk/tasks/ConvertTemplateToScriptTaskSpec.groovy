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
import org.gradle.api.file.FileCollection

class ConvertTemplateToScriptTaskSpec extends Specification {
    def "Test precompile task"() {
        given:
            Project project = ProjectBuilder.builder().build()
            GroovyBasePlugin groovyBasePlugin = new GroovyBasePlugin()
            groovyBasePlugin.apply(project)
            def configuration = project.configurations.getByName(GroovyBasePlugin.GROOVY_CONFIGURATION_NAME)

            def dir = TempDir.createNew("precompile-task")
            def templatesSrcDir = new File(dir, '/templates')
            templatesSrcDir.mkdirs()
            new File(templatesSrcDir, 'datetime.gtpl').append('<html><body>Hello $world</body></html>')
            def packageDir = new File(templatesSrcDir, '/pkg')
            packageDir.mkdirs()
            new File(packageDir, 'datetime.gtpl').append('<html><body>Hello $world</body></html>')
            
            def classDestDir = new File(dir, '/classes')
            classDestDir.mkdirs()

            ConvertTemplatesToScriptsTask task = createTask(project)
            task.classpath = configuration.asFileTree
            task.source = project.fileTree templatesSrcDir
            task.destinationDir = classDestDir

        when:
            task.compile()

        then:
            new File(task.destinationDir, '$gtpl$datetime.groovy').exists()
            new File(task.destinationDir, 'pkg/$gtpl$datetime.groovy').exists()
    }
    
    def "Test fail precompilation"() {
        given:
            Project project = ProjectBuilder.builder().build()
            GroovyBasePlugin groovyBasePlugin = new GroovyBasePlugin()
            groovyBasePlugin.apply(project)
            def configuration = project.configurations.getByName(GroovyBasePlugin.GROOVY_CONFIGURATION_NAME)

            def dir = TempDir.createNew("precompile-task")
            def templatesSrcDir = new File(dir, '/templates')
            templatesSrcDir.mkdirs()
            def datetimeTemplate = new File(templatesSrcDir, 'datetime.gtpl')
            datetimeTemplate.append('''


            <html><body>Hello $world</body></html>
            This will fail: ${ hello(world }



''')
            def packageDir = new File(templatesSrcDir, '/pkg')
            
            def classDestDir = new File(dir, '/classes')
            classDestDir.mkdirs()

            ConvertTemplatesToScriptsTask task = createTask(project)
            task.classpath = configuration.asFileTree
            task.source = project.fileTree templatesSrcDir
            task.destinationDir = classDestDir

        when:
            task.compile()

        then:
            def e = thrown(GroovyRuntimeException)
            e.message.contains(datetimeTemplate.absolutePath)
    }
    
    
    
    def "Get script name"() {
        given:
            Project project = ProjectBuilder.builder().build()
            ConvertTemplatesToScriptsTask task = createTask(project)
            def info =  task.getTemplateScriptInfo('/xyz/template.gtpl')
        expect:
            info.dir == 'xyz'
            info.file == '$gtpl$template.groovy'
    }
    
    def "Dir to pkg"() {
        given:
            Project project = ProjectBuilder.builder().build()
            ConvertTemplatesToScriptsTask task = createTask(project)
        expect:
            task.dirToPackage('') == ''
            task.dirToPackage('pkg') == 'pkg'
            task.dirToPackage('pkg\\next\\other') == 'pkg.next.other'
            task.dirToPackage('pkg/next/other') == 'pkg.next.other'
            task.dirToPackage('WEB-INF/something') == 'web_inf.something'
            task.dirToPackage('I"m extra obscure') == 'i_m_extra_obscure'
        
    }

    private ConvertTemplatesToScriptsTask createTask(Project project) {
        project.task('convert', type: ConvertTemplatesToScriptsTask)
    }
}
