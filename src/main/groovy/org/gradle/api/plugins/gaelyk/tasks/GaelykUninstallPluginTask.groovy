package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.gaelyk.tools.PluginManager;
import org.gradle.api.tasks.TaskAction;

class GaelykUninstallPluginTask extends DefaultTask {
	
	PluginManager manager = new PluginManager()
	
	GaelykUninstallPluginTask(){
		dependsOn 'args'
		group = 'gaelyk'
		description = "Uninstalls Gaelyk plugin of the given name. Use plugin=<name> to specify the name."
	}
	
	@TaskAction
	def uninstall(){
		def args = project.tasks.findByName('args')
		assert args
		def plugin = args.map.plugin
		assert plugin
		manager.uninstall plugin
	}

}
