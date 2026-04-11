/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The XtextBuilder authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mavenplugins.gradle.xtext.plugin.utils

import groovy.transform.CompileDynamic
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.logging.Logger

import java.util.regex.Matcher
import java.util.regex.Pattern

class DependencyUtil {
    private DependencyUtil() {
        // prevent instantiation
    }

    static String toDependencyNotation(String group, String name, String version = null) {
        return "${group}:${name}${version?":${version}":''}"
    }

    @CompileDynamic
    static List<String> resolveBomManagedDependencies(
            final String bomNotation,
            final DependencyHandler dependencies,
            final ConfigurationContainer configurations,
            final Logger logger) {
        final List<String> managedDeps = []

        try {
            // Create a detached configuration to resolve the BOM POM file
            final Dependency bomDependency = dependencies.create(bomNotation.endsWith('@pom') ? bomNotation : bomNotation + '@pom')
            final Configuration bomConfig = configurations.detachedConfiguration(bomDependency)

            // Resolve to get the actual POM file
            final Set<File> bomFiles = bomConfig.resolve()
            if (bomFiles.isEmpty()) {
                logger.warn("Could not resolve BOM POM file for: ${bomNotation}")
                return managedDeps
            }

            final File pomFile = bomFiles.first()
            logger.info("Parsing BOM POM file: ${pomFile}")

            // Parse the POM XML to extract dependencyManagement section
            GPathResult pom = new XmlSlurper().parse(pomFile)

            // Extract managed dependencies from dependencyManagement section
            pom.dependencyManagement.dependencies.dependency.each { dep ->
                String group = dep.groupId.text()
                String name = dep.artifactId.text()
                String version = dep.version.text()

                // Resolve property references in version (e.g., ${project.version})
                if (version.contains('${')) {
                    version = resolveMavenProperty(version, pom, logger)
                }

                if (group && name && version) {
                    String notation = "${group}:${name}:${version}"
                    managedDeps.add(notation)
                    logger.debug("BOM manages: ${notation}")
                }
            }

            logger.info("Extracted ${managedDeps.size()} managed dependencies from BOM")

        } catch (Exception e) {
            logger.warn("Failed to parse BOM ${bomNotation}: ${e.message}", e)
        }

        return managedDeps
    }

    @CompileDynamic
    private static String resolveMavenProperty(final String value, final GPathResult pom, final Logger logger) {
        if (!value.contains('${')) {
            return value
        }

        String result = value

        // Common property patterns
        Pattern propertyPattern = ~/\$\{([^}]+)\}/
        Matcher matcher = propertyPattern.matcher(value)

        while (matcher.find()) {
            def propertyName = matcher.group(1)
            def propertyValue = null

            // Check for common properties
            if (propertyName == 'project.version' || propertyName == 'pom.version') {
                propertyValue = pom.version.text()
            } else if (propertyName == 'project.groupId' || propertyName == 'pom.groupId') {
                propertyValue = pom.groupId.text()
            } else {
                // Look in properties section
                def propNode = pom.properties."${propertyName}"
                if (propNode) {
                    propertyValue = propNode.text()
                }
            }

            if (propertyValue) {
                result = result.replace("\${${propertyName}}", propertyValue)
            } else {
                logger.debug("Could not resolve property: ${propertyName}")
            }
        }

        return result
    }

}
