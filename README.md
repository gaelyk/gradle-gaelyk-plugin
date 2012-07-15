# Gradle Gaelyk plugin

![Gaelyk Logo](http://d.hatena.ne.jp/images/keyword/283651.png)

The plugin provides tasks for managing [Gaelyk](http://gaelyk.appspot.com/) projects in any given Gradle build. It applies
[Gradle GAE plugin](https://github.com/bmuschko/gradle-gae-plugin) to the project. It changes the configuration of 
[FatJAR](https://github.com/musketyr/gradle-fatjar-plugin) and GAE plugins and of main source set to better fit Gaelyk
application needs. Finally it adds some Gaelyk specific tasks.


## Usage

To use the Gaelyk plugin, apply the plugin to your build script:

    apply plugin: 'gaelyk'

The plugin JAR needs to be defined in the classpath of your build script. It is directly available on
[Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.gradle.api.plugins%22%20AND%20a%3A%22gradle-gaelyk-plugin%22).
Alternatively, you can download it from GitHub and deploy it to your local repository. You'll also have to assign
the base directory of your web application (`war` by default). The following code snippet shows an example on how to retrieve
it from Maven Central:

    buildscript {
        repositories {
            mavenCentral()
        }

        dependencies {
            classpath 'bmuschko:gradle-gaelyk-plugin:0.3.3'
        }
    }

    webAppDirName = new File("war")

## Integration with other Gradle plugins
* Gaelyk plugin uses `webAppDir` convention property of [War plugin](http://gradle.org/docs/current/userguide/war_plugin.html)
(which is applied to the project by GAE plugin) to determine the location of project's webapp dir. The default location 
is `src/main/webapp` and can be changed using `webAppDirName` convention property of War plugin.
* GAE plugin's `warDir` convention property is set to the value of `webAppDir` War plugin's property. Please use
`webAppDirName` convention property of War plugin to change `warDir` property of GAE plugin.
* GAE plugin's `dowloadSdk` property is set to true by default
* GAE plugin's `optimizeWar` property is set to true by default
* FATJar plugin's tasks are skipped when starting development server as the server works against webapp dir

## Modifications to project configuration
* Main source set output directory is set to `<webAppDir>/WEB-INF/classes` - this is required by Gaelyk to run development
server against webapp dir which in turn enables reloading of changes to groovlets and templayes code 
without restarting the server

## Tasks
* `gaelykListPlugins`: Shows all available plugins from the Gaelyk plugins catalogue.
* `gaelykInstallPlugin`: Installs plugin provided by the command line property `plugin`. Plugin must be plugin identificator from the catalgoue or ZIP
archive either on the file system or on the web.
 _Example:_ `gradle gaelykInstallPlugin -Pplugin=http://cloud.github.com/downloads/bmuschko/gaelyk-jsonlib-plugin/gaelyk-jsonlib-plugin-0.2.zip`
installs the [JSON plugin](https://github.com/bmuschko/gaelyk-jsonlib-plugin).
* `gaelykUninstallPlugin`: Uninstalls plugin specified by given `path` or `name` provided by the
 command line property `plugin`. Path or name can easily be determined by running the `gaelykListInstalledPlugins` task.
 The name is the name of the original file without the ZIP extension.
 _Example:_ `gradle gaelykUninstallPlugin -Pplugin=http://cloud.github.com/downloads/bmuschko/gaelyk-jsonlib-plugin/gaelyk-jsonlib-plugin-0.2.zip`
uninstalls the [JSON plugin](https://github.com/bmuschko/gaelyk-jsonlib-plugin). `gradle gaelykUninstallPlugin -Pplugin=gaelyk-jsonlib-plugin-0.2` would do the same work.
* `gaelykListInstalledPlugins`: Shows plugins that have been installed by the `gaelykInstallPlugin` task.
* `gaelykPrecompileGroovlet`: Precompiles Groovlets to minimize startup costs. If your scripts reside in any subfolder don't forget to declare the script's `package` correspondingly.
* `gaelykPrecompileTemplate`: Precompiles Groovy templates to minimize startup costs.
* `gaelykCopyRuntimeLibraries`: Synchronises runtime libraries in webapp directory.
## Task Rules

* `gaelykCreateController<ControllerName>`: Creates a Gaelyk controller (Groovlet). Optionally, you can define the directory
to put the file in using the command line property `dir`. _Example:_ `gradle gaelykCreateControllerUser` creates the file
`user.groovy` in the directory `war/WEB-INF/groovy`.
* `gaelykCreateView<ViewName>`: Creates a Gaelyk view (Groovy template). Optionally, you can define the directory
to put the file in using the command line property `dir`. _Example:_ `gaelykCreateViewAddress -Pdir=address` creates the file
`address.gtpl` in the directory `war/address`.