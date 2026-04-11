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

package io.github.mavenplugins.gradle.xtext.plugin.builderinterface

import io.github.mavenplugins.gradle.xtext.plugin.dsl.LanguageDSL
import io.github.mavenplugins.gradle.xtext.plugin.internal.GradleRuntimeFilteringClassLoader
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.logging.Logger

class XtextStandaloneBuilderProvider {

    static final Object lock = new Object()

    private XtextStandaloneBuilderProvider() {
        // not supposed to be instantiated
    }

    static IXtextStandaloneBuilder getBuilder(
            NamedDomainObjectContainer<LanguageDSL> languageDSLs,
            Set<File> xtextClasspath,
            List<String> gradleClassLoaderIncludes,
            List<String> gradleClassLoaderExcludes,
            Logger logger) {
        synchronized (lock) {
            final URLClassLoader builderClassLoader = getBuilderClassLoader(xtextClasspath, gradleClassLoaderIncludes, gradleClassLoaderExcludes)
            return createBuilder(languageDSLs, builderClassLoader, logger)
        }
    }

    private static URLClassLoader getBuilderClassLoader(Set<File> xtextClasspath, List<String> gradleClassLoaderIncludes, List<String> gradleClassLoaderExcludes) {
        URLClassLoader parent = XtextStandaloneBuilderProvider.classLoader as URLClassLoader
        //For debugging purpose only:
        //logClassLoaderURLs(parent, XtextStandaloneBuilderProvider)
        final GradleRuntimeFilteringClassLoader filteredClassLoader = new GradleRuntimeFilteringClassLoader(
                parent,
                gradleClassLoaderIncludes,
                gradleClassLoaderExcludes
        )
        final URL[] urls = xtextClasspath.collect { file ->
            try {
                file.toURI().toURL()
            } catch (MalformedURLException e) {
                throw new RuntimeException("Path ${file} failed to convert to a URL", e)
            }
        } as URL[]

        final URLClassLoader urlClassLoader = new URLClassLoader(urls, filteredClassLoader)
        // For debugging purpose only:
        //logClassLoaderURLs(urlClassLoader, IXtextStandaloneBuilderFactory)
        return urlClassLoader
    }

    private static IXtextStandaloneBuilder createBuilder(
            NamedDomainObjectContainer<LanguageDSL> languageDSLs,
            URLClassLoader builderClassLoader,
            Logger logger) {
        ServiceLoader<IXtextStandaloneBuilderFactory> loader = ServiceLoader.load(IXtextStandaloneBuilderFactory.class, builderClassLoader)
        Iterator<IXtextStandaloneBuilderFactory> providers = loader.iterator()
        if (providers.hasNext()) {
            return providers.next().get(languageDSLs, builderClassLoader, logger)
        } else {
            throw new IllegalStateException("No " + IXtextStandaloneBuilderFactory.class.getName() + " found on classpath.")
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    private static void logClassLoaderURLs(URLClassLoader classLoader, Class clazz) {
        List<URL> urls = classLoader.getURLs() as List
        String className = clazz.simpleName.replace("_Decorated", "")
        println "====== Class ${className} classLoader URLs[${urls.size()}] - BEGIN ======"
        urls.eachWithIndex { url, i ->
            println "    ${className} classpath url[${i}]=${url.file}"
        }
        println "====== Class ${className} classLoader URLs[${urls.size()}] - END ======"
    }
}

