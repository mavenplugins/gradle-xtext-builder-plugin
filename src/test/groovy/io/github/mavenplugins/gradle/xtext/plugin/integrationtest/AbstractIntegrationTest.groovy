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

package io.github.mavenplugins.gradle.xtext.plugin.integrationtest

import io.github.mavenplugins.gradle.xtext.plugin.utils.PluginResourcesUtil
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask

abstract class AbstractIntegrationTest extends AbstractGradleBuildTest {

    protected static final String PLUGIN_ID = "${PluginResourcesUtil.pluginId}"

    protected static final String XTEXT_MIN_VERSION = "${PluginResourcesUtil.xtextMinVersion}"

    @Override
    void setup() {
        super.setup()
        rootProject.buildFile = """
            plugins {
                id '${PLUGIN_ID}' apply false
            }
            allprojects {
                ${repositories}
            }
        """.stripIndent()
        rootProject.createFile('gradle.properties',
"""
org.gradle.jvmargs=-XX:MaxMetaspaceSize=512m
org.gradle.daemon=false
""")
    }

    protected CharSequence getRepositories() {
        return """
            repositories {
                mavenCentral()
            }
        """.stripIndent()
    }

    BuildTask getXtextTask(BuildResult buildResult) {
        return getXtextTask(buildResult, rootProject)
    }

    BuildTask getXtextTask(BuildResult buildResult, ProjectUnderTest project) {
        String taskName = "${project.path}:generateXtext"
        return buildResult.task(taskName)
    }

    OutputSnapshot snapshot(File baseDir) {
        return new OutputSnapshot(baseDir)
    }

    ComparableVersion getXtextVersion() {
        return new ComparableVersion(System.getProperty("xtext.version", XTEXT_MIN_VERSION))
    }

}
