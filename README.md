# Gradle Gaelyk plugin [![Build Status](https://buildhive.cloudbees.com/job/bmuschko/job/gradle-gaelyk-plugin/badge/icon)](https://buildhive.cloudbees.com/job/bmuschko/job/gradle-gaelyk-plugin/)

![Gaelyk Logo](http://d.hatena.ne.jp/images/keyword/283651.png)

The plugin provides tasks for managing [Gaelyk](http://gaelyk.appspot.com/) projects in any given Gradle build. It applies
[Gradle GAE plugin](https://github.com/bmuschko/gradle-gae-plugin) and 
[Groovy plugin](http://www.gradle.org/docs/current/userguide/groovy_plugin.html) to the project. It changes the 
configuration of [FatJAR](https://github.com/musketyr/gradle-fatjar-plugin) and GAE plugins and of main source set 
to better fit Gaelyk application needs. Finally it adds some Gaelyk specific tasks.

Gaelyk plugin applies GAE plugin to provide tasks for running the application, uploading it to GAE etc. - see
[Gradle GAE plugin's documentation](https://github.com/bmuschko/gradle-gae-plugin) for details.


## Usage

To use the Gaelyk plugin, apply the plugin to your build script:

    apply plugin: 'gaelyk'

The plugin JAR needs to be defined in the classpath of your build script. It is directly available on
[Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.gradle.api.plugins%22%20AND%20a%3A%22gradle-gaelyk-plugin%22).
Alternatively, you can download it from GitHub and deploy it to your local repository. The following code snippet shows an example on how to retrieve
it from Maven Central:

    buildscript {
        repositories {
            jcenter()
            mavenCentral()
        }

        dependencies {
            classpath 'org.gradle.api.plugins:gradle-gaelyk-plugin:0.6'
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
        compile 'org.codehaus.groovy:groovy-all:2.2.2'
        gaeSdk "com.google.appengine:appengine-java-sdk:1.9.1"
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
