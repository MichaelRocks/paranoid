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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import groovy.util.Node
import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import java.util.Date

class PublishPlugin : Plugin<Project> {
  private lateinit var project: Project
  private lateinit var logger: Logger
  private lateinit var extension: PublishPluginExtension

  override fun apply(project: Project) {
    this.project = project
    this.logger = project.logger
    this.extension = createPluginExtension()

    applyPlugins()
    createRelocateConfiguration()

    project.afterEvaluate {
      configureArtifacts()
    }

    project.gradle.addBuildListener(
        object : BuildAdapter() {
          override fun projectsEvaluated(gradle: Gradle) {
            val resolvedDependencies = DependencyResolver.resolve(project)
            if (extension.repackage) {
              copyTransitiveDependencies()
            }
            configureShadowJar(resolvedDependencies)
            configureBintrayPublishing(resolvedDependencies)
          }
        }
    )
  }

  fun getArtifactName(): String {
    return extension.artifactName ?: project.rootProject.name + '-' + project.name
  }

  private fun createPluginExtension(): PublishPluginExtension {
    return project.extensions.create("publish", PublishPluginExtension::class.java)
  }

  private fun applyPlugins() {
    project.plugins.apply("java")
    project.plugins.apply("maven-publish")
    project.plugins.apply("com.jfrog.bintray")
    project.plugins.apply("com.github.johnrengelman.shadow")
  }

