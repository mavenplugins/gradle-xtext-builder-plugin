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

import org.eclipse.xtext.builder.standalone.ILanguageConfiguration
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

import javax.inject.Inject

/**
 * DSL configuration for {@link ILanguageConfiguration}.
 */
abstract class LanguageDSL implements ICloneableNamed<LanguageDSL> {

    @Input
    private final Property<Boolean> javaSupport
    final Property<Boolean> getJavaSupport() {
        return javaSupport
    }

    @Inject
    LanguageDSL(ObjectFactory objects) {
        this.javaSupport = objects.property(Boolean.class).convention(true)
    }

    @Input
    @Override
    abstract String getName()

    @Input
    abstract Property<String> getSetup()

    @Nested
    abstract NamedDomainObjectContainer<OutputConfigurationDSL> getOutputConfigurations()

    void outputConfigurations(Action<NamedDomainObjectContainer<OutputConfigurationDSL>> action) {
        action.execute(getOutputConfigurations())
    }

    @Override
    LanguageDSL clone(ObjectFactory objects) {
        LanguageDSL clone = objects.newInstance(LanguageDSL, name)
        clone.javaSupport.set(this.javaSupport.get())
        clone.setup.set(this.setup.get())
        this.outputConfigurations.each { outputConfig ->
            clone.outputConfigurations.add(outputConfig.clone(objects))
        }
        return clone
    }
}
