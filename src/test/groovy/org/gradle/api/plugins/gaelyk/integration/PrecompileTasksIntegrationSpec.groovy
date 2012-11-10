package org.gradle.api.plugins.gaelyk.integration

import org.gradle.BuildResult
import static org.gradle.api.plugins.gae.GaePlugin.getGAE_RUN
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.getGAELYK_PRECOMPILE_GROOVLET
import static org.gradle.api.plugins.gaelyk.GaelykPlugin.getGAELYK_PRECOMPILE_TEMPLATE
import spock.lang.Unroll

class PrecompileTasksIntegrationSpec extends IntegrationSpec {
    def 'precompile tasks should be skipped when gaeRun is in task graph'() {
        given:
        skipGaeRun()

        when:
        runTasks(GAE_RUN)

        then:
        tasks(GAELYK_PRECOMPILE_GROOVLET, GAELYK_PRECOMPILE_TEMPLATE).every { it.state.skipped }
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
