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

class XtextLanguageBuilderTest extends AbstractXtendIntegrationTest {

    @BeforeEach
    @Override
    void setup() {
        super.setup()
        rootProject.buildFile << """

            xtextBuilder {
                xtextVersion = '${getXtextVersion()}'
                sourceSets {
                    main {
                        srcDir layout.projectDirectory.dir('src/main/java')
                    }
                }
                languages {
                    xtend {
                        setup = 'org.eclipse.xtend.core.XtendStandaloneSetup'
                        javaSupport = true
                        //compilerSourceLevel = '8'
                        //compilerTargetLevel = '8'
                        outputConfigurations {
                            // Refer to
                            // https://github.com/eclipse-xtext/xtext/blob/main/org.eclipse.xtend.core/src/org/eclipse/xtend/core/compiler/XtendOutputConfigurationProvider.java
                            DEFAULT_OUTPUT {
                                description = 'Output folder for generated Java files'
                                outputDirectory = layout.buildDirectory.dir('xtend-gen')
                                overrideExistingResources = true
                                createOutputDirectory = true
                                canClearOutputDirectory = false
                                cleanUpDerivedResources = true
                                setDerivedProperty = true
                                keepLocalHistory = false
                            }
                        }
                    }
                }
            }
        """.stripIndent()
        createHelloWorld()
    }

    @Test
    void checkXtextGenerate() {
        BuildResult result = build("generateXtext", "--info", "--logXtextConfig")

        // Log all files in build directory recursively
        //logBuildDirectoryFiles()

        assertThat(rootProject.file('build/tmp/generateXtext/stubs/HelloWorld.java').exists()).isTrue()
        if (isJava8Runtime()) {
            assertThat(rootProject.file('build/tmp/generateXtext/classes/HelloWorld.class').exists()).isTrue()
        } else {
            assertThat(rootProject.file('build/tmp/generateXtext/stub-classes/HelloWorld.class').exists()).isTrue()
        }
        assertThat(rootProject.file('build/xtend-gen/HelloWorld.java').exists()).isTrue()
        assertThat(rootProject.file('build/xtend-gen/.HelloWorld.java._trace').exists()).isTrue()
        assertThat(result.output).contains("Xtext generated 1 resource.")
    }
}
