package org.gradle.api.plugins.gaelyk.tasks

import com.google.appengine.AppEnginePlugin
import com.google.appengine.task.ExplodeAppTask
import directree.Synchronizer
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.WarPluginConvention
import org.gradle.api.tasks.TaskAction

/**
 * Created by ladin on 22.03.14.
 */
class GaelykSynchronizeResourcesTask extends DefaultTask {

    @TaskAction
    void startSync() {
        ExplodeAppTask explodeTask = project.tasks.getByName(AppEnginePlugin.APPENGINE_EXPLODE_WAR)

        if(explodeTask.getArchive().name.endsWith(".ear")) {
            project.logger.error("Gaelyk plugin cannot currently synchronize changes in ear projects. " +
                    "If you need synchronize files you need to do it manually or add Gaelyk plugin to the module" +
                    "and run it separely if possible.")
            return
        }

        final String appEngineGeneratedDir = "WEB-INF/appengine-generated/"
        final String appEngineGeneratedBkpDir = "data-backup"
        final String preserved = "WEB-INF/lib/*.jar WEB-INF/classes/** $appEngineGeneratedDir/** WEB-INF/web.xml WEB-INF/appengine-web.xml META-INF/MANIFEST.MF"
        final File webAppDir =  project.convention.getPlugin(WarPluginConvention).webAppDir


        Synchronizer.build {
            sourceDir  webAppDir.absolutePath
            targetDir  explodeTask.getExplodedAppDirectory().absolutePath, includeEmptyDirs: true
            preserve   includes: preserved, preserveEmptyDirs: true
            syncFrequencyInSeconds  3
        }.start()

        File syncedAppengineGeneratedDir    = new File(explodeTask.getExplodedAppDirectory(), appEngineGeneratedDir)
        File syncedAppengineGeneratedBkpDir = new File(project.projectDir, appEngineGeneratedBkpDir)

        syncedAppengineGeneratedDir.mkdirs()
        syncedAppengineGeneratedBkpDir.mkdirs()

        Synchronizer.build {
            sourceDir syncedAppengineGeneratedBkpDir.absolutePath
            targetDir syncedAppengineGeneratedDir.absolutePath
        }.sync()

        Synchronizer.build {
            sourceDir syncedAppengineGeneratedDir.absolutePath
            targetDir syncedAppengineGeneratedBkpDir.absolutePath, includeEmptyDirs: true

            syncFrequencyInSeconds 3
        }.start()

    }

}
