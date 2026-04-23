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

package io.github.mavenplugins.gradle.xtext.plugin.dsl

import org.eclipse.xtext.generator.OutputConfiguration
import org.gradle.api.Named
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory

import javax.inject.Inject

/**
 * DSL configuration for {@link OutputConfiguration}.
 */
abstract class OutputConfigurationDSL implements ICloneableNamed<OutputConfigurationDSL> {

    @Inject
    OutputConfigurationDSL(ObjectFactory objects) {
        // set default values for properties
        description = objects.property(String.class).convention("")
        outputDirectory = objects.directoryProperty()
        createOutputDirectory = objects.property(Boolean.class).convention(true)
        overrideExistingResources = objects.property(Boolean.class).convention(true)
        installDslAsPrimarySource = objects.property(Boolean.class).convention(false)
        hideSyntheticLocalVariables = objects.property(Boolean.class).convention(true)
        canClearOutputDirectory = objects.property(Boolean.class).convention(false)
        cleanUpDerivedResources = objects.property(Boolean.class).convention(true)
        sourceMappings = objects.listProperty(SourceMappingDSL.class).convention(new ArrayList<SourceMappingDSL>())
        setDerivedProperty = objects.property(Boolean.class).convention(true)
        keepLocalHistory = objects.property(Boolean.class).convention(false)
    }

    @Input
    @Override
    abstract String getName()

    void sourceMapping(SourceMappingDSL sourceMappingDSL) {
        sourceMappings.add(sourceMappingDSL)
    }

    final Property<String> getDescription() {
        return description
    }

    final DirectoryProperty getOutputDirectory() {
        return outputDirectory
    }

    final Property<Boolean> getCreateOutputDirectory() {
        return createOutputDirectory
    }

    final Property<Boolean> getOverrideExistingResources() {
        return overrideExistingResources
    }

    final Property<Boolean> getInstallDslAsPrimarySource() {
        return installDslAsPrimarySource
    }

    final Property<Boolean> getHideSyntheticLocalVariables() {
        return hideSyntheticLocalVariables
    }

    final Property<Boolean> getCanClearOutputDirectory() {
        return canClearOutputDirectory
    }

    final Property<Boolean> getCleanUpDerivedResources() {
        return cleanUpDerivedResources
    }

    final ListProperty<SourceMappingDSL> getSourceMappings() {
        return sourceMappings
    }

    final Property<Boolean> getSetDerivedProperty() {
        return setDerivedProperty
    }

    final Property<Boolean> getKeepLocalHistory() {
        return keepLocalHistory
    }

    @Input
    private final Property<String> description
    @OutputDirectory
    private final DirectoryProperty outputDirectory
    @Input
    private final Property<Boolean> createOutputDirectory
    @Input
    private final Property<Boolean> overrideExistingResources
    @Input
    private final Property<Boolean> installDslAsPrimarySource
    @Input
    private final Property<Boolean> hideSyntheticLocalVariables
    @Input
    private final Property<Boolean> canClearOutputDirectory
    @Input
    private final Property<Boolean> cleanUpDerivedResources
    @Input
    private final ListProperty<SourceMappingDSL> sourceMappings
    @Input
    private final Property<Boolean> setDerivedProperty
    @Input
    private final Property<Boolean> keepLocalHistory

    abstract class SourceMappingDSL {
        @Inject
        SourceMappingDSL(OutputConfigurationDSL enclosing, ObjectFactory objects) {
            sourceFolder = objects.directoryProperty()
            outputDirectory = objects.directoryProperty()
            ignore = objects.property(Boolean.class).convention(false)
        }

        final DirectoryProperty getSourceFolder() {
            return sourceFolder
        }

        final DirectoryProperty getOutputDirectory() {
            return outputDirectory
        }

        final Property<Boolean> getIgnore() {
            return ignore
        }

        @InputDirectory
        private final DirectoryProperty sourceFolder
        @OutputDirectory
        private final DirectoryProperty outputDirectory
        @Input
        private final Property<Boolean> ignore
    }

    @Override
    OutputConfigurationDSL clone(ObjectFactory objects) {
        OutputConfigurationDSL clone = objects.newInstance(OutputConfigurationDSL.class, name)
        clone.description.set(this.description.get())
        clone.outputDirectory.set(this.outputDirectory.get())
        clone.createOutputDirectory.set(this.createOutputDirectory.get())
        clone.overrideExistingResources.set(this.overrideExistingResources.get())
        clone.installDslAsPrimarySource.set(this.installDslAsPrimarySource.get())
        clone.hideSyntheticLocalVariables.set(this.hideSyntheticLocalVariables.get())
        clone.canClearOutputDirectory.set(this.canClearOutputDirectory.get())
        clone.cleanUpDerivedResources.set(this.cleanUpDerivedResources.get())
        this.sourceMappings.get().each { sourceMapping ->
            SourceMappingDSL clonedSourceMapping = objects.newInstance(SourceMappingDSL.class)
            clonedSourceMapping.sourceFolder.set(sourceMapping.sourceFolder.get())
            clonedSourceMapping.outputDirectory.set(sourceMapping.outputDirectory.get())
            clonedSourceMapping.ignore.set(sourceMapping.ignore.get())
            clone.sourceMappings.add(clonedSourceMapping)
        }
        clone.setDerivedProperty.set(this.setDerivedProperty.get())
        clone.keepLocalHistory.set(this.keepLocalHistory.get())
        return clone
    }
}
