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

package io.michaelrocks.paranoid.processor

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.paranoid.processor.logging.getLogger
import io.michaelrocks.paranoid.processor.model.Deobfuscator
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File

class Patcher(
    private val deobfuscator: Deobfuscator,
    private val stringRegistry: StringRegistry,
    private val classRegistry: ClassRegistry
) {
  private val logger = getLogger()

  fun copyAndPatchClasses(inputs: List<File>, outputs: List<File>, analysisResult: AnalysisResult) {
    for (index in inputs.indices) {
      copyAndPatchClasses(inputs[index], outputs[index], analysisResult)
    }
  }

  private fun copyAndPatchClasses(inputPath: File, outputPath: File, analysisResult: AnalysisResult) {
    logger.info("Patching...")
    logger.info("   Input: {}", inputPath)
    logger.info("  Output: {}", outputPath)
    // FIXME: Support JARs here.
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
      val internalName = relativePath.substringBeforeLast(".class").replace('\\', '/')
      return getObjectTypeByInternalName(internalName)
    }
    return null
  }

  private fun patchClass(sourceFile: File, targetFile: File, configuration: ClassConfiguration) {
    logger.debug("Patching class...")
    logger.debug("  Source: {}", sourceFile)
    logger.debug("  Target: {}", targetFile)
    val reader = ClassReader(sourceFile.readBytes())
    val writer = StandaloneClassWriter(reader, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, classRegistry)
    val stringLiteralsPatcher = StringLiteralsClassPatcher(deobfuscator, stringRegistry, writer)
    val stringConstantsPatcher = StringConstantsClassPatcher(configuration, stringLiteralsPatcher)
    reader.accept(stringConstantsPatcher, ClassReader.SKIP_FRAMES)
    targetFile.parentFile.mkdirs()
    targetFile.writeBytes(writer.toByteArray())
  }
}
