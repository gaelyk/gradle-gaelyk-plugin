package org.gradle.api.plugins.gaelyk.integration

import org.gradle.api.plugins.GroovyPlugin
import spock.lang.Ignore

import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME

@Ignore
class GroovyPluginIntegrationSpec extends IntegrationSpec {
    void 'groovy plugin is applied to the project'() {
        expect:
        run(CLASSES_TASK_NAME).buildAnalysis.gradle.rootProject.plugins.hasPlugin(GroovyPlugin)
    }
}
