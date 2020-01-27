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

package io.michaelrocks.paranoid.processor.loadsofstrings;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoadsOfStringsTest {
  @Test
  public void testLoadsOfStrings() {
    assertEquals(3479, LoadsOfStrings.A.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.A.INSTANCE.getStrings(), 'a');
    assertArraySorted(LoadsOfStrings.A.INSTANCE.getStrings());

    assertEquals(3210, LoadsOfStrings.B.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.B.INSTANCE.getStrings(), 'b');
    assertArraySorted(LoadsOfStrings.B.INSTANCE.getStrings());

    assertEquals(5493, LoadsOfStrings.C.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.C.INSTANCE.getStrings(), 'c');
    assertArraySorted(LoadsOfStrings.C.INSTANCE.getStrings());

    assertEquals(3776, LoadsOfStrings.D.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.D.INSTANCE.getStrings(), 'd');
    assertArraySorted(LoadsOfStrings.D.INSTANCE.getStrings());

    assertEquals(2588, LoadsOfStrings.E.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.E.INSTANCE.getStrings(), 'e');
    assertArraySorted(LoadsOfStrings.E.INSTANCE.getStrings());

    assertEquals(2557, LoadsOfStrings.F.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.F.INSTANCE.getStrings(), 'f');
    assertArraySorted(LoadsOfStrings.F.INSTANCE.getStrings());

    assertEquals(1836, LoadsOfStrings.G.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.G.INSTANCE.getStrings(), 'g');
    assertArraySorted(LoadsOfStrings.G.INSTANCE.getStrings());

    assertEquals(2026, LoadsOfStrings.H.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.H.INSTANCE.getStrings(), 'h');
    assertArraySorted(LoadsOfStrings.H.INSTANCE.getStrings());

    assertEquals(2673, LoadsOfStrings.I.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.I.INSTANCE.getStrings(), 'i');
    assertArraySorted(LoadsOfStrings.I.INSTANCE.getStrings());

    assertEquals(473, LoadsOfStrings.J.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.J.INSTANCE.getStrings(), 'j');
    assertArraySorted(LoadsOfStrings.J.INSTANCE.getStrings());

    assertEquals(354, LoadsOfStrings.K.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.K.INSTANCE.getStrings(), 'k');
    assertArraySorted(LoadsOfStrings.K.INSTANCE.getStrings());

    assertEquals(1837, LoadsOfStrings.L.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.L.INSTANCE.getStrings(), 'l');
    assertArraySorted(LoadsOfStrings.L.INSTANCE.getStrings());

    assertEquals(2944, LoadsOfStrings.M.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.M.INSTANCE.getStrings(), 'm');
    assertArraySorted(LoadsOfStrings.M.INSTANCE.getStrings());

    assertEquals(919, LoadsOfStrings.N.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.N.INSTANCE.getStrings(), 'n');
    assertArraySorted(LoadsOfStrings.N.INSTANCE.getStrings());

    assertEquals(1388, LoadsOfStrings.O.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.O.INSTANCE.getStrings(), 'o');
    assertArraySorted(LoadsOfStrings.O.INSTANCE.getStrings());

    assertEquals(4563, LoadsOfStrings.P.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.P.INSTANCE.getStrings(), 'p');
    assertArraySorted(LoadsOfStrings.P.INSTANCE.getStrings());

    assertEquals(290, LoadsOfStrings.Q.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.Q.INSTANCE.getStrings(), 'q');
    assertArraySorted(LoadsOfStrings.Q.INSTANCE.getStrings());

    assertEquals(3634, LoadsOfStrings.R.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.R.INSTANCE.getStrings(), 'r');
    assertArraySorted(LoadsOfStrings.R.INSTANCE.getStrings());

    assertEquals(3335, LoadsOfStrings.S1.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.S1.INSTANCE.getStrings(), 's');
    assertArraySorted(LoadsOfStrings.S1.INSTANCE.getStrings());

    assertEquals(3335, LoadsOfStrings.S2.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.S2.INSTANCE.getStrings(), 's');
    assertArraySorted(LoadsOfStrings.S2.INSTANCE.getStrings());

    assertEquals(2881, LoadsOfStrings.T.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.T.INSTANCE.getStrings(), 't');
    assertArraySorted(LoadsOfStrings.T.INSTANCE.getStrings());

    assertEquals(1921, LoadsOfStrings.U.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.U.INSTANCE.getStrings(), 'u');
    assertArraySorted(LoadsOfStrings.U.INSTANCE.getStrings());

    assertEquals(811, LoadsOfStrings.V.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.V.INSTANCE.getStrings(), 'v');
    assertArraySorted(LoadsOfStrings.V.INSTANCE.getStrings());

    assertEquals(1542, LoadsOfStrings.W.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.W.INSTANCE.getStrings(), 'w');
    assertArraySorted(LoadsOfStrings.W.INSTANCE.getStrings());

    assertEquals(14, LoadsOfStrings.X.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.X.INSTANCE.getStrings(), 'x');
    assertArraySorted(LoadsOfStrings.X.INSTANCE.getStrings());

    assertEquals(144, LoadsOfStrings.Y.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.Y.INSTANCE.getStrings(), 'y');
    assertArraySorted(LoadsOfStrings.Y.INSTANCE.getStrings());

    assertEquals(86, LoadsOfStrings.Z.INSTANCE.getStrings().length);
    assertArrayItemsStartWith(LoadsOfStrings.Z.INSTANCE.getStrings(), 'z');
    assertArraySorted(LoadsOfStrings.Z.INSTANCE.getStrings());
  }

  private static void assertArrayItemsStartWith(final String[] array, final char c) {
    for (int i = 0; i < array.length - 1; ++i) {
      if (array[i].charAt(0) != c) {
        fail("String \"" + array[i] + "\" at index " + i + " doesn't start with '" + c + "'");
      }
    }
  }

  private static void assertArraySorted(final String[] array) {
    for (int i = 0; i < array.length - 1; ++i) {
      if (array[i].compareTo(array[i + 1]) >= 0) {
        fail("String \"" + array[i] + "\" at index " + i + " is greater or equal to string \"" + array[i + 1] + "\" at index " + (i + 1));
      }
    }
  }
}
