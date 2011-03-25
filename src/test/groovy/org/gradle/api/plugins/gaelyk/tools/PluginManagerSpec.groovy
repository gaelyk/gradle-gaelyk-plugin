package org.gradle.api.plugins.gaelyk.tools

import spock.lang.Specification


class PluginManagerSpec extends Specification {

	def "Install from archive"(){
		setup:
		def dest = TempDir.createNew("install-from-archive")
		
		when:
		new PluginManager(dest).install archive: "src/test/resources/archives/project-with-git.zip"
		
		then:
		new File(dest, file).exists() == exists
		
		cleanup:
			new AntBuilder().delete dir:dest.path 

		where:
		file           | exists
		'test'         | true
		'.test'        | true
		'.git'         | false
		'README.TXT'   | false
		'build.groovy' | false
		'gaelyk.plugin'| false
		
	}
	
	def "Installation is recorded into history file"(){
		setup:
		def dest = TempDir.createNew("install-from-archive")
		
		when:
		new PluginManager(dest).install archive: "src/test/resources/archives/project-with-git.zip"
		def history = new XmlSlurper().parse(new File(dest, ".gaelykhistory"))
		then:
		history.plugin.size() == 1
		history.plugin.find{it.@origin.text().endsWith("src/test/resources/archives/project-with-git.zip")}
		history.plugin.find{it.@origin.text().endsWith("src/test/resources/archives/project-with-git.zip")}.file.size() == 2
		
		cleanup:
		new AntBuilder().delete dir:dest.path
	}
	
	def "Read plugin descriptor"(){
		setup:
		def props = new PluginManager(new File(".")).readPluginDescriptor("src/test/resources")
		
		expect:
		props[prop] == value
		
		where:
		prop       | value
		'excludes' | ["README.TXT"]
		'includes' | [".test"]
		'version'  | "2.0"
	}
	
	def "Don't uninstall files somebody other installed"(){
		setup:
		def manager = new PluginManager("src/test/resources")
		when:
		def tbd = manager.filesToDelete("git://example.com/plugin1.git")
		then:
		tbd
		tbd.size() == 1
		tbd[0] == "test"
	}
}
