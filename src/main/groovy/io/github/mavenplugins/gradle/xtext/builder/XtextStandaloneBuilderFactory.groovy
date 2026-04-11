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

package io.github.mavenplugins.gradle.xtext.builder

import io.github.mavenplugins.gradle.xtext.plugin.internal.GradleRuntimeFilteringClassLoader
import io.github.mavenplugins.gradle.xtext.plugin.builderinterface.IXtextStandaloneBuilder
import io.github.mavenplugins.gradle.xtext.plugin.builderinterface.IXtextStandaloneBuilderFactory
import io.github.mavenplugins.gradle.xtext.plugin.dsl.LanguageDSL
import io.github.mavenplugins.gradle.xtext.plugin.internal.guava.Preconditions
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.logging.Logger

class XtextStandaloneBuilderFactory implements IXtextStandaloneBuilderFactory {

    XtextStandaloneBuilderFactory() {
        Preconditions.checkState(getClass().getClassLoader().getParent() instanceof GradleRuntimeFilteringClassLoader,
                "${XtextStandaloneBuilderFactory} must be loaded via child classloader of ${GradleRuntimeFilteringClassLoader}")
    }
    @Override
    IXtextStandaloneBuilder get(NamedDomainObjectContainer<LanguageDSL> languageDSLs, URLClassLoader xtextClassLoader, Logger logger){
        IXtextStandaloneBuilder builder = new XtextStandaloneBuilder(logger)
        builder.setLanguages(languageDSLs)
        return builder
    }
}
