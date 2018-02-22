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

import java.util.HashMap

interface StringRegistry {
  fun registerString(string: String): Int
  fun getAllIds(): Collection<Int>
  fun getAllStrings(): Collection<String>
  fun findStringById(id: Int): String
}

class StringRegistryImpl : StringRegistry {
  private val stringsById = HashMap<Int, String>()
  private var lastId: Int = -1

  override fun registerString(string: String): Int {
    val id = ++lastId
    stringsById[id] = string
    return id
  }

  override fun getAllIds(): Collection<Int> {
    return stringsById.keys.toList()
  }

  override fun getAllStrings(): Collection<String> {
    return stringsById.values.toList()
  }

  override fun findStringById(id: Int): String {
    return stringsById[id]!!
  }
}