package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.gaelyk.GaelykPlugin;
import org.gradle.api.plugins.gaelyk.tools.PluginManager;
import org.gradle.api.tasks.TaskAction;

/**
* {@link Task} which installs Gaelyk plugins defined by plugin=path.
* @author Vladimir Orany
*
*/
class GaelykInstallPluginTask extends DefaultTask {
	
	PluginManager manager = new PluginManager()
	
	GaelykInstallPluginTask(){
		dependsOn 'args'
		group = GaelykPlugin.GAELYK_GROUP
		description = "Installs Gaelyk plugin. Use plugin=<path> to specify the name. Use '//' instead of 'http://'."
	}
	
	@TaskAction
	def install(){
		def args = project.tasks.findByName('args')
		assert args
		def plugin = args.map.plugin
		assert plugin
		if(plugin.startsWith("//")) { plugin = "http:" + plugin }
		manager.install plugin
		println "$plugin uninstalled successfully."
	}

}
