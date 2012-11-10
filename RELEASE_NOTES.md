### Version 0.4.1 (SNAPSHOT)

* Better reporting of templates' compilation errors during `gaelykPrecompileTemplates` task - [Issue 23](https://github.com/bmuschko/gradle-gaelyk-plugin/issues/23)
* Inlined static templates' includes such as `<% include 'foo.gtpl' %>` - [Issue 10] - (https://github.com/bmuschko/gradle-gaelyk-plugin/issues/10)
* Added non-RAD mode in which `gaeRun` is executed against exploded war in contrary to being executed against `webAppDir` in the default RAD mode - [Issue 14](https://github.com/bmuschko/gradle-gaelyk-plugin/issues/14)
* Several bug fixes to the plugin when in RAD mode [Issue 16](https://github.com/bmuschko/gradle-gaelyk-plugin/issues/16), [Issue 22](https://github.com/bmuschko/gradle-gaelyk-plugin/issues/22)

### Version 0.4 (July 21, 2012)

* Add common boilerplate code from Gaelyk template project - [Issue 13](https://github.com/bmuschko/gradle-gaelyk-plugin/pull/13).
* Upgrade to Gradle Wrapper 1.0.

### Version 0.3.3 (June 10, 2012)

* Update the path matching pattern to work with the Windows path separator - [Issue 11](https://github.com/bmuschko/gradle-gaelyk-plugin/pull/11).
* Using Gradle Nexus plugin for publishing artifact.

### Version 0.3.2 (May 28, 2012)

* Remove hard-coded `WEB-INF/classes` as output dir - [Issue 6](https://github.com/bmuschko/gradle-gaelyk-plugin/pull/6).
* Updated documentation - [Issue 8](https://github.com/bmuschko/gradle-gaelyk-plugin/pull/8).

### Version 0.3.1 (April 1, 2012)

* Added tasks for Groovy template precompilation - [Issue 5](https://github.com/bmuschko/gradle-gaelyk-plugin/pull/5).

### Version 0.3 (November 20, 2011)

* Added task Groovlet precompilation - [Issue 4](https://github.com/bmuschko/gradle-gaelyk-plugin/pull/4).

### Version 0.2 (August 13, 2011)

* Added ability to list and install plugins from Gaelyk plugin catalogue.

### Version 0.1 (April 17, 2011)

* Initial release.