  private fun createRelocateConfiguration() {
    val relocate = project.configurations.create(RELOCATE_CONFIGURATION_NAME)
    project.configurations.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME).extendsFrom(relocate)
    project.configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME).extendsFrom(relocate)
  }

  private fun copyTransitiveDependencies() {
    val relocate = project.configurations.getByName(RELOCATE_CONFIGURATION_NAME)
    relocate.dependencies.forEach { dependency ->
      processRelocateDependency(dependency)
    }
  }

  private fun processRelocateDependency(dependency: Dependency) {
    if (dependency is ProjectDependency) {
      copyTransitiveDependenciesFromSubproject(dependency.dependencyProject)
    }
  }

  private fun copyTransitiveDependenciesFromSubproject(subproject: Project) {
    val projectShadowJar = project.tasks.getByName(SHADOW_JAR_TASK_NAME) as ShadowJar
    val subprojectShadowJar = subproject.tasks.findByName(SHADOW_JAR_TASK_NAME) as ShadowJar?
    subprojectShadowJar?.relocators?.forEach {
      projectShadowJar.relocate(it)
    }

    subproject.configurations.forEach { configuration ->
      configuration.dependencies.forEach { dependency ->
        project.configurations.getByName(configuration.name).dependencies.add(dependency.copy())
        if (configuration.name == RELOCATE_CONFIGURATION_NAME) {
          processRelocateDependency(dependency)
        }
      }
    }
  }

  private fun configureArtifacts() {
    val sourceSets = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets

    val sourcesJar = project.tasks.create(SOURCES_JAR_TASK_NAME, Jar::class.java) { task ->
      task.dependsOn(project.tasks.getByName(JavaPlugin.CLASSES_TASK_NAME))
      task.from(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).allSource)
    }

    val javadocJar = project.tasks.create(JAVADOC_JAR_TASK_NAME, Jar::class.java) { task ->
      val javadoc = project.tasks.getByName(JavaPlugin.JAVADOC_TASK_NAME) as Javadoc
      task.dependsOn(javadoc)
      task.from(javadoc.destinationDir)
    }

    project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, sourcesJar)
    project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, javadocJar)
  }

  private fun configureShadowJar(resolvedDependencies: DependencyResolver.DependencyResolutionResult) {
    val shadowJar = project.tasks.getByName(SHADOW_JAR_TASK_NAME) as ShadowJar
    shadowJar.configurations = listOf(
        project.configurations.findByName(RELOCATE_CONFIGURATION_NAME)
    )

    val filter = shadowJar.dependencyFilter
    filter.include(resolvedDependencies.toIncludeSpec())
  }

  private fun DependencyResolver.DependencyResolutionResult.toIncludeSpec(): Spec<ResolvedDependency> {
    val dependenciesToRelocate = scopeToNotationsMap[DependencyResolver.Scope.RELOCATE] ?: emptyList()
    return Spec { dependency ->
      val originalNotation =
          DependencyResolver.DependencyNotation(dependency.moduleGroup, dependency.moduleName, dependency.moduleVersion)
      val notation = dependencyToDependencyMap[originalNotation] ?: originalNotation
      notation in dependenciesToRelocate
    }
  }

  private fun configureBintrayPublishing(resolvedDependencies: DependencyResolver.DependencyResolutionResult) {
    val hasCredentials = project.hasProperty("bintrayUser") && project.hasProperty("bintrayKey")
    if (hasCredentials) {
      configureBintray()
      configurePublications(resolvedDependencies)
    }
  }

  private fun configureBintray() {
    project.extensions.getByType(BintrayExtension::class.java).also { bintray ->
      bintray.user = project.property("bintrayUser")?.toString()
      bintray.key = project.property("bintrayKey")?.toString()

      bintray.setPublications("mavenJava")

      val extra = project.extensions.extraProperties
      bintray.dryRun = extra["dryRun"] as Boolean
      bintray.publish = extra["publish"] as Boolean

      bintray.pkg.also { pkg ->
        pkg.repo = extension.repository ?: "maven"
        pkg.name = getArtifactName()

        pkg.version.also { version ->
          version.released = Date().toString()
          version.vcsTag = "v$project.version"
        }
      }
    }
  }

  private fun configurePublications(resolvedDependencies: DependencyResolver.DependencyResolutionResult) {
    val bintray = project.extensions.getByType(BintrayExtension::class.java)
    val publishing = project.extensions.getByType(PublishingExtension::class.java)
    publishing.publications.create("mavenJava", MavenPublication::class.java) { publication ->
      publication.artifactId = bintray.pkg.name
      if (extension.repackage) {
        publication.artifact(project.tasks.getByName(SHADOW_JAR_TASK_NAME)) { artifact ->
          artifact.classifier = "repack"
        }
      }

      publication.artifact(project.tasks.getByName(JavaPlugin.JAR_TASK_NAME))

      publication.artifact(project.tasks.findByName(SOURCES_JAR_TASK_NAME)) { artifact ->
        artifact.classifier = "sources"
      }
      publication.artifact(project.tasks.findByName(JAVADOC_JAR_TASK_NAME)) { artifact ->
        artifact.classifier = "javadoc"
      }

      publication.pom.withXml { xml ->
        val root = xml.asNode()
        val dependencies = root.appendNode("dependencies")
        dependencies.addDependenciesToPom(resolvedDependencies)
      }
    }
  }

  private fun Node.addDependenciesToPom(resolvedDependencies: DependencyResolver.DependencyResolutionResult) {
    resolvedDependencies.scopeToNotationsMap.forEach { (scope, notations) ->
      if (scope != DependencyResolver.Scope.RELOCATE) {
        notations.forEach { addDependencyNode(it, scope.toMavenScope()) }
      }
    }
  }

  private fun Node.addDependencyNode(notation: DependencyResolver.DependencyNotation, scope: String) {
    appendNode("dependency").let {
      it.appendNode("groupId", notation.group)
      it.appendNode("artifactId", notation.name)
      it.appendNode("version", notation.version)
      it.appendNode("scope", scope)
    }
  }

  private fun DependencyResolver.Scope.toMavenScope(): String {
    return when (this) {
      DependencyResolver.Scope.RELOCATE -> error("Cannot add a Maven scope for $this")
      DependencyResolver.Scope.COMPILE -> "compile"
      DependencyResolver.Scope.RUNTIME -> "runtime"
      DependencyResolver.Scope.PROVIDED -> "provided"
    }
  }

  companion object {
    const val SHADOW_JAR_TASK_NAME = "shadowJar"
    const val SOURCES_JAR_TASK_NAME = "sourcesJar"
    const val JAVADOC_JAR_TASK_NAME = "javadocJar"
    const val RELOCATE_CONFIGURATION_NAME = "relocate"
  }
}
