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
import org.gradle.api.plugins.JavaPlugin

import static org.gradle.api.plugins.gaelyk.GaelykPlugin.*

class IntegrationSpec extends Specification {
    private final static String DEFAULT_WEB_APP_PATH = 'src/main/webapp'

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
        directory("$DEFAULT_WEB_APP_PATH/$GROOVLET_DIRECTORY_RELATIVE_PATH")

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

    private void specifyWebAppDirAndCreateGroovletsDir(String webAppDir) {
        if (webAppDir) {
            directory("$webAppDir/$GROOVLET_DIRECTORY_RELATIVE_PATH")
            file('build.gradle') << """
                webAppDirName = '$webAppDir'
            """
        }
    }

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
}
