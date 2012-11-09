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
package org.gradle.api.plugins.gaelyk.util

import groovy.text.SimpleTemplateEngine

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.gradle.api.file.FileCollection

/**
 * Template to script converter.
 *
 * @author Vladimir Orany
 */
class TemplateToScriptConverter {
    private final HiJackGroovyShell hjgs
    private final SimpleTemplateEngine ste

    TemplateToScriptConverter(FileCollection classpath) {
        hjgs = [classpath]
        ste = [hjgs]
    }

    String getTemplateAsScript(String template, String pkg, File original) {
        try {
            ste.createTemplate(template)            
        } catch (GroovyRuntimeException gre){
            def lines = hjgs.scriptText.split('\n')
            def details = []
            for(SyntaxErrorMessage compilationError in hjgs.errors.findAll{ it instanceof SyntaxErrorMessage }){
                details << ''
                details << compilationError.cause.message
                details << ''
                int detailStartLine = Math.max(0, compilationError.cause.line - 4)
                int detailEndLine = Math.min(compilationError.cause.line + 2, lines.size() - 1)
                int counter = detailStartLine + 1
                int padding = 4
                for(line in lines[detailStartLine..compilationError.cause.line - 1]){
                    details << "${counter}".padLeft(padding) + ": $line"
                    counter ++
                }
                details << ' ' * (compilationError.cause.column + padding + 1) + '^'
                for(line in lines[compilationError.cause.line..detailEndLine]){
                    details << "${counter}".padLeft(padding) + ": $line"
                    counter ++
                }
                details << ''
            }
            if(details){
                throw new GroovyRuntimeException("Problems compiling template $original.absolutePath\nSee attached helping script snippet:\n${details.join('\n')}")                
            }
            throw new GroovyRuntimeException("Problems compiling template $original.absolutePath\n${gre.message}")
        }

        if(pkg) {
            return 'package ' + pkg + ';' + hjgs.scriptText
        }

        hjgs.scriptText
    }
}
