package org.gradle.api.plugins.gaelyk.integration

import org.gradle.api.plugins.GroovyPlugin
import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME

class GroovyPluginIntegrationSpec extends IntegrationSpec {
    void 'groovy plugin is applied to the project'() {
        expect:
        launcher(CLASSES_TASK_NAME).buildAnalysis.gradle.rootProject.plugins.hasPlugin(GroovyPlugin)
    }
}
