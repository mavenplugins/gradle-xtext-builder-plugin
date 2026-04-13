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

package io.github.mavenplugins.gradle.xtext.builder

import com.google.inject.Guice
import com.google.inject.Injector
import io.github.mavenplugins.gradle.xtext.builder.utils.XtextModelUtil
import io.github.mavenplugins.gradle.xtext.plugin.builderinterface.IXtextStandaloneBuilder
import io.github.mavenplugins.gradle.xtext.plugin.dsl.LanguageDSL
import io.github.mavenplugins.gradle.xtext.plugin.internal.GradleRuntimeFilteringClassLoader
import io.github.mavenplugins.gradle.xtext.plugin.internal.guava.Preconditions
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.builder.standalone.ClusteringConfig
import org.eclipse.xtext.builder.standalone.LanguageAccess
import org.eclipse.xtext.builder.standalone.LanguageAccessFactory
import org.eclipse.xtext.builder.standalone.StandaloneBuilder
import org.eclipse.xtext.builder.standalone.compiler.CompilerConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.logging.Logger

import java.lang.reflect.Method

class XtextStandaloneBuilder implements IXtextStandaloneBuilder {

    private final Logger logger

    private GeneratedResourcesCountingStandaloneBuilder standaloneBuilder

    private int generatedResourcesCount = 0

    XtextStandaloneBuilder(Logger logger) {
        Preconditions.checkState(getClass().getClassLoader().getParent() instanceof GradleRuntimeFilteringClassLoader,
                "${XtextStandaloneBuilder} must be loaded via child classloader of ${GradleRuntimeFilteringClassLoader}")
        this.logger = logger
    }

    @Override
    void setLanguages(final NamedDomainObjectContainer<LanguageDSL> languages) {
        Map<String, LanguageAccess> xtextLanguages = new LanguageAccessFactory().createLanguageAccess(
                XtextModelUtil.languageConfigurationsFrom(languages),
                this.getClass().getClassLoader())
        Injector injector = Guice.createInjector(new GradleStandaloneBuilderModule())
        standaloneBuilder = injector.getInstance(GeneratedResourcesCountingStandaloneBuilder)
        standaloneBuilder.setLanguages(xtextLanguages)
    }

    @Override
    void setBaseDir(final String baseDir) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setBaseDir")
        standaloneBuilder.setBaseDir(baseDir)
    }

    @Override
    void setSourceDirs(final Iterable<String> sourceDirs) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setSourceDirs")
        standaloneBuilder.setSourceDirs(sourceDirs)
    }

    @Override
    void setJavaSourceDirs(final Iterable<String> javaSourceDirs) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setJavaSourceDirs")
        standaloneBuilder.setJavaSourceDirs(javaSourceDirs)
    }

    @Override
    void setClassPathEntries(final Iterable<String> classPathEntries) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setClassPathEntries")
        standaloneBuilder.setClassPathEntries(classPathEntries)
    }

    @Override
    void setTempDir(final File tempDir) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setTempDir")
        standaloneBuilder.setTempDir(tempDir)
    }

    @Override
    void setEncoding(final String encoding) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setEncoding")
        standaloneBuilder.setEncoding(encoding)
    }

    @Override
    void setClassPathLookUpFilter(final String classPathLookUpFilter) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setClassPathLookUpFilter")
        standaloneBuilder.setClassPathLookUpFilter(classPathLookUpFilter)
    }

    @Override
    void setFailOnValidationError(final boolean failOnValidationError) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setFailOnValidationError")
        standaloneBuilder.setFailOnValidationError(failOnValidationError)
    }

    @Override
    void setDebugLog(final boolean debugLog) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setDebugLog")
        standaloneBuilder.setDebugLog(debugLog)
    }

    @Override
    void setIncrementalBuild(final boolean incrementalBuild) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setIncrementalBuild")
        try {
            Method method = standaloneBuilder.class.getMethod('setIncrementalBuild', boolean.class)
            method.invoke(standaloneBuilder, incrementalBuild)
        } catch (NoSuchMethodException e) {
            // Method doesn't exist in this version of Xtext, warn if it is to be enabled.
            if (incrementalBuild) {
                logger.warn("Incremental build is not supported by StandaloneBuilder with the current version of Xtext. Please upgrade to Xtext 2.19.0 or later to enable incremental build.")
            }
        }
    }

    @Override
    void setWriteStorageResources(final boolean writeStorageResources) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setWriteStorageResources")
        standaloneBuilder.setWriteStorageResources(writeStorageResources)
    }

    @Override
    void setClusteringConfig(final Object clusteringConfig) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before setClusteringConfig")
        Preconditions.checkArgument(clusteringConfig instanceof ClusteringConfig, "clusteringConfig must be an instance of ClusteringConfig")
        standaloneBuilder.setClusteringConfig((ClusteringConfig) clusteringConfig)
    }

    @Override
    void configureCompiler(final String sourceLevel,
                                  final String targetLevel,
                                  final boolean isDebugLog,
                                  final boolean isSkipAnnotationProcessing,
                                  final boolean isPreserveInformationAboutFormalParameters) {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before configureCompiler")
        CompilerConfiguration conf = standaloneBuilder.getCompiler().getConfiguration()
        conf.setSourceLevel(sourceLevel)
        conf.setTargetLevel(targetLevel)
        conf.setVerbose(isDebugLog)
        conf.setSkipAnnotationProcessing(isSkipAnnotationProcessing) // compilerSkipAnnotationProcessing TODO check if this should be configurable
        conf.setPreserveInformationAboutFormalParameters(isPreserveInformationAboutFormalParameters) // compilerPreserveInformationAboutFormalParameters TODO check if this should be configurable
    }

    @Override
    boolean launch() {
        Preconditions.checkNotNull(standaloneBuilder, "setLanguages must be called before launch")
        boolean isSuccess = standaloneBuilder.launch()
        generatedResourcesCount = standaloneBuilder.getGeneratedResourcesCount()
        return isSuccess
    }

    @Override
    int getGeneratedResourcesCount() {
        return generatedResourcesCount
    }

    @Override
    void close() {
        if (getClass().getClassLoader().getParent() instanceof GradleRuntimeFilteringClassLoader) {
            try {
                ((URLClassLoader) getClass().getClassLoader()).close()
            } catch (Exception e) {
            }
        }
        standaloneBuilder = null
    }

    private static class GeneratedResourcesCountingStandaloneBuilder extends StandaloneBuilder {
        private int generatedResourcesCount = 0

        @Override
        protected void generate(final List<Resource> sourceResources) {
            super.generate(sourceResources)
            generatedResourcesCount += sourceResources.size()
        }

        int getGeneratedResourcesCount() {
            return generatedResourcesCount
        }
    }
}
