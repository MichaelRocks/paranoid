/*
 * Copyright 2021 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.michaelrocks.paranoid.processor

import com.joom.grip.ClassRegistry
import com.joom.grip.Grip
import com.joom.grip.mirrors.Type
import com.joom.grip.mirrors.Typed
import com.joom.grip.objectType

fun newObfuscatedTypeRegistry(classRegistry: ClassRegistry): ObfuscatedTypeRegistry {
  return ObfuscatedTypeRegistryImpl(classRegistry)
}

fun ObfuscatedTypeRegistry.withCache(): ObfuscatedTypeRegistry {
  return this as? CachedObfuscatedTypeRegistry ?: CachedObfuscatedTypeRegistry(this)
}

fun ObfuscatedTypeRegistry.shouldObfuscate(): (Grip, Typed<Type.Object>) -> Boolean {
  return objectType { _, type -> shouldObfuscate(type) }
}
