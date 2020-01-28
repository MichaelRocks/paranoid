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

package io.michaelrocks.paranoid.processor.subproject

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import io.michaelrocks.paranoid.Obfuscate
import io.michaelrocks.paranoid.processor.subproject.android.AndroidConstants
import io.michaelrocks.paranoid.processor.subproject.java.JavaConstants

@Obfuscate
class MainActivity : Activity() {

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val textView = TextView(this)
    setContentView(textView)
    textView.text = "Subprojects: ${AndroidConstants.getName()}, ${JavaConstants.getName()}"
  }
}
