package org.gradle.api.plugins.gaelyk.rules

import org.gradle.api.Project;
import org.gradle.api.plugins.gaelyk.tasks.ArgsTask;
import org.gradle.testfixtures.ProjectBuilder;

import spock.lang.Specification;

class ArgsRuleSpec extends Specification {
	
	def "Args rule works as expected"(){
		Project project = ProjectBuilder.builder().build()
		def args = project.task('args', type: ArgsTask)
		def rule = new ArgsRule(args: args, project: project)
		
		when:
		rule.apply "property=value"
		then:
		args.map.property == "value"
		
	}

}
