package org.gradle.api.plugins.gaelyk.integration

import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.*

abstract class IntegrationSpec extends Specification {
    protected final static String DEFAULT_WEB_APP_PATH = 'src/main/webapp'

    @Rule final TemporaryFolder dir = new TemporaryFolder()

    protected List<ExecutedTask> executedTasks = []

    private final OutputStream standardError = new ByteArrayOutputStream()
    private final OutputStream standardOutput = new ByteArrayOutputStream()

    protected GradleProject run(String... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(dir.root).connect()

        try {
            BuildLauncher builder = connection.newBuild()
            builder.standardError = standardError
            builder.standardOutput = standardOutput
            builder.forTasks(tasks).run()
            def model = connection.getModel(GradleProject)
            return model
        }
        finally {
            connection?.close()
        }
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
        def tasks = executedTasks.findAll { it.task.name in names }
        assert tasks.size() == names.size()
        tasks
    }

    def setup() {
        directory("$DEFAULT_WEB_APP_PATH/$GROOVLET_DIRECTORY_RELATIVE_PATH")

        buildFile << """
            def GaelykPlugin = project.class.classLoader.loadClass('org.gradle.api.plugins.gaelyk.GaelykPlugin')

            apply plugin: GaelykPlugin

            dependencies {
                groovy 'org.codehaus.groovy:groovy-all:2.1.8'
                compile 'org.gaelyk:gaelyk:2.0'
                gaeSdk "com.google.appengine:appengine-java-sdk:1.6.8"
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
