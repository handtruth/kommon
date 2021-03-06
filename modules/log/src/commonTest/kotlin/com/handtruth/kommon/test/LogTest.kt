package com.handtruth.kommon.test

import com.handtruth.kommon.AppendableLog
import com.handtruth.kommon.FatalError
import com.handtruth.kommon.Log
import com.handtruth.kommon.LogLevel
import kotlin.test.*

class LogTest {

    @Test
    fun testLogObject() {
        val builder = StringBuilder()
        val log: Log = AppendableLog(builder, "TAG")
        log.debug { "debug" }
        assertEquals("", builder.toString())
        log.verbose { "verbose" }
        assertEquals("", builder.toString())
        log.info { "info" }
        assertEquals("TAG [info]: info\n", builder.toString())
        builder.clear()
        log.warning { "warning" }
        assertEquals("TAG [warning]: warning\n", builder.toString())
        builder.clear()
        log.error { "error" }
        assertEquals("TAG [error]: error\n", builder.toString())
        builder.clear()
        assertFailsWith<FatalError> {
            log.fatal { "fatal" }
        }
        assertEquals("TAG [fatal]: fatal\n", builder.toString())
    }

    @Test
    fun testLogThrowable() {
        val builder = StringBuilder()
        val log: Log = AppendableLog(builder, "TAG", LogLevel.Warning)
        val e = Exception()
        val strE = e.toString()
        log.debug(e) { "debug" }
        assertEquals("", builder.toString())
        log.verbose(e) { "verbose" }
        assertEquals("", builder.toString())
        log.info(e) { "info" }
        assertEquals("", builder.toString())
        log.warning(e) { "warning" }
        fun sukaBlyatSlozno(): String {
            return builder.toString().split('\n', '\r').find { it.isNotEmpty() }!!
        }
        assertEquals("TAG [warning]: warning: $strE", sukaBlyatSlozno())
        builder.clear()
        log.error(e) { "error" }
        assertEquals("TAG [error]: error: $strE", sukaBlyatSlozno())
        builder.clear()
        assertFailsWith<FatalError> {
            log.fatal(e) { "fatal" }
        }
        assertEquals("TAG [fatal]: fatal: $strE", sukaBlyatSlozno())
    }
}
