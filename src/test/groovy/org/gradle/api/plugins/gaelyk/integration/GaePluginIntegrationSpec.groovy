package org.gradle.api.plugins.gaelyk.integration

import org.gradle.api.Project
import org.gradle.api.plugins.WarPluginConvention
import org.gradle.api.plugins.gae.GaePlugin
import org.gradle.api.plugins.gae.GaePluginConvention
import org.gradle.api.plugins.gae.task.GaeRunTask
import org.gradle.api.plugins.gaelyk.GaelykPlugin
import spock.lang.Unroll
import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME

class GaePluginIntegrationSpec extends IntegrationSpec {
    def cleanup() {
        launcher(GaePlugin.GAE_STOP).run()
    }

    def setup() {
        buildFile << """
            gae {
                stopKey = 'stop'
                daemon = true
            }
        """
    }

    private void skipWarOptimization() {
        buildFile << """
            gae {
                optimizeWar = false
            }
        """
    }

    void 'gae plugin is applied to the project'() {
        expect:
        launcher(CLASSES_TASK_NAME).buildAnalysis.gradle.rootProject.plugins.hasPlugin(GaePlugin)
    }

    void 'gae plugin conventions are set up'() {
        given:
        Project project = projectForTasks(CLASSES_TASK_NAME)
        GaePluginConvention gaeConvention = project.convention.plugins.gae

        expect:
        gaeConvention.downloadSdk
        gaeConvention.optimizeWar
    }

    void 'war explosion is not performed'() {
        given:
        skipWarOptimization()
        skipGaeRun()

        when:
        runTasks(GaePlugin.GAE_RUN)

        then:
        task(GaePlugin.GAE_EXPLODE_WAR).state.skipped
    }

    @Unroll
    void "gae run task's war dir is set based on war plugin's convention when #scenario"() {
        given:
        skipWarOptimization()
        specifyWebAppDirAndCreateGroovletsDir(webAppDir)

        when:
        Project project = projectForTasks(GaePlugin.GAE_RUN)
        WarPluginConvention warPluginConvention = project.convention.plugins.war

        then:
        GaeRunTask runTask = task(GaePlugin.GAE_RUN).task
        runTask.explodedWarDirectory == warPluginConvention.webAppDir

        where:
        scenario                     | webAppDir
        'convention is not modified' | null
        'a custom dir is specified'  | 'customWebapp'
    }

    void 'gaelykCopyRuntimeLibraries is executed before gaeRun'() {
        given:
        skipWarOptimization()
        skipGaeRun()

        when:
        runTasks(GaePlugin.GAE_RUN)

        then:
        task(GaelykPlugin.GAELYK_COPY_RUNTIME_LIBRARIES).state.executed
    }
}
