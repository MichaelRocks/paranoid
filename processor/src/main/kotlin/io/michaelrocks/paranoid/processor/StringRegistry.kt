package io.michaelrocks.paranoid.processor

import java.util.HashMap

interface StringRegistry {
    fun registerString(string: String): Int
    fun getAllIds(): Collection<Int>
    fun getAllStrings(): Collection<String>
    fun findStringById(id: Int): String
}

class StringRegistryImpl : StringRegistry {
    private val stringsById = HashMap<Int, String>()
    private var lastId: Int = -1

    override fun registerString(string: String): Int {
        val id = ++lastId
        stringsById[id] = string
        return id
    }

    override fun getAllIds(): Collection<Int> {
        return stringsById.keys.toList()
    }

    override fun getAllStrings(): Collection<String> {
        return stringsById.values.toList()
    }

    override fun findStringById(id: Int): String {
        return stringsById[id]!!
    }
}