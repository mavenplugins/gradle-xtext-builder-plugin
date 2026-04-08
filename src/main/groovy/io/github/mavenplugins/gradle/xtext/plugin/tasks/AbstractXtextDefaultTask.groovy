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

package io.github.mavenplugins.gradle.xtext.plugin.tasks

import groovy.transform.CompileStatic
import io.github.mavenplugins.gradle.xtext.plugin.XtextBuilderPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import javax.inject.Inject
import java.nio.charset.StandardCharsets

/**
 *
 * @author Markus Hoffrogge
 * @since 1.0.0
 */
@CacheableTask
@CompileStatic
abstract class AbstractXtextDefaultTask extends DefaultTask {

//    static File builderJar
//    static {
//        builderJar = File.createTempFile("xtext-standalone-builder", "jar")
//        builderJar.deleteOnExit()
//        URL resourceUrl = AbstractXtextDefaultTask.classLoader.getResource("xtext-standalone-builder-0.1.0-SNAPSHOT.jar")
//        resourceUrl.openStream().withCloseable { inputStream ->
//            builderJar.withOutputStream { outputStream ->
//                outputStream << inputStream
//            }
//        }
//        println("###### Copied xtext-standalone-builder.jar to temporary file: ${builderJar.absolutePath}")
//    }

    @Nested
    final Property<XtextBuilderPluginExtension> extension

    @Input
    final Property<Boolean> logXtextConfig

    @Input
    final Property<String> encoding

    @Input
    final Property<String> compilerSourceLevel

    @Input
    final Property<String> compilerTargetLevel

    @Inject
    AbstractXtextDefaultTask(ObjectFactory objects) {
        extension = objects.property(XtextBuilderPluginExtension).convention(project.extensions.findByType(XtextBuilderPluginExtension))
        logXtextConfig = objects.property(Boolean).convention(false)
        encoding = objects.property(String).convention(StandardCharsets.UTF_8.name())
        compilerSourceLevel = objects.property(String).convention(extension.get().compilerSourceLevel.get())
        compilerTargetLevel = objects.property(String).convention(extension.get().compilerTargetLevel.get())
    }

    @Classpath
    abstract ConfigurableFileCollection getXtextStandaloneClasspath()

    @Classpath
    abstract ConfigurableFileCollection getXtextCompilerClasspath()

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE) // leverage cross machine build caching
    abstract ConfigurableFileCollection getJavaSourceDirectories()

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE) // leverage cross machine build caching
    abstract ConfigurableFileCollection getXtextSourceDirectories()

    @OutputDirectories
    abstract ConfigurableFileCollection getXtextOutputDirectories()

    /**
     * Scratch space for this task - follows Gradle's own convention of build/tmp/<taskName>.
     * Declared @Internal so it is not part of the build cache key.
     * Cleaned automatically by the 'clean' task since it lives under the build directory.
     */
    @Internal
    abstract DirectoryProperty getTempDirectory()

    @Option(option = 'logXtextConfig', description = 'Log actual Xtext config at task execution time on info level (OPTIONAL).')
    void setLogXtextConfig(boolean logXtextConfig) {
        this.logXtextConfig.set(logXtextConfig)
    }

    @TaskAction
    void performAction() {
        if (logXtextConfig.getOrElse(false)) {
            logClassLoaderURLs()
            logList("Xtext gradleClassLoaderInclusions", extension.get().gradleClassLoaderIncludes.get())
            logList("Xtext gradleClassLoaderExclusions", extension.get().gradleClassLoaderExcludes.get())
            javaSourceDirectories.eachWithIndex {javaSourceDirectory, int i ->
                logger.info("Java source directory[{}]={}", i, javaSourceDirectory)
            }
            xtextSourceDirectories.eachWithIndex {xtextSourceDirectory, int i ->
                logger.info("Xtext source directory[{}]={}", i, xtextSourceDirectory)
            }
            xtextOutputDirectories.eachWithIndex {xtextOutputDirectory, int i ->
                logger.info("Xtext output directory[{}]={}", i, xtextOutputDirectory)
            }
            logger.info(
                    "Xtext Encoding: " + (!encoding.present ? "not set. Encoding provider will be used." : encoding.get()));
            logger.info("Xtext Compiler source level: " + compilerSourceLevel.get());
            logger.info("Xtext Compiler target level: " + compilerTargetLevel.get());
        }
    }

    // For analysis purposes
    void logClassLoaderURLs() {
        URLClassLoader classLoader = (URLClassLoader) this.getClass().getClassLoader()
        List<URL> urls = Arrays.asList(classLoader.getURLs())
        String taskName = this.getClass().simpleName.replace('_Decorated', '')
        logger.info("====== Task '{}' classLoader URLs[{}] - BEGIN ======", taskName, urls.size())
        urls.eachWithIndex { URL url, int i -> logger.info("    {} classpath url[{}]={}", taskName, i, url.getFile()) }
        logger.info("====== Task '{}' classLoader URLs[{}] - END ======", taskName, urls.size())
        Set<File> classpathFiles = getXtextStandaloneClasspath().files
        logger.info("====== Task '{}' xtextStandaloneClasspath URLs[{}] - BEGIN ======", taskName, classpathFiles.size())
        classpathFiles.eachWithIndex { File file, int i -> logger.info("    {} classpath url[{}]={}", taskName, i, file) }
        logger.info("====== Task '{}' xtextStandaloneClasspath URLs[{}] - END ======", taskName, classpathFiles.size())
        classpathFiles = getXtextCompilerClasspath().files
        logger.info("====== Task '{}' xtextCompilerClasspath URLs[{}] - BEGIN ======", taskName, classpathFiles.size())
        classpathFiles.eachWithIndex { File file, int i -> logger.info("    {} classpath url[{}]={}", taskName, i, file) }
        logger.info("====== Task '{}' xtextCompilerClasspath URLs[{}] - END ======", taskName, classpathFiles.size())
    }

    private void logList(String context, List<String> elements) {
        if (elements == null || elements.isEmpty()) {
            logger.info("{}: []", context)
        } else {
            logger.info("{} [{}]:", context, elements.size())
            elements.eachWithIndex { String element, int i -> logger.info("    [{}]='{}'", i, element) }
        }
    }
}
