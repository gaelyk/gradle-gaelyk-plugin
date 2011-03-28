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
	
	def "Install from URL"(){
		setup:
		def dest = TempDir.createNew("install-from-url")
		
		when:
		new PluginManager(dest).install url: "http://klient.appsatori.eu/github/gradle-gaelyk-plugin/project-with-git.zip"
		
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
	
	def "Installation removed from history file"(){
		setup:
		def dest = TempDir.createNew("remove-history-file")
		
		when:
		new AntBuilder().copy(file: "src/test/resources/.gaelykhistory", todir: dest)
		new PluginManager(dest).removeHistoryRecord "git://example.com/plugin1.git"
		def history = new XmlSlurper().parse(new File(dest, ".gaelykhistory"))
		then:
		history.plugin.size() == 1
		!history.plugin.find{it.@origin == "git://example.com/plugin1.git"}
		
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
		def tbd = manager.filesToDelete(what)
		
		then:
		tbd
		tbd.size() == 2
		tbd == ["test", "war/WEB-INF/plugins/plugin1.groovy"]
		
		where:
		what << ["git://example.com/plugin1.git", "plugin1"]
	}
	
	def "Enable plugin"(){
		setup:
		def dest = TempDir.createNew("enable-plugin")
		def manager = new PluginManager(dest)
		when:
		manager.installPlugins "src/test/resources"
		def pluginFile = new File("$dest.path/war/WEB-INF/plugins.groovy")
		then:
		pluginFile.exists()
		pluginFile.filterLine {it =~ /\s*install\s+plugin1\s*/}
	}
	
	def "Disable plugin"(){
		setup:
		def dest = TempDir.createNew("enable-plugin")
		def manager = new PluginManager(dest)
		new AntBuilder().copy(file: "./src/test/resources/.gaelykhistory", todir: dest.path)
		
		when:
		manager.installPlugins "src/test/resources"
		manager.removePlugin "plugin1"
		def pluginFile = new File("$dest.path/war/WEB-INF/plugins.groovy")
		
		then:
		!pluginFile.exists()
		
		cleanup:
		new AntBuilder().delete dir:dest.path
	}
	
	def "Install from git"(){
		setup:
		def temp = TempDir.createNew("install-from-git-temp")
		def dest = TempDir.createNew("install-from-git-dest")
		
		when:
		new AntBuilder().unzip(src: "src/test/resources/archives/project-with-git.zip", dest: temp.path)
		def manager = new PluginManager(dest)
		manager.installFromGit temp.path
		
		then:
		new File(dest, file).exists() == exists
		
		cleanup:
		new AntBuilder().delete dir:temp.path
		new AntBuilder().delete dir:dest.path
		
		where:
		file           | exists
		'.test'        | true
		'.git'         | false
		'README.TXT'   | false
		'build.groovy' | false
		'gaelyk.plugin'| false
	}
	
	def "List installed plugins"(){
		when:
		def manager = new PluginManager("src/test/resources")
		then:
		manager.installedPlugins == ["git://example.com/plugin1.git", "git://example.com/plugin2.git"]
	}
}
