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
package org.gradle.api.plugins.gaelyk.tasks

import java.util.regex.Matcher

import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.gaelyk.tools.TempDir
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Task for precompiling templates located in the webapp directory.
 *
 * @author Greg Butt, Benjamin Muschko, Vladimir Orany
 */
class GaelykPrecompileTemplateTask extends DefaultTask {
    static final Logger LOGGER = Logging.getLogger(GaelykPrecompileTemplateTask)

    static getTemplateScriptInfo(File tplRoot, File tplFile){
        String relativePath = (tplFile.absolutePath - tplRoot.absolutePath)
        Matcher m = relativePath =~ "/?(.*)/(.+?)\\.gtpl"
        if(m){
            return [dir: m[0][1] ?: '', file: '$gtpl$' + m[0][2] + '.groovy']
        }
        return null
    }

    @InputFiles FileCollection groovyClasspath
    @InputFiles FileCollection runtimeClasspath
    @InputDirectory File srcDir
    @OutputDirectory File destDir

    @TaskAction
    def precompile(){
        TemplateToScriptConvertor convertor = [runtimeClasspath]


        File dir = TempDir.createNew('gtpl-staging')
        srcDir.eachFileRecurse { File template ->
            if(template.name.endsWith('.gtpl')){
                def info = getTemplateScriptInfo(srcDir, template)
                File parent = dir
                if(info.dir){
                    parent = new File(dir.absolutePath + File.separator + info.dir)
                    assert parent.mkdirs()
                }
                assert parent.exists()
                File file = new File(parent, info.file)
                assert file.createNewFile()
                file.write convertor.getTemplateAsScript(template.text)
            }

        }
        ant.taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc', classpath: getGroovyClasspath().asPath)
        LOGGER.info 'Precompiling groovy templates...'
        ant.groovyc(srcdir: dir, destdir: getDestDir(), classpath: getRuntimeClasspath().asPath)
        LOGGER.info 'Finished precompiling groovy templates.'
    }
}


class TemplateToScriptConvertor {
    private final HiJackGroovyShell hjgs
    private final SimpleTemplateEngine ste

    TemplateToScriptConvertor(FileCollection classpath){
        hjgs = [classpath]
        ste = [hjgs]
    }

    public String getTemplateAsScript(String template){
        ste.createTemplate(template)
        hjgs.scriptText
    }
}

class HiJackGroovyShell extends GroovyShell {

    HiJackGroovyShell(FileCollection classpath){
        super(new CompilerConfiguration(classpath: classpath.asPath))
    }

    String scriptText

    public Script parse(String scriptText, String fileName)
    throws CompilationFailedException {
        this.scriptText = scriptText
        return super.parse(scriptText, fileName)
    }
}