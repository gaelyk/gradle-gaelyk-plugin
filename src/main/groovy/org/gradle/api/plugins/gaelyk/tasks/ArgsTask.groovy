package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

class ArgsTask extends DefaultTask {

	ArgsTask(){
		group = 'helper'
		description = 'Stores arguments <name>=<value> in the "map" property.'
	}
	
	def map = [:]
	
	@TaskAction
	def info(){
		println "Args task ready to store arguments from command line"
	}
	
}
