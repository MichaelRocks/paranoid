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
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.File

class Patcher(
    private val deobfuscator: Deobfuscator,
    private val stringRegistry: StringRegistry,
    private val analysisResult: AnalysisResult,
    private val classRegistry: ClassRegistry
) {
  private val logger = getLogger()

  fun copyAndPatchClasses(inputs: List<File>, outputs: List<File>) {
    for (index in inputs.indices) {
      copyAndPatchClasses(inputs[index], outputs[index])
    }
  }

  private fun copyAndPatchClasses(inputPath: File, outputPath: File) {
    logger.info("Patching...")
    logger.info("   Input: {}", inputPath)
    logger.info("  Output: {}", outputPath)
    // FIXME: Support JARs here.
    for (sourceFile in inputPath.walk()) {
      val relativePath = sourceFile.toRelativeString(inputPath)
      val targetFile = File(outputPath, relativePath)
      if (sourceFile.isFile) {
        getObjectTypeFromFile(relativePath)?.let { type ->
          copyAndPatchClass(sourceFile, targetFile, type)
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

  private fun copyAndPatchClass(sourceFile: File, targetFile: File, type: Type.Object) {
    if (!maybePatchClass(sourceFile, targetFile, type)) {
      sourceFile.parentFile.mkdirs()
      sourceFile.copyTo(targetFile, true)
    }
  }

  private fun maybePatchClass(sourceFile: File, targetFile: File, type: Type.Object): Boolean {
    val configuration = analysisResult.configurationsByType[type]
    val hasObfuscateAnnotation = OBFUSCATE_TYPE in classRegistry.getClassMirror(type).annotations
    if (configuration == null && !hasObfuscateAnnotation) {
      return false
    }

    logger.debug("Patching class...")
    logger.debug("  Source: {}", sourceFile)
    logger.debug("  Target: {}", targetFile)
    val reader = ClassReader(sourceFile.readBytes())
    val writer = StandaloneClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, classRegistry)
    val patcher =
        writer
            .wrapIf(hasObfuscateAnnotation) { RemoveObfuscateClassPatcher(it) }
            .wrapIf(configuration != null) { StringLiteralsClassPatcher(deobfuscator, stringRegistry, it) }
            .wrapIf(configuration != null) { StringConstantsClassPatcher(configuration!!, it) }
    reader.accept(patcher, ClassReader.SKIP_FRAMES)
    targetFile.parentFile.mkdirs()
    targetFile.writeBytes(writer.toByteArray())
    return true
  }

  private inline fun ClassVisitor.wrapIf(condition: Boolean, wrapper: (ClassVisitor) -> ClassVisitor): ClassVisitor {
    return if (condition) wrapper(this) else this
  }
}
