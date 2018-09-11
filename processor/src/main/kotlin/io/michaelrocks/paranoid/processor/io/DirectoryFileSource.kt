package io.michaelrocks.paranoid.processor.io

import java.io.File

internal class DirectoryFileSource(private val directory: File) : FileSource {
    override fun listFiles(callback: (String, FileSource.EntryType) -> Unit) {
        fun File.toEntryType() = when {
            isDirectory -> FileSource.EntryType.DIRECTORY
            name.endsWith(".class", ignoreCase = true) -> FileSource.EntryType.CLASS
            else -> FileSource.EntryType.FILE
        }

        for (file in directory.walkTopDown()) {
            callback(file.relativeTo(directory).path, file.toEntryType())
        }
    }

    override fun readFile(path: String): ByteArray = File(directory, path).readBytes()

    override fun close() {
    }

    override fun toString(): String {
        return "DirectoryFileSource($directory)"
    }
}
