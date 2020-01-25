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

package io.michaelrocks.paranoid;

public class RandomHelper {
  private RandomHelper() {
    // Cannot be instantiated.
  }

  public static long seed(final long x) {
    final long z = (x ^ (x >>> 33)) * 0x62a9d9ed799705f5L;
    return ((z ^ (z >>> 28)) * 0xcb24d0a5c88c35b3L) >>> 32;
  }

  public static long next(final long state) {
    short s0 = (short) (state & 0xffff);
    short s1 = (short) ((state >>> 16) & 0xffff);
    short next = s0;
    next += s1;
    next = rotl(next, 9);
    next += s0;

    s1 ^= s0;
    s0 = rotl(s0, 13);
    s0 ^= s1;
    s0 ^= (s1 << 5);
    s1 = rotl(s1, 10);

    long result = next;
    result <<= 16;
    result |= s1;
    result <<= 16;
    result |= s0;
    return result;
  }

  private static short rotl(final short x, final int k) {
    return (short) ((x << k) | (x >>> (32 - k)));
  }
}
