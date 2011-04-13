package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.gaelyk.tools.PluginManager;
import org.gradle.api.tasks.TaskAction;

class GaelykInstallPluginTask extends DefaultTask {
	
	PluginManager manager = new PluginManager()
	
	GaelykInstallPluginTask(){
		dependsOn 'args'
		group = 'gaelyk'
		description = "Installs Gaelyk plugin of the given name. Use plugin=<name> to specify the name."
	}
	
	@TaskAction
	def install(){
		def args = project.tasks.findByName('args')
		assert args
		def plugin = args.map.plugin
		assert plugin
		manager.install plugin
	}

}
