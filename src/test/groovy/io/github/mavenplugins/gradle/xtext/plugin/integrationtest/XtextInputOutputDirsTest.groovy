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

class XtextInputOutputDirsTest extends AbstractXtendIntegrationTest {

    final static String XTEXT_VERSION_TESTED = '2.29.0'

    @BeforeEach
    @Override
    void setup() {
        super.setup()
        rootProject.buildFile << """

            xtextBuilder {
                xtextVersion = '${XTEXT_VERSION_TESTED}'
                sourceSets {
                    main {
                        srcDir layout.projectDirectory.dir('src/main/testLanguage')
                        srcDir 'src/main/relative/path/testLanguage'
                        srcDir layout.projectDirectory.dir('src/test/testLanguage')
                    }
                    optional {    
                        srcDir layout.projectDirectory.dir('src/main/resources/testLanguageOptional')
                        srcDir layout.projectDirectory.dir('src/test/resources/testLanguageOptional')
                    }
                }
                languages {
                    xtend {
                        setup = 'org.eclipse.xtend.core.XtendStandaloneSetup'
                        outputConfigurations {
                            main {
                                outputDirectory = layout.buildDirectory.dir('generated-sources/testLanguage')
                            }
                            optional {
                                outputDirectory = layout.buildDirectory.dir('generated-sources/testLanguageOptional')
                            }
                        }
                    }
                }
            }
        """.stripIndent()
    }

    @Test
    void checkXtextClassPath() {
        BuildResult result = build("generateXtext", "--info", "--logXtextConfig")
        assertThat(result.output).contains("Xtext source directory[0]=${new File(rootProject.projectDir, "src/main/testLanguage").absolutePath}")
        assertThat(result.output).contains("Xtext source directory[1]=${new File(rootProject.projectDir, "src/main/relative/path/testLanguage").absolutePath}")
        assertThat(result.output).contains("Xtext source directory[2]=${new File(rootProject.projectDir, "src/test/testLanguage").absolutePath}")
        assertThat(result.output).contains("Xtext source directory[3]=${new File(rootProject.projectDir, "src/main/resources/testLanguageOptional").absolutePath}")
        assertThat(result.output).contains("Xtext source directory[4]=${new File(rootProject.projectDir, "src/test/resources/testLanguageOptional").absolutePath}")
        assertThat(result.output).contains("Xtext output directory[0]=${new File(rootProject.projectDir, "build/generated-sources/testLanguage").absolutePath}")
        assertThat(result.output).contains("Xtext output directory[1]=${new File(rootProject.projectDir, "build/generated-sources/testLanguageOptional").absolutePath}")
    }
}
