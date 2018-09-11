package io.michaelrocks.paranoid.processor

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class RemoveObfuscateClassPatcher(
        delegate: ClassVisitor
) : ClassVisitor(Opcodes.ASM5, delegate) {
    private val obfuscateDescriptor = OBFUSCATE_TYPE.descriptor

    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
        return if (obfuscateDescriptor != desc) super.visitAnnotation(desc, visible) else null
    }
}
