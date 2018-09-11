package io.michaelrocks.paranoid.processor.io

import java.io.Closeable
import java.io.File

interface FileSource : Closeable {
    fun listFiles(callback: (name: String, type: EntryType) -> Unit)
    fun readFile(path: String): ByteArray

    enum class EntryType { CLASS, FILE, DIRECTORY }

    interface Factory {
        fun createFileSource(inputFile: File): FileSource
    }
}
