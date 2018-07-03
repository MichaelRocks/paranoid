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
import java.util.Collections
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
    @Suppress("JavaCollectionsStaticMethod")
    val indexesByChar = joinedStrings
        .toHashSet()
        .toMutableList()
        .also { Collections.shuffle(it) }
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
      appendln("  private static final String[] charChunks = new String[] {")
      appendln("      ${createStringLiteralsForChars(chars).joinToString(",\n")}")
      appendln("  };")
      appendln("  private static final String[] indexChunks = new String[] {")
      appendln("      ${createStringLiteralsForIndexes(indexes).joinToString(",\n")}")
      appendln("  };")
      appendln("  private static final String[] locationChunks = new String[] {")
      appendln("      ${createStringLiteralsForLocations(offsets).joinToString(",\n")}")
      appendln("  };")
      appendln()
      appendln("  public static String ${deobfuscator.deobfuscationMethod.name}(final int id) {")
      appendln("    final int location1ChunkIndex = id / $MAX_LOCATION_COUNT;")
      appendln("    final int location1Index = id % $MAX_LOCATION_COUNT;")
      appendln("    final int location2ChunkIndex = (id + 1) / $MAX_LOCATION_COUNT;")
      appendln("    final int location2Index = (id + 1) % $MAX_LOCATION_COUNT;")
      appendln("    final String locations1 = locationChunks[location1ChunkIndex];")
      appendln("    final String locations2 = locationChunks[location2ChunkIndex];")
      appendln("    final int offset1Low = locations1.charAt(2 * location1Index) & 0xffff;")
      appendln("    final int offset1High = locations1.charAt(2 * location1Index + 1) & 0xffff;")
      appendln("    final int offset1 = (offset1High << 16) | offset1Low;")
      appendln("    final int offset2Low = locations2.charAt(2 * location2Index);")
      appendln("    final int offset2High = locations2.charAt(2 * location2Index + 1);")
      appendln("    final int offset2 = (offset2High << 16) | offset2Low;")
      appendln("    final int length = offset2 - offset1;")
      appendln("    final char[] stringChars = new char[length];")
      appendln("    for (int i = 0; i < length; ++i) {")
      appendln("      final int offset = offset1 + i;")
      appendln("      final int indexChunkIndex = offset / $MAX_INDEX_COUNT;")
      appendln("      final int indexIndex = offset % $MAX_INDEX_COUNT;")
      appendln("      final String indexes = indexChunks[indexChunkIndex];")
      appendln("      final int index = indexes.charAt(indexIndex) & 0xffff;")
      appendln("      final int charChunkIndex = index / $MAX_CHAR_COUNT;")
      appendln("      final int charIndex = index % $MAX_CHAR_COUNT;")
      appendln("      final String chars = charChunks[charChunkIndex];")
      appendln("      stringChars[i] = chars.charAt(charIndex);")
      appendln("    }")
      appendln("    return new String(stringChars);")
      appendln("  }")
      appendln("}")
    }
  }

  companion object {
    // That's not a maximum string length allowed by JVM and not even a maximum length of a string literal.
    // This constant is an approximation of the maximum string length with arbitrary content that's safe to store
    // in a class file. Actually it should be 65535 / 6, where 65535 is a maximum length of the string literal and 6 is
    // the maximum length of UTF-8 character.
    private const val MAX_STRING_LENGTH = 8192

    private const val MAX_CHAR_COUNT = MAX_STRING_LENGTH
    private const val MAX_INDEX_COUNT = MAX_STRING_LENGTH
    private const val MAX_LOCATION_COUNT = MAX_STRING_LENGTH / 2

    private fun createStringLiteralsForChars(array: CharArray): List<String> {
      return createChunkList(array.size, MAX_CHAR_COUNT) { chunkStart, chunkEnd ->
        buildString {
          append("\"")
          for (index in chunkStart until chunkEnd) {
            append(array[index].toLiteral())
          }
          append("\"")
        }
      }
    }

    private fun createStringLiteralsForIndexes(array: ShortArray): List<String> {
      return createChunkList(array.size, MAX_INDEX_COUNT) { chunkStart, chunkEnd ->
        buildString {
          append("\"")
          for (index in chunkStart until chunkEnd) {
            append(array[index].toChar().toLiteral())
          }
          append("\"")
        }
      }
    }

    private fun createStringLiteralsForLocations(array: IntArray): List<String> {
      return createChunkList(array.size, MAX_LOCATION_COUNT) { chunkStart, chunkEnd ->
        buildString {
          append("\"")
          for (index in chunkStart until chunkEnd) {
            val int = array[index]
            val char1 = (int and 0xffff).toChar()
            val char2 = (int ushr 16).toChar()
            append(char1.toLiteral())
            append(char2.toLiteral())
          }
          append("\"")
        }
      }
    }

    private inline fun createChunkList(size: Int, chunkSize: Int, string: (Int, Int) -> String): List<String> {
      val strings = ArrayList<String>()
      forEachChunk(size, chunkSize) { chunkStart, chunkEnd ->
        strings += string(chunkStart, chunkEnd)
      }
      return strings
    }

    private inline fun forEachChunk(size: Int, chunkSize: Int, action: (Int, Int) -> Unit) {
      val chunkCount = (size + chunkSize - 1) / chunkSize
      repeat(chunkCount) { chunkIndex ->
        val chunkStart = chunkIndex * chunkSize
        val chunkEnd = minOf(chunkStart + chunkSize, size)
        action(chunkStart, chunkEnd)
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
