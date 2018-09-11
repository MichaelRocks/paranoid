/*
 * Copyright 2017 Michael Rozumyanskiy
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

import com.android.builder.model.Version

object PluginVersion {
    val major: Int
    val minor: Int
    val patch: Int
    val suffix: String

    init {
        val version = Version.ANDROID_GRADLE_PLUGIN_VERSION
        suffix = version.substringAfter('-', "")
        val prefix = version.substringBefore('-')
        val parts = prefix.split('.', limit = 3)
        major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
    }
}
