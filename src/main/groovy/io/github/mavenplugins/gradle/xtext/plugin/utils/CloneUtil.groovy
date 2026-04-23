package io.github.mavenplugins.gradle.xtext.plugin.utils

import io.github.mavenplugins.gradle.xtext.plugin.dsl.ICloneableNamed
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory

class CloneUtil {
    private CloneUtil() {
        // prevent instantiation
    }

    static <T extends ICloneableNamed> NamedDomainObjectContainer<T> cloneNamedDomainObjectContainer(NamedDomainObjectContainer<T> sources, Class<T> clazz, ObjectFactory objects) {
        NamedDomainObjectContainer<T> clones = objects.domainObjectContainer(clazz)
        sources.each { T source ->
            clones.add(source.clone(objects))
        }
        return clones
    }

}
