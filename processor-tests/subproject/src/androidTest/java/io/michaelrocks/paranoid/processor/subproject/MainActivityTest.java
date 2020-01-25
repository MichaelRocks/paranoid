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

package io.michaelrocks.paranoid.processor.subproject;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
  @Test
  public void testMainActivityIsProcessed() throws Exception {
    final String suffix = BuildConfig.DEBUG ? "Debug" : "Release";
    final Class<?> deobfuscatorClass = Class.forName("io.michaelrocks.paranoid.Deobfuscator$processortests$subproject$" + suffix);
    final Field chunksField = deobfuscatorClass.getDeclaredField("chunks");
    chunksField.setAccessible(true);
    final String[] chunks = (String[]) chunksField.get(null);

    assertNotNull(chunks);
    assertEquals(1, chunks.length);
    assertTrue(chunks[0].length() > "Subprojects: ".length() + "android".length() + "java".length());
  }
}
