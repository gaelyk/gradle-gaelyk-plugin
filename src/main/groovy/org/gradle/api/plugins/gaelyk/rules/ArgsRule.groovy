package org.gradle.api.plugins.gaelyk.rules

import org.gradle.api.Project;
import org.gradle.api.Rule;
import org.gradle.api.plugins.gaelyk.tasks.ArgsTask;

class ArgsRule implements Rule{

	ArgsTask args
	Project project
	
	void apply(String taskName) {
		def match = taskName =~ /(.*?)=(.*?$)/
		if(match){
			args.map[match[0][1]] = match[0][2]
			project.task(taskName) << {
				println "Used to pass value ${match[0][2]} to args.map.${match[0][1]}"
			}
		}
	}
	public String getDescription() {
		"Pattern: <property>=<value>: Passes arguments to the scripts"
	}
	
	
}
