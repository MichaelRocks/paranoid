package io.michaelrocks.paranoid.processor.commons

import java.io.Closeable
import java.io.IOException

fun Closeable.closeQuietly() {
    try {
        close()
    } catch (exception: IOException) {
        // Ignore the exception.
    }
}
