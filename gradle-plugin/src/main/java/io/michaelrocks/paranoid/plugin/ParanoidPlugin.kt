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

import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException

class ParanoidPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    try {
      val android = project.extensions.getByName("android") as BaseExtension
      project.addDependencies(getDefaultConfiguration())
      android.registerTransform(ParanoidTransform(android))
    } catch (exception: UnknownDomainObjectException) {
      throw GradleException("Paranoid plugin must be applied *AFTER* Android plugin", exception)
    }
  }

  private fun getDefaultConfiguration(): String {
    return if (PluginVersion.major >= 3) "implementation" else "compile"
  }

  private fun Project.addDependencies(configurationName: String) {
    val version = Build.VERSION
    dependencies.add(configurationName, "io.michaelrocks:paranoid-core:$version")
  }
}
