# Gradle Gaelyk plugin

The plugin provides tasks for managing [Gaelyk](http://gaelyk.appspot.com/) projects in any given Gradle build. It extends
the War plugin.

## Usage

To use the Gaelyk plugin, apply the plugin to your build script:

    apply plugin: 'gaelyk'

The plugin JAR and the App Engine tools SDK library need to be defined in the classpath of your build script. You can
either get the plugin from the GitHub download section or upload it to your local repository. You'll also have to assign
the base directory of your web application (`war` by default). The following code snippet shows an example:

    buildscript {
	    repositories {
		    add(new org.apache.ivy.plugins.resolver.URLResolver()) {
    		    name = "GitHub"
    		    addArtifactPattern 'http://cloud.github.com/downloads/bmuschko/gradle-gaelyk-plugin/[module]-[revision].[ext]'
  		    }
            mavenCentral()
        }

	    dependencies {
            classpath ':gradle-gaelyk-plugin:0.1'
        }
    }

    webAppDirName = new File("war")

## Tasks

* `gaelykInstall plugin=<path>`: Installs plugin specified by given `path`. 
Plugin must be zip archive either on the file system or on the web.
Use `//` instead of `http://` to indicate that the plugin is located on the web.
 _Example:_ `gradle gaelykInstall plugin=//github.com/downloads/bmuschko/gaelyk-jsonlib-plugin/gaelyk-jsonlib-plugin-0.2.zip`
installs the [JSON plugin](https://github.com/bmuschko/gaelyk-jsonlib-plugin).

* `gaelykUninstall plugin=<path or name>`: Uninstalls plugin specified by given `path` or `name`. 
Path or name can be easily find using the `gaelykInstalled` task. The name is the name of the original file without the zip extension.
Use `//` instead of `http://` in path were necessary.
 _Example:_ `gradle gaelykUninstall plugin=//github.com/downloads/bmuschko/gaelyk-jsonlib-plugin/gaelyk-jsonlib-plugin-0.2.zip`
uninstalls the [JSON plugin](https://github.com/bmuschko/gaelyk-jsonlib-plugin).
`gradle gaelykUninstall plugin=gaelyk-jsonlib-plugin-0.2` would do the same work.

* `gaelykInstalled`: Shows what plugins are already installe using the `gaelykInstall` task


## Task Rules

* `gaelykCreateController<ControllerName>`: Creates a Gaelyk controller (Groovlet). Optionally, you can define the directory
to put the file in using the command line property `dir`. _Example:_ `gradle gaelykCreateControllerUser` creates the file
`user.groovy` in the directory `war/WEB-INF/groovy`.
* `gaelykCreateView<ViewName>`: Creates a Gaelyk view (Groovy template). Optionally, you can define the directory
to put the file in using the command line property `dir`. _Example:_ `gaelykCreateViewAddress -Pdir=address` creates the file
`address.gtpl` in the directory `war/address`.