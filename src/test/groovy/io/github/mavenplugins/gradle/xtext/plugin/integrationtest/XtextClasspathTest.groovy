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

import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat

class XtextClasspathTest extends AbstractPluginIntegrationTest {

    final static String XTEXT_VERSION_TESTED = '2.29.0'
    final static int EXPECTED_CLASSPATH_SIZE = 4
    final static int EXPECTED_STANDALONE_CLASSPATH_SIZE = 58 + 3 // 3 is coming from integration test build class path directories

    @BeforeEach
    @Override
    void setup() {
        super.setup()
        rootProject.buildFile << """
            xtextBuilder {
                xtextVersion = '${XTEXT_VERSION_TESTED}'
            }
        """.stripIndent()
    }

    @Test
    void checkXtextClassPath() {
        BuildResult result = build("generateXtext", "--info", "--logXtextConfig")
        assertThat(result.output).contains("====== Task 'GenerateXtextTask' classLoader URLs[${EXPECTED_CLASSPATH_SIZE}] - BEGIN ======")
        assertThat(result.output).contains("====== Task 'GenerateXtextTask' xtextStandaloneClasspath URLs[${EXPECTED_STANDALONE_CLASSPATH_SIZE}] - BEGIN ======")
        assertThat(result.output).contains("====== Task 'GenerateXtextTask' xtextCompilerClasspath URLs[0] - BEGIN ======")
    }
}
