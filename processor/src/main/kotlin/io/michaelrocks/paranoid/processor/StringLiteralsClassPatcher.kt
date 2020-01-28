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

import io.michaelrocks.grip.mirrors.toAsmType
import io.michaelrocks.paranoid.processor.logging.getLogger
import io.michaelrocks.paranoid.processor.model.Deobfuscator
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.commons.GeneratorAdapter

class StringLiteralsClassPatcher(
  private val deobfuscator: Deobfuscator,
  private val stringRegistry: StringRegistry,
  delegate: ClassVisitor
) : ClassVisitor(ASM5, delegate) {

  private val logger = getLogger()

  private var className: String = ""

  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<out String>?
  ) {
    super.visit(version, access, name, signature, superName, interfaces)
    className = name
  }

  override fun visitMethod(
    access: Int,
    name: String,
    desc: String,
    signature: String?,
    exceptions: Array<out String>?
  ): MethodVisitor {
    val visitor = super.visitMethod(access, name, desc, signature, exceptions)
    return object : GeneratorAdapter(ASM5, visitor, access, name, desc) {
      override fun visitLdcInsn(constant: Any) {
        if (constant is String) {
          replaceStringWithDeobfuscationMethod(constant)
        } else {
          super.visitLdcInsn(constant)
        }
      }

      private fun replaceStringWithDeobfuscationMethod(string: String) {
        logger.info("{}.{}{}:", className, name, desc)
        logger.info("  Obfuscating string literal: \"{}\"", string)
        val stringId = stringRegistry.registerString(string)
        push(stringId)
        invokeStatic(deobfuscator.type.toAsmType(), deobfuscator.deobfuscationMethod)
      }
    }
  }
}
