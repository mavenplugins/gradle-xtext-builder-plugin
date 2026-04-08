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
import io.github.mavenplugins.gradle.xtext.plugin.utils.PluginResourcesUtil
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider

@CompileStatic
class XtextBuilderPlugin implements Plugin<Project> {
    private static final String XTEXTBUILDER_GROUP = 'XtextBuilder'
    private static final String XTEXT_STANDALONE_CONFIGURATION_NAME = 'xtextStandalone'
    private static final String XTEXT_COMPILER_CONFIGURATION_NAME = 'xtextCompiler'

    private Configuration xtextStandalone
    private Configuration xtextCompiler

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
                    "A project must either use either 'org.xtext.builder' or '${PluginResourcesUtil.pluginId}' but cannot use both at the same time.");
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
        xtextStandalone = project.configurations.create(XTEXT_STANDALONE_CONFIGURATION_NAME)
        xtextCompiler = project.configurations.create(XTEXT_COMPILER_CONFIGURATION_NAME)

        registerTasks(project)

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {
                XtextBuilderPluginExtension extension = p.extensions.findByType(XtextBuilderPluginExtension)
                //if (!extension.enabled.get()) return
                extension.validate()

                configureXtextStandaloneConfiguration(p)
                //addClasspathToClassloader(xtextStandalone)

                if (hasKordampBasePluginApplied(p)) {
                    registerAllProjectsEvaluatedListener(p)
                } else {
                    configureXtextBuilder(p)
                }
            }
        })
    }

    private void configureXtextBuilder(Project project) {
        configureTasks(project, xtextStandalone, xtextCompiler)
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

    private void configureXtextStandaloneConfiguration(Project project) {
        XtextBuilderPluginExtension extension = project.extensions.findByType(XtextBuilderPluginExtension)
        String xtextVersion = extension.xtextVersion.get()
        List<String> xtextStandaloneDependencies =
        [
                'org.eclipse.xtext:org.eclipse.xtext.builder.standalone',
                'org.eclipse.xtext:org.eclipse.xtext.ecore',
                'org.eclipse.xtext:org.eclipse.xtext.xtext.generator',
                'org.eclipse.xtext:org.eclipse.xtext.smap',
                'org.eclipse.emf:org.eclipse.emf.ecore.xmi',
        ]
        if (!PluginResourcesUtil.isPluginIntegrationTestRuntime()) {
            // Add this plugin as a dependency if not running in an integration test runtime of this plugin itself.
            String pluginDependencyNotation = "${PluginResourcesUtil.pluginGroupId}:${PluginResourcesUtil.pluginArtifactId}:${PluginResourcesUtil.pluginVersion}"
            xtextStandaloneDependencies.add(pluginDependencyNotation)
        }
        xtextStandaloneDependencies.each {
            project.dependencies.add(xtextStandalone.name, it)
        }
        project.dependencies.add(xtextStandalone.name,
                project.dependencies.enforcedPlatform('org.eclipse.xtext:xtext-dev-bom'))
        xtextStandalone.resolutionStrategy.eachDependency {
            if (it.requested.group == 'org.eclipse.xtext' || it.requested.group == 'org.eclipse.xtend') {
                it.useVersion(xtextVersion)
            }
        }
    }

//    private void addClasspathToClassloader(Configuration configuration) {
//        Set<URL> configurationURLs = configuration.files.collect { it.toURI().toURL() } as Set<URL>
//        ClasspathUtil.addUrl(getClass().classLoader as URLClassLoader, configurationURLs)
//    }

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

    private void configureTasks(Project project, Configuration xtextStandalone, Configuration xtextLanguages) {
        XtextBuilderPluginExtension extension = project.extensions.findByType(XtextBuilderPluginExtension)
        project.tasks.withType(GenerateXtextTask,
                new Action<GenerateXtextTask>() {
                    @Override
                    void execute(GenerateXtextTask t) {
                        t.xtextStandaloneClasspath.from(xtextStandalone)
                        if (PluginResourcesUtil.isPluginIntegrationTestRuntime()) {
                            logger.info("###### Plugin integration test runtime determined.")
                            addProjectBuildClassPathsIfIntegrationTestRuntime(t.xtextStandaloneClasspath)
                        }
                        t.xtextCompilerClasspath.from(xtextLanguages)
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
                                    .collect { LanguageDSL lang -> lang.outputConfigurations
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
                })
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
