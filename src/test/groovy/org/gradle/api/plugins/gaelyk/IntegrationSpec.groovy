package org.gradle.api.plugins.gaelyk

import org.gradle.BuildResult
import org.gradle.GradleLauncher
import org.gradle.StartParameter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.WarPluginConvention
import org.gradle.api.plugins.gae.GaePlugin
import org.gradle.api.plugins.gae.GaePluginConvention
import org.gradle.api.plugins.gae.task.GaeRunTask
import org.gradle.api.tasks.TaskState
import org.gradle.initialization.DefaultGradleLauncher
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

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

    File directory(String path) {
        new File(dir.root, path).with {
            mkdirs()
            it
        }
    }

    File file(String path) {
        def splitted = path.split('/')
        def directory = splitted.size() > 1 ? directory(splitted[0..-2].join('/')) : dir.root
        def file = new File(directory, splitted[-1])
        file.createNewFile()
        file
    }

    ExecutedTask task(String name) {
        executedTasks.find { it.task.name == name }
    }

    def cleanup() {
        launcher(GaePlugin.GAE_STOP).run()
    }

    def setup() {
        directory('src/main/webapp/WEB-INF/groovy')

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

            gae {
                stopKey = 'stop'
                daemon = true
            }
        """
    }

    private BuildResult runTasks(String... tasks) {
        BuildResult result = launcher(tasks).run()
        assert !result.failure
        result
    }

    private Project projectForTasks(String... tasks) {
        runTasks(tasks).gradle.rootProject
    }

    void 'gae plugin conventions are set up'() {
        given:
        Project project = projectForTasks(GaePlugin.GAE_RUN)
        GaePluginConvention gaeConvention = project.convention.plugins.gae

        expect:
        gaeConvention.downloadSdk
        gaeConvention.optimizeWar
    }

    void 'war explosion is not performed'() {
        when:
        runTasks(GaePlugin.GAE_RUN)

        then:
        task(GaePlugin.GAE_EXPLODE_WAR).state.skipped
    }

    @Unroll
    void "gae run task's war dir is set based on war plugin's convention when #scenario"() {
        given:
        if (webAppDir) {
            directory(webAppDir + '/WEB-INF/groovy')
            file('build.gradle') << """
                webAppDirName = '$webAppDir'
            """
        }

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

    @Unroll
    void 'clean tasks also cleans class output directory when webAppDir is #scenario'() {
        given:
        if (webAppDir) {
            file('build.gradle') << """
                webAppDirName = '$webAppDir'
            """
        }

        directory(classOutputDir)

        when:
        runTasks(BasePlugin.CLEAN_TASK_NAME)

        then:
        !new File(dir.root, classOutputDir).exists()

        where:
        scenario        | classOutputDir                   | webAppDir
        'specified'     | 'src/main/webapp/WEB-INF/classes' | null
        'not specified' | 'customWebappDir/WEB-INF/classes' | 'customWebappDir'
    }
}
