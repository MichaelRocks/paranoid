package io.michaelrocks.paranoid.processor.io

import java.io.File
import java.io.IOException
import java.util.jar.JarEntry
import java.util.jar.JarFile

internal class JarFileSource(private val jarFile: File) : FileSource {
    private val jar = JarFile(jarFile, true)

    override fun listFiles(callback: (String, FileSource.EntryType) -> Unit) {
        fun JarEntry.toEntryType() = when {
            isDirectory -> FileSource.EntryType.DIRECTORY
            name.endsWith(".class", ignoreCase = true) -> FileSource.EntryType.CLASS
            else -> FileSource.EntryType.FILE
        }

        for (entry in jar.entries()) {
            callback(entry.name, entry.toEntryType())
        }
    }

    override fun readFile(path: String): ByteArray {
        return jar.getJarEntry(path).let { entry ->
            jar.getInputStream(entry).use { stream -> stream.readBytes() }
        }
    }

    override fun close() {
        try {
            jar.close()
        } catch (exception: IOException) {
            // Ignore the exception.
        }
    }

    override fun toString(): String {
        return "JarFileSource($jarFile)"
    }
}
