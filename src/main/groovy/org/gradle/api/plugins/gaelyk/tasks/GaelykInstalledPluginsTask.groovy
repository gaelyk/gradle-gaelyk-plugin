package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.gaelyk.tools.PluginManager;
import org.gradle.api.tasks.TaskAction;

class GaelykInstalledPluginsTask extends DefaultTask {
	
	PluginManager manager = new PluginManager()
	
	GaelykInstalledPluginsTask(){
		group = 'gaelyk'
		description = "Lists installed Gaelyk plugins."
	}
	
	@TaskAction
	def list(){
		manager.history.plugin.each{ plugin
			println "${plugin.@name.text().padRight(30)} | ${plugin.@origin.text()}"
		}
	}

}
