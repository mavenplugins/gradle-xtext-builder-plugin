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

package io.github.mavenplugins.gradle.xtext.plugin

import groovy.transform.CompileStatic
import io.github.mavenplugins.gradle.xtext.plugin.dsl.LanguageDSL
import io.github.mavenplugins.gradle.xtext.plugin.dsl.SourceSetDSL
import io.github.mavenplugins.gradle.xtext.plugin.utils.PluginResourcesUtil
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested

import javax.inject.Inject

@CompileStatic
abstract class XtextBuilderPluginExtension {

    @Internal
    final List<String> defaultGradleClassLoaderIncludes = [
            'groovy.lang',
            'javax',
            'org.apache.commons.logging',
            'org.apache.log4j',
            'org.codehaus.groovy',
            'org.gradle',
            'org.slf4j',
            PluginPackage.PACKAGE_NAME
    ]
    List<String> getDefaultGradleClassLoaderIncludes() {
        return defaultGradleClassLoaderIncludes.asImmutable()
    }

    @Internal
    final List<String> defaultGradleClassLoaderExcludes = []
    List<String> getDefaultGradleClassLoaderExcludes() {
        return defaultGradleClassLoaderExcludes.asImmutable()
    }

    /**
     * ProjectLayout is a Gradle service type — NOT a managed property type.
     * It must be injected via constructor and stored as a field.
     * @Internal excludes it from the task input fingerprint.
     * The configuration cache handles ProjectLayout safely when accessed via its interface.
     */
    @Internal
    final ProjectLayout layout

    @Input
    final Property<String> xtextVersion

    @Input
    final ListProperty<String> gradleClassLoaderIncludes

    @Input
    final ListProperty<String> gradleClassLoaderExcludes

    @Input
    final Property<String> compilerSourceLevel

    @Input
    final Property<String> compilerTargetLevel

    @Input
    final Property<String> targetJvmEnvironment

    @Input
    final Property<Boolean> failOnValidationError

    @Input
    final Property<Boolean> incrementalBuild

    @Inject
    XtextBuilderPluginExtension(ObjectFactory objects, ProjectLayout layout) {
        this.layout = layout
        xtextVersion = objects.property(String).convention(PluginResourcesUtil.xtextMinVersion)
        gradleClassLoaderIncludes = objects.listProperty(String).convention(getDefaultGradleClassLoaderIncludes())
        gradleClassLoaderExcludes = objects.listProperty(String).convention(getDefaultGradleClassLoaderExcludes())
        compilerSourceLevel = objects.property(String).convention(PluginResourcesUtil.defaultCompilerSourceLevel)
        compilerTargetLevel = objects.property(String).convention(PluginResourcesUtil.defaultCompilerTargetLevel)
        targetJvmEnvironment = objects.property(String).convention(TargetJvmEnvironment.STANDARD_JVM)
        failOnValidationError = objects.property(Boolean).convention(true)
        incrementalBuild = objects.property(Boolean).convention(false)
    }


    @Nested
    abstract NamedDomainObjectContainer<LanguageDSL> getLanguages()

    void languages(Action<? extends NamedDomainObjectContainer<LanguageDSL>> action) {
        action.execute(languages)
    }

    @Nested
    abstract NamedDomainObjectContainer<SourceSetDSL> getJavaSourceSets()

    void javaSourceSets(Action<NamedDomainObjectContainer<SourceSetDSL>> action) {
        action.execute(javaSourceSets)
    }

    @Nested
    abstract NamedDomainObjectContainer<SourceSetDSL> getSourceSets()

    void sourceSets(Action<NamedDomainObjectContainer<SourceSetDSL>> action) {
        action.execute(sourceSets)
    }

    void validate() {
        validateXtextVersion()
        validateJavaSourceSets()
        validateSourceSets()
        validateLanguages()
    }

    private void validateXtextVersion() {
        String xtextVersionConfigured = xtextVersion.get()
        String minimumXtextVersion = PluginResourcesUtil.xtextMinVersion
        if (new ComparableVersion(xtextVersionConfigured) < new ComparableVersion(minimumXtextVersion)) {
            throw new GradleException("Xtext version ${xtextVersionConfigured} is not supported. The minimum version is ${minimumXtextVersion}.")
        }
        if (!xtextVersionConfigured) {
            throw new IllegalStateException("Xtext version must be specified and non-empty.")
        }
    }

    private void validateJavaSourceSets() {
        // nothing to do yet - we allow zero java source sets and do not require any specific configuration for them
    }

    private void validateSourceSets() {
        // nothing to do yet - we allow zero source sets and do not require any specific configuration for them
    }

    private void validateLanguages() {
        languages.each( {LanguageDSL language ->
            if (!language.setup.isPresent()) {
                throw new IllegalStateException("Xtext setup class must be defined for language '${language.name}'.")
            }
        })
    }
}
