package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.Project
import org.gradle.api.plugins.gaelyk.tools.PluginManager
import org.gradle.api.plugins.gaelyk.tools.TempDir
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GaelykPrecompileGroovyTaskSpec extends Specification {

	def "Test precompile task"() {
		
		
		def groovyClassPath = System.getenv()['GROOVY_HOME'] + '\\lib'
		
		Project project = ProjectBuilder.builder().build()		
		project.configurations.add('groovy', { asFileTree:[asPath:groovyClassPath] })
		
		def dir = TempDir.createNew("precompile-task").toString()
		def groovySrcDir = dir.toString()+'/groovy'
		new File(groovySrcDir).mkdirs()
		new File(groovySrcDir, 'datetime.groovy').append('new Date().toString()')
		def classDestDir = dir.toString()+'/classes'
		new File(classDestDir).mkdirs()
		
		def task = project.task('precompile', type: GaelykPrecompileGroovyTask)
		task.groovyClassPath = groovyClassPath
		task.groovySrcDir = groovySrcDir
		task.classDestDir = classDestDir
		
		
		when:
		task.precompile()
		
		then:
		new File(classDestDir, "datetime.class").exists()
		
	}
}
