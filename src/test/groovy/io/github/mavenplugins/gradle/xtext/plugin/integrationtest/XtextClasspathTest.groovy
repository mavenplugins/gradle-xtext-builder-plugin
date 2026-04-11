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

import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat

class XtextClasspathTest extends AbstractPluginIntegrationTest {

    final static int EXPECTED_CLASSPATH_SIZE = 4
    final static int EXPECTED_STANDALONE_CLASSPATH_SIZE_2_17_1 = 63 + 3 // 3 is coming from integration test build class path directories
    final static int EXPECTED_STANDALONE_CLASSPATH_SIZE_2_29_0 = 81 + 3 // 3 is coming from integration test build class path directories

    @BeforeEach
    @Override
    void setup() {
        super.setup()
        rootProject.buildFile << """
            xtextBuilder {
                xtextVersion = '${getXtextVersion()}'
            }
        """.stripIndent()
    }

    @Test
    void checkXtextClassPath() {
        final int expectedStandaloneClasspathSize = getXtextVersion().compareTo(new ComparableVersion('2.29.0')) >= 0 ?
                EXPECTED_STANDALONE_CLASSPATH_SIZE_2_29_0 : EXPECTED_STANDALONE_CLASSPATH_SIZE_2_17_1
        BuildResult result = build("generateXtext", "--info", "--logXtextConfig")
        assertThat(result.output).contains("====== Task 'GenerateXtextTask' classLoader URLs[${EXPECTED_CLASSPATH_SIZE}] - BEGIN ======")
        assertThat(result.output).contains("====== Task 'GenerateXtextTask' xtextStandaloneClasspath URLs[${expectedStandaloneClasspathSize}] - BEGIN ======")
        assertThat(result.output).contains("====== Task 'GenerateXtextTask' xtextCompilerClasspath URLs[0] - BEGIN ======")
    }
}
