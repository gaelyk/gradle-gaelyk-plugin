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

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.gradle.api.file.FileCollection

/**
 * Specialized GroovyShell implementation that parses script text.
 *
 * @author Vladimir Orany
 */
class HiJackGroovyShell extends GroovyShell {
    HiJackGroovyShell(FileCollection classpath) {
        super(new CompilerConfiguration(classpath: classpath.asPath))
    }

    String scriptText
    def errors = []

    @Override
    Script parse(String scriptText, String fileName) throws CompilationFailedException {
        this.scriptText = scriptText
        try{
            super.parse(scriptText, fileName)            
        } catch(e){
            if(e instanceof MultipleCompilationErrorsException){
                errors = e.errorCollector.errors
            }
            throw e
        }
    }
}
