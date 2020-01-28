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
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

class StringConstantsClassPatcher(
  private val configuration: ClassConfiguration,
  delegate: ClassVisitor
) : ClassVisitor(ASM5, delegate) {

  private val logger = getLogger()

  private var isStaticInitializerPatched = false

  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<out String>?
  ) {
    super.visit(version, access, name, signature, superName, interfaces)
    isStaticInitializerPatched = configuration.constantStringsByFieldName.isEmpty()
  }

  override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? {
    val newValue = if (name in configuration.constantStringsByFieldName) null else value
    return super.visitField(access, name, desc, signature, newValue)
  }

  override fun visitMethod(
    access: Int,
    name: String,
    desc: String,
    signature: String?,
    exceptions: Array<out String>?
  ): MethodVisitor {
    val visitor = super.visitMethod(access, name, desc, signature, exceptions)
    if (name == STATIC_INITIALIZER_METHOD.name) {
      return createStaticInitializerPatcher(visitor, access, name, desc)
    } else {
      return visitor
    }
  }

  override fun visitEnd() {
    if (!isStaticInitializerPatched) {
      GeneratorAdapter(ACC_PRIVATE or ACC_STATIC, STATIC_INITIALIZER_METHOD, null, null, this).apply {
        visitCode()
        returnValue()
        endMethod()
      }
    }

    super.visitEnd()
  }

  private fun createStaticInitializerPatcher(
    visitor: MethodVisitor,
    access: Int,
    name: String,
    desc: String
  ): MethodVisitor {
    if (!isStaticInitializerPatched) {
      isStaticInitializerPatched = true
      return object : GeneratorAdapter(ASM5, visitor, access, name, desc) {
        override fun visitCode() {
          logger.info("{}:", configuration.container.internalName)
          logger.info("  Patching <clinit>...")
          super.visitCode()
          for ((field, value) in configuration.constantStringsByFieldName) {
            push(value)
            putStatic(configuration.container.toAsmType(), field, STRING_TYPE)
          }
        }
      }
    } else {
      return visitor
    }
  }

  companion object {
    private val STATIC_INITIALIZER_METHOD = Method("<clinit>", Type.VOID_TYPE, arrayOf())
    private val STRING_TYPE = Type.getType(String::class.java)
  }
}
