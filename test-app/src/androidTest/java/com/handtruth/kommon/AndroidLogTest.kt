package com.handtruth.kommon

import kotlin.test.Test
import kotlin.test.assertTrue

class AndroidLogTest {

    data class SomeData(val name: String, val id: Int, val float: Double)

    @Test
    fun useLog() {
        val datas = listOf(
            SomeData("Popka", 32, 56.98),
            SomeData("Kek", 34435, .0),
            SomeData("Very Long Name", 23, 2.2)
        )
        val log = Log.default(lvl = LogLevel.Debug)
        log.info { "it is working!" }
        log.debug(datas, SomeData::name, SomeData::id, SomeData::float) { "A table out" }
    }

    @Test
    fun testMark() {
        val builder = StringBuilder()
        val log = AppendableLog(builder, "TAG")
        log.info { "Hello World!".here }
        assertTrue(builder.toString().startsWith("TAG [info]: Hello World! at (AndroidLogTest.kt:"), builder.toString())
    }

}
