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

import io.michaelrocks.paranoid.processor.logging.getLogger
import io.michaelrocks.paranoid.processor.model.Deobfuscator
import java.io.File
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.StandardLocation
import javax.tools.ToolProvider

class Generator(
    private val deobfuscator: Deobfuscator,
    private val stringRegistry: StringRegistry
) {
  private val logger = getLogger()

  fun generateDeobfuscator(
      sourcePath: File,
      genPath: File,
      classpath: Collection<File>,
      bootClasspath: Collection<File>
  ) {
    val sourceCode = generateDeobfuscatorSourceCode()
    val sourceFile = File(sourcePath, "${deobfuscator.type.internalName}.java")

    sourceFile.parentFile.mkdirs()
    genPath.mkdirs()

    sourceFile.writeBytes(sourceCode.toByteArray())

    logger.info("Compiling {}", sourceFile)
    logger.debug("Source code:\n{}", sourceCode)

    val compiler = ToolProvider.getSystemJavaCompiler()
    val diagnostics = DiagnosticCollector<JavaFileObject>()
    val fileManager = compiler.getStandardFileManager(diagnostics, null, null)
    fileManager.setLocation(StandardLocation.SOURCE_PATH, listOf(sourcePath))
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT, listOf(genPath))
    fileManager.setLocation(StandardLocation.CLASS_PATH, classpath)
    fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, bootClasspath)
    val options = listOf("-g", "-source", "6", "-target", "6")
    val compilationUnits = fileManager.getJavaFileObjects(sourceFile)
    val task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits)
    try {
      if (!task.call()) {
        diagnostics.diagnostics.forEach {
          logger.error(
              "Compilation error: {}:{}:{}: {}", it.source.name, it.lineNumber, it.columnNumber, it.getMessage(null)
          )
        }

        val message = diagnostics.diagnostics.joinToString(separator = "\n", prefix = "Compilation error:\n") {
          "%s:%d:%d: %s".format(it.source.name, it.lineNumber, it.columnNumber, it.getMessage(null))
        }
        throw ParanoidException(message)
      }
    } catch (exception: Exception) {
      logger.error("Compilation error", exception)
      throw ParanoidException("Compilation error", exception)
    }
  }

  private fun generateDeobfuscatorSourceCode(): String {
    val strings = stringRegistry.getAllStrings().toList()
    val joinedStrings = strings.joinToString(separator = "")
    val indexesByChar = joinedStrings
        .toHashSet()
        .shuffled()
        .withIndex()
        .associateBy(
            { it.value },
            { it.index }
        )

    val chars = CharArray(indexesByChar.size)
    indexesByChar.forEach { (char, index) -> chars[index] = char }

    val indexes = ShortArray(joinedStrings.length) { indexesByChar[joinedStrings[it]]!!.toShort() }
    val offsets = IntArray(strings.size + 1)
    strings.forEachIndexed { index, string ->
      val lastIndex = offsets[index]
      offsets[index + 1] = lastIndex + string.length
    }

    return buildString {
      val internalName = deobfuscator.type.internalName
      val packageName = internalName.substringBeforeLast('/').replace('/', '.')
      val className = internalName.substringAfterLast('/')
      appendln("package $packageName;")
      appendln()
      appendln("public class $className {")
      appendln("  private static final String chars = ${createStringLiteral(chars)};")
      appendln("  private static final String indexes = ${createStringLiteral(indexes)};")
      appendln("  private static final String locations = ${createStringLiteral(offsets)};")
      appendln()
      appendln("  public static String ${deobfuscator.deobfuscationMethod.name}(final int id) {")
      appendln("    final int offset1Low = locations.charAt(2 * id) & 0xffff;")
      appendln("    final int offset1High = locations.charAt(2 * id + 1) & 0xffff;")
      appendln("    final int offset1 = (offset1High << 16) | offset1Low;")
      appendln("    final int offset2Low = locations.charAt(2 * id + 2);")
      appendln("    final int offset2High = locations.charAt(2 * id + 3);")
      appendln("    final int offset2 = (offset2High << 16) | offset2Low;")
      appendln("    final int length = offset2 - offset1;")
      appendln("    final char[] stringChars = new char[length];")
      appendln("    for (int i = 0; i < length; ++i) {")
      appendln("      final int index = indexes.charAt(offset1 + i) & 0xffff;")
      appendln("      stringChars[i] = chars.charAt(index);")
      appendln("    }")
      appendln("    return new String(stringChars);")
      appendln("  }")
      appendln("}")
    }
  }

  companion object {
    private fun createStringLiteral(array: CharArray): String {
      return buildString {
        append("\"")
        array.forEach {
          append(it.toLiteral())
        }
        append("\"")
      }
    }

    private fun createStringLiteral(array: ShortArray): String {
      return buildString {
        append("\"")
        array.forEach {
          val char = it.toChar()
          append(char.toLiteral())
        }
        append("\"")
      }
    }

    private fun createStringLiteral(array: IntArray): String {
      return buildString {
        append("\"")
        array.forEach {
          val char1 = (it and 0xffff).toChar()
          val char2 = (it ushr 16).toChar()
          append(char1.toLiteral())
          append(char2.toLiteral())
        }
        append("\"")
      }
    }

    // https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.10.4
    private fun Char.toLiteral(): String {
      return when (this) {
        '\n' -> "\\n"
        '\r' -> "\\r"
        '\'' -> "\\'"
        '\"' -> "\\\""
        '\\' -> "\\\\"
        else -> "\\u%04x".format(toShort())
      }
    }
  }
}
