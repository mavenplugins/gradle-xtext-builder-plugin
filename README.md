# Gradle Xtext Standalone Builder Plugin

[![Apache License](https://img.shields.io/github/license/mavenplugins/gradle-xtext-builder-plugin?label=License)](./LICENSE)
[![CI](https://github.com/mavenplugins/gradle-xtext-builder-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/mavenplugins/gradle-xtext-builder-plugin/actions/workflows/build.yml)
[![Maven Central Snapshot](https://img.shields.io/maven-metadata/v?label=Maven%20Central%20Snapshot&metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgithub%2Fmavenplugins%2Fgradle%2Fxtext-standalone-builder%2Fmaven-metadata.xml)](https://central.sonatype.com/repository/maven-snapshots/io/github/mavenplugins/gradle/xtext-standalone-builder/maven-metadata.xml)
[![Maven Central Release](https://img.shields.io/maven-central/v/io.github.mavenplugins.gradle/xtext-standalone-builder?label=Maven%20Central%20Release)](https://search.maven.org/artifact/io.github.mavenplugins.gradle/xtext-standalone-builder)

Gradle plugin to generate sources from a Xtext language definition. This plugin is making use of the Xtext Standalone Builder API.<br>
The implementation is adequate to the original [Xtext Maven plugin](https://github.com/eclipse-xtext/xtext/tree/main/org.eclipse.xtext.maven.plugin).<br>
It leverages Gradle incremental build features to generate sources only when the Xtext language definition or its dependencies have changed.

## Usage

<!-- This plugin is hosted on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.mavenplugins.xtext-standalone-builder). -->
This plugin is hosted
on [Maven Central](https://repo1.maven.org/maven2/io/github/mavenplugins/gradle/xtext-standalone-builder).
To use the plugin, add the following to your `build.gradle` file.

    plugins {
      id 'io.github.mavenplugins.gradle.xtext-standalone-builder' version '0.1.0-SNAPSHOT'
    }

## Configuration

TBD
