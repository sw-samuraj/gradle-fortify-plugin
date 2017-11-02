# Gradle Fortify plugin #

[![Build Status](https://travis-ci.org/sw-samuraj/gradle-fortify-plugin.svg?branch=master)](https://travis-ci.org/sw-samuraj/gradle-fortify-plugin)

A Gradle plugin for building of Fortify artifacts for static security analysis.

## Applying the plugin ##

### Gradle 2.1+ ###

```groovy
plugins {
    id "cz.swsamuraj.fortify" version "0.3.0"
}
```
### All Gradle versions (or local repository) ##

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "gradle.plugin.cz.swsamuraj:gradle-fortify-plugin:0.3.0"
    }
}

apply plugin: "cz.swsamuraj.fortify"
```

## Using the plugin ##

### Prerequisities ###

**Java plugin**

The plugin is meant for analysis of Java source code. Therefore expects an application of the *Java plugin* and
by default is processing Java *source sets*, excluding *test* source code.

Moreover, it re-uses `sourceCompatibility` property inherited from the *Java plugin*.

**sourceanalyzer**

The plugin requires that you have a local installation of the `sourceanalyzer` tool and that
this command is available on `$PATH`.

### Tasks ###

**fortify**

`fortify` task runs the following commands:

```shell
sourceanalyzer -b <Fortify build ID> -clean
sourceanalyzer -b <Fortify build ID> -source <source compatibility> -cp <project compile classpath> src/**/*.java -exclude src/test/**/*.java
sourceanalyzer -b <Fortify build ID> -build-label <project version> -export-build-session build/fortify/<Fortify build ID>@<project version>.mbs
sourceanalyzer -b <Fortify build ID> -scan -f build/fortify/<project-name>-<project-version>.fpr
```

Result of this task will be a `<project-name>-<project-version>.fpr` file, located in the `build/fortify` directory.
The `<project-name>-<project-version>.fpr` file can be then uploaded to *Fortify Security Center* via `scp`, or *Jenkins*.

### Config options ###

**fortifyBuildID**

There must be a `fortify` part in the `build.gradle` file which defines a mandatory parameter `fortifyBuildID`.

```groovy
fortify {
    fortifyBuildID = 'my-fort-proj'
}
```

**sourceCompatibility**

The `sourceCompatibility` property is inherited from the *Java plugin*. It can be explicitly set via standard *Java
plugin* configuration:

```groovy
plugins {
    id 'java'
}

sourceCompatibility = 1.8
```

## Example ##

Usage of the plugin and example project can be found in the `example` directory.

## License ##

The **gradle-fortify-plugin** is published under [BSD 3-Clause](http://opensource.org/licenses/BSD-3-Clause) license.
