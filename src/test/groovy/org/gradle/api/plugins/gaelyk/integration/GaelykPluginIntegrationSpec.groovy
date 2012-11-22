package org.gradle.api.plugins.gaelyk.integration

import static org.gradle.api.plugins.gaelyk.GaelykPluginBase.getGROOVLET_DIRECTORY_RELATIVE_PATH

abstract class GaelykPluginIntegrationSpec extends IntegrationSpec {

    def setup() {
        directory("$DEFAULT_WEB_APP_PATH/$GROOVLET_DIRECTORY_RELATIVE_PATH")

        buildFile << """
            def GaelykPlugin = project.class.classLoader.loadClass('org.gradle.api.plugins.gaelyk.GaelykPlugin')

            apply plugin: GaelykPlugin

            dependencies {
                groovy 'org.codehaus.groovy:groovy-all:1.8.6'
                compile 'org.gaelyk:gaelyk:1.2'
                gaeSdk "com.google.appengine:appengine-java-sdk:1.6.6"
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

    protected radMode(rad) {
        buildFile << """
            gaelyk {
                rad = $rad
            }
        """
    }

    protected nonRadMode() {
        radMode false
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
