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

class UnsupportedXtextVersionTest extends AbstractPluginIntegrationTest {

    @Override
    ComparableVersion getXtextVersion() {
        return new ComparableVersion('2.16.0')
    }

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
    void failWithXtextVersionNotSupported() {
        BuildResult result = buildAndFail("build")
        assertThat(result.output).contains("> Xtext version ${getXtextVersion()} is not supported. The minimum version is ${XTEXT_MIN_VERSION}.")
    }
}
