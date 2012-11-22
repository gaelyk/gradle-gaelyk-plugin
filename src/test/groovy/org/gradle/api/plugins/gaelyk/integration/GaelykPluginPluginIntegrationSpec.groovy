package org.gradle.api.plugins.gaelyk.integration

abstract class GaelykPluginPluginIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << '''
            def GaelykPluginPlugin = project.class.classLoader.loadClass('org.gradle.api.plugins.gaelyk.GaelykPluginPlugin')

            apply plugin: GaelykPluginPlugin

            dependencies {
                groovy 'org.codehaus.groovy:groovy-all:1.8.6'
            }

            repositories {
                mavenCentral()
            }
        '''
    }
}
