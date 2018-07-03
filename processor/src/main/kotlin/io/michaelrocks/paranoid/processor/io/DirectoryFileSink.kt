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

import java.io.File

class DirectoryFileSink(private val directory: File) : FileSink {
  override fun createFile(path: String, data: ByteArray) {
    val file = File(directory, path)
    file.parentFile?.mkdirs()
    file.writeBytes(data)
  }

  override fun createDirectory(path: String) {
    File(directory, path).mkdirs()
  }

  override fun flush() {
  }

  override fun close() {
  }

  override fun toString(): String {
    return "DirectoryFileSink($directory)"
  }
}
