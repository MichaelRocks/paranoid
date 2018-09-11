package io.michaelrocks.paranoid.processor.io

import java.io.Closeable
import java.io.File

interface FileSink : Closeable {
    fun createFile(path: String, data: ByteArray)
    fun createDirectory(path: String)
    fun flush()

    interface Factory {
        fun createFileSink(inputFile: File, outputFile: File): FileSink
    }
}
