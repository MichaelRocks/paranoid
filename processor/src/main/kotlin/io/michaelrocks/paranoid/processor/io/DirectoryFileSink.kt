package io.michaelrocks.paranoid.processor.io

import java.io.File

class DirectoryFileSink(private val directory: File) : FileSink {
    override fun createFile(path: String, data: ByteArray) {
        val file = File(directory, path)
        file.parentFile?.mkdirs()
        file.writeBytes(data)
    }

    override fun createDirectory(path: String) {
        File(directory, path).mkdirs()
    }

    override fun flush() {
    }

    override fun close() {
    }

    override fun toString(): String {
        return "DirectoryFileSink($directory)"
    }
}
