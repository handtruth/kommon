package com.handtruth.kommon

import kotlin.test.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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

}
