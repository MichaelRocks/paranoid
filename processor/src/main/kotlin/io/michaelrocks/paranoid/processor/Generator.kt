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

import io.michaelrocks.paranoid.processor.logging.getLogger
import java.io.File
import java.util.Collections
import java.util.HashSet
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.StandardLocation
import javax.tools.ToolProvider

class Generator(private val stringRegistry: StringRegistry) {
  private val logger = getLogger()

  fun generateDeobfuscator(
      sourcePath: File,
      outputPath: File,
      classpath: Collection<File>,
      bootClasspath: Collection<File>
  ) {
    val sourceCode = generateDeobfuscatorSourceCode()
    val sourceFile = File(sourcePath, "${DEOBFUSCATOR_TYPE.internalName}.java")
    sourceFile.parentFile.mkdirs()
    sourceFile.writeBytes(sourceCode.toByteArray())

    logger.info("Compiling {}", sourceFile)
    logger.debug("Source code:\n{}", sourceCode)

    val compiler = ToolProvider.getSystemJavaCompiler()
    val diagnostics = DiagnosticCollector<JavaFileObject>()
    val fileManager = compiler.getStandardFileManager(diagnostics, null, null)
    fileManager.setLocation(StandardLocation.SOURCE_PATH, listOf(sourcePath))
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT, listOf(outputPath))
    fileManager.setLocation(StandardLocation.CLASS_PATH, classpath + listOf(outputPath))
    fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, bootClasspath)
    val options = listOf("-g", "-source", "6", "-target", "6")
    val compilationUnits = fileManager.getJavaFileObjects(sourceFile)
    val task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits)
    try {
      if (!task.call()) {
        diagnostics.diagnostics.forEach {
          logger.error(
              "Compilation error: {}:{}:{}: {}", it.source.name, it.lineNumber, it.columnNumber, it.getMessage(null))
        }
      }
    } catch (exception: Exception) {
      logger.error("Compilation error", exception)
    }
  }

  private fun generateDeobfuscatorSourceCode(): String {
    val strings = stringRegistry.getAllStrings()
    val indexesByChar = strings
        .fold(HashSet<Char>()) { set, string ->
          string.forEach { set += it }
          set
        }
        .toMutableList()
        .apply { Collections.shuffle(this) }
        .withIndex()
        .associateBy(
            { it.value },
            { it.index }
        )

    return buildString {
      val internalName = DEOBFUSCATOR_TYPE.internalName
      val packageName = internalName.substringBeforeLast('/').replace('/', '.')
      val className = internalName.substringAfterLast('/')
      appendln("package $packageName;")
      appendln()
      appendln("public class $className {")
      appendln("  private static final char[] chars = new char[] {")
      indexesByChar.keys.joinTo(this, prefix = "    ", postfix = "\n") { it.toLiteral() }
      appendln("  };")
      appendln("  private static final short[][] indexes = new short[][] {")
      strings
          .map { string ->
            string.toCharArray()
                .map { char -> indexesByChar[char] }
                .joinToString(prefix = "    { ", postfix = " }")
          }
          .joinTo(this, separator = ",\n", postfix = "\n")
      appendln("  };")
      appendln("  public static String ${DEOBFUSCATION_METHOD.name}(final int id) {")
      appendln("    final short[] stringIndexes = indexes[id];")
      appendln("    final char[] stringChars = new char[stringIndexes.length];")
      appendln("    for (int i = 0; i < stringIndexes.length; ++i) {")
      appendln("      stringChars[i] = chars[stringIndexes[i]];")
      appendln("    }")
      appendln("    return new String(stringChars);")
      appendln("  }")
      appendln("}")
    }
  }

  // https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.10.4
  private fun Char.toLiteral(): String {
    return when (this) {
      '\n' -> "'\\n'"
      '\r' -> "'\\r'"
      else -> "'\\u%04x'".format(toShort())
    }
  }
}
