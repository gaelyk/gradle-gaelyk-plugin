package org.gradle.api.plugins.gaelyk.integration

import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import spock.lang.Unroll
import static org.gradle.api.plugins.gae.GaePlugin.GAE_RUN
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.LIBRARIES_DIRECTORY_RELATIVE_PATH
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.GAELYK_COPY_RUNTIME_LIBRARIES

class GaelykCopyRuntimeLibrariesIntegrationSpec extends IntegrationSpec {
    @Unroll
    void 'gaelykCopyRuntimeLibraries synchronises libs dir when webAppDir is #scenario'() {
        given:
        skipGaeRun()
        specifyWebAppDirAndCreateGroovletsDir(webAppDir)

        when:
        def project = projectForTasks(GAE_RUN)
        def libsDirFiles = new File(dir.root, libsDir).listFiles()
        Configuration runtimeConfiguration = project.configurations.findByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME)

        then:
        libsDirFiles
        libsDirFiles.size() == runtimeConfiguration.resolvedConfiguration.resolvedArtifacts.size()

        where:
        scenario        | libsDir                                                    | webAppDir
        'not specified' | "$DEFAULT_WEB_APP_PATH/$LIBRARIES_DIRECTORY_RELATIVE_PATH" | null
        'specified'     | "customWebappDir/$LIBRARIES_DIRECTORY_RELATIVE_PATH"       | 'customWebappDir'
    }

    void 'gaelykCopyRuntimeLibraries is skipped if running in non-rad mode'() {
        given:
        nonRadMode()

        when:
        runTasks(GAELYK_COPY_RUNTIME_LIBRARIES)

        then:
        task(GAELYK_COPY_RUNTIME_LIBRARIES).state.skipped
    }
}