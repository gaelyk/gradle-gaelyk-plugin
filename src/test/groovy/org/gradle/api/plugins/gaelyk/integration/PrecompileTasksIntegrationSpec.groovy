package org.gradle.api.plugins.gaelyk.integration

import org.gradle.BuildResult
import spock.lang.Unroll

import static org.gradle.api.plugins.gae.GaePlugin.getGAE_RUN
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.getGAELYK_PRECOMPILE_GROOVLET
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.getGAELYK_PRECOMPILE_TEMPLATE

class PrecompileTasksIntegrationSpec extends IntegrationSpec {
    @Unroll
    def 'precompile tasks are in graph when war optimization is #scenario'() {
        given:
        skipGaeRun()
        buildFile << """
            gae { optimizeWar = $optimizeWar }
        """

        when:
        runTasks(GAE_RUN)

        then:
        executedTasks*.task*.name.containsAll([GAELYK_PRECOMPILE_GROOVLET, GAELYK_PRECOMPILE_TEMPLATE])

        where:
        scenario   | optimizeWar
        'enabled'  | true
        'disabled' | false
    }

    @Unroll
    def 'precompile tasks should #scenario when gaeRun is in task graph'() {
        given:
        skipGaeRun()
        radMode rad

        when:
        runTasks(GAE_RUN)

        then:
        tasks(GAELYK_PRECOMPILE_GROOVLET, GAELYK_PRECOMPILE_TEMPLATE).each {
            assert it.state.skipped == skipped
        }

        where:
        scenario                         | rad   | skipped
        'not be skipped in non-RAD mode' | false | false
        'be skipped in RAD mode'         | true  | true
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
}
