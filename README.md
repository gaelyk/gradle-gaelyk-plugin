# Gradle Gaelyk plugin

![Gaelyk Logo](http://d.hatena.ne.jp/images/keyword/283651.png)

The plugin provides tasks for managing [Gaelyk](http://www.gaelyk.org/) projects in any given Gradle build. It applies
[Gradle App Engine plugin](https://github.com/GoogleCloudPlatform/gradle-appengine-plugin) and
[Groovy plugin](http://www.gradle.org/docs/current/userguide/groovy_plugin.html) to the project.
Finally it adds some Gaelyk specific tasks.

Gaelyk plugin applies GAE plugin to provide tasks for running the application, uploading it to App Engine etc. - see
[Gradle App Engine plugin's documentation](https://github.com/GoogleCloudPlatform/gradle-appengine-plugin) for details.
To run Gaelyk application just use `appengineRun` task as in any other App Engine website. The changes in source are
synchronized autmoatically for the websites. The plugin does not support synchronization for EAR modules yet since the
Gradle App Engine plugin doesn't handles these situations gracefully yet.


## Release Notes

### 0.7.0

This version is the first officially compatible with Gradle 2.x branch and it's new plugin mechanisms. 

**Breaking Change:** The name of generated GTPLs now escapes to `_gtpl_` instead of `$gtpl$`.
This change is reflected in Gaelyk 2.2.0 and later which has to be used to be able to find the precompiled templates.


## Usage

### Gradle 2.1 and Later
To use the Gaelyk plugin, apply the plugin to your build script:
```
plugins {
  id "org.gaelyk" version "0.7.0"
}
```

### Before Gradle 2.1
To use the Gaelyk plugin, apply the plugin to your build script:

    apply plugin: 'org.gaelyk'

The plugin JAR needs to be defined in the classpath of your build script. It is directly available on
[Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.gradle.api.plugins%22%20AND%20a%3A%22gradle-gaelyk-plugin%22).
Alternatively, you can download it from GitHub and deploy it to your local repository. The following code snippet shows an example on how to retrieve
it from Maven Central:

    buildscript {
        repositories {
            mavenCentral()
        }

        dependencies {
            classpath 'org.gradle.api.plugins:gradle-gaelyk-plugin:0.7'
        }
    }

## Convention properties

The Gaelyk plugin defines the following convention properties in the `gaelyk` closure:

* `templateExtension`: the extension of the templates which should be precompiled

## Integration with other Gradle plugins
* GAE plugin's `dowloadSdk` property is set to true by default

## Dependencies you need to specify
When applying gaelyk plugin to your project remember that you have to specify the folowing dependencies:
* `compile` - with groovy library specified
* `appengineSdk` - because GAE plugin requires it

To be able to develop a Gaelyk application you also need to add a `compile` dependency on Gaelyk. The dependencies
section of your build might look like this:

    dependencies {
        compile 'org.codehaus.groovy:groovy-all:2.4.3'
        appengineSdk "com.google.appengine:appengine-java-sdk:1.9.20"
        compile 'org.gaelyk:gaelyk:2.1'
    }

## Tasks
* `gaelykConvertTemplates`: Prepares templates for precompilation by converting them to scripts.
* `gaelykPrecompileTemplates`: Precompiles Groovy templates to minimize startup costs. All static templates' includes such as `<% include 'foo.gtpl' %>` are inlined for better performance.
* `gaelykSynchronizeResources`: Synchronizes changes from source directories to exploded app ones. Only web projects are supported at the moment.

## Task Rules

* `gaelykCreateController<ControllerName>`: Creates a Gaelyk controller (Groovlet). Optionally, you can define the directory
to put the file in using the command line property `dir`. _Example:_ `gradle gaelykCreateControllerUser` creates the file
`user.groovy` in the directory `war/WEB-INF/groovy`.
* `gaelykCreateView<ViewName>`: Creates a Gaelyk view (Groovy template). Optionally, you can define the directory
to put the file in using the command line property `dir`. _Example:_ `gaelykCreateViewAddress -Pdir=address` creates the file
`address.gtpl` in the directory `war/address`.
