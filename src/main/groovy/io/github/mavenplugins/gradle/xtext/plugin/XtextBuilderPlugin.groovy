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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.github.mavenplugins.gradle.xtext.plugin.dsl.LanguageDSL
import io.github.mavenplugins.gradle.xtext.plugin.dsl.OutputConfigurationDSL
import io.github.mavenplugins.gradle.xtext.plugin.dsl.SourceSetDSL
import io.github.mavenplugins.gradle.xtext.plugin.tasks.GenerateXtextTask
import io.github.mavenplugins.gradle.xtext.plugin.utils.DependencyUtil
import io.github.mavenplugins.gradle.xtext.plugin.utils.PluginResourcesUtil
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider

@CompileStatic
class XtextBuilderPlugin implements Plugin<Project> {
    private static final String XTEXTBUILDER_GROUP = 'XtextBuilder'
    private static final String XTEXT_STANDALONE_CONFIGURATION_NAME = 'xtextStandalone'
    private static final String XTEXT_COMPILE_CONFIGURATION_NAME = 'xtextCompile'

    private Configuration xtextStandalone
    private Configuration xtextCompile

    private Logger logger

    @Override
    void apply(Project project) {
        logger = project.logger
//        if (project.gradle.startParameter.logLevel != LogLevel.QUIET) {
//            project.gradle.sharedServices
//                .registerIfAbsent('jreleaser-banner', Banner, { spec -> })
//                .get().display(project)
//        } else {
//            System.setProperty(JRELEASER_QUIET, 'true')
//        }
        if (project.pluginManager.hasPlugin('org.xtext.builder') || project.pluginManager.hasPlugin('org.xtext.xtend')) {
            throw new IllegalStateException("The 'org.xtext.builder' or 'org.xtext.xtend' plugin cannot be applied together with the '${PluginResourcesUtil.pluginId}' plugin. " +
                    "A project must either use either 'org.xtext.builder' or '${PluginResourcesUtil.pluginId}' but cannot use both at the same time.")
        }

        // TODO - check if needed
//        Provider<String> nameProvider = project.provider({ -> project.name })
//        Provider<String> descriptionProvider = project.provider({ -> project.description })
//        Provider<String> versionProvider = project.provider({ -> String.valueOf(project.version) })
        // TODO - check if needed
//        project.plugins.apply(JavaPlugin)
//        project.plugins.apply("base")
//        project.plugins.apply(JvmEcosystemPlugin)
        project.extensions.create(XtextBuilderPluginExtension, 'xtextBuilder', XtextBuilderPluginExtension,
                project.objects, project.layout)
        xtextStandalone = project.configurations.maybeCreate(XTEXT_STANDALONE_CONFIGURATION_NAME)
        xtextCompile = project.configurations.maybeCreate(XTEXT_COMPILE_CONFIGURATION_NAME)
        registerTasks(project)

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {
                XtextBuilderPluginExtension extension = p.extensions.findByType(XtextBuilderPluginExtension)

                //if (!extension.enabled.get()) return
                extension.validate()

                configureXtextConfigurations(p, extension)

                if (hasKordampBasePluginApplied(p)) {
                    registerAllProjectsEvaluatedListener(p)
                } else {
                    configureXtextBuilder(p)
                }
            }
        })
    }

    private void configureXtextConfigurations(Project project, XtextBuilderPluginExtension extension) {
        // Enable configurations to resolve dependency variants - like e.g. guava
        String targetJvmEnvironment = extension.targetJvmEnvironment.get()
        configureXtextConfiguration(xtextStandalone, targetJvmEnvironment, project.objects)
        configureXtextConfiguration(xtextCompile, targetJvmEnvironment, project.objects)
        configureXtextStandaloneConfiguration(project, extension)
    }

    private void configureXtextConfiguration(Configuration configuration, String targetJvmEnvironment, ObjectFactory objects) {
        // Use internally only - cannot be consumed by other projects - and not visible in IDEs as a user consumable configuration
        configuration.setCanBeConsumed(false)
        // Not intended to be extended by user configurations - but can be extended by other plugin configurations if needed
        configuration.attributes {
            it.attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                    objects.named(TargetJvmEnvironment, targetJvmEnvironment))
            it.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EXTERNAL))
            it.attribute(org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE,
                    objects.named(org.gradle.api.attributes.Category, org.gradle.api.attributes.Category.LIBRARY))
            it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                    objects.named(LibraryElements, LibraryElements.JAR))
            it.attribute(Usage.USAGE_ATTRIBUTE,
                    objects.named(Usage, Usage.JAVA_RUNTIME))
        }
    }

    private void configureXtextBuilder(Project project) {
        configureTasks(project)
    }

    private boolean hasKordampBasePluginApplied(Project project) {
        project.rootProject.plugins.findPlugin('org.kordamp.gradle.base')
        // enforce to false for now to avoid the need to depend on Kordamp Base plugin and to be able to test the plugin without it
        false
    }

    @CompileDynamic
    private void registerAllProjectsEvaluatedListener(Project project) {
        Class c = Class.forName('com.rockwell.gradle.xtext.plugin.internal.XtextBuilderAllProjectsEvaluatedListener')
        def listener = c.getConstructor().newInstance()
        listener.runnable = { ->
            Class.forName('org.jreleaser.gradle.plugin.internal.KordampJReleaserAdapter')
                    .adapt(project)
            configureXtextBuilder(project)
        }

        Class m = Class.forName('org.kordamp.gradle.listener.ProjectEvaluationListenerManager')
        m.addAllProjectsEvaluatedListener(project, listener)
    }

    @CompileDynamic
    private void configureXtextStandaloneConfiguration(Project project, XtextBuilderPluginExtension extension) {
        String xtextVersion = extension.xtextVersion.get()
        List<String> xtextStandaloneDependencies =
                [
                        "org.eclipse.xtext:org.eclipse.xtext.builder.standalone:${xtextVersion}", // compile
                        "org.eclipse.xtext:org.eclipse.xtext.common.types:${xtextVersion}", // compile (transient of org.eclipse.xtext.builder.standalone)
                        "org.eclipse.xtext:org.eclipse.xtext.ecore:${xtextVersion}", // runtime
                        "org.eclipse.xtext:org.eclipse.xtext:${xtextVersion}", // compile (transient of org.eclipse.xtext.common.types)
                        "org.eclipse.xtext:org.eclipse.xtext.util:${xtextVersion}", // compile (transient of org.eclipse.xtext)
                        "org.eclipse.xtext:org.eclipse.xtext.xtext.generator:${xtextVersion}", // runtime
                        "org.eclipse.xtext:org.eclipse.xtext.smap:${xtextVersion}", // runtime
                        "org.eclipse.xtext:org.eclipse.xtext.xbase.lib:${xtextVersion}", // compile (transient of org.eclipse.xtend.lib)
                        "org.eclipse.xtext:org.eclipse.xtext.xbase:${xtextVersion}", // compile (transient of org.eclipse.xtext.builder.standalone)
                        "org.eclipse.xtend:org.eclipse.xtend.lib:${xtextVersion}", // compile (transient of org.eclipse.xtext)
                        "org.eclipse.xtend:org.eclipse.xtend.lib.macro:${xtextVersion}", // compile (transient of org.eclipse.xtend.lib)
                ]
        xtextStandaloneDependencies.addAll(
                DependencyUtil.resolveBomManagedDependencies(
                        "org.eclipse.xtext:xtext-dev-bom:${xtextVersion}",
                        project.dependencies,
                        project.configurations,
                        logger)
        )
        if (!PluginResourcesUtil.isPluginIntegrationTestRuntime()) {
            // Add this plugin as a dependency if not running in an integration test runtime of this plugin itself.
            String pluginDependencyNotation = "${PluginResourcesUtil.pluginGroupId}:${PluginResourcesUtil.pluginArtifactId}:${PluginResourcesUtil.pluginVersion}"
            xtextStandaloneDependencies.add(pluginDependencyNotation)
        }
        xtextStandaloneDependencies.each { String notation ->
            project.dependencies.add(xtextStandalone.name, notation, { ModuleDependency dependency ->
                logger.info("Adding xtext-dev-bom dependency: ${dependency}")
                if (dependency.group != 'com.google.inject') {
                    dependency.transitive = false
                }
            })
        }
//        xtextStandalone.resolutionStrategy.eachDependency {
//            if (it.requested.group == 'org.eclipse.xtext' || it.requested.group == 'org.eclipse.xtend') {
//                it.useVersion(xtextVersion)
//            }
//        }
    }

    private void registerTasks(Project project) {
        project.tasks.register(GenerateXtextTask.NAME, GenerateXtextTask,
                new Action<GenerateXtextTask>() {
                    @Override
                    void execute(GenerateXtextTask t) {
                        t.group = XTEXTBUILDER_GROUP
                        t.description = 'Generate classes for Xtext languages'
                    }
                })
    }

    private void configureTasks(Project project) {
        XtextBuilderPluginExtension extension = project.extensions.findByType(XtextBuilderPluginExtension)
        project.tasks.withType(GenerateXtextTask).configureEach { GenerateXtextTask t ->
            t.xtextStandaloneClasspath.from(xtextStandalone.filter { File it -> it.name.endsWith('.jar') })
            if (PluginResourcesUtil.isPluginIntegrationTestRuntime()) {
                logger.info("###### Plugin integration test runtime determined.")
                addProjectBuildClassPathsIfIntegrationTestRuntime(t.xtextStandaloneClasspath)
            }
            t.xtextCompileClasspath.from(xtextCompile.filter { File it -> it.name.endsWith('.jar') })
            // Collect all Java srcDirs from all SourceSetDSL entries lazily
            Provider<List<Directory>> allJavaSrcDirs = project.providers.provider {
                extension.javaSourceSets
                        .collect { SourceSetDSL sourceSet -> sourceSet.srcDirs.get() }
                        .flatten() as List<Directory>
            }
            t.javaSourceDirectories.from(allJavaSrcDirs)
            // Collect all Xtext srcDirs from all SourceSetDSL entries lazily
            Provider<List<Directory>> allXtextSrcDirs = project.providers.provider {
                extension.sourceSets
                        .collect { SourceSetDSL sourceSet -> sourceSet.srcDirs.get() }
                        .flatten() as List<Directory>
            }
            t.xtextSourceDirectories.from(allXtextSrcDirs)
            // Collect all outputDirectory values from all outputConfigurations of all languages lazily
            Provider<List<Directory>> allOutputDirs = project.providers.provider {
                extension.languages
                        .collect { LanguageDSL lang ->
                            lang.outputConfigurations
                                    .collect { OutputConfigurationDSL oc -> oc.outputDirectory }
                                    .findAll { it.present }
                                    .collect { it.get() }
                        }
                        .flatten() as List<Directory>
            }
            t.xtextOutputDirectories.from(allOutputDirs)
            // Scratch space: build/tmp/<taskName> - follows Gradle's own convention,
            // cleaned by 'clean', unique per task, no random suffix
            t.tempDirectory.convention(
                    project.layout.buildDirectory.dir("tmp/${t.NAME}")
            )
        }
    }

    private void addProjectBuildClassPathsIfIntegrationTestRuntime(ConfigurableFileCollection classPath) {
        final URL pluginBuildURL = new File(PluginResourcesUtil.getPluginBuildDirectory()).toURI().toURL()
        final URLClassLoader classLoader = getClass().getClassLoader() as URLClassLoader
        classLoader.getURLs().each { URL url ->
            if (url.file.startsWith("${pluginBuildURL.file}classes/")
                    || url.file.startsWith("${pluginBuildURL.file}resources/")) {
                logger.info("###### Integration test: adding project build URL to classpath: ${url.file}")
                classPath.from(new File(url.file))
            }
        }
    }

}
