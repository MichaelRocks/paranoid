/*
 * Copyright 2017 Michael Rozumyanskiy
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
      genPath: File,
      classpath: Collection<File>,
      bootClasspath: Collection<File>
  ) {
    val sourceCode = generateDeobfuscatorSourceCode()
    val sourceFile = File(sourcePath, "${DEOBFUSCATOR_TYPE.internalName}.java")

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

    val indexes = strings.joinToString(separator = "").map { indexesByChar[it] }
    val offsets = strings.
        foldIndexed(IntArray(strings.size + 1)) { index, offsets, string ->
          offsets[index + 1] = offsets[index] + string.length
          offsets
        }

    val charCount = indexesByChar.size
    val indexCount = indexes.size
    val stringCount = strings.size
    val locationCount = stringCount * 2

    val chunkSize = 1024
    val charChunkCount = (charCount + chunkSize - 1) / chunkSize
    val indexChunkCount = (indexCount + chunkSize - 1) / chunkSize
    val locationChunkCount = (stringCount + chunkSize - 1) / chunkSize

    return buildString {
      val internalName = DEOBFUSCATOR_TYPE.internalName
      val packageName = internalName.substringBeforeLast('/').replace('/', '.')
      val className = internalName.substringAfterLast('/')
      appendln("package $packageName;")
      appendln()
      appendln("public class $className {")
      appendln("  private static final char[] chars = new char[$charCount];")
      appendln("  private static final short[] indexes = new short[$indexCount];")
      appendln("  private static final int[] locations = new int[$locationCount];")
      appendln()
      appendln("  static {")
      repeat(charChunkCount) { chunkIndex ->
        appendln("    fillChars$chunkIndex();")
      }
      repeat(indexChunkCount) { chunkIndex ->
        appendln("    fillIndexes$chunkIndex();")
      }
      repeat(locationChunkCount) { chunkIndex ->
        appendln("    fillLocations$chunkIndex();")
      }
      appendln("  }")

      val charsWithIndexes = indexesByChar.entries.toList()
      repeat(charChunkCount) { chunkIndex ->
        appendln()
        appendln("  public static void fillChars$chunkIndex() {")
        appendln("    final char[] array = chars;")
        forEachInChunk(chunkIndex, chunkSize, charCount) { entryIndex ->
          val (char, index) = charsWithIndexes[entryIndex]
          val literal = char.toLiteral()
          appendln("    array[$index] = $literal;")
        }
        appendln("  }")
      }

      repeat(indexChunkCount) { chunkIndex ->
        appendln()
        appendln("  public static void fillIndexes$chunkIndex() {")
        appendln("    final short[] array = indexes;")
        forEachInChunk(chunkIndex, chunkSize, indexCount) { indexIndex ->
          val index = indexes[indexIndex]
          appendln("    array[$indexIndex] = $index;")
        }
        appendln("  }")
      }

      repeat(locationChunkCount) { chunkIndex ->
        appendln()
        appendln("  public static void fillLocations$chunkIndex() {")
        appendln("    final int[] array = locations;")
        forEachInChunk(chunkIndex, chunkSize, stringCount) { locationIndex ->
          val offset = offsets[locationIndex]
          val length = strings[locationIndex].length
          val offsetIndex = locationIndex * 2
          val lengthIndex = offsetIndex + 1
          appendln("    array[$offsetIndex] = $offset;")
          appendln("    array[$lengthIndex] = $length;")
        }
        appendln("  }")
      }

      appendln()
      appendln("  public static String ${DEOBFUSCATION_METHOD.name}(final int id) {")
      appendln("    final int offset = locations[id * 2];")
      appendln("    final int length = locations[id * 2 + 1];")
      appendln("    final char[] stringChars = new char[length];")
      appendln("    for (int i = 0; i < length; ++i) {")
      appendln("      stringChars[i] = chars[indexes[offset + i]];")
      appendln("    }")
      appendln("    return new String(stringChars);")
      appendln("  }")
      appendln("}")
    }
  }

  private inline fun forEachInChunk(chunkIndex: Int, chunkSize: Int, totalSize: Int, action: (Int) -> Unit) {
    val chunkIndexStart = chunkIndex * chunkSize
    val chunkIndexEnd = minOf((chunkIndex + 1) * chunkSize, totalSize)
    for (indexInChunk in chunkIndexStart until chunkIndexEnd) {
      action(indexInChunk)
    }
  }

  // https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.10.4
  private fun Char.toLiteral(): String {
    return when (this) {
      '\n' -> "'\\n'"
      '\r' -> "'\\r'"
      '\'' -> "'\\''"
      '\\' -> "'\\\\'"
      else -> "'\\u%04x'".format(toShort())
    }
  }
}
