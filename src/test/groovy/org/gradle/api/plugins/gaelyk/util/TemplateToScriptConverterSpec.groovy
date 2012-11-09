package org.gradle.api.plugins.gaelyk.util

import org.gradle.api.Project
import org.gradle.api.plugins.GroovyBasePlugin
import org.gradle.api.plugins.gaelyk.tools.TempDir;
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class TemplateToScriptConverterSpec extends Specification {

    
    def "Handle includes"(){
        String baseTemplateText = '''
<html>
    <% include 'some.groovy' %>
    <% include "/pkg/${num}.gtpl" %>
    <head><title>${title}</title></head>
    <body>
    <% include 'one.gtpl' %>
    <% include "/pkg/two.gtpl" %>
    <% include '/pkg/two/three.gtpl' %>
    </body>
</html>
'''
        String tplOne       = '<em>ONE is SUPER</em>'
        String tplTwo       = '<strong>TWO is GREAT</strong>'
        String tplThree     = '<% include \'/one.gtpl\' %>'
        
        TemplateToScriptConverter converter = [ buildClasspath()]
        
        File dir = TempDir.createNew("precompile-task")
        File templatesSrcDir = new File(dir, '/templates')
        templatesSrcDir.mkdirs()
        File original = new File(templatesSrcDir, 'base.gtpl')
        original.append(baseTemplateText)
        new File(templatesSrcDir, 'one.gtpl').append(tplOne)
        File packageDir = new File(templatesSrcDir, '/pkg')
        packageDir.mkdirs()
        new File(packageDir, 'two.gtpl').append(tplTwo)
        File packageDir2 = new File(packageDir, '/two')
        packageDir2.mkdirs()
        new File(packageDir2, 'three.gtpl').append(tplThree)
        
        when:
        String template = converter.getTemplateAsScript(baseTemplateText, '', original, templatesSrcDir)
          
        then:
        template.trim() == '''out.print("""
<html>
    """); include 'some.groovy' ;
out.print("""
    """); include "/pkg/${num}.gtpl" ;
out.print("""
    <head><title>${title}</title></head>
    <body>
    """);
/* include#begin /one.gtpl */
out.print("""<em>ONE is SUPER</em>""");
/* include#end   /one.gtpl */
out.print("""
    """);
/* include#begin /pkg/two.gtpl */
out.print("""<strong>TWO is GREAT</strong>""");
/* include#end   /pkg/two.gtpl */
out.print("""
    """);
/* include#begin /pkg/two/three.gtpl */
/* include#begin /one.gtpl */
out.print("""<em>ONE is SUPER</em>""");
/* include#end   /one.gtpl */
/* include#end   /pkg/two/three.gtpl */
out.print("""
    </body>
</html>
""");'''
        
    }

	private org.gradle.api.artifacts.Configuration buildClasspath() {
		Project project = ProjectBuilder.builder().build()
		GroovyBasePlugin groovyBasePlugin = new GroovyBasePlugin()
		groovyBasePlugin.apply(project)
		project.configurations.getByName(GroovyBasePlugin.GROOVY_CONFIGURATION_NAME)
	}
    
}
