package org.gradle.api.plugins.gaelyk.integration

import spock.lang.Unroll
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.*
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.BasePlugin

class MainSourceSetIntegrationSpec extends IntegrationSpec {

    @Unroll
    void 'clean tasks also cleans class output directory when webAppDir is #scenario'() {
        given:
        specifyWebAppDirAndCreateGroovletsDir(webAppDir)
        directory(classOutputDir)

        when:
        runTasks(BasePlugin.CLEAN_TASK_NAME)

        then:
        !new File(dir.root, classOutputDir).exists()

        where:
        scenario        | classOutputDir                                          | webAppDir
        'not specified' | "$DEFAULT_WEB_APP_PATH/$OUTPUT_DIRECTORY_RELATIVE_PATH" | null
        'specified'     | "customWebappDir/$OUTPUT_DIRECTORY_RELATIVE_PATH"       | 'customWebappDir'
    }

    @Unroll
    void 'main source set output points to classes dir in WEB-INF when webAppDir is #scenario'() {
        given:
        specifyWebAppDirAndCreateGroovletsDir(webAppDir)
        file(('src/main/groovy/A.groovy')) << """
            class A {}
        """

        when:
        runTasks(JavaPlugin.CLASSES_TASK_NAME)

        then:
        new File(dir.root, "$classOutputDir/A.class").exists()

        where:
        scenario        | classOutputDir                                          | webAppDir
        'not specified' | "$DEFAULT_WEB_APP_PATH/$OUTPUT_DIRECTORY_RELATIVE_PATH" | null
        'specified'     | "customWebappDir/$OUTPUT_DIRECTORY_RELATIVE_PATH"       | 'customWebappDir'
    }

    void 'main source set output is not modified when running in non-rad mode'() {
        given:
        file(('src/main/groovy/A.groovy')) << """
            class A {}
        """
        buildFile << """
            gaelyk {
                rad = false
            }
        """

        when:
        runTasks(JavaPlugin.CLASSES_TASK_NAME)

        then:
        !new File(dir.root, "$DEFAULT_WEB_APP_PATH/$OUTPUT_DIRECTORY_RELATIVE_PATH").exists()
    }

}
