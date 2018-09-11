package io.michaelrocks.paranoid.processor

import io.michaelrocks.grip.mirrors.Type

interface ObfuscatedTypeRegistry {
    fun shouldObfuscate(type: Type.Object): Boolean
}
