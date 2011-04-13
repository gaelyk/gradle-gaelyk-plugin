package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.gaelyk.GaelykPlugin;
import org.gradle.api.plugins.gaelyk.tools.PluginManager;
import org.gradle.api.tasks.TaskAction;

/**
* {@link Task} which prints installed Gaelyk plugins.
* @author Vladimir Orany
*
*/
class GaelykInstalledPluginsTask extends DefaultTask {
	
	PluginManager manager = new PluginManager()
	
	GaelykInstalledPluginsTask(){
		group = GaelykPlugin.GAELYK_GROUP
		description = "Lists installed Gaelyk plugins."
	}
	
	@TaskAction
	def list(){
		println "${'Name'.padRight(35)} Path"
		println "-" * 80
		manager.history.plugin.each{ plugin ->
			println "${(plugin.@name.text()?:'<unspecified>').padRight(35)} ${plugin.@origin.text()}"
		}
	}

}
