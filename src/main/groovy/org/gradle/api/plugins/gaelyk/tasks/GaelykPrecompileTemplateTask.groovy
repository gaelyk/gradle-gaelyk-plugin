/*
 * Copyright 2012 the original author or authors.
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

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.gaelyk.tools.TempDir
import org.gradle.api.plugins.gaelyk.util.TemplateToScriptConverter

import java.util.regex.Matcher

import org.gradle.api.tasks.*

/**
 * Task for precompiling templates located in the webapp directory.
 *
 * @author Vladimir Orany
 */
@Slf4j
class GaelykPrecompileTemplateTask extends DefaultTask {
    static final String GROOVY_TEMPLATE_FILE_EXT = '.gtpl'
    @InputFiles FileCollection groovyClasspath
    @InputFiles FileCollection runtimeClasspath
    @InputDirectory @SkipWhenEmpty File srcDir
    @OutputDirectory File destDir

    @TaskAction
    def precompile() {
        TemplateToScriptConverter converter = new TemplateToScriptConverter(getRuntimeClasspath())
        File dir = TempDir.createNew('gtpl-staging')

        getSrcDir().eachFileRecurse { File template ->
            if(template.name.endsWith(GROOVY_TEMPLATE_FILE_EXT)){
                def info = getTemplateScriptInfo(getSrcDir(), template)
                File parent = dir

                if(info.dir) {
                    parent = new File(dir.absolutePath, info.dir)
                    parent.mkdirs()
                }

                assert parent.exists()
                File file = new File(parent, info.file)
                assert file.createNewFile()
                file.write converter.getTemplateAsScript(template.text, dirToPackage(info.dir), template, getSrcDir())
            }

        }

        ant.taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc', classpath: getGroovyClasspath().asPath)
        log.info 'Precompiling groovy templates...'
        ant.groovyc(srcdir: dir, destdir: getDestDir(), classpath: getRuntimeClasspath().asPath)
        log.info 'Finished precompiling groovy templates.'
    }

    def getTemplateScriptInfo(File tplRoot, File tplFile){
        String relativePath = (tplFile.absolutePath - tplRoot.absolutePath)
        Matcher m = relativePath =~ "[/\\\\]?(.*)[/\\\\](.+?)\\.gtpl"

        if(m) {
            return [dir: m[0][1] ?: '', file: '$gtpl$' + m[0][2] + '.groovy']
        }

        null
    }

    String dirToPackage(String dir) {
        dir.replace(File.separator, '.').replaceAll(/[^a-zA-Z0-9\.]/, '_').toLowerCase()
    }
}