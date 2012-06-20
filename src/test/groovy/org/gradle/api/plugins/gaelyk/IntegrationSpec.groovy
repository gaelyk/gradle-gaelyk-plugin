package org.gradle.api.plugins.gaelyk

import org.gradle.GradleLauncher
import org.gradle.StartParameter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.plugins.gae.GaePlugin
import org.gradle.api.plugins.gae.GaePluginConvention
import org.gradle.api.tasks.TaskState
import org.gradle.initialization.DefaultGradleLauncher
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class IntegrationSpec extends Specification {
    @Rule final TemporaryFolder dir = new TemporaryFolder()

    static class ExecutedTask {
        Task task
        TaskState state
    }

    List<ExecutedTask> executedTasks = []

    GradleLauncher launcher(String... args) {
        StartParameter startParameter = GradleLauncher.createStartParameter(args)
        startParameter.setProjectDir(dir.root)
        DefaultGradleLauncher launcher = GradleLauncher.newInstance(startParameter)
        launcher.gradle.scriptClassLoader.addParent(getClass().classLoader)
        executedTasks.clear()
        launcher.addListener(new TaskExecutionListener() {
            void beforeExecute(Task task) {
                executedTasks << new ExecutedTask(task: task)
            }

            void afterExecute(Task task, TaskState taskState) {
                executedTasks.last().state = taskState
                taskState.metaClass.upToDate = taskState.skipMessage == "UP-TO-DATE"
            }
        })
        launcher
    }

    File getBuildFile() {
        file("build.gradle")
    }

    File file(String path) {
        def parts = path.split("/")
        if (parts.size() > 1) {
            dir.newFolder(* parts[0..-2])
        }
        dir.newFile(path)
    }

    ExecutedTask task(String name) {
        executedTasks.find { it.task.name == name }
    }

    def setup() {
        buildFile << """
            def GaelykPlugin = project.class.classLoader.loadClass('org.gradle.api.plugins.gaelyk.GaelykPlugin')

            apply plugin: 'groovy'
            apply plugin: GaelykPlugin
            apply plugin: 'gae'

            dependencies {
                gaeSdk "com.google.appengine:appengine-java-sdk:1.6.6"
                groovy 'org.codehaus.groovy:groovy-all:1.8.6'
            }

            repositories {
                mavenCentral()
            }

            /*gae {
                downloadSdk = true
            }*/
        """
    }

    void smoke() {
        given:
        Project project = launcher(GaePlugin.GAE_EXPLODE_WAR).run().gradle.rootProject

        expect:
        GaePluginConvention gaeConvention = project.convention.plugins.gae
        gaeConvention.downloadSdk == false
    }
}
