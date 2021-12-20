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

package io.michaelrocks.paranoid.plugin

import io.michaelrocks.paranoid.processor.logging.getLogger

open class ParanoidExtension {
  @Deprecated(IS_ENABLED_DEPRECATION_WARNING)
  var isEnabled: Boolean = true
    set(value) {
      getLogger().warn("WARNING: $IS_ENABLED_DEPRECATION_WARNING")
      field = value
    }
  var isCacheable: Boolean = false
  var includeSubprojects: Boolean = false
  var obfuscationSeed: Int? = null
  var applyToBuildTypes: BuildType = BuildType.ALL
}

enum class BuildType {
  NONE,
  ALL,
  NOT_DEBUGGABLE
}

private const val IS_ENABLED_DEPRECATION_WARNING = "paranoid.enabled is deprecated. Use paranoid.applyToBuildTypes"