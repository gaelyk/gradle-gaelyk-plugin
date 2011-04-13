package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.Project;
import org.gradle.api.plugins.gaelyk.tools.PluginManager;
import org.gradle.api.plugins.gaelyk.tools.TempDir;
import org.gradle.testfixtures.ProjectBuilder;

import spock.lang.Ignore;
import spock.lang.Specification;

class GaelykUninstallPluginTaskSpec extends Specification {

	@Ignore
	def "Test uninstall task"(){
		def dir = TempDir.createNew("install-task")
		Project project = ProjectBuilder.builder().build()
		def args = project.task('args', type: ArgsTask)
		args.map.plugin = plugin
		def install = project.task('install', type: GaelykInstallPluginTask)
		def uninstall = project.task('uninstall', type: GaelykUninstallPluginTask)
		
		install.manager = new PluginManager(dir)
		uninstall.manager = install.manager
		
		when:
		install.install()
		
		then:
		new File(dir, "test").exists()
		
		when:
		uninstall.uninstall()

		then:
		// FIXME does not work since files are deleted on exit
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
