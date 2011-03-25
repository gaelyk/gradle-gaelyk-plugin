package org.gradle.api.plugins.gaelyk.tools

import groovy.xml.StreamingMarkupBuilder;
import groovy.xml.XmlUtil;

import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;

class PluginManager {

	static final String GAELYKHISTORY = ".gaelykhistory"

	static final String GAELYKPLUGIN = ".gaelykplugin"
	
	final File projectRoot
	
	PluginManager(projectRoot){
		this.projectRoot = projectRoot as File
	}
	
	def install(plugin){
		if (plugin.archive) {
			installFromZip plugin.archive as File
		}
	}
	
	private installFromZip(File zip){
		TempDir.withTempDir 'gaelyk-unzipped-plugin', { tmp ->
			new AntBuilder().unzip src: zip.path, dest: tmp.path
			
			def props = readPluginDescriptor(tmp.path)
			
			TempDir.withTempDir 'gaelyk-plugin-stage', { stage ->
				
				new AntBuilder().copy(todir: stage.path){
					fileset(dir: tmp.path){
						exclude(name: "*build.*")
						exclude(name: ".**")
						exclude(name: "**/.*/**")
						for(ex in props.excludes){
							exclude(name: ex)
						}
					}
					fileset(dir: tmp.path){
						for(ex in props.includes){
							include(name: ex)
						}
					}
				}
				
				addHistory(stage, zip.path)
				
				new AntBuilder().copy(todir: projectRoot.path){
					fileset(dir: stage.path)
				}
		    }

		}
	}
	
	private readPluginDescriptor(pluginRoot){
		def file = new File(pluginRoot as File, GAELYKPLUGIN) 
		if(!file.exists()){
			return []
		}
        Binding binding = []
		GroovyShell shell = [binding]
		shell.evaluate file.text
		binding.variables
	}
	
	private readHistory(){
		def historyFile = new File(projectRoot, GAELYKHISTORY) 
		
	}
	
	private addHistory(stage, pluginUrl){
		def theHistory = history
		def pluginHistory = theHistory.plugin.find{it.@origin == pluginUrl}
		if(pluginHistory){
			pluginHistory.replaceNode{
				plugin(origin: pluginUrl, installed: new Date()){
					stage.eachFileRecurse{ theFile ->
						file(name: theFile.path[stage.path.size()..-1])
					}
				}
			}
		} else {
			theHistory.appendNode{
				plugin(origin: pluginUrl, installed: new Date()){
					stage.eachFileRecurse{ theFile ->
						file(name: theFile.path[stage.path.size()..-1])
					}
				}
			}
		}
		writeHistory(theHistory)
	}

	private getHistory() {
		new XmlSlurper().parse(historyFile)
	}

	private writeHistory(history) {
		historyFile.text = new StreamingMarkupBuilder().bind{ mkp.yield history }
	}

	private File getHistoryFile() {
		def historyFile = new File(projectRoot, GAELYKHISTORY)
		if(!historyFile.exists()){
			historyFile.createNewFile()
			historyFile.append("<history></history>")
		}
		return historyFile
	}
	
	private filesToDelete(origin){
		def installedBy = history.plugin.find{it.@origin == origin}.file*.@name*.text()
		def byOthers = []
		history.plugin.findAll{it.@origin != origin}.each{
			 byOthers << it.file.@name.text()
		}
		installedBy - byOthers
	}

}
