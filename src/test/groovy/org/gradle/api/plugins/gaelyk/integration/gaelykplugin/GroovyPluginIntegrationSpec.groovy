package org.gradle.api.plugins.gaelyk.integration.gaelykplugin

import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.gaelyk.integration.GaelykPluginPluginIntegrationSpec

import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME

class GroovyPluginIntegrationSpec extends GaelykPluginPluginIntegrationSpec {
    void 'groovy plugin is applied to the project'() {
        expect:
        launcher(CLASSES_TASK_NAME).buildAnalysis.gradle.rootProject.plugins.hasPlugin(GroovyPlugin)
    }
}
