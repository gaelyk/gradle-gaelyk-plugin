package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.Project;
import org.gradle.api.plugins.gaelyk.tools.PluginManager;
import org.gradle.api.plugins.gaelyk.tools.TempDir;
import org.gradle.testfixtures.ProjectBuilder;

import spock.lang.Specification;

class GaelykUninstallPluginTaskSpec extends Specification {

	def "Test uninstall task"(){
		def dir = TempDir.createNew("install-task")
		Project project = ProjectBuilder.builder().build()
		def args = project.task('args', type: ArgsTask)
		args.map.plugin = plugin
		def install = project.task('install', type: GaelykInstallPluginTask)
		def uninstall = project.task('uninstall', type: GaelykUninstallPluginTask)
		
		install.manager = new PluginManager(dir)
		
		when:
		install.install()
		
		then:
		new File(dir, "test").exists()
		
		when:
		uninstall.uninstall()

		then:
		!new File(dir, "test").exists()
		
		
		cleanup:
		new AntBuilder().delete dir:dir.path
		
		where:
		plugin << [
			"src/test/resources/archives/project-with-git.zip", 
			"http://klient.appsatori.eu/github/gradle-gaelyk-plugin/project-with-git.zip"
		]
	}
	
}
