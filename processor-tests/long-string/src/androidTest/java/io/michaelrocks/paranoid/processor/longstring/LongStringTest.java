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

package io.michaelrocks.paranoid.processor.longstring;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LongStringTest {
  @Test
  public void testLongString() {
    assertEquals(65535, LongString.INSTANCE.getString().length());
    assertTrue(LongString.INSTANCE.getString().startsWith("Lorem ipsum dolor sit amet, consectetur adipiscing elit."));
    assertTrue(LongString.INSTANCE.getString().endsWith("Mauris eget ligula sit amet odio fringilla ultricies rutrum."));
    assertTrue(LongString.INSTANCE.getString().contains("Pellentesque iaculis est quis massa faucibus, a volutpat magna sodales."));
  }
}
