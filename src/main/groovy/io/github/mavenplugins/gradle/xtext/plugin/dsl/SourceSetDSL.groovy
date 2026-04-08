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

package io.github.mavenplugins.gradle.xtext.plugin.dsl

import org.gradle.api.Named
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

import javax.inject.Inject

/**
 * DSL configuration for source directories.
 */
abstract class SourceSetDSL implements Named {

    @InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    final ListProperty<Directory> srcDirs

    @Internal
    final private Directory projectDir
    Directory getProjectDir() {
        return projectDir
    }

    @Inject
    SourceSetDSL(ObjectFactory objects, ProjectLayout layout) {
        // set default values for properties
        srcDirs = objects.listProperty(Directory).convention([])
        projectDir = layout.projectDirectory
    }

    @Input
    @Override
    abstract String getName()

    void srcDir(Directory dir) {
        srcDirs.add(dir)
    }

    void srcDir(Provider<Directory> dir) {
        srcDirs.add(dir)
    }

    void srcDir(String relativeProjectDir) {
        srcDirs.add(getProjectDir().dir(relativeProjectDir));
    }

}