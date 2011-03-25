package org.gradle.api.plugins.gaelyk.tools

import spock.lang.Specification;

class TempDirSpec extends Specification {

	private static final PREFIX = "gg-temp"

	def "Must be able to create temp dir"(){
		when:
		def tmp = TempDir.createNew(PREFIX)
		
		then:
		tmp in File
		tmp.exists()
		tmp.name.startsWith PREFIX
		
		cleanup:
		new AntBuilder().delete(dir: tmp.path)
	}
	def "Must be able to find temp dir"(){
		when:
		def random = new Random().nextInt()
		def tmp = TempDir.createNew(PREFIX, random)
		def tmpFound = TempDir.findDir(PREFIX, random)
		
		then:
		tmp == tmpFound
		tmp.name == "$PREFIX-$random"
		
		cleanup:
		new AntBuilder().delete(dir: tmp.path)
	}
}
