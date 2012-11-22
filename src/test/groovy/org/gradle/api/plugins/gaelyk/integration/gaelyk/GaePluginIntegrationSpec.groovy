package org.gradle.api.plugins.gaelyk.integration.gaelyk

import org.gradle.api.Project
import org.gradle.api.plugins.WarPluginConvention
import org.gradle.api.plugins.gae.GaePlugin
import org.gradle.api.plugins.gae.GaePluginConvention
import org.gradle.api.plugins.gae.task.GaeRunTask
import org.gradle.api.plugins.gaelyk.GaelykPlugin
import org.gradle.api.plugins.gaelyk.integration.GaelykPluginIntegrationSpec
import spock.lang.Unroll

import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME

class GaePluginIntegrationSpec extends GaelykPluginIntegrationSpec {
    def setup() {
        buildFile << """
            gae {
                stopKey = 'stop'
                daemon = true
            }
        """
    }

    private void stopServer() {
        launcher(GaePlugin.GAE_STOP).run()
    }

    void 'gae plugin is applied to the project'() {
        expect:
        launcher(CLASSES_TASK_NAME).buildAnalysis.gradle.rootProject.plugins.hasPlugin(GaePlugin)
    }

    void 'gae plugin conventions are set up'() {
        given:
        GaePluginConvention gaeConvention = projectForTasks(CLASSES_TASK_NAME).convention.plugins.gae

        expect:
        gaeConvention.downloadSdk
        gaeConvention.optimizeWar
    }

    @Unroll
    void 'gaelyk.preferPrecompiled system property is #scenario'() {
        given:
        radMode rad

        when:
        GaePluginConvention gaeConvention = projectForTasks(CLASSES_TASK_NAME).convention.plugins.gae

        then:
        gaeConvention.jvmFlags.contains('-Dgaelyk.preferPrecompiled=true') == propertySet

        where:
        scenario                   | rad   | propertySet
        'set when in non-RAD mode' | false | true
        'not set when in RAD mode' | true  | false
    }

    void 'war explosion is not performed in rad mode'() {
        given:
        skipGaeRun()

        when:
        runTasks(GaePlugin.GAE_RUN)

        then:
        task(GaePlugin.GAE_EXPLODE_WAR).state.skipped
    }

    void 'war explosion is performed in non-rad mode'() {
        given:
        skipGaeRun()
        nonRadMode()

        when:
        runTasks(GaePlugin.GAE_RUN)

        then:
        task(GaePlugin.GAE_EXPLODE_WAR).state.didWork
    }

    @Unroll
    void "gae run task's war dir is set based on war plugin's convention when #scenario"() {
        given:
        specifyWebAppDirAndCreateGroovletsDir(webAppDir)

        when:
        Project project = projectForTasks(GaePlugin.GAE_RUN)
        WarPluginConvention warPluginConvention = project.convention.plugins.war

        then:
        GaeRunTask runTask = task(GaePlugin.GAE_RUN).task
        runTask.explodedWarDirectory == warPluginConvention.webAppDir

        cleanup:
        stopServer()

        where:
        scenario                     | webAppDir
        'convention is not modified' | null
        'a custom dir is specified'  | 'customWebapp'
    }

    void 'gaelykCopyRuntimeLibraries is executed before gaeRun'() {
        given:
        skipGaeRun()

        when:
        runTasks(GaePlugin.GAE_RUN)

        then:
        task(GaelykPlugin.GAELYK_COPY_RUNTIME_LIBRARIES).state.executed
    }
}
