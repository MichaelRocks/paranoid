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

package io.michaelrocks.paranoid.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.BaseExtension
import io.michaelrocks.paranoid.processor.ParanoidProcessor
import java.io.File
import java.security.SecureRandom
import java.util.EnumSet

class ParanoidTransform(
    private val paranoid: ParanoidExtension,
    private val android: BaseExtension
) : Transform() {
  override fun transform(invocation: TransformInvocation) {
    if (!invocation.isIncremental) {
      invocation.outputProvider.deleteAll()
    }

    val inputs = invocation.inputs.flatMap { it.jarInputs + it.directoryInputs }
    val outputs = inputs.map { input ->
      val format = if (input is JarInput) Format.JAR else Format.DIRECTORY
      invocation.outputProvider.getContentLocation(
          input.name,
          input.contentTypes,
          input.scopes,
          format
      )
    }

    if (!paranoid.isEnabled) {
      copyInputsToOutputs(inputs.map { it.file }, outputs)
      return
    }

    val processor = ParanoidProcessor(
        obfuscationSeed = paranoid.obfuscationSeed ?: SecureRandom().nextInt(),
        inputs = inputs.map { it.file },
        outputs = outputs,
        genPath = invocation.outputProvider.getContentLocation(
            "gen-paranoid",
            QualifiedContent.DefaultContentType.CLASSES,
            QualifiedContent.Scope.PROJECT,
            Format.DIRECTORY
        ),
        classpath = invocation.referencedInputs.flatMap {
          it.jarInputs.map { it.file } + it.directoryInputs.map { it.file }
        },
        bootClasspath = android.bootClasspath,
        projectName = invocation.context.path.replace(":transformClassesWithParanoidFor", ":").replace(':', '$')
    )

    try {
      processor.process()
    } catch (exception: Exception) {
      throw TransformException(exception)
    }
  }

  override fun getName(): String {
    return "paranoid"
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
    val scopes = EnumSet.of(QualifiedContent.Scope.PROJECT)
    if (paranoid.includeSubprojects) {
      scopes += QualifiedContent.Scope.SUB_PROJECTS
    }
    return scopes
  }

  override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
    val scopes =
        if (PluginVersion.major >= 3) {
          EnumSet.of(
              QualifiedContent.Scope.PROJECT,
              QualifiedContent.Scope.EXTERNAL_LIBRARIES,
              QualifiedContent.Scope.PROVIDED_ONLY
          )
        } else {
          @Suppress("DEPRECATION")
          EnumSet.of(
              QualifiedContent.Scope.PROJECT,
              QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
              QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
              QualifiedContent.Scope.PROVIDED_ONLY
          )
        }
    if (!paranoid.includeSubprojects) {
      scopes += QualifiedContent.Scope.SUB_PROJECTS
    }
    return scopes
  }

  override fun isIncremental(): Boolean {
    return false
  }

  override fun getParameterInputs(): MutableMap<String, Any> {
    return mutableMapOf(
        "version" to Build.VERSION,
        "enabled" to paranoid.isEnabled,
        "includeSubprojects" to paranoid.includeSubprojects
    )
  }

  private fun TransformOutputProvider.getContentLocation(
      name: String,
      contentType: QualifiedContent.ContentType,
      scope: QualifiedContent.Scope,
      format: Format
  ): File {
    return getContentLocation(name, setOf(contentType), EnumSet.of(scope), format)
  }

  private fun copyInputsToOutputs(inputs: List<File>, outputs: List<File>) {
    inputs.zip(outputs) { input, output ->
      input.copyRecursively(output, overwrite = true)
    }
  }
}
