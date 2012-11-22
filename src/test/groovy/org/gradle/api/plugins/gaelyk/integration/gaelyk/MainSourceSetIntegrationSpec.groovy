package org.gradle.api.plugins.gaelyk.integration.gaelyk

import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.gaelyk.integration.GaelykPluginIntegrationSpec
import spock.lang.Unroll

import static org.gradle.api.plugins.gaelyk.GaelykPlugin.OUTPUT_DIRECTORY_RELATIVE_PATH
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.LIBRARIES_DIRECTORY_RELATIVE_PATH
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.APPENGINE_GENERATED_RELATIVE_PATH

class MainSourceSetIntegrationSpec extends GaelykPluginIntegrationSpec {

    @Unroll
    void 'clean tasks also cleans class output, libs, and appengine-generated dirs in rad mode when webAppDir is #scenario'() {
        given:
        specifyWebAppDirAndCreateGroovletsDir(webAppDirPath)
        def webAppDir = new File(dir.root, reslovedWebAppDir)
        [OUTPUT_DIRECTORY_RELATIVE_PATH, LIBRARIES_DIRECTORY_RELATIVE_PATH, APPENGINE_GENERATED_RELATIVE_PATH].each {
            new File(webAppDir, it).mkdirs()
        }

        when:
        runTasks(BasePlugin.CLEAN_TASK_NAME)

        then:
        [OUTPUT_DIRECTORY_RELATIVE_PATH, LIBRARIES_DIRECTORY_RELATIVE_PATH, APPENGINE_GENERATED_RELATIVE_PATH].each {
            assert !new File(webAppDir, it).exists()
        }

        where:
        scenario        | webAppDirPath
        'not specified' | null
        'specified'     | 'customWebappDir'

        reslovedWebAppDir = webAppDirPath ?: DEFAULT_WEB_APP_PATH
    }

    void 'class output, libs, and appengine-generated dirs are not cleaned if not in rad mode'() {
        given:
        nonRadMode()
        def webAppDir = new File(dir.root, DEFAULT_WEB_APP_PATH)
        [OUTPUT_DIRECTORY_RELATIVE_PATH, LIBRARIES_DIRECTORY_RELATIVE_PATH, APPENGINE_GENERATED_RELATIVE_PATH].each {
            new File(webAppDir, it).mkdirs()
        }

        when:
        runTasks(BasePlugin.CLEAN_TASK_NAME)

        then:
        [OUTPUT_DIRECTORY_RELATIVE_PATH, LIBRARIES_DIRECTORY_RELATIVE_PATH, APPENGINE_GENERATED_RELATIVE_PATH].each {
            assert new File(webAppDir, it).exists()
        }
    }

    @Unroll
    void 'main source set output points to classes dir in WEB-INF when webAppDir is #scenario'() {
        given:
        specifyWebAppDirAndCreateGroovletsDir(webAppDir)
        addSourcesAndResources()

        when:
        runTasks(JavaPlugin.CLASSES_TASK_NAME)

        then:
        ['A.class', 'resource.txt'].every {
            new File(dir.root, "$classOutputDir/$it").exists()
        }

        where:
        scenario        | classOutputDir                                          | webAppDir
        'not specified' | "$DEFAULT_WEB_APP_PATH/$OUTPUT_DIRECTORY_RELATIVE_PATH" | null
        'specified'     | "customWebappDir/$OUTPUT_DIRECTORY_RELATIVE_PATH"       | 'customWebappDir'
    }

    void 'main source set output is not modified when running in non-rad mode'() {
        given:
        addSourcesAndResources()
        nonRadMode()

        when:
        runTasks(JavaPlugin.CLASSES_TASK_NAME)

        then:
        !new File(dir.root, "$DEFAULT_WEB_APP_PATH/$OUTPUT_DIRECTORY_RELATIVE_PATH").exists()
    }

    private void addSourcesAndResources() {
        file(('src/main/groovy/A.groovy')) << '''
            class A {}
        '''
        file(('src/main/resources/resource.txt')) << 'text'
    }

}
