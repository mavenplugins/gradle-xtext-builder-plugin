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

package io.github.mavenplugins.gradle.xtext.plugin.internal;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inspired by https://github.com/xtext/xtext-gradle-plugin/blob/master/xtext-gradle-plugin/src/main/java/org/xtext/gradle/tasks/internal/FilteringClassLoader.xtend
 *
 * @author Christian Dietrich - Initial contribution and API
 */
public class GradleRuntimeFilteringClassLoader extends ClassLoader {

    static final char DOT = '.';
    static final char SLASH = '/';

    private final List<String> includes;
    private final List<String> resourceIncludes;

    private final List<String> excludes;
    private final List<String> resourceExcludes;

    public GradleRuntimeFilteringClassLoader(ClassLoader parent, List<String> includes, List<String> excludes) {
        super(parent);
        this.includes = includes.stream().map(it -> it + DOT).collect(Collectors.toList());
        this.resourceIncludes = includes.stream().map(it -> it.replace(DOT, SLASH) + SLASH).collect(Collectors.toList());
        this.excludes = excludes.stream().map(it -> it + DOT).collect(Collectors.toList());
        this.resourceExcludes = excludes.stream().map(it -> it.replace(DOT, SLASH) + SLASH).collect(Collectors.toList());
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            ClassLoader systemParentLoader = ClassLoader.getSystemClassLoader().getParent();
            if (systemParentLoader != null) {
                return systemParentLoader.loadClass(name);
            }
        } catch (ClassNotFoundException ignored) {
        }

        if (isClassToLoadByGradleClassLoader(name)) {
            Class<?> result = super.loadClass(name, false);
            if (resolve) {
                resolveClass(result);
            }
            return result;
        } else {
            throw new ClassNotFoundException(name);
        }
    }

    public URL getResource(String name) {
        ClassLoader systemParentLoader = ClassLoader.getSystemClassLoader().getParent();
        if (systemParentLoader != null) {
            URL result = systemParentLoader.getResource(name);
            if (result != null) {
                return result;
            }
        }
        if (isResourceToGetByGradleClassloader(name)) {
            return super.getResource(name);
        }
        return null;
    }

    private boolean isClassToLoadByGradleClassLoader(String name) {
        for (String it : excludes) {
            if (name.startsWith(it)) return false;
        }
        for (String it : includes) {
            if (name.startsWith(it)) return true;
        }
        return false;
    }

    private boolean isResourceToGetByGradleClassloader(String name) {
        for (String it : resourceExcludes) {
            if (name.startsWith(it)) return false;
        }
        for (String it : resourceIncludes) {
            if (name.startsWith(it)) return true;
        }
        return false;
    }

}
