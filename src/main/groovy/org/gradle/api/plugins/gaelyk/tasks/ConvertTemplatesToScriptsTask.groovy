package org.gradle.api.plugins.gaelyk.tasks

import java.util.regex.Matcher

import org.gradle.api.file.FileVisitDetails
import org.gradle.api.plugins.gaelyk.util.TemplateToScriptConverter
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.compile.AbstractCompile

class ConvertTemplatesToScriptsTask extends AbstractCompile {
    
    static final String DEFAULT_GROOVY_TEMPLATE_FILE_EXT = 'gtpl'
    static final String PRECOMPILE_TEMPLATE_STAGE_DIR = '/gtpl-as-script'
    
    @Input String templateExtension = ConvertTemplatesToScriptsTask.DEFAULT_GROOVY_TEMPLATE_FILE_EXT
    
    protected void compile () {
        TemplateToScriptConverter converter = new TemplateToScriptConverter(getClasspath())
        File dir = getDestinationDir()
        dir.deleteDir()
        dir.mkdirs()

        getSource().visit { FileVisitDetails details ->
            if (details.directory || !details.name.endsWith("." + templateExtension)) {
                return
            }
            def info = getTemplateScriptInfo("/" + details.relativePath.pathString)
            
            File parent = dir
            
            if(info.dir) {
                parent = new File(dir.absolutePath, info.dir)
                parent.mkdirs()
            }
    
            assert parent.exists()
            File file = new File(parent, info.file)
            assert file.createNewFile()
            file.write converter.getTemplateAsScript(details.file.text, dirToPackage(info.dir), details.file, new File(details.file.absolutePath - details.relativePath.pathString))
        }
    }

    def getTemplateScriptInfo(String relativePath){
        String pattern = "[/\\\\]?(.*)[/\\\\](.+?)\\." + templateExtension
        Matcher m = relativePath =~ (pattern)

        if(m) {
            return [dir: m[0][1] ?: '', file: getPrefix(templateExtension) + m[0][2] + '.groovy']
        }
        null
    }
    
    File getStageDir() {
        new File(project.buildDir.absolutePath + PRECOMPILE_TEMPLATE_STAGE_DIR)
    }

    String dirToPackage(String dir) {
        dir.replace(File.separator, '.').replaceAll(/[^a-zA-Z0-9\.]/, '_').toLowerCase()
    }
    
    static String getPrefix(String ext) {
        '$' + ext + '$'
    }

}
