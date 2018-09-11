package io.michaelrocks.paranoid.processor

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName

class ObfuscatedTypeRegistryImpl(
        private val classRegistry: ClassRegistry
) : ObfuscatedTypeRegistry {
    override fun shouldObfuscate(type: Type.Object): Boolean {
        val mirror = findClassMirror(type) ?: return false
        if (OBFUSCATE_TYPE in mirror.annotations) {
            return true
        }

        return mirror.enclosingType?.let { shouldObfuscate(it) } ?: false
    }

    private fun findClassMirror(type: Type.Object): ClassMirror? {
        return try {
            classRegistry.getClassMirror(type)
        } catch (exception: IllegalArgumentException) {
            // Sometimes Kotlin generates erroneous bytecode with InnerClass attribute referencing an non-existent class.
            extractOuterType(type)?.let { findClassMirror(it) }
        }
    }

    private fun extractOuterType(type: Type.Object): Type.Object? {
        val internalName = type.internalName
        val outerInternalName = internalName.substringBeforeLast('$', "")
        if (outerInternalName.isEmpty()) {
            return null
        }

        return getObjectTypeByInternalName(outerInternalName)
    }
}
