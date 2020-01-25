/*
 * Copyright 2020 Michael Rozumyanskiy
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

import io.michaelrocks.paranoid.DeobfuscatorHelper
import io.michaelrocks.paranoid.RandomHelper
import java.security.SecureRandom
import java.util.Random

interface StringRegistry {
  fun registerString(string: String): Long
  fun getAllChunks(): List<String>
}

class StringRegistryImpl(
  private val random: Random = SecureRandom()
) : StringRegistry {

  private val builder = StringBuilder()

  override fun registerString(string: String): Long {
    val seed = random.nextInt().toLong() and 0xffff_ffffL
    var mask = 0L
    var state = RandomHelper.seed(seed)
    state = RandomHelper.next(state)
    mask = mask or (state and 0xffff_0000_0000L)
    state = RandomHelper.next(state)
    mask = mask or ((state and 0xffff_0000_0000L) shl 16)
    val index = builder.length
    val id = seed or ((index.toLong() shl 32) xor mask)

    state = RandomHelper.next(state)
    builder.append((((state ushr 32) and 0xffffL) xor string.length.toLong()).toChar())

    for (char in string) {
      state = RandomHelper.next(state)
      builder.append((((state ushr 32) and 0xffffL) xor char.toLong()).toChar())
    }

    return id
  }

  override fun getAllChunks(): List<String> {
    return builder.toString().chunked(DeobfuscatorHelper.MAX_CHUNK_LENGTH)
  }
}
