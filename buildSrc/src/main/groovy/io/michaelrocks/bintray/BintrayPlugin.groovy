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

package io.michaelrocks.bintray

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

class BintrayPlugin implements Plugin<Project> {
  private Project project
  private Logger logger

  @Override
  void apply(final Project project) {
    this.project = project
    this.logger = project.logger

    project.extensions.create("maven", BintrayPluginExtension)

    project.apply plugin: 'java'
    project.apply plugin: 'maven-publish'
    project.apply plugin: 'com.jfrog.bintray'

    project.afterEvaluate {
      configureBintrayPublishing()
    }
  }

  private void configureBintrayPublishing() {
    final boolean hasCredentials = project.hasProperty('bintrayUser') && project.hasProperty('bintrayKey')
    if (hasCredentials) {
      configureBintray()
      configureArtifacts()
    }
  }

  private void configureBintray() {
    project.bintray {
      user = project.property('bintrayUser')
      key = project.property('bintrayKey')

      publications = ['mavenJava']

      dryRun = project.dryRun
      publish = project.publish
      pkg {
        repo = getRepositoryName()
        name = project.maven.artifactName ?: project.rootProject.name + '-' + project.name

        version {
          released = new Date()
          vcsTag = "v${project.version}"
        }
      }
    }
  }

  private void configureArtifacts() {
    project.task('sourcesJar', type: Jar, dependsOn: project.classes) {
      from project.sourceSets.main.allSource
    }

    project.task('javadocJar', type: Jar, dependsOn: project.javadoc) {
      from project.javadoc.destinationDir
    }

    project.artifacts {
      archives project.sourcesJar, project.javadocJar
    }

    project.publishing {
      publications {
        mavenJava(MavenPublication) {
          artifactId project.bintray.pkg.name
          if (project.plugins.hasPlugin('war')) {
            from project.components.web
          } else {
            from project.components.java
          }

          artifact project.sourcesJar {
            classifier = 'sources'
          }
          artifact project.javadocJar {
            classifier = 'javadoc'
          }
        }
      }
    }
  }

  private String getRepositoryName() {
    return project.maven.repository ?: 'maven'
  }
}
