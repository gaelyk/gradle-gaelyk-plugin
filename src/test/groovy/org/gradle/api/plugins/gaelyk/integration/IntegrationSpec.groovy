package org.gradle.api.plugins.gaelyk.integration

import org.gradle.BuildResult
import org.gradle.GradleLauncher
import org.gradle.StartParameter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.initialization.DefaultGradleLauncher
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.*

abstract class IntegrationSpec extends Specification {
    protected final static String DEFAULT_WEB_APP_PATH = 'src/main/webapp'

    @Rule final TemporaryFolder dir = new TemporaryFolder()

    protected List<ExecutedTask> executedTasks = []

    protected GradleLauncher launcher(String... args) {
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
            }
        })
        launcher
    }

    protected File getBuildFile() {
        file("build.gradle")
    }

    protected File directory(String path) {
        new File(dir.root, path).with {
            mkdirs()
            it
        }
    }

    protected File file(String path) {
        def splitted = path.split('/')
        def directory = splitted.size() > 1 ? directory(splitted[0..-2].join('/')) : dir.root
        def file = new File(directory, splitted[-1])
        file.createNewFile()
        file
    }

    protected ExecutedTask task(String name) {
        executedTasks.find { it.task.name == name }
    }

    protected Collection<ExecutedTask> tasks(String... names) {
        executedTasks.findAll { it.task.name in names }
    }

    def setup() {
        directory("$DEFAULT_WEB_APP_PATH/$GROOVLET_DIRECTORY_RELATIVE_PATH")

        buildFile << """
            def GaelykPlugin = project.class.classLoader.loadClass('org.gradle.api.plugins.gaelyk.GaelykPlugin')

            apply plugin: GaelykPlugin

            dependencies {
                groovy 'org.codehaus.groovy:groovy-all:1.8.6'
                compile 'org.gaelyk:gaelyk:1.2'
                gaeSdk "com.google.appengine:appengine-java-sdk:1.6.6"
            }

            repositories {
                mavenCentral()
            }
        """
    }

    protected skipGaeRun() {
        buildFile << '''
            gaeRun.onlyIf { false }
        '''
    }

    protected nonRadMode() {
        buildFile << '''
            gaelyk {
                rad = false
            }
        '''
    }

    protected BuildResult runTasks(String... tasks) {
        BuildResult result = launcher(tasks).run()
        assert !result.failure
        result
    }

    protected Project projectForTasks(String... tasks) {
        runTasks(tasks).gradle.rootProject
    }

    protected void specifyWebAppDirAndCreateGroovletsDir(String webAppDir) {
        if (webAppDir) {
            directory("$webAppDir/$GROOVLET_DIRECTORY_RELATIVE_PATH")
            buildFile << """
                webAppDirName = '$webAppDir'
            """
        }
    }
}
