package com.handtruth.kommon

data class Mark(val file: String, val line: Int) {
    override fun toString() = "($file:$line)"
    companion object
}

expect fun Mark.Companion.here(offset: Int = 0): Mark
