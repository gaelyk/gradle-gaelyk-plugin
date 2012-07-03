package org.gradle.api.plugins.gaelyk.integration

import spock.lang.Unroll
import org.gradle.api.plugins.JavaPlugin
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.*
import org.gradle.api.artifacts.Configuration

class GaelykCopyRuntimeLibrariesIntegrationSpec extends IntegrationSpec {
    @Unroll
    void 'gaelykCopyRuntimeLibraries synchronises libs dir when webAppDir is #scenario'() {
        given:
        specifyWebAppDirAndCreateGroovletsDir(webAppDir)

        when:
        def project = projectForTasks(GAELYK_COPY_RUNTIME_LIBRARIES)
        def libsDirFiles = new File(dir.root, libsDir).listFiles()
        Configuration runtimeConfiguration = project.configurations.findByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME)

        then:
        libsDirFiles
        libsDirFiles.size() == runtimeConfiguration.files.size()

        where:
        scenario        | libsDir                                                    | webAppDir
        'not specified' | "$DEFAULT_WEB_APP_PATH/$LIBRARIES_DIRECTORY_RELATIVE_PATH" | null
        'specified'     | "customWebappDir/$LIBRARIES_DIRECTORY_RELATIVE_PATH"       | 'customWebappDir'
    }
}