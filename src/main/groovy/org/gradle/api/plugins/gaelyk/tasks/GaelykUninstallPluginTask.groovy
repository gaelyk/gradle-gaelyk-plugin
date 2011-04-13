package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.gaelyk.GaelykPlugin;
import org.gradle.api.plugins.gaelyk.tools.PluginManager;
import org.gradle.api.tasks.TaskAction;

/**
* {@link Task} which uninstalls Gaelyk plugins defined by plugin=path.
* @author Vladimir Orany
*
*/
class GaelykUninstallPluginTask extends DefaultTask {
	
	PluginManager manager = new PluginManager()
	
	GaelykUninstallPluginTask(){
		dependsOn 'args'
		group = GaelykPlugin.GAELYK_GROUP
		description = "Uninstalls Gaelyk plugin. Use plugin=<path> to specify the name. Use '//' instead of 'http://'."
	}
	
	@TaskAction
	def uninstall(){
		def args = project.tasks.findByName('args')
		assert args
		def plugin = args.map.plugin
		assert plugin
		if(plugin.startsWith("//")) { plugin = "http:" + plugin }
		manager.uninstall plugin
		println "$plugin uninstalled successfully."
	}

}
