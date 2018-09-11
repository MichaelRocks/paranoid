package io.michaelrocks.paranoid.processor

import io.michaelrocks.grip.mirrors.Type

class CachedObfuscatedTypeRegistry(
        private val registry: ObfuscatedTypeRegistry
) : ObfuscatedTypeRegistry {
    private val cache = mutableMapOf<Type.Object, Boolean>()

    override fun shouldObfuscate(type: Type.Object): Boolean {
        return cache.getOrPut(type) {
            registry.shouldObfuscate(type)
        }
    }
}
