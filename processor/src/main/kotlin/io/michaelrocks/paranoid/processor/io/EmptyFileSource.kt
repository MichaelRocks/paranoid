package io.michaelrocks.paranoid.processor.io

object EmptyFileSource : FileSource {
    override fun listFiles(callback: (String, FileSource.EntryType) -> Unit) {
    }

    override fun readFile(path: String): ByteArray {
        throw UnsupportedOperationException()
    }

    override fun close() {
    }

    override fun toString(): String {
        return "EmptyFileSource"
    }
}
