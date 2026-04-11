/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The XtextBuilder authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package io.github.mavenplugins.gradle.xtext.plugin.builderinterface

import io.github.mavenplugins.gradle.xtext.plugin.dsl.LanguageDSL
import org.gradle.api.NamedDomainObjectContainer

interface IXtextStandaloneBuilder extends Closeable {

    void setLanguages(NamedDomainObjectContainer<LanguageDSL> languages)

    void setBaseDir(String baseDir)

    void setSourceDirs(Iterable<String> sourceDirs)

    void setJavaSourceDirs(Iterable<String> javaSourceDirs)

    void setClassPathEntries(Iterable<String> classPathEntries)

    void setTempDir(File tempDir)

    void setEncoding(String encoding)

    void setClassPathLookUpFilter(String classPathLookUpFilter)

    void setFailOnValidationError(boolean failOnValidationError)

    void setDebugLog(boolean debugLog)

    void setIncrementalBuild(boolean incrementalBuild)

    void setWriteStorageResources(boolean writeStorageResources)

    void setClusteringConfig(Object clusteringConfig)

    void configureCompiler(
            String sourceLevel,
            String targetLevel,
            boolean isDebugLog,
            boolean isSkipAnnotationProcessing,
            boolean PreserveInformationAboutFormalParameters)

    boolean launch()

    int getGeneratedResourcesCount()

}
