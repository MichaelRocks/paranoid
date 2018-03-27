/*
 * Copyright 2018 Michael Rozumyanskiy
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

package io.michaelrocks.publish

import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyConstraint
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.plugins.JavaPlugin

class DependencyResolver private constructor(
    private val project: Project
) {
  private val mapping = buildDependencyNotationMapping()
  private val builder = DependencyResolutionResult.Builder()

  private fun buildDependencyNotationMapping(): Map<String, DependencyNotation> {
    val mapping = HashMap<String, DependencyNotation>()
    buildDependencyNotationMapping(project.rootProject, mapping)
    return mapping
  }

  private fun buildDependencyNotationMapping(project: Project, mapping: MutableMap<String, DependencyNotation>) {
    project.plugins.findPlugin(PublishPlugin::class.java)?.let { plugin ->
      val group = project.group.toString()
      val name = plugin.getArtifactName()
      val version = project.version.toString()
      val notation = DependencyNotation(group, name, version)
      mapping.put(project.path, notation)
    }

    project.subprojects.forEach { subproject ->
      buildDependencyNotationMapping(subproject, mapping)
    }
  }

  private fun resolve(): DependencyResolutionResult {
    resolve(project)
    return builder.build()
  }

  private fun resolve(project: Project) {
    project.configurations.forEach { configuration ->
      val scope = Scope.fromConfigurationName(configuration.name)
      if (scope != null) {
        configuration.dependencies.forEach { resolve(it, scope) }
        configuration.dependencyConstraints.forEach { resolve(it, scope) }
      }
    }
  }

  private fun resolve(dependency: Dependency, scope: Scope) {
    when (dependency) {
      is ProjectDependency -> {
        val project = dependency.dependencyProject
        val projectDependencyNotation = mapping[project.path]!!
        builder.addDependencyNotation(scope, projectDependencyNotation)
        if (scope == Scope.RELOCATE) {
          val originalProjectDependencyNotation =
              DependencyNotation(project.group.toString(), project.name, project.version.toString())
          builder.addDependencyMapping(originalProjectDependencyNotation, projectDependencyNotation)
          resolve(project)
        }
      }
      is SelfResolvingDependency -> {
        // Do nothing.
      }
      else -> {
        val notation = DependencyNotation(dependency.group!!, dependency.name, dependency.version!!)
        builder.addDependencyNotation(scope, notation)
      }
    }
  }

  private fun resolve(dependencyConstraint: DependencyConstraint, scope: Scope) {
    val notation = DependencyNotation(dependencyConstraint.group, dependencyConstraint.name, dependencyConstraint.version)
    builder.addDependencyNotation(scope, notation)
  }

  data class DependencyResolutionResult(
      val scopeToNotationsMap: Map<Scope, List<DependencyNotation>>,
      val dependencyToDependencyMap: Map<DependencyNotation, DependencyNotation>
  ) {
    class Builder {
      private val scopeToNotationsMap = mutableMapOf<Scope, MutableList<DependencyNotation>>()
      private val dependencyToDependencyMap = mutableMapOf<DependencyNotation, DependencyNotation>()

      fun addDependencyNotation(scope: Scope, notation: DependencyNotation) = apply {
        val notations = scopeToNotationsMap.getOrPut(scope) { mutableListOf() }
        notations += notation
      }

      fun addDependencyMapping(source: DependencyNotation, target: DependencyNotation) = apply {
        dependencyToDependencyMap[source] = target
      }

      fun build(): DependencyResolutionResult {
        val notationToVersionMap =
            scopeToNotationsMap
                .flatMap { it.value }
                .groupBy(
                    { it.copy(version = "") },
                    { DefaultArtifactVersion(it.version) }
                )
                .mapValues { it.value.max() }

        val processedNotations = mutableSetOf<DependencyNotation>()
        val scopeToResolvedNotationsMap = mutableMapOf<Scope, List<DependencyNotation>>()
        for (scope in Scope.values()) {
          scopeToNotationsMap[scope]?.let { dependencies ->
            scopeToResolvedNotationsMap[scope] = dependencies.mapNotNull {
              val notation = it.copy(version = "")
              if (processedNotations.add(notation)) {
                val version = notationToVersionMap[notation]!!
                notation.copy(version = version.toString())
              } else {
                null
              }
            }
          }
        }

        return DependencyResolutionResult(scopeToResolvedNotationsMap, dependencyToDependencyMap.toMap())
      }
    }
  }

  data class DependencyNotation(
      val group: String,
      val name: String,
      val version: String
  )

  enum class Scope {
    RELOCATE,
    COMPILE,
    RUNTIME,
    PROVIDED;

    companion object {
      @Suppress("DEPRECATION")
      fun fromConfigurationName(configurationName: String): Scope? {
        return when (configurationName) {
          PublishPlugin.RELOCATE_CONFIGURATION_NAME ->
            RELOCATE
          JavaPlugin.COMPILE_CONFIGURATION_NAME,
          JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
          JavaPlugin.API_CONFIGURATION_NAME ->
            COMPILE
          JavaPlugin.RUNTIME_CONFIGURATION_NAME ->
            RUNTIME
          JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME,
          JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME ->
            PROVIDED
          else -> null
        }
      }
    }
  }

  companion object {
    fun resolve(project: Project): DependencyResolutionResult {
      return DependencyResolver(project).resolve()
    }
  }
}
