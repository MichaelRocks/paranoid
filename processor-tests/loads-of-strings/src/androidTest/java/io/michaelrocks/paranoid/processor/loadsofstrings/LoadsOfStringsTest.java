/*
 * Copyright 2019 Michael Rozumyanskiy
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

import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoadsOfStringsTest {
  @Test
  public void testLoadsOfStrings() {
    assertFalse(LoadsOfStrings.A.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.B.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.C.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.D.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.E.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.F.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.G.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.H.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.I.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.J.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.K.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.L.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.M.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.N.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.O.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.P.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.Q.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.R.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.S1.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.S2.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.T.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.U.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.V.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.W.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.X.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.Y.INSTANCE.getStrings().length == 0);
    assertFalse(LoadsOfStrings.Z.INSTANCE.getStrings().length == 0);
  }
}
