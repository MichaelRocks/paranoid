package io.michaelrocks.paranoid.processor.io

import java.io.File

object IoFactory : FileSource.Factory, FileSink.Factory {
    override fun createFileSource(inputFile: File): FileSource =
            inputFile.run {
                when (fileType) {
                    FileType.EMPTY -> EmptyFileSource
                    FileType.DIRECTORY -> DirectoryFileSource(this)
                    FileType.JAR -> JarFileSource(this)
                }
            }

    override fun createFileSink(inputFile: File, outputFile: File): FileSink =
            outputFile.run {
                when (inputFile.fileType) {
                    FileType.EMPTY -> EmptyFileSink
                    FileType.DIRECTORY -> DirectoryFileSink(this)
                    FileType.JAR -> JarFileSink(this)
                }
            }

    private val File.fileType: FileType
        get() = when {
            !exists() || isDirectory -> FileType.DIRECTORY
            extension.endsWith("jar", ignoreCase = true) -> FileType.JAR
            else -> error("Unknown file type for file $this")
        }

    private enum class FileType { EMPTY, DIRECTORY, JAR }
}
