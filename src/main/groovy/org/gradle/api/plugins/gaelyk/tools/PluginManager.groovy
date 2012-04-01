/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.plugins.gaelyk.tools

import groovy.xml.StreamingMarkupBuilder
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * {@link org.gradle.api.plugins.gaelyk.tools.PluginManager} manages installed Gaelyk plugins.
 * Use it to install or uninstall plugins.
 * Installed plugins are recoreded to the  {@code .gaelykhistory} file which
 * can be accessed using getHistory() method (as GPath result).
 * <br/>
 * Only zipped distribution are currently allowed. <code>.gaelykplugin</code>
 * file with includes and excludes variables can be included in the root of
 * the plugin archive. It specifies which files from archive should (not) be 
 * copied into the project directory.
 *
 * @author Vladimir Orany
 */
class PluginManager {
    static final Logger LOGGER = Logging.getLogger(PluginManager.class)
    static final String GAELYKHISTORY = ".gaelykhistory"
    static final String GAELYKPLUGIN = ".gaelykplugin"

    static final String CATALOGUE_LOCATION = 'http://atomicfeed.appspot.com/atom/gaelykByName/forGradle'
    static final String CATALOGUE_BY_UPDATED_LOCATION = 'http://atomicfeed.appspot.com/atom/gaelyk/forGradle'

    final File projectRoot

    PluginManager(projectRoot = "."){
        this.projectRoot = projectRoot as File
    }

    def install(plugin){
        switch (plugin) {
        case ~/http[s]?:.*?.zip/:
            installFromUrl plugin
            break
        case ~/.*?\.zip/:
            installFromZip plugin as File
            break
        default:
            installFromCatalogue plugin
        }
    }

    def getHistory() {
        new XmlSlurper().parse(historyFile)
    }


    def uninstall(origin){
        filesToDelete(origin).each{
            def theFile = new File(projectRoot, it)
            if(it =~ "war/WEB-INF/plugins/\\w+.groovy") {
                def name = theFile.name - ".groovy"
                def plugins = new File(projectRoot, "war/WEB-INF/plugins.groovy")
                if(plugins.exists()){
                    if(plugins.readLines().grep(~/\s*install\s+$name\s*/)){
                        plugins.text = plugins.text.replaceAll(/\s*install\s+$name\s*/, "")
                    }

                    if(!plugins.text.trim()){
                        plugins.delete()
                    }
                }
            }
            if(theFile.exists()){
                theFile.deleteOnExit()
            }
        }
        removeHistoryRecord origin
    }

    private installFromCatalogue(id){
        def plugins = new XmlSlurper().parseText(new URL(PluginManager.CATALOGUE_BY_UPDATED_LOCATION).text)
        def plugin = plugins.plugin.find { it.@id.text() == id }
        if(plugin && plugin.link?.text()){
            installFromUrl plugin.link.text(), id
        } else {
            LOGGER.error "Cannot install plugin: $plugin"
        }
    }

    private installFromUrl(url, id = null){
        File tmp = File.createTempFile("gaelyk-plugin-download-" + new Random().nextInt(), ".zip")
        new AntBuilder().get(src: url, dest: tmp.path)
        installFromZip tmp, url, id
        tmp.delete()
    }

    private installFromZip(File zip, origin = zip.path, id = null){
        TempDir.withTempDir 'gaelyk-unzipped-plugin', { tmp ->
            new AntBuilder().unzip src: zip.path, dest: tmp.path

            installFromDir(tmp, origin, id)

        }
    }

    private installFromDir(tmp, origin = tmp, id){
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
                if(props.includes){
                    fileset(dir: tmp.path){
                        for(ex in props.includes){
                            include(name: ex)
                        }
                    }
                }
            }

            addHistory(stage, origin, id)
            installPlugins(stage)

            new AntBuilder().copy(todir: projectRoot.path){
                fileset(dir: stage.path)
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

    private addHistory(stage, pluginUrl, id){
        def theHistory = history
        def pluginHistory = theHistory.plugin.find{it.@origin == pluginUrl}
        if(pluginHistory){
            pluginHistory.replaceNode{
                plugin(origin: pluginUrl, installed: new Date(), name: id ?: getBareName(pluginUrl.toString())){
                    stage.eachFileRecurse{ theFile ->
                        file(name: theFile.path[stage.path.size()..-1])
                    }
                }
            }
        } else {
            theHistory.appendNode{
                plugin(origin: pluginUrl, installed: new Date(), name: id ?: getBareName(pluginUrl.toString())){
                    stage.eachFileRecurse{ theFile ->
                        file(name: theFile.path[stage.path.size()..-1])
                    }
                }
            }
        }
        writeHistory(theHistory)
    }

    private getBareName(String origin){
        origin[(origin.lastIndexOf("/")+1)..(origin.lastIndexOf(".")-1)]
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
        def installedBy = getHistoryPlugin(origin).file*.@name*.text()
        def byOthers = []
        history.plugin.findAll{it.@origin != origin && it.@name != origin }.each{
             byOthers << it.file.@name.text()
        }
        installedBy - byOthers
    }

    private getHistoryPlugin(origin, theHistory = history){
        theHistory.plugin.find{it.@origin == origin || it.@name == origin }
    }

    private removeHistoryRecord(origin){
        def history = history
        getHistoryPlugin(origin, history).replaceNode{}
        writeHistory history
    }

    private installPlugins(origin){
        def pluginsDir = new File(origin as File, "/war/WEB-INF/plugins")
        if (pluginsDir.exists()) {
            def plugins = new File("$projectRoot.path/war/WEB-INF/plugins.groovy")
            if(!plugins.exists()){
                new AntBuilder().sequential{ mkdir(dir: "$projectRoot.path/war/WEB-INF") }
                assert plugins.createNewFile()
            }
            pluginsDir.list().each {
                def pluginName = it - ".groovy"
                if(!plugins.readLines().grep(~/\s*install\s+$pluginName\s*/))
                plugins.append("\ninstall $pluginName\n")
            }
        }
    }

    private getInstalledPlugins(){
        history.plugin.@origin*.text()
    }
}
