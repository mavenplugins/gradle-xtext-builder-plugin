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

package io.github.mavenplugins.gradle.xtext.builder.utils

import io.github.mavenplugins.gradle.xtext.plugin.dsl.LanguageDSL
import io.github.mavenplugins.gradle.xtext.plugin.dsl.OutputConfigurationDSL
import org.eclipse.xtext.builder.standalone.ILanguageConfiguration
import org.eclipse.xtext.generator.OutputConfiguration
import org.gradle.api.NamedDomainObjectContainer

class XtextModelUtil {

    static List<ILanguageConfiguration> languageConfigurationsFrom(NamedDomainObjectContainer<LanguageDSL> languages) {
        return languages.collect { language ->
                languageConfigurationFrom(language)
        }
    }

    static Set<OutputConfiguration> outputConfigurationsFrom(NamedDomainObjectContainer<OutputConfigurationDSL> outputConfigurations) {
        return outputConfigurations.collect { outputConfiguration ->
                outputConfigurationFrom(outputConfiguration)
        }.toSet()
    }

    static OutputConfiguration outputConfigurationFrom(OutputConfigurationDSL outputConfigurationDSL) {
        OutputConfiguration ret = new OutputConfiguration(outputConfigurationDSL.name)
        ret.setSetDerivedProperty(outputConfigurationDSL.setDerivedProperty.get())
        ret.setCanClearOutputDirectory(outputConfigurationDSL.canClearOutputDirectory.get())
        ret.setCleanUpDerivedResources(outputConfigurationDSL.cleanUpDerivedResources.get())
        ret.setCreateOutputDirectory(outputConfigurationDSL.createOutputDirectory.get())
        ret.setDescription(outputConfigurationDSL.description.get())
        ret.setHideSyntheticLocalVariables(outputConfigurationDSL.hideSyntheticLocalVariables.get())
        ret.setInstallDslAsPrimarySource(outputConfigurationDSL.installDslAsPrimarySource.get())
        ret.setKeepLocalHistory(outputConfigurationDSL.keepLocalHistory.get())
        ret.setOverrideExistingResources(outputConfigurationDSL.overrideExistingResources.get())
        ret.setOutputDirectory(outputConfigurationDSL.outputDirectory.get().asFile.absolutePath)
        if (!outputConfigurationDSL.sourceMappings.get().isEmpty()) {
            ret.setUseOutputPerSourceFolder(true)
            ret.getSourceMappings().addAll(outputConfigurationDSL.sourceMappings.collect {
                sourceMappingFrom(it.get())
            })
        }
        return ret
    }

    static OutputConfiguration.SourceMapping sourceMappingFrom(OutputConfigurationDSL.SourceMappingDSL sourceMappingDSL) {
        OutputConfiguration.SourceMapping ret = new OutputConfiguration.SourceMapping(sourceMappingDSL.sourceFolder.get().asFile.absolutePath)
        ret.setOutputDirectory(sourceMappingDSL.outputDirectory.get().asFile.absolutePath)
        return ret
    }

    static LanguageConfiguration languageConfigurationFrom(LanguageDSL languageDSL) {
        LanguageConfiguration ret = new LanguageConfiguration()
        ret.setJavaSupport(languageDSL.javaSupport.get())
        ret.setSetup(languageDSL.setup.get())
        ret.setOutputConfigurations(outputConfigurationsFrom(languageDSL.outputConfigurations))
        return ret
    }

    /**
     * @author Dennis Huebner - Initial contribution and API
     *
     */
    static class LanguageConfiguration implements ILanguageConfiguration {

        /**
         * whether this language links or produces Java types
         * @property
         */
        private boolean javaSupport = true;

        /**
         * @property
         * @required
         */
        private String setup;

        /**
         * @property
         */
        private Set<OutputConfiguration> outputConfigurations;

        @Override
        String getSetup() {
            return setup
        }

        void setSetup(String setup) {
            this.setup = setup
        }

        @Override
        Set<OutputConfiguration> getOutputConfigurations() {
            return outputConfigurations
        }

        void setOutputConfigurations(Set<OutputConfiguration> outputConfigurations) {
            this.outputConfigurations = outputConfigurations
        }

        @Override
        boolean isJavaSupport() {
            return javaSupport
        }

        void setJavaSupport(boolean javaSupport) {
            this.javaSupport = javaSupport
        }

    }
}
