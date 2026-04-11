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
import io.github.mavenplugins.gradle.xtext.plugin.builderinterface.IXtextStandaloneBuilder
import io.github.mavenplugins.gradle.xtext.plugin.builderinterface.XtextStandaloneBuilderProvider
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask

import javax.inject.Inject

@CacheableTask
@CompileStatic
abstract class GenerateXtextTask extends AbstractXtextDefaultTask {
    static final String NAME = 'generateXtext'

    @Inject
    GenerateXtextTask(ObjectFactory objects) {
        super(objects)
    }

    @Override
    void performAction() {
        super.performAction()
        Set<File> classPath = new LinkedHashSet(xtextStandaloneClasspath.files)
        IXtextStandaloneBuilder builder = XtextStandaloneBuilderProvider.getBuilder(
                extension.get().languages,
                classPath,
                extension.get().gradleClassLoaderIncludes.get(),
                extension.get().gradleClassLoaderExcludes.get(),
                logger
        )
        builder.setBaseDir(extension.get().layout.projectDirectory.asFile.absolutePath)
        builder.setEncoding(encoding.get())
        builder.setClassPathEntries(getClassPathElementsConfigured())
        builder.setClassPathLookUpFilter(null) // TODO check if this should be configurable
        builder.setSourceDirs(getSourceRootsConfigured())
        builder.setJavaSourceDirs(getJavaSourceRootsConfigured())
        builder.setFailOnValidationError(failOnValidationError.get())
        // Use build/tmp/<taskName> as scratch space - follows Gradle's convention,
        // cleaned by 'clean', deterministic path, no random suffix
        builder.setTempDir(tempDirectory.get().asFile.tap { it.mkdirs() })
        builder.setDebugLog(logger.isDebugEnabled())
        builder.setIncrementalBuild(incrementalBuild.get())
        // TODO check if clusteringConfig should be configurable
        //if (clusteringConfig != null) {
        //    builder.setClusteringConfig(clusteringConfig.convertToStandaloneConfig())
        //}
        builder.configureCompiler(
                compilerSourceLevel.get(),
                compilerTargetLevel.get(),
                logger.isDebugEnabled(),
                false, // compilerSkipAnnotationProcessing TODO check if this should be configurable
                false  // compilerPreserveInformationAboutFormalParameters TODO check if this should be configurable
        )
        boolean errorDetected = !builder.launch()
        if (errorDetected) {
            throw new GradleException("Xtext generation failed due to a severe validation error.")
        }
        logger.info("Xtext generated {} resource{}.", builder.generatedResourcesCount, builder.generatedResourcesCount == 1 ? '' : 's')
    }

    private Iterable<String> getClassPathElementsConfigured() {
        return xtextCompilerClasspath.files.collect { it.absolutePath }
    }

    private Iterable<String> getSourceRootsConfigured() {
        return xtextSourceDirectories.collect { it.absolutePath }
    }

    private Iterable<String> getJavaSourceRootsConfigured() {
        return javaSourceDirectories.collect { it.absolutePath }
    }
}
