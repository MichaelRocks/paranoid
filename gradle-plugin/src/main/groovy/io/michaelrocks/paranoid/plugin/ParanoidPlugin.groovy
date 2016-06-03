/*
 * Copyright 2016 Michael Rozumyanskiy
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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

class ParanoidPlugin implements Plugin<Project>  {
  Project project
  Logger logger

  @Override
  void apply(final Project project) {
    this.project = project
    this.logger = project.logger

    if (project.hasProperty('android')) {
      addDependencies('compile')
      project.android.registerTransform(new ParanoidTransform(project))
    } else {
      throw new GradleException("Paranoid plugin must be applied *AFTER* Android plugin")
    }
  }

  void addDependencies(final String configurationName) {
    final String version = Build.VERSION
    project.dependencies.add(configurationName, "io.michaelrocks:paranoid-core:$version")
  }
}
