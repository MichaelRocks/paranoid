package io.michaelrocks.paranoid.processor

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.Typed
import io.michaelrocks.grip.objectType

fun newObfuscatedTypeRegistry(classRegistry: ClassRegistry): ObfuscatedTypeRegistry {
    return ObfuscatedTypeRegistryImpl(classRegistry)
}

fun ObfuscatedTypeRegistry.withCache(): ObfuscatedTypeRegistry {
    return this as? CachedObfuscatedTypeRegistry ?: CachedObfuscatedTypeRegistry(this)
}

fun ObfuscatedTypeRegistry.shouldObfuscate(): (Grip, Typed<Type.Object>) -> Boolean {
    return objectType { _, type -> shouldObfuscate(type) }
}
