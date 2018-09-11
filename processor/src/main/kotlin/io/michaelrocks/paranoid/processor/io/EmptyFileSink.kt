package io.michaelrocks.paranoid.processor.io

object EmptyFileSink : FileSink {
    override fun createFile(path: String, data: ByteArray) {
        throw UnsupportedOperationException()
    }

    override fun createDirectory(path: String) {
        throw UnsupportedOperationException()
    }

    override fun flush() {
    }

    override fun close() {
    }

    override fun toString(): String {
        return "EmptyFileSink"
    }
}
