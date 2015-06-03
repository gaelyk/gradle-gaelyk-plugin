package org.gradle.api.plugins.gaelyk.tasks

import org.gradle.api.tasks.TaskAction

import java.util.regex.Matcher

import org.gradle.api.file.FileVisitDetails
import org.gradle.api.plugins.gaelyk.util.TemplateToScriptConverter
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.compile.AbstractCompile

class ConvertTemplatesToScriptsTask extends AbstractCompile {
    
    static final String DEFAULT_GROOVY_TEMPLATE_FILE_EXT = 'gtpl'
    static final String PRECOMPILE_TEMPLATE_STAGE_DIR = '/gtpl-as-script'
    
    @Input String templateExtension = ConvertTemplatesToScriptsTask.DEFAULT_GROOVY_TEMPLATE_FILE_EXT

    @TaskAction
    protected void compile () {
        getProject().getLogger().info("Running convert task")
        TemplateToScriptConverter converter = new TemplateToScriptConverter(getClasspath())
        File dir = getDestinationDir()
        dir.deleteDir()
        dir.mkdirs()

        getProject().getLogger().info("Source is:  ${getSource()}, Destination: ${getDestinationDir()}")

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
            getLogger().info("Converting template $details.file")
            getLogger().debug("Absolute path is $details.file.absolutePath")
            getLogger().debug("Relative path is $details.relativePath.pathString")
            file.write converter.getTemplateAsScript(details.file.text, dirToPackage(info.dir), details.file, new File(details.file.absolutePath.replaceAll("\\\\", '/') - details.relativePath.pathString))
        }
    }

    def getTemplateScriptInfo(String relativePath){
        String pattern = "[/\\\\]?(.*)[/\\\\](.+?)\\." + templateExtension
        Matcher m = relativePath =~ (pattern)

        if(m) {
            String name = getPrefix(templateExtension) + (m[0][2].replaceAll(/[^a-zA-Z0-9\$]/, '_')) + '.groovy'
            getLogger().info("Script name is $name")
            return [dir: m[0][1] ?: '', file: name]
        }
        null
    }
    
    File getStageDir() {
        new File(project.buildDir.absolutePath + PRECOMPILE_TEMPLATE_STAGE_DIR)
    }

    String dirToPackage(String dir) {
        dir.replaceAll(/[\\\/]/, '.').replaceAll(/[^a-zA-Z0-9\.\$]/, '_').toLowerCase()
    }
    
    static String getPrefix(String ext) {
        '_' + ext + '_'
    }

}


