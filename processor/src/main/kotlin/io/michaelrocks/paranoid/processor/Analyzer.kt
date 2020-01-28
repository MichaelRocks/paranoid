/*
 * Copyright 2020 Michael Rozumyanskiy
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

import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.and
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.fields
import io.michaelrocks.grip.from
import io.michaelrocks.grip.isFinal
import io.michaelrocks.grip.isStatic
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.withFieldInitializer
import java.io.File

class Analyzer(private val grip: Grip) {
  fun analyze(inputs: List<File>): AnalysisResult {
    val typesToObfuscate = findTypesToObfuscate(inputs)
    val obfuscationConfigurationsByType = typesToObfuscate.associateBy(
      { it },
      { createObfuscationConfiguration(it) }
    )
    return AnalysisResult(obfuscationConfigurationsByType)
  }

  private fun findTypesToObfuscate(inputs: List<File>): Set<Type.Object> {
    val registry = newObfuscatedTypeRegistry(grip.classRegistry).withCache()
    val query = grip select classes from inputs where registry.shouldObfuscate()
    return query.execute().types.toHashSet()
  }

  private fun createObfuscationConfiguration(type: Type.Object): ClassConfiguration {
    val fields = findConstantStringFields(type)
    val stringConstantsByName = fields.associateBy(
      { it.name },
      { it.value as String }
    )
    return ClassConfiguration(type, stringConstantsByName)
  }

  private fun findConstantStringFields(type: Type.Object): Collection<FieldMirror> {
    val mirror = grip.classRegistry.getClassMirror(type)
    val query = grip select fields from mirror where (isStatic() and isFinal() and withFieldInitializer<String>())
    return query.execute()[type].orEmpty()
  }
}
