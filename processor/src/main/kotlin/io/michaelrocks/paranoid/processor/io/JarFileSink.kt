package io.michaelrocks.paranoid.processor.io

import io.michaelrocks.paranoid.processor.commons.closeQuietly
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

internal class JarFileSink(private val jarFile: File) : FileSink {
    private val stream = createJarOutputStream(jarFile)

    override fun createFile(path: String, data: ByteArray) {
        val entry = JarEntry(path)
        stream.putNextEntry(entry)
        stream.write(data)
        stream.closeEntry()
    }

    override fun createDirectory(path: String) {
        val directoryPath = if (path.endsWith("/")) path else "$path/"
        val entry = JarEntry(directoryPath)
        stream.putNextEntry(entry)
        stream.closeEntry()
    }

    override fun flush() {
        stream.flush()
    }

    override fun close() {
        stream.closeQuietly()
    }

    override fun toString(): String {
        return "JarFileSink($jarFile)"
    }

    private fun createJarOutputStream(jarFile: File): JarOutputStream {
        jarFile.parentFile?.mkdirs()
        return JarOutputStream(jarFile.outputStream().buffered())
    }
}
