package com.handtruth.kommon.test

import com.handtruth.kommon.AppendableLog
import com.handtruth.kommon.Log
import kotlin.test.Test
import kotlin.test.assertEquals

class AppenderTest {

    @Test
    fun testRealAppender() {
        val builder = StringBuilder()
        val log: Log = AppendableLog(builder, "TAG")
        val info = log.info
        info.append("Hello")
        assertEquals("", builder.toString())
        info.append(" World\n AnotherLine\nAnd One More")
        assertEquals("""
            TAG [info]: Hello World
            TAG [info]:  AnotherLine
""".trimIndent() + '\n', builder.toString())
        builder.clear()
        info.append("There is\n world", 5, 8)
        assertEquals("", builder.toString())
        info.append('3')
        assertEquals("", builder.toString())
        info.append('\n')
        assertEquals("TAG [info]: And One More is3\n", builder.toString())
    }

    @Test
    fun testFakeAppender() {
        val builder = StringBuilder()
        val log: Log = AppendableLog(builder, "TAG")
        val debug = log.debug
        debug.append('\n')
        assertEquals("", builder.toString())
        debug.append("Hello\n")
        assertEquals("", builder.toString())
        debug.append("Bye\n World", 0, 4)
        assertEquals("", builder.toString())
    }

}
