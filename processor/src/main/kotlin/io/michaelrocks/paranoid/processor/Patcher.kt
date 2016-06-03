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

package io.michaelrocks.paranoid.processor

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.paranoid.processor.logging.getLogger
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File

class Patcher(private val stringRegistry: StringRegistry) {
  private val logger = getLogger()

  fun copyAndPatchClasses(inputPath: File, outputPath: File, analysisResult: AnalysisResult) {
    for (sourceFile in inputPath.walk()) {
      val relativePath = sourceFile.toRelativeString(inputPath)
      val targetFile = File(outputPath, relativePath)
      if (sourceFile.isFile) {
        val type = getObjectTypeFromFile(relativePath)
        val configuration = analysisResult.configurationsByType[type]
        if (configuration != null) {
          patchClass(sourceFile, targetFile, analysisResult.configurationsByType[type]!!)
        } else {
          sourceFile.parentFile.mkdirs()
          sourceFile.copyTo(targetFile, true)
        }
      } else {
        targetFile.mkdirs()
      }
    }
  }

  private fun getObjectTypeFromFile(relativePath: String): Type.Object? {
    if (relativePath.endsWith(".class")) {
      val internalName = relativePath.substringBeforeLast(".class")
      return getObjectTypeByInternalName(internalName)
    }
    return null
  }

  private fun patchClass(sourceFile: File, targetFile: File, configuration: ClassConfiguration) {
    configuration.dump()
    val reader = ClassReader(sourceFile.readBytes())
    val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
    val stringLiteralsPatcher = StringLiteralsClassPatcher(stringRegistry, writer)
    val stringConstantsPatcher = StringConstantsClassPatcher(configuration, stringLiteralsPatcher)
    reader.accept(stringConstantsPatcher, ClassReader.SKIP_FRAMES)
    targetFile.parentFile.mkdirs()
    targetFile.writeBytes(writer.toByteArray())
  }

  private fun ClassConfiguration.dump() {
    logger.info("Patching class {}...", container)
    if (constantStringsByFieldName.isNotEmpty()) {
      logger.info("  Constants:")
      constantStringsByFieldName.forEach { field ->
        logger.info("  - {} = \"{}\"", field.key, field.value)
      }
    }
  }
}
