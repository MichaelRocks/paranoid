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

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.toAsmType
import io.michaelrocks.paranoid.processor.model.Deobfuscator
import jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC
import jdk.internal.org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

class DeobfuscatorGenerator(
  private val deobfuscator: Deobfuscator,
  private val stringRegistry: StringRegistry,
  private val classRegistry: ClassRegistry
) {

  fun generateDeobfuscator(): ByteArray {
    val writer = StandaloneClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, classRegistry)
    writer.visit(
      Opcodes.V1_6,
      ACC_PUBLIC or ACC_SUPER,
      deobfuscator.type.internalName,
      null,
      OBJECT_TYPE.internalName,
      null
    )

    writer.generateFields()
    writer.generateStaticInitializer()
    writer.generateDefaultConstructor()
    writer.generateGetStringMethod()

    writer.visitEnd()
    return writer.toByteArray()
  }

  private fun ClassVisitor.generateFields() {
    visitField(
      Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL,
      CHUNKS_FIELD_NAME,
      CHUNKS_FIELD_TYPE.descriptor,
      null,
      null
    ).apply {
      visitEnd()
    }
  }

  private fun ClassVisitor.generateStaticInitializer() {
    newMethod(Opcodes.ACC_STATIC, METHOD_STATIC_INITIALIZER) {
      val chunks = stringRegistry.getAllChunks()
      push(chunks.size)
      newArray(CHUNKS_ELEMENT_TYPE)
      putStatic(deobfuscator.type.toAsmType(), CHUNKS_FIELD_NAME, CHUNKS_FIELD_TYPE)

      getStatic(deobfuscator.type.toAsmType(), CHUNKS_FIELD_NAME, CHUNKS_FIELD_TYPE)
      chunks.forEachIndexed { index, chunk ->
        dup()
        push(index)
        push(chunk)
        arrayStore(CHUNKS_ELEMENT_TYPE)
      }
      pop()
    }
  }

  private fun ClassVisitor.generateDefaultConstructor() {
    newMethod(Opcodes.ACC_PUBLIC, METHOD_DEFAULT_CONSTRUCTOR) {
      loadThis()
      invokeConstructor(TYPE_OBJECT, METHOD_DEFAULT_CONSTRUCTOR)
    }
  }

  private fun ClassVisitor.generateGetStringMethod() {
    newMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, deobfuscator.deobfuscationMethod) {
      loadArg(0)
      getStatic(deobfuscator.type.toAsmType(), CHUNKS_FIELD_NAME, CHUNKS_FIELD_TYPE)
      invokeStatic(DEOBFUSCATOR_HELPER_TYPE.toAsmType(), METHOD_GET_STRING)
    }
  }

  companion object {
    private val METHOD_STATIC_INITIALIZER = Method("<clinit>", "()V")
    private val METHOD_DEFAULT_CONSTRUCTOR = Method("<init>", "()V")

    private val METHOD_GET_STRING = Method("getString", "(J[Ljava/lang/String;)Ljava/lang/String;")

    private val TYPE_OBJECT = Type.getObjectType("java/lang/Object")

    private const val CHUNKS_FIELD_NAME = "chunks"
    private val CHUNKS_FIELD_TYPE = Type.getType("[Ljava/lang/String;")
    private val CHUNKS_ELEMENT_TYPE = CHUNKS_FIELD_TYPE.elementType
  }
}
