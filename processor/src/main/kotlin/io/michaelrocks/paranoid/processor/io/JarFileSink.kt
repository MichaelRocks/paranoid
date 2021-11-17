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

package io.michaelrocks.paranoid.processor.io

import io.michaelrocks.paranoid.processor.commons.closeQuietly
import java.io.File
import java.nio.file.attribute.FileTime
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

internal class JarFileSink(private val jarFile: File) : FileSink {
  private val ZERO_FILE_TIME = FileTime.fromMillis(0L)
  private val stream = createJarOutputStream(jarFile)

  override fun createFile(path: String, data: ByteArray) {
    val entry = createJarEntryWithZeroTime(path)
    stream.putNextEntry(entry)
    stream.write(data)
    stream.closeEntry()
  }

  override fun createDirectory(path: String) {
    val directoryPath = if (path.endsWith("/")) path else "$path/"
    val entry = createJarEntryWithZeroTime(directoryPath)
    stream.putNextEntry(entry)
    stream.closeEntry()
  }

  override fun flush() {
    stream.flush()
  }

  override fun close() {
    stream.closeQuietly()
  }

  override fun toString(): String {
    return "JarFileSink($jarFile)"
  }

  private fun createJarOutputStream(jarFile: File): JarOutputStream {
    jarFile.parentFile?.mkdirs()
    return JarOutputStream(jarFile.outputStream().buffered())
  }

  // timestamps for files in jar in most cases are useless.
  // Set the modification/creation time to 0 to ensure that jars with same content
  // always have the same checksum. It's useful for reproducible builds
  private fun createJarEntryWithZeroTime(path: String): JarEntry {
    return JarEntry(path).apply {
      creationTime = ZERO_FILE_TIME
      lastAccessTime = ZERO_FILE_TIME
      lastModifiedTime = ZERO_FILE_TIME
    }
  }
}
