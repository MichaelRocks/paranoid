package io.michaelrocks.paranoid.processor.model

import io.michaelrocks.grip.mirrors.Type
import org.objectweb.asm.commons.Method

data class Deobfuscator(
        val type: Type.Object,
        val deobfuscationMethod: Method
)
