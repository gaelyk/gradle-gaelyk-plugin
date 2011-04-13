package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import spock.lang.Specification;

class ArgsTaskSpec extends Specification {

	def "Test args task"(){
		Project project = ProjectBuilder.builder().build()
		def task = project.task('args', type: ArgsTask)
		expect:
		task in ArgsTask
	}
	
}
