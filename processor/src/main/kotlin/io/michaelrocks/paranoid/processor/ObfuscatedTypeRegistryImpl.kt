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
import com.joom.grip.mirrors.ClassMirror
import com.joom.grip.mirrors.Type
import com.joom.grip.mirrors.getObjectTypeByInternalName

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
