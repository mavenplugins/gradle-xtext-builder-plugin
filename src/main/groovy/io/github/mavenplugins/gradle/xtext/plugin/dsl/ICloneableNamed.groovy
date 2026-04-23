package io.github.mavenplugins.gradle.xtext.plugin.dsl

import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory

interface ICloneableNamed<T> extends Named {
    T clone(ObjectFactory objects)
}