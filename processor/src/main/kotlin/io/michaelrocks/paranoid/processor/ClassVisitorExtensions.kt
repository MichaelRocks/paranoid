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

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

inline fun ClassVisitor.newMethod(access: Int, method: Method, body: GeneratorAdapter.() -> Unit) {
  GeneratorAdapter(access, method, null, null, this).apply {
    visitCode()
    body()
    returnValue()
    endMethod()
  }
}
