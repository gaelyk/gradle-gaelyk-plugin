package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

/**
 * {@link Task} which stores properties passed from command line.
 * @author Vladimir Orany
 *
 */
class ArgsTask extends DefaultTask {

	ArgsTask(){
		group = 'Helper'
		description = 'Stores arguments <name>=<value> in the "map" property.'
	}
	
	def map = [:]
	
	@TaskAction
	def info(){
		println "Args task ready to store arguments from command line"
	}
	
}
