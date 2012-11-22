package org.gradle.api.plugins.gaelyk.integration.gaelykplugin

import org.gradle.api.plugins.gaelyk.integration.GaelykPluginPluginIntegrationSpec
import spock.lang.Unroll

import static org.gradle.api.plugins.JavaPlugin.JAR_TASK_NAME
import static org.gradle.api.plugins.gaelyk.GaelykPluginBase.GAELYK_PRECOMPILE_GROOVLET
import static org.gradle.api.plugins.gaelyk.GaelykPluginBase.GAELYK_PRECOMPILE_TEMPLATE
import org.gradle.BuildResult

import static org.gradle.api.plugins.gaelyk.GaelykPluginBase.GROOVLET_DIRECTORY_RELATIVE_PATH

class PrecompileTasksIntegrationSpec extends GaelykPluginPluginIntegrationSpec {
    @Unroll
    def 'precompile tasks are executed when running jar task'() {
        when:
        runTasks(JAR_TASK_NAME)

        then:
        executedTasks*.task*.name.containsAll([GAELYK_PRECOMPILE_GROOVLET, GAELYK_PRECOMPILE_TEMPLATE])
    }

    @Unroll
    def '#task tasks should not blow up if run on its own and when precompiled contents depend on compiled classes'() {
        given:
        file('src/main/groovy/test/A.groovy') << '''
            package test
            class A {}
        '''
        file("$DEFAULT_WEB_APP_PATH/WEB-INF/$path") << contents

        when:
        BuildResult result = launcher(task).run()

        then:
        !result.failure

        where:
        task                       | path                     | contents
        GAELYK_PRECOMPILE_GROOVLET | 'groovy/groovlet.groovy' | 'new test.A()'
        GAELYK_PRECOMPILE_TEMPLATE | 'pages/template.gtpl'    | '<% new test.A() %>'
    }

    def 'can override web app dir which is used as precompiled tasks source'() {
        given:
        directory("src/main/customwebapp/$GROOVLET_DIRECTORY_RELATIVE_PATH")

        buildFile << '''
            gaelykplugin {
                webAppDirName = 'src/main/customwebapp'
            }
        '''

        when:
        runTasks(GAELYK_PRECOMPILE_GROOVLET, GAELYK_PRECOMPILE_TEMPLATE)

        then:
        task(GAELYK_PRECOMPILE_GROOVLET).task.srcDir.path.endsWith "src/main/customwebapp/$GROOVLET_DIRECTORY_RELATIVE_PATH"
        task(GAELYK_PRECOMPILE_TEMPLATE).task.srcDir.path.endsWith 'src/main/customwebapp'
    }
}
