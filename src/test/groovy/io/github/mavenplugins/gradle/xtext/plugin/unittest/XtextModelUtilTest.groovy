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

package io.github.mavenplugins.gradle.xtext.plugin.unittest

import io.github.mavenplugins.gradle.xtext.builder.utils.XtextModelUtil
import io.github.mavenplugins.gradle.xtext.plugin.dsl.LanguageDSL
import io.github.mavenplugins.gradle.xtext.plugin.dsl.OutputConfigurationDSL
import org.eclipse.xtext.builder.standalone.ILanguageConfiguration
import org.eclipse.xtext.generator.OutputConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat

class XtextModelUtilTest {

    Project project

    @BeforeEach
    void setup() {
        project = ProjectBuilder.builder().build()
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private NamedDomainObjectContainer<LanguageDSL> languageContainer() {
        project.objects.domainObjectContainer(LanguageDSL)
    }

    private NamedDomainObjectContainer<OutputConfigurationDSL> outputConfigContainer() {
        project.objects.domainObjectContainer(OutputConfigurationDSL)
    }

    private File tempDir(String name) {
        File dir = new File(project.projectDir, name)
        dir.mkdirs()
        return dir
    }

    // -------------------------------------------------------------------------
    // outputConfigurationFrom
    // -------------------------------------------------------------------------

    @Test
    void 'outputConfigurationFrom maps all scalar properties correctly'() {
        NamedDomainObjectContainer<OutputConfigurationDSL> container = outputConfigContainer()
        OutputConfigurationDSL dsl = container.create('DEFAULT')
        File outDir = tempDir('out/default')
        dsl.outputDirectory.set(outDir)
        dsl.description.set('My output')
        dsl.createOutputDirectory.set(false)
        dsl.overrideExistingResources.set(false)
        dsl.installDslAsPrimarySource.set(true)
        dsl.hideSyntheticLocalVariables.set(false)
        dsl.canClearOutputDirectory.set(true)
        dsl.cleanUpDerivedResources.set(false)
        dsl.setDerivedProperty.set(false)
        dsl.keepLocalHistory.set(true)

        OutputConfiguration result = XtextModelUtil.outputConfigurationFrom(dsl)

        assertThat(result.name).isEqualTo('DEFAULT')
        assertThat(result.outputDirectory).isEqualTo(outDir.absolutePath)
        assertThat(result.description).isEqualTo('My output')
        assertThat(result.isCreateOutputDirectory()).isFalse()
        assertThat(result.isOverrideExistingResources()).isFalse()
        assertThat(result.isInstallDslAsPrimarySource()).isTrue()
        assertThat(result.isHideSyntheticLocalVariables()).isFalse()
        assertThat(result.isCanClearOutputDirectory()).isTrue()
        assertThat(result.isCleanUpDerivedResources()).isFalse()
        assertThat(result.isSetDerivedProperty()).isFalse()
        assertThat(result.isKeepLocalHistory()).isTrue()
    }

    @Test
    void 'outputConfigurationFrom uses default property values'() {
        NamedDomainObjectContainer<OutputConfigurationDSL> container = outputConfigContainer()
        OutputConfigurationDSL dsl = container.create('DEFAULT')
        dsl.outputDirectory.set(tempDir('out/default'))

        OutputConfiguration result = XtextModelUtil.outputConfigurationFrom(dsl)

        assertThat(result.isCreateOutputDirectory()).isTrue()
        assertThat(result.isOverrideExistingResources()).isTrue()
        assertThat(result.isInstallDslAsPrimarySource()).isFalse()
        assertThat(result.isHideSyntheticLocalVariables()).isTrue()
        assertThat(result.isCanClearOutputDirectory()).isFalse()
        assertThat(result.isCleanUpDerivedResources()).isTrue()
        assertThat(result.isSetDerivedProperty()).isTrue()
        assertThat(result.isKeepLocalHistory()).isFalse()
        assertThat(result.sourceMappings).isEmpty()
        assertThat(result.isUseOutputPerSourceFolder()).isFalse()
    }

    @Test
    void 'outputConfigurationFrom with no source mappings does not set useOutputPerSourceFolder'() {
        NamedDomainObjectContainer<OutputConfigurationDSL> container = outputConfigContainer()
        OutputConfigurationDSL dsl = container.create('DEFAULT')
        dsl.outputDirectory.set(tempDir('out/default'))

        OutputConfiguration result = XtextModelUtil.outputConfigurationFrom(dsl)

        assertThat(result.isUseOutputPerSourceFolder()).isFalse()
        assertThat(result.sourceMappings).isEmpty()
    }

    // -------------------------------------------------------------------------
    // outputConfigurationsFrom
    // -------------------------------------------------------------------------

    @Test
    void 'outputConfigurationsFrom returns a set with all entries'() {
        NamedDomainObjectContainer<OutputConfigurationDSL> container = outputConfigContainer()
        container.create('DEFAULT') { it.outputDirectory.set(tempDir('out/default')) }
        container.create('SECONDARY') { it.outputDirectory.set(tempDir('out/secondary')) }

        Set<OutputConfiguration> result = XtextModelUtil.outputConfigurationsFrom(container)

        assertThat(result).hasSize(2)
        assertThat(result*.name as List<String>).containsExactlyInAnyOrder('DEFAULT', 'SECONDARY')
    }

    @Test
    void 'outputConfigurationsFrom returns empty set for empty container'() {
        NamedDomainObjectContainer<OutputConfigurationDSL> container = outputConfigContainer()

        Set<OutputConfiguration> result = XtextModelUtil.outputConfigurationsFrom(container)

        assertThat(result).isEmpty()
    }

    // -------------------------------------------------------------------------
    // languageConfigurationFrom
    // -------------------------------------------------------------------------

    @Test
    void 'languageConfigurationFrom maps setup and javaSupport'() {
        NamedDomainObjectContainer<LanguageDSL> container = languageContainer()
        LanguageDSL dsl = container.create('myLang')
        dsl.setup.set('com.example.MyDslStandaloneSetup')
        dsl.javaSupport.set(false)
        dsl.outputConfigurations.create('DEFAULT') {
            it.outputDirectory.set(tempDir('out/myLang'))
        }

        XtextModelUtil.LanguageConfiguration result = XtextModelUtil.languageConfigurationFrom(dsl)

        assertThat(result.setup).isEqualTo('com.example.MyDslStandaloneSetup')
        assertThat(result.isJavaSupport()).isFalse()
    }

    @Test
    void 'languageConfigurationFrom maps output configurations'() {
        NamedDomainObjectContainer<LanguageDSL> container = languageContainer()
        LanguageDSL dsl = container.create('myLang')
        dsl.setup.set('com.example.MyDslStandaloneSetup')
        dsl.outputConfigurations.create('DEFAULT') {
            it.outputDirectory.set(tempDir('out/default'))
        }
        dsl.outputConfigurations.create('SECONDARY') {
            it.outputDirectory.set(tempDir('out/secondary'))
        }

        XtextModelUtil.LanguageConfiguration result = XtextModelUtil.languageConfigurationFrom(dsl)

        assertThat(result.outputConfigurations).hasSize(2)
        assertThat(result.outputConfigurations*.name as List<String>).containsExactlyInAnyOrder('DEFAULT', 'SECONDARY')
    }

    @Test
    void 'languageConfigurationFrom defaults javaSupport to true'() {
        NamedDomainObjectContainer<LanguageDSL> container = languageContainer()
        LanguageDSL dsl = container.create('myLang')
        dsl.setup.set('com.example.MyDslStandaloneSetup')
        dsl.outputConfigurations.create('DEFAULT') {
            it.outputDirectory.set(tempDir('out/default'))
        }

        XtextModelUtil.LanguageConfiguration result = XtextModelUtil.languageConfigurationFrom(dsl)

        assertThat(result.isJavaSupport()).isTrue()
    }

    // -------------------------------------------------------------------------
    // languageConfigurationsFrom
    // -------------------------------------------------------------------------

    @Test
    void 'languageConfigurationsFrom returns list with all entries'() {
        NamedDomainObjectContainer<LanguageDSL> container = languageContainer()
        ['langA', 'langB', 'langC'].each { String name ->
            container.create(name) { LanguageDSL dsl ->
                dsl.setup.set("com.example.${name}Setup")
                dsl.outputConfigurations.create('DEFAULT') {
                    it.outputDirectory.set(tempDir("out/${name}"))
                }
            }
        }

        List<ILanguageConfiguration> result = XtextModelUtil.languageConfigurationsFrom(container)

        assertThat(result).hasSize(3)
        assertThat(result*.setup as List<String>).containsExactlyInAnyOrder(
            'com.example.langASetup',
            'com.example.langBSetup',
            'com.example.langCSetup'
        )
    }

    @Test
    void 'languageConfigurationsFrom returns empty list for empty container'() {
        NamedDomainObjectContainer<LanguageDSL> container = languageContainer()

        List<ILanguageConfiguration> result = XtextModelUtil.languageConfigurationsFrom(container)

        assertThat(result).isEmpty()
    }
}
