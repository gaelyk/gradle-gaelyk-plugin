package org.gradle.api.plugins.gaelyk.integration

import static org.gradle.api.plugins.gaelyk.GaelykPlugin.*
import static org.gradle.api.plugins.gae.GaePlugin.*

class PrecompileTasksIntegrationSpec extends IntegrationSpec {
    def 'precompile tasks should be skipped when gaeRun is in task graph'() {
        given:
        skipGaeRun()

        when:
        runTasks(GAE_RUN)

        then:
        tasks(GAELYK_PRECOMPILE_GROOVLET, GAELYK_PRECOMPILE_TEMPLATE).every { it.state.skipped }
    }
}
