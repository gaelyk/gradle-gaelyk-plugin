package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.Project
import org.gradle.api.plugins.gaelyk.tools.PluginManager
import org.gradle.api.plugins.gaelyk.tools.TempDir
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GaelykInstallPluginTaskSpec extends Specification {

	def "Test install task"() {
		def dir = TempDir.createNew("install-task")
		Project project = ProjectBuilder.builder().build()
		def install = project.task('install', type: GaelykInstallPluginTask)
		install.manager = new PluginManager(dir)
        install.plugin = "src/test/resources/archives/project-with-git.zip"
		
		when:
		install.install()
		
		then:
		new File(dir, "test").exists()
		cleanup:
		new AntBuilder().delete dir:dir.path
		
		where:
		plugin << [
			"src/test/resources/archives/project-with-git.zip", 
			"http://klient.appsatori.eu/github/gradle-gaelyk-plugin/project-with-git.zip"
		]
	}
}
