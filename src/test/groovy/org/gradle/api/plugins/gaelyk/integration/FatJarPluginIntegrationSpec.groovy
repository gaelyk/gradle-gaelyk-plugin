package org.gradle.api.plugins.gaelyk.integration

import static org.gradle.api.plugins.gae.GaePlugin.*
import static eu.appsatori.gradle.fatjar.FatJarPlugin.*

class FatJarPluginIntegrationSpec extends IntegrationSpec {
    def 'jar optimization tasks are skipped if we are executing gaeRun in rad mode'() {
        given:
        skipGaeRun()

        when:
        runTasks(GAE_RUN)

        then:
        [FATJAR_PREPARE_FILES, FATJAR_FAT_JAR, FATJAR_SLIM_WAR].every {
            task(it).state.skipped
        }
    }

    def 'jar optimization tasks are not skipped if we are executing gaeRun in non-rad mode'() {
        given:
        skipGaeRun()
        nonRadMode()

        when:
        runTasks(GAE_RUN)

        then:
        [FATJAR_PREPARE_FILES, FATJAR_FAT_JAR, FATJAR_SLIM_WAR].every {
            task(it).state.didWork
        }
    }

    def 'jar optimization tasks are not skipped if not executing gaeRun'() {
        when:
        runTasks(GAE_EXPLODE_WAR)

        then:
        [FATJAR_PREPARE_FILES, FATJAR_FAT_JAR, FATJAR_SLIM_WAR].every {
            task(it).state.didWork
        }
    }
}
